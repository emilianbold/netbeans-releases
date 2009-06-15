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
package org.netbeans.modules.dlight.perfan.storage.impl;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class ExperimentStatisticsTest {

    @Test
    public void testSeparatorComma() {
        ExperimentStatistics statistics = new ExperimentStatistics(new String[]{
        "Experiment: experiment_1.er",
        "                      Execution for entire program",
        "",
        "                                       Start Label: Total",
        "                                         End Label: Total",
        "                                 Start Time (sec.): 0,171",
        "                                   End Time (sec.): 9,019",
        "                                   Duration (sec.): 8,848",
        "                          Total Thread Time (sec.): 8,849",
        "                         Average number of Threads: 1,000",
        "",
        "                              Process Times (sec.):",
        "                                          User CPU: 7,762 ( 87,7%)",
        "                                        System CPU: 0,504 (  5,7%)",
        "                                          Wait CPU: 0,    (  0, %)",
        "                                         User Lock: 0,1   (  0, %)",
        "                                   Text Page Fault: 0,    (  0, %)",
        "                                   Data Page Fault: 0,    (  0, %)",
        "                                        Other Wait: 0,    (  0, %)",
        "",
        "                                 Minor Page Faults:  4741",
        "                                 Major Page Faults:     6",
        "                                     Process swaps:     0",
        "                                      Input blocks:     0",
        "                                     Output blocks:     0",
        "                                     Messages sent:     0",
        "                                 Messages received:     0",
        "                                   Signals handled:     0",
        "                        Voluntary context switches:    55",
        "                      Involuntary context switches:  1551",
        "                                      System calls:     0",
        "                                 Characters of I/O:     0"});
        assertEquals(Double.valueOf(8.848), statistics.getDuration());
        assertEquals(Double.valueOf(8.849), statistics.getTotalThreadTime());
        assertEquals(Double.valueOf(0.1), statistics.getULock());
        assertEquals(Double.valueOf(0), statistics.getULock_p());
    }

    @Test
    public void testSeparatorDot() {
        ExperimentStatistics statistics = new ExperimentStatistics(new String[]{
        "Experiment: experiment_1.er",
        "                      Execution for entire program",
        "",
        "                                       Start Label: Total",
        "                                         End Label: Total",
        "                                 Start Time (sec.): 0.171",
        "                                   End Time (sec.): 9.019",
        "                                   Duration (sec.): 9.848",
        "                          Total Thread Time (sec.): 9.849",
        "                         Average number of Threads: 1.000",
        "",
        "                              Process Times (sec.):",
        "                                          User CPU: 7.762 ( 87.7%)",
        "                                        System CPU: 0.504 (  5.7%)",
        "                                          Wait CPU: 0.    (  0. %)",
        "                                         User Lock: 0.    (  0. %)",
        "                                   Text Page Fault: 0.    (  0. %)",
        "                                   Data Page Fault: 0.    (  0. %)",
        "                                        Other Wait: 0.    (  0. %)",
        "",
        "                                 Minor Page Faults:  4741",
        "                                 Major Page Faults:     6",
        "                                     Process swaps:     0",
        "                                      Input blocks:     0",
        "                                     Output blocks:     0",
        "                                     Messages sent:     0",
        "                                 Messages received:     0",
        "                                   Signals handled:     0",
        "                        Voluntary context switches:    55",
        "                      Involuntary context switches:  1551",
        "                                      System calls:     0",
        "                                 Characters of I/O:     0"});
        assertEquals(Double.valueOf(9.848), statistics.getDuration());
        assertEquals(Double.valueOf(9.849), statistics.getTotalThreadTime());
        assertEquals(Double.valueOf(0), statistics.getULock());
        assertEquals(Double.valueOf(0), statistics.getULock_p());
    }

    @Test
    public void testLWPTime() {
        // On old Solarises there is "Total LWP Time" instead of "Total Thread Time"
        ExperimentStatistics statistics = new ExperimentStatistics(new String[] {
        "                             Total LWP Time (sec.): 8,849"});
        assertNull(statistics.getDuration());
        assertEquals(Double.valueOf(8.849), statistics.getTotalThreadTime());
    }

    @Test
    public void testIncorrect() {
        ExperimentStatistics statistics = new ExperimentStatistics(new String[] {
        "                                         User Lock: ???   (  ???%)"});
        assertNull(statistics.getDuration());
        assertNull(statistics.getULock());
    }
}
