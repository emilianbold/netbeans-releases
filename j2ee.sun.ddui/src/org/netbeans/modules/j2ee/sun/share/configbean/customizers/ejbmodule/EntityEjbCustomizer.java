/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
/*
 * EntityEjbCustomizer.java        October 23, 2003, 2:06 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;


import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.EntityEjb;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class EntityEjbCustomizer extends EjbCustomizer {

    private EntityEjb theEntityBean;
    
    private EntityEjbPanel entityEjbPanel;
    private BeanPoolPanel beanPoolPanel;
    private BeanCachePanel beanCachePanel;


    /** Creates a new instance of EntityEjbCustomizer */
	public EntityEjbCustomizer() {
	}
    
    public EntityEjb getEntityBean() {
        return theEntityBean;
    }
	
    // Get the bean specific panel
    protected javax.swing.JPanel getBeanPanel() {
        entityEjbPanel = new EntityEjbPanel(this);
        return entityEjbPanel;
    }

    // Initialize all the elements in the bean specific panel
    protected void initializeBeanPanel(BaseEjb theBean) {
        entityEjbPanel.initFields(theEntityBean);
    };

    protected void addTabbedBeanPanels() {
        beanPoolPanel = new BeanPoolPanel(this);
        beanPoolPanel.getAccessibleContext().setAccessibleName(bundle.getString("BeanPool_Acsbl_Name"));             //NOI18N
        beanPoolPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("BeanPool_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.insertTab(bundle.getString("LBL_BeanPool"), null, beanPoolPanel, null, 0); // NOI18N

        beanCachePanel = new BeanCachePanel(this);
        beanCachePanel.getAccessibleContext().setAccessibleName(bundle.getString("BeanCache_Acsbl_Name"));             //NOI18N
        beanCachePanel.getAccessibleContext().setAccessibleDescription(bundle.getString("BeanCache_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.addTab(bundle.getString("LBL_BeanCache"),          // NOI18N
            beanCachePanel);

        // Select Bean Pool Panel
        tabbedPanel.setSelectedIndex(tabbedPanel.indexOfTab(bundle.getString("LBL_BeanPool")));  //NOI18N
    }

    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        beanPoolPanel.initFields(theBean.getBeanPool());
        beanCachePanel.initFields(theBean.getBeanCache());
    }

    public String getHelpId() {
        return "AS_CFG_EntityEjb";                                      //NOI18N
    }

    protected boolean setBean(Object bean) {
		boolean result = super.setBean(bean);
		
		if(bean instanceof EntityEjb) {
            theEntityBean = (EntityEjb) bean;
			result = true;
		} else {
			// if bean is not a EntityEjb, then it shouldn't have passed BaseEjb either.
			assert (result == false) : 
				"EntityEjbCustomizer was passed wrong bean type in setBean(Object bean)";	// NOI18N
				
            theEntityBean = null;
			result = false;
		}
		
		return result;
    }       
}
