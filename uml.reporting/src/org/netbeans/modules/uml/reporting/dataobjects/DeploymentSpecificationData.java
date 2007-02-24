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
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IDeploymentSpecification;
import org.netbeans.modules.uml.core.metamodel.structure.INode;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.ErrorManager;
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
            OutputStreamWriter out = new OutputStreamWriter(fo);
            
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
