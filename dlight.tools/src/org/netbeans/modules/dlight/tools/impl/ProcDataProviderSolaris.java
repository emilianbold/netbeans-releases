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

import java.math.BigDecimal;
import java.util.Arrays;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.tools.impl.SolarisProcfsSupport.Prusage;
import static org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration.*;

/**
 * ProcDataProvider engine for Solaris.
 *
 * Thread count is determined as number of files in <code>/proc/pid/lwp</code>.
 * Thread count from /proc/pid/prusage is not what we want as it never decreases.
 *
 * @author Alexey Vladykin
 */
public class ProcDataProviderSolaris implements ProcDataProvider.Engine {

    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);

    private final DataRowConsumer consumer;
    private final int cpuCount;
    private final ServiceInfoDataStorage serviceInfoStorage;

    public ProcDataProviderSolaris(DataRowConsumer consumer, ServiceInfoDataStorage serviceInfoStorage, int cpuCount) {
        this.consumer = consumer;
        this.serviceInfoStorage = serviceInfoStorage;
        this.cpuCount = cpuCount;
    }

    public String getCommand(int pid) {
        return "while od -v -t x4 -N 64 /proc/" + pid + "/usage && ls /proc/" + pid + "/lwp | wc -l; do sleep 1; done"; // NOI18N
    }

    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
        return InputProcessors.bridge(new SolarisProcLineProcessor());
    }

    private class SolarisProcLineProcessor implements LineProcessor {

        private Prusage prevPrusage;
        private Prusage currPrusage;

        @Override
        public void processLine(String line) {
            try {
            String firstToken = getFirstToken(line);
            if (firstToken.length() == 7) {
                currPrusage = SolarisProcfsSupport.parsePrusage(line, currPrusage);
            } else {
                if (prevPrusage != null) {
                    int threads = Integer.parseInt(firstToken);
                    BigDecimal cpuTime = currPrusage.tstamp().toBigDecimal()
                            .subtract(prevPrusage.tstamp().toBigDecimal())
                            .multiply(BigDecimal.valueOf(cpuCount));
                    BigDecimal usrTime = currPrusage.utime().toBigDecimal()
                            .subtract(prevPrusage.utime().toBigDecimal());
                    BigDecimal sysTime = currPrusage.stime().toBigDecimal()
                            .subtract(prevPrusage.stime().toBigDecimal());
                    float usrPercent = percent(usrTime, cpuTime);
                    float sysPercent = percent(sysTime, cpuTime);
                    DataRow row = new DataRow(
                            Arrays.asList(
                                    USR_TIME.getColumnName(),
                                    SYS_TIME.getColumnName(),
                                    THREADS.getColumnName()),
                            Arrays.asList(
                                    usrPercent,
                                    sysPercent,
                                    threads));
                    consumer.consume(row);
                }
                prevPrusage = currPrusage;
                currPrusage = null;
            }
            } catch (IllegalArgumentException ex) {
                // silently ignore malformed line
            }
        }

        public void reset() {
            prevPrusage = currPrusage = null;
        }

        public void close() {
        }
    }

    private static String getFirstToken(String line) {
        line = line.trim();
        int endIdx = line.indexOf(' ');
        if (endIdx < 0) {
            endIdx = line.length();
        }
        return line.substring(0, endIdx);
    }

    private static float percent(BigDecimal value, BigDecimal total) {
        if (BigDecimal.ZERO.compareTo(total) < 0) { // 0 < total
            if (value.compareTo(BigDecimal.ZERO) <= 0) { // value <= 0
                return 0f;
            }
            if (total.compareTo(value) <= 0) { // total <= value
                return 100f;
            } else {
                return value.multiply(PERCENT).divideToIntegralValue(total).floatValue();
            }
        } else {
            return 0f;
        }
    }
}
