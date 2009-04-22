/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.dlight.tools.impl;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration;

/**
 * ProcDataProvider engine for Linux.
 *
 * @author Alexey Vladykin
 */
public class ProcDataProviderLinux implements ProcDataProvider.Engine {

    private static final int USR_TIME_IDX = 12; // count starts from 0
    private static final int SYS_TIME_IDX = 13; // count starts from 0
    private final ProcDataProvider provider;

    public ProcDataProviderLinux(ProcDataProvider provider) {
        this.provider = provider;
    }

    public String getCommand(int pid) {
        return "while head -n1 /proc/stat && head -n1 /proc/" + pid + "/stat; do sleep 1; done"; // NOI18N
    }

    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
        return InputProcessors.bridge(new LinuxProcLineProcessor());
    }

    private class LinuxProcLineProcessor implements LineProcessor {

        private long prevTicks;
        private long currTicks;
        private long prevUsrTicks;
        private long currUsrTicks;
        private long prevSysTicks;
        private long currSysTicks;

        @Override
        public void processLine(String line) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            try {
                if (tokenizer.nextToken().equals("cpu")) { // NOI18N
                    // cpu  3357 0 4313 1362393
                    // The amount of time, that the system spent in user mode, user mode
                    // with low priority (nice), system mode, and the idle task, respectively.
                    // In later Linuxes there are more counters. Sum them all.
                    prevTicks = currTicks;
                    long ticks = 0;
                    while (tokenizer.hasMoreTokens()) {
                        ticks += Long.parseLong(tokenizer.nextToken());
                    }
                    currTicks = ticks;
                } else {
                    prevUsrTicks = currUsrTicks;
                    prevSysTicks = currSysTicks;
                    for (int i = 0; i <= USR_TIME_IDX || i <= SYS_TIME_IDX; ++i) {
                        String token = tokenizer.nextToken();
                        if (i == USR_TIME_IDX) {
                            currUsrTicks = Long.parseLong(token);
                        } else if (i == SYS_TIME_IDX) {
                            currSysTicks = Long.parseLong(token);
                        }
                    }
                    if (0 < prevTicks) {
                        long deltaTicks = currTicks - prevTicks;
                        float usrPercent = percent(currUsrTicks - prevUsrTicks, deltaTicks);
                        float sysPercent = percent(currSysTicks - prevSysTicks, deltaTicks);
                        DataRow row = new DataRow(
                                ProcDataProviderConfiguration.CPU_TABLE.getColumnNames(),
                                Arrays.asList(usrPercent, sysPercent));
                        provider.notifyIndicators(row);
                    }
                }
            } catch (NoSuchElementException ex) {
                // silently ignore malformed line
            } catch (NumberFormatException ex) {
                // silently ignore malformed line
            }
        }

        public void reset() {
        }

        public void close() {
        }
    }

    private static float percent(long value, long total) {
        if (0 < total) {
            if (value <= 0) {
                return 0f;
            }
            if (total <= value) {
                return 100f;
            } else {
                return 100f * value / total;
            }
        } else {
            return 0f;
        }
    }
}
