/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.selector.ui;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bi-directional state persisting search cursor support with wrapping
 * <p>
 * Typically the cursor is backed up by a list of slots each of which in turn
 * contains a list of result items. In the most simple case there will be just
 * one slot with all the search results contained in it.
 * </p>
 * @author Jaroslav Bachorik
 */
abstract class WrappingSearchCursor<R> {
    private String searchTerm;
    
    private int majorCounter = 0, minorCounter = -1;
    
    final private AtomicBoolean cancelled = new AtomicBoolean(false);
    
    WrappingSearchCursor(String searchTerm) {
        this.searchTerm = searchTerm;
    }
    
    /**
     * Moves to the next result. 
     * If there is no more results the cursor wraps around and starts at the
     * beginning.
     * @return The next result or <code>NULL</code> if there are no results
     */
    final R forward() {
        cancelled.set(false);
        
        R item = null;
        int majorTmp = majorCounter, minorTmp = minorCounter;
        
        do {
            if (majorCounter >= getSlotsNumber()) {
                majorCounter = 0;
                minorCounter = -1;
                continue;
            }

//            if (initializeSlot(majorCounter)) {
//                minorCounter = -1;
//            }
            
            if (++minorCounter >= getSlotSize(majorCounter)) {
                minorCounter = -1;
                majorCounter++;
                continue;
            }
        
            item = getItem(majorCounter, minorCounter);
            if (cancelled.get()) return null;
        } while (item == null && (majorTmp != majorCounter || minorTmp != minorCounter));
        return item;
    }
    
    /**
     * Moves to the previous result. 
     * If there is no more results the cursor wraps around and starts at the
     * end.
     * @return The previous result or <code>NULL</code> if there are no results
     */
    final R back() {
        cancelled.set(false);
        
        R item = null;
        
        int majorTmp = majorCounter, minorTmp = minorCounter;
        do {
            while (--minorCounter < 0)  {
                if (--majorCounter < 0) {
                   majorCounter = getSlotsNumber() - 1;
                }
                if (minorCounter < 0 && majorCounter < 0) return null;
                minorCounter = getSlotSize(majorCounter);
                if (cancelled.get()) return null;
            }
            
            item = getItem(majorCounter, minorCounter);
            if (cancelled.get()) return null;
        } while (item == null && (majorTmp != majorCounter || minorTmp != minorCounter));
        return item;
    }
    
    /**
     * Call this method to cancel any ongoing cursor operation
     */
    final void cancel() {
        cancelled.set(true);
    }
    
    /**
     * Indicates whether a cursor operation has been cancelled recently
     * @return <code>true</code> if a cursor operation has been cancelled recently
     */
    final protected boolean isCancelled() {
        return cancelled.get();
    }

    /**
     * search term getter
     * @return search term used to initialise the cursor
     */
    final String getSearchTerm() {
        return searchTerm;
    }
    
    /**
     * 
     * @return the number of slots available
     */
    abstract protected int getSlotsNumber();
    /**
     * 
     * @param slotIndex the slot index
     * @return the number of items contained in a certain slot
     */
    abstract protected int getSlotSize(int slotIndex);
    
    /**
     * Provide access to an item identified by its slot and item indices
     * @param slotIndex slot index
     * @param itemIndex item index (valid within the given slot)
     * @return the item identified by slot and item indeces or <code>null</code>
     */
    abstract protected R getItem(int slotIndex, int itemIndex);
}
