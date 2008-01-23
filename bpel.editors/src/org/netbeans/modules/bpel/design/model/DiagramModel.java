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
package org.netbeans.modules.bpel.design.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.design.model.patterns.CollapsedPattern;
import org.netbeans.modules.bpel.design.model.patterns.ForEachPattern;
import org.netbeans.modules.bpel.design.model.patterns.OnEventPattern;
import org.netbeans.modules.bpel.design.model.patterns.PartnerLinksPattern;
import org.netbeans.modules.bpel.design.model.patterns.RepeatUntilPattern;
import org.netbeans.modules.bpel.design.model.patterns.TerminationHandlerPattern;
import org.netbeans.modules.bpel.design.model.patterns.UnsupportedPattern;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CompensatableActivityHolder;
import org.netbeans.modules.bpel.model.api.Compensate;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.Empty;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.Exit;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.If;


import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.patterns.AssignPattern;
import org.netbeans.modules.bpel.design.model.patterns.CatchAllPattern;
import org.netbeans.modules.bpel.design.model.patterns.CatchPattern;
import org.netbeans.modules.bpel.design.model.patterns.CompensatePattern;
import org.netbeans.modules.bpel.design.model.patterns.CompensateScopePattern;
import org.netbeans.modules.bpel.design.model.patterns.CompensationHandlerPattern;
import org.netbeans.modules.bpel.design.model.patterns.ElseIfPattern;
import org.netbeans.modules.bpel.design.model.patterns.ElsePattern;
import org.netbeans.modules.bpel.design.model.patterns.EmptyPattern;
import org.netbeans.modules.bpel.design.model.patterns.EventHandlersPattern;
import org.netbeans.modules.bpel.design.model.patterns.FaultHandlersPattern;
import org.netbeans.modules.bpel.design.model.patterns.FlowPattern;
import org.netbeans.modules.bpel.design.model.patterns.InvokePattern;
import org.netbeans.modules.bpel.design.model.patterns.OnAlarmEventPattern;
import org.netbeans.modules.bpel.design.model.patterns.OnAlarmPickPattern;
import org.netbeans.modules.bpel.design.model.patterns.OnMessagePattern;
import org.netbeans.modules.bpel.design.model.patterns.PartnerlinkPattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.model.patterns.PickPattern;
import org.netbeans.modules.bpel.design.model.patterns.ProcessPattern;
import org.netbeans.modules.bpel.design.model.patterns.ReceivePattern;
import org.netbeans.modules.bpel.design.model.patterns.ReplyPattern;

import org.netbeans.modules.bpel.design.model.patterns.ScopePattern;
import org.netbeans.modules.bpel.design.model.patterns.SequencePattern;
import org.netbeans.modules.bpel.design.model.patterns.ExitPattern;
import org.netbeans.modules.bpel.design.model.patterns.IfPattern;
import org.netbeans.modules.bpel.design.model.patterns.ImportPattern;
import org.netbeans.modules.bpel.design.model.patterns.ThrowPattern;
import org.netbeans.modules.bpel.design.model.patterns.WaitPattern;
import org.netbeans.modules.bpel.design.model.patterns.WhilePattern;
import org.netbeans.modules.bpel.design.model.patterns.ReThrowPattern;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.ReThrow;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.xml.xam.ui.XAMUtils;

/**
 *
 * @author Alexey Yarmolenko
 */
public class DiagramModel {

    private DesignView view;
    private Pattern rootPattern;
    private ModelChangeHandler changeHandler;
    private Object patternKey = new Object();
    private Object collapsedKey = new Object();
    private ViewFilters filters = new ViewFilters();
    private BpelModel bpelModel;

    public DiagramModel(DesignView view) {
        this.view = view;

        changeHandler = new ModelChangeHandler(this);

        bpelModel = view.getBPELModel();

        if (LAST_USED_COLLAPSED_KEY != null) {
            copyCollapsedState(bpelModel.getProcess(),
                    LAST_USED_COLLAPSED_KEY, collapsedKey);
        }

        BpelModel model = view.getBPELModel();

        model.addEntityChangeListener(changeHandler);
    }

    public Pattern getRootPattern() {
        return rootPattern;
    }

    public void setActivated() {
        LAST_USED_COLLAPSED_KEY = collapsedKey;
    }

    public static boolean isVisualizable(BpelEntity o) {
        return (o instanceof Process) || (o instanceof Sequence) || (o instanceof If) || (o instanceof ElseIf) || (o instanceof Else) || (o instanceof ForEach) || (o instanceof Assign) || (o instanceof Receive) || (o instanceof Reply) || (o instanceof Invoke) || (o instanceof Flow) || (o instanceof While) || (o instanceof RepeatUntil) || (o instanceof Wait) || (o instanceof Exit) || (o instanceof Throw) || (o instanceof Scope) || (o instanceof Pick) || (o instanceof OnMessage) || (o instanceof OnEvent) || (o instanceof OnAlarmPick) || (o instanceof Empty) || (o instanceof OnAlarmEvent) || (o instanceof CompensationHandler) || (o instanceof TerminationHandler) || (o instanceof Compensate) || (o instanceof EventHandlers) || (o instanceof FaultHandlers) || (o instanceof Catch) || (o instanceof CompensatableActivityHolder) || (o instanceof Activity) || (o instanceof PartnerLink) || (o instanceof Import) || (o instanceof PartnerLinkContainer);
    }

    public Pattern createPattern(BpelEntity o) {
        Pattern result = null;

        if (isCollapsed(o)) {
            if(o instanceof PartnerLink){
                result = new PartnerlinkPattern(this, true);
            } else {
                result = new CollapsedPattern(this);
            }
        } else if (o instanceof Process) {
            result = new ProcessPattern(this);
        } else if (o instanceof Sequence) {
            result = new SequencePattern(this);
        } else if (o instanceof If) {
            result = new IfPattern(this);
        } else if (o instanceof ElseIf) {
            result = new ElseIfPattern(this);
        } else if (o instanceof Else) {
            result = new ElsePattern(this);
        } else if (o instanceof ForEach) {
            result = new ForEachPattern(this);
        } else if (o instanceof Assign) {
            result = new AssignPattern(this);
        } else if (o instanceof Receive) {
            result = new ReceivePattern(this);
        } else if (o instanceof Reply) {
            result = new ReplyPattern(this);
        } else if (o instanceof Invoke) {
            result = new InvokePattern(this);
        } else if (o instanceof Flow) {
            result = new FlowPattern(this);
        } else if (o instanceof While) {
            result = new WhilePattern(this);
        } else if (o instanceof RepeatUntil) {
            result = new RepeatUntilPattern(this);
        } else if (o instanceof Wait) {
            result = new WaitPattern(this);
        } else if (o instanceof Exit) {
            result = new ExitPattern(this);
        } else if (o instanceof Throw) {
            result = new ThrowPattern(this);
        } else if (o instanceof ReThrow) {
            result = new ReThrowPattern(this);
        } else if (o instanceof Scope) {
            result = new ScopePattern(this);
        } else if (o instanceof Pick) {
            result = new PickPattern(this);
        } else if (o instanceof OnMessage) {
            result = new OnMessagePattern(this);
        } else if (o instanceof OnEvent) {
            result = new OnEventPattern(this);
        } else if (o instanceof OnAlarmPick) {
            result = new OnAlarmPickPattern(this);
        } else if (o instanceof Empty) {
            result = new EmptyPattern(this);
        } else if (o instanceof OnAlarmEvent) {
            result = new OnAlarmEventPattern(this);
        } else if (o instanceof CompensationHandler) {
            result = new CompensationHandlerPattern(this);
        } else if (o instanceof TerminationHandler) {
            result = new TerminationHandlerPattern(this);
        } else if (o instanceof Compensate) {
            result = new CompensatePattern(this);
        } else if (o instanceof CompensateScope) {
            result = new CompensateScopePattern(this);
        } else if (o instanceof EventHandlers) {
            result = new EventHandlersPattern(this);
        } else if (o instanceof FaultHandlers) {
            result = new FaultHandlersPattern(this);
        } else if (o instanceof Catch) {
            result = new CatchPattern(this);
        } else if (o instanceof CompensatableActivityHolder) {
            result = new CatchAllPattern(this);
        } else if (o instanceof Activity) {
            result = new UnsupportedPattern(this);
        } else if (o instanceof PartnerLink) {
            result = new PartnerlinkPattern(this);
        } else if (o instanceof Import) {
            result = new ImportPattern(this);
        } else if (o instanceof PartnerLinkContainer) {
            result = new PartnerLinksPattern(this);
        } else {
            return null;
        }

        o.setCookie(patternKey, result);
        result.initPattern(o);
        assert result != null;
        return result;
    }

    public DesignView getView() {
        return view;
    }

    public List<Pattern> getPartnerLinks(PartnerRole mode) {
        Process ps = view.getBPELModel().getProcess();

        if (ps != null) {

            PartnerLinkContainer plc = ps.getPartnerLinkContainer();

            if (plc != null) {

                PartnerLinksPattern pls = (PartnerLinksPattern) getPattern(plc);

                if (pls != null) {

                    return (mode == mode.CONSUMER) ? pls.getConsumers() : pls.getProviders();
                }
            }
        }
        return new ArrayList<Pattern>();
    }

    public void expandToBeVisible(BpelEntity bpelEntity) {
        while ((bpelEntity != null) && !isVisualizable(bpelEntity)) {
            bpelEntity = bpelEntity.getParent();
        }

        if (bpelEntity == null) {
            return;
        }

        boolean somethingWasExpanded = false;

        for (BpelEntity entity = bpelEntity.getParent();
                entity != null;
                entity = entity.getParent()) {
            if (entity.getCookie(collapsedKey) != null) {
                entity.removeCookie(collapsedKey);
                somethingWasExpanded = true;
            }
        }

        if (somethingWasExpanded) {
            view.reloadModel();
            view.diagramChanged();
            view.getDecorationManager().decorationChanged();
            view.getValidationDecorationProvider().updateDecorations();
        }
    }

    public void expandAll() {
        expandAll(bpelModel.getProcess());
    }

    public void expandAll(BpelEntity root) {
        if (expandRecursively(root)) {
            view.reloadModel();
            view.diagramChanged();
            view.getDecorationManager().decorationChanged();
            view.getValidationDecorationProvider().updateDecorations();
        }
    }

    private boolean expandRecursively(BpelEntity parent) {
        if (parent == null) {
            return false;
        }

        boolean somethingWasExpanded = false;

        List<BpelEntity> children = parent.getChildren();

        if (children != null) {
            for (BpelEntity child : children) {
                somethingWasExpanded = expandRecursively(child) || somethingWasExpanded;
            }
        }

        if (parent.getCookie(collapsedKey) != null) {
            parent.removeCookie(collapsedKey);
            somethingWasExpanded = true;
        }

        return somethingWasExpanded;
    }

    public void setCollapsed(BpelEntity bpelEntity, boolean value) {
        if (value) {
            bpelEntity.setCookie(collapsedKey, VALUE_COLLAPSED);
        } else {
            if (bpelEntity.getCookie(collapsedKey) != null) {
                bpelEntity.removeCookie(collapsedKey);
            }
        }

        view.reloadModel();
        view.diagramChanged();
        view.getDecorationManager().decorationChanged();
        view.getValidationDecorationProvider().updateDecorations();
    }

    public boolean isCollapsed(BpelEntity bpelEntity) {
        Object collapsedValue = bpelEntity.getCookie(collapsedKey);
        return (collapsedValue != null);
    }

    public Pattern getPattern(BpelEntity entity) {
        assert entity != null;
        return (Pattern) entity.getCookie(patternKey);
    }

    public ViewFilters getFilters() {
        return filters;
    }

    public void release() {
        bpelModel.removeEntityChangeListener(changeHandler);

        //releasing the cookie key will trigger removal of all cookie references.
        patternKey = null;
        collapsedKey = null;

        view = null;

        rootPattern = null;
    }

    public boolean isReadOnly() {
        return !XAMUtils.isWritable(bpelModel);
    }

    public void setRootPattern(Pattern rootPattern) {
        this.rootPattern = rootPattern;
    }

    public BpelEntity getEntity(UniqueId id) {
        return (id != null) ? bpelModel.getEntity(id) : null;
    }

    private static void copyCollapsedState(
            BpelEntity bpelEntity,
            Object oldCollapsedKey,
            Object newCollapsedKey) {
        if (bpelEntity == null) {
            return;
        }

        Object value = bpelEntity.getCookie(oldCollapsedKey);

        if (value != null) {
            bpelEntity.setCookie(newCollapsedKey, value);
        }

        List<BpelEntity> children = bpelEntity.getChildren();

        if (children != null) {
            for (BpelEntity child : children) {
                copyCollapsedState(child, oldCollapsedKey, newCollapsedKey);
            }
        }
    }
    private static final Object VALUE_COLLAPSED = new Object();
    private static Object LAST_USED_COLLAPSED_KEY = null;
}
