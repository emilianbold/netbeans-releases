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
