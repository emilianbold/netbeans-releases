/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.project.ui.nodes.actions;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramTypesManager;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.project.UMLProjectModule;
import org.netbeans.modules.uml.ui.controls.newdialog.AddPackageVisualPanel1;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.archivesupport.ProductArchiveImpl;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
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
        ITreeDiagram cookie = diagramNode.getCookie(ITreeDiagram.class);
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
                if (Util.isDiagramNameValid(name))
                {
                    createDiagram(DiagramTypesManager.instance().
                        getUMLType(diagram.getDiagramKindName()),
                        diagram.getNamespace(), name);
                }
                else
                {
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(
                            AddPackageVisualPanel1.class,  
                            "MSG_Invalid_Diagram_Name", name)); // NOI18N
                    DialogDisplayer.getDefault().notify(msg);
                    
                }
            }
        }
    }
    
    private boolean saveFiles(String fileName, String newName)
    {
        String originalFile = original.getFilename();
        String original_l = originalFile;
        
        int index = originalFile.lastIndexOf(".");
        if (index > -1)
        {
            original_l = originalFile.substring(0, index) +
                    FileExtensions.DIAGRAM_LAYOUT_EXT;
        }
        String clone_l = fileName + FileExtensions.DIAGRAM_LAYOUT_EXT;
        
        // persist presentation file
        ProductArchiveImpl archive = new ProductArchiveImpl(original_l);
        IProductArchiveElement ele = archive.getDiagramElement(IProductArchiveDefinitions.UML_DIAGRAM_STRING);
        if (ele != null)
        {
            ele.addAttributeString(IProductArchiveDefinitions.DIAGRAMNAME_STRING, newName);
            ele.addAttributeString(IProductArchiveDefinitions.DIAGRAM_XMIID, UMLXMLManip.generateId(true));
            archive.save(clone_l);
            return true;
        }
        else 
        {
             NotifyDescriptor.Message msg 
                = new NotifyDescriptor.Message
                (NbBundle.getMessage(CopyDiagramAction.class,
                    "MSG_Cant_Copy_Diagram")); // NOI18N
            DialogDisplayer.getDefault().notify(msg);
            return false;
        }
    }
    
    
    private void createDiagram(String type, INamespace nameSpace, String name)
    {
        // 119568, save current working diagram before save as
        IDiagram diagram = original.getDiagram();
        if (diagram != null)
        {
            try {
                diagram.save();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        String filename = getFullFileName(name, nameSpace);

        if (saveFiles(filename, name)) {
            // create diagram node
            UMLProjectModule.getProjectTreeEngine().addDiagramNode(
                    filename + FileExtensions.DIAGRAM_PRESENTATION_EXT);
        }
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

    @Override
    protected boolean asynchronous()
    {
        return false;
    }


}
