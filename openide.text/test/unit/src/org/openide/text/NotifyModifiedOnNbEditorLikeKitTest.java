/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.text;



import org.openide.util.RequestProcessor;


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
    
    @Override
    protected javax.swing.text.EditorKit createEditorKit () {
        NbLikeEditorKit tmp = new NbLikeEditorKit ();
        return tmp;
    }
    
    @Override
    protected void doesVetoedInsertFireBadLocationException (javax.swing.text.BadLocationException e) {
        if (e == null) {
            fail("Vetoed insert has to generate BadLocationException");
        }
    }
    
    private static RequestProcessor testRP = new RequestProcessor("Test");
    @Override
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
