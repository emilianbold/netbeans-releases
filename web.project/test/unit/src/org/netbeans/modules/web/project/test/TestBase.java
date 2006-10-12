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

package org.netbeans.modules.web.project.test;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Common ancestor for all test classes.
 *
 * @author Andrei Badea, Radko Najman
 */
public class TestBase extends NbTestCase {
    
    private static final Repository REPOSITORY;
    
    static {
        // for setting the default lookup to TestUtil's one
        TestUtil.setLookup(new Object[0]);
        
        REPOSITORY = new RepositoryImpl();
    }
    
    public static void setLookup(Object[] instances) {
        Object[] newInstances = new Object[instances.length + 1];
        System.arraycopy(instances, 0, newInstances, 0, instances.length);
        newInstances[newInstances.length - 1] = REPOSITORY;
        
        TestUtil.setLookup(newInstances);
    }
    
    public TestBase(String name) {
        super(name);
    }
}
