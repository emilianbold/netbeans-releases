/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.progress.module;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.core.StatusLineElementProvider;
import org.openide.util.Utilities;

/**
 *
 * @author  Milos Kleint
 */
public final class ProgressVisualizerProvider implements StatusLineElementProvider {

    public Component getStatusLineElement () {
        return Controller.getDefault().getVisualComponent();
    }
    
}
