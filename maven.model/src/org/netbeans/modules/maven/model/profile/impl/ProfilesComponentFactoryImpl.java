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
import org.netbeans.modules.maven.model.profile.Activation;
import org.netbeans.modules.maven.model.profile.ActivationCustom;
import org.netbeans.modules.maven.model.profile.ActivationFile;
import org.netbeans.modules.maven.model.profile.ActivationOS;
import org.netbeans.modules.maven.model.profile.ActivationProperty;
import org.netbeans.modules.maven.model.profile.Profile;
import org.netbeans.modules.maven.model.profile.ProfilesComponent;
import org.netbeans.modules.maven.model.profile.ProfilesComponentFactory;
import org.netbeans.modules.maven.model.profile.ProfilesExtensibilityElement;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.netbeans.modules.maven.model.profile.ProfilesRoot;
import org.netbeans.modules.maven.model.profile.Properties;
import org.netbeans.modules.maven.model.profile.Repository;
import org.netbeans.modules.maven.model.profile.RepositoryPolicy;
import org.netbeans.modules.maven.model.profile.spi.ElementFactory;
import org.netbeans.modules.maven.model.profile.spi.ProfilesExtensibilityElementBase;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ProfilesComponentFactoryImpl implements ProfilesComponentFactory {
    
    private ProfilesModel model;
    
    /**
     * Creates a new instance of POMComponentFactoryImpl
     */
    public ProfilesComponentFactoryImpl(ProfilesModel model) {
        this.model = model;
    }    
 
    private static QName getQName(Element element, ProfilesComponentImpl context) {
        String namespace = element.getNamespaceURI();
        String prefix = element.getPrefix();
        if (namespace == null && context != null) {
            namespace = context.lookupNamespaceURI(prefix);
        }
        String localName = element.getLocalName();
        assert(localName != null);
        if (namespace == null && prefix == null) {
            return new QName(localName);
        } else if (namespace != null && prefix == null) {
            return new QName(namespace, localName);
        } else {
            return new QName(namespace, localName, prefix);
        }
    }

    public ProfilesComponent create(Element element, ProfilesComponent context) {
        // return new SCAComponentCreateVisitor().create(element, context);
        QName qName = getQName(element, (ProfilesComponentImpl)context);
        ElementFactory elementFactory = ElementFactoryRegistry.getDefault().get(qName);
        return create(elementFactory, element, context);
    }
    
    private ProfilesComponent create(ElementFactory elementFactory, Element element, ProfilesComponent context) {
        if (elementFactory != null ){
            return elementFactory.create(context, element);
        } else {
            return new ProfilesExtensibilityElementBase(model, element);
        }
    }
    
    public ProfilesComponent create(ProfilesComponent context, QName qName) {
       String prefix = qName.getPrefix();
       if (prefix == null || prefix.length() == 0) {
           prefix = qName.getLocalPart();
       } else {
           prefix = prefix + ":" + qName.getLocalPart();
       }
 
       ElementFactory factory = ElementFactoryRegistry.getDefault().get(qName);
       Element element = model.getDocument().createElementNS(qName.getNamespaceURI(), prefix);
       return create(factory, element, context);
    }
    
    public ProfilesRoot createProfilesRoot() {
        return new ProfilesRootImpl(model);
    }


    public Repository createRepository() {
        return new RepositoryImpl(model, false);
    }

    public Repository createPluginRepository() {
        return new RepositoryImpl(model, true);
    }

    public RepositoryPolicy createReleaseRepositoryPolicy() {
        return new RepositoryPolicyImpl(model, model.getProfilesQNames().RELEASES);
    }

    public RepositoryPolicy createSnapshotRepositoryPolicy() {
        return new RepositoryPolicyImpl(model, model.getProfilesQNames().SNAPSHOTS);
    }


    public Profile createProfile() {
        return new ProfileImpl(model);
    }

    public Activation createActivation() {
        return new ActivationImpl(model);
    }

    public ActivationProperty createActivationProperty() {
        return new ActivationPropertyImpl(model);
    }

    public ActivationOS createActivationOS() {
        return new ActivationOSImpl(model);
    }

    public ActivationFile createActivationFile() {
        return new ActivationFileImpl(model);
    }

    public ActivationCustom createActivationCustom() {
        return new ActivationCustomImpl(model);
    }

    public Properties createProperties() {
        return new PropertiesImpl(model);
    }

    public ProfilesExtensibilityElement createProfilesExtensibilityElement(QName name) {
        return new ProfilesExtensibilityElementBase(model, name);
    }


}
