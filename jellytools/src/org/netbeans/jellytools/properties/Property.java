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

import java.awt.Component;
import java.awt.Container;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyVersion;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Handles properties in IDE property sheets. Property consists of its name
 * and value. This class holds both name property sheet button and value
 * property sheet button. It enables to switch to editing mode and back.
 * It can also invoke custom editor dialog and set default value by popup menu.
 * <p>
 * Usage:<br>
 * <pre>
 *      PropertySheetOperator pso = new PropertySheetOperator("Properties of MyClass");
 *      PropertySheetTabOperator psto = new PropertySheetTabOperator(pso, "Properties");
 *      Property pr = new Property(psto, "Template");
 *      System.out.println("\nProperty name="+pr.getName());
 *      System.out.println("\nProperty value="+pr.getValue());
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
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

    static {
        // Checks if you run on correct jemmy version. Writes message to jemmy log if not.
        JellyVersion.checkJemmyVersion();
    }
    
    /** Waits for property with given name in specified container.
     * @param contOper ContainerOperator where to find property. It is
     * recommended to use {@link PropertySheetTabOperator}.
     * @param name property name
     */
    public Property(ContainerOperator contOper, String name) {
        this.contOper = contOper;
        this.name = name;
        this.name = nameButtonOperator().getLabel();
    }
    
    /** Waits for index-th property in specified container.
     * @param contOper ContainerOperator whete to find property. It is
     *                 recommended to use {@link PropertySheetTabOperator}.
     * @param index index (row number) of property inside property sheet
     *              (starts at 0)
     */
    public Property(ContainerOperator contOper, int index) {
        this.contOper = contOper;
        nameButtonOperator = SheetButtonOperator.nameButton(contOper, index);
        this.name = nameButtonOperator.getLabel();
    }
    
    /** Gets SheetButtonOperator instance of property's name button. It returns
     * valid button even if properties were reordered.
     * @return SheetButtonOperator instance of name button
     */
    public SheetButtonOperator nameButtonOperator() {
        if(nameButtonOperator != null) {
            if(!nameButtonOperator.isValid()) {
                nameButtonOperator = null;
            }
        }
        if(nameButtonOperator == null) {
            nameButtonOperator = SheetButtonOperator.nameButton(contOper, name);
        }
        return nameButtonOperator;
    }
    
    /** Gets SheetButtonOperator instance of property's value button. It returns
     * valid button even if properties were reordered.
     * @return SheetButtonOperator instance of value button
     */
    public SheetButtonOperator valueButtonOperator() {
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
    }
    
    /** Gets display name of this property. It is the real name obtained from
     * name button. It can differ from name given in constructor when only
     * substring of property name is used there.
     * @return display name of property
     */
    public String getName() {
        return name;
    }
    
    /** Gets string representation of property value. It is obtained from
     * value button.
     * @return value of property
     */
    public String getValue() {
        return valueButtonOperator().getLabel();
    }
    
    /** Returns true if this property is in editable state (it is being edited).
     * It is detected by presence of PropertySheetButton which stands
     * for property value in non editable state.
     * @return true - this property is being edited; false otherwise
     */
    public boolean isEditable() {
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
    }
    
    /** If this property is not editable, it scrolls to property and clicks
     * on name button. Otherwise does nothing.
     */
    public void startEditing() {
        if(!isEditable()) {
            nameButtonOperator().push();
        }
    }
    
    /** If this property is editable, it scrolls to property if needed and
     * clicks on name button. It cancels editing and sets original value back.
     */
    public void stopEditing() {
        if(isEditable()) {
            nameButtonOperator().push();
        }
    }
    
    
    /** Opens custom property editor for the property by click on "..." button.
     * If this property is not editable, it calls {@link #startEditing()}
     * first.
     */
    public void openEditor() {
        if(!isEditable()) {
            startEditing();
        }
        //click no block on "..." button
        SheetButtonOperator.customizerButton(contOper).pushNoBlock();
    }
    
    /** Sets default value for this property by popup menu on name button.
     * It scrolls to property if needed.
     */
    public void setDefaultValue() {
        nameButtonOperator().clickForPopup();
        String menuItem = Bundle.getString("org.openide.explorer.propertysheet.Bundle", 
                                           "SetDefaultValue");
        new JPopupMenuOperator().pushMenu(menuItem, "|");
        // need to wait until value button is changed
        new EventTool().waitNoEvent(100);
    }
}
