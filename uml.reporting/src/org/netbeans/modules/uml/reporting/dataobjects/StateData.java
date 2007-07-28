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
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.ErrorManager;
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
            OutputStreamWriter out = new OutputStreamWriter(fo);
            
            out.write(getHTMLHeader());
            out.write("<BODY BGCOLOR=\"white\">\r\n");
            out.write(getNavBar());
            out.write("<HR>\r\n");
            out.write("<H2>\r\n");
            out.write("<FONT SIZE=\"-1\">" + getOwningPackageName() + "</FONT>\r\n");
            out.write("<BR>\r\n");
            out.write(getElement().getExpandedElementType() + " " + getElement().getName() + "</H2>\r\n");
            
            out.write(getDependencies());
            
            out.write(getDocumentation());
            
            // property summary
            out.write(getProperties());
            
            // stereotype summary
            out.write(getStereoTypesSummary());
            
            // tagged value summary
            out.write(getTaggedValueSummary());
            
            // constraint summary
            out.write(getConstraintsSummary());
            
            if (incomings!=null && incomings.size()>0)
            {
                out.write("<!-- =========== INCOMING TRANSITION SUMMARY =========== -->\r\n\r\n");
                out.write(getSummaryHeader("incoming_transition_summary",
                        NbBundle.getMessage(StateData.class, "Incoming_Transition_Summary")));
                
                for (int i=0; i<incomings.size(); i++)
                {
                    ITransition transition = (ITransition)incomings.get(i);
                    if(transition.getSource()==null)
                        continue;
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD WIDTH=\"15%\"><B><A HREF=\"" + getLinkTo(transition.getSource()) +
                            "\" title=\"" + transition.getSource().getExpandedElementType() +
                            " in " + transition.getSource().getName() + "\">" +
                            transition.getSource().getName() + "</A></B></TD>\r\n");
                    
                    out.write("<TD>" + getBriefDocumentation(transition.getSource().getDocumentation()) 
                        + "</TD>\r\n");
                    
                    out.write("</TD>\r\n</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n");
            }
            
            if (outgoings!=null && outgoings.size()>0)
            {
                out.write("<!-- =========== INCOMING TRANSITION SUMMARY =========== -->\r\n\r\n");
                out.write(getSummaryHeader("outgoing_transition_summary",
                        NbBundle.getMessage(StateData.class, "Outgoing_Transition_Summary")));
                
                for (int i=0; i<outgoings.size(); i++)
                {
                    ITransition transition = (ITransition)outgoings.get(i);
                    if(transition.getTarget()==null)
                        continue;
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD WIDTH=\"15%\"><B><A HREF=\"" + getLinkTo(transition.getTarget()) +
                            "\" title=\"" + transition.getTarget().getExpandedElementType() +
                            " in " + transition.getTarget().getName() + "\">" +
                            transition.getTarget().getName() + "</A></B></TD>\r\n");
                    
                    out.write("<TD>" + getBriefDocumentation(transition.getTarget()
                        .getDocumentation()) + "</TD>\r\n");
                    
                    out.write("</TD>\r\n</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n");
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
