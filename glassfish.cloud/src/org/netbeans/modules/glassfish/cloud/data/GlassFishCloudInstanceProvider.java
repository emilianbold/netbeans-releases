/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.ChangeSupport;

/**
 * GlassFish Cloud Instances Provider.
 * <p>
 * Handles all registered glassFish cloud instances. Implemented as singleton.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishCloudInstanceProvider
    implements ServerInstanceProvider {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Singleton object instance. */
    private static volatile GlassFishCloudInstanceProvider instance;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Return existing singleton instance of this class or create a new one
     * when no instance exists.
     * <p>
     * @return <code>AdminFactoryHttp</code> singleton instance.
     */
    public static GlassFishCloudInstanceProvider getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (GlassFishCloudInstanceProvider.class) {
            if (instance == null) {
                instance = new GlassFishCloudInstanceProvider();
            }
        }
        return instance;
    }

    /**
     * Add new GlassFish cloud instance into this provider.
     * <p/>
     * @param instance GlassFish cloud instance to be added.
     */
    public static void addCloudInstance(GlassFishCloudInstance instance) {
        getInstance().addInstance(instance);
    }

    /**
     * Remove GlassFish cloud instance from this provider.
     * <p/>
     * @param instance GlassFish cloud instance to be removed.
     */
    public static void removeCloudInstance(GlassFishCloudInstance instance) {
        getInstance().removeInstance(instance);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Stored NEtBeans server instances. */
    private List<ServerInstance> serverInstances;

    /** Stored GlassFish cloud instances. */
    private Map<String, GlassFishCloudInstance> cloudInstances;

    /** Change listeners. */
    private ChangeSupport changeListeners;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    private GlassFishCloudInstanceProvider() {
        changeListeners = new ChangeSupport(this);
        serverInstances = new LinkedList<ServerInstance>();
        cloudInstances = new HashMap<String, GlassFishCloudInstance>();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Interface Methods                                          //
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public List<ServerInstance> getInstances() {
        return serverInstances;
    }

    /**
     * Adds a change listener to this provider.
     * <p/>
     * The listener must be notified any time instance is added or removed.
     * <p/>
     * @param listener Change listener to add, <code>null</code> is allowed 
     *                 (but it si no op then).
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        changeListeners.addChangeListener(listener);
    }

    /**
     * Removes the previously added listener.
     * <p/>
     * No more events will be fired on the removed listener.
     * <p/>
     * @param listener Listener to remove, <code>null</code> is allowed
     *                 (but it si no op then).
     */
    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.removeChangeListener(listener);
    }
   
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Add new GlassFish cloud instance into this provider.
     * <p/>
     * @param instance GlassFish cloud instance to be added.
     */
    public void addInstance(GlassFishCloudInstance instance) {
        serverInstances.add(instance.getServerInstance());
        // TODO: name is not unique key
        cloudInstances.put(instance.getName(), instance);
        changeListeners.fireChange();
    }

    /**
     * Remove GlassFish cloud instance from this provider.
     * <p/>
     * @param instance GlassFish cloud instance to be removed.
     */
    public void removeInstance(GlassFishCloudInstance instance) {
        serverInstances.remove(instance.getServerInstance());
        // TODO: name is not unique key
        cloudInstances.remove(instance.getName());
        changeListeners.fireChange();
    }
}
