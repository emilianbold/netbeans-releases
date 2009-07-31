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
public class DataraceImplTest {

    @Test
    public void testParse() {
        List<DataraceImpl> dataraces = DataraceImpl.fromErprint(new String[]{
        "",
        "Total Races:  1 Experiment:  /tmp/dlight_av202691/experiment_2.er",
        "",
        "Race #1, Vaddr: 0x804a040",
        "      Access 1: Write, work + 0x00000072,",
        "                       line 54 in \"pi_pthreads.c\"",
        "      Access 2: Write, work + 0x00000072,",
        "                       line 54 in \"pi_pthreads.c\"",
        "  Total Traces: 2",
        "  Trace 1",
        "      Access 1: Write",
        "                work + 0x00000072, line 54 in \"pi_pthreads.c\"",
        "                thread_hj_start_routine + 0x00000067",
        "                collector_root + 0x0000005A",
        "                start_thread + 0x000000B8",
        "                clone + 0x0000005C",
        "      Access 2: Write",
        "                work + 0x00000072, line 54 in \"pi_pthreads.c\"",
        "                thread_hj_start_routine + 0x00000067",
        "                collector_root + 0x0000005A",
        "                start_thread + 0x000000B8",
        "                clone + 0x0000005C",
        "  Trace 2",
        "      Access 1: Write",
        "                work + 0x00000072, line 54 in \"pi_pthreads.c\"",
        "                thread_hj_start_routine + 0x00000067",
        "                collector_root + 0x0000005A",
        "                start_thread + 0x000000B8",
        "                clone + 0x0000005C",
        "      Access 2: Write",
        "                work + 0x00000072, line 54 in \"pi_pthreads.c\"",
        "                main + 0x00000092, line 74 in \"pi_pthreads.c\"",
        "                __libc_start_main + 0x000000E2",
        "                _start + 0x0000003C"});
        assertEquals(1, dataraces.size());

        DataraceImpl r1 = dataraces.get(0);
        assertEquals(0x804a040, r1.getAddress());
    }
}
