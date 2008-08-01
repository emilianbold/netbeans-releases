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

package org.netbeans.modules.soa.mappercore;

import java.awt.Color;
import java.awt.Font;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.MapperModel;

/**
 *
 * @author anjeleevich
 */
public interface MapperContext {
    public String getRightDysplayText(MapperModel model, Object value);
    public Color getRightForeground(MapperModel model, Object value);
    public Icon getRightIcon(MapperModel model, Object value, Icon defaultIcon);
    public Font getRightFont(MapperModel model, Object value, Font defaultFont);
    public JPopupMenu getRightPopupMenu(MapperModel model, Object value);
    public String getRightToolTipText(MapperModel mode, Object value);

    public String getLeftDysplayText(MapperModel model, Object value);
    public Color getLeftForeground(MapperModel model, Object value, Color defaultColor);
    public Icon getLeftIcon(MapperModel model, Object value, Icon defaultIcon);
    public Font getLeftFont(MapperModel model, Object value, Font defaultFont);
    public JPopupMenu getLeftPopupMenu(MapperModel model, Object value);
    public String getLeftToolTipText(MapperModel mode, Object value);
    
    public JPopupMenu getCanvasPopupMenu(MapperModel mode, GraphItem item);
    public List<JMenu> getMenuNewEllements(MapperModel mode);
}
