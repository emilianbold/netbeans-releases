/*
 * KeyBindings.java
 *
 * Created on 10/17/02 12:54 PM
 */
package org.netbeans.jellytools.modules.editor;

import org.netbeans.jemmy.operators.*;
import java.util.*;

import javax.swing.ListModel;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.TextFieldProperty;

/** Class implementing all necessary methods for handling "Key Bindings" NbDialog.
 *
 * @author eh103527
 * @version 1.0
 */
public class KeyBindings extends JDialogOperator {
    
    /** Creates new KeyBindings that can handle it.
     */
    public KeyBindings() {
        super("Key Bindings");
    }
    
    private JRadioButtonOperator _rbSortByName;
    private JRadioButtonOperator _rbSortByAction;
    private JListOperator _lstActions;
    private JButtonOperator _btMetalScrollButton;
    private JButtonOperator _btMetalScrollButton2;
    private JLabelOperator _lblKeybindings;
    private JListOperator _lstKeybindings;
    private JButtonOperator _btAdd;
    private JButtonOperator _btRemove;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find " Sort by Name" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbSortByName() {
        if (_rbSortByName==null) {
            _rbSortByName = new JRadioButtonOperator(this, " Sort by Name");
        }
        return _rbSortByName;
    }
    
    /** Tries to find " Sort by Action" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbSortByAction() {
        if (_rbSortByAction==null) {
            _rbSortByAction = new JRadioButtonOperator(this, " Sort by Action");
        }
        return _rbSortByAction;
    }
    
    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstActions() {
        if (_lstActions==null) {
            _lstActions = new JListOperator(this);
        }
        return _lstActions;
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
    
    /** Tries to find "Keybindings:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblKeybindings() {
        if (_lblKeybindings==null) {
            _lblKeybindings = new JLabelOperator(this, "Keybindings:");
        }
        return _lblKeybindings;
    }
    
    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstKeybindings() {
        if (_lstKeybindings==null) {
            _lstKeybindings = new JListOperator(this, 1);
        }
        return _lstKeybindings;
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
    
    /** clicks on " Sort by Name" JRadioButton
     */
    public void sortByName() {
        rbSortByName().push();
    }
    
    /** clicks on " Sort by Action" JRadioButton
     */
    public void sortByAction() {
        rbSortByAction().push();
    }
    
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
    
    /** Performs verification of KeyBindings by accessing all its components.
     */
    public void verify() {
        rbSortByName();
        rbSortByAction();
        lstActions();
        btMetalScrollButton();
        btMetalScrollButton2();
        lblKeybindings();
        lstKeybindings();
        btAdd();
        btRemove();
        btOK();
        btCancel();
        btHelp();
    }
    
    public List listKeyBindings() {
        ListModel model = lstKeybindings().getModel();
        List ret=new Vector();
        for (int i=0;i < model.getSize();i++) {
            ret.add(model.getElementAt(i));
        }
        return ret;
    }
        
    public static KeyBindings invoke(String editorName) {
        OptionsOperator options = OptionsOperator.invoke();
        options.selectOption(ResourceBundle.getBundle("org/netbeans/core/Bundle").getString("UI/Services/Editing")+"|"+ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("OPTIONS_all")+"|" + editorName);
        PropertySheetOperator property = new PropertySheetOperator(options);
        new TextFieldProperty(property,ResourceBundle.getBundle("org/netbeans/modules/editor/options/Bundle").getString("PROP_KeyBindings")).openEditor();
        KeyBindings ret=new KeyBindings();
        options.close();
        return ret;
    }
    
    public static List listKeyBindings(String editorName) {
        KeyBindings instance = invoke(editorName);
        List          result = instance.listKeyBindings();
        instance.oK();
        return result;
    }    
    
    /** Performs simple test of KeyBindings
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        List list = KeyBindings.listKeyBindings("Java Editor");
        for (int i=0;i < list.size();i++) {
            System.out.println(list.get(i));
        }
    }
}

