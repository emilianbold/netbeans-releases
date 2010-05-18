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
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IDeploymentSpecification;
import org.netbeans.modules.uml.core.metamodel.structure.INode;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class DeploymentSpecificationData extends ElementDataObject
{
    private IDeploymentSpecification element;
    
    /** Creates a new instance of DeploymentSpecificationData */
    public DeploymentSpecificationData()
    {
    }
    
    
    public void setElement(IElement e)
    {
        if (e instanceof IDeploymentSpecification)
            this.element = (IDeploymentSpecification)e;
    }
    
    
    public IDeploymentSpecification getElement()
    {
        return element;
    }
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Deployment_Location,
            Property_Execution_Location,
            Property_Container
        };
    }
    
    protected Object[] getPropertyValues()
    {
        return new Object[] {getElement().getAlias(), getVisibility(getElement()),
        getElement().getDeploymentLocation(),
        getElement().getExecutionLocation(),
        getElement().getContainer()};
    }
    
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        boolean result = false;
        
        INode container = getElement().getContainer();
        ETList<IArtifact> descriptors = getElement().getDeploymentDescriptors();
        String deploymentLocation = getElement().getDeploymentLocation();
        String executionLocation = getElement().getExecutionLocation();
        
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
            
            out.append(getDependencies());
            out.append(getEnclosingDiagrams());
            
            out.write(getDocumentation());
            
            // property summary
            out.write(getProperties());
            
            // dependency summary
            out.write(getDependencies());
            
            // stereotype summary
            out.write(getStereoTypesSummary());
            
            // tagged value summary
            out.write(getTaggedValueSummary());
            
            // constraint summary
            out.write(getConstraintsSummary());
            
            // descriptor summary
            if (descriptors.size()>0)
            {
                out.write("<!-- =========== DESCRIPTOR SUMMARY =========== -->\r\n");
                out.write(getSummaryHeader("descriptor_summary",
                        NbBundle.getMessage(DeploymentSpecificationData.class, "Descriptor_Summary")));
                
                for (int i=0; i<descriptors.size(); i++)
                {
                    IArtifact descriptor = descriptors.get(i);
                    String filename = descriptor.getFileName();
                    if (filename == null || filename.trim().equals(""))
                        filename = "&nbsp;";
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD WIDTH=\"15%\"><B>" + descriptor.getName() + "</B></TD>\r\n");
                    out.write("<TD>" + filename + "</TD>\r\n");
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
