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

package org.netbeans.modules.javascript.editing;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.Document;
import org.mozilla.javascript.Node;
import org.netbeans.editor.BaseDocument;
import org.netbeans.fpi.gsf.CompilationInfo;
import org.netbeans.fpi.gsf.DeclarationFinder.DeclarationLocation;
import org.netbeans.fpi.gsf.ElementHandle;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class ElementUtilities {

    public static Element getElement(CompilationInfo info, ElementHandle handle) {
        Element element = null;
        if (handle instanceof ElementHandle.UrlHandle) {
            String url = ((ElementHandle.UrlHandle)handle).getUrl();
            DeclarationLocation loc = new JsDeclarationFinder().findLinkedMethod(info, url);
            if (loc != DeclarationLocation.NONE) {
                //element = loc.getElement();
                ElementHandle h = loc.getElement();
                if (handle != null) {
                    element = JsParser.resolveHandle(info, h);
                }
            }
        } else {
            element = JsParser.resolveHandle(info, handle);
        }
        return element;
    }
    
    /**
     * @todo If you invoke this on top of a symbol, I should really just show
     *   the documentation for that symbol!
     * 
     * @param element The element we want to look up comments for
     * @param info The (optional) compilation info for a document referencing the element.
     *   This is used to consult require-statements in the given compilation context etc.
     *   to choose among many alternatives. May be null, in which case the element had
     *   better be an IndexedElement.
     */
    public static List<String> getComments(CompilationInfo info, Element element) {
        assert info != null || element instanceof IndexedFunction;
        
        if (element == null) {
            return null;
        }
        
        Node node = getNode(info, element);

        if (node == null && element instanceof IndexedFunction) {
            // this was called in code moved to getNode
            List<String> comments = ((IndexedFunction)element).getComments();
            return comments;
        }

        // Initially, I implemented this by using JsParserResult.getCommentNodes.
        // However, I -still- had to rely on looking in the Document itself, since
        // the CommentNodes are not attached to the AST, and to do things the way
        // RDoc does, I have to (for example) look to see if a comment is at the
        // beginning of a line or on the same line as something else, or if two
        // comments have any empty lines between them, and so on.
        // When I started looking in the document itself, I realized I might as well
        // do all the manipulation on the document, since having the Comment nodes
        // don't particularly help.
        BaseDocument baseDoc = getBaseDocument(info, element);

        List<String> comments = null;

        // Check for JsComObject: These are external files (like Js lib) where I need to check many files
//        if (node instanceof ClassNode && !(element instanceof IndexedElement)) {
//            String className = AstUtilities.getClassOrModuleName((ClassNode)node);
//            List<ClassNode> classes = AstUtilities.getClasses(AstUtilities.getRoot(info));
//
//            // Iterate backwards through the list because the most recent documentation
//            // should be chosen, if any
//            for (int i = classes.size() - 1; i >= 0; i--) {
//                ClassNode clz = classes.get(i);
//                String name = AstUtilities.getClassOrModuleName(clz);
//
//                if (name.equals(className)) {
//                    comments = AstUtilities.gatherDocumentation(info, baseDoc, clz);
//
//                    if ((comments != null) && (comments.size() > 0)) {
//                        break;
//                    }
//                }
//            }
//        } else {
            comments = LexUtilities.gatherDocumentation(info, baseDoc, node.getSourceStart());
//        }

        if ((comments == null) || (comments.size() == 0)) {
            return null;
        }
        
        return comments;
    }
    
    public static String getSignature(Element element) {
        StringBuilder sb = new StringBuilder();
        IndexedFunction func = null;
        
        if (element instanceof FunctionElement) {
            // Insert browser icons... TODO - consult flags etc.
            sb.append("<table width=\"100%\" border=\"0\"><tr>\n");

            sb.append("<td>");

            FunctionElement executable = (FunctionElement) element;
            if (element.getIn() != null) {
                String in = element.getIn();
                sb.append("<i>");
                sb.append(in);
                sb.append("</i>");
                sb.append("<br>");
            }
            // TODO - share this between Navigator implementation and here...
            sb.append("<b>");
            sb.append(executable.getName());
            sb.append("</b>");

            Collection<String> parameters = executable.getParameters();

            if ((parameters != null) && (parameters.size() > 0)) {
                sb.append("(");

                sb.append("<font color=\"#808080\">");

                for (Iterator<String> it = parameters.iterator(); it.hasNext();) {
                    String ve = it.next();
                    // TODO - if I know types, list the type here instead. For now, just use the parameter name instead
                    sb.append(ve);

                    if (it.hasNext()) {
                        sb.append(", ");
                    }
                }

                sb.append("</font>");

                sb.append(")");
            }
//        } else if (element instanceof ClassElement) {
//            ClassElement clz = (ClassElement)element;
//            String name = element.getName();
//            final String fqn = clz.getFqn();
//            if (fqn != null && !name.equals(fqn)) {
//                sb.append("<i>");
//                sb.append(fqn);
//                sb.append("</i>");
//                sb.append("<br>");
//            }
//            sb.append("<b>");
//            sb.append(name);
//            sb.append("</b>");
            
            sb.append("</td>\n");
            sb.append("<td width=\"100\">");
            if (executable instanceof IndexedFunction) {
                func = (IndexedFunction)executable;
                EnumSet<BrowserVersion> es = func.getCompatibility();
                try {
                    if (es.contains(BrowserVersion.FF3)) {
                        appendImage(sb, "firefox20.png");
                    } else {
                        appendImage(sb, "firefox20-disabled.png");
                    }
                    if (es.contains(BrowserVersion.IE7)) {
                        appendImage(sb, "ie20.png");
                    } else {
                        appendImage(sb, "ie20-disabled.png");
                    }
                    if (es.contains(BrowserVersion.SAFARI3)) {
                        appendImage(sb, "safari20.png");
                    } else {
                        appendImage(sb, "safari20-disabled.png");
                    }
                    if (es.contains(BrowserVersion.OPERA)) {
                        appendImage(sb, "opera20.png");
                    } else {
                        appendImage(sb, "opera20-disabled.png");
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            sb.append("</td>");
            sb.append("</tr></table>");
            
        } else {
            sb.append(element.getName());
        }

        if (func != null && func.getFilenameUrl() != null && func.getFilenameUrl().indexOf("jsstubs") == -1) {
            sb.append(NbBundle.getMessage(JsCodeCompletion.class, "FileLabel"));
            sb.append(" <tt>");
            String file = func.getFilenameUrl();
            int baseIndex = file.lastIndexOf('/');
            if (baseIndex != -1) {
                file = file.substring(baseIndex+1);
            }
            sb.append(file);
            sb.append("</tt><br>");
        }
        
        // Generate compatibility notes
        if (func != null && !SupportedBrowsers.getInstance().isSupported(func.getCompatibility())) {
            sb.append("<hr>");
            sb.append("<p style=\"background:#ffcccc\">");
            sb.append(NbBundle.getMessage(JsCodeCompletion.class, "NotSupportedBr"));
            sb.append("\n");
            sb.append("<ul>");
            for (BrowserVersion v : BrowserVersion.ALL) {
                if (SupportedBrowsers.getInstance().isSupported(v) && 
                        !func.getCompatibility().contains(v)) {
                    sb.append("<li>");
                    sb.append(v.getDisplayName());
                }
            }
            sb.append("</ul>\n");
            //sb.append("Click <a href=\"netbeans:choosebrowsers\">here</a> to choose targeted browsers.\n");
            sb.append(NbBundle.getMessage(JsCodeCompletion.class, "EditTargetedBr"));
            sb.append("\n");
            sb.append("</p>");
        }

        return sb.toString();
    }

    private static Node getNode(CompilationInfo info, Element element) {
        Node node = null;

        if (element instanceof AstElement) {
            node = ((AstElement)element).getNode();
        } else if (element instanceof IndexedFunction) {
            IndexedFunction indexedFunction = (IndexedFunction)element;
            IndexedFunction match = null;
            Node root = null;
            if (info != null) {
                root = AstUtilities.getRoot(info);
                match = findDocumentationEntry(root, indexedFunction);
            }

            if (match != null) {
                indexedFunction = match;
            }

            node = AstUtilities.getForeignNode(indexedFunction, null);
        } else {
            assert false : element;
        }
        return node;
    }
    
    private static BaseDocument getBaseDocument(CompilationInfo info, Element element) {
        Document doc = null;
        BaseDocument baseDoc = null;

        try {
            if (element instanceof IndexedFunction) {
                doc = ((IndexedFunction)element).getDocument();
                info = null;
            } else if (info != null) {
                doc = info.getDocument();
            }

            if (doc instanceof BaseDocument) {
                baseDoc = (BaseDocument)doc;
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return baseDoc;
    }
    
    /**
     * @todo is it possible to have multiple sources of documentation fo JavaScript?
     */
    private static IndexedFunction findDocumentationEntry(Node root, IndexedFunction obj) {
        return obj;
    }
    
    private static void appendImage(StringBuilder sb, String image) {
        sb.append("<img src=\"" + JsCodeCompletion.class.getResource("icons/" + image).toExternalForm() + "\">"); // NOI18N
    }
    
}
