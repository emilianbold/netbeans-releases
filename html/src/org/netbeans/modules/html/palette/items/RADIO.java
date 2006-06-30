/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.html.palette.items;
import java.util.TreeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.html.HTMLSyntaxSupport;
import org.netbeans.editor.ext.html.HTMLTokenContext;
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
   
    private String[] groups = new String[0];
    
    public RADIO() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        Document doc = targetComponent.getDocument();
        if (doc instanceof BaseDocument) {
            
            String oldGN = null;
            if (groupIndex >= 0) // non-empty group list from previous run =>
                oldGN = groups[groupIndex]; // => save previously selected group name
            else if (group.length() > 0) // new group was inserted in the previous run
                oldGN = group;
            
            groups = findGroups((BaseDocument)doc);
            if (groups.length == 0) // no groups found => reset index
                groupIndex = GROUP_DEFAULT;
            
            if (groups.length > 0) { // some groups found
                groupIndex = 0; // point at the beginning by default
                if (groupIndex != GROUP_DEFAULT && oldGN != null) {// non-empty group list from previous run
                    for (; groupIndex < groups.length; groupIndex++) {
                        if (oldGN.equalsIgnoreCase(groups[groupIndex]))
                            break;
                    }
                    if (groupIndex == groups.length) // previously selected group not found
                        groupIndex = 0;
                }
            }
        }
        
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
        
        String strName = " name=\"\""; // NOI18N
        if (groupIndex == GROUP_DEFAULT)
            strName = " name=\"" + group + "\""; // NOI18N
        else 
            strName = " name=\"" + groups[groupIndex] + "\""; // NOI18N

        String strValue = " value=\"" + value + "\""; // NOI18N

        String strSelected = (selected ? " checked" : ""); // NOI18N

        String strDisabled = (disabled ? " disabled" : ""); // NOI18N

        String radioBody = "<input type=\"radio\"" + strName + strValue + strSelected + strDisabled + " />"; // NOI18N
        
        return radioBody;
    }

    private String[] findGroups(BaseDocument doc) {
         
        String[] groups = new String[] {};
        
        if (doc.getLength() == 0)
            return groups;

        HTMLSyntaxSupport sup = (HTMLSyntaxSupport)(doc.getSyntaxSupport().get(HTMLSyntaxSupport.class));
        
        try {
            TokenItem token = sup.getTokenChain(0, 1);
            final int end = doc.getLength();
            
            boolean inputTagFound = false; // '<input' found
            boolean typeAttrFound = false; // '<input type' found
            boolean radioValFound = false; // '<input type="radio"' found
            boolean nameAttrFound = false; // '<input name' found
            String groupName = null;
            TreeSet groupSet = new TreeSet();
            
            while (token != null && token.getOffset() < end) {
                token = token.getNext();
                if (token != null) {
                    if (token.getTokenID() == HTMLTokenContext.TAG_OPEN && token.getImage().equals("input")) { //input open
                        inputTagFound = true;
                    }
                    else if (inputTagFound && token.getTokenID() == HTMLTokenContext.TAG_CLOSE_SYMBOL) { // input close
                        
                        if (radioValFound && groupName != null && groupName.length() > 0)
                            groupSet.add(groupName);
                        
                        inputTagFound = false;
                        typeAttrFound = false;
                        radioValFound = false;
                        nameAttrFound = false;
                        groupName = null;
                    }
                    else if (inputTagFound && token.getTokenID() == HTMLTokenContext.ARGUMENT) {
                        if (token.getImage().equals("type"))
                            typeAttrFound = true;
                        else if (token.getImage().equals("name"))
                            nameAttrFound = true;
                    }
                    else if (typeAttrFound && token.getTokenID() == HTMLTokenContext.VALUE && token.getImage().equals("\"radio\"")) {
                        radioValFound = true;
                        typeAttrFound = false;
                    }
                    else if (nameAttrFound && token.getTokenID() == HTMLTokenContext.VALUE) {
                        groupName = token.getImage();
                        groupName = groupName.substring(1);
                        groupName = groupName.substring(0, groupName.length() - 1);
                        nameAttrFound = false;
                    }
                }
            }

            groups = (String[])groupSet.toArray(new String[0]);
            
        } catch (IllegalStateException ise) {
        } catch (BadLocationException ble) {
        }
        
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
