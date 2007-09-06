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

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pavel Benes
 */
public abstract class AbstractSVGAction extends AbstractAction implements Presenter.Popup {
    protected static final String ICON_PATH_PREFIX = "org/netbeans/modules/mobility/svgcore/resources/"; //NOI18N
    protected static final String LBL_ID_PREFIX    = "LBL_"; //NOI18N
    protected static final String ICON_ID_PREFIX   = "ICON_"; //NOI18N
    protected static final String HINT_ID_PREFIX   = "HINT_"; //NOI18N
    protected static final String KEY_ID_PREFIX    = "KEY_"; //NOI18N

    protected final String m_name;
    protected final String m_label;
    protected final String m_hint;
    protected final ImageIcon m_icon;
    protected final int    m_toolbarPos;
        
    public AbstractSVGAction(String name) {
        this(name, true);
    }

    public AbstractSVGAction(String name, boolean isEnabled) {
        this(name, isEnabled, 100);
    }
    
    public AbstractSVGAction(String name, boolean isEnabled, int toolBarPos) {
        m_name  = name;
        m_label = getMessage(LBL_ID_PREFIX + name);
        
        ImageIcon icon = null;
        
        try {
            String iconPath = ICON_PATH_PREFIX + getMessage( ICON_ID_PREFIX + name);
            Image img = Utilities.loadImage(iconPath);
            assert img != null : "Icon not found: " + iconPath;
            icon = new ImageIcon(img);
        } catch( MissingResourceException e) {}
        
        setIcon( m_icon = icon);
        
        String hint;
        try {
            hint = getMessage(HINT_ID_PREFIX + name);
        } catch( MissingResourceException e) {
            hint = m_label;
        }
        m_hint = hint;
        
        try {
            String    key    = getMessage( KEY_ID_PREFIX + name);
            KeyStroke stroke = KeyStroke.getKeyStroke(key);
            if (stroke != null) {
                putValue(Action.ACCELERATOR_KEY, stroke);
            } else {
                System.err.println("Invalid key stroke: " + name);
            }
        } catch( MissingResourceException e) {
        }
        
        setDescription(hint);
        setEnabled(isEnabled);
        m_toolbarPos = toolBarPos;
    }

    protected final void setIcon(ImageIcon icon) {
        if (icon != null) {
            putValue(Action.SMALL_ICON, icon);
        }
    }
    
    protected final void setDescription(String hint) {
        KeyStroke stroke = (KeyStroke) getValue(Action.ACCELERATOR_KEY);
        if (stroke != null) {
            hint += " (";
            String str = KeyEvent.getKeyModifiersText(stroke.getModifiers());
            if (str.length() > 0) {
                hint += str + "+";
            }
            hint += KeyEvent.getKeyText(stroke.getKeyCode()) + ")"; //NOI18N
        }
        putValue(Action.SHORT_DESCRIPTION, hint);
    }
    
    /*
    public AbstractSVGAction(String iconName, String hintResId, String lblResId) {
        this( iconName, hintResId, lblResId, true);
    }

    public AbstractSVGAction(String iconName, String hintResId, String lblResId, boolean enabled) {
        this(iconName, hintResId, lblResId, enabled, 100);
        putValue(Action.SMALL_ICON, new ImageIcon(Utilities.loadImage( ICON_PATH_PREFIX + iconName)));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SVGViewTopComponent.class, hintResId));
        setEnabled(enabled);
    }

    public AbstractSVGAction(String iconName, String hintResId, String lblResId, boolean enabled, int position) {
        m_name = iconName;
        m_label = getMessage(lblResId);
        putValue(Action.SMALL_ICON, new ImageIcon(Utilities.loadImage( ICON_PATH_PREFIX + iconName)));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SVGViewTopComponent.class, hintResId));
        setEnabled(enabled);
        m_toolbarPos = position;
        m_menuPos = 100;
    }
    */
    
    public String getActionID() {
        return m_name;
    }
    
    public JMenuItem getPopupPresenter() {
        JMenuItem menu = new JMenuItem(this);
        menu.setText(getLabel());
        menu.setToolTipText(null);
        menu.setIcon(null);
        return menu;
    }    
    
    public int getPositionInToolbar() {
        return m_toolbarPos;
    }    
    
    public void registerAction( TopComponent tc) {
        KeyStroke ks = (KeyStroke) getValue(Action.ACCELERATOR_KEY);
        if (ks != null) {
            InputMap inputMap = tc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            inputMap.put( ks, m_name);        
            tc.getActionMap().put(m_name, this);
        }
    }

    protected String getLabel() {
        return m_label;
    }
    
    protected static String getMessage(String msgId) {
        return NbBundle.getMessage(SVGViewTopComponent.class, msgId);       
    }
}
