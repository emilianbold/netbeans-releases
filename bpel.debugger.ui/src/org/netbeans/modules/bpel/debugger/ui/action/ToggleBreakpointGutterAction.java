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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.debugger.ui.action;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

/**
 * Mostly copied from HintsUI. This action should show the active 
 * annotation's description if there is an annotation, or toggle breakpoint if 
 * there is no.
 * 
 * @author Kirill Sorokin
 */
public class ToggleBreakpointGutterAction extends SystemAction implements AWTEventListener, KeyListener {

    private JTextComponent textComponent;
    private JLabel tooltip;
    private Popup popup;
    
    private PopupFactory pf = null;
    
    public ToggleBreakpointGutterAction() {
    }

    @Override
    public String getName() {
        return "bpel-toggle-breakpoint-gutter-action"; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    public void actionPerformed(ActionEvent event) {
        if (!invokeDefaultAction((JTextComponent) event.getSource())) {
            DebuggerManager.getDebuggerManager().getActionsManager().doAction(
                    ActionsManager.ACTION_TOGGLE_BREAKPOINT);
        }
    }

    private boolean invokeDefaultAction(final JTextComponent component) {
        final Document document = component.getDocument();

        if (document instanceof BaseDocument) {
            final int caret = component.getCaretPosition();
            final Annotations annotations = ((BaseDocument) document).getAnnotations();

            final boolean[] returnValue = new boolean[]{true};
            final String[] stringValue = new String[1];
            final Point[] pointValue = new Point[1];

            document.render(new Runnable() {

                public void run() {
                    try {
                        final Rectangle caretRectangle =
                                component.modelToView(caret);
                        final int currentLineOffset =
                                Utilities.getLineOffset((BaseDocument) document, caret);
                        final int currentRowOffset =
                                Utilities.getRowStartFromLineOffset((BaseDocument) document, currentLineOffset);
                        final AnnotationDesc description =
                                annotations.getActiveAnnotation(currentLineOffset);

                        if ((description == null) ||
                                description.getAnnotationType().equals("BpelBreakpoint_broken") ||
                                description.getAnnotationType().equals("BpelBreakpoint_disabled") ||
                                description.getAnnotationType().equals("BpelBreakpoint_normal")) {
                            returnValue[0] = false;
                        } else {
                            final Point point = component.modelToView(currentRowOffset).getLocation();

                            point.y += caretRectangle.height;
                            if (component.getParent() instanceof JViewport) {
                                point.x += ((JViewport) component.getParent()).getViewPosition().x;
                            }

                            stringValue[0] = description.getShortDescription();
                            pointValue[0] = point;
                        }
                    } catch (BadLocationException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            });
            
            if (returnValue[0]) {
                showPopup(stringValue[0], component, pointValue[0]);
                return true;
            }
        }

        return false;
    }

    private void setComponent(JTextComponent comp) {
        boolean change = this.textComponent != comp;
        if (change) {
            unregister();
            this.textComponent = comp;
            register();
        }
    }

    private void register() {
        if (textComponent == null) {
            return;
        }
        textComponent.addKeyListener(this);
    }

    private void unregister() {
        if (textComponent == null) {
            return;
        }
        textComponent.removeKeyListener(this);
    }

    private void showPopup(String description, JTextComponent component, Point point) {
        if (component == null) {
            return;
        }
        
        setComponent(component);
        
        SwingUtilities.convertPointToScreen(point, component);
        
        ToolTipManager.sharedInstance().setEnabled(false);
        ToolTipManager.sharedInstance().setEnabled(true);
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);

        tooltip = new JLabel("<html>" + translate(description)); // NOI18N
        tooltip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(0, 3, 0, 3)));

        popup = getPopupFactory().getPopup(
                component, tooltip, point.x, point.y);

        popup.show();
    }

    private void removePopup() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);

        if (popup != null) {
            popup.hide();

            tooltip = null;
            popup = null;
        }
    }

    private PopupFactory getPopupFactory() {
        if (pf == null) {
            pf = PopupFactory.getSharedInstance();
        }
        return pf;
    }

    private String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replaceAll(c[cntr], tags[cntr]);
        }

        return input;
    }

    public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent mv = (MouseEvent) event;
            if (mv.getID() == MouseEvent.MOUSE_CLICKED && mv.getClickCount() > 0) {
                removePopup();
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (textComponent == null) {
            return;
        }

        boolean errorTooltipShowing =
                tooltip != null && tooltip.isShowing();

        if (errorTooltipShowing) {
            // any key should disable the tooltip:
            removePopup();
            return;
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
    
    private static String[] c = new String[]{"&", "<", ">", "\n", "\""}; // NOI18N
    private static String[] tags = new String[]{"&amp;", "&lt;", "&gt;", "<br>", "&quot;"}; // NOI18N
}
