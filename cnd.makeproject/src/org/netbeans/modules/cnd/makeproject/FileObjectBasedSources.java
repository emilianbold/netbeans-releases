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

package org.netbeans.modules.cnd.makeproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;

/**
 * SourcesHelper does not work with pure FileObjects, it demands that FileUtil.toFile() is not null.
 * So we have to create an implementation of our own
 * @author Vladimir Kvashin
 */
public class FileObjectBasedSources implements Sources {

    private final ChangeSupport cs = new ChangeSupport(this);
    private final Map<String, List<SourceGroup>> groups = new HashMap<String, List<SourceGroup>>();

    @Override
    public SourceGroup[] getSourceGroups(String type) {
        synchronized (this) {
            List<SourceGroup> l = groups.get(type);
            return (l == null) ? new SourceGroup[0] : l.toArray(new SourceGroup[l.size()]);
        }
    }

    public SourceGroup addGroup(Project project, String type, FileObject fo, String displayName) {
        synchronized (this) {
            List<SourceGroup> l = groups.get(type);
            if (l == null) {
                l = new ArrayList<SourceGroup>();
                groups.put(type, l);
            }
            SourceGroup group = GenericSources.group(project, fo, fo.getPath(), displayName, null, null);
            l.add(group);
            return group;
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

//    private class Group implements SourceGroup {
//
//        private final FileObject loc;
//        private final String displayName;
//        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
//
//        private Group(FileObject loc, String displayName) {
//            this.loc = loc;
//            this.displayName = displayName;
//        }
//
//        @Override
//        public boolean contains(FileObject file) throws IllegalArgumentException {
//            if (file == loc) {
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public String getDisplayName() {
//            return displayName;
//        }
//
//        @Override
//        public Icon getIcon(boolean opened) {
//            return null;
//        }
//
//        @Override
//        public String getName() {
//            return loc.getPath();
//        }
//
//        @Override
//        public FileObject getRootFolder() {
//            return loc;
//        }
//
//        @Override
//        public void addPropertyChangeListener(PropertyChangeListener listener) {
//            pcs.addPropertyChangeListener(listener);
//        }
//
//        @Override
//        public void removePropertyChangeListener(PropertyChangeListener listener) {
//            pcs.removePropertyChangeListener(listener);
//        }
//
//        @Override
//        public String toString() {
//            return FileObjectBasedSources.class.getSimpleName() + "." + getClass().getSimpleName() + ' ' + loc.getPath(); // NOI18N
//        }
//    }
}
