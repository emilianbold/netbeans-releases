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

import java.awt.Image;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Marian Petras
 */
public class DummyRepository extends Repository {

    private static final Image icon = ImageUtilities.loadImage(
            "org/netbeans/modules/bugtracking/dummies/DummyRepositoryIcon.png");

    private final DummyBugtrackingConnector connector;
    private final String id;

    public DummyRepository(DummyBugtrackingConnector connector, String id) {
        this.connector = connector;
        this.id = id;
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public String getDisplayName() {
        return "Dummy repository \"" + getID() + '"';
    }

    @Override
    public String getTooltip() {
        return "dummy repository created for testing purposes";
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getUrl() {
        assert false : "This was assumed to be never called.";
        return null;
    }

    @Override
    public Issue getIssue(String id) {
        assert false : "This was assumed to be never called.";
        return null;
    }

    @Override
    public void remove() {
        connector.removeRepository(this);
    }

    @Override
    public BugtrackingController getController() {
        assert false : "This was assumed to be never called.";
        return null;
    }

    @Override
    public Query createQuery() {
        assert false : "This was assumed to be never called.";
        return null;
    }

    @Override
    public Issue createIssue() {
        assert false : "This was assumed to be never called.";
        return null;
    }

    @Override
    public Query[] getQueries() {
        assert false : "This was assumed to be never called.";
        return new Query[0];
    }

    @Override
    public Collection<RepositoryUser> getUsers() {
        assert false : "This was assumed to be never called.";
        return null;
    }

    @Override
    public Issue[] simpleSearch(String criteria) {
        assert false : "This was assumed to be never called.";
        return new Issue[0];
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

}
