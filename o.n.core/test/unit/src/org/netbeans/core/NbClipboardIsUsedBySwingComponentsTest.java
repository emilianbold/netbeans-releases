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

package org.netbeans.core;

import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import junit.textui.TestRunner;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExClipboard;

/** Test that verifies that Clipboard is used by swing components.
 * @author Jaroslav Tulach
 * @see "#40693"
 */
public class NbClipboardIsUsedBySwingComponentsTest extends NbTestCase {
    private Clip clip;
    private javax.swing.JTextField field;
    
    public NbClipboardIsUsedBySwingComponentsTest (String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(Clip.class);
        System.setProperty ("netbeans.security.nocheck", "true");
        Object clip = Lookup.getDefault ().lookup (ExClipboard.class);
        assertNotNull ("Some clipboard found", clip);
        assertEquals ("Correct clipboard found", Clip.class, clip.getClass());
        this.clip = (Clip)clip;
        
        if (System.getSecurityManager () == null) {
            java.text.NumberFormat.getInstance ();

            Object clazz = org.netbeans.TopSecurityManager.class;
            SecurityManager m = new org.netbeans.TopSecurityManager ();
            System.setSecurityManager (m);
            
            inMiddleOfSettingUpTheManager();
            
            org.netbeans.TopSecurityManager.makeSwingUseSpecialClipboard (this.clip);
        } else {
            inMiddleOfSettingUpTheManager();
        }
        
        field = new javax.swing.JTextField ();
    }
    protected boolean runInEQ () {
        return true;
    }
    
    protected javax.swing.JTextField getField () {
        return field;
    }
    
    
    public void testClipboardOurClipboardUsedDuringCopy () {
        javax.swing.JTextField f = getField ();
        f.setText ("Ahoj");
        f.selectAll ();
        assertEquals ("Ahoj", f.getSelectedText ());
        f.copy ();
        
        Clip.assertCalls ("Copy should call setContent", 1, 0);
        assertClipboard ("Ahoj");
    }
    
    public void testClipboardOurClipboardUsedDuringCut () {
        javax.swing.JTextField f = getField ();
        f.setText ("DoCut");
        f.selectAll ();
        assertEquals ("DoCut", f.getSelectedText ());
        f.cut ();
        
        Clip.assertCalls ("Cut should call setContent", 1, 0);
        assertClipboard ("DoCut");
        
        assertEquals ("Empty", "", f.getText ());
    }
    
    public void testClipboardOurClipboardUsedDuringPaste () {
        javax.swing.JTextField f = getField ();
        
        StringSelection sel = new StringSelection ("DoPaste");
        clip.setContents (sel, sel);
        Clip.assertCalls ("Of course there is one set", 1, 0);
        
        assertClipboard ("DoPaste");
        f.paste ();
        
        Clip.assertCalls ("Paste should call getContent", 0, 1);
        assertEquals ("Text is there", "DoPaste", f.getText ());
    }
    
    public void testCopyFromEditorPasteToTheSameOneIssue40785 () {
        javax.swing.JTextField f = getField ();
        f.setText (getName ());
        f.selectAll ();
        assertEquals ("Selection is correct", getName (), f.getSelectedText ());
        f.copy ();
        Clip.assertCalls ("Once in, none out", 1, 0);
        f.setText ("");
        f.paste ();
        Clip.assertCalls ("Once out, none in", 0, 1);
        
        assertEquals ("Test is again the same", getName (), f.getText ());
    }
    
    public void testItIsStillPossibleToGetTheClipboardForNormalCode () throws Exception {
        assertNotNull (
            java.awt.Toolkit.getDefaultToolkit ().getSystemClipboard ()
        );
    }
    
    public void assertClipboard (String text) {
        try {
            Transferable t = clip.getContentsSuper (this);
            Object obj = t.getTransferData (java.awt.datatransfer.DataFlavor.stringFlavor);
            assertEquals ("Clipboard is the same", text, obj);
        } catch (java.io.IOException ex) {
            fail (ex.getMessage ());
        } catch (java.awt.datatransfer.UnsupportedFlavorException ex) {
            fail (ex.getMessage ());
        }
    }

    protected void inMiddleOfSettingUpTheManager() {
    }
    
    public static final class Clip extends ExClipboard {
        private static int setContents;
        private static int getContents;
        
        public Clip () {
            super ("Clip");
        }
        
        protected ExClipboard.Convertor[] getConvertors () {
            return new ExClipboard.Convertor[0];
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
        public Transferable getContentsSuper (Object requestor) {
            return  super.getContents (requestor);
        }
        
        public static void assertCalls (String msg, int setContents, int getContents) {
            if (setContents != -1) assertEquals (msg + " setContents", setContents, Clip.setContents);
            if (getContents != -1) assertEquals (msg + " getContents", getContents, Clip.getContents);
            
            Clip.setContents = 0;
            Clip.getContents = 0;
        }
    } // Clip
}
