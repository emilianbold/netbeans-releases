/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax.completion;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.java.JavaCompletionProvider;
import org.netbeans.modules.web.core.syntax.JspDirectiveTokenContext;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Code completion functionality for embedded java code (scriptlets, JSP declarations)
 * as well as the <%@page import="..." %> and <%@page extends="..." %> directives
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JavaJSPCompletionProvider implements CompletionProvider {
    private JavaCompletionProvider javaCompletionProvider = new JavaCompletionProvider();

    public CompletionTask createTask(int queryType, final JTextComponent component) {
        if ((queryType & COMPLETION_QUERY_TYPE) == 0){
            return null;
        }

        try {
            JspSyntaxSupport sup = (JspSyntaxSupport)Utilities.getSyntaxSupport(component);
            TokenItem ti = sup.getItemAtOrBefore(component.getCaret().getDot());
            //delegate to java cc provider if the context is really java code
            if(ti != null && ti.getTokenContextPath().contains(JavaTokenContext.contextPath)){
                return new AsyncCompletionTask(new EmbeddedJavaCompletionQuery(component, queryType), component);
            }

        }catch(BadLocationException ex) {
            ex.printStackTrace();
        }

        //not java context =>
        return new AsyncCompletionTask(new ImportCompletionQuery(component, queryType), component);
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return javaCompletionProvider.getAutoQueryTypes(component, typedText);
    }

    static abstract class DelegatedQuery extends AsyncCompletionQuery {
        protected int queryType;
        protected JTextComponent component;

        DelegatedQuery(JTextComponent component, int queryType){
            this.queryType = queryType;
            this.component = component;
        }

        protected abstract FakedJavaClass getFakedJavaClass(Document doc, int caretOffset);

        @Override protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            FileObject fileDummyJava = null;

            try {
                FakedJavaClass fakedJavaClass = getFakedJavaClass(doc, caretOffset);

                if (fakedJavaClass == null){
                    resultSet.finish();
                    return;
                }

                FileSystem fs = FileUtil.createMemoryFileSystem();
                fileDummyJava = fs.getRoot().createData("DummyJSP", "java"); //NOI18N
                PrintStream ps = new PrintStream(fileDummyJava.getOutputStream());
                ps.print(fakedJavaClass.getBody());
                ps.close();

                FileObject jspFile = NbEditorUtilities.getFileObject(doc);
                //Project project = FileOwnerQuery.getOwner(jspFile);

                //Sources sources = ProjectUtils.getSources(project);
                //SourceGroup[] sg = sources.getSourceGroups("doc_root"); // doc_root
                //FileObject sourceRoot = sg != null && sg.length > 0 ? sourceRoot = sg[0].getRootFolder() : null;


                //ClassPathProvider cpProvider = (ClasspathProvider)project.getLookup().lookup(ClassPathProvider.class);
                //cpProvider.findClassPath(fileDummyJava, ClassPath.COMPILE);

                ClasspathInfo cpInfo = ClasspathInfo.create(jspFile);
                final JavaSource source = JavaSource.create(cpInfo, fileDummyJava);
                int shiftedOffset = fakedJavaClass.getOffset();
                //System.err.println(javaCreator.getTestSting());

                doc.putProperty(JavaSource.class, new WeakReference<JavaSource>(null) {
                    public JavaSource get() {
                        return source;
                    }
                });

                List<? extends CompletionItem> javaCompletionItems = JavaCompletionProvider.query(
                        source, queryType, shiftedOffset, caretOffset);

                resultSet.addAllItems(javaCompletionItems);
                resultSet.finish();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            } finally{
                if (fileDummyJava != null){
                    try{
                        fileDummyJava.delete();

                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class FakedJavaClass{
        int offset;
        String body;

        FakedJavaClass(String body, int offset){
            this.offset = offset;
            this.body = body;
        }

        String getBody(){
            return body;
        }

        int getOffset(){
            return offset;
        }
    }

    static class ImportCompletionQuery extends DelegatedQuery {
        public ImportCompletionQuery(JTextComponent component, int queryType){
            super(component, queryType);
        }

        private TokenItem findFirstOnTheLeft(TokenItem tknStart, TokenID tknID){
            TokenItem tkn = tknStart;

            while (tkn != null && tkn.getTokenID() != tknID){
                tkn = tkn.getPrevious();
            }

            return tkn;
        }

        // TODO: lexerize me
        private String getImport(int caretOffset) throws BadLocationException {
            JspSyntaxSupport sup = (JspSyntaxSupport)Utilities.getSyntaxSupport(component);
            TokenItem tokenAtCaretPos = sup.getTokenChain(caretOffset, caretOffset + 1);

            if (tokenAtCaretPos == null || 
                    tokenAtCaretPos.getTokenID() != JspDirectiveTokenContext.ATTR_VALUE){
                
                return null;
            }

            TokenItem tknImportAttr = findFirstOnTheLeft(tokenAtCaretPos, JspDirectiveTokenContext.ATTRIBUTE);

            if (tknImportAttr == null || !("import".equalsIgnoreCase(tknImportAttr.getImage()) //NOI18N
                || "extends".equalsIgnoreCase(tknImportAttr.getImage()))){ //NOI18N
                return null;
            }

            TokenItem tknPageTag = findFirstOnTheLeft(tknImportAttr, JspDirectiveTokenContext.TAG);

            if (tknPageTag == null || !"page".equalsIgnoreCase(tknPageTag.getImage())){ //NOI18N
                return null;
            }

            String imprt = tokenAtCaretPos.getImage().trim();

            // trim quotes

            if (!(imprt.startsWith("\"") && imprt.endsWith("\"") && imprt.length() > 1)){
                return null;
            }

            return imprt.substring(1, imprt.length() - 1);
        }

        protected FakedJavaClass getFakedJavaClass(Document doc, int caretOffset){
            try {
                String prefix = getImport(caretOffset);

                if (prefix != null){
                    String fakedClassBody = "import " + prefix; //NOI18N
                    FakedJavaClass fakedJavaClass = new FakedJavaClass(fakedClassBody, fakedClassBody.length());

                    return fakedJavaClass;
                }
            } catch (BadLocationException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            };

            return null;
        }

    }

    static class EmbeddedJavaCompletionQuery extends DelegatedQuery {
        public EmbeddedJavaCompletionQuery(JTextComponent component, int queryType){
            super(component, queryType);
        }

        protected FakedJavaClass getFakedJavaClass(Document doc, int caretOffset){
            JspSyntaxSupport sup = (JspSyntaxSupport)Utilities.getSyntaxSupport(component);
            VirtualJavaFromJSPCreator javaCreator = new VirtualJavaFromJSPCreator(sup, doc);

            try{
                javaCreator.process(caretOffset);
                String dummyJavaClass = javaCreator.getDummyJavaClassBody();

                FakedJavaClass fakedJavaClass = new FakedJavaClass(dummyJavaClass, javaCreator.getShiftedOffset());

                return fakedJavaClass;
            } catch (BadLocationException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            };

            return null;
        }
    }

}

