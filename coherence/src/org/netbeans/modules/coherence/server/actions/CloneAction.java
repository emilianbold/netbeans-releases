/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.server.actions;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.api.server.properties.InstancePropertiesManager;
import org.netbeans.modules.coherence.server.CoherenceInstance;
import org.netbeans.modules.coherence.server.CoherenceInstanceProvider;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.netbeans.modules.coherence.server.CoherenceServer;
import org.netbeans.modules.coherence.server.CoherenceServerProperty;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action allowing clone Coherence server with equivalent settings.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class CloneAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(CloneAction.class.getName());

    @Override
    protected void performAction(Node[] activatedNodes) {
        assert activatedNodes.length == 1;
        Node node = activatedNodes[0];
        CoherenceServer coherenceServer = node.getLookup().lookup(CoherenceServer.class);
        if (coherenceServer == null) {
            LOGGER.log(Level.INFO, "CoherenceServer for node {0} wasn''t found.", node);
            return;
        }

        InstanceProperties oldProperties = coherenceServer.getInstanceProperties();
        InstanceProperties newProperties = InstancePropertiesManager.getInstance().
                createProperties(CoherenceInstanceProvider.COHERENCE_INSTANCES_NS);

        // clone display name with the suffix '- Copy'
        if (!"".equals(oldProperties.getString(CoherenceProperties.PROP_DISPLAY_NAME, ""))) {
            newProperties.putString(CoherenceProperties.PROP_DISPLAY_NAME,
                    oldProperties.getString(CoherenceProperties.PROP_DISPLAY_NAME, "") + " - Copy"); //NOI18N
        }
        // clone all base properties except instance ID
        copyStringProperty(newProperties, oldProperties, CoherenceProperties.PROP_COHERENCE_CLASSPATH); //NOI18N
        copyStringProperty(newProperties, oldProperties, CoherenceProperties.PROP_JAVA_FLAGS); //NOI18N
        copyStringProperty(newProperties, oldProperties, CoherenceProperties.PROP_CUSTOM_PROPERTIES); //NOI18N
        // clone all Coherence server properties
        for (CoherenceServerProperty property : CoherenceProperties.SERVER_PROPERTIES) {
            copyProperty(newProperties, oldProperties, property);
        }

        CoherenceInstance.createPersistent(newProperties);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        // Clone is possible just for once server at the moment;
        if (activatedNodes != null && activatedNodes.length == 1) {
            for (Node node : activatedNodes) {
                CoherenceServer coherenceServer = node.getLookup().lookup(CoherenceServer.class);
                if (coherenceServer != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(CloneAction.class, "ACTION_ServerClone"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Copies property defined by {@code String} key from one properties to another one.
     *
     * @param newProperties target properties where will be property copied
     * @param oldProperties source properties
     * @param key {@code String} key of the property
     */
    private static void copyStringProperty(InstanceProperties newProperties, InstanceProperties oldProperties, String key) {
        if (!"".equals(oldProperties.getString(key, ""))) {
            LOGGER.log(Level.FINE, "Copying String property for key {0}.", key); //NOI18N
            newProperties.putString(key, oldProperties.getString(key, ""));
        }
    }

    /**
     * Copies standard {@link CoherenceServerProperty} from one properties to another one.
     *
     * @param newProperties target properties where will be property copied
     * @param oldProperties source properties
     * @param property {@link CoherenceServerProperty} for copying
     */
    private static void copyProperty(InstanceProperties newProperties, InstanceProperties oldProperties, CoherenceServerProperty property) {
        Class type = property.getClazz();
        String key = property.getPropertyName();

        if (type == String.class) {
            if (!"".equals(oldProperties.getString(key, ""))) {
                LOGGER.log(Level.FINE, "Copying server property (String) for key {0}.", key); //NOI18N
                newProperties.putString(key, oldProperties.getString(key, ""));
            }
        } else if (type == Integer.class) {
            if (oldProperties.getInt(key, 0) != 0) {
                LOGGER.log(Level.FINE, "Copying server property (Integer) for key {0}.", key); //NOI18N
                newProperties.putInt(key, oldProperties.getInt(key, 0));
            }
        } else if (type == Long.class) {
            if (oldProperties.getLong(key, 0) != 0) {
                LOGGER.log(Level.FINE, "Copying server property (Long) for key {0}.", key); //NOI18N
                newProperties.putLong(key, oldProperties.getLong(key, 0));
            }
        } else if (type == Boolean.class) {
            boolean defaultValue = Boolean.valueOf(property.getDefaultValue());
            if (oldProperties.getBoolean(key, defaultValue) != defaultValue) {
                LOGGER.log(Level.FINE, "Copying server property (Boolean) for key {0}.", key); //NOI18N
                newProperties.putBoolean(key, oldProperties.getBoolean(key, defaultValue));
            }
        } else {
            throw new UnsupportedOperationException("Copy properties for type " + type + "is not supported yet."); //NOI18N
        }
    }
}
