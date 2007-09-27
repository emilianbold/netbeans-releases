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

package org.netbeans.modules.iep.editor.designer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentType;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelManager;
import org.netbeans.modules.iep.editor.model.ModelConstants;
import org.netbeans.modules.iep.editor.tcg.util.ImageUtil;

/**
 *  DOCUMENT ME!
 */
public interface GuiConstants extends ModelConstants {

    public final static Border ETCHED_BORDER =  BorderFactory.createEtchedBorder();

    public final static Color LISTBOX_SELECTED_BACKGROUND = UIManager.getColor("textHighlight");

    public final static ImageIcon CUT_ICON = ImageUtil.getImageIcon("x16.cut.gif");

    public final static ImageIcon COPY_ICON = ImageUtil.getImageIcon("copy.gif");

    public final static ImageIcon PASTE_ICON = ImageUtil.getImageIcon("paste.gif");

    public final static ImageIcon DELETE_ICON = ImageUtil.getImageIcon("delete.gif");

    public final static ImageIcon UNDO_ICON = ImageUtil.getImageIcon("x16.undo.gif");

    public final static ImageIcon REDO_ICON = ImageUtil.getImageIcon("x16.redo.gif");

    public final static ImageIcon ORTHOGONAL_LINK_ICON = ImageUtil.getImageIcon("orthoLink.gif");
    
    public final static ImageIcon AUTO_LAYOUT_ICON = ImageUtil.getImageIcon("autoLayout.gif");
    
    public final static ImageIcon OVERVIEW_ICON = ImageUtil.getImageIcon("overview.gif");

    public final static ImageIcon VALIDATE_ICON = ImageUtil.getImageIcon("validation.png");

    public final static ImageIcon SCHEMA_ICON = ImageUtil.getImageIcon("schema.gif");
    
    public final static ImageIcon ERROR_ICON = ImageUtil.getImageIcon("error.gif");
    
    public final static String CUT_NAME = "Cut";
    public final static String COPY_NAME = "Copy";
    public final static String PASTE_NAME = "Paste";
    public final static String UNDO_NAME = "Undo";
    public final static String REDO_NAME = "Redo";
    public final static String DELETE_NAME = "Delete";
    public final static String ORTHOGONAL_LINK_NAME = "OrthogonalLink";
    public final static String AUTO_LAYOUT_NAME = "AutoLayout";
    public final static String OVERVIEW_NAME = "Overview";
    public final static String VALIDATE_NAME = "Validate";
    
}


