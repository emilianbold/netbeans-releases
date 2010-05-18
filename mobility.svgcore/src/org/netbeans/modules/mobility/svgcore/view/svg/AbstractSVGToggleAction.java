/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
