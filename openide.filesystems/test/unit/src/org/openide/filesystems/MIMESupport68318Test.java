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
package org.openide.filesystems;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;

/** Simulating stackover flow from issue 68318 
 *
 * @author Jaroslav Tulach
 */
public class MIMESupport68318Test extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.MIMESupport68318Test$Lkp");
    }
    
    public MIMESupport68318Test(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        ErrorManager.getDefault().log("Just initialize the ErrorManager");
    }

    protected void tearDown() throws Exception {
    }

    public void testQueryMIMEFromInsideTheLookup() throws IOException {
        Lkp l = (Lkp)Lookup.getDefault();
        {
            MIMEResolver[] result = MIMESupport.getResolvers();
            assertEquals("One is there", 1, result.length);
            assertEquals("It is c1", Lkp.c1, result[0]);

            assertNotNull("Result computed", l.result);
            assertEquals("But it has to be empty", 0, l.result.length);
        }
        
        l.result = null;
        l.ic.add(Lkp.c2);
        
        {
            MIMEResolver[] result = MIMESupport.getResolvers();
            assertEquals("Now two", 2, result.length);
            assertEquals("It is c1", Lkp.c1, result[0]);
            assertEquals("and c2", Lkp.c2, result[1]);

            assertNotNull("Result in lookup computed", l.result);
            assertEquals("And it contains the previous result", 1, l.result.length);
            assertEquals("which is c1", Lkp.c1, l.result[0]);
        }
    }
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        static MIMEResolver c1 = new MIMEResolver() {
            public String findMIMEType(FileObject fo) {
                return null;
            }
            
            public String toString() {
                return "C1";
            }
        };
        static MIMEResolver c2 = new MIMEResolver() {
            public String findMIMEType(FileObject fo) {
                return null;
            }
            
            public String toString() {
                return "C2";
            }
        };
        private MIMEResolver[] result;
        
        
        public InstanceContent ic;
        public Lkp () {
            this (new InstanceContent ());
        }
        
        private Lkp (InstanceContent ic) {
            super (ic);
            this.ic = ic;
            
            ic.add(c1);
        }

        protected void beforeLookup(org.openide.util.Lookup.Template template) {
            if (template.getType() == MIMEResolver.class) {
                assertNull("First invocation to assign result", result);
                result = MIMESupport.getResolvers();
            }
        }

        
    }
    
}
