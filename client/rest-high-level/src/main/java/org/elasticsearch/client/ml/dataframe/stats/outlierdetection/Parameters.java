/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticsearch.client.ml.dataframe.stats.outlierdetection;

import org.elasticsearch.xcontent.ParseField;
import org.elasticsearch.xcontent.ConstructingObjectParser;
import org.elasticsearch.xcontent.ToXContentObject;
import org.elasticsearch.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Objects;

import static org.elasticsearch.xcontent.ConstructingObjectParser.optionalConstructorArg;

public class Parameters implements ToXContentObject {

    public static final ParseField N_NEIGHBORS = new ParseField("n_neighbors");
    public static final ParseField METHOD = new ParseField("method");
    public static final ParseField FEATURE_INFLUENCE_THRESHOLD = new ParseField("feature_influence_threshold");
    public static final ParseField COMPUTE_FEATURE_INFLUENCE = new ParseField("compute_feature_influence");
    public static final ParseField OUTLIER_FRACTION = new ParseField("outlier_fraction");
    public static final ParseField STANDARDIZATION_ENABLED = new ParseField("standardization_enabled");

    @SuppressWarnings("unchecked")
    public static ConstructingObjectParser<Parameters, Void> PARSER = new ConstructingObjectParser<>("outlier_detection_parameters",
        true,
        a -> new Parameters(
            (Integer) a[0],
            (String) a[1],
            (Boolean) a[2],
            (Double) a[3],
            (Double) a[4],
            (Boolean) a[5]
        ));

    static {
        PARSER.declareInt(optionalConstructorArg(), N_NEIGHBORS);
        PARSER.declareString(optionalConstructorArg(), METHOD);
        PARSER.declareBoolean(optionalConstructorArg(), COMPUTE_FEATURE_INFLUENCE);
        PARSER.declareDouble(optionalConstructorArg(), FEATURE_INFLUENCE_THRESHOLD);
        PARSER.declareDouble(optionalConstructorArg(), OUTLIER_FRACTION);
        PARSER.declareBoolean(optionalConstructorArg(), STANDARDIZATION_ENABLED);
    }

    private final Integer nNeighbors;
    private final String method;
    private final Boolean computeFeatureInfluence;
    private final Double featureInfluenceThreshold;
    private final Double outlierFraction;
    private final Boolean standardizationEnabled;

    public Parameters(Integer nNeighbors, String method, Boolean computeFeatureInfluence, Double featureInfluenceThreshold,
                      Double outlierFraction, Boolean standardizationEnabled) {
        this.nNeighbors = nNeighbors;
        this.method = method;
        this.computeFeatureInfluence = computeFeatureInfluence;
        this.featureInfluenceThreshold = featureInfluenceThreshold;
        this.outlierFraction = outlierFraction;
        this.standardizationEnabled = standardizationEnabled;
    }

    public Integer getnNeighbors() {
        return nNeighbors;
    }

    public String getMethod() {
        return method;
    }

    public Boolean getComputeFeatureInfluence() {
        return computeFeatureInfluence;
    }

    public Double getFeatureInfluenceThreshold() {
        return featureInfluenceThreshold;
    }

    public Double getOutlierFraction() {
        return outlierFraction;
    }

    public Boolean getStandardizationEnabled() {
        return standardizationEnabled;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject();
        if (nNeighbors != null) {
            builder.field(N_NEIGHBORS.getPreferredName(), nNeighbors);
        }
        if (method != null) {
            builder.field(METHOD.getPreferredName(), method);
        }
        if (computeFeatureInfluence != null) {
            builder.field(COMPUTE_FEATURE_INFLUENCE.getPreferredName(), computeFeatureInfluence);
        }
        if (featureInfluenceThreshold != null) {
            builder.field(FEATURE_INFLUENCE_THRESHOLD.getPreferredName(), featureInfluenceThreshold);
        }
        if (outlierFraction != null) {
            builder.field(OUTLIER_FRACTION.getPreferredName(), outlierFraction);
        }
        if (standardizationEnabled != null) {
            builder.field(STANDARDIZATION_ENABLED.getPreferredName(), standardizationEnabled);
        }
        builder.endObject();
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameters that = (Parameters) o;
        return Objects.equals(nNeighbors, that.nNeighbors)
            && Objects.equals(method, that.method)
            && Objects.equals(computeFeatureInfluence, that.computeFeatureInfluence)
            && Objects.equals(featureInfluenceThreshold, that.featureInfluenceThreshold)
            && Objects.equals(outlierFraction, that.outlierFraction)
            && Objects.equals(standardizationEnabled, that.standardizationEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nNeighbors, method, computeFeatureInfluence, featureInfluenceThreshold, outlierFraction,
            standardizationEnabled);
    }
}
