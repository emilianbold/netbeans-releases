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
package org.netbeans.modules.groovy.editor;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.util.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Completable;
import org.netbeans.modules.gsf.api.Completable.QueryType;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.Element;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.modules.groovy.editor.elements.KeywordElement;
import org.netbeans.modules.groovy.editor.parser.GroovyParser;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.editor.elements.AstMethodElement;
import org.netbeans.modules.groovy.editor.lexer.LexUtilities;

public class CodeCompleter implements Completable {
 
    private static ImageIcon keywordIcon;
    boolean showSymbols = false;
    private boolean caseSensitive;
    private int anchor;
    private  final Logger LOG = Logger.getLogger(CodeCompleter.class.getName());
    
    public CodeCompleter() {
    
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                             : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }
    
    private boolean completeKeywords(List<CompletionProposal> proposals, CompletionRequest request, boolean isSymbol) {
        
        String prefix = request.prefix;
        
        // Keywords

        for (String keyword : GroovyUtils.GROOVY_KEYWORDS) {
            if (startsWith(keyword, prefix)) {
                KeywordItem item = new KeywordItem(keyword, null, anchor, request);

                if (isSymbol) {
                    item.setSymbol(true);
                }

                proposals.add(item);
            }
        }

        return false;
    }
    
    private boolean completeMethods(List<CompletionProposal> proposals, CompletionRequest request, boolean isSymbol) {
        
        // figure out which class we are dealing with:
        ASTNode root = AstUtilities.getRoot(request.info);
        AstPath path = new AstPath(root ,request.astOffset, request.doc);
        ASTNode closest = path.leaf();
        
        LOG.log(Level.FINEST, "completeMethods(...), closest: " + closest.getText());
        LOG.log(Level.FINEST, "completeMethods(...), closest(toString): " + closest.toString());
        LOG.log(Level.FINEST, "completeMethods(...), closest(getClass): " + closest.getClass());
        
        Class clz = null;
        ClassNode declClass = null;
        
        if (closest instanceof AnnotatedNode) {
            declClass = ((AnnotatedNode) closest).getDeclaringClass();
        } else if (closest instanceof Expression) {
            declClass = ((Expression) closest).getType();
        } else {
            return false;
        }
            
        if (declClass != null) {
            try {
                LOG.log(Level.FINEST, "completeMethods(...), closest(Name): " + declClass.getName());
                clz = Class.forName(declClass.getName());

            } catch (Exception e) {
            }

        }

      
        if (clz != null) {
            MetaClass metaClz = GroovySystem.getMetaClassRegistry().getMetaClass(clz);

            if (metaClz != null) {
                for (Object method : metaClz.getMetaMethods()) {
                    if (method != null) {
                        LOG.log(Level.FINEST, "completeMethods(...), MetaMethods: " + ((MetaMethod) method).toString());

                    }

                }

                for (Object method : metaClz.getMethods()) {
                    if (method != null) {
                        MetaMethod mm = (MetaMethod) method;
                        LOG.log(Level.FINEST, "completeMethods(...), Methods: " + mm.toString());
                        MethodItem item = new MethodItem(mm.getName(), mm.toString(), anchor, request);
                        proposals.add(item);
                    }
                }
            }

        }





            // FIXME: we need a decent stragegy to find enclosing class
//        TokenSequence<?> ts = request.th.tokenSequence();
//
//        if (ts != null) {
//            Token tok = LexUtilities.getToken(request.doc, request.lexOffset);
//            ts.movePrevious();
//            Token t = ts.token();
//
//            if (t != null) {
//                LOG.log(Level.FINEST, "completeMethods(...), prev. token: " + t.toString());
//
//            }
//
//
//        }
        
        return true;
    }
    

    public List<CompletionProposal> complete(CompilationInfo info, int lexOffset, String prefix, NameKind kind, QueryType queryType, boolean caseSensitive, HtmlFormatter formatter) {
        this.caseSensitive = caseSensitive;

        final int astOffset = AstUtilities.getAstOffset(info, lexOffset);
        
        // LOG.setLevel(Level.FINEST);
        LOG.log(Level.FINEST, "complete(...), prefix: " + prefix);
        
        
        // Avoid all those annoying null checks
        if (prefix == null) {
            prefix = "";
        }

        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

        anchor = lexOffset - prefix.length();

//        final RubyIndex index = RubyIndex.get(info.getIndex());

        final Document document;
        try {
            document = info.getDocument();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return null;
        }

        // TODO - move to LexUtilities now that this applies to the lexing offset?
//        lexOffset = AstUtilities.boundCaretOffset(info, lexOffset);

        // Discover whether we're in a require statement, and if so, use special completion
        final TokenHierarchy<Document> th = TokenHierarchy.get(document);
        final BaseDocument doc = (BaseDocument)document;
        final FileObject fileObject = info.getFileObject();
        
        boolean showLower = true;
        boolean showUpper = true;
        boolean showSymbols = false;
        char first = 0;

        doc.readLock(); // Read-lock due to Token hierarchy use
        
        try {        
            // Carry completion context around since this logic is split across lots of methods
            // and I don't want to pass dozens of parameters from method to method; just pass
            // a request context with supporting info needed by the various completion helpers i
            CompletionRequest request = new CompletionRequest();
            request.formatter = formatter;
            request.lexOffset = lexOffset;
            request.astOffset = astOffset;
//            request.index = index;
            request.doc = doc;
            request.info = info;
            request.prefix = prefix;
            request.th = th;
            request.kind = kind;
            request.queryType = queryType;
            request.fileObject = fileObject;

            // This is a bit stupid at the moment, not looking at the current typing context etc.
            ASTNode root = AstUtilities.getRoot(info);

            // No - we don't complete keywords, since one can get'em by hitting
            // ctrl-k or use an abbrevation. Displaying them without a documentation
            // makes no sense as well, see:
            // http://www.netbeans.org/issues/show_bug.cgi?id=126500
            // completeKeywords(proposals, request, showSymbols);
            
            // complte methods
            // completeMethods(proposals, request, showSymbols);
            
            return proposals;
        } finally {
            doc.readUnlock();
        }
        //return proposals;
    }

    public String document(CompilationInfo info, ElementHandle element) {
        return "";
    }

    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPrefix(CompilationInfo info, int caretOffset, boolean upToOffset) {
        return null;
    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        return QueryType.NONE;
    }

    public String resolveTemplateVariable(String variable, CompilationInfo info, int caretOffset, String name, Map parameters) {
        return "";
    }

    public Set<String> getApplicableTemplates(CompilationInfo info, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    public ParameterInfo parameters(CompilationInfo info, int caretOffset, CompletionProposal proposal) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private static class CompletionRequest {
        private TokenHierarchy<Document> th;
        private CompilationInfo info;
        private AstPath path;
        private Node node;
        private int lexOffset;
        private int astOffset;
        private BaseDocument doc;
        private String prefix = "";
        private NameKind kind;
        private QueryType queryType;
        private FileObject fileObject;
        private HtmlFormatter formatter;
    }

    private abstract class GroovyCompletionItem implements CompletionProposal {
        protected CompletionRequest request;
        protected Element element;
        protected int anchorOffset;
        protected boolean symbol;
        protected boolean smart;

        private GroovyCompletionItem(Element element, int anchorOffset, CompletionRequest request) {
            this.element = element;
            this.anchorOffset = anchorOffset;
            this.request = request;
        }

        public int getAnchorOffset() {
            return anchorOffset;
        }

        public String getName() {
            return element.getName();
        }

        public void setSymbol(boolean symbol) {
            this.symbol = symbol;
        }

        public String getInsertPrefix() {
            if (symbol) {
                return ":" + getName();
            } else {
                return getName();
            }
        }

        public String getSortText() {
            return getName();
        }

        public ElementHandle getElement() {
            return GroovyParser.createHandle(request.info, element);
        }

        public ElementKind getKind() {
            return element.getKind();
        }

        public ImageIcon getIcon() {
            return null;
        }

        public String getLhsHtml() {
            ElementKind kind = getKind();
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            formatter.name(kind, true);
            formatter.appendText(getName());
            formatter.name(kind, false);

            return formatter.getText();
        }

        public String getRhsHtml() {
            return null;
        }

        public Set<Modifier> getModifiers() {
            return element.getModifiers();
        }

        @Override
        public String toString() {
            String cls = getClass().getName();
            cls = cls.substring(cls.lastIndexOf('.') + 1);

            return cls + "(" + getKind() + "): " + getName();
        }

        void setSmart(boolean smart) {
            this.smart = smart;
        }

        public boolean isSmart() {
            return smart;
        }

        public List<String> getInsertParams() {
            return null;
        }
        
        public String[] getParamListDelimiters() {
            return new String[] { "(", ")" }; // NOI18N
        }

        public String getCustomInsertTemplate() {
            return null;
        }
    }

    private class MethodItem extends GroovyCompletionItem {
        private static final String GROOVY_METHOD = "org/netbeans/modules/groovy/editor/resources/groovydoc.png"; //NOI18N
        private final String name;
        private final String description;

        MethodItem(String name, String description, int anchorOffset, CompletionRequest request) {
            super(null, anchorOffset, request);
            this.name = name;
            this.description = description;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getRhsHtml() {
            if (description != null) {
                HtmlFormatter formatter = request.formatter;
                formatter.reset();
                //formatter.appendText(description);
                formatter.appendHtml(description);

                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public ImageIcon getIcon() {
            if (keywordIcon == null) {
                keywordIcon = new ImageIcon(org.openide.util.Utilities.loadImage(GROOVY_METHOD));
            }

            return keywordIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }
        
//        @Override
//        public ElementHandle getElement() {
//            // For completion documentation
//            return GroovyParser.createHandle(request.info, new AstMethodElement());
//        }
    }
    
    private class KeywordItem extends GroovyCompletionItem {
        private static final String GROOVY_KEYWORD = "org/netbeans/modules/groovy/editor/resources/groovydoc.png"; //NOI18N
        private final String keyword;
        private final String description;

        KeywordItem(String keyword, String description, int anchorOffset, CompletionRequest request) {
            super(null, anchorOffset, request);
            this.keyword = keyword;
            this.description = description;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml() {
            if (description != null) {
                HtmlFormatter formatter = request.formatter;
                formatter.reset();
                //formatter.appendText(description);
                formatter.appendHtml(description);

                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public ImageIcon getIcon() {
            if (keywordIcon == null) {
                keywordIcon = new ImageIcon(org.openide.util.Utilities.loadImage(GROOVY_KEYWORD));
            }

            return keywordIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }
        
        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return GroovyParser.createHandle(request.info, new KeywordElement(keyword));
        }
    }

}
