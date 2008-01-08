/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.netbeans.modules.spring.beans.editor;

import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class BeansReferenceableElementsLocator implements
        ReferenceableElementsLocator {

    public static final String BEAN_NAME_DELIMITERS = ",; ";

    public Map<String, Node> getReferenceableElements(Document document) {
        Map<String, Node> nodes = new HashMap<String, Node>();
        Tag rootNode = BeansEditorUtils.getDocumentRoot(document);
        NodeList childNodes = rootNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if ("bean".equals(node.getNodeName())) {
                if (BeansEditorUtils.hasAttribute(node, "id")) {
                    nodes.put(BeansEditorUtils.getAttribute(node, "id"), node);
                }
                if (BeansEditorUtils.hasAttribute(node, "name")) {
                    String aliasesString = BeansEditorUtils.getAttribute(node,
                            "name");
                    String[] nameArr = StringUtils.tokenizeToStringArray(
                            aliasesString, BEAN_NAME_DELIMITERS);
                    for (String name : nameArr) {
                        nodes.put(name, node);
                    }
                }
            }
        }
        return nodes;
    }
    

}
