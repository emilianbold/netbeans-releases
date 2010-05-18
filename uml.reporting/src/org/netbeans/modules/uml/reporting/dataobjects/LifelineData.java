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
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.Lifeline;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
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
            OutputStreamWriter out = new OutputStreamWriter(fo, ENCODING);
            
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
            
            if(rClassifier==null)
            {
//                Logger.getLogger(LifelineData.class.getName()).
//                        log(Level.WARNING, NbBundle.getMessage(LifelineData.class,
//                        "MSG_InvalidRepresentingClassifier", getElementType(), getElement().toString())); // NOI18N              
            }
            else
            {
                out.write("<DT>" + NbBundle.getMessage(LifelineData.class, "represents") + ": ");
                if (rClassifier.getOwningPackage() != null)
                {
                    out.write("<A HREF=\"" + getLinkTo(rClassifier) + "\" title=\"interface in " +
                            rClassifier.getOwningPackage().getFullyQualifiedName(false) +
                            "\">" + rClassifier.getName() + "</A>");
                }
                else
                {
                    out.write(rClassifier.getName());
                    Logger.getLogger(LifelineData.class.getName()).
                            log(Level.WARNING,
                            NbBundle.getMessage(LifelineData.class,
                            "MSG_InvalidPackage", rClassifier.getElementType(), rClassifier.getName()));
                }
                out.write("</DL>\r\n\r\n");
            }
            
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
        catch (Exception e)
        {
            Logger.getLogger(ElementDataObject.class.getName()).log(
                    Level.SEVERE, getElement().getElementType() + " - " +  getElement().getNameWithAlias(), e);
            result = false;
        }
        return result;
        
    }
}
