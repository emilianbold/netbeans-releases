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
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.api;

import static org.netbeans.modules.hudson.constants.HudsonInstanceConstants.*;
import org.netbeans.modules.hudson.api.HudsonInstance.Persistence;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.impl.HudsonInstanceProperties;
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.netbeans.modules.hudson.spi.BuilderConnector;

/**
 * Manages the list of Hudson instances.
 */
public class HudsonManager {

    private HudsonManager() {}

    /**
     * Adds a Hudson instance to the system (if not already registered).
     * @param name a name by which the instance will be identified (e.g. {@code Deadlock})
     * @param url the master URL (e.g. {@code http://deadlock.netbeans.org/hudson/})
     * @param sync interval (in minutes) between refreshes, or 0 to disable
     * @param persistent if true, persist this configuration; if false, will be transient
     * @return a new or existing instance
     */
    public static HudsonInstance addInstance(String name, String url, int sync,
            boolean persistent) {
        return addInstance(name, url, sync, Persistence.instance(persistent));
    }

    /**
     * Adds a Hudson instance to the system (if not already registered).
     *
     * @param name a name by which the instance will be identified (e.g.
     * {@code Deadlock})
     * @param url the master URL (e.g.
     * {@code http://deadlock.netbeans.org/hudson/})
     * @param sync interval (in minutes) between refreshes, or 0 to disable
     * @param persistence persistence settings for the new instance
     */
    public static HudsonInstance addInstance(String name, String url, int sync,
            final Persistence persistence) {
        for (HudsonInstance existing : HudsonManagerImpl.getDefault().getInstances()) {
            if (existing.getUrl().equals(url)) {
                return existing;
            }
        }
        HudsonInstanceProperties props = new HudsonInstanceProperties(name, url, Integer.toString(sync));
        props.put(INSTANCE_PERSISTED, persistence.isPersistent() ? TRUE : FALSE);
        HudsonInstanceImpl nue = HudsonInstanceImpl.createHudsonInstance(props, true);       
        HudsonManagerImpl.getDefault().addInstance(nue);
        return nue;
    }

    /**
     * Add a temporary instance with a custom {@link BuilderConnector}. If an
     * instance with the same url is already registered, its connector will be
     * replaced.
     *
     * @param name a name by which the instance will be identified (e.g.
     * {@code Deadlock})
     * @param url the master URL (e.g.
     * {@code http://deadlock.netbeans.org/hudson/})
     * @param sync interval (in minutes) between refreshes, or 0 to disable
     * @param builderConnector Connector for retrieving builder data.
     *
     * @since 1.22
     *
     * @return A new or existing connector.
     */
    public static HudsonInstance addInstance(String name, String url, int sync,
            BuilderConnector builderConnector) {
        HudsonInstanceImpl hi = HudsonManagerImpl.getDefault().getInstance(url);
        if (hi != null) {
            hi.changeBuilderConnector(builderConnector);
            return hi;
        } else {
            HudsonInstanceImpl nue = HudsonInstanceImpl.createHudsonInstance(
                    name, url, builderConnector, sync);
            HudsonManagerImpl.getDefault().addInstance(nue);
            return nue;
        }
    }

    /**
     * Remove a Hudson instance from Hudson Builders node.
     */
    public static void removeInstance(HudsonInstance instance) {
        if (instance instanceof HudsonInstanceImpl) {
            HudsonManagerImpl.getDefault().removeInstance(
                    (HudsonInstanceImpl) instance);
        }
    }
}
