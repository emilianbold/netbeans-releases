/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tl156378
 */
public class MBeanDO {
    private List/*<MBeanAttribute>*/ attributes;
    private List/*<MBeanOperation>*/ operations;
    
    public MBeanDO(List attributes, List operations) {
        this.attributes = attributes;
        this.operations = operations;
    }
    
    public List getAttributes() {
        return attributes;
    }
    
    public List getOperations() {
        return operations;
    }
}
