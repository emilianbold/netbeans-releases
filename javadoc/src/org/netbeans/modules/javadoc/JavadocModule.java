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
 
import java.io.*;

// IDE imports ------------------

import com.netbeans.ide.actions.CutAction;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.modules.ModuleInstall;
import com.netbeans.ide.loaders.DataFolder;
import com.netbeans.ide.loaders.DataLoader;
import com.netbeans.ide.TopManager;

import javax.swing.event.*;

// MODULE imports ---------------

import com.netbeans.developer.modules.loaders.java.JavaDataObject;
import com.netbeans.developer.modules.loaders.java.JavaNode;

/** Class for initializing Javadoc module on IDE startup.

 @author Petr Hrebejk
*/
public class JavadocModule implements ModuleInstall {
  
  static {
  }

  /** By first install of module in the IDE do nothing. 
  */
  public void installed() {
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
    installActions( TopManager.getDefault().getLoaderPool().firstProducerOf( DataFolder.class ),
      new SystemAction[] { SystemAction.get( GenerateDocAction.class ), null } );

    // Install JavaDataObject action
    
    DataLoader javaLoader = TopManager.getDefault().getLoaderPool().firstProducerOf( JavaDataObject.class );
    if (javaLoader != null) {
      installActions(javaLoader, new SystemAction[] { SystemAction.get( GenerateDocAction.class ), null } );
    }
    else {
      TopManager.getDefault().getLoaderPool().addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          DataLoader javaL = TopManager.getDefault().getLoaderPool().firstProducerOf( JavaDataObject.class );
          if (javaL != null) {
            installActions(javaL, new SystemAction[] { SystemAction.get( GenerateDocAction.class ), null } );
            TopManager.getDefault().getLoaderPool().removeChangeListener(this);
          }
        }
      });
    }

    // System.out.println ( "Javadoc module installed .." );
  }
	
  /** Called before exiting IDE. 
  * @return Allways <CODE>true</CODE>.
  */
  public boolean closing() {
    return true;
  }

 
  private void installActions ( DataLoader dl, SystemAction sa[] ) {
    SystemAction old_sa[], new_sa[]; 
    int i;
    int j = 0;

    old_sa = dl.getActions();

    new_sa = new SystemAction[ old_sa.length + sa.length ];
     
    for (i = 0; i < old_sa.length; i++) {
      if (old_sa[i] instanceof CutAction) {
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


  /** Installs sa[] actions on position <code>index</code> bottom of menu 
  */
  
  private void installActions ( DataLoader dl, int index, SystemAction sa[] ) {
    int          i, j; 
    SystemAction old_sa[], new_sa[]; 

    //System.out.println ( dl );

    old_sa = dl.getActions();

    new_sa = new SystemAction[ old_sa.length + sa.length ];
     
    // Old actions up to index
    for (i = 0; i < old_sa.length - index; i++) {
      new_sa[i] = old_sa[i];
    } 

    // New actions
    for (j = 0; i < old_sa.length - index + sa.length ; i++, j++ ) {
      new_sa[i] = sa[j];
      }
    
    // Rest of old actions
    for (; i < old_sa.length + sa.length; i++ ) {
      new_sa[i] = old_sa[i - index];
    }

    dl.setActions( new_sa );
  }
  


}

/* 
 * Log
 *  2    Gandalf   1.1         4/23/99  Petr Hrebejk    
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $ 
 */ 