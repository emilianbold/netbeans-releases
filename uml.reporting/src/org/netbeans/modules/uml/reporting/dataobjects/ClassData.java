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
import java.util.ArrayList;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.reporting.ReportTask;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


public class ClassData extends ElementDataObject
{
    IClassifier element;
    
    public ClassData()
    {
        
    }
    
    public ClassData(IClassifier element)
    {
        this.element = element;
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof IClassifier)
            element = (IClassifier)e;
    }
    
    public IClassifier getElement()
    {
        return element;
    }
    
    public String getElementName()
    {
        return getElement().getName();
    }
    
    public IClassifier[] getSuperClasses()
    {
        ArrayList<IClassifier> list = new ArrayList();
        
        ETList<IGeneralization> generalizations = getElement().getGeneralizations();
        for (int i=0; i<generalizations.size(); i++)
        {
            IGeneralization gen = generalizations.get(i);
            list.add(gen.getGeneral());
        }
        IClassifier[] a = new IClassifier[list.size()];
        return list.toArray(a);
    }
    
    
    public IInterface[] getImplementedInterfaces()
    {
        ArrayList<IInterface> list = new ArrayList();
        
        ETList<IImplementation> impls = getElement().getImplementations();
        for (int i=0; i<impls.size(); i++)
        {
            list.add(impls.get(i).getContract());
        }
        IInterface[] a = new IInterface[list.size()];
        return list.toArray(a);
    }
    
    
    public IClassifier[] getNestedClasses()
    {
        ArrayList<IClassifier> list = new ArrayList();
        ETList<INamedElement> owned = getElement().getOwnedElements();
        for (int i=0; i<owned.size(); i++)
        {
            INamedElement element = owned.get(i);
            if (element.getElementType().equals("Class") ||
                element.getElementType().equals("Interface") ||
                element.getElementType().equals("Enumeration"))
            {
                list.add((IClassifier)element);
            }
        }
        IClassifier[] a = new IClassifier[list.size()];
        return list.toArray(a);
    }
    
    
    public String getEnclosingClassSection()
    {    
        StringBuilder buffer = new StringBuilder();
        IElement enclosingClass = getElement().getOwner();
        if (enclosingClass instanceof IClassifier)
        {   
            buffer.append("<DL>\r\n");
            buffer.append("<DT><B>" +
                    NbBundle.getMessage(ClassData.class, "Enclosing_class") +
                    ":</B><DD><A HREF=\"" + getLinkTo(enclosingClass) + "\" >" +
                    ((IClassifier)enclosingClass).getName() + "</A></DD>\r\n");
            buffer.append("</DL>\r\n");
        }
        return buffer.toString();   
    }
    
    
    public IOperation[] getConstructors()
    {
        ArrayList<IOperation> list = new ArrayList();
        ETList<IOperation> ops = getElement().getOperations();
        for (int i=0; i<ops.size(); i++)
        {
            IOperation op = ops.get(i);
            if (op.getReturnType()==null)
                list.add(op);
        }
        IOperation[] a = new IOperation[list.size()];
        return list.toArray(a);
    }
    
    
    public IOperation[] getOperations()
    {
        ArrayList<IOperation> list = new ArrayList();
        ETList<IOperation> ops = getElement().getOperations();
        for (int i=0; i<ops.size(); i++)
        {
            IOperation op = ops.get(i);
            if (op.getReturnType()!=null)
                list.add(op);
        }
        IOperation[] a = new IOperation[list.size()];
        return list.toArray(a);
    }
    
    
    public String getOperationSummary(IOperation[] ops)
    {
        StringBuilder buff = new StringBuilder();
        IOperation op;
        for (int i=0; i<ops.length; i++)
        {
            op = ops[i];
            ETList<IParameter> params = ops[i].getParameters();
            IParameter returnType = op.getReturnType();
            buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
            if (returnType==null)
            {
                buff.append("<TD><CODE><B><A HREF=\"" + "#" + ops[i].getName() + "\">" + ops[i].getName() + "</A></B>(");
            }
            else
            {
                buff.append("<TD ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"1%\"><FONT SIZE=\"-1\">\r\n");
                if (returnType.getType()!=null)
                    buff.append("<CODE>" + "<A HREF=\"" + getLinkTo(returnType.getType()) + "\">" + returnType.getType().getName() + "</A></CODE></FONT></TD>\r\n");
                else
                    buff.append("<CODE>&nbsp;</CODE></FONT></TD>\r\n");
                buff.append("<TD><CODE><B><A HREF=\"#" + op.getName() + "\">" + op.getName() + "</A></B>(");
            }
            for (int j=0; j<params.size(); j++)
            {
                IParameter param = params.get(j);
                if (param.getDirection() == BaseElement.PDK_RESULT)
                    continue;
                if (param.getType()!=null)
                    buff.append("<A HREF=\"" + getLinkTo(param.getType()) + "\">" +
                            param.getType().getName() + "</A>&nbsp;" + param.getName());
                else
                    buff.append("&nbsp;" + param.getName());
                if (j!=params.size()-1)
                    buff.append(",&nbsp;");
            }
            buff.append(")</CODE>");
            buff.append("<BR>\r\n");
    
            buff.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<PRE>" +
                getBriefDocumentation(StringUtilities.unescapeHTML(
                ops[i].getDocumentation())) + "</PRE></TD>\r\n");
            
            buff.append("</TR>\r\n");
        }
        buff.append("</TABLE>\r\n&nbsp;\r\n");
        
        return buff.toString();
    }
    
    
    public String getTemplateParameterSummary()
    {
        ETList<IParameterableElement> params = getElement().getTemplateParameters();
        StringBuilder buff = new StringBuilder();
        if (params.size()>0)
        {
            buff.append("<!-- =========== TEMPLATE PARAMETER SUMMARY =========== -->\r\n\r\n");
            
            buff.append("<TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"3\" CELLSPACING=\"0\" SUMMARY=\"\">\r\n");
            buff.append("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\r\n");
            buff.append("<TH ALIGN=\"left\" COLSPAN=\"5\"><FONT SIZE=\"+2\">\r\n");
            buff.append("<B>" + NbBundle.getMessage(ClassData.class, "Template_Parameter_Summary") +
                    "</B></FONT></TH>\r\n");
            buff.append("</TR>\r\n");
            
            String[] columns = new String[] {
                NbBundle.getMessage(ClassData.class, "TemplateParameter_Name"),
                NbBundle.getMessage(ClassData.class, "TemplateParameter_Alias"),
                NbBundle.getMessage(ClassData.class, "TemplateParameter_Visibility"),
                NbBundle.getMessage(ClassData.class, "TemplateParameter_DefaultElement"),
                NbBundle.getMessage(ClassData.class, "TemplateParameter_TypeConstraint")
            };
            
            buff.append("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\r\n");
            for (int i=0; i<columns.length; i++)
            {
                buff.append("<TD ALIGN=\"left\" VALIGN=\"top\" WIDTH=\"1%\"><FONT SIZE=\"+1\">");
                buff.append(columns[i] + "</TD>\r\n");
            }
            
            buff.append("</TR>\r\n");
            
            for (int i=0; i<params.size(); i++)
            {
                IParameterableElement param = params.get(i);
                IParameterableElement element = param.getDefaultElement();
                buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                String[] values = new String[] {
                    param.getName(),
                    param.getAlias(),
                    getVisibility(param),
                    element==null?"&nbsp;":element.getName(),
                    param.getTypeConstraint()
                };
                for (int j=0; j<values.length; j++)
                {
                    String value = values[j];
                    if (value.equals(""))
                        value = "&nbsp;";
                    buff.append("<TD ALIGN=\"left\" VALIGN=\"top\" WIDTH=\"1%\">");
                    buff.append(value + "</TD>\r\n");
                }
                
                buff.append("</TR>\r\n");
            }
            buff.append("</TABLE>\r\n&nbsp;\r\n");
        }
        return buff.toString();
    }
    
    public String getOperationDetail(IOperation[] ops)
    {
        StringBuilder buff = new StringBuilder();
        IOperation op;
        for (int i=0; i<ops.length; i++)
        {
            op = ops[i];
            buff.append("<A NAME=\"" + op.getName() + "\"></A><H3>" + op.getName() + "</H3>\r\n");
            buff.append("<PRE>\r\n");
            buff.append(formatOperation(op) + " " + "<B>" + op.getName() + "</B>(");
            
            ETList<IParameter> params = op.getParameters();
            for (int j=0; j<params.size(); j++)
            {
                
                IParameter param = params.get(j);
                if (param.getDirection() != BaseElement.PDK_RESULT)
                {
                    if (param.getType()!=null)
                        buff.append("<A HREF=\"" + getLinkTo(param.getType()) + "\">" +
                                param.getType().getName() + "</A>&nbsp;" + param.getName());
                    else
                        buff.append("&nbsp;" + param.getName());
                    if (j!=params.size()-1)
                        buff.append(",&nbsp;");
                }
            }
            buff.append(")</PRE>\r\n");
            buff.append("<DL>\r\n");
            
            buff.append("<DD><PRE>" + StringUtilities.unescapeHTML(
                op.getDocumentation()) + "</PRE>\r\n");
            
            buff.append("<P>\r\n</DD></DL>\r\n");
            
            if (i<ops.length-1)
                buff.append("<HR>\r\n");
            else
                buff.append("\r\n");
        }
        
        return buff.toString();
    }
    
    
    public String formatAttribute(IAttribute attr)
    {
        if (attr.getType()!=null)
            return getVisibility(attr) + " " + "<A HREF=\"" +
                    getLinkTo(attr.getType()) + "\">" + attr.getType().getName() + "</A>" + " ";
        else
            return getVisibility(attr) + " ";
    }
    
    
    public String formatOperation(IOperation op)
    {
        String visibility = getVisibility(op);
        String staticString = op.getIsStatic()==true?" static":"";
        String finalString = op.getIsFinal()?" final":"";
        String abstractString = op.getIsAbstract()?" abstract":"";
        String returnType = op.getReturnType()==null || op.getReturnType().getType()==null?"": " " + "<A HREF=\"" +
                getLinkTo(op.getReturnType().getType()) + "\">" +
                op.getReturnType().getType().getName() + "</A>";
        return visibility + staticString + finalString + abstractString + returnType;
    }
    
    
    public String formatClass(IClass c)
    {
        String visibility = getVisibility(c);
        return visibility + " class";
    }
    
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Final,
            Property_Transient,
            Property_Abstract,
            Property_Leaf
        };
    }
    
    protected Object[] getPropertyValues()
    {
        Boolean isFinal = new Boolean(getElement().getIsFinal());
        Boolean isTransient = new Boolean(getElement().getIsTransient());
        Boolean isAbstract = new Boolean(getElement().getIsAbstract());
        Boolean isLeaf = new Boolean(getElement().getIsLeaf());
        
        return new Object[] {getElement().getAlias(),
        getVisibility(getElement()), isFinal,
        isTransient, isAbstract, isLeaf};
    }
    
    
    protected String getAssociations()
    {
        StringBuilder buff = new StringBuilder();
        
        ETList<IAssociation> associations = getElement().getAssociations();
        if (associations.size()>0)
        {
            buff.append("<DL>\r\n");
            buff.append("<DT><B>" +
                    NbBundle.getMessage(ClassData.class, "All_Known_Associations") + ": </B>");
            for (int i=0; i<associations.size(); i++)
            {
                IAssociation association = associations.get(i);
                String type = association.getExpandedElementType();
                buff.append("<DD><IMG SRC=\"" + ReportTask.getPathToReportRoot(getElement()) +
                        "images/" + type + ".png\" ALT=\"" + "(" +
                        type + ") \"> " + "<A HREF=\"" +
                        getLinkTo(association) + "\">" + type +  "</A>: ");
                
                ETList<IElement> participants = association.getAllParticipants();
                participants.remove(getElement());
                for (int j=0; j<participants.size(); j++)
                {
                    IElement parti = participants.get(j);
                    if (association.getIsReflexive() ||
                            !parti.getXMIID().equals(getElement().getXMIID()))
                    {
                        if (displayLink(parti) && parti instanceof INamedElement)
                        {
                            buff.append("<A HREF=\"" + getLinkTo(parti) +
                                    "\" title=\"association in " +
                                    parti.getOwningPackage().getFullyQualifiedName(false) +
                                    "\">" + ((INamedElement)parti).getName() + "</A>");
                        }
                        else
                        {
                            buff.append(parti.getElementType());
                        }
                        
                        if (j < participants.size()-1)
                            buff.append(", ");
                    }
                }
                buff.append("</DD>\r\n");
            }
            buff.append("</DL>\r\n");
        }
        return buff.toString();
        
    }
    
    
    public String getSpecifications()
    {
        StringBuilder buff = new StringBuilder();
        
        ETList<IGeneralization> specializations = getElement().getSpecializations();
        if (specializations.size() > 0)
        {
            buff.append("<DL>\r\n");
            buff.append("<DT><B>" +
                    NbBundle.getMessage(ClassData.class, "All_Specifications") + ": </B> <DD>");
            for (int i=0; i<specializations.size(); i++)
            {
                IClassifier sp = specializations.get(i).getSpecific();
                buff.append("<A HREF=\"" + getLinkTo(sp) + "\" title=\"specifications in " +
                        sp.getOwningPackage().getFullyQualifiedName(false) + "\">" +
                        sp.getName() + "</A>");
                if (i < specializations.size()-1)
                    buff.append(", ");
            }
            buff.append("</DD>\r\n");
            buff.append("</DL>\r\n");
        }
        return buff.toString();
    }
    
    
    public String getGeneralizations()
    {
        StringBuilder buff = new StringBuilder();
        
        ETList<IGeneralization> generalizations = getElement().getGeneralizations();
        if (generalizations.size() > 0)
        {
            buff.append("<DL>\r\n");
            buff.append("<DT><B>" +
                    NbBundle.getMessage(ClassData.class, "All_Generalizations") + ": </B> <DD>");
            for (int i=0; i<generalizations.size(); i++)
            {
                IClassifier sp = generalizations.get(i).getGeneral();
                buff.append("<A HREF=\"" + getLinkTo(sp) + "\" title=\"specifications in " +
                        sp.getOwningPackage().getFullyQualifiedName(false) + "\">" +
                        sp.getName() + "</A>");
                if (i < generalizations.size()-1)
                    buff.append(", ");
            }
            buff.append("</DD>\r\n");
            buff.append("</DL>\r\n");
        }
        return buff.toString();
    }
    
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        boolean result = false;
        try
        {
            FileOutputStream fo = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(fo);
            
            out.write(getHTMLHeader());
            out.write("<BODY BGCOLOR=\"white\">\r\n");
            out.write(getNavBar());
            out.write("<HR>\r\n");
            out.write("<H2>\r\n");
            out.write("<FONT SIZE=\"-1\">" + getOwningPackageName() + "</FONT>\r\n");
            out.write("<BR>\r\n");
            out.write(getElementType() + " " + getElementName() + "</H2>\r\n");
            out.write("<PRE>\r\n");
            
            IClassifier[] superClasses = getSuperClasses();
            if (getSuperClasses().length>0)
            {
                for (int i=0; i<superClasses.length; i++)
                {
                    IClassifier classifier = superClasses[i];
                    out.write("<A HREF=\"" + getLinkTo(classifier) +
                            "\" title=\"" + getElementType() + " in" + getOwningPackageName() +
                            "\">" + classifier.getName() + "</A>");
                    if (i < superClasses.length-1)
                        out.write(", ");
                }
                out.write("\r\n");
                out.write("  <IMG SRC=\"" + ReportTask.getPathToReportRoot(getElement()) +
                        "images/inherit.gif\" ALT=\"extended by \">");
            }
            out.write("<B>" + getElement().getFullyQualifiedName(false) + "</B>\r\n");
            out.write("</PRE>\r\n");
            
            IInterface[] infs = getImplementedInterfaces();
            if (infs.length > 0)
            {
                out.write("<DL>\r\n");
                out.write("<DT><B>" +
                        NbBundle.getMessage(ClassData.class, "All_Implemented_Interfaces") + ":</B> <DD>");
                for (int i=0; i<infs.length; i++)
                {
                    IInterface inf = infs[i];
                    out.write("<A HREF=\"" + getLinkTo(inf) + "\" title=\"interface in " + inf.getOwningPackage().getFullyQualifiedName(false) + "\">" + inf.getName() + "</A>");
                    if (i < infs.length-1)
                        out.write(", ");
                }
                out.write("</DD>\r\n");
                out.write("</DL>\r\n");
            }
            
            ETList<IGeneralization> specializations = getElement().getSpecializations();
            if (specializations.size() > 0)
            {
                out.write("<DL>\r\n");
                out.write("<DT><B>" +
                        NbBundle.getMessage(ClassData.class, "All_Known_Subclasses") + ":</B> <DD>");
                for (int i=0; i<specializations.size(); i++)
                {
                    IClassifier sp = specializations.get(i).getSpecific();
                    out.write("<A HREF=\"" + getLinkTo(sp) + "\" title=\"subclass in " + sp.getOwningPackage().getFullyQualifiedName(false) + "\">" + sp.getName() + "</A>");
                    if (i < specializations.size()-1)
                        out.write(", ");
                }
                out.write("</DD>\r\n");
                out.write("</DL>\r\n");
            }
            
            // dependency relationshp
            out.write(getDependencies());
            
            // association relationship
            out.write(getAssociations());
            
            // enclosing classes
            out.write(getEnclosingClassSection());
            
            // enclosing diagrams
            out.write(getEnclosingDiagrams());
            
            out.write("<HR>\r\n");
            out.write("<DL>\r\n");
            out.write("<DT><PRE>" + getVisibility(getElement()) + " " + getElementType().toLowerCase() + " <B>" + getElementName() + "</B></DT>");
            if (superClasses.length>0)
            {
                out.write("<DT>extends ");
                for (int i=0; i<superClasses.length; i++)
                {
                    IClassifier classifier = superClasses[i];
                    out.write("<A HREF=\"" + getLinkTo(classifier) +
                            "\" title=\"" + getElementType() + " in" + getOwningPackageName() +
                            "\">" + classifier.getName() + "</A>");
                    if (i < superClasses.length-1)
                        out.write(", ");
                }
            }
            
            if (infs.length > 0)
            {
                out.write("<DT>implements ");
                for (int i=0; i<infs.length; i++)
                {
                    IClassifier classifier = infs[i];
                    out.write("<A HREF=\"" + getLinkTo(classifier) + "\" title=\"interface in " + classifier.getOwningPackage().getFullyQualifiedName(false) + "\">" + classifier.getName() + "</A>");
                    if (i < infs.length-1)
                        out.write(", ");
                }
            }
            out.write("</DL>\r\n</PRE>\r\n\r\n");
            
            out.write(getDocumentation());
            
            // property summary
            
            out.write(getProperties());
            
            // nested class summary
            
            IClassifier[] nested = getNestedClasses();
            if (nested.length > 0)
            {
                out.write("<!-- =========== NESTED CLASS SUMMARY =========== -->\r\n");
                out.write(getSummaryHeader("nested_class_summary",
                        NbBundle.getMessage(ClassData.class, "Nested_Class_Summary")));
                
                for (int i=0; i<nested.length; i++)
                {
                    IClassifier classifier = nested[i];
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"1%\"><FONT SIZE=\"-1\">\r\n");
                    out.write("<CODE>" + getVisibility(classifier) + " " + getElementType().toLowerCase() + "</CODE></FONT></TD>\r\n");
                    out.write("<TD><CODE><B><A HREF=\"" + getLinkTo(classifier) + "\">" + classifier.getName() + "</A></B></CODE>\r\n");
                    out.write("<BR>\r\n");
                    
                    out.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<PRE>" 
                        + getBriefDocumentation(StringUtilities.unescapeHTML(
                        classifier.getDocumentation())) + "</PRE></TD>\r\n");
                    
                    out.write("</TR>\r\n\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n");
            }
            
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
                    out.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<PRE>" +
                        getBriefDocumentation(StringUtilities.unescapeHTML(
                        attr.getDocumentation())) + "</PRE></TD>\r\n</TR>\r\n");
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
            
            // template parameter summary
            out.write(getTemplateParameterSummary());
            
            // diagram summary
            out.write(getDiagramSummary());
            
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
                    out.write("<PRE>" + formatAttribute(attr) + "<B>" + attr.getName() + "</B></PRE>");
                    out.write("<DL>\r\n");
                    
                    out.write("<DD><PRE>" + StringUtilities.unescapeHTML(
                        attr.getDocumentation())
                        + "</PRE>\r\n<P>\r\n</DD>\r\n</DL>\r\n");
                    
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
