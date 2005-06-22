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

package org.netbeans.modules.editor.completion;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
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
    
    private final CompletionTask task;
    
    private final int queryType;
    
    private CompletionResultSet resultSet;
    
    private boolean active;

    private String title;
    
    private int anchorOffset;
    
    private List items;
    
    private boolean itemsAlreadySorted;
    
    private boolean finished;
    
    private CompletionDocumentation documentation;
    
    private JToolTip toolTip;
    
    private int estimatedItemCount;
    
    private int estimatedItemWidth;
    
    CompletionResultSetImpl(CompletionImpl completionImpl, CompletionTask task, int queryType) {
        assert (completionImpl != null);
        assert (task != null);
        this.completionImpl = completionImpl;
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
    public CompletionResultSet getResultSet() {
        return resultSet;
    }
    
    public void setResultSet(CompletionResultSet resultSet) {
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
    public void markInactive() {
        this.active = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        checkNotFinished();
        this.title = title;
    }
    
    public int getAnchorOffset() {
        return anchorOffset;
    }
    
    public void setAnchorOffset(int anchorOffset) {
        checkNotFinished();
        this.anchorOffset = anchorOffset;
    }
    
    public boolean addItem(CompletionItem item) {
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
    
    public List getItems() {
        assert isFinished() : "Adding not finished";
        return items;
    }
    
    public void setDocumentation(CompletionDocumentation documentation) {
        checkNotFinished();
        if (!active || queryType != CompletionProvider.DOCUMENTATION_QUERY_TYPE) {
            return;
        }
        this.documentation = documentation;
    }
    
    public CompletionDocumentation getDocumentation() {
        return documentation;
    }
    
    public void setToolTip(JToolTip toolTip) {
        checkNotFinished();
        if (!active || queryType != CompletionProvider.TOOLTIP_QUERY_TYPE) {
            return;
        }
        this.toolTip = toolTip;
    }
    
    public JToolTip getToolTip() {
        return toolTip;
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
    
    public synchronized boolean isFinished() {
        return finished;
    }
    
    public int getSortType() {
        return completionImpl.getSortType();
    }
    
    public void estimateItems(int estimatedItemCount, int estimatedItemWidth) {
        this.estimatedItemCount = estimatedItemCount;
        this.estimatedItemWidth = estimatedItemWidth;
    }
    
    private void checkNotFinished() {
        if (isFinished()) {
            throw new IllegalStateException("Result set already finished"); // NOI18N
        }
    }

}
