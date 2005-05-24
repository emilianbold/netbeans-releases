/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CmpEntityEjbCustomizer.java        October 26, 2003, 9:23 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.Collection;

//DEPLOYMENT API
import javax.enterprise.deploy.spi.DConfigBean;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.Cmp;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.FlushAtEndOfMethod;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.OneOneFinders;
import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.CmpEntityEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.modules.persistence.mapping.core.util.MappingContext;
import com.sun.jdo.modules.persistence.mapping.core.util.MappingContextFactory;
import com.sun.jdo.modules.persistence.mapping.ejb.ui.panels.BeanMappingPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.Base;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class CmpEntityEjbCustomizer extends EntityEjbCustomizer{

    private CmpEntityEjb theBean;
    private OneOneFinderPanel finderPanel;
    private FlushAtEndOfMethodPanel flushAtEndOfMethodPanel;
    private PrefetchDisabledPanel prefetchDisabledPanel;
    private CmpEntityEjbPanel cmpEntityEjbPanel;
    private CmpPanel cmpPanel;
    private BeanMappingPanel mappingPanel;

    /** Creates a new instance of CmpEntityEjbCustomizer */
    public CmpEntityEjbCustomizer() {
    }


    /** Creates a new instance of CmpEntityEjbCustomizer */
    public CmpEntityEjbCustomizer(DConfigBean theBean) {
        super(theBean);
        if(!(theBean instanceof CmpEntityEjb)){
            assert(false);
        }
        this.theBean = (CmpEntityEjb)theBean;
    }


    public void setObject(Object bean) {
        super.setObject(bean);
        // Only do this if the bean is actually changing.
        if(theBean != bean) {
            if(bean instanceof CmpEntityEjb) {
                theBean = (CmpEntityEjb) bean;
           }
        }
    }


    //get the bean specific panel
    protected javax.swing.JPanel getBeanPanel(){
        cmpPanel = new CmpPanel();
        java.awt.GridBagConstraints gridBagConstraints = 
           new java.awt.GridBagConstraints();

        EntityEjbPanel entityEjbPanel = (EntityEjbPanel)super.getBeanPanel();
        if(entityEjbPanel != null){
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
            cmpPanel.add(entityEjbPanel, gridBagConstraints);
        }

        cmpEntityEjbPanel = new CmpEntityEjbPanel(this);
        if(cmpEntityEjbPanel != null){
            gridBagConstraints.gridy = 1;
            cmpPanel.add(cmpEntityEjbPanel, gridBagConstraints);
        }

        return cmpPanel;
    }


    //initialize all the elements in the bean specific panel
    protected void initializeBeanPanel(BaseEjb theBean){
        super.initializeBeanPanel(theBean);
        if(!(theBean instanceof CmpEntityEjb)){
            assert(false);
        }

        CmpEntityEjb cmpEntityEjb = (CmpEntityEjb)theBean;
        Cmp cmp = cmpEntityEjb.getCmp();
        
        if(cmp != null){
            String mappingProperties =
                cmp.getMappingProperties();
            if(mappingProperties != null){
                cmpEntityEjbPanel.setMappingProperties(mappingProperties);
            }
        }
    }


    protected void addTabbedBeanPanels() {
        super.addTabbedBeanPanels();
        MappingContext dummyContext = 
            MappingContextFactory.getMappingContext(Model.RUNTIME);
        OneOneFinderModel finderModel = 
            new OneOneFinderModel();
        finderModel.addTableModelListener(this);
        finderPanel = new OneOneFinderPanel(finderModel);
        finderPanel.getAccessibleContext().setAccessibleName(bundle.getString("One_One_Finders_Acsbl_Name"));             //NOI18N
        finderPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("One_One_Finders_Acsbl_Desc"));      //NOI18N  
        tabbedPanel.addTab(bundle.getString("LBL_One_One_Finders"),    // NOI18N
            finderPanel);



        flushAtEndOfMethodPanel = new FlushAtEndOfMethodPanel(theBean, this);
        flushAtEndOfMethodPanel.addTableModelListener(this);
        tabbedPanel.addTab(bundle.getString("LBL_Flush_At_End_Of_Method"),    // NOI18N
            flushAtEndOfMethodPanel);


        prefetchDisabledPanel = new PrefetchDisabledPanel(theBean, this);
        prefetchDisabledPanel.getAccessibleContext().setAccessibleName(bundle.getString("Prefetch_Disabled_Acsbl_Name"));             //NOI18N
        prefetchDisabledPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("Prefetch_Disabled_Acsbl_Desc"));      //NOI18N  
        prefetchDisabledPanel.addTableModelListener(this);
        tabbedPanel.addTab(bundle.getString("LBL_Prefetch_Disabled"),        // NOI18N
            prefetchDisabledPanel);

        mappingPanel =  new BeanMappingPanel(dummyContext);
        mappingPanel.getAccessibleContext().setAccessibleName(bundle.getString("Cmp_Mapping_Acsbl_Name"));             //NOI18N
        mappingPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("Cmp_Mapping_Acsbl_Desc"));      //NOI18N  
        mappingPanel.setApplyChangesImmediately(true);
        tabbedPanel.add(mappingPanel, bundle.getString("LBL_Cmp_Mapping"), 0);   // NOI18N

        //Select Cmp Mapping Panel
        tabbedPanel.setSelectedIndex(0);
    }


    protected void initializeTabbedBeanPanels(BaseEjb theBean) {
        super.initializeTabbedBeanPanels(theBean);
        if(!(theBean instanceof CmpEntityEjb)){
            assert(false);
        }


        CmpEntityEjb cmpEntityEjb = (CmpEntityEjb)theBean;
        Cmp cmp = cmpEntityEjb.getCmp();
        if(cmp == null){
            finderPanel.setModel(cmpEntityEjb,null);
        }else{
            OneOneFinders oneOneFinders = cmp.getOneOneFinders();
            if(oneOneFinders == null){
                finderPanel.setModel(cmpEntityEjb,null);
            }else{
                Finder[] finder = oneOneFinders.getFinder();
                finderPanel.setModel(cmpEntityEjb, finder);
            }
        }
 
        flushAtEndOfMethodPanel.setData(cmpEntityEjb);

        prefetchDisabledPanel.setData(cmpEntityEjb);

        if (theBean != null) {
            Base myParent = theBean.getParent();
            EjbJarRoot jarBean;

            if (myParent instanceof EjbJarRoot) {
                String beanName = null;

                jarBean = (EjbJarRoot)myParent;
                beanName = theBean.getEjbName();
                mappingPanel.setMappingContext(
                    jarBean.getMappingContext(), jarBean.getEJBInfoHelper());

                // if no corresponding MCE object, this must be a new
                // bean, have jarBean create the skeleton
                jarBean.ensureCmpMappingExists(beanName);

                mappingPanel.showMappingForBean(beanName);
            }
        }
// TODO - clear the mapping panel if theBean is null?
    }


    public Collection getErrors(){
        ArrayList errors = null;
        if(validationSupport == null) assert(false);
        errors = (ArrayList)super.getErrors();

        //Cmp Entity Ejb field Validations
        String property = cmpEntityEjbPanel.getMappingProperties();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/cmp/mapping-properties", //NOI18N
                bundle.getString("LBL_Mapping_Properties")));           //NOI18N

        return errors;
    }


    public String getHelpId() {
	return "AS_CFG_CmpEntityEjb";                                   //NOI18N
    }

    //Cmp Entity Ejb(Mapping Properties) update methods
    void updateMappingProperties(String mappingProperties){
        Cmp cmp = getCmp();
        if((EMPTY_STRING.equals(mappingProperties)) || (null == mappingProperties)){
            cmp.setMappingProperties(null);
            updateCmp();
        }else{
            cmp.setMappingProperties(mappingProperties);
        }
        notifyChange();
    }


    private Cmp getCmp(){
        Cmp cmp = theBean.getCmp();
        if(cmp == null){
            cmp = StorageBeanFactory.getDefault().createCmp();
            try {
                theBean.setCmp(cmp);
            }catch(java.beans.PropertyVetoException exception){
            }
        }
        return cmp;
    }


    private void updateCmp(){
        Cmp cmp = getCmp();
        if(cmp.getMappingProperties() != null) return;
        if (cmp.getOneOneFinders() != null) return;

        try{
            theBean.setCmp(null);
        }catch(java.beans.PropertyVetoException exception){
        }
    }
}
