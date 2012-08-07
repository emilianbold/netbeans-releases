/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.hudson.api;

import java.util.Collection;
import java.util.prefs.Preferences;
import org.openide.awt.ActionReference;

/**
 * Instance of the the Hudson Server
 *
 * @author Michal Mocnak
 */
public interface HudsonInstance extends Comparable<HudsonInstance> {

    /**
     * Path used to load actions for the server instance.
     * A {@code HudsonInstance} object should be in the context lookup.
     * May be used e.g. for the context menu of an instance node.
     * @see ActionReference#path
     * @since 1.12
     */
    String ACTION_PATH = "org-netbeans-modules-hudson/Actions/instance";
    
    /**
     * Name of the Hudson instance
     *
     * @return instance name
     */
    public String getName();
    
    /**
     * Returns version of the hudson instance
     *
     * @return hudson version
     */
    public HudsonVersion getVersion();
    
    /**
     * Returns state of the hudson instance
     * 
     * @return true if the instance is connected
     */
    public boolean isConnected();
    
    /**
     * URL of the Hudson instance
     *
     * @return instance url
     */
    public String getUrl();
    
    /**
     * Returns all Hudson jobs from registered instance
     *
     * @return collection of all jobs
     */
    public Collection<HudsonJob> getJobs();

    /**
     * Returns all Hudson views from registered instance
     *
     * @return collection of all views
     */
    public Collection<HudsonView> getViews();
    
    HudsonView getPrimaryView();
    
    /**
     * Register HudsonChangeListener
     *
     * @param l HudsonChangeListener
     */
    public void addHudsonChangeListener(HudsonChangeListener l);
    
    /**
     * Unregister HudsonChangeListener
     *
     * @param l HudsonChangeListener
     */
    public void removeHudsonChangeListener(HudsonChangeListener l);

    /**
     * Checks whether this instance's configuration is persisted to disk.
     */
    boolean isPersisted();

    /**
     * Per-instance preferences.
     * @return preferences for various customizations
     */
    Preferences prefs();

    /**
     * Class holding info about Hudson instance persistence.
     *
     * @author jhavlin
     */
    public static final class Persistence {

        private static final Persistence TRANSIENT_INSTANCE =
                new Persistence(false);
        private static final Persistence PERSISTENT_INSTANCE =
                new Persistence(true);
        private boolean isPersistent;
        private String info;

        /**
         * Constructor for persistence settings that use default info message.
         */
        private Persistence(boolean isPersistent) {
            this.isPersistent = isPersistent;
            this.info = null;
        }

        /**
         * Constructor for persistence settings that use custom info message.
         */
        public Persistence(boolean isPersistent, String info) {
            this.isPersistent = isPersistent;
            this.info = (info == null ? "" : info);                     //NOI18N
        }

        /**
         * Settings for persistent instances, with default info message.
         */
        public static Persistence persistent() {
            return PERSISTENT_INSTANCE;
        }

        /**
         * Settings for transient instances.
         */
        public static Persistence tranzient() {
            return TRANSIENT_INSTANCE;
        }

        /**
         * Transient instance with a custom info message.
         */
        public static Persistence tranzient(String info) {
            return new Persistence(false, info);
        }

        /**
         * Instance with default info message.
         *
         * @param persistent True for persistent instance, false for transient
         * instance.
         */
        public static Persistence instance(boolean persistent) {
            if (persistent) {
                return PERSISTENT_INSTANCE;
            } else {
                return TRANSIENT_INSTANCE;
            }
        }

        /**
         * Get info message, or specified default message if no custom message
         * was set.
         */
        public String getInfo(String defaultInfo) {
            if (info == null) {
                return defaultInfo;
            } else {
                return info;
            }
        }

        /**
         * Return true if the instance is persistent, false otherwise.
         */
        public boolean isPersistent() {
            return isPersistent;
        }
    }
}
