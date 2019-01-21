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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.mobility.project.ui;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import org.netbeans.api.project.SourceGroup;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
/**
 * Fake source group used to create a package view for misc resource nodes
 *
 * 
 */
class LibrariesSourceGroup implements SourceGroup {

    private final FileObject root;
    private final String displayName;
    private final Icon icon;
    private final Icon openIcon;

    LibrariesSourceGroup(FileObject root, String displayName, Icon icon, Icon openIcon) {
        super();
        assert root != null;
        this.root = root;
        this.displayName = displayName;
        this.icon = icon;
        this.openIcon = openIcon;
    }

    public FileObject getRootFolder() {
        return this.root;
    }

    public String getName() {
        try {
            return root.getURL().toExternalForm();
        } catch (FileStateInvalidException fsi) {
            ErrorManager.getDefault().notify(fsi);
            return root.toString();
        }
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Icon getIcon(boolean opened) {
        return opened ? openIcon : icon;
    }

    public boolean contains(FileObject file) throws IllegalArgumentException {
        return root.equals(file) || FileUtil.isParentOf(root, file);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LibrariesSourceGroup)) {
            return false;
        }
        LibrariesSourceGroup osg = (LibrariesSourceGroup) other;
        return displayName == null ? osg.displayName == null : 
            displayName.equals(osg.displayName) && root == null ?
                osg.root == null : root.equals(osg.root);
    }

    @Override
    public int hashCode() {
        return ((displayName == null ? 0 : 
            displayName.hashCode()) << 16) |
            ((root == null ? 0 : root.hashCode()) & 65535);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //do nothing
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        //do nothing
    }
}
