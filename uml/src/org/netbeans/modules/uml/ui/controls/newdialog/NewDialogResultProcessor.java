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



package org.netbeans.modules.uml.ui.controls.newdialog;

import java.io.File;
import java.io.IOException;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManagementException;
import org.netbeans.modules.uml.ui.support.NewElementKind;
import org.netbeans.modules.uml.ui.support.NewPackageKind;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.core.scm.ISCMIntegrator;
import java.util.Arrays;
import java.util.List;
import org.openide.util.Exceptions;

/**
 * @author sumitabhk
 *
 */
public class NewDialogResultProcessor implements INewDialogResultProcessor
{
	
	/**
	 *
	 */
	public NewDialogResultProcessor()
	{
		super();
	}
	
	/**
	 *
	 * Acts on a result from the new dialog.
	 *
	 * @param pResult[in] The result of the INewDialog display
	 *
	 */
	public IElement handleResult(INewDialogTabDetails pResult)
	{
		IElement obj = null;
		if (pResult instanceof INewDialogWorkspaceDetails)
		{
			INewDialogWorkspaceDetails pWorkspace = 
				(INewDialogWorkspaceDetails)pResult;
			newWorkspace(pWorkspace);
		}
		
		else if (pResult instanceof INewDialogProjectDetails)
		{
			INewDialogProjectDetails pProject = 
				(INewDialogProjectDetails)pResult;
			obj = newProject(pProject);
		}
		
		else if (pResult instanceof INewDialogDiagramDetails)
		{
			INewDialogDiagramDetails pDiagram = 
				(INewDialogDiagramDetails)pResult;
			obj = newDiagram(pDiagram);
		}
		
		else if (pResult instanceof INewDialogPackageDetails)
		{
			INewDialogPackageDetails pPackage = 
				(INewDialogPackageDetails)pResult;
			obj = newPackage(pPackage);
		}
		
		else if (pResult instanceof INewDialogElementDetails)
		{
			INewDialogElementDetails pElement = 
				(INewDialogElementDetails)pResult;
			obj = newElement(pElement);
		}
		
		boolean isHandled = false;
		if (obj != null)
		{
			isHandled = true;
		}
		return obj;
	}
	
	/**
	 *
	 * Creates a new element based on the details in pDetails
	 *
	 * @param pDetails  Information on the new element (ie name)
	 *
	 */
	private IElement newElement(INewDialogElementDetails pElement)
	{
		IElement retEle = null;
		String name = pElement.getName();
		INamespace space = pElement.getNamespace();
		int eleKind = NewElementKind.NEK_CLASS;
		eleKind = pElement.getElementKind();
		
		if (space != null && name != null)
		{
			
			// CR#6263225 cvc
			//  added arrays to NewElementKind to make the
			//  maintenance of adding/changing/removing elements much easier
			//	switch/case logic no longer needed
			
			List eleNumList = Arrays.asList(NewElementKind.ELEMENT_NUMBERS);
			int index = eleNumList.indexOf(new Integer(eleKind));

			String elementType = NewElementKind.ELEMENT_NAMES[0];
			
			if (index == -1)
				elementType = NewElementKind.ELEMENT_NAMES[0];
			else
				elementType = NewElementKind.ELEMENT_NAMES[index];

//			String elementType = "Class";
//			if (eleKind == NewElementKind.NEK_ACTOR)
//				elementType = "Actor";
//			else if (eleKind == NewElementKind.NEK_ATTRIBUTE)
//				elementType = "Attribute";
//			else if (eleKind == NewElementKind.NEK_INTERFACE)
//				elementType = "Interface";
//			else if (eleKind == NewElementKind.NEK_OPERATION)
//				elementType = "Operation";
//			else if (eleKind == NewElementKind.NEK_USE_CASE)
//				elementType = "UseCase";
//			else if (eleKind == NewElementKind.NEK_DATATYPE)
//				elementType = "DataType";
//			else if (eleKind == NewElementKind.NEK_ALIASED_TYPE)
//				elementType = "AliasedType";
//			else if (eleKind == NewElementKind.NEK_ARTIFACT)
//				elementType = "Artifact";
//			else if (eleKind == NewElementKind.NEK_ENUMERATION)
//				elementType = "Enumeration";
//			else if (eleKind == NewElementKind.NEK_NODE)
//				elementType = "Node";
//			else if (eleKind == NewElementKind.NEK_INVOCATION_NODE)
//				elementType = "InvocationNode";
//			else if (eleKind == NewElementKind.NEK_ACTIVITY_GROUP)
//				elementType = "ActivityPartition";
//			else if (eleKind == NewElementKind.NEK_INITIAL_NODE)
//				elementType = "InitialNode";
//			else if (eleKind == NewElementKind.NEK_ACTIVITY_FINAL_NODE)
//				elementType = "ActivityFinalNode";
//			else if (eleKind == NewElementKind.NEK_ACTIVITY_FLOW_FINAL_NODE)
//				elementType = "FlowFinalNode";
//			else if (eleKind == NewElementKind.NEK_DECISION_MERGE_NODE)
//				elementType = "DecisionMergeNode";
//			else if (eleKind == NewElementKind.NEK_ABORTED_FINAL_STATE)
//				elementType = "AbortedFinalState";
//			else if (eleKind == NewElementKind.NEK_COMPONENT)
//				elementType = "Component";
//			else if (eleKind == NewElementKind.NEK_COMPOSITE_STATE)
//				elementType = "CompositeState";
//			else if (eleKind == NewElementKind.NEK_DATA_STORE_NODE)
//				elementType = "DataStoreNode";
//			else if (eleKind == NewElementKind.NEK_DERIVATION_CLASSIFIER)
//				elementType = "DerivationClassifier";
//			else if (eleKind == NewElementKind.NEK_ENUMERATION_LITERAL)
//				elementType = "EnumerationLiteral";
//			else if (eleKind == NewElementKind.NEK_FINAL_STATE)
//				elementType = "FinalState";
//			else if (eleKind == NewElementKind.NEK_FORK_STATE)
//				elementType = "ForkState";
//			else if (eleKind == NewElementKind.NEK_INITIAL_STATE)
//				elementType = "InitialState";
//			else if (eleKind == NewElementKind.NEK_JOIN_FORK_NODE)
//				elementType = "JoinForkNode";
//			else if (eleKind == NewElementKind.NEK_JOIN_STATE)
//				elementType = "JoinState";
//			else if (eleKind == NewElementKind.NEK_JUNCTION_STATE)
//				elementType = "JunctionState";
//			else if (eleKind == NewElementKind.NEK_LIFELINE)
//				elementType = "Lifeline";
//			else if (eleKind == NewElementKind.NEK_PARAMETER_USAGE_NODE)
//				elementType = "ParameterUsageNode";
//			else if (eleKind == NewElementKind.NEK_SIGNAL_NODE)
//				elementType = "SignalNode";
//			else if (eleKind == NewElementKind.NEK_SIMPLE_STATE)
//				elementType = "SimpleState";
//			else if (eleKind == NewElementKind.NEK_STATE)
//				elementType = "State";
//			else if (eleKind == NewElementKind.NEK_STOP_STATE)
//				elementType = "StopState";
//			else if (eleKind == NewElementKind.NEK_USE_CASE_DETAIL)
//				elementType = "UseCaseDetail";
			
			
			if (elementType.equals(NewElementKind.NEK_ATTRIBUTE) ||
				elementType.equals(NewElementKind.NEK_OPERATION))
			{
				if (space instanceof IClassifier)
				{
					IClassifier pClass = (IClassifier)space;
					
					if (elementType.equals(NewElementKind.NEK_OPERATION))
					{
						// Create an operation
						IOperation oper = pClass.createOperation3();
						if (oper != null)
						{
							oper.setName(name);
						}
						pClass.addFeature(oper);
						retEle = oper;
					}
					
					else
					{
						// Create an attribute
						IAttribute attr = pClass.createAttribute3();
						if (attr != null)
						{
							attr.setName(name);
						}
						pClass.addFeature(attr);
						retEle = attr;
					}
				}
			}
			
			else
			{
				// Create the element
				FactoryRetriever ret = FactoryRetriever.instance();
				Object obj = ret.createType(elementType, null);
				if (obj != null)
				{
					// Name the element and add it to the correct namespace
					if (obj instanceof INamedElement)
					{
						INamedElement nEle = (INamedElement)obj;
						
						// Set the name on the package
						space.addOwnedElement(nEle);
						nEle.setName(name);
						retEle = nEle;
					}
				}
			}
		}
		return retEle;
	}
	
	/**
	 *
	 * Creates a new package based on the details in pDetails
	 *
	 * @param pDetails  Information on the new package (ie name)
	 *
	 */
	private INamedElement newPackage(INewDialogPackageDetails pPackage)
        {
            INamedElement retPack = null;
            String name = pPackage.getName();
            INamespace space = pPackage.getNamespace();
            boolean createScopedDiagram = false;
            createScopedDiagram = pPackage.getCreateScopedDiagram();
            String scopedDiaName = pPackage.getScopedDiagramName();
            int diaKind = IDiagramKind.DK_CLASS_DIAGRAM;
            diaKind = pPackage.getScopedDiagramKind();
            int packKind = NewPackageKind.NPKGK_PACKAGE;
            packKind = pPackage.getPackageKind();
            
            if (space != null && name != null &&
                    packKind == NewPackageKind.NPKGK_PACKAGE)
            {
                FactoryRetriever ret = FactoryRetriever.instance();
                Object obj = ret.createType("Package", null); // NOI18N
                if (obj instanceof INamedElement)
                {
                    INamedElement pNamedPack = (INamedElement)obj;
                    space.addOwnedElement(pNamedPack);
                    pNamedPack.setName(name);
                    retPack = pNamedPack;
                }
                
                if (obj instanceof INamespace)
                {
                    INamespace packNamespace = (INamespace)obj;
                    if (createScopedDiagram)
                    {
                        IProduct prod = ProductHelper.getProduct();
                        if (prod != null)
                        {
                            IProductDiagramManager man = prod.getDiagramManager();
                            if (man != null)
                            {
                                IDiagram newDiagram = man.createDiagram(
                                        diaKind, packNamespace, scopedDiaName, null);
                                
                                // Fixed issue 95782. When a diagram is 1st created, its dirty state is false.
                                // Set the dirty state to true to have the diagram autosaved.
                                if (newDiagram != null )
                                {
                                    newDiagram.setDirty(true);
                                    try {
                                        newDiagram.save();
                                    } catch (IOException e) {
                                        Exceptions.printStackTrace(e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            return retPack;
        }
	
	/**
	 *
	 * Creates a new diagram based on the details in pDetails
	 *
	 * @param pDetails  Information on the new diagram (ie name)
	 *
	 */
	private IDiagram newDiagram(INewDialogDiagramDetails pDiagram)
        {
            IDiagram retDia = null;
            String name = pDiagram.getName();
            INamespace space = pDiagram.getNamespace();
            int diaKind = IDiagramKind.DK_CLASS_DIAGRAM;
            diaKind = pDiagram.getDiagramKind();
            
            ProductRetriever.retrieveProduct();
            IProduct prod = ProductHelper.getProduct();
            if (prod != null)
            {
                IProductDiagramManager diaMan = ProductHelper
                        .getProductDiagramManager();
                //TestNewDialog.getDiagramManager();//prod.getDiagramManager();
                
                if (diaMan != null)
                {
                    retDia = diaMan.createDiagram(diaKind, space, name, null);
                    
                    // Fixed issue 95782. When a diagram is 1st created, its dirty state is false.
                    // Set the dirty state to true to have the diagram autosaved.
                    if (retDia != null )
                    {
                        retDia.setDirty(true);
                        try
                        {
                            retDia.save();
                        } catch (IOException e)
                        {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            }
            
            return retDia;
        }
	
	/**
	 *
	 * Creates a new project based on the details in pDetails
	 *
	 * @param pDetails  Information on the new project (ie name)
	 *
	 */
	private IProject newProject(INewDialogProjectDetails pProject)
	{
		IProject retProj = null;
		String name = pProject.getName();
		String location = pProject.getLocation();
		String mode = pProject.getMode();
		String lang = pProject.getLanguage();
		boolean addtoSourceControl = pProject.getAddToSourceControl();
		int projKind = pProject.getProjectKind();
		
		IWorkspace pWorkspace = getWorkspace();
		IApplication pApp = ProductHelper.getApplication();
		if (pWorkspace != null && pApp != null)
		{
			if (location != null && name != null)
			{
				boolean bContinue = true;
				String fulFileName = StringUtilities
					.createFullPath(location, name, ".etd"); // NOI18N
				
				//if the file already exists we want to ask user for
				//overriding the file.
				File file = new File(fulFileName);
				if (file.exists())
				{
					int result =
						displayQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNOCANCEL,
						NewDialogResources.getString("IDS_OVERWRITE"), // NOI18N
						NewDialogResources.getString("IDS_OVERWRITETITLE")); // NOI18N
					
					if (result == SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL ||
						result == SimpleQuestionDialogResultKind.SQDRK_RESULT_NO)
					{
						bContinue = false;
					}
					else
					{
						// Remove the project from the workspace
						try
						{
							pWorkspace.removeWSElementByLocation(fulFileName);
						}
						catch (WorkspaceManagementException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				if (bContinue)
				{
					try
					{
						IWSProject proj = 
							pWorkspace.createWSProject(fulFileName, name);
						
						if (proj != null)
						{
							proj.save(fulFileName);
						}
						
						boolean addToSCM = false;
						addToSCM = pProject.getAddToSourceControl();
						
						IProject curProj = 
							pApp.getProjectByName(pWorkspace, name);
						
						if (curProj != null)
						{
							curProj.setMode(mode);
							curProj.setDefaultLanguage(lang);
							retProj = curProj;
							
							if (addToSCM)
							{
								handleVersionControl(curProj);
							}
						}
						
						pProject.setCreatedProject( curProj );
					}
					catch (Exception e)
					{
						//error in creating project
					}
				}
			}
		}
		return retProj;
	}
	
	/**
	 * @param i
	 * @param string
	 * @param string2
	 * @return
	 */
	private int displayQuestionDialog(int i, String msg, String title)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 *
	 * Adds the Project to the user's SCM tool.
	 *
	 * @param pProject[in]  The IProject to version control
	 *
	 * @return HRESULT
	 *
	 */
	private void handleVersionControl(IProject curProj)
	{
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod instanceof IProduct)
		{
			IProduct guiProd = (IProduct)prod;
			ISCMIntegrator scm = guiProd.getSCMIntegrator();
			if (scm != null)
			{
				scm.versionProject(curProj);
			}
		}
	}
	
	/**
	 * Returns the workspace this result processor should use
	 *
	 * @param pWorkspace [out,retval] The workspace
	 */
	private IWorkspace getWorkspace()
	{
		IWorkspace retSpace = null;
		INewDialogContext context = new NewDialogContext();
		retSpace = context.getWorkspace();
		return retSpace;
	}
	
	/**
	 *
	 * Creates a new workspace based on the details in pDetails
	 *
	 * @param pDetails [in]  Information on the new workspace (ie name)
	 *
	 */
	private IWorkspace newWorkspace(INewDialogWorkspaceDetails pWorkspace)
	{
		IWorkspace retSpace = null;
		String name = pWorkspace.getName();
		String location = pWorkspace.getLocation();
		
		if (name != null && location != null )
		{
			IProduct prod = ProductHelper.getProduct();
			if (prod != null)
			{
				String fullPath = location;
				if (!location.endsWith(File.separator))
				{
					fullPath += File.separator;
				}
				
				fullPath += name + ".etw"; // NOI18N
				
				if (fullPath.length() > 0)
				{
					String validatedStr = StringUtilities
						.ensureExtension(fullPath, ".etw"); // NOI18N
					
					//create the actual CoreProduct
					IProduct actual = (IProduct)prod;
					// This will set the current workspace
					try
					{
						IWorkspace workspace = 
							actual.createWorkspace(validatedStr, name);
						
						if (workspace != null)
						{
							retSpace = workspace;
							prod.openWorkspace(workspace.getLocation());
						}
					}
					
					catch (InvalidArguments e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					catch (WorkspaceManagementException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		return retSpace;
	}
	
}
