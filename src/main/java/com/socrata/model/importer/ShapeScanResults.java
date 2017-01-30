package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Results of scanning a shape file.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ShapeScanResults
{
    final public String fileId;
    final public ShapeScanSummary summary;
    final public List<String> warnings;

    @JsonCreator
    public ShapeScanResults(final @JsonProperty("field") String fileId, final @JsonProperty("summary") ShapeScanSummary summary, final @JsonProperty("warnings") List<String> warnings)
    {
        this.fileId = fileId;
        this.summary = summary;
        this.warnings = warnings;
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
    public ShapeScanSummary getSummary()
    {
        return summary;
    }

    /**
     * List of warnings that came from analyzing the shape files.
     * @return
     */
    public List<String> getWarnings()
    {
        return warnings;
    }
}
