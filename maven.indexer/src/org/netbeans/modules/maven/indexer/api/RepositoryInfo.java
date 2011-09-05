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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 *
 * @author mkleint
 */
public final class RepositoryInfo {

    /** @see org.sonatype.nexus.index.context.DefaultIndexingContext#INDEX_DIRECTORY */
    static final String DEFAULT_INDEX_SUFFIX = ".index/"; // NOI18N

    private final String id;
    private final String type;
    private final @NonNull String name;
    private final String repositoryPath;
    private final String repositoryUrl;
    private final String indexUpdateUrl;

    public RepositoryInfo(String id, String type, @NullAllowed String name, String repositoryPath, String repositoryUrl) throws URISyntaxException {
        this(id, type, name, repositoryPath, repositoryUrl, null);
    }
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    RepositoryInfo(String id, String type, @NullAllowed String name, String repositoryPath,
            String repositoryUrl, String indexUpdateUrl) throws URISyntaxException {
        this.id = id;
        this.type = type;
        this.name = name != null ? name : id;
        this.repositoryPath = repositoryPath;
        if (repositoryUrl != null && !repositoryUrl.endsWith("/")) {
            repositoryUrl += "/";
        }
        if (repositoryUrl != null) {
            new URI(repositoryUrl);
        }
        this.repositoryUrl = repositoryUrl;
        this.indexUpdateUrl = indexUpdateUrl != null ? indexUpdateUrl : repositoryUrl != null ? repositoryUrl + DEFAULT_INDEX_SUFFIX : null;
        if (!isLocal() ^ isRemoteDownloadable()) {
            throw new IllegalArgumentException("Cannot have both local and remote index fields filled in. Repository: " + id + " Path=" + repositoryPath + " Remote URL:" + indexUpdateUrl);
        }
    }

    public @NonNull String getId() {
        return id;
    }

    public @NonNull String getType() {
        return type;
    }

    public @NonNull String getName() {
        return name;
    }

    public @CheckForNull String getRepositoryPath() {
        return repositoryPath;
    }

    public @CheckForNull String getRepositoryUrl() {
        return repositoryUrl;
    }

    public @CheckForNull String getIndexUpdateUrl() {
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

    public @Override String toString() {
        return id;
    }
    
}
