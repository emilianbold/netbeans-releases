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
 *
 * $Id$
 */
package org.netbeans.installer.product.components;

import java.util.Locale;
import javax.swing.UIManager;
import org.netbeans.installer.product.*;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Kirill Sorokin
 */
public class Group extends ProductRegistryNode {
    public Group() {
        uid = "";
        displayNames.put(Locale.getDefault(), "Product Tree Root");
        descriptions.put(Locale.getDefault(), "");
        icon = UIManager.getDefaults().getIcon("Tree.expandedIcon");
    }
    
    public boolean isEmpty() {
        for (ProductRegistryNode node: getVisibleChildren()) {
            if (node instanceof Group) {
                if (!((Group) node).isEmpty()) {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        return true;
    }
    
    // node <-> dom /////////////////////////////////////////////////////////////////
    protected String getTagName() {
        return "group";
    }
    
    public Group loadFromDom(Element element) throws InitializationException {
        super.loadFromDom(element);
        
        return this;
    }
}
