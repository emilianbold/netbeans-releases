/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.form;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import javax.swing.JComponent;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/**
 * Handle editing of forms in IDE. It enables to design components and also
 * modify source code.
 * <p>
 * Usage:<br>
 * <pre>
        FormDesignerOperator designer = new FormDesignerOperator("MyForm");
        ComponentPaletteOperator palette = new ComponentPaletteOperator();
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        //add first panel
        palette.expandSwing();
        palette.selectComponent("JPanel");
        designer.clickOnComponent(designer.fakePane().getSource());
        //set layout to north
        inspector.selectComponent("JFrame|jPanel1"); // NOI18N
        new Property(inspector.properties(), "Direction").setValue("North"); // NOI18N
        //find panel
        Component firstPanel = designer.findComponent(JPanel.class);
        //add something there
        palette.expandSwing();
        palette.selectComponent("JLabel"); // NOI18N
        designer.clickOnComponent(firstPanel);
        // get editor and do editing
        EditorOperator editor = designer.editor();
        editor.insert("my code", 23, 1);
 * </pre>
 *
 * @see ComponentInspectorOperator
 * @see ComponentPaletteOperator
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class FormDesignerOperator extends TopComponentOperator {
    private ComponentOperator _handleLayer;
    private ContainerOperator _componentLayer;
    private ContainerOperator _fakePane;
    private JToggleButtonOperator _tbSource;
    private JToggleButtonOperator _tbDesign;
    private JToggleButtonOperator _tbSelectionMode;
    private JToggleButtonOperator _tbConnectionMode;
    private JButtonOperator _btTestForm;
    private JButtonOperator _btPaletteManager;

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
    
    /** Returns JToggleButtonOperator instance of Source button
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbSource() {
        if(_tbSource == null) {
            _tbSource = new JToggleButtonOperator(this, 
                                    Bundle.getString("org.netbeans.modules.form.Bundle", 
                                                     "CTL_SourceTabCaption"));
        }
        return _tbSource;
    }
    
    /** Returns JToggleButtonOperator instance of Design button
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbDesign() {
        if(_tbDesign == null) {
            _tbDesign = new JToggleButtonOperator(this, 
                                    Bundle.getString("org.netbeans.modules.form.Bundle", 
                                                     "CTL_DesignTabCaption"));
        }
        return _tbDesign;
    }

    /** Getter for the "Selection Mode" toggle button.
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbSelectionMode() {
        if(_tbSelectionMode == null) {
            _tbSelectionMode = new JToggleButtonOperator(this, new ToolTipChooser(
                                    Bundle.getString("org.netbeans.modules.form.Bundle", 
                                                     "CTL_SelectionMode")));
        }
        return(_tbSelectionMode);
    }

    /** Getter for the "Connection Mode" toggle button.
     * @return JToggleButtonOperator instance
     */
    public JToggleButtonOperator tbConnectionMode() {
        if(_tbConnectionMode == null) {
            _tbConnectionMode = new JToggleButtonOperator(this, new ToolTipChooser(
                                    Bundle.getString("org.netbeans.modules.form.Bundle", 
                                                     "CTL_ConnectionMode")));
        }
        return(_tbConnectionMode);
    }

   /** Getter for the "Palette Manager" button.
     * @return JButtonOperator instance
     */
    public JButtonOperator btPaletteManager() {
        if(_btPaletteManager == null) {
            _btPaletteManager = new JButtonOperator(this, new ToolTipChooser(
                                   Bundle.getString("org.netbeans.modules.form.palette.Bundle",
                                                    "CTL_PaletteManager_Title")));
        }
        return _btPaletteManager;
    }

    /** Getter for the "Test Form" button.
     * @return JButtonOperator instance
     */
    public JButtonOperator btTestForm() {
        if(_btTestForm == null) {
            _btTestForm = new JButtonOperator(this, new ToolTipChooser(
                                   Bundle.getString("org.netbeans.modules.form.actions.Bundle", 
                                                    "ACT_TestMode")));
        }
        return _btTestForm;
    }

    /** Switches to the source editor. It pushes Source toggle button if we
     * are not in source editor already.
     */
    public void source() {
        if(!tbSource().isSelected()) {
            tbSource().push();
        }
        waitState(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return tbSource().isSelected();
            }
            public String getDescription() {
                return("Source toggle button is selected");
            }
        });
    }
    
    /** Switches to the form designer. It pushes Design toggle button if we
     * are not in form designer already.
     */
    public void design() {
        if(!tbDesign().isSelected()) {
            tbDesign().push();
        }
        waitState(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return tbDesign().isSelected();
            }
            public String getDescription() {
                return("Design toggle button is selected");
            }
        });
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
    
    /** Pushes "Palette Manager" button and waits for the Palette Manager dialog opened.
     * @return NbDialogOperator instance of "Palette Manager" window
     */
    public NbDialogOperator paletteManager() {
        btPaletteManager().push();
        return new NbDialogOperator(Bundle.getString("org.netbeans.modules.form.palette.Bundle",
                                                     "CTL_PaletteManager_Title"));
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

    /** Clicks Source button and returns EditorOperator to handle form source
     * code.
     * @return EditorOperator instance
     */
    public EditorOperator editor() {
        source();
        return new EditorOperator((JComponent)this.waitSubComponent(new EditorOperator.EditorSubchooser()));
    }
    
    private static class FormDesignerChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return null != new ContainerOperator((Container)comp).findSubComponent(new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return comp.getClass().getName().equals("org.netbeans.modules.form.FormDesigner") ||
                           comp.getClass().getName().equals("org.netbeans.modules.form.FormEditorSupport$JavaEditorTopComponent");
                }
                public String getDescription() {
                    return("FormDesigner or Form Java Editor");
                }
            });
        }
        public String getDescription() {
            return("FormDesigner or Form Java Editor");
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
    
    private static class ToolTipChooser implements ComponentChooser {
        private String tooltip;
        public ToolTipChooser(String tooltip) {
            this.tooltip = tooltip;
        }
        public boolean checkComponent(Component comp) {
            return ((JComponent)comp).getToolTipText().equals(tooltip);
        }
        public String getDescription() {
            return("ToolTip equals to "+tooltip);
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
        tbSource();
        tbDesign();
    }
    
}
