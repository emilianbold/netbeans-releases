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

/**
 *
 * $Id$ $Revision$ $Date$
 *
 */
package org.netbeans.modules.visualweb.gravy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.Point;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.Timeouts;

/**
 * Flasher is a utility class allowing to highlight special point by drawing a cross with
 * this point as center. It can be useful when it's necessary to see where is given point situated
 * @version 1.0 Mar 15, 2005
 */
public class Flasher implements Timeoutable{
    
    /**
     * Default color of the cross. Now it's red.
     */
    public static final Color DEFAULT_COLOR=new Color(255,0,0);
    protected static final int len=30;
    protected Color color=DEFAULT_COLOR;
    protected static long WAIT_TIME=1000;

    /**
     * By default, flash is executed in the main thread
     */
    protected boolean separateThread=false;
    protected Timeouts timeouts;

    static {
	Timeouts.initDefault("Flasher.FlashTimeout", WAIT_TIME);
    }

    public void setTimeouts(Timeouts timeouts) {
	this.timeouts = timeouts;
    }

    public Timeouts getTimeouts() {
	return timeouts;
    }
    
    /**
     * Constructs new object ready for flashing
     */
    public Flasher(){
        this(false);
    }
    
    /**
     * Constructs new object ready for flashing
     * @param separateThread If true, flash will be started in separate thread
     * and test execution continues without delay
     */
    public Flasher(boolean separateThread){
        this.separateThread=separateThread;
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
    }

    /**
     * Sets color of the cross. Must be used before flash() call
     * @param color New color
     */
    public void setColor(Color color){
        this.color=color;
    }

    /**
     * Draws a cross with center in specified point of component
     * Delays by "Flasher.FlashTimeout" timeout, then cross disappears
     * @param p Center of the cross
     * @param op Operator for parent component of the point
     */
    public void flash(ComponentOperator op, Point p){        
        flash(op.getSource(),p);
    }
    
    /**
     * Draws a cross with center in specified point of component
     * Delays by "Flasher.FlashTimeout" timeout, then cross disappears
     * @param x x-coordinate of center of the cross
     * @param y x-coordinate of center of the cross
     * @param op Operator for parent component of the point
     */
    public void flash(ComponentOperator op, int x, int y){        
        flash(op.getSource(),x,y);
    }

    /**
     * Draws a cross with center in specified point of component
     * Delays by "Flasher.FlashTimeout" timeout, then cross disappears
     * @param x x-coordinate of center of the cross
     * @param y x-coordinate of center of the cross
     * @param comp Parent component of the point
     */
    public void flash(Component comp, int x, int y){        
        flash(comp,new Point(x,y));
    }

    /**
     * Draws a cross with center in specified point of component
     * Delays by "Flasher.FlashTimeout" timeout, then cross disappears
     * @param p Center of the cross
     * @param comp Parent component of the point
     */
    public void flash(final Component comp, final Point p){        
                                      	
       if (separateThread){
           new Thread(new Runnable() {
                   public void run() {
                       doFlash(comp,p);
                   }
               }).start();
       }else{
           doFlash(comp,p);
       }
    }

    /**
     *  Draws a cross. Called from flash() in the main thread or in a 
     * separate thread depending on separateThread field value
     */
    protected void doFlash(Component comp, Point p){        

        // we need to find JDialog or JFrame ancestor to use JLayeredPane
        JFrame f;
        JDialog d;
        JLayeredPane lpane=null;
        if (comp instanceof JDialog){
            lpane=((JDialog)comp).getLayeredPane();
        }else if (comp instanceof JFrame){
            lpane=((JFrame)comp).getLayeredPane();
        }else if ((d=(JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, comp))!=null){
            lpane=d.getLayeredPane();
        }else if ((f=(JFrame)SwingUtilities.getAncestorOfClass(JFrame.class, comp))!=null){
            lpane=f.getLayeredPane();
        }else{
            throw new JemmyException("Containing dialog or frame not found");
        }
        Point p1= new Point (p.x+comp.getLocationOnScreen().x-lpane.getLocationOnScreen().x,
                             p.y+comp.getLocationOnScreen().y-lpane.getLocationOnScreen().y);
        JPanel flash=new FlashPane(p1,lpane.getSize());
        lpane.add(flash, JLayeredPane.DRAG_LAYER);
        lpane.repaint();
        try{
            Thread.currentThread().sleep(timeouts.getTimeout("Flasher.FlashTimeout"));
	}catch(InterruptedException e) {}
        lpane.remove(flash);
        lpane.repaint();
    }

    protected class FlashPane extends JPanel{
        
        private Point p;
        public FlashPane(Point p, Dimension d){
            super();
            this.p=p;
            setSize(d);
            setOpaque(false);
        }        
       
        public synchronized void paint(Graphics g){
            super.paint(g);
            g.setColor(color);
            g.drawLine(p.x-len,p.y,p.x+len,p.y);
            g.drawLine(p.x,p.y-len,p.x,p.y+len);
        }              
    }
}
