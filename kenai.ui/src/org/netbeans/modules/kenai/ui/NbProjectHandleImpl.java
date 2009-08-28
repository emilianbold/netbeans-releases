/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.kenai.ui.spi.NbProjectHandle;
import org.netbeans.modules.project.ui.api.UnloadedProjectInformation;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * Handle representing netbeans project in kenai dashboard
 * @author Jan Becicka
 */
public class NbProjectHandleImpl extends NbProjectHandle{

    private Icon icon;
    private String displayName;
    URL url;
    private SourceHandleImpl parent;

    NbProjectHandleImpl(Project p, SourceHandleImpl parent) throws IOException {
        displayName = ProjectUtils.getInformation(p).getDisplayName();
        icon = ProjectUtils.getInformation(p).getIcon();
        url = p.getProjectDirectory().getURL();
        p.getProjectDirectory().addFileChangeListener(new FileChangeAdapter() {
            @Override
            public void fileDeleted(FileEvent fe) {
                try {
                    if (fe.getFile().getURL().equals(url)) {
                        remove();
                    }
                } catch (FileStateInvalidException ex) {
                    //ignore
                }
            }
        });
        this.parent = parent;
        assert this.parent!=null;
        assert displayName!=null;
        assert icon!=null;
        assert url!=null;
    }

    NbProjectHandleImpl(UnloadedProjectInformation i, SourceHandleImpl parent) {
        displayName = i.getDisplayName();
        icon = i.getIcon();
        url = i.getURL();
        this.parent = parent;
        assert this.parent!=null;
        assert displayName!=null;
        assert icon!=null;
        assert url!=null;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    /**
     * Getter fot NB Project
     * @return
     */
    public Project getProject() {
        try {
            final FileObject fo = URLMapper.findFileObject(url);
            if (fo==null) {
                return null;
            }
            Project project = ProjectManager.getDefault().findProject(fo);
            if (project==null)
                Logger.getLogger(NbProjectHandleImpl.class.getName()).severe("Cannot find project for " + fo.getPath()); // NOI18N
            return project;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public void remove() {
        parent.remove(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NbProjectHandleImpl other = (NbProjectHandleImpl) obj;
        if (this.url != other.url && (this.url == null || !this.url.equals(other.url))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.url != null ? this.url.hashCode() : 0);
        return hash;
    }
}
