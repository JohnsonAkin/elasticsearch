/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.client.watcher;

import org.elasticsearch.client.NodesResponseHeader;
import org.elasticsearch.client.NodesResponseHeaderTestUtils;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.test.ESTestCase;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.elasticsearch.test.AbstractXContentTestCase.xContentTester;

public class WatcherStatsResponseTests extends ESTestCase {

    public void testFromXContent() throws IOException {
        xContentTester(
            this::createParser,
            this::createTestInstance,
            this::toXContent,
            WatcherStatsResponse::fromXContent)
            .supportsUnknownFields(true)
            .randomFieldsExcludeFilter(field -> field.endsWith("stats"))
            .test();
    }

    private void toXContent(WatcherStatsResponse response, XContentBuilder builder) throws IOException {
        builder.startObject();
        NodesResponseHeaderTestUtils.toXContent(response.getHeader(), response.getClusterName(), builder);
        toXContent(response.getWatcherMetadata(), builder);
        builder.startArray("stats");
        for (WatcherStatsResponse.Node node : response.getNodes()) {
            toXContent(node, builder);
        }
        builder.endArray();
        builder.endObject();
    }

    private void toXContent(WatcherMetadata metadata, XContentBuilder builder) throws IOException {
        builder.field("manually_stopped", metadata.manuallyStopped());
    }

    private void toXContent(WatcherStatsResponse.Node node, XContentBuilder builder) throws IOException {
        builder.startObject();
        builder.field("node_id", node.getNodeId());
        builder.field("watcher_state", node.getWatcherState().toString().toLowerCase(Locale.ROOT));
        builder.field("watch_count", node.getWatchesCount());
        builder.startObject("execution_thread_pool");
        builder.field("queue_size", node.getThreadPoolQueueSize());
        builder.field("max_size", node.getThreadPoolMaxSize());
        builder.endObject();

        if (node.getSnapshots() != null) {
            builder.startArray("current_watches");
            for (WatchExecutionSnapshot snapshot : node.getSnapshots()) {
                toXContent(snapshot, builder);
            }
            builder.endArray();
        }
        if (node.getQueuedWatches() != null) {
            builder.startArray("queued_watches");
            for (QueuedWatch queuedWatch : node.getQueuedWatches()) {
                toXContent(queuedWatch, builder);
            }
            builder.endArray();
        }
        if (node.getStats() != null) {
            builder.field("stats", node.getStats());
        }
        builder.endObject();
    }

    private void toXContent(WatchExecutionSnapshot snapshot, XContentBuilder builder) throws IOException {
        builder.startObject();
        builder.field("watch_id", snapshot.getWatchId());
        builder.field("watch_record_id", snapshot.getWatchRecordId());
        builder.timeField("triggered_time", snapshot.getTriggeredTime());
        builder.timeField("execution_time", snapshot.getExecutionTime());
        builder.field("execution_phase", snapshot.getPhase());
        if (snapshot.getExecutedActions() != null) {
            builder.startArray("executed_actions");
            for (String executedAction : snapshot.getExecutedActions()) {
                builder.value(executedAction);
            }
            builder.endArray();
        }
        if (snapshot.getExecutionStackTrace() != null) {
            builder.startArray("stack_trace");
            for (String element : snapshot.getExecutionStackTrace()) {
                builder.value(element);
            }
            builder.endArray();
        }
        builder.endObject();
    }

    private void toXContent(QueuedWatch queuedWatch, XContentBuilder builder) throws IOException {
        builder.startObject();
        builder.field("watch_id", queuedWatch.getWatchId());
        builder.field("watch_record_id", queuedWatch.getWatchRecordId());
        builder.timeField("triggered_time", queuedWatch.getTriggeredTime());
        builder.timeField("execution_time", queuedWatch.getExecutionTime());
        builder.endObject();
    }

    protected WatcherStatsResponse createTestInstance() {
        int nodeCount = randomInt(10);
        List<WatcherStatsResponse.Node> nodes = new ArrayList<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            List<WatchExecutionSnapshot> snapshots = null;
            if (randomBoolean()) {
                int snapshotCount = randomInt(10);
                snapshots = new ArrayList<>(snapshotCount);

                for (int j = 0; j < snapshotCount; j++) {
                    String[] actions = null;
                    if (randomBoolean()) {
                        actions = new String[randomInt(10)];
                        for (int k = 0; k < actions.length; k++) {
                            actions[k] = randomAlphaOfLength(10);
                        }
                    }
                    String[] stackTrace = null;
                    if (randomBoolean()) {
                        stackTrace = new String[randomInt(10)];
                        for (int k = 0; k < stackTrace.length; k++) {
                            stackTrace[k] = randomAlphaOfLength(10);
                        }
                    }
                    snapshots.add(new WatchExecutionSnapshot(randomAlphaOfLength(10), randomAlphaOfLength(10),
                        ZonedDateTime.ofInstant(Instant.ofEpochMilli(randomInt()), ZoneOffset.UTC),
                        ZonedDateTime.ofInstant(Instant.ofEpochMilli(randomInt()), ZoneOffset.UTC),
                        randomFrom(ExecutionPhase.values()), actions, stackTrace));
                }
            }

            List<QueuedWatch> queuedWatches = null;
            if(randomBoolean()) {
                int queuedWatchCount = randomInt(10);
                queuedWatches = new ArrayList<>(queuedWatchCount);
                for (int j=0; j<queuedWatchCount; j++) {
                    queuedWatches.add(new QueuedWatch(randomAlphaOfLength(10), randomAlphaOfLength(10),
                        ZonedDateTime.ofInstant(Instant.ofEpochMilli(randomInt()), ZoneOffset.UTC),
                        ZonedDateTime.ofInstant(Instant.ofEpochMilli(randomInt()), ZoneOffset.UTC)));
                }
            }

            Map<String, Object> stats = null;
            if (randomBoolean()) {
                int statsCount = randomInt(10);
                stats = new HashMap<>(statsCount);
                for (int j=0; j<statsCount; j++) {
                    stats.put(randomAlphaOfLength(10), randomNonNegativeLong());
                }
            }

            nodes.add(new WatcherStatsResponse.Node(randomAlphaOfLength(10), randomFrom(WatcherState.values()), randomNonNegativeLong(),
                randomNonNegativeLong(), randomNonNegativeLong(), snapshots, queuedWatches, stats));
        }
        NodesResponseHeader nodesResponseHeader = new NodesResponseHeader(randomInt(10), randomInt(10),
            randomInt(10), Collections.emptyList());
        WatcherMetadata watcherMetadata = new WatcherMetadata(randomBoolean());
        return new WatcherStatsResponse(nodesResponseHeader, randomAlphaOfLength(10), watcherMetadata, nodes);
    }
}
