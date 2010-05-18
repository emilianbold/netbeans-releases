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

package org.netbeans.modules.bpel.mapper.model.customitems;

import java.util.List;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Literal;
import org.netbeans.modules.bpel.model.api.Literal.LiteralForm;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author AlexanderPermyakov
 */
public class XmlLiteralUtils {

    private DefaultValidator mValidator;

    public static XmlLiteralInfo calculateLiteralInfo(Literal mXmlLiteral) {
        if (mXmlLiteral == null) {
            return null;
        }
        //
        String textContent = mXmlLiteral.getContent();
        LiteralForm literalForm = null;
        //
        boolean multiForm = false;
        boolean multiNode = false;
        boolean unsupportedNode = false;
        Element frstElement = null;
        //
        //
        List<BpelEntity> bpelChildren = mXmlLiteral.getChildren();
        //
        NodeList xmlNodesList = mXmlLiteral.getPeer().getChildNodes();

        int subnodesCount = xmlNodesList.getLength();
        if (subnodesCount == 0) {
            // It's Ok.
            if (literalForm == null) {
                // There isn't neither text content nor any xml subelements.
                literalForm = LiteralForm.EMPTY;
            }
        } else {
            if (literalForm != null) {
                multiForm = true;
            } else {
                if (subnodesCount > 0) {
                    for (int i = 0; i < subnodesCount; i++) {
                        Node xmlNode = xmlNodesList.item(i);
                        if (xmlNode instanceof CDATASection) {
                            literalForm = LiteralForm.CDATA_SUBELEMENT;
                            break;
                        } else if (xmlNode instanceof Element) {
                            literalForm = LiteralForm.SUBELEMENT;
                            frstElement = (Element) xmlNode;
                            break;
                        }
                    }
//                    else {
//                        unsupportedNode = true;
//                    }
                } else {
                    // Error
                    // Only one subnode allowed here.
                    multiNode = true;
                }
            }
        }
        if (literalForm == null && textContent != null && textContent.length() > 0) {
            literalForm = LiteralForm.TEXT_CONTENT;
        }
        switch (literalForm) {
            case EMPTY:
                // do nothing
                break;
            case CDATA_SUBELEMENT:
                textContent = mXmlLiteral.getCDataContent();
//                textContent = ((AbstractDocumentComponent) mXmlLiteral).getModel().
//                        getAccess().getXmlFragment(mXmlLiteral.getPeer());
                break;
            case TEXT_CONTENT:
                break;
            case SUBELEMENT:
                textContent = ((AbstractDocumentComponent) mXmlLiteral).getModel().
                        getAccess().getXmlFragment(mXmlLiteral.getPeer());

//                while (textContent.startsWith(" ") || textContent.startsWith("\n")) {
//                    textContent = textContent.substring(1);
//                }
//                while (textContent.endsWith(" ") || textContent.endsWith("\n")) {
//                    textContent = textContent.substring(0, textContent.length() - 1);
//                }
                break;
        }

        //
//        if (validator != null) {
//            if (multiForm) {
//                validator.addReasonKey(Severity.ERROR, "LITERAL_MULTIFORM_ERROR");
//            }
//            if (multiNode) {
//                validator.addReasonKey(Severity.ERROR, "LITERAL_MULTINODE_ERROR");
//            }
//            if (unsupportedNode) {
//                validator.addReasonKey(Severity.ERROR, "LITERAL_UNSUPPORTED_NODE_ERROR");
//            }
//        }
        //
        
        return new XmlLiteralInfo(literalForm, frstElement, textContent);
    }
//            public DefaultValidator getValidator() {
//        if (mValidator == null) {
//            mValidator = new DefaultValidator(
//                    (ValidStateManager.Provider)XmlLiteralEditor.this,
//                    XmlLiteralEditor.class) {
//
//                public void doFastValidation() {
//                }
//
//                @Override
//                public void doDetailedValidation() {
//                }
//            };
//        }
//        return mValidator;
//    }
    public static class XmlLiteralInfo {
        private LiteralForm literalForm;
        private Element firstElement;
        private String textValue;

        public XmlLiteralInfo(LiteralForm literalForm, Element firstElement, String value) {
            this.literalForm = literalForm;
            this.firstElement = firstElement;
            textValue = value;
        }

        public Element getFirstElement() {
            return firstElement;
        }

        public LiteralForm getLiteralForm() {
            return literalForm;
        }

        public String getTextValue() {
            return textValue;
        }
    }
}
