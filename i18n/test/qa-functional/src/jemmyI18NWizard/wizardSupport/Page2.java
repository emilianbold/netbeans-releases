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

package jemmyI18NWizard.wizardSupport;

import org.netbeans.jemmy.operators.JTableOperator;

import org.netbeans.test.oo.gui.jam.JamComboBox;
import org.netbeans.test.oo.gui.jam.Jemmy;
import org.netbeans.test.oo.gui.jello.JelloWizard;
import org.netbeans.test.oo.gui.jello.JelloBundle;
import org.netbeans.test.oo.gui.jello.JelloUtilities;

import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nString;


public class Page2 extends JelloWizard {

    protected JamComboBox sourceSelectionComboBox;
    public JTableOperator foundStringTable;

    private static final String wizardBundle = "org.netbeans.modules.i18n.wizard.Bundle";
    
    public Page2() {
        super(JelloUtilities.getForteFrame(), JelloBundle.getString(wizardBundle, "LBL_WizardTitle"));
        sourceSelectionComboBox = this.getJamComboBox(0);
        foundStringTable = new JTableOperator(Jemmy.getOp(this));
    } 
    
    public void clearTableSelection() {
        foundStringTable.clearSelection();
    }
    
    public String getColumnName(int index) {
        return foundStringTable.getColumnName(index);
    }
    
    private boolean isCellEditable(int rowIndex, int columnIndex) {
        return foundStringTable.isCellEditable(rowIndex, columnIndex);
    }
    
    public Object getCell(int rowIndex, int columnIndex) {
        return foundStringTable.getValueAt(rowIndex, columnIndex);
    }
    
    public void setCell(Object object, int rowIndex, int columnIndex) {
        foundStringTable.setValueAt(object, rowIndex, columnIndex);
    }
    
    public void selectRow(int index) {
        foundStringTable.setRowSelectionInterval(index, index);
    }
    
    public void selectRows(int index1, int index2) {
        foundStringTable.setRowSelectionInterval(index1, index2);
    }
    
    public int getSelectedRow() {
        return foundStringTable.getSelectedRow();
    }
    
    public int[] getSelectedRows() {
        return foundStringTable.getSelectedRows();
    }
    
    public int getSelectedColumn() {
        return foundStringTable.getSelectedColumn();
    }
    
    public void selectSource(int index) {
        sourceSelectionComboBox.setSelectedItem(index);
    }
    
    public void selectSource(String source) {
        sourceSelectionComboBox.setSelectedItem(source);
    }
    
    public String getSelectedSource() {
        return sourceSelectionComboBox.getSelectedItem();
    }
    
    public int getSelectedItemIndex() {
        return sourceSelectionComboBox.getSelectedIndex();
    }
    
    public String getHardcodedString(int rowIndex) {
        HardCodedString hardcodedString = (HardCodedString)this.getCell(rowIndex,1);
        return hardcodedString.getText();
    }
    
    public String getKey(int rowIndex) {
        I18nString string = (I18nString)this.getCell(rowIndex,2);
        return string.getKey();
    }
    
    public String getValue(int rowIndex) {
        I18nString string = (I18nString)this.getCell(rowIndex,3);
        return string.getValue();
    }
    
    public boolean getEnabled(int rowIndex) {
        Boolean enabled = (Boolean)this.getCell(rowIndex,0);
        return enabled.booleanValue();        
    }
    
    public String getComment(int rowIndex) {
        I18nString string = (I18nString)this.getCell(rowIndex,3);
        return string.getComment();
    }
    
    public void setKey(int rowIndex, String key) {
        I18nString string = (I18nString)this.getCell(rowIndex,2);
        string.setKey(key);
    }
    
    public void setValue(int rowIndex, String value) {
        I18nString string = (I18nString)this.getCell(rowIndex,2);
        string.setValue(value);        
    }
    
    public void setEnabled(int rowIndex, boolean enabled) {
        this.setCell(enabled ? Boolean.TRUE : Boolean.FALSE, rowIndex, 0);
    }    
    
    public void setComment(int rowIndex, String comment) {
        I18nString string = (I18nString)this.getCell(rowIndex,3);
        string.setComment(comment);
    }
    
    /** Dummy here. */
    protected void updatePanel(int panelIndex) {
    }
    
}


