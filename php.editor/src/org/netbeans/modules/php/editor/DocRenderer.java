/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.php.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.index.PHPDOCTagElement;
import org.netbeans.modules.php.editor.index.PredefinedSymbolElement;
import org.netbeans.modules.php.editor.parser.annotation.LinkParsedLine;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
@NbBundle.Messages("PHPDocNotFound=PHPDoc not found")
class DocRenderer {

    private static final String TD_STYLE = "style=\"text-aling:left; border-width: 0px;padding: 1px;padding:3px;\" ";  //NOI18N
    private static final String TD_STYLE_MAX_WIDTH = "style=\"text-aling:left; border-width: 0px;padding: 1px;padding:3px;width:80%;\" ";  //NOI18N
    private static final String TABLE_STYLE = "style=\"border: 0px; width: 100%;\""; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(PHPCodeCompletion.class.getName());

    static String document(ParserResult info, ElementHandle element) {
        if (element instanceof PHPDOCTagElement) {
            PHPDOCTagElement pHPDOCTagElement = (PHPDOCTagElement) element;
            return pHPDOCTagElement.getDoc();
        }

        if (element instanceof PredefinedSymbolElement) {
            PredefinedSymbolElement predefinedSymbolElement = (PredefinedSymbolElement) element;
            return predefinedSymbolElement.getDoc();
        }

        if (element instanceof PhpElement) {
            return documentIndexedElement((PhpElement) element);
        }

        if (element instanceof TypeMemberElement) {
            TypeMemberElement indexedClassMember = (TypeMemberElement) element;
            return documentIndexedElement(indexedClassMember);
        }

        return null;
    }

    private static String documentIndexedElement(final PhpElement indexedElement) {
        final StringBuilder description = new StringBuilder();
        final CCDocHtmlFormatter locationHeader = new CCDocHtmlFormatter();
        CCDocHtmlFormatter header = new CCDocHtmlFormatter();
        final String location = getLocation(indexedElement);
        final StringBuilder phpDoc = new StringBuilder();
        final ElementQuery elementQuery = indexedElement.getElementQuery();
        if (location != null) {
            locationHeader.appendHtml(String.format("<div align=\"right\"><font size=-1>%s</font></div>", location));  //NOI18N
        }
        if (canBeProcessed(indexedElement)) {
            if (getPhpDoc(indexedElement, header, phpDoc).length() == 0) {
                if (indexedElement instanceof MethodElement) {
                    ElementFilter forName = ElementFilter.forName(NameKind.exact(indexedElement.getName()));
                    ElementQuery.Index index = elementQuery.getQueryScope().isIndexScope() ? (Index) elementQuery
                            : ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(indexedElement.getFileObject()));
                    final Set<TypeElement> inheritedTypes = index.getInheritedTypes(((MethodElement) indexedElement).getType());
                    for (Iterator<TypeElement> typeIt = inheritedTypes.iterator(); phpDoc.length() == 0 && typeIt.hasNext();) {
                        final Set<MethodElement> inheritedMethods = forName.filter(index.getDeclaredMethods(typeIt.next()));
                        for (Iterator<MethodElement> methodIt = inheritedMethods.iterator(); phpDoc.length() == 0 && methodIt.hasNext();) {
                            header = new CCDocHtmlFormatter();
                            getPhpDoc(methodIt.next(), header, phpDoc);
                        }
                    }
                }
            }
        }
        if (phpDoc.length() > 0) {
            description.append(phpDoc);
        } else {
            description.append(Bundle.PHPDocNotFound());
        }
        return String.format("%s%s%s", locationHeader.getText(), header.getText(), description.toString());

    }

    private static boolean canBeProcessed(PhpElement indexedElement) {
        return indexedElement != null && indexedElement.getOffset() > -1 && indexedElement.getFileObject() != null;
    }

    private static StringBuilder getPhpDoc(final PhpElement indexedElement, final CCDocHtmlFormatter header, final StringBuilder phpDoc) {
        if (canBeProcessed(indexedElement)) {
            FileObject nextFo = indexedElement.getFileObject();
            try {
                Source source = Source.create(nextFo);
                if (source != null) {
                    ParserManager.parse(Collections.singleton(source), new PHPDocExtractor(header, phpDoc, indexedElement));
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return phpDoc;
    }

    @NbBundle.Messages("PHPPlatform=PHP Platform")
    private static String getLocation(PhpElement indexedElement) {
        String location = null;
        if (indexedElement.isPlatform()) {
            location = Bundle.PHPPlatform();
        } else {
            FileObject fobj = indexedElement.getFileObject();
            if (fobj != null) {
                Project project = FileOwnerQuery.getOwner(fobj);
                if (project != null) {
                    // find the appropriate source root
                    Sources sources = ProjectUtils.getSources(project);
                    // TODO the PHPSOURCE constatnt has to be published in the project api
                    for (SourceGroup group : sources.getSourceGroups("PHPSOURCE")) {
                        //NOI18N
                        String relativePath = FileUtil.getRelativePath(group.getRootFolder(), fobj);
                        if (relativePath != null) {
                            location = relativePath;
                            break;
                        }
                    }
                    if (location == null) {
                        // just to be sure, that the relative location was resolved
                        location = fobj.getPath();
                    }
                } else {
                    location = indexedElement.getFilenameUrl();
                }
            }
        }
        return location;
    }

    static final class PHPDocExtractor extends UserTask {
        // http://manual.phpdoc.org/HTMLSmartyConverter/HandS/phpDocumentor/tutorial_phpDocumentor.howto.pkg.html#basics.desc
        // + table (table, tr, th, td)

        private static final Pattern KEEP_TAGS_PATTERN
                = Pattern.compile("<(?!(/|b|code|br|i|kbd|li|ol|p|pre|samp|ul|var|table|tr|th|td)(\\b|\\s))", Pattern.CASE_INSENSITIVE); // NOI18N
        private static final Pattern REPLACE_NEWLINE_PATTERN = Pattern.compile("(\r?\n){2,}"); // NOI18N
        // #183594
        private static final Pattern LIST_PATTERN = Pattern.compile("(\r?\n)(?=([-+#o]\\s|\\d\\.?\\s))"); // NOI18N
        private static final ArrayList<String> LINK_TAGS = new ArrayList<>();

        static {
            LINK_TAGS.add("@link");
            LINK_TAGS.add("@see");
            LINK_TAGS.add("@use");
        }
        private CCDocHtmlFormatter header;
        private StringBuilder phpDoc;
        private PhpElement indexedElement;

        public PHPDocExtractor(CCDocHtmlFormatter header, StringBuilder phpDoc, PhpElement indexedElement) {
            this.header = header;
            this.phpDoc = phpDoc;
            this.indexedElement = indexedElement;
        }

        public void cancel() {
        }

        private void doFunctionDeclaration(FunctionDeclaration functionDeclaration) {
            String fname = CodeUtils.extractFunctionName(functionDeclaration);
            header.appendHtml("<font size=\"+1\">"); //NOI18N
            header.name(ElementKind.METHOD, true);
            header.appendText(fname);
            header.name(ElementKind.METHOD, false);
            header.appendHtml("</font>"); //NOI18N

            header.parameters(true);
            header.appendText("("); //NOI18N
            int paramCount = functionDeclaration.getFormalParameters().size();

            for (int i = 0; i < paramCount; i++) {
                FormalParameter param = functionDeclaration.getFormalParameters().get(i);
                if (param.getParameterType() != null) {
                    Identifier paramId = CodeUtils.extractUnqualifiedIdentifier(param.getParameterType());
                    if (paramId != null) {
                        header.type(true);
                        header.appendText(paramId.getName() + " "); //NOI18N
                        header.type(false);
                    }
                }

                header.appendText(CodeUtils.getParamDisplayName(param));

                if (param.getDefaultValue() != null) {
                    header.type(true);
                    header.appendText("=");

                    if (param.getDefaultValue() instanceof Scalar) {
                        Scalar scalar = (Scalar) param.getDefaultValue();
                        header.appendText(scalar.getStringValue());
                    }

                    header.type(false);
                }

                if (i + 1 < paramCount) {
                    header.appendText(", "); //NOI18N
                }
            }

            header.appendText(")");
            header.parameters(false);
        }

        private void extractPHPDoc(PHPDocMethodTag methodTag) {
            StringBuilder params = new StringBuilder();
            StringBuilder returnValue = new StringBuilder();
            String description = methodTag.getDocumentation();

            if (description != null && description.length() > 0) {
                description = processPhpDoc(description);
            }

            if (methodTag.getParameters() != null && methodTag.getParameters().size() > 0) {
                for (PHPDocVarTypeTag tag : methodTag.getParameters()) {
                    params.append(composeParameterLine(tag));
                }
            }

            returnValue.append(composeReturnValue(methodTag.getTypes(), null));

            phpDoc.append(composeFunctionDoc(description, params.toString(), returnValue.toString(), null, null));
        }

        private void extractPHPDoc(PHPDocBlock pHPDocBlock) {
            StringBuilder params = new StringBuilder();
            StringBuilder links = new StringBuilder();
            StringBuilder returnValue = new StringBuilder();
            StringBuilder others = new StringBuilder();

            for (PHPDocTag tag : pHPDocBlock.getTags()) {
                AnnotationParsedLine kind = tag.getKind();
                if (kind.equals(PHPDocTag.Type.PARAM)) {
                    params.append(composeParameterLine((PHPDocVarTypeTag) tag));
                } else if (kind.equals(PHPDocTag.Type.RETURN)) {
                    PHPDocTypeTag returnTag = (PHPDocTypeTag) tag;
                    returnValue.append(composeReturnValue(returnTag.getTypes(), returnTag.getDocumentation()));
                } else if (kind.equals(PHPDocTag.Type.VAR)) {
                    PHPDocTypeTag typeTag = (PHPDocTypeTag) tag;
                    String type = composeType(typeTag.getTypes());
                    others.append(processPhpDoc(String.format("<tr><th align=\"left\">Type:</th><td>%s</td></tr>", type))); //NOI18N
                } else if (kind.equals(PHPDocTag.Type.DEPRECATED)) {
                    String oline = String.format("<tr><th align=\"left\">%s</th><td>%s</td></tr>%n", //NOI18N
                            processPhpDoc(tag.getKind().getName()), processPhpDoc(tag.getDocumentation(), "")); //NOI18N
                    others.append(oline);
                } else if (kind instanceof LinkParsedLine) {
                    String line = String.format("<a href=\"%s\">%s</a><br>%n", kind.getDescription(), kind.getDescription()); //NOI18N
                    links.append(line);
                } else {
                    String oline = String.format("<tr><th align=\"left\">%s</th><td>%s</td></tr>%n", //NOI18N
                            processPhpDoc(tag.getKind().getName()), processPhpDoc(tag.getKind().getDescription(), "")); //NOI18N
                    others.append(oline);
                }
            }

            phpDoc.append(composeFunctionDoc(processDescription(
                    processPhpDoc(pHPDocBlock.getDescription(), "")), //NOI18N
                    params.toString(),
                    returnValue.toString(),
                    links.toString(),
                    others.toString()));
        }

        protected String processDescription(String text) {
            StringBuilder result = new StringBuilder();
            int lastIndex = 0;
            int index = text.indexOf('{', 0);
            while (index > -1 && text.length() > (index + 1)) {
                result.append(text.substring(lastIndex, index));
                lastIndex = index;
                char charAt = text.charAt(index + 2);
                if (charAt == 'l' || charAt == 's' || charAt == 'u') {
                    int endIndex = text.indexOf(' ', index);
                    if (endIndex > -1) {
                        String tag = text.substring(index + 1, endIndex).trim();
                        if (LINK_TAGS.contains(tag)) {
                            index = endIndex + 1;
                            endIndex = text.indexOf('}', index);
                            if (endIndex > -1) {
                                String link = text.substring(index, endIndex).trim();
                                result.append(String.format("<a href=\"%s\">%s</a>", link, link));
                                lastIndex = endIndex + 1;
                            }
                        }
                    }
                }

                index = text.indexOf('{', index + 1);
            }
            if (lastIndex > -1) {
                result.append(text.substring(lastIndex));
            }
            return result.toString();
        }

        @NbBundle.Messages({
            "Parameters=Parameters:",
            "ReturnValue=Returns:",
            "OnlineDocs=Online Documentation"
        })
        private String composeFunctionDoc(String description, String parameters, String returnValue, String links, String others) {
            StringBuilder value = new StringBuilder();

            value.append(description);
            value.append("<br />\n"); //NOI18N

            if (parameters.length() > 0) {
                value.append("<h3>"); //NOI18N
                value.append(Bundle.Parameters());
                value.append("</h3>\n<table cellspacing=0 " + TABLE_STYLE + ">\n").append(parameters).append("</table>\n"); //NOI18N
            }

            if (returnValue.length() > 0) {
                value.append("<h3>"); //NOI18N
                value.append(Bundle.ReturnValue());
                value.append("</h3>\n<table>\n"); //NOI18N
                value.append(returnValue);
                value.append("</table>");
            }

            if (links != null && links.length() > 0) {
                value.append("<h3>"); //NOI18N
                value.append(Bundle.OnlineDocs());
                value.append("</h3>\n").append(links); //NOI18N
            }

            if (others != null && others.length() > 0) {
                value.append("<table>\n").append(others).append("</table>\n"); //NOI18N
            }
            return value.toString();
        }

        private String composeParameterLine(PHPDocVarTypeTag param) {
            String type = composeType(param.getTypes());
            String pline = String.format("<tr><td>&nbsp;</td><td valign=\"top\" %s><nobr>%s</nobr></td><td valign=\"top\" %s><nobr><b>%s</b></nobr></td><td valign=\"top\" %s>%s</td></tr>%n", //NOI18N
                    TD_STYLE, type, TD_STYLE, param.getVariable().getValue(), TD_STYLE_MAX_WIDTH, param.getDocumentation() == null ? "&nbsp" : processPhpDoc(param.getDocumentation()));
            return pline;
        }

        @NbBundle.Messages({
            "Type=Type",
            "Description=Description"
        })
        private String composeReturnValue(List<PHPDocTypeNode> types, String documentation) {
            StringBuilder returnValue = new StringBuilder();
            if (types != null && types.size() > 0) {
                returnValue.append(String.format("<tr><td>&nbsp;</td><td><b>%s:</b></td><td>%s</td></tr>", //NOI18N
                        Bundle.Type(), composeType(types)));
            }

            if (documentation != null && documentation.length() > 0) {
                returnValue.append(String.format("<tr><td>&nbsp;</td><td valign=\"top\"><b>%s:</b></td><td>%s</td></tr>", //NOI18N
                        Bundle.Description(), processPhpDoc(documentation)));
            }
            return returnValue.toString();
        }

        /**
         * Create a string from the list of types;
         *
         * @param tag
         * @return
         */
        private String composeType(List<PHPDocTypeNode> types) {
            StringBuilder type = new StringBuilder();
            if (types != null) {
                for (PHPDocTypeNode typeNode : types) {
                    if (type.length() > 0) {
                        type.append(" | "); //NOI18N
                    }
                    type.append(typeNode.getValue());
                    if (typeNode.isArray()) {
                        type.append("[]"); //NOI18N
                    }
                }
            }
            return type.toString();
        }

        // because of unit tests
        static String processPhpDoc(String phpDoc) {
            return processPhpDoc(phpDoc, Bundle.PHPDocNotFound());
        }

        static String processPhpDoc(String phpDoc, String defaultText) {
            String result = defaultText;
            if (StringUtils.hasText(phpDoc)) {
                String notags = KEEP_TAGS_PATTERN.matcher(phpDoc).replaceAll("&lt;"); // NOI18N
                notags = REPLACE_NEWLINE_PATTERN.matcher(notags).replaceAll("<br><br>"); // NOI18N
                result = LIST_PATTERN.matcher(notags).replaceAll("<br>&nbsp;&nbsp;&nbsp;&nbsp;"); // NOI18N
            }
            return result;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            ParserResult presult = (ParserResult) resultIterator.getParserResult();
            if (presult != null) {
                Program program = Utils.getRoot(presult);

                if (program != null) {
                    ASTNode node = Utils.getNodeAtOffset(program, indexedElement.getOffset());

                    if (node == null) { // issue #118222
                        LOGGER.log(
                                Level.WARNING,
                                "Could not find AST node for element {0} defined in {1}",
                                new Object[]{indexedElement.getName(), indexedElement.getFilenameUrl()});
                        return;
                    }
                    if (node instanceof FunctionDeclaration) {
                        doFunctionDeclaration((FunctionDeclaration) node);
                    } else {
                        header.name(indexedElement.getKind(), true);
                        header.appendText(indexedElement.getName());
                        header.name(indexedElement.getKind(), false);
                        String value = null;
                        if (indexedElement instanceof ConstantElement) {
                            ConstantElement constant = (ConstantElement) indexedElement;
                            value = constant.getValue();
                        } else if (indexedElement instanceof TypeConstantElement) {
                            TypeConstantElement constant = (TypeConstantElement) indexedElement;
                            value = constant.getValue();
                        }
                        if (value != null) {
                            header.appendText(" = "); //NOI18N
                            header.appendText(value);
                        }
                    }

                    header.appendHtml("<br/><br/>"); //NOI18N
                    if (node instanceof PHPDocTag) {
                        if (node instanceof PHPDocMethodTag) {
                            extractPHPDoc((PHPDocMethodTag) node);
                        } else {
                            if (node instanceof PHPDocVarTypeTag) {
                                PHPDocVarTypeTag varTypeTag = (PHPDocVarTypeTag) node;
                                String type = composeType(varTypeTag.getTypes());
                                phpDoc.append(processPhpDoc(String.format("%s<br /><table><tr><th align=\"left\">Type:</th><td>%s</td></tr></table>", varTypeTag.getDocumentation(), type))); //NOI18N
                            } else {
                                phpDoc.append(processPhpDoc(((PHPDocTag) node).getDocumentation()));
                            }
                        }
                    } else {
                        Comment comment = Utils.getCommentForNode(program, node);

                        if (comment instanceof PHPDocBlock) {
                            extractPHPDoc((PHPDocBlock) comment);
                        }
                    }
                }
            }
        }
    }
}
