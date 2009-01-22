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

package org.netbeans.modules.cnd.refactoring.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class CsmContext {
    private final CsmFile file;
    private final Document doc;
    private final FileObject fo;

    private final int startOffset;
    private final int endOffset;
    private List<CsmObject> path = null;
    private CsmClass enclosingClass = null;
    private CsmNamespaceDefinition enclosingNS = null;
    private CsmFunction enclosingFun = null;
    private CsmOffsetable objectUnderOffset = null;

    private CsmContext(CsmFile file, FileObject fo, Document doc, int startOffset, int endOffset) {
        this.file = file;
        this.fo = fo;
        this.doc = doc;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public static CsmContext create(final Document doc, int start, int end) {
        CsmFile csmFile = CsmUtilities.getCsmFile(doc, false);
        if (csmFile != null) {
            return new CsmContext(csmFile, CsmUtilities.getFileObject(doc), doc, start, end);
        }
        return null;
    }

    public static CsmContext create(final Lookup context) {
        JTextComponent component = context.lookup(JTextComponent.class);
        if (component == null) {
            EditorCookie ec = context.lookup(EditorCookie.class);
            JEditorPane[] openedPanes = (ec == null) ? null : CsmUtilities.getOpenedPanesInEQ(ec);
            if (openedPanes != null && openedPanes.length > 0) {
                component = openedPanes[0];
            }
        }
        if (component != null) {
            CsmFile csmFile = CsmUtilities.getCsmFile(component, true);
            if (csmFile != null) {
                final int start = component.getSelectionStart();
                final int end = component.getSelectionEnd();
                final Document compDoc = component.getDocument();
                final FileObject compFO = CsmUtilities.getFileObject(compDoc);
                return new CsmContext(csmFile, compFO, compDoc, start, end);
            }
        }
        return null;
    }
    
    public FileObject getFileObject() {
        return fo;
    }

    public Document getDocument() {
        return doc;
    }

    public CsmFile getFile() {
        return file;
    }

    public List<CsmObject> getPath() {
        initPath();
        return path;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public String toString() {
        return "context: [" + file + ":" + startOffset + ", " + endOffset + "]"; // NOI18N
    }

    public CsmClass getEnclosingClass() {
        initPath();
        return enclosingClass;
    }

    public CsmFunction getEnclosingFunction() {
        initPath();
        return enclosingFun;
    }

    public CsmNamespaceDefinition getEnclosingNamespace() {
        initPath();
        return enclosingNS;
    }

    public CsmOffsetable getObjectUnderOffset() {
        initPath();
        return objectUnderOffset;
    }

    private Iterator<? extends CsmObject> getInnerObjectsIterator(CsmSelect select, CsmFilter offsetFilter, CsmScope scope) {
        Iterator<? extends CsmObject> out = Collections.<CsmObject>emptyList().iterator();
        if (CsmKindUtilities.isFile(scope)) {
            out = select.getDeclarations((CsmFile)scope, offsetFilter);
        } else if (CsmKindUtilities.isNamespaceDefinition(scope)) {
            out = select.getDeclarations(((CsmNamespaceDefinition)scope), offsetFilter);
        } else if (CsmKindUtilities.isClass(scope)) {
            out = select.getClassMembers(((CsmClass)scope), offsetFilter);
        } else {
            out = scope.getScopeElements().iterator();
        }
        return out;
    }
    
    private synchronized void initPath() {
        if (path != null) {
            return;
        }
        path = new ArrayList<CsmObject>(5);
        path.add(file);
        CsmSelect select = CsmSelect.getDefault();
        CsmFilter offsetFilter = select.getFilterBuilder().createOffsetFilter(startOffset);
        Iterator<? extends CsmObject> fileElements = getInnerObjectsIterator(select, offsetFilter, file);
        CsmObject innerDecl = fileElements.hasNext() ? fileElements.next() : null;
        if (innerDecl != null) {
            path.add(innerDecl);
            rememberObject(innerDecl);
            if (CsmKindUtilities.isScope(innerDecl)) {
                CsmScope curScope = (CsmScope)innerDecl;
                boolean cont;
                do {
                    cont = false;
                    final Iterator<? extends CsmObject> innerObjects = getInnerObjectsIterator(select, offsetFilter, curScope);
                    while (innerObjects.hasNext()) {
                        CsmObject csmScopeElement = innerObjects.next();
                        if (CsmKindUtilities.isOffsetable(csmScopeElement)) {
                            CsmOffsetable elem = (CsmOffsetable) csmScopeElement;
                            // stop if element starts after offset
                            if (this.startOffset < elem.getStartOffset()) {
                                break;
                            } else if (this.startOffset < elem.getEndOffset()) {
                                // offset is in element
                                cont = true;
                                path.add(elem);
                                rememberObject(elem);
                                if (CsmKindUtilities.isScope(elem)) {
                                    // deep diving
                                    curScope = (CsmScope)elem;
                                    break;
                                } else {
                                    objectUnderOffset = elem;
                                    cont = false;
                                }
                            }
                        }
                    }
                } while (cont);
            }
        }
    }

    private void rememberObject(CsmObject obj) {
        if (CsmKindUtilities.isNamespaceDefinition(obj)) {
            enclosingNS = (CsmNamespaceDefinition) obj;
        } else if (CsmKindUtilities.isClass(obj)) {
            enclosingClass = (CsmClass)obj;
        } else if (CsmKindUtilities.isFunction(obj)) {
            enclosingFun = (CsmFunction) obj;
        }
    }
}
