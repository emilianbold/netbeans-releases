/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.actions.EditAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 * Various hacks that should be solved better later.
 */
public class Hacks {
    
    /** @author Petr Hrebejk */
    static void hackFolderActions() {
        
                
        try {
            Class folderLoaderClass = Class.forName( "org.openide.loaders.DataLoaderPool$FolderLoader" );
            
            SharedClassObject folderLoader = SharedClassObject.findObject( folderLoaderClass );
            ((DataLoader)folderLoader).getActions();
            
            
            Method getProperty = SharedClassObject.class.getDeclaredMethod( "getProperty", new Class[] { Object.class } );
            getProperty.setAccessible( true );
            Method putProperty = SharedClassObject.class.getDeclaredMethod( "putProperty", new Class[] { Object.class, Object.class } );
            putProperty.setAccessible( true );
            
            SystemAction defaultActions[] = (SystemAction[])getProperty.invoke( folderLoader, new Object[] { "defaultActions" } );
            
            ArrayList newActions = new ArrayList();
            for( int i = 0; i < defaultActions.length; i++ ) {

                //System.out.println( i + " " + defaultActions[i] );
                
                if ( defaultActions[i] == null ) {
                    newActions.add( defaultActions[i]);
                    continue;
                }
                
                String className = defaultActions[i].getClass().getName();
                
                if ( "org.openide.actions.NewTemplateAction".equals( className ) ) {
                    newActions.add( PlaceHolderAction.NewFile.get( PlaceHolderAction.NewFile.class ) );
                }
                else {
                    newActions.add( defaultActions[i]);
                }
            }

            defaultActions = new SystemAction[ newActions.size() ];
            newActions.toArray( defaultActions );
           
            putProperty.invoke( folderLoader, new Object[]{ "defaultActions", defaultActions } );
        }
        catch( ClassNotFoundException e ) {
            System.err.println( e );
        }
        catch( NoSuchMethodException e ) {
            e.printStackTrace();
        }
        catch( IllegalAccessException e ) {
            System.err.println( e );
        }
        catch( InvocationTargetException e  ) {
            System.err.println( e );
        }
        
        
    }
    
    private static Object windowSystemImpl = null;
    private static Method setProjectName = null;
    /**
     * Show name of project corresponding to selection in Main Window title bar.
     * @author Jesse Glick
     */
    static void keepCurrentProjectNameUpdated() {
        try {
            Class windowSystemImplClazz = Class.forName(
                "org.netbeans.core.NbTopManager$WindowSystem", true, 
                Thread.currentThread().getContextClassLoader());
            windowSystemImpl = Lookup.getDefault().lookup(windowSystemImplClazz);
            if (windowSystemImpl != null) {
                setProjectName = windowSystemImplClazz.getMethod(
                    "setProjectName", new Class[] {String.class});
            }
        } catch (Exception e) {
            // OK.
            e.printStackTrace();
        }
        if (setProjectName != null) {
            final TopComponent.Registry r = TopComponent.getRegistry();
            r.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(ev.getPropertyName())) {
                        Node[] sel = r.getActivatedNodes();
                        Set/*<Project>*/ projects = new HashSet();
                        for (int i = 0; i < sel.length; i++) {
                            Lookup l = sel[i].getLookup();
                            Project p = (Project)l.lookup(Project.class);
                            if (p != null) {
                                projects.add(p);
                            } else {
                                DataObject d = (DataObject)l.lookup(DataObject.class);
                                if (d != null) {
                                    FileObject f = d.getPrimaryFile();
                                    p = FileOwnerQuery.getOwner(f);
                                    if (p != null) {
                                        projects.add(p);
                                    }
                                }
                            }
                        }
                        String pname;
                        if (projects.size() == 1) {
                            pname = ProjectUtils.getInformation((Project)projects.iterator().next()).getDisplayName();
                            assert pname != null;
                        } else if (projects.isEmpty()) {
                            pname = "No Project"; // XXX I18N
                        } else {
                            pname = "Multiple Projects"; // XXX I18N
                        }
                        try {
                            setProjectName.invoke(windowSystemImpl, new Object[] {pname});
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
    
    /**
     * Set the disk roots by default to ignore the same junk files that Ant
     * itself ignores by default.
     * CURRENTLY UNUSED: cf #40400
     * @author Jesse Glick
     */
    static void setDefaultExcludesList() {
        if (System.getProperty(DEFAULT_EXCLUDES_REGEXP_PROP) == null) {
            System.setProperty(DEFAULT_EXCLUDES_REGEXP_PROP, DEFAULT_EXCLUDES_REGEXP);
        }
    }
    /** @see org.netbeans.core.ExLocalFileSystem#IGNORED_FILES_PROP */
    private static final String DEFAULT_EXCLUDES_REGEXP_PROP = "netbeans.ignored.files"; // NOI18N
    /** @see org.apache.tools.ant.DirectoryScanner#DEFAULTEXCLUDES */
    private static final String DEFAULT_EXCLUDES_REGEXP =
        "^(CVS|SCCS|vssver\\.scc|#.*#|%.*%|\\.(cvsignore|svn|DS_Store))$|^\\.[#_]|~$"; // NOI18N
    
}
