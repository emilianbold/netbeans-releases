/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.html.palette.items;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.html.palette.HTMLPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class RADIO implements ActiveEditorDrop {

    private static final int GROUP_DEFAULT = -1;
    
    private String group = "";
    private int groupIndex = GROUP_DEFAULT;
    private String value = "";
    private boolean selected = false;
    private boolean disabled = false;
   
    private String[] groups;
    
    public RADIO() {
        groups = findGroups();
        if (groups.length > 0)
            groupIndex = 0;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        RADIOCustomizer c = new RADIOCustomizer(this);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                HTMLPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        
        return accept;
    }
    
    private String createBody() {
        
        String strName = " name=\"\"";
        if (groupIndex == -1)
            strName = " name=\"" + group + "\"";
        else 
            strName = " name=\"" + groups[groupIndex] + "\"";

        String strValue = " value=\"" + value + "\"";

        String strSelected = (selected ? " checked" : "");

        String strDisabled = (disabled ? " disabled" : "");

        String radioBody = "<input type=\"radio\"" + strName + strValue + strSelected + strDisabled + " />";
        
        return radioBody;
    }

    private String[] findGroups() {
         
        //TODO retrieve existing group names
        String[] groups = new String[] {};
        
        return groups;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String[] getGroups() {
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }
    
}
