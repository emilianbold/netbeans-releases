/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs;

import java.io.File;
import junit.framework.*;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Radek Matous
 */
public class GlobalSharabilityQueryImplTest extends TestCase {
    private static SharabilityQueryImplementation sq = new GlobalSharabilityQueryImpl();
    private static GlobalVisibilityQueryImpl vq = new GlobalVisibilityQueryImpl() {
        protected String getIgnoredFiles() {
            return "^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store)|_svn)$|^\\.[#_]|~$";
        }
    };
    
    static {
        System.setProperty("org.openide.util.Lookup", GlobalSharabilityQueryImplTest.TestLookup.class.getName());
    }
    
    public GlobalSharabilityQueryImplTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite(GlobalSharabilityQueryImplTest.class);
    }
    
    /**
     * Test of getSharability method, of class org.netbeans.modules.masterfs.GlobalSharabilityQueryImpl.
     */
    public void testGetSharability() {
        File[] all = new File[] {
            new File("/myroot/mydirectory/myfile.java"),
                    new File("/myroot/mydirectory/myfile.class"),
                    new File("/myroot/mydirectory/myfile.xml"),
                    new File("/myroot/mydirectory/.cvsignore"),
                    new File("/myroot/mydirectory/CVS"),
                    new File("/myroot/mydirectory/.DS_Store"),
                    new File("/myroot/mydirectory/.svn"),
                    new File("/myroot/mydirectory/_svn")
        };
        
        for (int i = 0; i < all.length; i++) {
            boolean isNotSharable = sq.getSharability(all[i]) == SharabilityQuery.NOT_SHARABLE;
            boolean isNotVisible = !vq.isVisible(all[i].getName());
            assertEquals(isNotSharable, isNotVisible);
        }
    }
    
    public static class TestLookup extends ProxyLookup {
        public TestLookup() {
            super();
            setLookups(new Lookup[] {getInstanceLookup()});
        }
        
        private Lookup getInstanceLookup() {
            InstanceContent instanceContent = new InstanceContent();
            instanceContent.add(sq);
            instanceContent.add(vq);
            Lookup instanceLookup = new AbstractLookup(instanceContent);
            return instanceLookup;
        }        
    }    
}
