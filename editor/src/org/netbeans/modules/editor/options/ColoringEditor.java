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

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;

import org.netbeans.editor.Coloring;
import org.openide.util.HelpCtx;

/**
 * Coloring Editor for editor settings. Operates over one ColoringBean
 *
 * @author Miloslav Metelka
 * @author Petr Nejedly
 */
public class ColoringEditor extends PropertyEditorSupport {

    /** Editor for font and color components. */
    private ColoringEditorPanel editor;

    /** Construct new instance */
    public ColoringEditor() {
    }
    
    private static final String HELP_ID = "editing.fontsandcolors"; // !!! NOI18N
    
    protected HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }

    /** Get value as text is not supported */
    public String getAsText() {
        return null;
    }

    /** Set value as text is not supported */
    public void setAsText(String text) {
        throw new IllegalArgumentException();
    }

    /** Set the new value into property editor */
    public void setValue(Object value) {
        super.setValue( value );
        if (editor != null) {
            editor.setValue( (ColoringBean)getValue() );
        }
    }

    /** It supports custom editor */
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Get custom editor */
    public Component getCustomEditor() {
        if (editor == null) {

            // If we don't have any, create one
            editor = new ColoringEditorPanel();

            // fill it with our current value
            editor.setValue( (ColoringBean)getValue() );

            // register listener, which will propagate editor changes to our interval value with firing
            editor.addPropertyChangeListener(new PropertyChangeListener() {
                                                 public void propertyChange(PropertyChangeEvent evt) {
                                                     if (evt.getPropertyName() == "value") // NOI18N
                                                         superSetValue( editor.getValue()); // skip updating editor
                                                 }
                                             });
            HelpCtx.setHelpIDString( editor, getHelpCtx().getHelpID() );
        }

        return editor;
    }


    /** when we don't need to update editor, use this */
    void superSetValue( Object value ) {
        super.setValue( value );
    }

    /** This editor is paintable */
    public boolean isPaintable() {
        return true;
    }

    /** Paint the current value */
    public void paintValue(Graphics g, Rectangle box) {
        Coloring c = getAppliedColoring();
        if (c != null) {
            // clear background
            g.setColor(c.getBackColor());
            g.fillRect(box.x, box.y, box.width - 1, box.height - 1);

            // draw example text
            g.setColor(c.getForeColor());
            g.setFont(c.getFont());
            String text = ((ColoringBean)getValue()).example;
            FontMetrics fm = g.getFontMetrics();
            int x = Math.max((box.width - fm.stringWidth(text)) / 2, 0);
            int y = Math.max((box.height - fm.getHeight()) / 2 + fm.getAscent(), 0);
            g.drawString(text, x, y);
        }
    }

    private Coloring getAppliedColoring() {
        ColoringBean value = ((ColoringBean)getValue());
        if( value == null ) return null;
        Coloring dc = value.defaultColoring;
        Coloring c = value.coloring;
        Coloring ret = null;
        if (dc != null && c != null) {
            ret = c.apply(dc);
        }
        return ret;
    }

}
