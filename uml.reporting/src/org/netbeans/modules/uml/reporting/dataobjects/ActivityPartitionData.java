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
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
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
public class ActivityPartitionData extends ActivityGroupData
{
    private IActivityPartition element;
    
    /**
     * Creates a new instance of ActivityPartitionData
     */
    public ActivityPartitionData()
    {
    }
    
    
    public void setElement(IElement e)
    {
        if (e instanceof IActivityPartition)
            this.element = (IActivityPartition)e;
    }
    
    public IActivityPartition getElement()
    {
        return element;
    }
    
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Activity,
            Property_External,
            Property_Dimension
        };
    }
    
    protected Object[] getPropertyValues()
    {
        Boolean isExternal = new Boolean(getElement().getIsExternal());
        Boolean isDimension = new Boolean(getElement().getIsDimension());
        return new Object[] {getElement().getAlias(), getVisibility(getElement()),
        getElement().getActivity(), isExternal, isDimension};
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
            String doc = "";
            
            out.write(getHTMLHeader());
            out.write("<BODY BGCOLOR=\"white\">\r\n"); // NOI18N
            out.write(getNavBar());
            out.write("<HR>\r\n"); // NOI18N
            out.write("<H2>\r\n"); // NOI18N
            out.write("<FONT SIZE=\"-1\">" + getOwningPackageName() + "</FONT>\r\n"); // NOI18N
            out.write("<BR>\r\n"); // NOI18N
            
            out.write(getElementType() + " " + getElement().getName() + "</H2>\r\n"); // NOI18N
            
            out.write(getDependencies());
            
            // super partition
            IElement owner = getElement().getOwner();
            if (owner instanceof IActivityPartition)
            {
                if (owner!=null)
                {
                    out.write("<DL>\r\n"); // NOI18N
                    out.write("<DT><B>" + NbBundle.getMessage(
                            ActivityPartitionData.class, "Super_Partition") + ": </B><DD><A HREF=\"" + // NOI18N
                            getLinkTo(owner) + "\" >" + // NOI18N
                            ((IActivityPartition)owner).getName() + "</A></DD>\r\n"); // NOI18N
                    out.write("</DL>\r\n"); // NOI18N
                }
            }
            
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
            
            // contained nodes summary
            ETList<IActivityNode> nodes = getElement().getNodeContents();
            if (nodes.size()>0)
            {
                out.write("<!-- =========== ACTIVITY NODE SUMMARY =========== -->\r\n\r\n"); // NOI18N
                out.write(getSummaryHeader("contained_node_summary", // NOI18N
                        NbBundle.getMessage(ActivityPartitionData.class, "Contained_Node_Summary"))); // NOI18N
                
                for (int i=0; i<nodes.size(); i++)
                {
                    IActivityNode node = (IActivityNode)nodes.get(i);
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
                    
                    doc = getBriefDocumentation(node.getDocumentation());
                    
                    if (doc == null || doc.trim().equals(""))
                        doc = "&nbsp;"; // NOI18N
                    
                    String name = node.getName();
                    if (name==null || name.equals(""))
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
                        "\" border=n>&nbsp<B><A HREF=\"" + getLinkTo(node) + // NOI18N
                        "\" title=\"" + node.getElementType() + " in " +  // NOI18N
                        name + "\">" + name + "</A></B></TD>\r\n"); // NOI18N

                    out.write("<TD>" + doc + "</TD>\r\n"); // NOI18N
                    out.write("</TR>\r\n"); // NOI18N
                }
                out.write("</TABLE>\r\n&nbsp;\r\n"); // NOI18N
            }
            
            // sub partition summary
            
            ETList<IActivityPartition> subs = getElement().getSubPartitions();
            if (subs.size()>0)
            {
                out.write("<!-- =========== SUB PARTITION SUMMARY =========== -->\r\n\r\n"); // NOI18N
                out.write(getSummaryHeader("sub_partition_summary", // NOI18N
                        NbBundle.getMessage(ActivityPartitionData.class, "Sub_Partition_Summary"))); // NOI18N
                
                for (int i=0; i<subs.size(); i++)
                {
                    IActivityPartition sub = subs.get(i);
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
                    
                    doc = getBriefDocumentation(sub.getDocumentation());
                    
                    if (doc == null || doc.trim().equals(""))
                        doc = "&nbsp;"; // NOI18N
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n"); // NOI18N
                    out.write("<TD WIDTH=\"15%\"><B><A HREF=\"" + getLinkTo(sub) + // NOI18N
                            "\" title=\"" + sub.getName() + " in " + getElement().getName() +
                            "\">" + sub.getName() + "</A></B></TD>\r\n"); // NOI18N
                    out.write("<TD>" + doc + "</TD>\r\n"); // NOI18N
                    out.write("</TR>\r\n"); // NOI18N
                }
                out.write("</TABLE>\r\n&nbsp;\r\n"); // NOI18N
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
