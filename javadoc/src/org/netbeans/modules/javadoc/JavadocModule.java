/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.javadoc;
 
import java.io.File;

import java.util.Enumeration;
import javax.swing.event.*;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import com.netbeans.ide.util.Utilities;

// IDE imports ------------------

import com.netbeans.ide.actions.CutAction;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.modules.ModuleInstall;
import com.netbeans.ide.loaders.DataFolder;
import com.netbeans.ide.loaders.DataLoader;
import com.netbeans.ide.TopManager;
import com.netbeans.ide.filesystems.LocalFileSystem;
import com.netbeans.ide.filesystems.Repository;

// MODULE imports ---------------

import com.netbeans.developer.modules.loaders.java.JavaDataObject;
import com.netbeans.developer.modules.loaders.java.JavaNode;
import com.netbeans.developer.modules.javadoc.settings.StdDocletSettings;

/** Class for initializing Javadoc module on IDE startup.

 @author Petr Hrebejk
*/
public class JavadocModule implements ModuleInstall {
  
  private static  Keymap map;

  /** By first install of module in the IDE, check whether standard documentation folder
  * exists. If not creates it.
  */
  public void installed() {
    // Create Standard Doclet option to get Standard javadoc directory
    StdDocletSettings sdsTemp = new StdDocletSettings();


    // Try to find standard java doc

    File jdkDocsDir = new File ( System.getProperty ("java.home")  + java.io.File.separator + ".." 
                                 + java.io.File.separator + "docs" + java.io.File.separator + "api" );

    if ( jdkDocsDir.isDirectory() ) {
      try {
        File jdkDocsDirCan = new File( jdkDocsDir.getCanonicalPath() );
        if ( jdkDocsDirCan.isDirectory() ) {
          LocalFileSystem lfs = new LocalFileSystem( );
          lfs.setRootDirectory(jdkDocsDirCan);
          lfs.setHidden( true );
      
          String systemName = lfs.getSystemName();
    
          if ( TopManager.getDefault().getRepository().findFileSystem( systemName ) == null ) {
            //System.out.println ( "ADD:" + lfs.getSystemName() );
            TopManager.getDefault().getRepository().addFileSystem ( lfs );
            }
        }
      }
      catch ( java.io.IOException ex ) {
      }
      catch ( java.beans.PropertyVetoException ex ) {
      }
    }


    // Try to find netbeans api docs

    File apiDocsDir = new File ( System.getProperty ("netbeans.home")  + java.io.File.separator + "docs" 
                                 + java.io.File.separator + "open-api" );

    if ( apiDocsDir.isDirectory() ) {
      try {
        LocalFileSystem lfs = new LocalFileSystem( );
        lfs.setRootDirectory(apiDocsDir);
        lfs.setHidden( true );
        String systemName = lfs.getSystemName();
    
          if ( TopManager.getDefault().getRepository().findFileSystem( systemName ) == null ) {
            //System.out.println ( "ADD:" + lfs.getSystemName() );
            TopManager.getDefault().getRepository().addFileSystem ( lfs );    
            }  

      }
      catch ( java.io.IOException ex ) {
      }
      catch ( java.beans.PropertyVetoException ex ) {
      }
    }



    File dir = sdsTemp.getDirectory();
    
    if ( !dir.isDirectory() ) 
      dir.mkdirs();

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
    
    // Install DataFolder action
    /*
    installActions( TopManager.getDefault().getLoaderPool().firstProducerOf( DataFolder.class ),
      new SystemAction[] { SystemAction.get( GenerateDocAction.class ), null } );
    */
    // Install GenerateDocActon for every producer of JavaDataObject and derived classes.
    /*
    TopManager.getDefault().getLoaderPool().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {

        Class jdoClass = JavaDataObject.class;
        if ( jdoClass == null ) {
          return;
        }

        Enumeration en = TopManager.getDefault().getLoaderPool().producersOf( jdoClass );

        while ( en.hasMoreElements() ) {
          DataLoader loader =  (DataLoader)en.nextElement();
          if ( !hasGenerateDoc( loader )) 
            installActions(loader, new SystemAction[] { SystemAction.get( GenerateDocAction.class ), null } );                          
        }
      }
    });
    */
    
    // Assign the Ctrl+F1 to JavaDoc Index Search Action

    map = TopManager.getDefault ().getGlobalKeymap ();
    try {
      assign ("C-F1", "com.netbeans.developer.modules.javadoc.search.SearchDocAction");
    } catch (ClassNotFoundException e) {
      // print and go on
      e.printStackTrace();
    }
  }
	
  /** Called before exiting IDE. 
  * @return Allways <CODE>true</CODE>.
  */
  public boolean closing() {
    return true;
  }

 
  /** Installs array of new actions into popup menu of DataLoeader before the action Cut
  * @param dl DataLoadaer object where to install new actions
  * @param sa[] Array of new acrions
  */
  /*
  private void installActions ( DataLoader dl, SystemAction sa[] ) {
    SystemAction old_sa[], new_sa[]; 
    int i;
    int j = 0;

    old_sa = dl.getActions();

    new_sa = new SystemAction[ old_sa.length + sa.length ];
     
    for (i = 0; i < old_sa.length; i++) {
      if ( old_sa[i] instanceof CutAction ) {
        for (j = 0; j < sa.length ; j++ )
          new_sa[i + j] = sa[j];
        }
      new_sa[i+j] = old_sa[i];
    } 

    if (j == 0) {
      for (j = 0; i < sa.length ; j++ )
          new_sa[i + j] = sa[j];
    }

  dl.setActions( new_sa );
  }  
  */
  /** Checks whether an action of type actionClass is already installed in 
  * DataLoader's popup menu
  */
  /*
  private boolean hasGenerateDoc ( DataLoader dl ) {
    SystemAction actions[] = dl.getActions();
    
    for (int i = 0; i < actions.length; i++) 
      if (actions[i] instanceof GenerateDocAction )
        return true;

    return false;
  }
  */

  /** Assigns a key to an action
  * @param key key name
  * @param action name of the action
  */
  private static void assign (String key, String action) throws ClassNotFoundException {
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
  }



}

/* 
 * Log
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