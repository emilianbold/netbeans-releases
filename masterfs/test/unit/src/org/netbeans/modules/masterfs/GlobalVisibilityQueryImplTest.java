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
            return "^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store)|_svn)$|^\\.[#_]|~$";
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
        
        //#68590 suggests to make also these files "^\\..*$" invisible as default
        assertTrue(vq.isVisible(".telnetrc"));                        
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
