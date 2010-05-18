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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/**
 *  This generated bean class ElementOrTypeOrMessagePartChooser
 *  matches the schema element 'ElementOrTypeOrMessagePartChooser'.
 *  The root bean class is ElementProperties
 *
 *  ===============================================================
 *  Shows a Tree based selector, which shows all the elements/types from Inline/Imported schemas and also the messages from all imported and existing wsdls.
 *                                          elementAttributeName : the attribute on which GlobalElement data type would be set.
 *                                          typeAttributeName : the attribute on which GlobalType data type would be set.
 *                                          messageAttributeName : the attribute on which Message data type would be set.
 *                                          partAttributeName : the attribute on which part would be set.
 *                                          This chooser can select between a GlobalElement or GlobalType or a wsdl Part.
 *                                          
 *                                      
 *  ===============================================================
 *  Generated on Mon Feb 05 17:54:51 PST 2007
 * @Generated
 */

package org.netbeans.modules.xml.wsdl.ui.property.model;

public class ElementOrTypeOrMessagePartChooser {
    public static final String ELEMENTATTRIBUTENAME = "ElementAttributeName";   // NOI18N
    public static final String TYPEATTRIBUTENAME = "TypeAttributeName"; // NOI18N
    public static final String MESSAGEATTRIBUTENAME = "MessageAttributeName";   // NOI18N
    public static final String PARTATTRIBUTENAME = "PartAttributeName"; // NOI18N

    private java.lang.String _ElementAttributeName;
    private java.lang.String _TypeAttributeName;
    private java.lang.String _MessageAttributeName;
    private java.lang.String _PartAttributeName;

    /**
     * Normal starting point constructor.
     */
    public ElementOrTypeOrMessagePartChooser() {
    }

    /**
     * Deep copy
     */
    public ElementOrTypeOrMessagePartChooser(org.netbeans.modules.xml.wsdl.ui.property.model.ElementOrTypeOrMessagePartChooser source) {
        this(source, false);
    }

    /**
     * Deep copy
     * @param justData just copy the XML relevant data
     */
    public ElementOrTypeOrMessagePartChooser(org.netbeans.modules.xml.wsdl.ui.property.model.ElementOrTypeOrMessagePartChooser source, boolean justData) {
        _ElementAttributeName = source._ElementAttributeName;
        _TypeAttributeName = source._TypeAttributeName;
        _MessageAttributeName = source._MessageAttributeName;
        _PartAttributeName = source._PartAttributeName;
    }

    // This attribute is optional
    public void setElementAttributeName(java.lang.String value) {
        _ElementAttributeName = value;
    }

    public java.lang.String getElementAttributeName() {
        return _ElementAttributeName;
    }

    // This attribute is optional
    public void setTypeAttributeName(java.lang.String value) {
        _TypeAttributeName = value;
    }

    public java.lang.String getTypeAttributeName() {
        return _TypeAttributeName;
    }

    // This attribute is optional
    public void setMessageAttributeName(java.lang.String value) {
        _MessageAttributeName = value;
    }

    public java.lang.String getMessageAttributeName() {
        return _MessageAttributeName;
    }

    // This attribute is optional
    public void setPartAttributeName(java.lang.String value) {
        _PartAttributeName = value;
    }

    public java.lang.String getPartAttributeName() {
        return _PartAttributeName;
    }

    public void writeNode(java.io.Writer out) throws java.io.IOException {
        String myName;
        myName = "ElementOrTypeOrMessagePartChooser";
        writeNode(out, myName, ""); // NOI18N
    }

    public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException {
        writeNode(out, nodeName, null, indent, new java.util.HashMap());
    }

    /**
     * It's not recommended to call this method directly.
     */
    public void writeNode(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
        out.write(indent);
        out.write("<");
        if (namespace != null) {
            out.write((String)namespaceMap.get(namespace));
            out.write(":");
        }
        out.write(nodeName);
        writeNodeAttributes(out, nodeName, namespace, indent, namespaceMap);
        writeNodeChildren(out, nodeName, namespace, indent, namespaceMap);
        out.write("/>\n");
    }

    protected void writeNodeAttributes(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
        // elementAttributeName is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
        if (_ElementAttributeName != null) {
            out.write(" elementAttributeName='");
            org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _ElementAttributeName, true);
            out.write("'"); // NOI18N
        }
        // typeAttributeName is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
        if (_TypeAttributeName != null) {
            out.write(" typeAttributeName='");
            org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _TypeAttributeName, true);
            out.write("'"); // NOI18N
        }
        // messageAttributeName is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
        if (_MessageAttributeName != null) {
            out.write(" messageAttributeName='");
            org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _MessageAttributeName, true);
            out.write("'"); // NOI18N
        }
        // partAttributeName is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
        if (_PartAttributeName != null) {
            out.write(" partAttributeName='");
            org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _PartAttributeName, true);
            out.write("'"); // NOI18N
        }
    }

    protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
    }

    public void readNode(org.w3c.dom.Node node) {
        readNode(node, new java.util.HashMap());
    }

    public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
        if (node.hasAttributes()) {
            org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
            org.w3c.dom.Attr attr;
            java.lang.String attrValue;
            boolean firstNamespaceDef = true;
            for (int attrNum = 0; attrNum < attrs.getLength(); ++attrNum) {
                attr = (org.w3c.dom.Attr) attrs.item(attrNum);
                String attrName = attr.getName();
                if (attrName.startsWith("xmlns:")) {
                    if (firstNamespaceDef) {
                        firstNamespaceDef = false;
                        // Dup prefix map, so as to not write over previous values, and to make it easy to clear out our entries.
                        namespacePrefixes = new java.util.HashMap(namespacePrefixes);
                    }
                    String attrNSPrefix = attrName.substring(6, attrName.length());
                    namespacePrefixes.put(attrNSPrefix, attr.getValue());
                }
            }
            readNodeAttributes(node, namespacePrefixes, attrs);
        }
        readNodeChildren(node, namespacePrefixes);
    }

    protected void readNodeAttributes(org.w3c.dom.Node node, java.util.Map namespacePrefixes, org.w3c.dom.NamedNodeMap attrs) {
        org.w3c.dom.Attr attr;
        java.lang.String attrValue;
        attr = (org.w3c.dom.Attr) attrs.getNamedItem("elementAttributeName");
        if (attr != null) {
            attrValue = attr.getValue();
            _ElementAttributeName = attrValue;
        }
        attr = (org.w3c.dom.Attr) attrs.getNamedItem("typeAttributeName");
        if (attr != null) {
            attrValue = attr.getValue();
            _TypeAttributeName = attrValue;
        }
        attr = (org.w3c.dom.Attr) attrs.getNamedItem("messageAttributeName");
        if (attr != null) {
            attrValue = attr.getValue();
            _MessageAttributeName = attrValue;
        }
        attr = (org.w3c.dom.Attr) attrs.getNamedItem("partAttributeName");
        if (attr != null) {
            attrValue = attr.getValue();
            _PartAttributeName = attrValue;
        }
    }

    protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
    }

    public void changePropertyByName(String name, Object value) {
        if (name == null) return;
        name = name.intern();
        if (name == "elementAttributeName")
            setElementAttributeName((java.lang.String)value);
        else if (name == "typeAttributeName")
            setTypeAttributeName((java.lang.String)value);
        else if (name == "messageAttributeName")
            setMessageAttributeName((java.lang.String)value);
        else if (name == "partAttributeName")
            setPartAttributeName((java.lang.String)value);
        else
            throw new IllegalArgumentException(name+" is not a valid property name for ElementOrTypeOrMessagePartChooser");
    }

    public Object fetchPropertyByName(String name) {
        if (name == "elementAttributeName")
            return getElementAttributeName();
        if (name == "typeAttributeName")
            return getTypeAttributeName();
        if (name == "messageAttributeName")
            return getMessageAttributeName();
        if (name == "partAttributeName")
            return getPartAttributeName();
        throw new IllegalArgumentException(name+" is not a valid property name for ElementOrTypeOrMessagePartChooser");
    }

    public String nameSelf() {
        return "ElementOrTypeOrMessagePartChooser";
    }

    public String nameChild(Object childObj) {
        return nameChild(childObj, false, false);
    }

    /**
     * @param childObj  The child object to search for
     * @param returnSchemaName  Whether or not the schema name should be returned or the property name
     * @return null if not found
     */
    public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName) {
        return nameChild(childObj, returnConstName, returnSchemaName, false);
    }

    /**
     * @param childObj  The child object to search for
     * @param returnSchemaName  Whether or not the schema name should be returned or the property name
     * @return null if not found
     */
    public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName) {
        if (childObj instanceof java.lang.String) {
            java.lang.String child = (java.lang.String) childObj;
            if (child == _ElementAttributeName) {
                if (returnConstName) {
                    return ELEMENTATTRIBUTENAME;
                } else if (returnSchemaName) {
                    return "elementAttributeName";
                } else if (returnXPathName) {
                    return "@elementAttributeName";
                } else {
                    return "ElementAttributeName";
                }
            }
            if (child == _TypeAttributeName) {
                if (returnConstName) {
                    return TYPEATTRIBUTENAME;
                } else if (returnSchemaName) {
                    return "typeAttributeName";
                } else if (returnXPathName) {
                    return "@typeAttributeName";
                } else {
                    return "TypeAttributeName";
                }
            }
            if (child == _MessageAttributeName) {
                if (returnConstName) {
                    return MESSAGEATTRIBUTENAME;
                } else if (returnSchemaName) {
                    return "messageAttributeName";
                } else if (returnXPathName) {
                    return "@messageAttributeName";
                } else {
                    return "MessageAttributeName";
                }
            }
            if (child == _PartAttributeName) {
                if (returnConstName) {
                    return PARTATTRIBUTENAME;
                } else if (returnSchemaName) {
                    return "partAttributeName";
                } else if (returnXPathName) {
                    return "@partAttributeName";
                } else {
                    return "PartAttributeName";
                }
            }
        }
        return null;
    }

    /**
     * Return an array of all of the properties that are beans and are set.
     */
    public java.lang.Object[] childBeans(boolean recursive) {
        java.util.List children = new java.util.LinkedList();
        childBeans(recursive, children);
        java.lang.Object[] result = new java.lang.Object[children.size()];
        return (java.lang.Object[]) children.toArray(result);
    }

    /**
     * Put all child beans into the beans list.
     */
    public void childBeans(boolean recursive, java.util.List beans) {
    }

    public boolean equals(Object o) {
        return o instanceof org.netbeans.modules.xml.wsdl.ui.property.model.ElementOrTypeOrMessagePartChooser && equals((org.netbeans.modules.xml.wsdl.ui.property.model.ElementOrTypeOrMessagePartChooser) o);
    }

    public boolean equals(org.netbeans.modules.xml.wsdl.ui.property.model.ElementOrTypeOrMessagePartChooser inst) {
        if (inst == this) {
            return true;
        }
        if (inst == null) {
            return false;
        }
        if (!(_ElementAttributeName == null ? inst._ElementAttributeName == null : _ElementAttributeName.equals(inst._ElementAttributeName))) {
            return false;
        }
        if (!(_TypeAttributeName == null ? inst._TypeAttributeName == null : _TypeAttributeName.equals(inst._TypeAttributeName))) {
            return false;
        }
        if (!(_MessageAttributeName == null ? inst._MessageAttributeName == null : _MessageAttributeName.equals(inst._MessageAttributeName))) {
            return false;
        }
        if (!(_PartAttributeName == null ? inst._PartAttributeName == null : _PartAttributeName.equals(inst._PartAttributeName))) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = 17;
        result = 37*result + (_ElementAttributeName == null ? 0 : _ElementAttributeName.hashCode());
        result = 37*result + (_TypeAttributeName == null ? 0 : _TypeAttributeName.hashCode());
        result = 37*result + (_MessageAttributeName == null ? 0 : _MessageAttributeName.hashCode());
        result = 37*result + (_PartAttributeName == null ? 0 : _PartAttributeName.hashCode());
        return result;
    }

}


/*
        The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://xml.netbeans.org/schema/wsdlui/property"
            xmlns:tns="http://xml.netbeans.org/schema/wsdlui/property"
            elementFormDefault="qualified">
    <xsd:element name="ElementProperties">
        <xsd:annotation>
            <xsd:documentation xml:lang="en-US">Root node for specifying customizers for a element.
                This needs to be on the GlobalElement which would represent the node in the WSDL tree.
            If this is defined in local elements it is ignored.</xsd:documentation>
        </xsd:annotation>
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:PropertyGroup" maxOccurs="unbounded" />
                <xsd:element ref="tns:Property" maxOccurs="unbounded" />
                <xsd:element ref="tns:GroupedProperty" maxOccurs="unbounded" />
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>            
    
    <xsd:element name="PropertyGroup">
        <xsd:annotation>
            <xsd:documentation xml:lang="en-US">Used to create groups in the property sheet. 
                By default, if no groups are defined all the properties will be shown 
                in the default Property sheet called "Properties".
                name : defines the name of the Group.
                groupOrder : defines the order in which the groups will be created. The groupOrder starts with 1.
                isDefault : overrides the default property sheet to be this group rather than "Properties".
                This enables the user to put non-customized properties (which do not have a Property defined in this xml) to go into this property sheet.
                
                
            </xsd:documentation>
        </xsd:annotation>
        <xsd:complexType>
            <xsd:attribute name="name" type="xsd:string" use="required"/>
            <xsd:attribute name="groupOrder" type="xsd:int"/>
            <xsd:attribute name="isDefault" type="xsd:boolean" default="false"/>
        </xsd:complexType>
    </xsd:element>
    <xsd:element name="Property">
        <xsd:annotation>
            <xsd:documentation xml:lang="en-US">Property represents each attribute that would be created for the Node in the wsdleditor tree.
                It defines a way to specify customizers for attributes. 
                There are 3 types of Property customizers:
                SchemaCustomizer : The default Customizer is the SchemaCustomizer, which shows drop downs for enumerations and boolean attributes,
                and String customizer for all other types. So if there is no Property defined for a attribute, it will have 
                SchemaCustomizer.
                BuiltInCustomizer : specifies a way to put already defined customizer to be shown. Examples are part chooser, message chooser etc.
                NewCustomizer : provides a way to create a custom customizer specific to the user requirement.  When using this the developer has
                to implement the SPI org.netbeans.modules.xml.wsdl.ui.spi.WSDLLookupProvider, and add a implementation of 
                org.netbeans.modules.xml.wsdl.ui.spi.NewCustomizerProvider, which will provide the custom Node.Property to be shown in the 
                wsdl editor property sheet.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:complexType>
            <xsd:choice>
                <xsd:element name="SchemaCustomizer"/>
                <xsd:element name="BuiltInCustomizer">
                    <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                        <xsd:choice>
                            <xsd:element name="DependsOnCustomizer">
                                <xsd:annotation>
                                    <xsd:documentation xml:lang="en-US">Use a built-in customizer whose value(s) depend on some other attribute 
                                        of the same element or some other source.                                        
                                    </xsd:documentation>
                                </xsd:annotation>
                                <xsd:complexType>
                                    <xsd:choice>
                                        <xsd:element name="StaticCustomizer">
                                            <xsd:annotation>
                                                <xsd:documentation xml:lang="en-US">dependsOnAttributeName :  the attribute on which the value(s) of the chooser would depend on.
                                                    For example: some elements may have a attribute for message and another for part, and the PartsChooser should show parts from the message that is selected in the message attribute.
                                                    In that the dependsOnAttributeName for PartChooser would be message.
                                                </xsd:documentation>
                                            </xsd:annotation>
                                            <xsd:complexType>
                                                <xsd:attribute name="dependsOnAttributeName" type="xsd:QName"/>
                                            </xsd:complexType>
                                        </xsd:element>
                                        <!--No use case as of yet, xsd:element name="DynamicCustomizer">
                                            <xsd:annotation>
                                                <xsd:documentation xml:lang="en-US">
                                                    
                                                </xsd:documentation>
                                            </xsd:annotation>
                                            <xsd:complexType>
                                                <xsd:attribute name="dependsOnAttributeValueType" type="xsd:string"/>
                                                <xsd:attribute name="attributeValueProviderClass" type="xsd:string"/>
                                            </xsd:complexType>
                                        </xsd:element-->
                                    </xsd:choice>
                                    <xsd:attribute name="name" type="tns:builtInCustomizerTypes"/>
                                </xsd:complexType>
                            </xsd:element>
                            <xsd:element name="SimpleCustomizer">
                                <xsd:annotation>
                                    <xsd:documentation xml:lang="en-US">
                                        Use the builtin chooser that are available (the names are defined under builtInCustomizerTypes simple type as enumerations, 
                                        name: specifies which builtin chooser to use.
                                    </xsd:documentation>
                                </xsd:annotation>
                                <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                                    <xsd:sequence/>
                                    <xsd:attribute name="name" type="tns:builtInCustomizerTypes"/>
                                </xsd:complexType>
                            </xsd:element>
                        </xsd:choice>
                    </xsd:complexType>
                </xsd:element>
                <xsd:element ref="tns:NewCustomizer"/>
            </xsd:choice>
            <xsd:attribute name="attributeName" type="xsd:string" use="required"/>
            <xsd:attribute name="isNameableAttribute" type="xsd:boolean" default="false"/>
            <xsd:attribute name="decoratorAttribute" type="xsd:QName"/>
            <xsd:attribute name="groupName" type="xsd:string"/>
            <xsd:attribute name="propertyOrder" type="xsd:int"/>
        </xsd:complexType>
    </xsd:element>
    <xsd:element name="GroupedProperty">
        <xsd:annotation>
            <xsd:documentation xml:lang="en-US">Some attributes in a element are mutually exclusive, so in the UI, for unambiguous usage, the user may want to add a single property chooser for 2 or more attributes, which will set the appropriate attribute depending on some criteria that the customizer may determine.
                groupedAttributeNames : specify all the mutually exclusive attributes. There will be a single customizer for all these attributes.
                groupName : specifies which PropertyGroup this belongs to.
                propertyOrder : specifies the order in the PropertyGroup where this property would be placed.
                displayName: specifies the Display name of the combined chooser.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:complexType>
            <xsd:choice>
                <xsd:element name="BuiltInCustomizer" >
                    <xsd:annotation>
                        <xsd:documentation xml:lang="en-US">To use pre-built customizers.
                        </xsd:documentation>
                    </xsd:annotation>
                    <xsd:complexType>
                        <xsd:choice>
                            <xsd:element name="ElementOrTypeChooser">
                                <xsd:annotation>
                                    <xsd:documentation xml:lang="en-US">Shows a Tree based selector, which shows all the elements/types from Inline/Imported schemas.
                                        elementAttributeName : the attribute on which GlobalElement data type would be set.
                                        typeAttributeName : the attribute on which GlobalType data type would be set.
                                    </xsd:documentation>
                                </xsd:annotation>
                                <xsd:complexType>
                                    <xsd:attribute name="elementAttributeName" type="xsd:NCName"/>
                                    <xsd:attribute name="typeAttributeName" type="xsd:NCName"/>
                                </xsd:complexType>
                            </xsd:element>
                            <xsd:element name="ElementOrTypeOrMessagePartChooser">
                                <xsd:annotation>
                                    <xsd:documentation xml:lang="en-US">Shows a Tree based selector, which shows all the elements/types from Inline/Imported schemas and also the messages from all imported and existing wsdls.
                                        elementAttributeName : the attribute on which GlobalElement data type would be set.
                                        typeAttributeName : the attribute on which GlobalType data type would be set.
                                        messageAttributeName : the attribute on which Message data type would be set.
                                        partAttributeName : the attribute on which part would be set.
                                        This chooser can select between a GlobalElement or GlobalType or a wsdl Part.
                                        
                                    </xsd:documentation>
                                </xsd:annotation>                                
                                <xsd:complexType>
                                    <xsd:attribute name="elementAttributeName" type="xsd:NCName"/>
                                    <xsd:attribute name="typeAttributeName" type="xsd:NCName"/>
                                    <xsd:attribute name="messageAttributeName" type="xsd:NCName"/>
                                    <xsd:attribute name="partAttributeName" type="xsd:NCName"/>
                                </xsd:complexType>
                            </xsd:element>
                        </xsd:choice>
                    </xsd:complexType>
                </xsd:element>
                <xsd:element ref="tns:NewCustomizer"/>
            </xsd:choice>
            <xsd:attribute name="groupedAttributeNames" type="tns:attributeList" use="required"/>
            <xsd:attribute name="groupName" type="xsd:string"/>
            <xsd:attribute name="propertyOrder" type="xsd:int"/>
            <xsd:attribute name="displayName" type="xsd:NCName" use="required"/>
        </xsd:complexType>
    </xsd:element>
    
    
    <xsd:element name="NewCustomizer">
        <xsd:annotation>
            <xsd:documentation xml:lang="en-US">Provides a way for developer to provide a custom property customizer for the attribute, if the builtin chooser dont satisfy their requirements.
When using this the developer has to implement the SPI org.netbeans.modules.xml.wsdl.ui.spi.WSDLLookupProvider, and add a implementation of org.netbeans.modules.xml.wsdl.ui.spi.NewCustomizerProvider, which will provide the custom Node.Property to be shown in the wsdl editor property sheet.
            </xsd:documentation>
        </xsd:annotation>
    </xsd:element>
    
    
    <xsd:simpleType name="builtInCustomizerTypes">
        <xsd:restriction base="xsd:string">
            <xsd:enumeration value="MessageChooser">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en-US">Shows a drop down of all messages in the current WSDL document and also ones in imported WSDL documents.</xsd:documentation>
                </xsd:annotation>
            </xsd:enumeration>
            <xsd:enumeration value="PartChooser">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en-US">Show a drop down of all parts for a message. By default, the chooser assumes that it is in the binding section under input/output/fault, and shows all the parts for the message selected in the input/output/fault.
    If not, then the dependsOnCustomizer needs to be used to specify the attribute which represents the message, whose parts will be shown</xsd:documentation>
                </xsd:annotation>
            </xsd:enumeration>
            <xsd:enumeration value="PortTypeChooser">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en-US">Show a drop down of all port types in the WSDL Document/Imported WSDL Documents.</xsd:documentation>
                </xsd:annotation>
            </xsd:enumeration>
            <xsd:enumeration value="PartsChooser">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en-US">Show a dialog of all parts for a message, from which multiple parts can be selected. By default, the chooser assumes that it is in the binding section under input/output/fault, and shows all the parts for the message selected in the input/output/fault.
    If not, then the dependsOnCustomizer needs to be used to specify the attribute which represents the message, whose parts will be shown</xsd:documentation>
                </xsd:annotation>
            </xsd:enumeration>
        </xsd:restriction>
    </xsd:simpleType>
    
    <xsd:simpleType name="attributeList">
        <xsd:list itemType="xsd:string"/>
    </xsd:simpleType>
    
</xsd:schema>

*/
