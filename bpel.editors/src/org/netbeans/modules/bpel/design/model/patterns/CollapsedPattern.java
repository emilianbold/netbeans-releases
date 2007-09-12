/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


package org.netbeans.modules.bpel.design.model.patterns;

import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.PartnerLinkHelper;
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
        
        if (bpelEntity instanceof PartnerLink) {
            element = new ProcessBorder();
        } else {
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
        }
        
        appendElement(element);
        
        if (bpelEntity instanceof NamedElement) {
            registerTextElement(element);
        }
    }


    public void reconnectElements() {
        clearConnections();
        if (!(getOMReference() instanceof PartnerLink)) {
            new PartnerLinkHelper(getModel()).updateMessageFlowLinks(this);
        }
    }
}
