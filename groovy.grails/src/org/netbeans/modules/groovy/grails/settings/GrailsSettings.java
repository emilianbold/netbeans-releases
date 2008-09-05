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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.grails.settings;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grails.RuntimeHelper;
import org.netbeans.modules.groovy.grails.api.GrailsEnvironment;
import org.openide.util.Exceptions;
import org.openide.util.NbCollections;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author schmidtm
 */
public final class GrailsSettings {

    public static final String GRAILS_BASE_PROPERTY = "grailsBase"; // NOI18N

    private static final String GRAILS_HOME_KEY = "grailsHome"; // NOI18N
    private static final String GRAILS_PORT_KEY = "grailsPrj-Port-"; // NOI18N
    private static final String GRAILS_ENV_KEY = "grailsPrj-Env-"; // NOI18N
    private static final String GRAILS_DEPLOY_KEY = "grailsPrj-Deploy-"; // NOI18N
    private static final String GRAILS_AUTODEPLOY_KEY = "grailsPrj-Autodeploy-"; // NOI18N

    // Which browser to use for client side debugging Firfox or Internet Explorer ?
    // Possible values for this key are FIREFOX and INTERNET_EXPLORER
    private static final String GRAILS_DEBUG_BROWSER_KEY = "grailsPrj-DebugBrowser-"; // NOI18N

    private static GrailsSettings instance;

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private GrailsSettings() {
        super();
    }

    public static synchronized GrailsSettings getInstance() {
        if (instance == null) {
            instance = new GrailsSettings();
        }
        return instance;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public String getGrailsBase() {
        String base = null;
        synchronized (this) {
            base = getPreferences().get(GRAILS_HOME_KEY, null);
        }
        if (base == null || base.length() <= 0) {
            base = findGroovyPlatform();
        }
        return base;
    }

    public void setGrailsBase(String path) {
        String oldValue;
        synchronized (this) {
            oldValue = getGrailsBase();
            getPreferences().put(GRAILS_HOME_KEY, path);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_BASE_PROPERTY, oldValue, path);
    }

    // Which port should we run on
    public String getPortForProject(Project prj) {
        assert prj != null;
        return getPreferences().get(getPortKey(prj), null);
    }

    public void setPortForProject(Project prj, String port) {
        assert prj != null;
        assert port != null;

        getPreferences().put(getPortKey(prj), port);
    }

    // which Environment should we use (Test, Production, Development, etc.)
    public GrailsEnvironment getEnvForProject(Project prj) {
        assert prj != null;
        String value = getPreferences().get(getEnvKey(prj), null);
        if (value != null) {
            return GrailsEnvironment.valueOf(value);
        }
        return null;
    }

    public void setEnvForProject(Project prj, GrailsEnvironment env) {
        assert prj != null;
        assert env != null;

        getPreferences().put(getEnvKey(prj), env.toString());
    }

    // Should we Autodeploy right after a 'grails war' command?
    public boolean getAutoDeployFlagForProject(Project prj) {
        assert prj != null;
        return getPreferences().getBoolean(getAutodeployKey(prj), false);
    }

    public void setAutoDeployFlagForProject(Project prj, boolean flag) {
        assert prj != null;

        getPreferences().putBoolean(getAutodeployKey(prj), flag);
    }

    // Where should the WAR-File be deployed to?
    public String getDeployDirForProject(Project prj) {
        assert prj != null;
        return getPreferences().get(getDeployKey(prj), null);
    }

    public void setDeployDirForProject(Project prj, String dir) {
        assert prj != null;
        assert dir != null;

        getPreferences().put(getDeployKey(prj), dir);
    }

    // Which browser to use for client side debugging Firfox or Internet Explorer ?
    public String getDebugBrowserForProject(Project prj) {
        assert prj != null;
        return getPreferences().get(getDebugBrowserKey(prj), null);
    }

    public void setDebugBrowserProject(Project prj, String browser) {
        assert prj != null;
        assert browser != null;

        getPreferences().put(getDebugBrowserKey(prj), browser);
    }

    private String getProjectName(Project prj) {
        assert prj != null;

        ProjectInformation info = prj.getLookup().lookup(ProjectInformation.class);
        assert info != null;
        return info.getName();
    }

    private String getPortKey(Project prj) {
        assert prj != null;
        return GRAILS_PORT_KEY + getProjectName(prj);
    }

    private String getEnvKey(Project prj) {
        assert prj != null;
        return GRAILS_ENV_KEY + getProjectName(prj);
    }

    private String getDeployKey(Project prj) {
        assert prj != null;
        return GRAILS_DEPLOY_KEY + getProjectName(prj);
    }

    private String getAutodeployKey(Project prj) {
        assert prj != null;
        return GRAILS_AUTODEPLOY_KEY + getProjectName(prj);
    }

    private String getDebugBrowserKey(Project prj) {
        assert prj != null;
        return GRAILS_DEBUG_BROWSER_KEY + getProjectName(prj);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(GrailsSettings.class);
    }

    private String findGroovyPlatform() {
        String groovyPath = System.getenv(RuntimeHelper.GRAILS_HOME_PROPERTY);
        if (groovyPath == null) {
            for (String dir : dirsOnPath()) {
                File f = null;
                if (Utilities.isWindows()) {
                    f = new File(dir, RuntimeHelper.WIN_EXECUTABLE_FILE);
                } else {
                    f = new File(dir, RuntimeHelper.NIX_EXECUTABLE_FILE);
                }
                if (f.isFile()) {
                    try {
                        groovyPath = f.getCanonicalFile().getParentFile().getParent();
                        break;
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        }
        return groovyPath;
    }

    /**
     * Returns an {@link Iterable} which will uniquely traverse all valid
     * elements on the <em>PATH</em> environment variables. That means,
     * duplicates and elements which are not valid, existing directories are
     * skipped.
     *
     * @return an {@link Iterable} which will traverse all valid elements on the
     * <em>PATH</em> environment variables.
     */

    /*FIXME: This method has been copied from the ruby.platform module.
     *  ruby.platform/src/org/netbeans/modules/ruby/platform/Util.java
     *
     * I don't know if it could be included into a shared module.
    */
    public static Iterable<String> dirsOnPath() {
        String rawPath = System.getenv("PATH"); // NOI18N
        if (rawPath == null) {
            rawPath = System.getenv("Path"); // NOI18N
        }
        if (rawPath == null) {
            return Collections.emptyList();
        }
        Set<String> candidates = new LinkedHashSet<String>(Arrays.asList(rawPath.split(File.pathSeparator)));
        for (Iterator<String> it = candidates.iterator(); it.hasNext();) {
            String dir = it.next();
            if (!new File(dir).isDirectory()) { // remove non-existing directories (#124562)
                it.remove();
            }
        }
        return NbCollections.iterable(candidates.iterator());
    }
}
