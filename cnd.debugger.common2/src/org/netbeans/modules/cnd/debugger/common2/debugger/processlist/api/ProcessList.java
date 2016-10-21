/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;

/**
 *
 * @author ak119685
 */
public final class ProcessList {

    private final HashMap<Integer, ProcessInfo> data;
    private final ExecutionEnvironment execEnv;

    /*package*/ ProcessList(final Collection<ProcessInfo> info, ExecutionEnvironment execEnv) {
        this.data = new HashMap<Integer, ProcessInfo>(info.size());
        this.execEnv = execEnv;
        for (ProcessInfo i : info) {
            this.data.put(i.getPID(), i);
        }
    }

    public ProcessInfo getInfo(Integer pid) {
        return data.get(pid);
    }

    public Collection<Integer> getPIDs() {
        return data.keySet();
    }
        
//    public Collection<Integer> getExecutablePIDs(String executable) {
//        ArrayList<Integer> result = new ArrayList<Integer>();
//        if (executable == null) {
//            return result;
//        }
//        for (Integer pid : getPIDs()) {
//            ProcessInfo info = getInfo(pid);
//            String infoExecutable = info.getExecutable();
//            if (!infoExecutable.equals(executable)) {
//                infoExecutable = FileSystemProvider.normalizeAbsolutePath(infoExecutable, execEnv);
//            }
//            if (infoExecutable.equals(executable)) {
//                result.add(pid);
//            }
//        }
//
//        return result;
//    }
    
//    public Collection<Integer> getPIDs(String filter) {
//        ArrayList<Integer> result = new ArrayList<Integer>();
//        if (filter == null || filter.isEmpty()) {
//            return result;
//        }
//        for (Integer pid : getPIDs()) {
//            ProcessInfo info = getInfo(pid);
//
//            if (info.matches(filter)) {
//                result.add(pid);
//            }
//        }
//
//        return result;
//    }
    
//    public Collection<Integer> getPIDs(String descriptor_id, String filter) {
//        ArrayList<Integer> result = new ArrayList<Integer>();
//        if (filter == null || filter.isEmpty()) {
//            return result;
//        }
//        for (Integer pid : getPIDs()) {
//            ProcessInfo info = getInfo(pid);
//
//            if (info.matches(descriptor_id, filter)) {
//                result.add(pid);
//            }
//        }
//
//        return result;
//    }    

    public Collection<Integer> getPIDs(Integer ppid) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (Integer pid : getPIDs()) {
            if (getInfo(pid).getPPID().equals(ppid)) {
                result.add(pid);
            }
        }

        return result;
    }
}

