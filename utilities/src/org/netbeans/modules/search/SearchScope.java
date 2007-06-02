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
 * Software is Sun Microsystems, Inc. Portions Copyright 2003-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import javax.swing.event.ChangeListener;
import org.openidex.search.SearchInfo;

/**
 * Interface for obtaining information about scope of a search task.
 *
 * @author  Marian Petras
 */
public abstract class SearchScope {

    /**
     * Returns human-readable, localized name of this search scope.
     * 
     * @return  name of this search scope
     */
    protected abstract String getDisplayName();
    
    /**
     * Is this search scope applicable at the moment?
     * For example, search scope of all open projects is not applicable if there
     * is no open project.
     * 
     * @return  {@code true} if this search scope is applicable,
     *          {@code false} otherwise
     */
    protected abstract boolean isApplicable();
    
    /**
     * Registers a listener listening for changes of applicability.
     * Registered listeners should be notified each time this {@code SearchScope}
     * becomes applicable/unapplicable.
     * 
     * @param  l  listener to be registered
     * @see  #isApplicable
     */
    protected abstract void addChangeListener(ChangeListener l);
    
    /**
     * Unregisters a listener listening for changes of applicability.
     * If the passed listener is not currently registered or if the passed
     * listener is {@code null}, this method has no effect.
     * 
     * @param  l  listener to be unregistered
     * @see  #addChangeListener
     * @see  #isApplicable
     */
    protected abstract void removeChangeListener(ChangeListener l);
    
    /**
     * Returns object defining the actual search scope, i.e. the iterator over
     * {@code DataObject}s to be searched.
     * 
     * @return  {@code SearchInfo} defining the search scope
     */
    protected abstract SearchInfo getSearchInfo();

    @Override
    public String toString() {
        return getDisplayName();
    }
    
}
