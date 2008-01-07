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

package org.netbeans.modules.visualweb.gravy.properties;

import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Util;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.*;
import java.util.List;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.*;
import org.netbeans.jellytools.Bundle;

import org.openide.explorer.propertysheet.InplaceEditor;

/**
 * Class represented sheet of properties.
 */
public class SheetTableOperator extends JTableOperator {

    /**
     * Look for SheetTable in main window.
     */
    public SheetTableOperator() {
        super(Util.getMainWindow(), new SheetTableChooser());
    }
    
    /**
     * Look for SheetTable in specified container.
     * @param container Container where SheetTable is looked for.
     */
    public SheetTableOperator(ContainerOperator container) {
        super(container, new SheetTableChooser());
    }
    
    /**
     * Set comparison style.
     * @param compareExactly Make comparison by exact match. If false, comparison is made by entry.
     * @param caseSensitive Make case sensitive comparison.
     */
    public void setCompareStyle(boolean compareExactly, boolean caseSensitive){
        setComparator(new DefaultStringComparator(compareExactly,caseSensitive));
    }
    
    /**
     * Find row for specified property in this SheetTable.
     * @param propName Name of the property.
     */
    private int findRow(String propName){
        return findCell(new PropertyCellChooser(propName), null, new int[] {0}, 0).y;
    }
    
    /**
     * Click on row for specified property in this SheetTable.
     * @param propName Name of the property.
     */
    public void clickCell(String propName){
        clickOnCell(findRow(propName), 1);
        //TODO need second click to let component exit from inline editing mode
        clickOnCell(findRow(propName), 1);
    }
    
    /**
     * Make row for specified property editable.
     * @param propName Name of the property.
     */
    public void startEditing(String propName) {
        //clicknCell(findRow(propName), 1);
        clickCell(propName);
        pushKey(KeyEvent.VK_SPACE);
    }

    /**
     * Push dotten button in row for specified property.
     * @param propName Name of the property.
     */
    public void pushDotted(String propName) {
        clickOnCell(findRow(propName), 0);
        TestUtils.wait(2000);
        pushKey(KeyEvent.VK_SPACE,InputEvent.CTRL_MASK);
    }

    /**
     * Get value of specified property.
     * @param propName Name of the property.
     * @return String Value of specified property.
     */
    public String getValue(String propName) {
        return(((InplaceEditor)getRenderedComponent(findRow(propName), 1)).getValue().toString());
    }
    
    /**
     * Set value of specified property.
     * @param propName Name of the property.
     * @param value Value for specified property.
     */
    public void setValue(String propName, String value) {
        int row = findRow(propName);
        //((InplaceEditor)getRenderedComponent(row, 1)).setValue(value);
        System.out.println("Class: "+((InplaceEditor)getRenderedComponent(row, 1)).getClass());
    }
    
    /**
     * Set value of specified property with Text Component.
     * @param propName Name of the property.
     * @param value Value for specified property.
     */
    public void setTextValue(String propName, String value) {
        startEditing(propName);
        new JTextComponentOperator(this).enterText(value);
        Util.wait(500);
    }
    
    /**
     * Set value of specified property where is a button for call dialog for set value.
     * @param propName Name of the property.
     * @param value Value for specified property.
     */
    public void setButtonValue(String propName, String value) {
        clickForEdit(findCell(propName, 2).y, 1);
        //second click added to let component exit from inline editing mode
        clickForEdit(findCell(propName, 2).y, 1);
        new JTextComponentOperator(this).enterText(value);
    }
    
    /**
     * Set value of specified property with ComboBox.
     * @param propName Name of the property.
     * @param value Value for specified property.
     */
    public void setComboBoxValue(String propName, String value) {
        clickOnCell(findRow(propName), 1);
        new JComboBoxOperator(this).selectItem(value);
    }
    
    /**
     * Set value of specified property with CheckBox.
     * @param propName Name of the property.
     * @param value Value for specified property.
     */
    public void setCheckBoxValue(String propName, String value) {
        if (!getValue(propName).equals(value)) switchCheckBox(propName);
    }
    
    /**
     * Switch value of CheckBox for specified property.
     * @param propName Name of the property.
     */
    public void switchCheckBox(String propName){
        clickOnCell(findRow(propName), 1);
    }
    
    /**
     * Set image 
     * @param componentID ID of component.
     * @param propertyName url for image component and imageURL for imagehyperlink component.
     * @param imagePath Path to image.
     */
     public void setImage(String componentID, String propertyName, String imagePath) {
         pushDotted(propertyName);
         JDialogOperator dialog1 = new JDialogOperator(componentID);
         new JButtonOperator(dialog1, Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle","PropertySheet_AddFile")).pushNoBlock();
         JDialogOperator dialog2 = new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle","PropertySheet_AddFile"));
         new JTextFieldOperator(dialog2, 0).setText(imagePath);
         Util.wait(1000);
         new JButtonOperator(dialog2, Bundle.getStringTrimmed("org.netbeans.modules.visualweb.gravy.Bundle","PropertySheet_AddFile")).pushNoBlock();
         dialog2.waitClosed();
         Util.wait(1000);
         new JButtonOperator(dialog1, "OK").pushNoBlock();
         dialog1.waitClosed();
    }
     
    /**
     * Set specified value of specified property with ComboBox.
     * @param propName Name of the property.
     * @param item Item for specified property.
     */
    public void selectCombo(String propName, final String item) {
        int row = findRow(propName);
        //        ((InplaceEditor)getRenderedComponent(row, 1)).setValue(value);
        //        JComboBoxOperator combo = new JComboBoxOperator(((JComboBox)getRenderedComponent(row, 1)));
        //        combo.setVerification(false);
        //        combo.selectItem(item);
    }
    
    /** 
     * Get current component's id 
     */
     public String getSelectedComponentID() {
        setCompareStyle(true, true);
        String id = getValue("id"); 
        return id;
    }
    
    
    /**
     * Prints all properties from propertysheet to string
     * @return String with names and values
     */
    public String printAllProperties() {
        String output = "";
        
        for (int row = 0; row < getRowCount(); row++) {
            
            Object currentValue = null;
            String currentStringValue = "";
            String currentName = "";
            Object renderedComponent = null;
            
            currentName = ((JLabel) getRenderedComponent(row, 0)).getText();
            
            try {
                renderedComponent = getRenderedComponent(row, 1);
                currentValue = ( (InplaceEditor) renderedComponent ).getValue();
                
                try {
                    currentStringValue = (String) currentValue;
                } catch (ClassCastException e) {
                    currentStringValue = "Can't cast class " + currentValue.getClass().getName() +
                            " to String. toString returns: \"" + currentValue.toString() + "\"";
                }
                
            } catch (ClassCastException e) {
                if(renderedComponent instanceof TableCellRenderer) {
                    currentStringValue = "$TableCellRenderer";
                } else {
                    currentStringValue = "Can't cast class " + renderedComponent.getClass().getName() + " to InplaceEditor.";
                }
            }
            
            output += "Property: " + currentName + "; Value: " + currentStringValue + "\r\n";
        } // for
        
        return output;
    }

    
    /**
     * Open SheetTable.
     * @return Opened SheetTable.
     */
    public static SheetTableOperator createInstance() {
        Util.getMainMenu().pushMenuNoBlock("Window|Properties"); // show if hidden (collapse if already shown)
        
        SheetTableOperator props = new SheetTableOperator();
        int propsCnt = props.getRowCount();
        Util.getMainMenu().pushMenuNoBlock("Window|Properties"); // toggle expansion
        if(propsCnt > props.getRowCount()) {
            Util.getMainMenu().pushMenuNoBlock("Window|Properties"); // toggle expansion another time if was collapsed
        }
        
        return props;
    }
    
    protected static class PropertyCellChooser implements JTableOperator.TableCellChooser {
        String text;
        public PropertyCellChooser(String text) {
            this.text = text;
        }
        public boolean checkCell(JTableOperator oper, int row, int column) {
            JLabel label = (JLabel)oper.getRenderedComponent(row, column);
            return(label!=null && oper.getComparator().equals(label.getText(), text));
        }
        public String getDescription() {
            return(text + " property");
        }
    }
    
    static class SheetTableChooser implements ComponentChooser {
        public SheetTableChooser() {
        }
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.openide.explorer.propertysheet.SheetTable"));
        }
        public String getDescription() {
            return("org.openide.explorer.propertysheet.SheetTable");
        }
    }
}
