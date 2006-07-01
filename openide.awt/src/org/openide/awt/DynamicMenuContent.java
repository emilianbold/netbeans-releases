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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.awt;

import javax.swing.JComponent;

/**
 * Dynamic result of a {@link org.openide.util.actions.Presenter.Menu} or {@link org.openide.util.actions.Presenter.Popup}. If the presenters return
 * an instance of <code>DynamicMenuContent</code>, then the framework code
 * will use it's methods to populate the menu and keep it uptodate.
 * @author mkleint
 * @since org.openide.awt 6.5
 */
public interface DynamicMenuContent {
    /**
     * Create main menu/popup menuitems. Null values will be later replaced by JSeparators.
     * This method is called for popups and for menus. It's called each time a popup menu is contructed and just 
     * once for the main menu. Main menu updates happen through the <code>synchMenuPresenters()</code> method.
     * If you want different behaviour for menu and popup,
     * use a different implementation returned by {@link org.openide.util.actions.Presenter.Popup} and {@link org.openide.util.actions.Presenter.Menu}.
     */
    public JComponent[] getMenuPresenters();
    
    /**
     * update main menu presenters. This method is called only by the main menu processing.
     * @param items the previously used menuitems returned by previous call to <code>getMenuPresenters()</code> or <code>synchMenuPresenters()</code>
     * @return a new set of items to show in menu. Can be either an updated old set of instances or a completely new one.
     */
    public JComponent[] synchMenuPresenters(JComponent[] items);
}
