/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.editor.designer;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.modules.iep.editor.model.ModelConstants;
import org.netbeans.modules.tbls.model.ImageUtil;

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


