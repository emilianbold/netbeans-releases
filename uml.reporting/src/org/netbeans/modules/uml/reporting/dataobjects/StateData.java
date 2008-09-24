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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.reporting.ReportTask;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class StateData extends ElementDataObject
{
    
    private IState element;
    
    /** Creates a new instance of StateData */
    public StateData()
    {
    }
    
    
    public void setElement(IElement e)
    {
        if (e instanceof IState)
            element = (IState)e;
    }
    
    
    public IState getElement()
    {
        return element;
    }
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Container,
            Property_SubmachineState,
            Property_Simple,
            Property_Orthogonal,
            Property_Composite
        };
    }
    
    protected Object[] getPropertyValues()
    {
        
        Boolean isSubMachineState = new Boolean(getElement().getIsSubmachineState());
        Boolean isSimple = new Boolean(getElement().getIsSimple());
        Boolean isOrthogonal = new Boolean(getElement().getIsOrthogonal());
        Boolean isComposite = new Boolean(getElement().getIsComposite());
        
        return new Object[] {getElement().getAlias(), getVisibility(getElement()),
        getElement().getContainer(), isSubMachineState, isSimple,
        isOrthogonal, isComposite};
    }
    
    protected String[] getProcedurePropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Documentation,          
            Property_Transient,
            Property_Abstract,
            Property_Leaf,
            Property_Reentrant,
        };
    }
    
    private Object[] getProcedurePropertyValues(IProcedure procedure)
    {
        Boolean isTransient = new Boolean(procedure.getIsTransient());
        Boolean isAbstract = new Boolean(procedure.getIsAbstract());
        Boolean isLeaf = new Boolean(procedure.getIsLeaf());
        Boolean isReentrant = new Boolean(procedure.getIsReentrant());
        
        return new Object[] {procedure.getAlias(),
        getVisibility(getElement()), procedure.getDocumentation(), isTransient, 
                      isAbstract, isLeaf, isReentrant};
    }
    
    public String getProcedure(String header, IProcedure procedure)
    {
        if (procedure == null)
            return "";
        String[] properties = getProcedurePropertyNames();
        Object[] values = getProcedurePropertyValues(procedure);
        StringBuilder buff = new StringBuilder();
        
        buff.append("<!-- =========== PROCEDURE PROPERTY SUMMARY =========== -->\r\n"); // NOI18N
        buff.append(header + "\n");
        buff.append(getSummaryHeader("property_summary", // NOI18N
                    NbBundle.getMessage(ElementDataObject.class, "Properties"))); // NOI18N
       
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
    
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        boolean result = false;
        
        ETList<ITransition> incomings = getElement().getIncomingTransitions();
        ETList<ITransition> outgoings = getElement().getOutgoingTransitions();
        
        // TODO: template needs to be designed
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
            out.write(getElement().getExpandedElementType() + " " + getElement().getName() + "</H2>\r\n");
            
            out.write(getDependencies());
            
            out.append(getEnclosingDiagrams());
            
            out.write(getDocumentation());
            
            // property summary
            out.write(getProperties());
            
            // stereotype summary
            out.write(getStereoTypesSummary());
            
            // tagged value summary
            out.write(getTaggedValueSummary());
            
            // constraint summary
            out.write(getConstraintsSummary());
            
            if (getElement().getIsSubmachineState())
            {
                // events
                out.write(getSummaryHeader("events_summary", 
                            NbBundle.getMessage(StateData.class, "Events_Summary")));
                // entry 
                out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                        out.write("<TD WIDTH=\"15%\"><B>" + NbBundle.getMessage(StateData.class, "Entry") + "</B></TD>\r\n");
                if (getElement().getEntry() != null)        
                    out.write("<TD><A HREF=\"#entry\">" + getElement().getEntry().getNameWithAlias() + "</A>\r\n</TD>\r\n");
                else
                    out.write("<TD>&nbsp;</TD>\r\n");
                out.write("</TD>\r\n</TR>\r\n");

                // do         
                out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                        out.write("<TD WIDTH=\"15%\"><B>" + NbBundle.getMessage(StateData.class, "Do_Activity") + "</B></TD>\r\n");
                if (getElement().getDoActivity() != null)        
                    out.write("<TD><A HREF=\"#do\">" + getElement().getDoActivity().getNameWithAlias() + "\r\n</TD>\r\n");
                else
                    out.write("<TD>&nbsp;</TD>\r\n");        
                out.write("</TD>\r\n</TR>\r\n");


                // exit     
                out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                        out.write("<TD WIDTH=\"15%\"><B>" + NbBundle.getMessage(StateData.class, "Exit") + "</B></TD>\r\n");
                if (getElement().getExit() != null)        
                    out.write("<TD><A HREF=\"#exit\">" + getElement().getExit().getNameWithAlias() + "\r\n</TD>\r\n");
                else
                    out.write("<TD>&nbsp;</TD>\r\n");        
                out.write("</TD>\r\n</TR>\r\n");          

                out.write("</TABLE>\r\n&nbsp;\r\n");
            }
            
            
            if (incomings!=null && incomings.size()>0)
            {
                out.write("<!-- =========== INCOMING TRANSITION SUMMARY =========== -->\r\n\r\n");
                out.write(getSummaryHeader("incoming_transition_summary",
                        NbBundle.getMessage(StateData.class, "Incoming_Transition_Summary")));
                
                for (int i=0; i<incomings.size(); i++)
                {
                    ITransition transition = (ITransition)incomings.get(i);
                    
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    if(transition.getIsInternal())
                    {
                         out.write("<TD WIDTH=\"15%\"><B>" + transition.getName() +
                                 "&nbsp;</B>(" + NbBundle.getMessage(StateData.class, "internal") + ")</TD>\r\n");
                         out.write("<TD>" + getBriefDocumentation(transition.getDocumentation()) + "</TD>\r\n");
                    }
                    else if (transition.getSource()==null)
                        continue;
                    else
                    {
                        out.write("<TD WIDTH=\"15%\"><B><A HREF=\"" + getLinkTo(transition.getSource()) +
                            "\" title=\"" + transition.getSource().getExpandedElementType() +
                            " in " + transition.getSource().getName() + "\">" +
                            transition.getSource().getName() + "</A></B></TD>\r\n");
                    
                        out.write("<TD>" + getBriefDocumentation(transition.getSource().getDocumentation()) + "</TD>\r\n");
                    }
                    
                    out.write("</TD>\r\n</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n");
            }
            
            if (outgoings!=null && outgoings.size()>0)
            {
                out.write("<!-- =========== OUTGOING TRANSITION SUMMARY =========== -->\r\n\r\n");
                out.write(getSummaryHeader("outgoing_transition_summary",
                        NbBundle.getMessage(StateData.class, "Outgoing_Transition_Summary")));
                
                for (int i=0; i<outgoings.size(); i++)
                {
                    ITransition transition = (ITransition)outgoings.get(i);
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    if(transition.getIsInternal())
                    {
                         out.write("<TD WIDTH=\"15%\"><B>" + transition.getName() +
                                 "</B></TD>\r\n");
                         out.write("<TD>" + getBriefDocumentation(transition.getDocumentation()) + "</TD>\r\n");
                    }
                    else if (transition.getSource()==null)
                        continue;
                    else
                    {                   
                        out.write("<TD WIDTH=\"15%\"><B><A HREF=\"" + getLinkTo(transition.getTarget()) +
                            "\" title=\"" + transition.getTarget().getExpandedElementType() +
                            " in " + transition.getTarget().getName() + "\">" +
                            transition.getTarget().getName() + "</A></B></TD>\r\n");
                    
                        out.write("<TD>" + getBriefDocumentation(transition.getTarget().getDocumentation()) + "</TD>\r\n");
                    }
                    
                    out.write("</TD>\r\n</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n");
            }
            
            
            if (getElement().getEntry() != null)
            {
                out.write("<BR>\r\n");
                out.write(getProcedure("<A NAME=\"entry\" /><B>" + NbBundle.getMessage(StateData.class, "Entry")  
                        + ": </B>" + getElement().getEntry().getNameWithAlias(), getElement().getEntry()));
            }
            if (getElement().getDoActivity() != null)
            {
                out.write("<BR>\r\n");
                out.write(getProcedure("<A NAME=\"do\" /><B>" + NbBundle.getMessage(StateData.class, "Do_Activity")  
                        + ": </B>" + getElement().getDoActivity().getNameWithAlias(), getElement().getDoActivity()));
            }
            if (getElement().getExit() != null)
            {
                out.write("<BR>\r\n");
                out.write(getProcedure("<A NAME=\"exit\" /><B>" + NbBundle.getMessage(StateData.class, "Exit")  
                        + ": </B>" + getElement().getExit().getNameWithAlias(), getElement().getExit()));
            }
            
            out.write("<HR>\r\n");
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
