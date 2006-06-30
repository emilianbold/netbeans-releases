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
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.ArrayList;
import java.util.Collection;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.openide.ErrorManager;



/**
 *
 * @author  vkraemer
 */
public class EntityEjb extends BaseEjb {
    
    /** Holds value of property isReadOnlyBean. */
    private String isReadOnlyBean;
    
    /** Holds value of property refreshPeriodInSeconds. */
    private String refreshPeriodInSeconds;
    
    /** Holds value of property commitOption. */
    private String commitOption;

    
    /** Creates a new instance of SunONEEntityDConfigBean */
    public EntityEjb() {
    }
	
    protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);
		addVetoableChangeListener(new VetoRefreshPeriodChange());
		addPropertyChangeListener(new KeepRefreshPeriodValid());
    }	

    /* ------------------------------------------------------------------------
     * XPath to Factory mapping support
     */
/*
	private HashMap entityEjbFactoryMap;

    protected java.util.Map getXPathToFactoryMap() {
		if(entityEjbFactoryMap == null) {
			entityEjbFactoryMap = (HashMap) super.getXPathToFactoryMap();

			// add child DCB's specific to Entity Beans
		}

		return entityEjbFactoryMap;
    }
 */

    /* ------------------------------------------------------------------------
     * Persistence support.  Loads DConfigBeans from previously saved Deployment
     * plan file.
     */
    protected class EntityEjbSnippet extends BaseEjb.BaseEjbSnippet {
        public CommonDDBean getDDSnippet() {
            Ejb ejb = (Ejb) super.getDDSnippet();

            if(isReadOnlyBean != null){
                ejb.setIsReadOnlyBean(isReadOnlyBean);
            }

            if(refreshPeriodInSeconds != null){
                ejb.setRefreshPeriodInSeconds(refreshPeriodInSeconds);
            }

            if(commitOption != null){
                ejb.setCommitOption(commitOption);
            }

            return ejb;
        }

        public boolean hasDDSnippet() {
            if(super.hasDDSnippet()){
                return true;
            }

            if(isReadOnlyBean != null){
                return true;            }

            if(refreshPeriodInSeconds != null){
                return true;
            }

            if(commitOption != null){
                return true;
            }
            return false;
        }
    }


    java.util.Collection getSnippets() {
        Collection snippets = new ArrayList();
        snippets.add(new EntityEjbSnippet());
        return snippets;
    }


    protected void loadEjbProperties(Ejb savedEjb) {
        super.loadEjbProperties(savedEjb);

        isReadOnlyBean = savedEjb.getIsReadOnlyBean();
        refreshPeriodInSeconds = savedEjb.getRefreshPeriodInSeconds();
        commitOption = savedEjb.getCommitOption();
    }
    
    protected void clearProperties() {
        super.clearProperties();
        
        isReadOnlyBean = null;
        refreshPeriodInSeconds = null;
        commitOption = null;
    }
    
    public void fireXpathEvent(XpathEvent xpathEvent) {
        super.fireXpathEvent(xpathEvent);
//		dumpNotification("EntityEjb.fireXpathEvent", xpathEvent);
        
        DDBean eventBean = xpathEvent.getBean();
        String xpath = eventBean.getXpath();

        if("/ejb-jar/enterprise-beans/entity/remote".equals(xpath)) { // NOI18N
            try {
                if(xpathEvent.isAddEvent()) {
                    setJndiName(getDefaultJndiName());
                } else if(xpathEvent.isRemoveEvent()) {
                    setJndiName(null);
                }
            } catch(PropertyVetoException ex) {
                // should never happen.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    
    /** Getter for property isReadOnlyBean.
     * @return Value of property isReadOnlyBean.
     *
     */
    public String getIsReadOnlyBean() {
        return this.isReadOnlyBean;
    }


    class VetoRefreshPeriodChange implements VetoableChangeListener {
        
        /** This method gets called when a constrained property is changed.
         *
         * @param     evt a <code>PropertyChangeEvent</code> object describing the
         *   	      event source and the property that has changed.
         * @exception PropertyVetoException if the recipient wishes the property
         *              change to be rolled back.
         *
         */
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            boolean acceptable = true;
            String failureMessage = "bad things happen";
            try {
                String propName = evt.getPropertyName();
                if (propName.indexOf("refreshPeriodInSeconds") > -1) {
                    if (null == isReadOnlyBean || isReadOnlyBean.equals("false"))   //NOI18N
                        if (evt.getNewValue() != null) {
                            //PropertyVetoException pve = new PropertyVetoException();
                            //throw new PropertyVetoException("property only valid for read-only beans", evt);
                            failureMessage = "property only valid for read-only beans";
                        }
                }
            }
            catch (Exception ex) {
                acceptable = false;
            }
            if (!acceptable) {
                throw  new PropertyVetoException(failureMessage,evt);
            }
        }
        
    }


    public class KeepRefreshPeriodValid implements java.beans.PropertyChangeListener {
        /** This method gets called when a bound property is changed.
         * @param evt A PropertyChangeEvent object describing the event source
         *   	and the property that has changed.
         *
         */
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                String propName = evt.getPropertyName();
                if (propName.indexOf("isReadOnlyBean") > -1) {
                    Object oldValue = evt.getOldValue();
                    Object newValue = evt.getNewValue();
                    if (newValue.equals(Boolean.TRUE)) {
                        setRefreshPeriodInSeconds("60");
                    }
                    if (newValue.equals(Boolean.FALSE)) {
                        setRefreshPeriodInSeconds(null);
                    }
                }
            }
            catch (Throwable t) {
            }
        }
    }
    
    
    /** Setter for property isReadOnlyBean.
     * @param isReadOnlyBean New value of property isReadOnlyBean.
     *
     * @throws PropertyVetoException
     *
     */
    public void setIsReadOnlyBean(String isReadOnlyBean) throws java.beans.PropertyVetoException {
        String oldIsReadOnlyBean = this.isReadOnlyBean;
        getVCS().fireVetoableChange("isReadOnlyBean", oldIsReadOnlyBean, isReadOnlyBean);
        this.isReadOnlyBean = isReadOnlyBean;
        getPCS().firePropertyChange("isReadOnlyBean", oldIsReadOnlyBean, isReadOnlyBean);
    }
    
    /** Getter for property refreshPeriodInSeconds.
     * @return Value of property refreshPeriodInSeconds.
     *
     */
    public String getRefreshPeriodInSeconds() {
        return this.refreshPeriodInSeconds;
    }
    
    /** Setter for property refreshPeriodInSeconds.
     * @param refreshPeriodInSeconds New value of property refreshPeriodInSeconds.
     *
     * @throws PropertyVetoException
     *
     */
    public void setRefreshPeriodInSeconds(String refreshPeriodInSeconds) throws java.beans.PropertyVetoException {
        String oldRefreshPeriodInSeconds = this.refreshPeriodInSeconds;
        getVCS().fireVetoableChange("refreshPeriodInSeconds", oldRefreshPeriodInSeconds, refreshPeriodInSeconds);
        this.refreshPeriodInSeconds = refreshPeriodInSeconds;
        getPCS().firePropertyChange("refreshPeriodInSeconds", oldRefreshPeriodInSeconds, refreshPeriodInSeconds);
    }
    
    /** Getter for property commitOption.
     * @return Value of property commitOption.
     *
     */
    public String getCommitOption() {
        return this.commitOption;
    }
    
    /** Setter for property commitOption.
     * @param commitOption New value of property commitOption.
     *
     * @throws PropertyVetoException
     *
     */
    public void setCommitOption(String commitOption) throws java.beans.PropertyVetoException {
        String oldCommitOption = this.commitOption;
        getVCS().fireVetoableChange("commitOption", oldCommitOption, commitOption);
        this.commitOption = commitOption;
        getPCS().firePropertyChange("commitOption", oldCommitOption, commitOption);
    }

    public String getHelpId() {
        return "AS_CFG_EntityEjb";                                      //NOI18N
    }
}
