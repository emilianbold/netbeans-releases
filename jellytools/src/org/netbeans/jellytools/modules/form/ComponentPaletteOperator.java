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

package org.netbeans.jellytools.modules.form;

import java.awt.Component;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/**
 * Keeps methods to access component palette like one inside 
 * form editor.
 */
public class ComponentPaletteOperator extends TopComponentOperator {
    private JToggleButtonOperator _tbSwing;
    private JToggleButtonOperator _tbAWT;
    private JToggleButtonOperator _tbLayouts;
    private JToggleButtonOperator _tbBeans;

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
    
    /** Waits for "Swing" toggle button.
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbSwing() {
        if(_tbSwing == null) {
            _tbSwing = new JToggleButtonOperator(this, 
                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                     "Palette/Swing"));
        }
        return _tbSwing;
    }

    /** Waits for "AWT" toggle button.
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbAWT() {
        if(_tbAWT == null) {
            _tbAWT = new JToggleButtonOperator(this, 
                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                     "Palette/AWT"));
        }
        return _tbAWT;
    }

    /** Waits for "Layouts" toggle button.
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbLayouts() {
        if(_tbLayouts == null) {
            _tbLayouts = new JToggleButtonOperator(this, 
                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                     "Palette/Layouts"));
        }
        return _tbLayouts;
    }

    /** Waits for "Beans" toggle button.
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbBeans() {
        if(_tbBeans == null) {
            _tbBeans = new JToggleButtonOperator(this, 
                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                     "Palette/Beans"));
        }
        return _tbBeans;
    }

    /** Getter for the component types list.
     * List really looks like a toolbar here.
     * @return JListOperator instance of a palette
     */
    public JListOperator lstComponents() {
        return new JListOperator(this);
    }

    //common
    
    /** Select a component category like "Swing"
     * @param pageName name of category to be selected
     * @return JListOperator instance of selected category
     */
    public JListOperator selectPage(String pageName) {
        new JToggleButtonOperator(this, pageName).push();
        return lstComponents();
        
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

    /** Select "Swing" page.
     * @return JListOperator instance of selected tab (palette)
     */
    public JListOperator selectSwingPage() {
        tbSwing().push();
        return lstComponents();
    }    

    /** Select "AWT" page.
     * @return JListOperator instance of selected tab (palette)
     */
    public JListOperator selectAWTPage() {
        tbAWT().push();
        return lstComponents();
    }    

    /** Select "Beans" page.
     * @return JListOperator instance of selected tab (palette)
     */
    public JListOperator selectBeansPage() {
        tbBeans().push();
        return lstComponents();
    }    

    /** Select "Layouts" page.
     * @return JListOperator instance of selected tab (palette)
     */
    public JListOperator selectLayoutsPage() {
        tbLayouts().push();
        return lstComponents();
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
        lstComponents();
        selectAWTPage();
        selectBeansPage();
        selectLayoutsPage();
        selectSwingPage();
    }
}
