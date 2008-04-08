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

/*
 * UMLProject.java
 *
 * Created on February 13, 2004, 4:06 PM
 */

package org.netbeans.modules.uml.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.ReverseEngineerTask;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;
import org.netbeans.modules.uml.project.ui.nodes.UMLPhysicalViewProvider;
import org.netbeans.modules.uml.project.ui.nodes.ModelRootNodeCookie;
import org.netbeans.modules.uml.project.ui.customizer.UMLImportsUiSupport;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.util.ITaskFinishListener;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 * @author  Trey Spiva
 */
public class UMLProject implements Project, AntProjectListener
{
    private static final Icon PROJECT_ICON = 
        ImageUtil.instance().getIcon("uml-project.png"); // NOI18N

    private final Lookup mLookup;
    private final UMLProjectHelper mHelper;
    private final ReferenceHelper mRefHelper;
    private final GeneratedFilesHelper mGenFilesHelper;
    private final PropertyEvaluator mEval;
    private final UMLImportsUiSupport mImportSupport;
    
    // TODO is this bad practice?
    UMLProjectProperties uiProperties;
    
    public static final int PROJECT_MODE_ANALYSIS = 0;
    public static final int PROJECT_MODE_DESIGN = 1;
    public static final int PROJECT_MODE_IMPL = 2;
    
    public static final String PROJECT_MODE_ANALYSIS_STR = "Analysis"; // NOI18N
    public static final String PROJECT_MODE_DESIGN_STR = "Design"; // NOI18N
    public static final String PROJECT_MODE_IMPL_STR = "Implementation"; // NOI18N
    
    public static final String PROJECT_MODE_DEFAULT_STR =
            PROJECT_MODE_ANALYSIS_STR;
    
    public static final int PROJECT_LANG_JAVA = 0;
    public static final String PROJECT_LANG_JAVA_STR = "Java"; // NOI18N
    /* NB60TBD
    public static HashMap<String,MDRChangeListener> listenerMap =
            new HashMap<String,MDRChangeListener>();
    */
    /**
     * I am attempting to define an AntArtifact type so that we can
     *leverage all of the AntHelper and ReferenceHelper features when
     *tracking references across UMLProjects.
     *This is a bit of a hack because, as of yet, there is no need for a true
     *ant artifact. But the alternative is to reinvent the wheel in terms of
     *managing references, and that is unappealing when the Ant Project Support
     *has already done it.
     * @see org.netbeans.api.project.ant.AntArtifact
     */
    public static final String ARTIFACT_TYPE_UML_PROJ = "umlproj"; // NOI18N
    
    /**
     * This vector holds all Classes and Interfaces in the UML
     * Project(IProject)
     *
     */
//    public static Vector<String> clazzNames = new Vector<String>();
    private UMLProjectMetadataListener mListener = null;
    
    private DataObject obj;
    
    /** Creates a new instance of UMLProject */
    public UMLProject(AntProjectHelper helper)
    {
        mHelper = new UMLProjectHelper(helper, this);
        
        mEval = createEvaluator();
        mImportSupport = new UMLImportsUiSupport(this);
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        mRefHelper = new ReferenceHelper(helper, aux, mEval);
        mGenFilesHelper = new GeneratedFilesHelper(helper);
        
        mLookup = createLookup(aux);
        helper.addAntProjectListener(this);
        
        mListener = new UMLProjectMetadataListener(this);
//        mEval.addPropertyChangeListener(
//                WeakListeners.propertyChange(mListener, mEval));
//
//        mHelper.getAntProjectHelper().addAntProjectListener(
//                (AntProjectListener)WeakListeners.create(
//                AntProjectListener.class, mListener,mHelper));
        mEval.addPropertyChangeListener(mListener);
        mHelper.getAntProjectHelper().addAntProjectListener(mListener);
    }
    
    public Lookup getLookup()
    {
        return mLookup;
    }
    
    private PropertyEvaluator createEvaluator()
    {
        return mHelper.getStandardPropertyEvaluator();
    }
    
    public PropertyEvaluator evaluator()
    {
        return mEval;
    }
    
    public void removeUMLProjectMetaListener()
    {
        evaluator().removePropertyChangeListener(mListener);
        mHelper.getAntProjectHelper().removeAntProjectListener(mListener);
    }
    
    public ReferenceHelper getReferenceHelper()
    {
        return mRefHelper;
    }
    
    public UMLProjectProperties getUMLProjectProperties()
    {
        return uiProperties;
    }
    
    public void setUMLProjectProperties(UMLProjectProperties props)
    {
        uiProperties = props;
    }
    
    // TODO - what is the best lookup criteria for a client code to call
    // to determine if target is a uml project?
    protected Lookup createLookup(AuxiliaryConfiguration aux)
    {
        SubprojectProvider spp = mRefHelper.createSubprojectProvider();
        
        Object[] lookupObjs =
        {
            new Info(),
            aux,
            mHelper.createCacheDirectoryProvider(),
            spp,
            new UMLActionProvider(this, mHelper),
            new UMLPhysicalViewProvider(
                    this, mHelper, mEval, spp, mImportSupport, mRefHelper),
            new CustomizerProviderImpl(this, mHelper, mEval, mRefHelper),
            //new ProjectXMLSavedHook(),
            new ProjectOpenedHookImpl(),
            new RecommendedTemplatesImpl(),
            mHelper,
            // MCF we can't add the IProject at this point because it is not
            // properly initialized yet. And it messes up the first save
            // operation. So we opt to omit it from lookupObjs. It will still
            // be easily accessible via the mHelper.getProject
            //
            // mHelper.getProject(),
            //
            new AntProjectHelperProvider(),
            new AntArtifactProviderImpl(),
            new AssociatedSourceProvider(this, mHelper, mEval)
        };
        return Lookups.fixed(lookupObjs);
    }
    
    public FileObject getProjectDirectory()
    {
        return mHelper.getProjectDirectory();
    }
    
    /** Return configured project name. */
    public String getName()
    {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action()
        {
            public Object run()
            {
                Element data = mHelper.getPrimaryConfigurationData(true);
                
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(
                        UMLProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                        "name"); // NOI18N
                
                if (nl.getLength() == 1)
                {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0)
                    .getNodeType() == Node.TEXT_NODE)
                    {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "???"; // NOI18N
            }
        });
    }
    
    
    public void setName(final String name)
    {
        ProjectManager.mutex().writeAccess(new Mutex.Action()
        {
            public Object run()
            {
                Element data = mHelper.getPrimaryConfigurationData(true);
                
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(
                        UMLProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                        "name"); // NOI18N
                
                Element nameEl;
                if (nl.getLength() == 1)
                {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    
                    while (deadKids.getLength() > 0)
                    {
                        nameEl.removeChild(deadKids.item(0));
                    }
                }
                
                else
                {
                    nameEl = data.getOwnerDocument().createElementNS(
                            UMLProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                            "name"); // NOI18N
                    
                    data.insertBefore(nameEl, data.getChildNodes().item(0));
                }
                
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                mHelper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
    
    protected void retrieveFilenames(File fileObj, IStrings filenames,
            ILanguageManager manager)
    {
        if(fileObj != null)
        {
            if(fileObj.isDirectory() == true)
            {
                File[] children = fileObj.listFiles();
                for(int index = 0; index < children.length; index++)
                {
                    retrieveFilenames(children[index], filenames, manager);
                }
            }
            else
            {
                String path = fileObj.getAbsolutePath();
                if(manager.getLanguageForFile(path) != null)
                {
                    filenames.add(path);
                }
            }
        }
    }
    
    protected IStrings retrieveFilenames(File[] files)
    {
        
        IStrings retVal = new Strings();
        
        try
        {
            ILanguageManager manager = mHelper.getProduct().getLanguageManager();
            for(int index= 0; index < files.length; index++)
            {
                retrieveFilenames(files[index], retVal, manager);
            }
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        return retVal;
    }
    
    public void reverseEngineer(File[] sourceFolders)
    {
        initalizeProperties();
        IStrings files = retrieveFilenames(sourceFolders);
        
        ReverseEngineerTask reTask = new ReverseEngineerTask(
                mHelper.getProject(),
                files,
                false, false, true, true, 
		new ITaskFinishListener() {

		    public void taskFinished()
		    {
			SwingUtilities.invokeLater(new Runnable()
			{
			    public void run()
			    {
				UMLPhysicalViewProvider provider =
				    (UMLPhysicalViewProvider)UMLProject.this.getLookup().
				    lookup(UMLPhysicalViewProvider.class);
				if (provider != null) 
				{
				    ModelRootNodeCookie cookie =
					provider.getModelRootNodeCookie();
				    
				    if (cookie!=null)
					cookie.recalculateChildren();
				}
			    }
		        });			      
		    } 
		});
        
        RequestProcessor processor =
                new RequestProcessor("uml/ReverseEngineer"); // NOI18N
        
        processor.post(reTask);
        
//        final IUMLParsingIntegrator integrator = new UMLParsingIntegrator();
//        integrator.setFiles(files);
//        integrator.reverseEngineer(mHelper.getProject(),
//                false, // this brings up the file chooser
//                false, // this should be false for now.
//                true,  // this will display the progress dialog,
//                true); // this will cause all the classes to
//                       // be created in their own file. Not
//                       // currently enabled
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // AntProjectListener Implementation
    
    public void configurationXmlChanged(AntProjectEvent event)
    {
        if (event.getPath().equals(AntProjectHelper.PROJECT_XML_PATH))
        {
            // Could be various kinds of changes, but name
            //  & displayName might have changed.
            Info info = (Info)getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }
    
    
    public void propertiesChanged(AntProjectEvent antProjectEvent)
    {
        // currently ignored (probably better to listen to
        //  evaluator() if you need to)
    }
    
    
    // MCF - added in desperation. Not sure of best location for "save", jeez
    public void saveProject()
    {
        mHelper.saveProject();
    }
    
    protected void initalizeProperties()
    {
        // MCF I am not sure if this is bad practice, but I don't see the
        // point in reading the properties constantly. Our AssociatedSourceProvider
        // will need to access the referenced project stuff constantly.
        // So I would like to store the reference to the properties for
        // better performance. It does mean that we have to be careful to
        // keep the reference in sync with the one the Customizer uses.
        if(uiProperties == null)
        {
            uiProperties = new UMLProjectProperties(UMLProject.this,
                    mHelper,
                    mEval,
                    mRefHelper );
        }
    }
    
    protected void initializeProjectSettings(IProject project)
    {
        if(getUMLProjectProperties()!= null)
        {
            String mode = getUMLProjectProperties().getProjectMode();
            if(mode.equals(project.getMode()) == false)
            {
                setProjectMode(project, mode);
            }
        }
    }
    
    /**
     * Sets the projects mode.  The mode is used to control the round trip
     * behavior.
     */
    public void setProjectMode(String newMode)
    {
        IProject project = mHelper.getProject();
        setProjectMode(project, newMode);
        
    }
    
    protected void setProjectMode(IProject project, String newMode)
    {
        if(project != null)
        {
            project.setMode(newMode);
        }
    }
   
    
    ////////////////////////////////////////////////////////////////////////////
    // Heper Classes
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook
    {   
        UMLProjectChangeListener listener = new UMLProjectChangeListener();
        ProjectOpenedHookImpl()
        {
        }
        
        /**
         * Retrieves the project from the project helepr.  If the project has retrieved it
         * is initialized.
         */
        protected IProject retreiveProject()
        {
            IProject retVal = null;
            
            try
            {
                retVal = mHelper.getProject();
                initializeProjectSettings(retVal);
                mImportSupport.initializeProject();
            }
            catch(Exception e)
            {
                ErrorManager.getDefault().notify(e);
            }
            
            return retVal;
        }
        
        /**
         * Retreives the project and verifies that the project is valid.
         * The project needs be verified because the product is initialized
         * in a different thread than the thread that opens projects.  Therefore,
         * we need to make sure that the UML product has been initialized before
         * the project is created.
         */
        private IProject temp = null;
        protected IProject verifyProjectIsInitialized()
        {
            temp = retreiveProject();
            
            while(temp == null)
            {
                try
                {
                    SwingUtilities.invokeAndWait(new Runnable()
                    {
                        public void run()
                        {
                            temp = retreiveProject();
                        }
                    });
                }
                catch(Exception e)
                {
                    // Ignore interrupt messages.
                }
            }
            
            return temp;
        }
        
        protected void projectOpened()
        {
            // Cause the helper to initialize the project if it has not already
            // done so.
//            RequestProcessor.getDefault().post(new Runnable()
//            {
//                public void run()
//                {
//                    IProject project = mHelper.getProject();
//                    initializeProjectSettings(project);
//                }
//            });
            
            IProject project = verifyProjectIsInitialized();
            
            // Check up on build scripts.
            // nothing for us yet
            
            
            // register project's classpaths to GlobalPathRegistry
            // nothing for us yet
            
            //register updater of main.class
            //the updater is active only on the opened projects
            // nothing for us yet
            
            // Make it easier to run headless builds on the same machine at least.
            ProjectManager.mutex().writeAccess(new Mutex.Action()
            {
                public Object run()
                {
                    EditableProperties ep = mHelper.getProperties(
                            AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    File buildProperties = new File(System.getProperty(
                            "netbeans.user"), "build.properties"); // NOI18N
                    ep.setProperty("user.properties.file", // NOI18N
                            buildProperties.getAbsolutePath());
                    mHelper.putProperties(
                            AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    
                    try
                    {
                        ProjectManager.getDefault().saveProject(UMLProject.this);
                        
                    }
                    
                    catch (IOException e)
                    {
                        ErrorManager.getDefault().notify(e);
                    }
                    
                    return null;
                }
            });
            
            UMLPhysicalViewProvider physicalViewProvider =
                    (UMLPhysicalViewProvider)UMLProject.this.getLookup()
                    .lookup(UMLPhysicalViewProvider.class);
            
            boolean broken = physicalViewProvider.hasBrokenLinks();
            if (physicalViewProvider != null && (broken == true))
            {
                BrokenReferencesSupport.showAlert();
            }
            
            initalizeProperties();
            
            FileObject dobj = mHelper.getProjectDirectory();
            
            if (dobj != null)
            {
                Project currentUMLProj = FileOwnerQuery.getOwner(dobj);
                String filename = dobj.getPath();
                if(filename!=null && filename.length()>0)
                {
                    filename = normalizeFile(FileUtil.toFile(dobj).getPath());
                }
                org.netbeans.modules.uml.core.metamodel.structure.Project.PROJ_BASE_DIR = filename;
                
                if(broken == false)
                {
                    UMLProjectHelper.scanSourceGroups((UMLProject)currentUMLProj);
                }
                if (project instanceof org.netbeans.modules.uml.core.metamodel.structure.Project)
                {
                    ((org.netbeans.modules.uml.core.metamodel.structure.Project)project).
                            addPropertyChangeListener( listener);
                }
                try
                {
                    File prjFile = new File(project.getProject().getFileName());
                    FileObject fobj = FileUtil.toFileObject(new File(prjFile.getCanonicalPath()));
                    obj = DataObject.find(fobj);
                    if (project.getDirty())
                    {
                        ((UMLProjectDataObject)obj).addSaveCookie();
                        obj.setModified(true);
                    }
                }
                catch (Exception e)
                {
                    ErrorManager.getDefault().log(ErrorManager.EXCEPTION, e.getMessage());
                }
            }
        }
        public String normalizeFile(String filename)
        {
            if(filename!=null && filename.length()>0)
                filename = filename.replace("/", File.separator ).trim(); // NOI18N
            
            return filename;
        }
             
   
        protected void projectClosed()	    
        {                  
	    if (mHelper != null) 
		mHelper.closeProject(false);  
            
            if (mImportSupport != null) 
		mImportSupport.unInitializeProject();
            
	    Lookup l = UMLProject.this.getLookup();
	    if (l != null) 
	    {
		UMLPhysicalViewProvider physicalViewProvider =
                    (UMLPhysicalViewProvider)l.lookup(UMLPhysicalViewProvider.class);
		if (physicalViewProvider != null) 
		    physicalViewProvider.detachLogicalView();
	    }
            
            Project[] projects = ProjectUtil.getOpenUMLProjects();
            // close TCs in case all uml projects are closed
            if (projects.length == 0)
            {
                SwingUtilities.invokeLater( new Runnable()
                {
                    public void run()
                    {
                        TopComponent tc = WindowManager.getDefault().findTopComponent("designpattern");
                        if (tc != null)
                            tc.close();
                        tc = WindowManager.getDefault().findTopComponent("documentation");
                        if (tc != null)
                            tc.close();
                    }
                });
            }
        }
        
        /* listen to IProject change event, and modify project file data object
         * to enable save all button
         */
        private class UMLProjectChangeListener implements PropertyChangeListener
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (org.netbeans.modules.uml.core.metamodel.structure.Project.PROP_DIRTY.
                        equals(evt.getPropertyName()))
                {
                    if (obj instanceof UMLProjectDataObject)
                    {
                        ((UMLProjectDataObject)obj).addSaveCookie();
                        obj.setModified(((Boolean)evt.getNewValue()).booleanValue());
                    }
                }
            }
        }
        
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private final class Info implements ProjectInformation
    {
        
        private final PropertyChangeSupport pcs =
                new PropertyChangeSupport(this);
        
        Info()
        {
        }
        
        void firePropertyChange(String prop)
        {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName()
        {
            return PropertyUtils.getUsablePropertyName(
                    UMLProject.this.getName());
        }
        
        public String getDisplayName()
        {
            return UMLProject.this.getName();
        }
        
        public Icon getIcon()
        {
            return PROJECT_ICON;
        }
        
        public Project getProject()
        {
            return UMLProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener)
        {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener)
        {
            pcs.removePropertyChangeListener(listener);
        }
        
    }
    
    private static final class RecommendedTemplatesImpl
            implements RecommendedTemplates, PrivilegedTemplates
    {
        RecommendedTemplatesImpl()
        {
        }
        // List of primarily supported templates
        private static final String[] APPLICATION_TYPES = new String[]
        {
            "uml-type",         // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[]
        {
            "Templates/UML/newUMLDiagram",  // NOI18N
            "Templates/UML/newUMLPackage",  // NOI18N
            "Templates/UML/newUMLElement"   // NOI18N
        };
        
        public String[] getRecommendedTypes()
        {
            return APPLICATION_TYPES;
        }
        
        public String[] getPrivilegedTemplates()
        {
            return PRIVILEGED_NAMES;
        }
    }
    
    final class AntProjectHelperProvider
    {
        AntProjectHelper getAntProjectHelper()
        {
            return mHelper.getAntProjectHelper();
            
        }
    }
    
    /**
     * Exports the main JAR as an official build product for use from other
     * scripts. The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider
    {
        
        public AntArtifact[] getBuildArtifacts()
        {
            return new AntArtifact[] {
                mHelper.getAntProjectHelper().createSimpleAntArtifact(
                        UMLProject.ARTIFACT_TYPE_UML_PROJ,
                        UMLProjectProperties.UML_PROJECT_ANT_ARTIFACT, evaluator(),
                        "uml", "cleanUml"), // NOI18N
            };
        }
    }
}
