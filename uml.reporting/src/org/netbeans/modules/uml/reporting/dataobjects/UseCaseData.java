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
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtensionPoint;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.constructs.UseCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
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
            OutputStreamWriter out = new OutputStreamWriter(fo, ENCODING);
            
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
                    if (extend == null || extend.getBase() == null)
                    {
                        Logger.getLogger(UseCaseData.class.getName()).
                                log(Level.WARNING, NbBundle.getMessage(ClassData.class,
                                "MSG_InvalidUseCaseBase", getElementType(), getElementName())); // NOI18N
                        continue;
                    }
                    
                    if (extend.getBase().getOwningPackage()!=null)
                    {
                        out.write("<A HREF=\"" + getLinkTo(extend.getBase()) +
                                "\" title=\"" + getElementType() + " in" + extend.getBase().getOwningPackage().getFullyQualifiedName(false) +
                                "\">" + extend.getBase().getName() + "</A>");
                    }
                    else
                    {
                        out.write(extend.getBase().getName());
                        Logger.getLogger(UseCaseData.class.getName()).
                                log(Level.WARNING, NbBundle.getMessage(ClassData.class,
                                "MSG_InvalidPackage", extend.getBase().getElementType(), extend.getBase().getName())); // NOI18N
                    }
                    if (i < extendsList.size()-1)
                        out.write(", ");
                }
            }
            
            if (extendedByList.size()>0)
            {
                out.write("<DT>" + NbBundle.getMessage(UseCaseData.class, "extended_by") + " ");
                for (int i=0; i<extendedByList.size(); i++)
                {
                    IExtend extend = extendedByList.get(i);
                    if (extend == null || extend.getExtension() == null)
                    {
                        Logger.getLogger(UseCaseData.class.getName()).
                                log(Level.WARNING, NbBundle.getMessage(ClassData.class,
                                "MSG_InvalidUseCaseExtension", getElementType(), getElementName())); // NOI18N
                        continue;
                    }
                    if (extend.getExtension().getOwningPackage()!=null)
                    {
                        out.write("<A HREF=\"" + getLinkTo(extend.getExtension()) +
                                "\" title=\"" + getElementType() + " in" + extend.getExtension().getOwningPackage().getFullyQualifiedName(false) +
                                "\">" + extend.getExtension().getName() + "</A>");
                    }
                    else
                    {
                        out.write(extend.getExtension().getName());
                        Logger.getLogger(UseCaseData.class.getName()).
                                log(Level.WARNING, NbBundle.getMessage(ClassData.class,
                                "MSG_InvalidPackage", extend.getExtension().getElementType(), extend.getExtension().getName())); // NOI18N
                    }
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
                    if (include == null || include.getAddition() == null)
                    {
                        Logger.getLogger(UseCaseData.class.getName()).
                                log(Level.WARNING, NbBundle.getMessage(ClassData.class,
                                "MSG_InvalidUseCaseExtension", getElementType(), getElementName())); // NOI18N
                        continue;
                    }
                    if (include.getAddition().getOwningPackage()!=null)
                    {
                        out.write("<A HREF=\"" + getLinkTo(include.getAddition()) + "\" title=\"" +
                                getElementType() +" in " +
                                include.getAddition().getOwningPackage().getFullyQualifiedName(false) +
                                "\">" + include.getAddition().getName() + "</A>");
                    }
                    else
                    {
                        out.write(include.getAddition().getName());
                        Logger.getLogger(UseCaseData.class.getName()).
                                log(Level.WARNING, NbBundle.getMessage(ClassData.class,
                                "MSG_InvalidPackage", include.getAddition().getElementType(), include.getAddition().getName())); // NOI18N
                    }
                    
                    if (i < includesList.size()-1)
                        out.write(", ");
                }
            }
            
            if (includedByList.size() > 0)
            {
                out.write("<DT>" + NbBundle.getMessage(UseCaseData.class, "included_by") + " ");
                for (int i=0; i<includedByList.size(); i++)
                {
                    IInclude include = includedByList.get(i);
                    if (include == null || include.getBase() == null)
                    {
                        Logger.getLogger(UseCaseData.class.getName()).
                                log(Level.WARNING, NbBundle.getMessage(ClassData.class,
                                "MSG_InvalidUseCaseBase", getElementType(), getElementName())); // NOI18N
                        continue;
                    }
                    if (include.getBase().getOwningPackage()!=null)
                    {
                        out.write("<A HREF=\"" + getLinkTo(include.getBase()) + "\" title=\"" +
                                getElementType() +" in " +
                                include.getBase().getOwningPackage().getFullyQualifiedName(false) +
                                "\">" + include.getBase().getName() + "</A>");
                    }
                    else
                    {
                        out.write(include.getBase().getName());
                        Logger.getLogger(UseCaseData.class.getName()).
                                log(Level.WARNING, NbBundle.getMessage(ClassData.class,
                                "MSG_InvalidPackage", include.getBase().getElementType(), include.getBase().getName())); // NOI18N
                    }
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
        catch (Exception e)
        {
            Logger.getLogger(ElementDataObject.class.getName()).log(
                    Level.SEVERE, getElement().getElementType() + " - " +  getElement().getNameWithAlias(), e);
            result = false;
        }
        return result;
    }
    
}
