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
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class AssociationData extends ClassData
{
    private IAssociation element;
    
    /** Creates a new instance of AssociationData */
    public AssociationData()
    {
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof IAssociation)
            this.element = (IAssociation)e;
    }
    
    
    public IAssociation getElement()
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
            Property_Derived,
            Property_Reflexive
        };
    }
    
    protected Object[] getPropertyValues()
    {
        Boolean isFinal = new Boolean(getElement().getIsFinal());
        Boolean isTransient = new Boolean(getElement().getIsTransient());
        Boolean isAbstract = new Boolean(getElement().getIsAbstract());
        Boolean isLeaf = new Boolean(getElement().getIsLeaf());
        Boolean isDerived = new Boolean(getElement().getIsDerived());
        Boolean isReflexive = new Boolean(getElement().getIsReflexive());
        
        return new Object[] {getElement().getAlias(),
        getVisibility(getElement()), isFinal,
        isTransient, isAbstract, isLeaf, isDerived, isReflexive};
    }
    
    protected String getEndDetails()
    {
        StringBuilder buff = new StringBuilder();
        ETList<IAssociationEnd> ends = getElement().getEnds();
        if (ends.size()>0)
        {
            buff.append("<!-- =========== ASSOCIATION END DETAIL =========== -->\r\n");
            buff.append(getDetailHeader("association_end_detail",
                    NbBundle.getMessage(AssociationData.class, "Association_End_Detail")));
            for (int i=0; i<ends.size(); i++)
            {
                AssociationEndData endData;
                
                IAssociationEnd end = ends.get(i);
                String name = end.getName();
                if (name.equals(""))
                    name = end.getParticipant().getName();
                if (end instanceof INavigableEnd)
                {
                    endData = new NavigableEndData();
                    buff.append("<A NAME=\"" + name +
                            "\"></A><H3>" + NbBundle.getMessage(AssociationEndData.class,
                            "NavigableEnd") + "&nbsp;" +  name + "</H3>\r\n");
                }
                else
                {
                    endData = new AssociationEndData();
                    buff.append("<A NAME=\"" + name +
                            "\"></A><H3>" + NbBundle.getMessage(AssociationEndData.class,
                            "AssociationEnd") + "&nbsp;" +  name + "</H3>\r\n");
                }
                
                endData.setElement((IElement)end);
                
                buff.append(endData.getDocumentation());
                
                buff.append(endData.getProperties(false));
                buff.append(endData.getStereoTypesSummary());
                buff.append(endData.getTaggedValueSummary());
                buff.append(endData.getConstraintsSummary());
                
                if (i<ends.size()-1)
                    buff.append("<HR>\r\n\r\n");
                else
                    buff.append("\r\n");
            }
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
            
            // enclosing diagrams
            out.write(getEnclosingDiagrams());
            
            out.write(getDocumentation());

            // property summary
            
            out.write(getProperties());
            
            
            // association end summary
            ETList<IAssociationEnd> ends = getElement().getEnds();
            if (ends.size()>0)
            {
                out.write("<!-- =========== ASSOCIATION END SUMMARY =========== -->\r\n");
                out.write(getSummaryHeader("association_end_summary",
                        NbBundle.getMessage(AssociationData.class, "Association_End_Summary")));
                
                for (int i=0; i<ends.size(); i++)
                {
                    IAssociationEnd end = ends.get(i);
                    String name = end.getName();
                    if (name.equals(""))
                        name = end.getParticipant().getName();
                    
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD WIDTH=\"15%\"><B><A HREF=\"#" +
                            name + "\">" + name +
                            "</A></B></TD>\r\n");
                    
                    out.write("<TD>" + getBriefDocumentation(end.getDocumentation()) 
                        + "</TD>\r\n</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n");
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
            
            // association end detail
            out.write(getEndDetails());
            
            
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
