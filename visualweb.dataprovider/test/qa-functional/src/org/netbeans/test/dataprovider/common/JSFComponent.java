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
package org.netbeans.test.dataprovider.common;

import java.awt.*;
import javax.swing.tree.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.designer.*;
import org.netbeans.modules.visualweb.gravy.toolbox.*;
        
public abstract class JSFComponent implements Constants {
    protected String componentName, componentID, dbTableName;
    protected Point componentPoint;
    
    protected void putVisualComponentOnDesigner(int x, int y) {
        Utils.putComponentOnDesigner(PALETTE_NAME_BASIC, this.componentName, this.componentID,
            x, y, NAVIGATOR_TREE_NODE_FORM_PREFIX);
    }

    protected void changeComponentID(String newComponentID) { // component should be selected
        Utils.setTextPropertyValue(PROPERTY_NAME_ID, newComponentID);
        Utils.logMsg("+++ Component [" + componentName + "(" + componentID + 
            ")] has got new value of property [" + PROPERTY_NAME_ID + "] = [" + newComponentID + "]");
        componentID = newComponentID;
    }
    
    protected Point getComponentPoint() { // component should be selected
        Point componentPoint = Utils.getSelectedComponentLeftUpperPoint();
        if (componentPoint == null) {
            throw new RuntimeException("Problem with getting of coordinates of [" + 
                componentName + "(" + componentID + ")]");
        }
        Utils.logMsg("+++ Component [" + componentID + "] coordinates: [" + 
            componentPoint.x + ", " + componentPoint.y + "]");
        return componentPoint;
    }
    
    protected void putDBTableOnComponent() { 
        String dbURL = TestPropertiesHandler.getDatabaseProperty("DB_URL");
        Utils.putDBTableOnComponent(dbURL, dbTableName, componentPoint);
        String 
            rowSetName = NAVIGATOR_TREE_NODE_SESSION_PREFIX + Utils.getBaseRowSetName(dbTableName),
            dataProviderName = NAVIGATOR_TREE_NODE_PAGE_PREFIX + Utils.getBaseDataProviderName(dbTableName);
        checkRowSetAppearance(rowSetName);
        checkDataProviderAppearance(dataProviderName);
    }

    protected void checkRowSetAppearance(String rowSetNodeName) {
        StringBuffer errMsg = new StringBuffer();
        if (Utils.findNavigatorTreeNode(rowSetNodeName, 3500, false) == null) {
            errMsg.append("The tree node [" + rowSetNodeName + "] isn't found in the window [Navigator]");
        } else {
            Utils.logMsg("+++ CachedRowSet [" + rowSetNodeName + "] is found in the window [Navigator]");
        }
        if (errMsg.length() > 0) {
            throw new RuntimeException(errMsg.toString());
        }
    }
    
    protected void checkDataProviderAppearance(String dataProviderNodeName) {
        StringBuffer errMsg = new StringBuffer();
        if (Utils.findNavigatorTreeNode(dataProviderNodeName, 3500, false) == null) {
            errMsg.append((errMsg.length() > 0 ? " " : "") +
                "The tree node [" + dataProviderNodeName + "] isn't found in the window [Navigator]");
        } else {
            Utils.logMsg("+++ DataProvider [" + dataProviderNodeName + "] is found in the window [Navigator]");
        }
        if (errMsg.length() > 0) {
            throw new RuntimeException(errMsg.toString());
        }
    }
    
    protected void bindComponentDataProvider(String dataProviderName) { 
        callPopupMenuItem(POPUP_MENU_ITEM_BIND_DATA);
        try {
            JDialogOperator dialogOp = null;
            try {
                dialogOp = new JDialogOperator(DIALOG_TITLE_BIND_DATA);
            } catch(TimeoutExpiredException tee) {
                Util.wait(500);
                new QueueTool().waitEmpty();
                throw new RuntimeException("Dialog [" + DIALOG_TITLE_BIND_DATA + "] didn't appear");
            }
            if (dialogOp != null) {
                Utils.logMsg("+++ Dialog [" + DIALOG_TITLE_BIND_DATA + "] is found");
                
                JComboBoxOperator comboboxOp = new JComboBoxOperator(dialogOp, 0);
                comboboxOp.selectItem(dataProviderName);
                Util.wait(1500);
                new QueueTool().waitEmpty();
                Utils.logMsg("+++ Combobox item [" + dataProviderName + 
                    "] is selected for component [" + componentID + "]");
                
                JListOperator listOp = new JListOperator(dialogOp, 0);
                listOp.selectItem(1);
                String listSelectedValue = listOp.getSelectedValue().toString();
                Util.wait(1500);
                new QueueTool().waitEmpty();
                Utils.logMsg("+++ List item [" + listSelectedValue + 
                    "] is selected for component [" + componentID + "]");

                new JButtonOperator(dialogOp, BUTTON_LABEL_OK).pushNoBlock();
                Util.wait(1000);
                new QueueTool().waitEmpty();
            }
        } catch(Exception e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }        
        Util.wait(1000);
        new QueueTool().waitEmpty();
        Utils.doSaveAll();
    }
    
    protected void callPopupMenuItem(String menuItemLabel) {
        Utils.callPopupMenuOnNavigatorTreeNode(NAVIGATOR_TREE_NODE_FORM_PREFIX + componentID, menuItemLabel);
    }
}
