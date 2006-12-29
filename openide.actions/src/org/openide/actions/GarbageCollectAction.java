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
package org.openide.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CallableSystemAction;

import java.awt.*;
import java.awt.event.*;

import java.text.Format;
import java.text.MessageFormat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;


// Toolbar presenter like MemoryMeterAction, except:
// 1. Does not have a mark etc.
// 2. But pressing it runs GC.
// 3. Slim profile fits nicely in the menu bar (at top level).
// 4. Displays textual memory usage directly, not via tooltip.
// Intended to be unobtrusive enough to leave on for daily use.

/**
 * Perform a system garbage collection.
 * @author Jesse Glick, Tim Boudreau
 */
public class GarbageCollectAction extends CallableSystemAction {
    public String getName() {
        return NbBundle.getBundle(GarbageCollectAction.class).getString("CTL_GarbageCollect"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(GarbageCollectAction.class);
    }

    public void performAction() {
        gc();
    }

    private static void gc() {
        // Can be slow, would prefer not to block on it.
        RequestProcessor.getDefault().post(
            new Runnable() {
                public void run() {
                    System.gc();
                    System.runFinalization();
                    System.gc();
                }
            }
        );
    }

    protected boolean asynchronous() {
        return false;
    }

    public Component getToolbarPresenter() {
        return new MemButton();
    }

    /*
    public static void main(String[] x) {
        javax.swing.JFrame f = new javax.swing.JFrame();
        f.getContentPane().add(new javax.swing.JTextField("Hello world"));
        javax.swing.JMenuBar b = new javax.swing.JMenuBar();
        b.add(new javax.swing.JMenu("Test"));
        b.add(new MemButton());
        f.setJMenuBar(b);
        f.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        f.pack();
        f.show();
    }
     */
    private static final class MemButton extends JLabel implements ActionListener, ComponentListener {
        private static final boolean AUTOMATIC_REFRESH = System.getProperty("org.netbeans.log.startup") == null; // NOI18N
        private static final int TICK = 1000;
        private final Runtime r = Runtime.getRuntime();
        private final Format f = new MessageFormat(" {0,number,0.0}/{1,number,0.0}MB "); // NOI18N
        private final Timer t;
        private double proportion = 0.0d;

        /** cyclic buffer of historical values of proportion, after every tick */
        private double[] graph = null;

        /** current index into graph: where next value will be placed (not yet usable) */
        private int graphPointer;

        /** start of usable data in graph */
        private int graphBase;
        private boolean containsMouse = false;
        
        private boolean isInitialized = false;

        public MemButton() {
            t = new Timer(TICK, this);
            enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);

            // To get the size right:
            setText(f.format(new Object[] { new Double(999.0d), new Double(999.0d) }));
            setOpaque(false);

            setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
            setToolTipText(NbBundle.getMessage(GarbageCollectAction.class, "CTL_GC"));
            
            setHorizontalAlignment( JLabel.CENTER );
            setMinimumSize( getPreferredSize() );
            isInitialized = true;
        }

        public void paintBorder(Graphics g) {
        }

        public Dimension getMaximumSize() {
            return updateHeight(super.getMaximumSize());
        }

        public Dimension getPreferredSize() {
            return updateHeight(super.getPreferredSize());
        }

        /** #91618: Update preferred and maximum height according to parent's
         * height, count with borders.
         */
        private Dimension updateHeight (Dimension size) {
            Component parent = getParent();
            if (parent != null) {
                Insets insets = getInsets();
                size.height = parent.getHeight() - (insets.top + insets.bottom);
            }
            return size;
        }

        public void addNotify() {
            super.addNotify();
            getParent().addComponentListener(this);

            if (getParent().isVisible()) {
                if (AUTOMATIC_REFRESH) {
                    t.start();
                }

                update(false);
            }
        }

        public void removeNotify() {
            getParent().removeComponentListener(this);
            t.stop();
            super.removeNotify();
        }

        public void componentResized(ComponentEvent e) {
        }

        public void componentMoved(ComponentEvent e) {
        }

        public void componentShown(ComponentEvent e) {
            if (AUTOMATIC_REFRESH) {
                t.start();
            }

            update(false);
        }

        public void componentHidden(ComponentEvent e) {
            t.stop();
        }

        private void update(boolean ticked) {
            long total = r.totalMemory();
            long used = total - r.freeMemory();
            proportion = ((double) used) / total;

            if (ticked && (graph != null)) {
                graph[graphPointer] = proportion;
                graphPointer = (graphPointer + 1) % graph.length;

                if (graphPointer == graphBase) {
                    graphBase = (graphPointer + 1) % graph.length;
                }

                //System.err.println("graph.length=" + graph.length + " graphPointer=" + graphPointer + " graphBase=" + graphBase + " graph=" + java.util.Arrays.asList(org.openide.util.Utilities.toObjectArray(graph)));
            }

            Double _total = new Double(((double) total) / 1024 / 1024);
            Double _used = new Double(((double) used) / 1024 / 1024);
            String text = f.format(new Object[] { _used, _total });
            setText(text);
        }

        protected void paintComponent(Graphics g) {
            Dimension size = getSize();
            size.height -= 4; // better fits in typical containers
            size.width -= 3;
            g.translate (3, 1);
            Color old = g.getColor();

            try {
                Color c = UIManager.getColor("controlShadow"); //NOI18N
                g.setColor(c); // NOI18N

                int bufferLength = size.width - 2; // 1 pixel border on each side

                if (graph == null) {
                    graph = new double[bufferLength]; // initially all 0.0d
                    graphPointer = 0;
                    graphBase = 0;
                } else if (graph.length != bufferLength) {
                    int oldLength = graph.length;

                    // Resize the buffer.
                    double[] nue = new double[bufferLength];

                    // System.arraycopy would be slicker, but this is easier:
                    int i = bufferLength;

                    for (int j = graphPointer; (j != graphBase) && (i > 0);) {
                        j = ((j + oldLength) - 1) % oldLength;
                        nue[--i] = graph[j];
                    }

                    graph = nue;
                    graphPointer = 0;
                    graphBase = i % bufferLength;
                }

                // Now paint the graph.
                int x = size.width - 3;
                assert graphBase >= 0 : "graphBase=" + graphBase;
                assert graphBase < bufferLength : "graphBase=" + graphBase + " bufferLength=" + bufferLength;

                for (int i = graphPointer; i != graphBase;) {
                    assert i >= 0 : "i=" + i;
                    assert i < bufferLength : "i=" + i + " bufferLength=" + bufferLength;
                    i = ((i + bufferLength) - 1) % bufferLength;

                    double val = graph[i];
                    int drawnVal = (int) ((size.height - 2) * val); // 0 .. size.height - 4

                    //System.err.println("size=" + size + "x=" + x + " drawnVal=" + drawnVal);
                    g.drawLine(x, size.height - 1 - drawnVal, x, size.height - 1);
                    x--;
                }

                // Paint a border.
                c = containsMouse ? getBackground().brighter() : getBackground().darker();
                g.setColor(c);
                g.drawRect(0, 1, size.width - 2, size.height - 2); // i.e. from (0,1) to (w-1,h-1)

                if (containsMouse) {
                    g.drawRect(1, 2, size.width - 4, size.height - 4);
                }
            } finally {
                g.setColor(old);
                g.translate (-3, -1);
            }

            super.paintComponent(g);
        }

        protected void processMouseEvent(MouseEvent me) {
            super.processMouseEvent(me);

            if (me.getID() == me.MOUSE_CLICKED) {
                Graphics g = getGraphics();
                Color old = g.getColor();

                try {
                    g.setColor(UIManager.getColor("info")); //NOI18N
                    g.fillRect(0, 1, getWidth(), getHeight() - 1);
                    g.setColor(UIManager.getColor("infoText")); //NOI18N
                    g.setFont(getFont());

                    FontMetrics fm = g.getFontMetrics();
                    String text = NbBundle.getMessage(GarbageCollectAction.class, "MSG_GC");
                    Rectangle textRect = new Rectangle();
                    SwingUtilities.layoutCompoundLabel(
                        fm, text, null, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER,
                        SwingConstants.LEFT, new Rectangle(), new Rectangle(), textRect, 0
                    );
                    g.drawString(text, textRect.x, textRect.y);
                } finally {
                    g.setColor(old);
                }

                gc();

                // XXX repaint actually can happen before gc()'s Task finishes
                repaint();
            } else if (me.getID() == me.MOUSE_ENTERED) {
                setToolTipText(NbBundle.getMessage(GarbageCollectAction.class, "CTL_GC") + " [" + getText() + "]"); // NOI18N
                containsMouse = true;
                repaint();
            } else if (me.getID() == me.MOUSE_EXITED) {
                containsMouse = false;
                repaint();
            }
        }

        public void actionPerformed(ActionEvent e) {
            // Timer
            update(true);
        }

        private String myText;
        public void setText(String text) {
            this.myText = text;
            if( isInitialized ) {
                repaint();
            } else {
                super.setText(text);
            }
        }

        public String getText() {
            if( isInitialized )
                return myText;
            return super.getText();
        }
    }
}
