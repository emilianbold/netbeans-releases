/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.codeviation.pojson.PojsonLoad;
import org.codeviation.pojson.PojsonSave;
import org.netbeans.modules.kenai.ProjectData;
import org.netbeans.modules.kenai.util.Utils;

/**
 * Provides persistence services to Kenai module.
 *
 * @author Maros Sandor
 */
class Persistence {

    private static final Persistence instance = new Persistence();

    static Persistence getInstance() {
        return instance;
    }

    private Persistence() {
    }

    public synchronized boolean storeProjects(Collection<KenaiProject> projects) {
        File master = getMasterStorage();
        int idx = 0;
        for (KenaiProject project : projects) {
            String projectFileName = computeProjectFileName(project.getName()) + "-" + idx + ".kp";
            File file = new File(master, projectFileName);
            try {
                storeProject(file, project);
            } catch (IOException iOException) {
                Utils.logWarn(this, iOException);
                file.delete();
            }
        }
        return true;
    }

    public synchronized Collection<KenaiProject> loadProjects() {
        List<KenaiProject> projects = new ArrayList();

        File master = getMasterStorage();
        File [] projectFiles = master.listFiles();
        if (projectFiles != null) {
            PojsonLoad load = new PojsonLoad();
            for (File file : projectFiles) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    ProjectData data = load.load(fis, ProjectData.class);
                    fis.close();
                    //TODO: pass kenai instance
                    projects.add(KenaiProject.get(null, data));
                } catch (IOException iOException) {
                    Utils.logWarn(this, iOException);
                    file.delete();
                }
            }
        }
        return projects;
    }

    private void storeProject(File file, KenaiProject project) throws IOException {
        ProjectData data = project.getData();
        PojsonSave save = new PojsonSave();

        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        save.save(writer, data);
        writer.close();
    }

    private String computeProjectFileName(String name) {
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') {
                sb.append(c);
            } else {
                sb.append('-');
            }
        }
        return sb.toString();
    }

    private File getMasterStorage() {
        File masterStorage;
        String homeDir = System.getProperty("user.home"); // NOI18N
        if (homeDir == null) {
            homeDir = System.getProperty("netbeans.user"); // NOI18N
            masterStorage = new File(homeDir);
        } else {
            masterStorage = new File(new File(homeDir), "NetBeansKenai");
        }
        File store = new File(new File(masterStorage, "config"), "Kenai"); // NOI18N
        store.mkdirs();
        return store;
    }
}
