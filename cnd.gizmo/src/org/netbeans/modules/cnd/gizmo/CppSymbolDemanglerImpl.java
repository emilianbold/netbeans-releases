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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gizmo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.dlight.spi.CppSymbolDemangler;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory.CPPCompiler;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Exceptions;

/**
 * @author mt154047
 * @author Alexey Vladykin
 */
public class CppSymbolDemanglerImpl implements CppSymbolDemangler {

    private static final String ECHO = "echo"; // NOI18N
    private static final String DEM = "dem"; // NOI18N
    private static final String GCPPFILT = "gc++filt"; // NOI18N
    private static final String CPPFILT = "c++filt"; // NOI18N
    private static final String EQUALS_EQUALS = " == "; // NOI18N

    private static final int MAX_CMDLINE_LENGTH = 2000;

    private final static Map<String, String> demangledCache = new HashMap<String, String>();
    private final List<String> searchPaths;
    private final ExecutionEnvironment env;
    private final CPPCompiler cppCompiler;
    private String demanglerTool;
    private boolean demanglerChecked;

    public CppSymbolDemanglerImpl(ExecutionEnvironment execEnv, CPPCompiler cppCompiler, String compilerBinDir) {
        this.env = execEnv;
        this.cppCompiler = cppCompiler;
        this.searchPaths = compilerBinDir == null?
            Collections.<String>emptyList() : Collections.<String>singletonList(compilerBinDir);
        switch (cppCompiler) {
            case GNU:
                demanglerTool = GCPPFILT;
                break;
            case SS:
                demanglerTool = DEM;
                break;
            default:
                throw new IllegalArgumentException("Unknown cppCompiler " + cppCompiler); // NOI18N
        }
    }

    @Override
    public String demangle(String symbolName) {
        String mangledName = stripModuleAndOffset(symbolName);

        if (!isToolAvailable() || !isMangled(mangledName)) {
            return mangledName;
        }

        String demangledName = null;

        synchronized (demangledCache) {
            demangledName = demangledCache.get(mangledName);
        }

        if (demangledName == null) {
            List<String> list = Arrays.asList(mangledName);
            demangleImpl(list);
            demangledName = list.get(0);
            synchronized (demangledCache) {
                demangledCache.put(mangledName, demangledName);
            }
        }

        return demangledName;
    }

    @Override
    public List<String> demangle(List<String> symbolNames) {
        List<String> result = new ArrayList<String>(symbolNames.size());

        for (String name : symbolNames) {
            result.add(stripModuleAndOffset(name));
        }

        if (!isToolAvailable()) {
            return result;
        }

        List<String> missedNames = new ArrayList<String>();
        List<Integer> missedIdxs = new ArrayList<Integer>();

        synchronized (demangledCache) {
            for (int i = 0; i < result.size(); ++i) {
                String mangledName = result.get(i);
                if (isMangled(mangledName)) {
                    String demangledName = demangledCache.get(mangledName);
                    if (demangledName == null) {
                        missedNames.add(mangledName);
                        missedIdxs.add(i);
                    } else {
                        result.set(i, demangledName);
                    }
                }
            }
        }

        if (!missedNames.isEmpty()) {
            splitAndDemangle(missedNames);
            synchronized (demangledCache) {
                for (int i = 0; i < missedNames.size(); ++i) {
                    int idx = missedIdxs.get(i);
                    String mangledName = result.get(idx);
                    String demangledName = missedNames.get(i);
                    demangledCache.put(mangledName, demangledName);
                    result.set(idx, demangledName);
                }
            }
        }

        return result;
    }

    private boolean isMangled(String name) {
        // aggressive optimization, but invoking dozens of processes
        // on remote machine is not very fast
        return 0 < name.length() && name.charAt(0) == '_' || 0 <= name.indexOf("__"); // NOI18N
    }

    private static String stripModuleAndOffset(String functionName) {
        int plusPos = functionName.indexOf("+0x"); // NOI18N
        if (0 <= plusPos) {
            functionName = functionName.substring(0, plusPos);
        }
        int tickPos = functionName.indexOf('`'); // NOI18N
        if (0 <= tickPos) {
            functionName = functionName.substring(tickPos + 1);
        }
        return functionName;
    }

    /**
     * Splits mangled names list into chunks to avoid command line overflow.
     * Invokes {@link #demangleImpl(List)} for each chunk.
     *
     * @param mangledNames
     */
    private void splitAndDemangle(List<String> mangledNames) {

        if (demanglerTool == null) {
            // demangler not found
            return;
        }

        ListIterator<String> it = mangledNames.listIterator();
        while (it.hasNext()) {

            int startIdx = it.nextIndex();
            int cmdlineLength = ECHO.length() + demanglerTool.length();
            while (it.hasNext() && cmdlineLength < MAX_CMDLINE_LENGTH) {
                String name = it.next();
                cmdlineLength += name.length() + 3; // space and quotes
            }
            int endIdx = it.nextIndex();

            List<String> mangledNamesSublist = mangledNames.subList(startIdx, endIdx);
            demangleImpl(mangledNamesSublist);
        }
    }

    private void demangleImpl(List<String> mangledNames) {
        checkDemanglerIfNeeded();

        if (demanglerTool == null) {
            // demangler not found
            return;
        }

        final NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable(demanglerTool);

        // This code used to run 'echo mangled names | demangler' if demangler reads stdin,
        // but pipes and NativeProcessBuilder.setCommandLine() do not work together
        // on Windows (bug #177849), so the code had to be rewritten.
        final boolean demanglerReadsStdin = demanglerTool.contains(GCPPFILT) || demanglerTool.contains(CPPFILT);
        if (!demanglerReadsStdin) {
            npb.setArguments(mangledNames.toArray(new String[mangledNames.size()]));
        }

        Future<Process> task = DLightExecutorService.submit(npb, "CPPSymbolDemangler call");//NOI18N

        try {
            NativeProcess np = (NativeProcess) task.get();

            // Arrange for collection of output in a separate thread.
            // Start a thread right here without any exector services
            // to make sure that execution is not blocked or delayed.
            DemanglerOutputCollector outputCollector = new DemanglerOutputCollector(np.getInputStream());
            Thread outputCollectorThread = new Thread(outputCollector);
            outputCollectorThread.start();

            if (demanglerReadsStdin) {
                try {
                    OutputStream outputStream = np.getOutputStream();
                    try {
                        for (String mangledName : mangledNames) {
                            outputStream.write(mangledName.getBytes());
                            outputStream.write('\n');
                        }
                        outputStream.flush();
                    } finally {
                        outputStream.close();
                    }
                } catch (IOException ex) {
                    // hide it
                }
            }

            np.waitFor();
            outputCollectorThread.join();

            List<String> demangledNames = outputCollector.getDemangledNames();
            if (demangledNames.size() == mangledNames.size()) {
                for (int i = 0; i < mangledNames.size(); ++i) {
                    mangledNames.set(i, demangledNames.get(i));
                }
            }

        } catch (InterruptedException e) {
        } catch (ExecutionException execException) {
        }
    }

    private synchronized void checkDemanglerIfNeeded() {
        if (!demanglerChecked) {
            String exeSuffix = ""; // NOI18N
            try {
                HostInfo hostinfo = HostInfoUtils.getHostInfo(env);
                if (hostinfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                    exeSuffix = ".exe"; // NOI18N
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            String absPath = HostInfoUtils.searchFile(env, searchPaths, demanglerTool + exeSuffix, true);
            if (absPath == null) {
                absPath = HostInfoUtils.searchFile(env, searchPaths, CPPFILT + exeSuffix, true);
                if (absPath == null) {
                    demanglerTool = null;
                } else {
                    demanglerTool = absPath;
                }
            } else {
                demanglerTool = absPath;
            }
            demanglerChecked = true;
        }
    }

    /**
     * Discard caches. For unit tests.
     */
    /*package*/ void clearCache() {
        synchronized (demangledCache) {
            demangledCache.clear();
        }
    }

    /**
     * Checks if native demangler tool is available.
     * We can't work without this tool!
     *
     * @return <code>true</code> if tool is available and this demangler
     *      is functional, <code>false</code> otherwise
     */
    /*package*/ boolean isToolAvailable() {
        checkDemanglerIfNeeded();
        return demanglerTool != null;
    }

    private static class DemanglerOutputCollector implements Runnable {

        private final InputStream inputStream;
        private final List<String> demangledNames;

        private DemanglerOutputCollector(InputStream inputStream) {
            this.inputStream = inputStream;
            this.demangledNames = new ArrayList<String>();
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    while (true) {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.length() == 0) {
                            continue;
                        }

                        int eqPos = line.indexOf(EQUALS_EQUALS);
                        demangledNames.add(0 <= eqPos ? line.substring(eqPos + EQUALS_EQUALS.length()) : line);
                    }
                } finally {
                    reader.close();
                }
            } catch (IOException ex) {
                // hide it
            }
        }

        private List<String> getDemangledNames() {
            return demangledNames;
        }
    }
}
