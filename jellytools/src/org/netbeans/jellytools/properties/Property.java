/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.properties;

import java.awt.event.ActionEvent;
import java.beans.PropertyEditor;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.jellytools.JellyVersion;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTableOperator;
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
 * @author Jiri.Skrivanek@sun.com
 * @see PropertySheetOperator
 */
public class Property {
    
    /** Container to find property in */
    protected ContainerOperator contOper;
    /** Display name of the property */
    private String name;
    /** Operator of name button */
    private SheetButtonOperator nameButtonOperator;
    /** Operator of value button */
    private SheetButtonOperator valueButtonOperator;
    
    
    /** Instance of Node.Property. */
    protected Node.Property property;
    /** Property sheet where this property resides. */
    protected PropertySheetOperator propertySheetOper;
    
    static {
        // Checks if you run on correct jemmy version. Writes message to jemmy log if not.
        JellyVersion.checkJemmyVersion();
    }
    
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
        /*
        this.contOper = contOper;
        nameButtonOperator = SheetButtonOperator.nameButton(contOper, index);
        this.name = nameButtonOperator.getLabel();
         */
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
     *              (starts at 0). If there categories shown in property sheet,
     *              rows occupied by their names must by added to index.
     */
    private Node.Property waitProperty(final PropertySheetOperator propSheetOper, final int index) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object param) {
                    JTableOperator table = propSheetOper.tblSheet();
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
                pe.setValue(property.getValue());
            }
        } catch (Exception e) {
            throw new JemmyException("Exception while getting value from property.", e);
        }
        return pe.getAsText();
    }
    
    /** Sets value of this property to specified text.
     * @param textValue text to be set in property (e.g. "a new value",
     * "a new item from list", "false", "TRUE")
     */
    public void setValue(String textValue) {
        PropertyEditor pe = property.getPropertyEditor();
        pe.setAsText(textValue);
        try {
            property.setValue(pe.getValue());
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
                        // need to select property first
                        table.selectCell(row, 0);
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
        /*
        nameButtonOperator().clickForPopup();
        String menuItem = Bundle.getString("org.openide.explorer.propertysheet.Bundle",
        "SetDefaultValue");
        new JPopupMenuOperator().pushMenu(menuItem, "|");
        // need to wait until value button is changed
        new EventTool().waitNoEvent(100);
         */
    }
}
