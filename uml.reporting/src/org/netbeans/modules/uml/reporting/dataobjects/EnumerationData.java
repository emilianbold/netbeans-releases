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
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class EnumerationData extends DataTypeData
{
    private IEnumeration element;
    
    /** Creates a new instance of EnumerationData */
    public EnumerationData()
    {
    }
    
    public EnumerationData(IClassifier classifier)
    {
        super(classifier);
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof IEnumeration)
            this.element = (IEnumeration)e;
    }
    
    public IEnumeration getElement()
    {
        return element;
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
            out.write("<BODY BGCOLOR=\"white\">\r\n");
            out.write(getNavBar());
            out.write("<HR>\r\n");
            out.write("<H2>\r\n");
            out.write("<FONT SIZE=\"-1\">" + getOwningPackageName() + "</FONT>\r\n");
            out.write("<BR>\r\n");
            
            out.write(getElementType() + " " + getElement().getName() + "</H2>\r\n");
            
            out.write(getDependencies());
            out.write(getAssociations());
            out.write(getGeneralizations());
            out.write(getSpecifications());
                        
            out.write(getEnclosingClassSection());
            
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
            
            // literal summary
            ETList<IEnumerationLiteral> literals = getElement().getLiterals();
            if (literals.size()>0)
            {
                out.write("<!-- =========== LITERAL SUMMARY =========== -->\r\n\r\n");
                out.write(getSummaryHeader("literal_summary",
                        NbBundle.getMessage(EnumerationData.class, "Literal_Summary")));
                
                for (int i=0; i<literals.size(); i++)
                {
                    IEnumerationLiteral literal = (IEnumerationLiteral)literals.get(i);
                    doc = literal.getDocumentation();
                    doc = doc.equals("")?"&nbsp;":doc;
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD ALIGN=\"left\" VALIGN=\"top\" WIDTH=\"15%\">\r\n");
                    out.write(literal.getName() + "</TD>\r\n");
                    out.write("<TD>" + doc + "\r\n");
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
