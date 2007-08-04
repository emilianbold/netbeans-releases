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
/*
 * CmpEntityEjbCustomizer.java        October 26, 2003, 9:23 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.awt.GridBagConstraints;

/*import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.modules.persistence.mapping.core.util.MappingContext;
import com.sun.jdo.modules.persistence.mapping.core.util.MappingContextFactory;
import com.sun.jdo.modules.persistence.mapping.ejb.ui.panels.BeanMappingPanel;*/
import javax.swing.event.TableModelEvent;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.Cmp;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.OneOneFinders;

import org.netbeans.modules.j2ee.sun.share.configbean.Base;
import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.CmpEntityEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.ValidationError;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class CmpEntityEjbCustomizer extends EntityEjbCustomizer {

    private CmpEntityEjb theCmpEntityBean;
    
    private OneOneFinderPanel finderPanel;
//    private FlushAtEndOfMethodPanel flushAtEndOfMethodPanel;
    private PrefetchDisabledPanel prefetchDisabledPanel;
    private CmpEntityEjbPanel cmpEntityEjbPanel;
    private CmpPanel cmpPanel;
//    private BeanMappingPanel mappingPanel;

    private OneOneFinderModel finderModel;
    
    
    /** Creates a new instance of CmpEntityEjbCustomizer */
    public CmpEntityEjbCustomizer() {
    }
    
    public CmpEntityEjb getCmpEntityBean() {
        return theCmpEntityBean;
    }

    // Get the bean specific panel
    protected javax.swing.JPanel getBeanPanel() {
        cmpPanel = new CmpPanel();

        EntityEjbPanel entityEjbPanel = (EntityEjbPanel)super.getBeanPanel();
        if(entityEjbPanel != null) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            cmpPanel.add(entityEjbPanel, gridBagConstraints);
        }

        cmpEntityEjbPanel = new CmpEntityEjbPanel(this);
        if(cmpEntityEjbPanel != null) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            cmpPanel.add(cmpEntityEjbPanel, gridBagConstraints);
        }

        return cmpPanel;
    }


    // Initialize all the elements in the bean specific panel
    protected void initializeBeanPanel(BaseEjb theBean) {
        super.initializeBeanPanel(theBean);

        cmpEntityEjbPanel.initFields(theCmpEntityBean);
    }


    protected void addTabbedBeanPanels() {
        super.addTabbedBeanPanels();
        
//        MappingContext dummyContext = MappingContextFactory.getMappingContext(Model.RUNTIME);

        finderModel = new OneOneFinderModel();
        finderPanel = new OneOneFinderPanel(finderModel);
        finderPanel.putClientProperty(PARTITION_KEY, ValidationError.PARTITION_EJB_FINDER);
        finderPanel.getAccessibleContext().setAccessibleName(bundle.getString("One_One_Finders_Acsbl_Name"));             //NOI18N
        finderPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("One_One_Finders_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.addTab(bundle.getString("LBL_One_One_Finders"),    // NOI18N
            finderPanel);

//        flushAtEndOfMethodPanel = new FlushAtEndOfMethodPanel(theBean, this);
//        tabbedPanel.addTab(bundle.getString("LBL_Flush_At_End_Of_Method"),    // NOI18N
//            flushAtEndOfMethodPanel);

        prefetchDisabledPanel = new PrefetchDisabledPanel(theCmpEntityBean, this);
        prefetchDisabledPanel.putClientProperty(PARTITION_KEY, ValidationError.PARTITION_EJB_PREFETCH);
        prefetchDisabledPanel.getAccessibleContext().setAccessibleName(bundle.getString("Prefetch_Disabled_Acsbl_Name"));             //NOI18N
        prefetchDisabledPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("Prefetch_Disabled_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.addTab(bundle.getString("LBL_Prefetch_Disabled"),        // NOI18N
            prefetchDisabledPanel);

  /*      mappingPanel =  new BeanMappingPanel(dummyContext);
        mappingPanel.putClientProperty(PARTITION_KEY, ValidationError.PARTITION_EJB_CMPMAPPING);
        mappingPanel.getAccessibleContext().setAccessibleName(bundle.getString("Cmp_Mapping_Acsbl_Name"));             //NOI18N
        mappingPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("Cmp_Mapping_Acsbl_Desc"));      //NOI18N  
        mappingPanel.setApplyChangesImmediately(true);
        tabbedPanel.add(mappingPanel, bundle.getString("LBL_Cmp_Mapping"), 0);   // NOI18N

        // Select Cmp Mapping Panel
        tabbedPanel.setSelectedIndex(0);*/
    }

    protected void addListeners() {
        super.addListeners();
        
        finderModel.addTableModelListener(this);
//        flushAtEndOfMethodPanel.addTableModelListener(this);
        prefetchDisabledPanel.addTableModelListener(this);
    }
    
    protected void removeListeners() {
        super.removeListeners();

        finderModel.removeTableModelListener(this);
//        flushAtEndOfMethodPanel.removeTableModelListener(this);
        prefetchDisabledPanel.removeTableModelListener(this);
    } 
    
    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        super.initializeTabbedBeanPanels(theBean);

        Finder[] finders = null;
        Cmp cmp = theCmpEntityBean.getCmp();
        if(cmp != null) {
            OneOneFinders oneOneFinders = cmp.getOneOneFinders();
            if(oneOneFinders != null) {
                finders = oneOneFinders.getFinder();
            }
        }
        finderPanel.setModel(theCmpEntityBean, finders);
 
//        flushAtEndOfMethodPanel.setData(theCmpEntityBean);

        prefetchDisabledPanel.setData(theCmpEntityBean);

        if(theBean != null) {
            Base myParent = theBean.getParent();
            EjbJarRoot jarBean;

            if (myParent instanceof EjbJarRoot) {
                String beanName = null;

                jarBean = (EjbJarRoot) myParent;
                beanName = theBean.getEjbName();
/*                mappingPanel.setMappingContext(jarBean.getMappingContext(), jarBean.getEJBInfoHelper());

                // if no corresponding MCE object, this must be a new
                // bean, have jarBean create the skeleton
                jarBean.ensureCmpMappingExists(beanName);

                mappingPanel.showMappingForBean(beanName);*/
            }
        }
// TODO - clear the mapping panel if theBean is null?
    }

    public String getHelpId() {
        return "AS_CFG_CmpEntityEjb"; // NOI18N
    }
    
    protected boolean setBean(Object bean) {
		boolean result = super.setBean(bean);
		
		if(bean instanceof CmpEntityEjb) {
            theCmpEntityBean = (CmpEntityEjb) bean;
			result = true;
		} else {
			// if bean is not a CmpEntityEjb, then it shouldn't have passed BaseEjb either.
			assert (result == false) : 
				"CmpEntityEjbCustomizer was passed wrong bean type in setBean(Object bean)";	// NOI18N
				
            theCmpEntityBean = null;
			result = false;
		}
		
		return result;
    } 
    
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        
        CmpEntityEjb bean = getCmpEntityBean();
        if(bean != null) {
            Object eventSource = e.getSource();
            
            // TODO send event on what row actually changed.
            if(eventSource == finderModel) {
                bean.firePropertyChange("oneOneFinder", null, new Object());
//                validateField(CmpEntityEjb.FIELD_???);
            } else if(eventSource == prefetchDisabledPanel.getModel()) {
                bean.firePropertyChange("prefetchDisabled", null, new Object());
//                validateField(CmpEntityEjb.FIELD_???);
            }
        }
    }    
}
