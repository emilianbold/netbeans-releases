/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.properties;

import java.awt.Component;
import javax.swing.JComponent;
import org.netbeans.core.NbSheet;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.HelpAction;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.actions.ShowDescriptionAreaAction;
import org.netbeans.jellytools.actions.SortByCategoryAction;
import org.netbeans.jellytools.actions.SortByNameAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.openide.explorer.propertysheet.PropertySheet;

/**
 * Handles org.openide.explorer.propertysheet.PropertySheet which
 * represents IDE property sheet TopComponent.
 * It includes JTable with properties and optional description area.
 * Use {@link Property} class or its descendants to work with properties.
 * <p>
 * Usage:<br>
 * <pre>
        PropertySheetOperator pso = new PropertySheetOperator("Properties of MyClass");
        new Property(pso, "Arguments").setValue("arg1 arg2");
        pso.sortByName();
        System.out.println("Number of properties="+pso.tblSheet().getRowCount());
        pso.sortByCategory();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 * @see Property
 * @see PropertiesAction
 * @see SortByCategoryAction
 * @see SortByNameAction
 * @see ShowDescriptionAreaAction
 * @see HelpAction
 */
public class PropertySheetOperator extends TopComponentOperator {
    // in IDE PropertySheet extends JPanel (parent org.netbeans.core.NbSheet extends TopComponent)
    
    /** Operator for tabbed pane
     * @deprecated will be removed because of property sheet rewrite
     */
    private JTabbedPaneOperator _tbpPropertySheetTabPane;
    /** JTable representing property sheet. */
    private JTableOperator _tblSheet;
    private JLabelOperator _lblDescriptionHeader;
    private JTextAreaOperator _txtDescription;
    private JButtonOperator _btHelp;
    
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
     * @param sheet instance of PropertySheet
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
    
    /** Returns JTableOperator representing SheetTable of this property sheet. 
     * @return instance of JTableOperator
     */
    public JTableOperator tblSheet() {
        if(_tblSheet == null) {
            _tblSheet = new JTableOperator(this);
        }
        return _tblSheet;
    }
    
    /** Returns JLabelOperator representing header of description area.
     * @return instance of JLabelOperator
     */
    public JLabelOperator lblDescriptionHeader() {
        if(_lblDescriptionHeader == null) {
            _lblDescriptionHeader = new JLabelOperator(this);
        }
        return _lblDescriptionHeader;
    }
    
    /** Returns JTextAreaOperator representing text from description area.
     * @return instance of JTextAreaOperator
     */
    public JTextAreaOperator txtDescription() {
        if(_txtDescription == null) {
            _txtDescription = new JTextAreaOperator(this);
        }
        return _txtDescription;
    }
    
    /** Returns JButtonOperator representing help button of description area.
     * @return instance of JButtonOperator
     */
    public JButtonOperator btHelp() {
        if(_btHelp == null) {
            _btHelp = new JButtonOperator(this);
        }
        return _btHelp;
    }
    
    /** Gets JTabbedPaneOperator of this property sheet.
     * @return instance of JTabbedPaneOperator
     * @deprecated JTabbedPane is no more used in property sheet.
     */
    public JTabbedPaneOperator tbpPropertySheetTabbedPane() {
        throw new JemmyException("Don't use this! JTabbedPane no more used in property sheet.");
        /*
        if(_tbpPropertySheetTabPane == null) {
            _tbpPropertySheetTabPane = new JTabbedPaneOperator(this);
        }
        return _tbpPropertySheetTabPane;
         */
    }
    
    /** Gets PropertySheetTabOperator with given name. It selects requested tab
     * if exist and it is not active.
     * @param tabName name of tab to find.
     * @return instance of PropertySheetTabOperator
     * @deprecated will be removed because of property sheet rewrite
     */
    public PropertySheetTabOperator getPropertySheetTabOperator(String tabName) {
        return new PropertySheetTabOperator(this, tabName);
    }
    
    /** Gets PropertySheetToolbarOperator for this property sheet.
     * @return instance of PropertySheetToolbarOperator
     * @deprecated Tool bar no more used in property sheet.
     */
    public PropertySheetToolbarOperator getToolbar() {
        throw new JemmyException("Don't use this! Tool bar no more used in property sheet.");
        //return new PropertySheetToolbarOperator(this);
    }

    /** Gets text of header from description area.
     * @return text of header from description area
     */
    public String getDescriptionHeader() {
        return lblDescriptionHeader().getText();
    }
    
    /** Gest description from description area.
     * @return description from description area.
     */
    public String getDescription() {
        return txtDescription().getText();     
    }
    
    /** Sorts properties by name by calling of popup menu on property sheet. */
    public void sortByName() {
        new SortByNameAction().perform(this);
    }

    /** Sorts properties by category by calling of popup menu on property sheet. */
    public void sortByCategory() {
        new SortByCategoryAction().perform(this);
    }
    
    /** Shows or hides description area depending on whether it is already shown 
     * or not. It just invokes Show description area popup menu item.
     */
    public void showDescriptionArea() {
        new ShowDescriptionAreaAction().perform(this);
    }
    
    /** Shows help by calling popup menu on property sheet. */
    public void help() {
        new HelpAction().performPopup(this);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tblSheet();
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
