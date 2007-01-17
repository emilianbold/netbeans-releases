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

/*
 * Abbreviations.java
 *
 * Created on 1/2/03 4:04 PM
 */
package org.netbeans.jellytools.modules.editor;

import java.util.*;
import javax.swing.table.TableModel;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "Abbreviations" NbDialog.
 *
 * @author  Jan Lahoda
 * @author Max Sauer
 * @version 1.1
 */
public class Abbreviations extends JDialogOperator {
    
    /** Creates new Abbreviations that can handle it.
     */
    public Abbreviations() {
        super(java.util.ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("PROP_Abbreviations"));
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
    
    public void addAbbreviation(String abbreviation, String expansion) {
        new EventTool().waitNoEvent(500);
        btAdd().pushNoBlock();
        EnterAbbreviation enter = new EnterAbbreviation();
        enter.fillAbbreviation(abbreviation, expansion);
        enter.oK();
    }
    
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
    
    public void addOrEditAbbreviation(String abbreviationName, String newAbbreviationName, String newExpansion) {
        if (!editAbbreviation(abbreviationName, newAbbreviationName, newExpansion))
            addAbbreviation(newAbbreviationName, newExpansion);
    }
    
    public boolean removeAbbreviation(String abbreviation) {
        int row = tabAbbreviations().findCellRow(abbreviation,
                new Operator.DefaultStringComparator(true, true));
        
        if (row == (-1)) {
            System.out.println("Didn't find "+abbreviation);
            TableModel model = tabAbbreviations().getModel();
            int rowCount = model.getRowCount();
            for (int cntr = 0; cntr < rowCount; cntr++) {
                System.out.print(model.getValueAt(cntr, 0)+" ");
            }
            System.out.println("");
            return false;
        }
        tabAbbreviations().selectCell(row, 0);
        btRemove().pushNoBlock();
        
        return true;
    }
    
    public Map listAbbreviations() {
        TableModel model = tabAbbreviations().getModel();
        int rowCount = model.getRowCount();
        Map result = new HashMap();
        
        for (int cntr = 0; cntr < rowCount; cntr++) {
            result.put((String) model.getValueAt(cntr, 0), (String) model.getValueAt(cntr, 1));
        }
        
        return result;
    }
    
    public static Abbreviations invoke(String editorName) {
        OptionsOperator options = OptionsOperator.invoke();
        options.switchToClassicView(); //use switchToClassic, do not push the button
        options.selectOption(ResourceBundle.getBundle("org/netbeans/core/Bundle").getString("UI/Services/Editing")+
                "|"+ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("OPTIONS_all")+
                "|" + editorName);
        new EventTool().waitNoEvent(500);
        PropertySheetOperator pso = new PropertySheetOperator(options);
        new Property(pso,ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("PROP_Abbreviations")).openEditor();
        
        Abbreviations abbs = new Abbreviations();
        
        options.btClose().push();
        return abbs;
    }
    
    public static void addAbbreviation(String editorName, String abbreviation, String expansion) {
        Abbreviations instance = invoke(editorName);
        
        instance.addAbbreviation(abbreviation, expansion);
        instance.oK();
    }
    
    public static void addOrEditAbbreviation(String editorName, String abbreviationName, String newAbbreviationName, String newExpansion) {
        Abbreviations instance = invoke(editorName);
        
        instance.addOrEditAbbreviation(abbreviationName, newAbbreviationName, newExpansion);
        instance.oK();
    }
    
    public static boolean removeAbbreviation(String editorName, String abbreviation) {
        Abbreviations instance = invoke(editorName);
        boolean       result   = instance.removeAbbreviation(abbreviation);
        
        instance.oK();
        return result;
    }
    
    public static boolean editAbbreviation(String editorName, String abbreviationName, String newAbbreviationName, String newExpansion) {
        Abbreviations instance = invoke(editorName);
        boolean       result   = instance.editAbbreviation(abbreviationName, newAbbreviationName, newExpansion);
        
        instance.oK();
        return result;
    }
    
    public static Map listAbbreviations(String editorName) {
        Abbreviations instance = invoke(editorName);
        Map           result   = instance.listAbbreviations();
        
        instance.oK();
        
        return result;
    }
    
    /** Performs simple test of Abbreviations
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Abbreviations.addAbbreviation("Java Editor","aaa","All abbrev");
        try {
            Thread.currentThread().sleep(5000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Abbreviations.removeAbbreviation("Java Editor","aaa");
        /*
        try {
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

