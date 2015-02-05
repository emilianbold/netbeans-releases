/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.ui.customizer;

import java.io.IOException;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2me.project.J2MEProject;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Roman Svitanic
 */
public class LibletInfo {

    public enum Requirement {

        OPTIONAL, REQUIRED
    }

    public enum LibletType {

        LIBLET, STANDARD, SERVICE, PROPRIETARY
    }

    private ClassPathSupport.Item item;
    private Requirement requirement;
    private LibletType type;
    private String url;
    private String name;
    private String vendor;
    private String version;
    private boolean extractClasses;

    private LibletInfo(ClassPathSupport.Item item, String name, String vendor, String version) {
        this.item = item;
        this.name = name;
        this.vendor = vendor;
        this.version = version;
        this.type = LibletType.LIBLET;
        this.requirement = Requirement.OPTIONAL;
        this.url = ""; //NOI18N
        this.extractClasses = false;
    }

    public LibletInfo(LibletType type, String name, String vendor, String version, Requirement requirement, String url, boolean extractClasses) {
        this.type = type;
        this.name = name;
        this.vendor = vendor;
        this.version = version;
        this.requirement = requirement;
        this.item = null;
        this.url = url;
        this.extractClasses = extractClasses;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LibletInfo) {
            LibletInfo lib = (LibletInfo) obj;
            return this.type == lib.getType()
                    && this.name.equals(lib.getName())
                    && this.vendor.equals(lib.getVendor());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.type);
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + Objects.hashCode(this.vendor);
        return hash;
    }

    public static LibletInfo createLibletInfoForJar(ClassPathSupport.Item item) {
        LibletInfo li = null;
        try {
            JarFile jar = new JarFile(item.getResolvedFile());
            Attributes manifestAttributes = jar.getManifest().getMainAttributes();
            String name = manifestAttributes.getValue("LIBlet-Name"); //NOI18N
            String vendor = manifestAttributes.getValue("LIBlet-Vendor"); //NOI18N
            String version = manifestAttributes.getValue("LIBlet-Version"); //NOI18N
            li = new LibletInfo(item, name, vendor, version);
            FileObject jarFO = FileUtil.toFileObject(item.getResolvedFile());
            if (jarFO.existsExt("jad")) { //NOI18N
                li.setUrl("lib/" + jarFO.getName() + ".jad"); //NOI18N
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return li;
    }

    public static LibletInfo createLibletInfoForProject(ClassPathSupport.Item item) {
        LibletInfo li = null;
        AntArtifact artifact = item.getArtifact();
        if (artifact != null) {
            Project project = artifact.getProject();
            if (project != null && project instanceof J2MEProject) {
                J2MEProject j2meProj = (J2MEProject) project;
                String propVal = j2meProj.evaluator().getProperty("manifest.is.liblet"); //NOI18N
                if (propVal != null) {
                    boolean liblet = Boolean.parseBoolean(propVal);
                    if (liblet) {
                        String libletDetails = j2meProj.evaluator().getProperty("manifest.others"); //NOI18N
                        if (libletDetails == null) {
                            return null;
                        }
                        String splittedDetails[] = libletDetails.split("\n"); //NOI18N
                        String name = "", vendor = "", version = ""; //NOI18N
                        for (String s : splittedDetails) {
                            if (s.startsWith("LIBlet-Name:")) { //NOI18N
                                name = s.substring(s.indexOf(":") + 1).trim(); //NOI18N
                            } else if (s.startsWith("LIBlet-Vendor:")) { //NOI18N
                                vendor = s.substring(s.indexOf(":") + 1).trim(); //NOI18N
                            }
                            if (s.startsWith("LIBlet-Version:")) { //NOI18N
                                version = s.substring(s.indexOf(":") + 1).trim(); //NOI18N
                            }
                        }
                        li = new LibletInfo(item, name, vendor, version);
                        String jadName = j2meProj.evaluator().getProperty("dist.jad"); //NOI18N
                        if (jadName != null) {
                            li.setUrl("lib/" + jadName);
                        }
                    }
                }
            }
        }
        return li;
    }

    public ClassPathSupport.Item getItem() {
        return item;
    }

    public void setItem(ClassPathSupport.Item item) {
        this.item = item;
    }

    public LibletType getType() {
        return type;
    }

    public void setType(LibletType type) {
        this.type = type;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isExtractClasses() {
        return extractClasses;
    }

    public void setExtractClasses(boolean extractClasses) {
        this.extractClasses = extractClasses;
    }
}
