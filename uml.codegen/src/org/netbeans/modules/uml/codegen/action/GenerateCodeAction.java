
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

package org.netbeans.modules.uml.codegen.action;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JButton;

import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.codegen.CodeGenUtil;
import org.netbeans.modules.uml.codegen.action.ui.GenerateCodePanel;
import org.netbeans.modules.uml.codegen.dataaccess.DomainTemplatesRetriever;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;
import org.netbeans.modules.uml.util.AbstractNBTask;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class GenerateCodeAction extends CookieAction
{
    private final static int GC_NODE_PROJECT = 1;
    private final static int GC_NODE_NAMESPACES = 2;
    private final static int GC_NODE_CODEGENS = 4;

    private IProject parentProject = null;
    
    /**
     * Creates a new instance of GenerateCodeAction
     */
    public GenerateCodeAction()
    {
    }


    protected void performAction(Node[] nodes)
    {
        int genCodeNodeType = 0;
        
        final ETArrayList<IElement> elements = new ETArrayList<IElement>();
        ArrayList<IPackage> pkgList = new ArrayList<IPackage>();
        
        // get the parent UML IProject
        IElement element = nodes[0].getCookie(IElement.class);
 
        if (element == null)
        {
            parentProject = lookupProject(nodes[0]);
            element = parentProject;
        } 
        
        else
            parentProject = getParentProject(nodes[0]);
        
        if (nodes.length == 1)
        {
            if (element instanceof IProject)
            {
                genCodeNodeType = GC_NODE_PROJECT;
                element.addElement(element);
            }
            
            else if (element instanceof IPackage)
            {
                genCodeNodeType = GC_NODE_NAMESPACES;
                pkgList.add((IPackage)element);
            }
            
            else // code gen capable element
            {
                genCodeNodeType = GC_NODE_CODEGENS;
                elements.add(element);
            }                
        }

        else
        {
            for (Node curNode : nodes)
            {
                IElement curElement = curNode.getCookie(IElement.class);

                if (curElement != null)
                {
                    if (curElement instanceof IPackage)
                    {
                        genCodeNodeType |= GC_NODE_NAMESPACES;

                        // need to save these to reteive children and possibly
                        // "recursive" children based on dialog selections
                        pkgList.add((IPackage)curElement);
                    }
                    
                    // we only want to add the non-Package elements
                    else
                    {
                        genCodeNodeType |= GC_NODE_CODEGENS;
                        elements.add(curElement);
                    }
                } // if curElement !null
            } // for
        } // else - more than one node selected

        UMLProjectProperties prjProps = 
            retrieveUMLProject().getUMLProjectProperties();
        
        String targetFolderName = prjProps.getCodeGenFolderLocation();
        boolean hasTargetJavaPrj = true;
        
        if (targetFolderName == null || targetFolderName.length() == 0)
        {
            hasTargetJavaPrj = false;
        }
        
        else
        {
            File normalizedFile = FileUtil.normalizeFile(new File(targetFolderName));
            FileObject targetSrcFolderFO = 
                FileUtil.toFileObject(normalizedFile);

            if (targetSrcFolderFO == null || !targetSrcFolderFO.isValid())
            {
                hasTargetJavaPrj = false;
            }
        }
        
//        if (!hasTargetJavaPrj)
        
        boolean templatesEnabled = CodeGenUtil.areTemplatesEnabled(
            prjProps.getCodeGenTemplatesArray());
        
        if (prjProps.isCodeGenShowDialog() || !hasTargetJavaPrj || !templatesEnabled)
        {
            // display gen code options dialog
            GenerateCodePanel gcPanel = new GenerateCodePanel(
                // retrieveExportFolderDefault(nodes[0]), 
                true,
                retrieveUMLProject().getUMLProjectProperties(),
                retrieveUMLProject());

            gcPanel.requestFocus();

            if (!displayDialogDescriptor(
                gcPanel, hasTargetJavaPrj, templatesEnabled))
            {
                return;
            }
            
            gcPanel.storeProjectProperties();
        }
        
        ETList<IElement> selElements = new ETArrayList<IElement>();
        
        // action invoked from Project node
        if ((genCodeNodeType & GC_NODE_PROJECT) == GC_NODE_PROJECT)
            selElements = retrieveNamespaceElements(parentProject, true);
    
        // action not invoked from Project node
        else
        {
            // action invocation included at least one selected non-Package node
            if ((genCodeNodeType & GC_NODE_CODEGENS) == GC_NODE_CODEGENS)
                selElements.addThese(elements);

            // action invocation included at least one selected Package node
            if ((genCodeNodeType & GC_NODE_NAMESPACES) == GC_NODE_NAMESPACES)
            {
                // iterate through all selected package nodes and get 
                // the code gen capable elements - recursively if requested
                for (IPackage pkg: pkgList)
                {
                    ETList<IElement> selected = 
                        retrieveNamespaceElements((INamespace)pkg, true);

                    if (selected != null && selected.size() > 0)
                        selElements.addThese(selected);
                }
            }
        }        
        
        if (selElements == null || selElements.size() == 0)
        {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(
                    NbBundle.getMessage(
                        GenerateCodeAction.class, "MSG_NoElementsFound"), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE));

            return;
        }
        
        else
            selElements = removeImportedElements(selElements);
        
        final ETList<IElement> ecElements = selElements;
        
        RequestProcessor processor = 
            new RequestProcessor("uml/ExportCode"); // NOI18N
        
        HashMap<String, Object> settings = new HashMap<String, Object>();
        
        settings.put(AbstractNBTask.SETTING_KEY_TASK_NAME, 
            NbBundle.getMessage(GenerateCodeAction.class, 
                "CTL_ExportCodeActionName")); // NOI18N

        settings.put(AbstractNBTask.SETTING_KEY_TOTAL_ITEMS, 
            new Integer(selElements.size()));
        
        final String destFolderName = prjProps.getCodeGenFolderLocation();
        final boolean backupSources = prjProps.isCodeGenBackupSources();
    	final boolean generateMarkers = prjProps.isCodeGenUseMarkers();
        final boolean addMarkers = prjProps.isCodeGenAddMarkers();
        final boolean showGCDialog = prjProps.isCodeGenShowDialog();
        
        GenerateCodeTask task = new GenerateCodeTask(
            settings, selElements, parentProject.getName(), destFolderName, 
            backupSources, generateMarkers, addMarkers, showGCDialog);
        
        processor.post(task);
    }

    private final static String COLON_COLON = "::"; // NOI18N
    
    private ETList<IElement> removeImportedElements(ETList<IElement> elements)
    {
        String parentProjectName = parentProject.getName();
        ETList<IElement> noImports = new ETArrayList<IElement>();
        
        for (IElement ele: elements)
        {
            String eleName = ((INamedElement)ele).getFullyQualifiedName(true);
            
            int end = eleName.indexOf(COLON_COLON);
            String realPrjOwner = eleName;
            
            if (end != -1)
                realPrjOwner = eleName.substring(0, end);
            
            if (realPrjOwner.equals(parentProjectName))
                noImports.add(ele);
        }
        
        return noImports;
    }
    
    private boolean displayDialogDescriptor(
        GenerateCodePanel gcPanel, 
        boolean hasTargetJavaPrj, 
        boolean templatesEnabled)
    {
        JButton detailsButton = new JButton();
        detailsButton.setActionCommand("TEMPLATES"); // NOI18N
        detailsButton.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(
            GenerateCodeAction.class, "ACSD_DetailsButton")); // NOI18N

        if (templatesEnabled)
        {
            Mnemonics.setLocalizedText(detailsButton, 
                NbBundle.getMessage(
                GenerateCodeAction.class, "LBL_TemplatesShowButton")); // NOI18N
        }
        
        else
        {
            Mnemonics.setLocalizedText(detailsButton, 
                NbBundle.getMessage(
                GenerateCodeAction.class, "LBL_TemplatesHideButton")); // NOI18N
        }
        
        Object[] buttonOptions =
        {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION,
            detailsButton
        };
        
        Object[] closeOptions =
        {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION,
            DialogDescriptor.DEFAULT_OPTION
        };
        
        GenerateCodeDescriptor gcd = new GenerateCodeDescriptor(
            gcPanel, // inner pane
            NbBundle.getMessage(GenerateCodeAction.class,
            "LBL_GenerateCodeDialog_Title"), // NOI18N
            true, // modal flag
            buttonOptions, // button option type
            NotifyDescriptor.OK_OPTION, // default button
            DialogDescriptor.DEFAULT_ALIGN, // button alignment
            new HelpCtx("uml_diagrams_generating_code"), // NOI18N
            gcPanel, // button action listener
            false); // isLeaf
        
        gcPanel.getAccessibleContext().setAccessibleName(NbBundle
                .getMessage(GenerateCodeAction.class, "ACSN_CodeGenDialog")); // NOI18N
        gcPanel.getAccessibleContext().setAccessibleDescription(NbBundle
                .getMessage(GenerateCodeAction.class, "ACSD_CodeGenDialog")); // NOI18N
        gcPanel.requestFocus();
        
        gcd.setClosingOptions(closeOptions);

        return (DialogDisplayer.getDefault().notify(gcd) == 
            NotifyDescriptor.OK_OPTION);
    }

    
    protected Class[] cookieClasses()
    {
        return new Class[] {UMLProject.class, IElement.class};
    }

    
    protected int mode()
    {
        return MODE_ALL;
    }
    
    
    @Override
    protected boolean enable(Node[] nodes)
    {
        if (nodes == null || nodes.length == 0)
            return false;
        
        IElement element = nodes[0].getCookie(IElement.class);
        
        IProject parentPrj = null;
        if (element == null)
        {
            parentPrj = lookupProject(nodes[0]);
            element = parentPrj;
        }
        
        // we may have a UML project node and it doesn't hold IElement 
        // as a cookie so it won't pass the automated IElement mode test
        // so we need to verify that it is indeed a UML project (IProject)
        // and if it is, then it is 'enabled'
        boolean checkSuperEnable = nodes.length > 1 || parentPrj == null;
        
        if (checkSuperEnable && !super.enable(nodes))
            return false;
        
        // get the parent UML IProject
        if (parentPrj == null)
            parentPrj = getParentProject(nodes[0]);
        
        Project assocProject = ProjectUtil.findNetBeansProjectForModel(parentPrj);
        if (assocProject instanceof UMLProject)
        {
            UMLProject umlProject = (UMLProject)assocProject;
            UMLProjectProperties props = umlProject.getUMLProjectProperties();
        
            if (props != null) 
            {
                String mode = props.getProjectMode();
                if (mode != null) 
                {
                    if (mode.equals(UMLProject.PROJECT_MODE_ANALYSIS_STR))
                    {
                        return false;
                    }
                }
            }         
        }
        
        else
        {
            return false;
        }
        
        // single node selection criteria is a little different/easier/faster
        // than multi-node selection criteria
        if (nodes.length == 1)
        {
            // node has to be Project, Package, 
            // or CodeGenType (see enum defined at top of this class)
            if (element instanceof IProject ||
                element instanceof IPackage)
            {
                return true;
            }
            
            else if (isCodeGenElement(element))
                return true;
            
			else 
				return false;
        }
        
        // multiple nodes were selected - all must meet certain conditions
        // for this action to be available
        
        for (Node curNode: nodes)
        {
            IElement curEle = curNode.getCookie(IElement.class);
            
            // UML project node can't be part of a multi-node selection
            if (curEle instanceof IProject)
                return false;
            
            // all selected elements must be from same UML project
            if (curEle == null || getParentProject(curEle) != parentPrj)
                return false;
				
            // all selected nodes must be of type package or code gen capable
            if ((curEle instanceof IPackage) || isCodeGenElement(curEle))
				continue;
			else 
				return false;
        } // for-each curNode/nodes
        
        return true;
    }
        

    private boolean isCodeGenElement(IElement element)
    {
        try
        {
            CodeGenType cgType = CodeGenType.valueOf(element.getElementType());
			if (cgType == null)
                return false;
                
            if (element.toString().equalsIgnoreCase(
                getUnnamedElementPreference()))
            {
                return false;
            }
            
            // if the element is an inner element, then we
            // do not enable this action
            IElement owner = element.getOwner();
            if (owner instanceof IClass ||
                owner instanceof IEnumeration || 
                owner instanceof IInterface)
            {
                return false;
            }
			
            return true;
        }
        
        catch (IllegalArgumentException ex)
        {
            return false;
        }
    }
    
    @Override
    protected boolean asynchronous()
    {
        return false;
    }
    
    public HelpCtx getHelpCtx()
    {
        return null;
    }
    
    public String getName()
    {
        return NbBundle.getMessage(
            GenerateCodeAction.class, "CTL_ExportCodeActionName"); // NOI18N
    }
    

    // helper methods

    private IProject getParentProject(Node node)
    {
        IElement element = node.getCookie(IElement.class);
        if (element == null)
            return null;
        
        return (getParentProject(element));
    }
    
    private IProject getParentProject(IElement element)
    {
        return element.getProject();
    }
    
    private IProject lookupProject(Node node)
    {
        UMLProject umlProject = 
            node.getLookup().lookup(UMLProject.class);
        
        if (umlProject != null)
        {
            UMLProjectHelper helper = 
                umlProject.getLookup().lookup(UMLProjectHelper.class);
            
            return helper.getProject();
        }
        return null;
    }
    
    private UMLProject retrieveUMLProject()
    {
        return (UMLProject)ProjectUtil
            .findNetBeansProjectForModel(parentProject);
    }
    

//    private final static String BASE_QUERY = 
//        "//*[name() = \'UML:Class\'" + // NOI18N
//        " or name() = \'UML:Interface\'" + // NOI18N
//        " or name() = \'UML:Enumeration\'" + // NOI18N
//        " or name() = \'UML:Actor\'" + // NOI18N
//        " or name() = \'UML:UseCase\'" + // NOI18N
//        " or name() = \'UML:Component\'" + // NOI18N
//        " or name() = \'UML:Invocation\'" + // NOI18N
//        " or name() = \'UML:Lifeline\'" + // NOI18N
//        " or name() = \'UML:Node\'" + // NOI18N
//        " or name() = \'UML:SimpleState\'" + // NOI18N
//        " or name() = \'UML:AbortedFinalState\'" + // NOI18N
//        " or name() = \'UML:ActivityFinalNode\'" + // NOI18N
//        " or name() = \'UML:ChoicePseudoState\'" + // NOI18N
//        " or name() = \'UML:CombinedFragment\'" + // NOI18N
//        " or name() = \'UML:Comment\'" + // NOI18N
//        " or name() = \'UML:CompositeState\'" + // NOI18N
//        " or name() = \'UML:DataStore\'" + // NOI18N
//        " or name() = \'UML:Decision\'" + // NOI18N
//        " or name() = \'UML:DeepHistoryState\'" + // NOI18N
//        " or name() = \'UML:DeploymentSpecification\'" + // NOI18N
//        " or name() = \'UML:DerivationClassifier\'" + // NOI18N
//        " or name() = \'UML:EntryPointState\'" + // NOI18N
//        " or name() = \'UML:FinalState\'" + // NOI18N
//        " or name() = \'UML:FlowFinal\'" + // NOI18N
//        " or name() = \'UML:InitialNode\'" + // NOI18N
//        " or name() = \'UML:JunctionState\'" + // NOI18N
//        " or name() = \'UML:Package\'" + // NOI18N
//        " or name() = \'UML:ParameterUsage\'" + // NOI18N
//        " or name() = \'UML:ShallowHistoryState\'" + // NOI18N
//        " or name() = \'UML:Signal\'" + // NOI18N
//        " or name() = \'UML:SubmachineState\'" + // NOI18N
//        " or name() = \'UML:TemplateClass\']"; // NOI18N
    
    public ETList<IElement> retrieveNamespaceElements(
        INamespace nsElement, boolean recursiveSearch)
    {
        String query = null;
        IElementLocator elementLocator = new ElementLocator();

        if (nsElement instanceof IProject)
        {
            return elementLocator.findElementsByDeepQuery(nsElement, 
                getQuery((UMLProject)ProjectUtil.findElementOwner(nsElement)));
        }
        
        else // package scoped
        {
            org.dom4j.Node node = ((IVersionableElement)nsElement).getNode();
            
            query = node.getUniquePath() +
                getQuery((UMLProject)ProjectUtil.findElementOwner(nsElement));
            
            if (recursiveSearch)
                return elementLocator.findElementsByDeepQuery(nsElement, query);
            
            else 
                return elementLocator.findElementsByQuery(nsElement, query);
        }
    }

    private String getQuery(UMLProject umlProject)
    {
        DomainTemplatesRetriever.clear();
        DomainTemplatesRetriever.load(umlProject);
        
        List<String> elementTypes = 
            DomainTemplatesRetriever.retrieveProjectEnabledModelElements();
        
        StringBuffer query = new StringBuffer("//*["); // NOI18N
        
        for (String eleType: elementTypes)
        {
            query.append("name()=" + "\'UML:" + eleType + "\' or "); // NOI18N
        }
        
        int lastindex = query.length() - 1;
        query.delete(lastindex-3, lastindex);
        query.append("]"); // NOI18N
        
//        System.out.println();
//        System.out.println("Gen Code query: " + query.toString());
//        System.out.println();
        return query.toString();
    }
    
    private String getUnnamedElementPreference()
    {
        //Kris Richards - returning the default value.
        return "Unnamed" ; // NOI18N    
    }

    public enum CodeGenType 
    {
        Class, 
        Interface, 
        Enumeration, 
        UseCase, 
        Actor,
        Component,
        Datatype,
        Invocation,
        Lifeline,
        Node,
        SimpleState,
        AbortedFinalState,
        ActivityFinalNode,
        ChoicePseudoState,
        CombinedFragment,
        Comment,
        CompositeState,
        DataStore,
        Decision,
        DeepHistoryState,
        DeploymentSpecification,
        DerivationClassifier,
        EntryPointState,
        FinalState,
        FlowFinal,
        InitialNode,
        JunctionState,
        Package,
        ParameterUsage,
        ShallowHistoryState,
        Signal,
        SubmachineState,
        TemplateClass
    };
}
