/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.completion.impl.xref;

import java.io.IOException;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.UserQuestionException;

/**
 * implementation of references resolver
 * @author Vladimir Voskresensky
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver.class)
public class ReferenceResolverImpl extends CsmReferenceResolver {
    
    public ReferenceResolverImpl() {
    }    

    public boolean isKindOf(CsmReference ref, Set<CsmReferenceKind> kinds) {
        return kinds.equals(CsmReferenceKind.ALL) || kinds.contains(ref.getKind());
    }
    
    public CsmReference findReference(CsmFile file, int offset) {
        assert file != null;
        BaseDocument doc = ReferencesSupport.getDocument(file);
        if (doc == null) {
            return null;
        }
        CsmReference ref = ReferencesSupport.createReferenceImpl(file, doc, offset);
        return ref;
    }
    
    public CsmReference findReference(CsmFile file, int line, int column) {
        assert file != null;
        BaseDocument doc = ReferencesSupport.getDocument(file);
        if (doc == null) {
            return null;
        }
        int offset = ReferencesSupport.getDocumentOffset(doc, line, column);
        CsmReference ref = ReferencesSupport.createReferenceImpl(file, doc, offset);
        return ref;
    }
    
    @Override
    public CsmReference findReference(Node activatedNode) {
        assert activatedNode != null : "activatedNode must be not null";
        EditorCookie cookie = activatedNode.getCookie(EditorCookie.class);
        if (cookie != null) {
            JEditorPane pane = CsmUtilities.findRecentEditorPaneInEQ(cookie);
            if (pane != null) {
                //System.err.printf("caret: %d, %d, %d\n",panes[0].getCaretPosition(), panes[0].getSelectionStart(), panes[0].getSelectionEnd());
                int offset = pane.getSelectionStart();
                StyledDocument doc = null;
                try {
                    try {
                        doc = cookie.openDocument();
                    } catch (UserQuestionException ex) {
                        ex.confirmed();
                        doc = cookie.openDocument();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
                return findReferenceInDoc(doc, offset);
            }
        }
        return null;
    }
    
    @Override
    public CsmReference findReference(Document doc, int offset) {
        return findReferenceInDoc(doc, offset);
    }

    private CsmReference findReferenceInDoc(Document doc, int offset) {
        if (doc instanceof BaseDocument) {
            CsmFile file = CsmUtilities.getCsmFile(doc, false, false);
            if (file != null) {
                return ReferencesSupport.createReferenceImpl(file, (BaseDocument) doc, offset);
            }
        }
        return null;
    }

    @Override
    public Scope fastCheckScope(CsmReference ref) {
        return ReferencesSupport.fastCheckScope(ref);
    }    
}
