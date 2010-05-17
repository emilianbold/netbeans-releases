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

package org.netbeans.modules.bugtracking.ui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.Timer;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;

/**
 * Model of search results. Works as ListModel for JList which is displaying
 * results. Actual results data are stored in List of CategoryResult objects.
 *
 * As model changes can come very frequently, firing of changes is coalesced.
 * Coalescing of changes helps UI to reduce flicker and unnecessary updates.
 *
 * @author Jan Becicka
 */
public final class ResultsModel extends AbstractListModel implements ActionListener {

    private static ResultsModel instance;
    private List<PopupItem> results;

    /* Timer for coalescing fast coming changes of model */
    private Timer fireTimer;

    /** Amount of time during which model has to be unchanged in order to fire
     * changes to listeners. */
    static final int COALESCE_TIME = 200;

    private Map<Repository, Set<Issue>> issuesCached = new HashMap<Repository, Set<Issue>>();

    /** Singleton */
    private ResultsModel () {
    }

    public static ResultsModel getInstance () {
        if (instance == null) {
            instance = new ResultsModel();
        }
        return instance;
    }

    void setContent (List<PopupItem> results) {
        this.results = results;
        maybeFireChanges();
    }

    synchronized void cacheIssues(Repository repo, Issue[] issues) {
        HashSet<Issue> s = new HashSet<Issue>();
        for (Issue issue : issues) {
            assert issue != null;
            s.add(issue);
        }
        issuesCached.put(repo, s);
    }

    synchronized Issue[] getCachedIssues(Repository repo) {
        if(issuesCached != null) {
            Set<Issue> s = issuesCached.get(repo);
            if(s != null) return s.toArray(new Issue[s.size()]);
        }
        return new Issue[0];
    }
    /******* AbstractListModel impl ********/

    public int getSize() {
        if (results == null) {
            return 0;
        }
        return results.size();
    }

    public Object getElementAt (int index) {
        if (results == null) {
            return null;
        }
        return results.get(index);
    }

    private void maybeFireChanges () {
        if (fireTimer == null) {
            fireTimer = new Timer(COALESCE_TIME, this);
        }
        if (!fireTimer.isRunning()) {
            // first change in possible flurry, start timer
            fireTimer.start();
        } else {
            // model change came too fast, let's wait until providers calm down :)
            fireTimer.restart();
        }
    }

    public void actionPerformed(ActionEvent e) {
        fireTimer.stop();
        fireContentsChanged(this, 0, getSize());
    }
}
