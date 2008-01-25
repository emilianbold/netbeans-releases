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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


/*
 * OptionsOperator.java
 *
 * Created on March 13, 2006, 6:05 PM
 *
 * 
 */

package org.netbeans.test.umllib.util;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.umllib.exceptions.ElementVerificationException;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.exceptions.UMLCommonException;

/**
 *
 * @author sp153251
 */
public class OptionsOperator extends JDialogOperator {
    //
    public static final String STANDART_TITLE="Options";
    public static final String ADVANCED_TITLE="Advanced Options";
    //
    private static final String BUTTON_TO_ADVANCED=ADVANCED_TITLE;
    private static final String BUTTON_TO_STANDART="Basic Options";
    private static final String COMMON_INVOKE_MENU="Tools|Options";
    private static final String MAC_INVOKE_MENU="NetBeans|Preferences...";
    //
    private static final String SEARCH_MAC_BY="mac";
    //
    private boolean advanced;
    private TreeTableOperator _treeTable;
    /** Creates a new instance of OptionsOperator
     * Tries to find either advanced or standart options dialog
     */
    public OptionsOperator() {
        super(new ChooseOptionDialog());
        advanced=ADVANCED_TITLE.equals(getTitle());
        waitComponentShowing(true);
        waitComponentVisible(true);
    }
    /**
     * @return returns true if current OptionsOperator represent advanced options dialog
     */
    boolean isAdvanced()
    {
        return advanced && isShowing();
    }
    /**
     * Close options dialog with applying options (OK in standart and Close in advanced)
     */
    public void close()
    {
        String btnName="OK";
        if(advanced)btnName="Close";
        new JButtonOperator(this,btnName).push();
    }
    /**
     * Press "Advanced Options" in standart options dialog or do nothing in advanced dialog
     * @return returns OptionsOperator for advanced dialog
     */
    public OptionsOperator invokeAdvanced()
    {
        if(advanced)return this;
        new JButtonOperator(this,BUTTON_TO_ADVANCED).pushNoBlock();
        //WORKAROUND BUG ??
        long tmp=JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        long tmp2=JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000); 
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 1000);
        try
        {
            new JButtonOperator(new JDialogOperator("Question"),"Yes").pushNoBlock();
        }
        catch(Exception ex)
        {
        }
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", tmp2);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", tmp); 
        //WORKAROUN FINISHED
        waitClosed();
        return new OptionsOperator();
    }
    
   /**
     * Press "Basic Options" in advanced options dialog or do nothing in standart dialog
     * @return returns OptionsOperator for standart dialog
     */
    public OptionsOperator invokeBasic()
    {
        if(!advanced)return this;
        new JButtonOperator(this,BUTTON_TO_STANDART).push();
        waitClosed();
        advanced=false;
        return new OptionsOperator();
    }
    
    /** Getter for table containing property list and
     * property definition levels.
     * @return TreeTableOperator instance
     */
    public TreeTableOperator treeTable() {
        if(_treeTable == null) {
            _treeTable = new TreeTableOperator(this);
        }
        return _treeTable;
    }
    
    /**
     * Open Options dialog by main menu
     * @return OptionsOperator representing Options  Dialog
     */
    public static OptionsOperator invoke()
    {
        MainWindowOperator mw=MainWindowOperator.getDefault();
        JMenuBarOperator mainbar=new JMenuBarOperator(mw);
        //
        boolean macOs=System.getProperty("os.name").toLowerCase().indexOf(SEARCH_MAC_BY)>-1;
        //menu depends on platform
        String menuName="";
        if(macOs)
        {
            new Action(null, null, new Action.Shortcut(',', 4)).perform();
        }
        else
        {
            mainbar.pushMenu(COMMON_INVOKE_MENU);
        }
        new EventTool().waitNoEvent(500);
        return new OptionsOperator();
    }
    
    /**
     * set values to option in advanced view
     * @param keys - array of key in format {{nodePath,key1,key2..},{..
     * @param values - array of values in format {{null,value1,value2...},..
     * @throw qa.uml.exceptions.UMLCommonException in case if there is no advanced options active
     */
     public void setAdvancedValues(String[][] keys,String[][] values)
     {
         if(!isAdvanced())throw new UMLCommonException("Advanced options isn't active.");
         int i=0,j=1;
         int minLen=Math.min(keys.length,values.length);
            while(i<minLen) {
                String path=keys[i][0];
                //
                if(keys[i].length>1 && values[i].length>1)
                {
                    setAdvancedValue(path,keys[i][j],values[i][j]);
                    //
                }
                //
                j++;
                int min2=Math.min(keys[i].length,values[i].length);
                if(j>=min2)
                {
                    j=1;
                    i++;
                }
            }
         
     }
     
    /**
     * set values to option in advanced view
     * @param path - nodePath
     * @param key - name of propety
     * @param value - value to be set
     * @throw qa.uml.exceptions.UMLCommonException in case if there is no advanced options active
     */
     public void setAdvancedValue(String path,String key,String value)
     {
            if(!isAdvanced())throw new UMLCommonException("Advanced options isn't active.");
            //
            TreePath pth=treeTable().tree().findPath(path);
            if(pth==null)throw new NotFoundException("Can't find "+path);
            //
            JPopupMenuOperator f;
            //
            if(!pth.equals(treeTable().tree().getSelectionPath()))
            {
                treeTable().tree().scrollToPath(pth);
                treeTable().tree().selectPath(pth);
                treeTable().tree().waitSelected(pth);
            }
            //
            new EventTool().waitNoEvent(1000);
            PropertySheetOperator ps=new PropertySheetOperator(this);
            //
            Property pr=new Property(ps,key);
            if(pr.getValue()==null)throw new ElementVerificationException("Bad value in "+path+"|"+key);
            //set property
            pr.setValue(value);
            //change focus
            treeTable().tree().clickOnPath(pth);
     }
     
    /**
     * set values to option in advanced view
     * @param path - nodePath
     * @param key - name of propety
     * @return value
     * @throw qa.uml.exceptions.UMLCommonException in case if there is no advanced options active
     */
     public String getAdvancedValue(String path,String key)
     {
            if(!isAdvanced())throw new UMLCommonException("Advanced options isn't active.");
            //
            TreePath pth=treeTable().tree().findPath(path);
            if(pth==null)throw new NotFoundException("Can't find "+path);
            //
            JPopupMenuOperator f;
            //
            if(!pth.equals(treeTable().tree().getSelectionPath()))
            {
                treeTable().tree().scrollToPath(pth);
                treeTable().tree().selectPath(pth);
                treeTable().tree().waitSelected(pth);
            }
            //
            new EventTool().waitNoEvent(1000);
            PropertySheetOperator ps=new PropertySheetOperator(this);
            //
            Property pr=new Property(ps,key);
            if(pr.getValue()==null)throw new ElementVerificationException("Bad value in "+path+"|"+key);
            //
            return pr.getValue();
      }

    public static class ChooseOptionDialog implements ComponentChooser
    {
        /**
         * 
         * @param component 
         * @return 
         */
        public boolean checkComponent(java.awt.Component component) {
            if(component instanceof java.awt.Dialog)
            {
                java.awt.Dialog dlg=(java.awt.Dialog)component;
                return OptionsOperator.STANDART_TITLE.equals(dlg.getTitle()) || OptionsOperator.ADVANCED_TITLE.equals(dlg.getTitle());
            }
            else return false;
        }

        /**
         * 
         * @return 
         */
        public String getDescription() {
            return "Find dialog with '"+OptionsOperator.STANDART_TITLE+"' or '"+OptionsOperator.ADVANCED_TITLE+"' title.";
        }

    }
    
    
    /**
     * all uml options items (0) with default (1) and other possible values
     * avoid changing order, tests may refer to items by index
     * TBD
     */
    /*public static final HashMap<String,String[][]> UML_OPTIONS=new HashMap<String,String[][]>();
    {
         UML_OPTIONS.put("UML",
                 new String[][]
                       {
                           {"Automatically Hide Modeling Window","Yes","No"},
                           {"Delete File when Deleting Artifacts","Ask Me","Always","Naver"},
                           {"Show Aliases","No","Yes"},
                           {"Prompt to Save Diagram","Yes","No"},
                           {"Prompt to Save Project","Yes","No"},
                           {"Don't Show Filter Warning Dialog","Ask Me","Always"}
                        });
         UML_OPTIONS.put("UML|Code Generation",
                 new String[][]
                        {
                            {"Show Process Output","Yes","No"}
                        });
         UML_OPTIONS.put("UML|Design Center",
                 new String[][]{});
         UML_OPTIONS.put
                        ("UML|Design Center|Design Pattern Catalog",
                 new String[][]
                        {
                            {"Overwrite Existing Participants","Ask Me","Always","Never"}
                        });
         UML_OPTIONS.put("UML|Diagrams",
                 new String[][]
                        {
                            {"Ask Before Layout"},
                            {"Automatically Size Elements"},
                            {"Display Compartment Titles"},
                            {"Display Edit Control Tooltips"},
                            {"Display Empty Lists"},
                            {"Reconnect to Presentation Boundary"},
                            {"Resize with Show Aliases Mode"},
                            {"Show Stereotype Icons"}
                        });
         UML_OPTIONS.put("UML|Diagrams|Activity",
                 new String[][]
                        {
                            {"Indicate Interruptible Edges"}
                        });
         UML_OPTIONS.put("UML|Diagrams|Collaboration",
                 new String[][]
                        {
                            "Delete Connector Messages",
                            "Show Message Numbers"
                        });
         UML_OPTIONS.put("UML|Diagrams|Sequence",
                 new String[][]
                        {
                            "Automatically Create Classifiers",
                            "Classifier Type to Create",
                            "Delete Combined Fragment Messages",
                            "Group Operations by Classifier",
                            "New Message Action",
                            "Move Invoked Operation",
                            "Show Interaction Boundary as Created",
                            "Show Message Numbers"
                        });
         UML_OPTIONS.put("UML|Display",
                 new String[][]
                        {
                            "Tagged Values"
                        });
         UML_OPTIONS.put("UML|Expansion variables",
                 new String[][]
                        {
                            "Configuration Location"
                        });
         UML_OPTIONS.put("UML|Find Dialog",
                 new String[][]
                        {
                            "Allow Lengthy Searches"
                        });
         UML_OPTIONS.put("UML|Find Dialog|Displayed Columns",
                 new String[][]
                        {
                            "Alias",
                            "Fully Scoped Name",
                            "Icon",
                            "ID",
                            "Name",
                            "Project",
                            "Type"
                        });
         UML_OPTIONS.put("UML|Information Logging",
                 new String[][]
                        {
                            "Log File Name and Location",
                            "Log Messages",
                            "Log Errors",
                            "Log Exceptions",
                            "Log Informational Messages",
                            "Log Method Entry",
                            "Log Method Exit"
                        });
         UML_OPTIONS.put("UML|New Project",
                 new String[][]
                        {
                            "Create New Diagram",
                            "Default Diagram Name",
                            "Default Element Name"
                        });
         UML_OPTIONS.put("UML|New Project|Unknown Classifier",
                 new String[][]
                        {
                            "Create Classifier",
                            "Type to Create"
                        });
         UML_OPTIONS.put("UML|New Project|Unknown Stereotype",
                 new String[][]
                        {
                            "Automatically Create"
                        });
         UML_OPTIONS.put("UML|Notify When Namespace is Deleted",
                 new String[][]
                        {
                            "Actor Elements",
                            "Association Class Elements",
                            "Class Elements",
                            "Design Pattern Role Elements",
                            "Interface Elements",
                            "Package Elements",
                            "Use Case Elements"
                        });
         UML_OPTIONS.put("UML|Presentation",
                 new String[][]
                        {
                            "Documentation Font",
                            "Grid Font",
                            "Set Global Colors and Fonts"
                        });
         UML_OPTIONS.put("UML|Reverse Engineering",
                 new String[][]
                        {
                            "Show Process Output"
                        });
         UML_OPTIONS.put("UML|Reverse Engineering|Operation Elements",
                 new String[][]
                        {
                            "Create New Operation",
                            "Prompt for Source Folders",
                            "Source Folders Config File"
                        });
         UML_OPTIONS.put("UML|Code Engineering",
                 new String[][]
                        {
                            "Replace Conflicting Elements",
                            "Transform When Code May Be Lost",
                            "Warn When Impacted Count Reaches"
                        });
         UML_OPTIONS.put("UML|Code Engineering|Elements",
                 new String[][]
                        {
                            "Aggregation",
                            "Association",
                            "Association End",
                            "Attribute",
                            "Class",
                            "Generalization",
                            "Implementation",
                            "Interface",
                            "Multiplicity",
                            "Multiplicity Range",
                            "Navigable End",
                            "Operation",
                            "Package",
                            "Parameter",
                            "Project",
                            "Template Parameter",
                            "Enumeration",
                            "Enumeration Literal"
                        });
         UML_OPTIONS.put("UML|Code Engineering|Java",
                 new String[][]
                        {
                            "Capitalize Attribute Name in Accessors",
                            "Create Accessor Methods",
                            "Create Constructor Methods",
                            "Create Finalize Methods",
                            "Display Duplicate Operation Dialog",
                            "Modify Redefined Operations",
                            "Name Navigable Ends",
                            "Prefix for Member Attributes",
                            "Prefix for Read Accessors",
                            "Prefix for Write Accessors",
                            "Remove Prefix from Accessor Names",
                            "Collection Override"
                        });
         UML_OPTIONS.put("UML|UML Properties Pane",
                 new String[][]
                        {
                            "Default Filter",
                            "Display Type Fully Scoped Name",
                            "Maximum Display"
                        });
    }*/
    
    public void addWebBrowser(String name, String process ) {
        invokeBasic();
        (new JButtonOperator(this, "Edit...")).pushNoBlock();
        JDialogOperator webBrowerDialog = new JDialogOperator("Web Browsers");
        (new JButtonOperator(webBrowerDialog, "Add...")).pushNoBlock();
        JTextFieldOperator nameField = new JTextFieldOperator(webBrowerDialog, 0);
        nameField.clearText();
        nameField.typeText(name);
        JTextFieldOperator processField = new JTextFieldOperator(webBrowerDialog, 1);
        processField.clearText();
        processField.typeText(process);
        try{Thread.sleep(1000);}catch(Exception ex){}
        (new JButtonOperator(webBrowerDialog, "OK")).pushNoBlock();
        try{Thread.sleep(1000);}catch(Exception ex){}
        
    }

}

