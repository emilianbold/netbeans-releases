/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
*/
package org.netbeans.test.dataprovider.cachedrowsetdataprovider;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.designer.*;
import org.netbeans.modules.visualweb.gravy.toolbox.*;
import org.netbeans.test.dataprovider.common.*;
        
public class VirtualFormConfigurator implements Constants {
    private JDialogOperator dialogOp;
    private List<VirtualFormEntry> virtualFormEntryList = new ArrayList<VirtualFormEntry>();
            
    public VirtualFormConfigurator(String propertyName) {
        String virtualFormData = TestPropertiesHandler.getTestProperty(propertyName);
        if (virtualFormData == null) {
            throw new RuntimeException("Virtual Form Data [" + propertyName + "] is not found");
        }
        Utils.logMsg("+++ Virtual Form Data for property [" + propertyName + "] = [" + 
            virtualFormData + "]");
        virtualFormEntryList = parseVirtualFormData(virtualFormData);
    }
    
    private List<VirtualFormEntry> parseVirtualFormData(String virtualFormData) {
        List<VirtualFormEntry> entryList = new ArrayList<VirtualFormEntry>();
        
        String[] entryArray = virtualFormData.split("[{}]");
        for (String entry : entryArray) {
            entry = entry.trim();
            if (entry.length() > 0) {
                String[] valueArray = entry.split("[,]");
                int valueCounter = 0;
                VirtualFormEntry virtualFormEntry = null;
                for (String value : valueArray) {
                    value = value.trim();
                    if (value.length() > 0) {
                        if (valueCounter == 0) {
                            virtualFormEntry = new VirtualFormEntry();
                            virtualFormEntry.setComponentID(value);
                        } else if (valueCounter == 1) {
                            virtualFormEntry.setVirtualFormName(value);
                        }  else if (valueCounter == 2) {
                            virtualFormEntry.setParticipateValue(value);
                        }  else if (valueCounter == 3) {
                            virtualFormEntry.setSubmitValue(value);
                        }
                        ++ valueCounter;
                    }
                }
                if (virtualFormEntry != null) {
                    entryList.add(virtualFormEntry);
                }
            }
        }
        Utils.logMsg("+++ Virtual Form Entry List = " + entryList.toString());
        return entryList;
    }
    
    public void configureVirtualForm() {
        if (virtualFormEntryList.size() == 0) {
            throw new RuntimeException("Virtual Form Entry List is empty");
        }
        for (VirtualFormEntry virtualFormEntry : virtualFormEntryList) {
            String componentID = virtualFormEntry.getComponentID();
            Utils.callPopupMenuOnNavigatorTreeNode(NAVIGATOR_TREE_NODE_FORM_PREFIX + componentID, 
                POPUP_MENU_ITEM_CONFIGURE_VIRTUAL_FORMS);
            
            dialogOp = new JDialogOperator(DIALOG_TITLE_CONFIGURE_VIRTUAL_FORMS);
            Util.wait(500);
            new QueueTool().waitEmpty();
            Utils.logMsg("+++ Dialog [" + DIALOG_TITLE_CONFIGURE_VIRTUAL_FORMS + "] is found");
            try {
                updateVirtualForm(virtualFormEntry);
            } finally {
                closeVirtualFormDialog();
            }
        }
    }
    
    private void updateVirtualForm(VirtualFormEntry virtualFormEntry) {
        JTableOperator jTableOp = new JTableOperator(dialogOp);

        String newVirtualFormName = virtualFormEntry.getVirtualFormName(),
               newParticipateValue = virtualFormEntry.getParticipateValue(),
               newSubmitValue = virtualFormEntry.getSubmitValue();
        
        int rowNum = findVirtualFormRow(jTableOp, newVirtualFormName);
        Utils.logMsg("+++ Virtual Form: cell values in the row [" + rowNum + "] are changing...");
        
        // modify cell values
        jTableOp.setValueAt(newVirtualFormName, rowNum, VIRTUAL_FORM_COL_NAME);
        Util.wait(500);
        new QueueTool().waitEmpty();

        jTableOp.setValueAt(newParticipateValue, rowNum, VIRTUAL_FORM_COL_PARTICIPATE);
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        jTableOp.setValueAt(newSubmitValue, rowNum, VIRTUAL_FORM_COL_SUBMIT);
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        checkUpdatingResult(jTableOp, rowNum, virtualFormEntry);
        Utils.logMsg("+++ Virtual Form has been updated properly");
    }
    
    private void checkUpdatingResult(JTableOperator jTableOp, int rowNum, VirtualFormEntry virtualFormEntry) {
        StringBuffer errMsg = new StringBuffer();
        String
            virtualFormName = jTableOp.getValueAt(rowNum, VIRTUAL_FORM_COL_NAME).toString(),
            participateValue = jTableOp.getValueAt(rowNum, VIRTUAL_FORM_COL_PARTICIPATE).toString(),
            submitValue = jTableOp.getValueAt(rowNum, VIRTUAL_FORM_COL_SUBMIT).toString();
        Utils.logMsg("+++ Virtual Form: current NAME value = [" + virtualFormName + "]");
        Utils.logMsg("+++ Virtual Form: current PARTICIPATE value = [" + participateValue + "]");
        Utils.logMsg("+++ Virtual Form: current SUBMIT value = [" + submitValue + "]");
        
        if (! virtualFormName.equals(virtualFormEntry.getVirtualFormName())) {
            errMsg.append("Virtual Form: NAME value should be [" + virtualFormEntry.getVirtualFormName() + 
                "], but it's [" + virtualFormName + "]");
        }
        if (! participateValue.equals(virtualFormEntry.getParticipateValue())) {
            if (errMsg.length() > 0) errMsg.append(" ");
            errMsg.append("Virtual Form: PARTICIPATE value should be [" + virtualFormEntry.getParticipateValue() + 
                "], but it's [" + participateValue + "]");
        }
        if (! submitValue.equals(virtualFormEntry.getSubmitValue())) {
            if (errMsg.length() > 0) errMsg.append(" ");
            errMsg.append("Virtual Form: SUBMIT value should be [" + virtualFormEntry.getSubmitValue() + 
                "], but it's [" + submitValue + "]");
        }
        if (errMsg.length() > 0) {
            throw new RuntimeException(errMsg.toString());
        }
    }
    
    private int findVirtualFormRow(JTableOperator jTableOp, String virtualFormName) {
        int rowNum = jTableOp.findCellRow(virtualFormName);
        Util.wait(500);
        new QueueTool().waitEmpty();
        if (rowNum < 0) {
            addNewVirtualForm();
            rowNum = jTableOp.getRowCount() - 1;
        }
        jTableOp.setRowSelectionInterval(rowNum, rowNum);
        Util.wait(500);
        new QueueTool().waitEmpty();
        return rowNum;
    }
            
    private void addNewVirtualForm() {
        new JButtonOperator(dialogOp, BUTTON_LABEL_NEW).pushNoBlock();
        Util.wait(500);
        new QueueTool().waitEmpty();
    }
    
    private void closeVirtualFormDialog() {
        new JButtonOperator(dialogOp, BUTTON_LABEL_OK).pushNoBlock();
        Util.wait(500);
        new QueueTool().waitEmpty();
    }
    
    //========================================================================//
    private class VirtualFormEntry {
        private String virtualFormName, componentID, participateValue, submitValue;
        
        public String getVirtualFormName() {return virtualFormName;}
        public String getComponentID() {return componentID;}
        public String getParticipateValue() {return participateValue;}
        public String getSubmitValue() {return submitValue;}

        public void setVirtualFormName(String virtualFormName) {
            this.virtualFormName = virtualFormName.trim();
        }
        public void setComponentID(String componentID) {
            this.componentID = componentID.trim();
        }
        public void setSubmitValue(String submitValue) {
            this.submitValue = submitValue.trim().equalsIgnoreCase(VIRTUAL_FORM_YES) ? 
                VIRTUAL_FORM_YES : VIRTUAL_FORM_NO;
        }
        public void setParticipateValue(String participateValue) {
            this.participateValue = participateValue.trim().equalsIgnoreCase(VIRTUAL_FORM_YES) ? 
                VIRTUAL_FORM_YES : VIRTUAL_FORM_NO;
        }

        public String toString() {
            return "{" + getComponentID() + "," +
                         getVirtualFormName() + "," +
                         getParticipateValue() + "," +
                         getSubmitValue() + "}";
        }
    }
}
