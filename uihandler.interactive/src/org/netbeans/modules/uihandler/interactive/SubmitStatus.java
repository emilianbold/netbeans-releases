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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uihandler.interactive;

import org.netbeans.modules.uihandler.*;
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
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author jarda
 */
public class SubmitStatus implements StatusLineElementProvider {

    public SubmitStatus() {
    }

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
            
            tacho = new ImageIcon(Utilities.loadImage("org/netbeans/modules/uihandler/tachometer24.png"));
            tachoOk = new ImageIcon(Utilities.loadImage("org/netbeans/modules/uihandler/tachometer-ok.png"));
            hints = new ImageIcon(Utilities.loadImage("org/netbeans/lib/uihandler/def.png"));
            setIcon(tacho);
            setToolTipText(NbBundle.getMessage(SubmitAction.class, "CTL_SubmitAction"));

            addMouseListener(this);

            adjustSize();
        }
    
        public void addNotify() {
            adjustSize();
            super.addNotify();
            adjustSize();
        }
        
        public void propertyChange(PropertyChangeEvent arg0) {
            SwingUtilities.invokeLater(this);
        }
        
        public void run() {
            setIcon(tachoOk);
            timer.restart();
            
            adjustSize();
        }
    
        public void actionPerformed(ActionEvent arg0) {
            setIcon(tacho);
            timer.stop();
        }

        @SuppressWarnings("deprecated")
        private void adjustSize() {
            String msg;
            if (Controller.getDefault().isAutomaticSubmit()) {
                msg = NbBundle.getMessage(SubmitAction.class, "MSG_ShowHints", Controller.getDefault().getLogRecordsCount());
            } else {
                msg = NbBundle.getMessage(SubmitAction.class, "MSG_SubmitAction", Controller.getDefault().getLogRecordsCount());
            }
            setToolTipText(msg); // NOI18N
            resize(0, 16);
        }
        
        @SuppressWarnings("deprecated")
        @Deprecated
        public void resize(int w, int h) {
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
            if (Controller.getDefault().getLogRecordsCount() < 800) {
                //&& Installer.timesSubmitted() == 0) {
                super.resize(0, 16);
            } else {
                super.resize(16, 16);
            }
        }
        
        @Override
        public void setIcon(Icon original) {
            if (original == null) {
                super.setIcon(original);
                return;
            }
            if (Controller.getDefault().isAutomaticSubmit()) {
                super.setIcon(hints);
                return;
            }
            
            int size = 16;
            
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D imgG = (Graphics2D) img.getGraphics();
            imgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            imgG.drawImage(((ImageIcon)original).getImage(), 0, 0, size, size, null);
            
            int half = size / 2;
            final Arc2D bigger = new Arc2D.Double();
            bigger.setArcByCenter(half, half, half, 90, -(360.0 / 1000.0) * Controller.getDefault().getLogRecordsCount(), Arc2D.PIE);
            final Arc2D smaller = new Arc2D.Double();
            smaller.setArcByCenter(half, half, size == 24 ? 5.0 : 3.0, 0, 360, Arc2D.PIE);
              
            int s = Controller.getDefault().getLogRecordsCount();
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
            
            super.setIcon(new ImageIcon(img));
            
            adjustSize();
        }

        public void mouseClicked(MouseEvent e) {
            URL hint = Controller.getDefault().getHintsURL();
            if (hint == null || e.isPopupTrigger()) {
                action.actionPerformed(new ActionEvent(this, 0, ""));
            } else {
                HtmlBrowser.URLDisplayer.getDefault().showURL(hint);
            }
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    } // end of NrButton
    
}
