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


package org.netbeans.modules.uml.designpattern;

import org.netbeans.modules.uml.ui.addins.diagramcreator.DiagCreatorAddIn;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JDialog;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.IApplication;
//import org.netbeans.modules.uml.core.addinframework.IAddIn;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.constructs.ConstructsRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IConstructsRelationFactory;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NameResolver;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AssociationKindEnum;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.profiles.IProfile;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.RTMode;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.CodeGenerator;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.ICodeGenerator;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager;
import org.netbeans.modules.uml.core.support.umlutils.PropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.PropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.PropertyElementManager;
import org.netbeans.modules.uml.core.typemanagement.IPickListManager;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.ui.addins.diagramcreator.IDiagCreatorAddIn;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.controls.drawingarea.ElementBroadcastAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.IElementBroadcastAction;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.ADTransferable;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.SwingPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.UserSettings;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext;
import org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor;

public class DesignPatternManager implements IDesignPatternManager {
    private IPropertyDefinitionFactory 	m_Factory = null;
    private IPropertyElementManager     m_Manager = null;
    private IDataFormatter 				m_Formatter = null;
    // 6.2 feature private IPropertyDefinitionFilter   m_DefinitionFilter = null;
    
    private IDesignPatternDetails m_Details = null;
    private IProjectTreeControl m_ProjectTree = null;
    
    private Hashtable < String, Vector <Object> > m_ToDoMapRel = new Hashtable < String, Vector <Object> >();
    private ETList < String > m_ToDoSet = new ETArrayList<String>();
    private ETList < IElement > m_ToDoEle = new ETArrayList<IElement>();
    
    private int m_ParticipantScope = -1;
    private boolean m_DisplayGUI = true;
    private ICollaboration m_Collaboration = null;
    
    private ETList < String > m_IdsAlreadyProcessing = new ETArrayList<String>();
    
    private boolean m_Promoting = false;
    
    private int m_IDCount = 0;
    private HashMap< String, IPropertyElement > m_RelCreateMap = new HashMap<String, IPropertyElement>();
    
    // need an event dispatcher, so we can fire events
    private IDesignPatternEventDispatcher m_EventDispatcher = null;
    
    private String m_UnnamedString = "";
    
    private HashMap< String, String > m_AttrsOpsClonedMap = new HashMap<String, String>();
    private JDialog m_Dialog = null;

    /**
     *
     */
    public DesignPatternManager() {
        super();
        // create a factory if necessary
        if (m_Factory == null) {
            m_Factory = new PropertyDefinitionFactory();
        }
        if (m_Factory != null) {
            // set up where the factory should get its definitions
            ICoreProduct pProduct = ProductHelper.getCoreProduct();
            if (pProduct != null) {
                IConfigManager pConfigMgr = pProduct.getConfigManager();
                if (pConfigMgr != null) {
                    String home = pConfigMgr.getDefaultConfigLocation();
                    String file = home + "PropertyDefinitions.etc";
                    m_Factory.setDefinitionFile(file);
                }
            }
        }
        // create a element manager if necessary
        if (m_Manager == null) {
            m_Manager = new PropertyElementManager();
        }
        if (m_Manager != null) {
            // set up its information
            m_Manager.setPDFactory(m_Factory);
            m_Manager.setCreateSubs(true);
        }
        // create a data formatter if necessary
        if (m_Formatter == null) {
            m_Formatter = new DataFormatter();
        }
        // create a definition filter if necessary
      /* 6.2 feature
      if (m_DefinitionFilter == null)
      {
         m_DefinitionFilter = new PropertyDefinitionFilter();
         m_DefinitionFilter.setProcessName("DesignPatternApply");
      }
       */
        // set up an event dispatcher
        String eventName = "DesignPatternDispatcher";
        if (m_EventDispatcher == null) {
            EventDispatchRetriever ret = EventDispatchRetriever.instance();
            IDesignPatternEventDispatcher disp = ret.getDispatcher(eventName);
            if( disp != null ) {
                m_EventDispatcher = disp;
            } else {
                m_EventDispatcher = new DesignPatternEventDispatcher();
                ICoreProduct pProduct = ProductHelper.getCoreProduct();
                if (pProduct != null) {
                    IEventDispatchController pEventController = pProduct.getEventDispatchController();
                    if (pEventController != null) {
                        pEventController.addDispatcher(eventName, m_EventDispatcher);
                    }
                }
            }
        }
        // if there is not a value in the cloned element or
        // the prop element that we are processing is not the
        // name, then set the value of the prop element
        IPreferenceAccessor pPref = PreferenceAccessor.instance();
        if (pPref != null) {
            m_UnnamedString = pPref.getDefaultElementName();
        }
        m_AttrsOpsClonedMap.clear();
    }
    /**
     * Retrieve the pattern details for this manager
     *
     *
     * @param pVal[out]	The details stored on the manager
     *
     * @return HRESULT
     */
    public IDesignPatternDetails getDetails() {
        return m_Details;
    }
    /**
     * Set the pattern details for this manager
     *
     *
     * @param pVal[in]	The details stored on the manager
     *
     * @return HRESULT
     */
    public void setDetails(IDesignPatternDetails newVal) {
        m_Details = newVal;
    }
    /**
     * The project tree used
     */
    public IProjectTreeControl getProjectTree() {
        return m_ProjectTree;
    }
    /**
     * The project tree used
     */
    public void setProjectTree(IProjectTreeControl newVal) {
        m_ProjectTree = newVal;
    }
    /**
     * The scope of where to get the participants for the pattern from - the tree, the drawing
     * area, or do not determine
     *
     * @param[in] pVal		The scope
     *
     * @return HRESULT
     */
    public int getParticipantScope() {
        return m_ParticipantScope;
    }
    /**
     * See getParticipantScoope
     *
     *
     * @return HRESULT
     */
    public void setParticipantScope(int newVal) {
        m_ParticipantScope = newVal;
    }
    /**
     * Whether or not the manager should display the GUI associated with the process
     *
     * @param[out] pVal		The flag for whether or not the gui should be displayed
     *
     * @return HRESULT
     */
    public boolean getDisplayGUI() {
        return m_DisplayGUI;
    }
    /**
     * See getDisplayGUI
     *
     *
     * @return HRESULT
     */
    public void setDisplayGUI(boolean newVal) {
        m_DisplayGUI = newVal;
    }
    /**
     * The currently known collaboration
     */
    public ICollaboration getCollaboration() {
        return m_Collaboration;
    }
    /**
     * The currently known collaboration
     */
    public void setCollaboration(ICollaboration newVal) {
        m_Collaboration = newVal;
    }
    public void setDialog(JDialog diag) {
        m_Dialog = diag;
    }
    /**
     * Validate the information on the passed in details
     *
     *
     * @param *pDetails[in]		The pattern details
     *
     * @return HRESULT
     *
     */
    public int validatePattern(IDesignPatternDetails pDetails) {
        int status = -1;
        if (pDetails != null) {
            //
            // validate the project
            //
            IProject pProject = pDetails.getProject();
            if (pProject == null) {
                // there is no project
                status = DesignPatternResultsEnum.DPR_E_PROJECT_NOT_EXIST;
            } else {
                // is the project versioned
                boolean bVersioned = pProject.isVersioned();
                if (bVersioned) {
                    // it is versioned, so must be checked out (not readonly)
                    String filename = pProject.getVersionedFileName();
                    File file = new File(filename);
                    if (!file.canWrite()) {
                        status = DesignPatternResultsEnum.DPR_E_CHECKOUT_PROJECT;
                    }
                } else {
                    // not version controlled. so now check if the file exists
                    String fileName = pProject.getFileName();
                    File file = new File(fileName);
                    if (file == null) {
                        // the file does not exist, but we do have a project
                        // so this is the case where the project has not been saved
                        // do nothing here
                    } else {
                        // file does exist, so is the project file readonly
                        if (!file.canWrite()) {
                            status = DesignPatternResultsEnum.DPR_E_PROJECT_READ_ONLY;
                        }
                    }
                }
            }
            //
            // validate if there is a diagram name, that it is valid
            //
            boolean createDiagram = pDetails.getCreateDiagram();
            if (createDiagram) {
                String diagName = pDetails.getDiagramName();
                IProxyDiagramManager pDiagManager = ProxyDiagramManager.instance();
                if (pDiagManager != null) {
                    ETPairT<Boolean, String> pResult = pDiagManager.isValidDiagramName(diagName);
                    if (pResult != null) {
                        boolean bIsCorrect = pResult.getParamOne().booleanValue();
                        if (!bIsCorrect) {
                            status = DesignPatternResultsEnum.DPR_E_INVALID_DIAGRAMNAME;
                        }
                    }
                }
            }
            //
            // validate the roles - making sure there are participant names for each one
            // and that the names are valid
            //
            if (status == -1) {
                ETList < IDesignPatternRole > pRoles = pDetails.getRoles();
                if (pRoles != null) {
                    // loop through the roles
                    int count = pRoles.getCount();
                    for (int x = 0; x < count; x++) {
                        IDesignPatternRole pRole = pRoles.item(x);
                        if (pRole != null) {
                            //
                            // Make sure there are participants for each of the roles
                            //
                            String roleID = pRole.getID();
                            if (roleID != null && roleID.length() > 0) {
                                ETList <String> pNames = pDetails.getParticipantNames(roleID);
                                if (pNames != null) {
                                    int strcount = pNames.getCount();
                                    if (strcount == 0) {
                                        status = DesignPatternResultsEnum.DPR_E_INVALIDROLE;
                                        break;
                                    }
                                } else {
                                    status = DesignPatternResultsEnum.DPR_E_NOROLES;
                                }
                            }
                        }
                    }
                }
            }
        }
        return status;
    }
    /**
     * Apply ("instantiate") the pattern
     *
     *
     * @param pDetails[in]		The design pattern details
     *
     * @return HRESULT
     *
     */
    public void applyPattern(IDesignPatternDetails pDetails) {
        if (pDetails != null) {
            boolean hr = true;
            m_IDCount = 0;
            // set up member variable, so don't have to keep passing it around
            setDetails(pDetails);
            // validate the pattern information
            int status = validatePattern(pDetails);
            if (status == -1) {
                // fire the preapply
                firePreApply();
                ICoreProduct pCoreProduct = ProductRetriever.retrieveProduct();
                // turn off the events that the property editor responds to
                IPropertyEditor pEditor = ProductHelper.getPropertyEditor();
                if (pEditor != null) {
                    pEditor.setRespondToReload(false);
                }
                //
                // turn Java round trip off before doing this.  Round trip is not behaving it self very
                // well so it was deemed from above that we should just turn it off.
                //
                DesignPatternUtilities.startWaitCursor(m_Dialog);
                int oldMode = RTMode.RTM_OFF;
                IRoundTripController rtController = null;
                if (pCoreProduct != null) {
                    rtController = pCoreProduct.getRoundTripController();
                    if (rtController != null) {
                        // what was the mode before we set it off
                        oldMode = rtController.getMode();
                        rtController.setMode(RTMode.RTM_SHUT_DOWN);
                    }
                }
                // force this code into an if statement, so that the blocker is just for the apply
                // not the post apply
                if (hr) {
                    // need to warn the user if we are going to change
                    hr = overwriteParticipants(pDetails);
                    if (hr) {
                        // Create a blocker that blocks all events
                        //EventBlocker.startBlocking();
                        //
                        // then create any of the participants that don't already exist in the project
                        //
                        hr = createParticipants(pDetails);
                        if (hr) {
                            // now make each of the participants look like the item that they are supposed to
                            // be patterned from
                            hr = cloneParticipants(pDetails);
                            if (hr) {
                                // now we want to see if the user has told us to create a diagram for this newly
                                // instantiated pattern and its details
                                boolean bCreate = pDetails.getCreateDiagram();
                                if (bCreate) {
                                    hr = createDiagramForPatternDetails(pDetails);
                                }
                            }
                        }
                        //EventBlocker.stopBlocking(false);
                    }
                }
                
                // since we turned off events during the apply, we need to potentially
                // refresh the diagrams because they will not be updated
// Events are presently enabled, and this causes NPE in AWT thread where diagram is updated.
//            if (hr)
//            {
//               refreshDiagrams();
//            }
                // now tell the property editor to listen to reload events
                if (pEditor != null) {
                    pEditor.setRespondToReload(true);
                }
                // now put roundtrip back to the state it was before this process
                if (rtController != null) {
                    // what was the mode before we set it off
                    rtController.setMode(oldMode);
                }
                // refresh the project tree because some things may appear to be a top-level
                // (relationships) when they really are listed in folders under their respected
                // elements
                IProjectTreeControl pControl = ProductHelper.getProjectTree();
                if (pControl != null && hr) {
                    pControl.refresh(true);
                }
                // because all of the events were blocked during this process, we need to mark
                // the project as dirty
                IProject pProject = pDetails.getProject();
                if (pProject != null && hr) {
                    pProject.setDirty(true);
                }
                
                // now we want to see if the user has told us to do code generation for the stuff
                // that was just applied
// IZ 78766 - conover: decision was to not prompt for code gen anymore 
//                if (hr) {
//                    performCodeGeneration();
//                }
                
                // fire the post apply
                firePostApply();
                DesignPatternUtilities.endWaitCursor(m_Dialog);
            } else {
                // Error, not every role has a participant named
            }
        }
    }
    /**
     * Create any of the participants in the pattern details that do not exist in the project/namespace
     * specified in the pattern details
     *
     *
     * @param *pDetails[in]		The pattern details
     *
     * @return HRESULT
     *
     */
    public boolean overwriteParticipants(IDesignPatternDetails pDetails) {
        boolean hr = true;
        boolean found = false;
        String msgText = "";
        // get the roles
        ETList <IDesignPatternRole > pRoles = pDetails.getRoles();
        if (pRoles != null) {
            // loop through the roles
            int count = pRoles.getCount();
            for (int x = 0; x < count; x++) {
                IDesignPatternRole pRole = pRoles.item(x);
                if (pRole != null) {
                    String roleID = pRole.getID();
                    if (roleID != null && roleID.length() > 0) {
                        // get the participants for this role
                        ETList < String > pNames = pDetails.getParticipantNames(roleID);
                        if (pNames != null) {
                            // loop through
                            int count2 = pNames.getCount();
                            for (int y = 0; y < count2; y++) {
                                String name = pNames.item(y);
                                if (name != null && name.length() > 0) {
                                    // Need to find out if the project contains an element of this type
                                    // with this name
                                    IProject pProject = pDetails.getProject();
                                    // if the type id is blank, then look for an element matching the id
                                    // but if it is not blank, then look for the specific element
                                    // this actually isn't necessary since we are looking by id, at one
                                    // point I think we were looking by element type
                                    String type = pRole.getTypeID();
                                    String pattern = "";
                                    if (type != null && type.length() > 0) {
                                        pattern = "//UML:" + type;
                                    } else {
                                        pattern = "//*";
                                    }
                                    pattern += "[@xmi.id=\"" + name + "\"]";
                                    IElement pElement = getElement(pProject, pattern);
                                    if (pElement != null) {
                                        // found the element
                                        if (pElement instanceof INamedElement) {
                                            INamedElement pNamed = (INamedElement)pElement;
                                            found = true;
                                            String name2 = pNamed.getName();
                                            msgText += name2;
                                            msgText += "\n";
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (found) {
            //
            // check the preference to see what the user wants us to do
            // ask them, overwrite, or do not overwrite
            //
            IPreferenceManager2 prefMgr = ProductHelper.getPreferenceManager();
            if (prefMgr != null) {
                String cstr = DesignPatternUtilities.translateString("IDS_OVERWRITEMSG");
                cstr = StringUtilities.replaceSubString(cstr, "%s", msgText);
                String title = DesignPatternUtilities.translateString("IDS_OVERWRITETITLE");
                IPreferenceQuestionDialog cpQuestionDialog = new SwingPreferenceQuestionDialog(m_Dialog);
                if( cpQuestionDialog != null ) {
                    int nResult = cpQuestionDialog.displayFromStrings("Default",
                            "DesignCenter|DesignPatternCatalog",
                            "UML_ShowMe_Overwrite_Existing_Participants",
                            "PSK_ALWAYS",
                            "PSK_NEVER",
                            "PSK_ASK",
                            cstr,
                            SimpleQuestionDialogResultKind.SQDRK_RESULT_NO,
                            title,
                            SimpleQuestionDialogKind.SQDK_YESNO,
                            MessageIconKindEnum.EDIK_ICONQUESTION,
                            null);
                    //
                    // Now we know what the user wants us to do
                    //
                    if (nResult != SimpleQuestionDialogResultKind.SQDRK_RESULT_YES) {
                        hr = false;
                    }
                }
            }
        }
        return hr;
    }
    /**
     * Create any of the participants in the pattern details that do not exist in the project/namespace
     * specified in the pattern details
     *
     *
     * @param *pDetails[in]		The pattern details
     *
     * @return HRESULT
     *
     */
    public boolean createParticipants(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            // get the roles
            ETList <IDesignPatternRole > pRoles = pDetails.getRoles();
            if (pRoles != null) {
                // loop through the roles
                int count = pRoles.getCount();
                for (int x = 0; x < count; x++) {
                    IDesignPatternRole pRole = pRoles.item(x);
                    if (pRole != null) {
                        String roleID = pRole.getID();
                        if (roleID != null && roleID.length() > 0) {
                            // get the participants for this role
                            ETList <String> pNames = pDetails.getParticipantNames(roleID);
                            if (pNames != null) {
                                // loop through
                                int count2 = pNames.getCount();
                                for (int y = 0; y < count2; y++) {
                                    String name = pNames.item(y);
                                    if (name != null && name.length() > 0) {
                                        // Need to find out if the project contains an element of this type
                                        // with this name
                                        IProject pProject = pDetails.getProject();
                                        // if the type id is blank, then look for an element matching the id
                                        // but if it is not blank, then look for the specific element
                                        // this actually isn't necessary since we are looking by id, at one
                                        // point I think we were looking by element type
                                        String type = pRole.getTypeID();
                                        String pattern;
                                        if (type != null && type.length() > 0) {
                                            pattern = "//UML:" + type;
                                        } else {
                                            pattern = "//*";
                                        }
                                        pattern += "[@xmi.id=\"" + name + "\"]";
                                        IElement pElement = getElement(pProject, pattern);
                                        if (pElement != null) {
                                            // found the element
                                            pDetails.addParticipantInstance(roleID, pElement);
                                        } else {
                                            if (type == null || type.length() == 0) {
                                                // if there is no type (Interface, Actor, Class, UseCase)
                                                // then that means that it can be of type Classifier
                                                // so go to the preference about unknown classifier and
                                                // that will be the type to create
                                                //type = "Class";
                                                IPreferenceAccessor pPref = PreferenceAccessor.instance();
                                                if (pPref != null) {
                                                    type = pPref.getUnknownClassifierType();
                                                }
                                            }
                                            // did not find an element, so create it
                                            Object pDisp = createElement(type);
                                            if (pDisp != null) {
                                                if (pDisp instanceof INamedElement) {
                                                    INamedElement pNamedEle = (INamedElement)pDisp;
                                                    // add it to the right namespace
                                                    addElementToNamespace(pDetails, pDisp);
                                                    pNamedEle.setName(name);
                                                    pDetails.addParticipantInstance(roleID, pNamedEle);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return hr;
    }
    /**
     * Add the passed in dispatch to the namespace stored on the passed in details
     *
     *
     * @param pDetails[in]			The pattern details
     * @param pDispatch[in]			The element to add
     *
     * @return HRESULT
     *
     */
    public void addElementToNamespace(IDesignPatternDetails pDetails, Object pDispatch) {
        if (pDetails != null && pDispatch != null) {
            // must be a NamedElement to add
            if (pDispatch instanceof INamedElement) {
                INamedElement pNamedEle = (INamedElement)pDispatch;
                // need the namespace on the details
                INamespace pName = pDetails.getNamespace();
                if (pName != null) {
                    // add it
                    pName.addOwnedElement(pNamedEle);
                } else {
                    // no namespace on the details, so add it to the project
                    IProject pProj = pDetails.getProject();
                    if (pProj != null) {
                        pProj.addOwnedElement(pNamedEle);
                    }
                }
            }
        }
    }
    /**
     * Create a new element of the passed in type
     *
     *
     * @param type[in]			The type of element to create
     * @param pDispatch[out]	The created element
     *
     * @return HRESULT
     *
     */
    public Object createElement(String type) {
        Object pDispatch = null;
        FactoryRetriever ret = FactoryRetriever.instance();
        if (ret != null) {
            pDispatch = ret.createType(type, null);
        }
        return pDispatch;
    }
    /**
     * Get the element from the project that matches the pattern
     *
     *
     * @param pProject[in]			The project to look in
     * @param pattern[in]			The xpath query to use in the search
     * @param pElement[out]			The found element
     *
     * @return HRESULT
     *
     */
    public IElement getElement(IProject pProject, String pattern) {
        IElement pElement = null;
        if (pProject != null) {
            // use the element locator to do this
            IElementLocator pLocator = new ElementLocator();
            if (pLocator != null) {
                // find any elements matching the xpath query
                ETList <IElement> pElements = pLocator.findElementsByDeepQuery(pProject, pattern);
                if (pElements != null) {
                    // put one of them into the out param
                    int count = pElements.getCount();
                    for (int x = 0; x < count; x++) {
                        pElement = pElements.item(x);
                        break;
                    }
                }
            }
        }
        return pElement;
    }
    /**
     * Clone the participants in the pattern details.  This will create a property element structure
     * exactly like the element that the participant is playing.
     *
     *
     * @param *pDetails[in]		The pattern details
     *
     * @return HRESULT
     *
     */
    public boolean cloneParticipants(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            // clear out our temporary variables
            m_IdsAlreadyProcessing.clear();
            m_ToDoMapRel.clear();
            m_ToDoSet.clear();
            m_ToDoEle.clear();
            
            // get the roles
            ETList <IDesignPatternRole> pRoles = pDetails.getRoles();
            if (pRoles != null) {
                // loop through the roles
                int count = pRoles.getCount();
                for (int x = 0; x < count; x++) {
                    IDesignPatternRole pRole = pRoles.item(x);
                    if (pRole != null) {
                        String roleID = pRole.getID();
                        if (roleID != null && roleID.length() > 0) {
                            // get the element that is representing the role
                            IElement pElementDP = pRole.getElement();
                            if (pElementDP != null) {
                                // the element on this role should be a part facade
                                // but when we go to clone it, we don't want to clone it as a part
                                // facade, we want to clone it as the type of element that the part
                                // facade represents
                                // this is stored in the type constraint (which is on a parameterable element)
                                if (pElementDP instanceof IParameterableElement) {
                                    IParameterableElement pFacade = (IParameterableElement)pElementDP;
                                    String type = pFacade.getTypeConstraint();
                                    if (type == null || type.length() == 0) {
                                        // if there is no type (Interface, Actor, Class, UseCase)
                                        // then that means that it can be of type Classifier
                                        // so go to the preference about unknown classifier and
                                        // that will be the type to create
                                        //type = "Class";
                                        IPreferenceAccessor pPref = PreferenceAccessor.instance();
                                        type = pPref.getUnknownClassifierType();
                                    }
                                    // get the definition for this element type
                                    IPropertyDefinition pDef = m_Factory.getPropertyDefinitionByName(type);
                                    if (pDef != null) {
                                        // build a corresponding property element for this role
                                        m_Manager.setModelElement(pElementDP);
                                        IPropertyElement pPropEleDP = m_Manager.buildTopPropertyElement(pDef);
                                        //
                                        // Find the participants (instances) in this role
                                        //
                                        ETList <IElement> pElements = pDetails.getParticipantInstances(roleID);
                                        if (pElements != null) {
                                            // loop through the instances
                                            int count2 = pElements.getCount();
                                            for (int x2 = 0; x2 < count2; x2++) {
                                                IElement pElement = pElements.item(x2);
                                                if (pElement != null) {
                                                    // build a corresponding property element for this instance
                                                    m_Manager.setModelElement(pElement);
                                                    IPropertyElement pPropEle = m_Manager.buildTopPropertyElement(pDef);
                                                    // if we have property elements for the role and the instance
                                                    if (pPropEle != null && pPropEleDP != null) {
                                                        // make the instance look just like the role
                                                        hr = clonePropertyElements(pPropEleDP, pPropEle);
                                                        if (hr) {
                                                            // save it
                                                            hr = saveParticipant(pPropEle);
                                                            if (hr) {
                                                                // if the save was successful then store a tagged
                                                                // value on the element that references the project
                                                                // the xmi id of the pattern, and the xmi id of the
                                                                // part facade (role) for future use
                                                                addTaggedValue(pDetails, pRole, pElement);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return hr;
    }
    /**
     * Clone the property elements.  This will create a property element structure
     * exactly like the element that the participant is playing.
     *
     *
     * @param pPropEleDP[in]		The property element representing the role
     * @param pPropEle[in]			The property element representing the instance (participant)
     *
     * @return HRESULT
     *
     */
    public boolean clonePropertyElements(IPropertyElement pPropEleDP, IPropertyElement pPropEle) {
        boolean hr = true;
        if (pPropEleDP != null && pPropEle != null) {
            // Need to make sure each of the elements that we are processing are
            // built before we try and clone them
            ensurePropertyElementIsBuilt(pPropEle);
            ensurePropertyElementIsBuilt(pPropEleDP);
            //
            // Now get each of the sub elements in the one that is the role
            //
            Vector<IPropertyElement> pSubsDP = pPropEleDP.getSubElements();
            if (pSubsDP != null) {
                // And get each of the sub elements in the one that is the instance
                Vector<IPropertyElement> pSubs = pPropEle.getSubElements();
                if (pSubs != null) {
                    // Make sure that the instance is at least smaller than the role otherwise
                    // bad things started happening
                    int count = pSubs.size();
                    int countDP = pSubsDP.size();
                    for (int i = 0; i < countDP; i++) {
                        // get the sub element for the role
                        IPropertyElement pEleDP = pSubsDP.get(i);
                        if (pEleDP != null) {
                            // gets its info
                            String name = pEleDP.getName();
                            if (name == null) {
                                name = "";
                            }
                            String value = pEleDP.getValue();
                            if (value == null){
                                value = "";
                            }
                            // get the matching prop element in the to be cloned element
                            IPropertyElement pEle = findMatchingPropertyElement(pEleDP, pPropEle);
                            if (pEle != null) {
                                // found the same element in the cloned element
                                String myValue = pEle.getValue();
                                if (myValue == null){
                                    myValue = "";
                                }
                                if (name.equals("Type")) {
                                    // special processing for type
                                    determineTypeValue(pEleDP, pEle);
                                } else if ( (myValue.length() == 0) ||
                                        ((!(name.equals("Name"))) && (!(name.equals("Alias")))) ||
                                        (myValue.equals(m_UnnamedString))
                                        ) {
                                    if (value != null && value.length() > 0) {
                                        pEle.setValue(value);
                                    }
                                    // now determine if the prop element has been modified
                                    IPropertyDefinition pDef = pEle.getPropertyDefinition();
                                    if (pDef != null) {
                                        // we are only going to say it was modified if there is a set method
                                        // on it
                                        String putM = pDef.getSetMethod();
                                        if (putM != null && putM.length() > 0) {
                                            pEle.setModified(true);
                                        }
                                    }
                                }
                            } else {
                                // did not find the element in the cloned element, so create one
                                IPropertyElement pNewEle = cloneNewPropertyElement(pEleDP);
                                if (pNewEle != null) {
                                    // set it up so that it will be saved
                                    pNewEle.setModified(true);
                                    pPropEle.setModified(true);
                                    // set up its model element (behind the scenes)
                                    Object pDisp = pPropEle.getElement();
                                    pNewEle.setElement(pDisp);
                                    // add the new element to the parent prop element
                                    pPropEle.addSubElement(pNewEle);
                                    // set the new element as the current
                                    pEle = pNewEle;
                                    
                                    if (name.equals("Type")) {
                                        // special processing for type
                                        determineTypeValue(pEleDP, pEle);
                                    }
                                }
                            }
                            // recurse through the sub elements
                            clonePropertyElements(pEleDP, pEle);
                        }
                    }
                }
            }
        }
        return hr;
    }
        /*
        Special processing for a property element that is the "Type" property of an element.  If it is a type,
        then we need to potentially create that type in the current project.  We were originally using the setType2
        method, but that was setting a non-fully qualified name and it was using the preference, so it was
        creating a datatype (which may not be what we want).
         */
    public void determineTypeValue(IPropertyElement pPropEleDP, IPropertyElement pPropEle) {
        if (pPropEleDP != null && pPropEle != null) {
            Object pDisp = pPropEleDP.getElement();
            if (pDisp != null) {
                // get the type from the current IElement
                IClassifier pTypeEle = null;
                if (pDisp instanceof IParameter) {
                    IParameter pParam = (IParameter)pDisp;
                    pTypeEle = pParam.getType();
                } else if (pDisp instanceof IAttribute) {
                    IAttribute pAttr = (IAttribute)pDisp;
                    pTypeEle = pAttr.getType();
                } else {
                    // this was a catch all (definitely used for RaisedException on an Operation)
                    if (pDisp instanceof IClassifier) {
                        pTypeEle = (IClassifier)pDisp;
                    }
                }
                // have the element representing the type
                if (pTypeEle != null) {
                    // if the element representing the type is a part facade (a participant in the pattern)
                    // then that is one case
                    if (pTypeEle instanceof IPartFacade) {
                        IPartFacade pRole = (IPartFacade)pTypeEle;
                        if (pRole != null) {
                            // if it is a participant in the pattern then at the time that this property element
                            // is saved, the type will become the instance in the project that is playing the role
                            // of this participant
                            // we need to mark this as special to figure this out at save time
                            // so we are storing the id of the participant with a tag of "?" on the property element value
                            // this will be interpreted at save time
                            String id = pRole.getXMIID();
                            String tempStr = "?" + id;
                            pPropEle.setValue(tempStr);
                            pPropEle.setModified(true);
                        }
                    } else {
                        // the element representing the type is not a part facade
                        // so at save time of this property element, this type will need to either be created
                        // in the current project or if already there, used
                        // we need to mark this as special to figure this out at save time
                        // so we are storing the metatype of the type element and its full name on the property
                        // element value
                        // this will be interpreted at save time
                        String typeStr = pTypeEle.getElementType();
                        String typeName = pTypeEle.getQualifiedName2();
                        String temp =  typeStr + "??";
                        if(typeName.indexOf("::") <= 0)
                        {
                            temp += "::";
                        }
                        temp += typeName;
                        
                        pPropEle.setValue(temp);
                        pPropEle.setModified(true);
                    }
                }
            }
        }
    }
    /**
     * Create a new property element
     *
     *
     * @param pPropEleDP[in]		The property element representing the role
     * @param pNewPropEle[out]		The newly created property element
     *
     * @return HRESULT
     *
     */
    public IPropertyElement cloneNewPropertyElement(IPropertyElement pPropEleDP) {
        IPropertyElement pNewPropEle = null;
        if (pPropEleDP != null) {
            boolean bMember = isMemberOfCollection(pPropEleDP);
            if (bMember) {
                pNewPropEle = cloneNewPropertyElementMultiple(pPropEleDP);
            } else {
                pNewPropEle = cloneNewPropertyElementSingle(pPropEleDP);
            }
        }
        return pNewPropEle;
    }
    /**
     * Create a new property element that represents an element that is a member of a collection
     *
     *
     * @param pPropEleDP[in]		The property element representing the role
     * @param pNewPropEle[out]		The newly created property element
     *
     * @return HRESULT
     *
     */
    public IPropertyElement cloneNewPropertyElementMultiple(IPropertyElement pPropEleDP) {
        IPropertyElement pNewPropEle = null;
        if (pPropEleDP != null) {
            if (m_Factory != null) {
                // get the element representing the role
                Object pDisp = pPropEleDP.getElement();
                // get the definition for this element
                String name = pPropEleDP.getName();
                IPropertyDefinition pDef = m_Factory.getPropertyDefinitionByName(name);
                if (pDef != null) {
                    // if the definition is a relationship, we need to have special processing
                    // because the definitions for the property editor (which we are reusing)
                    // do not have create/insert information for relationships because you cannot
                    // do this through the property editor
                    IPropertyDefinition pDef2 = m_Factory.getPropertyDefinitionForElement("", pDisp);
                    String name2 = pDef2.getName();
                    String lookupName = name2;
                    String isRel = pDef.getFromAttrMap("isRel");
                    if (isRel != null && isRel.length() > 0) {
                        // we have created special create definitions for relationships that are
                        // in the property editor file, so find it
                        lookupName += "Create";
                        IPropertyDefinition pDefCreate = m_Factory.getPropertyDefinitionByName(lookupName);
                        if (pDefCreate != null) {
                            // set up the element manager to build the right stuff
                            m_Manager.setCreateSubs(true);
                            m_Manager.setModelElement(pDisp);
                            // build the property element based on this create definition
                            IPropertyElement pNewEle = m_Manager.buildTopPropertyElement(pDefCreate);
                            if (pNewEle != null) {
                                // For these newly created relation prop elements
                                // they are stubbed out and look like:
                                // Generalization
                                //		Super
                                //		Sub
                                // there is no information on them, at this time, so we needed
                                // a way to tie this prop element back to the element on the design pattern role
                                // so that when we go to save this relation prop element, the right model elements
                                // will be the super/sub in this case
                                //
                                // Created a map to house this information.  The generalization prop element will
                                // store a number as its value and this will be the lookup into the map to find
                                // the corresponding design pattern role prop element.
                                Integer i = new Integer(m_IDCount);
                                String buffer = i.toString();
                                pNewEle.setValue(buffer);
                                m_RelCreateMap.put(buffer, pPropEleDP);
                                m_IDCount++;
                                //
                                // Finish setting the information on the new prop element
                                //
                                pNewEle.setElement(null);
                                pNewEle.setModified(true);
                                markPropertyElementAsModified(pNewEle);
                                pNewPropEle = pNewEle;
                            }
                        }
                    } else {
                        // set up the element manager to build the right stuff
                        m_Manager.setCreateSubs(true);
                        m_Manager.setModelElement(pDisp);
                        // build the property element based on this definition
                        IPropertyElement pNewEle = m_Manager.buildTopPropertyElement(pDef);
                        if (pNewEle != null) {
                            // Finish setting the information on the new prop element
                            pNewEle.setElement(null);
                            clearOutPropertyElement(pNewEle);
                            pNewEle.setModified(true);
                            pNewPropEle = pNewEle;
                        }
                    }
                }
            }
        }
        return pNewPropEle;
    }
    /**
     * Special processing for a newly created property element to blank out its element
     * and set its modified flag for itself and any of its sub elements.  This sets up
     * the element so that it will be properly saved when the time comes.
     *
     *
     * @param pPropEle[in]		The property element to process
     *
     * @return HRESULT
     *
     */
    public void clearOutPropertyElement(IPropertyElement pPropEle) {
        if (pPropEle != null) {
            // get the sub elements
            Vector <IPropertyElement> pSubs = pPropEle.getSubElements();
            if (pSubs != null) {
                // loop through the sub elements
                int count = pSubs.size();
                for (int x = 0; x < count; x++) {
                    IPropertyElement pSub = pSubs.get(x);
                    if (pSub != null) {
                        // blank out its model element
                        pSub.setElement(null);
                        // set its modified flag to true, so it will be stored
                        pSub.setModified(true);
                        // recurse through children
                        clearOutPropertyElement(pSub);
                    }
                }
            }
        }
    }
    /**
     * Special processing for a property element to set its modified flag for itself
     * and any of its sub elements.  This sets up
     * the element so that it will be properly saved when the time comes.
     *
     *
     * @param pPropEle[in]		The property element to process
     *
     * @return HRESULT
     */
    public void markPropertyElementAsModified(IPropertyElement pPropEle) {
        if (pPropEle != null) {
            // set its modified flag to true, so it will be stored
            pPropEle.setModified(true);
            // get the sub elements
            Vector <IPropertyElement> pSubs = pPropEle.getSubElements();
            if (pSubs != null) {
                // loop through the sub elements
                int count = pSubs.size();
                for (int x = 0; x < count; x++) {
                    IPropertyElement pSub = pSubs.get(x);
                    if (pSub != null) {
                        // set its modified flag to true, so it will be stored
                        pSub.setModified(true);
                        // recurse through children
                        markPropertyElementAsModified(pSub);
                    }
                }
            }
        }
    }
    /**
     * Create a new property element that represents a simple element.
     *
     *
     * @param pPropEleDP[in]		The property element representing the role
     * @param pNewPropEle[out]		The newly created property element
     *
     * @return HRESULT
     *
     */
    public IPropertyElement cloneNewPropertyElementSingle(IPropertyElement pPropEleDP) {
        IPropertyElement pNewPropEle = null;
        if (pPropEleDP != null) {
            // create a new property element
            IPropertyElement pNewEle = new PropertyElement();
            if (pNewEle != null) {
                // get the existing one's information
                String name = pPropEleDP.getName();
                String value = pPropEleDP.getValue();
                // set the new one's information
                pNewEle.setName(name);
                if (value != null && value.equals("successor")) {
                    int j = 0;
                }
                if (value == null || value.length() == 0) {
                    int j = 0;
                }
                if (value != null && value.length() > 0) {
                    pNewEle.setValue(value);
                }
                
                String origValue = pPropEleDP.getOrigValue();
                if((origValue != null) && (origValue.length() > 0))
                {
                    pNewEle.setOrigValue(origValue);
                }
                //
                // Need to store a property definition on the newly created property element
                //
                IPropertyDefinition pNewDef = null;
                // get the existing one's definition
                IPropertyDefinition pDef = pPropEleDP.getPropertyDefinition();
                if (pDef != null) {
                    // has it been marked as a relationship (because we need to treat those differently)
                    String isRel = pDef.getFromAttrMap("isRel");
                    if (isRel != null && isRel.length() > 0) {
                        // we have created special create definitions for relationships that are
                        // in the property editor file, so find it
                        String newName = name;
                        newName += "Create";
                        pNewDef = m_Factory.getPropertyDefinitionByName(newName);
                    } else {
                        pNewDef = pDef;
                    }
                }
                // now that we have the new definition, set it on the prop element
                if (pNewDef != null) {
                    pNewEle.setPropertyDefinition(pNewDef);
                    pNewEle.setModified(true);
                    pNewPropEle = pNewEle;
                }
            }
        }
        return pNewPropEle;
    }
    /**
     * Determines if the property element is a member of a collection or not.
     *
     *
     * @param pPropEle[in]		The property element to process
     * @param bMember[out]		Whether or not the property element is a member of a collection
     *
     * @return HRESULT
     *
     */
    public boolean isMemberOfCollection(IPropertyElement pPropEle) {
        boolean bMember = false;
        if (pPropEle != null) {
            //
            // check the parent property element's multiplicity
            //
            long mult = 0;
            IPropertyElement pParentEle = pPropEle.getParent();
            if (pParentEle != null) {
                IPropertyDefinition pParentDef = pParentEle.getPropertyDefinition();
                if (pParentDef != null) {
                    mult = pParentDef.getMultiplicity();
                }
            }
            if (mult > 1) {
                bMember = true;
            }
        }
        return bMember;
    }
    /**
     * Helper function to find the corresponding prop element in the array of passed in prop elements.
     *
     *
     * @param pPropEle[in]			The prop element to find a match for
     * @param pPropEles[in]			The prop elements to search
     * @param pFound[out]			The matching property element
     *
     * @return HRESULT
     */
    public IPropertyElement findMatchingPropertyElement(IPropertyElement pPropEle, IPropertyElement pPropEles) {
        IPropertyElement pFound = null;
        if (pPropEle != null && pPropEles != null) {
            // make sure what we are searching in is fully built
            ensurePropertyElementIsBuilt(pPropEle);
            // get the definition based on the name of the property element, I can't remember why
            // we didn't want to get the definition from the property element itself
            String identifier = "";
            String name = pPropEle.getName();
            IPropertyDefinition pDef = m_Factory.getPropertyDefinitionByName(name);
            if (pDef != null) {
                // check to see if the definition has been marked as a relationship
                // because it will have special processing associated with it to determine
                // what the matching rel is, both ends will need to be the same
                String isRel = pDef.getFromAttrMap("isRel");
                if (isRel != null && isRel.length() > 0) {
                    pFound = findMatchingForRel(pPropEle, pPropEles);
                    // set this temporarily so that are upcoming processing will work
                    identifier = "dummy";
                } else {
                    // if we are not a relationship, see if there is something on the definition that
                    // tells us what to use instead of the definition name
                    identifier = pDef.getFromAttrMap("identifier");
                }
            }
            // if we haven't already found our match (would be the case if we found the relationship)
            if (pFound == null) {
                if (identifier == null || identifier.length() == 0) {
                    // if there is no identifier on the definition
                    // then the matching property element will be based on name
                    IPropertyElement pSubEle = pPropEles.getSubElement(name, null);
                    if (pSubEle != null) {
                        // sometimes the sub property element had the pdref definition stored on it
                        // and that was causing problems in the save, so we are replacing the definition
                        // that it knows about with the complete one
                        if (pDef != null){
                            pSubEle.setPropertyDefinition(pDef);
                        }
                        pFound = pSubEle;
                    }
                } else if (identifier.equals("FormatString")) {
                    // if the identifier has stated that we should use the model elements format string
                    // to identify it, then there will be special processing to find it
                    IPropertyElement pSubEle = findMatchingPropertyElementFormatString(pPropEle, pPropEles);
                    if (pSubEle != null) {
                        pFound = pSubEle;
                    }
                } else if (identifier.equals("Participant")) {
                    // if the identifier has stated that we should use the model elements participant
                    // to identify it, then there will be special processing to find it
                    // this will be the case for association ends
                    IPropertyElement pSubEle = findMatchingPropertyElementParticipant(pPropEle, pPropEles);
                    if (pSubEle != null) {
                        pFound = pSubEle;
                    }
                } else {
                    // there is an identifier, but it is not one of the special cases listed above
                    // so the values of the property elements will need to match in order for it to
                    // be the one
                    // get the property element from the one that we are using as our base
                    IPropertyElement pKeySub = pPropEle.getSubElement(identifier, null);
                    if (pKeySub != null) {
                        // get its value
                        String keyValue = pKeySub.getValue();
                        // get the property element from the array of ones that we are searching
                        IPropertyElement pKeySub2 = pPropEles.getSubElement(identifier, null);
                        if (pKeySub2 != null) {
                            // found it, so get its value
                            String keyValue2 = pKeySub2.getValue();
                            if (keyValue.equals(keyValue2)) {
                                // same value, so return it
                                pFound = pKeySub2;
                            }
                        } else {
                            Vector < IPropertyElement > pSubs = pPropEles.getSubElements();
                            // the identifier could be a property element other than the one that we are on
                            // so just in case we will go one more level deep and check there for a match
                            int count = pSubs.size();
                            for (int x = 0; x < count; x++) {
                                IPropertyElement pSubEle = pSubs.get(x);
                                if (pSubEle != null) {
                                    ensurePropertyElementIsBuilt(pSubEle);
                                    IPropertyElement pKeySub3 = pSubEle.getSubElement(identifier, null);
                                    if (pKeySub3 != null) {
                                        String keyValue3 = pKeySub3.getValue();
                                        if (keyValue.equals(keyValue3)) {
                                            pFound = pSubEle;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return pFound;
    }
    /**
     * Finds the appropriate ends of an association in the prop element structure.
     * Had to have this as special processing because there was not a way to generically define
     * how to retrieve the ends.  The structure is different based on the type of association.
     * And the structure is built differently than all of the other relationships, because the individual
     * ends are housed in an Ends routine for an association, but for an aggregation, they are
     * just like any other relationship.
     *
     *
     * @param pPropEle[in]		The association prop element structure
     * @param pFound[out]		The prop elemements representing the association ends
     *
     * @return HRESULT
     *
     */
    public IPropertyElement[] findAssocEnds(IPropertyElement pPropEle) {
                /* This is not used in c++, so did not convert it
                 
                HR_PARM_CHECK(pPropEle && pFound);
           HRESULT hr = S_OK;
           try
           {
                 *pFound = 0;
                        IPropertyElements > pTemp;
                        pTemp.CoCreateInstance(__uuidof(PropertyElements)));
                        if (pTemp)
                        {
                                // need to make sure the prop element that we are dealing with is fully built
                                EnsurePropertyElementIsBuilt(pPropEle);
                                // get its sub elements
                                IPropertyElements > pSubEles;
                                pPropEle.getSubElements(&pSubEles));
                                if (pSubEles)
                                {
                                        // get the sub element that is the "ends" element
                                        IPropertyElement > pEndsEle;
                                        pSubEles.Item(CComVariant("Ends"), &pEndsEle);
                                        if (pEndsEle)
                                        {
                                                // found it, so it must be an association
                                                // get the sub elements of the ends element
                                                // which will be the individual ends
                                                IPropertyElements > pSubEles2;
                                                pEndsEle.getSubElements(&pSubEles2));
                                                if (pSubEles2)
                                                {
                                                        // loop through the individual ends and add them to the
                                                        // array to return
                                                        int count;
                                                        pSubEles2.getCount();
                                                        for (int x = 0; x < count; x++)
                                                        {
                                                                IPropertyElement > pEle;
                                                                pSubEles2.Item(CComVariant(x), &pEle));
                                                                if (pEle)
                                                                {
                                                                        pTemp.Add(pEle));
                                                                }
                                                        }
                                                }
                                        }
                                        else
                                        {
                                                // did not find an "ends" element, so this must be an aggregation
                                                // get its part end and agg end individually
                                                IPropertyElement > pPartEndEle;
                                                pSubEles.Item(CComVariant("PartEnd"), &pPartEndEle));
                                                if (pPartEndEle)
                                                {
                                                        pTemp.Add(pPartEndEle));
                                                }
                                                IPropertyElement > pAggEndEle;
                                                pSubEles.Item(CComVariant("AggregateEnd"), &pAggEndEle));
                                                if (pAggEndEle)
                                                {
                                                        pTemp.Add(pAggEndEle));
                                                }
                                        }
                                }
                                pTemp.CopyTo(pFound));
                        }
           }
           catch ( _com_error& err )
           {
                  hr = COMErrorManager::ReportError( err );
           }
           return hr;
                 */
        return null;
    }
    /**
     * Relationships posed a problem in the generic prop element structure, because the related prop element
     * could not be determined by name, or value, or format string.  It is based on the model elements on the
     * ends of the relationship.
     *
     *
     * @param pPropEle[in]		The prop element to find the matching one
     * @param pPropEles[in]		The structure of prop elements to search for the matching one
     * @param pFound[out]		The matching one
     *
     * @return HRESULT
     */
    public IPropertyElement findMatchingForRel(IPropertyElement pPropEle, IPropertyElement pPropEles) {
        IPropertyElement pFound = null;
        if (pPropEle != null && pPropEles != null) {
            // what is the prop element that we are looking for
            String name = pPropEle.getName();
            if (name == null){
                name = "";
            }
            // get the model element of the prop element we are looking for
            Object pDispDP = pPropEle.getElement();
            // special case if the model element is an aggregation, the prop element could be named
            // association, but we don't want to look up the values of an aggregation based on the definition
            // for an association because the getting of the ends is different
            if (pDispDP instanceof IAggregation) {
                name = "Aggregation";
            }
            if (name.equals("Association")) {
                // special processing for an association
                pFound = findMatchingForAssociation(pPropEle, pPropEles);
            } else {
                // all other relationships can be handled generically through their definitions
                IElement pElement1 = getEnd1OfRelationship(pPropEle);
                IElement pElement2 = getEnd2OfRelationship(pPropEle);
                // get each end of the relationship (the pattern roles)
                if (pElement1 != null && pElement2 != null) {
                    // now loop through the prop elements that we are supposed to be looking
                    // for the prop element
                    Vector <IPropertyElement> pSubs = pPropEles.getSubElements();
                    if (pSubs != null) {
                        int count = pSubs.size();
                        for (int x = 0; x < count; x++) {
                            IPropertyElement pEle = pSubs.get(x);
                            if (pEle != null) {
                                // need to make sure its sub elements are there
                                ensurePropertyElementIsBuilt(pEle);
                                // get each end of the relationship (the instances)
                                IElement pElement3 = getEnd1OfRelationship(pEle);
                                IElement pElement4 = getEnd2OfRelationship(pEle);
                                if (pElement3 != null && pElement4 != null) {
                                    // Now we need to determine if the elements that we have just
                                    // found on the instance match any of the elements playing in
                                    // the appropriate role
                                    String id1 = pElement1.getXMIID();
                                    boolean bSame1 = findMatchingInstance(id1, pElement3);
                                    if (bSame1) {
                                        // one of the elements match, so see if the other element matches
                                        // this will prevent us from creating duplicate relationships
                                        // between the same elements
                                        String id2 = pElement2.getXMIID();
                                        boolean bSame2 = findMatchingInstance(id2, pElement4);
                                        if (bSame2) {
                                            pFound = pEle;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return pFound;
    }
    /**
     * Associations posed a problem in the generic prop element structure, because the related prop element
     * could not be determined by name, or value, or format string.  It is based on the model elements on the
     * ends of the relationship.
     *
     *
     * @param pPropEle[in]		The prop element to find the matching one
     * @param pPropEles[in]		The structure of prop elements to search for the matching one
     * @param pFound[out]		The matching one
     *
     * @return HRESULT
     */
    public IPropertyElement findMatchingForAssociation(IPropertyElement pPropEle, IPropertyElement pPropEles) {
        IPropertyElement pFound = null;
        if (pPropEle != null && pPropEles != null) {
            // get each end of the relationship (the pattern roles)
            IElement pElement1 = getEnd1OfRelationship(pPropEle);
            IElement pElement2 = getEnd2OfRelationship(pPropEle);
            if (pElement1 != null && pElement2 != null) {
                // now loop through the prop elements that we are supposed to be looking
                // for the prop element
                Vector <IPropertyElement> pSubs = pPropEles.getSubElements();
                if (pSubs != null) {
                    int count = pSubs.size();
                    for (int x = 0; x < count; x++) {
                        IPropertyElement pEle = pSubs.get(x);
                        if (pEle != null) {
                            // need to make sure its sub elements are there
                            ensurePropertyElementIsBuilt(pEle);
                            // get each end of the relationship (the instances)
                            IElement pElement3 = getEnd1OfRelationship(pEle);
                            IElement pElement4 = getEnd2OfRelationship(pEle);
                            if (pElement3 != null && pElement4 != null) {
                                // Now we need to determine if the elements that we have just
                                // found on the instance match any of the elements playing in
                                // the appropriate role
                                String id1 = pElement1.getXMIID();
                                boolean bSame1 = findMatchingInstance(id1, pElement3);
                                if (bSame1) {
                                    // one of the elements match, so see if the other element matches
                                    // this will prevent us from creating duplicate relationships
                                    // between the same elements
                                    String id2 = pElement2.getXMIID();
                                    boolean bSame2 = findMatchingInstance(id2, pElement4);
                                    if (bSame2) {
                                        pFound = pEle;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return pFound;
    }
    /**
     * Performs a save of the passed in property element
     *
     *
     * @param pPropEle[in]		The property element to save
     *
     * @return HRESULT
     *
     */
    public boolean saveParticipant(IPropertyElement pPropEle) {
        boolean hr = true;
        if (pPropEle != null) {
            // process all of its sub elements
            Vector <IPropertyElement> pSubs = pPropEle.getSubElements();
            if (pSubs != null) {
                int count = pSubs.size();
                for (int i = 0; i < count; i++) {
                    IPropertyElement pSubEle = pSubs.get(i);
                    if (pSubEle != null) {
                        // get information from the sub element
                        String name = pSubEle.getName();
                        String value = pSubEle.getValue();
                        Object pDisp = pSubEle.getElement();
                        IPropertyDefinition pDef = pSubEle.getPropertyDefinition();
                        if (pDef != null) {
                            // check to see if this sub element represents a relationship
                            // because if it does we will have much different processing for it
                            String special = pDef.getFromAttrMap("isRel");
                            if (special != null && special.length() > 0) {
                                saveRelationship(pSubEle);
                            } else {
                                // not a relationship, so now see what about the sub element
                                // we need to do
                                String putM = pDef.getSetMethod();
                                String createM = pDef.getCreateMethod();
                                if (putM != null && putM.length() > 0) {
                                    // this sub element just has a get/put on its definition
                                    saveParticipantPutMethod(pDef, pSubEle);
                                } else if (createM != null && createM.length() > 0) {
                                    // this sub element has a create method on its definition
                                    // so it may need to be created
                                    saveParticipantCreateMethod(pDef, pSubEle);
                                }
                                // recurse through my sub elements
                                saveParticipant(pSubEle);
                            }
                        }
                    }
                }
            }
        }
        return hr;
    }
    /**
     * Performs a save of the passed in property element (which is a relationship)
     *
     *
     * @param pPropEle[in]		The property element to save
     *
     * @return HRESULT
     *
     */
    public void saveRelationship(IPropertyElement pPropEle) {
        if (pPropEle != null) {
            // do we even need to save it
            boolean mod = pPropEle.getModified();
            if (mod) {
                // get some information from the property element
                String name = pPropEle.getName();
                String val = pPropEle.getValue();
                if (val != null && val.length() > 0) {
                    // we did some special processing when we "cloned" this property element and
                    // stored in its value a tie to the pattern role's property element that this
                    // property element is based on
                    // so now we are retrieving it from the map
                    IPropertyElement pFound = m_RelCreateMap.get(val);
                    if (pFound != null) {
                        // this will be the property element that this relationship property element is based
                        // on
                        // we needed this information to properly set the ends of the relationship so we needed
                        // to get back to the roles that were in the pattern relationship
                        // more special processing to handle relationship creates
                        boolean hr = saveParticipantSpecialCreate(pFound, pPropEle);
                        if (hr) {
                            // the definition that we used to create this relationship was different than
                            // an existing relationships definition, so now that the rel has been created
                            // we need to swap out the create definition for the real one
                            String cstr = StringUtilities.replaceAllSubstrings(name, "Create", "");
                            // now that we have actually done the create of the relationship
                            // we need to populate its property element information with that
                            // from the pattern's relationship information
                            IPropertyDefinition pDef = m_Factory.getPropertyDefinitionByName(cstr);
                            if (pDef != null) {
                                // got the definition for "Generalization" instead of "GeneralizationCreate"
                                Object pDisp2 = pPropEle.getElement();
                                // got the element that is the relationship
                                // so rebuild the relationships property element structure
                                m_Manager.reloadElement(pDisp2, pDef, pPropEle);
                                pPropEle.setPropertyDefinition(pDef);
                                // copy the information from the pattern role to this newly created
                                // element structure
                                clonePropertyElements(pFound, pPropEle);
                                // save the new element structure data
                                saveParticipant(pPropEle);
                            }
                        }
                        m_RelCreateMap.remove(val);
                    }
                }
            }
        }
    }
    /**
     * Performs a save of the passed in property element
     *
     *
     * @param pDef[in]			The property definition
     * @param pPropEle[in]		The property element to save
     *
     * @return HRESULT
     */
    public void saveParticipantPutMethod(IPropertyDefinition pDef, IPropertyElement pPropEle) {
        if (pDef != null && pPropEle != null) {
            // get some information from the element
            String name = pPropEle.getName();
            String value = pPropEle.getValue();
            if (value == null){
                pPropEle.setValue("");
            }
            Object pDisp = pPropEle.getElement();
            // get some information from the definition
            String pDefName = pDef.getName();
            String cType = pDef.getControlType();
            if (cType == null){
                cType = "";
            }
            String parentDefName = "";
            IPropertyDefinition parentDef = pDef.getParent();
            if (parentDef != null) {
                parentDefName = parentDef.getName();
            }
            // should we even save at all
            boolean mod = pPropEle.getModified();
            if (mod) {
                // totally had to special case the isNavigable on an association end
                // for times sake
                if (!(pDefName.equals("IsNavigable"))) {
                    if (name.equals("TypeConstraint")) {
                    } else if (name.equals("Type")) {
                        IElement pFound = resolveTypeValue(pPropEle);
                        if (pFound != null) {
                            putTypeValue(pPropEle, pFound);
                        }
                    }
                    // don't need to do a set if the definition has said that this is
                    // a read only value
                    else if (!(cType.equals("read-only"))) {
                        if (name != null && name.equals("Name") && parentDefName != null && parentDefName.indexOf("End") > -1 ) {
                            int j = 0;
                        }
                        
                        m_Manager.processData(pDisp, pDef, pPropEle);
                    }
                } else {
                    // special case for IsNavigable on an association end
                    if (parentDefName.equals("AssociationEnd")) {
                        // if we are a sub element of an association end, not a NavigableEnd, or PartEnd, or AggEnd
                        // and we should be setting it to true
                        // we can let the regular processing handle it because it will make it navigable
                        if (value.equals("1")) {
                            m_Manager.processData(pDisp, pDef, pPropEle);
                        }
                    } else {
                        // we are a sub element of a NavigableEnd, PartEnd, or AggEnd
                        if (value.equals("0")) {
                            // if we are setting it to false
                            // we can let the regular processing handle it because it will make it nonnavigable
                            m_Manager.processData(pDisp, pDef, pPropEle);
                        } else {
                            // if we are setting it to true and we are an AssociationEnd
                            if (pDisp instanceof IAssociationEnd) {
                                IAssociationEnd pEnd = (IAssociationEnd)pDisp;
                                // force it to be a navigable end
                                INavigableEnd pNav = pEnd.makeNavigable();
                                // and then store the navigable end on our property element
                                pPropEle.setElement(pNav);
                            }
                        }
                    }
                }
            }
        }
    }
        /*
        Special processing for the property elements that represent "Type".  This is figuring out if an
        element representing the "Type" should be created or not.
         */
    public IElement resolveTypeValue(IPropertyElement pPropEle) {
        IElement pElement = null;
        if (pPropEle != null) {
            // get some information from the property element
            String name = pPropEle.getName();
            String value = pPropEle.getValue();
            // get the IElement from the property element
            Object pDisp = pPropEle.getElement();
            String type = "";
            String value2 = "";
            // when we "cloned" this type property element we stored special information in the value
            // field to help us in saving this properly.
            // we are now going to figure out whether or not we need to process the special information
            if (value != null && value.length() > 0) {
                String xstr = value;
                // if there was a string put into the value field that contains a "??", it is telling us
                // that we need to look for an IElement in the current project of a certain type and certain
                // name.
                // The string will be "Class??a::b::c" which will tell us that we should be looking for a Class
                // named c in the package structure a::b
                // if we don't find it, we create it
                int pos = xstr.indexOf("??");
                if( pos >= 0 ) {
                    type = xstr.substring( 0, pos );
                    value2 = xstr.substring( pos + 2 );        // Step passed the '?' and go to end
                    pElement = getElementInDetailsProject2(value2, type);
                    if (pElement == null) {
                        pElement = createElementInDetailsProject2(value2, type);
                    }
                } else {
                    // if there was a string put into the value field that contains a "?", it is telling us
                    // that one of the participants in the pattern was the "type", so we need to translate
                    // that into the proper instance of the participant
                    //
                    // The string will be "?123.344" which will tell us that we should be looking for an
                    // instance in the project that is playing this role, and that instance should be the type
                    // for this property element
                    int pos2 = xstr.indexOf("?");
                    if( pos2 >= 0 ) {
                        String id = xstr.substring( pos2 + 1 );        // Step passed the '?'
                        ETList <IElement> pParticipants = m_Details.getParticipantInstances(id);
                        if (pParticipants != null) {
                            int partCount = pParticipants.getCount();
                            if (partCount > 0) {
                                IElement pParticipant = pParticipants.item(0);
                                if (pParticipant != null) {
                                    pElement = getElementInDetailsProject(pParticipant);
                                    if (pElement == null) {
                                        pElement = createElementInDetailsProject(pParticipant);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return pElement;
    }
        /*
        Special processing for the property elements that represent "Type".  This is actually doing the
        set on the IElement.
         */
    public void putTypeValue(IPropertyElement pPropEle, IElement pElement) {
        if (pPropEle != null && pElement != null) {
            if (pElement instanceof IClassifier) {
                IClassifier pClassifier = (IClassifier)pElement;
                Object pDisp = pPropEle.getElement();
                if (pDisp != null) {
                    if (pDisp instanceof IAttribute) {
                        IAttribute pAttr = (IAttribute)pDisp;
                        pAttr.setType(pClassifier);
                    } else if (pDisp instanceof IParameter) {
                        IParameter pParam = (IParameter)pDisp;
                        pParam.setType(pClassifier);
                    }
                } else {
                    // this is behaving differently than in c++, but I cannot figure out
                    // why at this time.  we are on a Type property element of a return type
                    // on an operation and there is not an element associated with it
                    IPropertyElement pParentEle = pPropEle.getParent();
                    if (pParentEle != null) {
                        String parentName = pParentEle.getName();
                        if (parentName != null && parentName.equals("ReturnType")) {
                            Object pDisp2 = pParentEle.getElement();
                            if (pDisp2 != null) {
                                if (pDisp2 instanceof IOperation) {
                                    IOperation pOp = (IOperation)pDisp2;
                                    String name = pClassifier.getName();
                                    pOp.setReturnType2(name);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * Performs a save of the passed in property element
     *
     *
     * @param pDef[in]			The property definition
     * @param pPropEle[in]		The property element to save
     *
     * @return HRESULT
     */
    public void saveParticipantCreateMethod(IPropertyDefinition pDef, IPropertyElement pPropEle) {
        if (pDef != null && pPropEle != null) {
            String name = pPropEle.getName();
            if (name.equals("Exception")) {
                // special processing for raised exceptions on an operation.  This is similar to all of the
                // special put logic for "type", but the exceptions actually get created through the
                // AddRaisedException logic which goes through this path
                Vector <IPropertyElement> pSubs = pPropEle.getSubElements();
                if (pSubs != null) {
                    // the sub elements of "Exception" are "Name" property elements which will contain
                    // the fully qualified name of the exception
                    int count = pSubs.size();
                    if (count > 0) {
                        IPropertyElement pSub = pSubs.get(0);
                        if (pSub != null) {
                            // figure out if we need to create the IElement representing the exception
                            IElement pFound = resolveTypeValue(pSub);
                            if (pFound != null) {
                                // it was either created or found, so now set the value of the "Name" property
                                // element to the fully qualified name, and continue on with the create logic.
                                // this will cause AddRaisedException to be called, and it will find the "type"
                                if (pFound instanceof INamedElement) {
                                    INamedElement pNamed = (INamedElement)pFound;
                                    String qname = pNamed.getQualifiedName2();
                                    if (qname.length() == 0) {
                                        int j = 0;
                                    }
                                    pSub.setValue(qname);
                                }
                            }
                        }
                    }
                }
            }
            // get the parent of the property element that we are processing
            // this will be needed to do the insert after we have created
            IPropertyElement pParentEle = pPropEle.getParent();
            if (pParentEle != null) {
                String temp = pParentEle.getName();
                // should we even be saving it
                boolean mod = pPropEle.getModified();
                if (mod) {
                    // yes we should, so we want to make sure that all of my subelements
                    // are also marked to be saved because this routine just does the
                    // create and then we will be looping through all of the sub elements
                    // and doing a put, so we want them to be modified as well
                    markPropertyElementAsModified(pPropEle);
                    // get the model element of the parent
                    Object pDisp2 = pParentEle.getElement();
                    if (pDisp2 != null) {
                        if (pDisp2 instanceof IMultiplicity) {
                            // special processing for multiplicity
                            // in order to do anything with multiplicity, we need the model element
                            // that it is on, so we need to go up one more level to get it
                            IPropertyElement pGrand = pParentEle.getParent();
                            if (pGrand != null) {
                                IPropertyElement pGrand2 = pGrand.getParent();
                                if (pGrand2 != null) {
                                    Object pDispG = pGrand2.getElement();
                                    if (pDispG != null) {
                                        if (pDispG instanceof ITypedElement) {
                                            ITypedElement pElementG = (ITypedElement)pDispG;
                                            IMultiplicity pMult2 = pElementG.getMultiplicity();
                                            if (pMult2 != null) {
                                                IMultiplicityRange pRange = pMult2.createRange();
                                                if (pRange != null) {
                                                    IPropertyElement pLower = pPropEle.getSubElement("Lower", null);
                                                    String low = pLower.getValue();
                                                    IPropertyElement pUpper = pPropEle.getSubElement("Upper", null);
                                                    String up = pUpper.getValue();
                                                    pRange.setLower(low);
                                                    pRange.setUpper(up);
                                                    pMult2.addRange(pRange);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // regular create processing
                            m_Manager.createData(pDisp2, pDef, pPropEle);
                            // do we need to insert as well
                            String insertM = pDef.getInsertMethod();
                            if (insertM != null && insertM.length() > 0) {
                                m_Manager.insertData(pDisp2, pDef, pPropEle);
                            }
                            //
                            // Special code to fix J2629 in jUML.  Stereotypes created through applying a pattern
                            // were not being saved when switching projects because in the save, the profile is
                            // not marked as dirty.  Since we are so close to release, this is the safer change.
                            //
                            if (name.equals("Stereotype")) {
                                Object pDisp = pPropEle.getElement();
                                if (pDisp instanceof IElement) {
                                    IElement pMent = (IElement)pDisp;
                                    IElement owner = pMent.getOwner();
                                    if (owner instanceof IProfile) {
                                        owner.setDirty(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * Special processing required to save a property element that is representing a relationship
     *
     *
     * @param pPropEleDP[in]			The property element in the pattern
     * @param pPropEle[in]				The property element in the instance of the pattern
     *
     * @return HRESULT
     *
     */
    public boolean saveParticipantSpecialCreate(IPropertyElement pPropEleDP, IPropertyElement pPropEle) {
        boolean hr = true;
        if (pPropEleDP != null && pPropEle != null) {
            // get each end of the relationship (the pattern roles)
            IElement pElement1 = getEnd1OfRelationship(pPropEleDP);
            IElement pElement2 = getEnd2OfRelationship(pPropEleDP);
            if (pElement1 != null && pElement2 != null) {
                String id1 = pElement1.getXMIID();
                // get the roles that match the passed in id
                ETList <String> roleIDs = 	m_Details.getParticipantRoles(id1);
                if (roleIDs != null) {
                    // loop through the roles
                    int count = roleIDs.size();
                    if (count > 0) {
                        for (int x = 0; x < count; x++) {
                            String roleID = roleIDs.get(x);
                            if (roleID != null && roleID.length() > 0) {
                                // now get the instances that have been assigned to this role
                                ETList <IElement> pParticipants = m_Details.getParticipantInstances(roleID);
                                if (pParticipants != null) {
                                    // loop through the instances
                                    int partCount = pParticipants.getCount();
                                    for (int x2 = 0; x2 < partCount; x2++) {
                                        IElement pParticipant = pParticipants.get(x2);
                                        if (pParticipant != null) {
                                            saveParticipantSpecialCreateSecondParticipant(pPropEle, pParticipant, pElement2);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // no roles were found for this guy
                        IElement pFound = cloneNonParticipant(pElement1);
                        if (pFound != null) {
                            saveParticipantSpecialCreateSecondParticipant(pPropEle, pFound, pElement2);
                        }
                    }
                }
            }
        }
        return hr;
    }
        /*
        Special processing if there are elements connected to pattern participants that are not in the
        pattern.
         */
    public IElement cloneNonParticipant(IElement pElementToUse) {
        IElement pNew = null;
        if (pElementToUse != null) {
            if (pElementToUse instanceof INamedElement) {
                // find the element in the current project
                IElement pFound = getElementInDetailsProject(pElementToUse);
                if (pFound == null) {
                    // did not find it in the current project, so create it there
                    pFound = createElementInDetailsProject(pElementToUse);
                }
                if (pFound != null) {
                    // now we need to copy the information from the pattern project element
                    // to the current project element
                    if (pElementToUse instanceof IClassifier) {
                        IClassifier pClassifier = (IClassifier)pElementToUse;
                        if (pFound instanceof IClassifier) {
                            IClassifier pClassifierNew = (IClassifier)pFound;
                            cloneAttrsAndOps(pClassifier, pClassifierNew);
                            pNew = pFound;
                        }
                    }
                }
            }
        }
        return pNew;
    }
        /*
        Special processing if there are elements connected to pattern participants that are not in the
        pattern.
         */
    public void cloneAttrsAndOps(IClassifier pClassifier, IClassifier pClassifierNew) {
        if (pClassifier != null && pClassifierNew != null) {
            // check to see if we should be adding the attrs/ops from the pattern element to the current
            // element
            // if it is in the map, we should add them
            // it won't be in the map if it has already been cloned (no need to do it again)
            String id = pClassifierNew.getXMIID();
            String str = m_AttrsOpsClonedMap.get(id);
            if (str != null && str.length() > 0) {
                ETList <IAttribute> pAttrs = pClassifier.getAttributes();
                if (pAttrs != null) {
                    int count = pAttrs.size();
                    for (int x = 0; x < count; x++) {
                        IAttribute pAttr = pAttrs.get(x);
                        // we are going to try and use this logic so that we don't have to write it
                        IVersionableElement pDup = pAttr.duplicate();
                        if (pDup != null) {
                            // add it to the new element
                            if (pDup instanceof IAttribute) {
                                IAttribute pDupNamed = (IAttribute)pDup;
                                pClassifierNew.addAttribute(pDupNamed);
                                // there were some problems with type and the duplicate, so handle it here again
                                IClassifier pType = pAttr.getType();
                                if (pType != null) {
                                    String name = pType.getName();
                                    pDupNamed.setType2(name);
                                }
                            }
                        }
                    }
                }
                ETList <IOperation> pOps = pClassifier.getOperations();
                if (pOps != null) {
                    int count = pOps.size();
                    for (int x = 0; x < count; x++) {
                        IOperation pOp = pOps.get(x);
                        if (pOp != null) {
                            // only clone if it is not redefined
                            boolean bRedefined = pOp.getIsRedefined();
                            if (!bRedefined) {
                                // big problems with the duplicate and operations, so handling it manually
                                IOperation pNewOp = pClassifierNew.createOperation3();
                                if (pNewOp != null) {
                                    pClassifierNew.addOperation(pNewOp);
                                    IPropertyDefinition pDef = m_Factory.getPropertyDefinitionByName("Operation");
                                    if (pDef != null) {
                                        // build a corresponding property element for old pattern
                                        IPropertyElement pPropEleOrig = m_Manager.buildElement(pOp, pDef, null);
                                        if (pPropEleOrig  != null) {
                                            // build a property element for the new pattern
                                            IPropertyElement pPropEle = m_Manager.buildElement(pNewOp, pDef, null);
                                            if (pPropEle != null) {
                                                clonePropertyElements(pPropEleOrig, pPropEle);
                                                saveParticipant(pPropEle);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                m_AttrsOpsClonedMap.remove(str);
            }
        }
    }
        /*
        Special processing if there are elements connected to pattern participants that are not in the
        pattern.
         */
    public void saveParticipantSpecialCreateSecondParticipant(IPropertyElement pPropEle, IElement pFirst, IElement pToProcess) {
        if (pPropEle != null && pFirst != null && pToProcess != null) {
            String id2 = pToProcess.getXMIID();
            // get the roles that match the passed in id
            ETList <String> roleIDs2 = m_Details.getParticipantRoles(id2);
            if (roleIDs2 != null) {
                // loop through the roles
                int count2 = roleIDs2.size();
                if (count2 > 0) {
                    for (int x2= 0; x2 < count2; x2++) {
                        String roleID2 = roleIDs2.get(x2);
                        if (roleID2 != null && roleID2.length() > 0) {
                            // now get the instances that have been assigned to this role
                            ETList <IElement>  pParticipants2 = m_Details.getParticipantInstances(roleID2);
                            if (pParticipants2 != null) {
                                // loop through the instances
                                int partCount2 = pParticipants2.size();
                                for (int y = 0; y < partCount2; y++) {
                                    IElement pParticipant2 = pParticipants2.get(y);
                                    if (pParticipant2 != null) {
                                        createRelationship(pPropEle, pFirst, pParticipant2);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // no roles were found for this guy
                    IElement pFound = cloneNonParticipant(pToProcess);
                    if (pFound != null) {
                        createRelationship(pPropEle, pFirst, pFound);
                    }
                }
            }
        }
    }
        /*
         */
    public void createRelationship(IPropertyElement pPropEle, IElement pParticipant1, IElement pParticipant2) {
        if (pPropEle != null && pParticipant1 != null && pParticipant2 != null) {
            String type = "";
            IPropertyDefinition pDef = pPropEle.getPropertyDefinition();
            if (pDef != null) {
                // the definition for a relationship will include the type of relationship to create
                type = pDef.getFromAttrMap("type");
                if (type != null && type.length() > 0) {
                    if ( (type.equals("Extend")) || (type.equals("Include")) ) {
                        IConstructsRelationFactory pFactory = new ConstructsRelationFactory();
                        if (pFactory != null) {
                            if (type.equals("Include")) {
                                IUseCase p1 = (IUseCase)pParticipant1;
                                IUseCase p2 = (IUseCase)pParticipant2;
                                IInclude pLink = pFactory.createInclude(p1, p2);
                                if (pLink != null) {
                                    pPropEle.setElement(pLink);
                                }
                            } else if (type.equals("Extend")) {
                                IUseCase p1 = (IUseCase)pParticipant1;
                                IUseCase p2 = (IUseCase)pParticipant2;
                                IExtend pLink = pFactory.createExtend(p1, p2);
                                if (pLink != null) {
                                    pPropEle.setElement(pLink);
                                }
                            }
                        }
                    } else {
                        IRelationFactory pFactory = new RelationFactory();
                        if (pFactory != null) {
                            if (type.equals("Generalization")) {
                                IClassifier pClass1 = (IClassifier)pParticipant1;
                                IClassifier pClass2 = (IClassifier)pParticipant2;
                                IGeneralization pGen = pFactory.createGeneralization(pClass1, pClass2);
                                if (pGen != null) {
                                    pPropEle.setElement(pGen);
                                }
                            } else if ( (type.equals("Dependency")) || (type.equals("Realization")) ||
                                    (type.equals("Abstraction")) || (type.equals("Permission")) ||
                                    (type.equals("Usage"))
                                    ) {
                                INamedElement p1 = (INamedElement)pParticipant1;
                                INamedElement p2 = (INamedElement)pParticipant2;
                                INamespace pName = m_Details.getNamespace();
                                IDependency pLink = pFactory.createDependency2(p1, p2, type, pName);
                                if (pLink != null) {
                                    pPropEle.setElement(pLink);
                                }
                            } else if (type.equals("Implementation")) {
                                INamedElement p1 = (INamedElement)pParticipant1;
                                INamedElement p2 = (INamedElement)pParticipant2;
                                INamespace pName = m_Details.getNamespace();
                                IInterface pInter = (IInterface)p2;
                                if (pInter != null) {
                                    ETPairT < IInterface, IDependency > argumentPair = pFactory.createImplementation(p1, p2, pName);
                                    IDependency pLink = argumentPair.getParamTwo();
                                    if (pLink != null) {
                                        pPropEle.setElement(pLink);
                                    }
                                } else {
                                    // if the supplier isn't an interface, we are not going
                                    // to create this.
                                    // hr = S_FALSE;
                                }
                            } else if (type.equals("Association")) {
                                IClassifier p1 = (IClassifier)pParticipant1;
                                IClassifier p2 = (IClassifier)pParticipant2;
                                IAssociation pLink = pFactory.createAssociation(p1, p2, null);
                                if (pLink != null) {
                                    pPropEle.setElement(pLink);
                                }
                            } else if (type.equals("Aggregation")) {
                                IClassifier p1 = (IClassifier)pParticipant1;
                                IClassifier p2 = (IClassifier)pParticipant2;
                                IAssociation pLink = pFactory.createAssociation2(p1, p2, AssociationKindEnum.AK_AGGREGATION, false, false, null);
                                if (pLink != null) {
                                    pPropEle.setElement(pLink);
                                }
                            } else if (type.equals("AssociationClass")) {
                                IClassifier p1 = (IClassifier)pParticipant1;
                                IClassifier p2 = (IClassifier)pParticipant2;
                                IAssociation pLink = pFactory.createAssociation2(p1, p2, AssociationKindEnum.AK_ASSOCIATION_CLASS, false, false, null);
                                if (pLink != null) {
                                    pPropEle.setElement(pLink);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * Because of how the property element structure is set up, the nodes that represent relationships
     * and their ends do not contain the information that is needed for the pattern processing.  This method
     * determines what the end model element is for a particular piece of a relationship.
     *
     * See method for more details.
     *
     *
     * @param pPropEle[in]		The current property element
     * @param pElement[out]		The model element representing an end of a relationship
     *
     * @return HRESULT
     *
     */
    public IElement getElementInRelationship(IPropertyElement pPropEle) {
        IElement pElement = null;
        if (pPropEle != null) {
            // The property element structure looks like this:
            // <Specific> - these are sub elements of the generalization node
            // <General>
            // What we really want out of this routine is the model element that is the specific or general
            // node, but what is stored on the property element is the relationship model element (generalization
            // in this case)
            // get the model element on the passed in property element (this will usually be the
            // relationship model element)
            Object pDisp = pPropEle.getElement();
            if (pDisp != null) {
                // get the definition for the property element
                IPropertyDefinition pDef = pPropEle.getPropertyDefinition();
                if (pDef != null) {
                    // if the model element is an association end, then we really want the participant
                    // of the association end
                    if (pDisp instanceof IAssociationEnd) {
                        IAssociationEnd pEnd = (IAssociationEnd)pDisp;
                        IClassifier pClass = pEnd.getParticipant();
                        pElement = pClass;
                    } else {
                        // the property element houses the relationship model element, but what we really
                        // want is the model element that is the result of the node (the general class or
                        // specific class).  In order to get this, we must invoke the get method on the
                        // relationship model element
                        String getstr = pDef.getGetMethod();
                        if (getstr != null && getstr.length() > 0) {
                            Class clazz1 = pDisp.getClass();
                            Method meth = null;
                            Method[] meths = clazz1.getMethods();
                            if (meths != null) {
                                for (int i=0; i<meths.length; i++) {
                                    if (meths[i].getName().equals(getstr)) {
                                        meth = meths[i];
                                        break;
                                    }
                                }
                                Object retObj = null;
                                try {
                                    if (meth != null) {
                                        retObj = meth.invoke(pDisp);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (retObj != null) {
                                    // special processing if what comes back from the invoke is an association
                                    // end, then we really want the participant, not the end
                                    if (retObj instanceof IAssociationEnd) {
                                        IAssociationEnd pEnd = (IAssociationEnd)retObj;
                                        IClassifier pClass = pEnd.getParticipant();
                                        if (pClass != null) {
                                            pElement = pClass;
                                        }
                                    } else if (retObj instanceof IElement) {
                                        pElement = (IElement)retObj;
                                        
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return pElement;
    }
    
    /**
     * Because a lot of the property elements are specified as "on demand" for performance
     * reasons, when cloning the elements, we need these "on demand" nodes to be fully flushed
     * out so that all information is cloned properly.
     *
     *
     * @param pPropEle[in]		The current property element
     *
     * @return HRESULT
     */
    public void ensurePropertyElementIsBuilt(IPropertyElement pPropEle) {
        if (pPropEle != null) {
            // get its corresponding definition
            IPropertyDefinition pDef = pPropEle.getPropertyDefinition();
            if (pDef != null) {
                String pDefName = pDef.getName();
                // has the definition been marked as on demand
                boolean onDemand = pDef.isOnDemand();
                if (onDemand) {
                    // get the model element on the property element
                    Object pDisp = pPropEle.getElement();
                    if (pDisp != null) {
                        if (pDisp instanceof IElement) {
                            IElement pElement = (IElement)pDisp;
                            String id = pElement.getXMIID();
                            String eleType = pElement.getElementType();
                            if ( (pDefName.equals(eleType)) ||
                                    (eleType.equals("Aggregation")) ||
                                    (eleType.equals("AssociationClass")) ||
                                    (eleType.equals("NavigableEnd")) ||
                                    (eleType.equals("AssociationEnd")) ||
                                    (eleType.equals("PartEnd")) ||
                                    (eleType.equals("AggregateEnd")) ||
                                    (eleType.equals("Dependency")) ||
                                    (eleType.equals("Implementation")) ||
                                    (eleType.equals("Realization")) ||
                                    //(eleType.equals("PartFacade")) ||
                                    (eleType.equals("Permission")) ||
                                    (eleType.equals("Usage")) ||
                                    (eleType.equals("Abstraction"))
                                    ) {
                                boolean found = false;
                                int count = m_IdsAlreadyProcessing.size();
                                for (int x = 0; x < count; x++) {
                                    String cur = m_IdsAlreadyProcessing.get(x);
                                    if (cur.equals(id)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if ( (!found) || (eleType.equals("Parameter")) || (eleType.equals("MultiplicityRange")) ) {
                                    // get the property definition representing this model element
                                    IPropertyDefinition pDef2 = m_Factory.getPropertyDefinitionForElement("", pDisp);
                                    if (pDef2 != null) {
                                        m_Manager.reloadElement(pDisp, pDef2, pPropEle);
                                        pPropEle.setPropertyDefinition(pDef2);
                                    }
                                }
                            } else if ( (eleType.equals("PartFacade")) && (m_Promoting) ) {
                                boolean found = false;
                                int count = m_IdsAlreadyProcessing.size();
                                for (int x = 0; x < count; x++) {
                                    String cur = m_IdsAlreadyProcessing.get(x);
                                    if (cur.equals(id)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    // get the property definition representing this model element
                                    String exType = pElement.getExpandedElementType();
                                    String cstr = StringUtilities.replaceAllSubstrings(exType, "_", "");
                                    IPropertyDefinition pDef2 = m_Factory.getPropertyDefinitionByName(cstr);
                                    if (pDef2 != null) {
                                        m_Manager.reloadElement(pDisp, pDef2, pPropEle);
                                        pPropEle.setPropertyDefinition(pDef2);
                                    }
                                }
                            } else if ( (pDefName.equals("Exception")) || (pDefName.equals("ParameterableElement")) ||
                                    (pDefName.equals("UseCaseDetail2"))
                                    ) {
                                // get the property definition representing this model element
                                IPropertyDefinition pDef2 = m_Factory.getPropertyDefinitionByName(pDefName);
                                if (pDef2 != null) {
                                    m_Manager.reloadElement(pDisp, pDef2, pPropEle);
                                    pPropEle.setPropertyDefinition(pDef2);
                                }
                            }
                            m_IdsAlreadyProcessing.add(id);
                        }
                    }
                }
            }
        }
    }
    /**
     * Not used
     *
     *
     * @param pDetails[in]					The design pattern details
     * @param roleID[in]						The role to get its instances
     * @param pElement[in]					The particular element that we are looking for
     * @param pMatchingElement[out]		The found element in the instance map
     *
     * @return HRESULT
     *
     */
    public IElement getElementFromInstanceMap(IDesignPatternDetails pDetails, String roleID, IElement pElement) {
        IElement pMatchingElement = null;
        if (pDetails != null && pElement != null) {
            // get the instances for the passed in role
            ETList <IElement> pParticipants = pDetails.getParticipantInstances(roleID);
            if (pParticipants != null) {
                // loop through the instances
                int partCount = pParticipants.size();
                for (int x2 = 0; x2 < partCount; x2++) {
                    IElement pParticipant = pParticipants.get(x2);
                    if (pParticipant != null) {
                        // is it the same as what was passed in
                        boolean isSame = pParticipant.isSame(pElement);
                        if (isSame) {
                            pMatchingElement = pParticipant;
                            break;
                        }
                    }
                }
            }
        }
        return pMatchingElement;
    }
    /**
     * In order to properly display and eventually "instantiate" the pattern, we need
     * to know certain pieces of information.  The design pattern details houses
     * this information.
     *
     * @param pDispatch[in]			The dispatch representing the collaboration(pattern)
     * @param pDetails[out]			The newly created pattern details
     *
     * return HRESULT
     */
    public void buildPatternDetails( Object pDispatch, IDesignPatternDetails pDetails) {
        if (pDispatch != null && pDetails != null) {
            // make sure that we have a collaboration
            if (pDispatch instanceof ICollaboration) {
                ICollaboration pCollab = (ICollaboration)pDispatch;
                // store the collaboration on the details
                pDetails.setCollaboration(pCollab);
                // store the project
                boolean hr = buildPatternProject(pDetails);
                if (hr) {
                    // store the namespace
                    hr = buildPatternNamespace(pDetails);
                    if (hr) {
                        // store the information about the roles in the pattern
                        hr = buildPatternRoles(pDetails);
                        if (hr) {
                            // store the information about the instantiated participants
                            // for the pattern
                            hr = buildParticipants(pDetails);
                        }
                    }
                }
            }
        }
    }
    /**
     * Fill in the project for the pattern details
     *
     * @param pDetails[in]			The pattern details to fill in
     *
     * return HRESULT
     */
    public boolean buildPatternProject(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            // blank it out at first
            pDetails.setProject(null);
            if (m_ParticipantScope == DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_PROJECTTREE) {
                hr = buildProjectFromTree(pDetails);
            } else if (m_ParticipantScope == DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_PRESENTATION) {
                hr = buildProjectFromPresentation(pDetails);
            } else if (m_ParticipantScope == DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_COMPARTMENT) {
                hr = buildProjectFromCompartment(pDetails);
            } else {
            }
        }
        return hr;
    }
    /**
     * Fill in the project in the pattern details based on what is selected in the project tree
     * that this manager knows about.
     *
     * @param pDetails[in]			The pattern details to fill in
     *
     * return HRESULT
     */
    public boolean buildProjectFromTree(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            if (m_ProjectTree != null) {
                // if there is a project tree, this means that the user has clicked in the
                // project tree to process a pattern, so we can get the item that is selected
                // in the project tree to determine the project in which to instantiate
                // the pattern
                //
                // otherwise, we will make the user pick a project in the GUI, or they
                // can set it after the details are "built"
                IProjectTreeItem[] pTreeItems = m_ProjectTree.getSelected();
                if (pTreeItems != null) {
                    // loop through the selected items
                    int count = pTreeItems.length;
                    for (int x = 0; x < count; x++) {
                        IProjectTreeItem pTreeItem = pTreeItems[x];
                        if (pTreeItem != null) {
                            // get the model element from the tree item
                            IElement pElement = pTreeItem.getModelElement();
                            if (pElement != null) {
                                // ask the model element for its project
                                // we will default to the first project that we find
                                // if multiple items are selected from different projects
                                Object pDisp = pElement.getProject();
                                if (pDisp != null) {
                                    if (pDisp instanceof IProject) {
                                        IProject pProj = (IProject)pDisp;
                                        pDetails.setProject(pProj);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return hr;
    }
    /**
     * Fill in the project in the pattern details based on what is selected in the drawing area
     * that this manager knows about.
     *
     * @param pDetails[in]			The pattern details to fill in
     *
     * return HRESULT
     */
    public boolean buildProjectFromPresentation(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            IElement pElement = null;
            // since we are building the information based on something from the presentation
            // (the diagram), we need to get the current diagram and see what is selected
            // on it
            IProductDiagramManager pDiagramMgr = ProductHelper.getProductDiagramManager();
            if (pDiagramMgr != null) {
                // get the current diagram
                IDiagram pDiagram = pDiagramMgr.getCurrentDiagram();
                if (pDiagram != null) {
                    // get the items selected on the diagram
                    ETList <IPresentationElement> pSelected = pDiagram.getSelected();
                    if (pSelected != null) {
                        // loop through what is selected
                        int count = pSelected.size();
                        if (count > 0) {
                            IPresentationElement pSelected2 = pSelected.get(0);
                            if (pSelected2 != null) {
                                // get the element from the presentation
                                pElement = pSelected2.getFirstSubject();
                            }
                        }
                    }
                }
            }
            // now get the project from the element that is selected
            if (pElement != null) {
                // we only want to set the project on the details (this will limit the user
                // to only applying the pattern to the project) if the pattern that is selected
                // is in the same project as the diagram that is selected.
                Object pDisp = pElement.getProject();
                if (pDisp instanceof IProject) {
                    IProject pProj = (IProject)pDisp;
                    pDetails.setProject(pProj);
                }
            }
        }
        return hr;
    }
    /**
     * Fill in the project in the pattern details based on what is selected in the drawing area
     * that this manager knows about.
     *
     * @param pDetails[in]			The pattern details to fill in
     *
     * return HRESULT
     */
    public boolean buildProjectFromCompartment(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            // right now we are not being smart about the compartment
            hr = buildProjectFromPresentation(pDetails);
        }
        return hr;
    }
    /**
     * Fill in the namespace for the pattern details
     *
     * @param pDetails[in]			The pattern details to fill in
     *
     * return HRESULT
     */
    public boolean buildPatternNamespace(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            // blank it out at first
            pDetails.setNamespace(null);
            if (m_ProjectTree != null) {
                // if there is a project tree, this means that the user has clicked in the
                // project tree to process a pattern, so we can get the item that is selected
                // in the project tree to determine the namespace in which to instantiate
                // the pattern
                //
                // otherwise, we will make the user pick a namespace in the GUI, or they
                // can set it after the details are "built"
                IProjectTreeItem[] pTreeItems = m_ProjectTree.getSelected();
                if (pTreeItems != null) {
                    // loop through the selected items
                    int count = pTreeItems.length;
                    for (int x = 0; x < count; x++) {
                        IProjectTreeItem pTreeItem = pTreeItems[x];
                        if (pTreeItem != null) {
                            // get the model element from the tree item
                            IElement pElement = pTreeItem.getModelElement();
                            if (pElement != null) {
                                // if the selected element is a package, use it
                                // otherwise, ask the model element for its owner
                                // we will default to the first owner that is a namespace that we find
                                // if multiple items are selected from different namespaces
                                if (pElement instanceof IPackage) {
                                    IPackage pName = (IPackage)pElement;
                                    pDetails.setNamespace(pName);
                                    break;
                                } else {
                                    IElement pDisp = pElement.getOwner();
                                    if (pDisp != null) {
                                        if (pDisp instanceof IPackage) {
                                            IPackage pName = (IPackage)pDisp;
                                            pDetails.setNamespace(pName);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return hr;
    }
    /**
     * Fill in the pattern roles for the pattern details
     *
     * @param pDetails[in]			The pattern details to fill in
     *
     * return HRESULT
     */
    public boolean buildPatternRoles( IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            //
            // Get the template parameters of the collaboration because this is where the element ids
            // of the participants are
            //
            ICollaboration pCollab = pDetails.getCollaboration();
            if (pCollab != null) {
                ETList <IParameterableElement> pParameters = pCollab.getTemplateParameters();
                if (pParameters != null) {
                    // loop through the parameters
                    int count = pParameters.size();
                    for (int x = 0; x < count; x++) {
                        IParameterableElement pParam = pParameters.get(x);
                        if (pParam != null) {
                            // create a role representing this parameterable element
                            IDesignPatternRole pRole = buildPatternRole(pParam);
                            if (pRole != null) {
                                // add it to the details
                                pDetails.addRole(pRole);
                                //
                                // Keep a map of elements participating in the collaboration
                                // and the role that they are playing
                                //
                                String roleID = pRole.getID();
                                IElement pElement = pRole.getElement();
                                if (pElement != null) {
                                    String id = pElement.getXMIID();
                                    pDetails.addParticipantRole(id, roleID);
                                }
                            }
                        }
                    }
                }
            }
        }
        return hr;
    }
    /**
     * Create a DesignPatternRole object to house the information about a particular
     * role in a pattern
     *
     *
     * @param pNamed[in]
     * @param pRole[out]
     *
     * @return HRESULT
     *
     */
    public IDesignPatternRole buildPatternRole(INamedElement pNamed) {
        IDesignPatternRole pRole = null;
        if (pNamed != null) {
            // the element that represents the role must be a PartFacade
            if (pNamed instanceof IPartFacade) {
                IPartFacade pClass = (IPartFacade)pNamed;
                // create the role
                IDesignPatternRole pTempRole = new DesignPatternRole();
                if (pTempRole != null) {
                    // role id comes from the id of the part facade
                    String id = pNamed.getXMIID();
                    pTempRole.setID(id);
                    // role name is the named element's name
                    String name = pNamed.getName();
                    pTempRole.setName(name);
                    // role multiplicity comes from the multiplicity of the part facade
                    pTempRole.setMultiplicity(1);
                    IMultiplicity pMult = pClass.getMultiplicity();
                    if (pMult != null) {
                        String range = pMult.getRangeAsString(false);
                        if (range.indexOf('*') > -1) {
                            // if the multiplicity contains a '*', then
                            // there can be an unlimited number of instances in this role
                            pTempRole.setMultiplicity(9999);
                        } else {
                            ETList <IMultiplicityRange> pRanges = pMult.getRanges();
                            if (pRanges != null) {
                                int count = pRanges.getCount();
                                if (count > 0) {
                                    // get the last range to determine the upper bound
                                    IMultiplicityRange pRange = pRanges.get(count - 1);
                                    if (pRange != null) {
                                        // does it have an entry for the upper bound
                                        String upper = pRange.getUpper();
                                        if (upper != null && upper.length() > 0) {
                                            // use the upper bound value
                                            Integer i = new Integer(upper);
                                            pTempRole.setMultiplicity(i.intValue());
                                        } else {
                                            // no upper bound, so get the lower bound
                                            String lower = pRange.getLower();
                                            if (lower != null && lower.length() > 0) {
                                                // use the lower bound value
                                                Integer i = new Integer(lower);
                                                pTempRole.setMultiplicity(i.intValue());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // role type is stored on the parameterable element
                    String type = "";
                    if (pNamed instanceof IParameterableElement) {
                        IParameterableElement pParam = (IParameterableElement)pNamed;
                        type = pParam.getTypeConstraint();
                    }
                    pTempRole.setTypeID(type);
                    // the element on the role is the named element that was found
                    pTempRole.setElement(pNamed);
                    pRole = pTempRole;
                }
            }
        }
        return pRole;
    }
    /**
     * Fill in the pattern participants in the pattern details
     *
     * @param pDetails[in]			The pattern details to fill in
     *
     * return HRESULT
     */
    public boolean buildParticipants(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            // We have set a member variable on the addin that will tell us to default
            // participants for the pattern based on what is selected in the project tree
            //
            // Participants will be based on what is selected in the project tree, what is
            // selected on the drawing area, or if neither of these, we will default it
            // to the role names
            //
            if (m_ParticipantScope == DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_PROJECTTREE) {
                hr = buildParticipantsFromTree(pDetails);
            } else if (m_ParticipantScope == DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_PRESENTATION) {
                hr = buildParticipantsFromPresentation(pDetails);
            } else if (m_ParticipantScope == DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_COMPARTMENT) {
                hr = buildParticipantsFromCompartment(pDetails);
            } else {
            }
        }
        return hr;
    }
    /**
     * Fill in the pattern participants in the pattern details based on what is selected in the
     * project tree that this manager knows about
     *
     * @param pDetails[in]			The pattern details to fill in
     *
     * return HRESULT
     */
    public boolean buildParticipantsFromTree(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            if (m_ProjectTree != null) {
                // if there is a project tree, this means that the user has clicked in the
                // project tree to process a pattern, so we can get the item that is selected
                // in the project tree to determine the participants in which to instantiate
                // the pattern
                IProjectTreeItem[] pTreeItems = m_ProjectTree.getSelected();
                if (pTreeItems != null) {
                    // loop through the selected items
                    int count = pTreeItems.length;
                    for (int x = 0; x < count; x++) {
                        IProjectTreeItem pTreeItem = pTreeItems[x];
                        if (pTreeItem != null) {
                            // get the model element from the tree item
                            IElement pElement = pTreeItem.getModelElement();
                            if (pElement != null) {
                                determineParticipantName(pDetails, pElement);
                            }
                        }
                    }
                }
            }
        }
        return hr;
    }
    /**
     * Fill in the pattern participants in the pattern details based on what is selected in the
     * drawing area that this manager knows about
     *
     * @param pDetails[in]			The pattern details to fill in
     *
     * return HRESULT
     */
    public boolean buildParticipantsFromPresentation(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            // also check to see if there is anything selected on the diagram
            // that we care about
            // need the diagram manager to get the current diagram
            IProductDiagramManager pDiagramMgr = ProductHelper.getProductDiagramManager();
            if (pDiagramMgr != null) {
                // get the current diagram
                IDiagram pDiagram = pDiagramMgr.getCurrentDiagram();
                if (pDiagram != null) {
                    // get the items selected on the diagram
                    ETList <IPresentationElement> pSelected = pDiagram.getSelected();
                    if (pSelected != null) {
                        // loop through what is selected
                        int count = pSelected.size();
                        for (int x = 0; x < count; x++) {
                            IPresentationElement pSelected2 = pSelected.get(x);
                            if (pSelected2 != null) {
                                // get the element from the presentation
                                IElement pSubject = pSelected2.getFirstSubject();
                                if (pSubject != null) {
                                    determineParticipantName(pDetails, pSubject);
                                }
                            }
                        }
                    }
                }
            }
        }
        return hr;
    }
    /**
     * Fill in the pattern participants in the pattern details based on what is selected in the
     * drawing area
     *
     * @param pDetails[in]			The pattern details to fill in
     *
     * return HRESULT
     */
    public boolean buildParticipantsFromCompartment(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            // right now we are not being smart about the compartment
            buildParticipantsFromPresentation(pDetails);
                        /*  This was commented out in 6.1 c++ code as well
                        if (m_Compartment)
                        {
                                IElement > pElement;
                                m_Compartment.GetModelElement(&pElement));
                                if (pElement)
                                {
                                        String type;
                                        pElement.getElementType(&type));
                                        DetermineParticipantName(pDetails, pElement);
                         
                                        boolean bIsSelected;
                                        m_Compartment.getSelected(&bIsSelected));
                                        if (bIsSelected)
                                        {
                                                IDrawEngine > pDrawEng;
                                                m_Compartment.GetEngine(&pDrawEng));
                                                if (pDrawEng)
                                                {
                                                        ICompartments > pCompartments;
                                                        pDrawEng.GetSelectedCompartments(&pCompartments));
                                                        if (pCompartments)
                                                        {
                                                                int count;
                                                                pCompartments.getCount();
                                                                for (int x = 0; x < count; x++)
                                                                {
                                                                        ICompartment > pComp;
                                                                        pCompartments.Item(x, &pComp));
                                                                        if (pComp)
                                                                        {
                                                                                IElement > pElement2;
                                                                                pComp.GetModelElement(&pElement2));
                                                                                if (pElement2)
                                                                                {
                                                                                        String type2;
                                                                                        pElement2.getElementType(&type2));
                                                                                        if (type == type2)
                                                                                        {
                                                                                                DetermineParticipantName(pDetails, pElement2);
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                         */
        }
        return true;
    }
    /**
     * We are going to keep a map of participant names based on the role that they
     * will fill.  We are trying to make a match on the first role whose
     * participant matches the type of the element that we have passed in.
     *
     * If we find a role, we make sure that the element hasn't already filled
     * another role of the same type, and if not, add it to the map
     *
     * @param pDetails[in]			The pattern details to fill in
     * @param pElement[in]			The current element
     *
     * return HRESULT
     */
    public void determineParticipantName(IDesignPatternDetails pDetails, IElement pElement) {
        if (pDetails != null && pElement != null) {
            // get the element type
            String eleType = pElement.getElementType();
            boolean bIsClassifier = false;
            if (pElement instanceof IClassifier) {
                bIsClassifier = true;
            }
            boolean bIsCollab = false;
            if (pElement instanceof ICollaboration) {
                bIsCollab = true;
            }
            // get the pattern roles
            ETList <IDesignPatternRole> pRoles = pDetails.getRoles();
            if (pRoles != null) {
                // loop through the roles
                int rolecount = pRoles.size();
                for (int i = 0; i < rolecount; i++) {
                    IDesignPatternRole pRoleTemp = pRoles.get(i);
                    if (pRoleTemp != null) {
                        // get some role information
                        String roleID = pRoleTemp.getID();
                        String typeID = pRoleTemp.getTypeID();
                        if (typeID == null){
                            typeID = "";
                        }
                        // if the type id is blank, then the element just has to be a classifier
                        // but if it is not blank, then the element has to be of the same type,
                        // and cannot be a collaboration
                        if ( ( (typeID.length() > 0) && (eleType.equals(typeID)) ) ||
                                ( (typeID.length() == 0) && (bIsClassifier) && (!bIsCollab) )
                                ) {
                            // is the element named
                            if (pElement instanceof INamedElement) {
                                INamedElement pNamed = (INamedElement)pElement;
                                // get its name
                                String name = pNamed.getName();
                                if (name != null && name.length() > 0) {
                                    // find out if this element is already playing a role
                                    // we will not disallow it in the instantiation, just
                                    // in the default values
                                    ETList <String> pNames = pDetails.getParticipantNames(roleID);
                                    if (pNames != null) {
                                        int count = pNames.size();
                                        if (count == 0) {
                                            String id = pNamed.getXMIID();
                                            pDetails.addParticipantName(roleID, id);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * Common method to determine if the drag/drop should be allowed from the design center.
     *
     *
     * @param pContext[in]			The context information that was "dropped"
     * @param bAllow[out]			Whether or not we should allow the drag/drop
     * @param pCollab[out]			The collaboration that was dropped
     *
     * @return HRESULT
     */
    public boolean allowDragAndDrop(IDrawingAreaDropContext pContext) {
        boolean bAllow = false;
        if (pContext != null) {
            // get what is being dropped from the context
            ADTransferable.ADTransferData data = pContext.getDropData();
            
            // this string is a specially designed string that will be interpreted by CDragAndDropSupport
            // and then turned into the appropriate model elements
            IApplication pApplication = ProductHelper.getApplication();
                        /* TODO
                        CDragAndDropSupport dragSupport = null;         // Used to help interpret what's on the clipboard
                        IElements pMEs = null;               			// The elements represented by the strings on the clipboard
                        ETList <IPresentationElement> pPEs = null;   	// The PE's represented by the strings on the clipboard
                        ETList < String > diagramLocations = null;		// The diagrams on the clipboard
                        int num = dragSupport.getElementsOnClipboard(str, pApplication, "DRAGGEDITEMS", &pMEs, &pPEs, diagramLocations);
                        if( num > 0)
                        {
                                if (pMEs)
                                {
                                        // only going to deal with one item being dragged
                                        int count;
                                        pMEs.getCount();
                                        if (count == 1)
                                        {
                                                // get the model element of this item
                                                IElement > pElement;
                                                pMEs.Item(0, &pElement));
                                                if (pElement)
                                                {
                                                        // if the item being dragged is a collaboration
                                                        if (CComQIPtr < ICollaboration > pCollab2 = pElement)
                                                        {
                         *bAllow = true;
                                                                // since we are allowing the drag, we also want to put the part facade objects owned
                                                                // by this collaboration onto to the drop context
                                                                INamedElements > pOwned;
                                                                pCollab2.getOwnedElements(&pOwned));
                                                                if (pOwned)
                                                                {
                                                                        int ownCount;
                                                                        pOwned.getCount(&ownCount));
                                                                        for (int x = 0; x < ownCount; x++)
                                                                        {
                                                                                INamedElement > pNamed;
                                                                                pOwned.Item(x, &pNamed));
                                                                                if (pNamed)
                                                                                {
                                                                                        pContext.AddAdditionalDropElement(pNamed));
                                                                                }
                                                                        }
                                                                }
                                                                pCollab2.CopyTo(pCollab));
                                                        }
                                                }
                                        }
                                }
                        }
                         */
        }
        return bAllow;
    }
    public ICollaboration getDragAndDropCollab(IDrawingAreaDropContext pContext) {
        ICollaboration pCollab = null;
        if (pContext != null) {
            // get what is being dropped from the context
            ADTransferable.ADTransferData data = pContext.getDropData();
            // TODO
        }
        return pCollab;
    }
    /**
     * Helper method to determine if the passed in diagram is owned by the passed in project.  This will
     * help in determining whether to draw the collaboration being dropped or apply it.
     *
     *
     * @param pProject[in]				The current project
     * @param pParentDiagram[in]		The current diagram
     * @param pContext[in]				The drop context
     * @param bOwned[out]				Whether or not the passed in diagram is owned by
     *
     * @return HRESULT
     */
    public boolean diagramOwnedByAddInProject(IProject pProject, IDiagram pParentDiagram, IDrawingAreaDropContext pContext) {
        boolean bOwned = false;
        if (pContext != null) {
            IProject pTemp = pProject;
            // get what is being dropped from the context
            ADTransferable.ADTransferData data = pContext.getDropData();
            
            // this string is a specially designed string that will be interpreted by CDragAndDropSupport
            // and then turned into the appropriate model elements
            IApplication pApplication = ProductHelper.getApplication();
                        /* TODO
                  CDragAndDropSupport dragSupport;          // Used to help interpret what's on the clipboard
                  IElements > pMEs;               // The elements represented by the strings on the clipboard
                  IPresentationElements > pPEs;   // The PE's represented by the strings on the clipboard
                  std::vector < xstring > diagramLocations; // The diagrams on the clipboard
                        CString cstr = str;
                        int num = dragSupport.GetElementsOnClipboard(cstr, pApplication, xstring(_T("DRAGGEDITEMS")), &pMEs,&pPEs, diagramLocations);
                  if( num > 0)
                  {
                                // if there is not a current project, then get it from what is being dropped
                                if (pTemp == 0)
                                {
                                        if (pMEs)
                                        {
                                                // only going to deal with one item being dragged
                                                int count;
                                                pMEs.getCount();
                                                if (count == 1)
                                                {
                                                        // get the model element of this item
                                                        IElement > pElement;
                                                        pMEs.Item(0, &pElement));
                                                        if (pElement)
                                                        {
                                                                // if the item being dragged is a collaboration
                                                                if (CComQIPtr < ICollaboration > pCollab = pElement)
                                                                {
                                                                        // now get its project
                                                                        Object pDisp;
                                                                        pCollab.getProject(&pDisp));
                                                                        if (CComQIPtr < IProject > pProj = pDisp)
                                                                        {
                                                                                pTemp = pProj;
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                                // have the project
                                if (pTemp)
                                {
                                        IProxyDiagramManager > pManager;
                                        pManager.CoCreateInstance(__uuidof(ProxyDiagramManager)));
                                        if (pManager)
                                        {
                                                bool found = false;
                                                // get the diagrams in this directory
                                                IProxyDiagrams > pTempDiagrams;
                                                pManager.GetDiagramsInDirectory2(pTemp, &pTempDiagrams));
                                                if (pTempDiagrams)
                                                {
                                                        String parentDiagName;
                                                        pParentDiagram.getName(&parentDiagName));
                                                        // loop through the diagrams owned by this namespace
                                                        int dcount;
                                                        pTempDiagrams.getCount(&dcount));
                                                        for (int x = 0; x < dcount; x++)
                                                        {
                                                                IProxyDiagram > pDiagram;
                                                                pTempDiagrams.Item(x, &pDiagram));
                                                                if (pDiagram)
                                                                {
                                                                        // if this diagram is the same as the one being passed in
                                                                        String str;
                                                                        pDiagram.getName(&str);
                                                                        if (str == parentDiagName)
                                                                        {
                                                                                found = true;
                                                                                break;
                                                                        }
                                                                }
                                                        }
                                                }
                                                if (found)
                                                {
                         *bOwned = true;
                                                }
                                        }
                                }
                        }
                         */
        }
        return bOwned;
    }
    /**
     * Determines whether or not the passed in element fulfills all roles of the pattern.  This will
     * determine whether or not to display the gui associated with applying a pattern.
     *
     *
     * @param pElement[in]		The element
     * @param pCollab[in]		The pattern
     * @param bFulfill[out]		Whether or not the element fulfills the pattern
     *
     * @return HRESULT
     *
     */
    public boolean doesElementFulfillPattern(IElement pElement, ICollaboration pCollab) {
        boolean bFulfill = false;
        if (pElement != null && pCollab != null) {
            String type = pElement.getElementType();
            boolean bClassifier = false;
            if (pElement instanceof IClassifier) {
                bClassifier = true;
            }
            ETList <IParameterableElement> pParameters = pCollab.getTemplateParameters();
            if (pParameters != null) {
                // if there is only one role and the type of that role's participant
                // is the same as the passed in element's type
                // then it has been fulfilled
                int count = pParameters.size();
                if (count == 1) {
                    IParameterableElement pParam = pParameters.get(0);
                    if (pParam != null) {
                        String type2 = pParam.getTypeConstraint();
                        if ( (type.equals(type2)) || ( (type2.length() == 0) && (bClassifier) ) ) {
                            bFulfill = true;
                        }
                    }
                }
            }
        }
        return bFulfill;
    }
    /**
     * Determines whether or not the pattern details contains the information necessary to apply the pattern.
     * This will determine whether or not to display the gui associated with applying a pattern.
     *
     *
     * @param pDetails[in]		The pattern details
     * @param bFulfill[out]		Whether or not the the pattern is complete and can be applied
     *
     * @return HRESULT
     *
     */
    public boolean isPatternFulfilled(IDesignPatternDetails pDetails) {
        boolean bFulfill = false;
        if (pDetails != null) {
            ETList <IDesignPatternRole> pRoles = pDetails.getRoles();
            if (pRoles != null) {
                // loop through the roles
                int count = pRoles.size();
                if (count == 1) {
                    IDesignPatternRole pRole = pRoles.get(0);
                    if (pRole != null) {
                        String roleID = pRole.getID();
                        if (roleID != null && roleID.length() > 0) {
                            ETList <String> pNames = pDetails.getParticipantNames(roleID);
                            if (pNames != null) {
                                // loop through the instances
                                int count2 = pNames.size();
                                if (count2 > 0) {
                                    bFulfill = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return bFulfill;
    }
    /**
     * Promote the pattern
     *
     *
     * @param pDetails[in]		The design pattern details
     *
     * @return HRESULT
     *
     */
    public void promotePattern(IDesignPatternDetails pDetails) {
        if (pDetails != null) {
            //
            // Because the duplicate does not work for any xml item that is not under the
            // node that we are duplicating, we are having to improvise a little here.  We
            // could not use the same clone/save logic as with the apply because we are actually
            // creating relationships between the actual instances, not new instances and because
            // with cloning the template parameters on the new were not created as partfacade
            // objects.  So, we
            // are going to use both the duplicate and the clone logic to get us to be able
            // to promote a pattern to another project.
            //
            m_IDCount = 0;
            // clear out our temporary variables
            m_IdsAlreadyProcessing.clear();
            m_ToDoMapRel.clear();
            m_ToDoSet.clear();
            m_ToDoEle.clear();
            m_Promoting = true;
            // need to turn round trip off - like when applying pattern
            m_Details = pDetails;
            ICoreProduct pCoreProduct = ProductHelper.getCoreProduct();
            // 	Fix for #5058318
            boolean goFlag = true;
            if (pCoreProduct != null) {
                int oldMode = RTMode.RTM_OFF;
                IRoundTripController rtController = null;
                if (pCoreProduct != null) {
                    rtController = pCoreProduct.getRoundTripController();
                    if (rtController != null) {
                        // what was the mode before we set it off
                        oldMode = rtController.getMode();
                        rtController.setMode(RTMode.RTM_SHUT_DOWN);
                    }
                }
                // get the collaboration that we are promoting from the details
                ICollaboration pCollab = pDetails.getCollaboration();
                if (pCollab != null) {
                    // get the namespace that we are promoting it to from the details
                    INamespace pNamespace = pDetails.getNamespace();
                    if (pNamespace != null) {
                        //  only want to check the project that we are promoting from if we are
                        // removing it from the project
                        boolean hr = true;
                        boolean bRemove = pDetails.getRemoveOnPromote();
                        if (bRemove) {
                            hr = checkPromotingProject(pDetails);
                        }
                        if (hr) {
                            // now we want to check the project that we are promoting it to
                            hr = checkPromotedProject(pDetails);
                            if (hr) {
                                // before we actually do anything for the promote, we want to check
                                // to see if the pattern that they are promoting already exists in the
                                // project/namespace that they are promoting too
                                // if it does, we will warn them and then based on what they say
                                // make another one
                                boolean bExists = promotePatternExists(pDetails);
                                if (bExists) {
                                    // ask them if they want to promote a duplicate
                                    IQuestionDialog cpQuestionDialog = new SwingQuestionDialogImpl(m_Dialog);
                                    if( cpQuestionDialog != null ) {
                                        String title = DesignPatternUtilities.translateString("IDS_DUPLICATEPATTERN");
                                        String msg = DesignPatternUtilities.translateString("IDS_DUPLICATEPATTERN2");
                                        QuestionResponse nResult =
                                                cpQuestionDialog.displaySimpleQuestionDialog(
                                                MessageDialogKindEnum.SQDK_YESNO,
                                                MessageIconKindEnum.EDIK_ICONQUESTION, msg,
                                                SimpleQuestionDialogResultKind.SQDRK_RESULT_YES, null, title );
                                        if (nResult.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_NO) {
                                            goFlag = false;
                                        }
                                    }
                                }
                                // if they have told us to continue
                                if (goFlag) {
                                    DesignPatternUtilities.startWaitCursor(m_Dialog);
                                    //
                                    // create the new collaboration
                                    //
                                    Object pDisp = createElement("Collaboration");
                                    if (pDisp != null) {
                                        // get the name of the collaboration from the old one
                                        String oldName = pCollab.getName();
                                        // must be a NamedElement in order to add it as an owned element
                                        if (pDisp instanceof INamedElement) {
                                            INamedElement pNamed = (INamedElement)pDisp;
                                            // set the new one's name, and add it to the namespace
                                            pNamed.setName(oldName);
                                            pNamespace.addOwnedElement(pNamed);
                                            //
                                            // for each role on the old collaboration, we are going
                                            // to create a new role and make it the same type/name
                                            // as the old one
                                            //
                                            ETList <IParameterableElement > pParameters = pCollab.getTemplateParameters();
                                            if (pParameters != null) {
                                                // loop through the template parameters of the old
                                                int count = pParameters.size();
                                                for (int x = 0; x < count; x++) {
                                                    IParameterableElement pParam = pParameters.get(x);
                                                    if (pParam != null) {
                                                        // get the info from the old
                                                        String roleid = pParam.getXMIID();
                                                        String pname = pParam.getName();
                                                        String type = pParam.getTypeConstraint();
                                                        // create the new role
                                                        Object pDisp2 = createElement("PartFacade");
                                                        if (pDisp2 instanceof IParameterableElement) {
                                                            IParameterableElement pRole = (IParameterableElement)pDisp2;
                                                            if (pNamed instanceof IClassifier) {
                                                                IClassifier pFier = (IClassifier)pNamed;
                                                                if (pRole != null && pFier != null) {
                                                                    // set its information
                                                                    if (type != null && type.length() > 0) {
                                                                        pRole.setTypeConstraint(type);
                                                                    }
                                                                    pRole.setName(pname);
                                                                    pFier.addTemplateParameter(pRole);
                                                                    // we need to add this new role to the pattern details
                                                                    // as an instance of the old role so that the
                                                                    // relationship create logic will work properly
                                                                    pDetails.addParticipantInstance(roleid, pRole);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                // Now the new collaboration has been created so we will clone it
                                                IPropertyDefinition pDef = m_Factory.getPropertyDefinitionByName("Collaboration");
                                                if (pDef != null) {
                                                    // build a corresponding property element for old pattern
                                                    m_Manager.setModelElement(pCollab);
                                                    IPropertyElement pPropEleDP = m_Manager.buildTopPropertyElement(pDef);
                                                    if (pPropEleDP != null) {
                                                        // build a property element for the new pattern
                                                        IPropertyElement pPropEle = m_Manager.buildElement(pNamed, pDef, null);
                                                        if (pPropEle != null) {
                                                            clonePropertyElements(pPropEleDP, pPropEle);
                                                            saveParticipant(pPropEle);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    DesignPatternUtilities.endWaitCursor(m_Dialog);
                                }
                            }
                            // now remove the old pattern if told to
                            if (bRemove && goFlag) // Fix for #5058318
                            {
                                pCollab.delete();
                            }
                        } else {
                            String title = DesignPatternUtilities.translateString("IDS_UNABLETOPROMOTE");
                            String msg = DesignPatternUtilities.translateString("IDS_NOPROMOTE");
                            displayErrorDialog(title, msg);
                        }
                    }
                }
                // now put roundtrip back to the state it was before this process
                if (rtController != null) {
                    // what was the mode before we set it off
                    rtController.setMode(oldMode);
                }
                m_Promoting = false;
            }
        }
    }
    /**
     * Create a diagram for a pattern that has been
     * applied.
     *
     *
     * @param pDetails[in]			The pattern details to get the information from
     *
     * @return HRESULT
     *
     */
    public boolean createDiagramForPatternDetails(IDesignPatternDetails pDetails) {
        boolean hr = true;
        if (pDetails != null) {
            // did the user say to create one
            boolean bCreate = pDetails.getCreateDiagram();
            if (bCreate) {
                // get the namespace that the user picked
                INamespace pName = pDetails.getNamespace();
                if (pName == null) {
                    // if there isn't a namespace, use the project
                    IProject pProj = pDetails.getProject();
                    pName = pProj;
                }
                if (pName != null) {
                    // have the "namespace", so get the pattern
                    ICollaboration pCollab = pDetails.getCollaboration();
                    if (pCollab != null) {
                        // get the diagram name
                        String name = pDetails.getDiagramName();
                        // what elements should go on the diagram
                        ETList <IElement> pSelected = getElementsFromDetails(pDetails);
                        if (pSelected != null) {
                            // use this addin to help with the creation of the diagram
                            IDiagCreatorAddIn pAddin = new DiagCreatorAddIn();
                            
                            IDiagCreatorAddIn pCreator = (IDiagCreatorAddIn)pAddin;
                            if (pCreator != null) {
                                pCreator.createDiagramForElements(IDiagramKind.DK_CLASS_DIAGRAM,
                                        pName,
                                        name,
                                        pSelected,
                                        null);
                            }
                        }
                    }
                }
            }
        }
        return hr;
    }
    /**
     * Adds a hidden tagged value to the passed in element for future use of updating patterns
     * and ensuring consistency
     *
     *
     * @param pDetails[in]		The design pattern details (used to get the pattern)
     * @param pRole[in]			The particular role in question
     * @param pElement[in]		The element that is playing the role
     *
     * @return HRESULT
     */
    public void addTaggedValue(IDesignPatternDetails pDetails, IDesignPatternRole pRole, IElement pElement) {
        if (pDetails != null && pRole != null && pElement != null) {
            // the tagged value is going to be in the format
            // Name = "PatternApplied"
            // Value = "PatternProjectFile^PatternXMIID^RoleXMIID"
            String tvName = DesignPatternUtilities.translateString("IDS_TVNAME");
            ICollaboration pCollab = pDetails.getCollaboration();
            if (pCollab != null) {
                // need the project file name
                IProject pProject = pCollab.getProject();
                if (pProject != null) {
                    String filename = pProject.getFileName();
                    if (filename != null && filename.length() > 0) {
                        // now need the pattern's xmi id
                        String patID = pCollab.getXMIID();
                        if (patID.length() > 0) {
                            // now need the role's xmi id (part facade)
                            String roleID = pRole.getID();
                            if (roleID != null && roleID.length() > 0) {
                                String tvStr = filename;
                                tvStr += "^";
                                tvStr += patID;
                                tvStr += "^";
                                tvStr += roleID;
                                // now check to see if this tagged value already exists on the element
                                ETList <ITaggedValue > tvs = pElement.getTaggedValuesByName(tvName);
                                if (tvs != null) {
                                    boolean found = false;
                                    int count = tvs.size();
                                    for (int x = 0; x < count; x++) {
                                        ITaggedValue pTV = tvs.get(x);
                                        if (pTV != null) {
                                            String value = pTV.getDataValue();
                                            if (value.equals(tvStr)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!found) {
                                        // not found, so add it
                                        ITaggedValue pNew = pElement.addTaggedValue(tvName, tvStr);
                                        if (pNew != null) {
                                            // make it hidden
                                            pNew.setHidden(true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * Figures out the elements that are housed within the pattern details and puts them into
     * one array.  They are separated by role id within the details and we sometimes needed them
     * all at once.
     *
     *
     * @param pDetails[in]		The pattern details
     * @param pElements[out]	The elements found in the pattern details
     *
     * @return HRESULT
     *
     */
    public ETList<IElement> getElementsFromDetails(IDesignPatternDetails pDetails) {
        ETList < IElement > pOutElements = null;
        if (pDetails != null) {
            ETList <IElement> pTemp = new ETArrayList<IElement>();
            if (pTemp != null) {
                ETList <IDesignPatternRole> pRoles = pDetails.getRoles();
                if (pRoles != null) {
                    // loop through the roles
                    int count = pRoles.size();
                    for (int x = 0; x < count; x++) {
                        IDesignPatternRole pRole = pRoles.get(x);
                        if (pRole != null) {
                            String roleID = pRole.getID();
                            if (roleID != null && roleID.length() > 0) {
                                ETList <IElement> pElements = pDetails.getParticipantInstances(roleID);
                                if (pElements != null) {
                                    // loop through the instances
                                    int count2 = pElements.size();
                                    for (int y = 0; y < count2; y++) {
                                        IElement pElement = pElements.get(y);
                                        if (pElement != null) {
                                            pTemp.add(pElement);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                pOutElements = pTemp;
            }
        }
        return pOutElements;
    }
    /**
     * Helper routine to get the ends of the relationship stored on the passed in property element.
     *
     *
     * @param pPropEle[in]			The prop element to get the relationship ends for
     * @param pElement1[out]		The first element in the relationship
     * @param pElement2[out]		The second element in the relationship
     *
     * @return HRESULT
     *
     */
    public IElement getEnd1OfRelationship(IPropertyElement pPropEle) {
        IElement pElement = null;
        if (pPropEle != null) {
            pElement = getEndOfRelationship(pPropEle, 0);
        }
        return pElement;
    }
    public IElement getEnd2OfRelationship(IPropertyElement pPropEle) {
        IElement pElement = null;
        if (pPropEle != null) {
            pElement = getEndOfRelationship(pPropEle, 1);
        }
        return pElement;
    }
    public IElement getEndOfRelationship(IPropertyElement pPropEle, int relEnd) {
        IElement pElement = null;
        if (pPropEle != null) {
            // 78871, do not process the interim relationship element
            if (pPropEle.getValue() == null)
                return null;
            
            String name = pPropEle.getName();
            Object pDispDP = pPropEle.getElement();
            if (pDispDP instanceof IAggregation) {
                name = "Aggregation";
            }
            if (name.equals("Association")) {           
                if (pDispDP instanceof IAssociation) {
                    IAssociation pAssocDP = (IAssociation)pDispDP;
                    ETList <IAssociationEnd> pEndsDP = pAssocDP.getEnds();
                    if (pEndsDP != null) {
                        int countEndsDP = pEndsDP.size();
                        if (countEndsDP == 2) {
                            IAssociationEnd pEndDP = pEndsDP.get(relEnd);
                            if (pEndDP != null) {
                                pElement = pEndDP.getParticipant();
                            }
                        }
                    }
                }
            } else {
                String createName = name;
                if (createName.indexOf("Create") < 0) {
                    createName += "Create";
                }
                IPropertyDefinition pDefCreate = m_Factory.getPropertyDefinitionByName(createName);
                if (pDefCreate != null) {
                    Vector <IPropertyDefinition> pSubDefs = pDefCreate.getSubDefinitions();
                    if (pSubDefs != null) {
                        int count = pSubDefs.size();
                        if (count == 2) {
                            IPropertyDefinition pSub = pSubDefs.get(relEnd);
                            if (pSub != null) {
                                String subDefName = pSub.getName();
                                IPropertyElement pSubEle = pPropEle.getSubElement(subDefName, null);
                                if (pSubEle != null) {
                                    pElement = getElementInRelationship(pSubEle);
                                }
                            }
                        }
                    }
                }
            }
        }
        return pElement;
    }
    /**
     * Helper function to determine if the pattern details contains an instance that
     * matches the passed in role id.
     *
     *
     * @param id[in]				The xmi id of the element which is the role
     * @param pElement[in]		The element to compare
     * @param bSame[out]			Whether or not there is an instance in the pattern details
     *									that matches the passed in element
     *
     * @return HRESULT
     *
     */
    public boolean findMatchingInstance(String id, IElement pElement) {
        boolean bSame = false;
        if (pElement != null) {
            // get the roles that match the passed in id
            ETList <String> roleIDs = m_Details.getParticipantRoles(id);
            if (roleIDs != null) {
                // loop through the roles
                int count = roleIDs.getCount();
                for (int x = 0; x < count; x++) {
                    String roleID = roleIDs.item(x);
                    if (roleID != null && roleID.length() > 0) {
                        // now get the instances that have been assigned to this role
                        ETList <IElement> pParticipants = m_Details.getParticipantInstances(roleID);
                        if (pParticipants != null) {
                            // loop through the instances
                            int partCount = pParticipants.getCount();
                            for (int x2 = 0; x2 < partCount; x2++) {
                                IElement pParticipant = pParticipants.item(x2);
                                if (pParticipant != null) {
                                    // is this instance the same as the element that we are checking against
                                    boolean bSame2 = pElement.isSame(pParticipant);
                                    if (bSame2) {
                                        bSame = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return bSame;
    }
    /**
     * Helper function to find the corresponding prop element in the array of passed in prop elements
     * based on the property element's format string
     *
     *
     * @param pPropEle[in]			The prop element to find a match for
     * @param pPropEles[in]			The prop elements to search
     * @param pFound[out]			The matching property element
     *
     * @return HRESULT
     */
    public IPropertyElement findMatchingPropertyElementFormatString(IPropertyElement pPropEle, IPropertyElement pPropEles) {
        IPropertyElement pFound = null;
        if (pPropEle != null && pPropEles != null) {
            if (m_Formatter != null) {
                // get the model element from the prop element to match
                Object pDisp = pPropEle.getElement();
                if (pDisp != null) {
                    if (pDisp instanceof IElement) {
                        IElement pElement = (IElement)pDisp;
                        // get its format string (based on xml dom node)
                        Node pNode = pElement.getNode();
                        if (pNode != null) {
                            // calling formatNode so that it is language/mode independent
                            // this will only work if the data formatter has a predefined xsl script
                            // for the element type
                            // right now we are only using this on attrs/ops and they have an xsl script defined
                            String str = m_Formatter.formatNode(pNode);
                            //
                            // now loop through the prop elements that we are supposed to find the match to
                            //
                            Vector < IPropertyElement > pSubEles = pPropEles.getSubElements();
                            int count = pSubEles.size();
                            for (int x = 0; x < count; x++) {
                                IPropertyElement pSubEle = pSubEles.get(x);
                                if (pSubEle != null) {
                                    // need to get its format string also
                                    Object pDisp2 = pSubEle.getElement();
                                    if (pDisp2 != null) {
                                        if (pDisp2 instanceof IElement) {
                                            IElement pElement2 = (IElement)pDisp2;
                                            Node pNode2 = pElement2.getNode();
                                            if (pNode2 != null) {
                                                String str2 = m_Formatter.formatNode(pNode2);
                                                if (str != null && str.equals(str2)) {
                                                    pFound = pSubEle;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return pFound;
    }
    /**
     * Retrieves the patterns in the passed in project
     *
     * @param pProject[in]		The project to retrieve the patterns
     * @param pElements[out]	The elements representing patterns in the current GoF project
     *
     * @return HRESULT
     */
    public ETList<IElement> getPatternsInProject(IProject pProject) {
        ETList < IElement > pElements = null;
        if (pProject != null) {
            // create the ElementLocator in order to get the patterns in the GoF project
            IElementLocator pLocator = new ElementLocator();
            if (pLocator != null) {
                // get the patterns in our project
                String pattern = "//UML:Collaboration";
                pElements = pLocator.findElementsByDeepQuery(pProject, pattern);
            }
        }
        return pElements;
    }
    /**
     * Helper function to find the corresponding prop element in the array of passed in prop elements.
     * What we are looking for is a participant in an association end, which cannot be obtained in a
     * generic way, so we are special casing this one.
     *
     *
     * @param pPropEle[in]			The prop element to find a match for
     * @param pPropEles[in]			The prop elements to search
     * @param pFound[out]			The matching property element
     *
     * @return HRESULT
     */
    public IPropertyElement findMatchingPropertyElementParticipant(IPropertyElement pPropEle, IPropertyElement pPropEleOrig) {
        IPropertyElement pFound = null;
        if (pPropEle != null && pPropEleOrig != null) {
            IPropertyElement pKeySub = pPropEle.getSubElement("Participant", null);
            if (pKeySub != null) {
                IElement pElementM = getElementInRelationship(pKeySub);
                if (pElementM != null) {
                    String id1 = pElementM.getXMIID();
                    Vector <IPropertyElement> pSubs = pPropEleOrig.getSubElements();
                    if (pSubs != null) {
                        int subCount = pSubs.size();
                        for (int pos = 0; pos < subCount; pos++) {
                            IPropertyElement pSubEle = pSubs.get(pos);
                            if (pSubEle != null) {
                                String eleName = pSubEle.getName();
                                if ( (eleName.equals("Association End")) || (eleName.equals("AssociationEnd")) ) {
                                    ensurePropertyElementIsBuilt(pSubEle);
                                    IPropertyElement pKeySub2 = pSubEle.getSubElement("Participant", null);
                                    if (pKeySub2 != null) {
                                        IElement pElement = getElementInRelationship(pKeySub2);
                                        if (pElement != null) {
                                            boolean bSame = findMatchingInstance(id1, pElement);
                                            if (bSame) {
                                                pFound = pSubEle;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return pFound;
    }
    /**
     * Call to the event dispatcher to fire a pre apply event.
     *
     *
     * @return HRESULT
     */
    public void firePreApply() {
        if (m_Details != null && m_EventDispatcher != null) {
            IEventPayload payload = m_EventDispatcher.createPayload("PreApply");
            m_EventDispatcher.firePreApply(m_Details, payload );
        }
    }
    /**
     * Call to the event dispatcher to fire a post apply event.
     *
     *
     * @return HRESULT
     */
    public void firePostApply() {
        if (m_Details != null && m_EventDispatcher != null) {
            IEventPayload payload = m_EventDispatcher.createPayload("PostApply");
            m_EventDispatcher.firePostApply(m_Details, payload );
        }
    }
    /**
     * Since we turned off the events to perform the apply, we now need to refresh the
     * diagrams (presentation elements) that were affected by the apply
     *
     * @return HRESULT
     *
     */
    public void refreshDiagrams() {
        if (m_Details != null) {
            // how we tell the diagrams to refresh is through this broadcast action
            IElementBroadcastAction pAction = new ElementBroadcastAction();
            if (pAction != null) {
                // get the elements that were affected during the apply
                ETList <IElement> pElements = getElementsFromDetails(m_Details);
                if (pElements != null) {
                    // add them to the action
                    pAction.add(pElements);
                    pAction.setKind(DiagramAreaEnumerations.EBK_DEEP_SYNC_AND_RESIZE);
                }
                // have the action set up, so now send it to the open diagrams
                IProxyDiagramManager pDiagManager = ProxyDiagramManager.instance();
                if (pDiagManager != null) {
                    pDiagManager.broadcastToAllOpenDiagrams(pAction);
                }
            }
        }
    }
    /**
     * Since we turned off the events to perform the apply, we now need to ask the user
     * if they would like to perform code generation on the elements that were affected
     * by the apply.
     *
     *
     * @return HRESULT
     *
     */
    public void performCodeGeneration() {
        if (m_Details != null) {
            //
            // we are only going to do this if the project (not the element) is of the
            // right mode (Implementation) and the right language (not UML)
            //
            // if any of the cases are true, we will not do anything here
            //
            IProject pProject = m_Details.getProject();
            if (pProject != null) {
                String mode = pProject.getMode();
                String lang = pProject.getDefaultLanguage();
                if ( ((mode.equals("PSK_IMPLEMENTATION")) || (mode.equals("Implementation"))) &&
                        (!(lang.equals("UML")))
                        ) {
                    //
                    // check the preference to see what the user wants us to do
                    // ask them, do code generation, or do not do code generation
                    //
                    IPreferenceManager2 prefMgr = ProductHelper.getPreferenceManager();
                    if (prefMgr != null) {
                        int nResult = SimpleQuestionDialogResultKind.SQDRK_RESULT_NO;
                        String text = DesignPatternUtilities.translateString("IDS_CODEGENTEXT");
                        String title = DesignPatternUtilities.translateString("IDS_CODEGENTITLE");
                        IPreferenceQuestionDialog cpQuestionDialog = new SwingPreferenceQuestionDialog(m_Dialog);
                        if( cpQuestionDialog != null ) {
                            nResult = cpQuestionDialog.displayFromStrings("Default",
                                    "DesignCenter|DesignPatternCatalog",
                                    "CodeGenAfterApply",
                                    "PSK_ALWAYS",
                                    "PSK_NEVER",
                                    "PSK_ASK",
                                    text,
                                    SimpleQuestionDialogResultKind.SQDRK_RESULT_NO,
                                    title,
                                    SimpleQuestionDialogKind.SQDK_YESNO,
                                    MessageIconKindEnum.EDIK_ICONQUESTION,
                                    null);
                        }
                        //
                        // Now we know what the user wants us to do
                        //
                        if (nResult == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES) {
                            ICodeGenerator codeGen = new CodeGenerator();
                            if (codeGen != null) {
                                ETList <IElement> pElements = getElementsFromDetails(m_Details);
                                if (pElements != null) {
                                    codeGen.generateCode( lang, pElements );
                                }
                            }
                        } else {
                            // new in juml - if the preference is set to never code gen, then the
                            // user does not know when the apply is done
                            if (prefMgr != null) {
                                String value = prefMgr.getPreferenceValue("Default|DesignCenter|DesignPatternCatalog|CodeGenAfterApply");
                                if (value.equals("PSK_NEVER")) {
                                    boolean bDisplay = true;
                                    // one more check because this was messing up all of our automated tests
                                    UserSettings userSettings = new UserSettings();
                                    if (userSettings != null) {
                                        String str = userSettings.getSettingValue("DesignPattern", "DisplayCompleteMessage");
                                        if (str != null && str.equals("PSK_NO")) {
                                            bDisplay = false;
                                        }
                                    }
                                    if (bDisplay) {
                                        String msg = DesignPatternUtilities.translateString("IDS_PROCESSCOMPLETEMSG");
                                        String title2 = DesignPatternUtilities.translateString("IDS_PROCESSCOMPLETETITLE");
                                        IErrorDialog pTemp = new SwingErrorDialog(m_Dialog);
                                        if (pTemp != null) {
                                            pTemp.display(msg, MessageIconKindEnum.EDIK_ICONINFORMATION, title2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public boolean promotePatternExists(IDesignPatternDetails pDetails) {
        boolean bExists = false;
        if (pDetails != null) {
            ICollaboration pCollab = pDetails.getCollaboration();
            if (pCollab != null) {
                String name = pCollab.getName();
                INamespace pNamespace = pDetails.getNamespace();
                if (pNamespace == null) {
                    // use the project
                    IProject pProject = pDetails.getProject();
                    if (pProject != null) {
                        pNamespace = pProject;
                    }
                }
                // now have the right scope
                if (pNamespace != null) {
                    ETList <INamedElement> pElements = pNamespace.getOwnedElementsByName(name);
                    if (pElements != null) {
                        int count = pElements.getCount();
                        for (int x = 0; x < count; x++) {
                            INamedElement pEle = pElements.item(x);
                            if (pEle != null) {
                                if (pEle instanceof ICollaboration) {
                                    ICollaboration pCollab2 = (ICollaboration)pEle;
                                    ETList <IParameterableElement> pParameters = pCollab2.getTemplateParameters();
                                    if (pParameters != null) {
                                        // loop through the parameters
                                        int count2 = pParameters.getCount();
                                        if (count2 > 0) {
                                            bExists = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return bExists;
    }
    /**
     * Determines if the namespace that we are taking the pattern from can be modified.
     * Is it versioned and checked out or not versioned, but not read-only.
     *
     *
     * @param pDetails[in]		The current pattern details
     *
     * @return HRESULT
     *
     */
    public boolean checkPromotingProject(IDesignPatternDetails pDetails) {
        boolean hr = false;
        if (pDetails != null) {
            // get the namespace of the collaboration that is being promoted
            ICollaboration pCollab = pDetails.getCollaboration();
            if (pCollab != null) {
                INamespace pOwner = pCollab.getNamespace();
                if (pOwner != null) {
                    hr = checkNamespaceForAccess(pOwner);
                }
            }
        }
        return hr;
    }
    /**
     * Determines if the namespace that we are putting the pattern into can be modified.
     * Is it versioned and checked out or not versioned, but not read-only.
     *
     *
     * @param pDetails[in]		The current pattern details
     *
     * @return HRESULT
     *
     */
    public boolean checkPromotedProject(IDesignPatternDetails pDetails) {
        boolean hr = false;
        if (pDetails != null) {
            // get the namespace from the details, this will be the package or project within
            // the design center that we are promoting to
            INamespace pNamespace = pDetails.getNamespace();
            if (pNamespace != null) {
                hr = checkNamespaceForAccess(pNamespace);
            }
        }
        return hr;
    }
    /**
     * Determines if the passed in namespace can be modified.
     * Is it versioned and checked out or not versioned, but not read-only.
     *
     *
     * @param pDetails[in]		The namespace to check
     *
     * @return HRESULT
     *
     */
    public boolean checkNamespaceForAccess(INamespace pNamespace) {
        boolean hr = true;
        if (pNamespace != null) {
            // is the namespace a project, because that will have different messages
            // and a couple of different checks
            boolean isProject = false;
            if (pNamespace instanceof IProject) {
                isProject = true;
            }
            // has the namespace been versioned
            boolean bVersioned = pNamespace.isVersioned();
            if (bVersioned) {
                // yes it has, so see if the file is read-only
                String filename = pNamespace.getVersionedFileName();
                File file = new File(filename);
                if (!file.canWrite()) {
                    hr = false;
                    //hr = DPR_E_PROMOTE_READ_ONLY_NAMESPACE;
                    if (isProject) {
                        //hr = DPR_E_PROMOTE_READ_ONLY_PROJECT;
                    }
                }
            } else {
                // not been versioned, so if it is a project, see if it is readonly
                if (isProject) {
                    IProject pProject = (IProject)pNamespace;
                    String filename = pProject.getFileName();
                    File file = new File(filename);
                    if (file.exists()) {
                        if (!file.canWrite()) {
                            hr = false;
                            //hr = DPR_E_PROMOTE_READ_ONLY_PROJECT;
                        }
                    }
                }
            }
        }
        return hr;
    }
    /**
     * Helper method to display a dialog
     *
     *
     * @param title[in]	Title text of dialog
     * @param msg[in]		Text of dialog
     *
     * @return HRESULT
     *
     */
    public void displayErrorDialog(String title, String msg) {
        IErrorDialog pDialog = new SwingErrorDialog(m_Dialog);
        if( pDialog != null ) {
            pDialog.display(msg, MessageIconKindEnum.EDIK_ICONERROR, title);
        }
    }
    public IPickListManager getPicklistManagerForDetailsProject() {
        IPickListManager pManager = null;
        IProject pProject = m_Details.getProject();
        if (pProject != null) {
            ITypeManager cpTypeMgr = pProject.getTypeManager();
            if ( cpTypeMgr != null ) {
                pManager = cpTypeMgr.getPickListManager();
            }
        }
        return pManager;
    }
    public IElement getElementInDetailsProject(IElement pElement) {
        IElement pFound = null;
        if (pElement != null) {
            if (pElement instanceof INamedElement) {
                INamedElement pNamed = (INamedElement)pElement;
                String qName = pNamed.getQualifiedName2();
                String type = pNamed.getElementType();
                pFound = getElementInDetailsProject2(qName, type);
            }
        }
        return pFound;
    }
    public IElement getElementInDetailsProject2(String name, String type) {
        IElement pFound = null;
        IPickListManager pPickMgr = getPicklistManagerForDetailsProject();
        if (pPickMgr != null) {
            ETList <IElement> pFoundElements = pPickMgr.getElementsByNameAndStringFilter(name, type);
            if (pFoundElements != null) {
                int count = pFoundElements.getCount();
                if (count > 0) {
                    pFound = pFoundElements.item(0);
                }
            }
        }
        return pFound;
    }
    public IElement createElementInDetailsProject(IElement pElement) {
        IElement pNew = null;
        if (pElement != null) {
            if (pElement instanceof INamedElement) {
                INamedElement pNamed = (INamedElement)pElement;
                String qName = pNamed.getQualifiedName2();
                String type = pNamed.getElementType();
                pNew = createElementInDetailsProject2(qName, type);
            }
        }
        return pNew;
    }
    public IElement createElementInDetailsProject2(String name, String type) {
        IElement pNew = null;
        IProject pProject = m_Details.getProject();
        if (pProject != null) {
            INamedElement pNewNamed = NameResolver.resolveFullyQualifiedNameByType(pProject, name, type);
            if (pNewNamed != null) {
                String id = pNewNamed.getXMIID();
                m_AttrsOpsClonedMap.put(id, id);
                pNew = pNewNamed;
            }
        }
        return pNew;
    }
    
}
