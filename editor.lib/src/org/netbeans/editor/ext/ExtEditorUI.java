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

package org.netbeans.editor.ext;

import java.awt.event.ActionEvent;
import javax.swing.JPopupMenu;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.PopupManager;


/**
* Editor UI for the component. All the additional UI features
* like advanced scrolling, info about fonts, abbreviations,
* keyword matching are based on this class.
*
* @author Miloslav Metelka
* @version 1.00
*/
public class ExtEditorUI extends EditorUI {

    private ToolTipSupport toolTipSupport;

    private JPopupMenu popupMenu;

    private Completion completion;
    
    private PopupManager popupManager;

    private CompletionJavaDoc completionJavaDoc;
    
    private boolean noCompletion; // no completion available

    private boolean noCompletionJavaDoc; // no completion available
    

    
    public ExtEditorUI() {

        getToolTipSupport();
        getCompletion();
        getCompletionJavaDoc();
    }

    public ToolTipSupport getToolTipSupport() {
        if (toolTipSupport == null) {
            toolTipSupport = new ToolTipSupport(this);
        }
        return toolTipSupport;
    }

    public Completion getCompletion() {
        
        if (completion == null) {
            if (noCompletion) {
                return null;
            }

            synchronized (getComponentLock()) {
                JTextComponent component = getComponent();
                if (component != null) {
                    BaseKit kit = Utilities.getKit(component);
                    if (kit != null && kit instanceof ExtKit) {
                        completion = ((ExtKit)kit).createCompletion(this);
                        if (completion == null) {
                            noCompletion = true;
                        }
                    }
                }
            }
        }

        return completion;
    }

    
    public CompletionJavaDoc getCompletionJavaDoc() {
        if (completionJavaDoc == null) {
            if (noCompletionJavaDoc) {
                return null;
            }

            synchronized (getComponentLock()) {
                JTextComponent component = getComponent();
                if (component != null) {
                    BaseKit kit = Utilities.getKit(component);
                    if (kit != null && kit instanceof ExtKit) {
                        completionJavaDoc = ((ExtKit)kit).createCompletionJavaDoc(this);
                        if (completionJavaDoc == null) {
                            noCompletionJavaDoc = true;
                        }
                    }
                }
            }
        }

        return completionJavaDoc;
    }
    
    
    public PopupManager getPopupManager() {
        if (popupManager == null) {

            synchronized (getComponentLock()) {
                JTextComponent component = getComponent();
                if (component != null) {
                    popupManager = new PopupManager(component);
                }
            }
        }

        return popupManager;
    }
    

    
    public void showPopupMenu(int x, int y) {
        // First call the build-popup-menu action to possibly rebuild the popup menu
        JTextComponent component = getComponent();
        JPopupMenu pm = getPopupMenu();
        if (component != null && (pm == null || !pm.isVisible())) {
            BaseKit kit = Utilities.getKit(component);
            if (kit != null) {
                Action a = kit.getActionByName(ExtKit.buildPopupMenuAction);
                if (a != null) {
                    a.actionPerformed(new ActionEvent(component, 0, "")); // NOI18N
                }
            }

            pm = getPopupMenu(); // refresh after building of the popup menu
            if (pm != null) {
                if (component.isShowing()) { // fix of #18808
                    pm.show(component, x, y);
                }
            }
        }
    }

    public void hidePopupMenu() {
        JPopupMenu pm = getPopupMenu();
        if (pm != null) {
            pm.setVisible(false);
            setPopupMenu(null);
        }
    }

    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    public void setPopupMenu(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }

}
