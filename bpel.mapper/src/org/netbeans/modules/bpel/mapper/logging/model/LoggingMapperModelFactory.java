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

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.mapper.logging.tree.LogAlertType;
import org.netbeans.modules.bpel.mapper.logging.tree.model.LoggingAlertingTreeModel;
import org.netbeans.modules.bpel.mapper.model.BpelChangeProcessor;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModelFactory;
import org.netbeans.modules.bpel.mapper.model.FromProcessor;
import org.netbeans.modules.bpel.mapper.model.EditorExtensionProcessor;
import org.netbeans.modules.bpel.mapper.model.EditorExtensionProcessor.BpelEditorExtensions;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.EmptyTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.PartnerLinkTreeExtModel;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeModel;
import org.netbeans.modules.bpel.mapper.tree.search.LoggingNodeFinder;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.FromHolder;
import org.netbeans.modules.bpel.model.ext.logging.api.Alert;
import org.netbeans.modules.bpel.model.ext.logging.api.Location;
import org.netbeans.modules.bpel.model.ext.logging.api.Log;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;

/**
 * Implementaiton of the MapperModelFactory for the Logging mapper.
 * 
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class LoggingMapperModelFactory extends BpelMapperModelFactory {

    public static boolean needShowMapper(Object source) {
            return source instanceof ExtensibleElements;
    }

    public LoggingMapperModelFactory(MapperTcContext mapperTcContext, 
        BpelDesignContext context) {
        super(mapperTcContext, context);
    }
    
    @Override
    public MapperModel constructModel() {
        //
        Mapper mapper = currentMapperTcContext.getMapper();
        BpelChangeProcessor changeProcessor = new BpelChangeProcessor(
                mapper, new LoggingBpelModelUpdater(currentMapperTcContext));
        //
//        BpelEntity bpelEntity = context.getBpelEntity();
        BpelEntity bpelEntity = currentBpelDesignContext.getContextEntity();
        if (!(bpelEntity instanceof ExtensibleElements)) {
            return null;
        }
        LoggingAlertingTreeModel targetModel = 
                new LoggingAlertingTreeModel((ExtensibleElements)bpelEntity);

        EmptyTreeModel sourceModel = new EmptyTreeModel();

        VariableTreeModel sourceVariableModel = 
                new VariableTreeModel(currentBpelDesignContext, true, mapper);
        sourceModel.addExtensionModel(sourceVariableModel);
        PartnerLinkTreeExtModel pLinkExtModel = 
                new PartnerLinkTreeExtModel(bpelEntity, true);
        sourceModel.addExtensionModel(pLinkExtModel);
        //
        BpelMapperModel newMapperModel = new BpelMapperModel(
                currentMapperTcContext, changeProcessor, sourceModel, targetModel);
        //
        editorExtProcessor = new EditorExtensionProcessor(newMapperModel, currentBpelDesignContext);
        editorExtProcessor.processVariables();
        //
        addTraceGraph((ExtensibleElements)bpelEntity, newMapperModel);
        postProcess(newMapperModel);
        //
        return newMapperModel;
    }

    // todo m
    private void addTraceGraph(ExtensibleElements entity, BpelMapperModel newMapperModel) {
        assert entity != null && newMapperModel != null;
        List<Trace> traces = (entity).getChildren(Trace.class);
        if (traces != null && traces.size() > 0) {
            Trace trace = traces.get(0);
            assert trace != null;
            Log[] logs = trace.getLogs();
            if (logs != null && logs.length > 0) {
                for (Log log : logs) {
                    BpelEditorExtensions extList = editorExtProcessor.getExtList(log);
                    From from = log.getFrom();
                    if (from != null) {
                        addFromGraph(log, newMapperModel, 
                                LogAlertType.LOG, log.getLocation(), 
                                log.getLevel(), entity, extList);
                    }
                }
            }

            Alert[] alerts = trace.getAlerts();
            if (alerts != null && alerts.length > 0) {
                for (Alert alert : alerts) {
                    BpelEditorExtensions extList = editorExtProcessor.getExtList(alert);
                    From from = alert.getFrom();
                    if (from != null) {
                        addFromGraph(alert, newMapperModel, 
                                LogAlertType.ALERT, alert.getLocation(), 
                                alert.getLevel(), entity, extList);
                    }
                }
            }
        }
    }
    
    private void addFromGraph(FromHolder fromHolder, 
            BpelMapperModel newMapperModel, 
            LogAlertType type,
            Location location,
            Object level,
            BpelEntity contextEntity, 
            BpelEditorExtensions extList) {
        assert fromHolder != null && newMapperModel != null && type != null && location != null && level != null;
        if (fromHolder.getFrom() == null) {
            return;
        }
        //
        Graph newGraph = new Graph(newMapperModel);
        //
        FromProcessor fromProcessor = new FromProcessor(this, fromHolder);
        MapperSwingTreeModel leftTreeModel = newMapperModel.getLeftTreeModel();
        newGraph = fromProcessor.populateGraph(newGraph, leftTreeModel, extList);
        if (newGraph == null) {
            return;
        }

        List<TreeItemFinder> finderList = Collections.singletonList(
                (TreeItemFinder)new LoggingNodeFinder(type, location, level));
        //
        PreprocessedGraphLocation graphLocation = 
                new PreprocessedGraphLocation(newGraph, finderList);
        mPreprGraphLocationList.add(graphLocation);
    }
}
