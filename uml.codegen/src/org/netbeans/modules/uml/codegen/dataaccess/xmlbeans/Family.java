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
 * This generated bean class Family matches the schema element 'family'.
 * The root bean class is TemplateFamilies
 *
 * Generated on Thu Jun 07 12:07:29 PDT 2007
 * @Generated
 */

package org.netbeans.modules.uml.codegen.dataaccess.xmlbeans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Family extends org.netbeans.modules.schema2beans.BaseBean
    implements org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.FamilyInterface, org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.CommonBean
{
    
    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    
    static public final String NAME = "Name";	// NOI18N
    static public final String EXPANDED = "Expanded";	// NOI18N
    static public final String DOMAINOBJECT = "DomainObject";	// NOI18N
    
    public Family()
    {
        this(Common.USE_DEFAULT_VALUES);
    }
    
    public Family(int options)
    {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(1);
        this.createProperty("domainObject", 	// NOI18N
            DOMAINOBJECT,
            Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY,
            DomainObject.class);
        this.createAttribute(DOMAINOBJECT, "name", "Name",
            AttrProp.CDATA | AttrProp.IMPLIED,
            null, null);
        this.initialize(options);
    }
    
    // Setting the default values of the properties
    void initialize(int options)
    {
        
    }
    
    // This attribute is optional
    public void setName(java.lang.String value)
    {
        setAttributeValue(NAME, value);
    }
    
    //
    public java.lang.String getName()
    {
        return getAttributeValue(NAME);
    }
    
    // This attribute is optional
    public void setExpanded(boolean value)
    {
        setAttributeValue(EXPANDED, ""+value);
    }
    
    //
    public boolean isExpanded()
    {
        return (getAttributeValue(EXPANDED) == null) ? false : java.lang.Boolean.valueOf(getAttributeValue(EXPANDED)).booleanValue();
    }
    
    public boolean fetchDefaultExpanded()
    {
        return false;
    }
    
    // This attribute is an array containing at least one element
    public void setDomainObject(int index, DomainObject value)
    {
        this.setValue(DOMAINOBJECT, index, value);
    }
    
    //
    public DomainObject getDomainObject(int index)
    {
        return (DomainObject)this.getValue(DOMAINOBJECT, index);
    }
    
    // Return the number of properties
    public int sizeDomainObject()
    {
        return this.size(DOMAINOBJECT);
    }
    
    // This attribute is an array containing at least one element
    public void setDomainObject(DomainObject[] value)
    {
        this.setValue(DOMAINOBJECT, value);
    }
    
    //
    public DomainObject[] getDomainObject()
    {
        return (DomainObject[])this.getValues(DOMAINOBJECT);
    }
    
    // Add a new element returning its index in the list
    public int addDomainObject(org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.DomainObject value)
    {
        int positionOfNewItem = this.addValue(DOMAINOBJECT, value);
        return positionOfNewItem;
    }
    
    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeDomainObject(org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.DomainObject value)
    {
        return this.removeValue(DOMAINOBJECT, value);
    }
    
    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public DomainObject newDomainObject()
    {
        return new DomainObject();
    }
    
    //
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c)
    {
        comparators.add(c);
    }
    
    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c)
    {
        comparators.remove(c);
    }
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException
    {
    }
    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent)
    {
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("DomainObject["+this.sizeDomainObject()+"]");	// NOI18N
        for(int i=0; i<this.sizeDomainObject(); i++)
        {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (org.netbeans.modules.schema2beans.BaseBean) this.getDomainObject(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(DOMAINOBJECT, i, str, indent);
        }
        
    }
    public String dumpBeanNode()
    {
        StringBuffer str = new StringBuffer();
        str.append("Family\n");	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }

    public DomainObject getDomainByName(String domainName)
    {
        DomainObject[] domainList = getDomainObject();
        DomainObject theDomain = null;
        
        for (DomainObject domain: domainList)
        {
            if (domain.getName().equals(domainName))
            {
                theDomain = domain;
                break;
            }
        }
        
        return theDomain;
    }
    
    public boolean isUniqueDomainName(String domainName)
    {
        return getDomainByName(domainName) == null;
    }


}

// END_NOI18N


/*
                The following schema file has been used for generation:
 
<?xml version="1.0" encoding="UTF-8"?>
 
<!--
The contents of this file are subject to the terms of the Common Development
and Distribution License (the License). You may not use this file except in
compliance with the License.
 
 You can obtain a copy of the License at http://www.netbeans.org/cddl.html
or http://www.netbeans.org/cddl.txt.
 
When distributing Covered Code, include this CDDL Header Notice in each file
and include the License file at http://www.netbeans.org/cddl.txt.
If applicable, add the following below the CDDL Header, with the fields
enclosed by brackets [] replaced by your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"
 
 The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 Microsystems, Inc. All Rights Reserved.
-->
 
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <xsd:element name="templateFamilies">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element name="family" maxOccurs="unbounded">
                    <xsd:complexType>
                        <xsd:sequence>
                            <xsd:element name="domainObject" maxOccurs="unbounded">
                                <xsd:complexType>
                                    <xsd:sequence>
                                        <xsd:element name="modelElement" type="xsd:string"/>
                                        <xsd:element name="stereotype" type="xsd:string"/>
                                        <xsd:element name="description" type="xsd:string"/>
                                        <xsd:element name="template" maxOccurs="unbounded">
                                            <xsd:complexType>
                                                <xsd:sequence>
                                                    <xsd:element name="filenameFormat" type="xsd:string"/>
                                                    <xsd:element name="fileExtension" type="xsd:string"/>
                                                    <xsd:element name="folderPath" type="xsd:string"/>
                                                    <xsd:element name="templateFile" type="xsd:string"/>
                                                </xsd:sequence>
                                            </xsd:complexType>
                                        </xsd:element>
                                    </xsd:sequence>
                                    <xsd:attribute name="name" type="xsd:string"/>
                                </xsd:complexType>
                            </xsd:element>
                        </xsd:sequence>
                        <xsd:attribute name="name" type="xsd:string"/>
                        <xsd:attribute name="expanded" type="xsd:boolean"/>
                    </xsd:complexType>
                </xsd:element>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>
</xsd:schema>
 
 */
