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

package org.netbeans.modules.xml.xam.ui.search;

import java.util.EventObject;
import java.util.List;

/**
 * An event which indicates that a search operation has commenced,
 * completed, was dismissed, or failed due to an exception.
 *
 * @author Nathan Fiedler
 */
public class SearchEvent extends EventObject {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Search results, if any. */
    private List<Object> results;
    /** Event type. */
    private Type type;
    /** The exception for this event, if any (e.g. for searchFailed()). */
    private SearchException exception;

    /**
     * Type of the search event.
     */
    public static enum Type {
        COMMENCED {
            public void fireEvent(SearchEvent e, SearchListener l) {
                l.searchCommenced(e);
            }
        },
        DISMISSED {
            public void fireEvent(SearchEvent e, SearchListener l) {
                l.searchDismissed(e);
            }
        },
        FAILED {
            public void fireEvent(SearchEvent e, SearchListener l) {
                l.searchFailed(e);
            }
        },
        FINISHED {
            public void fireEvent(SearchEvent e, SearchListener l) {
                l.searchFinished(e);
            }
        };

        /**
         * Dispatches the event to the listener.
         *
         * @param  e  event to dispatch.
         * @param  l  listener to receive event.
         */
        public abstract void fireEvent(SearchEvent e, SearchListener l);
    }

    /**
     * Creates a new instance of SearchEvent.
     *
     * @param  src   event source.
     * @param  type  event type.
     */
    public SearchEvent(Object src, Type type) {
        super(src);
        this.type = type;
    }

    /**
     * Creates a new instance of SearchEvent.
     *
     * @param  src      event source.
     * @param  type     event type.
     * @param  results  set of search results.
     */
    public SearchEvent(Object src, Type type, List<Object> results) {
        this(src, type);
        this.results = results;
    }

    /**
     * Creates a new instance of SearchEvent.
     *
     * @param  src    event source.
     * @param  type   event type.
     * @param  error  the search exception for this event.
     */
    public SearchEvent(Object src, Type type, SearchException error) {
        this(src, type);
        this.exception = error;
    }

    /**
     * Return the search exception, if any, for this event.
     *
     * @return  search exception, or null if none.
     */
    public SearchException getException() {
        return exception;
    }

    /**
     * Retrieve the results of the search.
     *
     * @return  search results, or null if none available.
     */
    public List<Object> getResults() {
        return results;
    }

    /**
     * Get the search event type.
     *
     * @return  search event type.
     */
    public Type getType() {
        return type;
    }
}
