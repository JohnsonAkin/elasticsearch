/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticsearch.client.ml.dataframe.evaluation.outlierdetection;

import org.elasticsearch.client.ml.dataframe.evaluation.EvaluationMetric;
import org.elasticsearch.common.Strings;
import org.elasticsearch.xcontent.ConstructingObjectParser;
import org.elasticsearch.xcontent.ToXContent;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.elasticsearch.xcontent.ConstructingObjectParser.constructorArg;

public class PrecisionMetric extends AbstractConfusionMatrixMetric {

    public static final String NAME = "precision";

    @SuppressWarnings("unchecked")
    private static final ConstructingObjectParser<PrecisionMetric, Void> PARSER =
        new ConstructingObjectParser<>(NAME, args -> new PrecisionMetric((List<Double>) args[0]));

    static {
        PARSER.declareDoubleArray(constructorArg(), AT);
    }

    public static PrecisionMetric fromXContent(XContentParser parser) {
        return PARSER.apply(parser, null);
    }

    public static PrecisionMetric at(Double... at) {
        return new PrecisionMetric(Arrays.asList(at));
    }

    public PrecisionMetric(List<Double> at) {
        super(at);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrecisionMetric that = (PrecisionMetric) o;
        return Arrays.equals(thresholds, that.thresholds);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(thresholds);
    }

    public static class Result implements EvaluationMetric.Result {

        public static Result fromXContent(XContentParser parser) throws IOException {
            return new Result(parser.map(LinkedHashMap::new, p -> p.doubleValue()));
        }

        private final Map<String, Double> results;

        public Result(Map<String, Double> results) {
            this.results = Objects.requireNonNull(results);
        }

        @Override
        public String getMetricName() {
            return NAME;
        }

        public Double getScoreByThreshold(String threshold) {
            return results.get(threshold);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
            return builder.map(results);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result that = (Result) o;
            return Objects.equals(results, that.results);
        }

        @Override
        public int hashCode() {
            return Objects.hash(results);
        }

        @Override
        public String toString() {
            return Strings.toString(this);
        }
    }
}
