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

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class DeadlockImplTest {

    @Test
    public void testParse() {
        List<DeadlockImpl> deadlocks = DeadlockImpl.fromErprint(new String[] {
        "",
        "Deadlock #1, Potential deadlock",
        "\tThread #1",
        "\t\tLock being held:\t0x804a0f8",
        " \t\tStack: get_fork + 0x00000021, line 104 in \"din_phil.c\"",
        "\t\t       philosopher + 0x000000FC, line 73 in \"din_phil.c\"",
        "\t\t       thread_hj_start_routine + 0x00000067",
        "\t\t       collector_root + 0x0000005A",
        "\t\t       start_thread + 0x000000B8",
        "\t\t       clone + 0x0000005C",
        "\t\tLock being requested:\t0x804a098",
        " \t\tStack: get_fork + 0x00000021, line 104 in \"din_phil.c\"",
        "\t\t       philosopher + 0x00000118, line 74 in \"din_phil.c\"",
        "\t\t       thread_hj_start_routine + 0x00000067",
        "\t\t       collector_root + 0x0000005A",
        "\t\t       start_thread + 0x000000B8",
        "\t\t       clone + 0x0000005C",
        "\tThread #2",
        "\t\tLock being held:\t0x804a098",
        " \t\tStack: get_fork + 0x00000021, line 104 in \"din_phil.c\"",
        "\t\t       philosopher + 0x000000FC, line 73 in \"din_phil.c\"",
        "\t\t       thread_hj_start_routine + 0x00000067",
        "\t\t       collector_root + 0x0000005A",
        "\t\t       start_thread + 0x000000B8",
        "\t\t       clone + 0x0000005C",
        "\t\tLock being requested:\t0x804a0b0",
        " \t\tStack: get_fork + 0x00000021, line 104 in \"din_phil.c\"",
        "\t\t       philosopher + 0x00000118, line 74 in \"din_phil.c\"",
        "\t\t       thread_hj_start_routine + 0x00000067",
        "\t\t       collector_root + 0x0000005A",
        "\t\t       start_thread + 0x000000B8",
        "\t\t       clone + 0x0000005C",
        "",
        "Deadlock #2, Actual deadlock",
        "\tThread #1",
        "\t\tLock being held:\t0x804a0f8",
        " \t\tStack: get_fork + 0x00000021, line 104 in \"din_phil.c\"",
        "\t\t       philosopher + 0x000000FC, line 73 in \"din_phil.c\"",
        "\t\t       thread_hj_start_routine + 0x00000067",
        "\t\t       collector_root + 0x0000005A",
        "\t\t       start_thread + 0x000000B8",
        "\t\t       clone + 0x0000005C",
        "\t\tLock being requested:\t0x804a098",
        " \t\tStack: get_fork + 0x00000021, line 104 in \"din_phil.c\"",
        "\t\t       philosopher + 0x00000118, line 74 in \"din_phil.c\"",
        "\t\t       thread_hj_start_routine + 0x00000067",
        "\t\t       collector_root + 0x0000005A",
        "\t\t       start_thread + 0x000000B8",
        "\t\t       clone + 0x0000005C",
        "",
        "Deadlock Detailed List Summary: Experiment: /tmp/dlight_av202691/experiment_1.er Total Deadlocks: 2",
        ""});
        assertEquals(2, deadlocks.size());

        DeadlockImpl d1 = deadlocks.get(0);
        assertFalse(d1.isActual());

        DeadlockImpl d2 = deadlocks.get(1);
        assertTrue(d2.isActual());
    }
}
