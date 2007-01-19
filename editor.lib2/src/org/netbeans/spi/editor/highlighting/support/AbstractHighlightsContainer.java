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

package org.netbeans.spi.editor.highlighting.support;

import java.util.List;
import org.netbeans.lib.editor.util.ListenerList;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * The default implementation of the <code>HighlightsContainer</code> interface.
 * It provides standard implementation of the methods for adding and removing
 * <code>HighlightsChangeListener</code>s and allows subclasses to notify listeners
 * by calling the <code>fireHighlightsChange</code> method.
 * 
 * @author Vita Stejskal
 */
public abstract class AbstractHighlightsContainer implements HighlightsContainer {
    
    private ListenerList<HighlightsChangeListener> listeners = new ListenerList<HighlightsChangeListener>();
    
    /** Creates a new instance of AbstractHighlightsContainer */
    protected AbstractHighlightsContainer() {
    }

    public abstract HighlightsSequence getHighlights(int startOffset, int endOffset);

    /**
     * Adds <code>HighlightsChangeListener</code> to this container.
     * 
     * @param listener The listener to add.
     */
    public final void addHighlightsChangeListener(HighlightsChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes <code>HighlightsChangeListener</code> to this container.
     * 
     * @param listener The listener to remove.
     */
    public final void removeHighlightsChangeListener(HighlightsChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Notifies all registered listeners about a change in this container. The
     * area of a document where highlights changed is specified by the
     * <code>changeStartOffset</code> and <code>changeEndOffset</code> parameters.
     * 
     * @param changeStartOffset The starting offset of the changed area.
     * @param changeEndOffset The ending offset of the changed area.
     */
    protected final void fireHighlightsChange(int changeStartOffset, int changeEndOffset) {
        List<HighlightsChangeListener> targets;
        
        synchronized (listeners) {
            targets = listeners.getListeners();
        }
        
        if (targets.size() > 0) {
            HighlightsChangeEvent evt = new HighlightsChangeEvent(this, changeStartOffset, changeEndOffset);
            
            for(HighlightsChangeListener l : targets) {
                l.highlightChanged(evt);
            }
        }
    }
}
