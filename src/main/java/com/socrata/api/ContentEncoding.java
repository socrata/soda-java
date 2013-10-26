package com.socrata.api;

/**
 * The supported content encodings for this service.
 */
public enum ContentEncoding
{
    IDENTITY("identity"),   //Don't compress
    GZIP("gzip");           //Use GZIP compression


    final String header;

    private ContentEncoding(String header) { this.header = header; }
}
