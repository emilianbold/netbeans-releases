/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.repository.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.execution.NativeExecutor;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Tracer;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author sg155630
 */
public class RepositoryValidationBase extends TraceModelTestBase {

    public RepositoryValidationBase(String testName) {
        super(testName);
    }

    protected static final String nimi = "ModelBuiltFromRepository"; //NOI18N
    protected static final String modelimplName = "cnd.modelimpl";
    protected static final String moduleName = "cnd.repository";
    private static String goldenDirectory;

    @Override
    protected File getTestCaseDataDir() {
        String dataPath = getDataDir().getAbsolutePath().replaceAll("repository", "modelimpl"); //NOI18N
        String filePath = "common";
        return Manager.normalizeFile(new File(dataPath, filePath));
    }

    @Override
    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        super.doTest(args, streamOut, streamErr, params);
    }

    @Override
    protected void postTest(String[] args, Object... params) {
        if (!getTraceModel().getProject().isStable(null)) {
            CndUtils.threadsDump();
            while (!getTraceModel().getProject().isStable(null)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        Map<CharSequence, FileImpl> map = new TreeMap<CharSequence, FileImpl>();
        for (CsmFile f : getTraceModel().getProject().getAllFiles()){
            map.put(f.getAbsolutePath(), (FileImpl)f);
            if (!f.isParsed()){
                CndUtils.threadsDump();
            }
        }
        for (FileImpl file : map.values()){
            CsmTracer tracer = new CsmTracer(false);
            tracer.setDeep(true);
            tracer.setDumpTemplateParameters(false);
            tracer.setTestUniqueName(false);
            tracer.dumpModel(file);
        }
        dumpProjectContainers(getTraceModel().getProject());
        super.postTest(args, params);
    }

    private void dumpProjectContainers(CsmProject project){
        Tracer.dumpProjectContainers((ProjectBase) project);
    }

    protected static String getGoldenDirectory() {
        return goldenDirectory;
    }

    protected static void setGoldenDirectory(String goldenDirectory) {
        RepositoryValidationBase.goldenDirectory = goldenDirectory;
    }
    
    protected final List<String> find() throws IOException {
        return download();
//        List<String> list = new ArrayList<String>();
//        //String dataPath = getDataDir().getAbsolutePath().replaceAll("repository", "modelimpl"); //NOI18N
//        //list.add(dataPath + "/common/quote_nosyshdr"); //NOI18N
//        //list.add(dataPath + "/org"); //NOI18N
//        String dataPath = getDataDir().getAbsolutePath();
//        int i = dataPath.indexOf("repository");
//        dataPath = dataPath.substring(0,i+11)+"test";
//        list.add(dataPath + "/CLucene"); //NOI18N
//        list.add(dataPath + "/pkg-config"); //NOI18N
//        list = expandAndSort(list);
//        list.add("-I"+dataPath+"/CLucene");
//        list.add("-I"+dataPath+"/CLucene/CLucene");
//        list.add("-DHAVE_CONFIG_H");
//        list.add("-I"+dataPath + "/pkg-config");
//        return list;
//
//
    }
    
    // "http://pkgconfig.freedesktop.org/releases/pkgconfig-0.18.tar.gz"
    // "http://www.mirrorservice.org/sites/download.sourceforge.net/pub/sourceforge/l/li/litesql/litesql-0.3.3.tar.gz"
    // wget http://pkgconfig.freedesktop.org/releases/pkgconfig-0.18.tar.gz
    // gzip -d pkgconfig-0.18.tar.gz
    // tar xf pkgconfig-0.18.tar
    private List<String> download() throws IOException{
        List<String> list = new ArrayList<String>();
        File fileDataPath = CndCoreTestUtils.getDownloadBase();
        String dataPath = fileDataPath.getAbsolutePath();
        final AtomicBoolean finish = new AtomicBoolean(false);
        ExecutionListener listener = new ExecutionListener() {
            public void executionStarted(int pid) {
            }
            public void executionFinished(int rc) {
                finish.set(true);
            }
        };
        NativeExecutor ne = null;
        File file = new File(dataPath + "/pkg-config-0.23");
        if (!file.exists()){
            file.mkdirs();
        }
        if (file.list().length == 0){
            ne = new NativeExecutor(dataPath,"wget",
                    "http://pkgconfig.freedesktop.org/releases/pkg-config-0.23.tar.gz",new String[0],"wget","run",false,false);
            waitExecution(ne, listener, finish);
            ne = new NativeExecutor(dataPath,"gzip",
                    "-d pkg-config-0.23.tar.gz",new String[0],"gzip","run",false,false);
            waitExecution(ne, listener, finish);
            ne = new NativeExecutor(dataPath,"tar",
                    "xf pkg-config-0.23.tar",new String[0],"tar","run",false,false);
            waitExecution(ne, listener, finish);
        }

        file = new File(dataPath + "/litesql-0.3.3");
        if (!file.exists()){
            file.mkdirs();
        }
        if (file.list().length == 0){
            ne = new NativeExecutor(dataPath,"wget",
                    "http://www.mirrorservice.org/sites/download.sourceforge.net/pub/sourceforge/l/li/litesql/litesql-0.3.3.tar.gz",new String[0],"wget","run",false,false);
            waitExecution(ne, listener, finish);
            ne = new NativeExecutor(dataPath,"gzip",
                    "-d litesql-0.3.3.tar.gz",new String[0],"gzip","run",false,false);
            waitExecution(ne, listener, finish);
            ne = new NativeExecutor(dataPath,"tar",
                    "xf litesql-0.3.3.tar",new String[0],"tar","run",false,false);
            waitExecution(ne, listener, finish);
        }
        list.add(dataPath + "/pkg-config-0.23"); //NOI18N
        list.add(dataPath + "/litesql-0.3.3"); //NOI18N
        for(String f : list){
            file = new File(f);
            assertTrue("Not found folder "+f, file.exists());
        }
        list = expandAndSort(list);
        list.add("-DHAVE_CONFIG_H");
        list.add("-I"+dataPath + "/pkg-config-0.23");
        list.add("-I"+dataPath + "/litesql-0.3.3");
        return list;
    }

    private void waitExecution(NativeExecutor ne, ExecutionListener listener, AtomicBoolean finish){
        finish.set(false);
        ne.addExecutionListener(listener);
        try {
            ne.execute();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        while(!finish.get()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    protected final List<String> expandAndSort(List<String> files) {
        List<String> result = new ArrayList<String>();
        for( String file : files ) {
            addFile(file, result);
        }
        Collections.sort(result);
        return result;
    }
    
    private void addFile(String fileName, List<String> files) {
        File file = new File(fileName);
        if( file.isDirectory() ) {
            String[] list = file.list();
            for( int i = 0; i < list.length; i++ ) {
                addFile(new File(file, list[i]).getAbsolutePath(), files);
            }
        } else {
            if (fileName.endsWith(".c")||fileName.endsWith(".cpp")){
                if (fileName.indexOf("32.")>0){
                    return;
                }
                files.add(fileName);
            }
        }
    }
}