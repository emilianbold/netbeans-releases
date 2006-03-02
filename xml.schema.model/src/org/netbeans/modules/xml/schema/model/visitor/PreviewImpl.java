/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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
