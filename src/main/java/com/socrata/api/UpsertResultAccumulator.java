package com.socrata.api;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;

public interface UpsertResultAccumulator<Result> {
    void insert(Soda2Producer.NewUpsertRow row);
    void update(Soda2Producer.NewUpsertRow row);
    void delete(Soda2Producer.NewUpsertRow row);
    void error(Soda2Producer.NewUpsertRow row);

    Result deserializeSimple(JsonParser parser) throws IOException;

    Result result();
}
