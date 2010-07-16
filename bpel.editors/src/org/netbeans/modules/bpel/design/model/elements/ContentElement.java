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
package org.netbeans.modules.bpel.design.model.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import org.netbeans.modules.bpel.design.geometry.FEllipse;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.geometry.FRoumb;
import org.netbeans.modules.bpel.design.geometry.FShape;
import org.netbeans.modules.bpel.design.geometry.FStroke;
import org.netbeans.modules.bpel.design.model.elements.icons.ANDIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.AssignIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.ValidateIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.JavaScriptIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.CompensateBadgeIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.CompensateIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.CompensateScopeIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.EndEventIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.EventBadgeIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.EventIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.ExitIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.FaultBadgeIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.ForEachIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.Icon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.InvokeIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.MessageIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.ReceiveIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.RepeatUntilIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.ReplyIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.ReThrowIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.TerminationBadgeIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.ThrowIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.TimerIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.WhileIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.XORIcon2D;
import org.netbeans.modules.bpel.design.model.elements.icons.XORSmallIcon2D;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

/**
 *
 * @author anjeleevich
 */
public class ContentElement extends VisualElement {
    
    private Icon2D icon;
    
    public ContentElement(Icon2D icon) {
        this(TASK_SHAPE, icon);
    }
    
    public ContentElement(FShape shape, Icon2D icon) {
        super(shape);
        this.icon = icon;
    }

    public void paint(Graphics2D g2) {
        FShape shape = this.shape;
        
        // draw background;
        g2.setPaint(new TexturePaint(GRADIENT_TEXTURE, shape.getBounds2D()));
        g2.fill(shape);
        
        // draw icon
        if (icon != null) {
            g2.setRenderingHint(
                    RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            FPoint center = shape.getNormalizedCenter(g2);
            g2.translate(center.x, center.y);
            icon.paint(g2);
            g2.translate(-center.x, -center.y);
        }
              
        // draw border
        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setStroke(STROKE.createStroke(g2));
        g2.setPaint(STROKE_COLOR);
        g2.draw(shape);
        
        if (isPaintText()) {
            BorderElement border = null;
            for (Pattern p = getPattern(); p != null; p = p.getParent()) {
                if (p instanceof CompositePattern) {
                    border = ((CompositePattern) p).getBorder();
                    if (border != null) break;
                }
            }
            
            if (border != null) {
                double cx = getCenterX();
                double x1 = border.getX();
                double x2 = x1 + border.getWidth();
                
                double hw = Math.min(Math.abs(x2 - cx), Math.abs(x1 - cx)) - 4;
                
                if (hw > 0) {
                    g2.setPaint(getTextColor());
                    drawXCenteredString(g2, getText(), cx, getY() + getHeight(), 
                            hw * 2);
                }
            }
        }
    }
    
    public void paintThumbnail(Graphics2D g2) {
        FShape shape = this.shape;
        
        // draw background;
        g2.setPaint(GRADIENT_TEXTURE_COLOR);
        g2.fill(shape);

        // draw border
        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setStroke(STROKE.createStroke(g2));
        g2.setPaint(STROKE_COLOR);
        g2.draw(shape);
    }
    
    public Icon2D getIcon() {
        return icon;
    }

    public static ContentElement createAssign() {
        return new ContentElement(TASK_SHAPE, AssignIcon2D.INSTANCE);
    }

    public static ContentElement createJavaScript() {
        return new ContentElement(TASK_SHAPE, JavaScriptIcon2D.INSTANCE);
    }

    public static ContentElement createValidate() {
        return new ContentElement(EVENT_SHAPE, ValidateIcon2D.INSTANCE);
    }
    
    public static ContentElement createEmpty() {
        return new ContentElement(TASK_SHAPE, null);
    }
    
    public static ContentElement createReply() {
        return new ContentElement(TASK_SHAPE, ReplyIcon2D.INSTANCE);
    }
    
    public static ContentElement createInvoke() {
        return new ContentElement(TASK_SHAPE, InvokeIcon2D.INSTANCE);
    }
    
    public static ContentElement createReceive() {
        return new ContentElement(EVENT_SHAPE, ReceiveIcon2D.INSTANCE);
    }
    
    public static ContentElement createExit() {
        return new ContentElement(EVENT_SHAPE, ExitIcon2D.INSTANCE);
    }

    public static ContentElement createCompensate() {
        return new ContentElement(EVENT_SHAPE, CompensateIcon2D.INSTANCE);
    }
    
    public static ContentElement createCompensateScope() {
        return new ContentElement(EVENT_SHAPE, CompensateScopeIcon2D.INSTANCE);
    }
    
    public static ContentElement createWait() {
        return new ContentElement(EVENT_SHAPE, TimerIcon2D.INSTANCE);
    }

    public static ContentElement createCompensateBadge() {
        return new ContentElement(BADGE_SHAPE, CompensateBadgeIcon2D.INSTANCE);
    }
    
    public static ContentElement createFaultBadge() {
        return new ContentElement(BADGE_SHAPE, FaultBadgeIcon2D.INSTANCE);
    }
    
    public static ContentElement createEventBadge() {
        return new ContentElement(BADGE_SHAPE, EventBadgeIcon2D.INSTANCE);
    }
    
    public static ContentElement createTerminationBadge() {
        return new ContentElement(BADGE_SHAPE, TerminationBadgeIcon2D.INSTANCE);
    }
    
    public static ContentElement createMessageEvent() {
        return new ContentElement(EVENT_SHAPE, MessageIcon2D.INSTANCE);
    }
    
    public static ContentElement createTimerEvent() {
        return new ContentElement(EVENT_SHAPE, TimerIcon2D.INSTANCE);
    }
    

    public static ContentElement createThrow() {
        return new ContentElement(EVENT_SHAPE, ThrowIcon2D.INSTANCE);
    }

    public static ContentElement createReThrow() {
        return new ContentElement(EVENT_SHAPE, ReThrowIcon2D.INSTANCE);
    }
    
    public static ContentElement createEndEvent() {
        return new ContentElement(START_END_EVENT_SHAPE, EndEventIcon2D.INSTANCE);
    }

    public static ContentElement createStartEvent() {
        return new ContentElement(START_END_EVENT_SHAPE, null);
    }
    
    public static ContentElement createIfGateway() {
        return new ContentElement(GATEWAY_SHAPE, XORIcon2D.INSTANCE);
    }
    
    public static ContentElement createWhileGateway() {
        return new ContentElement(GATEWAY_SHAPE, WhileIcon2D.INSTANCE);
    }
    
    public static ContentElement createRepeatUntilGateway() {
        return new ContentElement(GATEWAY_SHAPE, RepeatUntilIcon2D.INSTANCE);
    }

    public static ContentElement createForEachGateway() {
        return new ContentElement(GATEWAY_SHAPE, ForEachIcon2D.INSTANCE);
    }
    
    public static ContentElement createPickGateway() {
        return new ContentElement(GATEWAY_SHAPE, EventIcon2D.INSTANCE);
    }
    
    public static ContentElement createFlowGateway() {
        return new ContentElement(GATEWAY_SHAPE, ANDIcon2D.INSTANCE);
    }
    
    public static ContentElement createElseIfGateway() {
        return new ContentElement(SMALL_GATEWAY_SHAPE, XORSmallIcon2D.INSTANCE);
    }
    
    public static final FShape BADGE_SHAPE = new FEllipse(16, 16);
    public static final FShape START_END_EVENT_SHAPE = new FEllipse(20, 20);
    public static final FShape EVENT_SHAPE = new FEllipse(28, 28); //new FEllipse(24, 24);
    public static final FShape GATEWAY_SHAPE = new FRoumb(28, 28); //new FRoumb(40, 40);
    public static final FShape SMALL_GATEWAY_SHAPE = new FRoumb(20, 20);
    public static final FShape TASK_SHAPE = new FRectangle(40, 28, 6); //new FRectangle(72, 40, 8);
    public static final FShape COLLAPSED_SHAPE = new FRectangle(28, 28, 6); //new FRectangle(40, 40, 8);
    
    private static final FStroke STROKE = new FStroke(1);
    public static final Paint STROKE_COLOR = new Color(0xA7A2A7);
}
