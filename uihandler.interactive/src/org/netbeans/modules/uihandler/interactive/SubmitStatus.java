/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uihandler.interactive;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.modules.uihandler.api.Controller;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.awt.StatusLineElementProvider.class, position=500)
public class SubmitStatus implements StatusLineElementProvider {

    public SubmitStatus() {
    }

    @Override
    public Component getStatusLineElement() {
        return new NrLabel(SubmitAction.get(SubmitAction.class));
    }

    private static final class NrLabel extends JLabel
    implements PropertyChangeListener, Runnable, ActionListener, MouseListener {
        private PropertyChangeListener weakL;
        
        private ImageIcon tacho;
        private ImageIcon tachoOk;
        private ImageIcon hints;
        private Timer timer;
        private Action action;
        
        public NrLabel(Action action) {
            weakL = WeakListeners.propertyChange(this, Controller.getDefault());
            Controller.getDefault().addPropertyChangeListener(weakL);
            
            timer = new Timer(100, this);
            this.action = action;
            
            tacho = ImageUtilities.loadImageIcon("org/netbeans/modules/uihandler/tachometer24.png", false);
            tachoOk = ImageUtilities.loadImageIcon("org/netbeans/modules/uihandler/tachometer-ok.png", false);
            hints = ImageUtilities.loadImageIcon("org/netbeans/lib/uihandler/def.png", false);
            setIcon(tacho);
            setToolTipText(NbBundle.getMessage(SubmitAction.class, "CTL_SubmitAction"));

            addMouseListener(this);

            adjustSize();
        }
    
        @Override
        public void addNotify() {
            adjustSize();
            super.addNotify();
            adjustSize();
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent arg0) {
            SwingUtilities.invokeLater(this);
        }
        
        @Override
        public void run() {
            setIcon(tachoOk);
            timer.restart();
            
            adjustSize();
        }
    
        @Override
        public void actionPerformed(ActionEvent arg0) {
            setIcon(tacho);
            timer.stop();
        }

        private void adjustSize() {
            LRUtil.invokeWithLogRecordsCount(new LRUtil.LRRun() {
                @Override
                public void run(int logRecordsCount) {
                    adjustSize(logRecordsCount);
                }
            });
        }
        
        private void adjustSize(int logRecordsCount) {
            String msg;
            if (Controller.getDefault().isAutomaticSubmit()) {
                msg = NbBundle.getMessage(SubmitAction.class, "MSG_ShowHints", logRecordsCount);
            } else {
                msg = NbBundle.getMessage(SubmitAction.class, "MSG_SubmitAction", logRecordsCount);
            }
            setToolTipText(msg); // NOI18N
            resize(0, 16, logRecordsCount);
        }
        
        @Deprecated
        @Override
        public void resize(int w, int h) {
            resize(w, h, -1);
        }
        
        @SuppressWarnings("deprecated")
        private void resize(int w, int h, int logRecordsCount) {
            boolean ignore = Boolean.getBoolean("netbeans.full.hack"); // NOI18N
            if (ignore) {
                super.resize(0, 16);
                return;
            }
            
            if (Controller.getDefault().isAutomaticSubmit()) {
                if (Controller.getDefault().getHintsURL() == null) {
                    super.resize(0, 16);
                } else {
                    super.resize(16, 16);
                }
                return;
            }
            
            // regular mode
            if (logRecordsCount < 0) {
                LRUtil.invokeWithLogRecordsCount(new LRUtil.LRRun() {
                    @Override
                    public void run(int logRecordsCount) {
                        if (logRecordsCount < 800) {
                            //&& Installer.timesSubmitted() == 0) {
                            NrLabel.super.resize(0, 16);
                        } else {
                            NrLabel.super.resize(16, 16);
                        }
                    }
                });
            } else {
                if (logRecordsCount < 800) {
                    //&& Installer.timesSubmitted() == 0) {
                    super.resize(0, 16);
                } else {
                    super.resize(16, 16);
                }
            }
        }
        
        @Override
        public void setIcon(final Icon original) {
            if (original == null) {
                super.setIcon(original);
                return;
            }
            if (Controller.getDefault().isAutomaticSubmit()) {
                super.setIcon(hints);
                return;
            }
            
            final int size = 16;
            
            LRUtil.invokeWithLogRecordsCount(new LRUtil.LRRun() {
                @Override
                public void run(int logRecordsCount) {
                    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D imgG = (Graphics2D) img.getGraphics();
                    imgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    imgG.drawImage(((ImageIcon)original).getImage(), 0, 0, size, size, null);

                    int half = size / 2;
                    final Arc2D bigger = new Arc2D.Double();
                    bigger.setArcByCenter(half, half, half, 90, -(360.0 / 1000.0) * logRecordsCount, Arc2D.PIE);
                    final Arc2D smaller = new Arc2D.Double();
                    smaller.setArcByCenter(half, half, size == 24 ? 5.0 : 3.0, 0, 360, Arc2D.PIE);

                    int s = logRecordsCount;
                    if (s < 800) {
                        imgG.setColor(Color.RED.darker().darker());
                    } else if (s < 990) {
                        imgG.setColor(Color.ORANGE);
                    } else {
                        imgG.setColor(Color.RED);
                    }
                    imgG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

                    Area minus = new Area(bigger);
                    minus.subtract(new Area(smaller));
                    imgG.fill(minus);

                    NrLabel.super.setIcon(new ImageIcon(img));

                    adjustSize(logRecordsCount);
                }
            });
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            URL hint = Controller.getDefault().getHintsURL();
            if (hint == null || e.isPopupTrigger()) {
                action.actionPerformed(new ActionEvent(this, 0, ""));
            } else {
                HtmlBrowser.URLDisplayer.getDefault().showURL(hint);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    } // end of NrButton
    
}
