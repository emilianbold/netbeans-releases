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
package org.netbeans.modules.maven.model.profile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author mkleint
 */
public final class ProfilesQNames {
    
    public final ProfilesQName PROFILESROOT; // NOI18N
    public final ProfilesQName REPOSITORY; // NOI18N
    public final ProfilesQName PLUGINREPOSITORY; // NOI18N
    public final ProfilesQName RELEASES; // NOI18N
    public final ProfilesQName SNAPSHOTS; // NOI18N
    public final ProfilesQName PROFILE; // NOI18N
    public final ProfilesQName ACTIVATION; // NOI18N
    public final ProfilesQName ACTIVATIONPROPERTY; // NOI18N
    public final ProfilesQName ACTIVATIONOS; // NOI18N
    public final ProfilesQName ACTIVATIONFILE; // NOI18N
    public final ProfilesQName ACTIVATIONCUSTOM; // NOI18N

    public final ProfilesQName PROFILES; // NOI18N
    public final ProfilesQName REPOSITORIES; // NOI18N
    public final ProfilesQName PLUGINREPOSITORIES; // NOI18N

    public final ProfilesQName ID; //NOI18N
    public final ProfilesQName CONFIGURATION; //NOI18N
    public final ProfilesQName PROPERTIES; //NOI18N

    public final ProfilesQName URL; //NOI18N
    public final ProfilesQName NAME; //NOI18N
    public final ProfilesQName VALUE; //NOI18N

    public final ProfilesQName LAYOUT; //NOI18N

    public final ProfilesQName ACTIVEPROFILES; //NOI18N
    public final ProfilesQName ACTIVEPROFILE; //NOI18N
    private boolean ns;

    public ProfilesQNames(boolean ns) {
        this.ns = ns;
        PROFILESROOT = new ProfilesQName(ProfilesQName.createQName("profilesXml",ns), ns); // NOI18N
        REPOSITORY = new ProfilesQName(ProfilesQName.createQName("repository",ns), ns); // NOI18N
        PLUGINREPOSITORY = new ProfilesQName(ProfilesQName.createQName("pluginRepository",ns), ns); // NOI18N
        RELEASES = new ProfilesQName(ProfilesQName.createQName("releases",ns), ns); // NOI18N
        SNAPSHOTS = new ProfilesQName(ProfilesQName.createQName("snapshots",ns), ns); // NOI18N
        PROFILE = new ProfilesQName(ProfilesQName.createQName("profile",ns), ns); // NOI18N
        ACTIVATION = new ProfilesQName(ProfilesQName.createQName("activation",ns), ns); // NOI18N
        ACTIVATIONPROPERTY = new ProfilesQName(ProfilesQName.createQName("property",ns), ns); // NOI18N
        ACTIVATIONOS = new ProfilesQName(ProfilesQName.createQName("os",ns), ns); // NOI18N
        ACTIVATIONFILE = new ProfilesQName(ProfilesQName.createQName("file",ns), ns); // NOI18N
        ACTIVATIONCUSTOM = new ProfilesQName(ProfilesQName.createQName("custom",ns), ns); // NOI18N
        PROFILES = new ProfilesQName(ProfilesQName.createQName("profiles",ns), ns); // NOI18N
        REPOSITORIES = new ProfilesQName(ProfilesQName.createQName("repositories",ns), ns); // NOI18N
        PLUGINREPOSITORIES = new ProfilesQName(ProfilesQName.createQName("pluginRepositories",ns), ns); // NOI18N

        ID = new ProfilesQName(ProfilesQName.createQName("id",ns), ns); //NOI18N
        CONFIGURATION = new ProfilesQName(ProfilesQName.createQName("configuration",ns), ns); //NOI18N
        PROPERTIES = new ProfilesQName(ProfilesQName.createQName("properties",ns), ns); //NOI18N
        URL = new ProfilesQName(ProfilesQName.createQName("url",ns), ns); //NOI18N
        NAME = new ProfilesQName(ProfilesQName.createQName("name",ns), ns); //NOI18N

        VALUE = new ProfilesQName(ProfilesQName.createQName("value",ns), ns); //NOI18N

        LAYOUT = new ProfilesQName(ProfilesQName.createQName("layout",ns), ns); //NOI18N

        ACTIVEPROFILE = new ProfilesQName(ProfilesQName.createQName("activeProfile",ns), ns); //NOI18N
        ACTIVEPROFILES = new ProfilesQName(ProfilesQName.createQName("activeProfiles",ns), ns); //NOI18N

        //when adding items here, need to add them to the set below as well.

    }

    public boolean isNSAware() {
        return ns;
    }

    public Set<QName> getElementQNames() {
        QName[] names = new QName[] {
            PROFILESROOT.getQName(),
            REPOSITORY.getQName(),
            PLUGINREPOSITORY.getQName(),
            RELEASES.getQName(),
            SNAPSHOTS.getQName(),
            PROFILE.getQName(),
            ACTIVATION.getQName(),
            ACTIVATIONPROPERTY.getQName(),
            ACTIVATIONOS.getQName(),
            ACTIVATIONFILE.getQName(),
            ACTIVATIONCUSTOM.getQName(),
            PROFILES.getQName(),
            REPOSITORIES.getQName(),
            PLUGINREPOSITORIES.getQName(),
            ID.getQName(),
            CONFIGURATION.getQName(),
            PROPERTIES.getQName(),
            URL.getQName(),
            NAME.getQName(),
            VALUE.getQName(),
            LAYOUT.getQName(),
            ACTIVEPROFILE.getQName(),
            ACTIVEPROFILES.getQName(),
        };
        List<QName> list = Arrays.asList(names);
        return new HashSet<QName>(list);
    }
    
}
