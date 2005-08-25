/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/** Object representing one module, possibly installed.
 * Responsible for opening of module JAR file; reading
 * manifest; parsing basic information such as dependencies;
 * and creating a classloader for use by the installer.
 * Methods not defined in ModuleInfo must be called from within
 * the module manager's read mutex as a rule.
 * @author Jesse Glick
 */
final class FixedModule extends Module {
    
    /** localized properties, only non-null if requested from disabled module */
    private Properties localizedProps;
    
    /** Map from extension JARs to sets of JAR that load them via Class-Path.
     * Used only for debugging purposes, so that a warning is printed if two
     * different modules try to load the same extension (which would cause them
     * to both load their own private copy, which may not be intended).
     */
    private static final Map extensionOwners = new HashMap(); // Map<File,Set<File>>
    /** Simple registry of JAR files used as modules.
     * Used only for debugging purposes, so that we can be sure
     * that no one is using Class-Path to refer to other modules.
     */
    private static final Set moduleJARs = new HashSet(); // Set<File>

    /** Set of locale-variants JARs for this module (or null).
     * Added explicitly to classloader, and can be used by execution engine.
     */
    private Set localeVariants = null; // Set<File>
    /** Set of extension JARs that this module loads via Class-Path (or null).
     * Can be used e.g. by execution engine. (#9617)
     */
    private Set plainExtensions = null; // Set<File>
    /** Set of localized extension JARs derived from plainExtensions (or null).
     * Used to add these to the classloader. (#9348)
     * Can be used e.g. by execution engine.
     */
    private Set localeExtensions = null; // Set<File>
    /** Patches added at the front of the classloader (or null).
     * Files are assumed to be JARs; directories are themselves.
     */
    private Set patches = null; // Set<File>

    /** Create a special-purpose "fixed" JAR. */
    public FixedModule(ModuleManager mgr, Events ev, Manifest manifest, Object history, ClassLoader classloader) throws InvalidException {
        super(mgr, ev, manifest, history, classloader);
        loadLocalizedPropsClasspath();
        parseManifest();
    }
    
    /** Get a localized attribute.
     * First, if OpenIDE-Module-Localizing-Bundle was given, the specified
     * bundle file (in all locale JARs as well as base JAR) is searched for
     * a key of the specified name.
     * Otherwise, the manifest's main attributes are searched for an attribute
     * with the specified name, possibly with a locale suffix.
     * If the attribute name contains a slash, and there is a manifest section
     * named according to the part before the last slash, then this section's attributes
     * are searched instead of the main attributes, and for the attribute listed
     * after the slash. Currently this would only be useful for localized filesystem
     * names. E.g. you may request the attribute org/foo/MyFileSystem.class/Display-Name.
     * In the future certain attributes known to be dangerous could be
     * explicitly suppressed from this list; should only be used for
     * documented localizable attributes such as OpenIDE-Module-Name etc.
     */
    public Object getLocalizedAttribute(String attr) {
        String locb = getManifest().getMainAttributes().getValue("OpenIDE-Module-Localizing-Bundle"); // NOI18N
        boolean usingLoader = false;
        if (locb != null) {
            if (classloader != null) {
                if (locb.endsWith(".properties")) { // NOI18N
                    usingLoader = true;
                    String basename = locb.substring(0, locb.length() - 11).replace('/', '.');
                    try {
                        ResourceBundle bundle = NbBundle.getBundle(basename, Locale.getDefault(), classloader);
                        try {
                            return bundle.getString(attr);
                        } catch (MissingResourceException mre) {
                            // Fine, ignore.
                        }
                    } catch (MissingResourceException mre) {
                        Util.err.notify(mre);
                    }
                } else {
                    Util.err.log(ErrorManager.WARNING, "WARNING - cannot efficiently load non-*.properties OpenIDE-Module-Localizing-Bundle: " + locb);
                }
            }
            if (!usingLoader) {
                if (localizedProps != null) {
                    String val = localizedProps.getProperty(attr);
                    if (val != null) {
                        return val;
                    }
                }
            }
        }
        // Try in the manifest now.
        int idx = attr.lastIndexOf('/'); // NOI18N
        if (idx == -1) {
            // Simple main attribute.
            return NbBundle.getLocalizedValue(getManifest().getMainAttributes(), new Attributes.Name(attr));
        } else {
            // Attribute of a manifest section.
            String section = attr.substring(0, idx);
            String realAttr = attr.substring(idx + 1);
            Attributes attrs = getManifest().getAttributes(section);
            if (attrs != null) {
                return NbBundle.getLocalizedValue(attrs, new Attributes.Name(realAttr));
            } else {
                return null;
            }
        }
    }
    
    public boolean isFixed() {
        return true;
    }
   
    /** Similar, but for fixed modules only.
     * Should be very rarely used: only for classpath modules with a strangely
     * named OpenIDE-Module-Localizing-Bundle (not *.properties).
     */
    private void loadLocalizedPropsClasspath() throws InvalidException {
        Attributes attr = manifest.getMainAttributes();
        String locbundle = attr.getValue("OpenIDE-Module-Localizing-Bundle"); // NOI18N
        if (locbundle != null) {
            Util.err.log("Localized props in " + locbundle + " for " + attr.getValue("OpenIDE-Module"));
            try {
                int idx = locbundle.lastIndexOf('.'); // NOI18N
                String name, ext;
                if (idx == -1) {
                    name = locbundle;
                    ext = ""; // NOI18N
                } else {
                    name = locbundle.substring(0, idx);
                    ext = locbundle.substring(idx);
                }
                List suffixes = new ArrayList(10);
                Iterator it = NbBundle.getLocalizingSuffixes();
                while (it.hasNext()) {
                    suffixes.add(it.next());
                }
                Collections.reverse(suffixes);
                it = suffixes.iterator();
                while (it.hasNext()) {
                    String suffix = (String)it.next();
                    String resource = name + suffix + ext;
                    InputStream is = classloader.getResourceAsStream(resource);
                    if (is != null) {
                        Util.err.log("Found " + resource);
                        if (localizedProps == null) {
                            localizedProps = new Properties();
                        }
                        localizedProps.load(is);
                    }
                }
                if (localizedProps == null) {
                    throw new IOException("Could not find localizing bundle: " + locbundle); // NOI18N
                }
            } catch (IOException ioe) {
                InvalidException e = new InvalidException(ioe.toString());
                Util.err.annotate(e, ioe);
                throw e;
            }
        }
    }

    /** Get all JARs loaded by this module.
     * Includes the module itself, any locale variants of the module,
     * any extensions specified with Class-Path, any locale variants
     * of those extensions.
     * The list will be in classpath order (patches first).
     * Currently the temp JAR is provided in the case of test modules, to prevent
     * sporadic ZIP file exceptions when background threads (like Java parsing) tries
     * to open libraries found in the library path.
     * JARs already present in the classpath are <em>not</em> listed.
     * @return a <code>List&lt;File&gt;</code> of JARs
     */
    public List getAllJars() {
        return Collections.EMPTY_LIST;
    }

    /**
     * This method can be overriden
     * in subclasses in case they want to change the reloadable semantix
     * of the fixed modules.
     *
     * @throws IllegalStateException as FixedModule cannot be reloaded
     */
    public void setReloadable(boolean r) {
        throw new IllegalStateException();
    }
    
    /** Reload this module. Access from ModuleManager.
     * If an exception is thrown, the module is considered
     * to be in an invalid state.
     *
     * @throws IllegalStateException as FixedModule cannot be reloaded
     */
    public void reload() throws IOException {
        throw new IOException("Fixed module cannot be reloaded!"); // NOI18N
    }
    
    // Access from ModuleManager:
    /** Turn on the classloader. Passed a list of parent modules to use.
     * The parents should already have had their classloaders initialized.
     */
    protected void classLoaderUp(Set parents) throws IOException {
        return; // no need
    }
    
    /** Turn off the classloader and release all resources. */
    protected void classLoaderDown() {
        return; // don't touch it
    }
    /** Should be called after turning off the classloader of one or more modules & GC'ing. */
    protected void cleanup() {
        return; // don't touch it
    }
    
    /** Notify the module that it is being deleted. */
    protected void destroy() {
    }
    
    /** String representation for debugging. */
    public String toString() {
        String s = "FixedModule:" + getCodeNameBase(); // NOI18N
        if (!isValid()) s += "[invalid]"; // NOI18N
        return s;
    }
}
