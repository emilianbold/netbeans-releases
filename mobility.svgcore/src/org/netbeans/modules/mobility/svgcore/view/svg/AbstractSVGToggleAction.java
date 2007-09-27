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

import java.awt.event.ActionEvent;
import java.util.MissingResourceException;
import javax.swing.ImageIcon;

/**
 *
 * @author Pavel Benes
 */
public abstract class AbstractSVGToggleAction extends AbstractSVGAction {
    private static final long serialVersionUID  = 5862679852552354L;    
    private static final String RES_NAME_SUFFIX = "1_"; //NOI18N
    
    public static final String  SELECTION_STATE = "selected"; //NOI18N
    
    protected final String    m_label1;
    protected final String    m_hint1;
    protected final ImageIcon m_icon1;
    protected       boolean   m_isSelected;

    public AbstractSVGToggleAction(String name) {
        this(name, true);
    }
    
    public AbstractSVGToggleAction(String name, boolean enabled) {
        super(name, enabled);
        m_isSelected = true;
        
        String label1;
        
        try {
            label1 = getMessage(LBL_ID_PREFIX + RES_NAME_SUFFIX + name); 
        } catch( MissingResourceException e) {
            label1 = m_label;
        }
        m_label1 = label1;
        
        String hint;
        try {
            hint = getMessage(HINT_ID_PREFIX + RES_NAME_SUFFIX + name);
        } catch( MissingResourceException e) {
            hint = m_label1;
        }
        m_hint1 = hint;
        m_icon1 = getIcon(ICON_ID_PREFIX + RES_NAME_SUFFIX + name);
    }
    
    protected String getLabel() {
        return m_isSelected ? m_label1 : m_label;
    }
    
    public void actionPerformed(ActionEvent e) {
        setIsSelected( !m_isSelected);
    }
    
    public final void setIsSelected(boolean isSelected) {
        m_isSelected = isSelected;
        setDescription(m_isSelected ? m_hint1 : m_hint);
        setIcon(m_isSelected ? m_icon1 : m_icon);
        putValue( SELECTION_STATE, isSelected);
    }
    
    public final boolean isSelected() {
        return m_isSelected;
    }
}
