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

package org.netbeans.modules.uml.project.ui.nodes;

import java.util.Iterator;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.uml.project.ui.customizer.UMLImportsUiSupport;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;

import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.FolderLookup;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.project.ui.codegen.CodeGeneratorAction;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewDiagramType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewElementType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewPackageType;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.NewAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;


public class UMLPhysicalViewProvider implements LogicalViewProvider
{
    
    private static final RequestProcessor BROKEN_LINKS_RP =
        new RequestProcessor("UMLPhysicalViewProvider.BROKEN_LINKS_RP"); // NOI18N
    
    private static final RequestProcessor rp = new RequestProcessor();
    
    private final UMLProject mProject;
    private final UMLProjectHelper mHelper;
    private final SubprojectProvider mSubProjetProvider;
    private final PropertyEvaluator evaluator;
    private final UMLImportsUiSupport mImportSupport;
    private UMLLogicalViewRootNode node;
    private ReferenceHelper mResolver = null;
    
    
    public UMLPhysicalViewProvider(UMLProject project,
        UMLProjectHelper helper,
        PropertyEvaluator evaluator,
        SubprojectProvider spp,
        UMLImportsUiSupport importSupport,
        ReferenceHelper resolver)
    {
        
        this.mProject = project;
        assert mProject != null : "Project can not be NULL"; // NOI18N
        
        this.mHelper = helper;
        assert mHelper != null : "Helper can not be NULL"; // NOI18N
        
        this.evaluator = evaluator;
        assert evaluator != null;
        
        this.mSubProjetProvider = spp;
        assert spp != null : "SubprojectProvider can not be NULL";// NOI18N
        
        this.mImportSupport = importSupport;
        assert importSupport != null : "UMLImportsUiSupport can not be NULL";// NOI18N
        
        mResolver = resolver;
        assert resolver != null : "ReferenceHelper can not be NULL";// NOI18N
        
    }
    
    public Node createLogicalView()
    {
        
        if (this.node == null)
        {
            this.node = new UMLLogicalViewRootNode();
        }
        
        return this.node;
    }
    
    public org.openide.nodes.Node findPath( Node root, Object target )
    {
        // TODO: Figure out how to retrieve a node
//       Project project = (Project)root.getLookup().lookup( Project.class );
//       if ( project == null )
//       {
//          return null;
//       }
//
//       if ( target instanceof FileObject )
//       {
//          FileObject fo = (FileObject)target;
//          Project owner = FileOwnerQuery.getOwner( fo );
//          if ( !project.equals( owner ) )
//          {
//             return null; // Don't waste time if project does not own the fo
//          }
//
//          Node[] nodes = root.getChildren().getNodes( true );
//          for ( int i = 0; i < nodes.length; i++ )
//          {
//             Node result = PackageView.findPath( nodes[i], target );
//             if ( result != null )
//             {
//                return result;
//             }
//          }
//       }
        
        return null;
    }
    
    /**
     * Used by UMLProjectCustomizer to mark the project as broken when it warns user
     * about project's broken references and advices him to use BrokenLinksAction to correct it.
     *
     */
    public void testBroken()
    {
        /*
        UMLLogicalViewRootNode rootNode = (UMLLogicalViewRootNode) this.createLogicalView();
        rootNode.setBroken(hasBrokenLinks());
         */
    }
    
    
//    private static Lookup createLookup(IProject project)
//    {
//        DataFolder rootFolder = 
//            DataFolder.findFolder(project.getProjectDirectory());
//        
//        // XXX Remove root folder after FindAction rewrite
//        return Lookups.fixed( new Object[] { project, rootFolder } );
//    }
    
    
    // MCF - this breakable infrastructure is copied from JESE project.
    // maybe we need something like this, maybe not.
    // can remove it eventually if we don't need it. I suspect we will need
    // some "isBroken" facility though
    //
    /////// beging isBroken stuff
    
    
    private static final String[] BREAKABLE_PROPERTIES = new String[]
    {
        UMLProjectProperties.REFERENCED_JAVA_PROJECT,
            UMLProjectProperties.REFERENCED_JAVA_PROJECT_ARTIFACTS,
            UMLProjectProperties.REFERENCED_JAVA_PROJECT_SRC,
    };
    
    public boolean hasBrokenLinks()
    {
        boolean retVal = false;
        
        retVal = 
            BrokenReferencesSupport.isBroken(mHelper.getAntProjectHelper(),
            mResolver,
            getBreakableProperties(),
            new String[]{});
            
        return retVal;
    }
    
    
    
    private String[] getBreakableProperties()
    {
        /*
        SourceRoots roots = this.project.getSourceRoots();
        String[] srcRootProps = roots.getRootProperties();
        roots = this.project.getTestSourceRoots();
        String[] testRootProps = roots.getRootProperties();
        String[] result = new String [BREAKABLE_PROPERTIES.length + srcRootProps.length + testRootProps.length];
        System.arraycopy(BREAKABLE_PROPERTIES, 0, result, 0, BREAKABLE_PROPERTIES.length);
        System.arraycopy(srcRootProps, 0, result, BREAKABLE_PROPERTIES.length, srcRootProps.length);
        System.arraycopy(testRootProps, 0, result, BREAKABLE_PROPERTIES.length + srcRootProps.length, testRootProps.length);
        return result;
         */
        return BREAKABLE_PROPERTIES; // mcf hack
    }
	
	
	public ModelRootNodeCookie getModelRootNodeCookie()
	{
		return ((UMLLogicalViewRootNode)createLogicalView()).getModelRootNodeCookie();
	}
	
	
	public UMLModelRootNode getModelRootNode()
	{
		return ((UMLLogicalViewRootNode)createLogicalView()).getModelRootNode();
	}
    
    private static Image brokenProjectBadge = Utilities.loadImage(
        "org/netbeans/modules/uml/project/ui/resources/brokenProjectBadge.gif"); // NOI18N
    /////// end isBroken stuff
    
    // Private innerclasses ----------------------------------------------------
    
    /** Filter node containin additional features for the UML physical
     */
    private final class UMLLogicalViewRootNode extends AbstractNode
        implements UMLLogicalViewCookie
    {
        
        private Image icon;
        private Lookup lookup;
        private Action brokenLinksAction;
        private boolean broken;
        
        public UMLLogicalViewRootNode()
        {
            super(
                new LogicalViewChildren(
                    mProject, mHelper, evaluator, mImportSupport), 
                    Lookups.singleton(mProject));
            
            setIconBaseWithExtension(
                "org/netbeans/modules/uml/project/ui/resources/umlProject.gif"); // NOI18N
            
            setName(ProjectUtils.getInformation( mProject ).getDisplayName());
            
            if (hasBrokenLinks())
                broken = true;
            
            brokenLinksAction = new BrokenLinksAction();
        }
        
        public Image getIcon(int type)
        {
            Image original = super.getIcon(type);
            
            return broken
                ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0)
                : original;
        }
        
        public Image getOpenedIcon(int type)
        {
            Image original = super.getOpenedIcon(type);
            
            return broken
                ? Utilities.mergeImages(original, brokenProjectBadge, 8, 0)
                : original;
        }
        
        public String getHtmlDisplayName()
        {
            String dispName = super.getDisplayName();
            
            try
            {
                dispName = XMLUtil.toElementContent(dispName);
            }
            
            catch (CharConversionException ex)
            {
                // OK, no annotation in this case
                return null;
            }
            
            return broken
                ? "<font color=\"#A40000\">" + dispName + "</font>" //NOI18N
                : null;
        }
        
        public Action[] getActions(boolean context)
        {
            return getAdditionalActions();
        }
        
        public boolean canRename()
        {
            return false;
        }
        
        public boolean canCopy()
        {
            return false;
        }
        
        public boolean canCut()
        {
            return true;
        }
        
        public boolean canDestroy()
        {
            return true;
        }
        
        public void destroy() throws IOException
        {
            NotifyDescriptor descriptor=new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(UMLPhysicalViewProvider.class,
                "MSG_ConfirmDeleteProject", mProject.getName(),
                mProject.getProjectDirectory().getPath()),
                NotifyDescriptor.YES_NO_OPTION);
            
            if (DialogDisplayer.getDefault().notify(descriptor)==
                NotifyDescriptor.YES_OPTION)
            {
                mProject.removeUMLProjectMetaListener();

		closeDiagramsWithoutSave();
                    
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        CommonProjectActions.closeProjectAction()
                            .actionPerformed(new ActionEvent(this, 0, ""));
                    }
                });
                
                mHelper.getProjectDirectory().delete();
            }
        }
		
        private void closeDiagramsWithoutSave() {
            ICoreProduct coreProduct = ProductRetriever.retrieveProduct();
            if (coreProduct instanceof IProduct)
            {
                IProduct product = (IProduct)coreProduct;
                List<IProxyDiagram> diagrams = product.getDiagramManager().getOpenDiagrams();
                for (IProxyDiagram diagram: diagrams)
                {
                    File f = new File(diagram.getFilename());
		    FileObject fobj = FileUtil.toFileObject(f);
		    if (fobj != null) 
		    {
			Project owner = FileOwnerQuery.getOwner(fobj);
			if (mProject.equals(owner)) 
			{
			    IDiagram diag = diagram.getDiagram();
			    if (diag != null) 
			    {
				diag.setIsDirty(false);
				product.getDiagramManager().closeDiagram2(diag);
			    }
			}
		    }
		}
            }
        }
	
        public Node.Cookie getCookie(Class type)
        {
            Node.Cookie cookie = super.getCookie(type);
            
            if (cookie == null)
            {
                if (type.isAssignableFrom(UMLLogicalViewCookie.class))
                    return this;
            }
            
            return cookie;
        }
        
        
        // this is the UMLLogicalViewCookie method(s)
        public ModelRootNodeCookie getModelRootNodeCookie()
        {
            // find the ModelRootNode and tell it to get its diagrams
            Children kids = this.getChildren();
            
            ModelRootNodeCookie dHelper = null;
            
            for (int i = 0; i < kids.getNodes().length; i++)
            {
                dHelper = (ModelRootNodeCookie)
                kids.getNodes()[i].getCookie(ModelRootNodeCookie.class);
                
                if (dHelper != null)
                    return dHelper;
            }
            
            return null;
            
        }
		
		public UMLModelRootNode getModelRootNode()
		{
			Children kids = this.getChildren();
			ModelRootNodeCookie dHelper = null;
			
			for (int i = 0; i < kids.getNodes().length; i++)
            {	
                dHelper = (ModelRootNodeCookie)
                kids.getNodes()[i].getCookie(ModelRootNodeCookie.class);
                
                if (dHelper != null)
                    return (UMLModelRootNode)(kids.getNodes()[i]);
            }
			return null;
		}

        
		
	/**
	 * Get the new types that can be created in this node. For example, a node
	 * representing a class will permit attributes, operations, classes,
	 * interfaces, and enumerations to be added.
	 *
	 * @return An array of new type operations that are allowed.
	 */
	public NewType[] getNewTypes()
	{
		return new NewType[]
		{
			new NewDiagramType(mHelper.getProject()),
			new NewPackageType(mHelper.getProject()),
			new NewElementType(mHelper.getProject())
		};
	}
		
		
        // Private methods -------------------------------------------------
        
        private Action[] getAdditionalActions()
        {
            
            ResourceBundle bundle = NbBundle.getBundle( UMLPhysicalViewProvider.class );
            
            List actions = new ArrayList();
            // cvc - CR 6316000
            // Set As Main Project not beneficial for UML Project types
            // actions.add(CommonProjectActions.setAsMainProjectAction());
            
			actions.add(SystemAction.get(NewAction.class));
			actions.add(null);
            actions.add(CommonProjectActions.openSubprojectsAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
			actions.add(CommonProjectActions.deleteProjectAction());
			actions.add(null);
            
            try
            {
                Repository repository  = Repository.getDefault();
                FileSystem sfs = repository.getDefaultFileSystem();
                FileObject fo = sfs.findResource("UMLProjects/Actions"); // NOI18N
                
                if (fo != null)
                {
                    DataObject dobj = DataObject.find(fo);
                    FolderLookup actionRegistry = new FolderLookup((DataFolder)dobj);
                    Lookup.Template query = new Lookup.Template(Object.class);
                    Lookup lookup = actionRegistry.getLookup();
                    Iterator it = lookup.lookup(query).allInstances().iterator();
//                    if (it.hasNext())
//                    {
//                        actions.add(null);
//                    }
                    
                    while (it.hasNext())
                    {
                        Object next = it.next();
                        
                        if (next instanceof Action)
                            actions.add(next);

                        else if (next instanceof JSeparator)
                            actions.add(null);
                    }
                }
            }
            
            catch (DataObjectNotFoundException ex)
            {
                // data folder for exitinf fileobject expected
                ErrorManager.getDefault().notify(ex);
            }
            
            // Condition for Generate Code Menu
//            actions.add(SystemAction.get(
//                org.netbeans.modules.uml.project.ui.nodes.actions
//                    .GenerateCodeAction.class));
//
//            GenerateCodeAction code = new GenerateCodeAction(mProject);
//            boolean status = code.isAssociatedWithImpl();
//            
//            if (!status)
//            {
//                actions.add(code);
//                actions.add(null);
//            }

            addContextMenus(actions);
            actions.add(null);
            
            if (broken == true)
                actions.add(brokenLinksAction);
            
            actions.add(CommonProjectActions.customizeProjectAction());
            return (Action[]) actions.toArray(new Action[actions.size()]);
        }
        
        
        private boolean isBroken()
        {
            //return this.broken;
            return false; // mcf hack
        }
        
        
        private void setBroken(boolean broken)
        {
            this.broken = broken;
            brokenLinksAction.setEnabled(broken);
            fireIconChange();
            fireOpenedIconChange();
            fireDisplayNameChange(null, null);
        }
        
        
        /** This action is created only when project has broken references.
         * Once these are resolved the action is disabled.
         */
        private class BrokenLinksAction extends AbstractAction
            implements PropertyChangeListener, Runnable
        {
            private RequestProcessor.Task task = null;
            private PropertyChangeListener weakPCL;
            
            public BrokenLinksAction()
            {
                putValue(Action.NAME, NbBundle.getMessage(
                    UMLPhysicalViewProvider.class,
                    "LBL_Fix_Broken_Links_Action")); // NOI18N
                
                setEnabled(broken);
                evaluator.addPropertyChangeListener( this );
                
                // When evaluator fires changes that platform properties were
                // removed the platform still exists in JavaPlatformManager.
                // That's why I have to listen here also on JPM:
                
                // MCF - do we care about platform?
                /*
                weakPCL = WeakListeners.propertyChange( this, JavaPlatformManager.getDefault() );
                JavaPlatformManager.getDefault().addPropertyChangeListener( weakPCL );
                 */
            }
            
            public void actionPerformed(ActionEvent e)
            {
                // We are not worrying about updating the project version.
                //mHelper.requestSave();
                BrokenReferencesSupport.showCustomizer(
                    mHelper.getAntProjectHelper(),
                    mResolver, getBreakableProperties(),
                    new String[]{});
                    
                mHelper.scanSourceGroups();
                run();
            }
            
            public void propertyChange(PropertyChangeEvent evt)
            {
                // check project state whenever there was a property change
                // or change in list of platforms.
                // Coalesce changes since they can come quickly:
                if (task == null)
                    task = BROKEN_LINKS_RP.create(this);
                
                task.schedule(100);
            }
            
            public synchronized void run()
            {
                boolean old = broken;
                broken = hasBrokenLinks();
                
                if (old != broken)
                    setBroken(broken);
            }
        }
    }
    
    private static final class LogicalViewChildren extends Children.Keys/*<SourceGroup>*/
        implements ChangeListener
    {
        private static final Object UML_MODEL = "Model"; //NOI18N
        private static final Object UML_DIAGRAMS = "Diagrams"; //NOI18N
        private static final Object UML_IMPORTED_PROJECTS = "Imported Projects"; //NOI18N

        private final UMLProject project;
        private final PropertyEvaluator evaluator;
        private final UMLProjectHelper helper;
        private final UMLImportsUiSupport mImportSupport;
        
        private Node mModelNode = null;
        private Node mDiagramNode = null;
        private ImportProjectRootNode mImportedNode = null;

        
        public LogicalViewChildren(
            UMLProject project,
            UMLProjectHelper helper,
            PropertyEvaluator evaluator,
            UMLImportsUiSupport importSupport)
        {
            this.project = project;
            this.evaluator = evaluator;
            this.helper = helper;
            mImportSupport = importSupport;
        }
        
        protected void addNotify()
        {
            super.addNotify();
            getSources().addChangeListener(this);
            setKeys( getKeys() );
        }
        
        protected void removeNotify()
        {
            setKeys(Collections.EMPTY_SET);
            getSources().removeChangeListener(this);
            super.removeNotify();
        }
        
        protected synchronized Node getModelRootNode()
        {
            if (mModelNode == null)
                mModelNode = new UMLModelRootNode(project, helper, evaluator);
            
            return mModelNode;
        }
        
        protected synchronized Node getDiagramRootNode()
        {
            if (mDiagramNode == null)
            {
                mDiagramNode = 
                    new UMLDiagramsRootNode(project, helper, evaluator);
            }
            
            return mDiagramNode;
        }
        
        protected synchronized ImportProjectRootNode getImportsRootNode()
        {
            if (mImportedNode == null)
                mImportedNode = new ImportProjectRootNode(helper);
            
            return mImportedNode;
        }
        
        protected Node[] createNodes( Object key )
        {
            Node[] result;
            
            if (key == UML_MODEL)
                result = new Node[] {getModelRootNode()};
            
            else if (key == UML_DIAGRAMS)
                result = new Node[] {getDiagramRootNode()};
            
            else if(key ==  UML_IMPORTED_PROJECTS)
            {
                ImportProjectRootNode node = getImportsRootNode();
                mImportSupport.addImportElementListener(node);
                result = new Node[] { node };
            }
            
            else
            {
                assert false : "Unknown key type";  //NOI18N
                result = new Node[0];
            }
            
            return result;
        }
        
        public void stateChanged(ChangeEvent e)
        {
////            setKeys( getKeys() );
//            //The caller holds ProjectManager.mutex() read lock
//            rp.post (new Runnable () {
//                public void run () {
//                    setKeys( getKeys() );
//                }
//            });
            
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Private methods -----------------------------------------------------
        
        private Collection getKeys()
        {
            List result =  new ArrayList();
            result.add(UML_MODEL);
            result.add(UML_DIAGRAMS);
            result.add(UML_IMPORTED_PROJECTS);
            return result;
        }
        
        private Sources getSources()
        {
            return ProjectUtils.getSources( project );
        }
    }
    
    
    /** The special properties action
     */
    private static class PreselectPropertiesAction extends AbstractAction
    {
        private final Project project;
        private final String nodeName;
        private final String panelName;
        
        public PreselectPropertiesAction( Project project, String nodeName )
        {
            this(project, nodeName, null);
        }
        
        public PreselectPropertiesAction( Project project, String nodeName, String panelName )
        {
            super( NbBundle.getMessage( UMLPhysicalViewProvider.class,
                "LBL_Properties_Action" ) ); // NOI18N
            this.project = project;
            this.nodeName = nodeName;
            this.panelName = panelName;
        }
        
        public void actionPerformed( ActionEvent e )
        {
            // J2SECustomizerProvider cp = 
            //    (J2SECustomizerProvider)project.getLookup()
            //        .lookup( J2SECustomizerProvider.class );
            
            CustomizerProviderImpl cp = 
                (CustomizerProviderImpl)project.getLookup()
                    .lookup( CustomizerProviderImpl.class );
            
            if (cp != null)
                cp.showCustomizer(nodeName, panelName);
        }
    }
    
    
    
//    class GenerateCodeAction extends AbstractAction
//    {
//        private final UMLProject project;
//        
//        /** Creates a new instance of GenerateCodeAction */
//        public GenerateCodeAction(UMLProject p)
//        {
//            super(NbBundle.getMessage(GenerateCodeAction.class, 
//                "LBL_GenerateCodeAction")); //NOI18N
//            
//            this.project = p;
//        }
//        
//        public void actionPerformed(ActionEvent e)
//        {
//            if (!isAssociatedWithImpl())
//            {  
//                // If the java project reference is already set, you can go 
//                // ahead and invoke code generation / synchronization
//                CodeGeneratorAction codeGenerator = new CodeGeneratorAction();
//                codeGenerator.generateCode(project);
//            }
//            
//            else
//            {    // If java project reference is not set bring up the project customizer.
//                // Debug.out.println("UMLPhysicalViewProvider():GenerateCodeAction(): Calling generate code after user customizes project");
//                CustomizerProviderImpl cp = 
//                    (CustomizerProviderImpl)project.getLookup()
//                        .lookup( CustomizerProviderImpl.class );
//                
//                // After properties are set generate code.
//                cp.setGenerateCode(true);  
//                CommonProjectActions.customizeProjectAction().actionPerformed(e);
//            }
//        }
//        
//        public boolean isAssociatedWithImpl()
//        {
//            String refJavaProject;
//            
//            AntProjectHelper umlAntProjectHelper = 
//                (AntProjectHelper)((UMLProjectHelper) 
//                    project.getLookup().lookup(UMLProjectHelper.class))
//                        .getAntProjectHelper();
//            
//            EditableProperties editableProperties = 
//                umlAntProjectHelper.getProperties(
//                    AntProjectHelper.PROJECT_PROPERTIES_PATH);
//            
//            refJavaProject = (String)editableProperties
//                .getProperty(UMLProjectProperties.REFERENCED_JAVA_PROJECT);
//            
//            // You can also check the uml project type here to see whether it is Implementation, Analysis or Design.
//            // But however currently we support creating a new UML project of type Implementation which might not have
//            // any associated Java project until 'Generate code' is called initially.
//            // So here we are checking for the REFERENCED_JAVA_PROJECT field directly.
//            if (refJavaProject == null || refJavaProject.equals("")) // NOI18N
//                return true;
//            
//            else
//                return false;
//        }
//        
//        public boolean isEnabled()
//        {
//            return true;
//        }
//    }
    
    /**
     * Retrieve the context actions added by other modules.
     *
     * @param actions The action collection to add the actions to.
     */
    protected void addContextMenus(List actions)
    {
        UMLElementNode node = new UMLElementNode();
        
        Action[] nodeActions =
            node.getActionsFromRegistry("Actions/UML/Search"); // NOI18N
        
        for (Action curAction : nodeActions)
        {
            if (curAction == null)
                // Make Sure the Seperators are kept.
                actions.add(null);
            
            else if (curAction.isEnabled())
                actions.add(curAction);
        }
        actions.add(null);
        
//        nodeActions =
//            node.getActionsFromRegistry("contextmenu/uml/sync"); // NOI18N
//        
//        for (Action curAction : nodeActions)
//        {
//            if (curAction == null)
//                // Make Sure the Seperators are kept.
//                actions.add(null);
//            
//            else if (curAction.isEnabled())
//                actions.add(curAction);
//        }

        nodeActions =
            node.getActionsFromRegistry("contextmenu/uml/generate"); // NOI18N
        
        for (Action curAction : nodeActions)
        {
            if (curAction == null)
                // Make Sure the Seperators are kept.
                actions.add(null);
            
            else if (curAction.isEnabled())
                actions.add(curAction);
        }
    }
}
