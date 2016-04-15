/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.launchers.actions.ui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.launchers.LaunchersProjectMetadataFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander Simon
 */
public class LaunchersConfig {

    private static final String COMMON = "common"; //NOI18N
    private static final String LAUNCHER = "launcher"; //NOI18N
    private static final String DISPLAY_NAME = "displayName"; //NOI18N
    private static final String RUN_COMMAND = "runCommand"; //NOI18N
    private static final String BUILD_COMMAND = "buildCommand"; //NOI18N
    private static final String RUN_DIR = "runDir"; //NOI18N
    private static final String SYMBOL_FILES = "symbolFiles"; //NOI18N
    private static final String ENV = "env"; //NOI18N

    private final Project project;
    private final TreeMap<Integer, LauncherConfig> map = new TreeMap<>();
    private final ArrayList<String> commentsPublic = new ArrayList<>();
    private final ArrayList<String> commentsPrivate = new ArrayList<>();

    public LaunchersConfig(Project project) {
        this.project = project;
    }

    public TreeMap<Integer, LauncherConfig> getLaunchers() {
        return map;
    }

    public void load() {
        FileObject projectDirectory = project.getProjectDirectory();
        final FileObject nbProjectFolder = projectDirectory.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbProjectFolder == null || !nbProjectFolder.isValid()) {  // LaunchersRegistry shouldn't be updated in case the project has been deleted.
            return;
        }
        final FileObject publicLaunchers = nbProjectFolder.getFileObject(LaunchersProjectMetadataFactory.NAME);
        final FileObject privateNbFolder = projectDirectory.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
        final FileObject privateLaunchers;
        if (privateNbFolder != null && privateNbFolder.isValid()) {
            privateLaunchers = privateNbFolder.getFileObject(LaunchersProjectMetadataFactory.NAME);
        } else {
            privateLaunchers = null;
        }
        map.put(-1, new LauncherConfig(-1, true));
        map.put(0, new LauncherConfig(-1, false));
        if (publicLaunchers != null && publicLaunchers.isValid()) {
            load(publicLaunchers, true);
        }
        if (privateLaunchers != null && privateLaunchers.isValid()) {
            load(privateLaunchers, false);
        }
    }

    private void load(FileObject config, boolean pub) {
        try {
            int id = pub ? -1 : 0;
            LauncherConfig l = map.get(id);
            if (l == null) {
                l = new LauncherConfig(id, pub);
                map.put(id, l);
            }
            List<String> asLines = new ArrayList<>(config.asLines("UTF-8")); //NOI18N
            Iterator<String> it = asLines.iterator();
            boolean initComments = true;
            while (it.hasNext()) {
                String line = it.next();
                int i = line.indexOf('#');
                if (i >= 0) {
                    if (i == 0) {
                        if (initComments) {
                            if (pub) {
                                commentsPublic.add(line);
                            } else {
                                commentsPrivate.add(line);
                            }
                        }
                        continue;
                    }
                    line = line.substring(0, i);
                }
                line = line.replace('\t', ' ');
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                while (it.hasNext() && line.endsWith("\\")) { //NOI18N
                    String next = it.next();
                    next = next.replace('\t', ' ');
                    line = line.substring(0, line.length() - 1) + next;
                }
                i = line.indexOf('=');
                if (i > 0) {
                    String key = line.substring(0, i).trim();
                    String value = line.substring(i + 1).trim();
                    add(key, value, pub);

                }
            }

        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private void add(String key, String value, boolean pub) {
        if (key.startsWith(COMMON + ".")) { //NOI18N
            int id = pub ? -1 : 0;
            LauncherConfig l = map.get(id);
            if (l == null) {
                l = new LauncherConfig(id, pub);
                map.put(id, l);
            }
            String subkey = key.substring(7);
            if (subkey.equals(RUN_DIR)) {
                l.runDir = value;
            } else if (subkey.equals(SYMBOL_FILES)) {
                l.symbolFiles = value;
            } else if (subkey.startsWith(ENV + ".")) { //NOI18N
                String var = subkey.substring(4);
                l.env.put(var, value);
            }
        } else if (key.startsWith(LAUNCHER)) {
            int i = key.indexOf('.');
            if (i > 0) {
                try {
                    int id = Integer.parseInt(key.substring(8, i));
                    LauncherConfig l = map.get(id);
                    if (l == null) {
                        l = new LauncherConfig(id, pub);
                        map.put(id, l);
                    }
                    String subkey = key.substring(i + 1);
                    if (subkey.equals(DISPLAY_NAME)) {
                        l.name = value;
                    } else if (subkey.equals(RUN_COMMAND)) {
                        l.command = value;
                    } else if (subkey.equals(BUILD_COMMAND)) {
                        l.buildCommand = value;
                    }  else if (subkey.equals(RUN_DIR)) {
                        l.runDir = value;
                    } else if (subkey.equals(SYMBOL_FILES)) {
                        l.symbolFiles = value;
                    } else if (subkey.startsWith(ENV + ".")) { //NOI18N
                        String var = subkey.substring(4);
                        l.env.put(var, value);
                    }
                } catch (NumberFormatException ex) {
                    // skip
                }
            }
        }
    }

    public void save(ArrayList<LauncherConfig> launchers) {
        int max = -2;
        boolean monotonius = true;
        for (LauncherConfig l : launchers) {
            if (l.id > max) {
                max = l.id;
                continue;
            } else {
                monotonius = false;
                break;
            }
        }
        if (!monotonius) {
            // need to reorder IDs
            int i = 0;
            for (LauncherConfig l : launchers) {
                if (l.id > 0) {
                    if (l.pub) {
                        i = (i + 1000) / 1000;
                        i = i * 1000;
                        l.id = i;
                    } else {
                        i = (i + 10) / 10;
                        i = i * 10;
                        l.id = i;
                    }
                }
            }
        }
        //
        boolean hasPrivateConfig = false;
        boolean hasPublicConfig = false;
        for (LauncherConfig l : launchers) {
            if (l.id > 0) {
                if (l.pub) {
                    hasPublicConfig |= !l.name.isEmpty() || !l.command.isEmpty() || l.buildCommand.isEmpty() || !l.env.isEmpty() || !l.runDir.isEmpty() || !l.symbolFiles.isEmpty();
                } else {
                    hasPrivateConfig |= !l.name.isEmpty() || !l.command.isEmpty() || l.buildCommand.isEmpty() || !l.env.isEmpty() || !l.runDir.isEmpty() || !l.symbolFiles.isEmpty();
                }
            } else {
                if (l.pub) {
                    hasPublicConfig |= !l.env.isEmpty() || !l.runDir.isEmpty() || !l.symbolFiles.isEmpty();
                } else {
                    hasPrivateConfig |= !l.env.isEmpty() || !l.runDir.isEmpty() || !l.symbolFiles.isEmpty();
                }
            }
        }
        FileObject projectDirectory = project.getProjectDirectory();
        final FileObject nbProjectFolder = projectDirectory.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbProjectFolder == null || !nbProjectFolder.isValid()) {  // LaunchersRegistry shouldn't be updated in case the project has been deleted.
            return;
        }
        FileObject privateNbFolder = projectDirectory.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
        FileObject privateLaunchers = null;
        if (privateNbFolder != null && privateNbFolder.isValid()) {
            privateLaunchers = privateNbFolder.getFileObject(LaunchersProjectMetadataFactory.NAME);
            if (hasPrivateConfig) {
                // save private configuration
                if (privateLaunchers == null || !privateLaunchers.isValid()) {
                    try {
                        privateLaunchers = privateNbFolder.createData(LaunchersProjectMetadataFactory.NAME);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                save(launchers, privateLaunchers, false);
            } else {
                if (privateLaunchers != null && privateLaunchers.isValid()) {
                    // save comments if private config exists
                    save(launchers, privateLaunchers, false);
                }
            }
        }

        FileObject publicLaunchers = nbProjectFolder.getFileObject(LaunchersProjectMetadataFactory.NAME);
        if (hasPublicConfig) {
            // save public configuration
            if (publicLaunchers == null || !publicLaunchers.isValid()) {
                try {
                    publicLaunchers = nbProjectFolder.createData(LaunchersProjectMetadataFactory.NAME);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            save(launchers, publicLaunchers, true);
        } else {
            // save comments if public configuration exists
            if (publicLaunchers != null && publicLaunchers.isValid()) {
                save(launchers, publicLaunchers, true);
            }
        }
    }

    private void save(ArrayList<LauncherConfig> launchers, FileObject config, boolean pub) {
        if (config == null) {
            return;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(config.getOutputStream(), "UTF-8")); //NOI18N
            ArrayList<String> c = pub ? commentsPublic : commentsPrivate;
            for (String s : c) {
                bw.write(s);
                bw.newLine();
            }
            for (LauncherConfig l : launchers) {
                if (l.pub == pub) {
                    if (l.id <= 0) {
                        if (!l.runDir.isEmpty()) {
                            bw.write(COMMON + "."); //NOI18N
                            bw.write(RUN_DIR);
                            bw.write("="+l.runDir); //NOI18N
                            bw.newLine();
                        }
                        if (!l.symbolFiles.isEmpty()) {
                            bw.write(COMMON + "."); //NOI18N
                            bw.write(SYMBOL_FILES);
                            bw.write("="+l.symbolFiles); //NOI18N
                            bw.newLine();
                        }
                        for(Map.Entry<String,String> e : l.env.entrySet()) {
                            bw.write(COMMON + "."); //NOI18N
                            bw.write(ENV+"."); //NOI18N
                            bw.write(e.getKey()+"="); //NOI18N
                            bw.write(e.getValue());
                            bw.newLine();
                        }
                    } else {
                        if (!l.name.isEmpty()) {
                            bw.write(LAUNCHER + l.id + "."); //NOI18N
                            bw.write(DISPLAY_NAME);
                            bw.write("="+l.name); //NOI18N
                            bw.newLine();
                        }
                        if (!l.command.isEmpty()) {
                            StringBuilder buf = new StringBuilder();
                            buf.append(LAUNCHER + l.id + "."); //NOI18N
                            buf.append(RUN_COMMAND);
                            buf.append("="+l.command); //NOI18N
                            writeWrapLine(buf.toString(), bw);
                        }
                        if (!l.buildCommand.isEmpty()) {
                            bw.write(LAUNCHER + l.id + "."); //NOI18N
                            bw.write(BUILD_COMMAND);
                            bw.write("="+l.buildCommand); //NOI18N
                            bw.newLine();
                        }
                        if (!l.runDir.isEmpty()) {
                            bw.write(LAUNCHER + l.id + "."); //NOI18N
                            bw.write(RUN_DIR);
                            bw.write("="+l.runDir); //NOI18N
                            bw.newLine();
                        }
                        if (!l.symbolFiles.isEmpty()) {
                            bw.write(LAUNCHER + l.id + "."); //NOI18N
                            bw.write(SYMBOL_FILES);
                            bw.write("="+l.symbolFiles); //NOI18N
                            bw.newLine();
                        }
                        for(Map.Entry<String,String> e : l.env.entrySet()) {
                            bw.write(LAUNCHER + l.id + "."); //NOI18N
                            bw.write(ENV+"."); //NOI18N
                            bw.write(e.getKey()+"="); //NOI18N
                            bw.write(e.getValue());
                            bw.newLine();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private void writeWrapLine(String s, BufferedWriter bw) throws IOException {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == ' ' && buf.length() > 80) {
                buf.append("\\");
                bw.write(buf.toString());
                bw.newLine();
                buf.setLength(0);
            }
            buf.append(c);
        }
        bw.write(buf.toString());
        bw.newLine();
    }

    public static final class LauncherConfig {

        private int id;
        private boolean pub;
        private String name = "";
        private String command = "";
        private String buildCommand = "";
        private String runDir = "";
        private String symbolFiles = "";
        private Map<String, String> env = new HashMap<>();
        private boolean isModified = false;

        public LauncherConfig(int id, boolean pub) {
            this.id = id;
            this.pub = pub;
        }

        public boolean isModified() {
            return isModified;
        }

        public int getID() {
            return id;
        }

        public boolean getPublic() {
            return pub;
        }

        /*package*/ void setPublic(boolean pub) {
            isModified |= pub != this.pub;
            this.pub = pub;
        }

        public String getName() {
            return name;
        }

        /*package*/ void setName(String name) {
            isModified |= !name.equals(this.name);
            this.name = name;
        }

        public String getCommand() {
            return command;
        }

        /*package*/ void setCommand(String command) {
            isModified |= !command.equals(this.command);
            this.command = command;
        }

        public String getBuildCommand() {
            return buildCommand;
        }

        public void setBuildCommand(String buildCommand) {
            isModified |= !buildCommand.equals(this.buildCommand);
            this.buildCommand = buildCommand;
        }

        public String getRunDir() {
            return runDir;
        }

        /*package*/ void setRunDir(String runDir) {
            isModified |= !runDir.equals(this.runDir);
            this.runDir = runDir;
        }

        public Map<String, String> getEnv() {
            return env;
        }

        /*package*/ void putEnv(String key, String value) {
            env.put(key, value);
        }

        public String getSymbolFiles() {
            return symbolFiles;
        }

        /*package*/ void setSymbolFiles(String symbolFiles) {
            this.symbolFiles = symbolFiles;
        }

        public String getDisplayedName() {
            return (name == null || name.isEmpty() ? command : name);
        }

        public LauncherConfig copy(int newID) {
            LauncherConfig res = new LauncherConfig(newID, pub);
            res.command = this.command;
            res.name = this.name;
            res.buildCommand = this.buildCommand;
            res.runDir = this.runDir;
            res.symbolFiles = this.symbolFiles;
            res.env = new HashMap<>(env);
            return res;
        }

        @Override
        public String toString() {
            return getDisplayedName();
        }

    }
}
