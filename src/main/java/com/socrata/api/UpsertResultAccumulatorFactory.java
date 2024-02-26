package com.socrata.api;

public interface UpsertResultAccumulatorFactory<Result> {
    public UpsertResultAccumulator<Result> createAccumulator(Long truthDataVersion, long truthDataShapeVersion);
}
