/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.performance.bde;

import java.io.File;

/** Describes one test */
public final class StoreDefinition {
    
    private File file;
    
   /** Creates new Interval */
    public StoreDefinition(String file) {
        if (file == null) {
            file = LoadDefinition.RES;
        }
        this.file = new File(LoadDefinition.DIR, file);
    }
    
    /** @return file */
    public File getFile() {
        return file;
    }
}
