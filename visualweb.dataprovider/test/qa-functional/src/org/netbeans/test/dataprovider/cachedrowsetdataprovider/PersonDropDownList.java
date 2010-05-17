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
import javax.swing.tree.*;
import org.netbeans.jemmy.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.designer.*;
import org.netbeans.modules.visualweb.gravy.toolbox.*;
import org.netbeans.test.dataprovider.common.*;
        
public class PersonDropDownList extends JSFComponent {
    public PersonDropDownList() {
        this("dropDown1"); // default DropDownList id
    }
    
    public PersonDropDownList(String componentID) {
        componentName = COMPONENT_DROP_DOWN_LIST_NAME;
        this.componentID = componentID;
        dbTableName = DB_TABLE_PERSON;    
    }
    
    public String makePersonDropDownList() {
        String errMsg = null;
        try {
            putVisualComponentOnDesigner(25, 25); 
            // now DropDownList component is selected in Navigator
            String newDropDownID = TestPropertiesHandler.getTestProperty(
                "ID_DropDownList_For_DBTablePerson");
            changeComponentID(newDropDownID); // component should be selected
            componentPoint = getComponentPoint(); // component should be selected
            Utils.doSaveAll();
            putDBTableOnComponent();
            callPopupMenuItem(POPUP_MENU_ITEM_AUTO_SUBMIT);
            Utils.doSaveAll();
            
            VirtualFormConfigurator virtFormConfigurator = new VirtualFormConfigurator("Virtual_Form_Person");
            virtFormConfigurator.configureVirtualForm();
            Utils.doSaveAll();
        } catch (Exception e) {
            e.printStackTrace(Utils.logStream);
            errMsg = (e.getMessage() == null ? e.toString() : e.getMessage());
        }
        return errMsg;
    }
    
    public String modifyJavaCode() {
        String errMsg = null;
        try {
            insertEventHandlerJavaCode();
            insertPrerenderJavaCode();
        } catch (Exception e) {
            e.printStackTrace(Utils.logStream);
            errMsg = (e.getMessage() == null ? e.toString() : e.getMessage());
        }
        return errMsg;
    }
    
    private void insertEventHandlerJavaCode() {
        String javaCode = TestPropertiesHandler.getTestProperty(
            "personDD_EventHandler_ProcessValueChange");
        // change string "/n/" to new_line character "\n"
        javaCode = javaCode.replace("/n/", "\n");
        
        if (Utils.isUsedDBOracle() || Utils.isUsedDBPostgres()) {
            javaCode = javaCode.replace(dbTableName + ".", ""); // remove DB table name from java code
        }
        callPopupMenuItem(POPUP_MENU_ITEM_PROCESS_VALUE_CHANGE);
        
        EditorOperator editor = Utils.getJavaEditor();
        editor.pushDownArrowKey();
        Util.wait(500);
        new QueueTool().waitEmpty();
        editor.insert(javaCode);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        Util.getMainMenu().pushMenuNoBlock(MAIN_MENU_ITEM_SOURCE_FIX_IMPORTS);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        Utils.doSaveAll();
        
        String editorText = editor.getText();
        Utils.logMsg("+++ Source java code: [" + editorText + "]");
        if (! editorText.contains(javaCode)) {
            throw new RuntimeException("Java Editor Code doesn't contain java code: [" + javaCode + "]");
        }
        DesignerPaneOperator designPaneOp = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designPaneOp.switchToDesignerPane();
        /*
        //=====================        
        // workaround: 
        // if getVerification() returns "true", waiter, defined in the method 
        // JTextComponentOperator.waitText(String, int), won't find inserted
        // javaCodeText
        javaEditor.txtEditorPane().setVerification(false); 
        //=====================        
        */        
    }
    
    private void insertPrerenderJavaCode() {
        String javaCode = TestPropertiesHandler.getTestProperty("prerenderMethod");
        // change string "/n/" to new_line character "\n"
        javaCode = javaCode.replace("/n/", "\n");
        
        if (Utils.isUsedDBOracle() || Utils.isUsedDBPostgres()) {
            javaCode = javaCode.replace(dbTableName + ".", ""); // remove DB table name from java code
        }

        DesignerPaneOperator designPaneOp = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designPaneOp.switchToJavaSource();
        
        EditorOperator editor = Utils.getJavaEditor();
        editor.setCaretPosition(METHOD_DECLARATION_PRERENDER, true);
        
        editor.pushDownArrowKey();
        Util.wait(500);
        new QueueTool().waitEmpty();
        editor.insert(javaCode);
        Util.wait(500);
        new QueueTool().waitEmpty();

        Util.getMainMenu().pushMenuNoBlock(MAIN_MENU_ITEM_SOURCE_FIX_IMPORTS);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        Utils.doSaveAll();
        
        String editorText = editor.getText();
        Utils.logMsg("+++ Source java code: [" + editorText + "]");
        if (! editorText.contains(javaCode)) {
            throw new RuntimeException("Java Editor Code doesn't contain java code: [" + javaCode + "]");
        }
        designPaneOp = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designPaneOp.switchToDesignerPane();
        /*
        //=====================        
        // workaround: 
        // if getVerification() returns "true", waiter, defined in the method 
        // JTextComponentOperator.waitText(String, int), won't find inserted
        // javaCodeText
        javaEditor.txtEditorPane().setVerification(false); 
        //=====================        
        */        
    }
}
