/*
 * Abbreviations.java
 *
 * Created on 8/28/02 11:08 AM
 */
package org.netbeans.jellytools.modules.editor;

import java.util.*;
import javax.swing.table.TableModel;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.TextFieldProperty;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Abbreviations" NbDialog.
 *
 * @author  Jan Lahoda
 * @version 1.0
 */
public class Abbreviations extends JDialogOperator {
    
    /** Creates new Abbreviations that can handle it.
     */
    public Abbreviations() {
        super("Abbreviations");
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
            _btAdd = new JButtonOperator(this, "Add...");
        }
        return _btAdd;
    }
    
    /** Tries to find "Edit..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btEdit() {
        if (_btEdit==null) {
            _btEdit = new JButtonOperator(this, "Edit...");
        }
        return _btEdit;
    }
    
    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            _btRemove = new JButtonOperator(this, "Remove");
        }
        return _btRemove;
    }
    
    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
    }
    
    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }
    
    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
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
        
        if (row == (-1))
            return false;
        
        tabAbbreviations().selectCell(row, 0);
        btRemove().push();
        
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
        
        options.selectOption(ResourceBundle.getBundle("org/netbeans/core/Bundle").getString("UI/Services/Editing")+
        "|"+ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("OPTIONS_all")+
        "|" + editorName);
        PropertySheetOperator property = new PropertySheetOperator(options);
        new TextFieldProperty(property,ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("PROP_Abbreviations")).openEditor();

        Abbreviations abbs = new Abbreviations();
        
        options.close(); /*??? not sure whether this is assured to work*/
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
        new Abbreviations().verify();
        System.out.println("Abbreviations verification finished.");
    }
}

