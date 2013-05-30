/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils.Artifacts;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ItemProperties.LanguageKind;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectImpl;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class AnalyzeExecLog extends BaseDwarfProvider {

    private Map<String, ProviderProperty> myProperties = new LinkedHashMap<String, ProviderProperty>();
    public static final String EXEC_LOG_KEY = "exec-log-file"; // NOI18N
    public static final String EXEC_LOG_PROVIDER_ID = "exec-log"; // NOI18N

    public AnalyzeExecLog() {
        clean();
    }

    @Override
    public final void clean() {
        myProperties.clear();
        myProperties.put(EXEC_LOG_KEY, new ProviderProperty() {

            private String myPath;

            @Override
            public String getName() {
                return i18n("Exec_Log_File_Name"); // NOI18N
            }

            @Override
            public String getDescription() {
                return i18n("Exec_Log_File_Description"); // NOI18N
            }

            @Override
            public Object getValue() {
                return myPath;
            }

            @Override
            public void setValue(Object value) {
                if (value instanceof String) {
                    myPath = (String) value;
                }
            }

            @Override
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.MakeLogFile;
            }
        });
        myProperties.put(RESTRICT_SOURCE_ROOT, new ProviderProperty() {

            private String myPath = "";

            @Override
            public String getName() {
                return i18n("RESTRICT_SOURCE_ROOT"); // NOI18N
            }

            @Override
            public String getDescription() {
                return i18n("RESTRICT_SOURCE_ROOT"); // NOI18N
            }

            @Override
            public Object getValue() {
                return myPath;
            }

            @Override
            public void setValue(Object value) {
                if (value instanceof String) {
                    myPath = (String) value;
                }
            }

            @Override
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.String;
            }
        });
        myProperties.put(RESTRICT_COMPILE_ROOT, new ProviderProperty() {

            private String myPath = "";

            @Override
            public String getName() {
                return i18n("RESTRICT_COMPILE_ROOT"); // NOI18N
            }

            @Override
            public String getDescription() {
                return i18n("RESTRICT_COMPILE_ROOT"); // NOI18N
            }

            @Override
            public Object getValue() {
                return myPath;
            }

            @Override
            public void setValue(Object value) {
                if (value instanceof String) {
                    myPath = (String) value;
                }
            }

            @Override
            public ProviderProperty.PropertyKind getKind() {
                return ProviderProperty.PropertyKind.String;
            }
        });
    }

    @Override
    public String getID() {
        return EXEC_LOG_PROVIDER_ID; // NOI18N
    }

    @Override
    public String getName() {
        return i18n("Exec_Log_Provider_Name"); // NOI18N
    }

    @Override
    public String getDescription() {
        return i18n("Exec_Log_Provider_Description"); // NOI18N
    }

    @Override
    public List<String> getPropertyKeys() {
        return new ArrayList<String>(myProperties.keySet());
    }

    @Override
    public ProviderProperty getProperty(String key) {
        return myProperties.get(key);
    }

    @Override
    public boolean isApplicable(ProjectProxy project) {
        String set = (String) getProperty(EXEC_LOG_KEY).getValue();
        if (set != null && set.length() > 0) {
            return true;
        }
        Object o = getProperty(RESTRICT_COMPILE_ROOT).getValue();
        if (o == null || "".equals(o.toString())) { // NOI18N
            getProperty(RESTRICT_COMPILE_ROOT).setValue(project.getSourceRoot());
            return true;
        }
        return false;
    }

    @Override
    public DiscoveryExtensionInterface.Applicable canAnalyze(ProjectProxy project, Interrupter interrupter) {
        String set = (String) getProperty(EXEC_LOG_KEY).getValue();
        if (set == null || set.length() == 0) {
            return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeExecLog.class, "NotFoundExecLog")));
        }
        return new ApplicableImpl(true, null, null, 80, false, null, null, null, null);
    }

    @Override
    protected List<SourceFileProperties> getSourceFileProperties(String objFileName, Map<String, SourceFileProperties> map, ProjectProxy project, Set<String> dlls, List<String> buildArtifacts, CompileLineStorage storage) {
        ProviderProperty p = getProperty(RESTRICT_COMPILE_ROOT);
        String root = "";
        if (p != null) {
            root = (String) p.getValue();
        }
        List<SourceFileProperties> res = runLogReader(objFileName, root, progress, project, buildArtifacts, storage);
        progress = null;
        return res;

    }
    private List<SourceFileProperties> runLogReader(String objFileName, String root, Progress progress, ProjectProxy project, List<String> buildArtifacts, CompileLineStorage storage) {
        FileSystem fileSystem = getFileSystem(project);
        ExecLogReader reader = new ExecLogReader(objFileName, root, project, getRelocatablePathMapper(), fileSystem);
        List<SourceFileProperties> list = reader.getResults(progress, getStopInterrupter(), storage);
        buildArtifacts.addAll(reader.getArtifacts(progress, getStopInterrupter(), storage));
        return list;
    }
    
    private Progress progress;

    @Override
    public List<Configuration> analyze(final ProjectProxy project, Progress progress, Interrupter interrupter) {
        resetStopInterrupter(interrupter);
        List<Configuration> confs = new ArrayList<Configuration>();
        init(project);
        this.progress = progress;
        if (!getStopInterrupter().cancelled()) {
            Configuration conf = new Configuration() {

                private List<SourceFileProperties> myFileProperties;
                private List<String> myBuildArtifacts;
                private List<String> myIncludedFiles = new ArrayList<String>();

                @Override
                public List<ProjectProperties> getProjectConfiguration() {
                    return ProjectImpl.divideByLanguage(getSourcesConfiguration(), project);
                }

                @Override
                public List<String> getDependencies() {
                    return null;
                }

                @Override
                public List<String> getBuildArtifacts() {
                    if (myBuildArtifacts == null) {
                        myBuildArtifacts = Collections.synchronizedList(new ArrayList<String>());
                        String set = (String) getProperty(EXEC_LOG_KEY).getValue();
                        if (set != null && set.length() > 0) {
                            myFileProperties = getSourceFileProperties(new String[]{set}, null, project, null, myBuildArtifacts, new CompileLineStorage());
                            store(project);
                        }
                    }
                    return myBuildArtifacts;
                }

                @Override
                public List<SourceFileProperties> getSourcesConfiguration() {
                    if (myFileProperties == null) {
                        myBuildArtifacts = Collections.synchronizedList(new ArrayList<String>());
                        String set = (String) getProperty(EXEC_LOG_KEY).getValue();
                        if (set != null && set.length() > 0) {
                            myFileProperties = getSourceFileProperties(new String[]{set}, null, project, null, myBuildArtifacts, new CompileLineStorage());
                            store(project);
                        }
                    }
                    return myFileProperties;
                }

                @Override
                public List<String> getIncludedFiles() {
                    return myIncludedFiles;
                }
            };
            confs.add(conf);
        }
        return confs;
    }

    private static String i18n(String id) {
        return NbBundle.getMessage(AnalyzeExecLog.class, id);
    }

    private static final class ExecLogReader {
        private final String root;
        private final String fileName;
        private List<SourceFileProperties> result;
        private List<String> buildArtifacts;
        private final ProjectProxy project;
        private final PathMap pathMapper;
        private final RelocatablePathMapper localMapper;
        private final FileSystem fileSystem;
        private final CompilerSettings compilerSettings;
        private final Set<String> C_NAMES;
        private final Set<String> CPP_NAMES;
        private final Set<String> FORTRAN_NAMES;
        private final Set<String> LIBREARIES_NAMES;

        private ExecLogReader(String fileName, String root, ProjectProxy project, RelocatablePathMapper relocatablePathMapper, FileSystem fileSystem) {
            this.fileName = fileName;
            this.project = project;
            this.pathMapper = getPathMapper(project);
            this.localMapper = relocatablePathMapper;
            this.fileSystem = fileSystem;
            this.compilerSettings = new CompilerSettings(project);
            C_NAMES = DiscoveryUtils.getCompilerNames(project, PredefinedToolKind.CCompiler);
            CPP_NAMES = DiscoveryUtils.getCompilerNames(project, PredefinedToolKind.CCCompiler);
            FORTRAN_NAMES = DiscoveryUtils.getCompilerNames(project, PredefinedToolKind.FortranCompiler);
            LIBREARIES_NAMES = new HashSet<String>();
            LIBREARIES_NAMES.add("ld"); //NOI18N
            LIBREARIES_NAMES.add("ar"); //NOI18N
            if (project != null && root.isEmpty()) {
                String sourceRoot = project.getSourceRoot();
                if (sourceRoot != null && sourceRoot.length() > 1) {
                    root = sourceRoot;
                }
            }
            if (root.isEmpty()) {
                String sourceRoot = PathUtilities.getDirName(fileName);
                if (sourceRoot != null && sourceRoot.length() > 1) {
                    root = sourceRoot;
                }
            }
            if (root.length() > 0) {
                this.root = CndFileUtils.normalizeFile(new File(root)).getAbsolutePath();
            } else {
                this.root = root;
            }
        }
        
        private PathMap getPathMapper(ProjectProxy project) {
            if (project != null) {
                Project p = project.getProject();
                if (p != null) {                
                    return RemoteSyncSupport.getPathMap(p);
                }
            }
            return null;
        }

        // Exec log format
        //called: /opt/solstudio12.2/bin/cc
        //        /var/tmp/as204739-cnd-test-downloads/pkg-config-0.25/popt
        //        /opt/solstudio12.2/bin/cc
        //        -DHAVE_CONFIG_H
        //        -I.
        //        -I..
        //        -g
        //        -c
        //        findme.c
        //        -o
        //        findme.o
        //

        private void run(Progress progress, Interrupter isStoped, CompileLineStorage storage) {
            result = new ArrayList<SourceFileProperties>();
            buildArtifacts = new ArrayList<String>();
            File file = new File(fileName);
            if (file.exists() && file.canRead()) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(file));
                    long length = file.length();
                    long read = 0;
                    int done = 0;
                    if (length <= 0) {
                        progress = null;
                    }
                    if (progress != null) {
                        progress.start(100);
                    }
                    try {
                        String tool = null;
                        List<String> params = new ArrayList<String>();
                        while (true) {
                            if (isStoped.cancelled()) {
                                break;
                            }
                            String line = in.readLine();
                            if (line == null) {
                                break;
                            }
                            read += line.length() + 1;
                            if (read * 100 / length > done && done < 100) {
                                done++;
                                if (progress != null) {
                                    progress.increment(null);
                                }
                            }
                            if (line.startsWith("called:")) { //NOI18N
                                tool = line.substring(7).trim();
                                continue;
                            }
                            if (line.startsWith("\t")) { //NOI18N
                                params.add(line.substring(1).trim());
                                continue;
                            }
                            if (line.length()==0) {
                                // create new result entry
                                try {
                                    addSources(tool, params, storage);
                                } catch (Throwable ex) {
                                    // ExecSource constructor can throw IllegalArgumentException for non source exec
                                    DwarfSource.LOG.log(Level.INFO, "Tool:"+tool, ex);
                                    for(String p : params) {
                                        DwarfSource.LOG.log(Level.INFO, "\t{0}", p); //NOI18N
                                    }
                                }
                                tool = null;
                                params = new ArrayList<String>();
                                continue;
                            }
                        }
                    } finally {
                        if (progress != null) {
                            progress.done();
                        }
                    }
                    in.close();
                } catch (IOException ex) {
                    DwarfSource.LOG.log(Level.INFO, "Cannot read file "+fileName, ex);
                }
            }
        }

        public List<SourceFileProperties> getResults(Progress progress, Interrupter isStoped, CompileLineStorage storage) {
            if (result == null) {
                run(progress, isStoped, storage);
            }
            return result;
        }

        public List<String> getArtifacts(Progress progress, Interrupter isStoped, CompileLineStorage storage) {
            if (buildArtifacts == null) {
                run(progress, isStoped, storage);
            }
            return buildArtifacts;
        }
        
        private void addSources(String tool, List<String> args, CompileLineStorage storage) {
            String compiler;
            ItemProperties.LanguageKind language;
            String compilePath = null;
            if (tool.lastIndexOf('/') > 0) { //NOI18N
                compiler = tool.substring(tool.lastIndexOf('/')+1); //NOI18N
            } else {
                compiler = tool;
            }
            if (C_NAMES.contains(compiler)) {
                language = LanguageKind.C;
            } else if (CPP_NAMES.contains(compiler)) {
                language = LanguageKind.CPP;
            } else if (FORTRAN_NAMES.contains(compiler)) {
                language = LanguageKind.Fortran;
            } else if (LIBREARIES_NAMES.contains(compiler)) {
                processLibrary(compiler, args, storage);
                return;
            } else {
                language = LanguageKind.Unknown;
            }
            if (args.size()>0) {
                if (pathMapper != null) {
                    compilePath = pathMapper.getLocalPath(args.get(0));
                    if (compilePath == null) {
                        compilePath = args.get(0);
                    }
                } else {
                    compilePath = args.get(0);
                }
            }
            Iterator<String> iterator = args.iterator();
            if (iterator.hasNext()) {
                // skip path
                iterator.next();
            }
            if (iterator.hasNext()) {
                // skip tool
                iterator.next();
            }
            Artifacts artifacts = new Artifacts();
            List<String> sourcesList = DiscoveryUtils.gatherCompilerLine(iterator, DiscoveryUtils.LogOrigin.ExecLog, artifacts, compilerSettings.getProjectBridge(), language == LanguageKind.CPP);
            for (String what : sourcesList) {
                if (what == null) {
                    continue;
                }
                if (what.endsWith(".s") || what.endsWith(".S")) {  //NOI18N
                    // It seems assembler file was compiled by C compiler.
                    // Exclude assembler files from C/C++ code model.
                    continue;
                }
                String fullName;
                String sourceName;
                List<String> userIncludes = new ArrayList<String>(artifacts.userIncludes.size());
                for(String s : artifacts.userIncludes){
                    if (s.startsWith("/") && pathMapper != null) { // NOI18N
                        String mapped = pathMapper.getLocalPath(s);
                        if (mapped != null) {
                            s = mapped;
                        }
                    }
                    userIncludes.add(PathCache.getString(s));
                }
                Map<String, String> userMacros = new HashMap<String, String>(artifacts.userMacros.size());
                for(Map.Entry<String,String> e : artifacts.userMacros.entrySet()){
                    if (e.getValue() == null) {
                        userMacros.put(PathCache.getString(e.getKey()), null);
                    } else {
                        userMacros.put(PathCache.getString(e.getKey()), PathCache.getString(e.getValue()));
                    }
                }
                if (what.startsWith("/")){  //NOI18N
                    if (pathMapper != null) {
                        String mapped = pathMapper.getLocalPath(what);
                        if (mapped != null) {
                            what = mapped;
                        }
                    }
                    fullName = what;
                    sourceName = DiscoveryUtils.getRelativePath(compilePath, what);
                } else {
                    fullName = compilePath+"/"+what; //NOI18N
                    sourceName = what;
                }
                //FileObject f = fileSystem.findResource(fullName);
                //if (f != null && f.isValid() && f.isData()) {
                    fullName = PathCache.getString(fullName);
                    if (artifacts.languageArtifacts.contains("c")) { // NOI18N
                        language = ItemProperties.LanguageKind.C;
                    } else if (artifacts.languageArtifacts.contains("c++")) { // NOI18N
                        language = ItemProperties.LanguageKind.CPP;
                    } else {
                        if (language == LanguageKind.Unknown) {
                            String mime =MIMESupport.getKnownSourceFileMIMETypeByExtension(fullName);
                            if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
                                language = ItemProperties.LanguageKind.CPP;
                            } else if (MIMENames.C_MIME_TYPE.equals(mime)) {
                                language = ItemProperties.LanguageKind.C;
                            }
                        } else if(language == LanguageKind.C && !compiler.equals("cc")){ // NOI18N
                            // GNU driver detect language by mime type
                            String mime =MIMESupport.getKnownSourceFileMIMETypeByExtension(fullName);
                            if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
                                language = ItemProperties.LanguageKind.CPP;
                            }
                        }
                    }
                    ExecSource res = new ExecSource(storage);
                    res.compilePath = compilePath;
                    res.compiler = compiler;
                    res.sourceName = sourceName;
                    //
                    File file = new File(fullName);
                    fullName = CndFileUtils.normalizeFile(file).getAbsolutePath();
                    //
                    res.fullName = fullName;
                    res.language = language;
                    res.userIncludes = userIncludes;
                    res.userMacros = userMacros;
                    res.undefinedMacros = artifacts.undefinedMacros;
                    for(String lang : artifacts.languageArtifacts) {
                        if ("c89".equals(lang)) { //NOI18N
                            res.standard = ItemProperties.LanguageStandard.C89;
                        } else if ("c99".equals(lang)) { //NOI18N
                            res.standard = ItemProperties.LanguageStandard.C89;
                        } else if ("c++98".equals(lang)) { //NOI18N
                            res.standard = ItemProperties.LanguageStandard.CPP;
                        } else if ("c++11".equals(lang)) { //NOI18N
                            res.standard = ItemProperties.LanguageStandard.CPP11;
                        } 
                    }
                    if (storage != null) {
                        StringBuilder buf = new StringBuilder();
                        for (int i = 2; i < args.size(); i++) {
                            if (buf.length() > 0) {
                                buf.append(' ');
                            }
                            String s = args.get(i);
                            String s2 = CndPathUtilities.quoteIfNecessary(s);
                            if (s.equals(s2)) {
                                if (s.indexOf('"') > 0) {// NOI18N
                                    int j = s.indexOf("\\\"");// NOI18N
                                    if (j < 0) {
                                        s = s.replace("\"", "\\\"");// NOI18N
                                    }
                                }
                            } else {
                                s = s2;
                            }
                            buf.append(s);
                        }
                        res.handler = storage.putCompileLine(buf.toString());
                    }
                    result.add(res);
                //} else {
                //    continue;
                //}
            }
        }
        
        private FileObject convertPath(String path) {
            FileObject fo = fileSystem.findResource(path);
            if (localMapper != null) {
                if (fo == null || !fo.isValid()) {
                    RelocatablePathMapper.ResolvedPath resolvedPath = localMapper.getPath(path);
                    if (resolvedPath == null) {
                        if (root != null) {
                            RelocatablePathMapper.FS fs = new RelocatablePathMapperImpl.FS() {
                                @Override
                                public boolean exists(String path) {
                                    FileObject fo = fileSystem.findResource(path);
                                    if (fo != null && fo.isValid()) {
                                        return true;
                                    }
                                    return false;
                                }

                                @Override
                                public List<String> list(String path) {
                                    List<String> res = new ArrayList<String>();
                                    FileObject fo = fileSystem.findResource(path);
                                    if (fo != null && fo.isValid() && fo.isFolder()) {
                                        for (FileObject f : fo.getChildren()) {
                                            res.add(f.getPath());
                                        }
                                    }
                                    return res;
                                }
                            };
                            if (localMapper.discover(fs, root, path)) {
                                resolvedPath = localMapper.getPath(path);
                                fo = fileSystem.findResource(resolvedPath.getPath());
                            }
                        }
                    } else {
                        fo = fileSystem.findResource(resolvedPath.getPath());
                    }
                }
            }
            return fo;
        }
        
        private void processLibrary(String tool, List<String> args, CompileLineStorage storage) {
            //TODO: get library name
            if ("ar".equals(tool)) { // NOI18N
                // static library
                //called: /usr/ccs/bin/ar
                //        /var/tmp/alsimon-cnd-test-downloads/pkg-config-0.25/popt
                //        ar
                //        cru
                //        .libs/libpopt.a
                //        .libs/popt.o
                //        .libs/poptconfig.o
            } else if ("ld".equals(tool)) { // NOI18N
                // executable or dynamic library
                //called: /usr/ccs/bin/ld
                //        /var/tmp/alsimon-cnd-test-downloads/pkg-config-0.25/glib-1.2.10/gmodule
                //        /usr/ccs/bin/ld
                //        -zld32=-S/tmp/lib_link.1359732141.24769.01/libldstab_ws.so
                //        /opt/solarisstudio12.3/prod/lib/crti.o
                //        testgmodule.o
                //        ./.libs/libgmodule.a
                //        ../.libs/libglib.a
                //        -o
                //        testgmodule
                //        -Y
                //        P,/opt/solarisstudio12.3/prod/lib:/usr/ccs/lib:/lib:/usr/lib
                //        -Qy
                //        -lc
                //        /opt/solarisstudio12.3/prod/lib/crtn.o
                Iterator<String> iterator = args.iterator();
                if (!iterator.hasNext()) {
                    return;
                }
                String compilePath = iterator.next();
                if (pathMapper != null) {
                    String anCompilePath = pathMapper.getLocalPath(compilePath);
                    if (anCompilePath != null) {
                        compilePath = anCompilePath;
                    }
                }
                if (!iterator.hasNext()) {
                    return;
                }
                // skip tool
                iterator.next();
                String binary = null;
                while(iterator.hasNext()) {
                    String option = iterator.next();
                    if ("-o".equals(option)) { // NOI18N
                        if (iterator.hasNext()) {
                            binary = iterator.next();
                            break;
                        }
                    }
                }
                if (binary != null) {
                    String fullName;
                    if (binary.startsWith("/")){  //NOI18N
                        if (pathMapper != null) {
                            String mapped = pathMapper.getLocalPath(binary);
                            if (mapped != null) {
                                binary = mapped;
                            }
                        }
                        fullName = binary;
                    } else {
                        fullName = compilePath+"/"+binary; //NOI18N
                    }
                    FileObject f = fileSystem.findResource(fullName);
                    if (f == null) {
                        // probably it is just created binary. Try to refresh folder.
                        FileObject folder = fileSystem.findResource(compilePath);
                        if (folder != null && folder.isValid() && folder.isFolder()) {
                            folder.refresh();
                            f = fileSystem.findResource(fullName);
                        }
                    }
                    if (f != null && f.isValid() && f.isData()) {
                        buildArtifacts.add(fullName);
                    } else {
                        f = convertPath(fullName);   
                        if (f != null && f.isValid() && f.isData()) {
                            buildArtifacts.add(fullName);
                        }
                    }
                }
            }
        }
    }
    
    private static class ExecSource extends RelocatableImpl implements SourceFileProperties {

        private String sourceName;
        private String compiler;
        private ItemProperties.LanguageKind language;
        private LanguageStandard standard = LanguageStandard.Unknown;
        private List<String> systemIncludes = Collections.<String>emptyList();
        private Map<String, String> userMacros;
        private List<String> undefinedMacros;
        private Map<String, String> systemMacros = Collections.<String, String>emptyMap();
        private final CompileLineStorage storage;
        private int handler = -1;

        private ExecSource(CompileLineStorage storage) {
            this.storage = storage;
        }
        
        @Override
        public String getCompilePath() {
            return compilePath;
        }

        @Override
        public String getItemPath() {
            return fullName;
        }

        @Override
        public String getCompileLine() {
            return storage.getCompileLine(handler);
        }

        @Override
        public String getItemName() {
            return sourceName;
        }
        
        @Override
        public List<String> getUserInludePaths() {
            return userIncludes;
        }

        @Override
        public List<String> getSystemInludePaths() {
            return systemIncludes;
        }

        public Set<String> getIncludedFiles() {
            return includedFiles;
        }

        @Override
        public Map<String, String> getUserMacros() {
            return userMacros;
        }

        @Override
        public List<String> getUndefinedMacros() {
            return undefinedMacros;
        }

        @Override
        public Map<String, String> getSystemMacros() {
            return systemMacros;
        }

        @Override
        public ItemProperties.LanguageKind getLanguageKind() {
            return language;
        }

        @Override
        public String getCompilerName() {
            return compiler;
        }

        @Override
        public LanguageStandard getLanguageStandard() {
            return standard;
        }
    }
}
