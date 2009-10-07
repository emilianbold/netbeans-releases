/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.mapper.logging.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.cast.AbstractTypeCast;
import org.netbeans.modules.bpel.mapper.logging.tree.AlertItem;
import org.netbeans.modules.bpel.mapper.logging.tree.LogItem;
import org.netbeans.modules.bpel.mapper.model.BpelModelUpdater;
import org.netbeans.modules.bpel.mapper.model.GraphInfoCollector;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.ext.logging.api.Alert;
import org.netbeans.modules.bpel.model.ext.logging.api.AlertLevel;
import org.netbeans.modules.bpel.model.ext.logging.api.Location;
import org.netbeans.modules.bpel.model.ext.logging.api.Log;
import org.netbeans.modules.bpel.model.ext.logging.api.LogLevel;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.openide.util.NbBundle;

/**
 * 
 * 
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class LoggingBpelModelUpdater extends BpelModelUpdater {
        
    private static final Logger LOGGER = Logger.getLogger(LoggingBpelModelUpdater.class.getName());
//    private BpelMapperModel mMapperModel;
//    private BpelDesignContext mDesignContext;
//    private TreePath mTreePath;
    
    public LoggingBpelModelUpdater(MapperTcContext mapperTcContext) {
        super(mapperTcContext);
    }

    @Override
    public Object updateOnChanges(TreePath treePath) throws Exception {
        //
//        BpelEntity bpelEntity = getDesignContext().getBpelEntity();
        BpelEntity bpelEntity = getDesignContext().getContextEntity();
        //
        if (bpelEntity instanceof ExtensibleElements) {
            updateExtensileElements(treePath, (ExtensibleElements)bpelEntity);
        }
        //
        return null; // TODO: return some result flag
    }
    
    //==========================================================================

    private Log findLog(Trace trace, Location location, LogLevel level) {
        assert trace != null && location != null && level != null;
        Log[] logs = trace.getLogs();
        Log rightLog = null;
        if (logs != null) {
            for (Log log : logs) {
                if (level != null && level.equals(log.getLevel())
                        && location != null && location.equals(log.getLocation()))
                {
                    rightLog = log;
                    break;
                } 
            }
        }
        return rightLog;
    }
    
    private Alert findAlert(Trace trace, Location location, AlertLevel level) {
        assert trace != null && location != null && level != null;
        Alert[] alerts = trace.getAlerts();
        Alert rightAlert = null;
        if (alerts != null) {
            for (Alert alert : alerts) {
                if (level != null && level.equals(alert.getLevel())
                        && location != null && location.equals(alert.getLocation()))
                {
                    rightAlert = alert;
                    break;
                } 
            }
        }
        return rightAlert;
    }

    private void updateExtensileElements(TreePath rightTreePath,
            ExtensibleElements extensibleElement) throws Exception 
    {
        assert extensibleElement != null;

        ExtensibleElements editorExtensibleElement = null;
        //
        // Do common preparations
        //
        Graph graph = getMapperModel().graphRequired(rightTreePath);        
        //
        GraphInfoCollector graphInfo = new GraphInfoCollector(graph);
        BpelModel bpelModel = extensibleElement.getBpelModel();
        
        Trace trace = null;
        // 125695
        // remove trace if there is not content
        if (graph.isEmpty()) {
            trace = getTrace(extensibleElement, false);
            if (trace == null) {
                return;
            }
            removeEmptyGraph(trace, rightTreePath);
            return;
        }

        trace = trace == null ? getTrace(extensibleElement) : trace;
        if (trace == null) {
            return;
        }

        Object rightTreeDO = MapperSwingTreeModel.getDataObject(rightTreePath);
        From bpelExpr = null;

        if (rightTreeDO instanceof LogItem) {
            LogLevel level = ((LogItem)rightTreeDO).getLevel();
            Location location = ((LogItem)rightTreeDO).getLocation();
            Log rightLog = findLog(trace, location, level);
            if (rightLog == null) {
                rightLog = bpelModel.getBuilder().createExtensionEntity(Log.class);
                rightLog.setLevel(level);
                rightLog.setLocation(location);
                trace.addLog(rightLog);
                rightLog = findLog(trace, location, level);
            }
            if (rightLog == null) {
                return;
            }
            editorExtensibleElement = rightLog;
            
            From from = rightLog.getFrom();
            if (from == null) {
                from = bpelModel.getBuilder().createFrom();
                rightLog.setFrom(from);
                from = rightLog.getFrom();
            }
            bpelExpr = from;
        } else if (rightTreeDO instanceof AlertItem) {
            AlertLevel level = ((AlertItem)rightTreeDO).getLevel();
            Location location = ((AlertItem)rightTreeDO).getLocation();
            Alert rightAlert = findAlert(trace, location, level);
            if (rightAlert == null) {
                rightAlert = bpelModel.getBuilder().createExtensionEntity(Alert.class);
                rightAlert.setLevel(level);
                rightAlert.setLocation(location);
                trace.addAlert(rightAlert);
                rightAlert = findAlert(trace, location, level);
            }
            if (rightAlert == null) {
                return;
            }
            editorExtensibleElement = rightAlert;

            From from = rightAlert.getFrom();
            if (from == null) {
                from = bpelModel.getBuilder().createFrom();
                rightAlert.setFrom(from);
                from = rightAlert.getFrom();
            }
            bpelExpr = from;
        } else {
            return;
        }
        //
        // Populate 
        Set<AbstractTypeCast> typeCastCollector = new HashSet<AbstractTypeCast>();
        Set<XPathPseudoComp> pseudoCollector = new HashSet<XPathPseudoComp>();

        updateFrom(graph, typeCastCollector, pseudoCollector, bpelExpr);
        
//        populateContentHolder(bpelExpr, graphInfo, typeCastCollector);
        if (editorExtensibleElement != null) {
            registerTypeCasts(editorExtensibleElement, typeCastCollector, true);
        } else {
            LOGGER.log(Level.WARNING, NbBundle.getMessage(
                    LoggingBpelModelUpdater.class, "MSG_WarningNoEditorExtensibleElement")); // NOI18N
        }
    }

    // todo m
    private void removeEmptyGraph(Trace trace, TreePath rightTreePath ) {
        assert trace != null;
        assert rightTreePath != null;
        
        Object rightTreeDO = MapperSwingTreeModel.getDataObject(rightTreePath);

        if (rightTreeDO instanceof LogItem) {
            LogLevel level = ((LogItem)rightTreeDO).getLevel();
            Location location = ((LogItem)rightTreeDO).getLocation();
            Log rightLog = findLog(trace, location, level);
            if (rightLog != null) {
                trace.remove(rightLog);
            }
        } else if (rightTreeDO instanceof AlertItem) {
            AlertLevel level = ((AlertItem)rightTreeDO).getLevel();
            Location location = ((AlertItem)rightTreeDO).getLocation();
            Alert rightAlert = findAlert(trace, location, level);
            if (rightAlert != null) {
                trace.remove(rightAlert);
            }
        }

        List<BpelEntity> traceChildren = trace.getChildren();
        if (traceChildren == null || traceChildren.isEmpty()) {
            BpelContainer parent = trace.getParent();
            if (parent != null) {
                parent.remove(trace);
            }
        }
        getMapperModel().deleteGraph(rightTreePath); // Remove empty graph !!!
    }
    
    private Trace getTrace(ExtensibleElements extensibleElement) {
        return getTrace(extensibleElement, true);
    }

    private Trace getTrace(ExtensibleElements extensibleElement, boolean create) {
        List<Trace> traces = extensibleElement.getChildren(Trace.class);
        if (traces != null && traces.size() > 0) {
            return traces.get(0);
        } 

        if (!create) {
            return null;
        }
        
        BpelModel bpelModel = extensibleElement.getBpelModel();
        if (bpelModel == null) {
            return null;
        }
//        Process process = bpelModel.getProcess();
//        try {
//            System.out.println("going to add namespace context");
//            process.getNamespaceContext().
//                    addNamespace(Trace.LOGGING_NAMESPACE_URI);
//        } catch (InvalidNamespaceException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        try {
//            bpelModel.sync();
//        } catch (IOException ex) {
//            ErrorManager.getDefault().notify(ex);
//        }
        
        Trace newTrace = bpelModel.getBuilder().createExtensionEntity(Trace.class);
        extensibleElement.addExtensionEntity(Trace.class, newTrace);
        traces = extensibleElement.getChildren(Trace.class);

        return traces != null && traces.size() > 0 ? traces.get(0) : null;
    }
}
    
