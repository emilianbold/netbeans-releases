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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.webclient;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
// import javax.swing.*;

import org.openide.windows.TopComponent;

import org.mozilla.webclient.*;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Adds some functionality to Mozilla browser.
 *
 * @author  Radim.Kubacki@sun.com
 */
class WebclientBrowserComponent extends Frame {

    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 350;

    private static final boolean debug = true;
    
    private WebclientBrowserImpl      browserImpl;
    /** java.awt.Canvas for drawing of pages. */
    private BrowserControlCanvas    browser;
    /** toolbar button */
    private Button                 bPrev;
    /** toolbar button */
    private Button                 bNext;
    /** toolbar button */
    private Button                 bHome;
    /** toolbar button */
    private Button                 bReload;
    /** toolbar button */
    private Button                 bStop;
    
    private TextField              tfLocation;
    
    private BrowserListener        bListener;
    
    /** Creates new MozillaBrowserComponent */
    public WebclientBrowserComponent (WebclientBrowserImpl browserImpl) {
        super ();
        if (debug) System.out.println ("Creating MozillaBrowserComponent"); // NOI18N
        setLayout (new BorderLayout ());
        this.browserImpl = browserImpl;
        
        try {
            browser = (BrowserControlCanvas)
                    browserImpl.getBrowserControl ().queryInterface(BrowserControl.BROWSER_CONTROL_CANVAS_NAME);
            add (browser, "Center");    // NOI18N
            browser.setVisible (true);
            
            bListener = new BrowserListener ();
            addWindowListener(bListener);
            
            initToolbar ();
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace ();
        }
        catch (NullPointerException ex) {
            ex.printStackTrace ();
        }
    }
   
    private void initToolbar () {
        Panel toolbar = new Panel ();
        toolbar.add (bPrev = new Button (NbBundle.getMessage(WebclientBrowserComponent.class,"LBL_Prev")));
        toolbar.add (bNext = new Button (NbBundle.getMessage(WebclientBrowserComponent.class,"LBL_Next")));
        toolbar.add (bStop = new Button (NbBundle.getMessage(WebclientBrowserComponent.class,"LBL_Stop")));
        toolbar.add (bReload = new Button (NbBundle.getMessage(WebclientBrowserComponent.class,"LBL_Reload")));
        toolbar.add (bHome = new Button (NbBundle.getMessage(WebclientBrowserComponent.class,"LBL_Home")));
        toolbar.add (tfLocation = new TextField (30));
        add (toolbar, "North"); // NOI18N
        bPrev.addActionListener(bListener);
        bNext.addActionListener(bListener);
        bStop.addActionListener(bListener);
        bReload.addActionListener(bListener);
        bHome.addActionListener(bListener);
        tfLocation.addActionListener(bListener);
        browserImpl.addPropertyChangeListener(bListener);
        refreshToolbar ();
    }
    
    /** Updates toolbar buttons and location */
    void refreshToolbar () {
        bPrev.setEnabled(browserImpl.isBackward());
        bNext.setEnabled(browserImpl.isForward());
        java.net.URL url = browserImpl.getURL();
        if (url != null) {
            if (debug) System.out.println("setText to "+url);   // NOI18N
            tfLocation.setText(url.toString());
        }
    }

    /** 
     * Initialize rest of Webclient interfaces.
     */
    public void addNotify() {
        super.addNotify ();
        browserImpl.initialize ();
    }
    
    /**
    * Returns preferred size.
    */
    public java.awt.Dimension getPreferredSize () {
        java.awt.Dimension superPref = super.getPreferredSize ();
        return new java.awt.Dimension (
                   Math.max (DEFAULT_WIDTH, superPref.width),
                   Math.max (DEFAULT_HEIGHT, superPref.height)
               );
    }

    /**
     * browser implementation does most of work
     */
    private void delete () {
        browser.setVisible (false);
        browser = null;
    }
    
    private class BrowserListener 
    implements ActionListener, WindowListener, PropertyChangeListener, Runnable {
        public void actionPerformed  (ActionEvent e) {
            if (e.getSource () == bPrev) {
                browserImpl.backward();
            }
            else if (e.getSource () == bNext) {
                browserImpl.forward();
            }
            else if (e.getSource () == bHome) {
                try {
                    java.net.URL homePage = new java.net.URL (org.openide.awt.HtmlBrowser.getHomePage());
                    browserImpl.setURL(homePage);
                }
                catch (java.net.MalformedURLException ex) {
                    browserImpl.setStatusMessage(NbBundle.getMessage(WebclientBrowserComponent.class,"MSG_Cannot_get_home_page"));
                }
            }
            else if (e.getSource () == bStop) {
                browserImpl.stopLoading();
            }
            else if (e.getSource () == bReload) {
                browserImpl.reloadDocument ();
            }
            else if (e.getSource () == tfLocation) {
                try {
                    browserImpl.setURL (new java.net.URL (tfLocation.getText()));
                }
                catch (java.net.MalformedURLException ex) {
                    tfLocation.setText ("");
                }
            }
        }
        
        public void propertyChange (PropertyChangeEvent evt) {
            String property = evt.getPropertyName ();
            if (property == null) {
                RequestProcessor.postRequest (this);
                return;
            }

            if (property.equals (browserImpl.PROP_URL) ||
                property.equals (browserImpl.PROP_TITLE) ||
                property.equals (browserImpl.PROP_BACKWARD) ||
                property.equals (browserImpl.PROP_FORWARD))
                RequestProcessor.postRequest (this);
        }

        
        private void close () {
            // System.out.println("destroying the BrowserControl");
            // WebclientBrowserComponent.this.delete ();
            // should close the BrowserControlCanvas
            browserImpl.destroy ();
        }
        
        public void windowClosing(WindowEvent e) {
            setVisible (false);
        }
        public void windowClosed(WindowEvent e) {}
        public void windowActivated(java.awt.event.WindowEvent windowEvent) {}
        public void windowDeactivated(java.awt.event.WindowEvent windowEvent) {}
        public void windowDeiconified(java.awt.event.WindowEvent windowEvent) {}
        public void windowIconified(java.awt.event.WindowEvent windowEvent) {}
        public void windowOpened(java.awt.event.WindowEvent windowEvent) {}
        
        public void run() {
            refreshToolbar();
        }
        
    }
}
