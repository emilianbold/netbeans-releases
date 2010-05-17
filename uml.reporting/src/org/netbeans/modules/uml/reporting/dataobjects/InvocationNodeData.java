/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInvocationNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
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
            OutputStreamWriter out = new OutputStreamWriter(fo, ENCODING);
            
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
        catch (Exception e)
        {
            Logger.getLogger(ElementDataObject.class.getName()).log(
                    Level.SEVERE, getElement().getElementType() + " - " +  getElement().getNameWithAlias(), e);
            result = false;
        }
        return result;
    }
}
