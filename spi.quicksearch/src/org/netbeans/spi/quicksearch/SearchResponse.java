/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.quicksearch;

import javax.swing.KeyStroke;
import org.netbeans.modules.quicksearch.CategoryResult;
import org.netbeans.modules.quicksearch.ResultsModel;
    
/**
 * Response object for collecting results of {@link SearchProvider#evaluate} search
 * operation. SearchProvider implementors are expected to fill SearchResponse
 * in steps by calling various {@link SearchResponse#addResult} methods.
 * 
 * @author Dafe Simonek
 */
public final class SearchResponse {

    private CategoryResult catResult;
   
    /** Package private creation, made available to other packages via
     * Accessor pattern.
     * @param catResult CategoryResult for storing response data 
     */
    SearchResponse (CategoryResult catResult) {
        this.catResult = catResult;
    }

    /**
     * Adds new result of quick search operation.
     *  
     * @param action Action to invoke when this result item is chosen by user
     * @param displayName Localized display name of this result item
     * 
     * @return true when result was accepted and more results are needed if available.
     * False when no further results are needed.
     * {@link SearchProvider} implementore should stop computing and leave
     * SearchProvider.evaluate(...) immediatelly if false is returned.
     */
    public boolean addResult (Runnable action, String displayName) {
        return addResult(action, displayName, null, null);
    }
    
    /**
     * Adds new result of quick search operation.
     *  
     * @param action Action to invoke when this result item is chosen by user
     * @param displayName Localized display name of this result item
     * @param shortcut Shortcut of this result item or null if shorcut isn't available
     * @param displayHint Localized display hint of this result item or null if not available
     * 
     * @return true when result was accepted and more results are needed if available.
     * False when no further results are needed.
     * {@link SearchProvider} implementore should stop computing and leave
     * SearchProvider.evaluate(...) immediatelly if false is returned.
     */
    public boolean addResult (Runnable action, String displayName,
                            KeyStroke shortcut, String displayHint) {
        return catResult.addItem(
                new ResultsModel.ItemResult(catResult, action,
                displayName, shortcut, displayHint));
    }

}
