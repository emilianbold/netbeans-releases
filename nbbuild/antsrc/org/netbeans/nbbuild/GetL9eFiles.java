/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.FileSet;
import java.io.*;
import java.util.*;

/** This task copies the localizable files to a directory.
 * This task uses the L10nTask, but it discards the generated
 * files and the tar files.
 */
public class GetL9eFiles extends Task {

  /** The name of file that contains the localizable file 
   * regular expressions.
   * <p>Default: <samp>l10n.list</samp>
   */
  protected String listFile = "l10n.list";

  /** The grandparent directory of the l10n.list files.
   * <p>Default: <samp>..</samp>
   */
  protected String baseDir = "..";
  protected File grandParent = null ;
  
  /** The target directory to copy all translatable files to.
   * <p>Default: <samp>src-todo</samp>
   */
  protected File targetDir = new File( "src-todo") ;

  /** List of exclude patterns that override the listFiles.
   * <p>Default: Ja localized files
   */
  protected String excludes = "**/ja/,**/*_ja.*" ;

  public void setBaseDir(String s) {
    File f ;

    baseDir = s;
    f = new File( baseDir) ;
    try {
      grandParent = new File( f.getCanonicalPath()) ;
    }
    catch( Exception e) {
      e.printStackTrace();
      throw new BuildException() ;
    }
  }
  public void setListFile( String s) {
    listFile = s ;
  }
  public void setTargetDir( File f) {
    targetDir = f ;
  }
  public void setExcludes( String s) {
    excludes = s ;
  }

  /** A file filter that accepts only directories
   */
  class DirectoryFilter implements FileFilter {
    public boolean accept(File f) {
      return( f.isDirectory()) ;
    }
  }

  class TarFileFilter implements FileFilter {
    public boolean accept(File f) {
      return( f.getName().endsWith( ".tar")) ;
    }
  }

  public void execute() throws BuildException {
    String modules ;
    L10nTask l10nTask ;
    Delete delTask ;
    FileSet fileSet ;
    Copy copyTask ;
    File tmp ;
    File[] files ;
    int i ;
    Untar untar ;

    // Get the list of modules with listFiles. //
    modules = getModulesWithListFiles() ;

    // Run the l10n task. //
    project.addTaskDefinition( "l10nTask", L10nTask.class) ;
    l10nTask = (L10nTask) project.createTask( "l10nTask") ;    
    l10nTask.init() ;
    l10nTask.setTopdirs( grandParent.getPath()) ;
    l10nTask.setModules( modules) ;
    l10nTask.setLocalizableFile( listFile) ;
    l10nTask.setGeneratedFile( "l10n.list.translated") ;
    l10nTask.setDistDir( "tmp-l10n-dist") ;
    l10nTask.setChangedFile( "l10n.list.changed") ;
    l10nTask.setBuildDir( "tmp-l10n-build") ;
    l10nTask.setBuildNumber( "0") ;
    l10nTask.setExcludePattern( excludes) ;
    l10nTask.execute() ;

    // Un-tar the bundles. //
    tmp = new File( "tmp-l10n-build/" + grandParent.getName()) ;
    files = tmp.listFiles( new TarFileFilter()) ;
    for( i = 0; i < files.length; i++) {
      untar = (Untar) project.createTask( "untar") ;
      untar.init() ;
      untar.setSrc( files[ i]) ;
      untar.setDest( files[ i].getParentFile()) ;
      untar.execute() ;
    }

    // Copy the files except the ones we don't want to the final target dir. //
    copyTask = (Copy) project.createTask( "copy") ;
    copyTask.init() ;
    copyTask.setTodir( targetDir) ;
    fileSet = new FileSet() ;
    fileSet.setDir( new File( "tmp-l10n-build/" + grandParent.getName())) ;
    fileSet.setExcludes( "*.tar,*/*.l10n.list.*") ;
    copyTask.addFileset( fileSet) ;
    copyTask.execute() ;

    // Cleanup. //
    delTask = (Delete) project.createTask( "delete") ;
    delTask.init() ;
    delTask.setDir( new File( "tmp-l10n-build")) ;
    delTask.execute() ;
    delTask = (Delete) project.createTask( "delete") ;
    delTask.init() ;
    delTask.setDir( new File( "tmp-l10n-dist")) ;
    delTask.execute() ;
  }

  protected String getModulesWithListFiles() {
    File module, list ;
    File[] parents ;
    String modules = new String() ;
    int i ;
    boolean firstTime = true ;

    parents = grandParent.listFiles( new DirectoryFilter()) ;
    for( i = 0; i < parents.length; i++) {
      module = parents[ i] ;
      list = new File( module.getPath() + "/" + listFile) ;
      if( list.exists()) {
	if( firstTime) {
	  modules += module.getName() ;
	}
	else {
	  modules += "," + module.getName() ;
	}
	firstTime = false ;
      }
    }
    return( modules) ;
  }

}
