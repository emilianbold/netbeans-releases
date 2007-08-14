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

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Image;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Ajit
 */
public interface TabWidget {
    
    /**
     * Return the title to be displayed in this tab.
     * @return title the title to be displayed in this tab.
     */ 
    String getTitle();
    
    /**
     * Return the image icon to be displayed in this tab.
     * @return image the image icon to be displayed in this tab.
     */ 
    Image getIcon();
    
    /**
     * Return the widget to be displayed in this tab.
     * @return widget the widget to be displayed in this tab.
     */ 
    Widget getComponentWidget();
    
    /**
     * Returns the object that can be used as a hashtable key.
     * @return  hashtable key.
     */
    Object hashKey();
}
