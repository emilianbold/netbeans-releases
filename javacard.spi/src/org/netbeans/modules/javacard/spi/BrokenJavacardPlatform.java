/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.spi;

import java.util.Properties;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup.Provider;
import org.openide.util.NbBundle;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.Lookup;

/**
 * Dummy JavacardPlatform which can be used for cases where something
 * references a JavacardPlatform by name, but the platform has been deleted.
 * Always returns false from isValid().
 * <p/>
 * Use this where a platform cannot be found, but null is not allowed.
 *
 * @author Tim Boudreau
 */
final class BrokenJavacardPlatform extends JavacardPlatform {
    private final String displayName;
    private final Collection<? extends String> cardNames;
    BrokenJavacardPlatform(String displayName) {
        this.displayName = displayName;
        this.cardNames = Collections.<String>emptyList();
    }

    /**
     * Create a new dummy platform
     * @param displayName The ID/display name of the platform
     * @param cardNames The names of any cards that are expected to be present
     */
    BrokenJavacardPlatform(String displayName, Collection<? extends String> cardNames) {
        this.displayName = displayName;
        this.cardNames = cardNames;
    }

    @Override
    public ClassPath getBootstrapLibraries(ProjectKind kind) {
        return ClassPathSupport.createClassPath(new FileObject[0]);
    }

    @Override
    public ClassPath getProcessorClasspath(ProjectKind kind) {
        return ClassPathSupport.createClassPath(new FileObject[0]);
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public ClassPath getBootstrapLibraries() {
        return ClassPathSupport.createClassPath(""); //NOI18N
    }

    @Override
    public ClassPath getStandardLibraries() {
        return ClassPathSupport.createClassPath(new FileObject[0]);
    }

    @Override
    public String getVendor() {
        return NbBundle.getMessage(BrokenJavacardPlatform.class,
                "MSG_UNKNOWN_VENDOR"); //NOI18N
    }

    @Override
    public Specification getSpecification() {
        return new Specification ("JCRE", //NOI18N
                new SpecificationVersion("1.6")); //NOI18N
    }

    @Override
    public Collection<FileObject> getInstallFolders() {
        String prop = System.getProperty("java.home"); //NOI18N
        File f = FileUtil.normalizeFile (new File (prop));
        return Collections.<FileObject>singleton(FileUtil.toFileObject(f));
    }

    @Override
    public FileObject findTool(String toolName) {
        return null;
    }

    @Override
    public ClassPath getSourceFolders() {
        return ClassPathSupport.createClassPath(new FileObject[0]);
    }

    @Override
    public List<URL> getJavadocFolders() {
        return Collections.emptyList();
    }

    @Override
    public String getSystemName() {
        return displayName;
    }

    @Override
    public SpecificationVersion getJavacardVersion() {
        return new SpecificationVersion ("9.9"); //NOI18N
    }

    @Override
    public boolean isVersionSupported(SpecificationVersion javacardVersion) {
        return false;
    }

    @Override
    public Properties toProperties() {
        return new Properties();
    }

    @Override
    public Cards getCards() {
        return new CI(this, cardNames);
    }

    @Override
    public String getPlatformKind() {
        return "_NONE"; //NOI18N
    }

    @Override
    public Set<ProjectKind> supportedProjectKinds() {
        return ProjectKind.kindsFor(null, true);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getDisplayName() + "]";
    }

    private static final class CI extends Cards {
        private final JavacardPlatform p;
        Collection<? extends String> names;
        CI(JavacardPlatform p, Collection<? extends String> names) {
            this.p = p;
            this.names = names;
        }

        @Override
        public List<? extends Provider> getCardSources() {
            List<Provider> result = new ArrayList<Lookup.Provider>(names.size());
            for (String n : names) {
                result.add(new BrokenCard(n, p));
            }
            return result;
        }
    }
}
