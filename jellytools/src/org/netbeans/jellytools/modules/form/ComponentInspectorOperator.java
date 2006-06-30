/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.form;

import java.awt.Component;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;

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
