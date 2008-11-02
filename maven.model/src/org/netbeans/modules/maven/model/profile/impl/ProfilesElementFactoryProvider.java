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

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.profile.Activation;
import org.netbeans.modules.maven.model.profile.ActivationCustom;
import org.netbeans.modules.maven.model.profile.ActivationFile;
import org.netbeans.modules.maven.model.profile.ActivationOS;
import org.netbeans.modules.maven.model.profile.ActivationProperty;
import org.netbeans.modules.maven.model.profile.ModelList;
import org.netbeans.modules.maven.model.profile.Profile;
import org.netbeans.modules.maven.model.profile.ProfilesComponent;
import org.netbeans.modules.maven.model.profile.ProfilesExtensibilityElement;
import org.netbeans.modules.maven.model.profile.ProfilesQName;
import org.netbeans.modules.maven.model.profile.ProfilesQNames;
import org.netbeans.modules.maven.model.profile.ProfilesRoot;
import org.netbeans.modules.maven.model.profile.Properties;
import org.netbeans.modules.maven.model.profile.Repository;
import org.netbeans.modules.maven.model.profile.RepositoryPolicy;
import org.netbeans.modules.maven.model.profile.StringList;
import org.netbeans.modules.maven.model.profile.spi.ElementFactory;
import org.netbeans.modules.maven.model.profile.spi.ProfilesExtensibilityElementBase;
import org.netbeans.modules.maven.model.profile.visitor.DefaultVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;


/**
 *
 * @author mkleint
 */
public class ProfilesElementFactoryProvider implements ElementFactory {

    private ProfilesQNames ns = new ProfilesQNames(true);
    private ProfilesQNames nonns = new ProfilesQNames(false);
    private Set<QName> all;


    public ProfilesElementFactoryProvider() {
        all = new HashSet<QName>();
        all.addAll(ns.getElementQNames());
        all.addAll(nonns.getElementQNames());
    }

    public Set<QName> getElementQNames() {
        return all;
    }

    public ProfilesComponent create(ProfilesComponent context, Element element) {
        return new ProfilesComponentCreateVisitor().create(element, context);
    }
}

class ProfilesComponentCreateVisitor extends DefaultVisitor {
    private Element element;
    private ProfilesComponent created;
        
    public ProfilesComponent create(Element element, ProfilesComponent context) {
        this.element = element;
        context.accept(this);
        return created;
    }

    private boolean isElementQName(ProfilesQName q) {
        return areSameQName(q, element);
    }
      
    public static boolean areSameQName(ProfilesQName q, Element e) {
        return q.getQName().equals(AbstractDocumentComponent.getQName(e));
    }
/* 
    private boolean isForeignElement() {
        return !context.getModel().getProfilesQNames().NS_URI.equals(AbstractDocumentComponent.getQName(element).getNamespaceURI());
    }

    private void createExtensibilityElement(SCAComponent context) {
        assert isForeignElement();
        created = new ProfilesExtensibilityElementImpl(context.getModel(), element);
    }
*/
    @Override
    public void visit(ProfilesRoot context) {

        if (isElementQName(context.getModel().getProfilesQNames().PROFILES)) {
            created = new ProfileImpl.List(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getProfilesQNames().REPOSITORIES)) {
            created = new RepositoryImpl.RepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getProfilesQNames().PLUGINREPOSITORIES)) {
            created = new RepositoryImpl.PluginRepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getProfilesQNames().PROPERTIES)) {
            created = new PropertiesImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }


    @Override
    public void visit(Repository context) {
        if (isElementQName(context.getModel().getProfilesQNames().RELEASES)) {
            created = new RepositoryPolicyImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getProfilesQNames().SNAPSHOTS)) {
            created = new RepositoryPolicyImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(RepositoryPolicy context) {
        //createExtensibilityElement(context);
    }


    @Override
    public void visit(Profile context) {
        if (isElementQName(context.getModel().getProfilesQNames().ACTIVATION)) {
            created = new ActivationImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getProfilesQNames().REPOSITORIES)) {
            created = new RepositoryImpl.RepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getProfilesQNames().PLUGINREPOSITORIES)) {
            created = new RepositoryImpl.PluginRepoList(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getProfilesQNames().PROPERTIES)) {
            created = new PropertiesImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }


    @Override
    public void visit(StringList context) {
        created = new ProfilesExtensibilityElementBase(context.getModel(), element);
    }


    @Override
    public void visit(Activation context) {
        if (isElementQName(context.getModel().getProfilesQNames().ACTIVATIONOS)) {
            created = new ActivationOSImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getProfilesQNames().ACTIVATIONPROPERTY)) {
            created = new ActivationPropertyImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getProfilesQNames().ACTIVATIONFILE)) {
            created = new ActivationFileImpl(context.getModel(), element);
            return;
        }

        if (isElementQName(context.getModel().getProfilesQNames().ACTIVATIONCUSTOM)) {
            created = new ActivationCustomImpl(context.getModel(), element);
            return;
        }

        //createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationProperty context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationOS context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationFile context) {
        //createExtensibilityElement(context);
    }

    @Override
    public void visit(ActivationCustom context) {
        //createExtensibilityElement(context);
    }


    @Override
    public void visit(ModelList context) {
        if (isElementQName(context.getModel().getProfilesQNames().PROFILE) && context.getListClass().equals(Profile.class)) {
            created = new ProfileImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getProfilesQNames().REPOSITORY) && context.getListClass().equals(Repository.class)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }
        if (isElementQName(context.getModel().getProfilesQNames().PLUGINREPOSITORY) && context.getListClass().equals(Repository.class)) {
            created = new RepositoryImpl(context.getModel(), element);
            return;
        }
    }


    @Override
    public void visit(Properties context) {
        created = new ProfilesExtensibilityElementBase(context.getModel(), element);
    }


    @Override
    public void visit(ProfilesExtensibilityElement context) {
        created = new ProfilesExtensibilityElementBase(context.getModel(), element);
    }
}
    