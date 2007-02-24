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
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class InvocationNodeData extends ElementDataObject
{
    
    IInvocationNode element;
    /** Creates a new instance of InvocationNodeData */
    public InvocationNodeData()
    {
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof IInvocationNode)
            this.element = (IInvocationNode)e;
    }
    
    public IInvocationNode getElement()
    {
        return element;
    }
    
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Final,
            Property_Multiple_Invocation,
            Property_Synchronous,
        };
    }
    
    protected Object[] getPropertyValues()
    {
        Boolean isFinal = new Boolean(getElement().getIsFinal());
        Boolean isMultipleInvocation = new Boolean(getElement().getIsMultipleInvocation());
        Boolean isSync = new Boolean(getElement().getIsSynchronous());
        
        return new Object[] {getElement().getAlias(), getVisibility(getElement()),
        isFinal, isMultipleInvocation, isSync};
    }
    
    
    public String getConditionsSummary(String title, ETList<IConstraint> constraints)
    {
        StringBuilder buff = new StringBuilder();
        if (constraints.size()>0)
        {
            buff.append("<!-- =========== CONDITION SUMMARY =========== -->\r\n\r\n");
            buff.append(getSummaryHeader("constraint_summary", title));
            for (int i=0; i<constraints.size(); i++)
            {
                IConstraint constraint = constraints.get(i);
                buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                buff.append("<TD ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"1%\"><FONT SIZE=\"-1\">\r\n");
                buff.append("<CODE>" + constraint.getName() + "</CODE></FONT></TD>\r\n");
                buff.append("<TD><CODE>" + constraint.getExpression() + "</CODE>\r\n");
                buff.append("</TD>\r\n</TR>\r\n");
            }
            buff.append("</TABLE>\r\n&nbsp;\r\n");
        }
        return buff.toString();
    }
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        boolean result = false;
        
        ETList<IConstraint> preConditions = getElement().getLocalPreconditions();
        ETList<IConstraint> postConditions = getElement().getLocalPostConditions();
        
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
            out.write(getElementType() + " " + getElement().getName() + "</H2>\r\n");
            
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
            
            // precondition summary
            out.write(getConditionsSummary(
                    NbBundle.getMessage(InvocationNodeData.class, "Pre_Condition_Summary"), preConditions));
            
            // post-condition summary
            out.write(getConditionsSummary(
                    NbBundle.getMessage(InvocationNodeData.class, "Post_Condition_Summary"), postConditions));
            
            
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
