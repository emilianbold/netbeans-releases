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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.profiles.IStereotype;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.reporting.ReportTask;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.ui.controls.projecttree.DefaultNodeFactory;
import org.netbeans.modules.uml.ui.support.DiagramBuilder;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IPresentationTarget;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ProjectTreeBuilderImpl;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;


public class ElementDataObject implements Report
{
    private IElement element;
    private String name;
    private String type;
    
    public static String Property_Alias = NbBundle.getMessage(ElementDataObject.class, "Property_Alias"); // NOI18N
    public static String Property_Visibility = NbBundle.getMessage(ElementDataObject.class, "Property_Visibility"); // NOI18N
    public static String Property_Final = NbBundle.getMessage(ElementDataObject.class, "Property_Final"); // NOI18N
    public static String Property_Transient = NbBundle.getMessage(ElementDataObject.class, "Property_Transient"); // NOI18N
    public static String Property_Abstract = NbBundle.getMessage(ElementDataObject.class, "Property_Abstract"); // NOI18N
    public static String Property_Leaf = NbBundle.getMessage(ElementDataObject.class, "Property_Leaf"); // NOI18N
    public static String Property_ActivityKind = NbBundle.getMessage(ElementDataObject.class, "Property_ActivityKind"); // NOI18N
    public static String Property_SingleCopy = NbBundle.getMessage(ElementDataObject.class, "Property_SingleCopy"); // NOI18N
    public static String Property_MultipleInvocation = NbBundle.getMessage(ElementDataObject.class, "Property_MultipleInvocation"); // NOI18N
    public static String Property_Synchronous = NbBundle.getMessage(ElementDataObject.class, "Property_Synchronous"); // NOI18N
    public static String Property_Kind = NbBundle.getMessage(ElementDataObject.class, "Property_Kind"); // NOI18N
    public static String Property_GroupKind = NbBundle.getMessage(ElementDataObject.class, "Property_GroupKind"); // NOI18N
    public static String Property_Activity = NbBundle.getMessage(ElementDataObject.class, "Property_Activity"); // NOI18N
    public static String Property_Ordering = NbBundle.getMessage(ElementDataObject.class, "Property_Ordering"); // NOI18N
    public static String Property_External = NbBundle.getMessage(ElementDataObject.class, "Property_External"); // NOI18N
    public static String Property_Dimension = NbBundle.getMessage(ElementDataObject.class, "Property_Dimension"); // NOI18N
    public static String Property_Container = NbBundle.getMessage(ElementDataObject.class, "Property_Container"); // NOI18N
    public static String Property_Instantiation = NbBundle.getMessage(ElementDataObject.class, "Property_Instantiation"); // NOI18N
    public static String Property_FileName = NbBundle.getMessage(ElementDataObject.class, "Property_FileName"); // NOI18N
    public static String Property_Reentrant = NbBundle.getMessage(ElementDataObject.class, "Property_Reentrant"); // NOI18N
    public static String Property_SubmachineState = NbBundle.getMessage(ElementDataObject.class, "Property_SubmachineState"); // NOI18N
    public static String Property_Simple = NbBundle.getMessage(ElementDataObject.class, "Property_Simple"); // NOI18N
    public static String Property_Orthogonal = NbBundle.getMessage(ElementDataObject.class, "Property_Orthogonal"); // NOI18N
    public static String Property_Composite = NbBundle.getMessage(ElementDataObject.class, "Property_Composite"); // NOI18N
    public static String Property_Reflexive = NbBundle.getMessage(ElementDataObject.class, "Property_Reflexive"); // NOI18N
    public static String Property_Derived = NbBundle.getMessage(ElementDataObject.class, "Property_Derived"); // NOI18N
    public static String Property_Static = NbBundle.getMessage(ElementDataObject.class, "Property_Static"); // NOI18N
    public static String Property_Volatile = NbBundle.getMessage(ElementDataObject.class, "Property_Volatile"); // NOI18N
    public static String Property_Type = NbBundle.getMessage(ElementDataObject.class, "Property_Type"); // NOI18N
    public static String Property_Client_Changeability = NbBundle.getMessage(ElementDataObject.class, "Property_Client_Changeability"); // NOI18N
    public static String Property_Multiplicity = NbBundle.getMessage(ElementDataObject.class, "Property_Multiplicity"); // NOI18N
    public static String Property_Participant = NbBundle.getMessage(ElementDataObject.class, "Property_Participant"); // NOI18N
    public static String Property_Navigable = NbBundle.getMessage(ElementDataObject.class, "Property_Navigable"); // NOI18N
    public static String Property_Operator = NbBundle.getMessage(ElementDataObject.class, "Property_Operator"); // NOI18N
    public static String Property_Deployment_Location = NbBundle.getMessage(ElementDataObject.class, "Property_Deployment_Location"); // NOI18N
    public static String Property_Execution_Location = NbBundle.getMessage(ElementDataObject.class, "Property_Execution_Location"); // NOI18N
    public static String Property_Multiple_Invocation = NbBundle.getMessage(ElementDataObject.class, "Property_Multiple_Invocation"); // NOI18N
    public static String Property_Default = NbBundle.getMessage(ElementDataObject.class, "Property_Default"); // NOI18N
    public static String Property_Primary_Key = NbBundle.getMessage(ElementDataObject.class, "Property_Primary_Key"); // NOI18N
    public static String Property_Redefined = NbBundle.getMessage(ElementDataObject.class, "Property_Redefined"); // NOI18N
    public static String navBar1;
    public static String navBar2;
    public static String navBar3;
    public static String navBar4;
    public static String navBar5;
    
    static
    {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("<!-- ========= START OF NAVBAR ======= -->\r\n"); // NOI18N
        buffer.append("<A NAME=\"navbar_top\"></A>\r\n"); // NOI18N
        buffer.append("<A HREF=\"#skip-navbar_top\" title=\"Skip navigation links\"></A>\r\n"); // NOI18N
        buffer.append("<TABLE BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" CELLSPACING=\"0\" SUMMARY=\"\">\r\n"); // NOI18N
        buffer.append("<TR>\r\n"); // NOI18N
        buffer.append("<TD COLSPAN=2 BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">\r\n"); // NOI18N
        buffer.append("<A NAME=\"navbar_top_firstrow\"></A>\r\n"); // NOI18N
        buffer.append("<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"3\" SUMMARY=\"\">\r\n"); // NOI18N
        buffer.append("  <TR ALIGN=\"center\" VALIGN=\"top\">\r\n"); // NOI18N
        
        navBar1 = buffer.toString();
        
        buffer = new StringBuilder();
        buffer.append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1Rev\">    <FONT CLASS=\"NavBarFont1Rev\"><B>" + NbBundle.getMessage(ElementDataObject.class, "Header_Package") + "</B></FONT>&nbsp;</TD>\r\n"); // NOI18N
        buffer.append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">" + NbBundle.getMessage(ElementDataObject.class, "Header_Element") + "</FONT>&nbsp;</TD>\r\n"); // NOI18N
        buffer.append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">" + NbBundle.getMessage(ElementDataObject.class, "Header_Diagram") + "</FONT>&nbsp;</TD>\r\n"); // NOI18N
        
        navBar2 = buffer.toString();
        
        buffer = new StringBuilder();
        buffer.append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">" + NbBundle.getMessage(ElementDataObject.class, "Header_Package") + "</FONT>&nbsp;</TD>\r\n"); // NOI18N
        buffer.append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">" + NbBundle.getMessage(ElementDataObject.class, "Header_Element") + "</FONT>&nbsp;</TD>\r\n"); // NOI18N
        buffer.append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1Rev\">    <FONT CLASS=\"NavBarFont1Rev\"><B>" + NbBundle.getMessage(ElementDataObject.class, "Header_Diagram") + "</B></FONT>&nbsp;</TD>\r\n"); // NOI18N
        
        navBar3 = buffer.toString();
        
        buffer = new StringBuilder();
        buffer.append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">" + NbBundle.getMessage(ElementDataObject.class, "Header_Package") + "</FONT>&nbsp;</TD>\r\n"); // NOI18N
        buffer.append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1Rev\">    <FONT CLASS=\"NavBarFont1Rev\"><B>" + NbBundle.getMessage(ElementDataObject.class, "Header_Element") + "</B></FONT>&nbsp;</TD>\r\n"); // NOI18N
        buffer.append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">" + NbBundle.getMessage(ElementDataObject.class, "Header_Diagram") + "</FONT>&nbsp;</TD>\r\n"); // NOI18N
        
        navBar4 = buffer.toString();
        
        buffer = new StringBuilder();
        buffer.append("  </TR>\r\n"); // NOI18N
        buffer.append("</TABLE>\r\n"); // NOI18N
        buffer.append("</TD>\r\n"); // NOI18N
        buffer.append("<TD ALIGN=\"right\" VALIGN=\"top\" ROWSPAN=3><EM>\r\n"); // NOI18N
        buffer.append("<b>" + NbBundle.getMessage(ElementDataObject.class, "brand") + "</b>\r\n"); // NOI18N
        buffer.append("</TD>\r\n"); // NOI18N
        buffer.append("</TR>\r\n"); // NOI18N
        buffer.append("</TABLE>\r\n"); // NOI18N
        buffer.append("<A NAME=\"skip-navbar_top\"></A>\r\n"); // NOI18N
        buffer.append("<!-- ========= END OF NAVBAR ========= -->\r\n\r\n"); // NOI18N
        
        navBar5 = buffer.toString();
    }
    
    
    /**
     * Creates a new instance of ElementDataObject
     */
    public ElementDataObject()
    {
    }
    
    public ElementDataObject(IElement element)
    {
        if (element instanceof INamedElement)
            setElement((INamedElement)element);
    }
    
    
    public IElement getElement()
    {
        return element;
    }
    
    
    protected void setElement(IElement e)
    {
        this.element = e;
    }
    
    protected void setElement(INamedElement e)
    {
        this.element = e;
    }
    
    public String getImage()
    {
        return "";
    }
    
    
    public String getElementType()
    {
        return getElement().getElementType();
    }
    
    
    public String getDocumentation()
    {
        String doc = getElement().getDocumentation().trim();
        
        if (doc.length()>0)
        {
            StringBuilder buff = new StringBuilder();
            buff.append("<P>\r\n"); // NOI18N
            buff.append(doc);
            buff.append("</P>"); // NOI18N
            buff.append("<HR>\r\n"); // NOI18N
            return buff.toString();
        }
        return ""; // NOI18N
    }
    
    
    private final static String ANGLE_BRACKET_OPEN = "<"; // NOI18N
    private final static String ANGLE_BRACKET_CLOSE = ">"; // NOI18N
    private final static String HTML_NBSP = "&nbsp"; // NOI18N
    private final static String SPACE = " "; // NOI18N
    private final static String SLASH = "/"; // NOI18N
    
    public String getBriefDocumentation(String doc)
    {
        if (doc == null || doc.equals(""))
            return HTML_NBSP;
        // return (doc.indexOf(".")>0)?doc.substring(0, doc.indexOf(".")):doc;

        StringBuffer docBuff = new StringBuffer(doc);
        
        Stack<String> tags = new Stack<String>();
        String tag = "";

        int i = 0;
        while(i > -1 && i < docBuff.length())
        {
            char ch = docBuff.charAt(i);
            if (ch == '<')
            {
                // we have found an escaped "<" symbol; possible html "a" tag
                if (docBuff.charAt(i+1) != '/')
                {
                    // start tag
                    int nextCloseAnglePos = docBuff.indexOf(
                        ANGLE_BRACKET_CLOSE, i+1);
                    
                    int nextSpacePos = docBuff.indexOf(SPACE, i+1);
                    int endpos = nextSpacePos < nextCloseAnglePos 
                        ? nextSpacePos : nextCloseAnglePos;
                    
                    // note this is a nested trinary if/then operation
                    // looking for the lesser of the two indexes, but
                    // if the lesser is -1 then we want the greater of the two
                    endpos = endpos < 0 
                        ? (nextSpacePos > nextCloseAnglePos 
                            ? nextSpacePos : nextCloseAnglePos) 
                        : endpos;
                    
                    tag = docBuff.substring(i+1, endpos);
                    tags.push(tag);
                    i = docBuff.indexOf(ANGLE_BRACKET_CLOSE, endpos);
                }
                
                else
                {
                    // end tag
                    int endpos = docBuff.indexOf(ANGLE_BRACKET_CLOSE, i+2);
                    tag = docBuff.substring(i+2, endpos);
                    i = endpos + 1;
                    
                    int stackPos = tags.lastIndexOf(tag);
                    if (stackPos > -1)
                        tags.remove(stackPos);
                }
                
                int index = docBuff.indexOf(ANGLE_BRACKET_CLOSE, i+1);
                // tags.push(tag);
            }

            // we found a period not inside an html tag; truncate and return
            else if (ch == '.')
            {
                docBuff.delete(i+1, docBuff.length());
                break;
            }
            
            else
                i++;
        }

        return docBuff.append(resolveTags(tags)).toString();
    }
    
    private String resolveTags(Stack<String> tags)
    {
        if (tags.size() == 0)
            return "";
        
        StringBuffer tagBuff = new StringBuffer();
        
        for (String tag: tags)
        {
            tagBuff.append(ANGLE_BRACKET_OPEN).append(SLASH)
                .append(tag).append(ANGLE_BRACKET_CLOSE);
        }
        
        return tagBuff.toString();
    }
    
    public ITreeItem[] getDiagrams()
    {
        ProjectTreeBuilderImpl builder =
                new ProjectTreeBuilderImpl(new DefaultNodeFactory());
        ArrayList<ITreeItem> list = new ArrayList();
        builder.retrieveDiagramsForElement(getElement(), getElement(), list);
        
        ITreeItem[] a = new ITreeItem[list.size()];
        return list.toArray(a);
    }
    
    
    public String getLinkTo(IElement element)
    {
        return ReportTask.getPathToReportRoot(getElement()) + ReportTask.getLinkTo(element);
    }
    
    
    public String getLinkToDiagram(IProxyDiagram item)
    {
        String fullname = item.getFilename();
        String name = StringUtilities.getFileName(fullname);
        return ReportTask.getPathToReportRoot(getElement()) +
                ReportTask.getLinkPathToDiagram(item);
    }
    
    public String getOwningPackageName()
    {
        return getElement().getOwningPackage().getFullyQualifiedName(false);
    }
    
    
    public String getSummaryHeader(String anchorName, String header)
    {
        StringBuilder buff = new StringBuilder();
        
        buff.append("<A NAME=\"" + anchorName + "\"></A>\r\n"); // NOI18N
        buff.append("<TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"3\" CELLSPACING=\"0\" SUMMARY=\"\">\r\n"); // NOI18N
        buff.append("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\r\n"); // NOI18N
        buff.append("<TH ALIGN=\"left\" COLSPAN=\"2\"><FONT SIZE=\"+2\">\r\n"); // NOI18N
        buff.append("<B>" + header + "</B></FONT></TH>\r\n"); // NOI18N
        buff.append("</TR>\r\n"); // NOI18N
        
        return buff.toString();
    }
    
    
    public String getDetailHeader(String anchor, String header)
    {
        StringBuilder buff = new StringBuilder();
        buff.append("<A NAME=\"" + anchor + "\"></A>\r\n"); // NOI18N
        buff.append("<TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"3\" CELLSPACING=\"0\" SUMMARY=\"\">\r\n"); // NOI18N
        buff.append("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\r\n"); // NOI18N
        buff.append("<TH ALIGN=\"left\" COLSPAN=\"1\"><FONT SIZE=\"+2\">\r\n"); // NOI18N
        buff.append("<B>" + header + "</B></FONT></TH>\r\n"); // NOI18N
        buff.append("</TR>\r\n"); // NOI18N
        buff.append("</TABLE>\r\n"); // NOI18N
        
        return buff.toString();
    }
    
    
    
    
    public String getVisibility(INamedElement element)
    {
        int v = element.getVisibility();
        return NbBundle.getMessage(ElementDataObject.class, "Visibility"+v); // NOI18N
    }
    
    
    protected boolean displayLink(IElement element)
    {
        ElementDataObject dataObject = DataObjectFactory.getDataObject(element);
        if (dataObject==null)
            return false;
        return true;
    }
    
    
    public String getStereoTypesSummary()
    {
        ETList<Object> stypes = getElement().getAppliedStereotypes();
        StringBuilder buff = new StringBuilder();
        if (stypes.size()>0)
        {
            buff.append("<!-- =========== STEREOTYPE SUMMARY =========== -->\r\n\r\n"); // NOI18N
            buff.append(getSummaryHeader("stereotype_summary", // NOI18N
                    NbBundle.getMessage(ElementDataObject.class, "Stereotype_Summary"))); // NOI18N
            
            for (int i=0; i<stypes.size(); i++)
            {
                IStereotype stype = (IStereotype)stypes.get(i);
                buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
                buff.append("<TD ALIGN=\"left\" VALIGN=\"top\" WIDTH=\"15%\">\r\n"); // NOI18N
                buff.append(stype.getName() + "\r\n"); // NOI18N
                buff.append("</TD>\r\n</TR>\r\n"); // NOI18N
            }
            buff.append("</TABLE>\r\n&nbsp;\r\n"); // NOI18N
        }
        return buff.toString();
    }
    
    
    public String getTaggedValueSummary()
    {
        ETList<ITaggedValue> taggedValues = getElement().getAllTaggedValues();
        for (int i=0; i<taggedValues.size(); i++)
        {
            ITaggedValue value = taggedValues.get(i);
            if (value.getName().equals("documentation")) // NOI18N
                taggedValues.remove(value);
        }
        
        StringBuilder buff = new StringBuilder();
        
        if (taggedValues.size()>0)
        {
            buff.append("<!-- =========== TAGGED VALUE SUMMARY =========== -->\r\n\r\n"); // NOI18N
            buff.append(getSummaryHeader("tagged_value_summary", // NOI18N
                    NbBundle.getMessage(ElementDataObject.class, "Tagged_Value_Summary"))); // NOI18N
            
            for (int i=0; i<taggedValues.size(); i++)
            {
                ITaggedValue value = taggedValues.get(i);
                buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
                buff.append("<TD ALIGN=\"left\" VALIGN=\"top\" WIDTH=\"15%\">\r\n"); // NOI18N
                buff.append(value.getName() + "</TD>\r\n"); // NOI18N
                buff.append("<TD>" + value.getDataValue() + "\r\n"); // NOI18N
                buff.append("</TD>\r\n</TR>\r\n"); // NOI18N
            }
            buff.append("</TABLE>\r\n&nbsp;\r\n"); // NOI18N
        }
        return buff.toString();
    }
    
    
    public String getConstraintsSummary()
    {
        ETList<IConstraint> constraints = getElement().getOwnedConstraints();
        StringBuilder buff = new StringBuilder();
        if (constraints.size()>0)
        {
            buff.append("<!-- =========== CONSTRAINT SUMMARY =========== -->\r\n\r\n"); // NOI18N
            buff.append(getSummaryHeader("constraint_summary", // NOI18N
                    NbBundle.getMessage(ElementDataObject.class, "Constraint_Summary"))); // NOI18N
            
            for (int i=0; i<constraints.size(); i++)
            {
                IConstraint constraint = constraints.get(i);
                buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
                buff.append("<TD ALIGN=\"left\" VALIGN=\"top\" WIDTH=\"15%\">\r\n"); // NOI18N
                buff.append(constraint.getName() + "</TD>\r\n"); // NOI18N
                buff.append("<TD>" + constraint.getExpression() + "\r\n"); // NOI18N
                buff.append("</TD>\r\n</TR>\r\n"); // NOI18N
            }
            buff.append("</TABLE>\r\n&nbsp;\r\n"); // NOI18N
        }
        return buff.toString();
    }
    
    
    
    public String getDiagramSummary()
    {
        StringBuilder buff = new StringBuilder();
        ITreeItem[] diagrams = getDiagrams();
        
        if (diagrams.length>0)
        {
            buff.append("<!-- =========== DIAGRAM SUMMARY =========== -->\r\n\r\n"); // NOI18N
            
            buff.append(getSummaryHeader("diagram_summary", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Diagram_Summary"))); // NOI18N
            
            for (int i=0; i<diagrams.length; i++)
            {
                IDiagram diagram = 
                    diagrams[i].getData().getDiagram().getDiagram();
                
                if (diagram == null)
                {
                    diagram = ReportTask.loadDiagram(
                        diagrams[i].getData().getDiagram().getFilename());
                }
                
                buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
                
                String imageName = ImageUtil.instance()
                    .getDiagramTypeImageName(diagram.getDiagramKind());

                buff.append("<TD WIDTH=\"25%\"><IMG src=\"" +  // NOI18N
                    ReportTask.getPathToReportRoot(getElement()) +
                    "images/" + imageName + "\" border=n>&nbsp;<B><A HREF=\"" + // NOI18N
                    getLinkToDiagram(diagrams[i].getData().getDiagram()) +
                    "\">" + diagram.getName() + "</A></B></TD>\r\n"); // NOI18N
                
                buff.append("<TD>" + getBriefDocumentation(diagram.getDocumentation()) 
                    + "</TD>\r\n"); // NOI18N
                
                buff.append("</TR>\r\n"); // NOI18N
            }
            buff.append("</TABLE>\r\n&nbsp;\r\n"); // NOI18N
        }
        return buff.toString();
    }
    
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility
        };
    }
    
    protected Object[] getPropertyValues()
    {
        if (getElement() instanceof INamedElement)
            return new Object[] {((INamedElement)getElement()).getAlias(),
            getVisibility((INamedElement)getElement())};
        else
            return new Object[] {"&nbsp;", "&nbsp;"}; // NOI18N
    }
    
    
    public String getProperties()
    {
        return getProperties(true);
    }
    
    public String getProperties(boolean displayHeader)
    {
        StringBuilder buff = new StringBuilder();
        
        buff.append("<!-- =========== PROPERTY SUMMARY =========== -->\r\n"); // NOI18N
        if (displayHeader)
            buff.append(getSummaryHeader("property_summary", // NOI18N
                    NbBundle.getMessage(ElementDataObject.class, "Properties"))); // NOI18N
        else
        {
            buff.append("<A NAME=\"" + "property_summary" + "\"></A>\r\n"); // NOI18N
            buff.append("<TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"3\" CELLSPACING=\"0\" SUMMARY=\"\">\r\n"); // NOI18N
            
        }
        
        String[] properties = getPropertyNames();
        Object[] values = getPropertyValues();
        
        for (int i=0; i<properties.length; i++)
        {
            String property = properties[i];
            Object value = values[i];
            buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
            buff.append("<TD WIDTH=\"15%\"><B>" + property + "</B></TD>\r\n"); // NOI18N
            if (value instanceof Boolean)
            {
                if (((Boolean)value).booleanValue() == true)
                {
                    buff.append("<TD><IMG src=\"" + // NOI18N
                            ReportTask.getPathToReportRoot(getElement()) +
                            "images/checked.png" + "\" border=n></TD>\r\n"); // NOI18N
                }
                else
                    buff.append("<TD><IMG src=\"" + // NOI18N
                            ReportTask.getPathToReportRoot(getElement()) +
                            "images/unchecked.png" + "\" border=n></TD>\r\n"); // NOI18N
                
            }
            else if (value instanceof IElement && displayLink((IElement)value))
            {
                buff.append("<TD><B><A HREF=\"" + getLinkTo((IElement)value) + "\">" + // NOI18N
                        value.toString() + "</A></B></TD>\r\n"); // NOI18N
            }
            else if (value!=null)
            {
                String v = value.toString();
                if (v.equals("")) // NOI18N
                    v = "&nbsp;"; // NOI18N
                buff.append("<TD>" + v + "</TD>\r\n"); // NOI18N
            }
            else
                buff.append("<TD>&nbsp;</TD>\r\n"); // NOI18N
            
            buff.append("</TR>\r\n"); // NOI18N
        }
        buff.append("</TABLE>\r\n&nbsp;\r\n"); // NOI18N
        
        return buff.toString();
    }
    
    
    public String getEnclosingDiagrams()
    {
        StringBuilder buff = new StringBuilder();
        
        DiagramBuilder diagramBuilder = new DiagramBuilder();
        ETList<IPresentationTarget> targets = diagramBuilder.getPresentationTargets(getElement());
        ArrayList diagrams = new ArrayList();
        
        if (targets.size()>0)
        {
            buff.append("<DL>\r\n"); // NOI18N
            
            buff.append("<DT><B>" + NbBundle.getMessage(ElementDataObject.class,
                "All_Enclosing_Diagrams") + ":</B><DD>"); // NOI18N
            
            for (int i=0; i<targets.size(); i++)
            {
                IProxyDiagram proxy = targets.get(i).getProxyDiagram();
                
                if (!diagrams.contains(proxy.getFilename()))
                {
                    diagrams.add(proxy.getFilename());
                    
                    if (proxy.getDiagram() == null)
                        ReportTask.loadDiagram(proxy.getFilename());
                    
                    String imageName = ImageUtil.instance()
                        .getDiagramTypeImageName(
                        proxy.getDiagram().getDiagramKind());

                    ReportTask.addToImageList(imageName);

                    buff.append("<IMG src=\"" + // NOI18N
                        ReportTask.getPathToReportRoot(getElement()) +
                        "images/" + imageName + "\" border=n>&nbsp;<A HREF=\"" + // NOI18N
                        getLinkToDiagram(proxy) + "\" >" + // NOI18N
                        proxy.getName() + "</A>&nbsp;&nbsp"); // NOI18N
                }
            }
            
            buff.append("</DD></DT>\r\n</DL>\r\n"); // NOI18N
        }
        
        return buff.toString();
    }
    
    
    public String getDependencies()
    {
        if (!(getElement() instanceof INamedElement))
            return "";
        
        StringBuilder buff = new StringBuilder();
        
        HashMap<INamedElement, ArrayList<String>> clients = 
            new HashMap<INamedElement, ArrayList<String>>();
        
        HashMap<INamedElement, ArrayList<String>> suppliers = 
            new HashMap<INamedElement, ArrayList<String>>();
        
        // client dependency
        ETList<IDependency> dependencies = ((INamedElement)getElement()).getClientDependencies();
        if (dependencies.size() > 0)
        {
            for (int i=0; i < dependencies.size(); i++)
            {
                INamedElement dependent = dependencies.get(i).getSupplier();

                // filter out self
                if (dependent != null && !dependent.getXMIID().equals(getElement().getXMIID()))
                {
                    if (suppliers.get(dependent) == null)
                        suppliers.put(dependent, new ArrayList<String>());
                            
                    suppliers.get(dependent).add(
                        dependencies.get(i).getExpandedElementType());
                }
            }
        }
        
        // supplier dependency
        dependencies = ((INamedElement)getElement()).getSupplierDependencies();
        if (dependencies.size() > 0)
        {
            for (int i=0; i<dependencies.size(); i++)
            {
                INamedElement dependent = dependencies.get(i).getClient();

                // filter out self
                if (dependent != null && !dependent.getXMIID().equals(getElement().getXMIID()))
                {
                    if (clients.get(dependent) == null)
                        clients.put(dependent, new ArrayList<String>());
                            
                    clients.get(dependent).add(
                        dependencies.get(i).getExpandedElementType());
                }
            }
        }
        
        // list all client classes
        if (clients.size() > 0)
        {
            buff.append("<DL>\r\n"); // NOI18N
            
            buff.append("<DT><B>" + // NOI18N
                NbBundle.getMessage(ElementDataObject.class, 
                "All_Dependency_Clients") + ":</B>"); // NOI18N
            
            Iterator<INamedElement> keys = clients.keySet().iterator();
            
            while (keys.hasNext())
            {
                INamedElement client = keys.next();
                ArrayList<String> links = clients.get(client);
                                
                for (String type: links)
                { 
                    String name = client.getName();

                    if (name == null || name.equals("")) // NOI18N
                        name = client.getExpandedElementType();

                    String imageName = CommonResourceManager.instance()
                        .getIconDetailsForElementType(type);

                    if (imageName.lastIndexOf("/") > -1) // NOI18N
                        imageName = imageName.substring(imageName.lastIndexOf("/")+1); // NOI18N

                    ReportTask.addToImageList(imageName);

                    String img = "<DD><IMG SRC=\"" + ReportTask.getPathToReportRoot( // NOI18N
                        getElement()) + "images/" + imageName + // NOI18N
                        "\" ALT=\"(" + type + ") \">"; // NOI18N

                    buff.append(img + "&nbsp;" + type + "&nbsp;<A HREF=\"" +  // NOI18N
                        getLinkTo(client) + "\" title=\"dependency in " + // NOI18N
                        client.getOwningPackage().getFullyQualifiedName(false) +
                        "\">" + name + "</A>"); // NOI18N
                    
                    buff.append("</DD>\r\n"); // NOI18N
                }
            }         
            
            buff.append("</DL>\r\n"); // NOI18N
        }
        
        // list all supplier classes
        if (suppliers.size() > 0)
        {
            buff.append("<DL>\r\n"); // NOI18N
            
            buff.append("<DT><B>" + NbBundle.getMessage(ElementDataObject.class,  // NOI18N
                "All_Dependency_Suppliers") + ":</B>"); // NOI18N
            
            Iterator<INamedElement> keys = suppliers.keySet().iterator();
            
            while (keys.hasNext())
            {
                INamedElement supplier = keys.next();
                ArrayList<String> links = suppliers.get(supplier);
                
                for (String type: links)
                {
                    String name = supplier.getName();

                    if (name==null || name.equals(""))// NOI18N
                        name = supplier.getExpandedElementType();

                    String imageName = CommonResourceManager.instance()
                        .getIconDetailsForElementType(type);

                    if (imageName.lastIndexOf("/") > -1) // NOI18N
                        imageName = imageName.substring(imageName.lastIndexOf("/")+1); // NOI18N

                    ReportTask.addToImageList(imageName);

                    String img = "<DD><IMG SRC=\"" + ReportTask.getPathToReportRoot( // NOI18N
                        getElement()) + "images/" + imageName +  // NOI18N
                        "\" ALT=\"(" + type + ") \">"; // NOI18N

                    buff.append(img + "&nbsp;" + type + "&nbsp;<A HREF=\"" +  // NOI18N
                        getLinkTo(supplier) + "\" title=\"dependency in " + // NOI18N
                        supplier.getOwningPackage().getFullyQualifiedName(false) +
                        "\">" + name + "</A>"); // NOI18N    
                    buff.append("</DD>\r\n"); // NOI18N
                }
            }
            
            buff.append("</DL>\r\n"); // NOI18N
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
            out.write("<BODY BGCOLOR=\"white\">\r\n"); // NOI18N
            out.write(getNavBar());
            out.write("<HR>\r\n"); // NOI18N
            out.write("<H2>\r\n"); // NOI18N
            out.write("<FONT SIZE=\"-1\">" + getOwningPackageName() + "</FONT>\r\n"); // NOI18N
            out.write("<BR>\r\n"); // NOI18N
            if (getElement() instanceof INamedElement)
                out.write(getElementType() + " " + ((INamedElement)getElement()).getName() + "</H2>\r\n"); // NOI18N
            else
                out.write(getElementType() + "</H2>\r\n"); // NOI18N
            
            out.write(getDependencies());
            
            out.write(getEnclosingDiagrams());
            
            out.write(getDocumentation());
            
            // property summary
            out.write(getProperties());
            
            // stereotype summary
            out.write(getStereoTypesSummary());
            
            // tagged value summary
            out.write(getTaggedValueSummary());
            
            // constraint summary
            out.write(getConstraintsSummary());
            
            
            out.write("<HR>\r\n"); // NOI18N
            out.write(getNavBar());
            out.write("</BODY>\r\n</HTML>"); // NOI18N
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
    
    
    public String getHTMLHeader()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\r\n"); // NOI18N
        buffer.append("<HTML>\r\n"); // NOI18N
        buffer.append("<HEAD>\r\n"); // NOI18N
        buffer.append("<TITLE>" + getElement().getElementType() + "</TITLE>\r\n"); // NOI18N
        buffer.append("<META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=" + getCharset() + "\">\r\n"); // NOI18N
        buffer.append("<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"" + // NOI18N
                ReportTask.getPathToReportRoot(getElement()) + "stylesheet.css\" TITLE=\"Style\">"); // NOI18N
        buffer.append("</HEAD>\r\n"); // NOI18N
        
        return buffer.toString();
    }
    
    
    public String getNavBar()
    {
        StringBuilder buffer = new StringBuilder(navBar1);
        
        buffer.append("  <TD BGCOLOR=\"#FFFFFF\" CLASS=\"NavBarCell1\"> &nbsp;<A HREF=\"" + ReportTask.getPathToReportRoot(getElement()) + "overview-summary.html\"><FONT CLASS=\"NavBarFont1\"><B>" + // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Overview") + "</B></FONT></A>&nbsp;</TD>\r\n"); // NOI18N
        if (getElement() instanceof IPackage)
        {
            buffer.append(navBar2);
        }
        else if (getElement() instanceof IDiagram)
        {
            buffer.append(navBar3);
        }
        else
        {
            buffer.append(navBar4);
        }
        
        buffer.append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"" + ReportTask.getPathToReportRoot(getElement()) + "help.html\"><FONT CLASS=\"NavBarFont1\"><B>" + NbBundle.getMessage(ElementDataObject.class, "Header_Help") + "</B></FONT></A>&nbsp;</TD>\r\n"); // NOI18N
        buffer.append(navBar5);
        
        return buffer.toString();
        
    }
    
    
    
    protected String getCharset()
    {
        return System.getProperty("file.encoding"); // NOI18N
    }
}
