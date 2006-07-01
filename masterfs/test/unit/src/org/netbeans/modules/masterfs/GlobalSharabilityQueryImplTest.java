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
            return "^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store)|_svn)$|~$|^\\..*$";//NOI18N
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
