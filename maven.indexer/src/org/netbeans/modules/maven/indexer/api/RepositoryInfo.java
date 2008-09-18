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
package org.netbeans.modules.maven.indexer.api;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public final class RepositoryInfo {

    private String id;
    private String type;
    private String name;
    private String repositoryPath;
    private String repositoryUrl;
    private String indexUpdateUrl;

    public RepositoryInfo(String id, String type, String name, String repositoryPath,
            String repositoryUrl, String indexUpdateUrl) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.repositoryPath = repositoryPath;
        this.repositoryUrl = repositoryUrl;
        this.indexUpdateUrl = indexUpdateUrl;
        assert (isLocal() == true && isRemoteDownloadable() == true) != true : "XXXCannot have both local and remote index fields filled in."; //NOI18N
    }

    public static RepositoryInfo createRepositoryInfo(FileObject fo) {
        String type = (String) fo.getAttribute(RepositoryPreferences.KEY_TYPE);
        assert type != null;
        String id = fo.getName();
        String name = id;
        String remoteBundleName = (String) fo.getAttribute ("SystemFileSystem.localizingBundle"); // NOI18N
        if (remoteBundleName != null) {
            try {
                ResourceBundle bundle = NbBundle.getBundle (remoteBundleName);
                String nm = bundle.getString(fo.getPath());
                if (nm != null) {
                    name = nm;
                }
            } catch (MissingResourceException e) {
                //just ignore
            }
        }
        String path = (String) fo.getAttribute(RepositoryPreferences.KEY_PATH);
        String repourl =(String) fo.getAttribute(RepositoryPreferences.KEY_REPO_URL);
        String indexurl = (String) fo.getAttribute(RepositoryPreferences.KEY_INDEX_URL);
        return new RepositoryInfo(id, type, name, path, repourl, indexurl);
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getIndexUpdateUrl() {
        return indexUpdateUrl;
    }

    public boolean isRemoteDownloadable() {
        return indexUpdateUrl != null;
    }
    
    public boolean isLocal() {
        return repositoryPath != null;
    }
    
    public void addChangeListener(ChangeListener cl) {
        synchronized (changeListeners) {
            changeListeners.add(cl);
        }
    }

    public void removeChangeListener(ChangeListener cl) {
        synchronized (changeListeners) {
            changeListeners.remove(cl);
        }
    }
    private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

    /**
     * Notifies listeners that the index content has changed.
     * to be called from RepositoryIndexerImplementation only.
     */
    public void fireChangeIndex() {
        synchronized (changeListeners) {
            for (ChangeListener changeListener : changeListeners) {
                changeListener.stateChanged(new ChangeEvent(this));
            }
        }

    }    
    
}

