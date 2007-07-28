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
import java.util.ArrayList;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.CombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class InteractionData extends ElementDataObject
{
    private IInteraction element;
    /** Creates a new instance of InteractionData */
    public InteractionData()
    {
    }
    
    
    public void setElement(IElement e)
    {
        if (e instanceof IInteraction)
            this.element = (IInteraction)e;
    }
    
    
    public IInteraction getElement()
    {
        return this.element;
    }
    
    
    private String getSummaryTable(ETList list)
    {
        StringBuilder buff = new StringBuilder();
        
        for (int i=0; i<list.size(); i++)
        {
            INamedElement node = (INamedElement)list.get(i);
            buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
            
            String doc = getBriefDocumentation(node.getDocumentation());
            
            if (doc == null || doc.trim().equals(""))
                doc = "&nbsp;";
            
            String name = node.getName();
            if (name==null || name.equals(""))
                name = NbBundle.getMessage(InteractionData.class, "unnamed");
            buff.append("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
            buff.append("<TD WIDTH=\"15%\"><B><A HREF=\"" + getLinkTo(node) +
                    "\" title=\"" + node.getElementType() + " in " + node.getName() +
                    "\">" + name + "</A></B></TD>\r\n");
            buff.append("<TD>" + doc + "</TD>\r\n");
            buff.append("</TR>\r\n");
        }
        
        buff.append("</TABLE>\r\n&nbsp;\r\n");
        return buff.toString();
    }
    
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        ETList<ILifeline> lifelines = getElement().getLifelines();
        
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
            out.write(getElementType() + " " + getElement().getName() + "</H2>\r\n");
            
            out.write(getDocumentation());

            // diagram summary
            out.write(getDiagramSummary());
            // stereotype summary
            out.write(getStereoTypesSummary());
            
            // tagged value summary
            out.write(getTaggedValueSummary());
            
            // constraint summary
            out.write(getConstraintsSummary());
            
            // lifeline summary
            if (lifelines!=null && lifelines.size()>0)
            {
                out.write("<!-- =========== LIFELINE SUMMARY =========== -->\r\n\r\n");
                out.write(getSummaryHeader("lifeline_summary",
                        NbBundle.getMessage(InteractionData.class, "Lifeline_Summary")));
                
                out.write(getSummaryTable(lifelines));
            }
            
            // fragment summary
            
            ETList<IInteractionFragment> fragments = getElement().getFragments();
            ArrayList<IInteractionFragment> list = new ArrayList();
            for (int i=0; i<fragments.size(); i++)
            {
                IInteractionFragment f = fragments.get(i);
                
                if (! (f instanceof CombinedFragment))
                    list.add(f);
            }
            for (int i=0; i<list.size(); i++)
            {
                fragments.removeItem(list.get(i));
            }
            
            if (fragments.size()>0)
            {
                out.write("<!-- =========== FRAGMENT SUMMARY =========== -->\r\n\r\n");
                out.write(getSummaryHeader("combined_fragment_summary",
                        NbBundle.getMessage(InteractionData.class, "Combined_Fragment_Summary")));
                out.write(getSummaryTable(fragments));
                
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
