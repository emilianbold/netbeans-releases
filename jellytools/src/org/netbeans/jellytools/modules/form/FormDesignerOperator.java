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
import java.awt.Point;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/**
 * Handles access to org.netbeans.modules.form.FormDesigner component.
 */
public class FormDesignerOperator extends TopComponentOperator {
    private ComponentOperator _handleLayer;
    private ContainerOperator _componentLayer;
    private ContainerOperator _fakePane;
    private JToggleButtonOperator _tbSelectionMode;
    private JToggleButtonOperator _tbConnectionMode;
    private JButtonOperator _btTestForm;

    /** Waits for the form Designer appearence and creates operator for it.
     * It is activated by defalt.
     */
    public FormDesignerOperator() {
        super(waitTopComponent(null, null, 0, new FormDesignerChooser()));
    }

    /** Waits for the form Designer appearence and creates operator for it.
     * It is activated by defalt.
     * @param name name of form designer
     */
    public FormDesignerOperator(String name) {
        super(waitTopComponent(null, name, 0, new FormDesignerChooser()));
    }

    
    /** Searches for FormDesigner in the specified ContainerOperator.
     * @param contOper ContainerOperator where to find FormDesigner
     */
    public FormDesignerOperator(ContainerOperator contOper) {
        super(waitTopComponent(contOper, null, 0, new FormDesignerChooser()));
        copyEnvironment(contOper);
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
        return _btTestForm;
    }

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
    
    /** Returns component which actually handles all events happening
     * on components inside designer.
     * During reproducing, all events should be posted to this component.
     * @see #convertCoords(java.awt.Component, java.awt.Point)
     * @see #convertCoords(java.awt.Component)
     * @return ComponentOperator for handle layer
     */
    public ComponentOperator handleLayer() {
        if(_handleLayer == null) {
            _handleLayer = createSubOperator(new HandleLayerChooser());
        }
        return(_handleLayer);
    }
    
    /** Return ContainerOperator for a component which contains all the designing components.
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser, int)
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser)
     * @see #findComponent(java.lang.Class, int)
     * @see #findComponent(java.lang.Class)
     * @return ContainerOperator for component layer
     */
    public ContainerOperator componentLayer() {
        if(_componentLayer == null) {
            _componentLayer = new ContainerOperator((Container)waitSubComponent(new ComponentLayerChooser()));
        }
        return(_componentLayer);
    }
    
    /** Returns ContainerOperator for component which represents designing form 
     * (like JFrame, JDialog, ...).
     * @return ContainerOperator for fake pane
     */
    public ContainerOperator fakePane() {
        if(_fakePane == null) {
            _fakePane = new ContainerOperator((Container)componentLayer().waitSubComponent(new FakePaneChooser()));
        }
        return(_fakePane);
    }

    /** Converts relative coordinates inside one of the components
     * laying on the designer to coordinates relative to handleLayer()
     * @see #handleLayer()
     * @see #componentLayer()
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser, int)
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser)
     * @see #findComponent(java.lang.Class, int)
     * @see #findComponent(java.lang.Class)
     * @param subComponent Component in designer.
     * @param localCoords Local <code>subComponent</code>'s coordinates
     * @return coordinates relative to handle layer
     */
    public Point convertCoords(Component subComponent, Point localCoords) {
        Point subLocation = subComponent.getLocationOnScreen();
        Point location = handleLayer().getLocationOnScreen();
        return(new Point(subLocation.x - location.x + localCoords.x,
                         subLocation.y - location.y + localCoords.y));
    }

    /** Converts components center coordinates
     * to coordinates relative to handleLayer()
     * @see #handleLayer()
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser, int)
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser)
     * @see #findComponent(java.lang.Class, int)
     * @see #findComponent(java.lang.Class)
     * @param subComponent Component in designer.
     * @return coordinates of the center of the subComponent relative to handle layer
     */
    public Point convertCoords(Component subComponent) {
        return(convertCoords(subComponent, new Point(subComponent.getWidth() / 2, 
                                                     subComponent.getHeight() / 2)));
    }

    /**
     * Clicks on component. Events are really sent to handleLayer()
     * @param subComponent Component in designer.
     * @param localCoords Local <code>subComponent</code>'s coordinates
     * @see #handleLayer()
     */
    public void clickOnComponent(Component subComponent, Point localCoords) {
        Point pointToClick = convertCoords(subComponent, localCoords);
        handleLayer().clickMouse(pointToClick.x, pointToClick.y, 1);
    }

    /**
     * Clicks on the component center. Events are really sent to handleLayer()
     * @param subComponent Component in designer.
     * @see #handleLayer()
     */
    public void clickOnComponent(Component subComponent) {
        Point pointToClick = convertCoords(subComponent);
        handleLayer().makeComponentVisible();
        handleLayer().clickMouse(pointToClick.x, pointToClick.y, 1);
    }

    /** Searches a component inside fakePane().
     * @see #fakePane()
     * @param chooser chooser specifying criteria to find a component
     * @param index index of component
     * @return index-th component from fake pane matching chooser's criteria
     */
    public Component findComponent(ComponentChooser chooser, int index) {
        return(fakePane().waitSubComponent(chooser, index));
    }
    
    /** Searches a component inside fakePane().
     * @see #fakePane()
     * @param chooser chooser specifying criteria to find a component
     * @return component from fake pane matching chooser's criteria
     */
    public Component findComponent(ComponentChooser chooser) {
        return(findComponent(chooser, 0));
    }
    
    /** Searches <code>index</code>'s instance of a <code>clzz</code> class inside fakePane().
     * @see #fakePane()
     * @param clzz class of component to be find (e.g. <code>JButton.class</code>)
     * @param index index of component
     * @return index-th component from fake pane of the given class
     */
    public Component findComponent(final Class clzz, int index) {
        return(findComponent(new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return(clzz.isInstance(comp) &&
                           comp.isShowing());
                }
                public String getDescription() {
                    return("Any " + clzz.getName());
                }
            }, index));
    }

    /** Searches first instance of a <code>clzz</code> class inside fakePane().
     * @see #fakePane()
     * @param clzz class of component to be find (e.g. <code>JButton.class</code>)
     * @return first component from fake pane of the given class
     */
    public Component findComponent(Class clzz) {
        return(findComponent(clzz, 0));
    }

    private static class FormDesignerChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().equals("org.netbeans.modules.form.FormDesigner");
        }
        public String getDescription() {
            return("Any FormDesigner");
        }
    }
    
    private static class HandleLayerChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.modules.form.HandleLayer"));
        }
        public String getDescription() {
            return("Any HandleLayer");
        }
    }
    
    private static class ComponentLayerChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.modules.form.ComponentLayer"));
        }
        public String getDescription() {
            return("Any ComponentLayer");
        }
    }
    
    private static class FakePaneChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.modules.form.fakepeer.FakePeerContainer"));
        }
        public String getDescription() {
            return("Any FakePeerContainer");
        }
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        handleLayer();
        componentLayer();
        fakePane();
        btTestForm();
        tbConnectionMode();
        tbSelectionMode();
    }
    
}
