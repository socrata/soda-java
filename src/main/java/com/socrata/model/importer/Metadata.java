package com.socrata.model.importer;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class represents the Metadata for a dataset.
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Metadata
{

    Map<String, Map<String, String>>    custom_fields;
    List<String>                        warnings;
    String                              rowIdentifier;
    String                              rdfSubject;
    String                              rdfClass;
    List<Attachment>                    attachments;

    public static Metadata copy(Metadata src) {

        Metadata retVal = new Metadata();
        try {
            BeanUtils.copyProperties(retVal, src);

            //Copy Collections
            if (retVal.custom_fields != null) {
                retVal.custom_fields = Maps.newHashMap(Maps.transformValues(retVal.custom_fields, new Function<Map<String, String>, Map<String, String>>()
                {
                    public Map<String, String> apply(@Nullable Map<String, String> input)
                    {
                        return Maps.newHashMap(input);
                    }
                }));
            } else {
                retVal.custom_fields = new HashMap<String, Map<String, String>>();
            }

            if (retVal.attachments != null) {
                retVal.attachments = Lists.newArrayList(retVal.attachments);
            } else {
                retVal.attachments = new ArrayList<Attachment>();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        return retVal;
    }

    public Metadata()
    {
    }

    public Metadata(Map<String, Map<String, String>> custom_metadata, String rowIdentifier, String rdfSubject, String rdfClass, List<Attachment> attachments)
    {
        this.custom_fields = custom_metadata;
        this.rowIdentifier = rowIdentifier;
        this.rdfSubject = rdfSubject;
        this.rdfClass = rdfClass;
        this.attachments = attachments;
    }

    public Map<String, Map<String, String>> getCustom_fields()
    {
        return custom_fields;
    }

    public void addCustomField(String category, String key, String value) {
        if (custom_fields == null) {
            custom_fields = Maps.newHashMap();
        }

        Map fields = custom_fields.get(category);
        if (fields == null) {
            fields = new HashMap();
            custom_fields.put(category, fields);
        }

        fields.put(key, value);
    }

    public void setCustom_fields(Map<String, Map<String, String>> custom_metadata)
    {
        this.custom_fields = custom_metadata;
    }

    public String getRowIdentifier()
    {
        return rowIdentifier;
    }

    public void setRowIdentifier(String rowIdentifier)
    {
        this.rowIdentifier = rowIdentifier;
    }

    public String getRdfSubject()
    {
        return rdfSubject;
    }

    public void setRdfSubject(String rdfSubject)
    {
        this.rdfSubject = rdfSubject;
    }

    public String getRdfClass()
    {
        return rdfClass;
    }

    public void setRdfClass(String rdfClass)
    {
        this.rdfClass = rdfClass;
    }

    public List<String>  getWarnings()
    {
        return warnings;
    }

    public void setWarnings(List<String>  warnings)
    {
        this.warnings = warnings;
    }

    public List<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments)
    {
        this.attachments = attachments;
    }
}
