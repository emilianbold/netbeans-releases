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
public enum DbxMemoryFormat implements FormatOption {
    HEXADECIMAL8(Catalog.get("L_Hexadecimal"), "lX"), //NOI18N
    HEXADECIMAL4(Catalog.get("l_Hexadecimal"), "X"), //NOI18N
    HEXADECIMAL2(Catalog.get("w_Hexadecimal"), "x"), //NOI18N
    DECIMAL(Catalog.get("l_Decimal"), "D"), //NOI18N
    OCTAL(Catalog.get("l_Octal"), "O"), //NOI18N
    FLOAT8(Catalog.get("L_Float"), "F"), //NOI18N
    FLOAT4(Catalog.get("l_Float"), "f"), //NOI18N
    INSTRUCTION(Catalog.get("L_Instructions"), "i"), //NOI18N
    CHARACTER(Catalog.get("L_Characters"), "c"), //NOI18N
    WCHARACTER(Catalog.get("L_WideCharacters"), "w"); //NOI18N

    private final String dispName;
    private final String option;
    
    DbxMemoryFormat(String dispName, String option) {
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
