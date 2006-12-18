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

package org.netbeans.modules.tomcat5.util;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Common ancestor for all test classes.
 *
 * @author sherold
 */
public class TestBase extends NbTestCase {

    static {
        // set the lookup which will be returned by Lookup.getDefault()
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        assertEquals("Unable to set the default lookup!", Lkp.class, Lookup.getDefault().getClass());
        
        ((Lkp)Lookup.getDefault()).setRepository(new RepositoryImpl());
        assertEquals("The default Repository is not our repository!", RepositoryImpl.class, Repository.getDefault().getClass());
    }
    
    public TestBase(String name) {
        super(name);
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            setProxyLookups(new Lookup[0]);
        }
        
        private void setProxyLookups(Lookup[] lookups) {
            Lookup[] allLookups = new Lookup[lookups.length + 2];
            ClassLoader l = TestBase.class.getClassLoader();
            allLookups[allLookups.length - 1] = Lookups.metaInfServices(l);
            allLookups[allLookups.length - 2] = Lookups.singleton(l);
            System.arraycopy(lookups, 0, allLookups, 0, lookups.length);
            setLookups(allLookups);
        }
        
        private void setRepository(Repository repository) {
            // must set out repository first
            setProxyLookups(new Lookup[] {
                Lookups.singleton(repository),
            });
            
            DataFolder services = DataFolder.findFolder(repository.getDefaultFileSystem().getRoot().getFileObject("Services"));
            FolderLookup lookup = new FolderLookup(services);
            setProxyLookups(new Lookup[] {
                Lookups.singleton(repository),
                new FolderLookup(services).getLookup()
            });
        }
    }
}
