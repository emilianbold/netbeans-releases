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
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.EmptyTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.PartnerLinkTreeExtModel;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeModel;
import org.netbeans.modules.bpel.mapper.tree.search.LoggingNodeFinder;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemFinder;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.Expression;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.ext.logging.api.Alert;
import org.netbeans.modules.bpel.model.ext.logging.api.Location;
import org.netbeans.modules.bpel.model.ext.logging.api.Log;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.MapperModel;

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

//    private interface myLeftTreeSpecialCreator {
//        boolean accept(Class entityClass);
//        void addGraph(MapperModel mapperModel);
//        MapperTreeModel getSourceTree();
//    }
    
    
    public LoggingMapperModelFactory() {
        super();
    }
    
    @Override
    public MapperModel constructModel(
            MapperTcContext mapperTcContext, BpelDesignContext context) {
        //
        BpelChangeProcessor changeProcessor = 
                new BpelChangeProcessor(new LoggingBpelModelUpdater(mapperTcContext));
        mapperTcContext.getDesignContextController().
                setBpelModelUpdateSource(changeProcessor);
        //
//        BpelEntity bpelEntity = context.getBpelEntity();
        BpelEntity bpelEntity = context.getContextEntity();
        if (!(bpelEntity instanceof ExtensibleElements)) {
            return null;
        }
        LoggingAlertingTreeModel targetModel = 
                new LoggingAlertingTreeModel((ExtensibleElements)bpelEntity);
        
//        if (bpelEntity instanceof Copy) {
//            Copy copy = (Copy)bpelEntity;
//            //
//            EmptyTreeModel sourceModel = new EmptyTreeModel();
//            VariableTreeModel sourceVariableModel = new VariableTreeModel(context);
//            sourceModel.addExtensionModel(sourceVariableModel);
//            PartnerLinkTreeExtModel pLinkExtModel = 
//                    new PartnerLinkTreeExtModel(copy, true);
//            sourceModel.addExtensionModel(pLinkExtModel);
//            //
//            BpelMapperModel newMapperModel = new BpelMapperModel(
//                    mapperTcContext, changeProcessor, sourceModel, targetModel);
//            //
//            addTraceGraph(copy, newMapperModel);
//            postProcess(newMapperModel);
//            //
//            return newMapperModel;
//            //
//        } else if (bpelEntity instanceof Assign) {
//            Assign assign = (Assign)bpelEntity;
//            //
//            EmptyTreeModel sourceModel = new EmptyTreeModel();
//            VariableTreeModel variableModel = new VariableTreeModel(context);
//            sourceModel.addExtensionModel(variableModel);
//            PartnerLinkTreeExtModel pLinkExtModel = 
//                    new PartnerLinkTreeExtModel(assign, true);
//            sourceModel.addExtensionModel(pLinkExtModel);
//            //
//            BpelMapperModel newMapperModel = new BpelMapperModel(
//                    mapperTcContext, changeProcessor, sourceModel, targetModel);
//            addTraceGraph(assign, newMapperModel);
//            postProcess(newMapperModel);
//            //
//            return newMapperModel;
//            //
//        } else {
            //
            EmptyTreeModel sourceModel = new EmptyTreeModel();
            VariableTreeModel sourceVariableModel = new VariableTreeModel(context);
            sourceModel.addExtensionModel(sourceVariableModel);
            PartnerLinkTreeExtModel pLinkExtModel = 
                    new PartnerLinkTreeExtModel(bpelEntity, true);
            sourceModel.addExtensionModel(pLinkExtModel);
            //
            BpelMapperModel newMapperModel = new BpelMapperModel(
                    mapperTcContext, changeProcessor, sourceModel, targetModel);

            addTraceGraph((ExtensibleElements)bpelEntity, newMapperModel);
            postProcess(newMapperModel);
            //
            return newMapperModel;
//        }
        //
//        return null;
    }

    private void addTraceGraph(ExtensibleElements entity, BpelMapperModel newMapperModel) {
        assert entity != null && newMapperModel != null;
            List<Trace> traces = (entity).getChildren(Trace.class);
            if (traces != null && traces.size() > 0) {
                Trace trace = traces.get(0);
                assert trace != null;
                Log[] logs = trace.getLogs();
                if (logs != null && logs.length > 0) {
                    for (Log log : logs) {
                        From from = log.getFrom();
                        if (from != null) {
                            addExpressionGraph(from, newMapperModel, 
                                    LogAlertType.LOG, log.getLocation(), log.getLevel(), entity);
                        }
                    }
                }

                Alert[] alerts = trace.getAlerts();
                if (alerts != null && alerts.length > 0) {
                    for (Alert alert : alerts) {
                        From from = alert.getFrom();
                        if (from != null) {
                            addExpressionGraph(from, newMapperModel, 
                                    LogAlertType.ALERT, alert.getLocation(), alert.getLevel(), entity);
                        }
                    }
                }
            }
    }
    
    private void addExpressionGraph(Expression expr, 
            BpelMapperModel newMapperModel, 
            LogAlertType type,
            Location location,
            Object level,
            BpelEntity contextEntity) {
        assert expr != null && newMapperModel != null && type != null && location != null && level != null;
        //
        Graph newGraph = new Graph(newMapperModel);
        //
        MapperSwingTreeModel leftTreeModel = newMapperModel.getLeftTreeModel();
        populateGraph(newGraph, leftTreeModel, contextEntity, expr);
        //
        List<TreeItemFinder> finderList = Collections.singletonList(
                (TreeItemFinder)new LoggingNodeFinder(type, location, level));
        //
        PreprocessedGraphLocation graphLocation = 
                new PreprocessedGraphLocation(newGraph, finderList);
        mPreprGraphLocationList.add(graphLocation);
    }
}
