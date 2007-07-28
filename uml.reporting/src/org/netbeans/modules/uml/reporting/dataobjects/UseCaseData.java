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
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtensionPoint;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.UseCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class UseCaseData extends ClassData
{
    private IUseCase element;
    /** Creates a new instance of UseCaseData */
    public UseCaseData()
    {
    }
    
    public UseCaseData(UseCase usecase)
    {
        setElement(usecase);
    }
    
    public void setElement(IElement e)
    {
        if (e instanceof IUseCase)
            element = (IUseCase)e;
    }
    
    public IUseCase getElement()
    {
        return element;
    }
    
    public String getElementName()
    {
        return getElement().getName();
    }
    
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        boolean result = false;
        ETList<IExtend> extendsList = getElement().getExtends();
        ETList<IExtend> extendedByList = getElement().getExtendedBy();
        ETList<IInclude> includesList = getElement().getIncludes();
        ETList<IInclude> includedByList = getElement().getIncludedBy();
//		ETList<IUseCaseDetail> detailList = getElement().getDetails();
        ETList<IExtensionPoint> points = getElement().getExtensionPoints();
        
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
            
            
            
            out.write("<DL>\r\n");
            out.write("<DT>" + getVisibility(getElement()) + " " +
                    getElementType().toLowerCase() + " <B>" + getElementName() + "</B></DT>");
            if (extendsList.size()>0)
            {
                out.write("<DT>" + NbBundle.getMessage(UseCaseData.class, "extends") + " ");
                for (int i=0; i<extendsList.size(); i++)
                {
                    IExtend extend = extendsList.get(i);
                    if (extend!=null && extend.getBase()!=null)
                    {
                        out.write("<A HREF=\"" + getLinkTo(extend.getBase()) +
                                "\" title=\"" + getElementType() + " in" + getOwningPackageName() +
                                "\">" + extend.getBase().getName() + "</A>");
                        if (i < extendsList.size()-1)
                            out.write(", ");
                    }
                }
            }
            
            if (extendedByList.size()>0)
            {
                out.write("<DT>" + NbBundle.getMessage(UseCaseData.class, "extended_by") + " ");
                for (int i=0; i<extendedByList.size(); i++)
                {
                    IExtend extend = extendedByList.get(i);
                    out.write("<A HREF=\"" + getLinkTo(extend.getExtension()) +
                            "\" title=\"" + getElementType() + " in" + getOwningPackageName() +
                            "\">" + extend.getExtension().getName() + "</A>");
                    if (i < extendedByList.size()-1)
                        out.write(", ");
                }
            }
            
            if (includesList.size() > 0)
            {
                out.write("<DT>" + NbBundle.getMessage(UseCaseData.class, "includes") + " ");
                for (int i=0; i<includesList.size(); i++)
                {
                    IInclude include = includesList.get(i);
                    if (include!=null && include.getAddition()!=null)
                    {
                        out.write("<A HREF=\"" + getLinkTo(include.getAddition()) + "\" title=\"" +
                                getElementType() +" in " +
                                include.getOwningPackage().getFullyQualifiedName(false) +
                                "\">" + include.getAddition().getName() + "</A>");
                        if (i < includesList.size()-1)
                            out.write(", ");
                    }
                }
            }
            
            if (includedByList.size() > 0)
            {
                out.write("<DT>" + NbBundle.getMessage(UseCaseData.class, "included_by") + " ");
                for (int i=0; i<includedByList.size(); i++)
                {
                    IInclude include = includedByList.get(i);
                    out.write("<A HREF=\"" + getLinkTo(include.getBase()) + "\" title=\"" +
                            getElementType() +" in " +
                            include.getOwningPackage().getFullyQualifiedName(false) +
                            "\">" + include.getBase().getName() + "</A>");
                    if (i < includedByList.size()-1)
                        out.write(", ");
                }
            }
            
            out.write("</DL>\r\n\r\n");
            
            out.write(getDependencies());
            out.write(getAssociations());
            out.write(getGeneralizations());
            out.write(getSpecifications());
            
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
            
            // template parameter summary
            out.write(getTemplateParameterSummary());
            
            // extension point summary
            if (points.size()>0)
            {
                out.write("<!-- =========== EXTENSION POINT SUMMARY =========== -->\r\n");
                out.write(getSummaryHeader("extension_point_summary",
                        NbBundle.getMessage(UseCaseData.class, "Extension_Point_Summary")));
                for (int i=0; i<points.size(); i++)
                {
                    IExtensionPoint point = points.get(i);
                    
                    String doc = point.getDocumentation();
                    
                    if (doc == null || doc.trim().equals(""))
                        doc = "&nbsp;";
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD WIDTH=\"15%\"><B>" + point.getName() + "</B></TD>\r\n");
                    out.write("<TD>" + doc + "</TD>\r\n");
                    out.write("</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n<P>\r\n");
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
