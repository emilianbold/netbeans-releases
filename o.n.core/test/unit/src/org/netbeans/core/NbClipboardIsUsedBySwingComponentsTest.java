/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;
import junit.textui.TestRunner;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.*;
import org.openide.util.datatransfer.ExClipboard;

/** Test that verifies that Clipboard is used by swing components.
 * @author Jesse Glick
 * @see "#30923"
 */
public class NbClipboardIsUsedBySwingComponentsTest extends NbTestCase {
    private ExClipboard clip;
    
    public NbClipboardIsUsedBySwingComponentsTest (String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NbClipboardIsUsedBySwingComponentsTest.class));
    }
    
    protected void setUp() throws Exception {
        System.setProperty ("org.openide.util.Lookup", "org.netbeans.core.NbClipboardIsUsedBySwingComponentsTest$Lkp");
        clip = (ExClipboard)Lookup.getDefault ().lookup (ExClipboard.class);
        assertNotNull ("Some clipboard found", clip);
        assertEquals ("Correct clipboard found", Clip.class, clip.getClass());
        
        Object clazz = org.netbeans.TopSecurityManager.class;
        SecurityManager m = new org.netbeans.TopSecurityManager ();
        System.setSecurityManager (m);
        org.netbeans.TopSecurityManager.makeSwingUseSpecialClipboard (clip);
    }
    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testClipboardOurClipboardUsedDuringCopy () {
        javax.swing.JTextField f = new javax.swing.JTextField ();
        f.setText ("Ahoj");
        f.selectAll ();
        assertEquals ("Ahoj", f.getSelectedText ());
        f.copy ();
        
        Clip.assertCalls ("Copy should be called", 1, 0);
    }
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new Clip ()); // DataLoaderPool
        }
    } // end of Lkp    
    
    private static final class Clip extends org.openide.util.datatransfer.ExClipboard {
        private static int setContents;
        private static int getContents;
        
        public Clip () {
            super ("Clip");
        }
        
        protected org.openide.util.datatransfer.ExClipboard.Convertor[] getConvertors () {
            return new Convertor[0];
        }
        
        public void setContents (Transferable contents, ClipboardOwner owner) {
            super.setContents (contents, owner);
            setContents++;
        }
        
        public Transferable getContents (Object requestor) {
            Transferable retValue;
            getContents++;
            retValue = super.getContents (requestor);
            return retValue;
        }
        
        public static void assertCalls (String msg, int setContents, int getContents) {
            if (setContents != -1) assertEquals (msg + " setContents", setContents, Clip.setContents);
            if (getContents != -1) assertEquals (msg + " getContents", getContents, Clip.getContents);
            
            Clip.setContents = 0;
            Clip.getContents = 0;
        }
        
    } // Clip
}
