/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.project.ui.actions;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.WeakSet;

/** Nice utility methods to be used in ProjectBased Actions
 * 
 * @author Pet Hrebejk 
 */
class ActionsUtil {
    
    /*
    public static LookupResultsCache lookupResultsCache;
     */
    
    public static final ShortcutsManager SHORCUTS_MANAGER = new ShortcutsManager();
    
    public static HashMap<String,MessageFormat> pattern2format = new HashMap<String,MessageFormat>(); 
    
    /** Finds all projects in given lookup. If the command is not null it will check 
     * whther given command is enabled on all projects. If and only if all projects
     * have the command supported it will return array including the project. If there
     * is one project with the command disabled it will return empty array.
     */
    public static Project[] getProjectsFromLookup( Lookup lookup, String command ) {    
        /*
        if ( lookupResultsCache == null ) {
            lookupResultsCache = new LookupResultsCache( new Class[] { Project.class, DataObject.class } );
        }
        
        Project[] projectsArray = lookupResultsCache.getProjects( lookup );
         */
        // #74161: do not cache
        // First find out whether there is a project directly in the Lookup
        Set<Project> result = new HashSet<Project>();
        for (Project p : lookup.lookupAll(Project.class)) {
            result.add(p);
        }
        // Now try to guess the project from dataobjects
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null ) {
                result.add( p );
            }
        }
        Project[] projectsArray = result.toArray(new Project[result.size()]);
                
        if ( command != null ) {
            // All projects have to have the command enabled
            for (Project p : projectsArray) {
                if (!commandSupported(p, command, lookup)) {
                    return new Project[0];
                }
            }
        }
        
        return projectsArray;
    }

    /** In given lookup will find all FileObjects owned by given project
     * with given command supported.
     */    
    public static FileObject[] getFilesFromLookup( Lookup lookup, Project project ) {
        HashSet<FileObject> result = new HashSet<FileObject>();
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null && p.equals( project ) ) {
                result.add( fObj );                                        
            }

        }
        
        FileObject[] fos = new FileObject[ result.size() ];
        result.toArray( fos );        
        return fos;
    }
    
    
    /** 
     * Tests whether given command is available on the project and whether
     * the action as to be enabled in current Context
     * @param project Project to test
     * @param command Command for test
     * @param context Lookup representing current context or null if context
     *                does not matter.
     */    
    public static boolean commandSupported( Project project, String command, Lookup context ) {
        //We have to look whether the command is supported by the project
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        if ( ap != null ) {
            List commands = Arrays.asList( ap.getSupportedActions() );
            if ( commands.contains( command ) ) {
                if (context == null || ap.isActionEnabled(command, context)) {
                    //System.err.println("cS: true project=" + project + " command=" + command + " context=" + context);
                    return true;
                }
            }
        }            
        //System.err.println("cS: false project=" + project + " command=" + command + " context=" + context);
        return false;
    }
    
    
    
    public static String formatProjectSensitiveName( String namePattern, Project projects[] ) {
     
        // Set the action's name
        if ( projects == null || projects.length == 0 ) {
            // No project selected                 
            return ActionsUtil.formatName( namePattern, 0, null );
        }
        else {
            // Some project selected 
            // XXX what about passing an object that computes the name lazily
             return ActionsUtil.formatName( namePattern, projects.length, new Wrapper(projects[0]));
        }
    }
    
    private static class Wrapper {
        Wrapper(Project prj) {
            project = prj;
        }
        private Project project;

        @Override
        public String toString() {
            return ProjectUtils.getInformation( project ).getDisplayName();
        }
        
    }

    
    /** Good for formating names of actions with some two parameter pattern
     * {0} nuber of objects (e.g. Projects or files ) and {1} name of one
     * or first object (e.g. Project or file) or null if the number is == 0
     * {2} whats the type of the name 0 == normal, 1 == menu, 2 == popup
     */  
    public static String formatName( String namePattern, int numberOfObjects, Object firstObjectName ) {
        
        MessageFormat mf = null;
        
        synchronized ( pattern2format ) {
            mf = pattern2format.get(namePattern);
            if ( mf == null ) {
                mf = new MessageFormat( namePattern );
                pattern2format.put( namePattern, mf );
            }
        }
                
        StringBuffer result = new StringBuffer();
        
        mf.format( 
            new Object[] {
                numberOfObjects,
                firstObjectName == null ? "" : firstObjectName.toString(),
            }, 
            result, 
            null );            
            
        return result.toString();
    }
      
    
    // Innerclasses ------------------------------------------------------------
    
    /** Manages shortcuts based on the action's command. Usefull for File and
     * projects actions.
     */
    
    public static class ShortcutsManager {
        
        // command -> shortcut
        Map<String,Object> shorcuts = new HashMap<String, Object>(); 
        
        // command -> WeakSet of actions
        HashMap<String, Set<Action>> actions = new HashMap<String, Set<Action>>();
        
        
        public void registerAction( String command, Action action ) {
            
            synchronized ( this ) {
                Set<Action> commandActions = actions.get( command );

                if ( commandActions == null ) {
                    commandActions = new WeakSet<Action>();
                    actions.put( command, commandActions );                
                }
                
                commandActions.add( action );
                                
            }
            
            Object shorcut = getShortcut( command );
            
            if ( shorcut != null ) {
                action.putValue( Action.ACCELERATOR_KEY, shorcut );                
            }
            
        }
        
        
        public void registerShortcut( String command, Object shortcut ) {
            
            Set<Action> actionsToChange = null;
            
            synchronized ( this ) {
                
                Object exShorcut = getShortcut( command );
                
                if ( ( exShorcut != null && exShorcut.equals( shortcut ) ) ||  // Shorcuts are equal
                     ( exShorcut == null && shortcut == null ) ) {             // or both are null  
                    return; // No action needed
                }
                                
                shorcuts.put( command, shortcut );
                
                Set<Action> commandActions = actions.get( command );
                if ( commandActions != null && !commandActions.isEmpty() ) {
                    actionsToChange = new HashSet<Action>();
                    actionsToChange.addAll( commandActions );
                }
                
            }
                        
            if ( actionsToChange != null ) {
                // Need to change actions in existing actions
                for (Action a : actionsToChange) {
                    if ( a != null ) {
                        a.putValue( Action.ACCELERATOR_KEY, shortcut );
                    }                    
                }
            }
            
        }
        
        public synchronized Object getShortcut( String command ) {            
            return shorcuts.get( command );
        }
                
    }
    
    /** Caches the projects and files included in the last quried lookup.
     *
     * Using weak references to fix issue #67846. Please note that holding the
     * lookup results weak may cause that the cache will miss much more often
     * than strictly necessary, but it is the best solution found so far.
     * Holding the results weak should not break the correctness, it may only
     * cause the cache will not work very well (or not at all).
     *
     * Please see also the tests.
     */
    /* XXX #74161: does not actually work
    private static class LookupResultsCache implements LookupListener {
        
        private Class<?> watch[];
        
        private Reference<Lookup> lruLookup;
        private List<Reference<Lookup.Result>> lruResults;
        private Project[] projects;
                
        LookupResultsCache( Class[] watch ) {
            this.watch = watch;
        }
        
        public synchronized Project[] getProjects( Lookup lookup ) {
            Lookup lruLookupLocal = lruLookup != null ? lruLookup.get() : null;
            
            if ( lookup != lruLookupLocal ) { // Lookup changed
                if ( lruResults != null ) {
                    for (Reference<Lookup.Result> r : lruResults) {
                        Lookup.Result result = r.get();
                        if (result != null) {
                            result.removeLookupListener( this ); // Deregister
                        }
                    }        
                    lruResults = null;
                }
                makeDirty();
                lruLookupLocal = null;
            }
            
            if ( lruLookupLocal == null ) { // Needs to attach to lookup
                lruLookup = new CleanableWeakReference<Lookup>(lruLookupLocal = lookup);
                lruResults = new ArrayList<Reference<Lookup.Result>>();
                for (Class<?> c : watch) {
                    Lookup.Result result = lookup.lookupResult(c);
                    
                    result.allItems();
                    result.addLookupListener( this );
                    
                    lruResults.add(new CleanableWeakReference<Lookup.Result>(result));
                }                
            }
            
            if ( isDirty() ) { // Needs to recompute the result
                
                Set<Project> result = new HashSet<Project>();

                // First find out whether there is a project directly in the Lookup
                for (Project p : lruLookupLocal.lookupAll(Project.class)) {
                    result.add(p);
                }

                // Now try to guess the project from dataobjects
                for (DataObject dObj : lruLookupLocal.lookupAll(DataObject.class)) {
                    FileObject fObj = dObj.getPrimaryFile();
                    Project p = FileOwnerQuery.getOwner(fObj);
                    if ( p != null ) {
                        result.add( p );                                        
                    }

                }

                projects = new Project[ result.size() ];
                result.toArray( projects );        

            }
                        
            return projects;
        }
                        
                
        private boolean isDirty() {
            return projects == null;
        }
        
        private synchronized void makeDirty() {
            projects = null;
        }
                
        // Lookup listener implementation --------------------------------------
        
        public void resultChanged( LookupEvent e ) {
            makeDirty();
        }
        
        private class CleanableWeakReference<T> extends WeakReference<T> implements Runnable {
            
            public CleanableWeakReference(T o) {
                super(o, Utilities.activeReferenceQueue());
            }

            public void run() {
                synchronized (LookupResultsCache.this) {
                    lruLookup  = null;
                    lruResults = null;
                    projects   = null;
                }
            }
        }
        
    }
     */

}
