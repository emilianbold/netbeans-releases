/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

// AWT
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;

// Swing
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;

// Netbeans
import org.openide.util.NbBundle;

/** The simple dialog panel containing labels, text fields and browse buttons.
 * <br>
 * Individual components should be described using SimpleDialogPanel.DialogDescriptor class.
 *
 * @author  mk115033
 * Created on January 03, 2005
 *
 */
public class SimpleDialogPanel extends JPanel {
    private JTextComponent[] jTextComponents;
    private JLabel[] jLabels;
    private JButton[] jButtons;
    private GridBagConstraints gridBagConstraints;

    /** Constructor that creates the simple ADD/EDIT dialog with n labels and n empty text fields,
    * where the shape of the dialog is described using the DialogDescriptor object.
    * @param desc describes components inside the panel
    */      
    public SimpleDialogPanel(DialogDescriptor desc) {
        super();
        initComponents(desc.getLabels(), desc.isTextField(), desc.getSize(), desc.getButtons(), desc.getMnemonics(), desc.getA11yDesc());
        String[] initValues = desc.getInitValues();
        if (initValues!=null)
            for (int i=0;i<initValues.length;i++) {
                jTextComponents[i].setText(initValues[i]);
            }
    }
    
    private void initComponents(String[] labels, boolean[] isTextField, int size, boolean[] customizers, char[] mnem, String[] a11yDesc) {
        setLayout(new GridBagLayout());
        jLabels = new JLabel [labels.length];
        jTextComponents = new JTextComponent [labels.length];
        for (int i=0;i<labels.length;i++) {
            jLabels[i] = new JLabel(labels[i]);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = i;
            gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
            if (isTextField[i])
                gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            else 
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
            add(jLabels[i], gridBagConstraints);
        }
        for (int i=0;i<jTextComponents.length;i++) {
            if (isTextField[i]) { // text field
                jTextComponents[i] = new JTextField();
                ((JTextField)jTextComponents[i]).setColumns(size);
            } else { // text area
                jTextComponents[i] = new JTextArea();
                ((JTextArea)jTextComponents[i]).setRows(3);
                if (i>0) jTextComponents[i].setBorder(jTextComponents[0].getBorder());
            }
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = i;
            gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 11);
            gridBagConstraints.weightx = 1.0;
            jLabels[i].setLabelFor(jTextComponents[i]);
            if (isTextField[i]) {// text field
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                add(jTextComponents[i], gridBagConstraints);
            } else {
                gridBagConstraints.weighty = 1.0;
                gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
                javax.swing.JScrollPane sp = new javax.swing.JScrollPane(jTextComponents[i]);
                add(sp, gridBagConstraints);
            }
        }
        if (customizers!=null) {
            java.util.List buttonList = new java.util.ArrayList();
            int j=0;
            for (int i=0;i<customizers.length;i++) {
                if (customizers[i]) {
                    JButton button = new JButton();
                    button.setText(NbBundle.getMessage(SimpleDialogPanel.class,"LBL_browse"));
                    button.setMnemonic(NbBundle.getMessage(SimpleDialogPanel.class,"LBL_browse"+String.valueOf(j++)+"_mnem").charAt(0));
                    button.setMargin(new java.awt.Insets(0, 14, 0, 14));
                    buttonList.add(button);
                    gridBagConstraints = new java.awt.GridBagConstraints();
                    gridBagConstraints.gridx = 2;
                    gridBagConstraints.gridy = i;
                    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 11);
                    add(button, gridBagConstraints);
                }
            }
            jButtons = new JButton[buttonList.size()];
            buttonList.toArray(jButtons);
        }
        if (mnem!=null) {
            for (int i=0;i<labels.length;i++) {
                jLabels[i].setLabelFor(jTextComponents[i]);
                jLabels[i].setDisplayedMnemonic(mnem[i]);
            }
        }
        if (a11yDesc!=null) {
            for (int i=0;i<jTextComponents.length;i++) {
                jTextComponents[i].getAccessibleContext().setAccessibleDescription(a11yDesc[i]);
            }
        }
    }
    /** Returns the values from the text fields.
    * @return text fields values
    */
    public String[] getValues() {
        if (jTextComponents==null) return null;
        String[] values = new String[jTextComponents.length];
        for (int i=0;i<values.length;i++) {
            values[i] = jTextComponents[i].getText();
        }
        return values;
    }
    /** Returns the JButton components from the dialog described in DialogDescriptor.
    * @return JButton components that are used for invoking the text field customizers 
    */    
    public JButton[] getCustomizerButtons() {
        return jButtons;
    }
    /** Returns the dialog text fields.
    * @return array of text fields
    */    
    public JTextComponent[] getTextComponents() {
        return jTextComponents;
    }
    /** This is the descriptor for the dialog components.
    * Parameters are :<ul>
    * <li>labels = text array for text fields
    * <li>initValues = initialization values for text fields
    * <li>adding = indicates the type of the dialog (ADD/EDIT). Defaultly set to true (ADD dialog)
    * <li>customizers = describes the layout of the customizers buttons.<br>
    * For example setCustomizers(new boolean{false,true,false}) sets the customizer only for the second text field 
    * <li>size = default number of columns for text fields. Defaultly set to 25
    * </ul>
    */
    
    public static class DialogDescriptor {
        String[] labels;
        String[] initValues;
        boolean adding;
        boolean[] buttons;
        boolean textField[];
        char[] mnem;
        String[] a11yDesc;
        int size;

        
        /** the constructor for DialogDescriptor object
        * @param labels labels names
        */
        public DialogDescriptor(String[] labels) {
            this.labels=labels;
            size=25;
            adding=true;
            textField = new boolean[labels.length];
            for (int i=0;i<labels.length;i++) {
                textField[i]=true; // setting textFields to text fields
            }
        }
        public String[] getLabels() {
            return labels;
        }
        public void setButtons(boolean[] buttons) {
            this.buttons=buttons;
        }
        public boolean[] getButtons() {
            return buttons;
        }
        public void setTextField(boolean[] textField) {
            this.textField=textField;
        }
        public boolean[] isTextField() {
            return textField;
        }
        public void setInitValues(String[] initValues) {
            this.initValues=initValues;
            adding=false;
        }
        public String[] getInitValues() {
            return initValues;
        }
        public void setAdding(boolean adding) {
            this.adding=adding;
        }
        public boolean isAdding() {
            return adding;
        }
        public void setSize(int size) {
            this.size=size;
        }
        public int getSize() {
            return size;
        }
        public void setMnemonics(char[] mnem) {
            this.mnem=mnem;
        }
        public char[] getMnemonics() {
            return mnem;
        }
        public void setA11yDesc(String[] a11yDesc) {
            this.a11yDesc=a11yDesc;
        }
        public String[] getA11yDesc() {
            return a11yDesc;
        }
    }
}