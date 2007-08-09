/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.editor.completion.support;

import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.openide.util.RequestProcessor;

/**
 * Asynchronous completion task allowing asynchronous query execution
 * through {@link AsyncCompletionQuery}.
 * <br>
 * This is a final class and all the logic must be defined
 * in an implementation of {@link AsyncCompletionQuery} that must
 * be passed to constructor of this task.
 *
 * @see AsyncCompletionQuery
 * @author Miloslav Metelka, Dusan Balek
 * @version 1.00
 */

public final class AsyncCompletionTask implements CompletionTask, Runnable {
    
    private final AsyncCompletionQuery query;
    
    private final JTextComponent component;
    
    private Document doc;
    
    private int queryCaretOffset;
    
    private CompletionResultSet queryResultSet;
    
    private CompletionResultSet refreshResultSet;

    private RequestProcessor.Task rpTask;
    
    /** Whether this task is cancelled. */
    private boolean cancelled;

    /** Whether query was already invoked on this task. */    
    private boolean queryInvoked;
    
    /**
     * Construct asynchronous task for the given component.
     *
     * @param query non-null query implementation.
     * @param component text component to operate with. The completion infrastructure
     *  will cancel the task if the component or its associated document would change.
     *  <br>
     *  It may be null to indicate that no component was provided.
     */
    public AsyncCompletionTask(AsyncCompletionQuery query, JTextComponent component) {
        assert (query != null) : "Query must be non-null";
        this.query = query;
        this.component = component;
        query.initTask(this);
    }
    
    /**
     * Constructor for the case when there is no valid component
     * which can happen when creating task for documentation or tooltip computation.
     *
     * @param query non-null query implementation.
     */
    public AsyncCompletionTask(AsyncCompletionQuery query) {
        this(query, null);
    }

    /**
     * Called by completion infrastructure in AWT thread to populate
     * the given result set with data.
     */
    public void query(CompletionResultSet resultSet) {
        assert (resultSet != null);
        assert (SwingUtilities.isEventDispatchThread());
        if (component != null) {
            doc = component.getDocument();
        } else {
            doc = null;
        }
        queryInvoked = true;

        synchronized (this) {
            performQuery(resultSet);
        }
    }

    /**
     * Called by completion infrastructure in AWT thread once there
     * is a change in the component (caret position changes or document
     * gets modified).
     * <br>
     * The results should be fired into the newly provided completion listener.
     */
    public void refresh(CompletionResultSet resultSet) {
        assert (SwingUtilities.isEventDispatchThread());
        assert !cancelled : "refresh() called on canceled task"; // NOI18N
        if (queryInvoked) {
            assert (resultSet != null);
            refreshResultSet = resultSet;
            refreshImpl();
        } else {
            query.preQueryUpdate(component);
        }
    }

    /**
     * Called by completion infrastructure to cancel the running task.
     */
    public void cancel() {
        cancelled = true;
        synchronized (this) {
            if (rpTask != null) {
                rpTask.cancel();
                rpTask = null;
            }
        }
    }

    private void performQuery(CompletionResultSet resultSet) {
        // Runs in AWT thread only
        if (component != null) {
            queryCaretOffset = component.getSelectionStart();
        } else {
            queryCaretOffset = -1;
        }

        query.prepareQuery(component);
        synchronized (this) {
            queryResultSet = resultSet;
            rpTask = RequestProcessor.getDefault().post(this);
        }
    }
    
    void refreshImpl() {
        // Always called in AWT thread only
        CompletionResultSet resultSet;
        boolean rpTaskFinished;
        synchronized (this) {
            rpTaskFinished = (rpTask == null);
            resultSet = refreshResultSet; // refreshResultSet checked in run()
        }
        if (resultSet != null) {
            if (rpTaskFinished) { // query finished already
                synchronized (this) {
                    refreshResultSet = null;
                }
                // Synchronously call the refresh()
                if (query.canFilter(component)) {
                    query.filter(resultSet);
                    assert resultSet.isFinished()
                        : toString() + ": query.filter(): Result set not finished by resultSet.finish()"; // NOI18N
                } else { // cannot filter and query stopped => another full query
                    performQuery(resultSet);
                }

            } else { // pending query not finished yet
                if (!query.canFilter(component)) { // query should attempted to be stopped by canFilter()
                    // Leave the ongoing query to be finished and once that happens
                    // ask for another canFilter()
                } // Let the query finish and schedule refreshing by (refreshResultSet != null)
            }
        }
    }

    /**
     * This method will be run() from the RequestProcessor during
     * performing of the query.
     */
    public void run() {
        // First check whether there was not request yet to stop the query: (queryResultSet == null)
        CompletionResultSet resultSet = queryResultSet;
        if (resultSet != null) {
            // Perform the querying (outside of synchronized section)
            query.query(resultSet, doc, queryCaretOffset);
            assert resultSet.isFinished()
            : toString() + ": query.query(): Result set not finished by resultSet.finish()"; // NOI18N
        }

        synchronized (this) {
            rpTask = null;
            queryResultSet = null;
            // Check for pending refresh
            if (refreshResultSet != null) {
                // Post refresh computation into AWT thread
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        refreshImpl();
                    }
                });
            }
        }
    }

    synchronized boolean isCancelled() {
        return cancelled;
    }

    public String toString() {
        return "AsyncCompletionTask: query=" + query; // NOI18N
    }
}
