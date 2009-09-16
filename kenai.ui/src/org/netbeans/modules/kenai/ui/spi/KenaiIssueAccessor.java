/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.awt.Component;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * A class that is used for opening the issue in the IDE, based on repository and
 * the issue identifier.
 *
 * @author joshis
 * @author Tomas Stupka
 */
public abstract class KenaiIssueAccessor {

    /**
     * Retrieve the instance of the KenaiIssueAccessor from lookup
     * @return Default instance of the issue accessor
     */
    public static KenaiIssueAccessor getDefault() {
        return Lookup.getDefault().lookup(KenaiIssueAccessor.class);
    }

    /**
     * Opens a topcomponet with an issue with given ID
     * @param proj Kenai project where the issue is supposed to be
     * @param issueID ID of the issue
     */
    public abstract void open(KenaiProject proj, String issueID);

    /**
     * Returns an array of 0 to 5 recently opened kenai issues
     *
     * @return 0 to 5 recently opened kenai issues
     */
    public abstract IssueHandle[] getRecentIssues();

    /**
     * Returns an array of 0 to 5 recently opened kenai issues from the given project
     * 
     * @param project
     * @return 0 to 5 recently opened kenai issues or null a problem occured - e.g. no bugtracking repository was
     * available for the given KenaiProject
     */
    public abstract IssueHandle[] getRecentIssues(KenaiProject project);

    /**
     * Represents a kenai issue
     */
    public abstract class IssueHandle {

        /**
         * Returns the issues id
         *
         * @return
         */
        public abstract String getID();

        /**
         * Returns the project this issue belongs to
         *
         * @return
         */
        public abstract KenaiProject getProject();

        /**
         * Returns the issues short display name
         *
         * @return
         */
        public abstract String getShortDisplayName();

        /**
         * Returns the issues display name
         *
         * @return
         */
        public abstract String getDisplayName();

        /**
         * Determines whether this issues TopComponent is opened
         *
         * @return
         * @see TopComponent#isOpened() 
         */
        public abstract boolean isOpened();

        /**
         * Determines whether this issues TopComponent is showing on screen.
         *
         * @return
         * @see Component#isShowing()
         */
        public abstract boolean isShowing();
    }

}
