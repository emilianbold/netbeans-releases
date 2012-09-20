/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.logging.Level;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author tomas
 */
public final class HyperlinkSupport {
    
    private static HyperlinkSupport instance = new HyperlinkSupport();

    final static String STACKTRACE_ATTRIBUTE = "attribute.stacktrace.link";     // NOI18N
    final static String TYPE_ATTRIBUTE = "attribute.type.link";                 // NOI18N
    final static String URL_ATTRIBUTE = "attribute.url.link";                   // NOI18N
    public final static String LINK_ATTRIBUTE = "attribute.simple.link";        // NOI18N
    private final MotionListener motionListener;
    private final java.awt.event.MouseListener mouseListener;
    private RequestProcessor rp = new RequestProcessor("Bugtracking hyperlinks", 50); // NOI18N
    
    private HyperlinkSupport() { 
        motionListener = new MotionListener();
        mouseListener = new MouseListener();
    }
    
    public static HyperlinkSupport getInstance() {
        return instance;
    }
    
    public void registerForStacktraces(final JTextPane pane) {
        pane.removeMouseMotionListener(motionListener);
        rp.post(new Runnable() {
            @Override
            public void run() {
                StackTraceSupport.register(pane);
                pane.addMouseMotionListener(motionListener);
            }
        });    
    }
    
    public void registerForTypes(final JTextPane pane) {
        pane.removeMouseMotionListener(motionListener);
        rp.post(new Runnable() {
            @Override
            public void run() {
                FindTypesSupport.getInstance().register(pane);
                pane.addMouseMotionListener(motionListener);
            }
        });
    }
    
    public void registerForURLs(final JTextPane pane) {
        pane.removeMouseMotionListener(motionListener);
        rp.post(new Runnable() {
            @Override
            public void run() {
                WebUrlHyperlinkSupport.register(pane);
                pane.addMouseMotionListener(motionListener);
            }
        });    
    }
    
    public void registerLink(final JTextPane pane, final int pos[], final Link link) {
        pane.removeMouseMotionListener(motionListener);
        rp.post(new Runnable() {
            @Override
            public void run() {
                registerLinkIntern(pane, pos, link);
                pane.addMouseMotionListener(motionListener);
            }
        });    
    }
    
    public void registerForIssueLinks(final JTextPane pane, final Link issueLink, final IssueFinder issueFinder) {
        pane.removeMouseMotionListener(motionListener);
        rp.post(new Runnable() {
            @Override
            public void run() {
                String text = "";
                try {
                    text = pane.getStyledDocument().getText(0, pane.getStyledDocument().getLength());
                } catch (BadLocationException ex) {
                    BugtrackingManager.LOG.log(Level.INFO, null, ex);
                }
                registerLinkIntern(pane, issueFinder.getIssueSpans(text), issueLink);
                pane.addMouseMotionListener(motionListener);
            }
        });    
    }

    private void registerLinkIntern(final JTextPane pane, final int[] pos, final Link link) {
        final StyledDocument doc = pane.getStyledDocument();
                
        if (pos.length > 0) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
                    final Style hlStyle = doc.addStyle("regularBlue-link", defStyle); // NOI18N
                    hlStyle.addAttribute(LINK_ATTRIBUTE, link);
                    StyleConstants.setForeground(hlStyle, Color.BLUE);
                    StyleConstants.setUnderline(hlStyle, true);

                    for (int i=0; i<pos.length; i+=2) {
                        int off = pos[i];
                        int length = pos[i+1]-pos[i];
                        doc.setCharacterAttributes(off, length, hlStyle, true);
                    }
                    pane.addMouseListener(mouseListener);
                }
            });
        }
    }
    
    private class MouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    JTextPane pane = (JTextPane)e.getSource();
                    StyledDocument doc = pane.getStyledDocument();
                    Element elem = doc.getCharacterElement(pane.viewToModel(e.getPoint()));
                    AttributeSet as = elem.getAttributes();
                    Link link = (Link)as.getAttribute(LINK_ATTRIBUTE);
                    if (link != null) {
                        link.onClick(elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset()));
                    }
                }
            } catch(Exception ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private class MotionListener extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            JTextPane pane = (JTextPane)e.getSource();
            StyledDocument doc = pane.getStyledDocument();
            Element elem = doc.getCharacterElement(pane.viewToModel(e.getPoint()));
            AttributeSet as = elem.getAttributes();
            if (StyleConstants.isUnderline(as)) {
                pane.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                pane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    };
    
    public interface Link {
        public void onClick(String linkText);
    }
    
}
