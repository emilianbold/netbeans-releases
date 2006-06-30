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

package org.netbeans.modules.editor.completion;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JToolTip;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;

/**
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CompletionResultSetImpl {
    
    static {
        // Ensure the SPI accessor gets assigned
        CompletionResultSet.class.getName();
    }
    private static final CompletionSpiPackageAccessor spi
            = CompletionSpiPackageAccessor.get();

    private final CompletionImpl completionImpl;
    
    private final Object resultId;
    
    private final CompletionTask task;
    
    private final int queryType;
    
    private CompletionResultSet resultSet;
    
    private boolean active;

    private String title;
    
    private String waitText;
    
    private int anchorOffset;
    
    private List items;
    
    private boolean itemsAlreadySorted;
    
    private boolean finished;
    
    private CompletionDocumentation documentation;
    
    private JToolTip toolTip;
    
    private int estimatedItemCount;
    
    private int estimatedItemWidth;
    
    CompletionResultSetImpl(CompletionImpl completionImpl,
    Object resultId, CompletionTask task, int queryType) {
        assert (completionImpl != null);
        assert (resultId != null);
        assert (task != null);
        this.completionImpl = completionImpl;
        this.resultId = resultId;
        this.task = task;
        this.queryType = queryType;
        this.anchorOffset = -1; // not set
        this.estimatedItemCount = -1; // not estimated
        this.active = true;
        
        spi.createCompletionResultSet(this);
    }
    
    /**
     * Get the result set instance associated with this implementation.
     */
    public synchronized CompletionResultSet getResultSet() {
        return resultSet;
    }
    
    public synchronized void setResultSet(CompletionResultSet resultSet) {
        assert (resultSet != null);
        assert (this.resultSet == null);
        this.resultSet = resultSet;
    }
    
    /**
     * Get the task associated with this result.
     */
    public CompletionTask getTask() {
        return task;
    }
    
    /**
     * Get the query type to which this result set belongs.
     * The results of other query types will be ignored when
     * being set into this result set.
     */
    public int getQueryType() {
        return queryType;
    }
    
    /**
     * Mark that results from this result set should no longer
     * be taken into account.
     */
    public synchronized void markInactive() {
        this.active = false;
    }

    public synchronized String getTitle() {
        return title;
    }

    public synchronized void setTitle(String title) {
        checkNotFinished();
        this.title = title;
    }
    
    public synchronized int getAnchorOffset() {
        return anchorOffset;
    }
    
    public synchronized void setAnchorOffset(int anchorOffset) {
        checkNotFinished();
        this.anchorOffset = anchorOffset;
    }
    
    public synchronized boolean addItem(CompletionItem item) {
        assert (item != null) : "Added item cannot be null";
        checkNotFinished();
        if (!active || queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return false; // signal do not add any further results
        }

        if (items == null) {
            int estSize = (estimatedItemCount == -1) ? 10 : estimatedItemCount;
            items = new ArrayList(estSize);
        }
        items.add(item);
        return items.size() < 1000;
    }
    
    public boolean addAllItems(Collection/*<CompletionItem>*/ items) {
        boolean cont = true;
        for (Iterator it = items.iterator(); it.hasNext();) {
            cont = addItem((CompletionItem)it.next());
        }
        return cont;
    }
    
    /**
     * @return non-null list of items.
     */
    public synchronized List getItems() {
        assert isFinished() : "Adding not finished";
        return (items != null) ? items : Collections.EMPTY_LIST;
    }
    
    public synchronized void setDocumentation(CompletionDocumentation documentation) {
        checkNotFinished();
        if (!active || queryType != CompletionProvider.DOCUMENTATION_QUERY_TYPE) {
            return;
        }
        this.documentation = documentation;
    }
    
    public synchronized CompletionDocumentation getDocumentation() {
        return documentation;
    }
    
    public synchronized JToolTip getToolTip() {
        return toolTip;
    }
    
    public synchronized void setToolTip(JToolTip toolTip) {
        checkNotFinished();
        if (!active || queryType != CompletionProvider.TOOLTIP_QUERY_TYPE) {
            return;
        }
        this.toolTip = toolTip;
    }
    
    public synchronized boolean isFinished() {
        return finished;
    }
    
    public void finish() {
        synchronized (this) {
            if (finished) {
                throw new IllegalStateException("finish() already called"); // NOI18N
            }
            finished = true;
        }

        completionImpl.finishNotify(this);
    }
    
    public int getSortType() {
        return completionImpl.getSortType();
    }
    
    public synchronized void estimateItems(int estimatedItemCount, int estimatedItemWidth) {
        this.estimatedItemCount = estimatedItemCount;
        this.estimatedItemWidth = estimatedItemWidth;
    }
    
    CompletionImpl getCompletionImpl() {
        return completionImpl;
    }
    
    Object getResultId() {
        return resultId;
    }
    
    private void checkNotFinished() {
        if (isFinished()) {
            throw new IllegalStateException("Result set already finished"); // NOI18N
        }
    }

    public synchronized String getWaitText() {
        return waitText;
    }

    public synchronized void setWaitText(String waitText) {
        this.waitText = waitText;
    }

}
