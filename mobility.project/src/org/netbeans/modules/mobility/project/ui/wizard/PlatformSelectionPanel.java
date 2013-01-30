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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * PlatformSelectionPanel.java
 *
 * Created on 28. duben 2004, 15:52
 */
package org.netbeans.modules.mobility.project.ui.wizard;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.modules.SpecificationVersion;
import org.openide.util.HelpCtx;

/**
 *
 * @author  Adam Sotona, Petr Somol
 */
public class PlatformSelectionPanel implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    public static final String REQUIRED_CONFIGURATION = "RequiredConfiguration"; // NOI18N
    public static final String REQUIRED_PROFILE ="RequiredProfile"; // NOI18N
    
    public static final String IMPNG_PROFILE_NAME = "IMP-NG"; // NOI18N
    public static final String IMPNG_PROFILE_VERSION = "1.0"; // NOI18N

    public static final String PLATFORM_DESCRIPTION = "PlatformDescription"; //NOI18N

    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    private boolean isValid;
    
    public static class PlatformDescription {
        public J2MEPlatform platform;
        public J2MEPlatform.Device device;
        public String configuration;
        public String profile;
    }
    
    private PlatformSelectionPanelGUI gui;
    private boolean embedded;
    private String reqCfg, reqProf;
    private boolean first = true;
        
    @Override
    public boolean isFinishPanel() {
        return true;
    }
    
    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    @Override
    public synchronized Component getComponent() {
        if (gui == null) {
            gui = new PlatformSelectionPanelGUI();
            gui.addChangeListener(this);
        }
        return gui;
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(PlatformSelectionPanel.class.getName() + (embedded ? "Embedded" : "") ); // NOI18N
    }
    
    @Override
    public boolean isValid() {
        return isValid;
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        change();
    }
    
    private void change() {
        boolean isSelected = gui.getFinishable();
        if (isSelected) {
            setValid(true);
        } else {
            setValid(false);
        }
    }

    private void setValid(boolean val) {
        if (isValid != val) {
            isValid = val;
            fireChangeEvent();  // must do this to enable next/finish button
        }
    }

    @Override
    public void readSettings(final Object settings) {
        if (first) {
            first = false;
            getComponent();
            final TemplateWizard wiz = (TemplateWizard)settings;
            reqCfg = (String)wiz.getProperty(REQUIRED_CONFIGURATION);
            reqProf = (String)wiz.getProperty(REQUIRED_PROFILE);
            embedded = wiz.getProperty(Utils.IS_EMBEDDED) == null ? false : (Boolean)wiz.getProperty(Utils.IS_EMBEDDED);
            final ArrayList<Profile> l = new ArrayList<Profile>();
            final Profile cfg = parseProfile(reqCfg);
            if (cfg != null) l.add(cfg);
            final Profile prof = parseProfile(reqProf);
            if (prof != null) l.add(prof);
            final J2MEPlatform p = findTheBestPlatform(l.toArray(new Profile[l.size()]));
            assert p != null;
            gui.setValues(wiz, p, findDevice(p, l),  reqCfg, reqProf, embedded);
        } else {
            gui.updateErrorMessage();
        }
    }
    
    private static Profile parseProfile(final String profile) {
        if (profile != null) try {
            if(IMPNG_PROFILE_NAME.equals(profile)) {
                return new Profile(IMPNG_PROFILE_NAME, new SpecificationVersion(IMPNG_PROFILE_VERSION));
            }
            final int j = profile.lastIndexOf('-'); //NOI18N
            return j > 0 ? new Profile(profile.substring(0, j).trim(), new SpecificationVersion(profile.substring(j+1).trim())) : new Profile(profile.trim(), null);
        } catch (NumberFormatException nfe) {
            //HO HO HO ERROR IN PARSING !
        }
        return null;
    }
    
    private static boolean compareProfiles(final Profile p, final Profile req) {
        if (!p.getName().equalsIgnoreCase(req.getName())) return false;
        return req.getVersion() == null || (p.getVersion() != null && p.getVersion().compareTo(req.getVersion()) >= 0);
    }
    
    private static int ratePlatform(final J2MEPlatform platform, final Profile profiles[]) {
        int rating = 0;
        if (platform.getName().startsWith("J2ME_Wireless_Toolkit")) rating++; //NOI18N
        if (platform.getName().startsWith("J2ME_Wireless_Toolkit_2")) rating++; //NOI18N
        if (profiles == null) return rating;
        final Profile pp[] = platform.getSpecification().getProfiles();
        for (int j=0; j<profiles.length; j++) {
            for (int i=0; i<pp.length; i++) {
                if (compareProfiles(pp[i], profiles[j])) {
                    rating += 3;
                    i = pp.length;
                }
            }
        }
        return rating;
    }
    
    private static J2MEPlatform findTheBestPlatform(final Profile profiles[]) {
        final JavaPlatform plat[] = JavaPlatformManager.getDefault().getPlatforms(null,  new Specification(J2MEPlatform.SPECIFICATION_NAME, null, null)); //NOI18N
        J2MEPlatform best = null;
        int bestRating = -1;
        for (int i=0; i<plat.length; i++) {
            if (plat[i] instanceof J2MEPlatform) {
                final int rating = ratePlatform((J2MEPlatform)plat[i], profiles);
                if (rating > bestRating) {
                    bestRating = rating;
                    best = (J2MEPlatform)plat[i];
                }
            }
        }
        return best;
    }
    
    
    private J2MEPlatform.Device findDevice(final J2MEPlatform p, final ArrayList<Profile> profiles) {
        final J2MEPlatform.Device d[] = p.getDevices();
        for (int i=0; i<d.length; i++) {
            if (new HashSet<Profile>(Arrays.asList(d[i].getProfiles())).containsAll(profiles)) {
                return d[i];
            }
        }
        return null;
    }
    
    @Override
    public void storeSettings(final Object settings) {
        final PlatformDescription desc = new PlatformDescription();
        getComponent();
        final J2MEPlatform pl = gui.getPlatform();
        final J2MEPlatform.Device dev = gui.getDevice();
        final J2MEPlatform.J2MEProfile conf = gui.getConfiguration();
        final J2MEPlatform.J2MEProfile prof = gui.getProfile();
        desc.platform = pl;
        if (conf != null) {
            desc.configuration = conf.toString();
        }
        if (prof != null) {
            desc.profile = prof.toString();
        }
        desc.device = dev;
        final TemplateWizard wiz = (TemplateWizard)settings;
        wiz.putProperty(PLATFORM_DESCRIPTION, desc);
    }
    
}
