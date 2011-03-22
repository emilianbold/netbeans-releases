/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.smarty.editor.utlis;

import javax.swing.text.Document;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.php.smarty.editor.TplKit;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.text.CloneableEditor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Martin Fousek
 */
public class LexerUtils {

    public static final boolean isVariablePart(int character) {
        return Character.isJavaIdentifierPart(character);
    }

    public static final boolean isWS(int character) {
        return Character.isWhitespace(character);
    }

    public static final void relexerOpenedTpls() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                TopComponent[] topComponents = WindowManager.getDefault().getOpenedTopComponents(WindowManager.getDefault().findMode("editor"));
                for (TopComponent topComponent : topComponents) {
                    if (topComponent instanceof CloneableEditor) {
                        final Document doc = ((CloneableEditor) topComponent).getEditorPane().getDocument();
                        if (((CloneableEditor) topComponent).getEditorPane().getEditorKit() instanceof TplKit) {
                            ((TplKit) ((CloneableEditor) topComponent).getEditorPane().getEditorKit()).initLexerColoringListener(doc);
                        }
                        NbEditorDocument nbdoc = (NbEditorDocument) doc;
                        nbdoc.runAtomic(new Runnable() {

                            @Override
                            public void run() {
                                MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                                if (mti != null) {
                                    mti.tokenHierarchyControl().rebuild();
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
