/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.outline;

import javax.swing.table.TableModel;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeModel;

/** A model for an Outline (&quot;tree-table&quot;).  Implements both
 * TreeModel and TableModel (the default implementation, DefaultOutlineModel,
 * wraps a supplied TreeModel and TableModel).  It is vastly easier to
 * use <code>DefaultOutlineModel</code> than to implement this interface
 * directly.
 *
 * @author  Tim Boudreau  */
public interface OutlineModel extends TableModel, TreeModel {
    /** Get the <code>TreePathSupport</code> object this model uses to manage
     * information about expanded nodes.  <code>TreePathSupport</code> implements
     * logic for tracking expanded nodes, manages <code>TreeWillExpandListener</code>s,
     * and is a repository for preserving expanded state information about nodes whose parents
     * are currently collapsed.  JTree implements very similar logic internally
     * to itself.
     * <p>
     * <i>(PENDING) It is not yet determined if TreePathSupport will remain a
     * public class.</i>
     */
    public TreePathSupport getTreePathSupport();
    /** Get the layout cache which is used to track the visual state of nodes.
     * This is typically one of the standard JDK layout cache classes, such
     * as <code>VariableHeightLayoutCache</code> or <code>
     * FixedHeightLayoutCache</code>.  */
    public AbstractLayoutCache getLayout();
    /** Determine if the model is a large-model.  Large model trees keep less
     * internal state information, relying on the TreeModel more.  Essentially
     * they trade performance for scalability. An OutlineModel may be large
     * model or small model; primarily this affects the type of layout cache
     * used, just as it does with JTree.  */
    public boolean isLargeModel();
    /**
     * This method allows you to set the name of the column representing the node.
     * Added 4/19/2004 - David Botterill
     * @param inName - the name to be given to the node column.
     */
    public void setNodeColumnName(String inName);
    /**
     * Get the NodeRowModel interface so we can manage information about nodes as they related
     * to rows.
     * Added 4/19/2004 - David Botterill
     */
    public NodeRowModel getRowNodeModel();
    
}
