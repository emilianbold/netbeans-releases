/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.reporting.dataobjects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtensionPoint;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class PartFacadeData extends ClassData
{
    private IPartFacade element;
    
    /** Creates a new instance of PartFacadeData */
    public PartFacadeData()
    {
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof IPartFacade)
            this.element = (IPartFacade)e;
    }
    
    public IPartFacade getElement()
    {
        return element;
    }
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        ETList<IExtend> extendsList = getElement().getExtends();
        ETList<IInclude> includesList = getElement().getIncludes();
        ETList<IExtensionPoint> points = getElement().getExtensionPoints();
        ETList<IStructuredClassifier> contexts = getElement().getRoleContexts();
        
        boolean result = false;
        try
        {
            FileOutputStream fo = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(fo);
            String doc = "";
            
            out.write(getHTMLHeader());
            out.write("<BODY BGCOLOR=\"white\">\r\n");
            out.write(getNavBar());
            out.write("<HR>\r\n");
            out.write("<H2>\r\n");
            out.write("<FONT SIZE=\"-1\">" + getOwningPackageName() + "</FONT>\r\n");
            out.write("<BR>\r\n");
            out.write(NbBundle.getMessage(PartFacadeData.class, "ElementType_" +
                    getElement().getExpandedElementType()) + " " +
                    ((INamedElement)getElement()).getName() + "</H2>\r\n");
            
            out.write("<DL>\r\n");
            out.write("<DT>" + getVisibility(getElement()) + " " +
                    getElementType().toLowerCase() + " <B>" + getElement().getName() + "</B></DT>");
            if (extendsList.size()>0)
            {
                out.write("<DT>" + NbBundle.getMessage(PartFacadeData.class, "extends") + " ");
                for (int i=0; i<extendsList.size(); i++)
                {
                    IExtend extend = extendsList.get(i);
                    out.write("<A HREF=\"" + getLinkTo(extend.getBase()) +
                            "\" title=\"" + getElementType() + " in" + getOwningPackageName() +
                            "\">" + extend.getBase().getName() + "</A>");
                    if (i < extendsList.size()-1)
                        out.write(", ");
                }
            }
            
            if (includesList.size() > 0)
            {
                out.write("<DT>" + NbBundle.getMessage(PartFacadeData.class, "includes") + " ");
                for (int i=0; i<includesList.size(); i++)
                {
                    IInclude include = includesList.get(i);
                    out.write("<A HREF=\"" + getLinkTo(include.getAddition()) + "\" title=\"" +
                            getElementType() +" in " +
                            include.getOwningPackage().getFullyQualifiedName(false) +
                            "\">" + include.getAddition().getName() + "</A>");
                    if (i < includesList.size()-1)
                        out.write(", ");
                }
            }
            
            IClassifier classifier = getElement().getFeaturingClassifier();
            if (classifier!=null)
            {
                out.write("<DT>" + NbBundle.getMessage(PartFacadeData.class, "features") + " " + "<A HREF=\"" +
                        getLinkTo(classifier)+ "\" title=\"" +
                        getElement().getExpandedElementType() +" in " +
                        classifier.getOwningPackage().getFullyQualifiedName(false) +
                        "\">" + classifier.getName() + "</A>");
            }
            
            out.write("</DL>\r\n\r\n");
            
            out.write(getDependencies());
            out.write(getAssociations());
            
            out.write(getEnclosingDiagrams());
            
            out.write(getDocumentation());
            
            // property summary
            
            out.write(getProperties());
            
            // attribute summary
            ETList<IAttribute> attrs = getElement().getAttributes();
            IAttribute attr;
            IClassifier type;
            if (attrs.size() > 0)
            {
                out.write("<!-- =========== ATTRIBUTE SUMMARY =========== -->\r\n");
                out.write(getSummaryHeader("attribute_summary",
                        NbBundle.getMessage(ClassData.class, "Attribute_Summary")));
                
                for (int i=0; i<attrs.size(); i++)
                {
                    attr = attrs.get(i);
                    type = attr.getType();
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"1%\"><FONT SIZE=\"-1\">\r\n");
                    if (type!=null)
                        out.write("<CODE>" + "<A HREF=\"" + getLinkTo(type) + "\">" + type.getName() + "</A></CODE></FONT></TD>\r\n");
                    else
                        out.write("<CODE>&nbsp;</CODE></FONT></TD>\r\n");
                    out.write("<TD><CODE><B><A HREF=\"#" + attr.getName() + "\">" + attr.getName() + "</A></B></CODE>\r\n");
                    out.write("<BR>\r\n");
                    out.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                            getBriefDocumentation(attr.getDocumentation()) + "</TD>\r\n</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n");
            }
            
            // constructor summary
            
            IOperation[] constructors = getConstructors();
            if (constructors.length>0)
            {
                out.write("<!-- =========== CONSTRUCTOR SUMMARY =========== -->\r\n\r\n");
                out.write(getSummaryHeader("constructor_summary",
                        NbBundle.getMessage(ClassData.class, "Constructor_Summary")));
                out.write(getOperationSummary(constructors));
            }
            
            // operation summary
            
            IOperation[] ops = getOperations();
            IOperation op;
            IParameter returnType;
            if (ops.length > 0)
            {
                out.write("<!-- =========== OPERATION SUMMARY =========== -->\r\n");
                out.write(getSummaryHeader("operation_summary",
                        NbBundle.getMessage(ClassData.class, "Operation_Summary")));
                
                out.write(getOperationSummary(ops));
            }
            
            // stereotype summary
            out.write(getStereoTypesSummary());
            
            // tagged value summary
            out.write(getTaggedValueSummary());
            
            // constraint summary
            out.write(getConstraintsSummary());
            
            // extension point summary
            if (points.size()>0)
            {
                out.write("<!-- =========== EXTENSION POINT SUMMARY =========== -->\r\n");
                out.write(getSummaryHeader("extension_point_summary",
                        NbBundle.getMessage(PartFacadeData.class, "Extension_Point_Summary")));
                
                for (int i=0; i<points.size(); i++)
                {
                    IExtensionPoint point = points.get(i);
                    doc = point.getDocumentation();
                    if (doc == null || doc.trim().equals(""))
                        doc = "&nbsp;";
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD WIDTH=\"15%\"><B>" + point.getName() + "</B></TD>\r\n");
                    out.write("<TD>" + doc + "</TD>\r\n");
                    out.write("</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n<P>\r\n");
            }
            
            // attribute detail
            
            if (attrs.size() > 0)
            {
                out.write("<!-- =========== ATTRIBUTE DETAIL =========== -->\r\n");
                out.write(getDetailHeader("attribute_detail",
                        NbBundle.getMessage(ClassData.class, "Attribute_Detail")));
                
                
                for (int i=0; i<attrs.size(); i++)
                {
                    attr = attrs.get(i);
                    type = attr.getType();
                    
                    out.write("<A NAME=\"" + attr.getName() + "\"></A><H3>" + attr.getName() + "</H3>\r\n");
                    out.write("" + formatAttribute(attr) + "<B>" + attr.getName() + "</B>");
                    out.write("<DL>\r\n");
                    
                    out.write("<DD>" + attr.getDocumentation() + 
                        "\r\n<P>\r\n</DD>\r\n</DL>\r\n");
                    
                    if (i<attrs.size()-1)
                        out.write("<HR>\r\n\r\n");
                    else
                        out.write("\r\n");
                }
            }
            
            // constructor detail
            
            if (constructors.length>0)
            {
                out.write("<!-- =========== CONSTRUCTOR DETAIL =========== -->\r\n\r\n");
                out.write(getDetailHeader("constructor_detail",
                        NbBundle.getMessage(ClassData.class, "Constructor_Detail")));
                
                out.write(getOperationDetail(constructors));
            }
            // operation detail
            
            if (ops.length>0)
            {
                out.write("<!-- =========== OPERATION DETAIL =========== -->\r\n\r\n");
                out.write(getDetailHeader("operationr_detail",
                        NbBundle.getMessage(ClassData.class, "Operation_Detail")));
                
                out.write(getOperationDetail(ops));
            }
            
            out.write("<HR>\r\n");
            out.write(getNavBar());
            out.write("</BODY>\r\n</HTML>");
            out.close();
            result = true;
            
        }
        catch (FileNotFoundException e)
        {
            ErrorManager.getDefault().notify(e);
        }
        catch (IOException e)
        {
            ErrorManager.getDefault().notify(e);
        }
        return result;
        
    }
}
