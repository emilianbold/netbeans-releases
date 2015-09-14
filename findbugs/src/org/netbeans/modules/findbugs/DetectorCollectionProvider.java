/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.findbugs;

import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.PluginException;
import edu.umd.cs.findbugs.PluginLoader;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.findbugs.options.FindBugsPanel;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author lahvac
 */
public class DetectorCollectionProvider {
    
    private static final String PLUGIN_URL_PREFIX = "PluginURL";
    
    public static synchronized void initializeDetectorFactoryCollection() {
        clearRegisteredPlugins();
        
        List<String> paths = customPlugins();
        
        for (String path : paths) {
            try {
                File f = new File(path);
                Plugin p = Plugin.addCustomPlugin(f.toURI(), DetectorFactory.class.getClassLoader());
                System.err.println("path=" + path + ", f=" + f.getAbsolutePath() + ", p=" + p);
            } catch (PluginException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public static DetectorFactoryCollection getTemporaryCollection(List<String> paths) {
        clearRegisteredPlugins();
        
        Collection<Plugin> plugins = new ArrayList<Plugin>(paths.size());
        
        for (String path : paths) {
            try {
                File f = new File(path);
                plugins.add(PluginLoader.getPluginLoader(f.toURI().toURL(), DetectorFactory.class.getClassLoader(), false, true).loadPlugin());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (PluginException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return new DetectorFactoryCollection(plugins) { };
    }

    public static List<String> checkTemporaryCollection(List<String> paths) {
        clearRegisteredPlugins();

        List<String> ret = new ArrayList<String>(paths.size());

        for (String path : paths) {
            try {
                File f = new File(path);
                PluginLoader.getPluginLoader(f.toURI().toURL(), DetectorFactory.class.getClassLoader(), false, true).loadPlugin();
            } catch (MalformedURLException ex) {
                ret.add(path);
            } catch (PluginException ex) {
                ret.add(path);
            }
        }
        return ret;
    }
    
    public static List<String> customPlugins() {
        List<String> plugins = new ArrayList<String>();
        Preferences customPlugins = NbPreferences.forModule(FindBugsPanel.class).node("custom-plugins");
        
        try {
            for (String key : customPlugins.keys()) {
                if (key.startsWith(PLUGIN_URL_PREFIX)) {
                    plugins.add(customPlugins.get(key, null));
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        while (plugins.remove(null));
        
        return plugins;
    }
    
    public static void setCustomPlugins(List<String> customPlugins) {
        try {
            Preferences customPluginsPrefs = NbPreferences.forModule(FindBugsPanel.class).node("custom-plugins");
            customPluginsPrefs.clear();
            
            int i = 0;
            for (String plugin : customPlugins) {
                customPluginsPrefs.put(PLUGIN_URL_PREFIX + i, plugin);
                i++;
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        Installer.writeKeywords();
    }

    private static void clearRegisteredPlugins() {
        Iterator<Plugin> plugins = DetectorFactoryCollection.instance().pluginIterator();
        List<Plugin> toRemove = new ArrayList<>();
        while (plugins.hasNext()) {
            Plugin p = plugins.next();
            if (!p.isCorePlugin()) {
                toRemove.add(p);
            }
        }
        // prevents ConcurrentModificationException in the above code
        for (Plugin p : toRemove) {
            Plugin.removeCustomPlugin(p);
        }
    }
}
