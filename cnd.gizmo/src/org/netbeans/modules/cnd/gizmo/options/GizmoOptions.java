/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.gizmo.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.openide.util.NbBundle;

public class GizmoOptions implements ConfigurationAuxObject {
    public static final String PROFILE_ID = "gizmo_options"; // NOI18N

    private PropertyChangeSupport pcs = null;
    private boolean needSave = false;
    private String baseDir;

    // Profile on Run
    private BooleanConfiguration profileOnRun;
    // Cpu
    private BooleanConfiguration cpu;
    // Memory
    private BooleanConfiguration memory;
    // Synchronization
    private BooleanConfiguration synchronization;
    // Data Provider
    public static final int SUNSTUDIO = 0;
    public static final int DTRACE = 1;
    private static final String[] DATA_PROVIDER_NAMES = {
	  getString("SunStudio"),
	  getString("DTrace"),
    };
    private IntConfiguration dataProvider;
    
    public GizmoOptions(String baseDir, PropertyChangeSupport pcs) {
        this.baseDir = baseDir;
        this.pcs = pcs;

        profileOnRun = new BooleanConfiguration(null, true, null, null);
        cpu = new BooleanConfiguration(null, true, null, null);
        memory = new BooleanConfiguration(null, true, null, null);
        synchronization = new BooleanConfiguration(null, true, null, null);
        dataProvider = new IntConfiguration(null, DTRACE, DATA_PROVIDER_NAMES, null);
    }

    public void initialize() {
        clearChanged();
    }

    public boolean isModified() {
        return getProfileOnRun().getModified();
    }

    public String getId() {
        return PROFILE_ID;
    }


    /**
     * @return the profileOnRun
     */
    public BooleanConfiguration getProfileOnRun() {
        return profileOnRun;
    }

    /**
     * @param profileOnRun the profileOnRun to set
     */
    public void setProfileOnRun(BooleanConfiguration profileOnRun) {
        this.profileOnRun = profileOnRun;
    }

    /**
     * @return the cpu
     */
    public BooleanConfiguration getCpu() {
        return cpu;
    }

    /**
     * @param cpu the cpu to set
     */
    public void setCpu(BooleanConfiguration cpu) {
        this.cpu = cpu;
    }

    /**
     * @return the memory
     */
    public BooleanConfiguration getMemory() {
        return memory;
    }

    /**
     * @param memory the memory to set
     */
    public void setMemory(BooleanConfiguration memory) {
        this.memory = memory;
    }

    /**
     * @return the synchronization
     */
    public BooleanConfiguration getSynchronization() {
        return synchronization;
    }

    /**
     * @param synchronization the synchronization to set
     */
    public void setSynchronization(BooleanConfiguration synchronization) {
        this.synchronization = synchronization;
    }

    /**
     * @return the dataProvider
     */
    public IntConfiguration getDataProvider() {
        return dataProvider;
    }

    /**
     * @param dataProvider the dataProvider to set
     */
    public void setDataProvider(IntConfiguration dataProvider) {
        this.dataProvider = dataProvider;
    }

    /**
     *  Adds property change listener.
     *  @param l new listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.addPropertyChangeListener(l);
        }
    }
    
    /**
     *  Removes property change listener.
     *  @param l removed listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(l);
        }
    }

    public boolean shared() {
        return false;
    }

    public XMLDecoder getXMLDecoder() {
        return new GizmoOptionsXMLCodec(this);
    }
    
    public XMLEncoder getXMLEncoder() {
        return new GizmoOptionsXMLCodec(this);
    }
    
    // interface ProfileAuxObject
    public boolean hasChanged() {
        return needSave;
    }
    
    // interface ProfileAuxObject
    public void clearChanged() {
        needSave = false;
    }
    
    public void assign(ConfigurationAuxObject auxObject) {
        GizmoOptions gizmoOptions = (GizmoOptions)auxObject;
        
        getProfileOnRun().assign(gizmoOptions.getProfileOnRun());
        getCpu().assign(gizmoOptions.getCpu());
        getMemory().assign(gizmoOptions.getMemory());
        getSynchronization().assign(gizmoOptions.getSynchronization());
        getDataProvider().assign(gizmoOptions.getDataProvider());
    }

    
    @Override
    public GizmoOptions clone() {
        GizmoOptions clone = new GizmoOptions(getBaseDir(), null);

        clone.setProfileOnRun(getProfileOnRun().clone());
        clone.setCpu(getCpu().clone());
        clone.setMemory(getMemory().clone());
        clone.setSynchronization(getSynchronization().clone());
        clone.setDataProvider(getDataProvider().clone());
        return clone;
    }
    
    public String getBaseDir() {
        return baseDir;
    }

    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    protected static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(GizmoOptions.class);
        }
        return bundle.getString(s);
    }
}
