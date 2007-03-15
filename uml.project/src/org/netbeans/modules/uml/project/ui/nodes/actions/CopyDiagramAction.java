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

package org.netbeans.modules.uml.project.ui.nodes.actions;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.project.UMLProjectModule;
import org.netbeans.modules.uml.project.ui.nodes.UMLDiagramNode.DiagramCookie;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.archivesupport.ProductArchiveImpl;
import org.netbeans.modules.uml.ui.support.diagramsupport.DiagramTypesManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;

/**
 *
 * @author Sheryl
 */
public class CopyDiagramAction extends NodeAction
{
    private IProxyDiagram original;
    
    /** Creates a new instance of CopyDiagramAction */
    public CopyDiagramAction()
    {
    }
    
    public boolean enable(Node[] nodes)
    {
        if (nodes.length == 1)
            return true;
        return false;
    }
    
    public void performAction(Node[] nodes)
    {
        Node diagramNode = nodes[0];
        DiagramCookie cookie = (DiagramCookie)diagramNode.getCookie(DiagramCookie.class);
        if ( cookie != null)
        {
            
            IProxyDiagram diagram = cookie.getDiagram();
            original = diagram;
            
            String label = NbBundle.getMessage(CopyDiagramAction.class,
                    "LBL_CopyDiagramAction_Diagram_Name");
            String title = NbBundle.getMessage(CopyDiagramAction.class,
                    "TLTEL_CopyDiagramAction_Save_Diagram_As");
            
            NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(label, title);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION)
            {
                String name = d.getInputText();
                createDiagram(DiagramTypesManager.instance().
                        getUMLType(diagram.getDiagramKindName()),
                        diagram.getNamespace(), name);
            }
        }
    }
    

    
    // save presentation (.etlp) and layout (.etld) files
    private void saveFiles(String fileName, String newName)
    {
        String originalFile = original.getFilename();
        String original_p = originalFile;
        String original_l = originalFile;
        
        int index = originalFile.lastIndexOf(".");
        if (index > -1)
        {
            original_p = originalFile.substring(0, index) +
                    FileExtensions.DIAGRAM_PRESENTATION_EXT;
            original_l = originalFile.substring(0, index) +
                    FileExtensions.DIAGRAM_LAYOUT_EXT;
        }
        
        String clone_p = fileName + FileExtensions.DIAGRAM_PRESENTATION_EXT;
        String clone_l = fileName + FileExtensions.DIAGRAM_LAYOUT_EXT;
        
        // persist presentation file
        ProductArchiveImpl archive = new ProductArchiveImpl(original_p);
        IProductArchiveElement ele = archive.getElement(IProductArchiveDefinitions.DIAGRAMINFO_STRING);
        ele.addAttributeString(IProductArchiveDefinitions.DIAGRAMNAME_STRING, newName);
        archive.save(clone_p);
        
        // persist layout file
        archive = new ProductArchiveImpl(original_l);
        archive.save(clone_l);
    }
    
    
    private void createDiagram(String type, INamespace nameSpace, String name)
    {
        String filename = getFullFileName(name, nameSpace);
        saveFiles(filename, name);
        
        // create diagram node
        UMLProjectModule.getProjectTreeEngine().addDiagramNode(
                filename + FileExtensions.DIAGRAM_PRESENTATION_EXT);
    }
    
    
    public HelpCtx getHelpCtx()
    {
        return null;
    }
    
    public String getName()
    {
        return (String)NbBundle.getBundle(CopyDiagramAction.class)
        .getString("CopyDiagramAction_Name");
    }
    
    
    // generate unique file name for diagram
    // this logic was copied from ADDrawingAreaControl.getFullFileName()
    private String getFullFileName(String name, INamespace m_Namespace)
    {
        String retFileName = null;
        // If the user enters a filename then use that as the proposed filename.
        // If the filename is not absolute then we use the project directory
        // as the location of the file.  If, for some reason, the filename argument
        // is 0 then we use the name of the diagram as the name of the file and the
        // workspace location for the directory.
        if (name != null && name.length() > 0)
        {
            String formatStr = null;
            long timeInMillis = System.currentTimeMillis();
            
            // To avoid conflicts between filenames, esp for large groups that are
            // using an SCC to manage their model we append a timestamp to the
            // file name.
            retFileName = name + "_" + timeInMillis;
        }
        
        // Make sure we have a legal file.  If it is just a name then add the
        // .etld extension and put it in the same spot as the workspace.
        String buffer = retFileName;
        String drive = null;
        int pos = buffer.indexOf(":");
        if (pos >= 0)
        {
            drive = buffer.substring(0, pos);
        }
        
        if (drive == null && m_Namespace != null)
        {
            // Assume we don't have a path and create one from the project directory
            IProject proj = m_Namespace.getProject();
            if (proj != null)
            {
                String fileName = proj.getFileName();
                if (fileName != null && fileName.length() > 0)
                {
                    try
                    {
                        fileName = (new File(fileName)).getCanonicalPath();
                        int posSlash = fileName.lastIndexOf(File.separator);
                        if (posSlash >= 0)
                        {
                            fileName = fileName.substring(0, posSlash + 1);
                            fileName += buffer;
                            buffer = fileName;
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        return buffer;
    }
}
