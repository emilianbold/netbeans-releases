/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.editor.completion;

/**
 * The inteface of a task performing a code completion query.
 * <br>
 * The support class 
 * {@link org.netbeans.spi.editor.completion.support.CompletionTaskProvider}
 * can be used for convenience.
 *
 * @see CompletionProvider
 *
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.01
 */

public interface CompletionTask {

    /**
     * Called by the code completion infrastructure to ask the task
     * to do a query and return the results through the given completion listener.
     * <br>
     * This method is called only once during the lifetime of the completion task
     * object.
     *
     * <p>
     * This method is always called in AWT thread but it may reschedule
     * its processing into another thread and fire the given listener
     * once the computing is finished.
     * 
     * @param resultSet non-null result set to which the results
     *  of the query must be added.
     */
    public void query(CompletionResultSet resultSet);

    /**
     * Called by the code completion infrastructure to inform the task about
     * changes in the corresponding document. The task should reflect these
     * changes while creating the query result.
     * <br>
     * This method can be called multiple times on a single task instance.
     * <br>
     * It is guaranteed that this method will not be invoked in case
     * the document of the component would change since the last invocation
     * of either the <code>query()</code> or <code>refresh()</code>.
     *
     * <p>
     * This method is always called in AWT thread but it may reschedule
     * its processing into another thread and fire the given listener
     * once the computing is finished.
     * 
     * @param resultSet non-null result set to which the results
     *  of the refreshing must be added.
     */
    public void refresh(CompletionResultSet resultSet);
    
    /**
     * Called by the code completion infrastructure to cancel the task.
     * <br>
     * Once the cancel is done on the task no more querying or refreshing
     * is done on it.
     */
    public void cancel();
}
