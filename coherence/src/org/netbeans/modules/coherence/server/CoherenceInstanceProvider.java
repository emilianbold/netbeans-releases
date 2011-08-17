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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.api.server.properties.InstancePropertiesManager;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class CoherenceInstanceProvider implements ServerInstanceProvider {

    /**
     * Namespace for {@link InstanceProperties} where are {@link CoherenceInstance}s stored.
     */
    public static final String COHERENCE_INSTANCES_NS = "CoherenceInstances"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(CoherenceInstanceProvider.class.getName());
    private static final Map<Integer, ServerInstance> instances = new HashMap<Integer, ServerInstance>();
    private static volatile CoherenceInstanceProvider provider;

    private static volatile boolean initialized = false;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    @Override
    public List<ServerInstance> getInstances() {
        synchronized (instances) {
            if (!initialized) {
                initialized = true;
                init();
            }

            List<ServerInstance> result = new ArrayList<ServerInstance>();
            for (Map.Entry<Integer, ServerInstance> entry : instances.entrySet()) {
                result.add(entry.getValue());
            }

            return result;
        }
    }

    /**
     * Gets {@link ServerInstance} for given instance id.
     *
     * @param instanceId id of the instance
     * @return {@link ServerInstance} for given instance id
     */
    public ServerInstance getInstance(int instanceId) {
        synchronized (instances) {
            Iterator<Entry<Integer, ServerInstance>> iterator = instances.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Integer, ServerInstance> entry = iterator.next();
                if (entry.getKey().intValue() == instanceId) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private static synchronized void init() {
        try {
            loadServerInstances();
        } catch (RuntimeException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private static void loadServerInstances() {
        synchronized (instances) {
            assert Thread.holdsLock(CoherenceInstanceProvider.class);
            List<InstanceProperties> properties = InstancePropertiesManager.getInstance().getProperties(COHERENCE_INSTANCES_NS);
            for (InstanceProperties instanceProperties : properties) {
                CoherenceInstance coherenceInstance = CoherenceInstance.create(instanceProperties);
                instances.put(coherenceInstance.getId(), coherenceInstance.getServerInstance());
            }
        }
    }

    /**
     * Adds {@link CoherenceInstance} into the registered server instances.
     */
    public void addServerInstance(CoherenceInstance coherenceInstance) {
        synchronized (instances) {
            instances.put(coherenceInstance.getId(), coherenceInstance.getServerInstance());
            changeSupport.fireChange();
        }
    }

    /**
     * Removes {@link CoherenceInstance} from registered server instances.
     */
    public void removeServerInstance(CoherenceInstance coherenceInstance) {
        synchronized (instances) {
            instances.remove(coherenceInstance.getId());
            coherenceInstance.getProperties().remove();
            changeSupport.fireChange();
        }
    }

    /**
     * Gets the instance of {@code CoherenceInstanceProvider}.
     *
     * @return {@code CoherenceInstanceProvider} instance
     */
    public static synchronized CoherenceInstanceProvider getCoherenceProvider() {
        if (provider == null) {
            provider = new CoherenceInstanceProvider();
        }
        return provider;
    }

    /**
     * Checks if the given id is unique across all already registered instances.
     *
     * @param id id checked for unique value across instances
     * @return {@code true} if the id is unique; {@code false} otherwise
     */
    public static boolean isUniqueAcrossInstances(Integer id) {
        synchronized (instances) {
            return !instances.containsKey(id);
        }
    }
}
