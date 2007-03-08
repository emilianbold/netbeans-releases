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

package org.openide.awt;

import javax.swing.JTabbedPane;

/**
 * Factory class for TabbedPanes with closeable tabs.
 * 
 * @author S. Aubrecht
 * @since 6.10
 */
public class TabbedPaneFactory {
    
    /**
     * Name of the property that is fired from the closeable tabbed pane
     * when the user clicks close button on a tab.
     */
    public static final String PROP_CLOSE = CloseButtonTabbedPane.PROP_CLOSE;
    
    /** Creates a new instance of TabbedPaneFactory */
    private TabbedPaneFactory() {
    }
    
    /**
     * Creates a special {@link JTabbedPane} that displays a small 'close' button in each tab.
     * When user clicks the close button a {@link java.beans.PropertyChangeEvent} is fired from the
     * tabbed pane. The property name is {@link #PROP_CLOSE} and the property
     * value is the inner component inside the clicked tab.
     * 
     * @return Special TabbedPane with closeable tabs.
     */
    public static JTabbedPane createCloseButtonTabbedPane() {
        return new CloseButtonTabbedPane();
    }
}
