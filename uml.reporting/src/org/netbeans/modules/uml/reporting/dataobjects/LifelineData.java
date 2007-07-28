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
import org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.Lifeline;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class LifelineData extends ElementDataObject
{
    Lifeline lifeline;
    /** Creates a new instance of LifelineData */
    public LifelineData()
    {
    }
    
    public void setElement(IElement element)
    {
        if (element instanceof Lifeline)
            this.lifeline = (Lifeline)element;
    }
    
    public Lifeline getElement()
    {
        return lifeline;
    }
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        boolean result = false;
        
        IClassifier rClassifier = getElement().getRepresentingClassifier();
        
        ETList<IEventOccurrence> events = getElement().getEvents();
        
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
            
            out.write("<HR>\r\n");
            out.write("<DL>\r\n");
            out.write("<DT>" + getVisibility(getElement()) + " " +
                    getElementType().toLowerCase() + " <B>" + getElement().getName() +
                    "</B></DT>");
            if (rClassifier!=null)
            {
                out.write("<DT>" + NbBundle.getMessage(LifelineData.class, "represents") + ": ");
                out.write("<A HREF=\"" + getLinkTo(rClassifier) + "\" title=\"interface in " +
                        rClassifier.getOwningPackage().getFullyQualifiedName(false) +
                        "\">" + rClassifier.getName() + "</A>");
            }
            out.write("</DL>\r\n\r\n");
            
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
            
            // events summary
//			if (events.size()>0)
//			{
//				out.write("<!-- =========== EVENT SUMMARY =========== -->\r\n\r\n");
//				out.write(getSummaryHeader("event_summary", "Event Summary"));
//				for (int i=0; i<events.size(); i++)
//				{
//					IEventOccurrence event = (IEventOccurrence)events.get(i);
//					out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
//					out.write("<TD><CODE><B><A HREF=\"" + "#" + event.getName() + "\">" + event.getName() + "</A></B>");
////					out.write("<TD ALIGN=\"left\" VALIGN=\"top\" WIDTH=\"1%\"><FONT SIZE=\"-1\">\r\n");
////					out.write("<CODE><B>" + event.getName() + "</B></CODE>\r\n");
//					out.write("</TD>\r\n</TR>\r\n");
//				}
//				out.write("</TABLE>\r\n&nbsp;\r\n");
//			}
//
//
//			// event detail
//			if (events.size()>0)
//			{
//				out.write("<!-- =========== EVENT DETAIL =========== -->\r\n\r\n");
//				out.write(getDetailHeader("event_detail", "Event Detail"));
//				for (int i=0; i<events.size(); i++)
//				{
//					IEventOccurrence event = events.get(i);
//					IEvent e = event.getEventType();
//
//					IMessage receive = event.getReceiveMessage();
//					IMessage send = event.getSendMessage();
//					ETList<IGeneralOrdering> beforeOrdering = event.getBeforeOrderings();
//					ETList<IGeneralOrdering> afterOrdering = event.getAfterOrderings();
//
//					out.write("<A NAME=\"" + event.getName() + "\"></A><H3>" + event.getName() + "</H3>\r\n");
//					out.write("<B>" + event.getName() + "</B>");
//					out.write("<DL>\r\n");
//					out.write("<DD>" + StringUtilities.unescapeHTML(
//                                            event.getDocumentation()) + "\r\n<P>\r\n");
//					out.write("<DL>\r\n");
//					if (receive!=null)
//						out.write("<DT><B>Receive Message:</B><DD><CODE>" + receive.getName() +
//							"</CODE>\r\n");
//					if (send!=null)
//						out.write("<DT><B>Send Message:</B><DD><CODE>" + send.getName() +
//							"</CODE>\r\n");
//					out.write("<DL>\r\n");
//					out.write("<DT><B>Before Ordering:</B>");
//
//					for (int j=0; j<beforeOrdering.size(); j++)
//					{
//						out.write("<DD>" + beforeOrdering.get(j).getBefore().getName());
//					}
//
//					out.write("</DL>\r\n");
//					out.write("<DT><B>After Ordering:</B>");
//
//					for (int j=0; j<afterOrdering.size(); j++)
//					{
//						out.write("<DD>" + afterOrdering.get(j).getAfter().getName());
//					}
//					out.write("</DL>\r\n");
//					out.write("</DL>\r\n");
//
//					if (i<events.size()-1)
//						out.write("<HR>\r\n\r\n");
//					else
//						out.write("\r\n");
//				}
            
            
//			}
            
            
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
