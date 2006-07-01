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
