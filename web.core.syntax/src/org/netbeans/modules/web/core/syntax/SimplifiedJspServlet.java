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

package org.netbeans.modules.web.core.syntax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.core.syntax.spi.JspColoringData;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.Node.IncludeDirective;
import org.netbeans.modules.web.jsps.parserapi.Node.Visitor;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import static org.netbeans.api.jsp.lexer.JspTokenId.JavaCodeType;

/**
 * Utility class for generating a simplified <em>JSP servlet</em> class from a JSP file.
 * Using a full featured JSP parser would be too resource demanding,
 * we need a lightweight solution to be used with code completion.
 *
 * Inputs: original JSP document, caret offset within the original document
 * Outputs: a body of a simplified JSP servlet class, offset of the corresponding
 *          position in the servlet class
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class SimplifiedJspServlet {

    private static final String CLASS_HEADER = "\nclass SimplifiedJSPServlet extends %s {\n" + //NOI18N
            "\tprivate static final long serialVersionUID = 1L;\n"; //NOI18N
    private static final String METHOD_HEADER = "\n\tvoid mergedScriptlets(\n"
            + "\t\tHttpServletRequest request,\n"
            + "\t\tHttpServletResponse response,\n"
            + "\t\tHttpSession session,\n"
            + "\t\tServletContext application,\n"
            + "\t\tJspWriter out,\n"
            + "\t\tServletConfig config,\n"
            + "\t\tJspContext jspContext,\n"
            + "\t\tObject page,\n"
            + "\t\tPageContext pageContext,\n"
            + "\t\tThrowable exception\n"
            + "\t) throws Throwable {\n"; //NOI18N
    private static final String CLASS_FOOTER = "\n\t}\n}"; //NOI18N
    private final Document doc;
    private CharSequence charSequence;
    private final FileObject fobj;
    private final Snapshot snapshot;
    private final ArrayList<Embedding> codeBlocks = new ArrayList<Embedding>();

    private Embedding header;
    private List<Embedding> scriptlets = new LinkedList<Embedding>();
    private List<Embedding> declarations = new LinkedList<Embedding>();
    private List<Embedding> localImports = new LinkedList<Embedding>();
    // keep bean declarations separate to avoid duplicating the declaration, see #130745
    private Embedding beanDeclarations;
    private boolean processCalled = false;
    private Embedding implicitImports;
    private int expressionIndex = 1;
    private static final Logger logger = Logger.getLogger(SimplifiedJspServlet.class.getName());
    private boolean processingSuccessful = true;

    public SimplifiedJspServlet(Snapshot snapshot, Document doc){
        this(snapshot, doc, null);
    }

    public SimplifiedJspServlet(Snapshot snapshot, Document doc, CharSequence charSequence) {
        this.doc = doc;

        if (charSequence == null) {
            this.charSequence = snapshot.getText();
        } else {
            this.charSequence = charSequence;
        }

        if (doc != null){
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            fobj = (dobj != null) ? NbEditorUtilities.getDataObject(doc).getPrimaryFile(): null;
        } else {
            logger.log(Level.SEVERE, "Unable to find FileObject for document");
            fobj = null;
        }

        this.snapshot = snapshot;
    }

    public void process() throws BadLocationException{
        process(false);
    }

    public void process(final boolean processAsIncluded) throws BadLocationException {
        processCalled = true;

        if( fobj == null) {
            //do not handle non fileobject documents like coloring properties preview document
            processingSuccessful = false;
            return;
        }

        if (!isServletAPIOnClasspath()){
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    displayServletAPIMissingWarning();
                }
            });

            processingSuccessful = false;
            return;
        }
         //Workaround of issue #120195 - Deadlock in jspparser while reformatting JSP
        //Needs to be removed after properly fixing the issue
        if (!DocumentUtilities.isWriteLocked(doc)) {
            JspParserAPI.ParseResult parseResult = JspUtils.getCachedParseResult(fobj, false, false);
            if (parseResult == null || !parseResult.isParsingSuccess()) {
                processingSuccessful = false;
                return;
            }
        }

        final BadLocationException[] ex = new BadLocationException[1];

        processIncludes();

        TokenHierarchy tokenHierarchy = TokenHierarchy.create(charSequence, JspTokenId.language());//TokenHierarchy.get(doc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence(); //get top level token sequence
        if (!tokenSequence.moveNext()) {
            return; //no tokens in token sequence
        }

        /**
         * process java code blocks one by one
         * note: We count on the fact the scripting language in JSP is Java
         */
        do {
            Token token = tokenSequence.token();

            if (token.id() == JspTokenId.SCRIPTLET) {
                int blockStart = token.offset(tokenHierarchy);

                JavaCodeType blockType = (JavaCodeType) token.getProperty(JspTokenId.SCRIPTLET_TOKEN_TYPE_PROPERTY);

//                String blockBody = charSequence.subSequence(blockStart, blockEnd).toString(); //doc.getText(blockStart, blockEnd - blockStart);
                List<Embedding> buff = blockType == JavaCodeType.DECLARATION ? declarations : scriptlets;

                if (blockType == JavaCodeType.EXPRESSION) {
                    // ignore JSP expressions in included files
                    if (!processAsIncluded){
                        // the "" + (...) construction is used to preserve compatibility with pre-autoboxing java
                        // see issue #116598
                        buff.add(snapshot.create(String.format("\t\tObject expr%1$d = \"\" + (", expressionIndex++), "text/x-java")); //NOI18N
                        buff.add(snapshot.create(blockStart, token.length(), "text/x-java"));
                        buff.add(snapshot.create(");\n", "text/x-java")); //NOI18N
                    }
                } else {
                        buff.add(snapshot.create(blockStart, token.length(), "text/x-java"));
                        buff.add(snapshot.create("\n", "text/x-java")); //NOI18N
                }
            }
        } while (tokenSequence.moveNext());

        List<String> localImports = processImportDirectives();

        if (ex[0] != null) {
            throw ex[0];
        }

        header = snapshot.create(getClassHeader(), "text/x-java");
        implicitImports = snapshot.create(createImplicitImportStatements(localImports), "text/x-java");
        beanDeclarations = snapshot.create("\n" + createBeanVarDeclarations(), "text/x-java");
    }

    private void processIncludes()  {
        PageInfo pageInfo = getPageInfo();

        if (pageInfo == null) {
            //if we do not get pageinfo it is unlikely we will get something reasonable from
            //jspSyntax.getParseResult()...
            return ;
        }

        final Collection<String> processedFiles = new TreeSet<String>(Collections.singleton(fobj.getPath()));

        if (pageInfo.getIncludePrelude() != null){
            for (String preludePath : (List<String>)pageInfo.getIncludePrelude()){
                processIncludedFile(preludePath, processedFiles);
            }
        }

        Visitor visitor = new Visitor() {

            @Override
            public void visit(IncludeDirective includeDirective) throws JspException {
                String fileName = includeDirective.getAttributeValue("file");
                processIncludedFile(fileName, processedFiles);
            }
        };

        JspSyntaxSupport jspSyntax = JspSyntaxSupport.get(doc);
        try {
            JspParserAPI.ParseResult parseResult = jspSyntax.getParseResult();

            if (parseResult != null && parseResult.getNodes() != null){
                parseResult.getNodes().visit(visitor);
            }
        } catch (JspException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private boolean consumeWS(TokenSequence tokenSequence){
        if (tokenSequence.token().id() == JspTokenId.WHITESPACE){
            return tokenSequence.moveNext();
        }
        
        return true;
    }

    /**
     * The information about imports obtained from the JSP Parser
     * does not include data about offsets,
     * therefore it is necessary to some manual parsing.
     * 
     * This method creates embeddings and stores them in the
     * <code>localImports</code>
     *
     * additionaly it returns a list of imports found
     */
    private List<String> processImportDirectives() {
        List<String> imports = new ArrayList<String>();
        TokenHierarchy tokenHierarchy = TokenHierarchy.create(charSequence, JspTokenId.language());//TokenHierarchy.get(doc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        tokenSequence.moveStart();

        while (tokenSequence.moveNext()) {
            PieceOfCode pieceOfCode = extractCodeFromTagAttribute(tokenSequence, "page", "import"); //NOI18N

            if (pieceOfCode != null){
                localImports.add(snapshot.create("import ", "text/x-java")); //NOI18N
                localImports.add(snapshot.create(pieceOfCode.getStartOffset(), pieceOfCode.getLength(), "text/x-java")); //NOI18N
                localImports.add(snapshot.create(";\n", "text/x-java")); //NOI18N
            } else {
                pieceOfCode = extractCodeFromTagAttribute(tokenSequence, "tag", "import"); //NOI18N

                if (pieceOfCode != null) {
                    localImports.add(snapshot.create("import ", "text/x-java")); //NOI18N
                    localImports.add(snapshot.create(pieceOfCode.getStartOffset(), pieceOfCode.getLength(), "text/x-java")); //NOI18N
                    localImports.add(snapshot.create(";\n", "text/x-java")); //NOI18N
                }
            }
        }
        return imports;
    }

    private PieceOfCode extractCodeFromTagAttribute(TokenSequence tokenSequence, String tagName, String attrName) {
        if (tokenSequence.token().id() == JspTokenId.TAG && TokenUtilities.equals(tagName, tokenSequence.token().text())) { //NOI18N

            if (tokenSequence.moveNext() && consumeWS(tokenSequence)) {

                if (tokenSequence.token().id() == JspTokenId.ATTRIBUTE && TokenUtilities.equals(attrName, tokenSequence.token().text())) { //NOI18N

                    if (tokenSequence.moveNext() && consumeWS(tokenSequence) && tokenSequence.token().id() == JspTokenId.SYMBOL && TokenUtilities.equals("=", tokenSequence.token().text())) {

                        if (tokenSequence.moveNext() && consumeWS(tokenSequence) && tokenSequence.token().id() == JspTokenId.ATTR_VALUE) {

                            String val = tokenSequence.token().text().toString();

                            if (val.length() > 2 && val.charAt(0) == '"' && val.charAt(val.length() - 1) == '"') {

                                int startOffset = tokenSequence.offset() + 1;
                                int len = val.length() - 2;
                                String imprt = val.substring(1, len);
                                PieceOfCode pieceOfCode = new PieceOfCode(imprt, startOffset, len);
                                
                                return pieceOfCode;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private void processIncludedFile(String filePath, Collection<String> processedFiles) {
//        FileObject includedFile = JspUtils.getFileObject(doc, filePath);
//
//        if (includedFile != null && includedFile.canRead()
//                // prevent endless loop in case of a circular reference
//                && !processedFiles.contains(includedFile.getPath())) {
//
//            processedFiles.add(includedFile.getPath());
//
//            try {
//                DataObject includedFileDO = DataObject.find(includedFile);
//                String mimeType = includedFile.getMIMEType();
//
//                if ("text/x-jsp".equals(mimeType) || "text/x-tag".equals(mimeType)) { //NOI18N
//                    EditorCookie editor = includedFileDO.getCookie(EditorCookie.class);
//
//                    if (editor != null) {
//                        SimplifiedJspServlet simplifiedServlet = new SimplifiedJspServlet(editor.openDocument());
//                        simplifiedServlet.process(true);
//
//                        declarations.append(simplifiedServlet.declarations);
//                        scriptlets.append(simplifiedServlet.scriptlets);
//                    }
//                }
//            } catch (Exception e) {
//                logger.log(Level.WARNING, e.getMessage(), e);
//            }
//        }
    }

    private boolean isServletAPIOnClasspath() {
        ClassPath cp = ClassPath.getClassPath(fobj, ClassPath.COMPILE);

        if (cp != null && cp.findResource("javax/servlet/http/HttpServlet.class") != null) { //NOI18N
            return true;
        }

        return false;
    }

    private void displayServletAPIMissingWarning() {
        if (fobj == null){
            return; //issue #160889
        }

        try {
            DataObject doJsp = DataObject.find(fobj);
            EditorCookie editor = doJsp.getCookie(EditorCookie.class);

            if (editor != null && editor.getOpenedPanes() != null) {

                JTextComponent component = editor.getOpenedPanes()[0];
                if (component != null) {
                    org.netbeans.editor.Utilities.setStatusBoldText(component,
                            NbBundle.getMessage(SimplifiedJspServlet.class, "MSG_MissingServletAPI"));
                }
            }
        } catch (DataObjectNotFoundException e) {
            // ignore
        }
    }

    private String createBeanVarDeclarations() {
        //TODO: the parser data contains no information about offsets and
        //therefore it is not possible to create proper java embeddings
        //inside bean declarations. We need a similar solution to what was
        //done for imports, see issue #161246
        StringBuilder beanDeclarationsBuff = new StringBuilder();

        PageInfo pageInfo = getPageInfo();

        if (pageInfo != null) {
            PageInfo.BeanData[] beanData = getBeanData();

            if (beanData != null) {
                for (PageInfo.BeanData bean : beanData) {
                    beanDeclarationsBuff.append(bean.getClassName() + " " + bean.getId() + ";\n"); //NOI18N
                }
            }

            if (pageInfo.isTagFile()){
                for (TagAttributeInfo info : pageInfo.getTagInfo().getAttributes()){
                    if (info.getTypeName() != null){ // will be null e.g. for fragment attrs
                        beanDeclarationsBuff.append(info.getTypeName() + " " + info.getName() + ";\n"); //NOI18N
                    }
                }
            }
        }

        JspSyntaxSupport syntaxSupport = JspSyntaxSupport.get(doc);
        JspColoringData coloringData = JspUtils.getJSPColoringData(fobj);

        if (coloringData != null && coloringData.getPrefixMapper() != null){
            Collection<String> prefixes = coloringData.getPrefixMapper().keySet();
            TagData fooArg = new TagData((Object[][])null);

            for (String prefix : prefixes) {
                List<TagInfo> tags = syntaxSupport.getAllTags(prefix, false); //do not require fresh data - #146762

                for (TagInfo tag : tags) {
                    // #146754 - prevent NPE:
                    if (tag == null) {
                        continue;
                    }
                    VariableInfo vars[] = tag.getVariableInfo(fooArg);

                    if (vars != null){
                        for (VariableInfo var : vars) {
                            // Create Variable Definitions
                            if (var.getVarName() != null && var.getClassName() != null
                                    && var.getDeclare()){
                                String varDeclaration = var.getClassName() + " " + var.getVarName() + ";\n";
                                beanDeclarationsBuff.append(varDeclaration);
                            }
                        }
                    }
                }
            }
        }

        return beanDeclarationsBuff.toString();
    }

    private String[] getImportsFromJspParser() {
        PageInfo pi = getPageInfo();
        if (pi == null) {
            //we need at least some basic imports
            return new String[]{"javax.servlet.*", "javax.servlet.http.*", "javax.servlet.jsp.*"};
        }
        List<String> imports = pi.getImports();
        return imports.toArray(new String[imports.size()]);
    }

    private PageInfo.BeanData[] getBeanData() {

        PageInfo pageInfo = getPageInfo();
        //pageInfo can be null in some cases when the parser cannot parse
        //the webmodule or the page itself
        if (pageInfo != null) {
            return pageInfo.getBeans();
        }

        //TagLibParseSupport support = (dobj == null) ?
        //null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        //return support.getTagLibEditorData().getBeanData();
        return null;
    }

    private PageInfo getPageInfo() {
        //Workaround of issue #120195 - Deadlock in jspparser while reformatting JSP
        //Needs to be removed after properly fixing the issue
        if(DocumentUtilities.isWriteLocked(doc)) {
            return null;
        }

        JspParserAPI.ParseResult parseResult = JspUtils.getCachedParseResult(fobj, true, false);

        if (parseResult != null) {
            return parseResult.getPageInfo();
        }

        //report error but do not break the entire CC
        logger.log(Level.INFO, null, "PageInfo obtained from JspParserAPI.ParseResult is null");

        return null;
    }


    /**
     * Add extra imports according to information obtained from the JSP parser
     *
     * @param localImports imports already included in the Simplified Servlet
     * by the processImportDirectives method
     */
    private String createImplicitImportStatements(List<String> localImports) {
        StringBuilder importsBuff = new StringBuilder();
        String[] imports = getImportsFromJspParser();

        if (imports == null || imports.length == 0){
            processingSuccessful = false;
        } else {
            // TODO: better support for situation when imports is null
            // (JSP doesn't belong to a project)
            for (String pckg : imports) {
                if (!localImports.contains(pckg)){
                    importsBuff.append("import " + pckg + ";\n"); //NOI18N
                }
            }
        }

        return importsBuff.toString();
    }

    private String getClassHeader() {
        String extendsClass = null; //NOI18N
        PageInfo pageInfo = getPageInfo();

        if (pageInfo != null) {
            extendsClass = pageInfo.getExtends();
        }

        if (extendsClass == null ||
                // workaround for issue #116314
                "org.apache.jasper.runtime.HttpJspBase".equals(extendsClass)){ //NOI18N
            extendsClass = "HttpServlet"; //NOI18N
        }

        return String.format(CLASS_HEADER, extendsClass);
    }

    private void assureProcessCalled() {
        if (!processCalled) {
            throw new IllegalStateException("process() method must be called first!"); //NOI18N
        }
    }

    public Embedding getSimplifiedServlet() {
        assureProcessCalled();

        if (!processingSuccessful){
            return null;
        }

        if (localImports.isEmpty() && declarations.isEmpty() && scriptlets.isEmpty()) {
            return null;
        }

        List<Embedding> content = new LinkedList<Embedding>();

        content.add(implicitImports);
        content.addAll(localImports);
        content.add(header);
        content.addAll(declarations);
        content.add(beanDeclarations);
        content.add(snapshot.create(METHOD_HEADER, "text/x-java"));
        content.addAll(scriptlets);
        content.add(snapshot.create(CLASS_FOOTER, "text/x-java"));

        return Embedding.create(content);
    }

    public static abstract class VirtualJavaClass {

        public final void create(Document doc, String virtualClassBody) {
            FileObject fileDummyJava = null;
            List<? extends CompletionItem> javaCompletionItems = null;

            try {
                FileSystem memFS = FileUtil.createMemoryFileSystem();
                fileDummyJava = memFS.getRoot().createData("SimplifiedJSPServlet", "java"); //NOI18N
                PrintWriter writer = new PrintWriter(fileDummyJava.getOutputStream());
                writer.print(virtualClassBody);
                writer.close();

                Source source = Source.create(fileDummyJava);
                process(fileDummyJava, source);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        protected abstract void process(FileObject fileObject, Source javaEmbedding);
    }

    private class PieceOfCode{
        private String content;
        private int startOffset;
        private int length;

        public PieceOfCode(String content, int startOffset, int length) {
            this.content = content;
            this.startOffset = startOffset;
            this.length = length;
        }

        public String getContent() {
            return content;
        }

        public int getLength() {
            return length;
        }

        public int getStartOffset() {
            return startOffset;
        }
    }
}
