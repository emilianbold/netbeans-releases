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
 * Abbreviations.java
 *
 * Created on 1/2/03 4:04 PM
 */
package org.netbeans.modules.visualweb.gravy.properties.editors;

import java.util.*;
import javax.swing.table.TableModel;
import org.netbeans.modules.visualweb.gravy.OptionsOperator;
import org.netbeans.modules.visualweb.gravy.properties.PropertySheetOperator;
import org.netbeans.modules.visualweb.gravy.properties.PropertySheetTabOperator;
import org.netbeans.modules.visualweb.gravy.properties.TextFieldProperty;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

import javax.swing.JDialog;
import org.netbeans.jellytools.Bundle;
import org.netbeans.modules.visualweb.gravy.NbDialogOperator;
import org.netbeans.jemmy.operators.*;
/** Class implementing all necessary methods for handling "Abbreviations" NbDialog.
 */
public class AbbreviationsEditorOperator extends NbDialogOperator {
    
    /** Creates new Abbreviations that can handle it.
     */
    public AbbreviationsEditorOperator() {
        super(java.util.ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("PROP_Abbreviations"));
    }
    
    /** Creates new StringArrayCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public AbbreviationsEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    private JTableOperator _tabAbbreviations;
    private JButtonOperator _btMetalScrollButton;
    private JButtonOperator _btMetalScrollButton2;
    private JButtonOperator _btAdd;
    private JButtonOperator _btEdit;
    private JButtonOperator _btRemove;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find null JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabAbbreviations() {
        if (_tabAbbreviations==null) {
            _tabAbbreviations = new JTableOperator(this);
        }
        return _tabAbbreviations;
    }
    
    /** Tries to find "" MetalScrollButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalScrollButton() {
        if (_btMetalScrollButton==null) {
            _btMetalScrollButton = new JButtonOperator(this, "");
        }
        return _btMetalScrollButton;
    }
    
    /** Tries to find "" MetalScrollButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalScrollButton2() {
        if (_btMetalScrollButton2==null) {
            _btMetalScrollButton2 = new JButtonOperator(this, "", 1);
        }
        return _btMetalScrollButton2;
    }
    
    /** Tries to find "Add..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd==null) {
            _btAdd = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("KBEP_Add"));
        }
        return _btAdd;
    }
    
    /** Tries to find "Edit..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btEdit() {
        if (_btEdit==null) {
            _btEdit = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("AEP_Edit"));
        }
        return _btEdit;
    }
    
    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            _btRemove = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("Remove"));
        }
        return _btRemove;
    }
    
    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("KBEP_OK_LABEL"));
        }
        return _btOK;
    }
    
    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.openide.explorer.propertysheet.Bundle").getString("CTL_Cancel"));
        }
        return _btCancel;
    }
    
    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.openide.explorer.propertysheet.Bundle").getString("CTL_Help"));
        }
        return _btHelp;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** clicks on "" MetalScrollButton
     */
    public void metalScrollButton() {
        btMetalScrollButton().push();
    }
    
    /** clicks on "" MetalScrollButton
     */
    public void metalScrollButton2() {
        btMetalScrollButton2().push();
    }
    
    /** clicks on "Add..." JButton
     */
    public void add() {
        btAdd().push();
    }
    
    /** clicks on "Edit..." JButton
     */
    public void edit() {
        btEdit().push();
    }
    
    /** clicks on "Remove" JButton
     */
    public void remove() {
        btRemove().push();
    }
    
    /** clicks on "OK" JButton
     */
    public void oK() {
        btOK().push();
    }
    
    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }
    
    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of Abbreviations by accessing all its components.
     */
    public void verify() {
        tabAbbreviations();
        btMetalScrollButton();
        btMetalScrollButton2();
        btAdd();
        btEdit();
        btRemove();
        btOK();
        btCancel();
        btHelp();
    }
    
    /**
     * Add specified abbreviation with specified expansion.
     * @param abbreviation Name of abbreviation.
     * @param expansion Name of expansion.
     */
    public void addAbbreviation(String abbreviation, String expansion) {
        btAdd().pushNoBlock();
        EnterAbbreviation enter = new EnterAbbreviation();
        enter.fillAbbreviation(abbreviation, expansion);
        enter.oK();
    }
    
    /**
     * Edit specified abbreviation.
     * @param abbreviationName Name of abbreviations.
     * @param newAbbreviationName New name of abbreviations.
     * @param newExpansion New name of expansion.
     */
    public boolean editAbbreviation(String abbreviationName, String newAbbreviationName, String newExpansion) {
        int row = tabAbbreviations().findCellRow(abbreviationName);
        
        if (row == (-1))
            return false;
        
        tabAbbreviations().selectCell(row, 0);
        btEdit().pushNoBlock();
        
        EnterAbbreviation enter = new EnterAbbreviation();
        
        enter.fillAbbreviation(newAbbreviationName, newExpansion);
        enter.oK();
        
        return true;
    }
    
    /**
     * Edit specified abbreviation or add if it isn't exist.
     * @param abbreviationName Name of abbreviations.
     * @param newAbbreviationName New name of abbreviations.
     * @param newExpansion New name of expansion.
     */
    public void addOrEditAbbreviation(String abbreviationName, String newAbbreviationName, String newExpansion) {
        if (!editAbbreviation(abbreviationName, newAbbreviationName, newExpansion))
            addAbbreviation(newAbbreviationName, newExpansion);
    }
    
    /**
     * Remove specified abbreviation.
     * @param abbreviation Name of abbreviations.
     * @return True if abbreviation was removed.
     */
    public boolean removeAbbreviation(String abbreviation) {
        int row = tabAbbreviations().findCellRow(abbreviation,
        new Operator.DefaultStringComparator(true, true));
        
        if (row == (-1))
            return false;
        
        tabAbbreviations().selectCell(row, 0);
        btRemove().pushNoBlock();
        
        return true;
    }
    
    /**
     * Get map of abbreviations.
     * @return Map of abbreviations.
     */
    public Map listAbbreviations() {
        TableModel model = tabAbbreviations().getModel();
        int rowCount = model.getRowCount();
        Map result = new HashMap();
        
        for (int cntr = 0; cntr < rowCount; cntr++) {
            result.put((String) model.getValueAt(cntr, 0), (String) model.getValueAt(cntr, 1));
        }
        
        return result;
    }
    
    /**
     * Get abbreviations editor.
     * @return AbbreviationsEditorOperator.
     */
    public static AbbreviationsEditorOperator invoke(String editorName) {
        OptionsOperator options = OptionsOperator.invoke();
        new EventTool().waitNoEvent(500);

	options.advanced();        
        new EventTool().waitNoEvent(500);

        options.selectOption(ResourceBundle.getBundle("org/netbeans/core/Bundle").getString("UI/Services/Editing")+
        "|"+ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("OPTIONS_all")+
        "|" + editorName);
        new EventTool().waitNoEvent(500);
        PropertySheetOperator property = new PropertySheetOperator(options);
        PropertySheetTabOperator psto = new PropertySheetTabOperator(property);
        new TextFieldProperty(psto,ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("PROP_Abbreviations")).openEditor();
        
        AbbreviationsEditorOperator abbs = new AbbreviationsEditorOperator();
        
        options.close(); /*??? not sure whether this is assured to work*/
        return abbs;
    }
    
    /**
     * Add specified abbreviation with specified expansion with specified editor.
     * @param editorName Name of editor.
     * @param abbreviation Name of abbreviation.
     * @param expansion Name of expansion.
     */
    public static void addAbbreviation(String editorName, String abbreviation, String expansion) {
        AbbreviationsEditorOperator instance = invoke(editorName);
        
        instance.addAbbreviation(abbreviation, expansion);
        instance.oK();
    }
    
    /**
     * Edit specified abbreviation or add if it isn't exist with specified editor.
     * @param editorName Name of editor.
     * @param abbreviationName Name of abbreviations.
     * @param newAbbreviationName New name of abbreviations.
     * @param newExpansion New name of expansion.
     */
    public static void addOrEditAbbreviation(String editorName, String abbreviationName, String newAbbreviationName, String newExpansion) {
        AbbreviationsEditorOperator instance = invoke(editorName);
        
        instance.addOrEditAbbreviation(abbreviationName, newAbbreviationName, newExpansion);
        instance.oK();
    }
    
    /**
     * Remove specified abbreviation with specified editor.
     * @param editorName Name of editor.
     * @param abbreviation Name of abbreviations.
     * @return True if abbreviation was removed.
     */
    public static boolean removeAbbreviation(String editorName, String abbreviation) {
        AbbreviationsEditorOperator instance = invoke(editorName);
        boolean       result   = instance.removeAbbreviation(abbreviation);
        
        instance.oK();
        return result;
    }
    
    /**
     * Edit specified abbreviation with specified editor.
     * @param editorName Name of editor.
     * @param abbreviationName Name of abbreviations.
     * @param newAbbreviationName New name of abbreviations.
     * @param newExpansion New name of expansion.
     */
    public static boolean editAbbreviation(String editorName, String abbreviationName, String newAbbreviationName, String newExpansion) {
        AbbreviationsEditorOperator instance = invoke(editorName);
        boolean       result   = instance.editAbbreviation(abbreviationName, newAbbreviationName, newExpansion);
        
        instance.oK();
        return result;
    }
    
    /**
     * Get map of abbreviations.
     * @param editorName Name of AbbreviationsEditor.
     * @return Map of abbreviations.
     */
    public static Map listAbbreviations(String editorName) {
        AbbreviationsEditorOperator instance = invoke(editorName);
        Map           result   = instance.listAbbreviations();
        
        instance.oK();
        
        return result;
    }
    
    /** Performs simple test of Abbreviations
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        AbbreviationsEditorOperator.addAbbreviation("Java Editor","aaa","All abbrev");
        /*try {
            //java.io.PrintWriter pw= new java.io.PrintWriter(new java.io.FileWriter("/tmp/abbrevs.java"));
            Map map=Abbreviations.listAbbreviations(java.util.ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("OPTIONS_java"));
            java.util.Iterator keys=map.keySet().iterator();
            String key;
            while (keys.hasNext()) {
                key=(String)(keys.next());
                System.out.println(key+" "+map.get(key));
                //  pw.println("new Abbreviation(\""+key+"\", \""+map.get(key)+"\", \"\", \"\"),");
            }
            //pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }
}

