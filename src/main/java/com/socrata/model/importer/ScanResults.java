package com.socrata.model.importer;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 */
public class ScanResults
{

    final public String fileId;
    final public ScanSummary summary;

    @JsonCreator
    public ScanResults(final @JsonProperty("field") String fileId, final @JsonProperty("summary") ScanSummary summary)
    {
        this.fileId = fileId;
        this.summary = summary;
    }

    public String getFileId()
    {
        return fileId;
    }

    public ScanSummary getSummary()
    {
        return summary;
    }
}
