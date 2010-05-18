/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.xml.xsd;

import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents an XML element
 * @author  anovak
 */
class SchemaElement extends AbstractResultNode implements Element {

    /** namespace */
    protected final String namespaceURI;
    /** qname */
    protected final String qname;
    /** Attributes of this Element */
    protected final Attributes attributes;
    /** Sub elements */
    protected final List subelements;
    
    private String prefix;
    
    /** http://www.w3.org/2001/XMLSchema namespace prefix */
    private String schemaPrefix;

    /** Creates empty element */
    protected SchemaElement() {
        this.namespaceURI = null;
        this.qname = null;
        this.attributes = null;
        this.subelements = null;
        this.schemaPrefix = null;
    }
    
    /** Creates a new instance of Element */
    protected SchemaElement(String namespaceURI, String qname, Attributes attributes, String schemaPrefix) {
        this.namespaceURI = namespaceURI;
        this.qname = qname;
        this.attributes = (attributes == null ? null : new AttributesImpl(attributes));
        this.subelements = new ArrayList();
        this.schemaPrefix = schemaPrefix;
    }
    
    /** Creates a new SchemaElement */
    public static final SchemaElement createSchemaElement(String namespaceURI, String qname, Attributes attributes, String schemaPrefix) {
        String simpleType = null;
        String complexType = null;
        
        if (schemaPrefix == null) {
           simpleType = Type.XS_SIMPLE_TYPE;
           complexType = Type.XS_COMPLEX_TYPE;
        } else {
           simpleType = schemaPrefix + ':' + Type.XS_SIMPLE_TYPE;
           complexType = schemaPrefix + ':' + Type.XS_COMPLEX_TYPE;
        }
        
        if (qname.equalsIgnoreCase(simpleType) || qname.equalsIgnoreCase(complexType)) {
            return new Type(namespaceURI, qname, attributes, schemaPrefix);
        } else {
            return new SchemaElement(namespaceURI, qname, attributes, schemaPrefix);
        }
    }
    
    public final void addSubelement(SchemaElement e) {
        subelements.add(e);
    }
    
    public final java.util.Iterator getSubelements() {
        return subelements.iterator();
    }
    
    public final boolean isComposite() {
        String sequenceToken = getSchemaPrefix() == null ? "sequence" : getSchemaPrefix() + ':' + "sequence";
        return (this instanceof Type) || getQname().equalsIgnoreCase(sequenceToken);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append("SchemaElement ").append(qname);
        
        if (attributes != null) {
            sb.append("Attrs size: ").append(attributes.getLength());
            for (int i = 0; i < attributes.getLength(); i++) {
                sb.append("\n Attr[").append(i).append("] localname: ").
                        append(attributes.getLocalName(i)).
                        append(" qname: ").append(attributes.getQName(i)).
                        append(" value: ").append(attributes.getValue(i)).
                        append(" URI: ").append(attributes.getURI(i)).
                        append(" type: ").append(attributes.getType(i));
            }
        }
        
        return  sb.toString();
    }
    
    /**
     * Getter for property qname.
     * @return Value of property qname.
     */
    public java.lang.String getQname() {
        return qname;
    }
    
    /**
     * Getter for property attributes.
     * @return Value of property attributes.
     */
    public org.xml.sax.Attributes getSAXAttributes() {
        return attributes;
    }

    // org.w3c.Node methods
    
    public short getNodeType() {
        return org.w3c.dom.Node.ELEMENT_NODE;
    }

    public String getNodeName() {
        String name = getSAXAttributes().getValue("name");
        if (prefix != null) {
            name = prefix + ':' + name;
        }
        return name;
    }

    public String getTagName() {
        return this.getNodeName();
    }
    
    public void setPrefix(String str) throws org.w3c.dom.DOMException {
        this.prefix = str;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    /**
     * Getter for property schemaPrefix.
     * @return Value of property schemaPrefix.
     */
    public java.lang.String getSchemaPrefix() {
        return schemaPrefix;
    }
    
    /**
     * Setter for property schemaPrefix.
     * @param schemaPrefix New value of property schemaPrefix.
     */
    public void setSchemaPrefix(java.lang.String schemaPrefix) {
        this.schemaPrefix = schemaPrefix;
    }
    
}
