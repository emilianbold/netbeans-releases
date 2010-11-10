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

import java.io.File;
import javax.swing.Action;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.git.ui.commit.GitFileNode;
import org.netbeans.modules.git.ui.diff.DiffAction;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author ondra
 */
public class StatusNode extends AbstractNode {
    private final GitFileNode node;
    private NameProperty nameProperty;
    private final Mode mode;

    public StatusNode (GitFileNode node, Mode mode) {
        super(Children.LEAF, Lookups.fixed(node.getLookupObjects()));
        this.node = node;
        this.mode = mode;
        initProperties();
    }

    public GitFileNode getNode() {
        return node;
    }

    @Override
    public Action getPreferredAction () {
        if (node.getInformation().containsStatus(FileInformation.Status.IN_CONFLICT)) {
            return new OpenInEditorAction(new File[] { getFile() });
        } else {
            return SystemAction.get(DiffAction.class);
        }
    }

    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();

        ps.put(nameProperty = new NameProperty(this));
        ps.put(new PathProperty(this));
        ps.put(new StatusProperty(this));

        sheet.put(ps);
        setSheet(sheet);
    }

    @Override
    public String getHtmlDisplayName () {
        return node.getInformation().annotateNameHtml(nameProperty.getValue());
    }

    @Override
    public String getName() {
        return node.getName();
    }

    public File getFile() {
        return node.getFile();
    }

    public GitFileNode getFileNode() {
        return node;
    }

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

    public static class PathProperty extends NodeProperty<String> {
        private String shortPath;
        public static final String NAME = "path"; //NOI18N
        public static final String DISPLAY_NAME = NbBundle.getMessage(StatusNode.class, "LBL_Path.DisplayName"); //NOI18N
        public static final String DESCRIPTION = NbBundle.getMessage(StatusNode.class, "LBL_Path.Description"); //NOI18N

        public PathProperty (StatusNode statusNode) {
            super(NAME, String.class, DISPLAY_NAME, DESCRIPTION); // NOI18N
            shortPath = statusNode.getNode().getRelativePath();
            setValue("sortkey", shortPath + "\t" + statusNode.getName()); // NOI18N
        }

        @Override
        public String getValue() {
            return shortPath;
        }
    }

    public static class NameProperty extends NodeProperty<String> {
        public static final String NAME = "name"; //NOI18N
        public static final String DISPLAY_NAME = NbBundle.getMessage(StatusNode.class, "LBL_File.DisplayName"); //NOI18N
        public static final String DESCRIPTION = NbBundle.getMessage(StatusNode.class, "LBL_File.Description"); //NOI18N
        private final GitFileNode fileNode;

        public NameProperty (StatusNode statusNode) {
            super(NAME, String.class, DISPLAY_NAME, DESCRIPTION); // NOI18N
            setValue("sortkey", statusNode.getNode().getName()); // NOI18N
            this.fileNode = statusNode.getNode();
        }

        @Override
        public String getValue () {
            return fileNode.getName();
        }
    }

    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N
    public static class StatusProperty extends NodeProperty<String> {
        public static final String NAME = "status"; //NOI18N
        public static final String DISPLAY_NAME = NbBundle.getMessage(StatusNode.class, "LBL_Status.DisplayName"); //NOI18N
        public static final String DESCRIPTION = NbBundle.getMessage(StatusNode.class, "LBL_Status.Description"); //NOI18N
        private final GitFileNode fileNode;
        private final Mode mode;

        public StatusProperty (StatusNode statusNode) {
            super(NAME, String.class, DISPLAY_NAME, DESCRIPTION);
            String sortable = Integer.toString(statusNode.getNode().getInformation().getComparableStatus());
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + statusNode.getNode().getName()); // NOI18N
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
