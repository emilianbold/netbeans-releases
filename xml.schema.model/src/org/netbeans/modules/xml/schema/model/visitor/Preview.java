/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * Preview.java
 *
 * Created on October 21, 2005, 9:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.List;
import java.util.Map;

import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * The preview class encapsulates a collection of schema components,
 * all of which, reference the same global schema component.
 *
 * This class is supposed to be used for refactoring purposes.
 * You can obtain the collection of objects that will be impacted before
 * an actual refactroing.
 *
 * @author Samaresh
 */
public interface Preview {
    
    /**
     * Returns a collection of schema components, all of which,
     * reference the same global schema component.
     */
    public Map<SchemaComponent, List<SchemaComponent>> getUsages();
    
}
