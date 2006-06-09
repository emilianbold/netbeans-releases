/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.action;

import java.awt.event.FocusEvent;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author David Kaspar
 */
public interface WidgetAction {
    
    public static abstract class State {
        
        public static final State REJECTED = new State() {
            public boolean isLockedInChain() { return false; }
            public boolean isConsumed() { return false; }
            public Widget getLockedWidget() { return null; }
            public WidgetAction getLockedAction() { return null; }
        };
        
        public static final State CONSUMED = new State() {
            public boolean isLockedInChain() { return false; }
            public boolean isConsumed() { return true; }
            public Widget getLockedWidget() { return null; }
            public WidgetAction getLockedAction() { return null; }
        };
        
        public static final State CHAIN_ONLY = new State() {
            public boolean isLockedInChain() { return true; }
            public boolean isConsumed() { return false; }
            public Widget getLockedWidget() { return null; }
            public WidgetAction getLockedAction() { return null; }
        };
        
        public static State createLocked(final Widget lockedWidget,final WidgetAction lockedAction) {
            assert lockedWidget != null;
            assert lockedAction != null;
            return new State() {
                public boolean isLockedInChain() { return false; }
                public boolean isConsumed() { return true; }
                public Widget getLockedWidget() { return lockedWidget; }
                public WidgetAction getLockedAction() { return lockedAction; }
            };
        }
        
        private State() {
        }
        
        public abstract boolean isLockedInChain();
        
        public abstract boolean isConsumed();
        
        public abstract Widget getLockedWidget();
        
        public abstract WidgetAction getLockedAction();
        
    }
    
    public State mouseClicked(Widget widget, WidgetMouseEvent event);
    
    public State mousePressed(Widget widget, WidgetMouseEvent event);
    
    public State mouseReleased(Widget widget, WidgetMouseEvent event);
    
    public State mouseEntered(Widget widget, WidgetMouseEvent event);
    
    public State mouseExited(Widget widget, WidgetMouseEvent event);
    
    
    public State mouseDragged(Widget widget, WidgetMouseEvent event);
    
    public State mouseMoved(Widget widget, WidgetMouseEvent event);
    
    
    public State mouseWheelMoved(Widget widget, WidgetMouseWheelEvent event);
    
    
    public State keyTyped(Widget widget, WidgetKeyEvent event);
    
    public State keyPressed(Widget widget, WidgetKeyEvent event);
    
    public State keyReleased(Widget widget, WidgetKeyEvent event);
    
    public State focusGained(Widget widget, WidgetFocusEvent event);
    
    public State focusLost(Widget widget, WidgetFocusEvent event);
    
    public static class Adapter implements WidgetAction {
        
        public State mouseClicked(Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }
        
        public State mousePressed(Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }
        
        public State mouseReleased(Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }
        
        public State mouseEntered(Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }
        
        public State mouseExited(Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }
        
        public State mouseDragged(Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }
        
        public State mouseMoved(Widget widget, WidgetMouseEvent event) {
            return State.REJECTED;
        }
        
        public State mouseWheelMoved(Widget widget, WidgetMouseWheelEvent event) {
            return State.REJECTED;
        }
        
        public State keyTyped(Widget widget, WidgetKeyEvent event) {
            return State.REJECTED;
        }
        
        public State keyPressed(Widget widget, WidgetKeyEvent event) {
            return State.REJECTED;
        }
        
        public State keyReleased(Widget widget, WidgetKeyEvent event) {
            return State.REJECTED;
        }
        
        public State focusGained(Widget widget, WidgetFocusEvent event) {
            return State.REJECTED;
        }
        
        public State focusLost(Widget widget, WidgetFocusEvent event) {
            return State.REJECTED;
        }
        
    }
    
    public static final class Chain implements WidgetAction {
        
        private List<WidgetAction> actions;
        private List<WidgetAction> actionsUm;
        
        public Chain() {
            actions = new ArrayList<WidgetAction> ();
            actionsUm = Collections.unmodifiableList(actions);
        }
        
        public List<WidgetAction> getActions() {
            return actionsUm;
        }
        
        public void addAction(WidgetAction action) {
            actions.add(action);
        }
        
        public void addAction(int index, WidgetAction action) {
            actions.add(index, action);
        }
        
        public void removeAction(WidgetAction action) {
            actions.remove(action);
        }
        
        public void removeAction(int index) {
            actions.remove(index);
        }
        
        public State mouseClicked(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseClicked(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State mousePressed(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mousePressed(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State mouseReleased(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseReleased(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State mouseEntered(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseEntered(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State mouseExited(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseExited(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State mouseDragged(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseDragged(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State mouseMoved(Widget widget, WidgetMouseEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseMoved(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State mouseWheelMoved(Widget widget, WidgetMouseWheelEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.mouseWheelMoved(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State keyTyped(Widget widget, WidgetKeyEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.keyTyped(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State keyPressed(Widget widget, WidgetKeyEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.keyPressed(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State keyReleased(Widget widget, WidgetKeyEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.keyReleased(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State focusGained(Widget widget, WidgetAction.WidgetFocusEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.focusGained(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
        public State focusLost(Widget widget, WidgetAction.WidgetFocusEvent event) {
            WidgetAction[] actionsArray = actions.toArray(new WidgetAction[actions.size()]);
            State chainState = State.REJECTED;
            for (WidgetAction action : actionsArray) {
                State state = action.focusLost(widget, event);
                if (state.isConsumed())
                    return state;
                if (state.isLockedInChain())
                    chainState = State.CONSUMED;
            }
            return chainState;
        }
        
    }
    
    public static class WidgetMouseEvent {
        
        private long id;
        private MouseEvent event;
        private int x, y;
        
        public WidgetMouseEvent(long id, MouseEvent event) {
            this.id = id;
            this.event = event;
            x = event.getX();
            y = event.getY();
        }
        
        public long getEventID() {
            return id;
        }
        
        public Point getPoint() {
            return new Point(x, y);
        }
        
        public void setPoint(Point point) {
            x = point.x;
            y = point.y;
        }
        
        public void translatePoint(int x, int y) {
            this.x += x;
            this.y += y;
        }
        
        public int getClickCount() {
            return event.getClickCount();
        }
        
        public int getButton() {
            return event.getButton();
        }
        
        public boolean isPopupTrigger() {
            return event.isPopupTrigger();
        }
        
        public boolean isShiftDown() {
            return event.isShiftDown();
        }
        
        public boolean isControlDown() {
            return event.isControlDown();
        }
        
        public boolean isMetaDown() {
            return event.isMetaDown();
        }
        
        public boolean isAltDown() {
            return event.isAltDown();
        }
        
        public boolean isAltGraphDown() {
            return event.isAltGraphDown();
        }
        
        public long getWhen() {
            return event.getWhen();
        }
        
        public int getModifiers() {
            return event.getModifiers();
        }
        
        public int getModifiersEx() {
            return event.getModifiersEx();
        }
        
    }
    
    public static class WidgetMouseWheelEvent extends WidgetMouseEvent {
        
        private long id;
        private MouseWheelEvent event;
        private int x, y;
        
        public WidgetMouseWheelEvent(long id, MouseWheelEvent event) {
            super(id, event);
            this.event = event;
        }
        
        public int getScrollType() {
            return event.getScrollType();
        }
        
        public int getScrollAmount() {
            return event.getScrollAmount();
        }
        
        public int getWheelRotation() {
            return event.getWheelRotation();
        }
        
        public int getUnitsToScroll() {
            return event.getUnitsToScroll();
        }
        
    }
    
    public static class WidgetKeyEvent {
        
        private long id;
        private KeyEvent event;
        
        public WidgetKeyEvent(long id, KeyEvent event) {
            this.id = id;
            this.event = event;
        }
        
        public long getEventID() {
            return id;
        }
        
        public int getKeyCode() {
            return event.getKeyCode();
        }
        
        public char getKeyChar() {
            return event.getKeyChar();
        }
        
        public int getKeyLocation() {
            return event.getKeyLocation();
        }
        
        public boolean isActionKey() {
            return event.isActionKey();
        }
        
        public boolean isShiftDown() {
            return event.isShiftDown();
        }
        
        public boolean isControlDown() {
            return event.isControlDown();
        }
        
        public boolean isMetaDown() {
            return event.isMetaDown();
        }
        
        public boolean isAltDown() {
            return event.isAltDown();
        }
        
        public boolean isAltGraphDown() {
            return event.isAltGraphDown();
        }
        
        public long getWhen() {
            return event.getWhen();
        }
        
        public int getModifiers() {
            return event.getModifiers();
        }
        
        public int getModifiersEx() {
            return event.getModifiersEx();
        }
        
    }
    
    public static class WidgetFocusEvent{
        
        private long id;
        private FocusEvent event;
        
        public WidgetFocusEvent(long id, FocusEvent event) {
            this.id = id;
            this.event = event;
        }
        
        public long getEventID() {
            return id;
        }
        
        //TODO 
        public Object getOppositeComponent() { 
            return event.getOppositeComponent();
        }
        
        public String paramString() {
            return event.paramString();
        }
        
        public boolean isTemporary() {
            return event.isTemporary();
        }
    }
    
}
