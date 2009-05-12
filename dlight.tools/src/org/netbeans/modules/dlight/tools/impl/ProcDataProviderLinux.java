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
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import static org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration.*;

/**
 * ProcDataProvider engine for Linux.
 *
 * @author Alexey Vladykin
 */
public class ProcDataProviderLinux implements ProcDataProvider.Engine {

    private static final Set<Integer> USR_TIME_IDX = new HashSet<Integer>(Arrays.asList(
            /* utime */ 13, /* cutime */ 15, /* guest_time */ 42, /* cguest_time */ 43));

    private static final Set<Integer> SYS_TIME_IDX = new HashSet<Integer>(Arrays.asList(
            /* stime */ 14, /* cstime */ 16, /* delayacct_blkio_ticks */ 41));

    private static final int THREADS_IDX = 19; /* num_threads */

    private final ProcDataProvider provider;
    private final ServiceInfoDataStorage serviceInfoStorage;
    private final boolean decreaseThreads;

    public ProcDataProviderLinux(ProcDataProvider provider, ServiceInfoDataStorage serviceInfoStorage) {
        this.provider = provider;
        this.serviceInfoStorage = serviceInfoStorage;
        String[] idps = this.serviceInfoStorage == null || 
                serviceInfoStorage.getValue(ServiceInfoDataStorage.IDP_NAMES) == null? null :
                    serviceInfoStorage.getValue(ServiceInfoDataStorage.IDP_NAMES).split(ServiceInfoDataStorage.DELIMITER);//NOI18N
        List<String> idpsList = idps == null ? null : Arrays.asList(idps);
        decreaseThreads = idpsList == null ? false : idpsList.contains(LLDataCollectorConfigurationAccessor.getDefault().getName());
    }

    public String getCommand(int pid) {
        return "while head -n1 /proc/stat && head -n1 /proc/" + pid + "/stat; do sleep 1; done"; // NOI18N
    }

    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
        return InputProcessors.bridge(new LinuxProcLineProcessor());
    }

    private class LinuxProcLineProcessor implements LineProcessor {

        private int threads;
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
                    long usrTicks = 0;
                    long sysTicks = 0;
                    for (int i = 1; tokenizer.hasMoreTokens(); ++i) {
                        String token = tokenizer.nextToken();
                        if (USR_TIME_IDX.contains(i)) {
                            usrTicks += Long.parseLong(token);
                        } else if (SYS_TIME_IDX.contains(i)) {
                            sysTicks += Long.parseLong(token);
                        } else if (THREADS_IDX == i) {
                            threads = Integer.parseInt(token);
                        }
                    }
                    if (decreaseThreads){
                        threads--;
                    }
                    currUsrTicks = usrTicks;
                    currSysTicks = sysTicks;
                    if (0 < prevTicks) {
                        long deltaTicks = currTicks - prevTicks;
                        float usrPercent = percent(currUsrTicks - prevUsrTicks, deltaTicks);
                        float sysPercent = percent(currSysTicks - prevSysTicks, deltaTicks);
                        DataRow row = new DataRow(
                                Arrays.asList(USR_TIME.getColumnName(),
                                              SYS_TIME.getColumnName(),
                                              THREADS.getColumnName()),
                                Arrays.asList(usrPercent,
                                              sysPercent,
                                              threads));
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
