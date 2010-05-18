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
 *	This generated bean class DomainObject matches the schema element 'domainObject'.
 *  The root bean class is TemplateFamilies
 *
 *	Generated on Thu Jun 07 12:07:29 PDT 2007
 * @Generated
 */

package org.netbeans.modules.uml.codegen.dataaccess.xmlbeans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N
import javax.swing.table.DefaultTableModel;

public class DomainObject extends org.netbeans.modules.schema2beans.BaseBean
    implements org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.DomainObjectInterface, org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.CommonBean
{
    
    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    
    static public final String NAME = "Name";	// NOI18N
    static public final String MODELELEMENT = "ModelElement";	// NOI18N
    static public final String STEREOTYPE = "Stereotype";	// NOI18N
    static public final String DESCRIPTION = "Description";	// NOI18N
    static public final String TEMPLATE = "Template";	// NOI18N
    
    public DomainObject()
    {
        this(Common.USE_DEFAULT_VALUES);
    }
    
    public DomainObject(int options)
    {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(4);
        this.createProperty("modelElement", 	// NOI18N
            MODELELEMENT,
            Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
            java.lang.String.class);
        this.createProperty("stereotype", 	// NOI18N
            STEREOTYPE,
            Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
            java.lang.String.class);
        this.createProperty("description", 	// NOI18N
            DESCRIPTION,
            Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
            java.lang.String.class);
        this.createProperty("template", 	// NOI18N
            TEMPLATE,
            Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY,
            Template.class);
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
    
    // This attribute is mandatory
    public void setModelElement(java.lang.String value)
    {
        this.setValue(MODELELEMENT, value);
    }
    
    //
    public java.lang.String getModelElement()
    {
        return (java.lang.String)this.getValue(MODELELEMENT);
    }
    
    public java.lang.String fetchDefaultModelElement()
    {
        return "";
    }
    
    // This attribute is mandatory
    public void setStereotype(java.lang.String value)
    {
        this.setValue(STEREOTYPE, value);
    }
    
    //
    public java.lang.String getStereotype()
    {
        return (java.lang.String)this.getValue(STEREOTYPE);
    }
    
    public java.lang.String fetchDefaultStereotype()
    {
        return "";
    }
    
    // This attribute is mandatory
    public void setDescription(java.lang.String value)
    {
        this.setValue(DESCRIPTION, value);
    }
    
    //
    public java.lang.String getDescription()
    {
        return (java.lang.String)this.getValue(DESCRIPTION);
    }
    
    public java.lang.String fetchDefaultDescription()
    {
        return "";
    }
    
    // This attribute is an array containing at least one element
    public void setTemplate(int index, Template value)
    {
        this.setValue(TEMPLATE, index, value);
    }
    
    //
    public Template getTemplate(int index)
    {
        return (Template)this.getValue(TEMPLATE, index);
    }
    
    // Return the number of properties
    public int sizeTemplate()
    {
        return this.size(TEMPLATE);
    }
    
    // This attribute is an array containing at least one element
    public void setTemplate(Template[] value)
    {
        this.setValue(TEMPLATE, value);
    }
    
    //
    public Template[] getTemplate()
    {
        return (Template[])this.getValues(TEMPLATE);
    }
    
    // Add a new element returning its index in the list
    public int addTemplate(org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.Template value)
    {
        int positionOfNewItem = this.addValue(TEMPLATE, value);
        return positionOfNewItem;
    }
    
    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeTemplate(org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.Template value)
    {
        return this.removeValue(TEMPLATE, value);
    }
    
    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public Template newTemplate()
    {
        return new Template();
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
        str.append("ModelElement");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getModelElement();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(MODELELEMENT, 0, str, indent);
        
        str.append(indent);
        str.append("Stereotype");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getStereotype();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(STEREOTYPE, 0, str, indent);
        
        str.append(indent);
        str.append("Description");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getDescription();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(DESCRIPTION, 0, str, indent);
        
        str.append(indent);
        str.append("Template["+this.sizeTemplate()+"]");	// NOI18N
        for(int i=0; i<this.sizeTemplate(); i++)
        {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (org.netbeans.modules.schema2beans.BaseBean) this.getTemplate(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(TEMPLATE, i, str, indent);
        }
        
    }
    public String dumpBeanNode()
    {
        StringBuffer str = new StringBuffer();
        str.append("DomainObject\n");	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
    
    
    public String[][] getTemplatesTableData()
    {
        Template[] templateList = getTemplate();
        String[][] data = new String[templateList.length][4];
        int row = 0;
        
        for (Template template: templateList)
        {
            data[row][0] = template.getFilenameFormat();
            data[row][1] = template.getFileExtension();
            data[row][2] = template.getFolderPath();
            data[row][3] = template.getTemplateFile();
            row++;
        }
        
        return data;
    }
    
    
    
    public void updateTemplates(DefaultTableModel templateTable)
    {
        Template[] templates = getTemplate();
        ArrayList<Template> templateList = new ArrayList(templates.length);

        for (Template curtemplate: templates)
            templateList.add(curtemplate);
        
        int listSize = templateList.size();
        int tableSize = templateTable.getRowCount();
        
        if (tableSize == 0)
        {
            setTemplate(new Template[]{});
            return;
        }
        
        Template template = null;
        
        for (int row=0; row < tableSize; row++)
        {
            if (row < listSize)
                template = templateList.get(row);
            
            else
                template = new Template();
            
            template.setFilenameFormat(
                templateTable.getValueAt(row, 0) == null ? null :
                    templateTable.getValueAt(row, 0).toString());
            
            template.setFileExtension(
                templateTable.getValueAt(row, 1) == null ? null :
                    templateTable.getValueAt(row, 1).toString());
            
            template.setFolderPath(
                templateTable.getValueAt(row, 2) == null ? null :
                    templateTable.getValueAt(row, 2).toString());
            
            template.setTemplateFile(
                templateTable.getValueAt(row, 3) == null ? null :
                    templateTable.getValueAt(row, 3).toString());
            
            if (row < listSize)
                templateList.set(row, template);
            
            else
                templateList.add(template);
        }
        
        if (tableSize < listSize)
            templateList.subList(tableSize, listSize).clear();
        
        setTemplate(templateList.toArray(new Template[]{}));
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
