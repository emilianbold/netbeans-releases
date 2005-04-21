/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.modules;

import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

import java.io.File;

import java.util.Collection;


/**
 * Service providing the ability to locate a module-installed file in
 * the NetBeans application's installation.
 * Zero or more instances may be registered to lookup.
 * @author Jesse Glick
 * @since 3.21
 * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=28683">Issue #28683</a>
 */
public abstract class InstalledFileLocator {
    private static final InstalledFileLocator DEFAULT = new InstalledFileLocator() {
            public File locate(String rp, String cnb, boolean l) {
                InstalledFileLocator[] ifls = getInstances();

                for (int i = 0; i < ifls.length; i++) {
                    File f = ifls[i].locate(rp, cnb, l);

                    if (f != null) {
                        return f;
                    }
                }

                return null;
            }
        };

    private static InstalledFileLocator[] instances = null;
    private static Lookup.Result result = null;

    /**
     * No-op constructor for use by subclasses.
     */
    protected InstalledFileLocator() {
    }

    /**
     * Try to locate a file.
     * <div class="nonnormative">
     * <p>
     * When using the normal NetBeans installation structure and NBM file format,
     * this path will be relative to the installation directory (or user directory,
     * for a locally installed module). Other possible installation mechanisms, such
     * as JNLP (Java WebStart), might arrange the physical files differently, but
     * generally the path indicated by a module's normal NBM file (beneath <samp>netbeans/</samp>
     * in the NBM) should be interpreted by the locator implementation to point to the actual
     * location of the file, so the module need not be aware of such details. Some
     * locator implementations may perform the search more accurately or quickly
     * when given a code name base for the module that supplies the file.
     * </p>
     * <p>
     * The file may refer to a directory (no trailing slash!), in which case the locator
     * should attempt to find that directory in the installation. Note that only one
     * file may be located from a given path, so generally this method will not be
     * useful where a directory can contain many items that may be merged between e.g.
     * the installation and user directories. For example, the <samp>docs</samp> folder
     * (used e.g. for Javadoc) might contain several ZIP files in both the installation and
     * user areas. There is currently no supported way to enumerate all such files. Therefore
     * searching for a directory should be attempted only when there is just one module which
     * is expected to provide that directory and all of its contents. The module may assume
     * that all contained files are in the same relative structure in the directory as in
     * the normal NBM-based installation; unusual locator implementations may need to create
     * temporary directories with matching structures to return from this method, in case the
     * physical file locations are not in such a directory structure.
     * See issue #36701 for details.
     * </p>
     * </div>
     * <p>
     * Localized and branded lookups should follow the normal naming conventions,
     * e.g. <samp>docs/OpenAPIs_ja.zip</samp> would be used for Japanese Javadoc
     * and <samp>locate("docs/OpenAPIs.zip",&nbsp;&#8230;,&nbsp;true)</samp>
     * would find it when running in Japanese locale.
     * </p>
     * <div class="nonnormative">
     * <p>
     * For cases where the search is for a module JAR or one of its extensions, client
     * code may prefer to use the code source given by a class loader. This will permit
     * a client to find the base URL (may or may not refer to a file) responsible for loading
     * the contents of the protection domain, typically a JAR file, containing a class
     * which is accessible to the module class loader. For example:
     * </p>
     * <pre>
    <span class="type">Class</span> <span class="variable-name">c</span> = ClassMyModuleDefines.<span class="keyword">class</span>;
    <span class="type">URL</span> <span class="variable-name">u</span> = c.getProtectionDomain().getCodeSource().getLocation();
     * </pre>
     * <p>
     * When running from a JAR file, this will typically give e.g.
     * <samp>file:/path/to/archive.jar</samp>. This information may be useful,
     * but it is not conclusive, since there is no guarantee what the URL protocol
     * will be, nor that the returned URL uniquely identifies a JAR shipped with
     * the module in its canonical NBM format. <code>InstalledFileLocator</code>
     * provides stronger guarantees than this technique, since you can explicitly
     * name a JAR file to be located on disk.
     * </p>
     * </div>
     * <div class="nonnormative">
     * <p>
     * This class should <em>not</em> be used just to find resources on the system
     * filesystem, which in the normal NetBeans installation structure means the
     * result of merging <samp>${netbeans.home}/system/</samp> with <samp>${netbeans.user}/system/</samp>
     * as well as module layers and perhaps project-specific storage. To find data in
     * the system filesystem, use the Filesystems API, e.g. in your layer you can predefine:
     * </p>
    <pre>
    &lt;<span class="function-name">filesystem</span>&gt;
    &lt;<span class="function-name">folder</span> <span class="variable-name">name</span>=<span class="string">"MyModule"</span>&gt;
        &lt;<span class="function-name">file</span> <span class="variable-name">name</span>=<span class="string">"data.xml"</span> <span class="variable-name">url</span>=<span class="string">"contents-in-module-jar.xml"</span>/&gt;
    &lt;/<span class="function-name">folder</span>&gt;
    &lt;/<span class="function-name">filesystem</span>&gt;
    </pre>
     * <p>
     * Then in your code use:
     * </p>
    <pre>
    <span class="type">String</span> <span class="variable-name">path</span> = <span class="string">"MyModule/data.xml"</span>;
    <span class="type">FileSystem</span> <span class="variable-name">sfs</span> = Repository.getDefault().getDefaultFileSystem();
    <span class="type">FileObject</span> <span class="variable-name">fo</span> = sfs.findResource(path);
    <span class="keyword">if</span> (fo != <span class="constant">null</span>) {
    <span class="comment">// use fo.getInputStream() etc.
    </span>    <span class="comment">// FileUtil.toFile(fo) will often be null, do not rely on it!
    </span>}
    </pre>
     * </div>
     * @param relativePath path from install root, e.g. <samp>docs/OpenAPIs.zip</samp>
     *                     or <samp>modules/ext/somelib.jar</samp>
     *                     (always using <samp>/</samp> as a separator, regardless of platform)
     * @param codeNameBase name of the supplying module, e.g. <samp>org.netbeans.modules.foo</samp>;
     *                     may be <code>null</code> if unknown
     * @param localized true to perform a localized and branded lookup (useful for documentation etc.)
     * @return the requested <code>File</code>, if it can be found, else <code>null</code>
     */
    public abstract File locate(String relativePath, String codeNameBase, boolean localized);

    /**
     * Get a master locator.
     * Lookup is searched for all registered locators.
     * They are merged together and called in sequence
     * until one of them is able to service a request.
     * If you use this call, require the token <code>org.openide.modules.InstalledFileLocator</code>
     * to require any autoload modules which can provide locators.
     * @return a master merging locator (never null)
     */
    public static InstalledFileLocator getDefault() {
        return DEFAULT;
    }

    private static synchronized InstalledFileLocator[] getInstances() {
        if (instances == null) {
            if (result == null) {
                result = Lookup.getDefault().lookup(new Lookup.Template(InstalledFileLocator.class));
                result.addLookupListener(
                    new LookupListener() {
                        public void resultChanged(LookupEvent e) {
                            // Should not try to acquire lock inside lookup's lock, since result.allInstances
                            // could then deadlock (#50289). However this means that actual changes in the IFL's
                            // in lookup will not be recognized immediately. Probably that doesn't matter since
                            // they will rarely change.
                            RequestProcessor.getDefault().post(
                                new Runnable() {
                                    public void run() {
                                        synchronized (InstalledFileLocator.class) {
                                            instances = null;
                                        }
                                    }
                                }
                            );
                        }
                    }
                );
            }

            Collection /*<InstalledFileLocator>*/ c = result.allInstances();
            instances = (InstalledFileLocator[]) c.toArray(new InstalledFileLocator[c.size()]);
        }

        return instances;
    }
}
