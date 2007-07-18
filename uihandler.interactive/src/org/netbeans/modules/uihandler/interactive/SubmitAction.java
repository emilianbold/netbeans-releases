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
package org.netbeans.modules.uihandler.interactive;

import org.netbeans.modules.uihandler.*;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.modules.uihandler.api.Controller;
import org.openide.awt.Actions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;

public final class SubmitAction extends CallableSystemAction {
    
    public void performAction() {
        Controller.getDefault().submit();
    }
    
    
    public String getName() {
        return NbBundle.getMessage(SubmitAction.class, "CTL_SubmitAction");
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/uihandler/tachometer.png";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    public Component getToolbarPresenter() {
        return new NrButton(this);
    }
   
    private static final class NrButton extends JButton
    implements PropertyChangeListener, Runnable, ActionListener {
        private PropertyChangeListener weakL;
        
        private ImageIcon tacho;
        private ImageIcon tachoOk;
        private Timer timer;
        
        public NrButton(Action action) {
            Actions.connect(this, action);
            weakL = WeakListeners.propertyChange(this, Controller.getDefault());
            Controller.getDefault().addPropertyChangeListener(weakL);
            
            timer = new Timer(100, this);
            
            tacho = new ImageIcon(Utilities.loadImage("org/netbeans/modules/uihandler/tachometer24.png"));
            tachoOk = new ImageIcon(Utilities.loadImage("org/netbeans/modules/uihandler/tachometer-ok.png"));
            setIcon(tacho);
            setToolTipText(NbBundle.getMessage(SubmitAction.class, "CTL_SubmitAction"));
        }
    
        public void propertyChange(PropertyChangeEvent arg0) {
            SwingUtilities.invokeLater(this);
        }
        
        public void run() {
            setIcon(tachoOk);
            setEnabled(true);
            timer.restart();
            
            setToolTipText(NbBundle.getMessage(
                SubmitAction.class, 
                "MSG_SubmitAction", 
                Controller.getDefault().getLogRecordsCount()
            )); // NOI18N
        }
    
        public void actionPerformed(ActionEvent arg0) {
            setIcon(tacho);
            timer.stop();
        }
    
        @Override
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
            if ("PreferredIconSize".equals(propertyName)) { // NOI18N
                run();
            }
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
        
        
        @Override
        public void setIcon(Icon original) {
            int size = 16;
            Object prop = getClientProperty("PreferredIconSize"); //NOI18N
            
            if (prop instanceof Integer) {
                if (((Integer) prop).intValue() == 24) {
                    size = 24;
                }
            }
            
            
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
        }
    } // end of NrButton
}
