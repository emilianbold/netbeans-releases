/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * DocumentationItem.java
 *
 * Created on June 28, 2006, 10:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.completion;

import java.net.URL;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.util.NbBundle;

/**
 *
 * @author Samaresh
 */
public abstract class DocumentationItem implements CompletionDocumentation {
    
    private CompletionResultItem completionItem;
    
    /**
     * Creates a new instance of DocumentationItem
     */
    public DocumentationItem(CompletionResultItem item) {
        this.completionItem = item;
    }
    
    public static DocumentationItem createDocumentationItem(CompletionResultItem item) {
        if(item.getAXIComponent() instanceof AbstractElement)
            return new ElementDocItem(item);
        if(item.getAXIComponent() instanceof AbstractAttribute)
            return new AttributeDocItem(item);
        
        return null;
    }
                
    public abstract String getText();
    
    public final CompletionResultItem getCompletionItem() {
        return completionItem;
    }
    
    public URL getURL() {
        return null;
    }
    
    public CompletionDocumentation resolveLink(String link) {
        return null;
    }
    
    public Action getGotoSourceAction() {
        return null;
    }
    
    static class ElementDocItem extends DocumentationItem {
                
        public ElementDocItem(CompletionResultItem item) {
            super(item);
        }
        
        public String getText() {
            AXIComponent axiComponent = getCompletionItem().getAXIComponent();
            if(!(axiComponent instanceof AbstractElement))
                return null;
            AbstractElement element = (AbstractElement)axiComponent;
            String[] params = {"","","","",""};
            params[0] = element.getTargetNamespace();
            if(params[0] == null)
                params[0] = NbBundle.getMessage(DocumentationQuery.class,
                    "Documentation-Text-No-TNS");
            params[1] = element.getName();
            params[2] = element.getDocumentation();
            if(params[2] == null)
                params[2] = NbBundle.getMessage(DocumentationQuery.class,
                    "Documentation-Text-Element-No-Description");
            params[3] = formChildElementsHTML(element);
            if(params[3] == null)
                params[3] = NbBundle.getMessage(DocumentationQuery.class,
                    "Documentation-Text-Element-No-Child-Elements");
            params[4] = formAttributesHTML(element);
            if(params[4] == null)
                params[4] = NbBundle.getMessage(DocumentationQuery.class,
                    "Documentation-Text-Element-No-Attributes");
            return NbBundle.getMessage(DocumentationQuery.class,
                    "Documentation-Text-Element", params);
        }
        
        private String formChildElementsHTML(AbstractElement element) {
            List<AbstractElement> children = element.getChildElements();
            if(children == null || children.size() == 0)
                return null;
            StringBuffer buffer = new StringBuffer();
            for(AbstractElement e: children) {
                String min = e.getMinOccurs();
                if(min != null && min.equals("1")) {
                    buffer.append("<b>" + e.getName() + "</b>");
                } else {
                    buffer.append(e.getName());
                }
                buffer.append(" ");
                if(e.supportsCardinality()) {
                    buffer.append("[" + e.getMinOccurs() + ".." + e.getMaxOccurs() + "]");
                }
                if(e instanceof AnyElement) {
                    buffer.append(" ");
                    buffer.append("{" + e.getTargetNamespace() + "}");
                }                
                buffer.append("<br>"); //NOI18N
            }
            
            return buffer.toString();
        }
        
        private String formAttributesHTML(AbstractElement element) {        
            List<AbstractAttribute> attrs = element.getAttributes();
            if(attrs == null || attrs.size() == 0)
                return null;
            StringBuffer buffer = new StringBuffer();
            for(AbstractAttribute attr: attrs) {
                if(attr instanceof Attribute) {
                    Use use = ((Attribute)attr).getUse();
                    if(use != null && use == Use.REQUIRED) //NOI18N
                        buffer.append("<b>" + attr.getName() + "</b>");
                    else
                        buffer.append(attr.getName());
                } else
                    buffer.append(attr.getName());
                
                if(attr instanceof AnyAttribute) {
                    buffer.append(" ");
                    buffer.append("{" + attr.getTargetNamespace() + "}");
                }                
                buffer.append("<br>"); //NOI18N
            }
            return buffer.toString();
        }
        
    }

        
    static class AttributeDocItem extends DocumentationItem {
                
        public AttributeDocItem(CompletionResultItem item) {
            super(item);
        }
        
        public String getText() {
            AXIComponent axiComponent = getCompletionItem().getAXIComponent();
            if(!(axiComponent instanceof AbstractAttribute))
                return null;
            AbstractAttribute attribute = (AbstractAttribute)axiComponent;
            String[] params = {"","","",""};                
            params[0] = attribute.getTargetNamespace();
            if(params[0] == null)
                params[0] = NbBundle.getMessage(DocumentationQuery.class,
                    "Documentation-Text-No-TNS");            
            params[1] = attribute.getName();
            params[2] = attribute.getDocumentation();
            if(params[2] == null)
                params[2] = NbBundle.getMessage(DocumentationQuery.class,
                    "Documentation-Text-Attribute-No-Description");
            if(attribute instanceof Attribute) {
                AXIType type = ((Attribute)attribute).getType();
                if(type instanceof Datatype) {
                    params[3] = ((Datatype)type).getKind().getName();
                }
            }
            return NbBundle.getMessage(DocumentationQuery.class, "Documentation-Text-Attribute", params);
        }                
    }    
    
}
