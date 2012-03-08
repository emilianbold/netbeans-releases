/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import org.netbeans.Module.PackageExport;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/** Information about essential properties of a module.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class ModuleData {
    private final static PackageExport[] ZERO_PACKAGE_ARRAY = new PackageExport[0];
    private final static String[] ZERO_STRING_ARRAY = new String[0];

    private final String codeName;
    private final String codeNameBase;
    private final int codeNameRelease;
    private final String implVersion;
    private final String buildVersion;
    private final Set<String> friendNames;
    private final SpecificationVersion specVers;
    private final PackageExport[] publicPackages;
    private final String[] provides;
    private final Dependency[] dependencies;
    private final Set<String> coveredPackages;
    
    
    ModuleData(Manifest mf, Module forModule) throws InvalidException {
        forModule.assignData(this);
        Attributes attr = mf.getMainAttributes();

        // Code name
        codeName = attr.getValue("OpenIDE-Module"); // NOI18N
        if (codeName == null) {
            InvalidException e = new InvalidException("Not a module: no OpenIDE-Module tag in manifest of " + /* #17629: important! */ this, mf); // NOI18N
            // #29393: plausible user mistake, deal with it politely.
            Exceptions.attachLocalizedMessage(e,
                NbBundle.getMessage(Module.class,
                "EXC_not_a_module",
                this.toString()));
            throw e;
        }
        try {
            // This has the side effect of checking syntax:
            if (codeName.indexOf(',') != -1) {
                throw new InvalidException("Illegal code name syntax parsing OpenIDE-Module: " + codeName); // NOI18N
            }
            Object[] cnParse = Util.parseCodeName(codeName);
            codeNameBase = (String) cnParse[0];
            Set<?> deps = forModule.getManager().loadDependencies(codeNameBase);
            boolean verifyCNBs = deps == null;
            if (verifyCNBs) {
                Dependency.create(Dependency.TYPE_MODULE, codeName);
            }
            codeNameRelease = (cnParse[1] != null) ? ((Integer) cnParse[1]).intValue() : -1;
            if (cnParse[2] != null) {
                throw new NumberFormatException(codeName);
            }
            // Spec vers
            String specVersS = attr.getValue("OpenIDE-Module-Specification-Version"); // NOI18N
            if (specVersS != null) {
                try {
                    specVers = new SpecificationVersion(specVersS);
                } catch (NumberFormatException nfe) {
                    throw (InvalidException) new InvalidException("While parsing OpenIDE-Module-Specification-Version: " + nfe.toString()).initCause(nfe); // NOI18N
                }
            } else {
                specVers = null;
            }
            String iv = attr.getValue("OpenIDE-Module-Implementation-Version"); // NOI18N
            implVersion = iv == null ? "" : iv;
            String bld = attr.getValue("OpenIDE-Module-Build-Version"); // NOI18N
            buildVersion = bld == null ? implVersion : bld;
            
            this.provides = computeProvides(forModule, attr, verifyCNBs);

            // Exports
            String exportsS = attr.getValue("OpenIDE-Module-Public-Packages"); // NOI18N
            if (exportsS != null) {
                if (exportsS.trim().equals("-")) { // NOI18N
                    publicPackages = ZERO_PACKAGE_ARRAY;
                } else {
                    StringTokenizer tok = new StringTokenizer(exportsS, ", "); // NOI18N
                    List<Module.PackageExport> exports = new ArrayList<Module.PackageExport>(Math.max(tok.countTokens(), 1));
                    while (tok.hasMoreTokens()) {
                        String piece = tok.nextToken();
                        if (piece.endsWith(".*")) { // NOI18N
                            String pkg = piece.substring(0, piece.length() - 2);
                            if (verifyCNBs) {
                                Dependency.create(Dependency.TYPE_MODULE, pkg);
                            }
                            if (pkg.lastIndexOf('/') != -1) {
                                throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                            }
                            exports.add(new Module.PackageExport(pkg.replace('.', '/') + '/', false));
                        } else if (piece.endsWith(".**")) { // NOI18N
                            String pkg = piece.substring(0, piece.length() - 3);
                            if (verifyCNBs) {
                                Dependency.create(Dependency.TYPE_MODULE, pkg);
                            }
                            if (pkg.lastIndexOf('/') != -1) {
                                throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                            }
                            exports.add(new Module.PackageExport(pkg.replace('.', '/') + '/', true));
                        } else {
                            throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                        }
                    }
                    if (exports.isEmpty()) {
                        throw new IllegalArgumentException("Illegal OpenIDE-Module-Public-Packages: " + exportsS); // NOI18N
                    }
                    publicPackages = exports.toArray(new Module.PackageExport[exports.size()]);
                }
            } else {
                // XXX new link?
                Util.err.log(Level.WARNING, "module {0} does not declare OpenIDE-Module-Public-Packages in its manifest, so all packages are considered public by default: http://www.netbeans.org/download/dev/javadoc/OpenAPIs/org/openide/doc-files/upgrade.html#3.4-public-packages", codeNameBase);
                publicPackages = null;
            }

            {
                HashSet<String> set = null;
                // friends 
                String friends = attr.getValue("OpenIDE-Module-Friends"); // NOI18N
                if (friends != null) {
                    StringTokenizer tok = new StringTokenizer(friends, ", "); // NOI18N
                    set = new HashSet<String>();
                    while (tok.hasMoreTokens()) {
                        String piece = tok.nextToken();
                        if (piece.indexOf('/') != -1) {
                            throw new IllegalArgumentException("May specify only module code name bases in OpenIDE-Module-Friends, not major release versions: " + piece); // NOI18N
                        }
                        if (verifyCNBs) {
                            // Indirect way of checking syntax:
                            Dependency.create(Dependency.TYPE_MODULE, piece);
                        }
                        // OK, add it.
                        set.add(piece);
                    }
                    if (set.isEmpty()) {
                        throw new IllegalArgumentException("Empty OpenIDE-Module-Friends: " + friends); // NOI18N
                    }
                    if (publicPackages == null || publicPackages.length == 0) {
                        throw new IllegalArgumentException("No use specifying OpenIDE-Module-Friends without any public packages: " + friends); // NOI18N
                    }
                }
                this.friendNames = set;
            }
            this.dependencies = initDeps(forModule, deps, attr);
        } catch (IllegalArgumentException iae) {
            throw (InvalidException) new InvalidException("While parsing " + codeName + " a dependency attribute: " + iae.toString()).initCause(iae); // NOI18N
        }
        this.coveredPackages = new HashSet<String>();
    }
    
    ModuleData(ObjectInput dis) throws IOException {
        try {
            this.codeName = dis.readUTF();
            this.codeNameBase = dis.readUTF();
            this.codeNameRelease = dis.readInt();
            this.coveredPackages = readStrings(dis, new HashSet<String>(), true);
            this.dependencies = (Dependency[]) dis.readObject();
            this.implVersion = dis.readUTF();
            this.buildVersion = dis.readUTF();
            this.provides = readStrings(dis);
            this.friendNames = readStrings(dis, new HashSet<String>(), false);
            this.specVers = new SpecificationVersion(dis.readUTF());
            this.publicPackages = null;
        } catch (ClassNotFoundException cnfe) {
            throw new IOException(cnfe);
        }
    }
    
    void write(ObjectOutput dos) throws IOException {
        dos.writeUTF(codeName);
        dos.writeUTF(codeNameBase);
        dos.writeInt(codeNameRelease);
        writeStrings(dos, coveredPackages);
        dos.writeObject(dependencies);
        dos.writeUTF(implVersion);
        dos.writeUTF(buildVersion);
        writeStrings(dos, provides);
        writeStrings(dos, friendNames);
        dos.writeUTF(specVers.toString());
    }

    private String[] computeProvides(Module forModule, Attributes attr, boolean verifyCNBs) throws InvalidException, IllegalArgumentException {
        String[] provides;
        // Token provides
        String providesS = attr.getValue("OpenIDE-Module-Provides"); // NOI18N
        if (providesS == null) {
            provides = ZERO_STRING_ARRAY;
        } else {
            StringTokenizer tok = new StringTokenizer(providesS, ", "); // NOI18N
            provides = new String[tok.countTokens()];
            for (int i = 0; i < provides.length; i++) {
                String provide = tok.nextToken();
                if (provide.indexOf(',') != -1) {
                    throw new InvalidException("Illegal code name syntax parsing OpenIDE-Module-Provides: " + provide); // NOI18N
                }
                if (verifyCNBs) {
                    Dependency.create(Dependency.TYPE_MODULE, provide);
                }
                if (provide.lastIndexOf('/') != -1) throw new IllegalArgumentException("Illegal OpenIDE-Module-Provides: " + provide); // NOI18N
                provides[i] = provide;
            }
            if (new HashSet<String>(Arrays.asList(provides)).size() < provides.length) {
                throw new IllegalArgumentException("Duplicate entries in OpenIDE-Module-Provides: " + providesS); // NOI18N
            }
        }
        String[] additionalProvides = forModule.getManager().refineProvides (forModule);
        if (additionalProvides != null) {
            if (provides == null) {
                provides = additionalProvides;
            } else {
                ArrayList<String> l = new ArrayList<String> ();
                l.addAll (Arrays.asList (provides));
                l.addAll (Arrays.asList (additionalProvides));
                provides = l.toArray (provides);
            }
        }
        return provides;
    }
    
    /**
     * Initializes dependencies of this module
     *
     * @param knownDeps Set<Dependency> of this module known from different
     * source, can be null
     * @param attr attributes in manifest to parse if knownDeps is null
     */
    private Dependency[] initDeps(Module forModule, Set<?> knownDeps, Attributes attr)
        throws IllegalStateException, IllegalArgumentException {
        if (knownDeps != null) {
            return knownDeps.toArray(new Dependency[knownDeps.size()]);
        }

        // deps
        Set<Dependency> deps = new HashSet<Dependency>(20);
        // First convert IDE/1 -> org.openide/1, so we never have to deal with
        // "IDE deps" internally:
        @SuppressWarnings(value = "deprecation")
        Set<Dependency> openideDeps = Dependency.create(Dependency.TYPE_IDE, attr.getValue("OpenIDE-Module-IDE-Dependencies")); // NOI18N
        if (!openideDeps.isEmpty()) {
            // If empty, leave it that way; NbInstaller will add it anyway.
            Dependency d = openideDeps.iterator().next();
            String name = d.getName();
            if (!name.startsWith("IDE/")) {
                throw new IllegalStateException("Weird IDE dep: " + name); // NOI18N
            }
            deps.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide/" + name.substring(4) + " > " + d.getVersion())); // NOI18N
            if (deps.size() != 1) {
                throw new IllegalStateException("Should be singleton: " + deps); // NOI18N
            }
            Util.err.log(Level.WARNING, "the module {0} uses OpenIDE-Module-IDE-Dependencies which is deprecated. See http://openide.netbeans.org/proposals/arch/modularize.html", codeNameBase); // NOI18N
        }
        deps.addAll(Dependency.create(Dependency.TYPE_JAVA, attr.getValue("OpenIDE-Module-Java-Dependencies"))); // NOI18N
        deps.addAll(Dependency.create(Dependency.TYPE_MODULE, attr.getValue("OpenIDE-Module-Module-Dependencies"))); // NOI18N
        String pkgdeps = attr.getValue("OpenIDE-Module-Package-Dependencies"); // NOI18N
        if (pkgdeps != null) {
            // XXX: Util.err.log(ErrorManager.WARNING, "Warning: module " + codeNameBase + " uses the OpenIDE-Module-Package-Dependencies 
            // manifest attribute, which is now deprecated: XXX URL TBD");
            deps.addAll(Dependency.create(Dependency.TYPE_PACKAGE, pkgdeps)); // NOI18N
        }
        deps.addAll(Dependency.create(Dependency.TYPE_REQUIRES, attr.getValue("OpenIDE-Module-Requires"))); // NOI18N
        deps.addAll(Dependency.create(Dependency.TYPE_NEEDS, attr.getValue("OpenIDE-Module-Needs"))); // NOI18N
        deps.addAll(Dependency.create(Dependency.TYPE_RECOMMENDS, attr.getValue("OpenIDE-Module-Recommends"))); // NOI18N
        forModule.refineDependencies(deps);
        return deps.toArray(new Dependency[0]);
    }

    final String getCodeName() {
        return codeName;
    }
    
    final String getCodeNameBase() {
        return codeNameBase;
    }

    final int getCodeNameRelease() {
        return codeNameRelease;
    }

    final String[] getProvides() {
        return provides;
    }

    final SpecificationVersion getSpecificationVersion() {
        return specVers;
    }

    final PackageExport[] getPublicPackages() {
        return publicPackages;
    }

    final Set<String> getFriendNames() {
        return friendNames;
    }

    final Dependency[] getDependencies() {
        return dependencies;
    }

    final String getBuildVersion() {
        return buildVersion.isEmpty() ? null : buildVersion;
    }

    final String getImplementationVersion() {
        return implVersion.isEmpty() ? null : implVersion;
    }
    
    void registerCoveredPackages(Set<String> known) {
        assert coveredPackages.isEmpty();
        coveredPackages.addAll(known);
    }

    Set<String> getCoveredPackages() {
        return coveredPackages.isEmpty() ? null : coveredPackages;
    }

    private <T extends Collection<String>> T readStrings(
        DataInput dis, T set, boolean returnEmpty
    ) throws IOException {
        int cnt = dis.readInt();
        if (!returnEmpty && cnt == 0) {
            return null;
        }
        while (cnt-- > 0) {
            set.add(dis.readUTF());
        }
        return set;
    }
    private String[] readStrings(ObjectInput dis) throws IOException {
        List<String> arr = new ArrayList<String>();
        readStrings(dis, new HashSet<String>(), false);
        return arr.toArray(new String[0]);
    }
    private void writeStrings(DataOutput dos, Collection<String> set) 
    throws IOException {
        if (set == null) {
            dos.writeInt(0);
            return;
        }
        dos.writeInt(set.size());
        for (String s : set) {
            dos.writeUTF(s);
        }
    }
    private void writeStrings(ObjectOutput dos, String[] provides) throws IOException {
        writeStrings(dos, Arrays.asList(provides));
    }

}
