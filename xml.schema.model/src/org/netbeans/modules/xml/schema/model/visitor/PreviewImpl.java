/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
