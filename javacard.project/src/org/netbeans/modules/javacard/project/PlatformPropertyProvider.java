/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javacard.JCUtil;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.modules.propdos.PropertiesAdapter;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbCollections;
import org.openide.util.WeakListeners;

class PlatformPropertyProvider implements PropertyProvider, PropertyChangeListener, Runnable {

    protected final AntProjectHelper antHelper;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    PlatformPropertyProvider(AntProjectHelper antHelper) {
        super();
        this.antHelper = antHelper;
    }
    private final ChangeSupport supp = new ChangeSupport(this);

    public final Map<String, String> getProperties() {
        PropertiesAdapter adap = findAdapter();
        if (adap != null) {
            ObservableProperties props = adap.asProperties();
            props.addPropertyChangeListener(WeakListeners.propertyChange(this, props));
            return NbCollections.checkedMapByFilter(props, String.class, String.class, false);
        }
        return Collections.<String, String>emptyMap();
    }

    protected PropertiesAdapter findAdapter() {
        PropertyProvider projectProps = antHelper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String platformName = projectProps.getProperties().get(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        platformName = platformName == null ? JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME : platformName;
        DataObject dob = JCUtil.findPlatformDataObjectNamed(platformName);
        if (dob != null) {
            PropertiesAdapter adap = dob.getLookup().lookup(PropertiesAdapter.class);
            return adap;
        }
        return null;
    }

    final void fire() {
        ProjectManager.mutex().readAccess(this);
    }

    public final void addChangeListener(ChangeListener arg0) {
        supp.addChangeListener(arg0);
    }

    public final void removeChangeListener(ChangeListener arg0) {
        supp.removeChangeListener(arg0);
    }

    public final void propertyChange(PropertyChangeEvent evt) {
        pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        fire();
    }

    public void run() {
        supp.fireChange();
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
}
