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

package org.netbeans.modules.xslt.tmap.model.api.completion;

import java.net.URI;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.netbeans.modules.xslt.tmap.TMapDataEditorSupport;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.completion.handler.TMapCompletionHandler;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Alex Petrov (26.06.2008)
 */
public class TMapCompletionUtil implements TMapCompletionConstants {
    private static enum AttributeNameParsingState {
        START_PARSING, LEFT_QUOTE_FOUND, EQUAL_SIGN_FOUND, CHAR_OF_NAME_FOUND
    };
    
    public static String getAttributeNameBeforeCaret(Document document, int caretOffset, 
        SyntaxElement surroundTag) {
        if ((document == null) || (surroundTag == null) || (caretOffset < 0)) 
            return null;
        
        int startTagPos = surroundTag.getElementOffset();
        if (startTagPos < 0) return null;
        try {
            StringBuffer attributeNameBuf = new StringBuffer();
            getAttributeNameBeforeCaret(attributeNameBuf, document, startTagPos, 
                caretOffset, AttributeNameParsingState.START_PARSING);
            return (attributeNameBuf.length() < 1 ? null : attributeNameBuf.toString());
        } catch (Exception e) {
            Logger.getLogger(TMapCompletionUtil.class.getName()).log(Level.INFO, 
                e.getMessage(), e);
            return null;
        }
    }
    
    private static void getAttributeNameBeforeCaret(StringBuffer attributeNameBuf, 
        Document document, int startTagPos, int currentPos, 
        AttributeNameParsingState parsingState) throws Exception {
        while (currentPos > startTagPos) {
            --currentPos;
            if (currentPos <= startTagPos) break;

            String text = document.getText(currentPos, 1);
            char lastChar = text.charAt(0);
        
            if (parsingState.equals(AttributeNameParsingState.START_PARSING)) {
                if (lastChar == '"') {
                    parsingState = AttributeNameParsingState.LEFT_QUOTE_FOUND;
                }
            } else if (parsingState.equals(AttributeNameParsingState.LEFT_QUOTE_FOUND)) {
                if (lastChar == '=') {
                    parsingState = AttributeNameParsingState.EQUAL_SIGN_FOUND;
                } else if (! Character.isSpaceChar(lastChar)) {
                    break;
                }
            } else if (parsingState.equals(AttributeNameParsingState.EQUAL_SIGN_FOUND)) {
                if (! Character.isSpaceChar(lastChar)) {
                    parsingState = AttributeNameParsingState.CHAR_OF_NAME_FOUND;
                    attributeNameBuf.insert(0, text);
                }
            } else if (parsingState.equals(AttributeNameParsingState.CHAR_OF_NAME_FOUND)) {
                if (! Character.isSpaceChar(lastChar)) {
                    attributeNameBuf.insert(0, text);
                } else {
                    break;
                }
            }
        }
    } 
    
    public static String valueofAttribute(Tag surroundTag, 
        String requiredAttributeName) {
        NamedNodeMap attributes = surroundTag.getAttributes();
        if (attributes == null) return null;
        
        for (int i = 0; i < attributes.getLength(); ++i) {
            Node attribute = attributes.item(i);
            String attrName = attribute.getNodeName();
            if ((attrName != null) && 
                (attrName.equalsIgnoreCase(requiredAttributeName))) {
                String attrValue = attribute.getNodeValue();
                return attrValue;
            }
        }
        return null;
    }
    
    public static String ignoreNamespace(String dataWithNamespace) {
        if (dataWithNamespace == null) return null;
        int index = dataWithNamespace.indexOf(":");
        if ((index > -1) && (index < dataWithNamespace.length() - 1)) {
            return dataWithNamespace.substring(index + 1);
        }
        return dataWithNamespace;
    }
    
    public static TMapDataEditorSupport getTMapDataEditorSupport() {
        try {
            TopComponent topComponent = TopComponent.getRegistry().getActivated();
            TMapDataEditorSupport editorSupport = topComponent.getLookup().lookup(
                TMapDataEditorSupport.class);
            return editorSupport;
        } catch(Exception e) {
            Logger logger = Logger.getLogger(TMapCompletionUtil.class.getName());
            logger.log(Level.INFO, null, e);
            return null;
        }
    }
    
    public static TMapModel getTMapModel() {
        TMapDataEditorSupport editorSupport = getTMapDataEditorSupport();
        if (editorSupport == null) return null;
        TMapModel tmapModel = editorSupport.getTMapModel();
        return tmapModel;
    }
    
    /**
     * Returns WSDL model related to 
     * @param tmapModel TransformMap object model
     * @param wsdlLocation location URI of imported WSDL document
     * @param checkWsdlModelState if true, the created WSDL model will be checked:
     *                            whether it's well-formed & valid or not
     * @return created WSDL object model or null, if any problem happens
     */
    public static WSDLModel getWsdlModel(TMapModel tmapModel, String wsdlLocation, 
        boolean checkWsdlModelState) {
        if ((tmapModel == null) || (wsdlLocation == null)) {
            return null;
        }
        WSDLModel wsdlModel;
        try {
            CatalogModelFactory catalogModelFactory = CatalogModelFactory.getDefault();
            ModelSource modelSource = tmapModel.getModelSource();
            
            CatalogModel catalogModel = catalogModelFactory.getCatalogModel(modelSource);
            
            URI uriWsdlLocation = new URI(wsdlLocation);
            modelSource = catalogModel.getModelSource(uriWsdlLocation, modelSource);    
                
            wsdlModel = WSDLModelFactory.getDefault().getModel(modelSource);
        } catch(Exception e) { // catch(URISyntaxException e), catch(CatalogModelException e)
            Logger.getLogger(TMapCompletionUtil.class.getName()).log(Level.INFO, 
                e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
            return null;
        }
        if ((wsdlModel != null) && (checkWsdlModelState) && 
            (! wsdlModel.getState().equals(Model.State.NOT_WELL_FORMED))) {
            return null;
        }
        return wsdlModel;
    }
    
    public static void logExceptionInfo(Exception e) {logExceptionInfo(e, Level.INFO);}
    public static void logExceptionWarning(Exception e) {logExceptionInfo(e, Level.WARNING);}
    
    private static void logExceptionInfo(Exception e, Level level) {
        if (e == null) return;
        Logger.getLogger(TMapCompletionUtil.class.getName()).log(level, 
            e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
    }
    
    public static void showMsgParentTagNotFound(String parentTagName) {
        String errMsg = MessageFormat.format(NbBundle.getMessage(
            TMapCompletionHandler.class, "ERR_MSG_PARENT_TAG_NOT_FOUND"), 
            new Object[] {parentTagName});
        UserNotification.showMessage(errMsg);
    }
    
    public static void showMsgAttributeNotDefined(String tagName, String attributeName) {
        String errMsg = MessageFormat.format(NbBundle.getMessage(
            TMapCompletionHandler.class, "ERR_MSG_ATTRIBUTE_VALUE_OF_TAG_NOT_DEFINED"), 
            new Object[] {tagName, attributeName});
        UserNotification.showMessage(errMsg);
    }
    
    public static Tag getParentTag(Tag currentTag, String requiredParentTagName) {
        if ((currentTag == null) || (requiredParentTagName == null)) return null;
        
        Node parentTag = currentTag;
        while (parentTag != null) {
            parentTag = parentTag.getParentNode();
            if (parentTag instanceof Tag) { // null isn't instance of any class
                String parentTagName = ((Tag) parentTag).getTagName();
                if (parentTagName.contains(requiredParentTagName))
                    return ((Tag) parentTag);
            }
        }
        return null; // parent tag hasn't been found
    }
}