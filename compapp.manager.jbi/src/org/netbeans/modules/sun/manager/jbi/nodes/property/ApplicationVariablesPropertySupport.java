/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.sun.manager.jbi.nodes.property;

import com.sun.esb.management.api.configuration.ConfigurationService;
import com.sun.esb.management.common.ManagementRemoteException;
import java.beans.PropertyEditor;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import org.netbeans.modules.sun.manager.jbi.GenericConstants;
import org.netbeans.modules.sun.manager.jbi.editors.ApplicationVariablesEditor;
import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.netbeans.modules.sun.manager.jbi.nodes.JBIComponentNode;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationDescriptor;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationMBeanAttributeInfo;

/**
 * Property support for Application Variables.
 * 
 * @author jqian
 */
class ApplicationVariablesPropertySupport extends AbstractTabularPropertySupport {
    
    private static final String APPLICATION_VARIABLE_NAME = "name"; // NOI18N
    private static final String APPLICATION_VARIABLE_TYPE = "type"; // NOI18N
            
    ApplicationVariablesPropertySupport(
            PropertySheetOwner propertySheetOwner,
            Attribute attr, 
            MBeanAttributeInfo info) {
        super(propertySheetOwner, attr, info, 
                new String[] {APPLICATION_VARIABLE_NAME, APPLICATION_VARIABLE_TYPE});
    }
    
    @Override
    public PropertyEditor getPropertyEditor() {
        JBIComponentConfigurationDescriptor descriptor = 
                (info instanceof JBIComponentConfigurationMBeanAttributeInfo) ? 
                ((JBIComponentConfigurationMBeanAttributeInfo)info).getConfigurationDescriptor() : null;
        
        return new ApplicationVariablesEditor(true, getTabularType(), 
                descriptor, info.isWritable());
    }
    
    protected TabularData getTabularData() throws ManagementRemoteException {
        AppserverJBIMgmtController controller =
                componentNode.getAppserverJBIMgmtController();
        ConfigurationService configService =
                controller.getConfigurationService();
        String compName = componentNode.getName();
        return configService.getApplicationVariablesAsTabularData(
                compName, AppserverJBIMgmtController.SERVER_TARGET);
    }
     
    protected String deleteCompositeData(CompositeData cd) 
        throws ManagementRemoteException {
        return ((JBIComponentNode)componentNode).deleteApplicationVariable(
                (String) cd.get(APPLICATION_VARIABLE_NAME));
    }

    protected String addCompositeData(CompositeData cd) 
            throws ManagementRemoteException {
        return ((JBIComponentNode)componentNode).addApplicationVariable(
                (String) cd.get(APPLICATION_VARIABLE_NAME), cd);
    }

    protected String setCompositeData(CompositeData cd) 
            throws ManagementRemoteException {
        return ((JBIComponentNode)componentNode).setApplicationVariable(
                (String) cd.get(APPLICATION_VARIABLE_NAME), cd);
    }    
    
    protected String getAddCompositeDataOperationName() {
        return GenericConstants.ADD_APPLICATION_VARIABLE_NAME;
    }
    
    protected String getDeleteCompositeDataOperationName() {
        return GenericConstants.DELETE_APPLICATION_VARIABLE_NAME;
    }
    
    protected String getSetCompositeDataOperationName() {
        return GenericConstants.SET_APPLICATION_VARIABLE_NAME;
    }
}
