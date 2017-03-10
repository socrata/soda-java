package com.socrata.model.importer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class that contains all the information about a dataset NOT related to its actual
 * schema.  An easy way to think of this class is all the parts of a dataset you can
 * change without having to put it into a Working Copy.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="viewType")
@JsonSubTypes({@JsonSubTypes.Type(value = Dataset.class, name = "tabular"),
               @JsonSubTypes.Type(value = NonDataFileDataset.class, name = "blobby"),
               @JsonSubTypes.Type(value = ExternalDataset.class, name = "href"),
               @JsonSubTypes.Type(value = GeoDataset.class, name = "geo")})
public class DatasetInfo
{
    public static final String DATASET_TYPE = "tabular";
    public static final String EXTERNAL_TYPE = "href";
    public static final String FILE_TYPE = "blobby";
    public static final String GEODATASET_TYPE = "geo";

    public static final     String PUBLISHED = "published";
    public static final     String UNPUBLISHED = "unpublished";

    private String              resourceName;
    private String              viewType;
    private String              displayType;
    private String              attribution;
    private String              attributionLink;
    private String              category;
    private String              description;
    private String              externalId;
    private String              id;
    private License             license;
    private String              licenseId;
    private String              name;
    private Metadata            metadata;
    private Map<String, Object> privateMetadata;
    private String              publicationStage;
    private Long                rowsUpdatedAt;
    private Long                viewLastModified;
    private Long                createdAt;
    private List<String>        rights = new ArrayList<String>();
    private List<String>        tags = new ArrayList<String>();
    private List<Grant>         grants;
    private UserInfo owner;
    private UserInfo tableAuthor;


    /**
     * Does a deep copy of this DatasetInfo.
     *
     * @param src
     * @return
     */
    public static DatasetInfo copy(DatasetInfo src) {
        DatasetInfo retVal = new DatasetInfo();
        try {
            BeanUtils.copyProperties(retVal, src);
            if (retVal.metadata != null) {
                retVal.metadata = Metadata.copy(retVal.getMetadata());
            }

            if (retVal.rights != null){
                retVal.rights = Lists.newArrayList(retVal.rights);
            }

            if (retVal.tags != null) {
                retVal.tags = Lists.newArrayList(retVal.tags);
            }

            if (retVal.privateMetadata != null) {
                retVal.privateMetadata = Maps.newHashMap(retVal.privateMetadata);
            }

            if (retVal.grants != null) {
                retVal.grants = Lists.newArrayList(retVal.grants);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        return retVal;
    }

    public DatasetInfo()
    {
    }

    /**
     * Gets the resource name for this dataset.  The Resource Name is
     * a name that can be used on the URL to access the dataset.
     *
     * E.g.  if a dataset had a resource name of my_dataset, it would
     * be accessible via:
     *
     *   https://mysite.socrata.com/id/my_dataset
     *
     * @return the resource name
     */
    public String getResourceName()
    {
        return resourceName;
    }

    /**
     * Sets the resource name for this dataset.  The Resource Name is
     * a name that can be used on the URL to access the dataset.
     *
     * E.g.  if a dataset had a resource name of my_dataset, it would
     * be accessible via:
     *
     *   https://mysite.socrata.com/id/my_dataset
     */
    public void setResourceName(String resourceName)
    {
        this.resourceName = resourceName;
    }

    /**
     * describes what type of object this is.  The possible values are:
     * <ul>
     *     <li>tabular</li>
     *     <li>blobby</li>
     *     <li>href</li>
     * </ul>
     * @return the object type
     */
    public String getViewType()
    {
        return viewType;
    }

    /**
     * Sets what type of object this is.  This should not be changed
     * for existing objects.
     *
     * @param viewType the object type
     */
    public void setViewType(String viewType)
    {
        this.viewType = viewType;
    }

    /**
     * describes the display type of the object.
     *
     * @return the display type
     */
    public String getDisplayType() {
        return displayType;
    }

    /**
     * Sets the display type of the object.
     *
     * @param displayType the display type
     */
    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    /**
     * The Attribution for the current license.  Not all licenses require attribution. Look
     * at the LicenseInfo enum to find some of the licenses that do or don't need attribution.
     *
     * @return the attribution for this license.
     */
    public String getAttribution()
    {
        return attribution;
    }

    /**
     * The Attribution for the current license.  Not all licenses require attribution. Look
     * at the LicenseInfo enum to find some of the licenses that do or don't need attribution.
     *
     * @param attribution the attribution for this license.
     */
    public void setAttribution(String attribution)
    {
        this.attribution = attribution;
    }

    /**
     * A link to the attribution for this license. Not all licenses require attribution. Look
     * at the LicenseInfo enum to find some of the licenses that do or don't need attribution.
     *
     * @return the link to the attribution for this license.
     */
    public String getAttributionLink()
    {
        return attributionLink;
    }

    /**
     * A link to the attribution for this license. Not all licenses require attribution. Look
     * at the LicenseInfo enum to find some of the licenses that do or don't need attribution.
     *
     * @param attributionLink the link to the attribution for this license.
     */
    public void setAttributionLink(String attributionLink)
    {
        this.attributionLink = attributionLink;
    }

    /**
     * Gets the main category used to classify this dataset.
     *
     * @return main category used to classify this dataset.
     */
    public String getCategory()
    {
        return category;
    }

    /**
     * Sets the main category used to classify this dataset.
     *
     * @param category main category used to classify this dataset.
     */
    public void setCategory(String category)
    {
        this.category = category;
    }

    /**
     * Gets the human readable description of this dataset.
     *
     * @return human readable description for this dataset.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the human readable description of this dataset.
     *
     * @param description human readable description for this dataset.
     */
    public void setDescription(final String description)
    {
        this.description = description;
    }

    /**
     * Sometimes datasets have an ID in a system outside Socrata.  This field is where you can
     * store this info.  Although this is opaque to the system, common values here are URLs, URIs, GUIDs or other globally
     * unique constructs.
     *
     * @return the external ID
     */
    public String getExternalId()
    {
        return externalId;
    }

    /**
     * Sometimes datasets have an ID in a system outside Socrata.  This field is where you can
     * store this info.  Although this is opaque to the system, common values here are URLs, URIs, GUIDs or other globally
     * unique constructs.
     *
     * @param externalId the external ID
     */
    public void setExternalId(String externalId)
    {
        this.externalId = externalId;
    }

    /**
     * This unique ID for this dataset.  The format of this key will be the 4x4 format used by
     * socrata, e.g.  abcd-efgh
     *
     * This key cannot be changed after the dataset is created.
     *
     * @return the unique Id of the dataset, or null if this is not persisted yet.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the unique ID for this dataset.  This should only really be set by the system.
     * @param id the unique Id of the dataset
     */
    public void setId(final String id)
    {
        this.id = id;
    }

    /**
     * Sets up the license portion of the dataset info from a LicenseInfo and an attribution.  This is a convenience
     * method to make sure all parts of the object are set correctly and in sync.
     *
     * @param licenseInfo the license info about this dataset's license
     * @param attribution the attribution for this license.  This is required when licenseInfo.attributionRequired is true
     * @param attributionLink a link for the attribution for this license.  This is recommended when licenseInfo.attributionRequired is true
     */
    public void setupLicense(final LicenseInfo licenseInfo, final String attribution, final String attributionLink) {
        if (licenseInfo != null) {
            if (licenseInfo.attributionRequired && attribution==null && attributionLink==null) {
                throw new IllegalArgumentException(licenseInfo.name() + " requires attribution, but no attribution is passed in.");
            }

            setLicense(new License(licenseInfo.friendlyName, licenseInfo.logoPath, licenseInfo.termsLink));
            setLicenseId(licenseInfo.uniqueId);
            setAttribution(attribution);
            setAttributionLink(attributionLink);

        } else {
            setLicense(null);
            setLicenseId(null);
            setAttribution(null);
            setAttributionLink(null);
        }
    }

    /**
     * Gets the license structure that defines this license.  This should describe the license
     * specified in the licenseId field.
     *
     * @return the license structure for this license.
     */
    public License getLicense()
    {
        return license;
    }

    /**
     * Sets the license structure that defines this license.  This should describe the license
     * specified in the licenseId field.
     */
    public void setLicense(License license)
    {
        this.license = license;
    }

    /**
     * The unique ID for the license that applies to this dataset.  Look at the LicenseInfo
     * enum for a list of the license IDs.
     *
     * @return unique id for the license for this dataset.
     */
    public String getLicenseId()
    {
        return licenseId;
    }

    /**
     * The unique ID for the license that applies to this dataset.  Look at the LicenseInfo
     * enum for a list of the license IDs.
     *
     * @param licenseId unique id for the license for this dataset.
     */
    public void setLicenseId(String licenseId)
    {
        this.licenseId = licenseId;
    }

    /**
     * Gets the metadata for this dataset
     *
     * @return metadata object for this dataset
     */
    public Metadata getMetadata()
    {
        return metadata;
    }

    /**
     * Sets the metadata for this dataset
     *
     * @param metadata metadata object for this dataset
     */
    public void setMetadata(final Metadata metadata)
    {
        this.metadata = metadata;
    }

    /**
     * Gets the human readable name for this dataset.
     *
     * @return human readable name for this dataset.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the human readable name for this dataset.
     *
     * @param name human readable name for this dataset.
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Gets private metadata for this dataset.  Private metadata
     * is metadata that is not viewable by the general public.
     *
     * @return the private metadata for this dataset
     */
    public Map<String, Object> getPrivateMetadata()
    {
        return privateMetadata;
    }

    /**
     * Sets private metadata for this dataset.  Private metadata
     * is metadata that is not viewable by the general public.
     *
     * @param privateMetadata the private metadata for this dataset
     */
    public void setPrivateMetadata(Map<String, Object> privateMetadata)
    {
        this.privateMetadata = privateMetadata;
    }

    /**
     * Returns whether this dataset is in a published or unpublished state.
     *
     * @return "published" if this dataset is published
     *         "unpublished" if this dataset is not published
     */
    public String getPublicationStage()
    {
        return publicationStage;
    }

    /**
     * This should really only be called by the system.  To publish a dataset or create
     * a working copy, look at the SodaWorkflow object, particularly at the methods publish
     * and createWorkingCopy
     *
     * @param publicationStage "published" if this dataset is published
     *         "unpublished" if this dataset is not published
     */
    public void setPublicationStage(String publicationStage)
    {
        this.publicationStage = publicationStage;
    }

    public List<String> getRights()
    {
        return rights;
    }

    public void setRights(List<String> rights)
    {
        this.rights = rights;
    }

    /**
     * A long representing the seconds since the epoch till this was modified.
     *
     * NOTE:  THis is <b>seconds</b> since the epoch, not millis.  So turning into a standard Date requires
     * multiplying by 1000.
     *
     * @return A long representing the seconds since the epoch till this was modified.
     */
    public Long getRowsUpdatedAt()
    {
        return rowsUpdatedAt;
    }

    /**
     * This method should only be called by the system, since the system is responsible for setting the
     * modified date.
     *
     * @param rowsUpdatedAt A long representing the seconds since the epoch till this was modified.
     */
    public void setRowsUpdatedAt(Long rowsUpdatedAt)
    {
        this.rowsUpdatedAt = rowsUpdatedAt;
    }

    public Long getViewLastModified()
    {
        return viewLastModified;
    }

    public void setViewLastModified(Long viewLastModified)
    {
        this.viewLastModified = viewLastModified;
    }

    public Long getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt)
    {
        this.createdAt = createdAt;
    }

    /**
     * Gets the list of freeform tags describing this dataset.
     *
     * @return the list of freeform tags describing this dataset.
     */
    public List<String> getTags()
    {
        return tags;
    }

    /**
     * Sets the list of freeform tags describing this dataset.
     */
    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }

    /**
     * Gets all the permission grants for this dataset.  This will include
     * a grant to make the dataset public, or any explicit sharing with another user.
     *
     * May be null if there are no grants.  This state is teh same as a dataset being private.
     *
     * @return list of grants
     */
    public List<Grant> getGrants()
    {
        return grants;
    }

    /**
     * Sets all the permission grants for this dataset.  This is an accessor on this
     * class, but to actually make a dataset public or private, you should use the
     *   {@code makePublic} and {@code makePrivate} methods in SodaWorkflow
     */
    public void setGrants(List<Grant> grants)
    {
        this.grants = grants;
    }

    public UserInfo getOwner()
    {
        return owner;
    }

    public void setOwner(UserInfo owner)
    {
        this.owner = owner;
    }

    public UserInfo getTableAuthor()
    {
        return tableAuthor;
    }

    public void setTableAuthor(UserInfo tableAuthor)
    {
        this.tableAuthor = tableAuthor;
    }
}
