/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.debugger.common2.ui.processlist;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessList;

/**
 *
 * @author ak119685
 */
public final class ProcessListTreeModel {

    private final Set<Integer> trackedList;
    private final Entry rootEntry;
    private final ProcessList processList;

    public ProcessListTreeModel(ProcessList processList, String filter) {
        this.processList = processList;        
        Collection<Integer> filteredLeafPIDs = processList.getPIDs(filter);
        trackedList = new HashSet<Integer>(filteredLeafPIDs);
        Set<Integer> trackedRoots = new HashSet<Integer>();

        Integer ppid;
        Integer apid;
        for (Integer pid : filteredLeafPIDs) {
            apid = pid;
            while ((ppid = processList.getInfo(apid).getPPID()) > 1) {
                trackedList.add(ppid);
                apid = ppid;
            }

            if (apid > 5) {
                trackedRoots.add(apid);
            }
        }

        rootEntry = new Entry(1);
    }

    public Entry getRootEntry() {
        return rootEntry;
    }

    public class Entry {

        public final Integer pid;
        public final List<Entry> children;

        public Entry(Integer pid) {
            ArrayList<Entry> clist = new ArrayList<Entry>();
            this.pid = pid;
            for (Integer cpid : trackedList) {
                if (processList.getInfo(cpid).getPPID().equals(pid)) {
                    clist.add(new Entry(cpid));
                }
            }
            children = Collections.unmodifiableList(clist);
        }

        public List<Integer> getChildrenPIDs() {
            List<Integer> result = new ArrayList<Integer>();
            for (Entry e : children) {
                result.add(e.pid);
            }
            return result;
        }

        Entry getChild(Integer pid) {
            for (Entry e : children) {
                if (e.pid.equals(pid)) {
                    return e;
                }
            }

            return null;
        }
    }
}
