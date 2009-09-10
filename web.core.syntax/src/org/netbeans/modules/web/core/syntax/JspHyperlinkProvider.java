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

package org.netbeans.modules.web.core.syntax;


import java.awt.Toolkit;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.web.core.syntax.completion.api.ELExpression;
import org.netbeans.modules.web.core.syntax.completion.JspELExpression;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 * @author Marek.Fukala@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class JspHyperlinkProvider implements HyperlinkProvider {
    private static final Logger logger = Logger.getLogger(JspHyperlinkProvider.class.getName());
    /**
     * Should determine whether there should be a hyperlink on the given offset
     * in the given document. May be called any number of times for given parameters.
     * <br>
     * This method is called from event dispatch thread.
     * It should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @return true if the provided offset should be in a hyperlink
     *         false otherwise
     */
    public boolean isHyperlinkPoint(Document doc, int offset){
        if (!(doc instanceof BaseDocument))
            return false;
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc) {
                return false;
            }
            
            JspSyntaxSupport jspSup = JspSyntaxSupport.get(bdoc);
            
            TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(bdoc);
            TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
            tokenSequence.move(offset);
            if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
                return false; //no token found
            }
            Token<?> token = tokenSequence.token();
            
            if (token.id() == JspTokenId.ATTR_VALUE){
                SyntaxElement syntaxElement = jspSup.getElementChain(offset);
                if(syntaxElement != null) {
                    if(syntaxElement.getCompletionContext() == 
                        JspSyntaxSupport.DIRECTIVE_COMPLETION_CONTEXT) 
                    {
                        // <%@include file="xxx"%> hyperlink usecase
                        SyntaxElement.Directive sed = (SyntaxElement.Directive)syntaxElement;
                        if("include".equals(sed.getName())) {
                            return containsAttribute(tokenSequence, "file");
                        } else if("page".equals(sed.getName())) {
                            return containsAttribute(tokenSequence, "errorPage");
                        }
                    }
                    if(syntaxElement.getCompletionContext() == 
                        JspSyntaxSupport.TAG_COMPLETION_CONTEXT) 
                    {
                        //find attribute name
                        while (tokenSequence.movePrevious() && 
                                tokenSequence.token().id() != JspTokenId.TAG) 
                        {
                            if(tokenSequence.token().id() == JspTokenId.ATTRIBUTE) {
                                String attributeName = tokenSequence.token().
                                    text().toString();
                                String tagName = ((SyntaxElement.Tag)syntaxElement).
                                    getName();

                                if ("jsp:include".equals(tagName) && 
                                        "page".equals(attributeName)) 
                                {
                                    //<jsp:include page="xxx"/> usecase
                                    return true;
                                }
                                if ("jsp:forward".equals(tagName) && 
                                        "page".equals(attributeName)) 
                                {
                                    //<jsp:forward page="xxx"/> usecase
                                    return true;
                                }
                                if ("jsp:useBean".equals(tagName)
                                        && ("type".equals(attributeName) || 
                                                "class".equals(attributeName))) 
                                {
                                    //<jsp:useBean class="xxx" type="yyy"/> usecase
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            
            // is it a bean in EL?
            tokenSequence.move(offset); //reset tokenSequence
            if(!tokenSequence.moveNext()) {
                return false; //no token
            }
            TokenSequence<ELTokenId> elTokenSequence = 
                tokenSequence.embedded(ELTokenId.language());
            if (elTokenSequence != null){
                JspELExpression exp = new JspELExpression(jspSup);
                elTokenSequence.move(offset);
                if(!elTokenSequence.moveNext()) {
                    return false; //no token
                }
                
                if (elTokenSequence.token().id() == ELTokenId.DOT){
                    return false;
                }
                
                int endOfEL = elTokenSequence.offset() + elTokenSequence.token().length();
                int res = exp.parse(endOfEL);
                if (res == ELExpression.EL_START) {
                    res = exp.parse(endOfEL + 1);
                }
                return res == ELExpression.EL_BEAN;
            }
            // is the a reachable tag file?
            return (canBeTagFile(tokenSequence, jspSup));
            
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
            return false;
        }
    }
    
    private boolean containsAttribute(TokenSequence<?> tokenSequence, 
            String attributeName) 
    {
        //find attribute name
        while (tokenSequence.movePrevious() && tokenSequence.token().id() != JspTokenId.TAG) {
            if (tokenSequence.token().id() == JspTokenId.ATTRIBUTE) {
                if (tokenSequence.token().text().toString().equals(attributeName)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Should determine the span of hyperlink on given offset. Generally, if
     * isHyperlinkPoint returns true for a given parameters, this class should
     * return a valid span, but it is not strictly required.
     * <br>
     * This method is called from event dispatch thread.
     * This method should run very fast as it is called very often.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     * @return a two member array which contains starting and ending offset of a hyperlink
     *         that should be on a given offset
     */
    public int[] getHyperlinkSpan(Document doc, int offset){
        if (!(doc instanceof BaseDocument)) {
            return null;
        }
        
        BaseDocument bdoc = (BaseDocument) doc;
        JTextComponent target = Utilities.getFocusedComponent();
        
        if (target == null || target.getDocument() != bdoc)
            return null;
        
        JspSyntaxSupport jspSup = JspSyntaxSupport.get(bdoc);
        
        TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(bdoc);
        TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
        tokenSequence.move(offset);
        if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
            return null; //no token found
        }
        Token<?> token = tokenSequence.token();
        
        if (canBeTagFile(tokenSequence, jspSup)){
            // a reachable tag file.
            int start = token.offset(tokenHierarchy);
            int end = token.offset(tokenHierarchy) + token.length();
            String text = token.text().toString().trim();
            if (text.startsWith("<")) {
                start = start + 1;
            }
            return new int[]{start, end};
        } else{
            // is it a bean in EL ?
            TokenSequence<ELTokenId> elTokenSequence = tokenSequence.embedded(
                    ELTokenId.language());
            
            if (elTokenSequence != null){
                JspELExpression exp = new JspELExpression(jspSup);
                elTokenSequence.move(offset);
                if(!elTokenSequence.moveNext()) {
                    return null; //no token
                }
                
                int elEnd = elTokenSequence.offset() + elTokenSequence.token().length();
                int res = exp.parse(elEnd);
                
                if (res == ELExpression.EL_BEAN || res == ELExpression.EL_START ){
                    return new int[] {elTokenSequence.offset(), elEnd};
                }
            }

            //the token image always contains the quotation marks e.g. "test.css"
            if(token.length() > 2) {
                //there is somethin between the qutation marks
                return new int[]{token.offset(tokenHierarchy) + 1, token.offset(tokenHierarchy) + token.length() - 1};
            } else {
                //empty value
                return null;
            }
        }
        
    }
    
    /**
     * The implementor should perform an action
     * corresponding to clicking on the hyperlink on the given offset. The
     * nature of the action is given by the nature of given hyperlink, but
     * generally should open some resource or move cursor
     * to certain place in the current document.
     *
     * @param doc document on which to operate.
     * @param offset &gt;=0 offset to test (it generally should be offset &lt; doc.getLength(), but
     *               the implementations should not depend on it)
     */
    public void performClickAction(final Document doc, final int offset){
        doc.render(new Runnable() {
            public void run() {        
                try {
                    BaseDocument bdoc = (BaseDocument) doc;

                    JTextComponent target = Utilities.getFocusedComponent();

                    if (target == null || target.getDocument() != bdoc) {
                        return;
                    }

                    JspSyntaxSupport jspSup = JspSyntaxSupport.get(bdoc);

                    TokenHierarchy<BaseDocument> tokenHierarchy = 
                        TokenHierarchy.get(bdoc);
                    TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
                    tokenSequence.move(offset);
                    if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
                        return; //no token found
                    }
                    Token<?> token = tokenSequence.token();

                    // is it a bean in EL
                    TokenSequence<ELTokenId> elTokenSequence = 
                        tokenSequence.embedded(ELTokenId.language());
                    if (elTokenSequence != null){
                        JspELExpression exp = new JspELExpression(jspSup);

                        elTokenSequence.move(offset);
                        if(!elTokenSequence.moveNext()) {
                            return ;//not token
                        }

                        int elEnd = elTokenSequence.offset() + 
                            elTokenSequence.token().length();
                        int res = exp.parse(elEnd);
                        if (res == ELExpression.EL_START ){
                            navigateToUserBeanDef(bdoc, jspSup, target, 
                                    elTokenSequence.token().text().toString());
                            return;
                        }
                        if (res == ELExpression.EL_BEAN){
                            if (!exp.gotoPropertyDeclaration(exp.getObjectClass())){
                                gotoSourceFailed();
                            }
                        }
                        return;
                    }

                    // is ti declaration of userBean?
                    while (tokenSequence.token().id() != JspTokenId.TAG && 
                            !"jsp:useBean".equals(tokenSequence.token().text().toString()) 
                            && tokenSequence.movePrevious());

                    if (tokenSequence.index() != -1 && tokenSequence.token().id() 
                            == JspTokenId.TAG)
                    {
                        //we are in useBean
                        String className = token.text().toString().substring(1, 
                                token.length()-1).trim();

                        GoToTypeDefTask gotoTask = new GoToTypeDefTask(className);

                        ClasspathInfo cpInfo = ClasspathInfo.create(jspSup.getFileObject());
                        JavaSource source = JavaSource.create(cpInfo, Collections.EMPTY_LIST);

                        try{
                            source.runUserActionTask(gotoTask, true);
                        } catch (IOException e){
                            logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                    }

                    tokenSequence.move(offset);//reset tokenSequence
                    if(!tokenSequence.moveNext()) {
                        return ; //no token
                    }

                    FileObject fObj = getTagFile(tokenSequence, jspSup);
                    if ( fObj != null)
                        openInEditor(fObj);
                    else {
                        String path = token.text().toString();
                        path = path.substring(path.indexOf('"') +1);
                        path = path.substring(0, path.indexOf('"'));

                        fObj = JspUtils.getFileObject(bdoc, path);
                        if (fObj != null) {
                            openInEditor(fObj);
                        } else {
                            // when the file was not found.
                            String msg = NbBundle.getMessage(JspHyperlinkProvider.class, 
                                    "LBL_file_not_found", path); //NOI18N
                            StatusDisplayer.getDefault().setStatusText(msg);
                        }

                    }
                } catch (BadLocationException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }
        
    private String getTagName(String tagwithprefix){
        int index = tagwithprefix.indexOf(':');
        if (index > 0)
            return tagwithprefix.substring(index+1);
        else
            return tagwithprefix;
    }
    
    private void openInEditor(FileObject fObj){
        if (fObj != null){
            DataObject dobj = null;
            try{
                dobj = DataObject.find(fObj);
            } catch (DataObjectNotFoundException e){
                Exceptions.printStackTrace(e);
                return;
            }
            if (dobj != null){
                Node.Cookie cookie = dobj.getCookie(EditCookie.class);
                if (cookie != null)
                    ((EditCookie)cookie).edit();
            }
        }
    }
    
    private boolean canBeTagFile(TokenSequence<?> tokenSequence, JspSyntaxSupport jspSup){
        Token token = tokenSequence.token();
        if(token.id() == JspTokenId.TAG) {
            String image = token.text().toString().trim();
            if (image.startsWith("<")) {                                 // NOI18N
                image = image.substring(1).trim();
            }
            if (!image.startsWith("jsp:") && image.indexOf(':') != -1){  // NOI18N
                return true;
            }

        }
        return false;
    }

    private FileObject getTagFile(TokenSequence<?> tokenSequence, JspSyntaxSupport jspSup){
        Token token = tokenSequence.token();
        if(token.id() == JspTokenId.TAG) {
            String image = token.text().toString().trim();
            if (image.startsWith("<")) {                                 // NOI18N
                image = image.substring(1).trim();
            }
            if (!image.startsWith("jsp:") && image.indexOf(':') != -1){  // NOI18N
                List l = jspSup.getTags(image);
                if (l.size() == 1){
                    TagLibraryInfo libInfo = ((TagInfo)l.get(0)).getTagLibrary();
                    if (libInfo != null){
                        TagFileInfo fileInfo = libInfo.getTagFile(getTagName(image));
                        if (fileInfo != null)
                            return JspUtils.getFileObject(jspSup.getDocument(),
                                    fileInfo.getPath());
                    }
                }
            }
        }
        return null;
    }
    
    /* Move the cursor to the user bean definition.
     */
    private void navigateToUserBeanDef(Document doc, JspSyntaxSupport jspSup, 
            JTextComponent target, String bean)
            throws BadLocationException 
    {
        String text = doc.getText(0, doc.getLength());
        int index = text.indexOf(bean);
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        
        while (index > 0){
            tokenSequence.move(index);
            if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
                return; //no token found
            }
            Token token = tokenSequence.token();
            
            if (token.id() == JspTokenId.ATTR_VALUE ){
                
                while (!(token.id() == JspTokenId.ATTRIBUTE
                        && (token.text().toString().equals("class") || 
                                token.text().toString().equals("type")))
                        && !(token.id() == JspTokenId.SYMBOL
                        && token.text().toString().equals("/>")) && tokenSequence.moveNext()) 
                {
                    token = tokenSequence.token();
                }
                
                if(tokenSequence.index() != -1 && token.id() == JspTokenId.SYMBOL) {
                    while (!(token.id() == JspTokenId.ATTRIBUTE
                            && (token.text().toString().equals("class") || 
                                    token.text().toString().equals("type")))
                            && !(token.id() != JspTokenId.SYMBOL
                            && token.text().toString().equals("<")) && tokenSequence.movePrevious()) 
                    {
                        token = tokenSequence.token();
                    }
                }
                
                if (tokenSequence.index() != -1 && token.id() == JspTokenId.ATTRIBUTE){
                    while (token.id() != JspTokenId.ATTR_VALUE && tokenSequence.moveNext()) {
                        token = tokenSequence.token();
                    }
                }
                
                if (tokenSequence.index() != -1 && token.id() == JspTokenId.ATTR_VALUE){
                    target.setCaretPosition(token.offset(tokenHierarchy)+1);
                    break;
                }
            }
            index = text.indexOf(bean, index + bean.length());
        }
    }
    
    private void gotoSourceFailed(){
        String msg = NbBundle.getBundle(JspHyperlinkProvider.class).
            getString("MSG_source_not_found");
        StatusDisplayer.getDefault().setStatusText(msg);
        Toolkit.getDefaultToolkit().beep();
    }
    
    private class GoToTypeDefTask implements CancellableTask<CompilationController>{
        private String className;
        
        GoToTypeDefTask(String className){
            this.className = className;
        }
        
        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(Phase.ELEMENTS_RESOLVED);
            TypeElement type = parameter.getElements().getTypeElement(className);
            
            if (type != null){
                if (!UiUtils.open(parameter.getClasspathInfo(), type)){
                    gotoSourceFailed();
                }
            } else{
                logger.fine("could not resolve " + className); //NOI18N
            }
        }
        
        public void cancel(){};
    }
}
