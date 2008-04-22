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

package org.netbeans.modules.spring.beans.hyperlink;

import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.spring.beans.BeansAttributes;
import org.netbeans.modules.spring.beans.BeansElements;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils.Public;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils.Static;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;

/**
 * Provides hyperlinking functionality for Spring XML Configuration files
 * 
 * @author Rohan Ranade
 */
public class SpringXMLConfigHyperlinkProvider implements HyperlinkProvider {

    private static final String P_NAMESPACE = "http://www.springframework.org/schema/p"; // NOI18N
    
    private BaseDocument lastDocument;

    private HyperlinkProcessor currentProcessor;

    private Map<String, HyperlinkProcessor> attribValueProcessors = 
            new HashMap<String, HyperlinkProcessor>();
    
    private PHyperlinkProcessor pHyperlinkProcessor = new PHyperlinkProcessor();
    
    public SpringXMLConfigHyperlinkProvider() {
        this.lastDocument = null;
        
        JavaClassHyperlinkProcessor classHyperlinkProcessor = new JavaClassHyperlinkProcessor();
        registerAttribValueHyperlinkPoint(BeansElements.BEAN, BeansAttributes.CLASS, classHyperlinkProcessor);
        
        JavaMethodHyperlinkProcessor methodHyperlinkProcessor 
                = new JavaMethodHyperlinkProcessor(Public.DONT_CARE, Static.NO, 0);
        registerAttribValueHyperlinkPoint(BeansElements.BEAN, BeansAttributes.INIT_METHOD, methodHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(BeansElements.BEAN, BeansAttributes.DESTROY_METHOD, methodHyperlinkProcessor);
        
        FactoryMethodHyperlinkProcessor factoryMethodHyperlinkProcessor 
                = new FactoryMethodHyperlinkProcessor();
        registerAttribValueHyperlinkPoint(BeansElements.BEAN, BeansAttributes.FACTORY_METHOD, factoryMethodHyperlinkProcessor);
        
        methodHyperlinkProcessor 
                = new JavaMethodHyperlinkProcessor(Public.DONT_CARE, Static.NO, 0);
        registerAttribValueHyperlinkPoint(BeansElements.LOOKUP_METHOD, BeansAttributes.NAME, methodHyperlinkProcessor);
        
        methodHyperlinkProcessor 
                = new JavaMethodHyperlinkProcessor(Public.DONT_CARE, Static.NO, -1);
        registerAttribValueHyperlinkPoint(BeansElements.REPLACED_METHOD, BeansAttributes.NAME, methodHyperlinkProcessor);
        
        ResourceHyperlinkProcessor resourceHyperlinkProcessor = new ResourceHyperlinkProcessor();
        registerAttribValueHyperlinkPoint(BeansElements.IMPORT, BeansAttributes.RESOURCE, resourceHyperlinkProcessor);
        
        PropertyHyperlinkProcessor propertyHyperlinkProcessor = new PropertyHyperlinkProcessor();
        registerAttribValueHyperlinkPoint(BeansElements.PROPERTY, BeansAttributes.NAME, propertyHyperlinkProcessor);
        
        BeansRefHyperlinkProcessor beansRefHyperlinkProcessor = new BeansRefHyperlinkProcessor(true);
        registerAttribValueHyperlinkPoint(BeansElements.BEAN, BeansAttributes.FACTORY_BEAN, beansRefHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(BeansElements.BEAN, BeansAttributes.DEPENDS_ON, beansRefHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(BeansElements.BEAN, BeansAttributes.PARENT, beansRefHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(BeansElements.LOOKUP_METHOD, BeansAttributes.BEAN, beansRefHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(BeansElements.REPLACED_METHOD, BeansAttributes.REPLACER, beansRefHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(BeansElements.PROPERTY, BeansAttributes.REF, beansRefHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(BeansElements.ALIAS, BeansAttributes.NAME, beansRefHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(BeansElements.CONSTRUCTOR_ARG, BeansAttributes.REF, beansRefHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(BeansElements.REF, BeansAttributes.BEAN, beansRefHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(BeansElements.IDREF, BeansAttributes.BEAN, beansRefHyperlinkProcessor);
        
        beansRefHyperlinkProcessor = new BeansRefHyperlinkProcessor(false);
        registerAttribValueHyperlinkPoint(BeansElements.IDREF, BeansAttributes.LOCAL, beansRefHyperlinkProcessor);
        registerAttribValueHyperlinkPoint(BeansElements.REF, BeansAttributes.LOCAL, beansRefHyperlinkProcessor);
    }
    
    private void registerAttribValueHyperlinkPoint(String tagName, String attribName, 
            HyperlinkProcessor processor) {
        attribValueProcessors.put(createRegisteredName(tagName, attribName), processor);
    }
    
    public boolean isHyperlinkPoint(Document document, int offset) {
        if (!(document instanceof BaseDocument)) {
            return false;
        }

        BaseDocument doc = (BaseDocument) document;
        if (!(doc.getSyntaxSupport() instanceof XMLSyntaxSupport)) {
            return false;
        }

        HyperlinkEnv env = new HyperlinkEnv(document, offset);
        if(env.getType().isValueHyperlink()) {
            currentProcessor = locateHyperlinkProcessor(env.getTagName(), env.getAttribName(), attribValueProcessors);
            if(currentProcessor == null && isPNamespaceName(env, env.getAttribName())) {
                currentProcessor = pHyperlinkProcessor;
            }
        } else if(env.getType().isAttributeHyperlink()) {
            if (isPNamespaceName(env, env.getAttribName())) {
                currentProcessor = pHyperlinkProcessor;
            } else {
                currentProcessor = null;
            }
        } else {
            currentProcessor = null;
        }
        
        return currentProcessor != null;
    }

    public int[] getHyperlinkSpan(Document document, int offset) {
        if (!(document instanceof BaseDocument)) {
            return null;
        }
        
        if(currentProcessor == null) {
            return null;
        }

        HyperlinkEnv env = new HyperlinkEnv(document, offset);
        return currentProcessor.getSpan(env);
    }

    public void performClickAction(Document document, int offset) {
        HyperlinkEnv env = new HyperlinkEnv(document, offset);
        if(currentProcessor != null) {
            currentProcessor.process(env);
        }
    }
    
    protected String createRegisteredName(String nodeName, String attributeName) {
        StringBuilder builder = new StringBuilder();
        if(StringUtils.hasText(nodeName)) {
            builder.append("/nodeName=");  // NOI18N
            builder.append(nodeName);
        } else {
            builder.append("/nodeName=");  // NOI18N
            builder.append("*");  // NOI18N
        }
        
        if(StringUtils.hasText(attributeName)) {
            builder.append("/attribute="); // NOI18N
            builder.append(attributeName);
        }
        
        return builder.toString();
    }
    
    private HyperlinkProcessor locateHyperlinkProcessor(String nodeName, 
            String attributeName, Map<String, HyperlinkProcessor> processors) {
        String key = createRegisteredName(nodeName, attributeName);
        if(processors.containsKey(key)) {
            return processors.get(key);
        }
               
        key = createRegisteredName("*", attributeName); // NOI18N
        if(processors.containsKey(key)) {
            return processors.get(key);
        }
        
        return null;
    }
    
    private boolean isPNamespaceName(HyperlinkEnv env, String nodeName) {
        String prefix = ContextUtilities.getPrefixFromNodeName(nodeName);
        if (prefix != null) {
            String namespaceUri = env.lookupNamespacePrefix(prefix);
            if (P_NAMESPACE.equals(namespaceUri)) {
                return true;
            }
        }
        return false;
    }
}
