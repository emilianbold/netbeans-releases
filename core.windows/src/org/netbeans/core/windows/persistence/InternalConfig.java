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

package org.netbeans.core.windows.persistence;

import org.openide.modules.SpecificationVersion;

/**
 * Class of internal config properties of window system elements.
 *
 * @author  Marek Slama
 */
public class InternalConfig {
    
    public SpecificationVersion specVersion;
    
    public String moduleCodeNameBase;
    public String moduleCodeNameRelease;
    public String moduleSpecificationVersion;
    
    /** Creates a new instance of InternalConfig */
    public InternalConfig () {
    }
    
    public void clear () {
        specVersion = null;
        moduleCodeNameBase = null;
        moduleCodeNameRelease = null;
        moduleSpecificationVersion = null;
    }
    
}
