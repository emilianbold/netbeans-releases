/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.openide.modules;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to standard file locations.
 * <div class="nonnormative">
 * This class should be used for limited purposes only. You might instead want to use:<ul>
 * <li><a href="@org-openide-filesystems@/org/openide/filesystems/FileUtil.html#getConfigFile(java.lang.String)"><code>FileUtil.getConfigFile</code></a>
 * to find a file declared in an XML layer or created or overridden in the {@code config} subdirectory of the user directory.
 * <li>{@link InstalledFileLocator} to find modules installed as part of an NBM.
 * <li>{@code someClass.getProtectionDomain().getCodeSource().getLocation()} to find resources inside a module class loader.
 * </ul>
 * </div>
 * @since 7.25
 */
public class Places {

    /**
     * System property key for {@link #getUserDirectory}.
     * Should not be used by most code directly.
     */
    public static final String USER_DIR_PROP = "netbeans.user";

    private static final String MEMORY = "memory";

    private static final Logger LOG = Logger.getLogger(Places.class.getName());

    private static File cacheDir;

    /**
     * Locates the NetBeans user directory.
     * This may be used to persist valuable files for which the system filesystem
     * ({@code FileUtil.getConfigFile}) is inappropriate due its being virtual.
     * Each module is responsible for using sufficiently unique filenames within this directory.
     * The system property {@link #USER_DIR_PROP} is used for compatibility.
     * @return a directory location (need not yet exist), or null if unconfigured
     */
    public static synchronized /*@CheckForNull*/ File getUserDirectory() {
        String p = System.getProperty(USER_DIR_PROP);
        return p != null && !p.equals(MEMORY) ? new File(p) : null;
    }

    /**
     * Configures the NetBeans user directory.
     * This would be called by startup code in the NetBeans Platform,
     * but might also be useful to call from a unit test if tested code calls {@link #getUserDirectory} or {@link #getCacheDirectory}.
     * @param dir a directory location (need not yet exist); null be passed but means the same as {@code memory} for the system property, interpreted by the module system
     */
    public static synchronized void setUserDirectory(/*@NullAllowed*/ File dir) {
        System.setProperty(USER_DIR_PROP, dir != null ? dir.getAbsolutePath() : MEMORY);
    }

    /**
     * Locates the NetBeans cache directory.
     * This may be used to store pure performance caches - files which could be safely deleted,
     * since they would be automatically recreated on demand.
     * Each module is responsible for using sufficiently unique filenames within this directory.
     * {@code $userdir/var/cache/} is used as a default when {@link #getUserDirectory} is configured.
     * As a final fallback, a location in the system temporary directory will be returned.
     * @return a directory location (never null but need not yet exist)
     * @see #getCacheSubdirectory
     * @see #getCacheSubfile
     */
    public static synchronized /*@NonNull*/ File getCacheDirectory() {
        if (cacheDir != null) {
            return cacheDir;
        }
        File userdir = getUserDirectory();
        if (userdir != null) {
            return new File(new File(userdir, "var"), "cache");
        }
        return new File(System.getProperty("java.io.tmpdir"), "nbcache");
    }

    /**
     * Convenience method to get a particular subdirectory within {@link #getCacheDirectory}.
     * The directory will be created if it does not yet exist (but a warning logged if permissions do not allow this).
     * @param path a subdirectory path such as {@code stuff} or {@code mymodule/stuff} ({@code /} permitted even on Windows)
     * @return a directory of that name within the general cache directory
     */
    public static /*@NonNull*/ File getCacheSubdirectory(String path) {
        File d = new File(getCacheDirectory(), path);
        if (!d.isDirectory() && !d.mkdirs()) {
            LOG.log(Level.WARNING, "could not create {0}", d);
        }
        return d;
    }

    /**
     * Convenience method to get a particular file within {@link #getCacheDirectory}.
     * The parent directory will be created if it does not yet exist (but a warning logged if permissions do not allow this);
     * the file itself will not be automatically created.
     * @param path a file path such as {@code stuff.ser} or {@code mymodule/stuff.ser} ({@code /} permitted even on Windows)
     * @return a file of that name within the general cache directory
     */
    public static /*@NonNull*/ File getCacheSubfile(String path) {
        File f = new File(getCacheDirectory(), path);
        File d = f.getParentFile();
        if (!d.isDirectory() && !d.mkdirs()) {
            LOG.log(Level.WARNING, "could not create {0}", d);
        }
        return f;
    }

    /**
     * Configures the NetBeans cache directory.
     * This would be called by startup code in the NetBeans Platform,
     * but might also be useful to call from a unit test if tested code calls {@link #getCacheDirectory}.
     * @param dir a directory location (need not yet exist)
     */
    public static synchronized void setCacheDirectory(/*@NonNull*/ File dir) {
        cacheDir = dir;
    }

    private Places() {}

}
