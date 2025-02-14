/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.ml.inference.pytorch.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.OriginSettingClient;
import org.elasticsearch.xcontent.NamedXContentRegistry;
import org.elasticsearch.xpack.ml.inference.persistence.ChunkedTrainedModelRestorer;
import org.elasticsearch.xpack.ml.inference.persistence.TrainedModelDefinitionDoc;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static org.elasticsearch.xpack.core.ClientHelper.ML_ORIGIN;

/**
 * PyTorch models in the TorchScript format are binary files divided
 * into small chunks and base64 encoded for storage in Elasticsearch.
 * The model is restored by base64 decoding the stored state and streaming
 * the binary objects concatenated in order. There is no delineation between
 * individual chunks the state should appear as one contiguous file.
 */
public class PyTorchStateStreamer {

    private static final Logger logger = LogManager.getLogger(PyTorchStateStreamer.class);

    private final OriginSettingClient client;
    private final ExecutorService executorService;
    private final NamedXContentRegistry xContentRegistry;
    private volatile boolean isCancelled;
    private int modelSize = -1;

    public PyTorchStateStreamer(Client client, ExecutorService executorService, NamedXContentRegistry xContentRegistry) {
        this.client = new OriginSettingClient(Objects.requireNonNull(client), ML_ORIGIN);
        this.executorService = Objects.requireNonNull(executorService);
        this.xContentRegistry = Objects.requireNonNull(xContentRegistry);
    }

    /**
     * Cancels the state streaming at the first opportunity.
     */
    public void cancel() {
        isCancelled = true;
    }

    /**
     * First writes the size of the model so the native process can
     * allocated memory then writes the chunks of binary state.
     *
     * @param modelId  The model to write
     * @param index    The index to search for the model
     * @param restoreStream The stream to write to
     * @param listener  error and success listener
     */
    public void writeStateToStream(String modelId, String index, OutputStream restoreStream, ActionListener<Boolean> listener) {
        ChunkedTrainedModelRestorer restorer = new ChunkedTrainedModelRestorer(modelId, client, executorService, xContentRegistry);
        restorer.setSearchIndex(index);
        restorer.setSearchSize(1);
        restorer.restoreModelDefinition(
            doc -> writeChunk(doc, restoreStream),
            success -> {
                logger.debug("model [{}] state restored in [{}] documents from index [{}]", modelId, restorer.getNumDocsWritten(), index);
                listener.onResponse(success);
            },
            listener::onFailure
        );
    }

    private boolean writeChunk(TrainedModelDefinitionDoc doc, OutputStream outputStream) throws IOException {
        if (isCancelled) {
            return false;
        }

        if (modelSize == -1) {
            modelSize = writeModelSize(doc.getModelId(), doc.getTotalDefinitionLength(), outputStream);
        }

        // The array backing the BytesReference may be bigger than what is
        // referred to so write only what is after the offset
        outputStream.write(doc.getBinaryData().array(), doc.getBinaryData().arrayOffset(), doc.getBinaryData().length());
        return true;
    }

    private int writeModelSize(String modelId, Long modelSizeBytes, OutputStream outputStream) throws IOException {
        if (modelSizeBytes == null) {
            String message = String.format(Locale.ROOT,
                "The definition doc for model [%s] has a null value for field [%s]",
                modelId, TrainedModelDefinitionDoc.TOTAL_DEFINITION_LENGTH.getPreferredName());
            logger.error(message);
            throw new IllegalStateException(message);
        }
        if (modelSizeBytes <= 0) {
            // The other end expects an unsigned 32 bit int a -ve value is invalid.
            // ByteSizeValue allows -1 bytes as a valid value so this check is still required
            String message = String.format(Locale.ROOT,
                "The definition doc for model [%s] has a negative value [%s] for field [%s]",
                modelId,
                modelSizeBytes,
                TrainedModelDefinitionDoc.TOTAL_DEFINITION_LENGTH.getPreferredName());

            logger.error(message);
            throw new IllegalStateException(message);
        }

        if (modelSizeBytes > Integer.MAX_VALUE) {
            // TODO use a long in case models are larger than 2^31 bytes
            String message = String.format(Locale.ROOT,
                "model [%s] has a size [%s] larger than the max size [%s]",
                modelId, modelSizeBytes, Integer.MAX_VALUE);
            logger.error(message);
            throw new IllegalStateException(message);
        }

        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(modelSizeBytes.intValue());
        outputStream.write(lengthBuffer.array());

        return modelSizeBytes.intValue();
    }
}
