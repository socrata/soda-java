package com.socrata.model.importer;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The results of a server scan over the csv, this is needed before importing a CSV
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

    /**
     * A unique ID created by the server that refers to the file that was updated.
     * @return
     */
    public String getFileId()
    {
        return fileId;
    }

    /**
     * Gets the summary results of the scan.
     * @return
     */
    public ScanSummary getSummary()
    {
        return summary;
    }
}
