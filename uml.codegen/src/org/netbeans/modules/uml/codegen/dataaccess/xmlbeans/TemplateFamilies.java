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
 *	This generated bean class TemplateFamilies matches the schema element 'templateFamilies'.
 *
 *	Generated on Thu Jun 07 12:07:29 PDT 2007
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the following bean graph:
 *
 *	templateFamilies <templateFamilies> : TemplateFamilies
 *		family <family> : Family[1,n]
 *			[attr: name CDATA #IMPLIED  : java.lang.String]
 *			[attr: expanded CDATA #IMPLIED  : boolean]
 *			domainObject <domainObject> : DomainObject[1,n]
 *				[attr: name CDATA #IMPLIED  : java.lang.String]
 *				modelElement <modelElement> : java.lang.String
 *				stereotype <stereotype> : java.lang.String
 *				description <description> : java.lang.String
 *				template <template> : Template[1,n]
 *					filenameFormat <filenameFormat> : java.lang.String
 *					fileExtension <fileExtension> : java.lang.String
 *					folderPath <folderPath> : java.lang.String
 *					templateFile <templateFile> : java.lang.String
 *
 * @Generated
 */

package org.netbeans.modules.uml.codegen.dataaccess.xmlbeans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.util.*;

// BEGIN_NOI18N
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class TemplateFamilies extends org.netbeans.modules.schema2beans.BaseBean
    implements org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.TemplateFamiliesInterface, org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.CommonBean
{
    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    
    static public final String FAMILY = "Family";	// NOI18N
    
    public TemplateFamilies() throws org.netbeans.modules.schema2beans.Schema2BeansException
    {
        this(null, Common.USE_DEFAULT_VALUES);
    }
    
    public TemplateFamilies(org.w3c.dom.Node doc, int options) throws org.netbeans.modules.schema2beans.Schema2BeansException
    {
        this(Common.NO_DEFAULT_VALUES);
        initFromNode(doc, options);
    }
    
    protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
    {
        if (doc == null)
        {
            doc = GraphManager.createRootElementNode("templateFamilies");	// NOI18N
            if (doc == null)
                throw new Schema2BeansException("Cannot create DOM root");	// NOI18N
        }
        Node n = GraphManager.getElementNode("templateFamilies", doc);	// NOI18N
        if (n == null)
            throw new Schema2BeansException("Doc root not in the DOM graph");	// NOI18N
        
        this.graphManager.setXmlDocument(doc);
        
        // Entry point of the createBeans() recursive calls
        this.createBean(n, this.graphManager());
        this.initialize(options);
    }
    
    public TemplateFamilies(int options)
    {
        super(comparators, runtimeVersion);
        initOptions(options);
    }
    
    protected void initOptions(int options)
    {
        // The graph manager is allocated in the bean root
        this.graphManager = new GraphManager(this);
        this.createRoot("templateFamilies", "TemplateFamilies",	// NOI18N
            Common.TYPE_1 | Common.TYPE_BEAN, TemplateFamilies.class);
        
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(1);
        this.createProperty("family", 	// NOI18N
            FAMILY,
            Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY,
            Family.class);
        this.createAttribute(FAMILY, "name", "Name",
            AttrProp.CDATA | AttrProp.IMPLIED,
            null, null);
        this.createAttribute(FAMILY, "expanded", "Expanded",
            AttrProp.CDATA | AttrProp.IMPLIED,
            null, null);
        this.initialize(options);
    }
    
    // Setting the default values of the properties
    void initialize(int options)
    {
        
    }
    
    // This attribute is an array containing at least one element
    public void setFamily(int index, Family value)
    {
        this.setValue(FAMILY, index, value);
    }
    
    //
    public Family getFamily(int index)
    {
        return (Family)this.getValue(FAMILY, index);
    }
    
    // Return the number of properties
    public int sizeFamily()
    {
        return this.size(FAMILY);
    }
    
    // This attribute is an array containing at least one element
    public void setFamily(Family[] value)
    {
        this.setValue(FAMILY, value);
    }
    
    //
    public Family[] getFamily()
    {
        return (Family[])this.getValues(FAMILY);
    }
    
    // Add a new element returning its index in the list
    public int addFamily(org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.Family value)
    {
        int positionOfNewItem = this.addValue(FAMILY, value);
        return positionOfNewItem;
    }
    
    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeFamily(org.netbeans.modules.uml.codegen.dataaccess.xmlbeans.Family value)
    {
        return this.removeValue(FAMILY, value);
    }
    
    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public Family newFamily()
    {
        return new Family();
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
    
    // This method returns the root of the bean graph
    // Each call creates a new bean graph from the specified DOM graph
    public static TemplateFamilies createGraph(org.w3c.dom.Node doc) throws org.netbeans.modules.schema2beans.Schema2BeansException
    {
        return new TemplateFamilies(doc, Common.NO_DEFAULT_VALUES);
    }
    
    public static TemplateFamilies createGraph(java.io.File f) throws org.netbeans.modules.schema2beans.Schema2BeansException, java.io.IOException
    {
        FileObject fo=FileUtil.toFileObject(f);
        java.io.InputStream in = fo.getInputStream();
        //java.io.InputStream in = new java.io.FileInputStream(f);
        try
        {
            return createGraph(in, false);
        }
        finally
        {
            in.close();
        }
    }
    
    public static TemplateFamilies createGraph(java.io.InputStream in) throws org.netbeans.modules.schema2beans.Schema2BeansException
    {
        return createGraph(in, false);
    }
    
    public static TemplateFamilies createGraph(java.io.InputStream in, boolean validate) throws org.netbeans.modules.schema2beans.Schema2BeansException
    {
        Document doc = GraphManager.createXmlDocument(in, validate);
        return createGraph(doc);
    }
    
    // This method returns the root for a new empty bean graph
    public static TemplateFamilies createGraph()
    {
        try
        {
            return new TemplateFamilies();
        }
        catch (Schema2BeansException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void validate() throws org.netbeans.modules.schema2beans.ValidateException
    {
    }
        
    public void _setSchemaLocation(String location)
    {
        if (beanProp().getAttrProp("xsi:schemaLocation", true) == null)
        {
            createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
            setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, location);
        }
        setAttributeValue("xsi:schemaLocation", location);
    }
    
    public String _getSchemaLocation()
    {
        if (beanProp().getAttrProp("xsi:schemaLocation", true) == null)
        {
            createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
            setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
        }
        return getAttributeValue("xsi:schemaLocation");
    }
    
    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent)
    {
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("Family["+this.sizeFamily()+"]");	// NOI18N
        for(int i=0; i<this.sizeFamily(); i++)
        {
            str.append(indent+"\t");
            str.append("#"+i+":");
            n = (org.netbeans.modules.schema2beans.BaseBean) this.getFamily(i);
            if (n != null)
                n.dump(str, indent + "\t");	// NOI18N
            else
                str.append(indent+"\tnull");	// NOI18N
            this.dumpAttributes(FAMILY, i, str, indent);
        }
        
    }
    @Override
    public String dumpBeanNode()
    {
        StringBuffer str = new StringBuffer();
        str.append("TemplateFamilies\n");	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }
    
    public Family getFamilyByName(String familyName)
    {
        Family[] familyList = getFamily();
        Family theFamily = null;
        
        for (Family family: familyList)
        {
            if (family.getName().equals(familyName))
            {
                theFamily = family;
                break;
            }
        }
        
        return theFamily;
    }
    
    
    public boolean isUniqueFamilyName(String familyName)
    {
        return getFamilyByName(familyName) == null;
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
