package com.socrata.api;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;

import com.socrata.model.UpsertError;
import com.socrata.model.UpsertResult;

public class StandardResultAccumulator implements UpsertResultAccumulator<UpsertResult> {
    private int count = 0;
    private long inserts = 0;
    private long updates = 0;
    private long deletes = 0;
    private final List<UpsertError> errors = new LinkedList<UpsertError>();

    final Long truthDataVersion;
    final Long truthDataShapeVersion;

    private StandardResultAccumulator(Long truthDataVersion, Long truthDataShapeVersion) {
        this.truthDataVersion = truthDataVersion;
        this.truthDataShapeVersion = truthDataShapeVersion;
    }

    public void insert(Soda2Producer.NewUpsertRow row) {
        inserts += 1;
        count += 1;
    }
    public void update(Soda2Producer.NewUpsertRow row) {
        updates += 1;
        count += 1;
    }

    public void delete(Soda2Producer.NewUpsertRow row) {
        deletes += 1;
        count += 1;
    }

    public void error(Soda2Producer.NewUpsertRow row) {
        errors.add(new UpsertError(row.err, count, row.id));
        count += 1;
    }

    public UpsertResult result() {
        return new UpsertResult(inserts, updates, deletes, errors.size() > 0 ? errors : null, truthDataVersion, truthDataShapeVersion);
    }

    public UpsertResult deserializeSimple(JsonParser parser) throws IOException {
        return parser.readValueAs(UpsertResult.class);
    }

    public static final UpsertResultAccumulatorFactory<UpsertResult> FACTORY =
        new UpsertResultAccumulatorFactory<UpsertResult>() {
            public UpsertResultAccumulator<UpsertResult> createAccumulator(Long truthDataVersion, long truthDataShapeVersion) {
                return new StandardResultAccumulator(truthDataVersion, truthDataShapeVersion);
            }
        };
}
