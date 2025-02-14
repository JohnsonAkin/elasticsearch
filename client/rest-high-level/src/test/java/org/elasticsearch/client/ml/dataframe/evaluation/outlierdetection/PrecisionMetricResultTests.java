/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticsearch.client.ml.dataframe.evaluation.outlierdetection;

import org.elasticsearch.xcontent.XContentParser;
import org.elasticsearch.test.AbstractXContentTestCase;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrecisionMetricResultTests extends AbstractXContentTestCase<PrecisionMetric.Result> {

    public static PrecisionMetric.Result randomResult() {
        return new PrecisionMetric.Result(
            Stream
                .generate(() -> randomDouble())
                .limit(randomIntBetween(1, 5))
                .collect(Collectors.toMap(v -> String.valueOf(randomDouble()), v -> v)));
    }

    @Override
    protected PrecisionMetric.Result createTestInstance() {
        return randomResult();
    }

    @Override
    protected PrecisionMetric.Result doParseInstance(XContentParser parser) throws IOException {
        return PrecisionMetric.Result.fromXContent(parser);
    }

    @Override
    protected boolean supportsUnknownFields() {
        return true;
    }

    @Override
    protected Predicate<String> getRandomFieldsExcludeFilter() {
        // disallow unknown fields in the root of the object as field names must be parsable as numbers
        return field -> field.isEmpty();
    }
}
