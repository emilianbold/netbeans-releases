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
package org.netbeans.modules.coherence.server;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.modules.coherence.server.ui.CustomizerCommon;
import org.netbeans.modules.coherence.server.ui.CustomizerServerProperties;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;

/**
 * This class represent Coherence Instance. Means virtual instance created for
 * given {@code InstanceProperties}.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class CoherenceInstance implements ServerInstanceImplementation {

    private final ServerInstance serverInstance;
    private final InstanceProperties instanceProperties;
    private CoherenceServerFullNode fullNode;
    private CoherenceServerBaseNode baseNode;

    private CoherenceInstance(InstanceProperties instanceProperties) {
        this.instanceProperties = instanceProperties;
        serverInstance = ServerInstanceFactory.createServerInstance(this);
    }

    /**
     * Gets id for given Coherence instance. This id should be unique across all
     * CoherenceInstances registered in the IDE.
     * @return unique identifier of Coherence instance
     */
    public int getId() {
        int id = instanceProperties.getInt(CoherenceProperties.PROP_COHERENCE_ID, 0);
        assert id != 0;
        return id;
    }

    @Override
    public String getDisplayName() {
        return instanceProperties.getString(CoherenceProperties.PROP_DISPLAY_NAME,
                CoherenceProperties.DISPLAY_NAME_DEFAULT);
    }

    @Override
    public String getServerDisplayName() {
        return CoherenceProperties.DISPLAY_NAME_DEFAULT;
    }

    @Override
    public Node getFullNode() {
        if (fullNode == null) {
            fullNode = new CoherenceServerFullNode(this);
        }
        return fullNode;
    }

    @Override
    public Node getBasicNode() {
        if (baseNode == null) {
            baseNode = new CoherenceServerBaseNode(this);
        }

        assert baseNode != null;
        return baseNode;
    }

    @Override
    public JComponent getCustomizer() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.add(new CustomizerCommon(instanceProperties));
        tabbedPane.add(new CustomizerServerProperties(instanceProperties));
        return tabbedPane;
    }

    @Override
    public void remove() {
        CoherenceInstanceProvider.getCoherenceProvider().removeServerInstance(this);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    /**
     * Loads {@code CoherenceInstance} into memory. Used just for loading instances from
     * files. Make sure that you aren't looking for
     * {@link #createPersistent(org.netbeans.api.server.properties.InstanceProperties)}
     * instead this one.
     * <p>
     * Note: <b>Created instances aren't persisted into preferences!</b>
     *
     * @param properties {@link InstanceProperties} for which will be instance created
     * @return created {@code CoherenceInstance}
     */
    public static CoherenceInstance create(InstanceProperties properties) {
        return new CoherenceInstance(properties);
    }

    /** Creates a new {@code CoherenceInstance}, add it into server instances loaded by
     * {@link CoherenceInstanceProvider} and also store the instance into preferences
     * with the new ID.
     *
     * @param properties {@link InstanceProperties} for which should be new instance created;
     * @return created {@code CoherenceInstance}
     */
    public static CoherenceInstance createPersistent(InstanceProperties properties) {
        // append new ID for the instance
        appendCoherenceID(properties);

        // create new instance
        CoherenceInstance instance = new CoherenceInstance(properties);

        CoherenceInstanceProvider.getCoherenceProvider().addServerInstance(instance);

        return instance;
    }

    /**
     * Appends unique ID to the Coherence properties.
     * @param properties {@link InstanceProperties} where the ID should be appended
     */
    private static void appendCoherenceID(InstanceProperties properties) {
        int uniqueId = properties.hashCode();
        while (!CoherenceInstanceProvider.isUniqueAcrossInstances(uniqueId)) {
            uniqueId++;
        }

        properties.putInt(CoherenceProperties.PROP_COHERENCE_ID, uniqueId);
    }

    /**
     * Gets {@link ServerInstance} for this {@code CoherenceInstance}.
     * @return {@link ServerInstance} for this instance
     */
    public ServerInstance getServerInstance() {
        return serverInstance;
    }

    /**
     * Gets {@link InstanceProperties} for this {@code CoherenceInstance}.
     * @return {@link InstanceProperties} for this instance
     */
    public InstanceProperties getProperties() {
        return instanceProperties;
    }

}

