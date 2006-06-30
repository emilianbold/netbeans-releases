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

/*
 * PreviewImpl.java
 *
 * Created on October 21, 2005, 11:55 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 *
 * @author Samaresh
 */
public class PreviewImpl implements Preview {
    
    /**
     * Collection of schema components, all of which,
     * reference the same global schema component.
     */
    private Map<SchemaComponent, List<SchemaComponent>> usages =
            new HashMap<SchemaComponent, List<SchemaComponent>>();
    
    /**
     * Returns a collection of schema components, all of which,
     * reference the same global schema component.
     */
    public Map<SchemaComponent, List<SchemaComponent>> getUsages() {
        return usages;
    }
        
    void addToUsage(SchemaComponent component) {        
        List<SchemaComponent> temp = new ArrayList<SchemaComponent>();
	SchemaComponent sc = component;
	while (sc != null) {
	    temp.add(sc);
	    sc = sc.getParent();
	};
        Collections.reverse(temp);
        usages.put(component, temp);
    }

}
