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

package org.netbeans.modules.uml.core.reverseengineering.reintegration;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.ui.SaveNotifier;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.IQueryManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IAliasedType;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IDataType;
import org.netbeans.modules.uml.core.metamodel.core.foundation.CreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDerivationClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IRelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AssociationKindEnum;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Parameter;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParser;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventsSink;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IClassEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IPackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IParserData;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.PackageEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageParserSettings;
import org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.JavaChangeHandlerUtilities;
import org.netbeans.modules.uml.core.scm.ISCMElementItem;
import org.netbeans.modules.uml.core.scm.ISCMIntegrator;
import org.netbeans.modules.uml.core.scm.ISCMItemFactory;
import org.netbeans.modules.uml.core.scm.ISCMItemGroup;
import org.netbeans.modules.uml.core.scm.ISCMMaskKind;
import org.netbeans.modules.uml.core.scm.ISCMTool;
import org.netbeans.modules.uml.core.scm.SCMObjectCreator;
import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlmessagingcore.MsgCoreConstants;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.PathManip;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.ResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.controls.drawingarea.ElementBroadcastAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.IElementBroadcastAction;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogProjectDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogProjectDetails;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.support.ErrorDialogIconKind;
import org.netbeans.modules.uml.ui.support.ISCMEnums;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.util.ITaskSupervisor;
import org.netbeans.modules.uml.util.ITaskWorker;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author sumitabhk
 *
 */
public class UMLParsingIntegrator
        implements IUMLParsingIntegrator, IUMLParserEventsSink, ITaskWorker
        //, IProgressDialogListener
{
    public UMLParsingIntegrator()
    {
        finalConstruct();
    }
    
    protected void finalConstruct()
    {
        m_CancelDueToConflict = false;
        m_FragDocument = XMLManip.getDOMDocument();
        m_Locator = new ElementLocator();
        m_Factory = new RelationFactory();
        
        // Create the root "Package" node that will house all other nodes
        Element el = XMLManip.createElement(m_FragDocument, "UML:TempPackage", null);
        el.detach();
        m_FragDocument.setRootElement(el);
        
        cacheUnknownType();
        
        m_Cancelled = false;
        m_Project = null;
        m_BaseDirectory = null;
        
        m_ReplaceDocs = false;
        redef = new HashSet();
    }
    
    /**
     * Add a new package to the collection of packages that has been discovered.
     * @param pack The descovered package.
     * @param fullName The fully qualified name of the package.
     */
    public void addPackage(IPackage pack, final String fullName)
    {
        if (pack != null)
        {
            Node node = pack.getNode();
            if (node != null)
            {
                m_Packages.put(fullName, node);
            }
        }
    }
    
    /**
     * Determines whether or not typeName is a fully qualified type
     *
     * @param typeName[in]  The string to resolve
     *
     * @return <code>true</code> if "::" is found in typeName, indicating that a fully
     *         qualified type is present, else <code>false</code>.
     */
    
    protected boolean fullyQualified(final String typeName)
    {
        boolean retVal = false;
        
        if (typeName.length() > 0)
        {
            if (typeName.indexOf("::") >= 0)
            {
                retVal = true;
            }
        }
        
        return retVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.reintegration.IUMLParsingIntegrator#getFiles()
    */
    public IStrings getFiles()
    {
        IStrings pVal = null;
        try
        {
            if (m_Files != null)
            {
                pVal = m_Files;
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return pVal;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.reintegration.IUMLParsingIntegrator#setFiles(org.netbeans.modules.uml.core.support.umlsupport.IStrings)
    */
    public void setFiles(IStrings value)
    {
        m_Files = value;
    }
    
    
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.reintegration.IUMLParsingIntegrator#reverseEngineer(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, boolean, boolean, boolean, boolean)
    */
    public boolean reverseEngineer(
            INamespace pSpace,
            boolean useFileChooser,
            boolean useDiagramCreateWizard,
            boolean displayProgress,
            boolean extractClasses)
    {
        m_Cancelled = false;
        boolean wasCancelled = false;
        logEnabled = displayProgress;
        
        try
        {
            boolean ableToWrite = true;
            
            if (pSpace != null)
            {
                m_Namespace = pSpace;
                setProject(pSpace);
                ableToWrite = isAbleToWrite(pSpace);
            }
            
            if (ableToWrite == true)
            {
                establishLanguageManager();
                
                if (useFileChooser)
                    retrieveFilesFromUser();
                
                // We need to make sure that the Project itself is checked out.
                // This is 'cause RE is doing many things "under the wire", not
                // notifying other subsystems of changes, etc. So to be safe,
                // check out the namespace and the Project if not already.
                if ((m_Files) != null && (m_Namespace != null) &&
                        isAbleToWrite(m_Project) && isAbleToWrite(m_Namespace))
                {
                    ETList<String> scrubbedFiles = new ETArrayList();
                    verifyUniqueness(scrubbedFiles);
                    int numFiles = scrubbedFiles.size();
                    
                    if (numFiles > 0)
                    {
                        if (useDiagramCreateWizard)
                            m_Namespace = displayDiagramCreationWizard();
                        
                        // **************************************************
                        // Saving the project file before starting the RE
                        // process. If the user presses the cancel button
                        // I will be able to roll back to the pre-RE state.
                        // **************************************************
                        if (saveProject(m_Namespace))
                        {
                            m_Project.save(m_Project.getFileName(), true);
                            setProjectBaseDirectory();
                            IUMLParser parser = connectToParser();
                            
                            // Now plug all events ( this won't affect the dispatcher
                            // on the parser as that is using a different event
                            // controller ). Purposely passing in a null here
                            // to have the EventBlocker automatically get the
                            // controller off the current product
                            boolean orig = EventBlocker.startBlocking();
                            
                            try
                            {
                                int count = scrubbedFiles.size();
                                
                                supervisor.log(ITaskSupervisor.SUMMARY);
                                supervisor.log(ITaskSupervisor.SUMMARY,
                                        getLocalMsg("IDS_PARSING_ELEMENTS_SIZE", // NOI18N
                                        Integer.valueOf(count)));
                                
                                // start next subtask: element parsing
                                if (!supervisor.start(count))
                                    onCancelled();
                                
                                for (int x = 0; x < count && !m_Cancelled; x++)
                                {
                                    String groupMessage =
                                            REIntegrationMessages.getString(
                                            "IDS_PARSING", new Object[]  // NOI18N
                                    {String.valueOf(x + 1),
                                     String.valueOf(count)});
                                    
                                    supervisor.log(ITaskSupervisor.TERSE,
                                            INDENT + groupMessage);
                                    
                                    if (!supervisor.proceed(1))
                                        onCancelled();
                                    
                                    String fileName =
                                            (String)scrubbedFiles.get(x);
                                    
                                    supervisor.log(ITaskSupervisor.TERSE,
                                            INDENT + fileName);
                                    
                                    try
                                    {
                                        if (m_LanguageParserSettings == null)
                                            parser.processStreamFromFile(fileName);
                                        
                                        else
                                        {
                                            parser.processStreamFromFile(
                                                    fileName,
                                                    m_LanguageParserSettings);
                                        }
                                    }
                                    
                                    catch (Exception e)
                                    {
                                        sendExceptionMessage(e);
                                    }
                                    
                                    catch (OutOfMemoryError oome)
                                    {
                                        reportHeapExceeded();
                                        break;
                                    }
                                }
                                
                                if (!m_Cancelled)
                                {
                                    integrate(m_Namespace);
                                    
                                    // Build the .QueryCache if we have a new Project
                                    String msg = REIntegrationMessages.getString(
                                            "IDS_BUILDING_QUERYCACHE"); // NOI18N
                                    
                                    supervisor.log(ITaskSupervisor.SUMMARY);
                                    supervisor.log(ITaskSupervisor.SUMMARY, msg);
                                    
                                    establishQueryCache();
                                }
                                
                                if (m_Dispatcher != null)
                                {
                                    //m_Dispatcher.revokeUMLParserSink(m_Cookie);
                                    m_Dispatcher.revokeUMLParserSink(this);
                                }
                                
                                m_Dispatcher = null;
                                //m_Cookie = null;
                                m_Integrator = null;
                            }
                            
                            catch (OutOfMemoryError oome)
                            {
                                reportHeapExceeded() ;
                            }
                            
                            finally
                            {
                                EventBlocker.stopBlocking(orig);
                            }
                        }
                    }
                    // ************************************************************
                    // If the user pressed the cancel button roll back to the state
                    // before the RE was started.
                    // ************************************************************
                    if ((m_Cancelled) && (m_Project != null))
                    {
                        IWSProject pWSProject = m_Project.getWSProject();
                        
                        if (pWSProject != null)
                        {
                            pWSProject.close(false);
                            pWSProject.open();
                        }
                    }
                    
                    else if (m_Project != null && numFiles > 0)
                    {
                        // Be sure to let the Project know that it is dirty
                        m_Project.setDirty(true);
                        // but save it
                        m_Project.save(m_Project.getFileName(), true);
                    }
                    
                    clearSymbolTable();
                    refreshProjectTree();
                }
            }
            
            else
            {
                String projectName = m_Project.getName();
                displayReadOnlyMsg(projectName);
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        catch (OutOfMemoryError oome)
        {
            reportHeapExceeded() ;
        }
        
        return m_Cancelled ? true : false;
    }
    
    private boolean heapExceededMsgAlreadyShown = false ;
    private void reportHeapExceeded()
    {
        
        if (heapExceededMsgAlreadyShown)
        {
            try
            {
                org.openide.LifecycleManager.getDefault().exit();
                System.exit(1);
            }
            catch (SecurityException se)
            {
                // do nothing
            }
            
            return ;
        }
        
        heapExceededMsgAlreadyShown = true ;
        
        String msg = REIntegrationMessages.getString("HEAP_EXCEEDED_WARNING"); // NOI18N
        String msgTitle = REIntegrationMessages.getString("WARNING"); // NOI18N
        
        Object[] options =
        {org.openide.DialogDescriptor.CANCEL_OPTION };
        
        org.openide.DialogDescriptor dd =
                new org.openide.DialogDescriptor(
                msg,
                msgTitle,
                true,
                options,
                options[0],
                org.openide.DialogDescriptor.BOTTOM_ALIGN,
                org.openide.util.HelpCtx.DEFAULT_HELP,
                new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent ae)
            {
                //System.exit(0) ;
                m_Cancelled = true ;
            }
        });
        dd.setClosingOptions(null);
        org.openide.DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }
    
    private void waitForClose(JDialog dialog) throws InterruptedException
    {
        while (dialog.isVisible() && !SwingUtilities.isEventDispatchThread())
            Thread.sleep(50);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.reintegration.IUMLParsingIntegrator#canOperationBeREed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation)
    */
    public boolean canOperationBeREed(IOperation pOperation)
    {
        boolean bCanBeREed = false;
        try
        {
            ETList < ILanguage > cpLanguages = pOperation.getLanguages();
            if (cpLanguages != null)
            {
                int lCnt = cpLanguages.size();
                for (int lIndx = 0; lIndx < lCnt; lIndx++)
                {
                    ILanguage cpLanguage = (ILanguage) cpLanguages.get(lIndx);
                    if (cpLanguage != null)
                    {
                        // See if the language supports operation RE
                        boolean bIsSupported = cpLanguage.isFeatureSupported("Operation Reverse Engineering"); // NOI18N
                        if (bIsSupported)
                        {
                            // See if there are any source files for the operation for this language
                            ETList < IElement > cpElements = pOperation.getSourceFiles3(cpLanguage);
                            if (cpElements != null)
                            {
                                int lSourceFileCnt = cpElements.size();
                                if (lSourceFileCnt > 0)
                                {
                                    bCanBeREed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return bCanBeREed;
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.reintegration.IUMLParsingIntegrator#reverseEngineerOperations(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace)
    */
    public void reverseEngineerOperations(
            INamespace pSpace, ETList<IElement> pElements)
    {
        boolean orig = EventBlocker.startBlocking();
        try
        {
            setProject(pSpace);
            OperationHandler opHandler = new OperationHandler(pElements);
            opHandler.processOperations(this);
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        finally
        {
            EventBlocker.stopBlocking(orig);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.reintegration.IUMLParsingIntegrator#reverseEngineerOperation(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
    */
    public void reverseEngineerOperation(INamespace pSpace, IElement pElement)
    {
        try
        {
            setProject(pSpace);
            OperationHandler opHandler = new OperationHandler(pElement);
            opHandler.processOperations(this);
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.reintegration.IUMLParsingIntegrator#addClassToProject(org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass)
    */
    public void addClassToProject(IREClass pClass)
    {
        try
        {
            if (m_Project == null)
            {
                throw new Exception("Null Value"); // NOI18N
            }
            
            Document cpDocument = m_Project.getDocument();
            if (cpDocument != null)
            {
                String bsPackageName = pClass.getPackage();
                Node cpPackageNode = retrievePackage(bsPackageName, cpDocument, true);
                Node cpClassNode = processClass(pClass);
                if ((cpPackageNode != null) && (cpClassNode != null))
                {
                    Node cpOwnedElement = cpPackageNode.selectSingleNode("UML:Element.ownedElement"); // NOI18N
                    if (cpOwnedElement != null)
                    {
                        cpClassNode.detach();
                        ((Element) cpOwnedElement).add(cpClassNode);
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    void setProject(IElement newVal)
    {
        try
        {
            m_Project = newVal.getProject();
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
    }
    public void onPackageFound(IPackageEvent data, IResultCell cell)
    {
        try
        {
            Node dataNode = data.getEventData();
            Node node = scrubData(data, true);
            
            Node afterDataNode = data.getEventData();
            Node packageToPush = preparePackage(node);
            pushPackage(packageToPush);
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void onDependencyFound(IDependencyEvent data, IResultCell cell)
    {
        try
        {
            addDependency(scrubData(data, false));
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    int numOfClasses = 0;
    
    /**
     * The event method used by the UMLParser to notify listeners that a class
     * was found while parsing a file.
     *
     * @param data The class information.
     * @param cell The result.
     */
    public void onClassFound(IClassEvent data, IResultCell cell)
    {
        Node dataNode = null;
        boolean proceed = true;
        boolean isAliased;
        String name = "";
        
        Debug.out.println("Number Of Class Event: " + ++numOfClasses); // NOI18N
        try
        {
            dataNode = data.getEventData();
            
            if (dataNode != null)
            {
                //DEBUG_XML_NODE(dataNode, _T("C:\\OnClassFound_DEBUG.xml"), false );
                proceed = true;
                isAliased = checkAliasedType(dataNode);
                
                if (isAliased)
                {
                    name = XMLManip.getAttributeValue(dataNode, "name"); // NOI18N
                    
                    if (m_SymbolTable.get(name) != null)
                    {
                        proceed = true;
                    }
                }
                
                if (proceed)
                {
                    addClass(processClass(data));
                }
            }
            
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void onBeginParseFile(String fileName, IResultCell cell)
    {
        m_ContextStack.push(new ParsingContext(fileName));
        
        if (m_LanguageManager != null)
        {
            if (fileName.length() > 0)
            {
                
                ILanguage pLanguage =
                        m_LanguageManager.getLanguageForFile(fileName);
                
                if (pLanguage != null)
                    m_ContextStack.peek().setLanguage(pLanguage);
            }
            
        }
    }
    
    public void onEndParseFile(String fileName, IResultCell cell)
    {
        if (m_ContextStack.size() > 0)
        {
            try
            {
                processContext(m_ContextStack.pop());
            }
            catch (Exception e)
            {
                sendExceptionMessage(e);
            }
        }
    }
    
    
    public void onError(IErrorEvent error, IResultCell cell)
    {
        String formattedMessage = error.getFormattedMessage();
        supervisor.log(ITaskSupervisor.SUMMARY, formattedMessage);
    }
    
    public void establishQueryCache()
    {
        try
        {
            if (m_Project != null)
            {
                ICoreProduct pProd = ProductRetriever.retrieveProduct();
                if (pProd != null)
                {
                    IApplication app = pProd.getApplication();
                    if (app != null)
                    {
                        IQueryManager qMan = app.getQueryManager();
                        if (qMan != null)
                        {
                            qMan.establishCache(m_Project);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public Node findElement(Node prototypeNode, Node searchNode)
    {
        Node foundNode = null;
        try
        {
            // Attempt to get the value of the @name attribute. It QUITE possible that xml attribute
            // doesn't exist...
            String value = XMLManip.getAttributeValue(prototypeNode, "name"); // NOI18N
            if (value != null)
            {
                // We've got a name, so let's see if we get lucky...
                String query = ".//*[@name=\""; // NOI18N
                query += value;
                query += "\"]"; // NOI18N
                List nodes = searchNode.selectNodes(query);
                if (nodes != null)
                {
                    int num = nodes.size();
                    if (num == 1)
                    {
                        foundNode = (Node) nodes.get(0);
                    }
                    else if (num == 0)
                    {
                        // Let's check to see if we have the element in searchNode
                        String name = XMLManip.getAttributeValue(searchNode, "name"); // NOI18N
                        if (name != null)
                        {
                            if (name == value)
                            {
                                foundNode = searchNode;
                            }
                        }
                    }
                    else
                    {
                        ; // We've got an ambiguous match. Bailing for now...
                    }
                }
            }
            else
            {
                ; // This is not an element that is named. For now, we're bailing....
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return foundNode;
    }
    
    public void ensureXMLAttrValues(String query, Node childInDestinationNamespace, Node elmentBeingInjected, String attrName)
    {
        try
        {
            List nodes = childInDestinationNamespace.selectNodes(query);
            if (nodes != null)
            {
                int num = nodes.size();
                for (int x = 0; x < nodes.size(); x++)
                {
                    Node node = (Node) nodes.get(x);
                    if (node != null)
                    {
                        Node foundNode = findElement(node, elmentBeingInjected);
                        if (foundNode != null)
                        {
                            String value = XMLManip.getAttributeValue(node, attrName);
                            XMLManip.setAttributeValue(foundNode, attrName, value);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void ensureOwnedMetaElementsAreMoved(Node childInDestinationNamespace, Node elementBeingInjected, String elementName, boolean appendNodes)
    {
        try
        {
            // This query is looking for the element passed in, then the element two elements up. This will
            // allow us to find the owner of say a Constraint or TaggedValue
            String query = ".//"; // NOI18N
            query += elementName;
            query += "/ancestor::*[2]"; // NOI18N
            List nodes = childInDestinationNamespace.selectNodes(query);
            if (nodes != null)
            {
                int num = nodes.size();
                for (int x = 0; x < num; x++)
                {
                    Node node = (Node) nodes.get(x);
                    if (node != null)
                    {
                        Node foundNode = findElement(node, elementBeingInjected);
                        if (foundNode != null)
                        {
                            // Now get the owned element and move it to the foundNode
                            String elementQuery = "UML:Element.ownedElement/"; // NOI18N
                            elementQuery += elementName;
                            List elements = node.selectNodes(elementQuery);
                            if (elements != null)
                            {
                                int numElements = elements.size();
                                if (numElements > 0)
                                {
                                    Node parent = XMLManip.ensureNodeExists(
                                            foundNode,
                                            "UML:Element.ownedElement", // NOI18N
                                            "UML:Element.ownedElement"); // NOI18N
                                    
                                    if (parent != null)
                                    {
                                        if (appendNodes == false)
                                        {
                                            
                                            List nodes1 = foundNode.selectNodes(elementQuery);
                                            
                                            if (nodes != null)
                                            {
                                                for (int i = 0, count = nodes1.size(); i < count; ++i)
                                                {
                                                    Node n = (Node) nodes1.get(i);
                                                    n.detach();
                                                }
                                            }
                                            
                                        }
                                        
                                        for (int y = 0; y < numElements; y++)
                                        {
                                            Node element = (Node) elements.get(y);
                                            if (element != null)
                                            {
                                                XMLManip.appendNewLineElement(parent, null);
                                                XMLManip.insertNode((Element) parent, element, 0);
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
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void ensureXMLAttrValues(Node childInDestinationNamespace, Node elementBeingInjected, String attrName)
    {
        try
        {
            // Find all the owning elements on child... that have an xml attribute that matches attrName
            String query = ".//*[@";
            query += attrName;
            query += "]";
            
            ensureXMLAttrValues(query, childInDestinationNamespace, elementBeingInjected, attrName);
            
            // Make sure to check the current element as well
            
            Element element = (childInDestinationNamespace instanceof Element) ? (Element) childInDestinationNamespace : null;
            ;
            
            if (element != null)
            {
                Attribute attr = element.attribute(attrName);
                if (attr != null)
                {
                    XMLManip manip;
                    String value = attr.getValue();
                    XMLManip.setAttributeValue(elementBeingInjected, attrName, value);
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void handleMiscMetaData(Node childInDestinationNamespace, Node elementBeingInjected)
    {
        if ((childInDestinationNamespace != null) && (elementBeingInjected != null))
        {
            ensureXMLAttrValues(childInDestinationNamespace, elementBeingInjected, "appliedStereotype"); // NOI18N
            ensureXMLAttrValues(childInDestinationNamespace, elementBeingInjected, "isTransient"); // NOI18N
            ensureOwnedMetaElementsAreMoved(childInDestinationNamespace, elementBeingInjected, "UML:Constraint", true); // NOI18N
            //         if (m_ReplaceDocs == false)
            //         {
            //            ensureOwnedMetaElementsAreMoved(childInDestinationNamespace, elementBeingInjected, "UML:TaggedValue", false);
            //         }
        }
        
    }
    
    public IProject createNewProject(String languageName)
    {
        IProject proj = null;
        try
        {
            IProductProjectManager cpProjectManager = ProductHelper.getProductProjectManager();
            if (cpProjectManager != null)
            {
                INewDialogProjectDetails pDetails = new NewDialogProjectDetails();
                if (pDetails != null)
                {
                    pDetails.setAllowFromRESelection(false);
                    pDetails.setIsLanguageReadOnly(true);
                    pDetails.setPromptToCreateDiagram(false);
                    pDetails.setLanguage(languageName);
                    pDetails.setMode("Implementation"); // NOI18N
                    cpProjectManager.displayNewProjectDialog(pDetails);
                }
                else
                {
                    cpProjectManager.displayNewProjectDialog();
                }
                proj = cpProjectManager.getCurrentProject();
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return proj;
    }
    
    public boolean isNodeContainer(String nodeName)
    {
        boolean isContainer = false;
        if (nodeName != null)
        {
            if (("UML:Package".equals(nodeName)) || // NOI18N
                    ("UML:Project".equals(nodeName)) || // NOI18N
                    ("UML:Model".equals(nodeName)) ||   // NOI18N
                    ("UML:Subsystem".equals(nodeName))) // NOI18N
            {
                isContainer = true;
            }
        }
        return isContainer;
    }
    
    //   public void verifyInMemoryStatus()
    //   {
    ////   /**
    ////    *
    ////    * Makes sure that the passed in element has not been 'orphaned' from its original
    ////    * document. This can happen when a modify of the element causes this SCMIntegrator
    ////    * to unload it's outer document. ( this can happen when moving a versioned element
    ////    * from one package to another, where those packages themselves have been versioned ).
    ////    * Essentially, the check is made to ensure that the element still has a parent
    ////    * xml node. If not, it has been orphaned.
    ////    *
    ////    * @param element[in]      The element to check for validity
    ////    * @param elementProj[in]  The Project the element is ultimately owned by.
    ////    */
    ////   protected void verifyInMemoryStatus( IElement element, IProject elementProj )
    ////   {
    ////      if( null == element )
    ////      {
    ////         throw new IllegalArgumentException();
    ////      }
    ////
    ////      if( elementProj != null)
    ////      {
    ////         ITypeManager man = elementProj.getTypeManager();
    ////
    ////         if( man != null)
    ////         {
    ////            boolean modified = man.verifyInMemoryStatus( element);
    ////         }
    ////      }
    ////   }
    //
    //      for (int i = 0; i < count; ++i)
    //      {
    //         m_ItemsToSink.get(i)
    //      }
    //   }
    
    public void broadCastDiagramSync()
    {
        try
        {
            if (m_ItemsToSink.size() > 0)
            {
                IProxyDiagramManager proxyMan = ProxyDiagramManager.instance();
                if (proxyMan != null)
                {
                    //
                    TypedFactoryRetriever < IElement > fact = new TypedFactoryRetriever < IElement > ();
                    int count = m_ItemsToSink.size();
                    
                    // Create a broadcast to update the open diagrams
                    IElementBroadcastAction elementAction = new ElementBroadcastAction();
                    elementAction.setKind(DiagramAreaEnumerations.EBK_DEEP_SYNC);
                    
                    for (int i = 0; i < count; ++i)
                    {
                        IElement element = fact.createTypeAndFill(m_ItemsToSink.get(i));
                        // Add this model element to our broadcast
                        elementAction.add(element);
                        reinitializePresentationElement(element);
                    }
                    
                    proxyMan.broadcastToAllOpenDiagrams(elementAction);
                    m_ItemsToSink.clear();
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void reinitializePresentationElement(IElement element)
    {
        ETList<IPresentationElement> presentations = element.getPresentationElements();
        Iterator < IPresentationElement > iter = presentations.iterator();
        
        while(iter.hasNext() == true)
        {
            IPresentationElement curElement = iter.next();
            if (curElement instanceof org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation)
            {
                ((org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation)curElement).setModelElement(null);
            }
        }
    }
    
    public void postProcess()
    {
        try
        {
            if (m_Namespace != null)
            {
                Node node = m_Namespace.getNode();
                
                removeTokenDescriptors(node, true);
            }
            //         verifyInMemoryStatus();
            broadCastDiagramSync();
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void addToResync(Node node)
    {
        m_ItemsToSink.add(node);
    }
    
    public void handlePresentationElements(Node childInDestinationNamespace, Node elementBeingInjected)
    {
        if (childInDestinationNamespace != null && elementBeingInjected != null)
        {
            Node presNode = childInDestinationNamespace.selectSingleNode("./UML:Element.presentation"); // NOI18N
            if (presNode != null)
            {
                //            String xmiID = UMLXMLManip.getAttributeValue(presNode, "xmi.id");
                //
                //            FactoryRetriever fr = FactoryRetriever.instance();
                //            IVersionableElement inMemory = fr.retrieveObject(xmiID);
                //            if (inMemory instanceof org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation)
                //            {
                //               ((org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation)inMemory).setModelElement(null);
                //            }
                
                presNode.detach();
                // This moves the presentation elements from the destination ( which is being wholly replaced )
                // to the replacing element
                ((Element) elementBeingInjected).add(presNode);
                // Store this element for a broadcast later on
                addToResync(elementBeingInjected);
            }
        }
    }
    
    public void handlePartElements(
            Node childInDestinationNamespace, Node elementBeingInjected)
    {
        if ((childInDestinationNamespace != null) &&
                (elementBeingInjected != null))
        {
            List pPartNodes = childInDestinationNamespace.selectNodes(
                    "./UML:Element.ownedElement/UML:Part"); // NOI18N
            
            if (pPartNodes != null)
            {
                int size = pPartNodes.size();
                if (size > 0)
                {
                    Node pOwnedElement = XMLManip.ensureNodeExists(
                            elementBeingInjected,
                            "UML:Element.ownedElement", "" +  // NOI18N
                            "UML:Element.ownedElement"); // NOI18N
                    
                    if (pOwnedElement != null)
                    {
                        for (int index = 0; index < size; index++)
                        {
                            // This moves the presentation elements from the
                            // destination ( which is being wholly replaced )
                            // to the replacing element
                            Node pCurrentNode = (Node) pPartNodes.get(0);
                            
                            if (pCurrentNode != null)
                            {
                                XMLManip.insertNode(
                                        (Element) pOwnedElement, pCurrentNode, 0);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public boolean handleVersionedElement(
            Element docElement,
            Node childInDestinationNamespace,
            Node elementBeingInjected,
            String finalXMIID)
    {
        boolean handled = false;
        if ((childInDestinationNamespace != null) &&
                (elementBeingInjected != null) &&
                (docElement != null))
        {
            // Now, if the childInDestinationNamespace is versioned, there are a number of xml
            // attributes that need to be maintained in elementBeingInjected.
            String href = UMLXMLManip.getAttributeValue(childInDestinationNamespace, "href"); // NOI18N
            String xmiIDToChange = UMLXMLManip.getAttributeValue(elementBeingInjected, "xmi.id"); // NOI18N
            
            if (href != null)
            {
                // If we have an href value, then we want to replace all references to the old
                // xmiid with this.
                UMLXMLManip.replaceReferences(docElement, xmiIDToChange, href);
                
                // Now set the xmi id of elementBeingInjected with finalXMIID, as it was
                // certainly changed in ReplaceReferences().
                XMLManip.setAttributeValue(
                        elementBeingInjected, "xmi.id", finalXMIID); // NOI18N
                
                // Need to add all the version control specific xml attributes
                String isVersioned = XMLManip.getAttributeValue(
                        childInDestinationNamespace, "isVersioned"); // NOI18N
                
                if (isVersioned != null)
                    XMLManip.setAttributeValue(elementBeingInjected, "isVersioned", isVersioned); // NOI18N
                
                String isDirty = XMLManip.getAttributeValue(childInDestinationNamespace, "isDirty"); // NOI18N
                
                if (isDirty != null)
                {
                    // Element will now be dirty now matter what.
                    XMLManip.setAttributeValue(elementBeingInjected, "isDirty", "true"); // NOI18N
                }
                
                XMLManip.setAttributeValue(elementBeingInjected, "href", href); // NOI18N
                String scmID = XMLManip.getAttributeValue(childInDestinationNamespace, "scmID"); // NOI18N
                
                if (scmID != null)
                    XMLManip.setAttributeValue(elementBeingInjected, "scmmID", scmID); // NOI18N
                
                handled = true;
            }
            
            else
                UMLXMLManip.replaceReferences(docElement, xmiIDToChange, finalXMIID);
        }
        return handled;
    }
    
    
    /**
     * implements simplistic "merging" for nested classes and interfaces.
     * "The sameness" is decided upon whether the full-qualified name the same or not.
     * It is a merge only in the sense that PEs from diagrams would still continue
     * to be valid in the sense that their refferrences to the MEs will continue to be valid.
     */
    public void handleNested(Node childInDestinationNamespace, Node elementBeingInjected)
    {
        
        if ((childInDestinationNamespace != null) && (elementBeingInjected != null))
        {
            Node destinationOwnedElement = childInDestinationNamespace.selectSingleNode("UML:Element.ownedElement"); // NOI18N
            Node injectOwnedElement = elementBeingInjected.selectSingleNode("UML:Element.ownedElement"); // NOI18N
            
            if ((destinationOwnedElement != null) && (injectOwnedElement != null))
            {
                String types[] = new String[]{"UML:Class", "UML:Interface"}; // NOI18N
                for(int j = 0; j < types.length; j ++)
                {
                    String nodeType = types[j];
                    List destList = destinationOwnedElement.selectNodes(nodeType);
                    if (destList != null)
                    {
                        for(int i = 0; i < destList.size(); i++)
                        {
                            Node destinationNestedClass = (Node)destList.get(i);
                            if (destinationNestedClass == null) continue;
                            
                            TypedFactoryRetriever < IElement > fact = new TypedFactoryRetriever < IElement > ();
                            IElement nodeInNamespace = fact.createTypeAndFill(destinationNestedClass);
                            removeClientDependencies(nodeInNamespace);
                            removeNonNavigableAssoc(nodeInNamespace);
                            removeGeneralizations(nodeInNamespace);
                            
                            String destName = UMLXMLManip.getAttributeValue(destinationNestedClass, "name"); // NOI18N
                            Node injectNestedClass = injectOwnedElement
                                    .selectSingleNode(nodeType+"[@name='"+destName+"']"); // NOI18N
                            
                            if (injectNestedClass != null)
                            {
                                String finalXMIID = null;
                                finalXMIID = replaceReferences(destinationNestedClass, injectNestedClass, finalXMIID);
                                
                                handleNested(destinationNestedClass, injectNestedClass);
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    public void verifyUniqueness(ETList<String> files)
    {
        if (m_Files != null)
        {
            for (int x = 0; x < m_Files.size(); x++)
            {
                String fileName = (String) m_Files.get(x);
                
                if (fileName != null)
                    files.add(fileName);
            }
        }
    }
    
    protected String replaceReferences(
            Node childInDestinationNamespace,
            Node elementBeingInjected,
            String finalXMIID)
    {
        boolean resolved = false;
        if ((childInDestinationNamespace != null) && (elementBeingInjected != null))
        {
            // Need the document element of all the elements that were just
            // re'd. This is so that we can be sure that any ids that we change are
            // fully resolved.
            
            Element docElement = m_FragDocument.getRootElement();
            if (docElement != null)
            {
                // Retrieve the xmi.id value of the element to be replaced. We will
                // replace the xmi.id of the elementBeingInjected with this value.
                String xmiIDToChange;
                finalXMIID = UMLXMLManip.getAttributeValue(
                        childInDestinationNamespace, "xmi.id"); // NOI18N
                
                // Now, if the childInDestinationNamespace is versioned, there are a number of xml
                // attributes that need to be maintained in elementBeingInjected.
                // VERSON TODO : Need to add in after version control is added
                handleVersionedElement(
                        docElement, childInDestinationNamespace,
                        elementBeingInjected, finalXMIID);
                
                // If the element has presentation elements associated with it, we need to inject those
                // into the elementBeingInjected
                handlePresentationElements(
                        childInDestinationNamespace, elementBeingInjected);
                
                handlePartElements(
                        childInDestinationNamespace, elementBeingInjected);
                
                handleMiscMetaData(
                        childInDestinationNamespace, elementBeingInjected);
                
                // Now make sure that if we have a type on our m_Types list by this name, we change
                // its ID as well
                String typeName = UMLXMLManip.getAttributeValue(
                        elementBeingInjected, "name"); // NOI18N
                
                //m_Types.put(typeName, finalXMIID);
                UnresolvedType type = m_Types.get(typeName);
                
                if (type != null)
                {
                    // Can only assume this is OK if there is only one type by typeName
                    if (type.getNumberOfTypes() == 1)
                        type.setXMIID(finalXMIID);
                    
                    // The PerformPreAttributeTypeResolution will cycle through the m_Types and
                    // set the XMI ID according to the m_SymbolTable symbols (if the symbols was
                    // found).  So, PerformPreAttributeTypeResolution will basically override
                    // the m_Types setting that we just made.  So, also update the symbol
                    // table.
                    //                ETPairT<Node,String> entry = m_SymbolTable.get(typeName);
                    //                if (entry != null)
                    //                    entry.setParamTwo(finalXMIID);
                    
                    // QUESTION: Why set all of the entries to the same ID.
                    ArrayList < ETPairT < Node, String > > entries = m_SymbolTable.get(typeName);
                    if (entries != null)
                    {
                        for (Iterator<ETPairT<Node, String>> iter =
                                entries.iterator(); iter.hasNext();)
                        {
                            ETPairT<Node, String> entry = iter.next();
                            entry.setParamTwo(finalXMIID);
                        }
                        //entry.setParamTwo(finalXMIID);
                    }
                    
                    resolved = true;
                    m_ReplacedNodes.put(finalXMIID, childInDestinationNamespace);
                }
            }
        }
        return finalXMIID;
    }
    
    public void injectElementsIntoNamespace(
            Node destinationParent, Node parentOfNodesToInject)
    {
        supervisor.log(ITaskSupervisor.TERSE, INDENT
                + " injecting elements into namespace "  // NOI18N
                + destinationParent.getName());
        
        try
        {
            List children = ((Element) parentOfNodesToInject).elements();
            if (children != null)
            {
                int num = children.size();
                if (num > 0)
                {
                    String parentXMIID = null;
                    if (m_Namespace != null)
                        parentXMIID = m_Namespace.getXMIID();
                    
                    HashMap<String, ETList<Node>> destinationParentNodes =
                            new HashMap<String, ETList<Node>>();
                    
                    buildNodesFromParent(destinationParent, destinationParentNodes);
                    
                    int x = 0;
                    while ((x < num) && !m_CancelDueToConflict)
                    {
                        String groupMessage = REIntegrationMessages.getString(
                                "IDS_INJECTING_ELEMENTS", new Object[] // NOI18N
                        {new Integer(x + 1), new Integer(num)});
                        
                        supervisor.log(ITaskSupervisor.VERBOSE,
                                INDENT + INDENT + groupMessage);
                        
                        // Note: C++ code does children->get_item( 0, &child ),
                        //       which'll pick up only one class per file.
                        Node child = (Node) children.get(x);
                        
                        if (child instanceof Element)
                        {
                            if (oKToInject(destinationParent,
                                    destinationParentNodes, child))
                            {
                                // If the object that is being injected already has an owner (It is
                                // already scoped by a package or outer class) then we do NOT want
                                // to set the owner to the project. We want the owner to be the
                                // namespace that is already specified.
                                //
                                String curOwner = XMLManip.getAttributeValue(child, "owner"); // NOI18N
                                if (curOwner == null)
                                    XMLManip.setAttributeValue(child, "owner", parentXMIID); // NOI18N
                                
                                XMLManip.insertNode((Element) destinationParent, child, 0);
                                
                                // Be sure to add the name and node to the parent map of named elements
                                String childName = XMLManip.getAttributeValue(child, "name"); // NOI18N
                                if (childName != null)
                                {
                                    ETList < Node > nodes = destinationParentNodes.get(childName);
                                    if (nodes == null)
                                    {
                                        nodes = new ETArrayList < Node > ();
                                        destinationParentNodes.put(childName, nodes);
                                    }
                                    nodes.add(child);
                                }
                            }
                            
                            else
                            {
                                removeFromSymbolTable(child);
                                Node pParentNode = child.getParent();
                                if (pParentNode != null)
                                {
                                    child.detach();
                                }
                                
                            }
                        }
                        x++;
                    }
                }
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public boolean replaceElement(Node childInDestinationNamespace, Node elementBeingInjected)
    {
        boolean replaced = false;
        if ((childInDestinationNamespace != null) && (elementBeingInjected != null))
        {
            // Make sure the element types are the same. If they are not the same,
            // then there is nothing to do
            Element destination = (Element) childInDestinationNamespace;
            String destNodeName = destination.getQualifiedName();
            Element inject = (Element) elementBeingInjected;
            String injectNodeName = inject.getQualifiedName();
            if ((destNodeName != null) && (destNodeName.equals(injectNodeName)))
            {
                String finalXMIID = null;
                TypedFactoryRetriever < IElement > fact = new TypedFactoryRetriever < IElement > ();
                IElement nodeInNamespace = fact.createTypeAndFill(childInDestinationNamespace);
                removeClientDependencies(nodeInNamespace);
                removeNonNavigableAssoc(nodeInNamespace);
                removeGeneralizations(nodeInNamespace);
                finalXMIID = replaceReferences(childInDestinationNamespace, elementBeingInjected, finalXMIID);
                
                // Now replace the destination element with the element to inject. HOwever,
                // if the destination element is a package, do nothing except resolve the ids,
                // which we just did.
                
                if (!isNodeContainer(destNodeName))
                {
                    // IZ 87008. While the node isn't of container type, yet it still may contain
                    // nested classes and interfaces.
                    handleNested(childInDestinationNamespace, elementBeingInjected);
                    
                    Node parent = childInDestinationNamespace.getParent();
                    if (parent != null)
                    {
                        // was: parent.replaceChild( elementBeingInjected, childInDestinationNamespace, 0);
                        XMLManip.insertNode((Element) parent, elementBeingInjected, childInDestinationNamespace);
                        childInDestinationNamespace.detach();
                        
                        // Now make sure that if the element is in memory, set the new node on it
                        FactoryRetriever fr = FactoryRetriever.instance();
                        if (fr != null)
                        {
                            IVersionableElement inMemory = fr.retrieveObject(finalXMIID);
                            if (inMemory != null)
                            {
                                inMemory.setNode(elementBeingInjected);
                            }
                        }
                        replaced = true;
                    }
                }
                else
                {
                    // We have a containing element ( Package, Project, etc. ), so lets get the
                    // UML:Element.ownedElement element and inject to there
                    Node destinationOwnedElement = childInDestinationNamespace.selectSingleNode("UML:Element.ownedElement"); // NOI18N
                    Node injectOwnedElement = elementBeingInjected.selectSingleNode("UML:Element.ownedElement"); // NOI18N
                    //ATLASSERT( destinationOwnedElement && injectOwnedElement );
                    if ((destinationOwnedElement != null) && (injectOwnedElement != null))
                    {
                        injectElementsIntoNamespace(destinationOwnedElement, injectOwnedElement);
                    }
                    // Now update the m_Packages collection.  I will need the fully scoped name of the
                    // container.
                    String fullName = getFullName(elementBeingInjected);
                    if (fullName != null)
                    {
                        m_Packages.put(fullName, childInDestinationNamespace);
                    }
                }
            }
        }
        return replaced;
    }
    
    public boolean displayConflictDialog(String eleName)
    {
        boolean overwrite = false;
        
        switch (promptStatus)
        {
        case PROMPT_NO_TO_ALL:
            return false;
            
        case PROMPT_YES_TO_ALL:
            return true;
            
        default:
            overwrite = userAuthorizedOverwrite(eleName);
        }
        
        if (dmgr.getResult() == DialogDescriptor.CANCEL_OPTION ||
                dmgr.getResult() == DialogDescriptor.CLOSED_OPTION)
        {
            onCancelled();
            m_CancelDueToConflict = true;
        }
        
        return overwrite;
    }
    
    private DialogManager dmgr = null;
    
    private boolean userAuthorizedOverwrite(String eleName)
    {
        if (dmgr == null)
            dmgr = new DialogManager();
        
        dmgr.prompt(eleName);
        
        return dmgr.getResult() == DialogDescriptor.YES_OPTION
                ? true : false;
    }
    
    private class DialogManager implements ActionListener
    {
        private DialogDescriptor dialogDesc = null;
        private Dialog dialog = null;
        private Object result = DialogDescriptor.NO_OPTION;
        private ConflictMessage msgPanel = new ConflictMessage();
        
        private JButton yesToAllBtn = new JButton(NbBundle.getMessage(
                UMLParsingIntegrator.class, "LBL_YesToAllButton")); // NOI18N
        
        private JButton noToAllBtn = new JButton(NbBundle.getMessage(
                UMLParsingIntegrator.class, "LBL_NoToAllButton")); // NOI18N
        
        private JButton noBtn = new JButton(NbBundle.getMessage(
                UMLParsingIntegrator.class, "LBL_NoButton")); // NOI18N
        
        
        private final Object[] buttonOptions =
        {
            DialogDescriptor.YES_OPTION,
            noBtn,
            yesToAllBtn,
            noToAllBtn,
            DialogDescriptor.CANCEL_OPTION,
        };
        
        private final Object[] closeOptions =
        {
            DialogDescriptor.YES_OPTION,
            DialogDescriptor.NO_OPTION,
            DialogDescriptor.YES_OPTION,
            DialogDescriptor.NO_OPTION,
            DialogDescriptor.CANCEL_OPTION,
        };
        
        
        private void setDialogMessage(String eleName)
        {
            msgPanel.getAccessibleContext().setAccessibleName(
                    NbBundle.getMessage(
                    UMLParsingIntegrator.class, "ACSN_ConflictDialog")); // NOI18N
            msgPanel.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(
                    UMLParsingIntegrator.class, "ACSD_ConflictDialog")); // NOI18N
            
            msgPanel.setElementName(eleName);
            dialogDesc.setMessage(msgPanel);
        }
        
        public DialogManager()
        {
            dialogDesc = new DialogDescriptor(
                    "", // NOI18N
                    NbBundle.getMessage(UMLParsingIntegrator.class,
                    "LBL_ElementOverwriteConfirmationDialog_Title"), // NOI18N
                    true, // modal?
                    buttonOptions,
                    DialogDescriptor.YES_OPTION,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null, // help context
                    this, // button action listener
                    false); // leaf?
            
            initAccessibility();
            
            dialogDesc.setClosingOptions(closeOptions);
            dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        }
        
        public void prompt(String eleName)
        {
            setDialogMessage(eleName);
            dialog.setVisible(true);
        }
        
        public void actionPerformed(ActionEvent actionEvent)
        {
            if (actionEvent.getActionCommand().equalsIgnoreCase(NbBundle
                    .getMessage(UMLParsingIntegrator.class, "LBL_YesToAllButton"))) // NOI18N
            {
                supervisor.log(ITaskSupervisor.VERBOSE, NbBundle.getMessage(
                        UMLParsingIntegrator.class,
                        "MSG_DismissedOverwritePrompt_YesToAll")); // NOI18N
                
                promptStatus = PROMPT_YES_TO_ALL;
            }
            
            else if (actionEvent.getActionCommand().equalsIgnoreCase(NbBundle
                    .getMessage(UMLParsingIntegrator.class, "LBL_NoToAllButton"))) // NOI18N
            {
                supervisor.log(ITaskSupervisor.VERBOSE, NbBundle.getMessage(
                        UMLParsingIntegrator.class,
                        "MSG_DismissedOverwritePrompt_NoToAll")); // NOI18N
                
                promptStatus = PROMPT_NO_TO_ALL;
            }
            
            dialog.setVisible(false);
            dialog.dispose();
        }
        
        public Object getResult()
        {
            result = dialogDesc.getValue();
            if (result == yesToAllBtn)
            {
                result = DialogDescriptor.YES_OPTION;
            }
            else if (result == noToAllBtn)
            {
                result = DialogDescriptor.NO_OPTION;
            }
            return result;
        }
        
        private void initAccessibility()
        {
            // Yes to All button
            yesToAllBtn.setActionCommand(NbBundle.getMessage(
                    UMLParsingIntegrator.class, "LBL_YesToAllButton")); // NOI18N
            yesToAllBtn.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(
                    UMLParsingIntegrator.class, "ACSD_YesToAllButton")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(
                    yesToAllBtn, NbBundle.getMessage(
                    UMLParsingIntegrator.class, "LBL_YesToAllButton")); // NOI18N
            
            // No to All button
            noToAllBtn.setActionCommand(NbBundle.getMessage(
                    UMLParsingIntegrator.class, "LBL_NoToAllButton")); // NOI18N
            noToAllBtn.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(
                    UMLParsingIntegrator.class, "ACSD_NoToAllButton")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(
                    noToAllBtn, NbBundle.getMessage(
                    UMLParsingIntegrator.class, "LBL_NoToAllButton")); // NOI18N
            
            // No to All button
            noBtn.setActionCommand(NbBundle.getMessage(
                    UMLParsingIntegrator.class, "LBL_NoButton")); // NOI18N
            noBtn.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(
                    UMLParsingIntegrator.class, "ACSD_NoButton")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(
                    noBtn, NbBundle.getMessage(
                    UMLParsingIntegrator.class, "LBL_NoButton")); // NOI18N
        }
    }
    
    private final static int PROMPT_FOR_ALL = 0;
    private final static int PROMPT_YES_TO_ALL = 1;
    private final static int PROMPT_NO_TO_ALL = 2;
    private int promptStatus = PROMPT_FOR_ALL;
    
    
    public boolean oKToInject(
            Node parent,
            HashMap<String, ETList<Node>> namedNodes,
            Node elementBeingInjected)
    {
        boolean ok = false;
        if ((parent != null) && (elementBeingInjected != null))
        {
            ok = true;
            String childName =
                    XMLManip.getAttributeValue(elementBeingInjected, "name");
            
            ETList<Node> temp = namedNodes.get(childName);
            
            if (temp != null)
            {
                Element injected = (Element) elementBeingInjected;
                String injectNodeName = injected.getQualifiedName();
                Node childInDestinationNamespace = getElementOfType(temp, injectNodeName);
                
                if ((childInDestinationNamespace != null) &&
                        !m_CancelDueToConflict)
                {
                    boolean overwrite = true;
                    
                    if (!isNodeContainer(injectNodeName))
                    {
                        String destFile = retrieveSourceFile(childInDestinationNamespace);
                        String injectFile = retrieveSourceFile(elementBeingInjected);
                        // displayConflictDialog(childName, destFile, injectFile);
                        
                        overwrite = displayConflictDialog(
                                XMLManip.getAttributeValue(
                                elementBeingInjected, "name")); // NOI18N
                    }
                    
                    if (!m_CancelDueToConflict && overwrite)
                    {
                        //CComPtr< IXMLDOMNode > pNewNode;
                        if (isAbleToWrite(childInDestinationNamespace))
                        {
                            //ok = ReplaceElement(pNewNode, elementBeingInjected);
                            ok = replaceElement(
                                    childInDestinationNamespace,
                                    elementBeingInjected);
                        }
                        
                        else
                            ok = false;
                    }
                    
                    else
                        ok = false;
                }
                
                else
                {
                    // There is not name conflict because the two
                    // elements are different model element types.
                    //ok = false;
                    ok = true;
                }
            }
        }
        
        return ok;
    }
    
    /**
     *
     * Builds a map of all child elements under parent that contain the @name xml attribute
     *
     * @param parent[in]       The parent to query
     * @param namedNodes[out]  The filled in map
     *
     * @return HRESULT
     *
     */
    public void buildNodesFromParent(Node parent, HashMap < String, ETList < Node >> namedNodes)
    {
        try
        {
            List nodes = XMLManip.selectNodeList(parent, "./*[@name]");
            if (nodes != null)
            {
                int num = nodes.size();
                for (int x = 0; x < num; x++)
                {
                    Node node = (Node) nodes.get(x);
                    if (node != null)
                    {
                        String name = XMLManip.getAttributeValue(node, "name");
                        if (name != null)
                        {
                            ETList < Node > nodeList = namedNodes.get(name);
                            if (nodeList == null)
                            {
                                nodeList = new ETArrayList < Node > ();
                                namedNodes.put(name, nodeList);
                            }
                            nodeList.add(node);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void scrubMultiplicities(Node typedElement)
    {
        try
        {
            List mults = typedElement.selectNodes(".//UML:Multiplicity");
            if (mults != null)
            {
                int numMults = mults.size();
                for (int x = 0; x < numMults; x++)
                {
                    Node mult = (Node) mults.get(x);
                    if (mult != null)
                    {
                        establishXMIID(mult);
                        processRanges(mult);
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void processRanges(Node multiplicity)
            
    {
        try
        {
            List ranges = multiplicity.selectNodes(".//UML:MultiplicityRange");
            if (ranges != null)
            {
                int numRanges = ranges.size();
                for (int x = 0; x < numRanges; x++)
                {
                    Node range = (Node) ranges.get(x);
                    if (range != null)
                    {
                        establishXMIID(range);
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void scrubExpressions(Node typedElement)
    {
        try
        {
            List expNodes = typedElement.selectNodes(".//UML:Expression");
            if (expNodes != null)
            {
                int numNodes = expNodes.size();
                for (int x = 0; x < numNodes; x++)
                {
                    Node node = (Node) expNodes.get(x);
                    if (node != null)
                    {
                        establishXMIID(node);
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public Node processClass(IParserData pData)
    {
        Node pNode = null;
        Node node = scrubData(pData, false);
        if (node != null)
        {
            prepareClass(node);
            addToSymbolTable(node);
            return node;
        }
        return null;
    }
    
    public void processNestedClass(Node classNode)
    {
        try
        {
            List nestedClasses = classNode.selectNodes("UML:Element.ownedElement/*[ name() = 'UML:Class' or name() = 'UML:Interface'  or name() = 'UML:Enumeration']");
            if (nestedClasses != null)
            {
                int numNested = nestedClasses.size();
                // Retrieve the XMI ID of the parent so to be used to set the owner attribute.
                String parentID = XMLManip.getAttributeValue(classNode, "xmi.id");
                for (int x = 0; x < numNested; x++)
                {
                    Node nested = (Node) nestedClasses.get(x);
                    if (nested != null)
                    {
                        XMLManip.setAttributeValue(nested, "owner", parentID);
                        establishXMIID(nested);
                        addToSymbolTable(nested);
                        processNestedClass(nested);
                        processTemplateParameters(nested);
                        processComment(nested);
                        addClass(nested);
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void processTemplateParameters(Node classNode)
    {
        try
        {
            List parmaterElements = classNode.selectNodes("UML:Element.ownedElement/UML:ParameterableElement");
            if (parmaterElements != null)
            {
                int numElements = parmaterElements.size();
                String parametersID = "";
                for (int x = 0; x < numElements; x++)
                {
                    Node parameter = (Node) parmaterElements.get(x);
                    if (parameter != null)
                    {
                        establishXMIID(parameter);
                        String idValue = XMLManip.getAttributeValue(parameter, "xmi.id");
                        if (idValue != null)
                        {
                            if (parametersID.length() > 0)
                            {
                                parametersID += " ";
                            }
                            parametersID += idValue;
                        }
                    }
                }
                if (parametersID != null && parametersID.length() > 0)
                {
                    XMLManip.setAttributeValue(classNode, "templateParameter", parametersID);
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    
    public INamespace displayDiagramCreationWizard()
    {
        //No code in C++
        return null;
    }
    
    public void establishLanguageManager()
    {
        if (m_LanguageManager == null)
        {
            ICoreProduct pProduct = ProductRetriever.retrieveProduct();
            
            if (pProduct != null)
                m_LanguageManager = pProduct.getLanguageManager();
        }
    }
    
    public void retrieveFilesFromUser()
    {
        try
        {
            // AZTEC TODO: Uncomment this lot once IREDialog and REDialog are
            // in place.
            
            //      		IREDialog  reDiag = new REDialog();
            //      		reDiag.setNamespace( m_Namespace );
            //      		reDiag.display(null);
            //
            //      		retrieveSettings(reDiag);
            //         	m_Files = reDiag.getFiles();
            //         	m_Namespace = reDiag.getNamespace();
            //         	if( m_Namespace != null)
            //         	{
            //            	setProject( m_Namespace );
            //         	}
            //         	else
            //         	{
            //            	m_Project = createNewProject( reDiag.getLanguageSelected());
            //            	m_Namespace = m_Project;
            //         	}
            
            // TODO: Remove after the RE Dialog has been implemented.
            org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager projMgr =
                    ProductHelper.getProductProjectManager();
            
            if (projMgr != null)
            {
                projMgr.displayNewProjectDialog();
                IProject project = projMgr.getCurrentProject();
                
                if (project != null)
                {
                    m_Namespace = project;
                    setProject(project);
                    m_Files = new Strings();
                    javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
                    
                    chooser.setFileSelectionMode(
                            javax.swing.JFileChooser.DIRECTORIES_ONLY);
                    
                    if (chooser.showOpenDialog(null) ==
                            javax.swing.JFileChooser.APPROVE_OPTION)
                    {
                        File selected = chooser.getSelectedFile();
                        
                        if (selected.isDirectory() == true)
                            fillInFiles(selected, "java");
                        
                        else
                            m_Files.add(selected.getAbsolutePath());
                    }
                }
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void fillInFiles(File file, String extension)
    {
        File[] children = file.listFiles();
        for (int index = 0; index < children.length; index++)
        {
            File curFile = children[index];
            
            if (curFile.isDirectory() == true)
                fillInFiles(curFile, extension);
            
            else if (StringUtilities.hasExtension(
                    curFile.getAbsolutePath(), extension))
            {
                m_Files.add(curFile.getAbsolutePath());
            }
        }
    }
    
    public void integrate(INamespace pSpace)
    {
        supervisor.log(ITaskSupervisor.SUMMARY);
        supervisor.log(
                ITaskSupervisor.SUMMARY,
                getLocalMsg("IDS_ANALYZING_ATTRS_OPS_SIZE",
                new Integer(m_SymbolTable.size())));
        
        try
        {
            m_CancelDueToConflict = false;
            // performPreAttributeTypeResolution() );
            
            // First rip through the list and analyze all the types found on attributes and operations.
            // We need to move this functionality here in order to not create bogus package structures
            // when a class owned another class or enum. We must pass through the list twice BEFORE we
            // begin relationship resolution so that our symbol table is complete.
            //
            // This code was in processSymbols(), but what was happening is that in some cases
            // a package would be added to the document fragment AFTER the InjectIntoNamespace
            // call was already done. This is bad in the case where the package is a top level
            // package that should go directly under the Project node.
            
            Collection<ArrayList<ETPairT<Node, String>>> values =
                    m_SymbolTable.values();
            
            supervisor.log(ITaskSupervisor.DEBUG,
                    INDENT_DASH + " symbolTable keys=" + m_SymbolTable.toString());
            
            // start next subtask: analyze attr/oper types
            if (!supervisor.start(values.size()))
                onCancelled();
            
            for (Iterator<ArrayList<ETPairT<Node, String>>> iter =
                    values.iterator(); iter.hasNext() && !m_Cancelled;)
            {
                ArrayList<ETPairT<Node, String>> curSymbolList = iter.next();
                
                //                supervisor.log(
                //                    ITaskSupervisor.SUMMARY,
                //                    getLocalMsg("IDS_ANALYZING_ATTRS_OPS_SIZE",
                //                        new Integer(curSymbolList.size())));
                
                supervisor.log(ITaskSupervisor.VERBOSE, INDENT + INDENT +
                        "Analyzing " + curSymbolList.size() + "symbol(s)");
                
                supervisor.log(ITaskSupervisor.DEBUG, INDENT_DASH +
                        "curSymbolList " + curSymbolList.toArray());
                
                for (Iterator<ETPairT<Node, String>> symbolIter =
                        curSymbolList.iterator();
                symbolIter.hasNext() && !m_Cancelled;)
                {
                    ETPairT<Node, String> curSymbol = symbolIter.next();
                    Node curNode = curSymbol.getParamOne();
                    
                    //                    if (!supervisor.proceed(1))
                    //                        onCancelled();
                    
                    supervisor.log(ITaskSupervisor.VERBOSE,
                            INDENT + INDENT + "Analyzing " + curNode.getName());
                    
                    analyzeAttributeTypes(curNode);
                    analyzeOperationTypes(curNode);
                }
            }
            
            // Add the document fragment that this integrator contains
            // to the node of the namespace. This will establish all the
            // packages and classes in the DOM tree for easy lookup later.
            injectIntoNamespace(pSpace);
            performPreAttributeTypeResolution();
            
            if (!m_CancelDueToConflict && !m_Cancelled)
            {
                prepareForIntegration(pSpace);
                postProcess();
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void onCancelled()
    {
        supervisor.log(ITaskSupervisor.SUMMARY, " Cancelled");
        
        m_Cancelled = true;
        supervisor.cancel();
    }
    
    public void establishDependency(
            INamedElement client,
            INamedElement supplier,
            INamespace classSpace,
            String depType,
            String typeName)
    {
        //This method not completed.
        try
        {
            INamespace relSpace = classSpace;
            if (classSpace == null)
            {
                relSpace = m_Project;
            }
            
            IDependency dep = null;
            ETPairT<IInterface, IDependency> argumentPair = null;
            
            IClassifier pOutSupplier = null;
            String supplierType = "";
            if ("Implementation".equals(depType))
            {
                if(supplier instanceof IDerivationClassifier)
                {
                    IDependency pImplementation = m_Factory.createDependency2( client,
                            supplier, depType, relSpace);
                    pOutSupplier = (IClassifier)supplier;
                    supplierType = supplier.getElementType();
                    redef.add(pImplementation);
                }
                else
                {
                    argumentPair = m_Factory.createImplementation(
                            client, supplier, relSpace);
                    
                    pOutSupplier = argumentPair.getParamOne();
                    supplierType = supplier.getElementType();
                    redef.add(argumentPair.getParamTwo());                    
                }
                
                
                if (pOutSupplier != null)
                {
                    String name = pOutSupplier.getName();
                    String id = pOutSupplier.getXMIID();
                    
                    // ANOTHER QUESTION
                    ArrayList<ETPairT<Node, String>> symList =
                            m_SymbolTable.get(name);
                    
                    if ((symList != null) && (symList.size() > 0))
                    {
                        Node pNode = pOutSupplier.getNode();
                        
                        // Put the id of the symbol in this attribute entry;
                        
                        if (symList.size() == 1)
                        {
                            ETPairT < Node, String > symbol = symList.get(0);
                            symbol.setParamOne(pNode);
                            symbol.setParamTwo(id);
                        }
                        
                        else
                        {
                            for (Iterator < ETPairT < Node, String > > iter = symList.iterator(); iter.hasNext();)
                            {
                                ETPairT < Node, String > curEntry = iter.next();
                                
                                if (curEntry.getParamTwo().equals(id))
                                {
                                    curEntry.setParamOne(pNode);
                                    break;
                                    
                                }
                            }
                            
                        }
                    }
                    
                    if (supplierType.equals("DataType"))
                    {
                        // Here's what's happening. The supplier element was just
                        // transformed from a DataType element to an Interface in
                        // order to support the Implmentation relationship. We
                        // now need to make sure our list of DataTypes is updated
                        // with the information.
                        
                        String fullName = pOutSupplier.getQualifiedName();
                        addDataTypeToUnresolved(fullName, pOutSupplier, false);
                        
                        if (!(fullName.equals(name)))
                        {
                            UnresolvedType type = m_Types.get(name);
                            
                            if (type != null)
                            {
                                String typeID = getUnknownTypeID(type);
                                
                                if (typeID.length() > 0 && typeID.equals(id))
                                {
                                    addDataTypeToUnresolved(name, pOutSupplier, false);
                                }
                            }
                        }
                        else if (typeName.length() != 0)
                        {
                            addDataTypeToUnresolved(typeName, pOutSupplier, false);
                        }
                    }
                    
                }
            }
            else
            {
                dep = m_Factory.createDependency2(client, supplier, depType, relSpace);
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void establishDependency(Node clazz, IClassifier clazzObj, INamespace classSpace, String typeName, boolean classDependent, String depType)
    {
        try
        {
            INamedElement supplier = null;
            if (classDependent)
            {
                supplier = resolveType(clazz, clazzObj, classSpace, typeName, true);
            }
            else
            {
                Node nodeValue = (Node) m_Packages.get(typeName);
                if (nodeValue != null)
                {
                    TypedFactoryRetriever fact;
                    IPackage pack = new TypedFactoryRetriever < IPackage > ().createTypeAndFill("Package", nodeValue);
                    supplier = pack;
                }
            }
            if (supplier != null)
            {
                establishDependency(clazzObj, supplier, classSpace, depType, typeName);
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void pushPackage(Node packageName)
    {
        try
        {
            if (m_ContextStack.size() > 0)
            {
                m_ContextStack.peek().addPackage(packageName);
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void establishGeneralization(IClassifier subClass, IClassifier superClass)
    {
        
        try
        {
            IGeneralization gen = m_Factory.createGeneralization(superClass, subClass);
            redef.add(gen);
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public INamedElement locateType(IElement context, String xmiID)
    {
        try
        {
            // We've found a type resolution, so we'll retrieve the
            // Class that this resolves to and create an association
            if (m_Locator != null)
            {
                //         		String 	query =  "id( '" ;
                //         				query += xmiID;
                //         				query += "')";
                
                //IElement element = m_Locator.findSingleElementByQuery( context, query);
                IElement element = m_Locator.findElementByID(context, xmiID);
                //ATLASSERT( element );
                if (element != null)
                {
                    INamedElement classifier = (element instanceof INamedElement) ? (INamedElement) element : null;
                    if (classifier != null)
                    {
                        return classifier;
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return null;
    }
    
    public INamedElement resolveType(Node clazz,
            IClassifier clazzObj,
            INamespace classSpace,
            String typeName,
            boolean createUnknownType)
    {
        INamedElement newType = null;
        try
        {
            boolean removeAttr = true;
            
            ETPairT < INamedElement, String > result = getTypeID(typeName, clazz, classSpace);
            
            String typeID = result.getParamTwo();
            
            newType = result.getParamOne();
            
            if (typeID.length() > 0)
            {
                newType = locateType(clazzObj, typeID);
            }
            
            if ((newType == null) && (typeName.length() > 0))
            {
                // Check to see if typeName is fully qualified.
                if (typeName.indexOf("::") >= 0)
                {
                    newType = findQualifiedType(classSpace, typeName, clazz);
                }
                
                INamedElement namedElement = retrieveType(clazz, classSpace, typeName, createUnknownType);
                if (namedElement != null)
                {
                    newType = namedElement;
                }
                else
                {
                    removeAttr = false;
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return newType;
    }
    
    protected INamedElement findQualifiedType(INamespace classSpace,
            final String typeName,
            Node clazzNode)
    {
        return findQualifiedType(classSpace, typeName, clazzNode, true);
    }
    
    /**
     * Finds a type that is partially or fully qualified. Attempts to resolve the difference between a scoping package
     * vs. a scoping class or interface
     *
     * @param classSpace[in]   The namespace that will be used to handle partial qualifications
     * @param typeName[in]     The fully / partially qualified type in UML syntax, e.g., "Format::FieldDelegate"
     * @param createUnknown[in] true to create the type if not found, else false to not
     * @return The found type, else <code>null</code>
     *
     * @return HRESULT
     */
    
    protected INamedElement findQualifiedType(INamespace classSpace,
            final String typeName,
            Node clazzNode,
            boolean createUnknown)
    {
        INamedElement newType = null;
        // First attempt to find the type in the immediate namespace, if it exists
        INamespace beginSpace = classSpace;
        if (beginSpace == null)
        {
            beginSpace = m_Project;
        }
        
        if (beginSpace != null && typeName != null && typeName.length() > 0)
        {
            String packageName = resolvePackageFromUMLName(typeName);
            String shortTypeName = resolveTypeNameFromUMLName(typeName);
            
            Node packageNode = m_Packages.get(packageName);
            
            if (packageNode != null)
            {
                // We first attempt to find a package that fully scopes the type.
                String ownerID = XMLManip.getAttributeValue(packageNode, "xmi.id");
                newType = resolveQualifiedTypeName(shortTypeName, ownerID, null);
            }
            else
            {
                // We might have an inner class
                newType = findQualifiedType(typeName, clazzNode, classSpace);
            }
            
            if (newType == null)
            {
                String spaceID = beginSpace.getXMIID();
                ETList < String > tokens = StringUtilities.splitOnDelimiter(typeName, "::");
                String fullSpaceName = classSpace != null ? classSpace.getQualifiedName2() : null;
                for (int i = 0, count = tokens.size(); i < count && newType == null; ++i)
                {
                    // First, check our symbol table with just the name to see
                    // if we hit.
                    String symName = tokens.get(i);
                    if (symName != null && symName.length() != 0)
                    {
                        if ((newType = resolveQualifiedTypeName(symName, spaceID, null)) == null)
                        {
                            // Now check the packages
                            if (fullSpaceName != null && fullSpaceName.length() != 0)
                                fullSpaceName += "::" + symName;
                            else
                                fullSpaceName = symName;
                            if (fullSpaceName != null && fullSpaceName.length() != 0)
                            {
                                if (m_Packages.containsKey(fullSpaceName))
                                {
                                    Node packNode = m_Packages.get(fullSpaceName);
                                    if (packNode != null)
                                    {
                                        String curID = XMLManip.getAttributeValue(packNode, "xmi.id");
                                        if (curID == null || curID.length() == 0)
                                        {
                                            establishXMIID(packNode);
                                            curID = XMLManip.getAttributeValue(packNode, "xmi.id");
                                        }
                                        
                                        if (curID != null && curID.length() != 0)
                                            spaceID = curID;
                                    }
                                }
                                else
                                {
                                    // Package structure has not been established. Just bail.
                                    break;
                                }
                            }
                            else
                            {
                                // Package structure has not been established. Just bail.
                                break;
                            }
                        }
                    }
                }
            }
            else
            {
                resolveUnknownType(typeName, newType.getXMIID(), false);
            }
            
            
            if ((newType == null) && (createUnknown == true))
            {
                // We could not find the fully qualified type, so we should
                // create the type at the namespace location specified
                newType = createUnknownTypeAtLocation(typeName);
            }
        }
        return newType;
    }
    
    /**
     * Attempts to find a type at a given nesting level. The id of the potential
     * enclosing namespace is passed in in an
     * effort to differentiate like named symbols with different owners.
     *
     * @param symName[in]   The name of the symbol to find. This should NOT be
     *                      fully qualified
     * @param spaceID[in]   The id of the namespace that should potentially own
     *                      the type
     * @param tokens        The tokenizer that symName was pulled from. This is an
     *                      iterator produced by iteratoring over
     *                      a list of tokens discovered while breaking down a
     *                      fully qualified name. Can be <code>null</code>
     *
     * @return The found type, else <code>null</code>.
     */
    
    protected INamedElement resolveQualifiedTypeName(final String symName, String spaceID, StringTokenizer tokens)
    {
        INamedElement newType = null;
        
        if (symName.length() > 0)
        {
            TypedFactoryRetriever < INamedElement > ret = new TypedFactoryRetriever();
            
            ArrayList < ETPairT < Node, String > > result = m_SymbolTable.get(symName);
            if (result != null)
            {
                //            SymbolTable : : iterator lowerIter = m_SymbolTable.lower_bound(symName);
                //            SymbolTable : : iterator upperIter = m_SymbolTable.upper_bound(symName);
                
                for (Iterator < ETPairT < Node, String > > iter = result.iterator(); iter.hasNext();)
                {
                    ETPairT < Node, String > curEntry = iter.next();
                    
                    Node symNode = curEntry.getParamOne();
                    
                    if (symNode != null)
                    {
                        // Now lets see if the owning namespace is our owner
                        String ownerID = XMLManip.getAttributeValue(symNode, "owner");
                        //Debug.assertFalse(ownerID.length() > 0);
                        
                        long numSyms = result.size();
                        
                        // typeNameIsComplete is used to understand whether or not a
                        // fully qualified type, which is being potentially iterated
                        // on, is done being iterated on. What this means is the
                        // following.  Give a type, A::B::C::D, where 'D' is a class,
                        // we need to make sure that we don't have a 'D' class in
                        // A::B. However, if the tokens being passed in is null AND the
                        // ownerID is equal to the spaceID, then we know we have a
                        // match.
                        
                        //boolean typeNameIsComplete = ((iter != 0 && (++iter == theEnd)) || (iter == 0)) ? true : false;
                        boolean typeNameIsComplete = true;
                        if ((tokens != null) && (tokens.hasMoreTokens() == false))
                        {
                            typeNameIsComplete = false;
                        }
                        
                        if ((ownerID.equals(spaceID) == true) || (numSyms == 1))
                        {
                            if ((typeNameIsComplete == true) || ((numSyms == 1) && (tokens == null)))
                            {
                                // We've found our symbol
                                newType = ret.createTypeAndFill(symNode);
                                break;
                            }
                            else if (tokens != null)
                            {
                                String curID = curEntry.getParamTwo();
                                
                                if (curID.length() == 0)
                                {
                                    establishXMIID(symNode);
                                    curID = XMLManip.getAttributeValue(symNode, "xmi.id");
                                }
                                
                                spaceID = curID;
                            }
                        }
                        //                  The C++ version incremented there iterator to check if
                        //                  typeNameIsComplete == true.  Since I did not have to
                        //                  increment the tokens to check if we where complete, I do not
                        //                  have to back up the tokenizer.
                        //                  if(tokens != null)
                        //                  {
                        //                     --iter;
                        //                  }
                    }
                }
            }
        }
        
        return newType;
    }
    
    protected INamedElement fillElementIfFound(final String typeName, Node clazz, INamespace classNamespace)
    {
        INamedElement retVal = null;
        boolean needSearch = true;
        
        if (typeName.length() > 0)
        {
            ArrayList < ETPairT < Node, String > > entry = m_SymbolTable.get(typeName);
            
            if (entry != null)
            {
                if (entry.size() == 1)
                {
                    Node node = entry.get(0).getParamOne();
                    //               Debug.assertNull(node);
                    
                    if (node != null)
                    {
                        TypedFactoryRetriever < INamedElement > ret = new TypedFactoryRetriever < INamedElement > ();
                        retVal = ret.createTypeAndFill(node);
                        needSearch = false;
                        
                    }
                }
                else
                {
                    retVal = resolveTypeThroughDependencies(typeName, clazz, classNamespace);
                }
            }
            else
            {
                retVal = resolveTypeThroughDependencies(typeName, clazz, classNamespace);
            }
            
            if (retVal != null)
            {
                String xmiID = retVal.getXMIID();
                resolveUnknownType(typeName, xmiID, needSearch);
            }
            
        }
        
        return retVal;
    }
    
    protected String getUnknownTypeID(final UnresolvedType type)
    {
        String id = "";
        
        // We only want to return the id of an unknown type if that is the only
        // type by that name
        long numTypes = type.getNumberOfTypes();
        if (numTypes == 1)
        {
            id = type.getXMIID();
        }
        
        return id;
    }
    
    public boolean performDependencySearch(UnresolvedType type)
    {
        return type.needsSearch();
    }
    
    protected void addDataTypeToUnresolved(final String typeName, final String xmiID)
    {
        setUnknowTypeDetails(typeName, xmiID, true, false, null);
    }
    
    public void addDataTypeToUnresolved(String typeName, INamedElement namedElement)
    {
        addDataTypeToUnresolved(typeName, namedElement, false);
    }
    
    public void addDataTypeToUnresolved(String typeName, INamedElement namedElement, boolean needSearch)
    {
        if (namedElement != null)
        {
            String xmiID = namedElement.getXMIID();
            
            String elementType = namedElement.getElementType();
            
            setUnknowTypeDetails(typeName, xmiID, elementType.equals(mUnknownType), needSearch, namedElement);
        }
    }
    
    //
    //   public void addDataTypeToUnresolved( String typeName, INamedElement namedElement, boolean needSearch )
    //   {
    //      if( namedElement != null )
    //      {
    //         String xmiID = namedElement.getXMIID();
    //
    //         String elementType = namedElement.getElementType();
    //
    //         setUnknowTypeDetails( typeName, xmiID, elementType.equals(mUnknownType), needSearch, namedElement );
    //      }
    //  }
    
    protected void resolveUnknownType(final String typeName, final String xmiID, boolean needSearch)
    {
        setUnknowTypeDetails(typeName, xmiID, false, needSearch, null);
    }
    
    protected void setUnknowTypeDetails(final String typeName, final String xmiID, boolean isDatatype, boolean needSearch, INamedElement namedElement)
    {
        if ((typeName.length() > 0) && (xmiID.length() > 0))
        {
            UnresolvedType value = m_Types.get(typeName);
            
            String id = xmiID;
            
            if (namedElement != null)
            {
                String temp = namedElement.getXMIID();
                if (temp.length() > 0)
                {
                    id = temp;
                }
            }
            
            if (value != null)
            {
                //value.setXMIID(xmiID);
                //value.setNumberOfTypes(1);
                //value.setDataType(isDatatype);
                
                value.reset(xmiID, 1, isDatatype, needSearch, namedElement);
            }
            else
            {
                m_Types.put(typeName, new UnresolvedType(xmiID, 1, isDatatype, needSearch, namedElement));
            }
        }
    }
    
    protected String resolveInnerClassName(final String typeName, final Node clazz)
    {
        String retVal = typeName;
        
        int loc = typeName.lastIndexOf("::");
        if ((loc > 0) && (clazz != null))
        {
            String namespaceName = resolvePackageFromUMLName(typeName);
            
            StringBuffer query = new StringBuffer("./UML:ResDeps/UML:Dependency[ ends-with( @supplier, '");
            query.append(namespaceName);
            query.append("')]");
            
            List deps = clazz.selectNodes(query.toString());
            if ((deps != null) && (deps.size() > 0))
            {
                Node dep = (Node) deps.get(0);
                Debug.assertNull(dep);
                
                if (dep != null)
                {
                    String supplier = XMLManip.getAttributeValue(dep, "supplier");
                    {
                        Node tDesc = dep.selectSingleNode("./TokenDescriptors[@type='Class Dependency']");
                        if (tDesc == null)
                        {
                            tDesc = dep.selectSingleNode("./TokenDescriptors/TDescriptor[@type='Class Dependency']");
                        }
                        Debug.assertNull(tDesc);
                        
                        if (tDesc != null)
                        {
                            String value = XMLManip.getAttributeValue(tDesc, "value");
                            
                            if (value.equals("true") == true)
                            {
                                // We have the type we need!
                                retVal = supplier + typeName.substring(loc);
                            }
                        }
                    }
                    
                }
            }
            else
            {
                retVal = resolveInnerClassName(namespaceName, clazz);
                retVal += "::" + resolveTypeNameFromUMLName(typeName);
            }
        }
        
        return retVal;
    }
    
    protected INamedElement resolveTypeThroughDependencies(final String typeName, Node clazz, INamespace classNamespace)
    {
        INamedElement foundElement = null;
        
        // Check to see if a type by the passed in name exists in the namespace of the class
        
        if (classNamespace != null)
        {
            foundElement = getOwnedTypeByName(classNamespace, typeName);
        }
        
        if (foundElement == null)
        {
            // Need to find the element through the dependencies on the class
            // Retrieve all the UML:Dependency elements owned by the UML:ResDeps
            // element
            
            StringBuffer query = new StringBuffer("./UML:ResDeps/UML:Dependency[ contains( @supplier, '");
            query.append(typeName);
            query.append("')]");
            List deps = clazz.selectNodes(query.toString());
            if ((deps != null) && (deps.size() > 0))
            {
                String typeNameStr = typeName;
                boolean found = false;
                String typeNameToFind = "";
                
                for (Iterator iter = deps.iterator(); iter.hasNext();)
                {
                    Node dep = (Node) iter.next();
                    //Debug.assertNull(dep);
                    
                    if (dep != null)
                    {
                        String supplier = XMLManip.getAttributeValue(dep, "supplier");
                        if (supplier.length() > typeNameStr.length())
                        {
                            // This code, without the prepending of the "::" would incorrectly
                            // match when comparing "java::lang::InterruptException" with
                            // "Exception"
                            String qualifiedType = "::" + typeNameStr;
                            
                            String match = supplier.substring((supplier.length() - qualifiedType.length()));
                            if (match.equals(qualifiedType) == true)
                            {
                                // We have a direct match in terms of name. Now make sure we
                                // don't have a Package name matching a type name
                                
                                //Node tDesc = dep.selectSingleNode( "./TokenDescriptors/TDescriptor[@type='Class Dependency']" );
                                Node tDesc = dep.selectSingleNode("./TokenDescriptors[@type='Class Dependency']");
                                if (tDesc == null)
                                {
                                    tDesc = dep.selectSingleNode("./TokenDescriptors/TDescriptor[@type='Class Dependency']");
                                }
                                //Debug.assertNull(tDesc);
                                
                                if (tDesc != null)
                                {
                                    String value = XMLManip.getAttributeValue(tDesc, "value");
                                    
                                    if (value.equals("true") == true)
                                    {
                                        // We have the type we need!
                                        typeNameToFind = supplier;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (typeNameToFind.length() > 0)
                {
                    foundElement = findQualifiedType(classNamespace, typeNameToFind, clazz);
                }
            }
            
            if (foundElement == null)
            {
                // If we still haven't found the element, then let's start appending the typeName
                // to the package names found in the dependencies
                String queryStr = "./UML:ResDeps/UML:Dependency[ ./TokenDescriptors/TDescriptor[@type='Class Dependency' and @value='false'] ]";
                deps = clazz.selectNodes(queryStr);
                
                if (deps != null)
                {
                    for (Iterator iter = deps.iterator(); iter.hasNext();)
                    {
                        Node dep = (Node) iter.next();
                        //Debug.assertNull(dep);
                        
                        if (dep != null)
                        {
                            StringBuffer supplier = new StringBuffer(XMLManip.getAttributeValue(dep, "supplier"));
                            
                            if (supplier.length() > 0)
                            {
                                supplier.append("::");
                                supplier.append(typeName);
                                
                                foundElement = findQualifiedType(classNamespace, supplier.toString(), clazz, false);
                                
                                if (foundElement != null)
                                {
                                    // We found it!
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return foundElement;
    }
    
    public void establishGeneralization(Node clazz, IClassifier clazzObj, INamespace classSpace, String typeName)
    {
        try
        {
            
            INamedElement named = resolveType(clazz, clazzObj, classSpace, typeName, true);
            IClassifier superClass = (named instanceof IClassifier) ? (IClassifier) named : null;
            if (superClass != null)
            {
                establishGeneralization(clazzObj, superClass);
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void analyzeForInterfaces(Node clazz, IClassifier clazzObj, INamespace classSpace)
    {
        try
        {
            // Get all of this Classes attributes and resolve their types...
            Node genTDesc = clazz.selectSingleNode("./TokenDescriptors/TRealization");
            if (genTDesc != null)
            {
                Node spElement = this.isDerivationPresent(genTDesc, false);
                if (spElement != null)
                {
                    List spDerivationElements = genTDesc.selectNodes("./TokenDescriptors/TDerivation");
                    
                    for (int i = 0; i < spDerivationElements.size(); i++)
                    {
                        Node spDerivationElement = (Node) spDerivationElements.get(i);
                        if (spDerivationElement != null)
                        {
                            IClassifier supplier = this.ensureDerivation(spDerivationElement, clazz, clazzObj, classSpace);
                            if (supplier != null)
                            {
                                String typeName = supplier.getName();
                                establishDependency(clazzObj, supplier,
                                        classSpace, "Implementation",
                                        typeName);
                            }
                        }
                    }
                }
                else
                {
                    List interfaces = genTDesc.selectNodes("./Interface");
                    if (interfaces != null)
                    {
                        int num = interfaces.size();
                        String classID = clazzObj.getXMIID();
                        for (int x = 0; x < num; x++)
                        {
                            Node inter = (Node) interfaces.get(x);
                            if (inter != null)
                            {
                                String typeName = XMLManip.getAttributeValue(inter, "value");
                                if (typeName.length() > 0)
                                {
                                    establishDependency(clazz, clazzObj, classSpace, typeName, true, "Implementation");
                                }
                            }
                        }
                    }
                    
                    // Be sure to remove the TGeneralization descriptor so we don't
                    // clutter the Class node
                    genTDesc.detach();
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void analyzeForDependencies(Node clazz, IClassifier clazzObj, INamespace classSpace)
    {
      /*
      try
      {
         // Get all of this Classes attributes and resolve their types...
         List dependencies = clazz.selectNodes(".//UML:Dependency");
         if (dependencies != null)
         {
            int num = dependencies.size();
            String classID = clazzObj.getXMIID();
            for (int x = 0; x < num; x++)
            {
               Node dep = (Node)dependencies.get(x);
               if (dep != null)
               {
                  String typeName = XMLManip.getAttributeValue(dep, "name");
                  String classDependent = XMLManip.getAttributeValue(dep, "classDependent");
                  if (typeName.length() > 0)
                  {
                     establishDependency(clazz, clazzObj, classSpace, typeName, classDependent.equals("true") ? true : false, "Usage");
                  }
               }
               // Remove this node, as it is the temporary dependency sent from the
               // Framework
               dep.detach();
            }
         }
      }
      catch (Exception e)
      {
         sendExceptionMessage(e);
      }
       */
    }
    
    public void analyzeForGeneralizations(Node clazz, IClassifier clazzObj, INamespace classSpace)
    {
        try
        {
            // Get all of this Classes attributes and resolve their types...
            Node genTDesc = clazz.selectSingleNode("./TokenDescriptors/TGeneralization");
            if (genTDesc != null)
            {
                Node spElement = this.isDerivationPresent(genTDesc, false);
                if (spElement != null)
                {
                    //List spDerivationElements = genTDesc.selectNodes(".//TokenDescriptors/TDerivation");
                    List spDerivationElements = genTDesc.selectNodes("./TokenDescriptors/TDerivation");
                    
                    for (int i = 0; i < spDerivationElements.size(); i++)
                    {
                        Node spDerivationElement = (Node) spDerivationElements.get(i);
                        if (spDerivationElement != null)
                        {
                            IClassifier spDerivationClassifier = this.ensureDerivation(spDerivationElement, clazz, clazzObj, classSpace);
                            if (spDerivationClassifier != null)
                            {
                                establishGeneralization(clazzObj, spDerivationClassifier);
                            }
                        }
                    }
                }
                else
                {
                    List genNodes = genTDesc.selectNodes("./SuperClass");
                    if (genNodes != null)
                    {
                        int num = genNodes.size();
                        String classID = clazzObj.getXMIID();
                        for (int x = 0; x < num; x++)
                        {
                            Node gen = (Node) genNodes.get(x);
                            if (gen != null)
                            {
                                String typeName = XMLManip.getAttributeValue(gen, "value");
                                if (typeName != null)
                                {
                                    establishGeneralization(clazz, clazzObj, classSpace, typeName);
                                }
                            }
                        }
                    }
                    
                    // Be sure to remove the TGeneralization descriptor so we don't
                    // clutter the Class node
                    genTDesc.detach();
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void analyzeForDerivations(Node clazz, IClassifier clazzObj, INamespace classSpace)
    {
        try
        {
            // Get derivations from TokenDescriptors
            List spDerivationElements = clazz.selectNodes("./TokenDescriptors/TDerivation");
            if (spDerivationElements != null)
            {
                // Get document
                Document spDocument = m_Project.getDocument();
                // Create CreationFactory
                ICreationFactory spCreationFactory = FactoryRetriever.instance().getCreationFactory();
                // Find unique derivation elements
                int numDerivationElements = spDerivationElements.size();
                List spUniqueDerivationElements = null;
                int numUniqueDerivationElements = 0;
                if (numDerivationElements > 0)
                {
                    ETList < String > unquieDerivations = new ETArrayList < String > ();
                    Node spHolder = XMLManip.createElement(spDocument, "NewParent");
                    for (int i = 0; i < numDerivationElements; i++)
                    {
                        // Build Derivation string
                        String tempStr = null;
                        String sDerivation = null;
                        Node spNode = (Node) spDerivationElements.get(i);
                        if (spNode != null)
                        {
                            // Derivation name
                            tempStr = XMLManip.getAttributeValue(spNode, "name");
                            sDerivation += tempStr;
                            
                            // Derivation parameters
                            List spDerivationParameters = spNode.selectNodes("./DerivationParameter");
                            int numDerivationParameters = spDerivationParameters.size();
                            for (int y = 0; y < numDerivationParameters; y++)
                            {
                                Node spParameter = (Node) spDerivationParameters.get(y);
                                tempStr = XMLManip.getAttributeValue(spParameter, "value");
                                sDerivation += ",";
                                sDerivation += tempStr;
                            }
                        }
                        
                        // Check for existing
                        boolean bExisting = unquieDerivations.contains(sDerivation);
                        if (bExisting == false)
                        {
                            unquieDerivations.add(sDerivation);
                            XMLManip.insertNode((Element) spHolder, spNode, 0);
                        }
                    }
                    spUniqueDerivationElements = spHolder.selectNodes("./TDerivation");
                    numUniqueDerivationElements = spUniqueDerivationElements.size();
                }
                // For each unique derivation elements
                for (int x = 0; x < numUniqueDerivationElements; x++)
                {
                    // Get Derivation Element
                    Node spDerivationElement = (Node) spUniqueDerivationElements.get(x);
                    // Find the supplier
                    String supplierName = XMLManip.getAttributeValue(spDerivationElement, "name");
                    IClassifier spClassifier = null;
                    ETList < INamedElement > spNamedElements = m_Locator.findByNameInMembersAndImports(m_Project, supplierName);
                    
                    if (spNamedElements != null)
                    {
                        int numNamedElements = spNamedElements.size();
                        if (numNamedElements == 0)
                            break;
                        for (int j = 0; j < numNamedElements; j++)
                        {
                            INamedElement spNamedElement = spNamedElements.get(j);
                            
                            if (spNamedElement instanceof IClassifier)
                            {
                                spClassifier = (IClassifier) spNamedElement;
                                break;
                            }
                        }
                    }
                    // Create DerivationClassifier
                    Object spIUnknown = spCreationFactory.retrieveMetaType("DerivationClassifier", null);
                    IClassifier spDerivationClassifier = (spIUnknown instanceof IClassifier) ? (IClassifier) spIUnknown : null;
                    clazzObj.addOwnedElement(spDerivationClassifier);
                    
                    // Create Derivation
                    IDerivation spDerivation = m_Factory.createDerivation(spDerivationClassifier, spClassifier);
                    if (spDerivation == null)
                        break;
                    
                    // Get bindings
                    ETList < IUMLBinding > spUMLBindings = spDerivation.getBindings();
                    int numUMLBindings = spUMLBindings.size();
                    // Get actual parameters
                    List spDerivationParameterElements = spDerivationElement.selectNodes("./DerivationParameter");
                    int numDerivationParameterElements = spDerivationParameterElements.size();
                    // Set binding actual parameter
                    if (numUMLBindings == numDerivationParameterElements)
                    {
                        for (int y = 0; y < numDerivationParameterElements; y++)
                        {
                            // Get binding
                            IUMLBinding spUMLBinding = (IUMLBinding) spUMLBindings.get(y);
                            if (spUMLBinding == null)
                                break;
                            
                            // Get actual parameter value
                            Node spParameterelement = (Node) spDerivationParameterElements.get(y);
                            String paramenterValue = XMLManip.getAttributeValue(spParameterelement, "value");
                            // Set fromal type
                            INamedElement spNamedElement = resolveType(clazz,
                                    clazzObj,
                                    classSpace,
                                    paramenterValue,
                                    true);
                            if (spNamedElement != null)
                            {
                                IParameterableElement spIParameterableElement = (spNamedElement instanceof IParameterableElement) ? (IParameterableElement) spNamedElement : null;
                                
                                spUMLBinding.setActual(spIParameterableElement);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void addTaggedValue(Node parent, String tag, String value)
    {
        addTaggedValue(parent, tag, value, false);
    }
    
    protected void addTaggedValue(Node parent, String tag, String value, boolean hidden)
    {
        try
        {
            Node node = XMLManip.ensureNodeExists(
                    parent, "UML:Element.ownedElement", "UML:Element.ownedElement");
            
            if (node != null)
            {
                Node tagNode = XMLManip.createElement(
                        m_FragDocument, "UML:TaggedValue");
                
                if (tagNode != null)
                {
                    establishXMIID(tagNode);
                    XMLManip.setAttributeValue(tagNode, "name", tag);
                    if (hidden)
                    {
                        XMLManip.setAttributeValue(tagNode, "hidden", "true");
                    }
                    
                    try
                    {
                        XMLManip.setNodeTextValue(
                                tagNode, "UML:TaggedValue.dataValue", value, false);
                        XMLManip.insertNode((Element) node, tagNode, 0);
                    }
                    
                    catch (Exception e)
                    {
                        sendExceptionMessage(e);
                        
                        String errorMsg = REIntegrationMessages.getString(
                                "IDS_INVALID_CHARACTER"); // NOI18N
                        
                        new UMLMessagingHelper().sendErrorMessage(errorMsg);
                        supervisor.log(ITaskSupervisor.SUMMARY, errorMsg);
                        
                    }
                }
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    /**
     * Adds a source file artifact node that can be added to a class.
     *
     * @param fileName The name of the source file that contains a class.
     * @return The XML node taht represents a  source file artifact.
     */
    protected Node createSourceFileArtifact(String fileName)
    {
        Node artifact = null;
        try
        {
            if (fileName != null && fileName.length() > 0)
            {
                
                Node artNode = XMLManip.createElement(m_FragDocument, "UML:SourceFileArtifact");
                if (artNode != null)
                {
                    String relFile = PathManip.retrieveSourceRelativePath(m_Project, fileName, m_BaseDirectory);
                    XMLManip.setAttributeValue(artNode, "sourcefile", relFile);
                    artifact = artNode;
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return artifact;
    }
    
    protected String retrieveSourceFile(Node element)
    {
        String fileName = null;
        try
        {
            Node pNode = element.selectSingleNode(".//UML:SourceFileArtifact");
            fileName = null;
            if (pNode != null)
            {
                fileName = XMLManip.getAttributeValue(pNode, "sourcefile");
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
        return fileName;
    }
    
    public void establishArtifacts(ParsingContext context)
    {
        String fileName = "";
        try
        {
            ETList < Node > classes = context.getClasses();
            
            for (int i = 0; i < classes.size(); i++)
            {
                
                fileName = context.getFileName();
                
                if (fileName != null && fileName.length() > 0)
                {
                    Node artifact = createSourceFileArtifact(fileName);
                    establishOwnership(classes.get(i), artifact, false);
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void addDocumentation(Node node, String doc)
    {
        try
        {
            if (doc.length() > 0)
            {
                addTaggedValue(node, "documentation", doc);
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void processComment(Node node)
    {
        try
        {
            Node commentNode = node.selectSingleNode("./TokenDescriptors/TDescriptor[@type='Comment']");
            if (commentNode != null)
            {
                String comment = XMLManip.getAttributeValue(commentNode, "value");
                addDocumentation(node, comment);
                // Remove the TDescriptor element from the passed in node.
                // No longer needed.
                
                commentNode.detach();
            }
            Node markerIDNode = node.selectSingleNode("./TokenDescriptors/TDescriptor[@type='Marker-id']");
            if (markerIDNode != null)
            {
                String markerID = XMLManip.getAttributeValue(markerIDNode, "value");
                addTaggedValue(node, "MarkerID", markerID, true);
                markerIDNode.detach();
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void analyzeInterfaceTypes(Node classNode)
    {
        try
        {
            List interfaces = classNode.selectNodes("./TokenDescriptors/TRealization/Interface");
            if (interfaces != null)
            {
                int num = interfaces.size();
                for (int x = 0; x < num; x++)
                {
                    Node inter = (Node) interfaces.get(x);
                    if (inter != null)
                    {
                        String interTypeName = XMLManip.getAttributeValue(inter, "value");
                        addToUnresolvedTypes(interTypeName);
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void analyzeGeneralizationTypes(Node classNode)
    {
        try
        {
            List gens = XMLManip.selectNodeList(classNode, "./TokenDescriptors/TGeneralization/SuperClass");
            if (gens != null)
            {
                int numGens = gens.size();
                for (int x = 0; x < numGens; x++)
                {
                    Node gen = (Node) gens.get(x);
                    if (gen != null)
                    {
                        String genTypeName = XMLManip.getAttributeValue(gen, "value");
                        addToUnresolvedTypes(genTypeName);
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected boolean isPackageDependency(Node dep)
    {
        boolean isPackage = true;
        if (dep != null)
        {
            Node descriptor = dep.selectSingleNode(".//TDescriptor[@type='Class Dependency']");
            if (descriptor != null)
            {
                String value = XMLManip.getAttributeValue(descriptor, "value");
                // We'll set this value so we don't have to query for the descriptor again
                XMLManip.setAttributeValue(dep, "classDependent", value);
                if (value != null)
                {
                    isPackage = false;
                }
            }
        }
        return isPackage;
    }
    
    /**
     *
     * Pulls all the dependency fragments from the current context,
     * and places them on the current list of classes. They will be properly
     * processed and Dependency relationship will be created during the Integrate()
     * call.
     *
     * @param context[in] The current context    *
     * @see Integrate()
     */
    protected void prepareDependencies(ParsingContext context)
    {
        
        final ETList < Node > deps = context.getDependencies();
        
        for (Iterator < Node > iter = deps.iterator(); iter.hasNext();)
        {
            Node dep = iter.next();
            Debug.assertNotNull(dep);
            
            String name = XMLManip.getAttributeValue(dep, "name");
            String supplier = XMLManip.getAttributeValue(dep, "supplier");
            
            final ETList < Node > classes = context.getClasses();
            for (Iterator < Node > classIter = classes.iterator(); classIter.hasNext();)
            {
                Node curClass = classIter.next();
                
                // Make the ownership of the dependency temporary, as
                // these dependencies are used solely for type resolution,
                // and will be removed once the class is processed.
                
                establishOwnership(curClass, (Node) dep.clone(), true, "UML:ResDeps");
            }
        }
    }
    
    protected void processContext(ParsingContext context)
    {
        try
        {
            // Need to establish dependency relations that will be processed for completeness
            // during the Integrate call. The class node will actually own the dependency node.
            // Again, this will be removed in the Integrate call.
            prepareDependencies(context);
            establishArtifacts(context);
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void addClass(Node clazz)
    {
        if (m_ContextStack.size() > 0)
        {
            m_ContextStack.peek().addClass(clazz);
        }
    }
    
    protected void addDependency(Node dep)
    {
        if (m_ContextStack.size() > 0)
        {
            m_ContextStack.peek().addDependency(dep);
        }
    }
    
    protected void cleanXMLAttributes(Node node)
    {
        try
        {
            Element element = (node instanceof Element) ? (Element) node : null;
            if (element != null)
            {
                Attribute attr = element.attribute("language");
                if (attr != null)
                    attr.detach();
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void scrubOperation(Node oper, Node clazz, IClassifier clazzObj, INamespace classSpace)
    {
        try
        {
            scrubParameters(oper, clazz, clazzObj, classSpace);
            scrubExceptions(oper, clazz, clazzObj, classSpace);
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
    }
    
    protected void scrubParameters(Node oper, Node clazz, IClassifier clazzObj, INamespace classSpace)
    {
        
        try
        {
            List parms = oper.selectNodes("./UML:Element.ownedElement/UML:Parameter");
            if (parms != null)
            {
                int numParms = parms.size();
                if (numParms > 0)
                {
                    String id = XMLManip.getAttributeValue(oper, "xmi.id");
                    for (int x = 0; x < numParms; x++)
                    {
                        Node parm = (Node) parms.get(x);
                        if (parm != null)
                        {
                            establishXMIID(parm);
                            analyzeParameterForCollections(parm, clazzObj);
                            
                            Node spDerivationElement = this.isDerivationPresent(parm, true);
                            if (spDerivationElement != null)
                            {
                                IClassifier spDerivationClassifier = this.ensureDerivation(spDerivationElement, clazz, clazzObj, classSpace);
                                if (spDerivationClassifier != null)
                                {
                                    
                                    //establishAssociation(parm, clazzObj, spDerivationClassifier);
                                    XMLManip.setAttributeValue(parm, "type", spDerivationClassifier.getXMIID());
                                }
                                
                                scrubExpressions(parm);
                                scrubMultiplicities(parm);
                            }
                            else
                            {
                                String typeName = XMLManip.getAttributeValue(parm, "type");
                                scrubTypedElement(parm, clazz, classSpace, id, typeName);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void scrubExceptions(Node oper, Node clazz, IClassifier clazzObj, INamespace classSpace)
    {
        try
        {
            List exceptions = oper.selectNodes("./UML:Element.ownedElement/UML:Exception");
            if (exceptions != null)
            {
                int numExceptions = exceptions.size();
                if (numExceptions > 0)
                {
                    String id = XMLManip.getAttributeValue(oper, "xmi.id");
                    String raisedExceptions = "";
                    for (int x = 0; x < numExceptions; x++)
                    {
                        Node excep = (Node) exceptions.get(x);
                        if (excep != null)
                        {
                            
                            establishXMIID(excep);
                            
                            String typeName = XMLManip.getAttributeValue(excep, "name");
                            //UnresolvedType findType = m_Types.get(typeName);
                            String typeID = null;
                            //if (findType != null)
                            //{
                            //   typeID = getUnknownTypeID( findType );
                            //}
                            
                            ETPairT < INamedElement, String > result = getTypeID(typeName, clazz, classSpace);
                            
                            typeID = result.getParamTwo();
                            
                            if ((typeID == null) || (typeID.length() <= 0))
                            {
                                INamedElement classifier = retrieveType(clazz, classSpace, typeName, true);
                                if (classifier != null)
                                    typeID = classifier.getXMIID();
                            }
                            
                            if (typeID != null)
                            {
                                if (raisedExceptions.length() > 0)
                                {
                                    raisedExceptions += " ";
                                }
                                raisedExceptions += typeID;
                            }
                            Node pParent = excep.getParent();
                            if (pParent != null)
                            {
                                excep.detach();
                                //								pParent.removeChild(excep, null);
                            }
                        }
                    }
                    if (raisedExceptions != null && raisedExceptions.length() > 0)
                    {
                        XMLManip.setAttributeValue(oper, "raisedException", raisedExceptions);
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected boolean resolveType(String typeName, Node typedNode, Node clazz, INamespace classSpace, String attrName)
    {
        boolean resolved = false;
        String typeID = null;
        
        if ((typedNode != null) && (clazz != null))
        {
            
            ETPairT < INamedElement, String > result = getTypeID(typeName, clazz, classSpace);
            
            typeID = result.getParamTwo();
            
            if ((typeID != null) && (typeID.length() > 0))
            {
                XMLManip.setAttributeValue(typedNode, attrName, typeID);
                resolved = true;
            }
            
            if (!resolved)
            {
                INamedElement classifier = retrieveType(clazz, classSpace, typeName, true);
                if (classifier != null)
                {
                    String id = classifier.getXMIID();
                    XMLManip.setAttributeValue(typedNode, attrName, id);
                    resolved = true;
                }
            }
        }
        return resolved;
    }
    
    protected void scrubOperations(Node clazz, IClassifier clazzObj, INamespace classSpace)
    {
        try
        {
            // Get all of this Classes attributes and resolve their types...
            List opers = clazz.selectNodes("./UML:Element.ownedElement/UML:Operation");
            if (opers != null)
            {
                int numOpers = opers.size();
                if (numOpers > 0)
                {
                    String classID = XMLManip.getAttributeValue(clazz, "xmi.id");
                    for (int x = 0; x < numOpers; x++)
                    {
                        Node oper = (Node) opers.get(x);
                        if (oper != null)
                        {
                            establishXMIID(oper);
                            XMLManip.setAttributeValue(oper, "owner", classID);
                            processComment(oper);
                            scrubOperation(oper, clazz, clazzObj, classSpace);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void scrubTypedElement(Node typedElement, Node clazz, INamespace classSpace, String classID, String typeName)
    {
        try
        {
            String splicedTypeName = convertNamespace(typeName);
            establishXMIID(typedElement);
            XMLManip.setAttributeValue(typedElement, "owner", classID);
            if (typeName != null && typeName.length() > 0)
            {
                scrubExpressions(typedElement);
                scrubMultiplicities(typedElement);
                
                if (!resolveType(splicedTypeName, typedElement, clazz, classSpace, "type"))
                {
                    throw new IllegalStateException("We should never be here!");
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void establishAssociation(Node attr, Node clazz, IClassifier clazzObj, INamespace classSpace, String typeName)
    {
        try
        {
            boolean removeAttr = true;
            String typeID = null;
            boolean needsResolution = true;
            
            ETPairT < INamedElement, String > result = getTypeID(typeName, clazz, classSpace);
            
            typeID = result.getParamTwo();
            
            if (typeID.length() > 0)
            {
                // We've found a type resolution, so we'll retrieve the
                // Class that this resolves to and create an association
                boolean isCreated = createAssociation(attr, clazzObj, typeID);
                needsResolution = !isCreated;
            }
            
            if (needsResolution)
            {
                INamedElement namedElement = retrieveType(clazz, classSpace, typeName, true);
                IClassifier classifier = (namedElement instanceof IClassifier) ? (IClassifier) namedElement : null;
                
                if (classifier != null)
                {
                    createAssociation(attr, clazzObj, classifier);
                }
                else
                {
                    removeAttr = false;
                }
            }
            if (removeAttr)
            {
                
                String nodeName;
                nodeName = attr.getName();
                
                // We only want to remove attributes. It's possible to be in this
                // routine with a Parameter node...
                
                if (nodeName == "UML:Attribute")
                {
                    // Now remove the attribute, as we don't want to create an
                    // actual attribute now that it has been realized in an
                    // association
                    attr.detach();
                    //         		XMLManip.removeNode( attr);
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    /**
     * Finds the XMI ID of the type that matches the passed in name ( fully qualified or not ), in the namespace indicated
     *
     * @param typeName[in]        The name of the element to find
     * @param clazz[in]           The class node that needs the information
     * @param classSpace[in]      The namespace of clazz
     * @param found[in/out]       The found element if found, else 0. Can come in as 0, which will guarentee that 0
     *                            will be returned.
     *
     * @return The ID of the type, else ""
     */
    
    public ETPairT getTypeID(String typeName, Node clazz, INamespace classSpace)
    {
        String typeID = "";
        INamedElement found = null;
        
        if (clazz != null && classSpace != null)
        {
            UnresolvedType foundType = m_Types.get(typeName);
            
            if (foundType != null)
            {
                typeID = getUnknownTypeID(foundType);
                
                // Only set the type id IF we know that the unresolved type is the only one by that name.
                // If not, it needs to be resolved
                
                if (typeID != null && typeID.length() > 0)
                {
                    if (performDependencySearch(foundType))
                    {
                        // Need to see if we need to actually perform a class dependency check before
                        // actually resolving the type. This occurs when a type has been realized via
                        // a fully qualified name search, which is then added to the m_Types collection
                        // with its short name as well.
                        
                        INamedElement foundElement = resolveTypeThroughDependencies(typeName, clazz, classSpace);
                        
                        if (foundElement != null)
                        {
                            String xmiID = foundElement.getXMIID();
                            
                            if (xmiID.length() > 0)
                            {
                                typeID = xmiID;
                                
                                if (found == null)
                                {
                                    found = foundElement;
                                }
                            }
                        }
                    }
                    else if (found == null)
                    {
                        INamedElement foundElement = foundType.getElement();
                        if (foundElement != null)
                        {
                            found = foundElement;
                        }
                    }
                }
            }
        }
        
        return new ETPairT < INamedElement, String > (found, typeID);
    }
    
    public INamedElement retrieveType(Node clazz,
            INamespace space,
            String typeName,
            boolean createUnknownType)
    {
        INamedElement foundType = null;
        boolean innerClass = false;
        
        try
        {
            if (typeName != null)
            {
                boolean typeCreated = false;
                
                String xName = typeName;
                String prefix = xName.length() >= 4 ? xName.substring(0, 4) : xName;
                // This is an example of a DCE XMI id.
                // DCE.4C54B80B-66C6-4663-B59E-07F535E77E28
                // It is always 40 characters long.
                if ("DCE.".equals(prefix) && typeName.length() > 0)
                {
                    foundType = locateType(space, typeName);
                }
                else
                {
                    
                    foundType = findInnerClass(clazz, typeName);
                    if(foundType == null)
                    {
                        boolean fullyQualified = typeName.indexOf("::") >= 0;
                        if (fullyQualified == true)
                        {
                            foundType = findQualifiedType(space, typeName, clazz);
                        }
                        else
                        {
                            foundType = fillElementIfFound(typeName, clazz, space);
                        }
                    }
                    else
                    {
                        innerClass = true;
                    }
                    
                    if (foundType == null)
                    {
                        // Create a new type based on preferences
                        
                        INamedElement element = null;
                        
                        String typeNameStr = typeName;
                        String shortTypeName = resolveTypeNameFromUMLName(typeNameStr);
                        String packageName = resolvePackageFromUMLName(typeNameStr);
                        
                        if (packageName != null && packageName.length() > 0)
                        {
                            INamespace pPackage = establishPackageOwnership(space, typeNameStr);
                            foundType = retrieveType(clazz, pPackage, shortTypeName, true);
                        }
                        
                        // The type was not found, so resolve via the unknown type mechanism
                        if ((foundType == null) && (createUnknownType == true))
                        {
                            element = resolveUnknownType(typeName, m_Project);
                            
                            if (element != null)
                            {
                                foundType = element;
                                typeCreated = true;
                                addDataTypeToUnresolved(typeName, element, false);
                            }
                        }
                    }
                    
                }
                
                if ((foundType != null) && (innerClass = false))
                {
                    // Now that the type is found, be sure to place it on our
                    // list of known types so that we don't have to create
                    // again.
                    String xmiID = "";
                    
                    if (typeCreated == false)
                    {
                        xmiID = foundType.getXMIID();
                        
                        UnresolvedType type = m_Types.get(typeName);
                        
                        if (type != null)
                        {
                            if (type.getNumberOfTypes() == 1)
                            {
                                type.setXMIID(xmiID);
                            }
                        }
                        else
                        {
                            addToUnresolvedTypes(typeName, xmiID);
                        }
                    }
                    
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        return foundType;
    }
    
    protected INamedElement findInnerClass(Node clazz, String typeName)
    {
        INamedElement retVal = null;
        
        // Before adding the type as a unresolved type.  Check
        // the type is an inner class.
        
        List < Node > nestedClasses = clazz.selectNodes("UML:Element.ownedElement/*[ name() = 'UML:Class' or name() = 'UML:Interface'  or name() = 'UML:Enumeration']");
        for(Node innerClassifier : nestedClasses)
        {
            String name = XMLManip.getAttributeValue(innerClassifier, "name");
            if(typeName.equals(name) == true)
            {
                String xmiid = XMLManip.getAttributeValue(innerClassifier, "xmi.id");
                
                String elementType = XMLManip.retrieveSimpleName(clazz);
                
                TypedFactoryRetriever<IClassifier> retreiver =
                        new TypedFactoryRetriever<IClassifier>();
                retVal = retreiver.createTypeAndFill(elementType, clazz);
                break;
            }
        }
        
        return retVal;
    }
    
    public void establishAssociation(Node attr, IClassifier from, IClassifier to)
    {
        try
        {
            // We need a way to determine if the association should be
            // an aggregation, composition, or just a navigable association
            INamespace space = from.getNamespace();
            IAssociation assoc = m_Factory.createAssociation2(from, to, AssociationKindEnum.AK_AGGREGATION, false, true, space);
            INavigableEnd pNavEnd = getNavigableEnd(assoc, to);
            if (pNavEnd != null)
            {
                nameElement(pNavEnd, attr);
                setDefaultValue(pNavEnd, attr);
                setMultiplicity(pNavEnd, attr);
                setVisibility(pNavEnd, attr);
            }
            //NameNavigableEnd(assoc, to, attr));
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void createAssociation(Node attr, IClassifier fromClass, IClassifier classifier)
    {
        try
        {
            establishAssociation(attr, fromClass, classifier);
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected boolean createAssociation(Node attr, IClassifier fromClass, String xmiID)
    {
        boolean isCreated = false;
        try
        {
            if (m_Locator != null)
            {
                //            String query = "id( '";
                //            query += xmiID;
                //            query += "')";
                //            IElement element = m_Locator.findSingleElementByQuery(fromClass, query);
                
                IElement element = m_Locator.findElementByID(fromClass, xmiID);
                if (element != null)
                {
                    IClassifier classifier = (element instanceof IClassifier) ? (IClassifier) element : null;
                    if (classifier != null)
                    {
                        establishAssociation(attr, fromClass, classifier);
                        isCreated = true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return isCreated;
    }
    
    protected boolean isTypeAllowedInAssociation(Node pAttrNode, String typeName)
    {
        boolean isAllowed = false;
        try
        {
            // If I do not have an attribute I can not check if the type is a
            // data type or not.  So, error on the side of caution and do not
            // allow the tyep in an association.
            if (pAttrNode != null)
            {
                // I know that we are currently only adding source file artifacts
                // to the class node.  Since pAttrNode is an attribute I can make
                // the assumption that the parent is the class element.
                Node pParent = pAttrNode.getParent();
                String fileName = retrieveSourceFile(pParent);
                
                if ((m_LanguageManager != null) && (fileName != null && fileName.length() > 0))
                {
                    ILanguage pLanguage = m_LanguageManager.getLanguageForFile(fileName);
                    if (pLanguage != null)
                    {
                        // First check if the type is been specified as a data type
                        // either a primitive or user defined data type.
                        boolean isAssociation = pLanguage.isDataType(typeName);
                        if (isAssociation == false)
                        {
                            ArrayList < ETPairT < Node, String > > symbolList = m_SymbolTable.get(typeName);
                            // 103234 in case of RE part of existing project, search type in entire project
                            // rather than the symbol table created for this RE session
                            if ((symbolList != null) && (symbolList.size() > 0) ||
                                    m_Locator.findByNameInMembersAndImports(m_Namespace, typeName).size()>0)
                            {
                                isAllowed = true;
                            }                          
                        }
                    }
                }
            }
        }
        
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
            isAllowed = false;
        }
        return isAllowed;
    }
    
    /**
     * Checks if any attributes are generic collection types.  If they are
     * generic collection types, the collection is removed and the type
     * of the attribute will be changed to the collections parameter.  The
     * attribute will have a multiplicity of [0..*]
     *
     * @param param The XML data for the class.
     * @param clazzObj The UML metamodel object.
     */
    protected void analyzeParameterForCollections(Node param,
            IClassifier clazzObj)
    {
        try
        {
            // Since we are reverse engineering, there will only be one language
            ILanguage lang = clazzObj.getLanguages().get(0);
            if ((param != null) && (lang != null))
            {
                Node derivation = isDerivationPresent(param, false);
                
                if (derivation != null)
                {
                    convertCollection(derivation, param, lang);
                }
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    /**
     * Checks if any attributes are generic collection types.  If they are
     * generic collection types, the collection is removed and the type
     * of the attribute will be changed to the collections parameter.  The
     * attribute will have a multiplicity of [0..*]
     *
     * @param clazz The XML data for the class.
     * @param clazzObj The UML metamodel object.
     */
    protected void analyzeAttributesForCollections(Node clazz,
            IClassifier clazzObj)
    {
        try
        {
            // Get all of this Classes attributes and resolve their types...
            List attrs = clazz.selectNodes(
                    "./UML:Element.ownedElement/UML:Attribute");
            
            // Since we are reverse engineering, there will only be one language
            ILanguage lang = clazzObj.getLanguages().get(0);
            if ((attrs != null) && (lang != null))
            {
                int numAttrs = attrs.size();
                if (numAttrs > 0)
                {
                    for (int x = 0; x < numAttrs; x++)
                    {
                        Node attr = (Node) attrs.get(x);
                        if (attr != null)
                        {
                            // Do not remove the derivation, unless the type is
                            // a collection.
                            Node derivation = isDerivationPresent(attr, false);
                            
                            if (derivation != null)
                            {
                                convertCollection(derivation, attr, lang);
                            }
                        }
                    }
                }
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    /**
     * Converts an attribute of a collection type, to have a type that is
     * contained by the collection.  There are two type the simple type
     * where the contained type simply a type.  The muli-diminsional type, where
     * the contained type is another collection.
     *
     * @param type The attributes type.
     * @param attr The attribute to modify
     * @param lang The language to use when determining a type is a collection.
     * @return true if a collection was found, false if a collection was not
     *         found.
     */
    protected boolean convertCollection(Node type, Node attr, ILanguage lang)
    {
        boolean retVal = false;
        
        String name = XMLManip.getAttributeValue(type, "name");
        
        // First try to find the fully qualified name of the type.  The fully
        // qualified type name is needed to make sure that we compare the
        // correct types.
        String fullName = "";
        ETList < INamedElement > spNamedElements = m_Locator.findByNameInMembersAndImports(m_Project, name);
        if ((spNamedElements != null) && (spNamedElements.size() > 0))
        {
            int numNamedElements = spNamedElements.size();
            for (int j = 0; j < numNamedElements; j++)
            {
                INamedElement spNamedElement = (INamedElement) spNamedElements.get(j);
                if (spNamedElement instanceof IClassifier)
                {
                    fullName =  spNamedElement.getFullyQualifiedName(false);
                    fullName += "::" + name;
                    break;
                }
            }
        }
        else
        {
            fullName = name;
        }
        
        // TODO: Need to search qualified name instead
        if(lang.isCollectionType(fullName) == true)
        {
            List params = type.selectNodes("./DerivationParameter");
            if(params.size() == 1)
            {
                setDefaultRange(attr, fullName);
                
                // Currently the Data is setup so that if one of the parameters
                // has derivation, then it is under the parent derivation not
                // the parameter.  It should be under the parameter.  However,
                // since we only have 1 parameter this is not a big deal at this
                // time.
                
                Node derivation = isDerivationPresent(type, false);
                if(derivation != null)
                {
                    retVal = convertCollection(derivation, attr, lang);
                    
                    if(retVal == false)
                    {
                        
                        
                        Element tokenDesc = (Element) attr.selectSingleNode("./TokenDescriptors");
                        derivation.detach();
                        tokenDesc.add(derivation);
                    }
                }
                else
                {
                    Node param = (Node) params.get(0);
                    String typeName = XMLManip.getAttributeValue(param, "value");
                    
                    XMLManip.setAttributeValue(attr, "type", typeName);
                }
                
                //                setDefaultRange(attr, fullName);
                
                // since we have moved the type directly into the attribute, we
                // need to remove the derivation declaration.
                type.detach();
            }
            
            retVal = true;
        }
        
        return retVal;
    }
    
    protected void setDefaultRange(Node owner, String collectionType)
    {
        
        if(owner != null)
        {
            Node structureNode = XMLManip.ensureNodeExists(owner,
                    "UML:TypedElement.multiplicity",
                    "UML:TypedElement.multiplicity");
            
            if(structureNode != null)
            {
                Node multiplictyNode = XMLManip.ensureNodeExists(structureNode,
                        "UML:Multiplicity",
                        "UML:Multiplicity");
                if(multiplictyNode != null)
                {
                    Node rangeNode = XMLManip.ensureNodeExists(multiplictyNode,
                            "UML:Multiplicity.range",
                            "UML:Multiplicity.range");
                    if(rangeNode instanceof Element)
                    {
                        Element rangeElement = (Element)rangeNode;
                        Node dataNode = XMLManip.createElement(rangeElement,
                                "UML:MultiplicityRange");
                        
                        if(dataNode != null)
                        {
                            XMLManip.setAttributeValue(dataNode, "lower", "0");
                            XMLManip.setAttributeValue(dataNode, "upper", "*");
                            XMLManip.setAttributeValue(dataNode, "collectionType", collectionType);
                        }
                    }
                }
            }
        }
    }
    
    
    protected void analyzeAttributesForAssociations(Node clazz,
            IClassifier clazzObj,
            INamespace classSpace)
    {
        supervisor.log(ITaskSupervisor.VERBOSE, INDENT + INDENT +
                " analyzing attribute for associations");
        
        try
        {
            // Get all of this Classes attributes and resolve their types...
            List attrs = clazz.selectNodes(
                    "./UML:Element.ownedElement/UML:Attribute");
            
            if (attrs != null)
            {
                int numAttrs = attrs.size();
                if (numAttrs > 0)
                {
                    String classID = clazzObj.getXMIID();
                    for (int x = 0; x < numAttrs; x++)
                    {
                        Node attr = (Node) attrs.get(x);
                        if (attr != null)
                        {
                            Node spDerivationElement =
                                    isDerivationPresent(attr, true);
                            
                            if (spDerivationElement != null)
                            {
                                IClassifier spDerivationClassifier =
                                        ensureDerivation(
                                        spDerivationElement,
                                        clazz, clazzObj, classSpace);
                                
                                if (spDerivationClassifier != null)
                                {
                                    establishAssociation(
                                            attr, clazzObj, spDerivationClassifier);
                                }
                            }
                            
                            else
                            {
                                if (!aliasedTypeAttribute(
                                        attr, clazz, clazzObj, classSpace))
                                {
                                    // This flag will be used to remove this
                                    // attribute node once we know that the
                                    // attribute has been fully realized
                                    // in an association
                                    
                                    String typeName =
                                            XMLManip.getAttributeValue(
                                            attr, "type"); // NOI18N
                                    
                                    ETPairT<Boolean, String> dec =
                                            getUndecoratedName(typeName);
                                    
                                    String actualType = dec.getParamTwo();
                                    
                                    if (actualType != null &&
                                            actualType.length() > 0 &&
                                            isTypeAllowedInAssociation(
                                            attr, actualType))
                                    {
                                        establishAssociation(
                                                attr, clazz, clazzObj,
                                                classSpace, typeName);
                                    }
                                    
                                    else if (actualType != null &&
                                            actualType.length() > 0)
                                    {
                                        // Need to scrub this Attribute node,
                                        // giving it a proper ID, etc.
                                        
                                        processComment(attr);
                                        scrubTypedElement(
                                                attr, clazz, classSpace,
                                                classID, typeName);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void injectIntoNamespace(INamespace space)
    {
        supervisor.log(ITaskSupervisor.TERSE,
                INDENT + " injecting into namespace " +
                space.getFullyQualifiedName(true));
        
        try
        {
            IVersionableElement ver = space;
            Node spaceNode = ver.getNode();
            Node element = XMLManip.ensureNodeExists(
                    spaceNode,
                    "UML:Element.ownedElement",
                    "UML:Element.ownedElement");
            
            if (element != null)
            {
                Element docElement = m_FragDocument.getRootElement();
                
                if (docElement != null)
                {
                    injectElementsIntoNamespace(element, docElement);
                }
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    /**
     *
     * Runs through all the class symbols on our symbol table and
     * the attribute type names and tries to resolve the types there
     * before have to search the DOM.
     *
     * @return HRESULT
     *
     */
    protected void performPreAttributeTypeResolution()
    {
        supervisor.log(ITaskSupervisor.SUMMARY);
        supervisor.log(ITaskSupervisor.SUMMARY,
                getLocalMsg("IDS_RESOLVING_ATTR_TYPES_SIZE",
                new Integer(m_Types.size())));
        
        try
        {
            Iterator<Map.Entry<String, UnresolvedType>> iter =
                    m_Types.entrySet().iterator();
            
            if (!supervisor.start(m_Types.size()))
                onCancelled();
            
            for (; iter.hasNext() && !m_Cancelled;)
            {
                // If there are multiple unknown types of the same name in the list,
                // then we can't make any assumptions about ID at this pointif()
                
                Map.Entry<String, UnresolvedType> entry = iter.next();
                UnresolvedType type = entry.getValue();
                
                if (!supervisor.proceed(1))
                    onCancelled();
                
                if (type.getNumberOfTypes() == 1)
                {
                    String attrTypeName = entry.getKey();
                    
                    String id = "";
                    
                    // We checking to make sure that we only have one symbol on the symbol table
                    // of a certain name. That's the only time we can feel safe assigning the id
                    // of that symbol to the unknown type.
                    ArrayList<ETPairT<Node, String>> symbols =
                            m_SymbolTable.get(attrTypeName);
                    
                    if ((symbols != null) && (symbols.size() == 1))
                    {
                        // Put the id of the symbol in this attribute entry;
                        id = symbols.get(0).getParamTwo();
                    }
                    
                    type.setXMIID(id);
                    
                    //		    String id = m_SymbolTable.get(attrTypeName).getParamTwo();
                    //		    if (id != null)
                    //                      entry.setValue(id);
                }
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void addToUnresolvedTypes(String typeName)
    {
        addToUnresolvedTypes(typeName, "");
    }
    
    protected void addToUnresolvedTypes(final String typeName, final String xmiID)
    {
        UnresolvedType type = m_Types.get(typeName);
        
        if (type == null)
        {
            // We do not know the xmi.id of the attribute type yet
            m_Types.put(typeName, new UnresolvedType(xmiID));
        }
        else
        {
            if ((xmiID.length() > 0) && (type.getNumberOfTypes() == 1))
            {
                type.setXMIID(xmiID);
            }
            else
            {
                type.incrementTypes();
            }
        }
    }
    protected void analyzeAttributeTypes(Node node)
    {
        supervisor.log(ITaskSupervisor.TERSE,
                INDENT + " analyzing attribute types");
        
        try
        {
            analyzeTypes(node, "./UML:Element.ownedElement/UML:Attribute/@type");
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void analyzeOperationTypes(Node node)
    {
        supervisor.log(ITaskSupervisor.TERSE,
                INDENT + " analyzing operation types");
        
        try
        {
            analyzeTypes(node, "./UML:Element.ownedElement/UML:Operation/UML:Element.ownedElement/UML:Parameter/@type");
            analyzeTypes(node, "./UML:Element.ownedElement/UML:Operation/UML:Element.ownedElement/UML:Exception/@name");
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void analyzeTypes(Node node, String xpath)
    {
        try
        {
            List typeNodes = node.selectNodes(xpath);
            if (typeNodes != null)
            {
                int count = typeNodes.size();
                for (int x = 0; x < count; x++)
                {
                    Node nodeValue = (Node) typeNodes.get(x);
                    Attribute attr = nodeValue instanceof Attribute ? (Attribute) nodeValue : null;
                    if (attr != null)
                    {
                        supervisor.log(ITaskSupervisor.VERBOSE,INDENT + INDENT
                                + "attribute " + attr.getName(), false);
                        
                        String typeName = attr.getValue();
                        
                        supervisor.log(ITaskSupervisor.VERBOSE,
                                INDENT + INDENT + "; type=" + attr.getName());
                        
                        addToUnresolvedTypes(typeName);
                        
                        // *********************************************************************
                        // This is a HACK.  In enterprise we will have make RetrieveType alot
                        // smarter.
                        // Problem: Since Integrate copies all the fragment document before
                        //          processing the symbols we must make sure that the package
                        //          structure is established before we actually integrate the
                        //          data.
                        //
                        //          Now if the type is a <Outer Class>::<Inner Class>
                        //          EstablishPackageStructure will create a package for the
                        //          outter class.  To solve this problem we will have to be
                        //          smarter about dependencies.  If the outer class can be
                        //          discovered by searching the dependencies then use the class
                        //          otherwise create a package and add the datatype to the new
                        //          package.  When the new package is created it must be added
                        //          to both the fragment document and the namespace that is
                        //          being integrated into.
                        // *********************************************************************
                        establishPackageStructure(typeName, node, m_FragDocument);
                    }
                }
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void processSymbols(INamespace space)
    {
        supervisor.log(ITaskSupervisor.SUMMARY);
        supervisor.log(ITaskSupervisor.SUMMARY,
                getLocalMsg("IDS_INTEGRATING_ELEMENTS_SIZE",
                new Integer(m_SymbolTable.size())));
        
        try
        {
            // Loop through all of our known symbols and analyze their
            // attributes. If the attributes resolve in our attribute types
            // table, we know we have an association to create and the
            // attribute will be removed from the class. If the type does not
            // resolve via our attribute types table, then we need to query
            // the entire document looking for a type with that name.
            int size = getSymbolTableTotalSize();
            
            // start next subtask: integrate elements
            if (!supervisor.start(size))
                onCancelled();
            
            //         adjustProgress(size);
            
            String analyzeDependenciesMsg =
                    REIntegrationMessages.getString("IDS_ANALYZE_DEPENDENCIES");
            String analyzeAttributesMsg =
                    REIntegrationMessages.getString("IDS_ANALYZE_ATTRIBUTES");
            String analyzeOperationsMsg =
                    REIntegrationMessages.getString("IDS_ANALYZE_OPERATIONS");
            String analyzeGeneralizationsMsg =
                    REIntegrationMessages.getString("IDS_ANALYZE_GENERALIZATIONS");
            String analyzeInterfacesMsg =
                    REIntegrationMessages.getString("IDS_ANALYZE_INTERFACES");
            
            int index = 1;
            Iterator<String> keyValues = m_SymbolTable.keySet().iterator();
            Integer symbolTableSize = new Integer(size);
            
            TypedFactoryRetriever<IClassifier> retreiver =
                    new TypedFactoryRetriever<IClassifier>();
            
            while (keyValues.hasNext() && (!m_Cancelled))
            {
                // ++index;
                
                String key = keyValues.next();
                String buffer = null;
                String numFileBuffer = null;
                
                if (!supervisor.proceed(1))
                    onCancelled();
                
                supervisor.log(ITaskSupervisor.VERBOSE, INDENT + INDENT + key);
                
                ArrayList<ETPairT<Node, String>> entry = m_SymbolTable.get(key);
                
                if (entry != null)
                {
                    for (Iterator<ETPairT<Node, String>> iter = entry.iterator();
                    (iter.hasNext() == true) && !m_Cancelled;)
                    {
                        Object[] params = new Object[]
                        {new Integer(index), symbolTableSize};
                        
                        String groupMessage = REIntegrationMessages.getString(
                                "IDS_INTEGRATING", params);
                        
                        supervisor.log(ITaskSupervisor.TERSE,
                                INDENT + groupMessage);
                        
                        ++index;
                        
                        ETPairT<Node, String> curEntry = iter.next();
                        Node clazz = curEntry.getParamOne();
                        
                        supervisor.log(ITaskSupervisor.VERBOSE, INDENT + INDENT
                                + "paramOne=" + curEntry.getParamOne(), false);
                        
                        supervisor.log(ITaskSupervisor.VERBOSE,
                                "; paramTwo=" + curEntry.getParamTwo());
                        
                        try
                        {
                            // Make sure the class has an xmi id...
                            establishXMIID(clazz);
                            // YET ANOTHER QUESTION
                            // Check AliasedType first
                            if ("AliasedType".equals(
                                    XMLManip.retrieveSimpleName(clazz)))
                            {
                                checkAliasedType(clazz);
                                continue;
                            }
                            
                            //                     // Check Enumeration
                            //                     if ("Enumeration".equals(XMLManip.retrieveSimpleName(clazz)))
                            //                     {
                            //                        analyzeEnumeration(clazz);
                            //                        if (m_ProgDiag != null)
                            //                           int pos = m_ProgDiag.increment();
                            //
                            //                        //continue;
                            //                     }
                            
                            // Regular class
                            IClassifier clazzObj =
                                    retreiver.createTypeAndFill(
                                    XMLManip.retrieveSimpleName(clazz), clazz);
                            
                            if (clazzObj != null)
                            {
                                supervisor.log(ITaskSupervisor.VERBOSE,
                                        INDENT + INDENT + clazzObj.getName());
                                
                                INamespace classSpace = clazzObj.getNamespace();
                                
                                // Check Enumeration
                                if ("Enumeration".equals(
                                        XMLManip.retrieveSimpleName(clazz)))
                                {
                                    analyzeEnumeration(
                                            clazz, clazzObj, classSpace);
                                    //continue;
                                }
                                
                                supervisor.log(ITaskSupervisor.VERBOSE,
                                        INDENT + INDENT + analyzeDependenciesMsg);
                                
                                analyzeForDependencies(
                                        clazz, clazzObj, classSpace);
                                
                                supervisor.log(ITaskSupervisor.VERBOSE,
                                        INDENT + INDENT + analyzeAttributesMsg);
                                
                                analyzeAttributesForCollections(clazz, clazzObj);
                                
                                analyzeAttributesForAssociations(
                                        clazz, clazzObj, classSpace);
                                
                                supervisor.log(ITaskSupervisor.VERBOSE,
                                        INDENT + INDENT + analyzeOperationsMsg);
                                
                                scrubOperations(clazz, clazzObj, classSpace);
                                
                                supervisor.log(ITaskSupervisor.VERBOSE,
                                        INDENT + INDENT + analyzeGeneralizationsMsg);
                                
                                analyzeForGeneralizations(
                                        clazz, clazzObj, classSpace);
                                
                                supervisor.log(ITaskSupervisor.VERBOSE,
                                        INDENT + INDENT + analyzeInterfacesMsg);
                                
                                analyzeForInterfaces(
                                        clazz, clazzObj, classSpace);
                                
                                //AnalyzeForDerivations( clazz, clazzObj, classSpace ));
                                handleReplacementIssues(clazzObj);
                                scrubStereoTypes(clazz, clazzObj);
                            }
                            
                            // removeTokenDescriptors(clazz, true);
                        }
                        
                        catch (Exception e)
                        {
                            sendExceptionMessage(e);
                        }
                    }
                }
            }
            // 78782, 87773, 87836 process redefinition, it has to be done after
            // all symbols are processed in the last pass.
            analyzeOperationRedefinition();
            
            if (m_Namespace != null)
            {
                Node node = m_Namespace.getNode();
                removeTokenDescriptors(node, true);
            }
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    
    private void analyzeOperationRedefinition()
    {
        JavaChangeHandlerUtilities util = new JavaChangeHandlerUtilities();
        IClassifier sub = null;
        IClassifier sup = null;
        
        for (Iterator it = redef.iterator(); it.hasNext();)
        {
            Object obj = it.next();
            if (obj instanceof IDependency)
            {
                sub = (IClassifier)((IDependency)obj).getClient();
                sup = (IClassifier)((IDependency)obj).getSupplier();   
                util.buildExistingRedefinitions2(sup, sub);  
            }
            else if (obj instanceof IGeneralization)
            {
                sub = (IClassifier)((IGeneralization)obj).getSpecific();
                sup = (IClassifier)((IGeneralization)obj).getGeneral();
                util.buildExistingRedefinitions2(sup, sub);  
            }        
        }
    }
    
    
    protected int getSymbolTableTotalSize()
    {
        int retVal = 0;
        
        // This is really ugly.  But what can I do since current Java does not
        // have a way for me to alias collection types.
        Collection<ArrayList<ETPairT<Node, String>>> values =
                m_SymbolTable.values();
        
        for (Iterator<ArrayList<ETPairT<Node, String>>> iter =
                values.iterator(); iter.hasNext();)
        {
            ArrayList<ETPairT<Node, String>> entry = iter.next();
            retVal += entry.size();
        }
        
        return retVal;
    }
    
    public void prepareForIntegration(INamespace space)
    {
        try
        {
            processSymbols(space);
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void addToSymbolTable(Node node)
    {
        try
        {
            String name = XMLManip.getAttributeValue(node, "name");
            String id = XMLManip.getAttributeValue(node, "xmi.id");
            
            ArrayList<ETPairT<Node, String>> entry = m_SymbolTable.get(name);
            if (entry != null)
            {
                entry.add(new ETPairT < Node, String > (node, id));
            }
            
            else
            {
                ArrayList<ETPairT<Node, String>> newEntry =
                        new ArrayList<ETPairT<Node, String>>();
                
                newEntry.add(new ETPairT<Node, String>(node, id));
                m_SymbolTable.put(name, newEntry);
            }
            
            //  m_SymbolTable.put(name, new ETPairT < Node, String > (node, id));
        }
        
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void clearSymbolTable()
    {
        // This is ugly but I can not do anything about it until Java adds a
        // aliasing mechanism for Generics.
        Collection < ArrayList < ETPairT < Node, String > > > values = m_SymbolTable.values();
        for (Iterator < ArrayList < ETPairT < Node, String > > > iter = values.iterator(); iter.hasNext();)
        {
            ArrayList < ETPairT < Node, String > > curValue = iter.next();
            curValue.clear();
        }
        
        m_SymbolTable.clear();
    }
    
    /**
     * Removed all references of a specific node from the symbol table.
     *
     * @param node The node to be removed.
     */
    protected void removeFromSymbolTable(Node node)
    {
        try
        {
            String name = XMLManip.getAttributeValue(node, "name");
            String id = XMLManip.getAttributeValue(node, "xmi.id");
            
            ArrayList < ETPairT < Node, String > > entry = m_SymbolTable.get(name);
            if (entry != null)
            {
                for (Iterator < ETPairT < Node, String > > iter = entry.iterator(); iter.hasNext();)
                {
                    ETPairT < Node, String > curEntry = iter.next();
                    if (id.equals(curEntry.getParamTwo()) == true)
                    {
                        iter.remove();
                    }
                }
                
                if (entry.size() == 0)
                {
                    m_SymbolTable.remove(name);
                }
            }
            
            //         if (tempPair != null && id != null && id.equals(tempPair.getParamTwo()))
            //         {
            //            m_SymbolTable.remove(name);
            //         }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void addToDocument(Node node)
    {
        try
        {
            establishXMIID(node);
            
            Element root = m_FragDocument.getRootElement();
            if (root != null)
                XMLManip.insertNode(root, node, 0);
            
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void prepareClass(Node classNode)
    {
        try
        {
            // Analyze the attribute types before putting this node
            // on the rest of the tree to help keep the query size
            // smaller
            //analyzeAttributeTypes(classNode);
            //analyzeOperationTypes(classNode);
            if (m_ContextStack.size() > 0)
            {
                Node packageValue = m_ContextStack.peek().getPackage();
                if (packageValue != null)
                {
                    establishOwnership(packageValue, classNode, false);
                }
                else
                {
                    addToDocument(classNode);
                }
            }
            else
            {
                addToDocument(classNode);
            }
            processNestedClass(classNode);
            processTemplateParameters(classNode);
            analyzeGeneralizationTypes(classNode);
            analyzeInterfaceTypes(classNode);
            processComment(classNode);
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
    }
    
    public void establishXMIID(Node node)
    {
        try
        {
            XMLManip manip;
            String curID = XMLManip.getAttributeValue(node, "xmi.id");
            if (curID == null || curID.length() == 0)
            {
                String id = XMLManip.retrieveDCEID();
                if (id != null && id.length() > 0)
                {
                    XMLManip.setAttributeValue(node, "xmi.id", id);
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    /**
     *
     * Adds the child node to the parent node, setting up the appropriate
     * xml elements per the UML dtd
     *
     * @param parent[in] The parent node
     * @param child[in] The child node
     * @param isTemp[in] - true if the element should not be assigned an XMI id, else
     *                   - false ( the default ) if it should
     */
    protected void establishOwnership(Node parent, Node child, boolean isTemp)
    {
        establishOwnership(parent, child, isTemp, "UML:Element.ownedElement");
    }
    
    /**
     *
     * Adds the child node to the parent node, setting up the appropriate
     * xml elements per the UML dtd
     *
     * @param parent[in] The parent node
     * @param child[in] The child node
     * @param isTemp[in] - true if the element should not be assigned an XMI id, else
     *                   - false ( the default ) if it should
     * @param owningElementName[in] The actual name of the element that will actual own,
     *                              in an xml sense, the child node. By default, this is
     *                              "UML:Element.ownedElement"
     */
    protected void establishOwnership(Node parent, Node child, boolean isTemp, final String owningElementName)
    {
        try
        {
            //Node element = XMLManip.ensureNodeExists(parent, "UML:Element.ownedElement", "UML:Element.ownedElement");
            
            Node element = XMLManip.ensureNodeExists(parent, owningElementName, owningElementName);
            if (element != null)
            {
                // No need to establish XMI ids if the child node is being place
                // on the parent node temporarily. See the Dependency processing
                // for reasons why this is so.
                if (!isTemp)
                {
                    establishXMIID(child);
                    // Get the xmi.id of the parent. It better have one
                    // by now...
                    String name = XMLManip.getAttributeValue(parent, "name");
                    String parentID = XMLManip.getAttributeValue(parent, "xmi.id");
                    XMLManip.setAttributeValue(child, "owner", parentID);
                }
                
                XMLManip.insertNode((Element) element, child, 0);
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    public void establishPackageOwnership(Node packageNode, String fullName)
    {
        try
        {
            String parentName = null;
            String curPackageName = fullName;
            int pos = fullName.lastIndexOf("::");
            if (pos != -1)
            {
                parentName = fullName.substring(0, pos);
                curPackageName = fullName.substring(pos + 2); // Step passed the '::'
                XMLManip.setAttributeValue(packageNode, "name", curPackageName);
            }
            if (parentName != null && parentName.length() > 0)
            {
                Node tempNode = m_Packages.get(parentName);
                if (tempNode != null)
                {
                    establishOwnership(tempNode, packageNode, false);
                }
            }
            else
            {
                // No owner, so establish the XMI id and move on
                addToDocument(packageNode);
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void addPackage(Node node, String fullName)
    {
        try
        {
            Node nodeValue = m_Packages.get(fullName);
            if (nodeValue == null)
            {
                establishXMIID(node);
                establishPackageOwnership(node, fullName);
                m_Packages.put(fullName, node);
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected void establishPackageStructure(String name, Node node, Document document)
    {
        try
        {
            // If this is an inner class, we do not want to create the package
            // structure.  The reason why we do not want to crete the package
            // structure, is because the name before the :: is the outer class.
            String fullName = resolveInnerClassName(name, node);
            if(fullName.equals(name) == true)
            {
                ETList < String > tokens = StringUtilities.splitOnDelimiter(name, "::");
                String fullyQualified = null;
                for (int i = 0, count = tokens.size() - 1; i < count; ++i)
                {
                    String token = tokens.get(i);
                    // Keep track of the fully qualified name as we move through
                    // the tokens
                    if (fullyQualified != null && fullyQualified.length() > 0)
                    {
                        fullyQualified += "::" + token;
                    }
                    else
                    {
                        fullyQualified = token;
                    }
                    //Packages::iterator iter = m_Packages.find( token );
                    Node nodeValue = m_Packages.get(fullyQualified);
                    if (nodeValue == null)
                    {
                        // We didn't find this package, so create a
                        // DOM node that will represent the package until we
                        // find one. DON'T add the package that matches
                        // the passed in name.
                        if (!fullyQualified.equals(name))
                        {
                            // Before we create a package, let's make sure that the current token is not a class
                            // rather than a package
                            
                            if (m_SymbolTable.get(token) == null)
                            {
                                
                                Node newNode = XMLManip.createElement(document, "UML:Package");
                                if (newNode != null)
                                {
                                    XMLManip.setAttributeValue(newNode, "name", token);
                                    addPackage(newNode, fullyQualified);
                                }
                            }
                            else
                            {
                                break;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
    }
    
    protected Node preparePackage(Node node)
    {
        Node existingPackage = null;
        try
        {
            String name = XMLManip.getAttributeValue(node, "name");
            if (name != null)
            {
                Node pack = m_Packages.get(name);
                
                if (pack == null)
                {
                    establishPackageStructure(name, node, m_FragDocument);
                    addPackage(node, name);
                    pack = node;
                }
                if (pack != null)
                {
                    existingPackage = pack;
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return existingPackage;
    }
    
    public void removeTokenDescriptors(Node node, boolean all)
    {
        try
        {
            List descriptors = null;
            if (all)
            {
                // descriptors = node.selectNodes(".//TokenDescriptors");
                descriptors = node.selectNodes(".//*[ name(.) = 'TokenDescriptors' or name(.) = 'UML:ResDeps']");
            }
            else
            {
                descriptors = node.selectNodes("./TokenDescriptors/TDescriptor[not( @type='Comment' or @type='Class Dependency')]");
            }
            if (descriptors != null)
            {
                for (int i = 0, count = descriptors.size(); i < count; ++i)
                {
                    Node n = (Node) descriptors.get(i);
                    n.detach();
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected Node scrubData(IParserData data, boolean removeAll)
    {
        Node node = null;
        try
        {
            Node dataNode = data.getEventData();
            if (dataNode != null)
            {
                removeTokenDescriptors(dataNode, removeAll);
                cleanXMLAttributes(dataNode);
                node = dataNode;
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return node;
    }
    
    protected IUMLParser connectToParser()
    {
        IUMLParser pVal = null;
        try
        {
            // In order to retrieve the correct dispatcher I must first retrieve the faciltiy manager
            // from the core product.  There will only be one facility factory on the core product.
            // Therefore, every one will be using the same dispatcher.
            IFacilityManager pManager = null;
            ICoreProduct pProduct = ProductRetriever.retrieveProduct();
            ProductRetriever retriever;
            if (pProduct != null)
            {
                pManager = pProduct.getFacilityManager();
                if (pManager != null)
                {
                    IFacility pFacility = pManager.retrieveFacility("Parsing.UMLParser");
                    IUMLParser pParser = pFacility instanceof IUMLParser ? (IUMLParser) pFacility : null;
                    if (pParser != null)
                    {
                        m_Dispatcher = pParser.getUMLParserDispatcher();
                        if (m_Dispatcher != null)
                        {
                            m_Dispatcher.registerForUMLParserEvents(this, " ");
                            m_Integrator = this;
                        }
                        pVal = pParser;
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return pVal;
    }
    
    public String resolveTypeNameFromUMLName(String fullName)
    {
        
        String retVal = fullName;
        int pos = fullName.lastIndexOf("::");
        if (pos != -1)
        {
            retVal = fullName.substring(pos + 2);
        }
        return retVal;
    }
    
    protected String resolvePackageFromUMLName(String fullName)
    {
        String retVal = null;
        int pos = fullName.lastIndexOf("::");
        if (pos != -1)
        {
            retVal = fullName.substring(0, pos);
        }
        return retVal;
    }
    
    protected Node retrievePackage(String fullyQualifiedName, Document pDoc, boolean bNameIsOnlyPackage)
    {
        Node pPackage = null;
        String packageName = bNameIsOnlyPackage ? fullyQualifiedName : resolvePackageFromUMLName(fullyQualifiedName);
        if (packageName != null)
        {
            Node node = m_Packages.get(packageName);
            if (node != null)
            {
                pPackage = node;
            }
            else
            {
                // If the package was not found in the m_Packages collection call
                // EstablishPackageStructure to build the package structure.  Then
                // retrieve teh package from the m_Packages collection.
                establishPackageStructure(fullyQualifiedName, null, pDoc);
                node = m_Packages.get(packageName);
                if (node != null)
                {
                    pPackage = node;
                }
            }
        }
        else if (m_Project != null)
        {
            pPackage = m_Project.getNode();
        }
        
        return pPackage;
    }
    
    
    protected boolean saveProject(INamespace pSpace)
    {
        boolean success = false;
        
        if (pSpace == null || m_Project == null || m_Project == null)
            return false;
        
        boolean prefVal = NbPreferences.forModule(UMLParsingIntegrator.class).getBoolean("UML_Prompt_to_Save_Project", false);
        
        
        if (!m_Project.isDirty() || ! prefVal)
            return true;
        
        // prompt user to save the target UML project
        
        Object result = SaveNotifier.getDefault().displayNotifier(
                NbBundle.getMessage(UMLParsingIntegrator.class,
                "MSG_DialogTitle_AuthorizeUMLProjectSave"), // NOI18N
                NbBundle.getMessage(UMLParsingIntegrator.class,
                "MSG_UMLProject"), // NOI18N
                m_Project.getName());
        
        if (result == SaveNotifier.SAVE_ALWAYS_OPTION)
        {
            success = true;
            NbPreferences.forModule(UMLParsingIntegrator.class).putBoolean("UML_Prompt_to_Save_Project", false); // NOI18N
        }
        
        else if (result == NotifyDescriptor.OK_OPTION)
            success = true;
        
        else // cancel or closed (x button)
        {
            success = false;
            onCancelled();
        }
        
        return success;
    }
    
    protected INavigableEnd getNavigableEnd(IAssociation assoc, IClassifier to)
    {
        INavigableEnd pVal = null;
        ETList < IAssociationEnd > pEnds = assoc.getEnds();
        if (pEnds != null)
        {
            int count = pEnds.size();
            for (int index = 0;(index < count) && (pVal == null); index++)
            {
                IAssociationEnd pItem = (IAssociationEnd) pEnds.get(index);
                if (pItem != null)
                {
                    INavigableEnd pNavEnd = (pItem instanceof INavigableEnd) ? (INavigableEnd) pItem : null;
                    if (pNavEnd != null)
                    {
                        boolean isSame = pItem.isSameParticipant(to);
                        if (isSame == true)
                        {
                            pVal = pNavEnd;
                        }
                    }
                }
            }
        }
        return pVal;
    }
    
    protected void nameElement(INamedElement pElement, Node pAttr)
    {
        try
        {
            String name = XMLManip.getAttributeValue(pAttr, "name");
            if (name != null)
            {
                pElement.setName(name);
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
    }
    
    protected void setDefaultValue(INamedElement pElement, Node pAttr)
    {
        try
        {
            String query = "UML:Attribute.default";
            Node pDefaultNode = pAttr.selectSingleNode(query);
            if (pDefaultNode != null)
            {
                pDefaultNode.detach();
                
                Node pNode = pElement.getNode();
                if (pNode != null)
                {
                    scrubExpressions(pDefaultNode);
                    ((Element) pNode).add(pDefaultNode);
                }
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
    }
    
    protected void setVisibility(INamedElement pElement, Node pAttr)
    {
        try
        {
            String visibility = XMLManip.getAttributeValue(pAttr, "visibility"); // NOI18N
            if (visibility != null)
            {
                visibility = visibility.trim();
                if (! visibility.equals(""))
                {
                    Node pNode = pElement.getNode();
                    if (pNode != null)
                    {
                        XMLManip.setAttributeValue(pNode, "visibility", visibility) ; // NOI18N
                    }
                }
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
    }
    
    protected void setMultiplicity(INamedElement pElement, Node pAttr)
    {
        try
        {
            String query = "UML:TypedElement.multiplicity";
            Node pMultNode = pAttr.selectSingleNode(query);
            if (pMultNode != null)
            {
                pMultNode.detach();
                
                Node pNode = pElement.getNode();
                if (pNode != null)
                {
                    scrubMultiplicities(pMultNode);
                    ((Element) pNode).add(pMultNode);
                }
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
    }
    
    protected void setProjectBaseDirectory()
    {
        try
        {
            if (m_Project != null)
            {
                String baseDir = m_Project.getBaseDirectory();
                if (baseDir != null)
                {
                    if(baseDir.endsWith("nbproject") == true)
                    {
                        baseDir = baseDir.substring(0, baseDir.length() - 10);
                    }
                    
                    m_BaseDirectory = baseDir;
                }
                else
                {
                    m_BaseDirectory = "";
                }
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
    }
    
    protected String getFullName(Node pNode)
    {
        String value = null;
        try
        {
            Node pCurrentNode = pNode;
            while (pCurrentNode != null)
            {
                Element temp = (Element) pCurrentNode;
                String nodeName = temp.getQualifiedName();
                if (nodeName != null)
                {
                    if (nodeName.equals("UML:Package"))
                    {
                        String name = XMLManip.getAttributeValue(pCurrentNode, "name");
                        if (name != null)
                        {
                            if (value == null)
                            {
                                value = name;
                            }
                            else
                            {
                                value = name + "::" + value;
                            }
                        }
                    }
                }
                // The temp node is use to stop the debug asserts.  I could
                // use the p pointer, but this is the same thing.
                Node tempNode = pCurrentNode.getParent();
                pCurrentNode = tempNode;
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
        return value;
    }
    
    protected boolean isAbleToWrite(Node pNode)
    {
        boolean retVal = false;
        if (pNode != null)
        {
            try
            {
                String nodeName = pNode.getName();
                IElement pClazzObj = new TypedFactoryRetriever<IElement>()
                        .createTypeAndFill(XMLManip.retrieveSimpleName(pNode), pNode);
                
                retVal = isAbleToWrite(pClazzObj);
            }
            
            catch (Exception e)
            {
                sendExceptionMessage(e);
            }
        }
        return retVal;
    }
    
    public boolean isAbleToWrite(IElement element)
    {
        boolean retVal = !isReadOnly(element);
        try
        {
            if (!retVal)
            {
                ICoreProduct pCoreProduct = ProductRetriever.retrieveProduct();
                IProduct pProduct = (pCoreProduct instanceof IProduct)
                        ? (IProduct) pCoreProduct : null;
                
                if (pProduct != null)
                {
                    ISCMIntegrator pSCMIntegrator = pProduct.getSCMIntegrator();
                    if (pSCMIntegrator != null)
                    {
                        int kind = pSCMIntegrator.getSCMStatusForElement(element);
                        if (kind == ISCMMaskKind.SMK_CONFIGURED)
                        {
                            ISCMTool pTool = pSCMIntegrator.getSCMToolByElement(element);
                            if (pTool != null)
                            {
                                IProject pProject = getProject();
                                if (pProject != null)
                                {
                                    ISCMItemFactory pFactory = (ISCMItemFactory)SCMObjectCreator.getInstanceFromRegistry("uml/scm/SCMItemFactory");//new SCMItemFactory();
                                    ISCMElementItem pSCMItem = pFactory.createElementItem(element);
                                    ISCMItemGroup pSCMGroup = (ISCMItemGroup)SCMObjectCreator.getInstanceFromRegistry("uml/scm/SCMItemGroup");//new SCMItemGroup();
                                    pSCMGroup.setTool(pTool);
                                    pSCMGroup.setProject(pProject);
                                    pSCMGroup.add(pSCMItem);
                                    pTool.executeFeature(ISCMEnums.FK_CHECK_OUT, pSCMGroup, null, true);
                                    // Now if the file associated with pElement is still read-only
                                    // then bail.
                                    retVal = !isReadOnly(element);
                                }
                            }
                        }
                        else
                        {
                            // Since we know that the file is read-only and
                            // it is either checked out, or not configured
                            // we must return false.
                            retVal = false;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
            retVal = false;
        }
        return retVal;
    }
    
    public boolean isReadOnly(IElement space)
    {
        boolean retVal = true;
        try
        {
            // If there is an associated URI then the file is versioned controlled
            // or is the child of a versioned element.  If there is no URI then
            // get the elements project and retrieve the projects file.
            String uri = space.getVersionedURI();
            String docLoc = URILocator.uriparts(uri).getParamOne();
            if (docLoc != null)
            {
                File docFile = new File(docLoc);
                if (!docFile.isAbsolute())
                {
                    IProject pProject = getProject();
                    if (pProject != null)
                    {
                        String projectFile = pProject.getFileName();
                        if (projectFile != null)
                        {
                            docLoc = PathManip.retrieveAbsolutePath(docLoc, projectFile);
                        }
                    }
                }
                retVal = isReadOnly(docLoc);
            }
            else
            {
                IProject pProject = getProject();
                if (pProject != null)
                {
                    String projectFile = pProject.getFileName();
                    retVal = isReadOnly(projectFile);
                }
            }
            
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            // I want to mark it as read-only (This is the safest option).
            retVal = true;
        }
        return retVal;
    }
    
    public boolean isReadOnly(String filename)
    {
        File file = new File(filename);
        return (file.exists() && !file.canWrite());
    }
    
    public void displayReadOnlyMsg(String projectName)
    {
        try
        {
            String msg = REIntegrationMessages.getString("IDS_READ_ONLY_MSG");
            String title = REIntegrationMessages.getString("IDS_READ_ONLY_TITLE");
            // QuestionDialog isn't yet implemented.
            IQuestionDialog pDialog = null; // new QuestionDialog();
            if (pDialog != null)
            {
                int result = pDialog.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_OK, ErrorDialogIconKind.EDIK_ICONINFORMATION, msg, SimpleQuestionDialogResultKind.SQDRK_RESULT_OK, null, title).getResult();
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
    }
    
    public IProject getProject()
    {
        return m_Project;
    }
    
    public void removeNonNavigableAssoc(IElement element)
    {
        try
        {
            IClassifier pClassifier = (element instanceof IClassifier) ? (IClassifier) element : null;
            if (pClassifier != null)
            {
                ETList < IAssociationEnd > pEnds = pClassifier.getAssociationEnds();
                if (pEnds != null)
                {
                    int max = pEnds.size();
                    for (int index = 0; index < max; index++)
                    {
                        IAssociationEnd pEnd = pEnds.get(index);
                        if (pEnd != null)
                        {
                            boolean isNavigable = pEnd.getIsNavigable();
                            if (isNavigable == false)
                            {
                                IAssociation pAssoc = pEnd.getAssociation();
                                if (pAssoc != null)
                                    pAssoc.delete();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
    }
    
    protected void removeClientDependencies(IElement pElement)
    {
        try
        {
            INamedElement pNamedElement = (pElement instanceof INamedElement) ? (INamedElement) pElement : null;
            if (pNamedElement != null)
            {
                ETList < IDependency > pDependencies = pNamedElement.getClientDependencies();
                if (pDependencies != null)
                {
                    int max = pDependencies.size();
                    for (int index = 0; index < max; index++)
                    {
                        IDependency pDep = pDependencies.get(index);
                        if(pDep!=null)
                        {
                            pDep.delete();
                        }
                        
                    }
                }
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
    }
    
    //JM: Fix for Issue#87116
    protected void removeGeneralizations(IElement pElement)
    {
        try
        {
            IClassifier pNamedElement = (pElement instanceof IClassifier) ? (IClassifier) pElement : null;
            if (pNamedElement != null)
            {
                ETList < IGeneralization > pDependencies = pNamedElement.getGeneralizations();
                if (pDependencies != null)
                {
                    int max = pDependencies.size();
                    for (int index = 0; index < max; index++)
                    {
                        IGeneralization pDep = pDependencies.get(index);
                        if(pDep!=null)
                        {
                            pDep.delete();
                        }
                        
                    }
                }
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
    }
    
    protected Node getElementOfType(List nodeList, String wantedNodeName)
    {
        Node pVal = null;
        try
        {
            int num = nodeList.size();
            for (int index = 0;(index < num) && (pVal == null); index++)
            {
                Node pCurNode = (Node) nodeList.get(index);
                if (pCurNode != null)
                {
                    String curNodeName = pCurNode.getName();
                    if (curNodeName == wantedNodeName)
                    {
                        pVal = pCurNode;
                        break;
                    }
                }
                
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
        return pVal;
    }
    
    protected Node getElementOfType(ETList < Node > nodes, String wantedNodeName)
    {
        Node pVal = null;
        try
        {
            for (int i = 0; i < nodes.size(); i++)
            {
                Element el = (Element) nodes.get(i);
                String nodeName = el.getQualifiedName();
                if (nodeName.equals(wantedNodeName))
                {
                    pVal = el;
                    break;
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        return pVal;
    }
    
    protected void handleReplacementIssues(IClassifier pClazzObj)
    {
        try
        {
            String curXMIID = pClazzObj.getXMIID();
            if (curXMIID != null)
            {
                Node pReplacedNode = m_ReplacedNodes.get(curXMIID);
                if (pReplacedNode != null)
                {
                    // The operation CreateTypeAndFill on TypedFactoryRetriever will
                    // retrieve the XMLDOMNode based on the XMI ID.  Since both
                    // the pClassObj and the pReplaceClass object will have the same
                    // ID they will also share the same IXMLDOMNode instance.
                    //
                    // Therefore, remove the XMI ID from the memory map of XMI ID.
                    // When CreateTypeAndFill is call it will not find the XMI ID.
                    // pClazzObj and pReplaceClass will not share the same IXMLDOMNode
                    // instance.
                    FactoryRetriever fact = FactoryRetriever.instance();
                    fact.removeObject(curXMIID);
                    IClassifier pReplaceClass = new TypedFactoryRetriever < IClassifier > ().createTypeAndFill(XMLManip.retrieveSimpleName(pReplacedNode), pReplacedNode);
                    ETList < IOperation > pReplaceClassOps = pReplaceClass.getOperations();
                    handleOperationReplacement(pClazzObj, pReplaceClassOps);
                }
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
    }
    
    protected void handleOperationReplacement(IClassifier pClazzObj, ETList < IOperation > pReplacedOperations)
    {
        try
        {
            ETList < IOperation > pOperationList = pClazzObj.getOperations();
            if (pOperationList != null)
            {
                int max = pOperationList.size();
                for (int index = 0; index < max; index++)
                {
                    IOperation pCurrentOperation = pOperationList.get(index);
                    if (pCurrentOperation != null)
                    {
                        IOperation pReplacedOperation = locateIdenticalOperation(pReplacedOperations, pCurrentOperation);
                        if (pReplacedOperation != null)
                        {
                            String replacedXMIID = pReplacedOperation.getXMIID();
                            if (replacedXMIID.length() > 0)
                            {
                                pCurrentOperation.setXMIID(replacedXMIID);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
    }
    
    protected IOperation locateIdenticalOperation(ETList < IOperation > pOperationList, IOperation pWantedOp)
    {
        IOperation pVal = null;
        try
        {
            int max = pOperationList.size();
            for (int index = 0;(index < max) && ((pVal) == null); index++)
            {
                IOperation pCurrentOperation = pOperationList.get(index);
                if (pCurrentOperation != null)
                {
                    boolean isSameOperation = pCurrentOperation.isFormalSignatureSame(pWantedOp);
                    if (isSameOperation)
                    {
                        pVal = pCurrentOperation;
                    }
                }
            }
        }
        catch (Exception e)
        {
            // I just want to forward the error to the listener.
            sendExceptionMessage(e);
        }
        return pVal;
    }
    
    protected void scrubStereoTypes(Node pClassNode, IElement pClassObj)
    {
        try
        {
            Element element = (Element) pClassNode;
            Attribute pStereotypeAttr = element.attribute("Stereotype");
            if (pStereotypeAttr != null)
            {
                String attrValue = pStereotypeAttr.getValue();
                element.remove(pStereotypeAttr);
                if (attrValue != null)
                {
                    pClassObj.applyStereotype2(attrValue);
                }
            }
            
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
    }
    
    protected void refreshProjectTree()
    {
        try
        {
            ICoreProduct pCoreProduct = ProductRetriever.retrieveProduct();
            
            IProduct pProduct = (pCoreProduct instanceof IProduct) ? (IProduct) pCoreProduct : null;
            if (pProduct != null)
            {
                IProjectTreeControl pProjectTree = pProduct.getProjectTree();
                if (pProjectTree != null)
                {
                    pProjectTree.refresh(false);
                }
            }
        }
        catch (Exception e)
        {
            // I do not care to propergate the HRESULT to the caller.
            // However, I do want to report the error.
            sendExceptionMessage(e);
        }
    }
    
    protected Node isDerivationPresent(Node pNode, boolean remove)
    {
        Node ppDerivationElement = null;
        try
        {
            // Get derivations from TokenDescriptors
            Node spDerivationElement = pNode.selectSingleNode("./TokenDescriptors/TDerivation");
            ppDerivationElement = spDerivationElement;
            
            if(remove == true)
            {
                if (spDerivationElement != null)
                {
                    spDerivationElement.detach();
                }
            }
        }
        catch (Exception e)
        {
            ppDerivationElement = null;
        }
        return ppDerivationElement;
    }
    
    protected String buildDerivationElementStr(Node pDerivationElement)
    {
        String result = null;
        try
        {
            // Build Derivation string
            String tempStr = null;
            String sDerivation = "";
            if (pDerivationElement != null)
            {
                // Derivation name
                tempStr = XMLManip.getAttributeValue(pDerivationElement, "name");
                sDerivation += tempStr;
                // Derivation parameters
                List spDerivationParameters = pDerivationElement.selectNodes("./DerivationParameter");
                int numDerivationParameters = spDerivationParameters.size();
                
                String params = "";
                for (int y = 0; y < numDerivationParameters; y++)
                {
                    Node spParameter = (Node) spDerivationParameters.get(y);
                    tempStr = XMLManip.getAttributeValue(spParameter, "value");
                    
                    if(params.length() > 0)
                    {
                        params += ", ";
                    }
                    params += tempStr;
                }
                
                sDerivation += "<" + params + ">";
            }
            result = sDerivation;
        }
        catch (Exception e)
        {
            result = null;
        }
        return result;
    }
    
    protected IClassifier checkExistingDerivation(Node pDerivationElement,
            Node pClazz, IClassifier pClazzObj,
            INamespace pClassSpace)
    {
        IClassifier pDerivationClassifier = null;
        try
        {
            String derivationElementStr = this.buildDerivationElementStr(pDerivationElement);
            if (derivationElementStr != null)
            {
                pDerivationClassifier = m_unquieDerivations.get(derivationElementStr);
            }
            
            if(pDerivationClassifier == null)
            {
                // Basically the derivation classifier could already be present if
                // we are doing an update model from source.  So, we need to
                // search the model, not just the elements that we have processed.
                INamedElement spNamedElement = resolveType(pClazz, pClazzObj,
                        pClassSpace,
                        derivationElementStr,
                        false);
                if (spNamedElement == null)
                {
                    // lets try in the namespace of template element
                    String tempStr = XMLManip.getAttributeValue(pDerivationElement, "name");
                    INamedElement templateElement = resolveType(pClazz, pClazzObj,
                            pClassSpace,
                            tempStr,
                            true);
                    if (templateElement != null)
                    {
                        spNamedElement = resolveType(pClazz, pClazzObj,
                                templateElement.getNamespace(),
                                derivationElementStr,
                                false);
                    }
                }
                if(spNamedElement instanceof IDerivationClassifier)
                {
                    pDerivationClassifier = (IDerivationClassifier)spNamedElement;
                    m_unquieDerivations.put(derivationElementStr, pDerivationClassifier);
                }
            }
        }
        catch (Exception e)
        {
            pDerivationClassifier = null;
        }
        return pDerivationClassifier;
    }
    
    protected IClassifier ensureDerivation(Node pDerivationElement,
            Node pClazz,
            IClassifier pClazzObj,
            INamespace pClassSpace)
    {
        IClassifier ppDerivationClassifier = null;
        try
        {
            // When a template is used as the parameter of a template, then
            // the template instance, will have child template instances.
            handleChildDerivations(pDerivationElement, pClazz, pClazzObj, pClassSpace);
            
            IClassifier spDerivationClassifier = this.checkExistingDerivation(pDerivationElement, pClazz,
                    pClazzObj, pClassSpace);
            // Check for existing match =============================
            if (spDerivationClassifier != null)
            {
                ppDerivationClassifier = spDerivationClassifier;
            }
            
            if(ppDerivationClassifier == null)
            {
                // Create new derivation =================================
                String derivationElementStr = this.buildDerivationElementStr(pDerivationElement);
                if (derivationElementStr == null)
                {
                    return null;
                }
                // Find the supplier
                String supplierName = XMLManip.getAttributeValue(pDerivationElement, "name");
                IClassifier templateClassifier = null;
                ETList < INamedElement > spNamedElements = m_Locator.findByNameInMembersAndImports(m_Project, supplierName);
                if ((spNamedElements != null) && (spNamedElements.size() > 0))
                {
                    int numNamedElements = spNamedElements.size();
                    for (int j = 0; j < numNamedElements; j++)
                    {
                        INamedElement spNamedElement = (INamedElement) spNamedElements.get(j);
                        if (spNamedElement instanceof IClassifier)
                        {
                            templateClassifier = (IClassifier) spNamedElement;
                            break;
                        }
                    }
                }
                else
                {
                    INamedElement spNamedElement = resolveType(pClazz,
                            pClazzObj,
                            pClassSpace,
                            supplierName,
                            true);
                    if (spNamedElement instanceof IClassifier)
                    {
                        templateClassifier = (IClassifier) spNamedElement;
                    }
                }
                
                // Create DerivationClassifier
                ICreationFactory spCreationFactory = new CreationFactory();
                spDerivationClassifier = (IClassifier) spCreationFactory.retrieveMetaType("DerivationClassifier", null);
                //                pClassSpace.addOwnedElement(spDerivationClassifier);
                
                // The derivation classifier should live in the same package that
                // owns the template definition.  That way there would only
                // ever be one derivation classifier.
                templateClassifier.getOwningPackage().addOwnedElement(spDerivationClassifier);
                
                // Change DerivationClassifier name
                INamedElement spNamedElement = spDerivationClassifier;
                if (spNamedElement != null)
                {
                    String derivationClassifierName = supplierName;
                    List spDerivationParameterElements = pDerivationElement.selectNodes("./DerivationParameter");
                    int numDerivationParameterElements = spDerivationParameterElements.size();
                    // Add actual parameters
                    derivationClassifierName += "<";
                    for (int y = 0; y < numDerivationParameterElements; y++)
                    {
                        if (y > 0)
                            derivationClassifierName += ", ";
                        Node spParameterelement = (Node) spDerivationParameterElements.get(y);
                        String paramenterValue = XMLManip.getAttributeValue(spParameterelement, "value");
                        derivationClassifierName += paramenterValue;
                    }
                    derivationClassifierName += ">";
                    spNamedElement.setName(derivationClassifierName);
                }
                
                // Create Derivation
                if (templateClassifier != null)
                {
                    // Get actual parameters
                    List spDerivationParameterElements = pDerivationElement.selectNodes("./DerivationParameter");
                    int numDerivationParameterElements = spDerivationParameterElements.size();
                    
                    if(templateClassifier instanceof IDataType)
                    {
                        ensureDefaultTemplateParameters((IDataType)templateClassifier,
                                numDerivationParameterElements);
                    }
                    
                    IDerivation spDerivation = m_Factory.createDerivation(spDerivationClassifier, templateClassifier);
                    if (spDerivation != null)
                    {
                        // Get bindings
                        ETList < IUMLBinding > spUMLBindings = spDerivation.getBindings();
                        int numUMLBindings = spUMLBindings.size();
                        
                        // Set binding actual parameter
                        if (numUMLBindings == numDerivationParameterElements)
                        {
                            for (int y = 0; y < numDerivationParameterElements; y++)
                            {
                                // Get binding
                                IUMLBinding spUMLBinding = spUMLBindings.get(y);
                                if (spUMLBinding == null)
                                    break;
                                // Get actula parameter value
                                Node spParameterelement = (Node) spDerivationParameterElements.get(y);
                                String paramenterValue = XMLManip.getAttributeValue(spParameterelement, "value");
                                
                                spNamedElement = m_unquieDerivations.get(paramenterValue);
                                if(spNamedElement == null)
                                {
                                    spNamedElement = resolveType(pClazz,
                                            pClazzObj,
                                            pClassSpace,
                                            paramenterValue,
                                            true);
                                }
                                
                                if (spNamedElement != null)
                                {
                                    IParameterableElement spIParameterableElement = (spNamedElement instanceof IParameterableElement) ? (IParameterableElement) spNamedElement : null;
                                    spUMLBinding.setActual(spIParameterableElement);
                                }
                            }
                        }
                    }
                    // Add new derivation ====================================
                    m_unquieDerivations.put(derivationElementStr, spDerivationClassifier);
                }
                // Done
                ppDerivationClassifier = spDerivationClassifier;
            }
        }
        catch (Exception e)
        {
            ppDerivationClassifier = null;
        }
        return ppDerivationClassifier;
    }
    
    protected boolean checkAliasedType(Node pAliasedTypeElement)
    {
        boolean isAliased = false;
        
        try
        {
            String nodeName = pAliasedTypeElement.getName();
            
            if ("UML:AliasedType".equals(nodeName))
            {
                isAliased = true;
                
                Node pParent = pAliasedTypeElement.getParent();
                if (pParent != null)
                {
                    String typeDecoration = XMLManip.getAttributeValue(pAliasedTypeElement, "typeDecoration");
                    String actualType = XMLManip.getAttributeValue(pAliasedTypeElement, "actualType");
                    String aliasedName = XMLManip.getAttributeValue(pAliasedTypeElement, "aliasedName");
                    
                    StringBuffer queryStr = new StringBuffer("./UML:AliasedType[@actualType=\"");
                    queryStr.append(actualType);
                    queryStr.append("\" and @aliasedName=\"");
                    queryStr.append(aliasedName);
                    queryStr.append("\"");
                    if (typeDecoration != null)
                    {
                        queryStr.append(" and @typeDecoration=\"");
                        queryStr.append(typeDecoration);
                        queryStr.append("\"");
                    }
                    queryStr.append("]");
                    List pNodeList = pParent.selectNodes(queryStr.toString());
                    if (pNodeList != null)
                    {
                        int nodeCount = pNodeList.size();
                        for (int i = 0; i < nodeCount; i++)
                        {
                            Node pNode = (Node) pNodeList.get(i);
                            if (i == 0)
                            {
                                //xstring typeName = xstring(actualType);
                                String typeName = aliasedName;
                                if (typeDecoration != null)
                                {
                                    typeName = typeName + typeDecoration;
                                }
                                establishXMIID(pNode);
                                XMLManip.setAttributeValue(pNode, "name", typeName);
                            }
                            else
                            {
                                pNode.detach();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        return isAliased;
    }
    
    public boolean aliasedTypeAttribute(Node pTypeElement, Node pClazz, IClassifier pClazzObj, INamespace pClassSpace)
    {
        try
        {
            boolean isAliasedTypeAttribute = false;
            
            // Get type
            String typeStr = XMLManip.getAttributeValue(pTypeElement, "type");
            
            ETPairT < Boolean, String > curEntry = getUndecoratedName(typeStr);
            
            if (curEntry.getParamOne() != null)
            {
                String aliasedName = curEntry.getParamTwo();
                // Find the AliasedType
                String supplierName = null;
                IClassifier spClassifier = null;
                ETList < INamedElement > spNamedElements = m_Locator.findByNameInMembersAndImports(m_Project, typeStr);
                if (spNamedElements != null)
                {
                    int numNamedElements = spNamedElements.size();
                    for (int j = 0; j < numNamedElements; j++)
                    {
                        INamedElement spNamedElement = (INamedElement) spNamedElements.get(j);
                        if (spNamedElement instanceof IClassifier)
                        {
                            spClassifier = (IClassifier) spNamedElement;
                            // Check alias name
                            IAliasedType spAliasedType = (spClassifier instanceof IAliasedType) ? (IAliasedType) spClassifier : null;
                            if (spAliasedType != null)
                            {
                                String curAliasedName = spAliasedType.getAliasedName();
                                if (curAliasedName.equals(aliasedName))
                                {
                                    establishAssociation(pTypeElement, pClazzObj, spClassifier);
                                    XMLManip.setAttributeValue(pTypeElement, "type", null);
                                    isAliasedTypeAttribute = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return isAliasedTypeAttribute;
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
            return false;
        }
    }
    
    // AZTEC TODO: Uncomment once we have IREDialog in place
    //	protected void retrieveSettings(IREDialog reDiag)
    //	{
    //		try
    //		{
    //			// Get language
    //			String languageBSTR = reDiag.getLanguageSelected();
    //			mReplaceDocs = reDiag.getReplaceDocumentation();
    //			// For this release(5/16/2003), only for C++
    //			if (!languageBSTR.equals("C++"))
    //			{
    //					m_LanguageParserSettings = null;
    //					return ;
    //			}
    //			// Create LanguageParserSettings
    //			m_LanguageParserSettings = new LanguageParserSettings();
    //			// Get macros
    //			IStrings macros = reDiag.getMacros();
    //			if (macros != null)
    //			{
    //				int lCount = macros.size();
    //				for(int i = 0; i < lCount; i++)
    //				{
    //					String macroBSTR = (String)macros.get(i);
    //					// Get the macro name and value
    //					String macroDefination = macroBSTR, macroName, macroValue;
    //					int iPos = macroDefination.indexOf("=");
    //					if (iPos != -1)
    //					{
    //						macroName = macroDefination.substring(0, iPos);
    //						//macroName = StringUtilities::TrimWhiteSpace(macroName);
    //						macroValue = macroDefination.substring(iPos + 1);
    //						//macroValue = StringUtilities::TrimWhiteSpace(macroValue);
    //						if (macroName != null && macroValue != null)
    //						{
    //							ILanguageMacro spLanguageMacro = new LanguageMacro();
    //							spLanguageMacro.setName(macroName);
    //							spLanguageMacro.setValue(macroValue);
    //							m_LanguageParserSettings.addMacro(languageBSTR, spLanguageMacro);
    //						}
    //					}
    //				}
    //			}
    //		}
    //		catch( Exception e )
    //		{
    //			sendExceptionMessage(e);
    //		}
    //	}
    
    public void analyzeEnumeration(Node pClazz, IClassifier clazzObj, INamespace classSpace)
    {
        
        Node spLiteralsNode = null;
        
        try
        {
            XMLManip manip;
            
            // Make sure it's Enumeration
            if (XMLManip.retrieveSimpleName(pClazz).equals("Enumeration"))
            {
                // Get Enumeration id
                String enumerationID = XMLManip.getAttributeValue(pClazz, "xmi.id");
                
                // Make sure UML:Enumeration.literal exists
                //            spLiteralsNode = XMLManip.ensureNodeExists(pClazz, "UML:Enumeration.literal", "UML:Enumeration.literal");
                
                // Get document
                Document spDocument = m_Project.getDocument();
                
                List literals = pClazz.selectNodes("./UML:Enumeration.literal/UML:EnumerationLiteral");
                long numLiterals = literals.size();
                for (int y = 0; y < numLiterals; y++)
                {
                    Element curElement = (Element)literals.get(y);
                    establishXMIID(curElement);
                    XMLManip.setAttributeValue(curElement, "enumeration", enumerationID);
                    processComment(curElement);
                }
                
                scrubOperations(pClazz, clazzObj, classSpace);
                
                //            // Select Attribute nodesb
                //            //DEBUG_XML_NODE(pClazz, _T( "c:\\test.xml" ), false );
                //            // Get derivations from TokenDescriptors
                //            List spAttributes = pClazz.selectNodes("./UML:Element.ownedElement/UML:Attribute");
                //            if (spAttributes != null)
                //            {
                //
                //               // Process enumaration attribute
                //               long numAttributes = spAttributes.size();
                //               for (int y = 0; y < numAttributes; y++)
                //               {
                //                  // Get literal name
                //                  Node spAttribute;
                //                  String enumName;
                //                  spAttribute = (Node) spAttributes.get(y);
                //                  enumName = XMLManip.getAttributeValue(spAttribute, "name");
                //
                //                  // Create literal
                //                  Element spLiteralNode = XMLManip.createElement((Element)spLiteralsNode, "UML:EnumerationLiteral");
                //                  establishXMIID(spLiteralNode);
                //                  XMLManip.setAttributeValue(spLiteralNode, "name", enumName);
                //                  XMLManip.setAttributeValue(spLiteralNode, "enumeration", enumerationID);
                //
                //                   spAttribute.detach();
                //
                //               }
                //            }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
    }
    
    public void HandleDocumentation(Node childInDestinationNamespace, Node elementBeingInjected)
    {
        
        try
        {
            if (childInDestinationNamespace != null && elementBeingInjected != null)
            {
                // Only replace the documentation fields if the user is instructing us to.
                // By default, we should NOT do it.
                
                if (m_ReplaceDocs == false)
                {
                    String curDocs = getDocumentation(childInDestinationNamespace);
                    setDocumentation(elementBeingInjected, curDocs);
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
    }
    
    /**
     * Retrieves the UML documentation for the passed in node
     *
     * @param node[in]   The node to retrieve docs from
     * @param docs[out]  The docs
     *
     * @return HRESULT
     */
    
    public String getDocumentation(Node node)
    {
        String docs = "";
        
        try
        {
            
            String value = XMLManip.retrieveNodeTextValue(node, "./UML:Element.ownedElement/UML:TaggedValue[@name='documentation']/UML:TaggedValue.dataValue");
            
            if (value.length() > 0)
            {
                docs = value;
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        return docs;
    }
    
    /**
     * Sets the documentation of the node. This method is smart about needing to create the
     * documentation tagged value or not.
     *
     * @param node[in]   The node to set the UML documentation on.
     * @param docs[in]   The documentation text to set
     *
     * @return HRESULT
     */
    
    public void setDocumentation(Node node, String docs)
    {
        
        try
        {
            Node docNode = node.selectSingleNode("./UML:Element.ownedElement/UML:TaggedValue[@name='documentation']/UML:TaggedValue.dataValue");
            
            if (docNode != null)
            {
                docNode.setText(docs);
            }
            else
            {
                addDocumentation(node, docs);
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
    }
    
    public INamedElement resolveUnknownType(String typeName, INamespace space)
    {
        INamedElement namedElement = null;
        
        if (space != null && typeName != null)
        {
            namedElement = getOwnedUnknownTypeByName(space, typeName);
            
            if (namedElement == null)
            {
                
                // Ok, so there is no type found in the immediate namespace, or in any
                // class or package dependencies. Let's make one last ditched effort to
                // located the type before simply creating the unknown type
                namedElement = UMLXMLManip.resolveSingleTypeFromString(space, typeName, true);
                if (namedElement == null)
                {
                    // The type does not exist anywhere in the project, nor in reference
                    // libraries. Create the unknown type
                    
                    Node node = space.getNode();
                    //ATLASSERT( node );
                    Document doc;
                    
                    if (node != null)
                    {
                        doc = node.getDocument();
                        //ATLASSERT( doc );
                        
                        if (doc != null)
                        {
                            // The type was not found, so resolve via the unknown type mechanism
                            namedElement = UMLXMLManip.resolveUnknownType(doc, space, typeName);
                            //created = true;
                        }
                    }
                }
            }
        }
        
        return namedElement;
    }
    
    public void cacheUnknownType()
    {
        IPreferenceAccessor pPref = PreferenceAccessor.instance();
        mUnknownType = pPref.getUnknownClassifierType();
    }
    
    /**
     * Creates an unknown type at the location indicated by the fully qualified name in typeName.
     * If typeName is not fully qualified, the type will be created at the root of the Project.
     *
     * @param typeName[in]  The name of the type to create.
     * @param newType[out]  The new type
     *
     * @return HRESULT
     */
    
    /**
     * Creates an unknown type at the location indicated by the fully qualified name in typeName.
     * If typeName is not fully qualified, the type will be created at the root of the Project.
     *
     * @param typeName[in]  The name of the type to create.
     * @param newType[out]  The new type
     *
     * @return HRESULT
     */
    
    public INamedElement createUnknownTypeAtLocation(String typeName)
    {
        UMLXMLManip manip;
        INamedElement element;
        INamespace space;
        INamedElement newType = null;
        
        try
        {
            String packageName = resolvePackageFromUMLName(typeName);
            String shortTypeName = resolveTypeNameFromUMLName(typeName);
            
            Node node = m_Packages.get(packageName);
            
            if (node != null)
            {
                space = new TypedFactoryRetriever < INamespace > ().createTypeAndFill(node);
            }
            else
            {
                space = m_Project;
            }
            
            if (shortTypeName.length() == 0)
            {
                shortTypeName = typeName;
            }
            
            if ((fullyQualified(typeName) == true) && (space.equals(m_Project) == true))
            {
                // This is a case where we have a fully qualified type to a package
                // structure that has not been established, and we don't have import information
                // on. This can occur when extending a type with a fully qualified name.
                IPackage owningSpace = space.createPackageStructure(packageName);
                addPackage(owningSpace, packageName);
                newType = resolveUnknownType(shortTypeName, owningSpace);
            }
            else
            {
                newType = resolveUnknownType(shortTypeName, space);
            }
            
            if (newType != null)
            {
                addDataTypeToUnresolved(typeName, newType, false);
                
                if (shortTypeName.length() < typeName.length())
                {
                    UnresolvedType type = m_Types.get(shortTypeName);
                    
                    if (type != null)
                    {
                        String typeID = getUnknownTypeID(type);
                        
                        if (typeID.length() == 0)
                        {
                            // If the short name version of the type has not been resolved,
                            // then be sure to "resolve" it, but indicate that a class dependency
                            // check should be performed.
                            
                            addDataTypeToUnresolved(shortTypeName, newType, true);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        return newType;
    }
    
    public List getOwnedTypesByName(INamespace space, String typeName)
    {
        List nodes = null;
        
        try
        {
            
            UMLXMLManip manip;
            Node node = space.getNode();
            //ATLASSERT( node );
            
            Document doc;
            
            if (node != null)
            {
                doc = node.getDocument();
            }
            
            // We need to check to see if a type exists by the same name and of the unknown type
            // before we create. If we find one that matches the name and unknown type, simply
            // return that one.
            String shortTypeName = resolveTypeNameFromUMLName(typeName);
            String query = "./UML:Element.ownedElement/*[@name='";
            query += shortTypeName;
            query += "']";
            
            nodes = node.selectNodes(query);
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        return nodes;
    }
    
    public INamedElement getOwnedTypeByName(INamespace space, String typeName)
    {
        if (space == null || typeName == null)
            throw new IllegalArgumentException();
        INamedElement namedElement = null;
        
        try
        {
            List nodes = getOwnedTypesByName(space, typeName);
            
            if (nodes != null && nodes.size() > 0)
            {
                Node namedNode = (Node) nodes.get(0);
                namedElement = new TypedFactoryRetriever < INamedElement > ().createTypeAndFill(namedNode);
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        return namedElement;
    }
    
    public INamedElement getOwnedUnknownTypeByName(INamespace space, String typeName)
    {
        
        INamedElement namedElement = null;
        
        try
        {
            
            List nodes = getOwnedTypesByName(space, typeName);
            
            if (nodes != null)
            {
                int num = nodes.size();
                
                String unknownType = "UML:";
                unknownType += mUnknownType;
                
                for (int x = 0; x < num; x++)
                {
                    Node namedNode = (Node) nodes.get(x);
                    //ATLASSERT( namedNode );
                    
                    String elementType = namedNode.getName();
                    
                    if (elementType.equals(unknownType))
                    {
                        namedElement = new TypedFactoryRetriever < INamedElement > ().createTypeAndFill(namedNode);
                        
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        return namedElement;
    }
    
    /**
     * Finds a type in our symbol table based on its fully qualified name
     *
     * @param typeName[in]  The fully qualified type name of the symbol to locate
     * @param newType[out]  The found type, else 0
     *
     * @return HRESULT
     */
    
    public INamedElement findQualifiedType(String typeName,
            Node clazzNode,
            INamespace classNamespace)
    {
        INamedElement newType = null;
        
        try
        {
            String packageName = resolvePackageFromUMLName(typeName);
            String shortTypeName = resolveTypeNameFromUMLName(typeName);
            
            // We have a potential situation where the qualified name coming in is actually
            // to an inner class. We will have no packages the the packageName value, so we
            // actually need to reverse the search, looking to see if we have a symbol on our
            // symbol table, and then compare its qualified name with packageName
            
            String shortName = shortTypeName;
            ArrayList < ETPairT < Node, String > > result = m_SymbolTable.get(shortName);
            if (result != null)
            {
                //            SymbolTable : : iterator lowerIter = m_SymbolTable.lower_bound(symName);
                //            SymbolTable : : iterator upperIter = m_SymbolTable.upper_bound(symName);
                
                for (Iterator < ETPairT < Node, String > > iter = result.iterator(); iter.hasNext();)
                {
                    ETPairT < Node, String > curEntry = iter.next();
                    
                    Node symNode = curEntry.getParamOne();
                    
                    if (symNode != null)
                    {
                        INamedElement named = new TypedFactoryRetriever < INamedElement > ().createTypeAndFill(symNode);
                        
                        if (named != null)
                        {
                            String fullName = named.getQualifiedName();
                            String fullPackName = resolvePackageFromUMLName(typeName);
                            
                            if ((fullPackName.length() > 0) && (fullPackName.equals(packageName) == true))
                            {
                                newType = named;
                                break;
                            }
                        }
                    }
                }
            }
            else
            {
                String fullName = resolveInnerClassName(typeName, clazzNode);
                if(typeName.equals(fullName) == false)
                {
                    String outerClass = resolvePackageFromUMLName(fullName);
                    
                    // First make sure (or create) that the outer class exist
                    // in the system.
                    INamedElement outer = retrieveType(clazzNode, classNamespace, outerClass, true);
                    
                    if(outer instanceof INamespace)
                    {
                        INamespace space = (INamespace)outer;
                        //                        newType = resolveUnknownType(typeName, space);
                        
                        newType = UMLXMLManip.createAndAddUnknownType(space, shortTypeName);
                        space.addOwnedElement(newType);
                        
                        // Then make sure (or create) the inner class.
                        //                        newType = retrieveType(clazzNode, classNamespace, fullName, true);
                    }
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        return newType;
    }
    
    /**
     * Note that the class has to be added to the project before this will work.
     */
    public void resolveTypes(IClassifier pClassifier, String xsQuery)
    {
        //DEBUG_XML_NODE( pClassifier, _T("c:\\temp\\UMLParsingIntegrator_ResolveTypes.xml"), false );
        
        ElementCollector < IParameter > col = new ElementCollector < IParameter > ();
        
        ETList < IParameter > cpParameters = col.retrieveElementCollection(pClassifier, xsQuery, Parameter.class);
        
        if (cpParameters != null)
        {
            XMLManip manip;
            
            long lCnt = cpParameters.getCount();
            
            for (int lIndx = 0; lIndx < lCnt; lIndx++)
            {
                IParameter cpParameter = cpParameters.get(lIndx);
                
                //ATLASSERT( cpParameter );
                
                if (cpParameter != null)
                {
                    Node cpNode = cpParameter.getNode();
                    if (cpNode != null)
                    {
                        String bsType = XMLManip.getAttributeValue(cpNode, "type");
                        if (bsType.length() > 0)
                        {
                            cpParameter.setType2(convertNamespace(bsType));
                        }
                    }
                }
            }
        }
    }
    
    /// Converts the namespace with periods to a namespace with double-colons.
    public String convertNamespace(String bsType)
    {
        //USES_CONVERSION;
        
        String xName = bsType;
        String prefix = "";
        
        if (bsType.length() > 4)
        {
            prefix = xName.substring(0, 4);
        }
        
        // This is an example of a DCE XMI id.
        // DCE.4C54B80B-66C6-4663-B59E-07F535E77E28
        // It is always 40 characters long.
        if ((prefix.equals("DCE.") && (bsType.length() == 40)))
        {
            
            return bsType;
        }
        else
        {
            String xsType = StringUtilities.replaceAllSubstrings(bsType, ".", "::");
            return xsType;
        }
    }
    
    public void sendREEvents(IREClass pClass)
    {
        
        IResultCell cell = null;
        
        try
        {
            Document pEventDoc = null;
            
            pEventDoc = XMLManip.getDOMDocument();
            
            String filename = pClass.getFilename();
            
            cell = new ResultCell();
            
            onBeginParseFile(filename, cell);
            
            String packageName = pClass.getPackage();
            
            if (packageName.length() > 0)
            {
                Node pPackageNode = createTopLevelNode(pEventDoc, "UML:Package");
                
                if (pPackageNode != null)
                {
                    XMLManip.setAttributeValue(pPackageNode, "name", packageName);
                    
                    IPackageEvent pEvent = new PackageEvent();
                    
                    if (pEvent != null)
                    {
                        pEvent.setEventData(pPackageNode);
                        
                        cell = new ResultCell();
                        
                        onPackageFound(pEvent, cell);
                    }
                }
            }
            
            Node cpEventData = pClass.getEventData();
            if (cpEventData != null)
            {
                IClassEvent cpClassEvent = new ClassEvent();
                if (cpClassEvent != null)
                {
                    cpClassEvent.setEventData(cpEventData);
                    
                    onClassFound(cpClassEvent, null);
                }
            }
            
            onEndParseFile(filename, cell);
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
    }
    
    /**
     * Creates the top level node.  The document will be created first then
     * the top level node will created.
     *
     * @param nodeName [in] The name of the top node.
     */
    public Node createTopLevelNode(Document doc, String nodeName)
    {
        Node pNewNode = null;
        
        Node pTopNode = null;
        
        try
        {
            Document pDoc = null;
            
            pDoc = XMLManip.getDOMDocument();
            if (pDoc != null)
            {
                pTopNode = (Node) pDoc;
                
                pNewNode = createNode(pTopNode, nodeName);
                
                if (pNewNode != null)
                {
                    XMLManip.setAttributeValue(pNewNode, "language", "");
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        return pNewNode;
    }
    
    /**
     * Create a new XML node and added to the document.  CreateElement will throw
     * _com_error exceptions will an invalid HRESULT is received.
     * @param pDoc [in] The document that will recieve the XML node.
     * @param nodeName [in] The name of the new XML node.
     * @param pNewNode [out] The new XML node.
     */
    public Node createNode(Node pOwner, String nodeName)
    {
        
        Node pNewNode = null;
        
        try
        {
            Document pDoc = pOwner.getDocument();
            
            // If we failed to retrieve the DOM document test if the
            // specified node is the DOM document.
            if (pDoc == null)
            {
                Document pTempDoc = (Document) pOwner;
                if (pTempDoc != null)
                {
                    pDoc = pTempDoc;
                }
            }
            
            if (pDoc != null)
            {
                pNewNode = XMLManip.createElement(pDoc, nodeName);
                
                //Node tempNode  = pOwner->appendChild(*pNewNode, &tempNode));
                pNewNode.setParent((org.dom4j.Element) pOwner);
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        return pNewNode;
    }
    
    /**
     *
     * Determines whether or not the passed in type name is decorated or not.
     *
     * @param typeStr[in]   The type name to check against
     *
     * @return NamePair.first will be true if typeStr is decorated with a pointer or reference
     *         indicator, otherwise false.
     *         NamePair.second will be the undecorated type name
     *
     */
    
    public ETPairT < Boolean, String > getUndecoratedName(String typeStr)
    {
        
        ETPairT < Boolean, String > result = new ETPairT < Boolean, String > (new Boolean(false), typeStr);
        
        int iLen = typeStr != null ? typeStr.length() : 0;
        if ((iLen > 0) && (typeStr.charAt(iLen - 1) == '*' || typeStr.charAt(iLen - 1) == '&'))
        {
            // Get type name
            int iPos1 = typeStr.indexOf("*"), iPos2 = typeStr.indexOf("&"), iPos;
            if ((iPos1 != -1) && (iPos2 != -1))
            {
                if (iPos1 < iPos2)
                    iPos = iPos1;
                else
                    iPos = iPos2;
            }
            else if (iPos1 != -1)
            {
                iPos = iPos1;
            }
            else
            {
                iPos = iPos2;
            }
            
            result.setParamTwo(typeStr.substring(0, iPos));
            result.setParamOne(new Boolean(true));
            
        }
        
        return result;
    }
    
    /**
     * Makes sure that, when given a fully qualified string, that the proper Package structure exists in the parent
     * namespace
     *
     * @param parent[in]             The owning namespace
     * @param name[in]               The fully qualified name, such as java::util::Vector
     * @param resolvedSpace[out]     The last package / namespace created. For example, if "java::util::Vector" is passed in for name,
     *                               the INamespace that represents "util" will be returned
     *
     * @return HRESULT
     */
    
    public INamespace establishPackageOwnership(INamespace parent, String name)
    {
        INamespace resolvedSpace = null;
        
        try
        {
            
            if (name.length() > 0)
            {
                INamespace curSpace = null;
                
                ETList < String > tokens = StringUtilities.splitOnDelimiter(name, "::");
                String fullyQualified = "";
                for (int i = 0, count = tokens.size() - 1; i < count; ++i)
                {
                    String token = tokens.get(i);
                    
                    // Keep track of the fully qualified name as we move through
                    // the tokens
                    
                    if (fullyQualified.length() > 0)
                    {
                        fullyQualified += "::" + token;
                    }
                    else
                    {
                        fullyQualified = token;
                    }
                    
                    //Packages::iterator iter = m_Packages.find( token );
                    Node node = m_Packages.get(fullyQualified);
                    //Packages::iterator iter = m_Packages.find( fullyQualified );
                    
                    if (node != null)
                    {
                        // We didn't find this package, so create a
                        // DOM node that will represent the package until we
                        // find one. DON'T add the package that matches
                        // the passed in name.
                        
                        if (!(fullyQualified.equals(name)))
                        {
                            // Before we create a package, let's make sure that the current token is not a class
                            // rather than a package
                            
                            if (m_SymbolTable.get(token) == null)
                            {
                                IClass clazzObj = null;
                                
                                INamespace pPackage = new TypedFactoryRetriever < INamespace > ().createTypeAndFill(node);
                                
                                if (pPackage != null)
                                {
                                    // We need to see if the package / namespace is already contained in a namespace.
                                    // If it is, no need to add pPackage to the current namespace. If it isn't,
                                    // we need to do so.
                                    
                                    INamespace immediateNamespace = pPackage.getNamespace();
                                    
                                    if (immediateNamespace == null && curSpace == null)
                                    {
                                        curSpace = parent;
                                        
                                        // Don't check the HRESULT here, as it is quite possible that curSpace is
                                        // actually a child of pPackage. Instead of AddOwnedElement, we need
                                        // a call to determine if pPackage is a parent of curSpace
                                        
                                        curSpace.addOwnedElement(pPackage);
                                    }
                                    else if (immediateNamespace != null)
                                    {
                                        curSpace = immediateNamespace;
                                    }
                                    
                                    curSpace = pPackage;
                                }
                            }
                            else
                            {
                                break;
                            }
                        }
                    }
                }
                
                if (curSpace != null)
                {
                    resolvedSpace = curSpace;
                }
            }
        }
        catch (Exception e)
        {
            sendExceptionMessage(e);
        }
        
        return resolvedSpace;
    }
    
    public class UnresolvedType
    {
        private String mXMIID = "";
        private long mNumberOfTypes = 0;
        private boolean mIsDataType = false;
        private INamedElement mElement = null;
        private boolean mRequireDependencySearch = false;
        
        public UnresolvedType()
        {
        }
        
        public UnresolvedType(String id)
        {
            this(id, 1);
        }
        
        public UnresolvedType(String id, long num)
        {
            this(id, num, false, false, null);
        }
        
        public UnresolvedType(String id, long num, boolean isDataType, boolean needsSearch, INamedElement namedElement)
        {
            setXMIID(id);
            setNumberOfTypes(num);
            setDataType(isDataType);
            mRequireDependencySearch = needsSearch;
            mElement = namedElement;
        }
        
        public long incrementTypes()
        {
            mNumberOfTypes++;
            return mNumberOfTypes;
        }
        
        public long decrementTypes()
        {
            mNumberOfTypes--;
            return mNumberOfTypes;
        }
        
        /**
         * @return
         */
        public boolean isDataType()
        {
            return mIsDataType;
        }
        
        /**
         * @return
         */
        public long getNumberOfTypes()
        {
            return mNumberOfTypes;
        }
        
        /**
         * @return
         */
        public String getXMIID()
        {
            return mXMIID;
        }
        
        /**
         * @param b
         */
        public void setDataType(boolean b)
        {
            mIsDataType = b;
        }
        
        /**
         * @param l
         */
        public void setNumberOfTypes(long l)
        {
            mNumberOfTypes = l;
        }
        
        /**
         * @param string
         */
        public void setXMIID(String id)
        {
            //         if((mXMIID.length() > 0) && (mXMIID.equals(string) == false))
            //         {
            //            ETSystem.out.println("I already have an XMIID");
            //         }
            mXMIID = id;
            
            if (mElement != null)
            {
                String curID = mElement.getXMIID();
                
                if (curID.length() != 0 && curID != id)
                {
                    mElement = null;
                }
            }
        }
        
        public boolean needsSearch()
        {
            return mRequireDependencySearch;
        }
        
        public INamedElement getElement()
        {
            return mElement;
        }
        
        public void reset(String xmiID, long num, boolean isDataType, boolean needsSearch, INamedElement element)
        {
            mElement = element;
            mNumberOfTypes = num;
            mIsDataType = isDataType;
            mRequireDependencySearch = needsSearch;
            setXMIID(xmiID);
        }
        
    }
    
    /**
     * Sends a message to the message service.  The exception is used to provide
     * the message.
     *
     * @param e The exception.
     */
    protected void sendExceptionMessage(Exception e)
    {
        UMLMessagingHelper helper = new UMLMessagingHelper();
        helper.sendMessage(MsgCoreConstants.MT_ERROR, e.getLocalizedMessage());
    }
    
    public void setTaskSupervisor(ITaskSupervisor val)
    {
        supervisor = val;
    }
    
    public ITaskSupervisor getTaskSupervisor()
    {
        return supervisor;
    }
    
    private String getLocalMsg(String key)
    {
        return NbBundle.getMessage(UMLParsingIntegrator.class, key);
    }
    
    private static String getLocalMsg(String key, Object param)
    {
        return NbBundle.getMessage(UMLParsingIntegrator.class, key, param);
    }
    
    private static String getLocalMsg(String key, Object[] params)
    {
        return NbBundle.getMessage(UMLParsingIntegrator.class, key, params);
    }
    
    /**
     * When we create a template is not know to the system, we create a datatype.
     * The problem is that we do not create the parameters.  However, when we
     * add the derivation between the derivation classifer and the template, it
     * needs to have the template parameters set up.  Therefore create the
     * default set of template parameters.
     *
     */
    private void ensureDefaultTemplateParameters(IDataType template,
            int numOfParameters)
    {
        ETList < IParameterableElement > curParams = template.getTemplateParameters();
        
        // TODO: The retrieve this information from the compiler before creating
        //       the model element.  We will not be able to fix this problem until
        //       we move away from using our own parser, and start using a compiler.
        
        if(curParams.size() < numOfParameters)
        {
            ICreationFactory spCreationFactory = FactoryRetriever.instance().getCreationFactory();
            
            for(int i = 0; i < numOfParameters; i++)
            {
                // In case a template has a default parameter last time this
                // datatype was processed.  We will have to only add the default
                // parameters.
                if(curParams.size() > i)
                {
                    continue;
                }
                
                IParameterableElement param = (IParameterableElement) spCreationFactory.retrieveMetaType("ParameterableElement", null);
                param.setName(defaultTemplateNames[i]);
                template.addTemplateParameter(param);
            }
        }
    }
    
    private void handleChildDerivations(Node pDerivationElement,
            Node clazz,
            IClassifier clazzObj,
            INamespace classSpace)
    {
        Node derivationElement = isDerivationPresent(pDerivationElement, true);
        while(derivationElement != null)
        {
            ensureDerivation(derivationElement, clazz, clazzObj, classSpace);
            derivationElement = isDerivationPresent(pDerivationElement, true);
        }
    }
    
    
    private final static String INDENT = "  ";
    private final static String INDENT_DASH = "---|";
    private boolean logEnabled = false;
    private ITaskSupervisor supervisor;
    private ETList < Node > m_ItemsToSink = new ETArrayList < Node > ();
    private Document m_FragDocument = null;
    private IElementLocator m_Locator = null;
    private IRelationFactory m_Factory = null;
    private IStrings m_Files = null;
    private boolean m_Cancelled = false;
    private IUMLParserEventDispatcher m_Dispatcher = null;
    private IUMLParsingIntegrator m_Integrator = null;
    //private IUMLParserEventsSink m_Cookie;
    private boolean m_ReplaceDocs;
    private String mUnknownType = "";
    
    /// true to ask the user if they want to replace elements that are duplicated when doing an Update Model From Source
    private boolean m_CancelDueToConflict;
    private ILanguageManager m_LanguageManager = null;
    private IProject m_Project = null;
    private INamespace m_Namespace = null;
    private String m_BaseDirectory = null;
    /// These are xml nodes that contain presentation elements that need to be resynched
    private ILanguageParserSettings m_LanguageParserSettings = null;
    private ETPairT < Node, String > Symbol;
    private HashMap < String, Node > m_Packages = new HashMap < String, Node > ();
    //private HashMap<String, ETPairT<Node, String>> m_SymbolTable = new HashMap<String, ETPairT<Node, String>>();
    private HashMap < String, ArrayList < ETPairT < Node, String > > > m_SymbolTable = new HashMap < String, ArrayList < ETPairT < Node, String > > > ();
    private Stack < ParsingContext > m_ContextStack = new Stack < ParsingContext > ();
    private HashMap < String, UnresolvedType > m_Types = new HashMap < String, UnresolvedType > ();
    private HashMap < String, Node > m_ReplacedNodes = new HashMap < String, Node > ();
    private HashMap < String, IClassifier > m_unquieDerivations = new HashMap < String, IClassifier > ();
    
    // This is an array of default template names.  This would be used when we
    // know template paramters are needed, but are not supplied.  For example
    // when dealing with classes in a library.
    private String[] defaultTemplateNames =
    {
        "T",
        "U",
        "V",
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "K",
        "L",
        "M",
        "N",
        "O",
        "P",
        "Q",
        "R",
        "S",
        "W",
        "X",
        "Y",
        "Z"
    };
    
    private HashSet redef;
}
