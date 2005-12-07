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
package org.netbeans.modules.collab.ui;

import javax.swing.*;

import org.openide.explorer.view.BeanTreeView;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class SessionsTreeView extends BeanTreeView {
    /**
     *
     *
     */
    public SessionsTreeView() {
        super();
        getJTree().setDoubleBuffered(true);
    }
    
    /**
     * Upgrade to public access
     *
     */
    public JTree getJTree() {
        return tree;
    }
}
