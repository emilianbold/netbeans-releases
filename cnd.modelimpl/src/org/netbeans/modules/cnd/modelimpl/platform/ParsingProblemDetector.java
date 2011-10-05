/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.openide.util.NbBundle;

/**
 * Detector of parsing problems. Class watches used memory while project parsing.
 * If algorithm detects slowdown parsing speed class shows alert message with advice to increase java heap size.
 * 
 * @author Alexander Simon
 */
public class ParsingProblemDetector {

    private static final int Mb = 1024 * 1024;
    private final Runtime runtime;
    private final int maxMemory;
    private final int startMemory;
    private int lineCount;
    private long startTime;
    private final List<Measure> measures;
    private final int threshold;

    /**
     * Constructs progress information for project
     */
    public ParsingProblemDetector(CsmProject project) {
        runtime = Runtime.getRuntime();
        maxMemory = (int) (runtime.maxMemory() / Mb);
        threshold = Math.max(maxMemory/10, 10);
        startMemory = (int) ((runtime.totalMemory() - runtime.freeMemory()) / Mb);
        measures = new ArrayList<Measure>();
    }

    public void start() {
    }

    public void finish() {
        if (measures.size() > 1) {
            int lines = measures.get(measures.size()-1).lines;
            if (lines > 0) {
                int parsingTime = measures.get(measures.size()-1).time;
                if (parsingTime > 0) {
                    int parsingMemory = 0;
                    for(Measure m : measures) {
                        parsingMemory = Math.max(parsingMemory, m.memory);
                    }
                    System.out.println("Parsed " + lines/1000 + " KLines, Time " +parsingTime/1000 + " seconds, Speed "+lines/parsingTime+" KLines/second, Max Memory "+parsingMemory+" Mb"); // NOI18N
                    int currentPercent = 1;
                    int curentTime = 0;
                    int curentLines = 0;
                    for(Measure m : measures) {
                        int p = m.lines*100/lines;
                        if (p - currentPercent*5 >= 0) {
                            int l = m.lines - curentLines;
                            curentLines = m.lines;
                            int t = m.time - curentTime;
                            curentTime = m.time;
                            currentPercent++;
                            if (t != 0) {
                                System.out.println("Parsing speed. Work " + p + "%, " +( l / t) + " KLines/second, Memory "+m.memory+" Mb"); // NOI18N
                            }
                        }
                    }
                }
            }
        }
    }

    private void showWarning() {
        // TODO implement it.
    }
    
    /**
     * inform about starting handling next file item
     */
    public String nextCsmFile(CsmFile file, int current, int allWork) {
        String msg = "";
        int usedMemory = (int) ((runtime.totalMemory() - runtime.freeMemory()) / Mb);
        if (TraceFlags.TIMING) {
            int lines = 0;
            if (file instanceof FileImpl) {
                int[] lineColumnByOffset = null;
                try {
                    lineColumnByOffset = ((FileImpl) file).getBuffer().getLineColumnByOffset(Integer.MAX_VALUE);
                } catch (IOException ex) {
                }
                if (lineColumnByOffset != null && lineColumnByOffset[0] > 0) {
                    lines = lineColumnByOffset[0];
                }
            }
            lineCount += lines;
            long delta = System.currentTimeMillis() - startTime;
            measures.add(new Measure(lineCount, (int)delta, usedMemory));
        }
        if (maxMemory - usedMemory < threshold) {
            msg = NbBundle.getMessage(ModelSupport.class, "MSG_LowMemory", "" + usedMemory, "" + maxMemory); // NOI18N
        }
        showWarning();
        return msg;
    }

    public void switchToDeterminate(int maxWorkUnits) {
        startTime = System.currentTimeMillis();
    }
    
    private static final class Measure {
        private final int lines;
        private final int time;
        private final int memory;
        Measure(int lines, int time, int memory) {
            this.lines = lines;
            this.time = time;
            this.memory = memory;
        }
    }
}
