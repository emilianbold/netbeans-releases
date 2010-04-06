/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.spi;

import java.awt.event.ActionListener;
import javax.swing.JPanel;
import org.openide.util.HelpCtx;

/**
 * Provides description of the editor component.
 * 
 * @author skini
 */
public interface ExtensibilityElementConfigurationEditorComponent {
    public static final String PROPERTY_ERROR_EVT = "PROPERTY_ERROR_EVT";
    public static final String PROPERTY_WARNING_EVT = "PROPERTY_WARNING_EVT";
    public static final String PROPERTY_CLEAR_MESSAGES_EVT = "PROPERTY_CLEAR_MESSAGES_EVT";
    public static final String PROPERTY_NORMAL_MESSAGE_EVT = "PROPERTY_NORMAL_MESSAGE_EVT";
    
    public static String BC_TO_BP_DIRECTION = "BC_TO_BP"; // NOI18N
    public static String BP_TO_BC_DIRECTION = "BP_TO_BC"; // NOI18N
    public static String BI_DIRECTION = "BI_DIRECTION"; // NOI18N
    public static String UNKNOWN_DIRECTION = "UNKNOWN_DIRECTION"; //NOI18N
    
    /**
     * Return a editor panel. In general, its better to cache this in implementation, 
     * till commit/rollback is called.
     * So that user provided values will be saved in the panel.
     * @return 
     */
    public JPanel getEditorPanel();
    
    /**
     * Return title for the dialog.
     * @return String title
     */
    public String getTitle();

    /**
     * Return the helpctx to be shown in dialog/wizards.
     * @return HelpCtx.
     */
    public HelpCtx getHelpCtx();
    
    /**
     * Generally is not needed to be used. Implement if you have special cases.
     * listener on OK/Cancel buttons.
     * @return ActionListener
     */
    public ActionListener getActionListener();
    
    
    /**
     * Commit all values from the panel and commit it to the wsdl model.
     * Return true, if successfully committed, otherwise false.
     * @return boolean
     */
    public boolean commit();
    
    /**
     * Cleanup panel, discard values.
     * 
     * @return boolean
     */
    public boolean rollback();
    
    
    /**
     * Do validation, and return true if valid, otherwise false.
     * @return boolean
     */
    public boolean isValid();
}
