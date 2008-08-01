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

package org.netbeans.editor.ext;

import java.lang.reflect.Method;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;


/**
 * Editor UI for the component. All the additional UI features
 * like advanced scrolling, info about fonts, abbreviations,
 * keyword matching are based on this class.
 *
 * @author Miloslav Metelka
 * @version 1.00
 * @deprecated Without any replacement.
 */
public class ExtEditorUI extends EditorUI {

    private Completion completion;
    
    private CompletionJavaDoc completionJavaDoc;
    
    private boolean noCompletion; // no completion available

    private boolean noCompletionJavaDoc; // no completion available
    
    public ExtEditorUI() {
        getCompletion();
        getCompletionJavaDoc();
    }

    public Completion getCompletion() {
        
        if (completion == null) {
            if (noCompletion) {
                return null;
            }

            synchronized (getComponentLock()) {
                JTextComponent component = getComponent();
                if (component != null) {
                    BaseKit kit = Utilities.getKit(component);
                    if (kit != null && kit instanceof ExtKit) {
                        try {
                            Method m = kit.getClass().getMethod("createCompletion", ExtEditorUI.class); //NOI18N
                            completion = (Completion) m.invoke(kit, this);
                        } catch (Exception e) {
                            completion = null;
                        }
                        if (completion == null) {
                            noCompletion = true;
                        }
                    }
                }
            }
        }

        return completion;
    }

    public CompletionJavaDoc getCompletionJavaDoc() {
        if (completionJavaDoc == null) {
            if (noCompletionJavaDoc) {
                return null;
            }

            synchronized (getComponentLock()) {
                JTextComponent component = getComponent();
                if (component != null) {
                    BaseKit kit = Utilities.getKit(component);
                    if (kit != null && kit instanceof ExtKit) {
                        try {
                            Method m = kit.getClass().getMethod("createCompletionJavaDoc", ExtEditorUI.class); //NOI18N
                            completionJavaDoc = (CompletionJavaDoc) m.invoke(kit, this);
                        } catch (Exception e) {
                            completionJavaDoc = null;
                        }
                        if (completionJavaDoc == null) {
                            noCompletionJavaDoc = true;
                        }
                    }
                }
            }
        }

        return completionJavaDoc;
    }
}
