/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
