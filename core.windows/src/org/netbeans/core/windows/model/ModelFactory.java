/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.model;

/**
 *
 * @author  Peter Zavadsky
 */
public final class ModelFactory {
    
    /** Creates a new instance of ModelFactory */
    private ModelFactory() {
    }
    
    public static Model createWindowSystemModel() {
        return new DefaultModel();
    }
}
