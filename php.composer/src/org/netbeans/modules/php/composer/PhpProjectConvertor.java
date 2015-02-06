/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.composer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

@ProjectConvertor.Registration(requiredPattern = PhpProjectConvertor.COMPOSER_JSON_FILENAME, position = 500)
public final class PhpProjectConvertor implements ProjectConvertor {

    private static final Logger LOGGER = Logger.getLogger(PhpProjectConvertor.class.getName());

    static final String COMPOSER_JSON_FILENAME = "composer.json"; // NOI18N
    @StaticResource
    private static final String PROJECT_ICON = "org/netbeans/modules/php/composer/ui/resources/phpProject.png"; // NOI18N


    @Override
    public Result isProject(FileObject projectDirectory) {
        assert projectDirectory != null;
        FileObject file = projectDirectory.getFileObject(COMPOSER_JSON_FILENAME);
        assert file != null : projectDirectory;
        String displayName = getDisplayName(file);
        if (!StringUtils.hasText(displayName)) {
            // should not happen often
            displayName = projectDirectory.getNameExt();
        }
        final TransientLookup transientLkp = new TransientLookup(
            new ConvertorClassPathProvider(),
            new ConvertorFileEncodingQuery());
        return new Result(
                transientLkp,
                new Factory(projectDirectory, displayName, transientLkp),
                displayName,
                ImageUtilities.image2Icon(ImageUtilities.loadImage(PROJECT_ICON)));
    }

    @CheckForNull
    private String getDisplayName(FileObject jsonFile) {
        assert jsonFile != null;
        JSONParser parser = new JSONParser();
        try (Reader reader = new BufferedReader(new InputStreamReader(jsonFile.getInputStream(), StandardCharsets.UTF_8))) {
            JSONObject content = (JSONObject) parser.parse(reader);
            Object name = content.get("name"); // NOI18N
            if (name instanceof String) {
                String fullName = (String) name;
                String[] parts = fullName.split("/", 2); // NOI18N
                if (parts.length == 2) {
                    return parts[1];
                }
                return fullName;
            }
        } catch (ParseException | IOException ex) {
            LOGGER.log(Level.INFO, jsonFile.getPath(), ex);
        }
        return null;
    }

    //~ Inner classes

    private static final class Factory implements Callable<Project> {

        private static final String[] KNOWN_SOURCE_ROOTS = new String[] {
            "src", // NOI18N
            "lib", // NOI18N
        };

        private final FileObject projectDirectory;
        private final String displayName;
        private final TransientLookup transientLkp;


        Factory(FileObject projectDirectory, String displayName, TransientLookup transientLkp) {
            assert projectDirectory != null;
            assert displayName != null : projectDirectory;
            assert transientLkp != null : projectDirectory;
            this.projectDirectory = projectDirectory;
            this.displayName = displayName;
            this.transientLkp = transientLkp;
        }

        @Override
        public Project call() throws Exception {
            transientLkp.hide(
                ConvertorClassPathProvider.class,
                ConvertorFileEncodingQuery.class);
            PhpModuleGenerator phpModuleGenerator = Lookup.getDefault().lookup(PhpModuleGenerator.class);
            assert phpModuleGenerator != null;
            phpModuleGenerator.createModule(new PhpModuleGenerator.CreateProperties()
                    .setName(displayName)
                    .setProjectDirectory(FileUtil.toFile(projectDirectory))
                    .setSourcesDirectory(FileUtil.toFile(detectSourceRoot()))
                    .setPhpVersion(PhpVersion.getDefault())
                    .setCharset(StandardCharsets.UTF_8));
            Project project = FileOwnerQuery.getOwner(projectDirectory);
            assert project != null : projectDirectory;
            return project;
        }

        private FileObject detectSourceRoot() {
            for (String dir : KNOWN_SOURCE_ROOTS) {
                FileObject srcDir = projectDirectory.getFileObject(dir);
                if (srcDir != null)  {
                    return srcDir;
                }
            }
            return projectDirectory;
        }

    }

    private static final class TransientLookup extends ProxyLookup {

        private final Lookup base;

        public TransientLookup(Object... services) {
            this(Lookups.fixed(services));
        }

        private TransientLookup(Lookup base) {
            super(base);
            this.base = base;
        }

        void hide(Class<?>... clzs) {
            setLookups(Lookups.exclude(base, clzs));
        }
    }

    private static class ConvertorClassPathProvider implements ClassPathProvider {

        @CheckForNull
        @Override
        public ClassPath findClassPath(@NonNull final FileObject file, @NonNull final String type) {
            if (PhpSourcePath.BOOT_CP.equals(type)
                    || PhpSourcePath.PROJECT_BOOT_CP.equals(type)
                    || PhpSourcePath.SOURCE_CP.equals(type)) {
                Project phpProject = findPhpProject(file);
                if (phpProject != null) {
                    return phpProject.getLookup()
                            .lookup(ClassPathProvider.class)
                            .findClassPath(file, type);
                }
            }
            return null;
        }
    }

    private static class ConvertorFileEncodingQuery extends FileEncodingQueryImplementation {

        @CheckForNull
        @Override
        public Charset getEncoding(FileObject file) {
            final Project phpProject = findPhpProject(file);
            if (phpProject != null) {
                return phpProject.getLookup()
                        .lookup(FileEncodingQueryImplementation.class)
                        .getEncoding(file);
            }
            return null;
        }
    }

    @CheckForNull
    private static Project findPhpProject(@NonNull final FileObject file) {
        for (FileObject parent = file.getParent(); parent != null; parent = parent.getParent()) {
            PhpModule phpModule = PhpModule.Factory.forFileObject(parent);
            if (phpModule != null) {
                Project project = FileOwnerQuery.getOwner(phpModule.getProjectDirectory());
                assert project != null : phpModule.getProjectDirectory();
                return project;
            }
        }
        return null;
    }

}
