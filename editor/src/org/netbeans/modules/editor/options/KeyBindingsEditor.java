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

package org.netbeans.modules.editor.options;

import java.beans.*;
import java.util.*;
import javax.swing.event.*;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * KeyBindingsEditor is editor for MultiKeyBindings settings of Editor,
 * operates over java.util.List, where single MultiKeyBindings are stored.
 * First item in List (with index 0) is used for transferring kitClass.
 *
 * @author  Petr Nejedly
 * @deprecated Without any replacement.
 */

public class KeyBindingsEditor extends PropertyEditorSupport {

    private KeyBindingsEditorPanel editorPanel;

    private static final String HELP_ID = "editing.keybindings"; // !!! NOI18N

    protected HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }

    /**
     * Tell the world that we have nice editor Component
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Create custom editor tightly coupled with this editor
     */
    public java.awt.Component getCustomEditor() {
        if( editorPanel == null ) {
            editorPanel = new KeyBindingsEditorPanel( this );
            HelpCtx.setHelpIDString( editorPanel, getHelpCtx().getHelpID() );
        }
        refreshEditor();
        return editorPanel;
    }

    private void refreshEditor() {
        if( editorPanel != null ) {
            editorPanel.setValue( (List)getValue() );
        }
    }

    /**
     *  Sets the value for editor / customEditor
     */
    public void setValue( Object obj ) {
        Object oldValue = getValue();
        if( (obj != null) && (! obj.equals( oldValue ) ) ) {
            super.setValue( obj );
            if( ( editorPanel != null ) && (! editorPanel.getValue().equals( getValue() ) ) ) {
                refreshEditor();
            }
        }
    }


    /**
     * The way our customEditor notifies us when user changes something.
     */
    protected void customEditorChange() {
        // forward it to parent, which will fire propertyChange
        super.setValue( editorPanel.getValue() );
    }

    /**
     * Return the label to be shown in the PropertySheet
     */
    public String getAsText() {
        //    return "KeyBindings";
        return NbBundle.getBundle( KeyBindingsEditor.class ).getString( "PROP_KeyBindings" ); // NOI18N
    }

    /**
     * Don't bother if the user tried to edit our label in the PropertySheet
     */
    public void setAsText( String s ) {
    }

}
