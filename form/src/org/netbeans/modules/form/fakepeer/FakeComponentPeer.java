/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

/**
 *
 * @author Tran Duc Trung
 */

abstract class FakeComponentPeer implements FakePeer
{
    Component _delegate;
    Component _target;

    FakeComponentPeer(Component target) {
        _target = target;
        _delegate = createDelegate();
        initDelegate();
    }

    void initDelegate() {
        Rectangle r = _target.getBounds();

        setBounds(r.x, r.y, r.width, r.height);
        setVisible(_target.isVisible());
        setCursor(_target.getCursor());
        setEnabled(_target.isEnabled());
        setFont(_target.getFont());
        setBackground(_target.getBackground());
        setForeground(_target.getForeground());

        _delegate.setName(_target.getName());
        //    _delegate.setLocale(_target.getLocale());
        _delegate.setDropTarget(_target.getDropTarget());
        _delegate.setComponentOrientation(_target.getComponentOrientation());

        repaint();
    }

    public void dispose() {
        _target = null;
        _delegate = null;
    }

    // XXX should be abstract
    Component createDelegate() {
        return null;
    }

    public void setVisible(boolean visible) {
        _delegate.setVisible(visible);
    }

    public void setEnabled(boolean enabled) {
        _delegate.setEnabled(enabled);
    }

    public void paint(Graphics g) {
        _delegate.paint(g);
        _target.paint(g);
    }

    public void repaint(long tm, int x, int y, int w, int h) {
        _delegate.repaint(tm, x, y, w, h);
    }

    public void print(Graphics g1) {
    }

    public void setBounds(int x, int y, int width, int height) {
        _delegate.setBounds(x, y, width, height);
    }

    public void handleEvent(AWTEvent e) {
    }

    public Point getLocationOnScreen() {
        return _target.getLocationOnScreen();
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public Dimension getMinimumSize() {
        return _delegate.getMinimumSize();
    }

    public ColorModel getColorModel() {
        return _delegate.getColorModel();
    }

    public Graphics getGraphics() {
        return _target.getGraphics();
    }

    public Toolkit getToolkit() {
        return _delegate.getToolkit();
    }

    public FontMetrics getFontMetrics(Font font) {
        return _target.getFontMetrics(font);
    }

    public void setForeground(Color color) {
        _delegate.setForeground(color);
    }

    public void setBackground(Color color) {
        _delegate.setBackground(color);
    }

    public void setFont(Font font) {
        _delegate.setFont(font);
    }

    public void setCursor(Cursor cursor) {
        _delegate.setCursor(cursor);
    }

    public void requestFocus() {
        _target.requestFocus();
    }

    public boolean isFocusTraversable() {
        return false;
    }

    public Image createImage(ImageProducer producer) {
        return _delegate.createImage(producer);
    }

    public Image createImage(int width, int height)
    {
        return _delegate.createImage(width, height);
    }

    public boolean prepareImage(Image img, int w, int h,
                                ImageObserver imageObserver) {
        return _delegate.prepareImage(img, w, h, imageObserver);
    }

    public int checkImage(Image img, int w, int h,
                          ImageObserver imageObserver)
    {
        return _delegate.checkImage(img, w, h, imageObserver);
    }

    //
    //
    //

    public Dimension preferredSize() {
        return getPreferredSize();
    }

    public Dimension minimumSize() {
        return getMinimumSize();
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    public void enable() {
        setEnabled(true);
    }

    public void disable() {
        setEnabled(false);
    }

    public void reshape(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
    }

    void clearRectBeforePaint(Graphics g, Rectangle r) {
        g.clearRect(r.x, r.y, r.width, r.height);
    }

    //
    // 1.3
    //

    public void coalescePaintEvent(PaintEvent e) {
    }

    public GraphicsConfiguration getGraphicsConfiguration() {
        //return _target.getGraphicsConfiguration();
        return null;                // XXX
    }

    //
    // helpers
    //

    void repaint() {
        Dimension sz = _target.getSize();
        repaint(0, 0, 0, sz.width, sz.height);
    }

    //
    //
    //

    protected class Delegate extends Component
    {
        public void paint(Graphics g) {
            Dimension sz = _target.getSize();

            Color c = getBackground();
            if (c == null)
                c = SystemColor.text;

            g.setColor(c);
            g.fillRect(0, 0, sz.width, sz.height);

            javax.swing.plaf.basic.BasicGraphicsUtils.drawLoweredBezel(
                g, 0, 0, sz.width, sz.height,
                SystemColor.controlShadow,
                SystemColor.controlDkShadow,
                SystemColor.controlHighlight,
                SystemColor.controlLtHighlight);

            g.setFont(new Font("Dialog", Font.BOLD, 12));

            String className = _target.getClass().getName();
            className = className.substring(className.lastIndexOf('.') + 1);

            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(className);
            int h = fm.getHeight() - fm.getDescent();

            int x = (sz.width - w) / 2;

            //        g.setColor(Color.white);
            //        g.drawString(className, x+1, (sz.height - h) / 2 + h + 1);
            g.setColor(Color.black);
            g.drawString(className, x, (sz.height - h) / 2 + h);

        }

        public Dimension getMinimumSize() {
            String className = _target.getClass().getName();
            className = className.substring(className.lastIndexOf('.') + 1);

            FontMetrics fm = this.getFontMetrics(new Font("Dialog", Font.BOLD, 12));
            int w = fm.stringWidth(className);
            int h = fm.getHeight();

            return new Dimension(w + 10, h + 4);
        }
    }
}
