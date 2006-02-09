/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;

abstract class AbstractEntry implements ModuleEntry {
    
    private String localizedName;
    private Set/*<String>*/ publicClassNames;
    
    protected abstract LocalizedBundleInfo getBundleInfo();
    
    public String getLocalizedName() {
        if (localizedName == null) {
            localizedName = getBundleInfo().getDisplayName();
            if (localizedName == null) {
                localizedName = getCodeNameBase();
            }
        }
        return localizedName;
    }
    
    public String getCategory() {
        return getBundleInfo().getCategory();
    }
    
    public String getShortDescription() {
        return getBundleInfo().getShortDescription();
    }
    
    public String getLongDescription() {
        return getBundleInfo().getLongDescription();
    }
    
    public int compareTo(Object o) {
        int retval = getLocalizedName().compareTo(((ModuleEntry) o).getLocalizedName()); 
        return (retval != 0) ? retval : getCodeNameBase().compareTo(((ModuleEntry) o).getCodeNameBase());
    }
    
    public synchronized Set/*<String>*/ getPublicClassNames() {
        if (publicClassNames == null) {
            try {
                publicClassNames = computePublicClassNamesInMainModule();
                String[] cpext = PropertyUtils.tokenizePath(getClassPathExtensions());
                for (int i = 0; i < cpext.length; i++) {
                    File ext = new File(cpext[i]);
                    if (!ext.isFile()) {
                        Util.err.log(ErrorManager.WARNING, "Could not find Class-Path extension " + ext + " of " + this);
                        continue;
                    }
                    scanJarForPublicClassNames(publicClassNames, ext);
                }
            } catch (IOException e) {
                publicClassNames = Collections.EMPTY_SET;
                Util.err.annotate(e, ErrorManager.UNKNOWN, "While scanning for public classes in " + this, null, null, null); // NOI18N
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return publicClassNames;
    }
    
    protected final void scanJarForPublicClassNames(Set/*<String>*/ result, File jar) throws IOException {
        ManifestManager.PackageExport[] pkgs = getPublicPackages();
        Set/*<String>*/ publicPackagesSlashNonRec = new HashSet();
        List/*<String>*/ publicPackagesSlashRec = new ArrayList();
        for (int i = 0; i < pkgs.length; i++) {
            String name = pkgs[i].getPackage().replace('.', '/') + '/';
            if (pkgs[i].isRecursive()) {
                publicPackagesSlashRec.add(name);
            } else {
                publicPackagesSlashNonRec.add(name);
            }
        }
        JarFile jf = new JarFile(jar);
        try {
            Enumeration entries = jf.entries();
            ENTRY: while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                String path = entry.getName();
                if (!path.endsWith(".class")) { // NOI18N
                    continue;
                }
                int slash = path.lastIndexOf('/');
                if (slash == -1) {
                    continue;
                }
                String pkg = path.substring(0, slash + 1);
                if (!publicPackagesSlashNonRec.contains(pkg)) {
                    boolean pub = false;
                    Iterator it = publicPackagesSlashRec.iterator();
                    while (it.hasNext()) {
                        if (pkg.startsWith((String) it.next())) {
                            pub = true;
                            break;
                        }
                    }
                    if (!pub) {
                        continue;
                    }
                }
                StringTokenizer tok = new StringTokenizer(path, "$"); // NOI18N
                while (tok.hasMoreTokens()) {
                    String component = tok.nextToken();
                    char c = component.charAt(0);
                    if (c >= '0' && c <= '9') {
                        // Generated anon inner class name, skip.
                        continue ENTRY;
                    }
                }
                if (!isPublic(jf, entry)) {
                    continue;
                }
                result.add(path.substring(0, path.length() - 6).replace('/', '.'));
            }
        } finally {
            jf.close();
        }
    }
    
    protected abstract Set/*<String>*/ computePublicClassNamesInMainModule() throws IOException;
    
    // XXX consider inheritance refactoring instead.
    /**
     * Just a convenient methods. <code>null</code> may be passed as a
     * <cdoe>friends</code>.
     */
    protected static boolean isDeclaredAsFriend(String[] friends, String cnb) {
        return friends == null ? true : Arrays.binarySearch(friends, cnb) >= 0;
    }

    /** Checks whether a .class file is marked as public or not. */
    private static boolean isPublic(JarFile jf, JarEntry entry) throws IOException {
        InputStream is = jf.getInputStream(entry);
        try {
            DataInput input = new DataInputStream(is);
            skip(input, 8); // magic, minor_version, major_version
            // Have to partially parse constant pool to skip over it:
            int size = input.readUnsignedShort() - 1; // constantPoolCount
            for (int i = 0; i < size; i++) {
                byte tag = input.readByte();
                switch (tag) {
                    case 1: // CONSTANT_Utf8
                        input.readUTF();
                        break;
                    case 3: // CONSTANT_Integer
                    case 4: // CONSTANT_Float
                    case 9: // CONSTANT_Fieldref
                    case 10: // CONSTANT_Methodref
                    case 11: // CONSTANT_InterfaceMethodref
                    case 12: // CONSTANT_NameAndType
                        skip(input, 4);
                        break;
                    case 7: // CONSTANT_Class
                    case 8: // CONSTANT_String
                        skip(input, 2);
                        break;
                    case 5: // CONSTANT_Long
                    case 6: // CONSTANT_Double
                        skip(input, 8);
                        i++; // weirdness in spec
                        break;
                    default:
                        throw new IOException("Unrecognized constant pool tag " + tag + " at index " + i); // NOI18N
                }
            }
            int accessFlags = input.readUnsignedShort();
            return (accessFlags & 0x0001) > 0;
        } finally {
            is.close();
        }
    }
    private static void skip(DataInput input, int bytes) throws IOException {
        int skipped = input.skipBytes(bytes);
        if (skipped != bytes) {
            throw new IOException();
        }
    }
    
}
