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

import javax.swing.text.Document;
import org.netbeans.modules.spring.beans.hyperlink.BeansContextHyperlinkProvider.Type;
import org.netbeans.modules.xml.text.syntax.dom.Tag;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class HyperlinkEnv {
    
    private Document document;
    private Tag currentTag;
    private String tagName;
    private String attribName;
    private String valueString;
    private Type type;

    /**
     * Holds environment information for a hyperlink situation
     * 
     * @param document document on which hyperlink invoked
     * @param currentTag tag where hyperlink was invoked
     * @param tagName name of the tag
     * @param attribName attribute name
     * @param valueString value of attribute
     * @param type type of hyperlink
     */
    public HyperlinkEnv(Document document, Tag currentTag, String tagName, 
            String attribName, String valueString, Type type) {
        this.document = document;
        this.currentTag = currentTag;
        this.tagName = tagName;
        this.attribName = attribName;
        this.valueString = valueString;
        this.type = type;
    }

    public String getAttribName() {
        return attribName;
    }

    public Tag getCurrentTag() {
        return currentTag;
    }

    public Document getDocument() {
        return document;
    }

    public String getTagName() {
        return tagName;
    }

    public String getValueString() {
        return valueString;
    }

    public Type getType() {
        return type;
    }
}
