/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla;

import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugzilla.issue.BugzillaIssueFinder;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 */
@BugtrackingConnector.Registration (
        id=BugzillaConnector.ID,
        displayName="#LBL_ConnectorName",
        tooltip="#LBL_ConnectorTooltip"
)    
public class BugzillaConnector extends BugtrackingConnector {

    public static final String ID = "org.netbeans.modules.bugzilla";
    private static BugzillaConnector instance;

    private BugzillaIssueFinder issueFinder;

    public BugzillaConnector() {
        instance = this;
    }
    
    static BugzillaConnector getInstance() {
        return instance;
    }

    @Override
    public RepositoryProvider createRepository(RepositoryInfo info) {
        return new BugzillaRepository(info);
    }
    
    @Override
    public RepositoryProvider createRepository() {
        Bugzilla.init();
        return new BugzillaRepository();
    }

    public static String getConnectorName() {
        return NbBundle.getMessage(BugzillaConnector.class, "LBL_ConnectorName");           // NOI18N
    }

    @Override
    public IssueFinder getIssueFinder() {
        if (issueFinder == null) {
            issueFinder = Lookup.getDefault().lookup(BugzillaIssueFinder.class);
        }
        return issueFinder;
    }

    @Override
    public Lookup getLookup() {
        return Lookups.singleton(Bugzilla.getInstance().getKenaiSupport());
    }

}
