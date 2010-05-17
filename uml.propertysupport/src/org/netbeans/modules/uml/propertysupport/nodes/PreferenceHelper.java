/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.propertysupport.nodes;

import org.openide.nodes.Sheet;
import org.openide.nodes.Node;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;

import java.util.List;

final class PreferenceHelper {

    public static Sheet createNodeProperties(List<IPropertyElement> properties) {
        Sheet sheet = new Sheet();
        Sheet.Set propSet = Sheet.createPropertiesSet();
        boolean haspProps = false;
        for (IPropertyElement elm: properties) {
            Node.Property nprop = DefinitionPropertyBuilder.instance().
                    getPropertyForDefinition(elm.getPropertyDefinition(), elm, true, true);
            if (nprop != null) {
                propSet.put(nprop);
                haspProps = true;
            }
        }
        
        if (haspProps) {
            sheet.put(propSet);
        }
        return sheet;
    }

    /**
     * analyzes subelements of the root element and puts them into children or properties lists 
     * @param root element to analyze 
     * @param children list of subelements that should be presented as node children
     * @param properties list of subelements that should be presented as node properties
     */ 
    public static void analyzeSubElements(IPropertyElement root,
                                           List<IPropertyElement> children,
                                           List<IPropertyElement> properties) {
        // The structure of the preference tree is to only display things in the tree that have of a certain
        // level of child nodes, so this is checking to see if the current element should have a node created
        // in the preference tree or not
        if (hasGrandChildren(root)) {
            
            for (IPropertyElement subElm: root.getSubElements()) {
                // if this element has child elements, then we want to add it to the preference tree
                if (hasChildren(subElm)) {
                    IPropertyDefinition subDef = subElm.getPropertyDefinition();
                    String name = subDef.getDisplayName();
                    String controlType = subDef.getControlType();
                    // definitions that have no display name or control type are for information only
                    // not for display
                    if ( (name == null || name.length() == 0) &&
                            (controlType == null || controlType.length() == 0) ) {
                        // ignore
                    } else {
                        children.add(subElm);
                    }
                } else {
                    if (isPresentableProperty(subElm)) {
                        properties.add(subElm);
                    }
                }
            }
            
        } else {
            for (IPropertyElement subElm: root.getSubElements()) {
                if (isPresentableProperty(subElm)) {
                    properties.add(subElm);
                }
            }
        }
    }
    
    public static void saveModifiedPreferences(IPropertyElement element) {
        element.save();
        IPreferenceManager2 prefMan = ProductRetriever.retrieveProduct().getPreferenceManager();
        prefMan.save();
    }

    private static boolean isPresentableProperty(IPropertyElement elm) {
        IPropertyDefinition def = elm.getPropertyDefinition();
        if (def != null) {
            String displayName = def.getDisplayName();
            if (displayName != null && displayName.length() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Does the passed-in property element have at least two levels of children.
     *
     * @param pEle	The property element in question
     *
     * @return BOOL	Whether or not is has at least two levels of children
     */
    private static boolean hasGrandChildren(IPropertyElement pEle)
    {
        boolean isGrandParent = false;
        if (pEle != null)
        {
            List<IPropertyElement> subEles = pEle.getSubElements();
            if (subEles != null)
            {
                for (IPropertyElement ele: subEles)
                {
                    List<IPropertyElement> subElems2 = ele.getSubElements();
                    if (subElems2 != null)
                    {
                        int count2 = subElems2.size();
                        if (count2 > 0)
                        {
                            isGrandParent = true;
                            break;
                        }
                    }
                }
            }
        }
        return isGrandParent;
    }

    /**
     * Does the passed-in property element have at least one level of children.
     *
     * @param pEle The property element in question
     *
     * @return BOOL	Whether or not is has at least one level of children
     */
    private static boolean hasChildren(IPropertyElement pEle)
    {
        boolean isParent = false;
        
        // the simple fact that it has children is not good enough
        // in some cases, it could have children, but the children may only be information
        // holders, not actually displayed to the user (Fonts/Colors)
        if (pEle != null)
        {
            List<IPropertyElement> subElems = pEle.getSubElements();
            if (subElems != null)
            {
                for (IPropertyElement subEle: subElems)
                {
                    IPropertyDefinition subDef = subEle.getPropertyDefinition();
                    if (subDef != null)
                    {
                        // check if the child is displayed to the user
                        String name = subDef.getDisplayName();
                        String controlType = subDef.getControlType();
                        
                        if ( (name == null || name.length() == 0) &&
                                (controlType == null || controlType.length() == 0) )
                        {
                            //not a valid child
                        }
                        else
                        {
                            isParent = true;
                            break;
                        }
                    }
                }
            }
        }
        
        return isParent;
    }

    
}
