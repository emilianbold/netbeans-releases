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

package com.netbeans.developer.modules.search;

import java.awt.*;
import java.util.*;

import javax.swing.event.*;

import org.openide.nodes.*;
import org.openide.util.*;

import org.openidex.search.*;

import com.netbeans.developer.modules.search.res.*;

/** 
 * Holds search result data.
 * 
 * @author  Petr Kuzel
 * @version 1.0
 */
public class ResultModel implements NodeAcceptor, TaskListener {

  private final ChangeEvent EVENT;
  
  private final Node root;
  
  private SearchTask task = null;
  
  private boolean done = false;
  private int found = 0;
  
  private HashSet listeners = new HashSet();
  
  
  /** Creates new ResultModel */
  public ResultModel() {       
    
    EVENT = new ChangeEvent(this);
    root = new ResultRootNode();   
  }
  
  public boolean acceptNodes(Node[] nodes) {
            
    root.getChildren().add(nodes);    
    found += nodes.length;
    
    return true;
  }

  /** Is search engine still running?  */
  public boolean isDone() {
    return done;
  }
  
  /**
  */
  public void setTask (SearchTask task) {
    this.task = task;
    this.task.addTaskListener(this);
  }
  
  /** @return root node of result 
  */
  public Node getRoot() {
    return root;
  }
  
  /** Search task finished. Notify all listeners.
  */
  public void taskFinished(final org.openide.util.Task task) {
    
    if (found>0) root.setDisplayName("Found " + found + " nodes.");
    else root.setDisplayName("No matching node found.");
    done = true;

    fireChange();
    
  }
  
  public void stop() {
    if (task != null) task.stop();
  }
  
  public void addChangeListener(ChangeListener lis) {
    listeners.add(lis);
  }
  
  public void removeChangedListener(ChangeListener lis) {
    listeners.remove(lis);
  }

  /** Fire event to all listeners.
  */
  private void fireChange() {
    Iterator it = listeners.iterator();
    
    while(it.hasNext()) {
      ChangeListener next = (ChangeListener) it.next();
      next.stateChanged(EVENT);
    }
  }
  
  /** Search Result root node. May contain some statistic properties. 
  */
  private class ResultRootNode extends AbstractNode {
    
    
    /** create new */
    public ResultRootNode () {
      super(new Children.Array());
      
      setDisplayName("Searching...");      
    }
    
    /** @return universal search icon.
    */
    public Image getIcon(int type) {
      return Res.image("SEARCH");
    }
        
  }
}


/* 
* Log
*  4    Gandalf   1.3         12/16/99 Petr Kuzel      
*  3    Gandalf   1.2         12/15/99 Petr Kuzel      
*  2    Gandalf   1.1         12/14/99 Petr Kuzel      Minor enhancements
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 
  