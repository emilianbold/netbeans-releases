/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.html.palette.items;
import java.util.TreeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.html.HtmlSyntaxSupport;
import org.netbeans.editor.ext.html.HtmlTokenContext;
import org.netbeans.modules.html.palette.HtmlPaletteUtilities;
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
                HtmlPaletteUtilities.insert(body, targetComponent);
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

        String strSelected = (selected ? " checked=\"checked\"" : ""); // NOI18N

        String strDisabled = (disabled ? " disabled=\"disabled\"" : ""); // NOI18N

        String radioBody = "<input type=\"radio\"" + strName + strValue + strSelected + strDisabled + " />"; // NOI18N
        
        return radioBody;
    }

    private String[] findGroups(BaseDocument doc) {
         
        String[] groups = new String[] {};
        
        if (doc.getLength() == 0)
            return groups;

        HtmlSyntaxSupport sup = HtmlSyntaxSupport.get(doc);
        
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
                    if (token.getTokenID() == HtmlTokenContext.TAG_OPEN && token.getImage().equals("input")) { //input open
                        inputTagFound = true;
                    }
                    else if (inputTagFound && token.getTokenID() == HtmlTokenContext.TAG_CLOSE_SYMBOL) { // input close
                        
                        if (radioValFound && groupName != null && groupName.length() > 0)
                            groupSet.add(groupName);
                        
                        inputTagFound = false;
                        typeAttrFound = false;
                        radioValFound = false;
                        nameAttrFound = false;
                        groupName = null;
                    }
                    else if (inputTagFound && token.getTokenID() == HtmlTokenContext.ARGUMENT) {
                        if (token.getImage().equals("type"))
                            typeAttrFound = true;
                        else if (token.getImage().equals("name"))
                            nameAttrFound = true;
                    }
                    else if (typeAttrFound && token.getTokenID() == HtmlTokenContext.VALUE && token.getImage().equals("\"radio\"")) {
                        radioValFound = true;
                        typeAttrFound = false;
                    }
                    else if (nameAttrFound && token.getTokenID() == HtmlTokenContext.VALUE) {
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
