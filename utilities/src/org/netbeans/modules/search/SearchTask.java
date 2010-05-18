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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.search;

import java.nio.charset.Charset;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.LifecycleManager;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openidex.search.SearchType;

/**
 * Task performing search.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 * @author  kaktus
 */
final class SearchTask implements Runnable, Cancellable {

    /** nodes to search */
    private final SearchScope searchScope;
    /** */
    private final List<SearchType> customizedSearchTypes;
    /** */
    private final BasicSearchCriteria basicSearchCriteria;
    /** ResultModel result model. */
    private ResultModel resultModel;
    /** <code>SearchGroup</code> to search on. */
    private SpecialSearchGroup searchGroup;
    /** attribute used by class <code>Manager</code> */
    private boolean notifyWhenFinished = true;
    /** */
    private volatile boolean interrupted = false;
    /** */
    private volatile boolean finished = false;
    /** */
    private final String replaceString;

    private ProgressHandle progressHandle;
    
    /**
     * Creates a new <code>SearchTask</code>.
     *
     * @param  searchScope  defines scope of the search task
     * @param  basicSearchCriteria  basic search criteria
     * @param  customizedSearchTypes  search types
     */
    public SearchTask(final SearchScope searchScope,
                      final BasicSearchCriteria basicSearchCriteria,
                      final List<SearchType> customizedSearchTypes) {
        this.searchScope = searchScope;
        this.basicSearchCriteria = basicSearchCriteria;
        this.customizedSearchTypes = customizedSearchTypes;
        
        this.replaceString = (basicSearchCriteria != null)
                             ? basicSearchCriteria.getReplaceExpr()
                             : null;
    }
    
    /**
     */
    private boolean isSearchAndReplace() {
        return (replaceString != null);
    }
    
    /** Runs the search task. */
    public void run() {
        if (isSearchAndReplace()) {
            LifecycleManager.getDefault().saveAll();
        }
        
        /* Start the actual search: */
        ensureResultModelExists();
        if (searchGroup == null) {
            return;
        }

        progressHandle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(ResultView.class,"TEXT_PREPARE_SEARCH___"), this); // NOI18N
        progressHandle.start();

        searchGroup.setListeningSearchTask(this);
        try {
            searchGroup.search();
        } catch (RuntimeException ex) {
            resultModel.searchException(ex);
            ex.printStackTrace();
        } finally {
            searchGroup.setListeningSearchTask(null);
            finished = true;
            progressHandle.finish();
            progressHandle = null;
        }
    }
    
    SearchTask createNewGeneration() {
        return new SearchTask(searchScope,
                              basicSearchCriteria,
                              customizedSearchTypes);
    }

    /**
     */
    BasicSearchCriteria getSearchCriteria() {
        return basicSearchCriteria;
    }

    /**
     */
    ResultModel getResultModel() {
        ensureResultModelExists();
        return resultModel;
    }
    
    /**
     */
    private void ensureResultModelExists() {
        if (resultModel == null) {
            searchGroup = new SpecialSearchGroup(basicSearchCriteria,
                                                 customizedSearchTypes,
                                                 searchScope);
            resultModel = new ResultModel(searchGroup,
                                          replaceString);
        }
    }

    /**
     * Called when a matching object is found by the <code>SearchGroup</code>.
     * Notifies the result model of the found object and stops searching
     * if number of the found objects reached the limit.
     *
     * @param  object  found matching object
     * @param  charset  charset used for full-text search of the object,
     *                  or {@code null} if the object was not full-text searched
     */
    void matchingObjectFound(Object object, Charset charset) {
        boolean canContinue = resultModel.objectFound(object, charset);
        if (!canContinue) {
            searchGroup.stopSearch();
        }
    }

    void searchStarted(int searchUnitsCount) {
        progressHandle.finish();
        progressHandle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(ResultView.class,"TEXT_SEARCHING___"), this); // NOI18N
        progressHandle.start(searchUnitsCount);
    }

    void progress(int progress) {
        progressHandle.progress(progress);
    }

    /**
     * Stops this search task.
     * This method also sets a value of attribute
     * <code>notifyWhenFinished</code>. This method may be called multiple
     * times (even if this task is already stopped) to change the value
     * of the attribute.
     *
     * @param  notifyWhenFinished  new value of attribute
     *                             <code>notifyWhenFinished</code>
     */
    void stop(boolean notifyWhenFinished) {
        if (notifyWhenFinished == false) {     //allow only change true -> false
            this.notifyWhenFinished = notifyWhenFinished;
        }
        stop();
    }
    
    /**
     * Stops this search task.
     *
     * @see  #stop(boolean)
     */
    void stop() {
        if (!finished) {
            interrupted = true;
        }
        if (searchGroup != null) {
            searchGroup.stopSearch();
        }
    }
    
    /** 
     * Cancel processing of the task. 
     *
     * @return true if the task was succesfully cancelled, false if job
     *         can't be cancelled for some reason
     * @see org.openide.util.Cancellable#cancel
     */
    public boolean cancel() {
        stop();
        return true;
    }

    /**
     * Returns value of attribute <code>notifyWhenFinished</code>.
     *
     * @return  current value of the attribute
     */
    boolean notifyWhenFinished() {
        return notifyWhenFinished;
    }
    
    /**
     * Was this search task interrupted?
     *
     * @return  <code>true</code> if this method has been interrupted
     *          by calling {@link #stop()} or {@link #stop(boolean)}
     *          during the search; <code>false</code> otherwise
     */
    boolean wasInterrupted() {
        return interrupted;
    }

}
