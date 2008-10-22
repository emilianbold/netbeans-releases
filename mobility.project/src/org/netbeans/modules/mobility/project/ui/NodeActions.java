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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * NodeActions.java
 *
 * Created on 16 May 2006, 16:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.project.ui;

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ui.customizer.CustomizerLibraries;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ui.customizer.NewConfigurationPanel;
import org.netbeans.modules.mobility.project.ui.customizer.VisualConfigSupport;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClasspathSupport;
import org.netbeans.modules.project.support.customizer.AntArtifactChooser;
import org.netbeans.modules.project.support.customizer.AntArtifactChooser.ArtifactItem;
import org.netbeans.modules.project.support.customizer.LibrariesChooser;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Lukas Waldmann
 */

class NodeActions {

static abstract class ContextAction extends org.openide.util.actions.NodeAction //AbstractAction implements ContextAwareAction
{
    final private String name;
    
    //Called just for creation common static action representation
    protected ContextAction(String n)
    {
        super();
        name=n;
    }
    
    protected boolean asynchronous()
    {
        return false;
    }

    protected boolean enable(Node[] activatedNodes)
    {
        return true;
    }

    public String getName()
    {
        return name;
    }

    public HelpCtx getHelpCtx()
    {
        return null;
    }
}

static abstract class NodeAction<T> extends ContextAction
{   
    protected FileObject defaultDir = null;
    
    protected NodeAction(String name)
    {
        super(name);
    }
    
    private void perform(final T obj[], final Node node, final J2MEProjectProperties j2meProperties)
    {
        final J2MEProject project=node.getLookup().lookup(J2MEProject.class);
        final ProjectConfiguration conf=node.getLookup().lookup(ProjectConfiguration.class);
        assert project != null;

        String propName;
        if (conf.getDisplayName().equals(project.getConfigurationHelper().getDefaultConfiguration().getDisplayName()))
        {
            propName=DefaultPropertiesDescriptor.LIBS_CLASSPATH;
        }
        else
        {
            propName=J2MEProjectProperties.CONFIG_PREFIX+conf.getDisplayName()+"."+DefaultPropertiesDescriptor.LIBS_CLASSPATH;
        }

        final List<VisualClassPathItem> list=(List)j2meProperties.get(propName);
        List<VisualClassPathItem> newList=new ArrayList<VisualClassPathItem>(list);
        newList=addItems (obj,newList, node);
        
        j2meProperties.put(propName,newList);
    }
    
    static private void save(final HashMap<J2MEProject,J2MEProjectProperties> map)
    {
        // Store all properties after they are set for all nodes
        ProjectManager.mutex().writeAccess( new Runnable() 
        {
            public void run()
            {
                for ( final J2MEProject project : map.keySet())
                {
                    final J2MEProjectProperties j2meProperties = map.get(project);
                    // Store the properties 
                    j2meProperties.store();

                    // And save the project
                    try {
                        ProjectManager.getDefault().saveProject(project);
                    }
                    catch ( IOException ex ) {
                        ErrorManager.getDefault().notify( ex );
                    }
                }
            }
        }); 
    }
    
    static public void pasteAction ( final HashSet<VisualClassPathItem> items, Node node)
    {
        final J2MEProject project=node.getLookup().lookup(J2MEProject.class);
        final ProjectConfiguration conf=node.getLookup().lookup(ProjectConfiguration.class);     
        final HashMap<J2MEProject,J2MEProjectProperties> map = new HashMap<J2MEProject,J2MEProjectProperties>();
        
        assert project != null;
        
        ProjectManager.mutex().writeAccess( new Runnable() 
        {
            public void run()
            {
                final J2MEProjectProperties j2meProperties = new J2MEProjectProperties( project, 
                                                            project.getLookup().lookup(AntProjectHelper.class),
                                                            project.getLookup().lookup(ReferenceHelper.class), 
                                                            project.getConfigurationHelper() );

                String propName;
                if (conf.getDisplayName().equals(project.getConfigurationHelper().getDefaultConfiguration().getDisplayName()))
                {
                    propName=DefaultPropertiesDescriptor.LIBS_CLASSPATH;
                }
                else
                {
                    propName=J2MEProjectProperties.CONFIG_PREFIX+conf.getDisplayName()+"."+DefaultPropertiesDescriptor.LIBS_CLASSPATH;
                }

                final List<VisualClassPathItem> list=(List)j2meProperties.get(propName);
                final HashSet<VisualClassPathItem> set=new HashSet<VisualClassPathItem>(list);
                set.addAll(items);
                list.clear();
                list.addAll(set);

                j2meProperties.put(propName,list);
                map.put(project,j2meProperties);
                save(map);
            }
        });
    }   
    
    synchronized  protected void performAction(Node[] activatedNodes)
    {
        J2MEProject proj=activatedNodes[0].getLookup().lookup(J2MEProject.class);
        //Check if all items are from the same project
        for (Node node : activatedNodes )
        {
            final J2MEProject project=node.getLookup().lookup(J2MEProject.class);
            if (proj != project)
            {
                proj = null;
                break;
            }                
        }   
        
        if (proj != null)
            defaultDir = proj.getProjectDirectory();
        else 
            defaultDir = null;
        
        final T obj[]=getItems();
        
        if (obj!=null)
        {            
            final HashMap<J2MEProject,J2MEProjectProperties> map = new HashMap<J2MEProject,J2MEProjectProperties>();
            for (Node node : activatedNodes )
            {
                final J2MEProject project=node.getLookup().lookup(J2MEProject.class);
                J2MEProjectProperties j2meProperties = map.get(project);
                if (j2meProperties == null)
                {
                    j2meProperties = new J2MEProjectProperties( project, 
                                                                project.getLookup().lookup(AntProjectHelper.class),
                                                                project.getLookup().lookup(ReferenceHelper.class), 
                                                                project.getConfigurationHelper() );
                    map.put(project,j2meProperties);
                }
                perform(obj,node,j2meProperties);
            }
            
            save(map);
        }
    }
    
    abstract protected List<VisualClassPathItem> addItems(T obj[], List<VisualClassPathItem> list, Node node);
    
    abstract protected T[] getItems();
    
}


static class AddLibraryAction extends NodeAction<Library>
{
    final static String aName  = NbBundle.getMessage(CustomizerLibraries.class,"LBL_CustLibs_Add_Library");
    final static Action action = new AddLibraryAction();
    
    private AddLibraryAction()
    {
        super(aName);
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }
    
    protected Library[] getItems()
    {
        Library result[]=null;
        final LibrariesChooser panel = new LibrariesChooser("j2se"); //NOI18N
        final Object[] options = new Object[] {
            NbBundle.getMessage (VisualClasspathSupport.class,"LBL_AddLibrary"), //NOI18N
            NotifyDescriptor.CANCEL_OPTION
        };
        final DialogDescriptor desc = new DialogDescriptor(panel,NbBundle.getMessage( VisualClasspathSupport.class, "LBL_Classpath_AddLibrary" ), //NOI18N
            true, options, options[0], DialogDescriptor.DEFAULT_ALIGN,null,null);
        desc.setHelpCtx (new HelpCtx (LibrariesChooser.class));
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible(true);
        if (desc.getValue() == options[0]) 
        {
            result=panel.getSelectedLibraries();
        }
        dlg.dispose();
        return result;
    }
    
    protected List<VisualClassPathItem> addItems(final Library libraries[], final List<VisualClassPathItem> set, final Node node)
    {
    	for ( Library lib : libraries ) {
            final String libraryName = lib.getName();
            set.add(new VisualClassPathItem( lib,
            VisualClassPathItem.TYPE_LIBRARY,
            "${libs."+libraryName+".classpath}", //NOI18N
            lib.getDisplayName()));
        }
        return set;
    }
}

static class AddFolderAction extends NodeAction<File>
{    
    private static File lastFile = null;
    
    final static String aName  = NbBundle.getMessage(CustomizerLibraries.class,"LBL_CustLibs_Add_Folder");
    final static Action action = new AddFolderAction();
    
    private AddFolderAction()
    {
        super(aName);
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }
    
    protected File[] getItems()
    {
        File files[]=null;
        // Let user search for the Jar file
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        chooser.setMultiSelectionEnabled( true );
        chooser.setDialogTitle( NbBundle.getMessage( VisualClasspathSupport.class, "LBL_Classpath_AddFolder" ) ); // NOI18N
        if (defaultDir != null)
            chooser.setSelectedFile(FileUtil.toFile(defaultDir.getChildren()[0]));
        else if (lastFile != null)
            chooser.setSelectedFile(lastFile);

        final int option = chooser.showOpenDialog( null ); // Sow the chooser

        if ( option == JFileChooser.APPROVE_OPTION ) 
        {
            files = chooser.getSelectedFiles();
            if (files.length > 0) lastFile = files[0];
        }
        return files;
    }
     
    
    protected List<VisualClassPathItem> addItems( File files[], final List<VisualClassPathItem> set, final Node node ) 
    {
    	for ( File file : files ) {
            file = FileUtil.normalizeFile(file);
            set.add(new VisualClassPathItem( file,
                VisualClassPathItem.TYPE_FOLDER,
                null,
                file.getPath()));
        }
        return set;
    }
}


static class RemoveResourceAction extends NodeAction<Object>
{
    final static String aName  = NbBundle.getMessage(CustomizerLibraries.class,"LBL_CustLibs_Remove");
    final static Action action = new RemoveResourceAction();
    final static Object empty[]= new Object[] {};
    
    private RemoveResourceAction()
    {
        super(aName);
        putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }

    protected Object[] getItems()
    {
        return empty;
    }
    
    protected List<VisualClassPathItem> addItems(final Object[] items, final List<VisualClassPathItem> set, final Node node )
    {
        final J2MEProject project=node.getLookup().lookup(J2MEProject.class);
        final VisualClassPathItem  item=node.getLookup().lookup(VisualClassPathItem.class);
        final PropertyEvaluator eval=project.getLookup().lookup(AntProjectHelper.class).getStandardPropertyEvaluator();
        List<VisualClassPathItem> newSet=new ArrayList<VisualClassPathItem>(set);
        
        for ( final VisualClassPathItem vcp : set ) {
            if (vcp.equals(item))
            {
                newSet.remove(vcp);
                break;
            }
        }
        return newSet;
    }
}

static class AddProjectAction extends NodeAction<ArtifactItem>
{
    final static String aName  = NbBundle.getMessage(CustomizerLibraries.class,"LBL_CustLibs_Add_Project");
    final static Action action = new AddProjectAction();
    
    private AddProjectAction()
    {
        super(aName);
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }

    protected ArtifactItem[] getItems() 
    {
        return AntArtifactChooser.showDialog( JavaProjectConstants.ARTIFACT_TYPE_JAR );
    }
    
    protected List<VisualClassPathItem> addItems(final ArtifactItem art[], final List<VisualClassPathItem> set, final Node node )
    {
        final J2MEProject project=node.getLookup().lookup(J2MEProject.class);
        
        for (ArtifactItem arti : art ) {
            final AntArtifact artifact = arti.getArtifact();
            final URI uri = arti.getURI();
            String location;
            try {
                location = FileUtil.normalizeFile(new File(artifact.getScriptLocation().getParentFile().toURI().resolve(uri))).getPath();
            } catch (Exception e) {
                location = uri.getPath();
            }
            final Project p = artifact.getProject();
            if (p!= null && p!=project)
            {
                set.add(new VisualClassPathItem( artifact, uri, 
                                                  VisualClassPathItem.TYPE_ARTIFACT,
                                                  null,
                                                  location ));
            }
        }
        return set;
    }
}


static class AddJarAction extends NodeAction<File>
{
    private static File lastFile = null;
    
    final static String aName  = NbBundle.getMessage(CustomizerLibraries.class,"LBL_CustLibs_Add_Jar");
    final static Action action = new AddJarAction();
    
    private AddJarAction()
    {
        super(aName);
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }
    
    private static class JarFileFilter extends FileFilter {
        
        JarFileFilter() {
            //to avoid creation of accessor class
        }
        
        public boolean accept(final File f) {
            final String s = f.getName().toLowerCase();
            return f.isDirectory() || s.endsWith(".zip") || s.endsWith(".jar"); //NOI18N
        }
        
        public String getDescription() {
            return NbBundle.getMessage( VisualClasspathSupport.class, "LBL_JarFileFilter"); //NOI18N
        }
    }

    protected File[] getItems()
    {
        File files[]=null;
        // Let user search for the Jar file
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        chooser.setMultiSelectionEnabled( true );
        chooser.setDialogTitle( NbBundle.getMessage( VisualClasspathSupport.class, "LBL_Classpath_AddJar" ) ); // NOI18N
        chooser.setFileFilter(new JarFileFilter());
        chooser.setAcceptAllFileFilterUsed( false );
        if (defaultDir != null)
            chooser.setSelectedFile(FileUtil.toFile(defaultDir.getChildren()[0]));
        else if (lastFile != null) 
            chooser.setSelectedFile(lastFile);
        
        final int option = chooser.showOpenDialog( null ); // Sow the chooser

        if ( option == JFileChooser.APPROVE_OPTION ) 
        {
            files = chooser.getSelectedFiles();
            if (files.length > 0) lastFile = files[0];
        }
        return files;
    }

    protected List<VisualClassPathItem> addItems(File files[], final List<VisualClassPathItem> set, final Node node )
    {
    	for ( File file : files ) {
            file = FileUtil.normalizeFile(file);
            set.add(new VisualClassPathItem( file,
                VisualClassPathItem.TYPE_JAR,
                null,
                file.getPath() ) );
        }
        return set;
    }
}

static class SetConfigurationAction extends ContextAction
{
    final static String aName  = NbBundle.getMessage(SetConfigurationAction.class,"LBL_SACAction_SetConfiguration");
    final static Action action = new SetConfigurationAction();
    
    private SetConfigurationAction()
    {
        super(aName);
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }
    
    protected boolean enable(final Node[] activatedNodes)
    {
        if (activatedNodes.length == 1)
        {
            final J2MEProject project=activatedNodes[0].getLookup().lookup(J2MEProject.class);
            final ProjectConfiguration conf=activatedNodes[0].getLookup().lookup(ProjectConfiguration.class);
            if (project != null)
            {
                if (!project.getConfigurationHelper().getActiveConfiguration().equals(conf))
                    return true;
            }         
        }
        return false;
    }

    protected void performAction(final Node[] activatedNodes)
    {
        final J2MEProject project=activatedNodes[0].getLookup().lookup(J2MEProject.class);
        final ProjectConfiguration conf=activatedNodes[0].getLookup().lookup(ProjectConfiguration.class);
        try {
            project.getConfigurationHelper().setActiveConfiguration(conf);
        } catch (IllegalArgumentException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
}

static class AddConfigurationAction extends ContextAction
{
    final static String aName  = NbBundle.getMessage(CustomizerLibraries.class,"LBL_VCS_AddConfiguration");
    final static Action action = new AddConfigurationAction();
    
    private AddConfigurationAction()
    {
        super(aName);
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }

    protected boolean enable(final Node[] activatedNodes)
    {
        return activatedNodes.length == 1;
    }
    
    protected void performAction(final Node[] activatedNodes) 
    {
        final J2MEProject project=activatedNodes[0].getLookup().lookup(J2MEProject.class);
        final J2MEProjectProperties j2meProperties = new J2MEProjectProperties( project,
                project.getLookup().lookup(AntProjectHelper.class),
                project.getLookup().lookup(ReferenceHelper.class),
                project.getConfigurationHelper() );
        final ArrayList<ProjectConfiguration> allNames=new ArrayList<ProjectConfiguration>();
        ProjectManager.mutex().postReadRequest(new Runnable() {
                public void run() {
                    allNames.addAll(Arrays.asList(j2meProperties.getConfigurations()));
                }
            });
        final ArrayList<String> names = new ArrayList<String>(allNames.size());
        for ( ProjectConfiguration cfg : allNames )
            names.add(cfg.getDisplayName());
        final NewConfigurationPanel ncp = new NewConfigurationPanel(names);
        final DialogDescriptor dd = new DialogDescriptor(ncp, NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_AddConfiguration"), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NOI18N
        ncp.setDialogDescriptor(dd);
        final String newName = NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd)) ? ncp.getName() : null;
        if (newName != null) {
            ProjectManager.mutex().postWriteRequest( new Runnable()
            {
                public void run()
                {
                    if (addNewConfig(project, newName, j2meProperties, ncp, allNames))
                    {
                        // And save the project
                        try {
                            ProjectManager.getDefault().saveProject(project);
                        }
                        catch ( IOException ex ) {
                            ErrorManager.getDefault().notify( ex );
                        }
                    }
                }
            });
        }
    }


    private boolean addNewConfig( J2MEProject project, final String newName, J2MEProjectProperties j2meProperties, NewConfigurationPanel ncp, List <ProjectConfiguration> allNames )
    {
        if (newName != null) 
        {
            final ProjectConfiguration cfg = new ProjectConfiguration() 
            {
                public String getDisplayName() {
                    return newName;
                }
            };
            VisualConfigSupport.createFromTemplate(j2meProperties,newName,ncp.getTemplate());
            allNames.add(cfg);
            j2meProperties.setConfigurations(allNames.toArray(new ProjectConfiguration[allNames.size()]));

            // Store the properties 
            j2meProperties.store();
            try {
                project.getConfigurationHelper().setActiveConfiguration(cfg); 
                return true;
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            } 
        }
        return false;
    }
}

static abstract class AntAction extends ContextAction
{
    final String command;
    
    AntAction(String aName, String comm)
    {
        super(aName);
        command=comm;
    }
    
    protected void performAction(final Node[] activatedNodes)
    {
        final HashMap<J2MEProject,String> todo=new HashMap<J2MEProject,String>();        
               
        for (Node node : activatedNodes)
        {
            J2MEProject project=node.getLookup().lookup(J2MEProject.class);
            String tobuild=todo.get(project);
            if (tobuild==null)
            {
                todo.put(project,tobuild=new String());
            }                
            String conf=node.getLookup().lookup(ProjectConfiguration.class).getDisplayName();
            String comma=(tobuild==null||tobuild.length()==0)?"":",";
            if (ProjectConfigurationsHelper.DEFAULT_CONFIGURATION_NAME.equals(conf))
                tobuild=" "+comma+tobuild;
            else
                tobuild+=comma+conf;
            todo.put(project,tobuild);
        }
        
        final Runnable action = new Runnable() 
        {
            public void run() 
            {
                for (Map.Entry<J2MEProject,String> entry : todo.entrySet())
                {
                    int tokens=new StringTokenizer(entry.getValue(),",").countTokens();
                    final String[] targetNames=new String[] {command};
                    final Properties props=new Properties();
                    props.put(DefaultPropertiesDescriptor.SELECTED_CONFIGURATIONS,
                              entry.getValue());
                    
                    try 
                    {
                        ActionUtils.runTarget(entry.getKey().getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH), 
                                              targetNames, props);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
        };
        
        action.run();
    }
}

static class BuildConfigurationAction extends AntAction
{
    final static String aName  = NbBundle.getMessage(ContextAction.class,"Title_CfgSelection_build-all");
    final static Action action = new BuildConfigurationAction();
    
    private BuildConfigurationAction()
    {
        super(aName,"build-all");
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }
    
}

static class CleanConfigurationAction extends AntAction
{
    final static String aName  = NbBundle.getMessage(ContextAction.class,"Title_CfgSelection_clean-all");
    final static Action action = new CleanConfigurationAction();
    
    private CleanConfigurationAction()
    {
        super(aName,"clean-all");
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }
    
}

static class CleanAndBuildConfigurationAction extends AntAction
{
    final static String aName  = NbBundle.getMessage(ContextAction.class,"Title_CfgSelection_rebuild-all");
    final static Action action = new CleanAndBuildConfigurationAction();
    
    private CleanAndBuildConfigurationAction()
    {
        super(aName,"rebuild-all");
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }
    
}


static class DeployConfigurationAction extends AntAction
{
    final static String aName  = NbBundle.getMessage(ContextAction.class,"Title_CfgSelection_deploy-all");
    final static Action action = new DeployConfigurationAction();
    
    private DeployConfigurationAction()
    {
        super(aName,"deploy-all");
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }
    
}

static abstract class AntSingleAction extends ContextAction
{
    final String command;
    
    AntSingleAction(String aName, String comm)
    {
        super(aName);
        command=comm;
    }
    
    protected void performAction(Node[] activatedNodes)
    {
        String conf=activatedNodes[0].getLookup().lookup(ProjectConfiguration.class).getDisplayName();
        J2MEProject project=activatedNodes[0].getLookup().lookup(J2MEProject.class);
        
        final String[] targetNames=new String[] {command};
        final Properties props=new Properties();
        props.put(DefaultPropertiesDescriptor.CONFIG_ACTIVE,conf);

        try 
        {
            ActionUtils.runTarget(project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH), 
                                  targetNames, props);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    protected boolean enable(final Node[] activatedNodes)
    {
        return activatedNodes.length == 1;
    }
}


static class RunConfigurationAction extends AntSingleAction
{
    final static String aName  = NbBundle.getMessage(ContextAction.class,"LBL_RunConfigurationAction_Name");
    final static Action action = new RunConfigurationAction();
    
    private RunConfigurationAction()
    {
        super(aName,"run");
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }
}

static class DebugConfigurationAction extends AntSingleAction
{
    final static String aName  = NbBundle.getMessage(ContextAction.class,"LBL_DebugConfigurationAction_Name");
    final static Action action = new DebugConfigurationAction();
    
    private DebugConfigurationAction()
    {
        super(aName,"debug");
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }
}



static class RemoveConfigurationAction extends ContextAction
{
    final static String aName  = NbBundle.getMessage(CustomizerLibraries.class,"ACSN_RemovePanel");
    final static Action action = new RemoveConfigurationAction();
    
    private RemoveConfigurationAction()
    {
        super(aName);
    }
    
    public static Action getStaticInstance()
    {
        return action;
    }
    
    protected void performAction(final Node[] activatedNodes)
    {
        StringBuffer buffer=new StringBuffer();
        for (Node node : activatedNodes)
        {
            ProjectConfiguration conf=node.getLookup().lookup(ProjectConfiguration.class);
            buffer.append('\n').append(conf.getDisplayName());
        }
        
        final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(NbBundle.getMessage(VisualConfigSupport.class, "LBL_VCS_ReallyRemove", buffer), NotifyDescriptor.YES_NO_OPTION); //NOI18N
        if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) 
        {   
            ProjectManager.mutex().postWriteRequest( new Runnable() 
            {
                public void run()
                {
                    for (Node node : activatedNodes)
                    {
                        final ProjectConfiguration conf=node.getLookup().lookup(ProjectConfiguration.class);
                        final J2MEProject project=node.getLookup().lookup(J2MEProject.class);
                        
                        removeProperties(project, conf); 

                        // And save the project
                        try {
                            ProjectManager.getDefault().saveProject(project);
                        }
                        catch ( IOException ex ) {
                            ErrorManager.getDefault().notify( ex );
                        }                        
                    }
                }
            });
        }
    }        

    private void removeProperties(final J2MEProject project, final ProjectConfiguration conf) 
    {
        final J2MEProjectProperties j2meProperties = new J2MEProjectProperties( project, 
                project.getLookup().lookup(AntProjectHelper.class),
                project.getLookup().lookup(ReferenceHelper.class), 
                project.getConfigurationHelper() );

        final String keys[] = j2meProperties.keySet().toArray(new String[j2meProperties.size()]);
        final String prefix = J2MEProjectProperties.CONFIG_PREFIX + conf.getDisplayName();
        for (int i=0; i<keys.length; i++) 
        {
            if (keys[i].startsWith(prefix))
                j2meProperties.remove(keys[i]);
        }
        final List<ProjectConfiguration> col=Arrays.asList(j2meProperties.getConfigurations());
        final ArrayList<ProjectConfiguration> list=new ArrayList<ProjectConfiguration>();
        list.addAll(col);
        list.remove(conf);

        //Set active in case we are deleting active
        if (project.getConfigurationHelper().getActiveConfiguration().equals(conf)) try {
                project.getConfigurationHelper().setActiveConfiguration(project.getConfigurationHelper().getDefaultConfiguration());
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            } 

        j2meProperties.setConfigurations(list.toArray(new ProjectConfiguration[list.size()]));

        // Store the properties 
        j2meProperties.store();
    }
}

}
