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
package org.netbeans.modules.visualweb.text;

import javax.swing.UIManager;

import javax.swing.plaf.ComponentUI;

import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;

/**
 * Text editor user interface
 *
 * @author  Timothy Prinzing
 * @version 1.31 01/23/03
 */
public abstract class DesignerPaneBaseUI extends ComponentUI {
//    /**
//     * Converts the given location in the model to a place in
//     * the view coordinate system.
//     *
//     * @param pos  the local location in the model to translate >= 0
//     * @return the coordinates as a rectangle
//     * @exception BadLocationException  if the given position does not
//     *   represent a valid location in the associated document
//     */
//    public abstract Rectangle modelToView(/*DesignerPaneBase t,*/ Position pos);
//
//    /**
//     * Converts the given place in the view coordinate system
//     * to the nearest representative location in the model.
//     *
//     * @param pt  the location in the view to translate.  This
//     *   should be in the same coordinate system as the mouse
//     *   events.
//     * @return the offset from the start of the document >= 0
//     */
//    public abstract Position viewToModel(DesignerPaneBase t, Point pt);

    /**
     * Provides a way to determine the next visually represented model
     * location that one might place a caret.  Some views may not be visible,
     * they might not be in the same order found in the model, or they just
     * might not allow access to some of the locations in the model.
     *
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @param direction the direction from the current position that can
     *  be thought of as the arrow keys typically found on a keyboard.
     *  This may be SwingConstants.WEST, SwingConstants.EAST,
     *  SwingConstants.NORTH, or SwingConstants.SOUTH.
     * @return the location within the model that best represents the next
     *  location visual position.
     * @exception BadLocationException
     * @exception IllegalArgumentException for an invalid direction
     */
//    public abstract Position getNextVisualPositionFrom(DesignerPaneBase t, Position pos,
//        int direction);
    public abstract DomPosition getNextVisualPositionFrom(DesignerPaneBase t, DomPosition pos, int direction);
    
    /**
     * Fetches the name used as a key to lookup properties through the
     * UIManager.  This is used as a prefix to all the standard
     * text properties.
     *
     * @return the name ("EditorPane")
     */
    protected abstract String getPropertyPrefix();
    
    /**
     * Creates the object to use for a caret.  By default an
     * instance of BasicCaret is created.  This method
     * can be redefined to provide something else that implements
     * the InputPosition interface or a subclass of JCaret.
     *
     * @return the caret object
     */
    DesignerCaret createCaret() {
        DesignerCaret caret = new DesignerCaret();
        
        String prefix = getPropertyPrefix();
        Object o = UIManager.get(prefix + ".caretBlinkRate"); // NOI18N
        
        if ((o != null) && (o instanceof Integer)) {
            Integer rate = (Integer)o;
            caret.setBlinkRate(rate.intValue());
        }
        
        return caret;
    }
    
}
