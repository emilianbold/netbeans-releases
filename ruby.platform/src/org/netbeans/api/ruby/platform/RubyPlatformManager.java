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
package org.netbeans.api.ruby.platform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ruby.spi.project.support.rake.EditableProperties;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyUtils;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 * Represents one Ruby platform, i.e. installation of a Ruby interpreter.
 */
public final class RubyPlatformManager {

    private static final String PLATFORM_PREFIX = "rubyplatform."; // NOI18N
    private static final String PLATFORM_INTEPRETER = ".interpreter"; // NOI18N
    private static final String PLATFORM_LABEL_SUFFIX = ".label"; // NOI18N
    private static final String PLATFORM_ID_DEFAULT = "default"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(RubyPlatformManager.class.getName());
    
    private static Set<RubyPlatform> platforms;

    private RubyPlatformManager() {
        // static methods only
    }

    /**
     * Get a set of all registered platforms.
     */
    public static synchronized Set<RubyPlatform> getPlatforms() {
        return new HashSet<RubyPlatform>(getPlatformsInternal());
    }

    private static Set<RubyPlatform> getPlatformsInternal() {
        if (platforms == null) {
            platforms = new HashSet<RubyPlatform>();
            Map<String, String> p = PropertyUtils.sequentialPropertyEvaluator(null,
                    PropertyUtils.globalPropertyProvider()).getProperties();
            if (p == null) { // #115909
                p = Collections.emptyMap();
            }
            boolean foundDefault = false;
            for (Map.Entry<String, String> entry : p.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(PLATFORM_PREFIX) && key.endsWith(PLATFORM_INTEPRETER)) {
                    String id = key.substring(PLATFORM_PREFIX.length(),
                            key.length() - PLATFORM_INTEPRETER.length());
                    String label = p.get(PLATFORM_PREFIX + id + PLATFORM_LABEL_SUFFIX);
                    String interpreterPath = entry.getValue();
                    platforms.add(new RubyPlatform(id, interpreterPath, label));
                    foundDefault |= id.equals(PLATFORM_ID_DEFAULT);
                }
            }
            if (!foundDefault) {
                String loc = RubyInstallation.getInstance().getJRuby();
                if (loc != null) {
                    platforms.add(new RubyPlatform(PLATFORM_ID_DEFAULT, loc,
                            NbBundle.getMessage(RubyPlatformManager.class, "CTL_BundledJRubyLabel")));
                }
            }
            LOGGER.fine("RubyPlatform initial list: " + platforms);
        }

        return platforms;
    }

    /** Typically bundled JRuby. */
    public static RubyPlatform getDefaultPlatform() {
        return RubyPlatformManager.getPlatformByID(PLATFORM_ID_DEFAULT);
    }

    /**
     * Find a platform by its ID.
     * @param id an ID (as in {@link #getID})
     * @return the platform with that ID, or null
     */
    public static synchronized RubyPlatform getPlatformByID(String id) {
        for (RubyPlatform p : getPlatformsInternal()) {
            if (p.getID().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public static synchronized RubyPlatform getPlatformByPath(String path) {
        for (RubyPlatform p : getPlatformsInternal()) {
            try {
                File current = new File(p.getInterpreter()).getCanonicalFile();
                File toFind = new File(path).getCanonicalFile();
                if (current.equals(toFind)) {
                    return p;
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
        return null;
    }

    public static RubyPlatform addPlatform(final File interpreter, final String label) throws IOException {
        final String id = computeID(label);
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    if (getPlatformByID(id) != null) {
                        throw new IOException("ID " + id + " already taken");
                    }
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    String interpreterKey = PLATFORM_PREFIX + id + PLATFORM_INTEPRETER;
                    props.setProperty(interpreterKey, interpreter.getAbsolutePath());
                    if (!interpreter.isFile()) {
                        throw new FileNotFoundException(interpreter.getAbsolutePath());
                    }
                    props.setProperty(PLATFORM_PREFIX + id + PLATFORM_LABEL_SUFFIX, label);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        RubyPlatform plaf = new RubyPlatform(id, interpreter.getAbsolutePath(), label);
        synchronized (RubyPlatform.class) {
            getPlatformsInternal().add(plaf);
        }
        LOGGER.fine("RubyPlatform added: " + plaf);
        return plaf;
    }

    public static void removePlatform(final RubyPlatform plaf) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_INTEPRETER);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_LABEL_SUFFIX);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        synchronized (RubyPlatform.class) {
            getPlatformsInternal().remove(plaf);
        }
        LOGGER.fine("RubyPlatform removed: " + plaf);
    }

    private static String computeID(final String label) {
        String base = label.replaceAll("[\\. ]", "_"); // NOI18N
        String id = base;
        for (int i = 0; getPlatformByID(id) != null; i++) {
            id = base + '_' + i;
        }
        return id;
    }

}
