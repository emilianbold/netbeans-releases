/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * BasicScrollingTabDisplayerUI.java
 *
 * Created on March 19, 2004, 1:08 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;

/**
 * Base class for tab displayers that have scrollable tabs.
 *
 * @author Tim Boudreau
 */
public abstract class BasicScrollingTabDisplayerUI extends BasicTabDisplayerUI {
    protected AbstractButton[] controlButtons;
    protected LayoutManager layoutManager;
    private Rectangle scratch = new Rectangle();

    /**
     * Creates a new instance of BasicScrollingTabDisplayerUI
     */
    public BasicScrollingTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    protected final TabLayoutModel createLayoutModel() {
        DefaultTabLayoutModel dtlm = new DefaultTabLayoutModel(
                displayer.getModel(),
                displayer);
        return new ScrollingTabLayoutModel(dtlm, selectionModel,
                displayer.getModel());
    }

    protected TabState createTabState() {
        return new ScrollingTabState();
    }
    
    protected HierarchyListener createHierarchyListener() {
        return new ScrollingHierarchyListener();
    }

    public void makeTabVisible (int tab) {
        if (scroll().makeVisible(tab, getTabsAreaWidth())) {
            getTabsVisibleArea(scratch);
            displayer.repaint(scratch.x, scratch.y, scratch.width, scratch.height);
        }
    }

    /**
     * Returns the width of the tabs area
     */
    protected final int getTabsAreaWidth() {
        int result = displayer.getWidth();
        Insets ins = getTabAreaInsets();
        return result - (ins.left + ins.right);
    }

    protected final int getLastVisibleTab() {
        if (displayer.getModel().size() == 0) {
            return -1;
        }
        return scroll().getLastVisibleTab(getTabsAreaWidth());
    }

    protected final int getFirstVisibleTab() {
        if (displayer.getModel().size() == 0) {
            return -1;
        }
        return scroll().getFirstVisibleTab(getTabsAreaWidth());
    }

    protected void install() {
        super.install();
        layoutManager = createLayout();
        controlButtons = createControlButtons();
        installControlButtons();
        ((ScrollingTabLayoutModel) layoutModel).setPixelsToAddToSelection (
                defaultRenderer.getPixelsToAddToSelection());
    }

    protected void uninstall() {
        super.uninstall();
        displayer.setLayout(null);
        displayer.removeAll();
    }

    protected abstract LayoutManager createLayout();

    protected abstract AbstractButton[] createControlButtons();
    
    protected ComponentListener createComponentListener() {
        return new ScrollingDisplayerComponentListener();
    }

    private int lastKnownModelSize = Integer.MAX_VALUE;
    /** Overrides <code>modelChanged()</code> to clear the transient information in the
     * state model, which may now contain tab indices that don't exist, and also
     * to clear cached width/last-visible-tab data in the layout model, and ensure that
     * the selected tab is visible.
     */
    protected void modelChanged() {
        scroll().clearCachedData();
        int index = selectionModel.getSelectedIndex();
        
        //If the user has intentionally scrolled the selected tab offscreen, do ensure space is
        //optimally used, but don't volunteer to radically change the scroll point
        if (index >= scroll().getCachedFirstVisibleTab() && index < scroll().getCachedLastVisibleTab()) {
            makeTabVisible(selectionModel.getSelectedIndex());
        }
        
        int modelSize = displayer.getModel().size();
        if (modelSize < lastKnownModelSize) {
            //When closing tabs, make sure we resync the state, so the
            //user doesn't end up with a huge gap due to closed tabs
            scroll().ensureAvailableSpaceUsed(true);
        }
        lastKnownModelSize = modelSize;
        super.modelChanged();
    }

    protected void installControlButtons() {
        displayer.setLayout(layoutManager);
        for (int i = 0; i < controlButtons.length; i++) {
            displayer.add(controlButtons[i]);
        }
    }

    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    /**
     * Convenience getter for the layout model as an instance of
     * ScrollingTabLayoutModel
     */
    protected final ScrollingTabLayoutModel scroll() {
        return (ScrollingTabLayoutModel) layoutModel;
    }

    /**
     * Overridden to update the offset of the ScrollingTabLayoutModel on mouse
     * wheel events
     */
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        int i = e.getWheelRotation();
        //clear the mouse-in-tab index so we don't occasionally have
        //tabs the mouse is not in scrolling away looking as if the mouse
        //is in them
        tabState.clearTransientStates();
        int offset = scroll().getOffset();
        if (i > 0 && (offset < displayer.getModel().size() - 1)) {
            if (scroll().isLastTabClipped()) {
                scroll().setOffset(offset + 1);
            }
        } else if (i < 0) {
            if (offset >= 0) {
                scroll().setOffset(offset - 1);
            }
        } else {
            return;
        }
        

        //tabState.repaintAllTabs();
        //XXX should optimize this - need to make sure the space below the tabs
        //is painted on metal and win classic
        displayer.repaint();
    }


    protected class ScrollingTabState extends BasicTabState {
        public int getState(int tabIndex) {
            int result = super.getState(tabIndex);
            int first = getFirstVisibleTab();
            int last = getLastVisibleTab();

            if (tabIndex < first || tabIndex > last) {
                return TabState.NOT_ONSCREEN;
            }
            if (first == last && first == tabIndex
                    && displayer.getModel().size() > 1) {
                //We have a very small area to fit tabs - smaller than even the
                //minimum clip width, probably < 40 pixels.  Definitely don't
                //want to display a close button or much of anything else
                result |= TabState.CLIP_LEFT | TabState.CLIP_RIGHT;

            } else if (getTabsAreaWidth() < scroll()
                    .getMinimumLeftClippedWidth()
                    + scroll().getMinimumRightClippedWidth()
                    && tabIndex == first && last == first - 1 && displayer.getModel()
                    .size()
                    > 1 && scroll().isLastTabClipped()) {
                //when we're displaying two tabs in less than enough room,
                //make sure a truncated tab is never displayed with a close button
                result |= TabState.CLIP_LEFT;
            } else {
                if (tabIndex == first && scroll().getOffset() == first) {
                    result |= TabState.CLIP_LEFT;
                }
                if (tabIndex == last && scroll().isLastTabClipped()) {
                    result |= TabState.CLIP_RIGHT;
                }
            }
            return result;
        }
    }

    protected class ScrollingDisplayerComponentListener extends ComponentAdapter {
        public void componentResized(ComponentEvent e) {
            //Notify the layout model that its cached sizes are invalid
            makeTabVisible(selectionModel.getSelectedIndex());
        }
    }
    
    protected class ScrollingHierarchyListener extends DisplayerHierarchyListener {
        public void hierarchyChanged(HierarchyEvent e) {
            super.hierarchyChanged (e);
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (displayer.isShowing()) {
                    //#47850 - for some reason, uninstall can be called on the Ui class, before this gets processed.
                    // check for null values just to be sure.
                    if (tabState != null && selectionModel != null) {
                        tabState.setActive (displayer.isActive());
                        makeTabVisible (selectionModel.getSelectedIndex());
                    }
                }
            }
        }
        
    }

    /** A convenience button class which will continue re-firing its action
     * on a timer for as long as the button is depressed.  Used for left-right scroll
     * buttons.
     */
    protected static class TimerButton extends JButton implements ActionListener {
        Timer timer = null;
        Image disabledImage = null;
        Image enabledImage = null;

        public TimerButton(Action a) {
            super(a);
        }

        private Timer getTimer() {
            if (timer == null) {
                timer = new Timer(400, this);
                timer.setRepeats(true);
            }
            return timer;
        }

        int count = 0;

        public void actionPerformed(java.awt.event.ActionEvent e) {
            count++;
            if (count > 2) {
                if (count > 5) {
                    timer.setDelay(75);
                } else {
                    timer.setDelay(200);
                }
            }
            performAction();
        }

        private void performAction() {
            if (!isEnabled()) {
                stopTimer();
                return;
            }
            getAction().actionPerformed(new ActionEvent(this,
                                                        ActionEvent.ACTION_PERFORMED,
                                                        getActionCommand()));
        }

        private void startTimer() {
            performAction();
            Timer t = getTimer();
            if (t.isRunning()) {
                return;
            }
            repaint();
            t.setDelay(400);
            t.start();
        }

        private void stopTimer() {
            if (timer != null) {
                timer.stop();
            }
            repaint();
            count = 0;
        }

        protected void processMouseEvent(MouseEvent me) {
            if (isEnabled() && me.getID() == me.MOUSE_PRESSED) {
                startTimer();
            } else if (me.getID() == me.MOUSE_RELEASED) {
                stopTimer();
            } else {
                super.processMouseEvent(me);
            }
        }

        protected void processFocusEvent(FocusEvent fe) {
            super.processFocusEvent(fe);
            if (fe.getID() == fe.FOCUS_LOST) {
                stopTimer();
            }
        }

        protected void paintComponent(Graphics g) {
            boolean enabled = isEnabled();
            if (enabled && enabledImage == null
            || !enabled && disabledImage == null) {
                GraphicsConfiguration gc = getGraphicsConfiguration();
                Image intermediateImage = gc.createCompatibleImage(16, 18, Transparency.BITMASK);
                Graphics2D gImg = (Graphics2D)intermediateImage.getGraphics();
                gImg.setComposite(AlphaComposite.Src);
                gImg.setColor(new Color(0, 0, 0, 0));
                gImg.fillRect(0, 0, 16, 18);
                super.paintComponent(gImg);
                gImg.dispose();
                if (enabled) {
                    enabledImage = intermediateImage;
                }
                else {
                    disabledImage = intermediateImage;
                }
            }

            g.drawImage(enabled? enabledImage: disabledImage, 0, 0, null);
        }
    }

    /** A convenience button class which fires its action event on mouse pressed, not
     * mouse released.   Used to enable press-and-drag behavior on the tab list popup.
     */
    protected static class OnPressButton extends JButton {
        public OnPressButton(Action a) {
            super(a);
        }

        protected void processMouseEvent(MouseEvent me) {
            super.processMouseEvent(me);
            if (isEnabled() && me.getID() == me.MOUSE_PRESSED) {
                getAction().actionPerformed(new ActionEvent(this,
                                                            ActionEvent.ACTION_PERFORMED,
                                                            "pressed"));
            }
        }
    }

    static SoftReference ctx = null;

    /**
     * Provides an offscreen graphics context so that widths based on character
     * size can be calculated correctly before the component is shown
     */
    public static Graphics2D getOffscreenGraphics() {
        BufferedImage result = null;
        //XXX multi-monitors w/ different resolution may have problems;
        //Better to call Toolkit to create a screen graphics
        if (ctx != null) {
            result = (BufferedImage) ctx.get();
        }
        if (result == null) {
            result = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            ctx = new SoftReference(result);
        }
        return (Graphics2D) result.getGraphics();
    }
}
