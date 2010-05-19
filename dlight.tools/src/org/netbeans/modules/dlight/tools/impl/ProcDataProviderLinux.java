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
package org.netbeans.modules.dlight.tools.impl;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.tools.impl.LinuxProcfsSupport.CpuStat;
import org.netbeans.modules.dlight.tools.impl.LinuxProcfsSupport.ProcessStat;
import org.netbeans.modules.dlight.util.DLightMath;
import static org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration.*;

/**
 * ProcDataProvider engine for Linux.
 *
 * @author Alexey Vladykin
 */
public class ProcDataProviderLinux implements ProcDataProvider.Engine {

    private static final List<String> COLNAMES = Collections.unmodifiableList(Arrays.asList(
            USR_TIME.getColumnName(), SYS_TIME.getColumnName(), THREADS.getColumnName()));
    private static final BigInteger PERCENT = BigInteger.valueOf(100);

    private final DataRowConsumer consumer;
    private final ServiceInfoDataStorage serviceInfoStorage;
    private final boolean decreaseThreads;
    private CpuStat prevCpuStat;
    private CpuStat currCpuStat;
    private ProcessStat prevProcessStat;
    private ProcessStat currProcessStat;

    public ProcDataProviderLinux(DataRowConsumer consumer, ServiceInfoDataStorage serviceInfoStorage) {
        this.consumer = consumer;
        this.serviceInfoStorage = serviceInfoStorage;
        String[] idps = this.serviceInfoStorage == null ||
                serviceInfoStorage.getValue(ServiceInfoDataStorage.IDP_NAMES) == null ? null : serviceInfoStorage.getValue(ServiceInfoDataStorage.IDP_NAMES).split(ServiceInfoDataStorage.DELIMITER);//NOI18N
        List<String> idpsList = idps == null ? null : Arrays.asList(idps);
        decreaseThreads = idpsList == null ? false : idpsList.contains(LLDataCollectorConfigurationAccessor.getDefault().getName());
    }

    public String getCommand(int pid) {
        return "while head -n1 /proc/stat && head -n1 /proc/" + pid + "/stat; do sleep 1; done"; // NOI18N
    }

    @Override
    public void processLine(String line) {
        try {
            if (line.startsWith("cpu")) { // NOI18N
                prevCpuStat = currCpuStat;
                currCpuStat = LinuxProcfsSupport.parseCpuStat(line);
            } else {
                prevProcessStat = currProcessStat;
                currProcessStat = LinuxProcfsSupport.parseProcessStat(line);
                if (prevProcessStat != null) {
                    BigInteger cpuTicks = currCpuStat.all().subtract(prevCpuStat.all());
                    BigInteger usrTicks = usrTicks(currProcessStat).subtract(usrTicks(prevProcessStat));
                    BigInteger sysTicks = sysTicks(currProcessStat).subtract(sysTicks(prevProcessStat));
                    long threads = currProcessStat.num_threads();
                    if (decreaseThreads) {
                        --threads;
                    }
                    float[] times = DLightMath.ensureSumLessOrEqual(100f, percent(usrTicks, cpuTicks), percent(sysTicks, cpuTicks));
                    DataRow row = new DataRow(COLNAMES, Arrays.asList(times[0], times[1], threads));
                    consumer.consume(row);
                }
            }
        } catch (IllegalArgumentException ex) {
            // silently ignore malformed line
            }
    }

    public void reset() {
        prevCpuStat = currCpuStat = null;
        prevProcessStat = currProcessStat = null;
    }

    public void close() {
    }

    private static float percent(BigInteger value, BigInteger total) {
        if (BigInteger.ZERO.compareTo(total) < 0) { // 0 < total
            if (value.compareTo(BigInteger.ZERO) <= 0) { // value <= 0
                return 0f;
            } else {
                return value.multiply(PERCENT).divide(total).floatValue();
            }
        } else {
            return 0f;
        }
    }

    private static BigInteger usrTicks(ProcessStat proc) {
        BigInteger result = proc.utime().add(proc.cutime());
        if (proc.guest_time() != null) {
            result = result.add(proc.guest_time());
        }
        if (proc.cguest_time() != null) {
            result = result.add(proc.cguest_time());
        }
        return result;
    }

    private static BigInteger sysTicks(ProcessStat proc) {
        BigInteger result = proc.stime().add(proc.cstime());
        if (proc.delayacct_blkio_ticks() != null) {
            result = result.add(proc.delayacct_blkio_ticks());
        }
        return result;
    }
}
