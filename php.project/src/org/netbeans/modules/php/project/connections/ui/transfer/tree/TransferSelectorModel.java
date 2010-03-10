/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.ui.transfer.tree;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.php.project.connections.TransferFile;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChangeSupport;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooserPanel.TransferFilesChangeListener;
import org.openide.nodes.Node;

final class TransferSelectorModel {
    private final Set<TransferFile> transferFiles;
    private final Set<TransferFile> selected = new HashSet<TransferFile>();
    private final TransferFilesChangeSupport filesChangeSupport = new TransferFilesChangeSupport(this);

    public TransferSelectorModel(Set<TransferFile> transferFiles, long timestamp) {
        assert transferFiles != null;

        this.transferFiles = transferFiles;

        boolean select = timestamp == -1;
        for (TransferFile file : transferFiles) {
            if (timestamp != -1) {
                // we have some timestamp
                select = file.getTimestamp() > timestamp;
            }
            if (select && !file.isProjectRoot()) {
                // intentionally not addChildren()!
                selected.add(file);
            }
        }
    }

    public void addChangeListener(TransferFilesChangeListener listener) {
        filesChangeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(TransferFilesChangeListener listener) {
        filesChangeSupport.removeChangeListener(listener);
    }

    public boolean isNodeSelected(Node node) {
        return selected.contains(getTransferFile(node));
    }

    public boolean isNodePartiallySelected(Node node) {
        TransferFile transferFile = getTransferFile(node);
        if (transferFile.isFile()
                || !selected.contains(transferFile)) {
            return false;
        }
        return !hasAllChildrenSelected(transferFile);
    }

    public void setNodeSelected(Node node, boolean select) {
        TransferFile transferFile = getTransferFile(node);
        if (transferFile == null) {
            // dblclick on root node
            return;
        }
        if (select) {
            addChildren(transferFile);
            addParents(transferFile);
        } else {
            removeChildren(transferFile);
        }
        filesChangeSupport.fireSelectedFilesChange();
    }

    public Set<TransferFile> getData() {
        return transferFiles;
    }

    public Set<TransferFile> getSelected() {
        return new HashSet<TransferFile>(selected);
    }

    public int getSelectedSize() {
        return selected.size();
    }

    private TransferFile getTransferFile(Node node) {
        return node.getLookup().lookup(TransferFile.class);
    }

    private void addChildren(TransferFile file) {
        if (file.isProjectRoot()) {
            // ignored
            return;
        }
        selected.add(file);
        for (TransferFile child : file.getChildren()) {
            addChildren(child);
        }
    }

    private void addParents(TransferFile fromFile) {
        TransferFile parent = fromFile.getParent();
        if (parent != null) {
            if (parent.isProjectRoot()) {
                // ignored
                return;
            }
            selected.add(parent);
            addParents(parent);
        }
    }

    private void removeChildren(TransferFile file) {
        selected.remove(file);
        for (TransferFile child : file.getChildren()) {
            removeChildren(child);
        }
    }

    private boolean hasAllChildrenSelected(TransferFile transferFile) {
        if (!selected.contains(transferFile)) {
            return false;
        }
        for (TransferFile child : transferFile.getChildren()) {
            if (!hasAllChildrenSelected(child)) {
                return false;
            }
        }
        return true;
    }
}
