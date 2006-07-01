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
