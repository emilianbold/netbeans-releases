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

/**
 *
 * @author Pavel Benes
 */
public abstract class AbstractSVGToggleAction extends AbstractSVGAction {
    protected final String  m_label1;
    protected final String  m_hint1;
    protected       boolean m_on;
    
    public AbstractSVGToggleAction(String name) {
        super(name);
        m_on = true;
        m_label1 = getMessage(LBL_ID_PREFIX + "1_" + name); //NOI18N
        
        String hint;
        try {
            hint = getMessage(HINT_ID_PREFIX + "1_" + name); //NOI18N
        } catch( MissingResourceException e) {
            hint = m_label1;
        }
        m_hint1 = hint;
    }
    
    protected String getLabel() {
        return (m_on || m_label1 == null) ? m_label : m_label1;
    }
    
    public void actionPerformed(ActionEvent e) {
        m_on = !m_on;
        setDescription(m_on ? m_hint : m_hint1);
    }
}
