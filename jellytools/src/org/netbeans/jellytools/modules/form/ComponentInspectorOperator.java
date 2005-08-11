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

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.openide.windows.TopComponent;

/**
 * Provides access to org.netbeans.modules.form.ComponentInspector component.
 */
public class ComponentInspectorOperator extends TopComponentOperator {
    private JTreeOperator _treeComponents;
    private PropertySheetOperator _properties;

    /** Waits for the Component Inspector appearence and creates operator for it.
     */
    public ComponentInspectorOperator() {
        super(waitTopComponent(null, null, 0, new ComponentInspectorChooser()));
    }

    /** Finds first ComponentInspector instance inside ContainerOperator. 
     * Usualy it is FormEditorOperator but Component Inspector can be docked 
     * to any window.
     * @param contOper container where to find Component Inspector
     * @deprecated Use {@link #ComponentInspectorOperator()} instead because
     * there is no need to specify container. In fact the Component Inspector
     * is singleton window in IDE.
     */
    public ComponentInspectorOperator(ContainerOperator contOper) {
        super(waitTopComponent(contOper, null, 0, new ComponentInspectorChooser()));
        copyEnvironment(contOper);
    }
    
    /** Getter for component tree.
     * @return JTreeOperator instance
     */
    public JTreeOperator treeComponents() {
        if(_treeComponents == null) {
            _treeComponents = new JTreeOperator(this);
        }
        return(_treeComponents);
    }
    
    /** Getter for PropertySheetOperator. It returns first found property
     * sheet within IDE. It is not guaranteed that it is the global property
     * placed next to Component Inspector by default.
     * @return PropertySheetOperator instance
     */
    public PropertySheetOperator properties() {
        if(_properties == null) {
            _properties = new PropertySheetOperator();
        }
        return(_properties);
    }

    //shortcuts
    
    /** Selects component in the tree.
     * @param componentPath path in component tree (e.g. "[JFrame]|jPanel1")
     */
    public void selectComponent(String componentPath) {
        new Node(treeComponents(), componentPath).select();
    }

    private static class ComponentInspectorChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.modules.form.ComponentInspector"));
        }
        public String getDescription() {
            return("Any ComponentInspector");
        }
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        treeComponents();
        properties().verify();
    }
}
