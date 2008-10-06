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

package org.netbeans.modules.maven.embedder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.extension.DefaultExtensionManager;
import org.apache.maven.extension.ExtensionManagerException;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

/**
 * Makes sure the extensions are downloaded even in project loading embedder.
 * That's necessary for proper loading of project in case the extension holds
 * artifacthandler definitions..
 * 
 * @author mkleint
 */
public class NbExtensionManager extends DefaultExtensionManager {

    protected Field wagonMan;

    private Logger LOG = Logger.getLogger(NbExtensionManager.class.getName());

    /** Creates a new instance of NbExtensionManager */
    public NbExtensionManager() {
        super();
        try {
            wagonMan = DefaultExtensionManager.class.getDeclaredField("wagonManager");
            wagonMan.setAccessible(true);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void addExtension(Extension extension, Model originatingModel, List remoteRepositories, MavenExecutionRequest request) throws ExtensionManagerException {
        String key = extension.getGroupId() + ":" + extension.getArtifactId();
        openSesame(key);
        try {
            LOG.fine("add extension1=" + extension.getGroupId() + ":" + extension.getArtifactId() + ":" + extension.getVersion()); //NOI18N
            super.addExtension(extension, originatingModel, remoteRepositories, request);
        } finally {
            LOG.fine("---------------------------------------------------------");
            closeSesame(key);
        }
    }

    @Override
    public void addExtension(Extension extension, MavenProject project, MavenExecutionRequest request) throws ExtensionManagerException {
        String key = extension.getGroupId() + ":" + extension.getArtifactId();
        openSesame(key);
        try {
            LOG.fine("add extension2=" + extension.getGroupId() + ":" + extension.getArtifactId() + ":" + extension.getVersion());
            super.addExtension(extension, project, request);
        } finally {
            LOG.fine("---------------------------------------------------------");
            closeSesame(key);
        }
    }

    @Override
    public void addPluginAsExtension(Plugin plugin, Model originatingModel, List remoteRepositories, MavenExecutionRequest request) throws ExtensionManagerException {
        String key = plugin.getGroupId() + ":" + plugin.getArtifactId();
        openSesame(key);
        try {
            LOG.fine("add plugin as ext=" + plugin.getGroupId() + ":" + plugin.getArtifactId() + ":" + plugin.getVersion());
            super.addPluginAsExtension(plugin, originatingModel, remoteRepositories, request);
        } finally {
            LOG.fine("---------------------------------------------------------");
            closeSesame(key);
        }
    }

    @Override
    public void registerWagons() {
        try {
            LOG.fine("register wagons...");
            super.registerWagons();
        } finally {
            LOG.fine("---------------------------------------------------------");
        }
    }

    private void closeSesame(String str) {
        if (wagonMan != null) {
            try {
                Object manObj = wagonMan.get(this);
                if (manObj instanceof NbWagonManager) {
                    NbWagonManager manager = (NbWagonManager)manObj;
                    manager.cleanLetGone(str);
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void openSesame(String str) {
        if (wagonMan != null) {
            try {
                Object manObj = wagonMan.get(this);
                if (manObj instanceof NbWagonManager) {
                    NbWagonManager manager = (NbWagonManager)manObj;
                    manager.letGoThrough(str);
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }
    
}
