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

package org.netbeans.modules.mercurial;

import org.openide.util.NbBundle;
import org.openide.awt.DynamicMenuContent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.mercurial.ui.annotate.AnnotateAction;
import org.netbeans.modules.mercurial.ui.log.IncomingAction;
import org.netbeans.modules.mercurial.ui.log.OutAction;
import org.netbeans.modules.mercurial.ui.view.ViewAction;
import org.netbeans.modules.versioning.spi.VCSContext;

/**
 * Container menu for branch actions.
 *
 * @author Maros Sandor
 */
public class ShowMenu extends AbstractAction implements DynamicMenuContent {
    private VCSContext ctx;
    private boolean bShowAnnotationMenu;
    private boolean bShowAnnotation;

    public ShowMenu(VCSContext ctx, boolean bShowAnnotationMenu,boolean bShowAnnotation) {
        super(NbBundle.getMessage(ShowMenu.class, "CTL_MenuItem_ShowMenu"));
        this.ctx = ctx;
        this.bShowAnnotationMenu = bShowAnnotationMenu;
        this.bShowAnnotation = bShowAnnotation;
    }

    public JComponent[] getMenuPresenters() {
        return new JComponent [] { createMenu() };
    }

    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return new JComponent [] { createMenu() };
    }

    public boolean isEnabled() {
        return true;
    }

    public void actionPerformed(ActionEvent ev) {
        // no operation
    }

    private JMenu createMenu() {
        JMenu menu = new JMenu(this);
        if(bShowAnnotationMenu){
            if (!bShowAnnotation) {
                menu.add(new AnnotateAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_ShowAnnotations"), ctx)); // NOI18N
            } else {
                menu.add(new AnnotateAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_HideAnnotations"), ctx)); // NOI18N
            }
        }
        menu.add(new IncomingAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_ShowIncoming"), ctx)); // NOI18N
        menu.add(new OutAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_ShowOut"), ctx)); // NOI18N
        menu.add(new ViewAction(NbBundle.getMessage(MercurialAnnotator.class, "CTL_PopupMenuItem_View"), ctx)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(menu, NbBundle.getMessage(ShowMenu.class, "CTL_MenuItem_ShowMenu"));
        return menu;
    }
}
