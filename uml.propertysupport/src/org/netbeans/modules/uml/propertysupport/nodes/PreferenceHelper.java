/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
