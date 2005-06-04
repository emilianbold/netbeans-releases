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

package org.netbeans.modules.html;

import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;

/** Shared utils used in HTML tests.
 *
 * @author Radim Kubacki
 */
public class Utils {
    
    /** Creates a new instance of Utils */
    private Utils() {
    }
    
    /*package*/ static void setUp() throws Exception {
        System.setProperty ("org.openide.util.Lookup", "org.netbeans.modules.html.Utils$Lkp");
    }
    
    /** 
     * Fake lookup for testings purposes.
     */
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () throws Exception {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) throws Exception {
            super (ic);
            
            ic.add (new Pool ());
            ic.add (new org.netbeans.modules.editor.html.HTMLIndentEngine());
//            ic.add (new EM ());
        }
    }
    
    
    private static final class Pool extends DataLoaderPool {
        
        protected java.util.Enumeration loaders () {
            return org.openide.util.Enumerations.singleton (
                DataLoader.getLoader(HtmlLoader.class)
            );
        }
        
    } // end of Pool

}
