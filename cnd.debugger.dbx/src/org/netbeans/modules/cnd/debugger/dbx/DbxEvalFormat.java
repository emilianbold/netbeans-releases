/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.debugger.dbx;

import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.FormatOption;

/**
 *
 * @author Egor Ushakov
 */
public enum DbxEvalFormat implements FormatOption {
    DEFAULT(Catalog.get("Default_format"), ""), //NOI18N
    HEXADECIMAL4(Catalog.get("l_Hexadecimal"), "-fx"), //NOI18N
    HEXADECIMAL8(Catalog.get("L_Hexadecimal"), "-flx"), //NOI18N
    DECIMAL4(Catalog.get("l_Decimal"), "-fd"), //NOI18N
    DECIMAL8(Catalog.get("L_Decimal"), "-fld"), //NOI18N
    UNSIGNED_DECIMAL4(Catalog.get("l_U_Decimal"), "-fu"), //NOI18N
    UNSIGNED_DECIMAL8(Catalog.get("L_U_Decimal"), "-flu"), //NOI18N
    FLOAT4(Catalog.get("l_Float"), "(float)"), //NOI18N
    FLOAT8(Catalog.get("L_Float"), "(double)"); //NOI18N

    private final String dispName;
    private final String option;
    
    DbxEvalFormat(String dispName, String option) {
        this.dispName = dispName;
        this.option = option;
    }

    @Override
    public String toString() {
        return dispName;
    }

    public String getOption() {
        return option;
    }
}
