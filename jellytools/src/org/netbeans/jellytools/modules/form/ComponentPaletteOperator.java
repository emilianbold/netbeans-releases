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

package org.netbeans.jellytools.modules.form;

import java.awt.Component;
import java.awt.Container;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.openide.windows.TopComponent;

/**
 * Keeps methods to access component palette like one inside 
 * form editor.
 */
public class ComponentPaletteOperator extends TopComponentOperator {
    private JTabbedPaneOperator _tbpComponents;
    private JToggleButtonOperator _tbSelectionMode;
    private JToggleButtonOperator _tbConnectionMode;
    private JButtonOperator _btTestForm;

    /** Waits for the Component Palette appearence and creates operator for it.
     */
    public ComponentPaletteOperator() {
        super(waitTopComponent(null, null, 0, new PaletteTopComponentChooser()));
    }

    /**
     * Creates an instance for the first ComponentPalette appearence
     * inside ContainerOperator. Usualy it is FormEditorOperator but Component
     * Palette can be docked to any window.
     * @param contOperator container where to find Component Palette
     * @deprecated Use {@link ComponentPaletteOperator()} instead because
     * there is no need to specify container. In fact the Component Palette
     * is singleton window in IDE.
     */
    public ComponentPaletteOperator(ContainerOperator contOperator) {
        super(waitTopComponent(contOperator, null, 0, new PaletteTopComponentChooser()));
        copyEnvironment(contOperator);
    }

    //subcomponents
    
    /** Getter for component types tabbed.
     * @return JTabbedPaneOperator instance
     */
    public JTabbedPaneOperator tbpComponents() {
        if(_tbpComponents == null) {
            _tbpComponents = new JTabbedPaneOperator(this);
        }
        return(_tbpComponents);
    }

    /** Getter for the "Selection Mode" toggle button.
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbSelectionMode() {
        if(_tbSelectionMode == null) {
            _tbSelectionMode = new JToggleButtonOperator(this, 0);
        }
        return(_tbSelectionMode);
    }

    /** Getter for the "Connection Mode" toggle button.
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbConnectionMode() {
        if(_tbConnectionMode == null) {
            _tbConnectionMode = new JToggleButtonOperator(this, 1);
        }
        return(_tbConnectionMode);
    }

    /** Getter for the "Test Form" button.
     * @return JButtonOperator instance
     */
    public JButtonOperator btTestForm() {
        if(_btTestForm == null) {
            _btTestForm = new JButtonOperator(this);
        }
        return(_btTestForm);
    }

    /** Getter for the component types list.
     * List really looks like a toolbar here.
     * @return JListOperator instance of a palette
     */
    public JListOperator lstComponents() {
        return(new JListOperator(tbpComponents()));
    }

    //common
    
    /** Select a component types tabbed page like "Swing"
     * @param pageName name of tab to be selected
     * @return JListOperator instance of selected tab (palette)
     */
    public JListOperator selectPage(String pageName) {
        tbpComponents().selectPage(pageName);
        return(lstComponents());
        
    }

    /** Select a component on the active page (palette).
     * @param displayName display name of component to be selected (e.g. JButton)
     */
    public void selectComponent(String displayName) {
        //TBD approach used here is not clearly "black box"
        //it might make sense to use getToolTipText(MouseEvent)
        //to find item by tooltip (support from Jemmy might be necessary)
        lstComponents().selectItem("displayName=" + displayName);
    }

    //shortcuts

    /**
     * Switches to the selection mode.
     */
    public void selectionMode() {
        tbSelectionMode().push();
    }

    /**
     * Switches to the connection mode.
     */
    public void connectionMode() {
        tbConnectionMode().push();
    }

    /** Pushes "Test Form" button and waits for a frame opened.
     * @param frameName Frame class name.
     * @return JFrameOperator instance of "Testing Form" window
     */
    public JFrameOperator testForm(String frameName) {
        btTestForm().push();
        return(new JFrameOperator(Bundle.getString("org.netbeans.modules.form.actions.Bundle",
                                                   "FMT_TestingForm",
                                                   new Object[] {frameName})));
    }

    /** Pushes "Test Form" button and waits for a frame opened.
     * @return JFrameOperator instance of "Testing Form" window
     */
    public JFrameOperator testForm() {
        btTestForm().push();
        return(new JFrameOperator(Bundle.getStringTrimmed("org.netbeans.modules.form.actions.Bundle",
                                                          "FMT_TestingForm")));
    }

    /** Select "Swing" page.
     * @return JListOperator instance of selected tab (palette)
     */
    public JListOperator selectSwingPage() {
        return(selectPage(Bundle.getString("org.netbeans.modules.form.resources.Bundle", 
                                           "Palette/Swing")));
    }    

    /** Select "Swing (Other)" page.
     * @return JListOperator instance of selected tab (palette)
     */
    public JListOperator selectSwingOtherPage() {
        return(selectPage(Bundle.getString("org.netbeans.modules.form.resources.Bundle", 
                                           "Palette/Swing2")));
    }    

    /** Select "AWT" page.
     * @return JListOperator instance of selected tab (palette)
     */
    public JListOperator selectAWTPage() {
        return(selectPage(Bundle.getString("org.netbeans.modules.form.resources.Bundle", 
                                           "Palette/AWT")));
    }    

    /** Select "Beans" page.
     * @return JListOperator instance of selected tab (palette)
     */
    public JListOperator selectBeansPage() {
        return(selectPage(Bundle.getString("org.netbeans.modules.form.resources.Bundle", 
                                           "Palette/Beans")));
    }    

    /** Select "Layouts" page.
     * @return JListOperator instance of selected tab (palette)
     */
    public JListOperator selectLayoutsPage() {
        return(selectPage(Bundle.getString("org.netbeans.modules.form.resources.Bundle", 
                                           "Palette/Layouts")));
    }    

    /** Select "Borders" page.
     * @return JListOperator instance of selected tab (palette)
     */
    public JListOperator selectBordersPage() {
        return(selectPage(Bundle.getString("org.netbeans.modules.form.resources.Bundle", 
                                           "Palette/Borders")));
    }    

    private static class PaletteTopComponentChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.modules.form.palette.PaletteTopComponent"));
        }
        public String getDescription() {
            return("Any PaletteTopComponent");
        }
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btTestForm();
        tbConnectionMode();
        tbSelectionMode();
        lstComponents();
        selectAWTPage();
        selectBeansPage();
        selectBordersPage();
        selectLayoutsPage();
        selectSwingOtherPage();
        selectSwingPage();
    }
}
