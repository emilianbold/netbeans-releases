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
package org.netbeans.modules.dlight.visualizers.threadmap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadState.MSAState;

/**
 *
 * @author Alexander Simon
 */
final class OrderedEnumStateIterator implements Iterator<Map.Entry<MSAState, AtomicInteger>> {

    private final Iterator<Map.Entry<MSAState, AtomicInteger>> iterator;

    OrderedEnumStateIterator(EnumMap<MSAState, AtomicInteger> map) {
        List<Map.Entry<MSAState, AtomicInteger>> list = new ArrayList<Map.Entry<MSAState, AtomicInteger>>(20);
        add(list, MSAState.ThreadFinished, map);
        add(list, MSAState.Running, map);
            add(list, MSAState.RunningUser, map);
            add(list, MSAState.RunningSystemCall, map);
            add(list, MSAState.RunningOther, map);
        add(list, MSAState.Blocked, map);
            add(list, MSAState.SleepingUserLock, map);
        add(list, MSAState.Waiting, map);
            add(list, MSAState.WaitingCPU, map);
        add(list, MSAState.Sleeping, map);
            add(list, MSAState.SleepingUserDataPageFault, map);
            add(list, MSAState.SleepingUserTextPageFault, map);
            add(list, MSAState.SleepingKernelPageFault, map);
            add(list, MSAState.SleepingOther, map);
        add(list, MSAState.Stopped, map);
            add(list, MSAState.ThreadStopped, map);
        iterator = list.iterator();
    }

    private void add(List<Map.Entry<MSAState, AtomicInteger>> list, final MSAState state, EnumMap<MSAState, AtomicInteger> map) {
        final AtomicInteger i = map.get(state);
        if (i != null) {
            list.add(new Entry<MSAState, AtomicInteger>() {

                public MSAState getKey() {
                    return state;
                }

                public AtomicInteger getValue() {
                    return i;
                }

                public AtomicInteger setValue(AtomicInteger value) {
                    throw new UnsupportedOperationException();
                }
            });
        }
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Entry<MSAState, AtomicInteger> next() {
        return iterator.next();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
