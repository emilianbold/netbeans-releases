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
import org.openide.util.SharedClassObject;

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
        restored();
    }

    /** By uninstalling module from the IDE do nothing.
    */
    public void uninstalled () {
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
        StdDocletSettings sdsTemp = (StdDocletSettings) SharedClassObject.findObject (StdDocletSettings.class, true);

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
