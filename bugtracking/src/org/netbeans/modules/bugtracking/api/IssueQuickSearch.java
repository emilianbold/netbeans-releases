/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.api;

import java.io.File;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchPanel;

/**
 *
 * @author Tomas Stupka
 */
public final class IssueQuickSearch {
    private final QuickSearchPanel panel;
    
    public enum Filter {
        ATTACH_FILE,
        ALL
    }
    
    /**
     * Factory method
     * 
     * @param caller 
     * @return  
     */
    public static IssueQuickSearch create(JComponent caller) {
       return new IssueQuickSearch(caller, null, Filter.ALL);
    }
    
    /**
     * Factory method
     * 
     * @param caller 
     * @param context 
     * @return  
     */
    public static IssueQuickSearch create(JComponent caller, File context) {
       return new IssueQuickSearch(caller, context, Filter.ALL);
    }
    
    /**
     * Factory method
     * 
     * @param caller 
     * @param context 
     * @param filter 
     * @return  
     */
    public static IssueQuickSearch create(JComponent caller, File context, Filter filter) {
       return new IssueQuickSearch(caller, context, filter);
    }
    
    private IssueQuickSearch(JComponent caller, File context, Filter filter) {
        panel = new QuickSearchPanel(caller, context, filter);
    }
    
    /**
     * Sets the repository for which issues should be made available in 
     * the issue combo bar
     * @param repository 
     */
    public void setRepository(Repository repository) {
        panel.setRepository(repository);
    }
    
    /**
     * Returns the quick search component
     * @return 
     */
    public JComponent getComponent() {
        return panel;
    }
    
    /**
     * Returns the issue selected in the issue combo bar or null if none selected
     * @return 
     */
    public Issue getIssue() {
        return panel.getIssue();
    }

    /**
     * Register for notifications about changes in the issue combo bar
     * @param listener 
     */
    public void setChangeListener(ChangeListener listener) {
        panel.setChangeListener(listener);
    }

    /**
     * Select the given issue in the combo bar
     * @param issue 
     */
    public void setIssue(Issue issue) {
        panel.setIssue(issue.getImpl());
    }

    /**
     * 
     * @return 
     */
    public Repository getSelectedRepository() {
        return panel.getSelectedRepository();
    }

    /**
     * 
     * @param enabled 
     */
    public void setEnabled(boolean enabled) {
        panel.setEnabled(enabled);
    }
}
