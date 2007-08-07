/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * AbstractSVGAction.java
 * Created on May 30, 2007, 4:43 PM
 */

package org.netbeans.modules.mobility.svgcore.view.svg;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Pavel Benes
 */
public abstract class AbstractSVGAction extends AbstractAction implements Presenter.Popup {
    private static final String ICON_PATH_PREFIX = "org/netbeans/modules/mobility/svgcore/resources/"; //NOI18N
    private final String lblResId;
    
    public AbstractSVGAction(String iconName, String hintResId, String lblResId) {
        this( iconName, hintResId, lblResId, true);
    }

    public AbstractSVGAction(String iconName, String hintResId, String lblResId, boolean enabled) {
        this.lblResId = lblResId;
        putValue(Action.SMALL_ICON, new ImageIcon(Utilities.loadImage( ICON_PATH_PREFIX + iconName)));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SVGViewTopComponent.class, hintResId));
        setEnabled(enabled);
    }
    
    public JMenuItem getPopupPresenter() {
        JMenuItem menu = new JMenuItem(this);
        menu.setText(NbBundle.getMessage(SVGViewTopComponent.class, lblResId)); //NOI18N
        menu.setToolTipText(null);
        menu.setIcon(null);
        return menu;
    }    
}
