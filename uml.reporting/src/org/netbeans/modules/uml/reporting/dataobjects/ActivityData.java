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
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.reporting.ReportTask;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 *
 * @author Sheryl
 */
public class ActivityData extends ClassData
{
    
    private IActivity element;
    
    /** Creates a new instance of ActivityData */
    public ActivityData()
    {
    }
    
    
    public void setElement(IElement activity)
    {
        if (activity instanceof IActivity)
            this.element = (IActivity)activity;
    }
    
    
    public IActivity getElement()
    {
        return element;
    }
    
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Final,
            Property_Transient,
            Property_Abstract,
            Property_Leaf,
            Property_ActivityKind,
            Property_SingleCopy };
    }
    
    protected Object[] getPropertyValues()
    {
        Boolean isFinal = new Boolean(getElement().getIsFinal());
        Boolean isTransient = new Boolean(getElement().getIsTransient());
        Boolean isAbstract = new Boolean(getElement().getIsAbstract());
        Boolean isLeaf = new Boolean(getElement().getIsLeaf());
        
        String kind = NbBundle.getMessage(
            ActivityData.class, "ActivityKind" + getElement().getKind()); // NOI18N
        
        Boolean isSingle = new Boolean(getElement().getIsSingleCopy());
        
        return new Object[] {getElement().getAlias(),
        getVisibility(getElement()), isFinal,
        isTransient, isAbstract, isLeaf, kind, isSingle};
    }
    
    private String getSummaryTable(ETList list)
    {
        StringBuilder buff = new StringBuilder();
        
        for (int i=0; i<list.size(); i++)
        {
            INamedElement node = (INamedElement)list.get(i);
            buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
            
            String doc = getBriefDocumentation(node.getDocumentation());
            if (doc == null || doc.trim().equals(""))
                doc = "&nbsp;"; // NOI18N
            buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
            buff.append("<TD WIDTH=\"15%\"><B><A HREF=\"" + getLinkTo(node) + // NOI18N
                    "\" title=\"" + node.getElementType() + " in " + node.getName() +
                    "\">" + node.getName() + "</A></B></TD>\r\n"); // NOI18N
            buff.append("<TD>" + doc + "</TD>\r\n"); // NOI18N
            buff.append("</TR>\r\n"); // NOI18N
        }
        
        buff.append("</TABLE>\r\n&nbsp;\r\n"); // NOI18N
        return buff.toString();
    }
    
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        ETList<IActivityNode> nodes = getElement().getNodes();
        ETList<IActivityGroup> groups = getElement().getGroups();
        ETList<IActivityPartition> partitions = getElement().getPartitions();
        
        
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
            out.write(getElementType() + " " + getElement().getName() + "</H2>\r\n"); // NOI18N
            
            out.write(getDocumentation());
            
            // property summary
            out.write(getProperties());
            
            // diagram summary
            out.write(getDiagramSummary());
            
            // stereotype summary
            out.write(getStereoTypesSummary());
            
            // tagged value summary
            out.write(getTaggedValueSummary());
            
            // constraint summary
            out.write(getConstraintsSummary());
            
            // template parameter summary
            out.write(getTemplateParameterSummary());
            
            // activity node summary
            if (nodes.size()>0)
            {
                out.write("<!-- =========== ACTIVITY NODE SUMMARY =========== -->\r\n\r\n"); // NOI18N
                out.write(getSummaryHeader("activity_node_summary", // NOI18N
                        NbBundle.getMessage(ActivityData.class, "Activity_Node_Summary"))); // NOI18N
                
                for (int i=0; i<nodes.size(); i++)
                {
                    IActivityNode node = (IActivityNode)nodes.get(i);
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
                    
                    String doc = getBriefDocumentation(node.getDocumentation());
                    
                    if (doc == null || doc.trim().equals(""))
                        doc = "&nbsp;"; // NOI18N
                    String type = node.getElementType().replaceAll(" ", ""); // NOI18N
                    String name = node.getName();
                    if (name==null || name.equals("")) // NOI18N
                        name = node.getElementType();
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N

                    String imageName = CommonResourceManager.instance()
                        .getIconDetailsForElementType(node.getElementType());
                    
                    if (imageName.lastIndexOf("/") > -1)
                        imageName = imageName.substring(imageName.lastIndexOf("/")+1); // NOI18N
                    
                    ReportTask.addToImageList(imageName);

                    out.write("<TD WIDTH=\"15%\"><img src=\"" + // NOI18N
                            ReportTask.getPathToReportRoot(getElement()) + 
                            "images/" + imageName + // NOI18N
                            "\" border=n>&nbsp<B><A HREF=\"" +  // NOI18N
                            getLinkTo(node) + "\" title=\"" +  // NOI18N
                            node.getElementType() + " in " + name + // NOI18N
                            "\">" + name + "</A></B></TD>\r\n"); // NOI18N
                    
                    out.write("<TD>" + doc + "</TD>\r\n"); // NOI18N
                    out.write("</TR>\r\n"); // NOI18N
                }
                out.write("</TABLE>\r\n&nbsp;\r\n"); // NOI18N
            }
            
            // activity groups summary
            if (groups.size()>0)
            {
                out.write("<!-- =========== ACTIVITY GROUP SUMMARY =========== -->\r\n\r\n"); // NOI18N
                out.write(getSummaryHeader("activity_group_summary", // NOI18N
                        NbBundle.getMessage(ActivityData.class, "Activity_Group_Summary"))); // NOI18N
                out.write(getSummaryTable(groups));
                
            }
            
            
            // activity partition summary
            if (partitions.size()>0)
            {
                out.write("<!-- =========== ACTIVITY PARTITION SUMMARY =========== -->\r\n\r\n"); // NOI18N
                out.write(getSummaryHeader("activity_partition_summary", // NOI18N
                        NbBundle.getMessage(ActivityData.class, "Activity_Partition_Summary"))); // NOI18N
                out.write(getSummaryTable(partitions));
                
            }
            
            
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
}
