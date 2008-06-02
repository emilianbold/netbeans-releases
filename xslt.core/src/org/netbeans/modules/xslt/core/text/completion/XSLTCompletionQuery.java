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

package org.netbeans.modules.xslt.core.text.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xslt.core.XSLTDataEditorSupport;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.model.spi.XslModelFactory;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * @author Alex Petrov (30.04.2008)
 */
public class XSLTCompletionQuery extends AsyncCompletionQuery implements Runnable {
    private static final String PATTERN_ATTRIB_VALUE_PREFIX = "=\"";
    
    private CompletionResultSet resultSet;
    private int caretOffset;
    private Document document;
    private JEditorPane srcEditorPane;
    
    @Override
    protected void query(CompletionResultSet resultSet, Document document, int caretOffset) {
        this.resultSet = resultSet;
        this.document = document;
        this.caretOffset = caretOffset;
        StyledDocument styledDoc = (StyledDocument) document;
        styledDoc.render(this);
    }
    
    public void run() {
        makeResultSet();
    }
    
    private void makeResultSet() {
        if ((resultSet == null) || (document == null) || 
            (srcEditorPane == null) || (caretOffset < 0)) {
            resultSet.finish();
            return;
        }
        XslModel xslModel = getXslModel(document);

        if (xslModel != null) {
            XslComponent activeXslComponent = findActiveXslComponent(xslModel, document);
            if (XSLTCompletionUtil.attributeValueExpected(document, caretOffset)) {
                String attributeName = XSLTCompletionUtil.extractAttributeName(
                    document, caretOffset, activeXslComponent);
                if (attributeName != null) {
                    CompletionModel completionModel = 
                        XSLTCompletionModelProvider.getCompletionModel();
                    if ((completionModel != null) && 
                        (completionModel.getSchemaModel() != null)){
                        SchemaModel schemaModel = completionModel.getSchemaModel();

                        NamedReferenceable refSchemaComponent = schemaModel.findByNameAndType(
                            activeXslComponent.getPeer().getLocalName(), GlobalElement.class);

                        List children = refSchemaComponent.getChildren();
                        List attributes = XSLTCompletionUtil.collectChildrenOfType(
                            children, Attribute.class);

                        String attrTypeName = XSLTCompletionUtil.getAttributeType(
                            attributes, attributeName);
                        if (attrTypeName != null) {
                            attrTypeName = XSLTCompletionUtil.ignoreNamespace(attrTypeName);
                            refSchemaComponent = schemaModel.findByNameAndType(
                                attrTypeName, GlobalSimpleType.class);

                            children = refSchemaComponent.getChildren();
                            List enumerations = XSLTCompletionUtil.collectChildrenOfType(
                                children, Enumeration.class);

                            if ((enumerations != null) && (! enumerations.isEmpty())) {
                                List<XSLTCompletionResultItem> resultList = 
                                    new ArrayList<XSLTCompletionResultItem>();
                                for (Object objEnum : enumerations) {
                                    String optionName = ((Enumeration) objEnum).getValue();
                                    resultList.add(new XSLTCompletionResultItem(optionName, 
                                        document, caretOffset));
                                }
                                resultSet.addAllItems(resultList);
                            }
                        }
                    }
                }
            }
        }
        resultSet.setAnchorOffset(caretOffset);
        resultSet.finish();
    }

    private XslComponent findActiveXslComponent(XslModel xslModel, Document doc) {
         if (srcEditorPane == null) return null;
         
         int dotPos = caretOffset; // srcEditorPane.getCaret().getDot();
         DocumentComponent docComponent = xslModel.findComponent(dotPos);    
         return ((XslComponent) docComponent);
    }

    @Override
    protected void prepareQuery(JTextComponent component) {
        super.prepareQuery(component);
        srcEditorPane = getXslSourceEditor();
    }
    
    private JEditorPane getXslSourceEditor() {
        TopComponent topComponent = TopComponent.getRegistry().getActivated();
        try {
            XSLTDataEditorSupport editorSupport = topComponent.getLookup().lookup(
                XSLTDataEditorSupport.class);

            JEditorPane[] editorPanes = editorSupport.getOpenedPanes();
            if ((editorPanes == null) || (editorPanes.length < 1)) return null;

            return editorPanes[0];
        } catch(Exception e) {
            Logger logger = Logger.getLogger(XSLTCompletionQuery.class.getName());
            logger.log(Level.INFO, null, e);
            return null;
        }
    }

    private XslModel getXslModel(Document doc) {
        ModelSource modelSource = new ModelSource(Lookups.singleton(doc), false);
        XslModelFactory xslModelFactory = XslModelFactory.XslModelFactoryAccess.getFactory();
        XslModel xslModel = xslModelFactory.getModel(modelSource);
        return xslModel;
    }
}