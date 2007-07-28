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
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.metamodel.structure.IDeploymentSpecification;
import org.netbeans.modules.uml.core.metamodel.structure.INode;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class ComponentData extends ClassData
{
    private IComponent element;
    
    /** Creates a new instance of ComponentData */
    public ComponentData()
    {
    }
    
    
    public void setElement(IElement e)
    {
        if (e instanceof IComponent)
            this.element = (IComponent)e;
    }
    
    public IComponent getElement()
    {
        return element;
    }
    
    
    protected String[] getPropertyNames()
    {
        return new String[] {
            Property_Alias,
            Property_Visibility,
            Property_Final,
            Property_Transient,
            Property_Abstract,
            Property_Leaf,
            Property_Instantiation
        };
    }
    
    protected Object[] getPropertyValues()
    {
        Boolean isFinal = new Boolean(getElement().getIsFinal());
        Boolean isTransient = new Boolean(getElement().getIsTransient());
        Boolean isAbstract = new Boolean(getElement().getIsAbstract());
        Boolean isLeaf = new Boolean(getElement().getIsLeaf());
        
        return new Object[] {getElement().getAlias(),
        getVisibility(getElement()), isFinal,
        isTransient, isAbstract, isLeaf,
        NbBundle.getMessage(ComponentData.class, "Instantiation"+
                getElement().getInstantiation())};
    }
    
    public boolean toReport(File file)
    {
        if (getElement()==null)
            return false;
        
        boolean result = false;
        
        ETList<IArtifact> artifacts = getElement().getArtifacts();
        ETList<IDeploymentSpecification> specs = getElement().getDeploymentSpecifications();
        ETList<INode> nodes = getElement().getNodes();
        ETList<IPort> ports = getElement().getExternalInterfaces();
        
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
            // association summary
            out.write(getAssociations());
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
            
            // artifact summary
            if (artifacts.size()>0)
            {
                out.write("<!-- =========== ARTIFACT SUMMARY =========== -->\r\n");
                out.write(getSummaryHeader("artifact_summary",
                        NbBundle.getMessage(ClassData.class, "Artifact_Summary")));
                for (int i=0; i<artifacts.size(); i++)
                {
                    IArtifact artifact = artifacts.get(i);
//					String filename = artifact.getFileName();
//					if (filename == null || filename.trim().equals(""))
//						filename = "&nbsp;";
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD WIDTH=\"15%\"><B><A HREF=\"" +
                            getLinkTo(artifact) + "\" >" + artifact.getName() +
                            "</A></B></TD>\r\n");
                    
                    out.write("<TD>" + getBriefDocumentation(
                            artifact.getDocumentation()) + "</TD>\r\n");
                    
                    out.write("</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n<P>\r\n");
            }
            
            // deployment specification summary
            if (specs.size()>0)
            {
                out.write("<!-- =========== DEPLOYMENT SPEC SUMMARY =========== -->\r\n");
                out.write(getSummaryHeader("deployment_spec_summary",
                        NbBundle.getMessage(ClassData.class, "Deployment_Spec_Summary")));
                
                for (int i=0; i<specs.size(); i++)
                {
                    IDeploymentSpecification spec = specs.get(i);
                    
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD WIDTH=\"15%\"><B><A HREF=\"" +
                            getLinkTo(spec) + "\" >" + spec.getName() +
                            "</A></B></TD>\r\n");
                    
                    out.write("<TD>" + getBriefDocumentation(spec.getDocumentation())
                        + "</TD>\r\n");
                    
                    out.write("</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n<P>\r\n");
            }
            
            // nodes summary
            if (nodes.size()>0)
            {
                out.write("<!-- =========== NODE SUMMARY =========== -->\r\n");
                out.write(getSummaryHeader("node_summary",
                        NbBundle.getMessage(ComponentData.class, "Node_Summary")));
                
                for (int i=0; i<nodes.size(); i++)
                {
                    INode node = nodes.get(i);
                    
                    out.write("<TR BGCOLOR=\"white\" CLASS=\"TableRowColor\">\r\n");
                    out.write("<TD WIDTH=\"15%\"><B><A HREF=\"" +
                            getLinkTo(node) + "\" >" + node.getName() +
                            "</A></B></TD>\r\n");
                    
                    out.write("<TD>" + getBriefDocumentation(node.getDocumentation())
                        + "</TD>\r\n");
                    
                    out.write("</TR>\r\n");
                }
                out.write("</TABLE>\r\n&nbsp;\r\n<P>\r\n");
            }
            
            // external interface summary
            
            
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
