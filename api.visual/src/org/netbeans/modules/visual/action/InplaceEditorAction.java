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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

/**
 * @author David Kaspar
 */
public final class InplaceEditorAction <C extends JComponent> extends WidgetAction.LockedAdapter implements InplaceEditorProvider.EditorController {

    private InplaceEditorProvider<C> provider;

    private C editor = null;
    private Widget widget = null;

    public InplaceEditorAction (InplaceEditorProvider<C> provider) {
        this.provider = provider;
    }

    protected boolean isLocked () {
        return editor != null;
    }

    public State mouseClicked (Widget widget, WidgetMouseEvent event) {
        if (event.getButton () == MouseEvent.BUTTON1 && event.getClickCount () == 2) {
            if (openEditor (widget))
                return State.createLocked (widget, this);
        }
        return State.REJECTED;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (editor != null)
            closeEditor (true);
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (editor != null)
            closeEditor (true);
        return State.REJECTED;
    }

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        if (event.getKeyChar () == KeyEvent.VK_ENTER)
            if (openEditor (widget))
                return State.createLocked (widget, this);
        return State.REJECTED;
    }

    public State keyReleased (Widget widget, WidgetKeyEvent event) {
        return super.keyReleased (widget, event); // TODO
    }

    public final boolean isEditorVisible () {
        return editor != null;
    }

    public final boolean openEditor (Widget widget) {
        if (editor != null)
            return false;

        Scene scene = widget.getScene ();
        JComponent component = scene.getView ();
        if (component == null)
            return false;

        editor = createEditorComponent (widget);
        if (editor == null)
            return false;
        this.widget = widget;

        component.add (editor);
        notifyOpened (widget, editor);

        Rectangle rectangle = scene.convertSceneToView (widget.convertLocalToScene (widget.getPreferredBounds ()));
        Point center = GeomUtil.center (rectangle);
        Dimension size = editor.getMinimumSize ();
        if (rectangle.width > size.width)
            size.width = rectangle.width;
        if (rectangle.height > size.height)
            size.height = rectangle.height;
        int x = center.x - size.width / 2;
        int y = center.y - size.height / 2;
        if (x + size.width > component.getWidth ())
            x = component.getWidth () - size.width;
        if (y + size.height > component.getHeight ())
            y = component.getHeight () - size.height;
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        editor.setBounds (x, y, size.width, size.height);
        editor.repaint ();
        editor.requestFocus ();

        return true;
    }

    public final void closeEditor (boolean commit) {
        if (editor == null)
            return;
        Container parent = editor.getParent ();
        Rectangle bounds = parent != null ? editor.getBounds () : null;
        notifyClosing (widget, editor, commit);
        if (bounds != null) {
            parent.remove (editor);
            parent.repaint (bounds.x, bounds.y, bounds.width, bounds.height);
        }
        editor = null;
        widget = null;
    }

    protected void notifyOpened (Widget widget, C editor) {
        provider.notifyOpened (this, widget, editor);
    }

    protected void notifyClosing (Widget widget, C editor, boolean discarded) {
        provider.notifyClosing (this, widget, editor, discarded);
    }

    protected C createEditorComponent (Widget widget) {
        return provider.createEditorComponent (this, widget);
    }

}
