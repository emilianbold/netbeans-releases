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

import java.awt.*;
import java.beans.*;
import java.util.HashMap;
import javax.swing.event.*;

import org.netbeans.editor.Coloring;
//import org.netbeans.modules.editor.options.ColoringBean;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * ColoringArrayEditor is editor for Editors colorings settings, operates over
 * java.util.HashMap as it needs null key. Null key is used for transferring kitClass,
 * default coloring is named Settings.DEFAULT, other colorings are mapped by their names.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */

public class ColoringArrayEditor extends PropertyEditorSupport {

    private ColoringArrayEditorPanel editor;
    
    private static final String HELP_ID = "editing.fontsandcolors"; // !!! NOI18N

    public boolean supportsCustomEditor() {
        return true;
    }

    public String getAsText() {
        return NbBundle.getMessage( ColoringArrayEditor.class, "PROP_Coloring" ); // NOI18N
    }

    public java.awt.Component getCustomEditor() {
        if( editor == null ) {
            editor = new ColoringArrayEditorPanel();
            HelpCtx.setHelpIDString( editor, getHelpCtx().getHelpID() );
            refreshEditor();
            editor.addPropertyChangeListener( new PropertyChangeListener() {
                                                  public void propertyChange( PropertyChangeEvent evt ) {
                                                      if( "value".equals( evt.getPropertyName() ) ) setValue( editor.getValue() ); // NOI18N
                                                  }
                                              });
        }
        return editor;
    }

    protected HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }
    
    public void setAsText( String s ) {
        return;
    }

    public void setValue( Object obj ) {
        Object oldValue = getValue();
        if( (obj != null) && (! obj.equals( oldValue ) ) ) {
            super.setValue( obj );
            if( ( editor != null ) && (! editor.getValue().equals( getValue() ) ) ) {
                refreshEditor();
            }
        }
    }

    private void refreshEditor() {
        if( editor != null ) {
            editor.setValue( (HashMap)getValue() );
        }
    }
}
