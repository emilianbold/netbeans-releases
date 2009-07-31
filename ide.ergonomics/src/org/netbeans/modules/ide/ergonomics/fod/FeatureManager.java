/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ide.ergonomics.fod;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.openide.modules.ModuleInfo;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jirka Rechtacek
 */
public final class FeatureManager
implements PropertyChangeListener, LookupListener {
    private static FeatureManager INSTANCE;
    private static Logger UILOG = Logger.getLogger("org.netbeans.ui.ergonomics"); // NOI18N
    private final Lookup.Result<ModuleInfo> result;
    private final ChangeSupport support;
    private Set<String> enabledCnbs = Collections.emptySet();

    private FeatureManager() {
        support = new ChangeSupport(this);
        result = Lookup.getDefault().lookupResult(ModuleInfo.class);
        result.addLookupListener(this);
        resultChanged(null);
    }

    public static synchronized FeatureManager getInstance () {
        if (INSTANCE == null) {
            INSTANCE = new FeatureManager();
        }
        return INSTANCE;
    }

    static void logUI(String msg, Object... params) {
        LogRecord rec = new LogRecord(Level.FINE, msg);
        rec.setResourceBundleName("org.netbeans.modules.ide.ergonomics.fod.Bundle"); // NOI18N
        rec.setResourceBundle(NbBundle.getBundle(ConfigurationPanel.class));
        rec.setParameters(params);
        rec.setLoggerName(UILOG.getName());
        UILOG.log(rec);
    }

    static boolean showInAU(ModuleInfo mi) {
        final Object show = mi.getAttribute("AutoUpdate-Show-In-Client"); // NOI18N
        return show == null || "true".equals(show); // NOI18N
    }


    public static Map<String,String> nbprojectTypes() {
        return FeatureInfo.nbprojectTypes();
    }

    public static Map<String,String> projectFiles() {
        return FeatureInfo.projectFiles();
    }

    public static Collection<? extends FeatureInfo> features() {
        return featureTypesLookup().lookupAll(FeatureInfo.class);
    }

    /** Returns the amount of (partially) enabled clusters, or -1 if not
     * computed.
     * @return
     */
    public static int dumpModules() {
        return dumpModules(Level.FINE, Level.FINEST);
    }
    /** Returns the amount of (partially) enabled clusters, or -1 if not
     * computed.
     * @param withLevel with what severity dump the modules?
     * @param detailsLevel level to print detailed infos
     * @return
     */
    public static int dumpModules(Level withLevel, Level detailsLevel) {
        if (!FoDFileSystem.LOG.isLoggable(withLevel)) {
            return -1;
        }
        int cnt = 0;
        Collection<? extends ModuleInfo> allModules = Lookup.getDefault().lookupAll(ModuleInfo.class);
        for (FeatureInfo info : features()) {
            Set<String> enabled = new TreeSet<String>();
            Set<String> disabled = new TreeSet<String>();
            for (ModuleInfo m : allModules) {
                if (info.getCodeNames().contains(m.getCodeNameBase())) {
                    if (m.isEnabled()) {
                        enabled.add(m.getCodeNameBase());
                    } else {
                        disabled.add(m.getCodeNameBase());
                    }
                }
            }
            if (enabled.isEmpty() && disabled.isEmpty()) {
                FoDFileSystem.LOG.log(withLevel, info.clusterName + " not present"); // NOTICES
                continue;
            }
            if (enabled.isEmpty()) {
                FoDFileSystem.LOG.log(withLevel, info.clusterName + " disabled"); // NOTICES
                continue;
            }
            if (disabled.isEmpty()) {
                FoDFileSystem.LOG.log(withLevel, info.clusterName + " enabled"); // NOTICES
                cnt++;
                continue;
            }
            FoDFileSystem.LOG.log(withLevel,
                info.clusterName + " enabled " + enabled.size() + " disabled " + disabled.size()); // NOTICES
            cnt++;
            for (String cnb : disabled) {
                FoDFileSystem.LOG.log(detailsLevel, "- " + cnb); // NOI18N
            }
            for (String cnb : enabled) {
                FoDFileSystem.LOG.log(detailsLevel, "+ " + cnb); // NOI18N
            }
        }
        return cnt;
    }

    /** Used from tests */
    public static synchronized void assignFeatureTypesLookup(Lookup lkp) {
        boolean eaOn = false;
        assert eaOn = true;
        if (!eaOn) {
            throw new IllegalStateException();
        }
        featureTypesLookup = lkp;
        noCnbCheck = true;
    }

    private static Lookup featureTypesLookup;
    private static boolean noCnbCheck;
    private static synchronized Lookup featureTypesLookup() {
        if (featureTypesLookup != null) {
            return featureTypesLookup;
        }

        String clusters = System.getProperty("netbeans.dirs");
        if (clusters == null) {
            featureTypesLookup = Lookup.EMPTY;
        } else {
            InstanceContent ic = new InstanceContent();
            AbstractLookup l = new AbstractLookup(ic);
            String[] paths = clusters.split(File.pathSeparator);
            for (String c : paths) {
                int last = c.lastIndexOf(File.separatorChar);
                String clusterName = c.substring(last + 1).replaceFirst("[0-9\\.]*$", "");
                String basename = "/org/netbeans/modules/ide/ergonomics/" + clusterName;
                String layerName = basename + "/layer.xml";
                String bundleName = basename + "/Bundle.properties";
                URL layer = FeatureManager.class.getResource(layerName);
                URL bundle = FeatureManager.class.getResource(bundleName);
                if (layer != null && bundle != null) {
                    FeatureInfo info;
                    try {
                        info = FeatureInfo.create(clusterName, layer, bundle);
                        ic.add(info);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            featureTypesLookup = l;
        }
        return featureTypesLookup;
    }

    public void addChangeListener(ChangeListener l) {
        support.addChangeListener(l);
    }
    public void removeChangeListener(ChangeListener l) {
        support.removeChangeListener(l);
    }

    public void resultChanged(LookupEvent ev) {
        for (ModuleInfo m : result.allInstances()) {
            m.removePropertyChangeListener(this);
            m.addPropertyChangeListener(this);
        }
        Set<String> tmp = new HashSet<String>();
        for (ModuleInfo mi : result.allInstances()) {
            if (mi.isEnabled()) {
                tmp.add(mi.getCodeNameBase());
            }
        }
        enabledCnbs = tmp;
        if (ev != null) {
            fireChange();
        }
    }
    public void propertyChange(PropertyChangeEvent evt) {
        if (ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {
            ModuleInfo mi = (ModuleInfo)evt.getSource();
            if (!noCnbCheck && enabledCnbs.contains(mi.getCodeNameBase()) && mi.isEnabled()) {
                return;
            }
            fireChange();
            if (mi.isEnabled()) {
                enabledCnbs.add(mi.getCodeNameBase());
            } else {
                enabledCnbs.remove(mi.getCodeNameBase());
            }
        }
    }

    private void fireChange() {
        FoDFileSystem.LOG.fine("Firing FeatureManager change"); // NOI18N
        for (FeatureInfo f : features()) {
            f.clearCache();
        }
        support.fireChange();
        FoDFileSystem.LOG.fine("FeatureManager change delivered"); // NOI18N
    }

}
