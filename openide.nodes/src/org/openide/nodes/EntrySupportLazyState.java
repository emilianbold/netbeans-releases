/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.openide.nodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.nodes.Children.Entry;
import org.openide.nodes.EntrySupportLazy.EntryInfo;

/** This class should represent an immutable state of a EntrySupportLazy instance.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class EntrySupportLazyState implements Cloneable {
    static final EntrySupportLazyState UNINITIALIZED = new EntrySupportLazyState();
    
    private EntrySupportLazyState() {
    }
    
    private boolean inited;
    private Thread initThread;
    private boolean initInProgress;
    private boolean mustNotifySetEntries;
    
    private List<Entry> entries = Collections.emptyList();
    private List<Entry> visibleEntries = Collections.emptyList();
    private Map<Entry, EntryInfo> entryToInfo = new HashMap<Entry, EntryInfo>();
    
    
    final boolean isInited() {
        return inited;
    }
    
    final boolean isInitInProgress() {
        return initInProgress;
    }
    final Thread initThread() {
        return initThread;
    }
    
    final boolean isMustNotify() {
        return mustNotifySetEntries;
    }
    final List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }
    final List<Entry> getVisibleEntries() {
        return Collections.unmodifiableList(visibleEntries);
    }
    final Map<Entry, EntryInfo> getEntryToInfo() {
        return Collections.unmodifiableMap(entryToInfo);
    }
    
    private EntrySupportLazyState cloneState() {
        try {
            return (EntrySupportLazyState)clone();
        } catch (CloneNotSupportedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    final EntrySupportLazyState changeInited(boolean newInited) {
        EntrySupportLazyState newESLS = cloneState();
        newESLS.inited = newInited;
        return newESLS;
    }

    final EntrySupportLazyState changeThread(Thread t) {
        EntrySupportLazyState s = cloneState();
        s.initThread = t;
        return s;
    }

    final EntrySupportLazyState changeProgress(boolean b) {
        EntrySupportLazyState s = cloneState();
        s.initInProgress = b;
        return s;
    }
    final EntrySupportLazyState changeMustNotify(boolean b) {
        EntrySupportLazyState s = cloneState();
        s.mustNotifySetEntries = b;
        return s;
    }
    final EntrySupportLazyState changeEntries(
        List<Entry> entries, 
        List<Entry> visibleEntries,
        Map<Entry, EntryInfo> entryToInfo
    ) {
        EntrySupportLazyState state = cloneState();
        if (entries != null) {
            state.entries = entries;
        }
        if (visibleEntries != null) {
            state.visibleEntries = visibleEntries;
        }
        if (entryToInfo != null) {
            state.entryToInfo = entryToInfo;
        }
        int entriesSize = 0;
        int entryToInfoSize = 0;
        assert (entriesSize = state.getEntries().size()) >= 0;
        assert (entryToInfoSize = state.getEntryToInfo().size()) >= 0;
        assert state.getEntries().size() == state.getEntryToInfo().size() : "Entries: " + state.getEntries().size() + "; vis. entries: " + EntrySupportLazy.notNull(state.getVisibleEntries()).size() + "; Infos: " + state.getEntryToInfo().size() + "; entriesSize: " + entriesSize + "; entryToInfoSize: " + entryToInfoSize + EntrySupportLazy.dumpEntriesInfos(state.getEntries(), state.getEntryToInfo()); // NOI18N
        return state;
    }

    @Override
    public String toString() {
        int entriesSize = getEntries().size();
        int entryToInfoSize = getEntryToInfo().size();
        return 
    
            "Inited: " + inited +
            "\nThread: " + initThread +
            "\nInProgress: " + initInProgress +
            "\nMustNotify: " + mustNotifySetEntries +
            "\nEntries: " + getEntries().size() + "; vis. entries: " + 
            EntrySupportLazy.notNull(getVisibleEntries()).size() + "; Infos: " + 
            getEntryToInfo().size() + "; entriesSize: " + 
            entriesSize + "; entryToInfoSize: " + entryToInfoSize + 
            EntrySupportLazy.dumpEntriesInfos(getEntries(), getEntryToInfo());
    }
}
