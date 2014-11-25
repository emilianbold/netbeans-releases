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
package org.netbeans.modules.web.clientproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.createprojectapi.ClientSideProjectGenerator;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

@ProjectConvertor.Registration(requiredPattern = "(bower|package)\\.json"/*, position = 1000*/) // XXX failing test
public final class ClientSideProjectConvertor implements ProjectConvertor {

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectConvertor.class.getName());


    @Override
    public Result isProject(FileObject projectDirectory) {
        assert projectDirectory != null;
        final FileObject jsonFile = getJsonFile(projectDirectory);
        assert jsonFile != null : projectDirectory;
        String displayName = getDisplayName(jsonFile);
        if (displayName == null) {
            // should not happen often
            displayName = projectDirectory.getNameExt();
        }
        return new Result(
                Lookup.EMPTY,
                new Factory(projectDirectory, displayName),
                displayName,
                ImageUtilities.image2Icon(ImageUtilities.loadImage(ClientSideProject.HTML5_PROJECT_ICON)));
    }

    private FileObject getJsonFile(FileObject projectDirectory) {
        // prefer package.json
        FileObject jsonFile = projectDirectory.getFileObject("package.json"); // NOI18N
        if (jsonFile != null) {
            return jsonFile;
        }
        return projectDirectory.getFileObject("bower.json"); // NOI18N
    }

    @CheckForNull
    private String getDisplayName(FileObject jsonFile) {
        assert jsonFile != null;
        JSONParser parser = new JSONParser();
        try (Reader reader = new BufferedReader(new InputStreamReader(jsonFile.getInputStream(), StandardCharsets.UTF_8))) {
            JSONObject content = (JSONObject) parser.parse(reader);
            Object name = content.get("name"); // NOI18N
            if (name instanceof String) {
                return (String) name;
            }
        } catch (ParseException | IOException ex) {
            LOGGER.log(Level.INFO, jsonFile.getPath(), ex);
        }
        return null;
    }

    //~ Inner classes

    private static final class Factory implements Callable<Project> {

        private static final String[] KNOWN_SITE_ROOTS = new String[] {
            "public", // NOI18N
            "app", // NOI18N
            "web", // NOI18N
            "www", // NOI18N
            "public_html", // NOI18N
        };

        private final FileObject projectDirectory;
        private final String displayName;


        Factory(FileObject projectDirectory, String displayName) {
            assert projectDirectory != null;
            assert displayName != null : projectDirectory;
            this.projectDirectory = projectDirectory;
            this.displayName = displayName;
        }

        @Override
        public Project call() throws Exception {
            return ClientSideProjectGenerator.createProject(new CreateProjectProperties()
                    .setProjectDir(projectDirectory)
                    .setProjectName(displayName)
                    .setSourceFolder("") // NOI18N
                    .setSiteRootFolder(detectSiteRoot())
                    .setAutoconfigured(true));
        }

        private String detectSiteRoot() {
            for (String dir : KNOWN_SITE_ROOTS) {
                if (projectDirectory.getFileObject(dir) != null)  {
                    return dir;
                }
            }
            return ""; // NOI18N
        }

    }

}
