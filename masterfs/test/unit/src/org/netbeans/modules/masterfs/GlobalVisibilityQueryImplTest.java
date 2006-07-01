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

import junit.framework.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Radek Matous
 */
public class GlobalVisibilityQueryImplTest extends TestCase {
    private static GlobalVisibilityQueryImpl vq = new GlobalVisibilityQueryImpl() {
        protected String getIgnoredFiles() {
            return "^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store)|_svn)$|~$|^\\..*$";//NOI18N
        }
    };
    
    static {
        System.setProperty("org.openide.util.Lookup", GlobalVisibilityQueryImplTest.TestLookup.class.getName());
    }
    
    public GlobalVisibilityQueryImplTest (String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite(GlobalVisibilityQueryImplTest .class);
    }
        
    public void testVisibility() {        
        assertFalse(vq.isVisible(".#telnetrc"));
        assertFalse(vq.isVisible("._telnetrc"));
        assertFalse(vq.isVisible(".#_telnetrc"));
        assertFalse(vq.isVisible(".cvsignore"));
        assertFalse(vq.isVisible("CVS"));                
        assertFalse(vq.isVisible(".svn"));
        assertFalse(vq.isVisible("_svn"));
        
        assertFalse(vq.isVisible(".telnetrc"));                                
    }
            
    public static class TestLookup extends ProxyLookup {
        public TestLookup() {
            super();
            setLookups(new Lookup[] {getInstanceLookup()});
        }
        
        private Lookup getInstanceLookup() {
            InstanceContent instanceContent = new InstanceContent();
            instanceContent.add(vq);
            Lookup instanceLookup = new AbstractLookup(instanceContent);
            return instanceLookup;
        }        
    }    
}
