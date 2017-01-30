package test.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataTypeSumInt extends DataTypeSumBase
{

    final Integer sum_count;


    @JsonCreator
    public DataTypeSumInt(@JsonProperty("datatype") String datatype, @JsonProperty("sum_count") Integer sum_count)
    {
        super(datatype);
        this.sum_count = sum_count;

    }

    public Integer getSum_count()
    {
        return sum_count;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DataTypeSumInt that = (DataTypeSumInt) o;

        if (sum_count != null ? !sum_count.equals(that.sum_count) : that.sum_count != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (sum_count != null ? sum_count.hashCode() : 0);
        return result;
    }
}
