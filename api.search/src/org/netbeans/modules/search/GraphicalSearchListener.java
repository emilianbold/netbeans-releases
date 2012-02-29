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
package org.netbeans.modules.search;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.spi.search.provider.SearchComposition;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 *
 * @author jhavlin
 */
class GraphicalSearchListener<R> extends SearchListener {

    private static final Logger LOG = Logger.getLogger(
            GraphicalSearchListener.class.getName());

    /**
     * Limit for length of path shown in the progress bar.
     */
    private static final int PATH_LENGTH_LIMIT = 153;
    /**
     * Underlying search composition.
     */
    private SearchComposition<R> searchComposition;
    /**
     * Progress handle instance.
     */
    private ProgressHandle progressHandle;
    /**
     * String in the middle of long text, usually three dots (...).
     */
    private String longTextMiddle = null;
    
    private ResultViewPanel resultViewPanel;

    public GraphicalSearchListener(SearchComposition<R> searchComposition,
            ResultViewPanel resultViewPanel) {
        this.searchComposition = searchComposition;
        this.resultViewPanel = resultViewPanel;
    }

    @Override
    public void searchStarted() {

        progressHandle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(ResultView.class,
                "TEXT_SEARCHING___"), new Cancellable() {               //NOI18N

            @Override
            public boolean cancel() {
                searchComposition.terminate();
                return true;
            }
        });
        progressHandle.start();
        searchComposition.getSearchResultsDisplayer().searchStarted();
    }

    @Override
    public void searchFinished() {
        if (progressHandle != null) {
            progressHandle.finish();
            progressHandle = null;
        }
        searchComposition.getSearchResultsDisplayer().searchFinished();
    }

    @Override
    public void directoryEntered(String path) {
        if (progressHandle != null) {
            progressHandle.progress(shortenPath(path));
        }
    }

    @Override
    public void fileContentMatchingStarted(String fileName) {
        if (progressHandle != null) {
            progressHandle.progress(shortenPath(fileName));
        }
    }

    /**
     * Shorten long part string
     */
    private String shortenPath(String p) {
        if (p.length() <= PATH_LENGTH_LIMIT) {
            return p;
        } else {
            String mid = getLongTextMiddle();
            int halfLength = (PATH_LENGTH_LIMIT - mid.length()) / 2;
            return p.substring(0, halfLength) + mid
                    + p.substring(p.length() - halfLength);
        }
    }

    /**
     * Get text replacement for middle part of long strings.
     */
    private String getLongTextMiddle() {
        if (longTextMiddle == null) {
            longTextMiddle = NbBundle.getMessage(SearchTask.class,
                    "TEXT_SEARCH_LONG_STRING_MIDDLE");                  //NOI18N
        }
        return longTextMiddle;
    }

    @Override
    public void generalError(Throwable t) {
        LOG.log(Level.INFO, t.getMessage(), t);
    }

    @Override
    public void fileContentMatchingError(String fileName, Throwable t) {
        LOG.log(Level.INFO, "Error matching file " + fileName, t);      //NOI18N
    }
}
