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
package org.netbeans.modules.maven.model.profile.impl;

import java.util.Collections;
import org.netbeans.modules.maven.model.profile.ProfilesComponent;
import org.netbeans.modules.maven.model.profile.ProfilesComponentVisitor;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.netbeans.modules.maven.model.profile.Repository;
import org.netbeans.modules.maven.model.profile.RepositoryPolicy;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class RepositoryImpl extends ProfilesComponentImpl implements Repository {

    public RepositoryImpl(ProfilesModel model, Element element) {
        super(model, element);
    }
    
    public RepositoryImpl(ProfilesModel model, boolean pluginRepo) {
        this(model, createElementNS(model,
                pluginRepo ? model.getProfilesQNames().PLUGINREPOSITORY : model.getProfilesQNames().REPOSITORY));
    }

    // attributes

    // child elements
    public RepositoryPolicy getReleases() {
        return getChild(RepositoryPolicy.class);
    }

    public void setReleases(RepositoryPolicy releases) {
        setChild(RepositoryPolicy.class, getModel().getProfilesQNames().RELEASES.getName(), releases,
                Collections.<Class<? extends ProfilesComponent>>emptyList());
    }

    public RepositoryPolicy getSnapshots() {
        return getChild(RepositoryPolicy.class);
    }

    public void setSnapshots(RepositoryPolicy snapshots) {
        setChild(RepositoryPolicy.class, getModel().getProfilesQNames().SNAPSHOTS.getName(), snapshots,
                Collections.<Class<? extends ProfilesComponent>>emptyList());
    }

    public String getId() {
        return getChildElementText(getModel().getProfilesQNames().ID.getQName());
    }

    public void setId(String id) {
        setChildElementText(getModel().getProfilesQNames().ID.getName(), id,
                getModel().getProfilesQNames().ID.getQName());
    }

    public String getName() {
        return getChildElementText(getModel().getProfilesQNames().NAME.getQName());
    }

    public void setName(String name) {
        setChildElementText(getModel().getProfilesQNames().NAME.getName(), name,
                getModel().getProfilesQNames().NAME.getQName());
    }

    public String getUrl() {
        return getChildElementText(getModel().getProfilesQNames().URL.getQName());
    }

    public void setUrl(String url) {
        setChildElementText(getModel().getProfilesQNames().URL.getName(), url,
                getModel().getProfilesQNames().URL.getQName());
    }

    public String getLayout() {
        return getChildElementText(getModel().getProfilesQNames().LAYOUT.getQName());
    }

    public void setLayout(String layout) {
        setChildElementText(getModel().getProfilesQNames().LAYOUT.getName(), layout,
                getModel().getProfilesQNames().LAYOUT.getQName());
    }

    public static class RepoList extends ListImpl<Repository> {
        public RepoList(ProfilesModel model, Element element) {
            super(model, element, model.getProfilesQNames().REPOSITORY, Repository.class);
        }

        public RepoList(ProfilesModel model) {
            this(model, createElementNS(model, model.getProfilesQNames().REPOSITORIES));
        }
    }

    public void accept(ProfilesComponentVisitor visitor) {
        visitor.visit(this);
    }

    public static class PluginRepoList extends ListImpl<Repository> {
        public PluginRepoList(ProfilesModel model, Element element) {
            super(model, element, model.getProfilesQNames().PLUGINREPOSITORY, Repository.class);
        }

        public PluginRepoList(ProfilesModel model) {
            this(model, createElementNS(model, model.getProfilesQNames().PLUGINREPOSITORIES));
        }
    }

}