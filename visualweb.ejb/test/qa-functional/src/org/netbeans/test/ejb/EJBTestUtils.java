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

package org.netbeans.test.ejb;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.*;
import junit.framework.*;

import org.netbeans.modules.visualweb.gravy.DocumentOutlineOperator;
import org.netbeans.modules.visualweb.gravy.properties.editors.*;
import org.netbeans.modules.visualweb.gravy.properties.*;
import org.netbeans.modules.visualweb.gravy.model.deployment.*;
import org.netbeans.modules.visualweb.gravy.model.*;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.*;
import org.netbeans.modules.visualweb.gravy.designer.*;
import org.netbeans.modules.visualweb.gravy.toolbox.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.Bundle;

import org.netbeans.modules.visualweb.ejb.ui.*;
import com.meterware.httpunit.*;
import javax.swing.tree.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;

/**
 * @author Roman Mostyka
 */
public class EJBTestUtils {
    
    public static final String EJB_JARS_PATH = System.getProperty("xtest.ejb_jars.dir");
    public static final String RMI_IIOP_PORT_APPSERVER = "3700";
    
    public static final String ejbNode = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.nodes.Bundle","ENTERPRISE_JAVA_BEANS");
    
    public static final String sessionEJBPopup = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.actions.Bundle","ADD_EJB_GROUP");
    public static final String importEJBPopup = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.actions.Bundle","IMPORT_EJB_DATASOURCE");
    public static final String exportEJBPopup = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.actions.Bundle","EXPORT_EJB_DATASOURCE");
    public static final String exportEJBsPopup = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.actions.Bundle","EXPORT_ALL_EJB_DATASOURCES");
    public static final String modifyPopup = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.actions.Bundle","MODIFY_EJB_GROUP");
    public static final String refreshPopup = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.actions.Bundle","REFRESH");
    public static final String removePopup = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.actions.Bundle","DELETE");
    public static final String exportPopup = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.actions.Bundle","EXPORT_EJB_DATASOURCE");
    public static final String configurePopup = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.actions.Bundle","CONFIGURE_EJB_METHODS");
    public static final String addToPagePopup = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.actions.Bundle","ADD_SESSION_BEAN_TO_PAGE");
    public static final String addBindingAttribute = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.insync.action.Bundle","LBL_AddBindingAttributeActionName");
    
    public static final String dlg_sessionEJB = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.ui.Bundle","ADD_EJB_GROUP");
    public static final String dlg_exportEJB = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.ui.Bundle","EXPORT_EJB_DATASOURCES");
    public static final String dlg_importEJB = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.ui.Bundle","IMPORT_EJB_DATASOURCES");
    public static final String dlg_modifyEJB = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.ui.Bundle","MODIFY_EJB_GROUP");
    public static final String dlg_removeConfirm = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.actions.Bundle","DELETE_DIALOG_TITLE");
    public static final String dlg_Error = "Error";
    public static final String dlg_save_export_file_name = "Save";
    public static final String dlg_open_import_file_name = "Open";
    public static final String dlg_question = "Question";
    public static final String dlg_information = "Information";
    public static final String dlg_export_help = "Help";
    
    public static final String msg_noEJBsFound = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.load.Bundle","NO_SESSION_EJBS_FOUND");
    public static final String msg_skipped = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.load.Bundle","SKIP_NO_PACKAGE_EJBS");
    public static final String msg_noDescriptor = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.load.Bundle","NO_DEPLOYMENT_DESCRIPTOR");
    public static final String msg_emptyJAR = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.ui.Bundle","EMPTY_CLIENT_JAR");
    
    private static String javaVersion = System.getProperty("java.version");
    private static String osName = System.getProperty("os.name");
    
    public static final String 
        btn_Clear = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.ui.Bundle","CLEAR_BUTTON_LABEL"),
        btn_SelectAll = Bundle.getStringTrimmed("org.netbeans.modules.visualweb.ejb.ui.Bundle","SELECT_ALL_BUTTON_LABEL"),
        btn_Cancel = "Cancel",
        btn_Add = "Add...",
        btn_Next = "Next",
        btn_Back = "Back",
        btn_Open = (javaVersion.indexOf("1.6") != -1 && osName.indexOf("Linux") != -1)?"OK":"Open",
        btn_Save = (javaVersion.indexOf("1.6") != -1 && osName.indexOf("Linux") != -1)?"OK":"Save",
        btn_Finish = "Finish",
        btn_Browse = "Browse...",
        btn_Yes = "Yes",
        btn_No = "No",
        btn_OK = "OK",
        btn_Help = "Help";

    private static final String 
        WEB_PAGE_TITLE = "Page1",
        JAVA_EDITOR_TITLE = "Page1.java",
        MENU_ITEM_RUN_PROJECT = "Run",
        MENU_ITEM_REFRESH = "Refresh",
        MENU_ITEM_UNDEPLOY = "Undeploy",
        MENU_ITEM_CLOSE_PROJECT = "Close";

    private static final String 
        PROP_NAME_EJB_SET_NAME  = "Name",
        PROP_NAME_RMI_IIOP_PORT = "RMI-IIOP Port",
        PROP_NAME_SERVER_HOST   = "Server Host";

    private static final String 
        WEB_PAGE_FORM_NAME = "form1";
    private static final int WEB_RESPONSE_CODE_OK = 200;
    private static final long WEB_RESPONSE_RECEIVING_DELAY = 10000;
    
    private static final String DEFAULT_EXPORT_NAME= "exported_ejb_datasources.jar";
    
    public static ServerNavigatorOperator server;
    public static JTreeOperator sntree;
    public static DesignerPaneOperator designer;

    private static String lastCreatedPrjName, lastTextField_ID;
    private static String serverType = "GlassFish V2";
    private static DeploymentTargetDescriptor dtd;
    private static DeploymentTarget dt;
    private static ApplicationServer as;
    private static String NODE_NAME_DEPLOYMENT_SERVER, NODE_NAME_DEPLOYED_COMPONENTS;
    
    public static String getLastCreatedPrjName() {return lastCreatedPrjName;}
    public static String getLastTextField_ID() {return lastTextField_ID;}
    
    public static ApplicationServer addApplicationServer() {
        dtd = new DeploymentTargetDescriptor();
        dtd.load();
        if (serverType != null) dtd.setProperty(dtd.SERVER_TYPE_KEY, serverType);
        dt = IDE.getIDE().addDeploymentTarget(dtd);
        as = (ApplicationServer) dt;
        NODE_NAME_DEPLOYMENT_SERVER   = "Servers|" + as.getName();
        NODE_NAME_DEPLOYED_COMPONENTS = "Servers|" + as.web_applications_path;
        return as;
    }
    
    public static ApplicationServer getApplicationServer() {
        if (as != null) return as;
        else return addApplicationServer();
    }
    
    public static void checkPopupMenuItemList(JPopupMenuOperator menuOperator, String[] menuItemList) {
        for (int i = 0; i < menuItemList.length; ++i) {
            checkPopupMenuItem(menuOperator, menuItemList[i]);
        }
    }
    
    private static void checkPopupMenuItem(JPopupMenuOperator menuOperator, String menuItemLabel) {
        if (TestUtils.findPopupMenuItemByLabel(menuOperator, 
            menuItemLabel, false, false) == null) {
            Assert.fail("Popup menu doesn't contain the item [" + menuItemLabel + "]");
        } else {
            TestUtils.outMsg("+++ Popup menu contains the item [" + menuItemLabel + "]");
        }
    }
   
    public static void addEJB(String EJBName, String host, String port, String pathToJAR, String pathToEAR) {
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        sntree = server.getTree();
        server.pushPopup(ejbNode, sessionEJBPopup);
        Util.wait(1000);
        //JDialogOperator addDialog = new JDialogOperator(dlg_sessionEJB);
        JDialogOperator addDialog = new JDialogOperator();
        Util.wait(1000);
        if (!EJBName.equals("")) new JTextFieldOperator(addDialog, 0).setText(EJBName);
        if (!host.equals("")) new JTextFieldOperator(addDialog, 1).setText(host);
        if (!port.equals("")) new JTextFieldOperator(addDialog, 2).setText(port);
        Util.wait(1000);
        if (!pathToEAR.equals("")) {
            new JButtonOperator(addDialog, btn_Browse).pushNoBlock();
            Util.wait(1000);
            JDialogOperator dlg_open = new JDialogOperator("Open");
	    if (System.getProperty("os.name").equals("Mac OS X")) {
	        String[] splittedPath = pathToEAR.substring(1, pathToEAR.length()).split(File.separator);
                TestUtils.wait(1000);
	        JComboBoxOperator cbRoot = new JComboBoxOperator(dlg_open);
	        for (int i = 0; i < cbRoot.getItemCount(); i++)
	            if (cbRoot.getItemAt(i).toString().equals(File.separator)) {
		        cbRoot.setSelectedIndex(i);
		        break;
	            }
	        String toCompare = "";
	        for (int i = 0; i < splittedPath.length; i++) {
                    JTableOperator jtoPath = new JTableOperator(dlg_open);
		    toCompare += "/" + splittedPath[i];
		    System.out.println("toComapre="+toCompare);
		    Point cell = jtoPath.findCell(toCompare, new Operator.DefaultStringComparator(true, true), 0);
		    jtoPath.clickOnCell((int) cell.getY(), (int) cell.getX(), 2);
		    TestUtils.wait(500);
                }
            } else {
                new JTextFieldOperator(dlg_open, 0).setText(pathToEAR);
                Util.wait(1000);
                new JButtonOperator(dlg_open, btn_Open).pushNoBlock();
                Util.wait(1000);
	    }
        }
        if (!pathToJAR.equals("")) {
            new JButtonOperator(addDialog, btn_Add).pushNoBlock();
            Util.wait(1000);
            JDialogOperator dlg_open = new JDialogOperator("Open");
            if (System.getProperty("os.name").equals("Mac OS X")) {
	        String[] splittedPath = pathToJAR.substring(1, pathToJAR.length()).split(File.separator);
                TestUtils.wait(1000);
	        JComboBoxOperator cbRoot = new JComboBoxOperator(dlg_open);
	        for (int i = 0; i < cbRoot.getItemCount(); i++)
	            if (cbRoot.getItemAt(i).toString().equals(File.separator)) {
		        cbRoot.setSelectedIndex(i);
		        break;
	            }
	        String toCompare = "";
	        for (int i = 0; i < splittedPath.length; i++) {
                    JTableOperator jtoPath = new JTableOperator(dlg_open);
		    toCompare += "/" + splittedPath[i];
		    System.out.println("toComapre="+toCompare);
		    Point cell = jtoPath.findCell(toCompare, new Operator.DefaultStringComparator(true, true), 0);
		    jtoPath.clickOnCell((int) cell.getY(), (int) cell.getX(), 2);
		    TestUtils.wait(500);
                }
            } else {
                JTextFieldOperator textField = new JTextFieldOperator(dlg_open, 0);
                textField.setText(pathToJAR);
                Util.wait(1000);
            
                new JButtonOperator(dlg_open, btn_Open).pushNoBlock();
                Util.wait(1000);
	    }
        }
        new JButtonOperator(addDialog, btn_Next).pushNoBlock();
        Util.wait(1500);
        new QueueTool().waitEmpty();
    }
    
    public static void addEJB() {
        addEJB("", "", "", "", "");
    }
    
    public static void addEJB(String pathToJAR) {
        addEJB("", "", "", pathToJAR, "");
    }
    
    public static void addEJB(String pathToJAR, String pathToEAR) {
        addEJB("", "", "", pathToJAR, pathToEAR);
    }
    
    public static void addEJB(String port, String pathToJAR, String pathToEAR) {
        addEJB("", "", port, pathToJAR, pathToEAR);
    }
    
    public static void addEJB(String EJBName, String port, String pathToJAR, String pathToEAR) {
        addEJB(EJBName, "", port, pathToJAR, pathToEAR);
    }
    
    public static void endAddEJB() {
        //new JButtonOperator(new JDialogOperator(dlg_sessionEJB), btn_Finish).pushNoBlock();
        new JButtonOperator(new JDialogOperator(), btn_Finish).pushNoBlock();
        Util.wait(3500);
        new QueueTool().waitEmpty();
    }
    
    public static void removeEJB(String EJBname) {
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        server.pushPopup(ejbNode + "|" + EJBname, removePopup);
        Util.wait(1000);
        new JButtonOperator(new JDialogOperator(dlg_removeConfirm), btn_Yes).pushNoBlock();
        Util.wait(2000);
    }
    
    public static void modifyEJBSet(String EJBSetName, int textFieldNumber, String textFieldValue) {
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        server.pushPopup(ejbNode + "|" + EJBSetName, modifyPopup);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        JDialogOperator dlg_modify = new JDialogOperator(dlg_modifyEJB);
        new JTextFieldOperator(dlg_modify, textFieldNumber).setText(textFieldValue);
        Util.wait(1000);
        new JButtonOperator(dlg_modify, btn_OK).pushNoBlock();
        Util.wait(2000);
        new QueueTool().waitEmpty();
    }
    
    public static String verifyModifiedEJBSet(String EJBSetName, String[] propertyValues) {
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        server.pushPopup(ejbNode + "|" + EJBSetName, modifyPopup);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        JDialogOperator dlg_modify = new JDialogOperator(dlg_modifyEJB);
        String errMsg = null;
        for (int i = 0; i < propertyValues.length; ++i) {
            errMsg = verifyModifiedEJBSetProperty(dlg_modify, i, propertyValues[i]);
            if (errMsg != null) {
                break;
            }
        }
        new JButtonOperator(dlg_modify, btn_OK).pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        return errMsg;
    }
    
    private static String verifyModifiedEJBSetProperty(JDialogOperator dlg_modify, 
        int textFieldNumber, String controlValue) {
        String errMsg = null;

        String propertyValue = new JTextFieldOperator(dlg_modify, textFieldNumber).getText();
        TestUtils.outMsg("+++ Value of text field [" + textFieldNumber + "] = [" + 
            propertyValue + "], control value = [" + controlValue + "]");
        if (!propertyValue.equals(controlValue)) {
            errMsg = "+++ Value of text field [" + textFieldNumber + "] = [" + 
            propertyValue + "] not equals the control value = [" + controlValue + "]";
        }
        return errMsg;
    }
    
    public static String exportEJBSet(String ejbSetName, String exportFileName, boolean verification) {
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        server.pushPopup(ejbNode + "|" + ejbSetName, exportEJBPopup);
        Util.wait(1000);
        
        JDialogOperator dlg_export = new JDialogOperator(dlg_exportEJB);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        String errMsg = null;
        if (verification) {
            if (!isEJBSetSelected(new JListOperator(dlg_export), ejbSetName)) {
                errMsg = "EJB Set [" + ejbSetName + "] is not selected in the dialog [" + 
                    dlg_exportEJB + "]";
            }
        }
        if (errMsg == null) {
            exportFileName = defineExportFileName(dlg_export, exportFileName);
            new JButtonOperator(dlg_export, btn_OK).pushNoBlock();
            Util.wait(1200);
            new QueueTool().waitEmpty();
            
            JDialogOperator dialog = new JDialogOperator(dlg_export);
            // if dialog "Question" appears (overwrite an existing file), press OK
            if (dialog.getTitle().equals(dlg_question)) {
                new JButtonOperator(dialog, btn_OK).pushNoBlock();
                Util.wait(1200);
                new QueueTool().waitEmpty();
            }
            new JButtonOperator(new JDialogOperator(dlg_information), btn_OK).pushNoBlock();
            Util.wait(1500);
            new QueueTool().waitEmpty();
            
            if (!new File(exportFileName).exists()) {
                errMsg = "Export file [" + exportFileName + "] of EJB Set [" + ejbSetName + "] not found";
            } else {
                TestUtils.outMsg("+++ Export file [" + exportFileName + "] of EJB Set [" + ejbSetName + "] exists");
            }
        } else {
            new JButtonOperator(new JDialogOperator(dlg_information), btn_Cancel).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
        }        
        return errMsg;
    }
    
    private static String defineExportFileName(JDialogOperator dialogExport, String exportFileName) {
            if (!exportFileName.equals("")) {
                new JButtonOperator(dialogExport, btn_Browse).pushNoBlock();
                Util.wait(1000);
                JDialogOperator dlg_save = new JDialogOperator(dlg_save_export_file_name);
                Util.wait(1000);
                new JTextFieldOperator(dlg_save, 0).setText(exportFileName);
                Util.wait(1000);
                new JButtonOperator(dlg_save, btn_Save).pushNoBlock();
                Util.wait(1000);
                new QueueTool().waitEmpty();
            } else {
                exportFileName = new JTextFieldOperator(dialogExport, 0).getText();
            }
            return exportFileName;
    }
    
    public static String exportEJBSet(String ejbSetName, boolean verification) {
        return exportEJBSet(ejbSetName, "", verification);
    }
    
    public static String exportRequiredEJBSets(String[] ejbSetNames) {
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        java.util.List ejbSetNameList = getAvailableEJBSetNameList();
        
        server.pushPopup(ejbNode, exportEJBsPopup);
        Util.wait(1000);
        
        JDialogOperator dlg_export = new JDialogOperator(dlg_exportEJB);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        new JButtonOperator(dlg_export, btn_Clear).pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        String errMsg = checkAllEJBSetsSelected(dlg_export, ejbSetNameList, false);
        
        if (errMsg == null) {
            JListOperator listOperator = new JListOperator(dlg_export);
            for (int i = 0; i < ejbSetNames.length; ++i) {
                String ejbSetName = ejbSetNames[i];
                selectEJBSetInList(listOperator, ejbSetName);
                if (!isEJBSetSelected(listOperator, ejbSetName)) {
                    errMsg = "EJB Set [" + ejbSetName + "] is not selected in the dialog [" + 
                        dlg_exportEJB + "]";
                    break;
                }
            }
        }
        if (errMsg == null) {
            String exportFileName = defineExportFileName(dlg_export, "");
            new JButtonOperator(dlg_export, btn_OK).pushNoBlock();
            Util.wait(1200);
            new QueueTool().waitEmpty();
            
            JDialogOperator dialog = new JDialogOperator(dlg_export);
            // if dialog "Question" appears (overwrite an existing file), press OK
            if (dialog.getTitle().equals(dlg_question)) {
                new JButtonOperator(dialog, btn_OK).pushNoBlock();
                Util.wait(1200);
                new QueueTool().waitEmpty();
            }
            new JButtonOperator(new JDialogOperator(dlg_information), btn_OK).pushNoBlock();
            Util.wait(1500);
            new QueueTool().waitEmpty();
            
            if (!new File(exportFileName).exists()) {
                errMsg = "Export file [" + exportFileName + "] for EJB Sets " + 
                    Arrays.asList(ejbSetNames) + " not found";
            } else {
                TestUtils.outMsg("+++ Export file [" + exportFileName + "] for EJB Sets " + 
                    Arrays.asList(ejbSetNames) + " exists");
            }
        } else {
            new JButtonOperator(new JDialogOperator(dlg_information), btn_Cancel).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
        }        
        return errMsg;
    }

    public static String exportAllEJBSets(String ejbTreeNodePath, java.util.List ejbSetNameList, 
        String exportFileName, boolean clickButtonsClearSelect, boolean verification) {
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        server.pushPopup(ejbTreeNodePath, (ejbTreeNodePath.equals(ejbNode) ? 
            exportEJBsPopup : exportEJBPopup));
        Util.wait(1000);
        
        JDialogOperator dlg_export = new JDialogOperator(dlg_exportEJB);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        String errMsg = null;
        if (clickButtonsClearSelect) {
            new JButtonOperator(dlg_export, btn_SelectAll).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
            if (verification) {
                errMsg = checkAllEJBSetsSelected(dlg_export, ejbSetNameList, true);
            }
        }
        if (clickButtonsClearSelect && (errMsg == null)) {
            new JButtonOperator(dlg_export, btn_Clear).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
            if (verification) {
                errMsg = checkAllEJBSetsSelected(dlg_export, ejbSetNameList, false);
            }
        }
        if (clickButtonsClearSelect && (errMsg == null)) {
            new JButtonOperator(dlg_export, btn_SelectAll).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
        }
        if (verification && (errMsg == null)) {
            errMsg = checkAllEJBSetsSelected(dlg_export, ejbSetNameList, true);
        }
        if (errMsg == null) {
            exportFileName = defineExportFileName(dlg_export, exportFileName);
            new JButtonOperator(dlg_export, btn_OK).pushNoBlock();
            Util.wait(1200);
            new QueueTool().waitEmpty();
            
            JDialogOperator dialog = new JDialogOperator(dlg_export);
            // if dialog "Question" appears (overwrite an existing file), press OK
            if (dialog.getTitle().equals(dlg_question)) {
                new JButtonOperator(dialog, btn_OK).pushNoBlock();
                Util.wait(1200);
                new QueueTool().waitEmpty();
            }
            new JButtonOperator(new JDialogOperator(dlg_information), btn_OK).pushNoBlock();
            Util.wait(1500);
            new QueueTool().waitEmpty();
            
            if (!new File(exportFileName).exists()) {
                errMsg = "Export file [" + exportFileName + "] of all EJB Sets not found";
            } else {
                TestUtils.outMsg("+++ Export file [" + exportFileName + "] of all EJB Sets exists");
            }
        } else {
            new JButtonOperator(new JDialogOperator(dlg_information), btn_Cancel).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
        }        
        return errMsg;
    }
    
    public static String exportAllEJBSets(String ejbTreeNodePath, java.util.List ejbSetNames, 
        boolean clickButtonsClearSelect, boolean verification) {
        return exportAllEJBSets(ejbTreeNodePath, ejbSetNames, "", clickButtonsClearSelect, verification);
    }
    
    public static java.util.List getAvailableEJBSetNameList() {
        java.util.List ejbSetNameList = new ArrayList();
        // make a list of names of available EJB Sets
        sntree = ServerNavigatorOperator.showNavigatorOperator().getTree();
        TreePath treePath = sntree.findPath(ejbNode);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
        for (int i = 0; i < treeNode.getChildCount(); ++i) {
            String ejbSetNodeName =  treeNode.getChildAt(i).toString();
            ejbSetNameList.add(ejbSetNodeName);
            TestUtils.outMsg("+++ Available EJB Set = [" + ejbSetNodeName + "]");
        }
        TestUtils.outMsg("+++ List of available EJB Sets = " + ejbSetNameList);
        return ejbSetNameList;
    }
    
    public static String importEJBSets(String importFileName, String[] ejbSetNames, 
        boolean doClear, boolean verification) {
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        server.pushPopup(ejbNode, importEJBPopup);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        JDialogOperator dlg_open = new JDialogOperator(dlg_open_import_file_name);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (!importFileName.equals("")) {
            if (System.getProperty("os.name").equals("Mac OS X")) {
                String[] splittedPath = importFileName.substring(1, importFileName.length()).split(File.separator);
                TestUtils.wait(1000);
                JComboBoxOperator cbRoot = new JComboBoxOperator(dlg_open);
                for (int i = 0; i < cbRoot.getItemCount(); i++)
                    if (cbRoot.getItemAt(i).toString().equals(File.separator)) {
                        cbRoot.setSelectedIndex(i);
                        break;
                    }
                String toCompare = "";
                for (int i = 0; i < splittedPath.length; i++) {
                    JTableOperator jtoPath = new JTableOperator(dlg_open);
                    toCompare += "/" + splittedPath[i];
                    System.out.println("toComapre="+toCompare);
                    Point cell = jtoPath.findCell(toCompare, new Operator.DefaultStringComparator(true, true), 0);
                    jtoPath.clickOnCell((int) cell.getY(), (int) cell.getX(), 2);
                    TestUtils.wait(500);
                }
            } else {
                new JTextFieldOperator(dlg_open, 0).setText(importFileName);
                Util.wait(1000);
                new QueueTool().waitEmpty();
            }
        }        
        if (System.getProperty("os.name").equals("Mac OS X")) {
            JTableOperator jtoPath = new JTableOperator(dlg_open);
            Point cell = jtoPath.findCell(
                System.getProperty("user.home") + File.separator + DEFAULT_EXPORT_NAME, 
                new Operator.DefaultStringComparator(true, true), 0);
            jtoPath.clickOnCell((int) cell.getY(), (int) cell.getX(), 2);
            TestUtils.wait(500);
        } else {
            new JButtonOperator(dlg_open, btn_Open).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
        }
        
        JDialogOperator dlg_import = new JDialogOperator(dlg_importEJB);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        if (doClear) {
            new JButtonOperator(dlg_import, btn_Clear).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();

            JListOperator listOperator = new JListOperator(dlg_import);
            for (int i = 0; i < ejbSetNames.length; ++i) {
                String ejbSetName = ejbSetNames[i];
                selectEJBSetInList(listOperator, ejbSetName);
            }
        }
        String errMsg = null;
        if (verification) {
            errMsg = checkAllEJBSetsSelected(dlg_import, Arrays.asList(ejbSetNames), true);
        }
        Util.wait(500);
        new QueueTool().waitEmpty();

        new JButtonOperator(dlg_import, btn_OK).pushNoBlock();
        Util.wait(2000);
        new QueueTool().waitEmpty();
        
        if (errMsg == null) {
            server = ServerNavigatorOperator.showNavigatorOperator();
            Util.wait(1000);
            new QueueTool().waitEmpty();
            sntree = server.getTree();
            Util.wait(1000);
            
            for (int i = 0; i < ejbSetNames.length; ++i) {
                String ejbSetName = ejbSetNames[i];
                try {
                    sntree.findPath(ejbNode + "|" + ejbSetName);
                    TestUtils.outMsg("+++ EJB-subnode [" + ejbSetName + 
                        "] has been found under tree node [" + ejbNode + "]");
                    
                    EJBTestUtils.removeEJB(ejbSetName);
                } catch (TimeoutExpiredException tee) {
                    errMsg = "EJB-subnode [" + ejbSetName + "] not found under tree node [" + ejbNode + "]";
                    break;
                }
            }
        }
        return errMsg;
    }
    
    public static boolean isEJBSetSelected(JListOperator jlo, String EJBSetName) {
        boolean result = false;
        ListCellRenderer lcr = ((JList) jlo.getSource()).getCellRenderer();
        int item_count = ((ListModel) jlo.getModel()).getSize();
        for (int i = 0; i < item_count; ++i) {
            Object objListElement = jlo.getModel().getElementAt(i);
            Component listElement = lcr.getListCellRendererComponent((JList) jlo.getSource(), 
                objListElement, i, ((PortableEjbDataSource) objListElement).isPortable(), true);
            
            TestUtils.outMsg("+++ " + (((PortableEjbDataSource) objListElement).getName() + 
                " is selected = [" + ((JCheckBox) listElement).isSelected() + "]"));
            
            boolean ejbSetFound = (((PortableEjbDataSource) objListElement).getName().equals(EJBSetName));
            result = ejbSetFound && (((JCheckBox) listElement).isSelected());
            if (ejbSetFound) break;
        }
        return result;
    }
    
    public static String checkAllEJBSetsSelected(JDialogOperator dialog, 
        java.util.List ejbSetNameList, boolean shouldSelected) {
        JListOperator listOperator = new JListOperator(dialog);
        String errMsg = null;
        for (int i = 0; i < ejbSetNameList.size(); ++i) {
            String ejbSetName = (String) ejbSetNameList.get(i);
            if (isEJBSetSelected(listOperator, ejbSetName) != shouldSelected) {
                errMsg = (shouldSelected ? "Not all" : "All") + 
                    " EJB Sets are selected in the dialog [" + dialog.getTitle() + 
                    "]: EJB Set [" + ejbSetName + "]";
                break;
            }
        }
        return errMsg;
    }
    
    public static void selectEJBSetInList(JListOperator jlo, String ejbSetName) {
        ListCellRenderer lcr = ((JList) jlo.getSource()).getCellRenderer();
        int item_count = ((ListModel) jlo.getModel()).getSize();
        for (int i = 0; i < item_count; ++i) {
            Object objListElement = jlo.getModel().getElementAt(i);
            String elementName = ((PortableEjbDataSource) objListElement).getName();
            Component cr = lcr.getListCellRendererComponent((JList) jlo.getSource(), 
                objListElement, i, ((PortableEjbDataSource) objListElement).isPortable(), true);
            if (elementName.equals(ejbSetName)) {
                TestUtils.outMsg("+++ Current value [Portable] of [" + elementName + "] = [" + 
                    ((PortableEjbDataSource) objListElement).isPortable() + "]");
                TestUtils.outMsg("+++ Current value [Selected] of [" + elementName + "] = [" + 
                    ((JCheckBox) cr).isSelected() + "]");
                
                //jlo.clickMouse(cr.getPreferredSize().height/2,(cr.getPreferredSize().height*(2*i+1))/2,1);
                ((PortableEjbDataSource) objListElement).setIsPortable(!((PortableEjbDataSource) objListElement).isPortable());
                ((JCheckBox) cr).setSelected(((PortableEjbDataSource) objListElement).isPortable());
                jlo.repaint();
                Util.wait(1500);
                new QueueTool().waitEmpty();
                
                objListElement = jlo.getModel().getElementAt(i);
                cr = lcr.getListCellRendererComponent((JList) jlo.getSource(), 
                    objListElement, i, ((PortableEjbDataSource) objListElement).isPortable(), true);
                TestUtils.outMsg("+++ New value [Portable] of [" + elementName + "] = [" + 
                    ((PortableEjbDataSource) objListElement).isPortable() + "]");
                TestUtils.outMsg("+++ New value [Selected] of [" + elementName + "] = [" + 
                    ((JCheckBox) cr).isSelected() + "]");
            }
        }
        Util.wait(2000);
        new QueueTool().waitEmpty();
    }
    
    public static void addEJBToPage(String EJBSetName, String EJBName) {
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        sntree = server.getTree();
        sntree.selectPath(sntree.findPath(ejbNode + "|" + EJBSetName + "|" + EJBName));
        Util.wait(2000);
        server.pushPopup(ejbNode + "|" + EJBSetName + "|" + EJBName, addToPagePopup);
        Util.wait(5000);
    }
    
    public static void changeEjbSetProperties(String newEjbSetName, String newRMIPort, String newServerHost) {
        Util.getMainMenu().pushMenuNoBlock("Window|Properties");
        Util.wait(1000);
        new QueueTool().waitEmpty();
         
        PropertySheetOperator pso = new PropertySheetOperator(RaveWindowOperator.getDefaultRave());
        new QueueTool().waitEmpty();
        Util.wait(500);

        PropertySheetTabOperator propertyTable = new PropertySheetTabOperator(pso);
        propertyTable.setComparator(new Operator.DefaultStringComparator(true, true));
        new QueueTool().waitEmpty();
        Util.wait(500);
        
        Property pr = new Property(propertyTable, PROP_NAME_EJB_SET_NAME);
        pr.setValue(newEjbSetName);
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        pr = new Property(propertyTable, PROP_NAME_SERVER_HOST);
        pr.setValue(newServerHost);
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        pr = new Property(propertyTable, PROP_NAME_RMI_IIOP_PORT);
        pr.setValue(newRMIPort);
        Util.wait(500);
        new QueueTool().waitEmpty();
    }
    
    public static String createProjectWithEJB(String ejbSetName,
        String ejbBusinessMethodName, String[] ejbLibJarNames, 
        String ejbReturnedValue, boolean isComparisonStrict, boolean closeProject) {
        String prjName = TestUtils.createNewJSFProject(),
               prjPath = TestUtils.getPathLastCreatedProject();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        TestUtils.outMsg("+++ Project Path = [" + prjPath + "]");
        TestUtils.outMsg("+++ Project Name = [" + prjName + "]");

        lastCreatedPrjName = prjName; // remember name of created project
        
        ServerNavigatorOperator serverNavigator = ServerNavigatorOperator.showNavigatorOperator();
        JTreeOperator tree = serverNavigator.getTree();
        
        String strEjbSetPath = ejbNode + "|" + ejbSetName;
        TreePath treePath = tree.findPath(strEjbSetPath);
        
        // get a name of the 1st EJB: a name of the 1st child subnode (the 1st EJB) of tree node [EJB Set]
        TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
        for (int i = 0; i < treeNode.getChildCount(); ++i) {
            TestUtils.outMsg("+++ EJB[" + i + "] of EJB Set [" + ejbSetName + "] = " + treeNode.getChildAt(i));
        }
        String ejbName = treeNode.getChildAt(0).toString();
        String strEjbPath = strEjbSetPath + "|" + ejbName;
        
        treePath = tree.findPath(strEjbPath);
        JPopupMenuOperator popupMenu = new JPopupMenuOperator(tree.callPopupOnPath(treePath));
        
        JMenuItemOperator menuItem = new JMenuItemOperator(popupMenu, addToPagePopup);
        menuItem.doClick();
        Util.wait(500);
        popupMenu.pressKey(KeyEvent.VK_ESCAPE);
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        // check Application Outline:
        String ejbClientName = checkAppOutlineWindowForEJB(ejbName);
        // check Projects | Library:
        checkProjectsWindowForEJB(prjName, ejbLibJarNames);
        
        // add Text Field and modify Page1.java to check availability of EJB's business method
        String textField_ID = putTextFieldOnDesigner();
        addBindingAttribute(WEB_PAGE_TITLE, "page1|html1|body1|form1|" + textField_ID);
        lastTextField_ID = textField_ID; // remember name of added text field

        addJavaCodeUsingEJBBusinessMethod(textField_ID, ejbClientName, ejbBusinessMethodName);

        //=====================        
        // workaround:
        // when EJB is added to web-page, a value of a project property 
        // "display.browser" will be replaced by "true" instead of "false"
        TestUtils.disableBrowser(prjName, prjPath, true);
        //=====================        
        String errMsg = checkDeploymentWithEJB(prjName, textField_ID, ejbReturnedValue, isComparisonStrict);
        doSaveAll();
        if (closeProject) doCloseProject(prjName);
        
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        return errMsg;
    }

    public static String checkDeploymentWithEJB(String prjName, String textField_ID, 
        String ejbReturnText, boolean isComparisonStrict) {
        deployProject(prjName);
        String errMsg = checkDeployment(prjName, textField_ID, ejbReturnText, isComparisonStrict);
        undeployProject(prjName);
        return errMsg;
    }
    
    private static void deployProject(String prjName) {
        /*
        ProjectNavigatorOperator projectNavigator = ProjectNavigatorOperator.pressPopupItemOnNode(prjName, 
            MENU_ITEM_RUN_PROJECT);
        */
        Util.getMainWindow().deploy();
        Util.wait(1000);
        new QueueTool().waitEmpty();

        Util.wait(20000);
        new QueueTool().waitEmpty();
        
        ServerNavigatorOperator serverNavigator = ServerNavigatorOperator.showNavigatorOperator();
        JTreeOperator tree = serverNavigator.getTree();
        Util.wait(500);
        
        serverNavigator.pushPopup(NODE_NAME_DEPLOYMENT_SERVER, MENU_ITEM_REFRESH);
        Util.wait(5000);
        serverNavigator.pushPopup(NODE_NAME_DEPLOYED_COMPONENTS, MENU_ITEM_REFRESH);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        TestUtils.outMsg("+++ Project [" + prjName + "] has been deployed");
    }
    
    private static String checkDeployment(String prjName, String textField_ID, String ejbReturnedValue, 
        boolean isComparisonStrict) {
        String errMsg = null;
        try {
            //String requestPrefix = TestUtils.getRequestPrefix(600000);
            String requestPrefix = as.requestPrefix + prjName;
            TestUtils.outMsg("+++ requestPrefix = " + requestPrefix);

            WebResponse response = getWebResponseAfterDeployment(requestPrefix);
            
            Util.wait(1500);
            String contentData = response.getText();
            TestUtils.outMsg("+++ Web response after project deployment: " +
                "content type = [" + response.getContentType() + "], " +
                "response code = [" + response.getResponseCode() + "], " +
                "content data = [" + contentData + "]");
            if (!isWebResponseOK(response)) {
                errMsg = "Result of deployment: web application isn't running correctly (web response code " +
                    "is " + response.getResponseCode() + " instead of " + EJBTestUtils.WEB_RESPONSE_CODE_OK + ")";
                return errMsg;
            }

            // check values of a text field
            String textfieldValue = (String) getTextFieldValueWaiter(textField_ID).waitAction(response);
            TestUtils.outMsg("+++ Value of " + textField_ID + " = [" + textfieldValue + "]");
            Util.wait(1000);

            boolean isComparisonOK = (isComparisonStrict ? 
                textfieldValue.equals(ejbReturnedValue) : 
                textfieldValue.indexOf(ejbReturnedValue) > -1);
            if (!isComparisonOK) {
                errMsg = "Value of " + textField_ID + " [" + textfieldValue + "] " +
                    "is different from value [" + ejbReturnedValue + "], expected from EJB method";
            } else {
                TestUtils.outMsg("+++ Value of " + textField_ID + " [" + 
                    textfieldValue + "] equals value, returned from EJB method");
                errMsg = null;
            }
        } catch (Throwable t) {
            String internalMsg = t.getMessage();
            if (internalMsg == null) {
                errMsg = t.toString();
            } else {
                t.printStackTrace();
                errMsg = internalMsg;
            }
        } finally {
            return errMsg;
        }
    }
    
    private static void undeployProject(String prjName) {
        ServerNavigatorOperator serverNavigator = ServerNavigatorOperator.showNavigatorOperator();
        JTreeOperator tree = serverNavigator.getTree();
        Util.wait(1000);
        String deployedApplicationNodeName = NODE_NAME_DEPLOYED_COMPONENTS + "|" + 
            prjName;
        TestUtils.outMsg("+++ Undeploying Application Node = " + deployedApplicationNodeName);
        
        serverNavigator.pushPopup(NODE_NAME_DEPLOYED_COMPONENTS, MENU_ITEM_REFRESH);
        Util.wait(1000);
        serverNavigator.pushPopup(deployedApplicationNodeName, MENU_ITEM_UNDEPLOY);
        Util.wait(2000);
        new QueueTool().waitEmpty();

        TestUtils.outMsg("+++ Project [" + prjName + "] has been undeployed");
    }
    
    private static String checkAppOutlineWindowForEJB(String ejbName) {
        String ejbClientName = ejbName.substring(0, ejbName.indexOf("EJB")).toLowerCase() +
                "Client1";
        TestUtils.outMsg("+++ Application Outline EJB's node = " + ejbClientName);
        DocumentOutlineOperator outline = new DocumentOutlineOperator(RaveWindowOperator.getDefaultRave());
        JTreeOperator outlineTree = outline.getStructTreeOperator();
        Util.wait(500);
        new QueueTool().waitEmpty();
        outlineTree.findPath("Page1|" + ejbClientName);
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        return ejbClientName;
    }
    
    private static void checkProjectsWindowForEJB(String prjName, String[] ejbLibJarNames) {
        // check Projects | Library:
        if ((ejbLibJarNames != null) && (ejbLibJarNames.length > 0)) {
            ProjectNavigatorOperator prjNavigator = new ProjectNavigatorOperator();
            Util.wait(500);
            new QueueTool().waitEmpty();
            for (int i = 0; i < ejbLibJarNames.length; ++i) {
                prjNavigator.tree().findPath(prjName + "|Libraries|" + ejbLibJarNames[i]);
                Util.wait(100);
                new QueueTool().waitEmpty();
                TestUtils.outMsg("+++ EJB Jar[" + i + "] is found = [" + ejbLibJarNames[i] + "]");
            }
        } else {
            TestUtils.outMsg("+++ No EJB Jars were defined");
        }
    }
    
    private static String putTextFieldOnDesigner() {
        String id = null;
        showPalette();
        PaletteContainerOperator palette = new PaletteContainerOperator("Basic");
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        DesignerPaneOperator designer = getDesigner(WEB_PAGE_TITLE);
        palette.addComponent("Text Field", designer, new Point(48, 48));
        // doesn't work out: palette.dndPaletteComponent("Text Field", designer, new Point(48, 48));
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        SheetTableOperator sheetTable = getSheetTableOperator(true, true);
        id = sheetTable.getValue("id");
        new QueueTool().waitEmpty();
        Util.wait(500);
        TestUtils.outMsg("+++ new Text Field [" + id + "] is put on Designer");
        
        doSaveAll();
        return id;
    }
    
    public static void showPalette() {
        Util.wait(1000);
        new QueueTool().waitEmpty();
        String menuString = "Window|Palette";
        Util.getMainMenu().pushMenuNoBlock(menuString);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    private static void addJavaCodeUsingEJBBusinessMethod(String textField_ID,
        String ejbClientName, String ejbBusinessMethodName) {
        
        DesignerPaneOperator.switchToJavaSource();
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        EditorOperator javaEditor = getJavaEditor();
 
        // put editor's caret to the start position of the last line
        putCaretToBeginOfLastLine(javaEditor);
        Util.wait(500);
        new QueueTool().waitEmpty();

        String javaCodeText = getJavaCodeUsingEJBBusinessMethod(
            textField_ID, ejbClientName, ejbBusinessMethodName);

        //=====================        
        // workaround: 
        // if getVerification() returns "true", waiter, defined in the method 
        // JTextComponentOperator.waitText(String, int), won't find inserted
        // javaCodeText
        javaEditor.txtEditorPane().setVerification(false); 
        //=====================        
        
        javaEditor.txtEditorPane().typeText(javaCodeText);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        TestUtils.outMsg("+++ java code inserted into [" + JAVA_EDITOR_TITLE + "]: \n" + javaCodeText);

        //=====================        
        // workaround:
        // Java Editor will insert an extra line containing characters "}}", 
        // but it should be deleted.
        deleteLastLines(javaEditor, 2);
        //=====================        
        
        TestUtils.outMsg("+++ Java code of [" + JAVA_EDITOR_TITLE + "]: \n" + 
            javaEditor.txtEditorPane().getDisplayedText());
        doSaveAll();
    }
 
    private static String getJavaCodeUsingEJBBusinessMethod(String textField_ID,
        String ejbClientName, String ejbBusinessMethodName) {
        /* template of inserted java code block
            {
                try {
                    textField1.setText(greeterClient1.getGreeting());
                } catch (Exception e) {
                    textField1.setText(e.getClass().getName() + ": " + e.getMessage());
                }
            }
        */        
        String[] javaCode = new String[] {
            "    {",
            "        try {",
            "            " + textField_ID + ".setText(" + ejbClientName + "." + ejbBusinessMethodName + "());",
            "        } catch (Exception e) {",
            "            textField1.setText(e.getClass().getName() + \": \" + e.getMessage());",
            "        }",
            "    }"
        };
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < javaCode.length; ++i) {
            buffer.append(javaCode[i]);
            buffer.append("\n");
        }
        return buffer.toString();
    }
    
    public static void doSaveAll() {
        Util.saveAllAPICall();
        //Util.getMainMenu().pushMenuNoBlock("File|Save All");
        Util.wait(1000);
        new QueueTool().waitEmpty();
        TestUtils.outMsg("+++ [Save All] action has been performed");
    }

    public static void doCloseProject(String projectName) {
        ProjectNavigatorOperator projectNavigator = 
            ProjectNavigatorOperator.pressPopupItemOnNode(projectName, 
            MENU_ITEM_CLOSE_PROJECT);
        Util.wait(500);
        new QueueTool().waitEmpty();
    }
    
    private static DesignerPaneOperator getDesigner(String webPageTitle) {
        DesignerPaneOperator designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        return designer;
    }
    
    private static SheetTableOperator getSheetTableOperator(
            boolean strictComparison, boolean ignoreCase) {
        SheetTableOperator sto = new SheetTableOperator();
        sto.setCompareStyle(strictComparison, ignoreCase);
        return sto;
    }
    
    private static EditorOperator getJavaEditor() {
        EditorOperator editor = new EditorOperator(Util.getMainWindow(),JAVA_EDITOR_TITLE);
        new QueueTool().waitEmpty();
        Util.wait(1000);
        TestUtils.outMsg("+++ Source editor for [" + JAVA_EDITOR_TITLE + "] found");
        return editor;
    }
 
    private static void putCaretToBeginOfLastLine(EditorOperator editor) {
        if (System.getProperty("os.name").equals("Mac OS X"))
            editor.pushKey(KeyEvent.VK_END, KeyEvent.META_DOWN_MASK);
        else
            editor.pushKey(KeyEvent.VK_END, KeyEvent.CTRL_DOWN_MASK);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        for (int i = 0; i < 2; ++i) {
            editor.pushUpArrowKey();
            Util.wait(500);
            new QueueTool().waitEmpty();
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    private static void deleteLastLines(EditorOperator editor, int lineCount) {
        editor.pushKey(KeyEvent.VK_END, KeyEvent.CTRL_DOWN_MASK);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        for (int i = 0; i < lineCount; ++i) {
            editor.pushKey(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK);
            Util.wait(500);
            new QueueTool().waitEmpty();
        }
        editor.pushKey(KeyEvent.VK_DELETE, 0);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    private static WebResponse getWebResponseAfterDeployment(String requestPrefix) throws Throwable {
        WebResponse response = null;
        try {
            response = getResponseUsingWaiter(requestPrefix);
        } catch (TimeoutExpiredException tee) {
            throw new JemmyException("Web response for URL [" + requestPrefix + 
                "] has not been received in " + WEB_RESPONSE_RECEIVING_DELAY + 
                " milliseconds (see the log of Application Server)");
        } catch (Throwable t) {
            String internalMsg = t.getMessage();
            if (internalMsg == null) {
                String errInfoMsg = "Web response was not received for URL [" + 
                    requestPrefix + "] due to [" + t.toString() + "]";
                throw new JemmyException(errInfoMsg, t);
            } else {
                t.printStackTrace();
                throw t;
            }
        }
        return response;
    }
    
    private static WebResponse getResponseUsingWaiter(String requestPrefix) throws Exception {
        Waiter responseWaiter = getResponseWaiter(requestPrefix);
        return ((WebResponse) responseWaiter.waitAction(null));
    }
        
    private static Waiter getResponseWaiter(final String requestPrefix) {
        final WebConversation webConversation = new WebConversation();
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);

        Waiter responseWaiter = new Waiter(new Waitable() {
            public Object actionProduced(Object obj) {
                try {
                    WebResponse webResponse = webConversation.getResponse(requestPrefix);
                    return webResponse;
                } catch (Throwable t) {
                    t.printStackTrace();                    
                }
                return null;
            }
            public String getDescription() {
                return ("Receiving web response");
            }
        });
        responseWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", 
            WEB_RESPONSE_RECEIVING_DELAY);
        return responseWaiter;
    }
        
    private static boolean isWebResponseOK(WebResponse response) {
        if ((response != null) && (response.getResponseCode() == WEB_RESPONSE_CODE_OK)) {
            return true;
        }
        return false;
    }
    
    private static String makeWebFormComponentID(String componentID) {
        return (WEB_PAGE_FORM_NAME + ":" + componentID);
    }
     
    private static Waiter getTextFieldValueWaiter(final String textField_ID) {
        Waiter dropdownWaiter = new Waiter(new Waitable() {
            public Object actionProduced(Object response) {
                try {
                    String value = ((WebResponse) response).getForms()[0].getParameterValue(
                        makeWebFormComponentID(textField_ID));
                    return value;
                } catch (Exception e) {
                    e.printStackTrace();                    
                }
                return null;
            }
            public String getDescription() {
                return ("Getting values of Dropdown List...");
            }
        });
        dropdownWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", 5000);
        return dropdownWaiter;
    }
    
    public static int getLabelIndex() {
        return 9;
    }

    public static void addBindingAttribute(String page, String full_component_path) {
        DocumentOutlineOperator outline = new DocumentOutlineOperator(RaveWindowOperator.getDefaultRave());
        TestUtils.wait(1000);
        JTreeOperator aotree = outline.getStructTreeOperator();
        aotree.callPopupOnPath(aotree.findPath(page + "|" + full_component_path));
        TestUtils.wait(1000);
        JPopupMenuOperator aopm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        new JMenuItemOperator(aopm, addBindingAttribute).pushNoBlock();
        TestUtils.wait(1000);
    }
}
