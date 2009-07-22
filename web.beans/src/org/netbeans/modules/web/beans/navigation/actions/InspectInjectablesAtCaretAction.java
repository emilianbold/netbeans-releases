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
package org.netbeans.modules.web.beans.navigation.actions;

import java.awt.event.ActionEvent;

import javax.swing.text.JTextComponent;

import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ext.ExtKit;
import org.openide.util.NbBundle;



/**
 * @author ads
 *
 */
public final class InspectInjectablesAtCaretAction extends BaseAction {

    private static final long serialVersionUID = 1857528107859448216L;
    
    private static final String INSPECT_INJACTABLES_AT_CARET =
        "LBL_InspectInjactablesAtCaret";                     // NOI18N
    
    private static final String INSPECT_INJACTABLES_AT_CARET_POPUP =
        "LBL_PopupInspectInjactablesAtCaret";                // NOI18N

    public InspectInjectablesAtCaretAction() {
        super(NbBundle.getMessage(InspectInjectablesAtCaretAction.class, 
                INSPECT_INJACTABLES_AT_CARET), 0);
        
        putValue(SHORT_DESCRIPTION, getValue(NAME));
        putValue(ExtKit.TRIMMED_TEXT,getValue(NAME));
        putValue(POPUP_MENU_TEXT, NbBundle.getMessage(
                InspectInjectablesAtCaretAction.class,
                INSPECT_INJACTABLES_AT_CARET_POPUP));

        putValue("noIconInMenu", Boolean.TRUE); // NOI18N*/
    }


    /* (non-Javadoc)
     * @see org.netbeans.editor.BaseAction#actionPerformed(java.awt.event.ActionEvent, javax.swing.text.JTextComponent)
     */
    @Override
    public void actionPerformed( ActionEvent event, JTextComponent component ) {
        
    }
    
    /* (non-Javadoc)
     * @see javax.swing.AbstractAction#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

}
