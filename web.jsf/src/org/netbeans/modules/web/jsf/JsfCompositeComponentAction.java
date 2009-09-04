/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author alexeybutenko
 */
public final class JsfCompositeComponentAction extends CookieAction {

    private String selectedText;
    private static final String TEMPLATES_FOLDER = "JSF";   //NOI18N
    private static final String TEMPLATE_NAME="out.xhtml";  //NOI18N

    protected void performAction(Node[] activatedNodes) {
        EditorCookie editorCookie = activatedNodes[0].getLookup().lookup(EditorCookie.class);
        Document doc = editorCookie.getDocument();
        FileObject fileObject = NbEditorUtilities.getFileObject(doc);
        Project project = FileOwnerQuery.getOwner(fileObject);

        assert project != null;
        if (editorCookie != null) {
            JEditorPane[] panes = editorCookie.getOpenedPanes();
            if (panes.length>0) {
//                int cursor = panes[0].getCaret().getDot();
                String selection = panes[0].getSelectedText();
                int startOffset = panes[0].getSelectionStart();
                int endOffset = panes[0].getSelectionEnd();
                if (selection != null) {
                    this.selectedText = selection;
                    instantiateTemplate(project, doc, startOffset, endOffset);
                }
            }
        }
 
    }

    private void instantiateTemplate(Project project, Document document, final int startOffset, final int endOffset) {
        TemplateWizard templateWizard = new TemplateWizard();
        templateWizard.putProperty("project", project);
        templateWizard.putProperty("selectedText", selectedText);
        templateWizard.setTitle("Insert Composite Component");
        DataFolder templatesFolder = templateWizard.getTemplatesFolder();
        FileObject template = templatesFolder.getPrimaryFile().getFileObject(TEMPLATES_FOLDER+File.separator+TEMPLATE_NAME);
        DataObject templateDO;
        FileObject projectDir = project.getProjectDirectory();
        DataFolder targetFolder = DataFolder.findFolder(projectDir);
//        if (!isValid(document, startOffset, endOffset)) {
//            String errorMessage = "This text selection can not be converted into Composite Component";
//            templateWizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage);
//        }
        try {
            templateDO = DataObject.find(template);
            Set<DataObject> result = templateWizard.instantiate(templateDO, targetFolder);
            if (result != null && result.size()>0) {
                final String compName = result.iterator().next().getName();
                //TODO XXX Replace selected text by created component in editor
                FileObject tF = Templates.getTargetFolder(templateWizard);
                final BaseDocument doc = (BaseDocument) document;
                final Indent indent = Indent.get(doc);
                indent.lock();
                try {
                    doc.runAtomic(new Runnable() {

                        public void run() {
                            try {
                                doc.remove(startOffset, endOffset - startOffset);
                                String text = "<ez:"+compName+">\n     </ez:"+compName+">"; //NOI18N
                                doc.insertString(startOffset, text, null);
                                indent.reindent(startOffset, startOffset+text.length());
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                } finally {
                    indent.unlock();
                }
                String compFolder = tF.getPath();
                compFolder = FileUtil.getRelativePath(projectDir, tF);
                if (compFolder.endsWith(File.separator)) {
                    compFolder = compFolder.substring(0, compFolder.lastIndexOf(File.separator));
                }
                compFolder = compFolder.substring(compFolder.lastIndexOf(File.separator)+1);
                try {
                    importLibrary((BaseDocument) document, startOffset, compFolder);
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        EditorCookie editorCookie = activatedNodes[0].getLookup().lookup(EditorCookie.class);
        Document doc = editorCookie.getDocument();
        if (editorCookie != null) {
            JEditorPane[] panes = editorCookie.getOpenedPanes();
            if (panes.length>0) {
                int cursor = panes[0].getCaret().getDot();
                String selection = panes[0].getSelectedText();
                int startOffset = panes[0].getSelectionStart();
                int endOffset = panes[0].getSelectionEnd();
                if (selection != null && isValid(doc, startOffset, endOffset)) {
                    return true;
                }
            }
        }
        return false;
//        return super.enable(activatedNodes);
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(JsfCompositeComponentAction.class, "CTL_JsfCompositeComponentAction");
    }

    protected Class[] cookieClasses() {
        return new Class[]{EditorCookie.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     * TODO Need to make text selection check here
     * @param doc
     * @param startOffset
     * @param endOffset
     * @return
     */
    protected boolean isValid(Document doc, final int startOffset, final int endOffset) {
        final Source source = Source.create(doc);
//        final AstNode[] htmlRootNode = new AstNode[1];
        final boolean[] isValid = new boolean[1];
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result result = resultIterator.getParserResult(startOffset);
                    if (result.getSnapshot().getMimeType().equals("text/html")) {
                        HtmlParserResult htmlResult = (HtmlParserResult)result;
                        int offset = startOffset;
                        SyntaxElement startEl = null, endEl = null, lastEl = null;
                        for (SyntaxElement element : htmlResult.elementsList()) {
                            if (element.offset()<startOffset && startOffset < element.offset()+element.length()
                                    && (element.type()==SyntaxElement.TYPE_TAG || element.type()==SyntaxElement.TYPE_ENDTAG) ) {
                                break;
                            }
                            if (startEl==null
                                    && element.offset()>=startOffset
                                    && (element.offset()+element.length())<endOffset
                                    && element.type() == SyntaxElement.TYPE_TAG) {
                                startEl = element;
                            }

                            if (element.type() == SyntaxElement.TYPE_ENDTAG && endOffset >= (element.offset()+element.length())) {
                                endEl = element;
                            }
                        }

                        if (startEl != null && endEl!=null 
                                && startEl.type() == SyntaxElement.TYPE_TAG
                                &&  endEl.type()==SyntaxElement.TYPE_ENDTAG
                                && startEl.text().toString().substring(1).startsWith(endEl.text().toString().substring(2, endEl.text().toString().length()-1))) {
                            isValid[0]=true;
                        } else {
                            isValid[0]=false;
                            return;
                        }
                        AstNode beginLeaf = htmlResult.findLeaf(startOffset);
                        AstNode currentNode = beginLeaf;
                        if (currentNode.getMatchingTag() == null) {
                            return;
                        }
                        if (currentNode.needsToHaveMatchingTag() 
                                && ((currentNode.startOffset()>=startOffset && currentNode.getMatchingTag().endOffset() <= endOffset)
                                    ||(currentNode.startOffset()<startOffset && currentNode.getMatchingTag().endOffset()>endOffset))) {
                            isValid[0] = true;
                        }else {
                            isValid[0] = false;
                        }
                    }
                }
            });
            return isValid[0];
        }catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
//        return true;
    }

    protected void importLibrary(final BaseDocument doc, int offset, String resourceFolder) throws ParseException{
        Source source = Source.create(doc);
        final AstNode[] htmlRootNode = new AstNode[1];
        final int substitutionOffset = offset;
        final String compFolder = resourceFolder;
        final String namespace = "http://java.sun.com/jsf/composite/"+compFolder; //NOI18N
        final String nsKey = "xmlns:ez";    //NOI18N

        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                //suppose we are always top level
                Result result = resultIterator.getParserResult(substitutionOffset);
                if (result.getSnapshot().getMimeType().equals("text/html") ) { //NOI18N
                    htmlRootNode[0] = AstNodeUtils.query(((HtmlParserResult)result).root(), "html"); //NOI18N
                }
            }
        });
        //TODO reformat
        //TODO decide whether to add a new line before or not based on other attrs - could be handled by the formatter!?!?!
        if (htmlRootNode[0] != null) {

            final Indent indent = Indent.get(doc);
            indent.lock();
            try {
                doc.runAtomic(new Runnable() {
                    public void run() {
                        try {
                            boolean noAttributes = htmlRootNode[0].getAttributeKeys().isEmpty();
                            boolean addNs = !htmlRootNode[0].getAttributeKeys().contains(nsKey);
                            //if there are no attributes, just add the new one at the end of the tag,
                            //if there are some, add the new one on a new line and reformat the tag
                            if (addNs) {
                                int insertPosition = htmlRootNode[0].endOffset() - 1; //just before the closing symbol
                                String text = (!noAttributes ? "\n" : "") + nsKey+"=\""+namespace+"\""; //NOI18N

                                doc.insertString(insertPosition, text, null);

                                if(!noAttributes) {
                                    //reformat the tag so the new attribute gets aligned with the previous one/s
                                    int newRootNodeEndOffset = htmlRootNode[0].endOffset() + text.length();
                                    indent.reindent(insertPosition, newRootNodeEndOffset);
                                }
                            }
                        } catch (BadLocationException ex) {
                            Logger.global.log(Level.INFO, null, ex);
                        }
                    }
                });
            } finally {
                indent.unlock();
            }
        } else {
            //TODO create the root node???
        }
    }
}

