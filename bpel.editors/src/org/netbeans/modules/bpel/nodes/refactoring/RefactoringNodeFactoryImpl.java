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
package org.netbeans.modules.bpel.nodes.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.bpel.editors.api.nodes.RefactoringNodeFactory;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.xam.Component;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.editors.api.nodes.RefactoringNodeFactory.class)
public class RefactoringNodeFactoryImpl implements RefactoringNodeFactory {
    private static AtomicReference<RefactoringNodeFactory> FACTORY_INSTANCE =
            new AtomicReference<RefactoringNodeFactory>();
    
    private static Map<UsageNodeType, RefactoringNodeCreator> TYPE_CREATOR_MAP =
            new HashMap<UsageNodeType, RefactoringNodeCreator>();
    
    static {
        TYPE_CREATOR_MAP.put(UsageNodeType.USAGE_CONTEXT,
                new UsageContextNodeCreator());
        TYPE_CREATOR_MAP.put(UsageNodeType.USAGE_OBJECT,
                new UsageObjectNodeCreator());
        TYPE_CREATOR_MAP.put(UsageNodeType.USAGE_DETAIL,
                new UsageDetailNodeCreator());
        TYPE_CREATOR_MAP.put(UsageNodeType.USAGE_UNKNOWN,
                new UnknownUsageNodeCreator());
    }
    
    private static Map<Class<? extends BpelEntity>, Boolean> USAGE_DETAIL_MAP = 
            new HashMap<Class<? extends BpelEntity>, Boolean>();
    
    static {
        USAGE_DETAIL_MAP.put(Assign.class,true);
//        USAGE_DETAIL_MAP.put(Pick.class,true);
//        USAGE_DETAIL_MAP.put(EventHandlers.class,true);
//        USAGE_DETAIL_MAP.put(CorrelationContainer.class,true);
    }
    
    public RefactoringNodeFactoryImpl() {
    }
    
//    public static RefactoringNodeFactory getInstance() {
//        if (FACTORY_INSTANCE.get() == null) {
//            RefactoringNodeFactory factory = new RefactoringNodeFactoryImpl();
//            FACTORY_INSTANCE.compareAndSet(null,factory);
//        }
//        return FACTORY_INSTANCE.get();
//    }
    
    public Node createNode(Component reference) {
        Node node = null;
        UsageNodeType nodeType = null;
        if (reference instanceof BpelEntity) {
            nodeType = (UsageNodeType)((BpelEntity)reference).getCookie(UsageNodeType.class);
        }
        nodeType = nodeType != null ? nodeType : UsageNodeType.USAGE_OBJECT;
        
        node = createNode(nodeType, reference);
        if (node == null) {
            node = createNode(UsageNodeType.USAGE_UNKNOWN, reference);
        }
        assert node != null;
        
        return node;
    }
    
    public List<Component> getPathFromRoot(Component component) {
        assert component != null;

        if (component instanceof BPELExtensibilityComponent) {
            return getPathFromRoot((BPELExtensibilityComponent)component);
        }
        
        if (component instanceof BpelEntity) {
            ArrayList<Component> pathFromRoot = new ArrayList<Component>();
            RefactoringBpelModelVisitor bpelVisitor 
                    = new RefactoringBpelModelVisitor(pathFromRoot);
            ((BpelEntity)component).accept(bpelVisitor);
            if (! pathFromRoot.equals(Collections.EMPTY_LIST)) {
                return pathFromRoot;
            }

            // temporary solution
//            return pathFromRoot.equals(Collections.EMPTY_LIST) 
//                ? getDefaultPathFromRoot(component) : 
//                    pathFromRoot ;
            BpelEntity dc = (BpelEntity)component;
            if (isUsageDetailEntity(dc)) {
                dc.setCookie(UsageNodeType.class,UsageNodeType.USAGE_DETAIL);
                return getPathFromRoot(dc,true);
            }

            dc.setCookie(UsageNodeType.class,UsageNodeType.USAGE_OBJECT);
            return getPathFromRoot(dc, false);
        }
        
        return getDefaultPathFromRoot(component);
    }

    private Node createNode(UsageNodeType nodeType, Object reference) {
        Node node = null;
        RefactoringNodeCreator nodeCreator = TYPE_CREATOR_MAP.get(nodeType);
        if (nodeCreator != null && nodeCreator.isSupported(nodeType)) {
            node = nodeCreator.create(nodeType,reference);
        }
        return node;
    }
    
    private List<Component> getPathFromRoot(BpelEntity entity, boolean isDetailObj) {
        ArrayList<Component> pathFromRoot = new ArrayList<Component>();
        BpelEntity dc = entity;
        pathFromRoot.add(dc);
        int level = 0 ;
        int maxDeep = isDetailObj ? 2 : 1;
        while (dc.getParent() != null && level < maxDeep) {
            level++;
            dc = dc.getParent();
            dc.setCookie(UsageNodeType.class,
                    level == 1 && isDetailObj ? UsageNodeType.USAGE_OBJECT :
                        UsageNodeType.USAGE_CONTEXT);
            pathFromRoot.add(0, dc);
        }
        return pathFromRoot;
    }

    private List<Component> getPathFromRoot(BPELExtensibilityComponent entity) {
        ArrayList<Component> pathFromRoot 
                = new ArrayList<Component>();
        BPELExtensibilityComponent dc = entity;
        pathFromRoot.add(dc);
        int level = 0 ;
        while (dc.getParent() instanceof BPELExtensibilityComponent) {
            dc = (BPELExtensibilityComponent)dc.getParent();
            pathFromRoot.add(0, dc);
        }
        return pathFromRoot;
    }
    
    private List<Component> getDefaultPathFromRoot(Component entity) {
        ArrayList<Component> pathFromRoot = new ArrayList<Component>();
        Component dc = entity;
        pathFromRoot.add(dc);
        int level = 0 ;
        while (dc.getParent() != null) {
            dc = dc.getParent();
            pathFromRoot.add(0, dc);
        }
        return pathFromRoot;
    }
    
    private boolean isUsageDetailEntity(BpelEntity entity) {
        assert entity != null;
        boolean isUsageDetailEntity = false;
            if (entity.getParent() == null) {
                return isUsageDetailEntity;
            }
        BpelEntity ded = entity.getParent().getParent();
        if (ded == null) {
            return isUsageDetailEntity;
        }
        
        isUsageDetailEntity = USAGE_DETAIL_MAP.get(ded.getElementType()) != null 
                && USAGE_DETAIL_MAP.get(ded.getElementType());

        return isUsageDetailEntity;
    }
}
