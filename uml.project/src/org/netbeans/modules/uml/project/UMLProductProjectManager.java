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

/*
 * UMLProductProjectManager.java
 *
 * Created on May 18, 2005, 1:53 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.project;

import java.awt.Dialog;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialog;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogDiagramDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogProjectDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogTabDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialog;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogDiagramDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogProjectDetails;
import org.netbeans.modules.uml.ui.support.NewDialogTabKind;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import java.io.File;
import java.util.ArrayList;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.project.ui.nodes.UMLModelRootNode;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogResultProcessor;
import org.netbeans.modules.uml.ui.controls.newdialog.ProjectLocationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Administrator
 */
public class UMLProductProjectManager implements IProductProjectManager {
    private static String previousDirectory = "";
    
    /** Creates a new instance of UMLProductProjectManager */
    public UMLProductProjectManager() {
    }
    
    public void displayInsertProjectDialog(IWorkspace iWorkspace) {
        JFileChooser chooser = new JFileChooser(previousDirectory);
        
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() ||
                        file.toString().toLowerCase().endsWith(".etd");
            }
            
            public String getDescription() {
                return NbBundle.getMessage(UMLProductProjectManager.class, "Dialog.InsertProject.FileFilter.Description");
            }
        });
        
        chooser.setDialogTitle(NbBundle.getMessage(UMLProductProjectManager.class, "Dialog.InsertProject.Title"));
        
        if (chooser.showOpenDialog(chooser) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();            
            if (file != null) {
                IApplication pApp = ProductHelper.getApplication();
                if (pApp != null) {
                    IProject proj = pApp.openProject(file.getAbsolutePath());
                    if (proj != null) {
                        insertProjectIntoWorkspace(iWorkspace, proj);
                    }
                }
                previousDirectory = file.getPath();
            }
        }
        
    }
    
    /**
     * Inserts the argument project into the passed in IWorkspace
     */
    private boolean insertProjectIntoWorkspace(IWorkspace pWorkspace, IProject pProject) {
        boolean inserted = false;
        IApplication pApp = ProductHelper.getApplication();
        if (pApp != null) {
            IWSProject appProj = pApp.importProject(pWorkspace, pProject);
            inserted = true;
        }
        return inserted;
    }
    
    public void displayNewProjectDialog() {
        NewDialogProjectDetails details = new NewDialogProjectDetails();
        details.setAllowFromRESelection(false);
        details.setMode("Analysis");
        displayNewProjectDialog(details);
    }
    
    public void displayNewProjectDialog(INewDialogProjectDetails pDetails) {
		
		NewDialogProjectDetails pResults=null;
		
		ProjectLocationPanel panel = new ProjectLocationPanel();
		DialogDescriptor dialogDescriptor=new DialogDescriptor(panel,
		NbBundle.getMessage(ProjectLocationPanel.class,
					"TITLE_CreateDesignCenterProject")); // NOI18N

		Dialog dialog=DialogDisplayer.getDefault().createDialog(dialogDescriptor);
		dialog.getAccessibleContext().setAccessibleDescription(
				NbBundle.getMessage(ProjectLocationPanel.class,
					"TITLE_CreateDesignCenterProject"));
		try
		{
			dialog.setVisible(true);

			if (dialogDescriptor.getValue()==DialogDescriptor.OK_OPTION) 
			{
				pResults = panel.getProjectDetails();
				NewDialogResultProcessor processor = new NewDialogResultProcessor();
				processor.handleResult(pResults);
			}
		}finally 
		{
			dialog.dispose();
		}	
		
        IProject retProj = null;

		if (pResults != null && pResults instanceof INewDialogProjectDetails) {
			// User hit ok, should be project results
			INewDialogProjectDetails projDetails = (INewDialogProjectDetails)pResults;

			retProj = newProject(projDetails);
			if (retProj != null) {
				// This call will ask the user to create a diagram if a preference is set.
				doPostNewProject(retProj, pDetails);
			}
		}
    }
    
    /**
     * Tells the IADApplication to create a new project.
     *
     * @param pFilename [in] The details about the new project
     * @param pOpenedProject [out] The new project that was created.
     */
    public IProject newProject(INewDialogProjectDetails pDetails) {
        IProject proj = null;
        if (pDetails != null) {
            String location = pDetails.getLocation();
            String name = pDetails.getName();
            
//         String fullFilename = FileSysManip.createFullPath(location, name, ".etd");
            String fullFilename = location + File.separatorChar + name + ".etd";
            
            //we do not need to do it here - as the new dialog display would have already created a project.
//         NewDialogResultProcessor processor = new NewDialogResultProcessor();
//         processor.handleResult(pDetails);
            
            IApplication pApp = ProductHelper.getApplication();
            if (pApp != null) {
                proj = pApp.getProjectByFileName(fullFilename);
            }
        }
        return proj;
    }
    
    /**
     * After a project we need to question the user about creating a new diagram.  The
     * INewDialogProjectDetails can specify that whether or not to question the user.
     *
     * @param pProject [in] The project that was just created.
     * @param pDetails [in] The new project details.
     */
    public void doPostNewProject(IProject pProject, INewDialogProjectDetails pDetails) {
        if (pProject != null) {
            boolean promptUser = true;
            if (pDetails != null) {
                promptUser = pDetails.getPromptToCreateDiagram();
            }
            
            if (promptUser) {
                
                    //Kris Richards - options no longer available. Default to yes.
                    //String prefVal = prefMan.getPreferenceValue("NewProject", "QueryForNewDiagram");                   
                        // Query the user for a new diagram
                        INamespace space = null;
                        if (pProject instanceof INamespace) {
                            space = (INamespace)pProject;
                        }
                        queryUserForNewDiagram(space, IDiagramKind.DK_UNKNOWN, IDiagramKind.DK_ALL);
                   
                }
            
        }
    }
    
    public IDiagram queryUserForNewDiagram(INamespace pNamespace, int diaKind, int availableKinds) {
        IDiagram retDia = null;
        INewDialog diag = new NewDialog();
        diag.addTab(NewDialogTabKind.NWIK_NEW_DIAGRAM);
        NewDialogDiagramDetails details = new NewDialogDiagramDetails();
        details.setNamespace(pNamespace);
        details.setDiagramKind(diaKind);
        details.setAvailableDiagramKinds(availableKinds);
        
        diag.specifyDefaults( details );
        
        INewDialogTabDetails results = diag.display( null );
        
        if( results != null && results instanceof INewDialogDiagramDetails) {
            INewDialogDiagramDetails elementResults = (INewDialogDiagramDetails)results;
            if( elementResults != null ) {
                INamespace space = elementResults.getNamespace();
                String name = elementResults.getName();
                int kind = elementResults.getDiagramKind();
                if (name != null && name.length() > 0) {
                    retDia = newDiagram(space, kind, name);
                }
            }
        }
        return retDia;
    }
    
    public IDiagram newDiagram(INamespace space, int diaKind, String name) {
        IDiagram retDia = null;
        IProductDiagramManager diaMan = ProductHelper.getProductDiagramManager();
        if (diaMan != null) {
            if (retDia == null) {
                if (space != null) {
                    retDia = diaMan.createDiagram(diaKind, space, name, null);
                } else {
                    IProductProjectManager mgr = ProductHelper.getProductProjectManager();
                    if (mgr != null) {
                        IProject proj = mgr.getCurrentProject();
                        if (proj != null) {
                            retDia = diaMan.createDiagram(diaKind, proj, name, null);
                        }
                    }
                }
            }
        }
        return retDia;
    }
    
    public IProject getCurrentProject() 
    {
        IProject retVal = null;
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        
        for (Node node : nodes) 
        {
            IElement element = (IElement)node.getCookie(IElement.class);
            
            if (element == null && node.getCookie(UMLModelRootNode.class) != null)
            {
                element = (IElement)node.getParentNode()
                    .getLookup().lookup(UMLProject.class);
            }
                
            if (element != null) 
            {
                retVal = element.getProject();
                break;
            }
        }
        
        return retVal;
    }
    
    public ArrayList < IProject > getOpenProjects() {
        ArrayList < IProject > retVal = new ArrayList < IProject >();
        
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        for(Project curProject : allProjects) {
            UMLProjectHelper helper = (UMLProjectHelper)curProject.getLookup().lookup(UMLProjectHelper.class);
            if(helper != null) {
                retVal.add(helper.getProject());
            }
        }
        
        return retVal;
    }
}
