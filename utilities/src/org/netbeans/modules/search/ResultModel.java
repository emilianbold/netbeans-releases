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
import java.text.*;

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

  /** */
  private final ChangeEvent EVENT;

  /** Node representing root of found nodes. 
  * As children holds all found nodes.
  */
  private final Node root;
  
  private SearchTask task = null;

  /** Search state field. */
  private boolean done = false;
  
  /** search statistics field. */
  private int found = 0;
  
  private HashSet listeners = new HashSet();
  
  /** Which criteria produced this result. */
  private CriteriaModel criteria;
  
  /** Creates new ResultModel */
  public ResultModel(CriteriaModel model) {       
    
    EVENT = new ChangeEvent(this);
    root = new ResultRootNode();   
    criteria = model;
  }

  /** Some nodes was found by engine.
  */
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
  
  /**
  * @return criteria model that produces these results.
  */
  public CriteriaModel getCriteriaModel() {
    return criteria;
  }
  
  /** Search task finished. Notify all listeners.
  */
  public void taskFinished(final org.openide.util.Task task) {
    
    // set proper label of search results root node
    
    if (found==1) {
      root.setDisplayName (
        MessageFormat.format(
          Res.text("MSG_FOUND_A_NODE"), 
          new Object[] {new Integer(found)}
        )
      );
        
    } else if (found>1) {
      root.setDisplayName (
        MessageFormat.format(
          Res.text("MSG_FOUND_X_NODES"), 
          new Object[] {new Integer(found)}
        )
      ); 
        
    } else { // <1
      root.setDisplayName(Res.text("MSG_NO_NODE_FOUND"));
    }
    
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
      
      setDisplayName(Res.text("SEARCHING___"));      
    }
    
    /** @return universal search icon.
    */
    public Image getIcon(int type) {
      return Res.image("SEARCH");
    }
        
    public Image getOpenedIcon(int type) {
      return getIcon(type);
    }
  }
}


/* 
* Log
*  8    Gandalf   1.7         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  7    Gandalf   1.6         1/4/00   Petr Kuzel      Bug hunting.
*  6    Gandalf   1.5         12/23/99 Petr Kuzel      Architecture improved.
*  5    Gandalf   1.4         12/17/99 Petr Kuzel      Bundling.
*  4    Gandalf   1.3         12/16/99 Petr Kuzel      
*  3    Gandalf   1.2         12/15/99 Petr Kuzel      
*  2    Gandalf   1.1         12/14/99 Petr Kuzel      Minor enhancements
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 
  
