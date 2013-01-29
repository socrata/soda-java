package com.socrata.builders;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.socrata.model.Location;
import com.socrata.model.importer.Blueprint;
import com.socrata.model.importer.BlueprintColumn;
import com.socrata.model.importer.ScanColumn;
import com.socrata.model.importer.ScanResults;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 */
public class BlueprintBuilder
{

    public static final Function<ScanColumn, BlueprintColumn>   CONVERT_BLUPRINT = new Function<ScanColumn, BlueprintColumn>()
    {
        @Override
        public BlueprintColumn apply(@Nullable ScanColumn input)
        {
            return new BlueprintColumn(input.getName(), "", input.getSuggestion());
        }
    };

    String name;
    String description;
    int skip;
    List<BlueprintColumn> columns;

    public BlueprintBuilder() {
        columns = new ArrayList<BlueprintColumn>();
    }

    public BlueprintBuilder(final ScanResults scanResults) {
        skip = scanResults.getSummary().getHeaders();
        columns = Lists.newArrayList(Collections2.transform(scanResults.getSummary().getColumns(), CONVERT_BLUPRINT));
    }

    public BlueprintBuilder setName(final String name) {
        this.name = name;
        return this;
    }

    public BlueprintBuilder setDescription(final String description) {
        this.description = description;
        return this;
    }

    public BlueprintBuilder setSkip(final int skip) {
        this.skip = skip;
        return this;
    }

    public  BlueprintBuilder addColumn(BlueprintColumn column) {
        columns.add(column);
        return this;
    }

    public Blueprint  build() {
        return new Blueprint(name, description, skip, columns);
    }
}
