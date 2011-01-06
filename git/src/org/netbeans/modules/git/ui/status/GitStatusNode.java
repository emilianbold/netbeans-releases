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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.status;

import javax.swing.Action;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.git.ui.commit.GitFileNode;
import org.netbeans.modules.git.ui.conflicts.ResolveConflictsAction;
import org.netbeans.modules.git.ui.diff.DiffAction;
import org.netbeans.modules.versioning.util.status.VCSStatusNode;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
public class GitStatusNode extends VCSStatusNode<GitFileNode> {
    private final Mode mode;

    public GitStatusNode (GitFileNode node, Mode mode) {
        super(node);
        this.mode = mode;
        initProperties();
    }

    @Override
    public Action getPreferredAction () {
        if (node.getInformation().containsStatus(FileInformation.Status.IN_CONFLICT)) {
            return SystemAction.get(ResolveConflictsAction.class);
        } else {
            return SystemAction.get(DiffAction.class);
        }
    }

    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();

        ps.put(nameProperty);
        ps.put(pathProperty);
        ps.put(new GitStatusProperty(this));

        sheet.put(ps);
        setSheet(sheet);
    }

    @Override
    public void refresh() {
        // do something when needed
    }

    protected static abstract class NodeProperty<T> extends ReadOnly<T> {
        protected NodeProperty (String name, Class<T> type, String displayName, String description) {
            super(name, type, displayName, description);
        }

        @Override
        public String toString() {
            return getValue().toString();
        }

        @Override
        public abstract T getValue ();
    }

    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N
    public static class GitStatusProperty extends NodeProperty<String> {
        public static final String NAME = "gitstatus"; //NOI18N
        public static final String DISPLAY_NAME = NbBundle.getMessage(GitStatusNode.class, "LBL_Status.DisplayName"); //NOI18N
        public static final String DESCRIPTION = NbBundle.getMessage(GitStatusNode.class, "LBL_Status.Description"); //NOI18N
        private final GitFileNode fileNode;
        private final Mode mode;

        public GitStatusProperty (GitStatusNode statusNode) {
            super(NAME, String.class, DISPLAY_NAME, DESCRIPTION);
            String sortable = Integer.toString(statusNode.getFileNode().getInformation().getComparableStatus());
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + statusNode.getFileNode().getName()); // NOI18N
            this.fileNode = statusNode.node;
            this.mode = statusNode.mode;
        }

        @Override
        public String getValue () {
            FileInformation finfo =  fileNode.getInformation();
            return finfo.getStatusText(mode);
        }
    }
}
