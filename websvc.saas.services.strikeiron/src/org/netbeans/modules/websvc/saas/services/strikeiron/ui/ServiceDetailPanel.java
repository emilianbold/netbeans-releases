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

package org.netbeans.modules.websvc.saas.services.strikeiron.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.modules.websvc.saas.spi.ServiceData;
import org.openide.awt.HtmlBrowser;

/**
 *
 * @author nam
 */
public class ServiceDetailPanel extends JTextPane {
    private JScrollPane scrollPane;
    private HeaderPanel header;
    private JLabel title;
    private ServiceData currentData;

    public ServiceDetailPanel() {
        header = new HeaderPanel();
        title = header.getTitle();
        Border outsideBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray);        
        Border insideBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
        header.setBorder(compoundBorder);
        initHtmlKit();
        header.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (KeyEvent.VK_ENTER == evt.getKeyCode()) {
                    if (currentData != null) {
                        currentData.setPackageName(header.getPackageName());
                    }
                }
            }
        });
        header.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (currentData != null) {
                    currentData.setPackageName(header.getPackageName());
                }
            }
        });
    }

    private void initHtmlKit() {
        HTMLEditorKit htmlkit = new HTMLEditorKit();
        StyleSheet css = htmlkit.getStyleSheet();
        
        if (css.getStyleSheets() == null) {
            StyleSheet css2 = new StyleSheet();
            Font f = new JList().getFont();
            int size = f.getSize();
            css2.addRule(new StringBuffer("body { font-size: ").append(size) // NOI18N
                    .append("; font-family: ").append(f.getName()).append("; }").toString()); // NOI18N
            css2.addStyleSheet(css);
            htmlkit.setStyleSheet(css2);
        }
        
        setEditorKit(htmlkit);
        
        addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    assert hlevt.getURL() != null;
                    showURL(hlevt.getURL());
                }
            }
        });
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        getScrollPane();
    }

    @Override
    public boolean isEditable() {
        return false;
    }
    
    JScrollPane getScrollPane() {
        if (scrollPane == null) {
            Container p = getParent();
            if (p instanceof JViewport) {
                Container gp = p.getParent();
                if (gp instanceof JScrollPane) {
                    scrollPane = (JScrollPane)gp;
                }
            }            
        }
        return scrollPane;
    }
    
    void clear() {
        setCurrentService(null);
    }
    
    void setCurrentService(ServiceData service) {
        if (currentData != null) {
            currentData.setPackageName(header.getPackageName());
        }
        currentData = service;
        if (currentData != null) {
            getScrollPane().setColumnHeaderView(header);
            header.setPackageName(currentData.getPackageName());
            setTitle(currentData.getServiceName());
            setDetails();
        } else {
            setTitle("");
            header.setPackageName("");
            setText("");
        }
    }
    
    ServiceData getCurrentService() {
        return currentData;
    }
    
    String getPackageName() {
        currentData.setPackageName(header.getPackageName());
        return currentData.getPackageName();
    }
    
    public void setTitle(String value) {
        if (value != null) {                            
            title.setText("<html><h3>"+value+"</h3></html>");//NOI18N
        } else {
            title.setText("");
        }
    }

    private void setDetails() {
        StringBuilder details = new StringBuilder();
        if (currentData != null) {
            String wsdl = currentData.getUrl();
            String info = currentData.getInfoPage();
            String link = currentData.getPurchaseLink();
            
            details.append("<b>Version: </b>"+currentData.getVersion()+"<br>");
            details.append("<b>Provider: </b>"+currentData.getProviderName()+"<br>");
            details.append("<h3>Description: </h3>"+currentData.getDescription()+"<br>");
            details.append("<br>");
            details.append("<b>WSDL Location: </b><br><a href=\""+wsdl+"\">"+wsdl+"</a><br>");
            details.append("<br>");
            details.append("<b>Info Page: </b><br><a href=\""+info+"\">"+info+"</a><br>");
            details.append("<br>");
            details.append("<b>Purchase Link: </b><br><a href=\""+link+"\">"+link+"</a><br>");
        }
        setText(details.toString());
        setCaretPosition(0);
    }

    public static void showURL (URL href) {
        HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
        assert displayer != null : "HtmlBrowser.URLDisplayer found.";
        if (displayer != null) {
            displayer.showURL (href);
        } else {
            Logger.global.log (Level.INFO, "No URLDisplayer found.");
        }
    }
    
}
