package com.socrata.model.importer;

/**
 * This is a convenient list of the licenses currently understood by Socrata. They are listed as
 * enums to make them easier to read, however, the important part here is to :
 * <ol>
 *     <li>Use the uniqueId when adding the license to the DatasetInfo for setting a license</li>
 *     <li>If the {@code attributionRequired} field is set, then the DatasetInfo class will need to have the attribution set.</li>
 * </ol>
 *
 */
public enum LicenseInfo
{
    publicDomain            ("PUBLIC_DOMAIN",   false,"Public Domain",                                                                null, null),
    ccUniversal_1_0         ("CC0_10",          false,"Creative Commons 1.0 Universal",                                               "http://creativecommons.org/publicdomain/zero/1.0/legalcode", "images/licenses/ccZero.png"),
    ccAttribution_3_0       ("CC_30_BY",        true, "Creative Commons Attribution 3.0 Unported",                                    "http://creativecommons.org/licenses/by/3.0/legalcode",       "images/licenses/cc30by.png"),
    ccAttribution_3_0_sa    ("CC_30_BY_SA",     true, "Creative Commons Attribution | Share Alike 3.0 Unported",                      "http://creativecommons.org/licenses/by-sa/3.0/legalcode",    "images/licenses/cc30bysa.png"),
    ccAttribution_3_0_nd    ("CC_30_BY_ND",     true, "Creative Commons Attribution | No Derivative Works 3.0 Unported",              "http://creativecommons.org/licenses/by-nd/3.0/legalcode",    "images/licenses/cc30bynd.png"),
    ccAttribution_3_0_nc    ("CC_30_BY_NC",     true, "Creative Commons Attribution | Noncommercial 3.0 Unported",                    "http://creativecommons.org/licenses/by-nc/3.0/legalcode",    "images/licenses/cc30bync.png"),
    ccAttribution_3_0_nc_sa ("CC_30_BY_NC_SA",  true, "Creative Commons Attribution | Noncommercial-Share Alike 3.0 Unported",        "http://creativecommons.org/licenses/by-nc-sa/3.0/legalcode", "images/licenses/cc30byncsa.png"),
    ccAttribution_3_0_nc_nd ("CC_30_BY_NC_ND",  true, "Creative Commons Attribution | Noncommercial-No Derivative Works 3.0 Unported","http://creativecommons.org/licenses/by-nc-nd/3.0/legalcode", "images/licenses/cc30byncnd.png");


    final String uniqueId;
    final String friendlyName;
    final boolean attributionRequired;
    final String termsLink;
    final String logoPath;

    private LicenseInfo(String uniqueId, boolean attributionRequired, String friendlyName, String termsLink, String logoPath)
    {
        this.uniqueId = uniqueId;
        this.friendlyName = friendlyName;
        this.attributionRequired = attributionRequired;
        this.termsLink = termsLink;
        this.logoPath = logoPath;
    }
}
