/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.dummies;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Marian Petras
 */
@BugtrackingConnector.Registration (
    id=DummyBugtrackingConnector.ID,
    displayName=DummyBugtrackingConnector.DISPLAY_NAME,
    tooltip=DummyBugtrackingConnector.TOOLTIP
)    
public class DummyBugtrackingConnector extends BugtrackingConnector {
    public static final String ID = "DummyBugtrackingConnector";
    public static final String DISPLAY_NAME = "Dummy bugtracking connector";
    public static final String TOOLTIP = "bugtracking connector created for testing purposes";
    
    private char newRepositoryName = 'A';
    private int newRepositoryNumber = 0;
    private List<RepositoryProvider> repositories;
    public static DummyBugtrackingConnector instance;

    public DummyBugtrackingConnector() {
        instance = this;
    }
    
    @Override
    public RepositoryProvider createRepository() {
        return createRepository(generateNewRepositoryName());
    }

    public RepositoryProvider createRepository(String repositoryName) {
        RepositoryProvider newRepository = new DummyRepository(this, repositoryName);
        storeRepository(newRepository);
        return newRepository;
    }

    private String generateNewRepositoryName() {
        if (newRepositoryName != 'X') {
            return String.valueOf(newRepositoryName++);
        } else {
            return 'X' + String.valueOf(++newRepositoryNumber);
        }
    }

    private void storeRepository(RepositoryProvider repository) {
        if (repositories == null) {
            repositories = new ArrayList<RepositoryProvider>();
        }
        repositories.add(repository);
        RepositoryRegistry.getInstance().addRepository(repository);
    }

    void removeRepository(DummyRepository repository) {
        if (repositories == null) {
            return;
        }

        repositories.remove(repository);
        RepositoryRegistry.getInstance().removeRepository(repository);
    }

    public void reset() {
        if(repositories != null) {
            for (RepositoryProvider repository : repositories) {
                RepositoryRegistry.getInstance().removeRepository(repository);
            }
            repositories = null;
        }
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public RepositoryProvider createRepository(RepositoryInfo info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
