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
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.CodeCompletionHandler;
import org.netbeans.modules.gsf.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.modules.groovy.editor.elements.KeywordElement;
import org.netbeans.modules.groovy.editor.parser.GroovyParser;
import org.openide.filesystems.FileObject;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.lang.model.element.TypeElement;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.reflection.CachedClass;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.groovy.editor.elements.AstMethodElement;
import org.netbeans.modules.groovy.editor.elements.GroovyElement;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.lexer.LexUtilities;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.netbeans.modules.gsf.api.CodeCompletionContext;
import org.netbeans.modules.gsf.api.CodeCompletionResult;
import org.netbeans.modules.gsf.spi.DefaultCompletionResult;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class CodeCompleter implements CodeCompletionHandler {

    private static ImageIcon classIcon;
    private boolean caseSensitive;
    private int anchor;
    private final Logger LOG = Logger.getLogger(CodeCompleter.class.getName());
    private String jdkJavaDocBase = null;
    private String groovyJavaDocBase = null;
    private String gapiDocBase = null;

    public CodeCompleter() {
        // LOG.setLevel(Level.FINEST);

        JavaPlatformManager platformMan = JavaPlatformManager.getDefault();
        JavaPlatform platform = platformMan.getDefaultPlatform();
        List<URL> docfolder = platform.getJavadocFolders();

        for (URL url : docfolder) {
            LOG.log(Level.FINEST, "JDK Doc path: {0}", url.toString()); // NOI18N
            jdkJavaDocBase = url.toString();
        }

        GroovySettings groovySettings = new GroovySettings();
        String docroot = groovySettings.getGroovyDoc() + "/";

        groovyJavaDocBase = directoryNameToUrl(docroot + "groovy-jdk/"); // NOI18N
        gapiDocBase = directoryNameToUrl(docroot + "gapi/"); // NOI18N

        LOG.log(Level.FINEST, "GDK Doc path: {0}", groovyJavaDocBase);
        LOG.log(Level.FINEST, "GAPI Doc path: {0}", gapiDocBase);
    }

    String directoryNameToUrl(String dirname) {
        if (dirname == null) {
            return "";
        }
        
        File dirFile = new File(dirname);

        if (dirFile != null && dirFile.exists() && dirFile.isDirectory()) {
            String fileURL = "";
            if (Utilities.isWindows()) {
                dirname = dirname.replace("\\", "/");
                fileURL = "file:/"; // NOI18N
            } else {
                fileURL = "file://"; // NOI18N
            }
            return fileURL + dirname;
        } else {
            return "";
        }
    }
    
    
    
    private void populateProposal(Class clz, Object method, CompletionRequest request, List<CompletionProposal> proposals, boolean isGDK) {
        if (method != null && (method instanceof MetaMethod)) {
            MetaMethod mm = (MetaMethod) method;

            if (!request.prefix.equals("")) {
                if (mm.getName().startsWith(request.prefix)) {
                    MethodItem item = new MethodItem(clz, mm, anchor, request, isGDK);
                    proposals.add(item);
                }
            } else {
                MethodItem item = new MethodItem(clz, mm, anchor, request, isGDK);
                proposals.add(item);
            }
        }
    }

    private void printASTNodeInformation(ASTNode node) {

        LOG.log(Level.FINEST, "--------------------------------------------------------");

        if (node == null) {
            LOG.log(Level.FINEST, "node == null");
        } else {
            LOG.log(Level.FINEST, "Node.getText()  : " + node.getText());
            LOG.log(Level.FINEST, "Node.toString() : " + node.toString());
            LOG.log(Level.FINEST, "Node.getClass() : " + node.getClass());
            
            if(node instanceof ModuleNode) {
                LOG.log(Level.FINEST, "ModuleNode.getClasses() : " + ((ModuleNode)node).getClasses());
                LOG.log(Level.FINEST, "SourceUnit.getName() : " + ((ModuleNode)node).getContext().getName());
            }
        }
    }

    private void printMethod(MetaMethod mm) {

        LOG.log(Level.FINEST, "--------------------------------------------------");
        LOG.log(Level.FINEST, "getName()           : " + mm.getName());
        LOG.log(Level.FINEST, "toString()          : " + mm.toString());
        LOG.log(Level.FINEST, "getDescriptor()     : " + mm.getDescriptor());
        LOG.log(Level.FINEST, "getSignature()      : " + mm.getSignature());
        LOG.log(Level.FINEST, "getParamTypes()     : " + mm.getParameterTypes());
        LOG.log(Level.FINEST, "getDeclaringClass() : " + mm.getDeclaringClass());
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
            : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    
    /**
     * Get the closest ASTNode related to this request. This is used to complete
     * Methods etc later on.
     * @param request
     * @return a valid ASTNode or null
     */
    ASTNode getClosestNode(CompletionRequest request) {
        
        AstPath path = getPathFromRequest(request);
        
        if (path == null) {
            LOG.log(Level.FINEST, "path == null"); // NOI18N
            return null;
        }
        
        ASTNode closest = null;

        if (request.prefix.equals("")) {
            closest = path.leaf();
        } else {
            closest = path.leafParent();
        }

        LOG.log(Level.FINEST, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        LOG.log(Level.FINEST, "(leaf): ");
        printASTNodeInformation(closest);
        LOG.log(Level.FINEST, "(parentLeaf): ");
        printASTNodeInformation(path.leafParent());
        
        // we gotta make sure not to catch the parameterts as closest node
        if (closest instanceof ConstantExpression &&
            path.leafParent() instanceof MethodNode) {
            return path.leafParent();
        }
        
        return closest;
    }    
    
    /**
     * Calculate an AstPath from a given request or null if we can not get a
     * AST root-node from the request.
     * 
     * @param request
     * @return a freshly created AstPath object for the offset given in the request
     */
    private AstPath getPathFromRequest(CompletionRequest request){
        // figure out which class we are dealing with:
        ASTNode root = AstUtilities.getRoot(request.info);

        // in some cases we can not repair the code, therefore root == null
        // therefore we can not complete. See # 131317

        if (root == null) {
            LOG.log(Level.FINEST, "root == null"); // NOI18N
            return null;
        }

        return new AstPath(root, request.astOffset, request.doc);
    }
    
    /**
     * Complete Groovy Keywords.
     * 
     * @param proposals
     * @param request
     * @param isSymbol
     * @return
     */
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

    private boolean completeFields(List<CompletionProposal> proposals, CompletionRequest request) {
        LOG.log(Level.FINEST, "completeFields(...)"); // NOI18N

        ASTNode closest = getClosestNode(request);
        ClassNode declClass = getDeclaringClass(closest);
        
        if(declClass == null){
            LOG.log(Level.FINEST, "No declaring class found, bail out ..."); // NOI18N
            return false;
        }
        
        LOG.log(Level.FINEST, "Declaring class is : {0}", declClass); // NOI18N
        
        List<FieldNode> fields = declClass.getFields();
        
        for (FieldNode field : fields) {
            proposals.add(new FieldItem(field.getName(), anchor, request, javax.lang.model.element.ElementKind.FIELD, field.getType()));
        }

        return false;
    }
    
    /**
     * Complete potential import statements if we're invoced from a suitable
     * position (outside method or class, right behind an import statement)
     * 
     * @param proposals the CompletionPropasal we should populate
     * @param request wrapper object for this specific request ( position etc.)
     * @return true if we found something suitable
     */
    private boolean completeImports(List<CompletionProposal> proposals, CompletionRequest request) {

        LOG.log(Level.FINEST, "completeImports(...)"); // NOI18N

        ASTNode closest = getClosestNode(request);

        if (closest != null && closest instanceof ModuleNode) {
            int position = request.lexOffset;

            try {
                int rowStart = org.netbeans.editor.Utilities.getRowStart(request.doc, position);
                int nonWhite = org.netbeans.editor.Utilities.getFirstNonWhiteFwd(request.doc, rowStart);
                
                Token<? extends GroovyTokenId> importToken = LexUtilities.getToken(request.doc, nonWhite);
                
                    if (importToken != null && importToken.id() == GroovyTokenId.LITERAL_import) {
                        LOG.log(Level.FINEST, "Right behind an import statement");
                        
                        // fixme: the positioning to nonWhite seems to fail in this example.
                        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(request.doc, nonWhite);
                        
                        ts.move(nonWhite);
                        ts.moveNext();
                        ts.moveNext();
                            
                        String pkgPrefix = "";
                        
                        while (ts.isValid() && ts.moveNext() && ts.offset() < position ) {
                            Token<? extends GroovyTokenId> t = (Token<? extends GroovyTokenId>) ts.token();
                            
                            if(t.id() == GroovyTokenId.DOT || t.id() == GroovyTokenId.IDENTIFIER){
                                pkgPrefix = pkgPrefix + t.text().toString();
                            } else {
                                break;
                            }
                        }
                        
                        LOG.log(Level.FINEST, "Token prefix = >{0}<", pkgPrefix);
                        
                        DataObject dob = NbEditorUtilities.getDataObject(request.doc);
                        
                        if (dob == null) {
                            LOG.log(Level.FINEST, "Problem getting DataObject");
                            return false;
                        }
                        
                        FileObject fo = dob.getPrimaryFile();
            
                        if (fo == null) {
                            LOG.log(Level.FINEST, "Problem getting FileObject");
                            return false;
                        }
                        
                        ClasspathInfo pathInfo = NbUtilities.getClasspathInfoForFileObject(fo);

                        if (pathInfo == null) {
                            LOG.log(Level.FINEST, "Problem getting ClasspathInfo");
                            return false;
                        }
                        
                        // try to find suitable packages ...
                        
                        Set<String> pkgSet;

                        pkgSet = pathInfo.getClassIndex().getPackageNames(pkgPrefix, true, EnumSet.allOf(ClassIndex.SearchScope.class));
        
                        for (String singlePackage : pkgSet) {
                            LOG.log(Level.FINEST, "PKG set item: {0}", singlePackage);
                            
                            singlePackage = singlePackage.substring(pkgPrefix.length());
                            
                            if(singlePackage.length() > 0){
                                proposals.add(new PackageItem(singlePackage, anchor, request));
                            }
                        }
                        
                        // There might be some classes to propose. This is a pretty stupid way of getting
                        // all types in a given package: Retrieve *all* types from the CL and filter-out
                        // everything you are not interested in. 

                        LOG.log(Level.FINEST, "Now looking for types ...");
                        LOG.log(Level.FINEST, "Prefix = >{0}<", pkgPrefix);
                        

                        Set<org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>> typeNames;

                        typeNames = pathInfo.getClassIndex().getDeclaredTypes(".*", org.netbeans.api.java.source.ClassIndex.NameKind.CASE_INSENSITIVE_REGEXP,
                            EnumSet.allOf(ClassIndex.SearchScope.class));

                        for (org.netbeans.api.java.source.ElementHandle<TypeElement> typeName : typeNames) {

                            String fqn = typeName.getQualifiedName();
                            if (fqn.startsWith(pkgPrefix) && 
                                !fqn.matches(".*\\.\\d$")) {
                                
                                if(!isPackageAlreadyProposed(pkgSet, fqn)){
                                    javax.lang.model.element.ElementKind ek = typeName.getKind();

                                    if (ek == javax.lang.model.element.ElementKind.CLASS ||
                                        ek == javax.lang.model.element.ElementKind.INTERFACE) {
                                        LOG.log(Level.FINEST, "Kind/Name: {0}/{1}", new Object[]{ek, fqn});
                                        fqn = fqn.substring(pkgPrefix.length());
                                        proposals.add(new TypeItem(fqn, anchor, request, ek));
                                    }
                                }
                            }
                        }
                        return true;
                    }
                
            } catch (BadLocationException ex) {
                LOG.log(Level.FINEST, "BadLocationException: {0}", ex);
                return false;
            }
        }
        return false;
    }

    
    boolean isPackageAlreadyProposed (Set<String> pkgSet, String prefix ) {
        for (String singlePackage : pkgSet) {
            if(prefix.startsWith(singlePackage)) {
                return true;
            }
        }
        return false;
    }
    
    
    private ClassNode getDeclaringClass(ASTNode closest) {
        ClassNode declClass = null;
        
        if (closest != null && closest instanceof AnnotatedNode) {
            LOG.log(Level.FINEST, "closest: AnnotatedNode"); // NOI18N

            // if this AnnotetedNode happens to be a ClassNode then 
            // just cast it. Otherwise try to find declaring class

            if (closest instanceof ClassNode) {
                declClass = (ClassNode) closest;
                
            } else {
                declClass = ((AnnotatedNode) closest).getDeclaringClass();
            }
        } else if (closest != null && closest instanceof Expression) {
            LOG.log(Level.FINEST, "closest: Expression"); // NOI18N
            declClass = ((Expression) closest).getType();
        } else if (closest != null && closest instanceof ExpressionStatement) {
            LOG.log(Level.FINEST, "closest: ExpressionStatement"); // NOI18N
            Expression expr = ((ExpressionStatement) closest).getExpression();
            if (expr instanceof PropertyExpression) {
                declClass = ((PropertyExpression) expr).getObjectExpression().getType();
            } else {
                return null;
            }
        } else {
            LOG.log(Level.FINEST, "Found nothing to work on"); // NOI18N
            return null;
        }
        
        return declClass;
    }
    
    
    /**
     * Complete the methods invocable on a class.
     * @param proposals the CompletionProposal List we populate (return value)
     * @param request location information used as input
     * @return true if we found something usable
     */
    private boolean completeMethods(List<CompletionProposal> proposals, CompletionRequest request) {

        LOG.log(Level.FINEST, "completeMethods(...)"); // NOI18N

        ASTNode closest = getClosestNode(request);

        Class clz = null;
        ClassNode declClass = getDeclaringClass(closest);

        if (declClass == null) {
            LOG.log(Level.FINEST, "No declaring class found"); // NOI18N
            return false;            
        }
        
        if (clz == null) {
            try {
                clz = Class.forName(declClass.getName());
            } catch (Exception e) {
                LOG.log(Level.FINEST, "Class.forName() failed: {0}", e.getMessage()); // NOI18N
                return false;
            }
        }

        if (clz != null) {
            MetaClass metaClz = GroovySystem.getMetaClassRegistry().getMetaClass(clz);

            if (metaClz != null) {
                for (Object method : metaClz.getMetaMethods()) {
                    populateProposal(clz, method, request, proposals, true);
                }

                for (Object method : metaClz.getMethods()) {
                    populateProposal(clz, method, request, proposals, false);
                }
            }
        }

        return true;
    }

    public CodeCompletionResult complete(CodeCompletionContext context) {
        CompilationInfo info = context.getInfo();
        int lexOffset = context.getCaretOffset();
        String prefix = context.getPrefix();
        NameKind kind = context.getNameKind();
        QueryType queryType = context.getQueryType();
        this.caseSensitive = context.isCaseSensitive();
        HtmlFormatter formatter = context.getFormatter();

        final int astOffset = AstUtilities.getAstOffset(info, lexOffset);

        LOG.log(Level.FINEST, "complete(...), prefix: {0}", prefix); // NOI18N


        // Avoid all those annoying null checks
        if (prefix == null) {
            prefix = "";
        }

        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

        anchor = lexOffset - prefix.length();

        final Document document = info.getDocument();
        if (document == null) {
            return CodeCompletionResult.NONE;
        }

        // TODO - move to LexUtilities now that this applies to the lexing offset?
//        lexOffset = AstUtilities.boundCaretOffset(info, lexOffset);

        // Discover whether we're in a require statement, and if so, use special completion
        final TokenHierarchy<Document> th = TokenHierarchy.get(document);
        final BaseDocument doc = (BaseDocument) document;
        final FileObject fileObject = info.getFileObject();

        doc.readLock(); // Read-lock due to Token hierarchy use

        try {
            // Carry completion context around since this logic is split across lots of methods
            // and I don't want to pass dozens of parameters from method to method; just pass
            // a request context with supporting info needed by the various completion helpers i
            CompletionRequest request = new CompletionRequest();
            request.formatter = formatter;
            request.lexOffset = lexOffset;
            request.astOffset = astOffset;
            request.doc = doc;
            request.info = info;
            request.prefix = prefix;
            request.th = th;
            request.kind = kind;
            request.queryType = queryType;
            request.fileObject = fileObject;

            // No - we don't complete keywords, since one can get'em by hitting
            // ctrl-k or use an abbrevation. Displaying them without a documentation
            // makes no sense as well, see:
            // http://www.netbeans.org/issues/show_bug.cgi?id=126500
            // completeKeywords(proposals, request, showSymbols);

            // complete methods
            completeMethods(proposals, request);

            // complete imports
            completeImports(proposals, request);
            
            // complete fields
            completeFields(proposals, request); 
           

            return new DefaultCompletionResult(proposals, false);
        } finally {
            doc.readUnlock();
        }
    //return proposals;
    }

    /**
     * create the signature-string of this method usable as a 
     * Javadoc URL suffix (behind the # ) 
     *    
     * This was needed, since from groovy 1.5.4 to 
     * 1.5.5 the MetaMethod.getSignature() changed from
     * human-readable to Class.getName() output.
     * 
     * To make matters worse, we have some subtle 
     * differences between JDK and GDK MetaMethods
     * 
     * method.getSignature for the JDK gives the return-
     * value right behind the method and encodes like Class.getName():
     *   
     * codePointCount(II)I
     *    
     * GDK-methods look like this:
     * java.lang.String center(java.lang.Number, java.lang.String)
     * 
     * TODO: if groovy folks ever change this (again), we're falling
     * flat on our face.
     * 
     */
    String getMethodSignature(MetaMethod method, boolean forURL, boolean isGDK) {
        String methodSignature = method.getSignature();
        methodSignature = methodSignature.trim();

        if (isGDK) {
            // remove return value
            int firstSpace = methodSignature.indexOf(" ");

            if (firstSpace != -1) {
                methodSignature = methodSignature.substring(firstSpace + 1);
            }

            if (forURL) {
                methodSignature = methodSignature.replaceAll(", ", ",%20");
            }

            return methodSignature;

        } else {
            String parts[] = methodSignature.split("[()]");

            if (parts.length < 2) {
                return "";
            }

            String paramsBody = decodeTypes(parts[1], forURL);

            return parts[0] + "(" + paramsBody + ")";
        }
    }

    /**
     * This is more a less the reverse function for Class.getName() 
     */
    String decodeTypes(final String encodedType, boolean forURL) {


        String DELIMITER = ",";

        if (forURL) {
            DELIMITER = DELIMITER + "%20";
        } else {
            DELIMITER = DELIMITER + " ";
        }

        StringBuffer sb = new StringBuffer("");
        boolean nextIsAnArray = false;

        for (int i = 0; i < encodedType.length(); i++) {
            char c = encodedType.charAt(i);

            if (c == '[') {
                nextIsAnArray = true;
                continue;
            } else if (c == 'Z') {
                sb.append("boolean");
            } else if (c == 'B') {
                sb.append("byte");
            } else if (c == 'C') {
                sb.append("char");
            } else if (c == 'D') {
                sb.append("double");
            } else if (c == 'F') {
                sb.append("float");
            } else if (c == 'I') {
                sb.append("int");
            } else if (c == 'J') {
                sb.append("long");
            } else if (c == 'S') {
                sb.append("short");
            } else if (c == 'L') { // special case reference
                i++;
                int semicolon = encodedType.indexOf(";", i);
                String typeName = encodedType.substring(i, semicolon);
                typeName = typeName.replace('/', '.');

                sb.append(typeName);
                i = semicolon;
            }

            if (nextIsAnArray) {
                sb.append("[]");
                nextIsAnArray = false;
            }

            if (i < encodedType.length() - 1) {
                sb.append(DELIMITER);
            }

        }

        return sb.toString();
    }

    public String document(CompilationInfo info, ElementHandle element) {
        LOG.log(Level.FINEST, "document(), ElementHandle : {0}", element);

        String ERROR = "<h2>" + NbBundle.getMessage(CodeCompleter.class, "CodeCompleter_NoJavaDocFound") + "</h2>";
        String doctext = null;

        if (element instanceof AstMethodElement) {
            AstMethodElement ame = (AstMethodElement) element;

            String base = "";

            if (jdkJavaDocBase != null && ame.isGDK() == false) {
                base = jdkJavaDocBase;
            } else if (groovyJavaDocBase != null && ame.isGDK() == true) {
                base = groovyJavaDocBase;
            } else {
                LOG.log(Level.FINEST, "Neither JDK nor GDK or error locating: {0}", ame.isGDK());
                return ERROR;
            }

            MetaMethod mm = ame.getMethod();

            // enable this to troubleshoot subtle differences in JDK/GDK signatures
            printMethod(mm);

            // figure out who originally defined this method

            String className = null;

            if (ame.isGDK()) {
                className = mm.getDeclaringClass().getCachedClass().getName();
            } else {

                String declName = null;

                if (mm != null) {
                    CachedClass cc = mm.getDeclaringClass();
                    if (cc != null) {
                        Class clz = cc.getCachedClass();
                        if (clz != null) {
                            declName = clz.getName();
                        }
                    }
                }

                if (declName != null) {
                    className = declName;
                } else {
                    className = ame.getClz().getName();
                }
            }

            // create path from fq java package name:
            // java.lang.String -> java/lang/String.html
            String classNamePath = className.replace(".", "/");
            classNamePath = classNamePath + ".html"; // NOI18N
            
            // if the file can be located in the GAPI folder prefer it
            // over the JDK
            if (!ame.isGDK()) {
                
                URL url;
                File testFile;
                
                try {
                    url = new URL(gapiDocBase + classNamePath);
                    testFile = new File(url.toURI());
                } catch (MalformedURLException ex) {
                    LOG.log(Level.FINEST, "MalformedURLException: {0}", ex);
                    return ERROR;
                } catch (URISyntaxException uriEx) {
                    LOG.log(Level.FINEST, "URISyntaxException: {0}", uriEx);
                    return ERROR;
                }
         
                if(testFile != null && testFile.exists()){
                    base = gapiDocBase;
                } 
            }

            // create the signature-string of the method
            String sig = getMethodSignature(ame.getMethod(), true, ame.isGDK());
            String printSig = getMethodSignature(ame.getMethod(), false, ame.isGDK());

            String urlName = base + classNamePath + "#" + sig;

            try {
                LOG.log(Level.FINEST, "Trying to load URL = {0}", urlName); // NOI18N
                doctext = HTMLJavadocParser.getJavadocText(
                    new URL(urlName),
                    false,
                    ame.isGDK());
            } catch (MalformedURLException ex) {
                LOG.log(Level.FINEST, "document(), URL trouble: {0}", ex); // NOI18N
                return ERROR;
            }

            // If we could not find a suitable JavaDoc for the method - say so. 
            if (doctext == null) {
                return ERROR;
            }

            doctext = "<h3>" + className + "." + printSig + "</h3><BR>" + doctext;
        }
        return doctext;
    }

    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPrefix(CompilationInfo info, int caretOffset, boolean upToOffset) {
        return null;
    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        char c = typedText.charAt(0);

        if (c == '.') {
            return QueryType.COMPLETION;
        }

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
        protected GroovyElement element;
        protected int anchorOffset;
        protected boolean symbol;
        protected boolean smart;

        private GroovyCompletionItem(GroovyElement element, int anchorOffset, CompletionRequest request) {
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
                return "." + getName();
            } else {
                return getName();
            }
        }

        public String getSortText() {
            return getName();
        }

        public ElementHandle getElement() {
            LOG.log(Level.FINEST, "getElement() request.info : {0}", request.info);
            LOG.log(Level.FINEST, "getElement() element : {0}", element);

            return null;
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
            return new String[]{"(", ")"}; // NOI18N
        }

        public String getCustomInsertTemplate() {
            return null;
        }
    }

    private class MethodItem extends GroovyCompletionItem {

        private static final String GROOVY_METHOD = "org/netbeans/modules/groovy/editor/resources/groovydoc.png"; //NOI18N
        MetaMethod method;
        HtmlFormatter formatter;
        boolean isGDK;
        AstMethodElement methodElement;
        Class clz;

        MethodItem(Class clz, MetaMethod method, int anchorOffset, CompletionRequest request, boolean isGDK) {
            super(null, anchorOffset, request);
            this.clz = clz;
            this.method = method;
            this.formatter = request.formatter;
            this.isGDK = isGDK;

            // This is an artificial, new ElementHandle which has no real
            // equivalent in the AST. It's used to match the one passed to super.document()
            methodElement = new AstMethodElement(new ASTNode(), clz, method, isGDK);
        }

        @Override
        public String getName() {
            return method.getName().toString() + "()";
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getLhsHtml() {

            ElementKind kind = getKind();
            boolean emphasize = false;

            formatter.reset();
            if (method.isStatic()) {
                emphasize = true;
                formatter.emphasis(true);
            }
            formatter.name(kind, true);

            if (isGDK) {
                formatter.appendText(method.getName().toString());

                // construct signature by removing package names.

                String signature = method.getSignature();
                int start = signature.indexOf("(");
                int end = signature.indexOf(")");

                String sig = signature.substring(start + 1, end);

                String simpleSig = "";

                for (String param : sig.split(",")) {
                    if (!simpleSig.equals("")) {
                        simpleSig = simpleSig + ", ";
                    }
                    simpleSig = simpleSig + NbUtilities.stripPackage(param);
                }

                formatter.appendText("(" + simpleSig + ")");
            } else {
                formatter.appendText(getMethodSignature(method, false, isGDK));
            }


            formatter.name(kind, false);

            if (emphasize) {
                formatter.emphasis(false);
            }
            return formatter.getText();
        }

        @Override
        public String getRhsHtml() {
            formatter.reset();

            // no FQN return types but only the classname, please:

            String retType = method.getReturnType().toString();
            retType = NbUtilities.stripPackage(retType);

            formatter.appendHtml(retType);

            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {

            if (!isGDK) {
                return null;
            }

            if (classIcon == null) {
                classIcon = new ImageIcon(org.openide.util.Utilities.loadImage(GROOVY_METHOD));
            }

            return classIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {

            // to display the documentation box for each element, the completion-
            // element needs to implement this method. Otherwise document(...)
            // won't even be called at all.

            return methodElement;
        }
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
            if (classIcon == null) {
                classIcon = new ImageIcon(org.openide.util.Utilities.loadImage(GROOVY_KEYWORD));
            }

            return classIcon;
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
    
    /**
     * 
     */
    private class PackageItem extends GroovyCompletionItem {
        private final String keyword;

        PackageItem(String keyword, int anchorOffset, CompletionRequest request) {
            super(null, anchorOffset, request);
            this.keyword = keyword;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        @Override
        public String getRhsHtml() {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.PACKAGE, null);
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
    
    /**
     * 
     */
    private class TypeItem extends GroovyCompletionItem {
        private final String name;
        private final javax.lang.model.element.ElementKind ek;

        TypeItem(String name, int anchorOffset, CompletionRequest request, javax.lang.model.element.ElementKind ek) {
            super(null, anchorOffset, request);
            this.name = name;
            this.ek = ek;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml() {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(ek, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return GroovyParser.createHandle(request.info, new KeywordElement(name));
        }
    }

    /**
     * 
     */    
    private class FieldItem extends GroovyCompletionItem {

        private final String name;
        private final javax.lang.model.element.ElementKind ek;
        private final ClassNode type;

        FieldItem(String name, int anchorOffset, CompletionRequest request, javax.lang.model.element.ElementKind ek, ClassNode type) {
            super(null, anchorOffset, request);
            this.name = name;
            this.ek = ek;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public String getRhsHtml() {
            return type.getNameWithoutPackage();
        }

        @Override
        public ImageIcon getIcon() {
            // todo: what happens, if i get a CCE here?
            return (ImageIcon) ElementIcons.getElementIcon(ek, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return GroovyParser.createHandle(request.info, new KeywordElement(name));
        }
    }
}
