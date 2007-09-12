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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.bpel.core.wizard;

import java.awt.Component;
import java.awt.Container;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import org.openide.WizardDescriptor;
/**
 *
 * @author  from nb webservice module
 */
public class Utilities {
    
    public static String[] createSteps(String[] before,
            WizardDescriptor.Panel[] panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
    
    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static JTextComponent findTextFieldForLabel(JComponent component,
            String text) {
        JLabel label = findLabel(component, text);
        if(label != null) {
            Component comp = label.getLabelFor();
            if (comp!=null && (comp instanceof JTextComponent))
                return (JTextComponent)comp;
        }
        return null;
    }
    
    
    /*
     * taken from j2ee module
     * Recursively gets all components in the components array and puts it in allComponents
     */
    private static void getAllComponents( Component[] components, 
            Collection allComponents ) 
    {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( 
                            ( ( Container )components[i] ).getComponents(), 
                            allComponents );
                }
            }
        }
    }
    
    /*
     *  taken from j2ee module
     *  Recursively finds a JLabel that has labelText in comp
     */
    private static JLabel findLabel(JComponent comp, String labelText) {
        Vector allComponents = new Vector();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator iterator = allComponents.iterator();
        while(iterator.hasNext()) {
            Component c = (Component)iterator.next();
            if(c instanceof JLabel) {
                JLabel label = (JLabel)c;
                if(label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }
    
    
}
