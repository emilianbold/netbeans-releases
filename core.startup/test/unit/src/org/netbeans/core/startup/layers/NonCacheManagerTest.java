/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup.layers;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;
/** Test absence of layer cache manager.
 * @author Jesse Glick
 */
public class NonCacheManagerTest extends CacheManagerTestBaseHid 
implements CacheManagerTestBaseHid.ManagerFactory {
    
    public NonCacheManagerTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        if (System.getProperty("nbjunit.workdir") == null) {
            // Hope java.io.tmpdir is set...
            System.setProperty("nbjunit.workdir", System.getProperty("java.io.tmpdir"));
        }
        System.setProperty("org.openide.util.Lookup", "-");
        //System.setProperty("org.netbeans.core.Plain.CULPRIT", "true");
        System.setProperty("org.netbeans.core.projects.cache", "0");
        TestRunner.run(new NbTestSuite(NonCacheManagerTest.class));
    }
    
    
    //
    // Manager factory methods
    //
    public LayerCacheManager createManager() throws Exception {
        return LayerCacheManager.emptyManager();
    }

    public boolean supportsTimestamps () {
        return false;
    }
}
