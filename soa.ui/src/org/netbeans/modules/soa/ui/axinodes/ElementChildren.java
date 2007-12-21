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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.soa.ui.axinodes;

import org.netbeans.modules.xml.axi.AbstractElement;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Loads the list of subelements and subattributes
 *
 * @author nk160297
 */
public class ElementChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public ElementChildren(AbstractElement element, Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(new Object[] {element});
    }
    
    protected Node[] createNodes(Object key) {
        assert key instanceof AbstractElement;
        return AxiomUtils.processAxiComponent((AbstractElement)key, myLookup);
    }
    
}
