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

package org.netbeans.modules.search;

import java.awt.*;
import java.util.*;
import java.text.*;

import javax.swing.event.*;

import org.openide.nodes.*;
import org.openide.util.*;

import org.openidex.search.*;

import org.netbeans.modules.search.res.*;

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
  
  private boolean useDisp = false;
  private SearchDisplayer disp = null;
  
  /** Creates new ResultModel */
  public ResultModel(CriteriaModel model) {       
    
    EVENT = new ChangeEvent(this);
    root = new ResultRootNode();   
    criteria = model;
  }

  /** Some nodes was found by engine.
  */
  public synchronized boolean acceptNodes(Node[] nodes) {
            
    root.getChildren().add(nodes);    
    found += nodes.length;
    
    if (useDisp && disp != null) {
      disp.acceptNodes(nodes);
    }
    
    return true;
  }

  /** Whether mirror search sesults in output window. 
  * @return new state
  */
  synchronized boolean fillOutput (boolean fill) {
    if (useDisp) return true;
    
    useDisp = true;
    
    disp = new SearchDisplayer();
    disp.acceptNodes(root.getChildren().getNodes());
    
    return true;
  }

  /** Does used criteria allow filling output window? 
  * Currently it check for presence of StructuredDetail.
  * @return true it it can be used.
  */
  synchronized boolean canFillOutput() {

    SearchType[] crs = getCriteriaModel().getCustomizedCriteria();

    for (int i=0; i < crs.length; i++) {

      Class[] detCls = crs[i].getDetailClasses();
      //we support just AND critera relation
      //so if one of them support a detail then
      //all search results (matched nodes) will do.
      if (detCls == null) continue;
      for (int x=0; x < detCls.length; x++) {
        if (StructuredDetail.class == detCls[x])
          return true;
      }
    }
    return false;
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
          Res.text("MSG_FOUND_A_NODE"), // NOI18N
          new Object[] {new Integer(found)}
        )
      );
        
    } else if (found>1) {
      root.setDisplayName (
        MessageFormat.format(
          Res.text("MSG_FOUND_X_NODES"), // NOI18N
          new Object[] {new Integer(found)}
        )
      ); 
        
    } else { // <1
      root.setDisplayName(Res.text("MSG_NO_NODE_FOUND")); // NOI18N
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
      
      setDisplayName(Res.text("SEARCHING___")); // NOI18N
    }
    
    /** @return universal search icon.
    */
    public Image getIcon(int type) {
      return Res.image("SEARCH"); // NOI18N
    }
        
    public Image getOpenedIcon(int type) {
      return getIcon(type);
    }
  }
}


/* 
* Log
*  11   Gandalf-post-FCS1.8.2.1     4/4/00   Petr Kuzel      Comments + output window 
*       fix
*  10   Gandalf-post-FCS1.8.2.0     2/24/00  Ian Formanek    Post FCS changes
*  9    Gandalf   1.8         1/13/00  Radko Najman    I18N
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
  
