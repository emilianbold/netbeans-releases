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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/** Nice utility methods to be used in ProjectBased Actions
 * 
 * @author Pet Hrebejk 
 */
class ActionsUtil {
     
    
    /** Registers property change listener on given lookup. (Or on the default
     *  lookup (if the paramater is null). The listener is notified when the
     *  set of currently selected projects changes.
     */          
    /*
    public static Project getProjectFromLookup( Lookup lookup, String command ) {
        Collection projects = getProjectsFromLookup( lookup, command );
        if ( projects.isEmpty() || projects.size() > 1 ) {
            return null;
        }
        else {
            return (Project)projects.iterator().next();
        }
    }
    */
    
    /** Finds all projects in given lookup
     */
    public static Project[] getProjectsFromLookup( Lookup lookup, String command ) {    

        Set result = new HashSet();

        // First find out whether there is a project directly in the Lookup                        
        Collection projects = lookup.lookup( new Lookup.Template( Project.class ) ).allInstances();            
        for( Iterator it = projects.iterator(); it.hasNext(); ) {
            Project p = (Project)it.next();
            if (command == null || commandSupported(p, command, lookup)) {
                result.add(p);
            }
        }

        // Now try to guess the project from dataobjects
        Collection dataObjects = lookup.lookup( new Lookup.Template( DataObject.class ) ).allInstances();
        for( Iterator it = dataObjects.iterator(); it.hasNext(); ) {

            DataObject dObj = (DataObject)it.next();
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null ) {
                if ( command != null && !commandSupported( p, command, lookup ) ) {
                    continue;
                }
                result.add( p );                                        
            }

        }
        
        Project[] projectsArray = new Project[ result.size() ];
        result.toArray( projectsArray );        
        return projectsArray;
    }

    /** In given lookup will find all FileObjects owned by given project
     * with given command supported.
     */    
    public static FileObject[] getFilesFromLookup( Lookup lookup, String command, Project project ) {
        HashSet result = new HashSet();
        Collection dataObjects = lookup.lookup( new Lookup.Template( DataObject.class ) ).allInstances();
        for( Iterator it = dataObjects.iterator(); it.hasNext(); ) {

            DataObject dObj = (DataObject)it.next();
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null && p.equals( project ) ) {
                if ( command != null && !commandSupported( p, command, lookup ) ) {
                    continue;
                }
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
     */  
    public static String formatName( String namePattern, int numberOfObjects, String firstObjectName ) {
        
        return MessageFormat.format( 
            namePattern, 
            new Object[] {
                new Integer( numberOfObjects ),
                firstObjectName == null ? "" : firstObjectName,
            });            
    }
        
    // Private innerclasses ----------------------------------------------------
    
    /** 
     * Listens on given lookup and reports changes to the current project
     * set as property changes.
     */    
    
    

}