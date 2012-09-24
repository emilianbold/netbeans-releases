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
package org.netbeans.modules.ods.ui.settings;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import org.netbeans.modules.ods.ui.project.ProjectDetailsTopComponent;
import org.openide.util.NbPreferences;

/**
 *
 * @author jpeska
 */
public class OdsSettings {

    public static final String AUTO_SYNC_SETTINGS_CHANGED = "ods.auto_sync_changed"; //NOI18N

    private static OdsSettings instance = null;
    private static final String AUTO_SYNC = "ods.auto_sync"; //NOI18N
    private static final String AUTO_SYNC_VALUE = "ods.auto_sync_value"; //NOI18N
    /*
     * default values in fields
     */
    private static final boolean DEFAULT_AUTO_SYNC = true;
    private static final int DEFAULT_AUTO_SYNC_VALUE = 15;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    private OdsSettings() {
    }

    public static OdsSettings getInstance(){
        if (instance == null) {
            instance = new OdsSettings();
        }
        return instance;
    }

    public void addPropertyChangedListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangedListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public boolean isAutoSync() {
        return getPreferences().getBoolean(AUTO_SYNC, DEFAULT_AUTO_SYNC);
    }

    public void setAutoSync(boolean autoSync, boolean fireEvent) {
        getPreferences().putBoolean(AUTO_SYNC, autoSync);
        if (fireEvent) {
            fireSyncChangedEvent();
        }
    }

    public int getAutoSyncValue() {
        return getPreferences().getInt(AUTO_SYNC_VALUE, DEFAULT_AUTO_SYNC_VALUE);
    }

    public void setAutoSyncValue(int autoSyncValue, boolean fireEvent) {
        getPreferences().putInt(AUTO_SYNC_VALUE, autoSyncValue);
        if (fireEvent) {
            fireSyncChangedEvent();
        }
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(OdsSettings.class);
    }

    private void fireSyncChangedEvent() {
        support.firePropertyChange(AUTO_SYNC_SETTINGS_CHANGED, null, null);
    }
}
