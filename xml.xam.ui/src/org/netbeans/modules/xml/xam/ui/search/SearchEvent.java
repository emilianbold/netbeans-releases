/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
