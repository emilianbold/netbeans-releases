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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


