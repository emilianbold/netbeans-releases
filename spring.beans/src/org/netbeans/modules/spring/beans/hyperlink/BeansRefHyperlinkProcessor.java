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

package org.netbeans.modules.spring.beans.hyperlink;

import org.netbeans.modules.spring.beans.editor.BeansEditorUtils;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.w3c.dom.Node;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class BeansRefHyperlinkProcessor implements HyperlinkProcessor {

    private boolean showExternal;

    public BeansRefHyperlinkProcessor(boolean showExternal) {
        this.showExternal = showExternal;
    }

    public void process(HyperlinkEnv env) {
        Node bean = BeansEditorUtils.getFirstReferenceableNodeById(env.getDocument(), env.getValueString());
        if (bean != null) {
            BeansEditorUtils.openDocumentAtOffset(env.getDocument(), getNodeOffset(bean));
        }
    }
    
    private int getNodeOffset(Node node) {
        int offset = -1;
        
        switch(node.getNodeType()) {
            case Node.ELEMENT_NODE:
                Tag tag = (Tag) node;
                offset = tag.getElementOffset();
                break;
        }
        
        return offset;
    }
}
