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
package org.netbeans.modules.versioning.history;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;

/**
 * Base class for actions return via {@link HistoryEntry#getActions()}.
 * 
 * @author Tomas Stupka
 */
public abstract class HistoryAction extends NodeAction {

    private Lookup context;
    private final String name;
    private final boolean multipleHistory;

    public HistoryAction() {
        name = null;
        multipleHistory = true;
    }
    public HistoryAction(String name) {
        this.name = name;
        this.multipleHistory = true;
    }
    public HistoryAction(String name, boolean multipleHistory) {
        this.name = name;
        this.multipleHistory = multipleHistory;
    }
    
    /**
     * Perform this action for the given HistoryEntry and files.
     * 
     * @param entry
     * @param value 
     */
    protected abstract void perform(HistoryEntry entry, Set<File> files);
    
    /**
     * Determines if this action should be enabled for single or multiple selection (1 or more HistoryEntries)
     * 
     * @return <code>true</code> in case this action should be enabled only for 1 selected <code>HistoryEntry</code>,
     *         <code>false</code> in case this action works for more than 1 <code>HistoryEntry</code>.
     * 
     */
    protected boolean isMultipleHistory() {
        return multipleHistory;
    };

    @Override
    public String getName() {
        assert name != null;
        return name;
    }
    
    protected HistoryEntry getHistoryEntry() {
        Collection<? extends Node> nodes = getContext().lookupAll(Node.class);
        HistoryEntry he = null;
        for(Node node : nodes) {
            he = node.getLookup().lookup(VCSHistoryProvider.HistoryEntry.class);
            if(he != null) {
                break;
            }
        }
        assert he != null;
        return he;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        this.context = actionContext;
        return super.createContextAwareInstance(actionContext);
    }

    /**
     * Returns the context for which this action was initialized.
     * 
     * @return 
     */
    protected Lookup getContext() {
        return context;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        Map<HistoryEntry, Set<File>> m = new HashMap<HistoryEntry, Set<File>>(activatedNodes.length);
        for(Node node : activatedNodes) {
            VCSHistoryProvider.HistoryEntry he = node.getLookup().lookup(VCSHistoryProvider.HistoryEntry.class);
            if(he == null) {
                continue;
            }                    

            Collection<? extends File> fos = node.getLookup().lookupAll(File.class);
            assert fos != null;  

            Set<File> files = m.get(he);
            if(files == null) {
                files = new HashSet<File>();
                m.put(he, files);
            }
            for (File f : fos) {
                if(f != null) {
                    files.add(f);
                }
            }
        }
        for(Map.Entry<HistoryEntry, Set<File>> e : m.entrySet()) {
            Set<File> files = e.getValue();
            if(files != null && !files.isEmpty()) {
                perform(e.getKey(), e.getValue());
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return hasEntryAndFiles(activatedNodes);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    private boolean hasEntryAndFiles(Node[] nodes) {
        boolean multipleHistory = isMultipleHistory();
        File file = null;
        VCSHistoryProvider.HistoryEntry historyEntry = null;
        for(Node node : nodes) {
            VCSHistoryProvider.HistoryEntry he = node.getLookup().lookup(VCSHistoryProvider.HistoryEntry.class);
            if(he == null) {
                continue;
            }                    
            if(historyEntry == null) {
                historyEntry = he;
            } else if(!multipleHistory) {
                if(!he.getDateTime().equals(historyEntry.getDateTime()) ||
                !he.getRevision().equals(historyEntry.getRevision())) 
                {
                    return false;
                }
            }
            Collection<? extends File> fos = node.getLookup().lookupAll(File.class);
            if(fos == null) {
                continue;
            }
            for (File f : fos) {
                if(f != null) {
                    file = f;
                    break;
                }
            }
            if(multipleHistory && historyEntry != null && file != null) {
                return true;
            }
        }
        return historyEntry != null && file != null;
    }
}
