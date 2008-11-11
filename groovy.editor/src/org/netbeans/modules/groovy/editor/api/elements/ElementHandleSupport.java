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

package org.netbeans.modules.groovy.editor.api.elements;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.ParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class ElementHandleSupport {

    public static ElementHandle createHandle(CompilationInfo info, final GroovyElement object) {
        if (object instanceof KeywordElement || object instanceof CommentElement) {
            // Not tied to an AST - just pass it around
            return new GroovyElementHandle(null, object, info.getFileObject());
        }

        if (object instanceof IndexedElement) {
            // Probably a function in a "foreign" file (not parsed from AST),
            // such as a signature returned from the index of the Groovy libraries.
            // TODO - make sure this is infrequent! getFileObject is expensive!            
            // Alternatively, do this in a delayed fashion - e.g. pass in null and in getFileObject
            // look up from index            
            return new GroovyElementHandle(null, object, ((IndexedElement)object).getFileObject());
        }

        if (!(object instanceof AstElement)) {
            return null;
        }

        // XXX Gotta fix this
        if (info == null) {
            return null;
        }
        
        ParserResult result = AstUtilities.getParseResult(info);

        if (result == null) {
            return null;
        }

        ASTNode root = AstUtilities.getRoot(info);

        return new GroovyElementHandle(root, object, info.getFileObject());
    }

    @SuppressWarnings("unchecked")
    public static ElementHandle createHandle(ParserResult result, final AstElement object) {
        ASTNode root = AstUtilities.getRoot(result);

        return new GroovyElementHandle(root, object, result.getFile().getFileObject());
    }
    
    public static GroovyElement resolveHandle(CompilationInfo info, ElementHandle handle) {
        GroovyElementHandle h = (GroovyElementHandle)handle;
        ASTNode oldRoot = h.root;
        ASTNode oldNode;

        if (h.object instanceof KeywordElement || h.object instanceof IndexedElement || h.object instanceof CommentElement) {
            // Not tied to a tree
            return h.object;
        }

        if (h.object instanceof AstElement) {
            oldNode = ((AstElement)h.object).getNode(); // XXX Make it work for DefaultComObjects...
        } else {
            return null;
        }

        ASTNode newRoot = AstUtilities.getRoot(info);
        if (newRoot == null) {
            return null;
        }

        // Find newNode
        ASTNode newNode = find(oldRoot, oldNode, newRoot);

        if (newNode != null) {
            GroovyElement co = AstElement.create(newNode);

            return co;
        }

        return null;
    }

    private static ASTNode find(ASTNode oldRoot, ASTNode oldObject, ASTNode newRoot) {
        // Walk down the tree to locate oldObject, and in the process, pick the same child for newRoot
        @SuppressWarnings("unchecked")
        List<?extends ASTNode> oldChildren = AstUtilities.children(oldRoot);
        @SuppressWarnings("unchecked")
        List<?extends ASTNode> newChildren = AstUtilities.children(newRoot);
        Iterator<?extends ASTNode> itOld = oldChildren.iterator();
        Iterator<?extends ASTNode> itNew = newChildren.iterator();

        while (itOld.hasNext()) {
            if (!itNew.hasNext()) {
                return null; // No match - the trees have changed structure
            }

            ASTNode o = itOld.next();
            ASTNode n = itNew.next();

            if (o == oldObject) {
                // Found it!
                return n;
            }

            // Recurse
            ASTNode match = find(o, oldObject, n);

            if (match != null) {
                return match;
            }
        }

        if (itNew.hasNext()) {
            return null; // No match - the trees have changed structure
        }

        return null;
    }

    private static class GroovyElementHandle implements ElementHandle {
        private final ASTNode root;
        private final GroovyElement object;
        private final FileObject fileObject;

        private GroovyElementHandle(ASTNode root, GroovyElement object, FileObject fileObject) {
            this.root = root;
            this.object = object;
            this.fileObject = fileObject;
        }

        public boolean signatureEquals(ElementHandle handle) {
            // XXX TODO
            return false;
        }

        public FileObject getFileObject() {
            if (object instanceof IndexedElement) {
                return ((IndexedElement)object).getFileObject();
            }

            return fileObject;
        }

        public String getMimeType() {
            return GroovyTokenId.GROOVY_MIME_TYPE;
        }

        public String getName() {
            return object.getName();
        }

        public String getIn() {
            return object.getIn();
        }

        public ElementKind getKind() {
            return object.getKind();
        }

        public Set<Modifier> getModifiers() {
            return object.getModifiers();
        }
    }

}
