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

package org.netbeans.modules.visualweb.project.jsf.actions;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;


/**
 * Action for setting the start (initial) page.
 * Formerly there were two actions put into project/jsfloader module, which was not a proper place.
 *
 * @author Peter Zavadsky (refactored previous actions)
 * @author Mark Dey (originally action for jsp)
 * @author David Botterill (originally action for portlet)
 * @author Po-Ting Wu
 */
public class SetStartPageAction extends AbstractAction implements Presenter.Menu, Presenter.Popup, ContextAwareAction {

    private static final int TYPE_NONE    = 0;
    private static final int TYPE_JSP     = 1;
    private static final int TYPE_PORTLET = 2;

    private final int type;
    private final FileObject fo;

    /** Creates a new instance of SetStartPageAction */
    public SetStartPageAction() {
        this(TYPE_NONE, null); // Fake action -> The context aware is real one, drawback of the NB design?
    }

    private SetStartPageAction(int type, FileObject fo) {
        super((type == TYPE_JSP) ? NbBundle.getMessage(SetStartPageAction.class, "LBL_SetAsStartPage") : null);

        this.type = type;
        this.fo = fo;
    }

    public void actionPerformed(ActionEvent evt) {
        if (type == TYPE_JSP) {
            String newStartPage = JsfProjectUtils.setStartPage(fo);
            String msg = newStartPage != null ?
                NbBundle.getMessage(SetStartPageAction.class, "MSG_StartPageChanged") + " " + newStartPage :
                NbBundle.getMessage(SetStartPageAction.class, "MSG_NoStartPage");

            StatusDisplayer.getDefault().setStatusText(msg);
        }
    }
    
    public Action createContextAwareInstance(Lookup context) {
        DataObject dob = (DataObject)context.lookup(DataObject.class);
        
        if (dob == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("SetStartPageAction: missing DataObject instance in the context, context=" + context)); // NOI18N
            return null;
        }
        
        int type = TYPE_JSP;
        FileObject fo = dob.getPrimaryFile();
        // check for jsp extension
        if ((fo != null) && "jsp".equalsIgnoreCase(fo.getExt())) { // NOI18N
            // check if this is a portlet project
            Project thisProj = FileOwnerQuery.getOwner(fo);
            if (JsfProjectUtils.getPortletSupport(thisProj) != null) {
                type = TYPE_PORTLET;
            }
        }

        return new SetStartPageAction(type, fo);
    }
    
    public JMenuItem getMenuPresenter() {
        return getPresenter();
    }

    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }

    public JMenuItem getPresenter() {
        JMenuItem mainItem = new JMenuItem();

        if (type == TYPE_JSP) {
            String name = NbBundle.getMessage(SetStartPageAction.class, "LBL_SetAsStartPage");
            mainItem.setEnabled(!(JsfProjectUtils.isStartPage(fo) || fo.getExt().equalsIgnoreCase("jspf")));
            Mnemonics.setLocalizedText(mainItem, name);
            mainItem.addActionListener(this);
        } else {
            mainItem.setVisible(false);
        }

        return mainItem;
    }
}
