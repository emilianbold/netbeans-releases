/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc;

import java.io.File;
import java.io.IOException;

import java.util.Enumeration;
import java.lang.reflect.Method;
import javax.swing.event.*;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import org.openide.util.Utilities;
import org.openide.src.nodes.FilterFactory;
import org.openide.actions.CutAction;
import org.openide.util.actions.SystemAction;
import org.openide.modules.ModuleInstall;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.loaders.*;

import org.netbeans.modules.javadoc.settings.StdDocletSettings;
import org.netbeans.modules.javadoc.comments.JavaDocPropertySupportFactory;
import org.netbeans.modules.javadoc.comments.AutoCommentAction;
import org.netbeans.modules.javadoc.search.SearchDocAction;
import org.netbeans.modules.javadoc.search.DocFileSystem;

import org.openidex.util.Utilities2;

/** Class for initializing Javadoc module on IDE startup.

 @author Petr Hrebejk
*/
public class JavadocModule extends ModuleInstall {

    /** serialVersionUID */
    private static final long serialVersionUID = 984124010415492146L;

    private static int numberOfStarts = 0;

    /** By first install of module in the IDE, check whether standard documentation folder
    * exists. If not creates it.
    */
    public void installed() {
        // Install Search Action

        try {
            Utilities2.createAction (SearchDocAction.class,
                                     DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Help"), // NOI18N
                                     "ModuleHelp", true, true, false, true); // NOI18N

            // Create Action in action pool
            Utilities2.createAction (SearchDocAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Help")); // NOI18N
            Utilities2.createAction (GenerateDocAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Build")); // NOI18N
            Utilities2.createAction (AutoCommentAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Tools")); // NOI18N
        }
        catch (IOException e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                e.printStackTrace ();
            }
            // ignore failure to install
        }

        restored();
    }

    /** By uninstalling module from the IDE do nothing.
    */
    public void uninstalled () {

        // Remove doc search action
        try {
            Utilities2.removeAction (SearchDocAction.class,
                                     DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Help")); // NOI18N

            // remove actions from action pool
            Utilities2.removeAction (SearchDocAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Help")); // NOI18N
            Utilities2.removeAction (GenerateDocAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Build")); // NOI18N
            Utilities2.removeAction (AutoCommentAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Tools")); // NOI18N

        }
        catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) {
                e.printStackTrace ();
            }
        }
    }

    /** Called on IDE startup. Registers actions for generating documentation
    * on DataFolder and JavaDataObject.
    */
    public void restored() {


        numberOfStarts ++;

        if ( numberOfStarts < 3 ) {
            /*
             * This works only on the first start when called from
             * install method or on second start - but only if doesn't
             * exist the project.basic & project.last file int system
             * directory. If these files exist on second start mounted
             * filesystems are overriden by project settings.
             */
            installJavadocDirectories();
        }

        // Install the factory for adding JavaDoc property to nodes
        invokeDynamic( "org.netbeans.modules.java.JavaDataObject", // NOI18N
                       "addExplorerFilterFactory", // NOI18N
                       new JavaDocPropertySupportFactory() );
        invokeDynamic( "org.netbeans.modules.java.JavaDataObject", // NOI18N
                       "addBrowserFilterFactory", // NOI18N
                       new JavaDocPropertySupportFactory() );

        // Assign the Ctrl+F1 to JavaDoc Index Search Action
        // [PENDING] should be in installed() whenever global keymap editor is finished
        /*
        Keymap map = TopManager.getDefault ().getGlobalKeymap ();
        try {
          assign ("C-F1", "org.netbeans.modules.javadoc.search.SearchDocAction", map);
    } catch (ClassNotFoundException e) {
          // print and go on
          e.printStackTrace();
    }
        */
    }

    // UTILITY METHODS ----------------------------------------------------------------------

    /** Assigns a key to an action
    * @param key key name
    * @param action name of the action
    */
    /*
    private static void assign (String key, String action, Keymap map) throws ClassNotFoundException {
      KeyStroke str = Utilities.stringToKey (key);
      if (str == null) {
        System.err.println ("Not a valid key: " + key);
        // go on
        return;
      }

      Class actionClass = Class.forName (action);

      // create instance of the action
      SystemAction a = SystemAction.get (actionClass);

      map.addActionForKeyStroke (str, a);
      a.setEnabled( true );
}
    */
    /** Dynamicaly invokes a method
     */
    private void invokeDynamic( String className, String methodName, FilterFactory factory ) {

        try {
            Class dataObject = TopManager.getDefault().systemClassLoader().loadClass( className );

            if ( dataObject == null )
                return;

            Method method = dataObject.getDeclaredMethod( methodName, new Class[] { FilterFactory.class }  );
            if ( method == null )
                return;

            method.invoke( null, new Object[] { factory } );
        }
        catch ( java.lang.ClassNotFoundException e ) {
        }
        catch ( java.lang.NoSuchMethodException e ) {
        }
        catch ( java.lang.IllegalAccessException e ) {
        }
        catch ( java.lang.reflect.InvocationTargetException e ) {
        }
    }

    /** Tries to find standard Javadoc directory, open-api javadoc directrory
     * and directroy for javadoc output and mounts it into javadoc repository
     */

    public static void installJavadocDirectories() {

        // Try to find Java documantation

        File jdkDocsDir = new File ( System.getProperty ("java.home")  + java.io.File.separator + ".." // NOI18N
                                     + java.io.File.separator + "docs" ); // NOI18N
        mount( jdkDocsDir, true );

        // Try to find NetBeans open-api documentation
        // This is now done by Apisupport module
        /*
        File apiDocsDir = null;

        apiDocsDir = new File ( System.getProperty ("netbeans.user")  + java.io.File.separator + "docs" // NOI18N
                                     + java.io.File.separator + "openide-api" ); // NOI18N
        if ( apiDocsDir == null || !apiDocsDir.isDirectory() )
          apiDocsDir = new File ( System.getProperty ("netbeans.home")  + java.io.File.separator + "docs" // NOI18N
                                     + java.io.File.separator + "openide-api" ); // NOI18N
        mount( apiDocsDir, true );
        */

        // Create default directory for JavaDoc
        StdDocletSettings sdsTemp = new StdDocletSettings();

        // Reseting javadoc output directory is necessary for
        // multiuser installation
        String fileSep = System.getProperty ("file.separator");

        File directory = null;

        try {
            directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getCanonicalFile();
        }
        catch ( java.io.IOException e ) {
            directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getAbsoluteFile();
        }

        //if ( sdsTemp.getDirectory() != null && !sdsTemp.getDirectory().equals( directory ) ) {
        if ( System.getProperty ("netbeans.user") != null &&
                !System.getProperty ("netbeans.user").equals(System.getProperty ("netbeans.home") ) ) {

            // Multiuser we need to unmount the old file system

            LocalFileSystem localFS = new LocalFileSystem();
            try {
                File oldDirectory = null;
                try {
                    oldDirectory = new File (System.getProperty ("netbeans.home") + fileSep + "javadoc").getCanonicalFile();
                }
                catch ( java.io.IOException e ) {
                    oldDirectory = new File (System.getProperty ("netbeans.home") + fileSep + "javadoc").getAbsoluteFile();
                }

                localFS.setRootDirectory ( oldDirectory );
                Repository r = TopManager.getDefault ().getRepository ();

                FileSystem fs = r.findFileSystem( localFS.getSystemName() );

                if (fs != null) {
                    r.removeFileSystem (fs);
                }
            }
            catch (java.io.IOException ex) {}
            catch (java.beans.PropertyVetoException ex) {}
        }

        sdsTemp.setDirectory( directory );
        File jdOutputDir = sdsTemp.getDirectory();

        if ( !jdOutputDir.isDirectory() )
            jdOutputDir.mkdirs();
        mount( jdOutputDir, false );

    }

    /** Method finds out wether directory exists and whether it is
     *  a searchable javadoc directory if so mounts the directory
     *  into Javadoc repository
     */
    static void mount( File root, boolean testSearchability ) {

        if ((root != null) && (root.isDirectory())) {
            String dirName = root.getAbsolutePath();

            FileSystemCapability.Bean cap = new FileSystemCapability.Bean();
            cap.setCompile( false );
            cap.setExecute( false );
            cap.setDebug( false );
            cap.setDoc( true );

            LocalFileSystem localFS = new LocalFileSystem( cap );
            localFS.setHidden( true );

            try {
                localFS.setRootDirectory (new File (dirName));
                Repository r = TopManager.getDefault ().getRepository ();

                FileSystem fs = r.findFileSystem(localFS.getSystemName());

                if (fs == null) {
                    if( !testSearchability ||
                            DocFileSystem.getDocFileObject( localFS ) != null ) {
                        r.addFileSystem (localFS);
                    }
                }
            }
            catch (java.io.IOException ex) {}
            catch (java.beans.PropertyVetoException ex) {}
        }
    }

    // Implementation of java.io.Externalizable ------------------

    public void readExternal(final java.io.ObjectInput objectInput )
    throws java.io.IOException, java.lang.ClassNotFoundException {

        super.readExternal( objectInput );

        numberOfStarts = objectInput.readInt();

    }

    public void writeExternal(final java.io.ObjectOutput objectOutput )
    throws java.io.IOException {
        super.writeExternal( objectOutput );

        objectOutput.writeInt( numberOfStarts );
    }
}

/*
 * Log
 *  26   Gandalf   1.25        2/8/00   Petr Hrebejk    Problem with mounting 
 *       Javadoc output directory in multiuser installation fix
 *  25   Gandalf   1.24        1/19/00  Petr Hrebejk    Hack for project module 
 *       added
 *  24   Gandalf   1.23        1/16/00  Jesse Glick     Actions pool.
 *  23   Gandalf   1.22        1/15/00  Jesse Glick     Actions pool 
 *       installation.
 *  22   Gandalf   1.21        1/12/00  Petr Hrebejk    i18n
 *  21   Gandalf   1.20        1/10/00  Petr Hrebejk    Bug 4747 - closing of 
 *       output tab fixed
 *  20   Gandalf   1.19        12/21/99 Jesse Glick     Installing after general
 *       documentation.
 *  19   Gandalf   1.18        10/27/99 Petr Hrebejk    Bug fixes & back button 
 *       in Javadoc Quickview
 *  18   Gandalf   1.17        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  17   Gandalf   1.16        10/7/99  Petr Hrebejk    Module Externalization 
 *       fix
 *  16   Gandalf   1.15        10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  15   Gandalf   1.14        8/13/99  Petr Hrebejk    Initialization of JDoc 
 *       repository on first and second start added
 *  14   Gandalf   1.13        7/21/99  Petr Hrebejk    Action installation fix
 *  13   Gandalf   1.12        7/20/99  Petr Hrebejk    Action installation 
 *       added
 *  12   Gandalf   1.11        7/9/99   Petr Hrebejk    JavaDoc comments support
 *       added to module
 *  11   Gandalf   1.10        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   Gandalf   1.9         6/8/99   Petr Hrebejk    
 *  9    Gandalf   1.8         5/27/99  Petr Hrebejk    
 *  8    Gandalf   1.7         5/16/99  Petr Hrebejk    
 *  7    Gandalf   1.6         5/14/99  Petr Hrebejk    
 *  6    Gandalf   1.5         5/11/99  Petr Hrebejk    
 *  5    Gandalf   1.4         5/11/99  Petr Hrebejk    
 *  4    Gandalf   1.3         5/7/99   Petr Hrebejk    
 *  3    Gandalf   1.2         4/27/99  Petr Hrebejk    GenerateDocAction for 
 *       all producersOf JavaDataObjects
 *  2    Gandalf   1.1         4/23/99  Petr Hrebejk    
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $ 
 */ 