/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Converts a set of NetBeans modules into OSGi bundles.
 * Note that a design constraint is that all modules are processed independently
 * (this is necessary in case you wish to perform incremental translation)
 * so no features are possible which would require knowledge of the contents of
 * another module. For example, an entry in {@code OpenIDE-Module-Module-Dependencies}
 * cannot be translated to an entry in {@code Import-Package} because that would
 * require knowing which packages from the imported module are being used in the
 * importing module, which the NetBeans metadata does not express.
 */
public class MakeOSGi extends Task {
    
    private File destdir;
    private List<ResourceCollection> modules = new ArrayList<ResourceCollection>();
    
    /**
     * Mandatory destination directory. Bundles will be created here.
     */
    public void setDestdir(File destdir) {
        this.destdir = destdir;
    }
    
    /**
     * Adds a set of module JARs.
     * It is permitted for them to be JARs anywhere on disk,
     * but it is best if they are in a cluster structure
     * with ../update_tracking/*.xml present
     * so that associated files can be included in the bundle.
     */
    public void add(ResourceCollection modules) {
        this.modules.add(modules);
    }

    public @Override void execute() throws BuildException {
        if (destdir == null) {
            throw new BuildException("missing destdir");
        }
        for (ResourceCollection rc : modules) {
            Iterator<?> it = rc.iterator();
            while (it.hasNext()) {
                File jar = ((FileResource) it.next()).getFile();
                try {
                    process(jar);
                } catch (Exception x) {
                    throw new BuildException("Could not process " + jar + ": " + x, x, getLocation());
                }
            }
        }
    }

    private void process(File module) throws Exception {
        JarFile jar = new JarFile(module);
        try {
            Manifest netbeans = jar.getManifest();
            Attributes netbeansAttr = netbeans.getMainAttributes();
            String originalBundleName = netbeansAttr.getValue("Bundle-SymbolicName");
            if (originalBundleName != null) { // #180201
                String originalBundleVersion = netbeansAttr.getValue("Bundle-Version");
                Copy copy = new Copy();
                copy.setProject(getProject());
                copy.setOwningTarget(getOwningTarget());
                copy.setFile(module);
                File bundleFile = new File(destdir, originalBundleName + (originalBundleVersion != null ? "-" + originalBundleVersion : "") + ".jar");
                copy.setTofile(bundleFile);
                copy.execute();
                log("Copying " + module + " unmodified into " + bundleFile);
                return;
            }
            Manifest osgi = new Manifest();
            Attributes osgiAttr = osgi.getMainAttributes();
            Set<String> importedPackages = new TreeSet<String>();
            Set<String> availablePackages = new TreeSet<String>();
            scanClasses(jar, importedPackages, availablePackages);
            translate(netbeansAttr, osgiAttr, importedPackages, availablePackages);
            String cnb = osgiAttr.getValue("Bundle-SymbolicName");
            String bundleVersion = osgiAttr.getValue("Bundle-Version");
            File bundleFile = new File(destdir, cnb + (bundleVersion != null ? "-" + bundleVersion : "") + ".jar");
            if (bundleFile.lastModified() > module.lastModified()) {
                log("Skipping " + module + " since " + bundleFile + " is newer", Project.MSG_VERBOSE);
                return;
            }
            log("Processing " + module + " into " + bundleFile);
            Properties localizedStrings = new Properties();
            String locbundle = netbeansAttr.getValue("OpenIDE-Module-Localizing-Bundle");
            if (locbundle != null) {
                InputStream is = jar.getInputStream(jar.getEntry(locbundle));
                try {
                    localizedStrings.load(is);
                } finally {
                    is.close();
                }
                osgiAttr.putValue("Bundle-Localization", locbundle.replaceFirst("[.]properties$", ""));
            }
            handleDisplayAttribute(localizedStrings, netbeansAttr, osgiAttr,
                    "OpenIDE-Module-Name", "Bundle-Name");
            handleDisplayAttribute(localizedStrings, netbeansAttr, osgiAttr,
                    "OpenIDE-Module-Display-Category", "Bundle-Category");
            handleDisplayAttribute(localizedStrings, netbeansAttr, osgiAttr,
                    "OpenIDE-Module-Short-Description", "Bundle-Description");
            Map<String,File> bundledFiles = findBundledFiles(module, cnb);
            // XXX any use for OpenIDE-Module-Long-Description?
            String classPath = netbeansAttr.getValue("Class-Path");
            if (classPath != null) {
                StringBuilder bundleCP = new StringBuilder();
                for (String entry : classPath.split("[, ]+")) {
                    String clusterPath = new URI(module.getParentFile().getName() + "/" + entry).normalize().toString();
                    if (bundledFiles.containsKey(clusterPath)) {
                        bundleCP.append("/OSGI-INF/files/").append(clusterPath).append(",");
                    } else {
                        log("Class-Path entry " + entry + " from " + module + " does not correspond to any apparent cluster file", Project.MSG_WARN);
                    }
                }
                osgiAttr.putValue("Bundle-Classpath", bundleCP + ".");
            }
            // XXX modules/lib/*.dll/so => Bundle-NativeCode (but syntax is rather complex)
            OutputStream bundle = new FileOutputStream(bundleFile);
            try {
                ZipOutputStream zos = new JarOutputStream(bundle, osgi);
                Set<String> parents = new HashSet<String>();
                Enumeration<? extends ZipEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String path = entry.getName();
                    if (path.endsWith("/") || path.equals("META-INF/MANIFEST.MF")) {
                        continue;
                    }
                    InputStream is = jar.getInputStream(entry);
                    try {
                        writeEntry(zos, path, is, parents);
                    } finally {
                        is.close();
                    }
                }
                for (Map.Entry<String,File> bundledFile : bundledFiles.entrySet()) {
                    InputStream is = new FileInputStream(bundledFile.getValue());
                    try {
                        // XXX need matching IFL impl in netigso
                        writeEntry(zos, "OSGI-INF/files/" + bundledFile.getKey(), is, parents);
                    } finally {
                        is.close();
                    }
                }
                zos.finish();
                zos.close();
            } finally {
                bundle.close();
            }
        } finally {
            jar.close();
        }
    }

    /**
     * Translate NetBeans module metadata to OSGi equivalents.
     * @param netbeans manifest header to be read
     * @param osgi manifest header to be written
     * @param importedPackages any OSGi packages, or JRE packages outside the java.* namespace, which seem to be in use
     * @param availablePackages packages defined in the module
     */
    static void translate(Attributes netbeans, Attributes osgi, Set<String> importedPackages, Set<String> availablePackages) throws Exception {
        osgi.putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        osgi.putValue("Bundle-ManifestVersion", "2");
        String codename = netbeans.getValue("OpenIDE-Module");
        if (codename == null) {
            throw new IllegalArgumentException("Does not appear to be a NetBeans module");
        }
        String cnb = codename.replaceFirst("/\\d+$", "");
        if (cnb.equals("org.netbeans.core.netigso")) {
            // special handling...
            osgi.putValue("Bundle-Activator", "org.netbeans.core.osgi.Activator");
        }
        osgi.putValue("Bundle-SymbolicName", cnb);
        String spec = netbeans.getValue("OpenIDE-Module-Specification-Version");
        String implVersion = netbeans.getValue("OpenIDE-Module-Implementation-Version");
        String bundleVersion = null;
        if (spec != null) {
            bundleVersion = threeDotsWithMajor(spec, codename);
            String buildVersion = netbeans.getValue("OpenIDE-Module-Build-Version");
            if (buildVersion == null) {
                buildVersion = implVersion;
            }
            if (buildVersion != null) {
                bundleVersion += "." + buildVersion.replaceAll("[^a-zA-Z0-9_-]", "_");
            }
            osgi.putValue("Bundle-Version", bundleVersion);
        }
        List<String> exportedPackages = new ArrayList<String>();
        String pp = netbeans.getValue("OpenIDE-Module-Public-Packages");
        if (implVersion != null && implVersion.matches("\\d+")) {
            // Since we have no idea who might be using these packages, have to make everything public.
            exportedPackages.addAll(availablePackages);
            pp = null;
        }
        if (pp != null && !pp.equals("-")) {
            for (String p : pp.split("[, ]+")) {
                if (p.isEmpty()) {
                    continue;
                }
                if (p.endsWith(".*")) {
                    exportedPackages.add(p.substring(0, p.length() - ".*".length()));
                } else {
                    if (!p.endsWith(".**")) {
                        throw new IllegalArgumentException("Invalid package export: " + p);
                    }
                    for (String actual : availablePackages) {
                        if (actual.equals(p.substring(0, p.length() - ".**".length())) ||
                                actual.startsWith(p.substring(0, p.length() - "**".length()))) {
                            exportedPackages.add(actual);
                        }
                    }
                }
            }
            // OpenIDE-Module-Friends is ignored since OSGi has no apparent equivalent
            // (could use mandatory export constraints but friends would then
            // need to use Import-Package to access, rather than Require-Bundle,
            // which would require knowing which packages are being imported by that dep)
        }
        if (!exportedPackages.isEmpty()) {
            StringBuilder b = new StringBuilder();
            for (String p : exportedPackages) {
                if (b.length() > 0) {
                    b.append(", ");
                }
                b.append(p);
            }
            osgi.putValue("Export-Package", b.toString());
        }
        for (String attrToCopy : new String[] {"OpenIDE-Module-Layer", "OpenIDE-Module-Install"}) {
            String val = netbeans.getValue(attrToCopy);
            if (val != null) {
                osgi.putValue(attrToCopy, val);
            }
        }
        StringBuilder requireBundles = new StringBuilder();
        /* XXX does not work, perhaps because of cyclic dependencies:
        // do not need to import any API, just need it to be started:
        requireBundles.append("org.netbeans.core.netigso");
         */
        String dependencies = netbeans.getValue("OpenIDE-Module-Module-Dependencies");
        if (dependencies != null) {
            for (String dependency : dependencies.split(" *, *")) {
                if (requireBundles.length() > 0) {
                    requireBundles.append(", ");
                }
                translateDependency(requireBundles, dependency);
            }
        }
        if (requireBundles.length() > 0) {
            osgi.putValue("Require-Bundle", requireBundles.toString());
        }
        if (!cnb.equals("org.netbeans.libs.osgi")) {
            StringBuilder staticImports = new StringBuilder();
            StringBuilder dynamicImports = new StringBuilder();
            for (String pkg : importedPackages) {
                StringBuilder b = staticImports;
                if (pkg.startsWith("com.sun.") || pkg.startsWith("sun.")) {
                    // JRE-specific dependencies will not be exported by Felix by default.
                    // But they might have been exported by another module
                    // (Require-Bundle should trump DynamicImport-Package),
                    // or Felix can be configured to offer export packages from the framework.
                    // Do not use DynamicImport-Package where not really needed, as it can lead to deadlocks in Felix:
                    // ModuleImpl.findClassOrResourceByDelegation -> Felix.acquireGlobalLock
                    b = dynamicImports;
                }
                if (b.length() > 0) {
                    b.append(", ");
                }
                b.append(pkg);
            }
            if (staticImports.length() > 0) {
                osgi.putValue("Import-Package", staticImports.toString());
            }
            if (dynamicImports.length() > 0) {
                osgi.putValue("DynamicImport-Package", dynamicImports.toString());
            }
        }
        // ignore OpenIDE-Module-Package-Dependencies; rarely used, and bytecode analysis is probably more accurate anyway
        // XXX OpenIDE-Module-Java-Dependencies => Bundle-RequiredExecutionEnvironment: JavaSE-1.6
        for (String tokenAttr : new String[] {"OpenIDE-Module-Provides", "OpenIDE-Module-Requires", "OpenIDE-Module-Needs"}) {
            String v = netbeans.getValue(tokenAttr);
            if (v != null) {
                osgi.putValue(tokenAttr, v);
            }
        }
        // autoload, eager status are ignored since OSGi has no apparent equivalent
    }

    private static void writeEntry(ZipOutputStream zos, String path, InputStream data, Set<String> parents) throws IOException {
        int size = Math.max(data.available(), 100);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
        byte[] buf = new byte[size];
        int read;
        while ((read = data.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        writeEntry(zos, path, baos.toByteArray(), parents);
    }
    private static void writeEntry(ZipOutputStream zos, String path, byte[] data, Set<String> parents) throws IOException {
        assert path.length() > 0 && !path.endsWith("/") && !path.startsWith("/") && path.indexOf("//") == -1 : path;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                String parent = path.substring(0, i + 1);
                if (parents.add(parent)) {
                    ZipEntry ze = new ZipEntry(parent);
                    ze.setMethod(ZipEntry.STORED);
                    ze.setSize(0);
                    ze.setCrc(0);
                    zos.putNextEntry(ze);
                    zos.closeEntry();
                }
            }
        }
        ZipEntry ze = new ZipEntry(path);
        ze.setMethod(ZipEntry.STORED);
        ze.setSize(data.length);
        CRC32 crc = new CRC32();
        crc.update(data);
        ze.setCrc(crc.getValue());
        zos.putNextEntry(ze);
        zos.write(data, 0, data.length);
        zos.closeEntry();
    }

    // copied from NetigsoModuleFactory
    private static String threeDotsWithMajor(String version, String withMajor) {
        int indx = withMajor.indexOf('/');
        int major = 0;
        if (indx > 0) {
            major = Integer.parseInt(withMajor.substring(indx + 1));
        }
        String[] segments = (version + ".0.0.0").split("\\.");
        assert segments.length >= 3 && segments[0].length() > 0;
        return (Integer.parseInt(segments[0]) + major * 100) + "."  + segments[1] + "." + segments[2];
    }

    static void translateDependency(StringBuilder b, String dependency) throws IllegalArgumentException {
        Matcher m = Pattern.compile("([^/ >=]+)(?:/(\\d+)(?:-(\\d+))?)? *(?:(=|>) *(.+))?").matcher(dependency);
        if (!m.matches()) {
            throw new IllegalArgumentException("bad dep: " + dependency);
        }
        String depCnb = m.group(1);
        String depMajLo = m.group(2);
        String depMajHi = m.group(3);
        String comparison = m.group(4);
        String version = m.group(5);
        b.append(depCnb);
        if (!"=".equals(comparison)) {
            if (version == null) {
                version = "0";
            }
            String targetVersion = threeDotsWithMajor(version, depMajLo == null ? "" : "x/" + depMajLo);
            b.append(";bundle-version=\"[").append(targetVersion).append(",");
            b.append(100 * ((depMajHi != null ? Integer.parseInt(depMajHi) : depMajLo != null ? Integer.parseInt(depMajLo) : 0) + 1));
            b.append(")\"");
        }
    }

    private void handleDisplayAttribute(Properties props, Attributes netbeans, Attributes osgi, String netbeansHeader, String osgiHeader) throws IOException {
        String val = netbeans.getValue(netbeansHeader);
        if (val != null) {
            osgi.putValue(osgiHeader, val);
        } else if (props.containsKey(netbeansHeader)) {
            osgi.putValue(osgiHeader, "%" + netbeansHeader);
        }
    }

    private Map<String,File> findBundledFiles(File module, String cnb) throws Exception {
        Map<String,File> result = new HashMap<String,File>();
        if (module.getParentFile().getName().matches("modules|core|lib")) {
            File cluster = module.getParentFile().getParentFile();
            File updateTracking = new File(new File(cluster, "update_tracking"), cnb.replace('.', '-') + ".xml");
            if (updateTracking.isFile()) {
                Document doc = XMLUtil.parse(new InputSource(updateTracking.toURI().toString()), false, false, null, null);
                NodeList nl = doc.getElementsByTagName("file");
                for (int i = 0; i < nl.getLength(); i++) {
                    String path = ((Element) nl.item(i)).getAttribute("name");
                    if (path.matches("config/Modules/.+[.]xml")) {
                        continue;
                    }
                    File f = new File(cluster, path);
                    if (f.equals(module)) {
                        continue;
                    }
                    // XXX exclude lib/nbexec{,.dll,.exe}, core/*felix*.jar
                    if (f.isFile()) {
                        result.put(path, f);
                    } else {
                        log("did not find " + f + " specified in " + updateTracking, Project.MSG_WARN);
                    }
                }
            } else {
                log("did not find expected " + updateTracking, Project.MSG_WARN);
            }
        } else {
            log("JAR " + module + " not found in expected cluster layout", Project.MSG_WARN);
        }
        return result;
    }

    private void scanClasses(JarFile module, Set<String> importedPackages, Set<String> availablePackages) throws Exception {
        Map<String, byte[]> classfiles = new TreeMap<String, byte[]>();
        VerifyClassLinkage.read(module, classfiles, new HashSet<File>(Collections.singleton(new File(module.getName()))), this, null);
        ClassLoader jre = ClassLoader.getSystemClassLoader().getParent();
        for (Map.Entry<String,byte[]> entry : classfiles.entrySet()) {
            String available = entry.getKey();
            int idx = available.lastIndexOf('.');
            if (idx != -1) {
                availablePackages.add(available.substring(0, idx));
            }
            for (String clazz : VerifyClassLinkage.dependencies(entry.getValue())) {
                if (classfiles.containsKey(clazz)) {
                    // Part of the same module; probably not an external import.
                    continue;
                }
                if (clazz.startsWith("java.")) {
                    // No need to declare as an import.
                    continue;
                }
                // XXX should OpenIDE-Module-Hide-Classpath-Packages be considered for anything?
                // Usually used when the module itself bundles those packages, in which case
                // the OSGi container would presumably not create a wire to the framework bundle.
                if (!clazz.startsWith("org.osgi.") && jre.getResource(clazz.replace('.', '/') + ".class") == null) {
                    // Not part of either JRE (incl. implementation) or OSGi platform; assumed to be a module dependency.
                    continue;
                }
                idx = clazz.lastIndexOf('.');
                if (idx != -1) {
                    importedPackages.add(clazz.substring(0, idx));
                }
            }
        }
    }

}
