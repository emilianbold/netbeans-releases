
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
package org.netbeans.modules.ods.tasks;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.ods.tasks.issue.C2CIssue;

/**
 *
 * @author Tomas Stupka
 */
public class C2CIssueProvider extends IssueProvider<C2CIssue> {

    @Override
    public String getDisplayName(C2CIssue data) {
        return data.getDisplayName();
    }

    @Override
    public String getTooltip(C2CIssue data) {
        return data.getTooltip();
    }

    @Override
    public String getID(C2CIssue data) {
        return data.getID();
    }

    @Override
    public String[] getSubtasks(C2CIssue data) {
        return data.getSubtasks();
    }

    @Override
    public String getSummary(C2CIssue data) {
        return data.getSummary();
    }

    @Override
    public boolean isNew(C2CIssue data) {
        return data.isNew();
    }

    @Override
    public boolean isFinished(C2CIssue data) {
        return data.isFinished();
    }

    @Override
    public boolean refresh(C2CIssue data) {
        return data.refresh();
    }

    @Override
    public void addComment(C2CIssue data, String comment, boolean closeAsFixed) {
        data.addComment(comment, closeAsFixed);
    }

    @Override
    public void attachPatch(C2CIssue data, File file, String description) {
        data.attachPatch(file, description);
    }

    @Override
    public BugtrackingController getController(C2CIssue data) {
        return data.getController();
    }

    @Override
    public void removePropertyChangeListener(C2CIssue data, PropertyChangeListener listener) {
        data.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(C2CIssue data, PropertyChangeListener listener) {
        data.addPropertyChangeListener(listener);
    }
    
}
