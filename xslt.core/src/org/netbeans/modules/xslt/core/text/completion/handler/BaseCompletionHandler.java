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

package org.netbeans.modules.xslt.core.text.completion.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionModelProvider;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionResultItem;
import org.netbeans.modules.xslt.core.text.completion.XSLTCompletionUtil;
import org.netbeans.modules.xslt.core.text.completion.XSLTEditorComponentHolder;
import org.netbeans.modules.xslt.model.XslModel;
import org.openide.util.NbBundle;

/**
 * @author Alex Petrov (06.06.2008)
 */
public abstract class BaseCompletionHandler implements XSLTCompletionHandler {
    protected JEditorPane srcEditorPane;
    protected Document document;
    protected int caretOffset;
    protected XslModel xslModel;
    protected Tag surroundTag;
    protected String attributeName;
    protected SchemaModel schemaModel;
        
    public List<XSLTCompletionResultItem> getResultItemList(
        XSLTEditorComponentHolder editorComponentHolder) {
        return Collections.emptyList();
    }

    protected void initHandler(XSLTEditorComponentHolder editorComponentHolder) {
        srcEditorPane = null;
        if (editorComponentHolder == null) return;
        srcEditorPane = editorComponentHolder.getSourceEditorComponent();
        if (srcEditorPane == null) return;
        
        document = null;
        document = srcEditorPane.getDocument();
        if (document == null) return;

        caretOffset = 0;
        caretOffset = srcEditorPane.getCaretPosition();
        
        xslModel = null;
        xslModel = XSLTCompletionUtil.getXslModel();
        if (xslModel == null) return;
            
        surroundTag = null;
        surroundTag = findSurroundTag(srcEditorPane);
        if (surroundTag == null) return;

        attributeName = XSLTCompletionUtil.getAttributeNameBeforeCaret(
            document, caretOffset, surroundTag);
        if (attributeName == null) return;
            
        CompletionModel completionModel = 
            new XSLTCompletionModelProvider().getCompletionModel();
        if ((completionModel == null) || (completionModel.getSchemaModel() == null)) return;

        schemaModel = null;
        schemaModel = completionModel.getSchemaModel();
    }

    protected Tag findSurroundTag(JEditorPane srcEditorPane) {
        if (srcEditorPane == null) return null;

        XMLSyntaxSupport xmlSyntaxSupport = (XMLSyntaxSupport) ((BaseDocument) 
            document).getSyntaxSupport();
        if (xmlSyntaxSupport == null) return null;
        
        SyntaxElement syntaxElement = null;
        try {
            syntaxElement = xmlSyntaxSupport.getElementChain(caretOffset);
            if (! (syntaxElement instanceof Tag)) return null;
        } catch (Exception e) {
            Logger logger = Logger.getLogger(BaseCompletionHandler.class.getName());
            logger.log(Level.INFO, e.getMessage(), e);
            return null;
        }
        return ((Tag) syntaxElement);
    }
/*    
    protected XslComponent findInclusiveXslComponent(JEditorPane srcEditorPane, 
        XslModel xslModel) {
        if ((srcEditorPane == null) || (xslModel == null)) return null;

        DocumentComponent docComponent = xslModel.findComponent(caretOffset);    
        return ((XslComponent) docComponent);
    }
*/ 
    
    protected List<XSLTCompletionResultItem> getIncorrectDocumentResultItem() {
        XSLTCompletionResultItem incorrectDocumentResultItem =
            new XSLTCompletionResultItem(NbBundle.getMessage(
                XSLTCompletionResultItem.class, "DOCUMENT_IS_NOT_CORRECT"), 
                document, caretOffset, false);
        return new ArrayList<XSLTCompletionResultItem>(Arrays.asList(
            new XSLTCompletionResultItem[] {incorrectDocumentResultItem}));
    }
}