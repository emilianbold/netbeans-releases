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
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class CombinedFragmentData extends ElementDataObject
{
    private ICombinedFragment element;
    
    /** Creates a new instance of CombinedFragmentData */
    public CombinedFragmentData()
    {
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof ICombinedFragment)
            this.element = (ICombinedFragment)e;
    }
    
    public ICombinedFragment getElement()
    {
        return element;
    }
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Operator
        };
    }
    
    protected Object[] getPropertyValues()
    {
        return new Object[] {getElement().getAlias(),
        getVisibility((INamedElement)getElement()),
        NbBundle.getMessage(CombinedFragmentData.class,
                "CombinedFragment_Operator" + getElement().getOperator())};
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
            
            out.write(getElementType() + " " + ((INamedElement)getElement()).getName() + "</H2>\r\n");
            
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
            
            // operand summary
            
            ETList<IInteractionOperand> operands = getElement().getOperands();
            if (operands.size()>0)
            {
                out.write("<!-- =========== OPERAND SUMMARY =========== -->\r\n\r\n");
                out.write(getSummaryHeader("operand_summary",
                        NbBundle.getMessage(ClassData.class, "Operand_Summary")));
                
                for (int i=0; i<operands.size(); i++)
                {
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    String name = operands.get(i).getName();
                    if (name.equals(""))
                        name = NbBundle.getMessage(CombinedFragmentData.class, "unnamed");
                    
                    IInteractionOperand operand = operands.get(i);
                    out.write("<TD WIDTH=\"15%\"><B><A HREF=\"" + "#" + name +
                            "\">" + name + "</A></B></TD>");
                    
                    out.write("<TD>" + getBriefDocumentation(
                        operand.getDocumentation()) +"</TD>\r\n");
                    
                    out.write("</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n");
            }
            
            // operand detail
            if (operands.size()>0)
            {
                out.write("<!-- =========== OPERAND DETAIL =========== -->\r\n\r\n");
                out.write(getDetailHeader("operand_detail",
                        NbBundle.getMessage(CombinedFragmentData.class, "Operand_Detail")));
                
                for (int i=0; i<operands.size(); i++)
                {
                    IInteractionOperand operand = operands.get(i);
                    String name = operand.getName();
                    name = name.equals("")?NbBundle.getMessage(CombinedFragmentData.class, "unnamed"):name;
                    
                    out.write("<A NAME=\"" + name + "\"></A><H3>" + name + "</H3>\r\n");
                    out.write("<DL>\r\n");
                    
                    out.write("<DD>" + operand.getDocumentation() + 
                        "\r\n<P>\r\n</DD>\r\n</DL>\r\n");
                    
                    if (operand.getGuard()!=null)
                    {
                        out.write("<DL>\r\n");
                        out.write("<DD>" + operand.getGuard().getExpression() + "\r\n<P>\r\n</DD>\r\n</DL>\r\n");
                    }
                    if (i<operands.size()-1)
                        out.write("<HR>\r\n\r\n");
                    else
                        out.write("\r\n");
                }
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
