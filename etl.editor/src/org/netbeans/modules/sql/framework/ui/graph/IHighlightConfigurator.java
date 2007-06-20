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
package org.netbeans.modules.sql.framework.ui.graph;

import java.awt.Color;

import com.nwoods.jgo.JGoBrush;

/**
 * Interface to hold configuration information for highlighting canvas objects.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public interface IHighlightConfigurator {
    public static final Color DEFAULT_BASIC_COLOR = new Color(214, 235, 255); // light
                                                                                // gray

    public static final Color DEFAULT_EDIT_COLOR = Color.WHITE;

    public static final Color DEFAULT_HOVER_COLOR = new Color(254, 254, 244); // light
                                                                                // beige

    public void setHoverBrush(JGoBrush newBrush);

    public JGoBrush getHoverBrush();

    public void setNormalBrush(JGoBrush newBrush);

    public JGoBrush getNormalBrush();
}
