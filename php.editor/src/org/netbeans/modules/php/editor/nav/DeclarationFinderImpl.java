/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.nav;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.DeclarationFinder;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedElement;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedElement.Kind;
import org.netbeans.modules.php.editor.nav.SemiAttribute.ClassMemberElement;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Union2;

/**
 *
 * @author Jan Lahoda
 */
public class DeclarationFinderImpl implements DeclarationFinder {

    public DeclarationLocation findDeclaration(CompilationInfo info, int caretOffset) {
        return findDeclarationImpl(info, caretOffset);
    }

    public OffsetRange getReferenceSpan(Document doc, final int caretOffset) {
        List<TokenSequence<?>> ets = TokenHierarchy.get(doc).embeddedTokenSequences(caretOffset, false);

        ets = new LinkedList<TokenSequence<?>>(ets);

        Collections.reverse(ets);

        for (TokenSequence<?> ts : ets) {
            if (ts.language() == PHPTokenId.language()) {
                Token<?> t = ts.token();

                if (t.id() == PHPTokenId.PHP_VARIABLE || t.id() == PHPTokenId.PHP_STRING) {
                    return new OffsetRange(ts.offset(), ts.offset() + t.length());
                }
            }
        }

        //XXX: to find out includes, we need to parse - but this means we are parsing on mouse move in AWT!:
        FileObject file = NavUtils.getFile(doc);

        if (file != null) {
            SourceModel model = SourceModelFactory.getInstance().getModel(file);
            final OffsetRange[] result = new OffsetRange[1];

            try {
                model.runUserActionTask(new CancellableTask<CompilationInfo>() {
                    public void cancel() {}
                    public void run(CompilationInfo parameter) throws Exception {
                        List<ASTNode> path = NavUtils.underCaret(parameter, caretOffset);

                        if (path.size() == 0) {
                            return ;
                        }

                        path = new LinkedList<ASTNode>(path);

                        Collections.reverse(path);

                        Scalar where = null;
                        for (ASTNode n : path) {
                            if (n instanceof Include) {
                                FileObject file = NavUtils.resolveInclude(parameter, (Include) n);
                                if (file != null && where != null) {
                                    result[0] = new OffsetRange(where.getStartOffset()+1, where.getEndOffset()-1);
                                    break;
                                }
                            }
                            else if (n instanceof Scalar) {
                                where = (Scalar)n;
                            }
                        }
                    }
                }, true);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }

            if (result[0] != null) {
                return result[0];
            }
        }

        return OffsetRange.NONE;
    }

    static DeclarationLocation findDeclarationImpl(CompilationInfo info, final int offset) {
        List<ASTNode> path = NavUtils.underCaret(info, offset);
        SemiAttribute a = SemiAttribute.semiAttribute(info);//, offset);
        AttributedElement el = NavUtils.findElement(info, path, offset, a);

        if (el != null) {
            return create(info, el);
        }

        if (path.size() == 0) {
            return DeclarationLocation.NONE;
        }

        path = new LinkedList<ASTNode>(path);

        Collections.reverse(path);

        for (ASTNode n : path) {
            if (n instanceof Include) {
                FileObject file = NavUtils.resolveInclude(info, (Include) n);

                if (file != null) {
                    return new DeclarationLocation(file, 0);
                }

                break;
            }
        }

        return DeclarationLocation.NONE;
    }

    static DeclarationLocation create(CompilationInfo info, final AttributedElement el) {
        List<Union2<ASTNode, IndexedElement>> writes = el.getWrites();
        Union2<ASTNode, IndexedElement> n;

        switch (el.getKind()) {
            case FUNC:
            case CLASS:
            case VARIABLE:
                int startOffest = -1;
                n = writes.get(0);
                for (Union2<ASTNode, IndexedElement> union2 : writes) {
                    if (union2.hasFirst()) {
                        ASTNode tmp = union2.first();
                        if (tmp != null && (tmp.getStartOffset() < startOffest || startOffest == -1)) {
                            n = union2;
                            startOffest = tmp.getStartOffset();
                        }
                    }
                }
                
                break;
            default:
                n = writes.get(writes.size() - 1);
                break;
        }

        if ((n.hasFirst() && n.first() == null) || (n.hasSecond() && n.second() != null)) {
            //cannot resolve, offer all possibilities:
            Index i = info.getIndex(PHPLanguage.PHP_MIME_TYPE);
            PHPIndex index = PHPIndex.get(i);
            Collection<? extends IndexedElement> fromIndex;

            switch (el.getKind()) {
                case FUNC:
                    if (el.isClassMember()) {
                        SemiAttribute.ClassMemberElement memberElement = (ClassMemberElement) el;
                        fromIndex = index.getAllMethods(null, memberElement.getClassName(), memberElement.getName(), NameKind.PREFIX, PHPIndex.ANY_ATTR);
                    } else {
                        fromIndex = index.getFunctions(null, el.getName(), NameKind.PREFIX);
                    }
                    break;
                case IFACE:
                    fromIndex = index.getInterfaces(null, el.getName(), NameKind.PREFIX);
                    break;
                case CLASS:
                    fromIndex = index.getClasses(null, el.getName(), NameKind.PREFIX);
                    break;
                case VARIABLE:
                    if (el.isClassMember()) {
                        SemiAttribute.ClassMemberElement memberElement = (ClassMemberElement) el;
                        fromIndex = index.getAllProperties(null, memberElement.getClassName(), memberElement.getName(), NameKind.PREFIX, PHPIndex.ANY_ATTR);
                    } else if (n.hasSecond()) {
                        final IndexedElement indexed = n.second();
                        FileObject file = indexed.getFileObject();

                        if (file == null){
                            return DeclarationLocation.NONE;
                        }

                        return new DeclarationLocation(file, indexed.getOffset());
                    } else {
                        fromIndex = Collections.emptyList();
                    }
                    break;
                case CONST:
                    if (el.isClassMember()) {
                        SemiAttribute.ClassMemberElement memberElement = (ClassMemberElement) el;
                        fromIndex = index.getAllClassConstants(null, memberElement.getClassName(), memberElement.getName(), NameKind.PREFIX);
                    } else if (n.hasSecond()) {
                        final IndexedElement indexed = n.second();
                        FileObject file = indexed.getFileObject();
                        assert file != null;
                        return new DeclarationLocation(file, indexed.getOffset());
                    } else {
                        fromIndex = Collections.emptyList();
                    }
                    break;
                default:
                    fromIndex = Collections.emptyList();
            }

            List<AlternativeLocation> locations = new LinkedList<AlternativeLocation>();

            for (IndexedElement e : fromIndex) {
                String name = e.getName();
                if (el.getKind().equals(SemiAttribute.AttributedElement.Kind.VARIABLE) && name.startsWith("$")) {//NOI18N
                    name = name.substring(1);
                }
                if (el.getName().equals(name)) {
                    DeclarationLocation l = new DeclarationLocation(e.getFileObject(), e.getOffset());
                    locations.add(new AlternativeLocationImpl(e, el.getKind(), l));
                }
            }

            if (locations.isEmpty()) {
                //nothing found:
                return DeclarationLocation.NONE;
            }

            if (locations.size() == 1) {
                return locations.get(0).getLocation();
            }

            DeclarationLocation result = locations.get(0).getLocation();

            for (AlternativeLocation l : locations) {
                result.addAlternative(l);
            }

            return result;
        }

        return new DeclarationLocation(info.getFileObject(), n.first().getStartOffset());
    }

    private static final class AlternativeLocationImpl implements AlternativeLocation {

        private final IndexedElement el;
        private final Kind k;
        private final DeclarationLocation l;

        public AlternativeLocationImpl(IndexedElement el, Kind k, DeclarationLocation l) {
            this.el = el;
            this.k = k;
            this.l = l;
        }

        public ElementHandle getElement() {
            return el;
        }

        public String getDisplayHtml(HtmlFormatter formatter) {
            formatter.reset();
            ElementKind ek = null;
            switch (k) {
                case FUNC:
                    ek = ElementKind.METHOD;
                    break;
                case CLASS:
                    ek = ElementKind.CLASS;
            }

            if (ek != null) {
                formatter.name(ek, true);
                formatter.appendText(el.getName());
                formatter.name(ek, false);
            } else {
                formatter.appendText(el.getName());
            }

            if (l.getFileObject() != null) {
                formatter.appendText(" in ");
                formatter.appendText(FileUtil.getFileDisplayName(l.getFileObject()));
            }

            return formatter.getText();
        }

        public DeclarationLocation getLocation() {
            return l;
        }

        public int compareTo(AlternativeLocation o) {
            AlternativeLocationImpl i = (AlternativeLocationImpl) o;

            return this.el.getName().compareTo(i.el.getName());
        }

    }
}
