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

package com.netbeans.developer.modules.javadoc.search;


import com.netbeans.ide.util.RequestProcessor;
import com.netbeans.ide.util.TaskListener;
import com.netbeans.ide.util.Task;
import com.netbeans.ide.filesystems.FileObject;

/** Abstract class for thread which searches for documentation
 *
 *  @author Petr Hrebejk
 */

public abstract class IndexSearchThread extends Thread  {

  // PENDING: Add some abstract methods

  protected String                toFind;
  protected FileObject            fo;
  private   DocIndexItemConsumer  ddiConsumer;
  RequestProcessor.Task           rpTask = null;


  /** This method must terminate the process of searching */
  abstract void stopSearch(); 

  public IndexSearchThread( String toFind, FileObject fo, DocIndexItemConsumer ddiConsumer ) {
    this.ddiConsumer = ddiConsumer;
    this.fo = fo;
    this.toFind = toFind;
    //rpTask = RequestProcessor.createRequest( this );
  };


  protected void insertDocIndexItem( DocIndexItem dii ) {
    ddiConsumer.addDocIndexItem ( dii );
  }

  public void go() {
    rpTask = RequestProcessor.postRequest( this, 0, NORM_PRIORITY );
    //rpTask.
    //rpTask.schedule( 0 );
  }

  public void finish() {
    if ( !rpTask.isFinished() && !rpTask.cancel() )
      stopSearch();
    taskFinished();
  }

  public void taskFinished() {
    //System.out.println ("Task ended");
    ddiConsumer.indexSearchThreadFinished( this );    
  }

  /** Class for callback. Used to feed some container with found
   * index items;
   */

  public static interface DocIndexItemConsumer {

    /** Called when an item is found */
    public void addDocIndexItem ( DocIndexItem dii );

    /** Called when a task finished. May be called more than once */
    public void indexSearchThreadFinished( IndexSearchThread ist );


  }

}

/* 
 * Log
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $ 
 */ 