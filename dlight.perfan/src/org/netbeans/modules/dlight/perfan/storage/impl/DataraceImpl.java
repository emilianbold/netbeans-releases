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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.dlight.api.stack.Datarace;
import org.netbeans.modules.dlight.api.stack.ThreadDump;

/**
 * @author Alexey Vladykin
 */
public final class DataraceImpl implements Datarace {

    private final int id;
    private final long address;

    public DataraceImpl(int id, long address) {
        this.id = id;
        this.address = address;
    }

    public long getAddress() {
        return address;
    }

    public List<ThreadDump> getThreadDumps() {
        return Collections.emptyList();
    }


    private static final Pattern RACE_PATTERN = Pattern.compile("Race #(\\d+), Vaddr: 0x(.+)"); // NOI18N

    public static List<DataraceImpl> fromErprint(String[] lines) {
        List<DataraceImpl> races = new ArrayList<DataraceImpl>();
        for (String line : lines) {
            Matcher m = RACE_PATTERN.matcher(line);
            if (m.matches()) {
                int id;
                try {
                    id = Integer.parseInt(m.group(1));
                } catch (NumberFormatException ex) {
                    id = -1;
                }
                long vaddr;
                try {
                    vaddr = Long.parseLong(m.group(2), 16);
                } catch (NumberFormatException ex) {
                    vaddr = -1;
                }
                races.add(new DataraceImpl(id, vaddr));
            }
        }
        return races;
    }
}
