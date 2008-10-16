/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rubyproject.ui;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.Collections;
import javax.swing.Action;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Represents Libraries folder node in the Ruby project's logical view.
 */
public final class LibrariesNode extends AbstractNode {

    private static final Image LIBRARIES_ICON_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/ruby/rubyproject/resources/libraries-badge.png");    //NOI18N

    private static Image folderIconCache;
    private static Image openedFolderIconCache;

    public LibrariesNode(final RubyBaseProject project) {
        super(new LibrariesChildren(project));
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(LibrariesNode.class, "LibrariesNode.Libraries");
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }

    @Override
    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    private Image computeIcon(boolean opened, int type) {
        Image image = getFolderIcon(opened);
        image = ImageUtilities.mergeImages(image, LIBRARIES_ICON_BADGE, 7, 7);
        return image;
    }

    /**
     * Returns Icon of folder on active platform.
     * 
     * @param opened should the icon represent opened folder
     * @return the folder icon
     */
    static synchronized Image getFolderIcon(boolean opened) {
        if (opened) {
            if (openedFolderIconCache == null) {
                openedFolderIconCache = getTreeFolderIcon(opened);
            }
            return openedFolderIconCache;
        } else {
            if (folderIconCache == null) {
                folderIconCache = getTreeFolderIcon(opened);
            }
            return folderIconCache;
        }
    }

    /**
     * Returns default folder icon as {@link java.awt.Image}. Never returns
     * <code>null</code>.
     *
     * @param opened wheter closed or opened icon should be returned.
     */
    private static Image getTreeFolderIcon(boolean opened) {
        final Node n = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
        final Image base = opened ? n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16) : n.getIcon(BeanInfo.ICON_COLOR_16x16);
        assert base != null;
        return base;
    }

    /**
     * So far only Ruby platform node.
     */
    private final static class LibrariesChildren extends Children.Keys {

        private final RubyBaseProject project;

        LibrariesChildren(final RubyBaseProject project) {
            this.project = project;
        }

        @Override
        protected void addNotify() {
            setKeys();
        }

        @Override
        protected void removeNotify() {
            this.setKeys(Collections.<RubyPlatform>emptySet());
        }

        @Override
        protected Node[] createNodes(final Object key) {
            return new Node[]{new PlatformNode(project)};
        }

        private void setKeys() {
            RubyPlatform platform = project.getPlatform();
            assert platform != null : "platform cannot be null";
            this.setKeys(Collections.singleton(platform));
        }

    }
}
