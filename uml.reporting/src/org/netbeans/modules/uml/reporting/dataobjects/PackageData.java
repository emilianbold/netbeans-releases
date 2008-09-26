/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.uml.reporting.dataobjects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.project.ui.nodes.AbstractModelElementNode;
import org.netbeans.modules.uml.reporting.ReportTask;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
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
            OutputStreamWriter out = new OutputStreamWriter(fo, ENCODING);
            
            out.write(getHTMLHeader());
            out.write("<BODY BGCOLOR=\"white\">\r\n\r\n"); // NOI18N
            out.write(getNavBar());
            out.write("<HR>\r\n<H2>\r\n"); // NOI18N
            
            if (getElement().getElementType().equals(AbstractModelElementNode.ELEMENT_TYPE_PROJECT))
                out.write(NbBundle.getMessage(PackageData.class,"package")  + " &lt;" + // NOI18N
                        NbBundle.getMessage(ReportTask.class,"default_package") + // NOI18N
                        ">\r\n"); // NOI18N
            else
            {
                out.write(getElementType() + " " +  // NOI18N
                    getElement().getFullyQualifiedName(false) + "\r\n"); // NOI18N
            }
            
            out.write("</H2>\r\n"); // NOI18N
            
            out.write(getDependencies());
            out.write(getEnclosingDiagrams());
            
            out.write(getDocumentation() + "\r\n"); // NOI18N
            out.write("<BR>\r\n"); // NOI18N
            
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
                    out.write("<TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"3\" CELLSPACING=\"0\" SUMMARY=\"\"> \r\n"); // NOI18N
                    out.write("<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\r\n"); // NOI18N
                    out.write("<TH ALIGN=\"left\" COLSPAN=\"2\"><FONT SIZE=\"+2\">\r\n"); // NOI18N
                    
                    out.write("<B>" + NbBundle.getMessage(PackageData.class, // NOI18N
                        "ElementType_" + type) + " " + // NOI18N
                        NbBundle.getMessage(PackageData.class, "Header_Summary") + // NOI18N
                        "</B></FONT></TH>\r\n"); // NOI18N
                    
                    out.write("</TR>\r\n"); // NOI18N
                    
                    for (int j=0; j<elements.length; j++)
                    {
                        INamedElement element = elements[j];
                        
                        String doc = getBriefDocumentation(element.getDocumentation());
                        
                        String name = element.getName();
                        if (name==null || name.equals("")) // NOI18N
                            name = NbBundle.getMessage(PackageData.class, "unnamed"); // NOI18N
                        if (doc == null || doc.trim().equals("")) // NOI18N
                            doc = "&nbsp;"; // NOI18N
                        out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
                        out.write("<TD WIDTH=\"15%\"><B><A HREF=\"" + getLinkTo(element) + // NOI18N
                                "\" title=\"" + type + " in " + name +
                                "\">" + name + "</A></B></TD>\r\n"); // NOI18N
                        out.write("<TD>" + doc + "</TD>\r\n"); // NOI18N
                        out.write("</TR>\r\n"); // NOI18N
                    }
                    out.write("</TABLE>\r\n&nbsp;\r\n<P>\r\n"); // NOI18N
                }
            }
            
            // diagrams
            out.write(getDiagramSummary());
            
            out.write("<HR>\r\n"); // NOI18N
            out.write(getNavBar());
            out.write("</BODY>\r\n</HTML>"); // NOI18N
            out.close();
            result = result && true;
        }
        catch (Exception e)
        {
            Logger.getLogger(ElementDataObject.class.getName()).log(
                    Level.SEVERE, getElement().getElementType() + " - " +  getElement().getNameWithAlias(), e);
            result = false;
        }
        return result;
    }
    
    
    public boolean generatePackageFrameHTML(File file)
    {
        boolean result = false;
        
        try
        {
            FileOutputStream fo = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(fo, ENCODING);
            
            out.write(getHTMLHeader());
            out.write("<BODY BGCOLOR=\"white\">"); // NOI18N
            
            out.write("<img src=\"" + // NOI18N
                ReportTask.getPathToReportRoot(getElement()) 
                + "images/package.png" + "\" border=n>&nbsp"); // NOI18N
            
            out.write("<FONT size=\"+1\" CLASS=\"FrameTitleFont\">"); // NOI18N
            
            if (getElement().getElementType().equals(AbstractModelElementNode.ELEMENT_TYPE_PROJECT))
            {
                out.write("<A HREF=\"" + getLinkTo(getElement()) + "\" target=\"elementframe\">" + // NOI18N
                    "&lt;" + NbBundle.getMessage(ReportTask.class,"default_package") + // NOI18N
                    ">" + "</A></FONT>\r\n"); // NOI18N
            }
            else
            {
                out.write("<A HREF=\"" + getLinkTo(getElement()) + "\" target=\"elementframe\">" + // NOI18N
                    pkg.getFullyQualifiedName(false) + "</A></FONT>\r\n"); // NOI18N
            }
            
            // different element type
            for (int i=0; i<elementTypes.length; i++)
            {
                String type = elementTypes[i];
                INamedElement[] elements = getElementByType(type);
                
                if (elements.length > 0)
                {
                    String category = NbBundle.getMessage(
                        PackageData.class, "Category_" + elementTypes[i]); // NOI18N
                    
                    out.write("<TABLE BORDER=\"0\" WIDTH=\"100%\">"); // NOI18N
                    out.write("<TR>\r\n"); // NOI18N
                    
                    String imageName = CommonResourceManager.instance()
                        .getIconDetailsForElementType(type);
                    
                    if (imageName.lastIndexOf("/") > -1)
                        imageName = imageName.substring(imageName.lastIndexOf("/")+1); // NOI18N
                    
                    ReportTask.addToImageList(imageName);

                    out.write("<TD NOWRAP><img src=\"" + // NOI18N
                        ReportTask.getPathToReportRoot(getElement()) +
                        "images/" + imageName + // NOI18N
                        "\" border=n>&nbsp;<FONT size=\"+1\" CLASS=\"FrameHeadingFont\">" + // NOI18N
                        category + "</FONT>&nbsp;<FONT CLASS=\"FrameItemFont\">\r\n"); // NOI18N
                    
                    for (int j=0; j<elements.length; j++)
                    {
                        INamedElement element = elements[j];
                        String name = element.getName();
                        
                        if (name==null || name.equals(""))
                            name = NbBundle.getMessage(PackageData.class, "unnamed"); // NOI18N
                        
                        out.write("<BR>\r\n"); // NOI18N
                        
                        out.write("<A HREF=\"" + getLinkTo(element) + "\" title=\"" + // NOI18N
                            category + " in " + getElementName() + // NOI18N
                            "\" target=\"elementframe\">" + // NOI18N
                            name + "</A>\r\n"); // NOI18N
                    }
                    
                    out.write("</FONT></TD>\r\n"); // NOI18N
                    out.write("</TR>\r\n</TABLE>\r\n\r\n<BR>\r\n"); // NOI18N
                }
            }
            
            // diagrams
            ITreeItem[] items = getDiagrams();
            
            String type = NbBundle.getMessage(
                PackageData.class, "Category_Diagram"); // NOI18N
            
            if (items.length>0)
            {
                out.write("<TABLE BORDER=\"0\" WIDTH=\"100%\">"); // NOI18N
                out.write("<TR>\r\n"); // NOI18N
                out.write("<TD NOWRAP><FONT size=\"+1\" CLASS=\"FrameHeadingFont\">" + // NOI18N
                    type + "</FONT>&nbsp;<FONT CLASS=\"FrameItemFont\">\r\n"); // NOI18N
                
            }
            
            for (int i=0; i<items.length; i++)
            {
                IDiagram diagram = items[i].getData().getDiagram().getDiagram();
                if (diagram == null)
                {
                    diagram = ReportTask.loadDiagram(
                        items[i].getData().getDiagram().getFilename());
                }
                
                if (diagram != null)
                {
                    out.write("<BR>\r\n"); // NOI18N
                    
                    String imageName = ImageUtil.instance()
                        .getDiagramTypeImageName(diagram.getDiagramKind());
                    
                    if (imageName.lastIndexOf("/") > -1)
                        imageName = imageName.substring(imageName.lastIndexOf("/")+1); // NOI18N
                    
                    ReportTask.addToImageList(imageName);

                    out.write("<img src=\"" + // NOI18N
                        ReportTask.getPathToReportRoot(getElement()) + 
                        "images/" + imageName +
                        "\" border=n>&nbsp;<A HREF=\"" + // NOI18N
                        getLinkToDiagram(items[i].getData().getDiagram()) +
                        "\" title=\"" + type + " in " + getElementName() + // NOI18N
                        "\" target=\"elementframe\">" + // NOI18N
                        diagram.getName() + "</A>\r\n"); // NOI18N
                }
            }
            
            out.write("</FONT></TD>\r\n"); // NOI18N
            out.write("</TR>\r\n</TABLE>\r\n\r\n"); // NOI18N
            out.write("</BODY>\r\n</HTML>"); // NOI18N
            out.close();
            result = true;
        }
        
        catch (Exception e)
        {
            Logger.getLogger(ElementDataObject.class.getName()).log(
                    Level.SEVERE, getElement().getElementType() + " - " +  getElement().getNameWithAlias(), e);
            result = false;
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
