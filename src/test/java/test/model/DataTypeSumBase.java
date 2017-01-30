package test.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

abstract public class DataTypeSumBase
{

    final String datatype;

    @JsonCreator
    public DataTypeSumBase(@JsonProperty("datatype") String datatype)
    {
        this.datatype = datatype;
    }


    public String getDatatype()
    {
        return datatype;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataTypeSumBase that = (DataTypeSumBase) o;

        if (datatype != null ? !datatype.equals(that.datatype) : that.datatype != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return datatype != null ? datatype.hashCode() : 0;
    }
}
