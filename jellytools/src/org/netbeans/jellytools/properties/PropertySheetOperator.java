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
import javax.swing.JComponent;
import org.netbeans.core.NbSheet;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.openide.explorer.propertysheet.PropertySheet;

/**
 * Handles org.openide.explorer.propertysheet.PropertySheet which
 * represents IDE property sheet TopComponent.
 * It includes toolbar and tabs with properties.
 * Use {@link PropertySheetToolbarOperator} to manipulate toolbar and
 * ancestors of {@link Property} class to work with properties.
 * <p>
 * Usage:<br>
 * <pre>
        PropertySheetOperator pso = new PropertySheetOperator("Properties of MyClass");
        PropertySheetTabOperator psto = pso.getPropertySheetTabOperator("Execution");
        new TextFieldProperty(psto, "Arguments").setValue("args");
        PropertySheetToolbarOperator pstoolbar = pso.getToolbar();
        pstoolbar.unsorted();
 * </pre>

 * @author Jiri.Skrivanek@sun.com
 */
public class PropertySheetOperator extends TopComponentOperator {
    // in IDE PropertySheet extends JPanel (parent org.netbeans.core.NbSheet extends TopComponent)
    
    /** Operator for tabbed pane */
    private JTabbedPaneOperator _tbpPropertySheetTabPane;
    
    /** "No Properties" property sheet. */
    public static final int MODE_NO_PROPERTIES = 0;
    /** "Properties of" property sheet. */
    public static final int MODE_PROPERTIES_OF_ONE_OBJECT = 1;
    /** "Properties of Multiple Objects" property sheet. */
    public static final int MODE_PROPERTIES_OF_MULTIPLE_OBJECTS = 2;
    
    /** "Properties" */
    private static final String propertiesText = Bundle.getStringTrimmed("org.openide.actions.Bundle", 
                                                                         "Properties");
    
    /** Generic constructor
     * @param instance of PropertySheet
     */    
    public PropertySheetOperator(JComponent sheet) {
        super(sheet);
    }
    
    /** Waits for TopComponent with "Properties" in its name. */
    public PropertySheetOperator() {
        this(propertiesText);
    }
    
    /** Waits for TopComponent with name according to given mode ("No Properties",
     * "Properties of" or "Properties of Multiple Objects").
     * @param mode type of shown properties
     * @see #MODE_NO_PROPERTIES
     * @see #MODE_PROPERTIES_OF_ONE_OBJECT
     * @see #MODE_PROPERTIES_OF_MULTIPLE_OBJECTS
     */
    public PropertySheetOperator(int mode) {
        this(mode, "");
    }
    
    /** Waits for TopComponent with name according to given mode ("No Properties",
     * "Properties of" or "Properties of Multiple Objects") plus objectName
     * in case of one object property sheet. In case of usage
     * <code>
     * new PropertySheetOperator(PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT, "MyClass");
     * </code>
     * will be searched TopComponent with name "Properties of MyClass" (on
     * English locale).
     * @param mode type of shown properties
     * @param objectName name of object for that properties are shown (e.g. "MyClass")
     * @see #MODE_NO_PROPERTIES
     * @see #MODE_PROPERTIES_OF_ONE_OBJECT
     * @see #MODE_PROPERTIES_OF_MULTIPLE_OBJECTS
     */
    public PropertySheetOperator(int mode, String objectName) {
        this(Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_GlobalProperties", 
                               new Object[]{new Integer(mode), objectName}));
    }
    
    /** Waits for TopComponent of PropertySheet with given name. Typically sheet
     * name is used as window title.
     * @param sheetName name of sheet to find (e.g. "Properties of MyClass")
     */
    public PropertySheetOperator(String sheetName) {
        this(null, sheetName);
    }
    
    /** Waits for TopComponent of PropertySheet with given name in specified
     * container.
     * @param contOper where to find
     * @param sheetName name of sheet to find (e.g. "Properties of MyClass")
     */
    public PropertySheetOperator(ContainerOperator contOper, String sheetName) {
        super(waitTopComponent(contOper, sheetName, 0, new PropertySheetSubchooser()));
        if(contOper != null) {
            copyEnvironment(contOper);
        }
    }
    
    /** Waits for non TopComponent PropertySheet in specified ContainerOperator.
     * It is for example PropertySheet in Options
     * @param contOper where to find
     */
    public PropertySheetOperator(ContainerOperator contOper) {
        this(contOper,0);
    }
    
    /** Waits for non TopComponent PropertySheet in specified ContainerOperator.
     * It is for example PropertySheet in Options
     * @param contOper where to find
     * @param index int index
     */
    public PropertySheetOperator(ContainerOperator contOper, int index) {
        super((JComponent)contOper.waitSubComponent(new PropertySheetSubchooser(), index));
        copyEnvironment(contOper);
    }
    
    /** Invokes properties by default action on currently selected object.
     * @return instance of PropertySheetOperator
     * @see org.netbeans.jellytools.actions.PropertiesAction
     */
    public static PropertySheetOperator invoke() {
        new PropertiesAction().perform();
        return new PropertySheetOperator();
    }
    
    /** Gets JTabbedPaneOperator of this property sheet.
     * @return instance of JTabbedPaneOperator
     */
    public JTabbedPaneOperator tbpPropertySheetTabbedPane() {
        if(_tbpPropertySheetTabPane == null) {
            _tbpPropertySheetTabPane = new JTabbedPaneOperator(this);
        }
        return _tbpPropertySheetTabPane;
    }
    
    /** Gets PropertySheetTabOperator with given name. It selects requested tab
     * if exist and it is not active.
     * @param tabName name of tab to find.
     * @return instance of PropertySheetTabOperator
     */
    public PropertySheetTabOperator getPropertySheetTabOperator(String tabName) {
        return new PropertySheetTabOperator(this, tabName);
    }
    
    /** Gets PropertySheetToolbarOperator for this property sheet.
     * @return instance of PropertySheetToolbarOperator
     */
    public PropertySheetToolbarOperator getToolbar() {
        return new PropertySheetToolbarOperator(this);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tbpPropertySheetTabbedPane();
    }

    /** SubChooser to determine PropertySheet TopComponent
     * Used in constructors.
     */
    private static final class PropertySheetSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return (comp instanceof PropertySheet || comp instanceof NbSheet);
        }

        public String getDescription() {
            return "org.openide.explorer.propertysheet.PropertySheet";
        }
    }
}
