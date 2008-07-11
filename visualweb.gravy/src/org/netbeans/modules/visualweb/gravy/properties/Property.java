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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

/**
 * Handles properties in IDE property sheets. Properties are grouped in
 * property sheet. Their are identified by their display names. Once you
 * have created a Property instance you can get value, set a new text value,
 * set a new value by index of possible options or open custom editor.
 * <p>
 * Usage:<br>
 * <pre>
        PropertySheetOperator pso = new PropertySheetOperator("Properties of MyClass");
        Property p = new Property(pso, "Name");
        System.out.println("\nProperty name="+p.getName());
        System.out.println("\nProperty value="+p.getValue());
        p.setValue("ANewValue");
        // set a new value by index where it is applicable
        //p.setValue(2);
        // open custom editor where it is applicable
        //p.openEditor();
 * </pre>
 *
 * @see PropertySheetOperator
 */
public class Property {

    // DEPRECATED>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    /** Container to find property in */
    protected ContainerOperator contOper;
    /** Display name of the property */
    private String name;
    /** Operator of name button */
    private SheetButtonOperator nameButtonOperator;
    /** Operator of value button */
    private SheetButtonOperator valueButtonOperator;
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<DEPRECATED

    /** Class name of string renderer. */
    public static final String STRING_RENDERER = "org.openide.explorer.propertysheet.RendererFactory$StringRenderer";  // NOI18N
    /** Class name of check box renderer. */
    public static final String CHECKBOX_RENDERER = "org.openide.explorer.propertysheet.RendererFactory$CheckboxRenderer";  // NOI18N
    /** Class name of combo box renderer. */
    public static final String COMBOBOX_RENDERER = "org.openide.explorer.propertysheet.RendererFactory$ComboboxRenderer";  // NOI18N
    /** Class name of radio button renderer. */
    public static final String RADIOBUTTON_RENDERER = "org.openide.explorer.propertysheet.RendererFactory$RadioButtonRenderer";  // NOI18N
    /** Class name of set renderer. */
    public static final String SET_RENDERER = "org.openide.explorer.propertysheet.RendererFactory$SetRenderer";  // NOI18N
    
    /** Instance of Node.Property. */
    protected Node.Property property;
    /** Property sheet where this property resides. */
    protected PropertySheetOperator propertySheetOper;

    /** Waits for property with given name in specified property sheet.
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name property display name
     */
    public Property(PropertySheetOperator propertySheetOper, String name) {
        this.propertySheetOper = propertySheetOper;
        this.property = waitProperty(propertySheetOper, name);
    }
    
    /** Waits for index-th property in specified property sheet.
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param index index (row number) of property inside property sheet
     *              (starts at 0). If there categories shown in property sheet,
     *              rows occupied by their names must by added to index.
     */
    public Property(PropertySheetOperator propertySheetOper, int index) {
        this.propertySheetOper = propertySheetOper;
        this.property = waitProperty(propertySheetOper, index);
    }
    
    /** Waits for property with given name in specified container.
     * @param contOper ContainerOperator where to find property. It is
     * recommended to use {@link PropertySheetOperator}.
     * @param name property name
     * @deprecated Use {@link #Property(PropertySheetOperator, String)} instead
     */
    public Property(ContainerOperator contOper, String name) {
        this(new PropertySheetOperator(contOper), name);
        /*
        this.contOper = contOper;
        this.name = name;
        this.name = nameButtonOperator().getLabel();
         */
    }
    
    /** Waits for index-th property in specified container.
     * @param contOper ContainerOperator whete to find property. It is
     *                 recommended to use {@link PropertySheetOperator}.
     * @param index index (row number) of property inside property sheet
     *              (starts at 0)
     * @deprecated Use {@link #Property(PropertySheetOperator, int)} instead
     */
    public Property(ContainerOperator contOper, int index) {
        this(new PropertySheetOperator(contOper), index);
    }
    
    /** Waits for property with given name in specified property sheet.
     * @param propSheetOper PropertySheetOperator where to find property.
     * @param name property display name
     */
    private Node.Property waitProperty(final PropertySheetOperator propSheetOper, final String name) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object param) {
                    Node.Property property = null;
                    JTableOperator table = propSheetOper.tblSheet();
                    for(int row=0;row<table.getRowCount();row++) {
                        if(table.getValueAt(row, 1) instanceof Node.Property) {
                            property = (Node.Property)table.getValueAt(row, 1);
                            if(propSheetOper.getComparator().equals(property.getDisplayName(), name)) {
                                return property;
                            }
                        }
                    }
                    return null;
                }
                public String getDescription() {
                    return("Wait property "+name);
                }
            });
            return (Node.Property)waiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }

    /** Waits for index-th property in specified property sheet.
     * @param propSheetOper PropertySheetOperator where to find property.
     * @param index index (row number) of property inside property sheet
     *              (starts at 0). If there are categories shown in property sheet,
     *              rows occupied by their names must by added to index.
     */
    private Node.Property waitProperty(final PropertySheetOperator propSheetOper, final int index) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object param) {
                    JTableOperator table = propSheetOper.tblSheet();
                    if(table.getRowCount() <= index) {
                        // If table is empty or index out of bounds, 
                        // it returns null to wait until table is populated by values
                        return null;
                    }
                    Object property = table.getValueAt(index, 1);
                    if(property instanceof Node.Property) {
                        return (Node.Property)property;
                    } else {
                        throw new JemmyException("On row "+index+" in table there is no property");
                    }
                }
                public String getDescription() {
                    return("Wait property on row "+index+" in property sheet.");
                }
            });
            //waiter.setOutput(TestOut.getNullOutput());
            return (Node.Property)waiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
    }
    
    /** Gets SheetButtonOperator instance of property's name button. It returns
     * valid button even if properties were reordered.
     * @return SheetButtonOperator instance of name button
     * @deprecated JTable used for property sheet instead of SheetButtons
     */
    public SheetButtonOperator nameButtonOperator() {
        throw new JemmyException("Don't use this! Property sheet uses JTable instead of SheetButton.");
        //return null;
        /*
        if(nameButtonOperator != null) {
            if(!nameButtonOperator.isValid()) {
                nameButtonOperator = null;
            }
        }
        if(nameButtonOperator == null) {
            nameButtonOperator = SheetButtonOperator.nameButton(contOper, name);
        }
        return nameButtonOperator;
         */
    }
    
    /** Gets SheetButtonOperator instance of property's value button. It returns
     * valid button even if properties were reordered.
     * @return SheetButtonOperator instance of value button
     * @deprecated JTable used for property sheet instead of SheetButtons
     */
    public SheetButtonOperator valueButtonOperator() {
        throw new JemmyException("Don't use this! Property sheet uses JTable instead of SheetButton.");
        /*
        if(valueButtonOperator != null) {
            if(!valueButtonOperator.isValid()) {
                valueButtonOperator = null;
            }
        }
        if(valueButtonOperator == null) {
            // Button can be changed (reordered, changed value, etc.).
            // We need to call nameButtonOperator().getNameButtonIndex()
            // to find valid name button for this property.
            valueButtonOperator = SheetButtonOperator.valueButton(contOper,
            nameButtonOperator().getNameButtonIndex());
        }
        return valueButtonOperator;
         */
    }
    
    /** Gets display name of this property.
     * It can differ from name given in constructor when only
     * substring of property name is used there.
     * @return display name of property
     */
    public String getName() {
        return property.getDisplayName();
    }
    
    /** Gets string representation of property value.
     * @return value of property
     */
    public String getValue() {
        PropertyEditor pe = property.getPropertyEditor();
        try {
            if(property.getValue() != pe.getValue()) {
                // Need to synchronize property and its property editor.
                // Otherwise it may cause problem when called pe.getAsText().
                pe.setValue(property.getValue());
            }
        } catch (Exception e) {
            throw new JemmyException("Exception while synchronizing value of property and property editor - property.getValue() != pe.getValue()", e);
        }
        return pe.getAsText();
    }
    
    /** Sets value of this property to specified text. If a new value is
     * not accepted, an information or error dialog is displayed by IDE.
     * If property is not writable JemmyException is thrown.
     * @param textValue text to be set in property (e.g. "a new value",
     * "a new item from list", "false", "TRUE")
     */
    public void setValue(String textValue) {
        propertySheetOper.getOutput().printTrace("Setting value \""+textValue+
                                                 "\" of property \""+getName()+"\".");
        if(!isEnabled()) {
            throw new JemmyException("Property \""+getName()+"\" is read only.");
        }
        PropertyEditor pe = property.getPropertyEditor();
        try {
            if(property.getValue() != pe.getValue()) {
                // Need to synchronize property and its property editor.
                // Otherwise it may cause IAE when called pe.setAsText(textValue).
                pe.setValue(property.getValue());
            }
        } catch (Exception e) {
            throw new JemmyException("Exception while synchronizing value of property and property editor - property.getValue() != pe.getValue()", e);
        }
        try {
            JemmyProperties.getCurrentOutput().printTrace("Setting property \""+getName()+"\" to value \""+textValue+"\".");
            pe.setAsText(textValue);
            property.setValue(pe.getValue());
        } catch (IllegalAccessException iae) {
            ErrorManager.getDefault().notify(iae);
        } catch (IllegalArgumentException iare) {
            ErrorManager.getDefault().notify(iare);
        } catch (InvocationTargetException ite) {
            ErrorManager.getDefault().notify(ite);
        } catch (Exception e) {
            throw new JemmyException("Exception while setting value of property.", e);
        }
    }
    
    /** Sets value of this property by given index.
     * It is applicable for properties which can be changed by combo box.
     * If property doesn't support changing value by index JemmyException
     * is thrown.
     * @param index index of item to be selected from possible options
     */
    public void setValue(int index) {
        PropertyEditor pe = property.getPropertyEditor();
        String[] tags = pe.getTags();
        if(tags != null) {
            setValue(tags[index]);
        } else {
            throw new JemmyException("Property doesn't support changing value by index.");
        }
    }
    
    /** Returns true if this property is in editable state (it is being edited).
     * It is detected by presence of PropertySheetButton which stands
     * for property value in non editable state.
     * @return true - this property is being edited; false otherwise
     * @deprecated Use {@link #setValue} to change property value
     */
    public boolean isEditable() {
        throw new JemmyException("Don't use this! Use setValue() to change property value.");
        /*
        // wait for name button index-th PropertyPanel
        Component propertyPanel = SheetButtonOperator.waitPropertyPanel(contOper,
        nameButtonOperator().getNameButtonIndex());
        ComponentChooser chooser = new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().indexOf("PropertySheetButton") != -1;
            }
         
            public String getDescription() {
                return "PropertySheetButton";
            }
        };
        // if PropertySheetButton not found, property is editable
        return contOper.findComponent((Container)propertyPanel, chooser) == null;
         */
    }
    
    /** If this property is not editable, it scrolls to property and clicks
     * on name button. Otherwise does nothing.
     * @deprecated Use {@link #setValue} to change property value
     */
    public void startEditing() {
        throw new JemmyException("Don't use this! Property sheet uses JTable instead of SheetButton.");
        /*
        if(!isEditable()) {
            nameButtonOperator().push();
        }
         */
    }
    
    /** If this property is editable, it scrolls to property if needed and
     * clicks on name button. It cancels editing and sets original value back.
     * @deprecated Use {@link #setValue} to change property value
     */
    public void stopEditing() {
        throw new JemmyException("Don't use this! Property sheet uses JTable instead of SheetButton.");
        /*
        if(isEditable()) {
            nameButtonOperator().push();
        }
         */
    }
    
    
    /** Opens custom property editor for the property by click on "..." button.
     * It checks whether this property supports custom editor by method
     * {@link #supportsCustomEditor}.
     */
    public void openEditor() {
        if(supportsCustomEditor()) {
            final JTableOperator table = this.propertySheetOper.tblSheet();
            for(int row=0;row<table.getRowCount();row++) {
                if(table.getValueAt(row, 1) instanceof Node.Property) {
                    if(this.property == (Node.Property)table.getValueAt(row, 1)) {
                        // Need to request focus before selection because invokeCustomEditor action works
                        // only when table is focused
                        /* mdk
                        table.makeComponentVisible();
                        table.requestFocus();
                        table.waitHasFocus();
                        */
                        // need to select property first
                        ((javax.swing.JTable)table.getSource()).changeSelection(row, 0, false, false);
                        // find action
                        final Action customEditorAction = ((JComponent)table.getSource()).getActionMap().get("invokeCustomEditor");  // NOI18N
                        // run action in a separate thread (no block)
                        new Thread(new Runnable() {
                            public void run() {
                                customEditorAction.actionPerformed(new ActionEvent(table.getSource(), 0, null));
                            }
                        }, "Thread to open custom editor no block").start(); // NOI18N
                        return;
                    }
                }
            }
        }
    }
    
    /** Checks whether this property supports custom editor.
     * @return true is property supports custom editor, false otherwise
     */
    public boolean  supportsCustomEditor() {
        return this.property.getPropertyEditor().supportsCustomEditor();
    }
    
    /** Sets default value for this property. If default value is not available,
     * it does nothing.
     */
    public void setDefaultValue() {
        try {
            property.restoreDefaultValue();
        } catch (Exception e) {
            throw new JemmyException("Exception while restoring default value.", e);
        }
    }

    /** Returns true if this property is enabled in property sheet, that means
     * it is possible to change its value by inplace editor.
     * @return true if this property is enabled, false otherwise
     */
    public boolean isEnabled() {
        return property.canWrite();
    }
    
    /** Returns true if this property can be edited as text by inplace text field.
     * It can be both for string renderer or combo box renderer.
     * @return true if this property can be edited, false otherwise
     */
    public boolean canEditAsText() {
        // if not enabled, it cannot be edited
        if(!isEnabled()) {
            return false;
        }
        final JTableOperator table = propertySheetOper.tblSheet();
        for(int row=0;row<table.getRowCount();row++) {
            if(table.getValueAt(row, 1) instanceof Node.Property) {
                if(property == (Node.Property)table.getValueAt(row, 1)) {
                    table.clickForEdit(row, 1);
                    long oldTimeout = propertySheetOper.getTimeouts().getTimeout("ComponentOperator.WaitComponentTimeout");
                    propertySheetOper.getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", 1000);
                    try {
                        new JTextFieldOperator(propertySheetOper);
                        return true;
                    } catch (JemmyException e) {
                        // property cannot be edited as text by inplace editor
                        return false;
                    } finally {
                        // push ESC to stop editing
                        table.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
                        // reset timeout
                        propertySheetOper.getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", oldTimeout);
                    }
                }
            }
        }
        // never should happen
        throw new JemmyException("Property "+getName()+" not found in this sheet:\n"+propertySheetOper.getSource().toString());
    }
    
    /** Returns class name of renderer used to render this property. It can
     * be used to determine whether correct renderer is used. Possible values
     * are defined in constants {@link #STRING_RENDERER}, {@link #CHECKBOX_RENDERER},
     * {@link #COMBOBOX_RENDERER}, {@link #RADIOBUTTON_RENDERER}, {@link #SET_RENDERER}.
     * @return class name of renderer used to render this property:
     * <UL>
     * <LI>org.openide.explorer.propertysheet.RendererFactory$StringRenderer</LI>
     * <LI>org.openide.explorer.propertysheet.RendererFactory$CheckboxRenderer</LI>
     * <LI>org.openide.explorer.propertysheet.RendererFactory$ComboboxRenderer</LI>
     * <LI>org.openide.explorer.propertysheet.RendererFactory$RadioButtonRenderer</LI>
     * <LI>org.openide.explorer.propertysheet.RendererFactory$SetRenderer</LI>
     * </UL>
     * @see #STRING_RENDERER
     * @see #CHECKBOX_RENDERER
     * @see #COMBOBOX_RENDERER
     * @see #RADIOBUTTON_RENDERER
     * @see #SET_RENDERER
     */
    public String getRendererName() {
        return getRenderer().getClass().getName();
    }
    
    /** Returns component which represents renderer for this property. */
    private Component getRenderer() {
        final JTableOperator table = propertySheetOper.tblSheet();
        for(int row=0;row<table.getRowCount();row++) {
            if(table.getValueAt(row, 1) instanceof Node.Property) {
                if(property == (Node.Property)table.getValueAt(row, 1)) {
                    // gets component used to render a value
                    TableCellRenderer renderer = table.getCellRenderer(row,1);
                    Component comp = renderer.getTableCellRendererComponent(
                                                        (JTable)table.getSource(), 
                                                        table.getValueAt(row, 1), 
                                                        false, 
                                                        false, 
                                                        row, 
                                                        1
                    );
                    // We need to find a real renderer because it can be embedded
                    // in ButtonPanel (supplies custom editor button "...")
                    // or IconPanel(supplies property marking).
                    try {
                        Class clazz = Class.forName("org.openide.explorer.propertysheet.RendererPropertyDisplayer");
                        Method findInnermostRendererMethod = clazz.getDeclaredMethod("findInnermostRenderer", new Class[] {JComponent.class});
                        findInnermostRendererMethod.setAccessible(true);
                        comp = (Component)findInnermostRendererMethod.invoke(null, new Object[] {comp});
                    } catch (Exception e) {
                        throw new JemmyException("RendererPropertyDisplayer.findInnermostRenderer() by reflection failed.", e);
                    }
                    return comp;
                }
            }
        }
        // never should happen
        throw new JemmyException("Property "+getName()+" not found in this sheet:\n"+propertySheetOper.getSource().toString());
    }
    
    /** Gets short description for this property. Short description is also 
    * used in tooltip.
    * @return short description for this property.
    */
    public String getShortDescription() {
        return this.property.getShortDescription();
    }
}
