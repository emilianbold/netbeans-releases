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

package src_modules.com.netbeans.developer.modules.search;

import javax.swing.event.*;

import org.openide.nodes.*;
import org.openide.util.*;

import org.openidex.search.*;

/** 
 * Holds search result data.
 * 
 * @author  Petr Kuzel
 * @version 
 */
public class ResultModel implements NodeAcceptor, TaskListener {

  private final ChangeEvent EVENT;
  
  private final Node root;
  
  private SearchTask task = null;
  
  private boolean done = false;
  private int found = 0;
  
  
  /** Creates new ResultModel */
  public ResultModel() {       
    
    EVENT = new ChangeEvent(this);
    root = new AbstractNode(new Children.Array());
    root.setDisplayName("Searching...");
  }
  
  public boolean acceptNodes(Node[] nodes) {
        
    
    root.getChildren().add(nodes);
    
    found += nodes.length;
    
    return true;
  }
   
  public void setTask (SearchTask task) {
    this.task = task;
    this.task.addTaskListener(this);
  }
  
  /** @return root node of result 
  */
  public Node getRoot() {
    return root;
  }
  
  /** Search task finished.
  */
  public void taskFinished(final org.openide.util.Task task) {
    
    if (found>0) root.setDisplayName("Found " + found + " nodes.");
    else root.setDisplayName("No matching node found.");
    done = true;
    
  }
  
  public void stop() {
    if (task != null) task.stop();
  }
  
}


/* 
* Log
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 
  