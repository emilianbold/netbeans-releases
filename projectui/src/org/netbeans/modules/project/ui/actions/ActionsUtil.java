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

package org.netbeans.modules.project.ui.actions;

import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakSet;

/** Nice utility methods to be used in ProjectBased Actions
 * 
 * @author Pet Hrebejk 
 */
class ActionsUtil {
    
    public static LookupResultsCache lookupResultsCache;
    
    public static final ShortcutsManager SHORCUTS_MANAGER = new ShortcutsManager();
    
    public static HashMap /*<String,MessageFormat>*/ pattern2format = new HashMap(); 
    
    /** Finds all projects in given lookup. If the command is not null it will check 
     * whther given command is enabled on all projects. If and only if all projects
     * have the command supported it will return array including the project. If there
     * is one project with the command disabled it will return empty array.
     */
    public static Project[] getProjectsFromLookup( Lookup lookup, String command ) {    
        
        if ( lookupResultsCache == null ) {
            lookupResultsCache = new LookupResultsCache( new Class[] { Project.class, DataObject.class } );
        }
         
        Project[] projectsArray = lookupResultsCache.getProjects( lookup );
                
        if ( command != null ) {
            // All projects have to have the command enabled
            for( int i = 0; i < projectsArray.length; i++ ) {
                if ( !commandSupported( projectsArray[i], command, lookup ) ) {
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
        HashSet result = new HashSet();
        Collection dataObjects = lookup.lookup( new Lookup.Template( DataObject.class ) ).allInstances();
        for( Iterator it = dataObjects.iterator(); it.hasNext(); ) {
            DataObject dObj = (DataObject)it.next();
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
        ActionProvider ap = (ActionProvider)project.getLookup().lookup( ActionProvider.class );
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
            return ActionsUtil.formatName( namePattern, projects.length, ProjectUtils.getInformation( projects[0] ).getDisplayName() );
        }
    }

    
    /** Good for formating names of actions with some two parameter pattern
     * {0} nuber of objects (e.g. Projects or files ) and {1} name of one
     * or first object (e.g. Project or file) or null if the number is == 0
     * {2} whats the type of the name 0 == normal, 1 == menu, 2 == popup
     */  
    public static String formatName( String namePattern, int numberOfObjects, String firstObjectName ) {
        
        MessageFormat mf = null;
        
        synchronized ( pattern2format ) {
            mf = (MessageFormat)pattern2format.get( namePattern );
            if ( mf == null ) {
                mf = new MessageFormat( namePattern );
                pattern2format.put( namePattern, mf );
            }
        }
                
        StringBuffer result = new StringBuffer();
        
        mf.format( 
            new Object[] {
                new Integer( numberOfObjects ),
                firstObjectName == null ? "" : firstObjectName,
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
        HashMap shorcuts = new HashMap(); 
        
        // command -> WeakSet of actions
        HashMap actions = new HashMap();
        
        
        public void registerAction( String command, Action action ) {
            
            synchronized ( this ) {
                Set commandActions = (Set)actions.get( command );

                if ( commandActions == null ) {
                    commandActions = new WeakSet();
                    actions.put( command, commandActions );                
                }
                
                commandActions.add( action );
                                
            }
            
            Object shorcut = getShortcut( command );
            
            if ( shorcut != null ) {
                action.putValue( Action.ACCELERATOR_KEY, shorcut );                
            }
            
        }
        
        
        public void registerShorcut( String command, Object shortcut ) {
            
            Set actionsToChange = null;
            
            synchronized ( this ) {
                
                Object exShorcut = getShortcut( command );
                
                if ( ( exShorcut != null && exShorcut.equals( shortcut ) ) ||  // Shorcuts are equal
                     ( exShorcut == null && shortcut == null ) ) {             // or both are null  
                    return; // No action needed
                }
                                
                shorcuts.put( command, shortcut );
                
                Set commandActions = (Set)actions.get( command );
                if ( commandActions != null && !commandActions.isEmpty() ) {
                    actionsToChange = new HashSet();
                    actionsToChange.addAll( commandActions );
                }
                
            }
                        
            if ( actionsToChange != null ) {
                // Need to change actions in existing actions
                for( Iterator it = actionsToChange.iterator(); it.hasNext(); ) {
                    Action a = (Action)it.next();
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
    
    /** Caches the projects and files included in the last quried lookup
     */
    private static class LookupResultsCache implements LookupListener {
        
        private Class watch[];
        
        private Lookup lruLookup;
        private Lookup.Result lruResults[];
        private Project[] projects;
                
        LookupResultsCache( Class[] watch ) {
            this.watch = watch;
        }
        
        public Project[] getProjects( Lookup lookup ) {
            
            if ( lookup != lruLookup ) { // Lookup changed
                if ( lruResults != null ) {
                    for( int i = 0; i < watch.length; i++ ) {
                        lruResults[i].removeLookupListener( this ); // Deregister
                    }        
                    lruResults = null;
                }
                makeDirty();
                lruLookup = null;
            }
            
            if ( lruLookup == null ) { // Needs to attach to lookup
                lruLookup = lookup;
                lruResults = new Lookup.Result[watch.length];
                for ( int i = 0; i < watch.length; i++ ) {
                    lruResults[i] = lookup.lookup( new Lookup.Template( watch[i] ) );
                    lruResults[i].allItems();
                    lruResults[i].addLookupListener( this ); 
                }                
            }
            
            if ( isDirty() ) { // Needs to recompute the result
                
                Set result = new HashSet();

                // First find out whether there is a project directly in the Lookup                        
                Collection currentProjects = lruLookup.lookup( new Lookup.Template( Project.class ) ).allInstances();
                
                for( Iterator it = currentProjects.iterator(); it.hasNext(); ) {
                    Project p = (Project)it.next();
                    result.add(p);
                }

                // Now try to guess the project from dataobjects
                Collection currentDataObjects = lruLookup.lookup( new Lookup.Template( DataObject.class ) ).allInstances();
                for( Iterator it = currentDataObjects.iterator(); it.hasNext(); ) {

                    DataObject dObj = (DataObject)it.next();
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
        
        private void makeDirty() {
            projects = null;
        }
                
        // Lookup listener implementation --------------------------------------
        
        public void resultChanged( LookupEvent e ) {
            makeDirty();
        }
        
    }
    

}