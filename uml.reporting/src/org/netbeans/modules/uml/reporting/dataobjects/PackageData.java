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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.project.ui.nodes.AbstractModelElementNode;
import org.netbeans.modules.uml.reporting.ReportTask;
import org.netbeans.modules.uml.ui.support.diagramsupport.DiagramTypesManager;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


public class PackageData extends ElementDataObject
{
    private IPackage pkg;
    private String htmlFilePath;
    private DisplayNameComparator comparator = new DisplayNameComparator();
    
    private String[] elementTypes = {
        AbstractModelElementNode.ELEMENT_TYPE_INTERFACE,
        AbstractModelElementNode.ELEMENT_TYPE_CLASS,
        AbstractModelElementNode.ELEMENT_TYPE_DERIVATION_CLASSIFIER,
        AbstractModelElementNode.ELEMENT_TYPE_ASSOCIATION_CLASS,
        AbstractModelElementNode.ELEMENT_TYPE_DATA_TYPE,
        AbstractModelElementNode.ELEMENT_TYPE_ALIASED_TYPE,
        AbstractModelElementNode.ELEMENT_TYPE_ENUMERATION,
        AbstractModelElementNode.ELEMENT_TYPE_STEREOTYPE,
        AbstractModelElementNode.ELEMENT_TYPE_ACTIVITY,
        AbstractModelElementNode.ELEMENT_TYPE_ACTOR,
        AbstractModelElementNode.ELEMENT_TYPE_NODE,
        AbstractModelElementNode.ELEMENT_TYPE_DEPLOYMENTSPECIFICATION,
        AbstractModelElementNode.ELEMENT_TYPE_COMPONENT,
        AbstractModelElementNode.ELEMENT_TYPE_SOURCE_FILE_ARTIFACT,
        AbstractModelElementNode.ELEMENT_TYPE_USECASE,
        AbstractModelElementNode.ELEMENT_TYPE_ACTIVITYGROUP,
        AbstractModelElementNode.ELEMENT_TYPE_FINALNODE,
        AbstractModelElementNode.ELEMENT_TYPE_FINALSTATE,
        AbstractModelElementNode.ELEMENT_TYPE_FORKNODE,
        AbstractModelElementNode.ELEMENT_TYPE_INITIALNODE,
        AbstractModelElementNode.ELEMENT_TYPE_INVOCATIONNODE,
        AbstractModelElementNode.ELEMENT_TYPE_PART_FACADE,
        AbstractModelElementNode.ELEMENT_TYPE_COMMENT,
        AbstractModelElementNode.ELEMENT_TYPE_COLLABORATION,
        AbstractModelElementNode.ELEMENT_TYPE_ARTIFACT,
        AbstractModelElementNode.ELEMENT_TYPE_LIFELINE,
        AbstractModelElementNode.ELEMENT_TYPE_INTERACTION,
        AbstractModelElementNode.ELEMENT_TYPE_STATE_MACHINE,
        AbstractModelElementNode.ELEMENT_TYPE_PACKAGE
    };
    
    public PackageData()
    {
        
    }
    
    public PackageData(IPackage element)
    {
        pkg = element;
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof IPackage)
            pkg = (IPackage)e;
    }
    
    public IPackage getElement()
    {
        return pkg;
    }
    
    
    public INamedElement[] getElementByType(String type)
    {
        if (getElement()==null)
            return null;
        
        ArrayList list = new ArrayList();
        ETList<INamedElement> elements = getElement().getOwnedElements();
        for (int i=0; i<elements.getCount(); i++)
        {
            INamedElement element = elements.get(i);
            if (element.getElementType().equals(type))
            {
                list.add(element);
            }
        }
        INamedElement[] a = new INamedElement[list.size()];
        list.toArray(a);
        
        Arrays.sort(a, comparator);
        return a;
    }
    
    public String getElementName()
    {
        return getElement().getName();
    }
    
    
    public String getHTMLFilePath()
    {
        return htmlFilePath;
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
        return new Object[] {getElement().getAlias(),
        getVisibility(getElement())};
    }
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        boolean result = false;
        
        htmlFilePath = file.getAbsolutePath();
        int index = htmlFilePath.indexOf(ReportTask.HTML_EXT);
        String frame = file.getAbsolutePath().substring(0, index) + ReportTask.FRAME + ReportTask.HTML_EXT;
        File frameFile = new File(frame);
        result = generatePackageFrameHTML(frameFile);
        
        
        try
        {
            FileOutputStream fo = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(fo);
            
            out.write(getHTMLHeader());
            out.write("<BODY BGCOLOR=\"white\">\r\n\r\n");
            out.write(getNavBar());
            out.write("<HR>\r\n<H2>\r\n");
            if (getElement().getElementType().equals(AbstractModelElementNode.ELEMENT_TYPE_PROJECT))
                out.write(NbBundle.getMessage(PackageData.class,"package")  + " &lt;" +
                        NbBundle.getMessage(ReportTask.class,"default_package") +
                        ">\r\n");
            else
                out.write(getElementType() + " " + getElement().getFullyQualifiedName(false) + "\r\n");
            out.write("</H2>\r\n");
            
            out.write(getDependencies());
            out.write(getEnclosingDiagrams());
            
            out.write(getDocumentation() + "\r\n");
            out.write("<BR>\r\n");
            
            // property summary
            out.write(getProperties());
            
            // stereotype summary
            out.write(getStereoTypesSummary());
            
            // tagged value summary
            out.write(getTaggedValueSummary());
            
            // constraint summary
            out.write(getConstraintsSummary());
            
            for (int i=0; i<elementTypes.length; i++)
            {
                String type = elementTypes[i];
                INamedElement[] elements = getElementByType(type);
                if (elements.length > 0)
                {
                    out.write("<TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"3\" CELLSPACING=\"0\" SUMMARY=\"\"> \r\n");
                    out.write("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\r\n");
                    out.write("<TH ALIGN=\"left\" COLSPAN=\"2\"><FONT SIZE=\"+2\">\r\n");
                    out.write("<B>" + NbBundle.getMessage(PackageData.class,
                            "ElementType_" + type) + " " +
                            NbBundle.getMessage(PackageData.class, "Header_Summary") +
                            "</B></FONT></TH>\r\n");
                    out.write("</TR>\r\n");
                    
                    for (int j=0; j<elements.length; j++)
                    {
                        INamedElement element = elements[j];
                        
                        String doc = getBriefDocumentation(
                            StringUtilities.unescapeHTML(
                            element.getDocumentation()));
                        
                        String name = element.getName();
                        if (name==null || name.equals(""))
                            name = NbBundle.getMessage(PackageData.class, "unnamed");
                        if (doc == null || doc.trim().equals(""))
                            doc = "&nbsp;";
                        out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                        out.write("<TD WIDTH=\"15%\"><B><A HREF=\"" + getLinkTo(element) +
                                "\" title=\"" + type + " in " + name +
                                "\">" + name + "</A></B></TD>\r\n");
                        out.write("<TD>" + doc + "</TD>\r\n");
                        out.write("</TR>\r\n");
                    }
                    out.write("</TABLE>\r\n&nbsp;\r\n<P>\r\n");
                }
            }
            
            // diagrams
            out.write(getDiagramSummary());
            
            out.write("<HR>\r\n");
            out.write(getNavBar());
            out.write("</BODY>\r\n</HTML>");
            out.close();
            result = result && true;
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
    
    
    public boolean generatePackageFrameHTML(File file)
    {
        boolean result = false;
        try
        {
            FileOutputStream fo = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(fo);
            
            out.write(getHTMLHeader());
            out.write("<BODY BGCOLOR=\"white\">");
            out.write("<img src=\"" + ReportTask.getPathToReportRoot(getElement()) + "images/Package.png" + "\" border=n>&nbsp");
            out.write("<FONT size=\"+1\" CLASS=\"FrameTitleFont\">");
            
            if (getElement().getElementType().equals(AbstractModelElementNode.ELEMENT_TYPE_PROJECT))
            {
                out.write("<A HREF=\"" + getLinkTo(getElement()) + "\" target=\"elementframe\">" +
                        "&lt;" + NbBundle.getMessage(ReportTask.class,"default_package") +
                        ">" + "</A></FONT>\r\n");
            }
            else
            {
                out.write("<A HREF=\"" + getLinkTo(getElement()) + "\" target=\"elementframe\">" +
                        pkg.getFullyQualifiedName(false) + "</A></FONT>\r\n");
            }
            
            // different element type
            for (int i=0; i<elementTypes.length; i++)
            {
                String type = elementTypes[i];
                INamedElement[] elements = getElementByType(type);
                if (elements.length > 0)
                {
                    String category = NbBundle.getMessage(PackageData.class, "Category_" + elementTypes[i]);
                    out.write("<TABLE BORDER=\"0\" WIDTH=\"100%\">");
                    out.write("<TR>\r\n");
                    out.write("<TD NOWRAP><img src=\"" +
                            ReportTask.getPathToReportRoot(getElement()) +
                            "images/" + type + ".png" +
                            "\" border=n>&nbsp;<FONT size=\"+1\" CLASS=\"FrameHeadingFont\">" +
                            category + "</FONT>&nbsp;<FONT CLASS=\"FrameItemFont\">\r\n");
                    
                    for (int j=0; j<elements.length; j++)
                    {
                        INamedElement element = elements[j];
                        String name = element.getName();
                        if (name==null || name.equals(""))
                            name = NbBundle.getMessage(PackageData.class, "unnamed");
                        out.write("<BR>\r\n");
                        out.write("<A HREF=\"" + getLinkTo(element) + "\" title=\"" +
                                category + " in " + getElementName() +
                                "\" target=\"elementframe\">" +
                                name + "</A>\r\n");
                    }
                    out.write("</FONT></TD>\r\n");
                    out.write("</TR>\r\n</TABLE>\r\n\r\n<BR>\r\n");
                }
            }
            
            // diagrams
            ITreeItem[] items = getDiagrams();
            String type = NbBundle.getMessage(PackageData.class, "Category_Diagram");
            if (items.length>0)
            {
                out.write("<TABLE BORDER=\"0\" WIDTH=\"100%\">");
                out.write("<TR>\r\n");
                out.write("<TD NOWRAP><FONT size=\"+1\" CLASS=\"FrameHeadingFont\">" +
                        type + "</FONT>&nbsp;<FONT CLASS=\"FrameItemFont\">\r\n");
                
            }
            for (int i=0; i<items.length; i++)
            {
                IDiagram diagram = items[i].getData().getDiagram().getDiagram();
                if (diagram == null)
                    diagram = ReportTask.loadDiagram(items[i].getData().getDiagram().getFilename());
                if (diagram != null)
                {
                    out.write("<BR>\r\n");
                    out.write("<img src=\"" + ReportTask.getPathToReportRoot(getElement()) + "images/" + DiagramTypesManager.instance().
                            getOpenIcon(diagram) +
                            ".png" + "\" border=n>&nbsp;<A HREF=\"" +
                            getLinkToDiagram(items[i].getData().getDiagram()) +
                            "\" title=\"" + type + " in " + getElementName() +
                            "\" target=\"elementframe\">" + diagram.getName() + "</A>\r\n");
                }
            }
            out.write("</FONT></TD>\r\n");
            out.write("</TR>\r\n</TABLE>\r\n\r\n");
            
            out.write("</BODY>\r\n</HTML>");
            out.close();
            result = true;
        }
        catch (Exception e)
        {
            ErrorManager.getDefault().notify(e);
        }
        return result;
        
    }
    
    private class DisplayNameComparator implements Comparator
    {
        
        private Comparator COLLATOR = Collator.getInstance();
        
        public int compare(Object o1, Object o2)
        {
            
            if ( !( o1 instanceof INamedElement ) )
            {
                return 1;
            }
            if ( !( o2 instanceof INamedElement ) )
            {
                return -1;
            }
            
            INamedElement e1 = (INamedElement)o1;
            INamedElement e2 = (INamedElement)o2;
            
            return COLLATOR.compare(e1.getName(), e2.getName());
        }
    }
}
