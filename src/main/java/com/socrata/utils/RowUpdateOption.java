package com.socrata.utils;

import java.util.Map;

// NOTE: If this class is changed, you may want to make similar changes to
// RowUpdate.scala of the soda-fountain project
public class RowUpdateOption
{
  public Boolean truncate;
  public Boolean mergeInsteadOfReplace;
  public Boolean errorsAreFatal;
  public String[] nonFatalRowErrors;

  public RowUpdateOption() {
    this.truncate = false;
    this.mergeInsteadOfReplace = true;
    this.errorsAreFatal = true;
    this.nonFatalRowErrors = new String[]{};
  }

  public void fromMap(Map<String,String[]> parameterMap) {
    String[] trunc = parameterMap.get("truncate");
    String[] mior = parameterMap.get("mergeInsteadOfReplace");
    String[] eaf = parameterMap.get("errorsAreFatal");
    String[] nfre = parameterMap.get("nonFatalRowErrors[]");

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
    return;
  }

  public String toQueryParams() {
    return "";
  }
}
