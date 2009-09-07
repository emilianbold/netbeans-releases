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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.model.Occurence;
import org.netbeans.modules.php.editor.model.OccurencesSupport;
import org.netbeans.modules.php.editor.model.QualifiedName;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Radek Matous
 */
public class DeclarationFinderImpl implements DeclarationFinder {

    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        return findDeclarationImpl(info, caretOffset);
    }

    public OffsetRange getReferenceSpan(Document doc, final int caretOffset) {
        List<TokenSequence<?>> ets = TokenHierarchy.get(doc).embeddedTokenSequences(caretOffset, false);
        boolean inDocComment = false;
        ets = new LinkedList<TokenSequence<?>>(ets);

        Collections.reverse(ets);

        for (TokenSequence<?> ts : ets) {
            if (ts.language() == PHPTokenId.language()) {
                Token<?> t = ts.token();

                if (t.id() == PHPTokenId.PHP_VARIABLE) {
                    return new OffsetRange(ts.offset() + 1, ts.offset() + t.length());
                } else if (t.id() == PHPTokenId.PHP_STRING) {
                    return new OffsetRange(ts.offset(), ts.offset() + t.length());
                }

                if (t.id() == PHPTokenId.PHPDOC_COMMENT || t.id() == PHPTokenId.PHP_COMMENT) {
                    inDocComment = true;
                    break;
                }
            }
        }

        //XXX: to find out includes, we need to parse - but this means we are parsing on mouse move in AWT!:
        FileObject file = NavUtils.getFile(doc);
        final OffsetRange[] result = new OffsetRange[1];

        if (!inDocComment) {
            if (file != null) {
                try {
                    Future<Void> f = ParserManager.parseWhenScanFinished(Collections.singleton(Source.create(doc)), new UserTask() {

                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            ParserResult parameter = (ParserResult) resultIterator.getParserResult();
                            List<ASTNode> path = NavUtils.underCaret(parameter, caretOffset);
                            if (path.size() == 0) {
                                return;
                            }
                            path = new LinkedList<ASTNode>(path);
                            Collections.reverse(path);
                            Scalar where = null;
                            for (ASTNode n : path) {
                                if (n instanceof Include) {
                                    FileObject file = NavUtils.resolveInclude(parameter, (Include) n);
                                    if (file != null && where != null) {
                                        result[0] = new OffsetRange(where.getStartOffset() + 1, where.getEndOffset() - 1);
                                        break;
                                    }
                                } else if (n instanceof FunctionInvocation) {
                                    FunctionInvocation functionInvocation = (FunctionInvocation) n;
                                    String fncName = CodeUtils.extractFunctionName(functionInvocation);
                                    if (fncName != null && fncName.equals("constant")) {
                                        result[0] = new OffsetRange(where.getStartOffset() + 1, where.getEndOffset() - 1);
                                    }
                                } else if (n instanceof Scalar) {
                                    where = (Scalar) n;
                                }
                            }
                        }
                    });
                    try {
                        f.get(300, TimeUnit.MILLISECONDS);
                    } catch (Exception ex) {
                        Logger.getLogger(DeclarationFinderImpl.class.getName()).fine(ex.getLocalizedMessage());
                        if (!f.isDone()) {
                            f.cancel(true);
                        }
                    }

                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                try {
                    Future<Void> f = ParserManager.parseWhenScanFinished(Collections.singleton(Source.create(doc)), new UserTask() {

                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            ParserResult parameter = (ParserResult) resultIterator.getParserResult();
                            Program program = Utils.getRoot(parameter);
                            Comment comment = null;
                            for (Comment comm : program.getComments()) {
                                if (comm.getStartOffset() < caretOffset && caretOffset < comm.getEndOffset()) {
                                    comment = comm;
                                    break;
                                }
                                if (caretOffset < comm.getStartOffset()) {
                                    break;
                                }
                            }
                            if (comment != null && comment instanceof PHPDocBlock) {
                                PHPDocBlock docComment = (PHPDocBlock) comment;
                                ASTNode[] hierarchy = Utils.getNodeHierarchyAtOffset(docComment, caretOffset);
                                PHPDocNode node = null;
                                if (hierarchy.length > 0) {
                                    if (hierarchy[0] instanceof PHPDocTypeTag) {
                                        for (PHPDocNode type : ((PHPDocTypeTag) hierarchy[0]).getTypes()) {
                                            if (type.getStartOffset() < caretOffset && caretOffset < type.getEndOffset()) {
                                                node = type;
                                                break;
                                            }
                                        }
                                        if (node != null && !PHPDocTypeTag.ORDINAL_TYPES.contains(node.getValue().toUpperCase())) {
                                            result[0] = new OffsetRange(node.getStartOffset(), node.getEndOffset());
                                        }
                                    }
                                    if (hierarchy[0] instanceof PHPDocVarTypeTag) {
                                        node = ((PHPDocVarTypeTag) hierarchy[0]).getVariable();
                                        if (node != null && node.getStartOffset() < caretOffset && caretOffset < node.getEndOffset()) {
                                            result[0] = new OffsetRange(node.getStartOffset(), node.getEndOffset());
                                        }
                                    }
                                }
                            } else if (comment instanceof PHPVarComment) {
                                PHPVarComment varComment = (PHPVarComment) comment;
                                PHPDocVarTypeTag varTypeTag = varComment.getVariable();
                                List<ASTNode> nodes = new ArrayList<ASTNode>(varTypeTag.getTypes());
                                nodes.add(varTypeTag.getVariable());
                                for (ASTNode node : nodes) {
                                    if (node != null && node.getStartOffset() < caretOffset && caretOffset < node.getEndOffset()) {
                                        result[0] = new OffsetRange(node.getStartOffset(), node.getEndOffset());
                                        break;
                                    }
                                }
                            }
                        }
                    });
                    try {
                        f.get(300, TimeUnit.MILLISECONDS);
                    } catch (Exception ex) {
                        Logger.getLogger(DeclarationFinderImpl.class.getName()).fine(ex.getLocalizedMessage());
                        if (!f.isDone()) {
                            f.cancel(true);
                        }
                    }
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }


            }

            if (result[0] != null) {
                return result[0];
            }
        }

        return OffsetRange.NONE;
    }

    public static DeclarationLocation findDeclarationImpl(ParserResult info, int caretOffset) {
        DeclarationLocation retval = DeclarationLocation.NONE;
        OccurencesSupport occurencesSupport = ModelFactory.getModel(info).getOccurencesSupport(caretOffset);
        Occurence underCaret = occurencesSupport.getOccurence();
        if (underCaret != null) {
            ModelElement declaration = underCaret.gotoDeclaratin();
            FileObject declarationFo = declaration.getFileObject();
            if (declarationFo == null) {
                return DeclarationLocation.NONE;
            }
            retval = new DeclarationLocation(declarationFo, declaration.getOffset(),declaration.getPHPElement());
            //TODO: if there was 2 classes with the same method or field it jumps directly into one of them
            if (info.getSnapshot().getSource().getFileObject() == declaration.getFileObject()) {
                return retval;
            }
            Collection<? extends ModelElement> alternativeDeclarations = underCaret.getAllDeclarations();
            if (alternativeDeclarations.size() > 1) {
                retval = DeclarationLocation.NONE;
                for (ModelElement elem : alternativeDeclarations) {
                    FileObject elemFo = elem.getFileObject();
                    if (elemFo == null) {
                        continue;
                    }
                    DeclarationLocation declLocation = new DeclarationLocation(elemFo, elem.getOffset(), elem.getPHPElement());
                    AlternativeLocation al = new AlternativeLocationImpl(elem, declLocation);
                    if (retval == DeclarationLocation.NONE) {
                        retval = al.getLocation();
                    }
                    retval.addAlternative(al);
                }
                return retval;
            }
        }
        return retval;
    }

    private static class AlternativeLocationImpl implements AlternativeLocation {

        private ModelElement modelElement;
        private DeclarationLocation declaration;

        AlternativeLocationImpl(ModelElement modelElement, DeclarationLocation declaration) {
            this.modelElement = modelElement;
            this.declaration = declaration;
        }

        public ElementHandle getElement() {
            return modelElement.getPHPElement();
        }

        public String getDisplayHtml(HtmlFormatter formatter) {
            formatter.reset();
            ElementKind ek = modelElement.getPHPElement().getKind();

            if (ek != null) {
                formatter.name(ek, true);
                QualifiedName namespaceName = modelElement.getNamespaceName();
                if (namespaceName.isDefaultNamespace()) {
                    formatter.appendText(modelElement.getName());
                } else {
                    formatter.appendText(namespaceName.append(modelElement.getName()).toString());
                }
                formatter.name(ek, false);
            } else {
                formatter.appendText(modelElement.getName());
            }

            if (declaration.getFileObject() != null) {
                formatter.appendText(" in ");
                formatter.appendText(FileUtil.getFileDisplayName(declaration.getFileObject()));
            }

            return formatter.getText();
        }

        public DeclarationLocation getLocation() {
            return declaration;
        }

        public int compareTo(AlternativeLocation o) {
            AlternativeLocationImpl i = (AlternativeLocationImpl) o;
            return this.modelElement.getName().compareTo(i.modelElement.getName());
        }
    }
}
