/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.storage.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.netbeans.modules.dlight.core.stack.api.CallStackEntry;
import org.netbeans.modules.dlight.core.stack.api.Function;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.spi.support.SQLRequest;
import org.netbeans.modules.dlight.spi.support.SQLRequestsProcessor;

/**
 *
 * @author ak119685
 */
class DBProxy {

    private final SQLRequestsProcessor requestsProcessor;
    private final SQLStackRequestsProvider requestsProvider;
    private final HashMap<Long, StackNode> stackNodesCache = new HashMap<Long, StackNode>();
    private final HashMap<Long, CallStackEntry> callStackEntriesCache = new HashMap<Long, CallStackEntry>();
    private final HashMap<CharSequence, StackNode> stackEntryToStackNode = new HashMap<CharSequence, StackNode>();
    private final HashMap<CharSequence, Long> funcNameToFuncID = new HashMap<CharSequence, Long>();
    private final HashMap<CharSequence, Long> fileNameToFileID = new HashMap<CharSequence, Long>();
    private final HashMap<CharSequence, Long> moduleNameToModuleID = new HashMap<CharSequence, Long>();
    private final AtomicLong lastNodeID = new AtomicLong();
    private final AtomicLong lastFuncID = new AtomicLong();
    private final AtomicLong lastFileID = new AtomicLong();
    private final AtomicLong lastModuleID = new AtomicLong();

    DBProxy(SQLRequestsProcessor requestsProcessor, SQLStackRequestsProvider requestsProvider) {
        this.requestsProcessor = requestsProcessor;
        this.requestsProvider = requestsProvider;
    }

    void shutdown() {
    }

    /**
     * Either returns already known or new id of a stack node.
     * 
     * @param stackEntry - entry of a call stack
     * @param callerID - id of previous entry
     * @param isNewNode (out) - true if this is a new entry. This means that other tables also 
     * need to be populated with (for example) source files info.. 
     * false if returned ID is a knonw one
     * @return object that represent a node
     */
    StackNode getNodeID(CallStackEntry entry, long callerID, AtomicBoolean isNewNode) {
        boolean isnew = false;
        StackNode node;

        synchronized (stackEntryToStackNode) {
            node = stackEntryToStackNode.get(entry.getOriginalEntry().toString() + callerID);

            if (node == null) {
                isnew = true;
                node = insertNode(entry, callerID);
            }
        }

        if (isNewNode != null) {
            isNewNode.set(isnew);
        }

        return node;
    }

    void updateNodeMetrics(long leafNodeID, long contextID, long timestamp, long duration) {
        long bucket = timeToBucket(timestamp);
        SQLRequest request = requestsProvider.updateNodeMetrics(leafNodeID, contextID, bucket, duration);
        requestsProcessor.queueRequest(request);
    }

    void updateFuncMetrics(long funcID, long contextID, long timestamp, long duration, boolean addInclusive, boolean addExclusive) {
        long bucket = timeToBucket(timestamp);
        SQLRequest request = requestsProvider.updateFunctionMetrics(funcID, contextID, bucket, duration, addInclusive, addExclusive);
        requestsProcessor.queueRequest(request);
    }

    private StackNode insertNode(CallStackEntry entry, long callerID) {
        Long funcID;

        synchronized (funcNameToFuncID) {
            funcID = funcNameToFuncID.get(entry.getFunctionName());
            if (funcID == null) {
                funcID = lastFuncID.incrementAndGet();
                funcNameToFuncID.put(entry.getFunctionName(), funcID);
                SQLRequest request = requestsProvider.addFunction(funcID, entry.getFunctionName());
                requestsProcessor.queueRequest(request);
            }
        }

        long offset = entry.getOffsetInFunction();
        Long nodeID = lastNodeID.incrementAndGet();
        StackNode node = new StackNode(nodeID, callerID, funcID, offset);
        stackNodesCache.put(node.nodeID, node);
        callStackEntriesCache.put(nodeID, entry);
        stackEntryToStackNode.put(entry.getOriginalEntry().toString() + callerID, node);
        SQLRequest request = requestsProvider.addNode(nodeID, callerID, funcID, offset);
        requestsProcessor.queueRequest(request);

        return node;
    }

    void addSourceInfo(StackNode node, long contextID, SourceFileInfo sourceFileInfo) {
        if (sourceFileInfo == null) {
            return;
        }

        String path = sourceFileInfo.getFileName();
        int line = sourceFileInfo.getLine();
        int column = sourceFileInfo.getColumn();
        long offset = sourceFileInfo.getOffset();

        Long fileID;

        synchronized (fileNameToFileID) {
            fileID = fileNameToFileID.get(path);
            if (fileID == null) {
                fileID = lastFileID.incrementAndGet();
                fileNameToFileID.put(path, fileID);
                SQLRequest request = requestsProvider.addFile(fileID, path);
                requestsProcessor.queueRequest(request);
            }
        }

        SQLRequest request = requestsProvider.addSourceInfo(node.nodeID, contextID, fileID, line, column, offset);
        requestsProcessor.queueRequest(request);
    }

    void addModuleInfo(StackNode node, long contextID, CharSequence module, long offsetInModule) {
        if (module == null) {
            return;
        }

        Long moduleID;
        synchronized (moduleNameToModuleID) {
            moduleID = moduleNameToModuleID.get(module);
            if (moduleID == null) {
                moduleID = lastModuleID.incrementAndGet();
                moduleNameToModuleID.put(module, moduleID);
                SQLRequest request = requestsProvider.addModule(moduleID, module);
                requestsProcessor.queueRequest(request);
            }
        }

        SQLRequest request = requestsProvider.addModuleInfo(node.nodeID, contextID, moduleID, offsetInModule);
        requestsProcessor.queueRequest(request);
    }

    private String getFunctionNameByFuncID(long funcId) {
        synchronized (funcNameToFuncID) {
            Iterator<CharSequence> it = funcNameToFuncID.keySet().iterator();
            while (it.hasNext()) {
                String name = it.next().toString();
                if (funcNameToFuncID.get(name).longValue() == funcId) {
                    return name;
                }
            }
        }
        return null;
    }

    Function getLeafFunction(long stackId) {
        //find the StackNode
        StackNode stackNode = stackNodesCache.get(stackId);
        long nodeID = stackNode.nodeID;
        long funcID = stackNode.funcID;
        long offset = stackNode.offset;
        String funcName = getFunctionNameByFuncID(funcID);
        CallStackEntry entry = callStackEntriesCache.get(stackId);
        StringBuilder qname = new StringBuilder();
        String module = entry.getModulePath().toString();
        long offsetInModule = entry.getOffsetInModule();
        SourceFileInfo sourceFileInfo = entry.getSourceFileInfo();
        if (sourceFileInfo == null) {
            sourceFileInfo = new SourceFileInfo(null, -1);//unknown file
        }
        String srcFile = sourceFileInfo.getFileName();
        long srcLine = sourceFileInfo.getLine();
        long srcColumn = sourceFileInfo.getColumn();
        String moduleOffset = offsetInModule < 0 ? null : "0x" + Long.toHexString(offsetInModule); // NOI18N

        if (module != null) {
            qname.append(module);
            if (moduleOffset != null) {
                qname.append('+').append(moduleOffset);
            }
            qname.append('`');
        }

        qname.append(funcName);

        if (offset > 0) {
            qname.append("+0x").append(Long.toHexString(offset)); // NOI18N
        }

        if (srcFile != null) {
            qname.append(':').append(srcFile);
            if (srcLine > 0) {
                qname.append(':').append(srcLine);
                if (srcColumn > 0) {
                    qname.append(':').append(srcColumn);
                }
            }
        }

        FunctionImpl func = new FunctionImpl(funcID, -1, funcName, qname.toString(), module, moduleOffset, srcFile);

        return func;

    }

    /**
     *
     * @param timestamp  in nanoseconds
     * @return bucket id
     */
    static long timeToBucket(long timestamp) {
        return TimeUnit.NANOSECONDS.toSeconds(timestamp);  // bucket is 1 second
    }

    static class StackNode {

        final long nodeID;
        final long callerID;
        final long funcID;
        final long offset;

        public StackNode(long nodeID, long callerID, long funcID, long offset) {
            this.nodeID = nodeID;
            this.callerID = callerID;
            this.funcID = funcID;
            this.offset = offset;
        }
    }
}
