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

import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.profile.ProfilesComponent;
import org.netbeans.modules.maven.model.profile.ProfilesComponentFactory;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.netbeans.modules.maven.model.profile.ProfilesQName;
import org.netbeans.modules.maven.model.profile.ProfilesQNames;
import org.netbeans.modules.maven.model.profile.ProfilesRoot;
import org.netbeans.modules.maven.model.profile.visitor.ChildComponentUpdateVisitor;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ProfilesModelImpl extends ProfilesModel {
    
    private ProfilesComponent rootComponent;
    private ProfilesComponentFactory componentFactory;
    private ProfilesQNames profilesQNames;
    private QName ROOT_NS = ProfilesQName.createQName("profilesXml", true); ///NOI18N
    private QName ROOT = ProfilesQName.createQName("profilesXml", false); ///NOI18N
    
    public ProfilesModelImpl(ModelSource source) {
        super(source);
        componentFactory = new ProfilesComponentFactoryImpl(this);
    }
    
    public ProfilesComponent getRootComponent() {
        return rootComponent;
    }

    public ProfilesComponentFactory getFactory() {
        return componentFactory;
    }

    public ProfilesRoot getProfilesRoot() {
        return (ProfilesRoot) getRootComponent();
    }

    public ProfilesComponent createRootComponent(Element root) {
        QName q = root == null ? null : AbstractDocumentComponent.getQName(root);
        if (root != null ) {
            if (ROOT.equals(q)) {
                profilesQNames = new ProfilesQNames(false);
                rootComponent = new ProfilesRootImpl(this, root);
            } else if (ROOT_NS.equals(q)) {
                profilesQNames = new ProfilesQNames(true);
                rootComponent = new ProfilesRootImpl(this, root);
            }
        } 
        
        return getRootComponent();
    }

    protected ComponentUpdater<ProfilesComponent> getComponentUpdater() {
        return new ChildComponentUpdateVisitor<ProfilesComponent>();
    }

    public ProfilesComponent createComponent(ProfilesComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    @Override
    public ProfilesQNames getProfilesQNames() {
        return profilesQNames;
    }


}
