package org.netbeans.modules.web.client.javascript.debugger.models;

import org.openide.util.NbBundle;

public class ResolvedLocationColumnModel extends AbstractColumnModel {

    public static final String RESOLVED_LOCATION_COLUMN_ID = "RESOLVED_LOCATION";

    @Override
    public String getID() {
        return RESOLVED_LOCATION_COLUMN_ID;
    }

    /**
     * Returns display name of this column.
     * 
     * @return display name of this column
     */
    public String getDisplayName() {
        String retStr = NbBundle.getBundle(ResolvedLocationColumnModel.class)
                .getString("CTL_NbJSModel_Column_ResolvedLocation_Name");
        return retStr;
    }

    public Character getDisplayedMnemonic() {
        Character retChar = new Character(NbBundle.getBundle(
                ResolvedLocationColumnModel.class).getString(
                "CTL_NbJSModel_Column_ResolvedLocation_Mnc").charAt(0));
        return retChar;
    }

    /**
     * Returns tooltip for given column.
     * 
     * @return tooltip for given node
     */
    @Override
    public String getShortDescription() {
        return NbBundle.getBundle(ResolvedLocationColumnModel.class).getString(
                "CTL_NbJSModel_Column_ResolvedLocation_Desc");
    }

    /**
     * Returns type of column items.
     * 
     * @return type of column items
     */
    public Class<String> getType() {
        return String.class;
    }
    
}
