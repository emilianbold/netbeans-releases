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

package org.netbeans.modules.hudson.impl;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonView;
import static org.netbeans.modules.hudson.constants.HudsonJobConstants.*;
import org.netbeans.modules.hudson.ui.interfaces.OpenableInBrowser;
import org.netbeans.modules.hudson.util.HudsonPropertiesSupport;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Implementation of the HudsonJob
 *
 * @author pblaha
 */
public class HudsonJobImpl implements HudsonJob, OpenableInBrowser {
    
    private HudsonPropertiesSupport properties = new HudsonPropertiesSupport();
    
    private Collection<HudsonView> views = new ArrayList<HudsonView>();
    
    private HudsonInstanceImpl instance;
    
    private Sheet.Set set;
    
    /**
     * Creates a new instance of Job
     *
     * @param name
     * @param url
     * @param color
     */
    public HudsonJobImpl(HudsonInstanceImpl instance) {
        this.instance = instance;
    }
    
    public void putProperty(String name, Object o) {
        properties.putProperty(name, o);
    }
    
    public String getDisplayName() {
        return properties.getProperty(JOB_DISPLAY_NAME, String.class);
    }
    
    public String getName() {
        return properties.getProperty(JOB_NAME, String.class);
    }
    
    public String getDescription() {
        
        return properties.getProperty(JOB_DESCRIPTION, String.class);
        
    }
    
    public String getUrl() {
        return properties.getProperty(JOB_URL, String.class);
    }
    
    public Color getColor() {
        return properties.getProperty(JOB_COLOR, Color.class);
    }
    
    public boolean isInQueue() {
        return properties.getProperty(JOB_IN_QUEUE, Boolean.class);
    }
    
    public boolean isBuildable() {
        return properties.getProperty(JOB_BUILDABLE, Boolean.class);
    }
    
    public int getLastBuild() {
        try {
            return properties.getProperty(JOB_LAST_BUILD, Integer.class);
        } catch (NullPointerException e) {
            return 0;
        }
    }
    
    public int getLastStableBuild() {
        try {
            return properties.getProperty(JOB_LAST_STABLE_BUILD, Integer.class);
        } catch (NullPointerException e) {
            return 0;
        }
    }
    
    public int getLastSuccessfulBuild() {
        try {
            return properties.getProperty(JOB_LAST_SUCCESSFUL_BUILD, Integer.class);
        } catch (NullPointerException e) {
            return 0;
        }
    }
    
    public int getLastFailedBuild() {
        try {
            return properties.getProperty(JOB_LAST_FAILED_BUILD, Integer.class);
        } catch (NullPointerException e) {
            return 0;
        }
    }
    
    public synchronized Collection<HudsonView> getViews() {
        return views;
    }
    
    public synchronized void addView(HudsonView view) {
        views.add(view);
    }
    
    public void start() {
        // Start job
        instance.getConnector().startJob(this);
        
        // Synchronize jobs
        instance.synchronize();
    }
    
    public Lookup getLookup() {
        return Lookups.singleton(instance);
    }
    
    public Sheet.Set getSheetSet() {
        if (null == set) {
            set = Sheet.createPropertiesSet();
            
            // Set display name
            set.setDisplayName(getDisplayName());
            
            // Put properties in
            set.put(new PropertySupport[] {
                new HudsonJobProperty(JOB_NAME,
                        NbBundle.getMessage(HudsonJobImpl.class, "TXT_Job_Prop_Name"),
                        NbBundle.getMessage(HudsonJobImpl.class, "DESC_Job_Prop_Name"),
                        true, false),
                        new HudsonJobProperty(JOB_URL,
                        NbBundle.getMessage(HudsonJobImpl.class, "TXT_Job_Prop_Url"),
                        NbBundle.getMessage(HudsonJobImpl.class, "DESC_Job_Prop_Url"),
                        true, false)
            });
        }
        
        return set;
    }

    /**
     * Obtains a filesystem representing the remote workspace as accessed by Hudson web services.
     * Requires Hudson 1.264 or later.
     */
    public FileSystem getRemoteWorkspace() {
        return instance.getRemoteWorkspace(this);
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof HudsonJobImpl))
            return false;
        
        final HudsonJobImpl j = (HudsonJobImpl) o;
        
        if (getDisplayName() != j.getDisplayName() &&
                (getDisplayName() == null || !getDisplayName().equals(j.getDisplayName())))
            return false;
        if (getName() != j.getName() &&
                (getName() == null || !getName().equals(j.getName())))
            return false;
        if (getUrl() != j.getUrl() &&
                (getUrl() == null || !getUrl().equals(j.getUrl())))
            return false;
        if (getColor() != j.getColor() &&
                (getColor() == null || !getColor().equals(j.getColor())))
            return false;
        if (isInQueue() != j.isInQueue())
            return false;
        if (isBuildable() != j.isBuildable())
            return false;
        if (this.getViews() != j.getViews() &&
                (this.getViews() == null || !this.getViews().equals(j.getViews())))
            return false;
        
        return true;
    }
    
    public int compareTo(HudsonJob o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }

    @Override
    public String toString() {
        return getUrl();
    }
    
    private class HudsonJobProperty extends PropertySupport<String> {
        
        private String key;
        
        public HudsonJobProperty(String key, String name, String desc, boolean read, boolean write) {
            super(key, String.class, name, desc, read, write);
            
            this.key = key;
        }
        
        @Override
        public void setValue(String value) {
            putProperty(key, value);
        }
        
        @Override
        public String getValue() {
            return properties.getProperty(key, String.class);
        }
    }
}