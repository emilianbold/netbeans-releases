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
 * Contributor(s): Daniel Mohni
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.persistence;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider;
import org.netbeans.modules.j2ee.persistence.spi.support.PersistenceScopesHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 * Maven2 Implementation of 
 * <CODE>org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider</CODE>
 * also implements PropertyChangeListener to watch for changes on the persistence.xml file
 * @author Daniel Mohni
 */
public class PersistenceScopesProviderImpl implements PersistenceScopesProvider, PropertyChangeListener {

    private static final RequestProcessor RP = new RequestProcessor(PersistenceScopesProviderImpl.class.getName(), 1, false, false);
    private PersistenceScopesHelper scopesHelper = null;
    private PersistenceScopeProvider scopeProvider = null;
    private Project project = null;
    //need to be uncommented when maven will support ap aprameters better
//    private final PropertyChangeListener scopeListener = new PropertyChangeListener() {
//        @Override
//        public void propertyChange(PropertyChangeEvent evt) {
//            Object newV = evt.getNewValue();
//            if (Boolean.TRUE.equals(newV)) {
//                puChanged();
//            }
//        }
//    };

    /**
     * Creates a new instance of PersistenceScopesProviderImpl
     * @param provider the PersistenceScopeProvider instance to use for lookups
     */
    public PersistenceScopesProviderImpl(PersistenceScopeProvider provider, Project project) {
        scopesHelper = new PersistenceScopesHelper();
        scopeProvider = provider;
        this.project = project;
        checkScope();
    }

    /**
     * property access to the persistence scopes
     * @return the PersistenceScopes instance of the current project
     */
    public PersistenceScopes getPersistenceScopes() {
        return scopesHelper.getPersistenceScopes();
    }

    /**
     * checks and initialise, updates the PersistenceScopeHelper of the 
     * current project 
     */
    private void checkScope() {
        File persistenceXml = null;
        PersistenceScope scope = scopeProvider.findPersistenceScope(null);

        if (scope != null) {
            persistenceXml = FileUtil.toFile(scope.getPersistenceXml());
            scopesHelper.changePersistenceScope(scope, persistenceXml);
//            scopesHelper.getPersistenceScopes().addPropertyChangeListener(scopeListener);
        } else {
            scopesHelper.changePersistenceScope(null, null);
        }
    }

    /**
     * watches for creation and deletion of the persistence.xml file
     * @param evt the change event to process
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (MavenPersistenceProvider.PROP_PERSISTENCE.equals(evt.getPropertyName())) {
            checkScope();
//            puChanged();
        }
    }

    //need to be uncommented when maven will support ap aprameters better
    //it one of possible o future realization of ap arameters support in maven
//    private void puChanged() {
//        ModelHandle handle = null;
//            try {
//                handle = ModelHandleUtils.createModelHandle(project);
//            } catch (IOException ex) {
//                return;
//            } catch (XmlPullParserException ex) {
//                return;
//            }
//        if (handle != null) {
//            POMModel mdl = handle.getPOMModel();
//            Plugin plugin = null;
//            Build bld = mdl.getProject().getBuild();
//            if (bld != null) {
//                plugin = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
//            }
//            if (plugin != null) {
//                Configuration conf = plugin.getConfiguration();
//                if (conf == null) {
//                    conf = mdl.getFactory().createConfiguration();
//                    plugin.setConfiguration(conf);
//                }
//                POMExtensibilityElement args = ModelUtils.getOrCreateChild(conf, "annotationProcessorArguments", mdl);//NOI18N
//                POMExtensibilityElement staticFactArg = ModelUtils.getOrCreateChild(args, "Aeclipselink.canonicalmodel.use_static_factory", mdl);
//                if(staticFactArg.getElementText() == null || staticFactArg.getElementText().length()==0)staticFactArg.setElementText("false");
//                handle.markAsModified(handle.getPOMModel());
//            }
//            try {
//                ModelHandleUtils.writeModelHandle(handle, project);
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//    }
}
