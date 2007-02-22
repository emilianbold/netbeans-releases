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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.search;

import java.util.EventListener;

/**
 * The listener interface for receiving search-related events.
 *
 * @author Nathan Fiedler
 */
public interface SearchListener extends EventListener {

    /**
     * Invoked when the user has initiated a search, but the search is
     * not yet complete.
     *
     * @param  event  the event object.
     */
    void searchCommenced(SearchEvent event);

    /**
     * Invoked when the user has dismissed the search by a means managed
     * within the search interface (e.g. pressing the Esc key).
     *
     * @param  event  the event object.
     */
    void searchDismissed(SearchEvent event);

    /**
     * Invoked when the search failed due to an unexpected exception. The
     * exception should contain a detail message that explains the issue.
     *
     * @param  event  the event object.
     */
    void searchFailed(SearchEvent event);

    /**
     * Invoked when the the search has completed and the results are now
     * available for display.
     *
     * @param  event  the event object.
     */
    void searchFinished(SearchEvent event);
}
