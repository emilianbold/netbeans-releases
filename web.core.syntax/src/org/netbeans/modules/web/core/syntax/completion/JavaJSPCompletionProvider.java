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
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.java.JavaCompletionProvider;
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
 * and the '<%@page import=... %>' directive
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JavaJSPCompletionProvider implements CompletionProvider {
    private JavaCompletionProvider javaCompletionProvider = new JavaCompletionProvider();
    
    public CompletionTask createTask(int queryType, final JTextComponent component) {
        try {
            JspSyntaxSupport sup = (JspSyntaxSupport)Utilities.getSyntaxSupport(component);
            TokenItem ti = sup.getItemAtOrBefore(component.getCaret().getDot());
            //delegate to java cc provider if the context is really java code
            if(ti != null && ti.getTokenContextPath().contains(JavaTokenContext.contextPath)){
                
                
                if ((queryType & COMPLETION_QUERY_TYPE) != 0){
                    return new AsyncCompletionTask(new ScriptletCompletionQuery(component, queryType), component);
                }
                
                return null;
            }
            
        }catch(BadLocationException ex) {
            ex.printStackTrace();
        }
        
        //not java context => return empty result
        return new AsyncCompletionTask(new EmptyQuery(), component);
    }
    
    private static final class EmptyQuery extends AsyncCompletionQuery {
        public EmptyQuery() {}
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            resultSet.finish();
        }
        
    }
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return javaCompletionProvider.getAutoQueryTypes(component, typedText);
    }
    
    static class ScriptletCompletionQuery extends AsyncCompletionQuery {
        private int queryType;
        private JTextComponent component;
        
        public ScriptletCompletionQuery(JTextComponent component, int queryType){
            this.queryType = queryType;
            this.component = component;
        }
        
        @Override protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                JspSyntaxSupport sup = (JspSyntaxSupport)Utilities.getSyntaxSupport(component);
                VirtualJavaFromJSPCreator javaCreator = new VirtualJavaFromJSPCreator(sup, Utilities.getDocument(component));
                javaCreator.process(component.getSelectionStart());
                String dummyJavaClass = javaCreator.getDummyJavaClassBody();
                
                FileSystem fs = FileUtil.createMemoryFileSystem();
                FileObject fileDummyJava = fs.getRoot().createData("DummyJSP", "java"); //NOI18N
                PrintStream ps = new PrintStream(fileDummyJava.getOutputStream());
                ps.print(dummyJavaClass);
                ps.close();
                
                FileObject jspFile = NbEditorUtilities.getFileObject(doc);
                //Project project = FileOwnerQuery.getOwner(jspFile);
                
                //Sources sources = ProjectUtils.getSources(project);
                //SourceGroup[] sg = sources.getSourceGroups("doc_root"); // doc_root
                //FileObject sourceRoot = sg != null && sg.length > 0 ? sourceRoot = sg[0].getRootFolder() : null;
                
                
                //ClassPathProvider cpProvider = (ClasspathProvider)project.getLookup().lookup(ClassPathProvider.class);
                //cpProvider.findClassPath(fileDummyJava, ClassPath.COMPILE);
                
                ClasspathInfo cpInfo = ClasspathInfo.create(jspFile);
                JavaSource source = JavaSource.create(cpInfo, fileDummyJava);
                int shiftedOffset = javaCreator.getShiftedOffset();
                //System.err.println(javaCreator.getTestSting());
                
                doc.putProperty(JavaSource.class, source);
                      
                List<? extends CompletionItem> javaCompletionItems = JavaCompletionProvider.query(
                        source, queryType, shiftedOffset, caretOffset);
                
                resultSet.addAllItems(javaCompletionItems);
                resultSet.finish();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            catch (BadLocationException ex){
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
        }
    }
}
