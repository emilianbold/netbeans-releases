/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xslt.tmap.model.api.completion.handler;

import com.sun.org.apache.xml.internal.utils.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.netbeans.modules.xslt.tmap.model.api.WsdlDataHolder;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionResultItem;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionUtil;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapEditorComponentHolder;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.completion.TMapCompletionConstants;
import org.netbeans.modules.xslt.tmap.model.api.completion.exception.PortTypeNotDefinedException;
import org.openide.util.NbBundle;

/**
 * @author Alex Petrov (26.06.2008)
 */
public abstract class BaseCompletionHandler implements TMapCompletionHandler, 
    TMapCompletionConstants {
    protected JEditorPane srcEditorPane;
    protected Document document;
    protected int caretOffset;
    protected TMapModel tmapModel;
    protected Tag surroundTag;
    protected String attributeName;
        
    public List<TMapCompletionResultItem> getResultItemList(
        TMapEditorComponentHolder editorComponentHolder) {
        return Collections.emptyList();
    }

    protected void initHandler(TMapEditorComponentHolder editorComponentHolder) {
        srcEditorPane = null;
        if (editorComponentHolder == null) return;
        srcEditorPane = editorComponentHolder.getEditorComponent();
        if (srcEditorPane == null) return;
        
        document = null;
        document = srcEditorPane.getDocument();
        if (document == null) return;

        caretOffset = 0;
        caretOffset = srcEditorPane.getCaretPosition();
        
        tmapModel = null;
        tmapModel = TMapCompletionUtil.getTMapModel();
        if (tmapModel == null) return;
            
        surroundTag = null;
        surroundTag = findSurroundTag(srcEditorPane);
        if (surroundTag == null) return;

        attributeName = TMapCompletionUtil.getAttributeNameBeforeCaret(
            document, caretOffset, surroundTag);
        if (attributeName == null) return;
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
    
    protected List<TMapCompletionResultItem> getIncorrectDocumentResultItem() {
        TMapCompletionResultItem incorrectDocumentResultItem =
            new TMapCompletionResultItem(NbBundle.getMessage(
                TMapCompletionResultItem.class, "DOCUMENT_IS_NOT_CORRECT"), 
                document, caretOffset, false);
        return new ArrayList<TMapCompletionResultItem>(Arrays.asList(
            new TMapCompletionResultItem[] {incorrectDocumentResultItem}));
    }

    public String getRequiredPortType(Tag requiredTag) {
        if (requiredTag == null) return null;
        try {
            String valuePortType = requiredTag.getAttribute(ATTRIBUTE_NAME_PORTTYPE);
            if ((valuePortType == null) || (valuePortType.length() < 1)) {
                throw new PortTypeNotDefinedException(requiredTag.getTagName());
            }
            return valuePortType;
        } catch(PortTypeNotDefinedException ptnde) {
            UserNotification.showMessage(ptnde.getMessage());
        } catch(Exception e) {
            TMapCompletionUtil.logExceptionWarning(e);
        }
        return null;
    }
     
    public WsdlDataHolder getWsdlHolder(String requiredPortType) {
        if (requiredPortType == null) return null;
 
        String portTypeQNamePrefix = QName.getPrefixPart(requiredPortType);
        if (portTypeQNamePrefix == null) return null;         
        
        List<WsdlDataHolder> wsdlHolders = WsdlDataHolder.getImportedWsdlList(tmapModel);
        WsdlDataHolder wsdlHolder = WsdlDataHolder.findWsdlByQNamePrefix(
            portTypeQNamePrefix, wsdlHolders);
        return wsdlHolder;
    }
}