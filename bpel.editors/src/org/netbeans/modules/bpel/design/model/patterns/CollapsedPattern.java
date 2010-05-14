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
package org.netbeans.modules.bpel.design.model.patterns;

import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.ProcessBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.elements.icons.ANDIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.CatchAllIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.CatchIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.CompensateIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.EventIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.ForEachIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.Icon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.InvokeIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.MessageIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.RepeatUntilIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.ScopeIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.SequenceIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.TerminationIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.TimerIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.WhileIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.XORIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.XORSmallIcon2D;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnMessageCommon;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.While;

/**
 *
 * @author anjeleevich
 */
public class CollapsedPattern extends BasicActivityPattern {

    /** Creates a new instance of CollapsedPattern */
    public CollapsedPattern(DiagramModel model) {
        super(model);
    }

    protected void createElementsImpl() {
        BpelEntity bpelEntity = getOMReference();

        VisualElement element;


        Icon2D icon = null;

        BpelEntity entity = getOMReference();

        if (entity instanceof Pick) {
            icon = EventIcon2D.INSTANCE;
        } else if (entity instanceof Flow) {
            icon = ANDIcon2D.INSTANCE;
        } else if (entity instanceof If) {
            icon = XORIcon2D.INSTANCE;
        } else if (entity instanceof ElseIf) {
            icon = XORSmallIcon2D.INSTANCE;
        } else if (entity instanceof While) {
            icon = WhileIcon2D.INSTANCE;
        } else if (entity instanceof RepeatUntil) {
            icon = RepeatUntilIcon2D.INSTANCE;
        } else if (entity instanceof ForEach) {
            icon = ForEachIcon2D.INSTANCE;
        } else if (entity instanceof Invoke) {
            icon = InvokeIcon2D.INSTANCE;
        } else if (entity instanceof Sequence) {
            icon = SequenceIcon2D.INSTANCE;
        } else if (entity instanceof Scope) {
            icon = ScopeIcon2D.INSTANCE;
        } else if (entity instanceof OnMessageCommon) {
            icon = MessageIcon2D.INSTANCE;
        } else if (entity instanceof OnAlarmPick) {
            icon = TimerIcon2D.INSTANCE;
        } else if (entity instanceof OnAlarmEvent) {
            icon = TimerIcon2D.INSTANCE;
        } else if (entity instanceof Catch) {
            icon = CatchIcon2D.INSTANCE;
        } else if (entity instanceof CatchAll) {
            icon = CatchAllIcon2D.INSTANCE;
        } else if (entity instanceof CompensationHandler) {
            icon = CompensateIcon2D.INSTANCE;
        } else if (entity instanceof TerminationHandler) {
            icon = TerminationIcon2D.INSTANCE;
        } else if (entity instanceof FaultHandlers) {
            icon = CatchAllIcon2D.INSTANCE;
        } else if (entity instanceof EventHandlers) {
            icon = MessageIcon2D.INSTANCE;
        }

        element = new ContentElement(ContentElement.COLLAPSED_SHAPE, icon);


        appendElement(element);

        if (bpelEntity instanceof NamedElement) {
            registerTextElement(element);
        }
    }
}
