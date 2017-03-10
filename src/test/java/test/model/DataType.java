package test.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
public class DataType
{

    String count;
    String domain;
    String datatype;

    public DataType()
    {
    }

    @JsonCreator
    public DataType(@JsonProperty("datatype") String datatype, @JsonProperty("domain") String domain, @JsonProperty("count") String count)
    {
        this.count = count;
        this.domain = domain;
        this.datatype = datatype;
    }

    public String getCount()
    {
        return count;
    }

    public void setCount(String count)
    {
        this.count = count;
    }

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public String getDatatype()
    {
        return datatype;
    }

    public void setDatatype(String datatype)
    {
        this.datatype = datatype;
    }
}
