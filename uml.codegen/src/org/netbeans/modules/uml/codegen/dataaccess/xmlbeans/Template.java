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
 * This generated bean class Template matches the schema element 'template'.
 * The root bean class is TemplateFamilies
 *
 * Generated on Thu Jun 07 12:07:29 PDT 2007
 * @Generated
 */

package org.netbeans.modules.uml.codegen.dataaccess.xmlbeans;

// BEGIN_NOI18N
import java.util.Vector;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Common;

public class Template extends BaseBean
    implements TemplateInterface, CommonBean
{
    
    static Vector comparators = new Vector();
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = 
        new org.netbeans.modules.schema2beans.Version(4, 2, 0);
    
    static public final String FILENAMEFORMAT = "FilenameFormat";	// NOI18N
    static public final String FILEEXTENSION = "FileExtension";	// NOI18N
    static public final String FOLDERPATH = "FolderPath";	// NOI18N
    static public final String TEMPLATEFILE = "TemplateFile";	// NOI18N
    
    public Template()
    {
        this(Common.USE_DEFAULT_VALUES);
    }
    
    public Template(int options)
    {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(4);
        this.createProperty("filenameFormat", 	// NOI18N
            FILENAMEFORMAT,
            Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
            java.lang.String.class);
        this.createProperty("fileExtension", 	// NOI18N
            FILEEXTENSION,
            Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
            java.lang.String.class);
        this.createProperty("folderPath", 	// NOI18N
            FOLDERPATH,
            Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
            java.lang.String.class);
        this.createProperty("templateFile", 	// NOI18N
            TEMPLATEFILE,
            Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
            java.lang.String.class);
        this.initialize(options);
    }
    
    // Setting the default values of the properties
    void initialize(int options)
    {
        
    }
    
    // This attribute is mandatory
    public void setFilenameFormat(java.lang.String value)
    {
        this.setValue(FILENAMEFORMAT, value);
    }
    
    //
    public java.lang.String getFilenameFormat()
    {
        return (java.lang.String)this.getValue(FILENAMEFORMAT);
    }
    
    public java.lang.String fetchDefaultFilenameFormat()
    {
        return "";
    }
    
    // This attribute is mandatory
    public void setFileExtension(java.lang.String value)
    {
        this.setValue(FILEEXTENSION, value);
    }
    
    //
    public java.lang.String getFileExtension()
    {
        return (java.lang.String)this.getValue(FILEEXTENSION);
    }
    
    public java.lang.String fetchDefaultFileExtension()
    {
        return "";
    }
    
    // This attribute is mandatory
    public void setFolderPath(java.lang.String value)
    {
        this.setValue(FOLDERPATH, value);
    }
    
    //
    public java.lang.String getFolderPath()
    {
        return (java.lang.String)this.getValue(FOLDERPATH);
    }
    
    public java.lang.String fetchDefaultFolderPath()
    {
        return "";
    }
    
    // This attribute is mandatory
    public void setTemplateFile(java.lang.String value)
    {
        this.setValue(TEMPLATEFILE, value);
    }
    
    //
    public java.lang.String getTemplateFile()
    {
        return (java.lang.String)this.getValue(TEMPLATEFILE);
    }
    
    public java.lang.String fetchDefaultTemplateFile()
    {
        return "";
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
        str.append("FilenameFormat");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getFilenameFormat();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(FILENAMEFORMAT, 0, str, indent);
        
        str.append(indent);
        str.append("FileExtension");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getFileExtension();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(FILEEXTENSION, 0, str, indent);
        
        str.append(indent);
        str.append("FolderPath");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getFolderPath();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(FOLDERPATH, 0, str, indent);
        
        str.append(indent);
        str.append("TemplateFile");	// NOI18N
        str.append(indent+"\t");	// NOI18N
        str.append("<");	// NOI18N
        o = this.getTemplateFile();
        str.append((o==null?"null":o.toString().trim()));	// NOI18N
        str.append(">\n");	// NOI18N
        this.dumpAttributes(TEMPLATEFILE, 0, str, indent);
        
    }
    public String dumpBeanNode()
    {
        StringBuffer str = new StringBuffer();
        str.append("Template\n");	// NOI18N
        this.dump(str, "\n  ");	// NOI18N
        return str.toString();
    }}

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
