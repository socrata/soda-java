package com.socrata.utils;

import java.util.Map;
import javax.ws.rs.core.UriBuilder;

// NOTE: If this class is changed, you may want to consider making changes to
// RowUpdate.scala of the soda-fountain project as well.
public class RowUpdateOption
{
  public Boolean truncate;
  public Boolean mergeInsteadOfReplace;
  public Boolean errorsAreFatal;
  public String[] nonFatalRowErrors;
  public Long expectedDataVersion;

  public RowUpdateOption() {
    this.truncate = null;
    this.mergeInsteadOfReplace = null;
    this.errorsAreFatal = null;
    this.nonFatalRowErrors = null;
  }

  public void fromMap(Map<String,String[]> parameterMap) {
    String[] trunc = parameterMap.get("truncate");
    String[] mior = parameterMap.get("mergeInsteadOfReplace");
    String[] eaf = parameterMap.get("errorsAreFatal");
    String[] nfre = parameterMap.get("nonFatalRowErrors[]");
    String[] edv = parameterMap.get("expectedDataVersion");

    if (trunc != null) {
      this.truncate = Boolean.valueOf(trunc[0]);
    }
    if (mior != null) {
      this.mergeInsteadOfReplace = Boolean.valueOf(mior[0]);
    }
    if (eaf != null) {
      this.errorsAreFatal = Boolean.valueOf(eaf[0]);
    }
    if (nfre != null) {
      this.nonFatalRowErrors = nfre;
    }
    if (edv != null) {
      try {
        this.expectedDataVersion = Long.valueOf(edv[0]);
      } catch (NumberFormatException e) {
        // ok
      }
    }
    return;
  }

  public UriBuilder addToRequest(UriBuilder uribuilder) {
    if (this.truncate != null) {
      uribuilder = uribuilder.queryParam("truncate", this.truncate);
    }
    if (this.mergeInsteadOfReplace != null) {
      uribuilder = uribuilder.queryParam("mergeInsteadOfReplace", this.mergeInsteadOfReplace);
    }
    if (this.errorsAreFatal != null) {
      uribuilder = uribuilder.queryParam("errorsAreFatal", this.errorsAreFatal);
    }
    if (this.nonFatalRowErrors != null) {
      uribuilder = uribuilder.queryParam("nonFatalRowErrors[]", (Object[]) this.nonFatalRowErrors);
    }
    if (this.expectedDataVersion != null) {
      uribuilder = uribuilder.queryParam("expectedDataVersion", this.expectedDataVersion.toString());
    }
    return uribuilder;
  }
}
