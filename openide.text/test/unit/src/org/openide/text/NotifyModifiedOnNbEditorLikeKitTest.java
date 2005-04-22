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

package org.openide.text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import junit.framework.*;

import org.netbeans.junit.*;

import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.*;


/** Testing different features of NotifyModifieTest with NbEditorKit
 *
 * @author Jaroslav Tulach
 */
public class NotifyModifiedOnNbEditorLikeKitTest extends NotifyModifiedTest {
    private NbLikeEditorKit k;
    
    public NotifyModifiedOnNbEditorLikeKitTest (String s) {
        super (s);
    }
    
    //
    // overwrite editor kit
    //
    
    protected javax.swing.text.EditorKit createEditorKit () {
        NbLikeEditorKit k = new NbLikeEditorKit ();
        return k;
    }
    
    protected void doesVetoedInsertFireBadLocationException (javax.swing.text.BadLocationException e) {
        if (e == null) {
            fail("Vetoed insert has to generate BadLocationException");
        }
    }
    
    private static RequestProcessor testRP = new RequestProcessor("Test");
    protected void checkThatDocumentLockIsNotHeld () {
        class X implements Runnable {
            private boolean second;
            private boolean ok;

            public void run () {
                if (second) {
                    ok = true;
                    return;
                } else {
                    second = true;
                    javax.swing.text.Document doc = support.getDocument ();
                    assertNotNull (doc);
                    // we have to pass thru read access
                    doc.render (this);
                    
                    if (ok) {
                        try {
                            // we have to be allowed to do modifications as well
                            doc.insertString (-1, "A", null);
                            ok = false;
                        } catch (javax.swing.text.BadLocationException ex) {
                        }

                        try {
                            doc.remove (-1, 1);
                            ok = false;
                        } catch (javax.swing.text.BadLocationException ex) {
                        }
                    }
                        
                    return;
                }
            }
        }

        X x = new X ();
        testRP.post (x).waitFinished ();
        assertTrue ("No lock is held on document when running notifyModified", x.ok);
    }
}
