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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;

import java.text.MessageFormat;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;

import org.netbeans.modules.j2ee.sun.share.Constants;
import org.openide.ErrorManager;

/** This is the base class for all DConfigBean objects in the SunONE App Server
 * JSR88 implementation.
 *
 * @author Vince Kraemer
 * @author Peter Williams
 */
public abstract class Base implements Constants, DConfigBean, XpathListener, DConfigBeanUIFactory {

    /** Resource bundle 
     */
    protected static final ResourceBundle bundle = ResourceBundle.getBundle(
        "org.netbeans.modules.j2ee.sun.share.configbean.Bundle");	// NOI18N

    /** Property event names 
     */
    public static final String DISPLAY_NAME = "displayName";    // NOI18N
    public static final String DIRTY_PROPERTY = "dirty";        // NOI18N

    /** Singleton object used as generic old value in property events to force
     *  them to be sent.  (if the new value is "" or null, then using "" or null
     *  for the old value is problematic - when new & old values match, the event
     *  is not fired.
     */
    public static final Object GenericOldValue = new Object();

    private DDBean dDBean;
    private Base parent;
    private String baseXpath;

    /** Name of descriptor element this bean represents, e.g. sun-web-app, servlet-ref, etc. */
    protected String descriptorElement;

    /** isValid represents the valid state of the bean:
     *    null: unknown
     *    TRUE: bean is valid
     *    FALSE: bean has at least one invalid field.
     */
    private Boolean isValid = null;

    /** Validation message database for this bean.
     */
    private ErrorMessageDB errorMessageDB = null;

    /** Utility field used by bound properties. */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /** Utility field used by constrained properties. */
    private VetoableChangeSupport vetoableChangeSupport =  new VetoableChangeSupport(this);

    private PropertyChangeListener validationListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(ErrorMessageDB.VALIDATION_STATE_CHANGED.equals(evt.getPropertyName())) {
                    validationStateChanged((Boolean) evt.getNewValue());
                }
            }
        };

    /** identity property to aid in debugging.  Displays absolute ID of bean in
     *  system.
     */
    private static int identitySource = 0;
    private String identity;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String id) {
    }

    /** Quickly set this bean to dirty so that Studio will add a SaveCookie
     *  for us.
     */
    private int dirtyFlag;

    public void setDirty() {
        int oldDirtyFlag = dirtyFlag;
        dirtyFlag += 1;
        getPCS().firePropertyChange(DIRTY_PROPERTY, oldDirtyFlag, dirtyFlag);
    }

    /** For use by the customizers when they modify a sub property of an element.
     */
    public void firePropertyChange(String propertyName, Object oldProperty, Object newProperty) {
        getPCS().firePropertyChange(propertyName, oldProperty, newProperty);
    }

    /** Creates a new instance of Base */
    protected Base() {
        identity = Integer.toString(++identitySource);
    }

    /** Since we create DConfigBeans via default constructors, this is the real
     *  initialization method.  Override this method if you need to do extra
     *  initialization in a derived class but make absolutely sure your first line
     *  is 'super.init(dDBean, parent)'!!!
     *
     * @param dDBean DDBean that this DConfigBean is bound to
     * @param parent DConfigBean that is the parent of this bean.  Will be null
     *   if this is a DConfigBeanRoot, otherwise, should have a value.
     * @throws ConfigurationException
     */	
    protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
        this.dDBean = dDBean;
        this.parent = parent;
        this.baseXpath = dDBean.getXpath();

        // Build validation field list for this bean
        // !PW We need a better way to do this.  See comment by validationFieldList
        //     member definition.
        updateValidationFieldList();

        dDBean.addXpathListener(dDBean.getXpath(), this);
        getMessageDB().addPropertyChangeListener(validationListener);
    }
    
    /** Cleanup routine.  This is called just before a DConfigBean is removed
     *  from the tree (and all caches).
     */
    protected void cleanup() {
        // remove listeners
        getMessageDB().removePropertyChangeListener(validationListener);
        dDBean.removeXpathListener(dDBean.getXpath(), this);

        // clear errorMessageDB
        synchronized (this) {
            errorMessageDB = null;
        }

        // remove from DConfigBean tree
        if(parent != null) {
            parent.removeChild(this);
        }

//		dDBean = null;
        parent = null;
    }

    protected String getDescriptorElement() {
        return descriptorElement;
    }

    protected void setDescriptorElement(String element) {
        descriptorElement = element;
    }

    protected String getComponentName() {
        return null;
    }

    protected String getAbsoluteXpath(String field) {
        StringBuffer buf = new StringBuffer(baseXpath.length() + field.length() + 1);
        buf.append(baseXpath);
        buf.append("/");	// NOI18N
        buf.append(field);
        return buf.toString();
    }

    /* Does this class of DConfigBeans require JNDI names in general.  Right now
     * this means J2EE 1.3, 1.4 = Yes, JavaEE5 = No.
     */
    protected boolean requiresJndiName() {
        return J2EEVersion.J2EE_1_4.compareSpecification(getJ2EEModuleVersion()) >= 0;
    }


    /** Named child bean data cache to prevent loss of data when named beans haven't
     *  been loaded yet and a save request comes in.
     */
    private Map namedBeanCache = new HashMap(11);

    protected void saveNamedBeans(String type, String nameProperty, CommonDDBean [] data) {
        if(data != null && data.length > 0) {
            Map dataMap = new HashMap(data.length*3);
            for(int i = 0; i < data.length; i++) {
                String beanName = (String) data[i].getValue(nameProperty);
                if(Utils.notEmpty(beanName)) {
                    dataMap.put(beanName, data[i]);
                }
            }
            
            namedBeanCache.put(type, dataMap);
        }    
    }
    
    protected CommonDDBean removeNamedBean(String type, String beanName) {
        CommonDDBean result = null;
        Map dataMap = getNamedBeanMap(type);
        if(dataMap != null) {
            result = (CommonDDBean) dataMap.remove(beanName);
        }
        
        return result;
    }
    
    protected CommonDDBean removeCachedEndpoint(String hostType, String hostNameType, 
            String hostName, String endpointType, String portComponentName) {
        // 1. get cache of host type
        // 2. iterate cache for hostname
        // 3. if found, remove endpoint(s) for portcomponentname from host entry
        CommonDDBean removedEndpoint = null;
        Map dataMap = getNamedBeanMap(hostType);
        if(dataMap != null) {
            try {
                Iterator entryIter = dataMap.entrySet().iterator();
                while(entryIter.hasNext()) {
                    Map.Entry entry = (Map.Entry) entryIter.next();
                    CommonDDBean host = (CommonDDBean) entry.getValue();
                    Object n = host.getValue(hostNameType);
                    if(n instanceof String && hostName.equals((String) n)) {
                        Object [] objs = host.getValues(endpointType);
                        if(objs != null) {
                            for(int i = 0; i < objs.length; i++) {
                                if(objs[i] instanceof WebserviceEndpoint) {
                                    WebserviceEndpoint endpoint = (WebserviceEndpoint) objs[i];
                                    if(portComponentName.equals(endpoint.getPortComponentName())) {
                                        host.removeValue(endpointType, endpoint);
                                        removedEndpoint = endpoint;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch(IllegalArgumentException ex) {
                // programmer bug if this happens.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return removedEndpoint;
    }
    
    protected Map getNamedBeanMap(String type) {
        return (Map) namedBeanCache.get(type);
    }
    
    protected void saveAllNamedBeans(CommonDDBean parentBean) {
        Iterator iter = getNamedBeanSpecs().iterator();
        while(iter.hasNext()) {
            NamedBean beanSpec = (NamedBean) iter.next();
            try {
                Object data = parentBean.getValues(beanSpec.getType());
                if(data instanceof CommonDDBean []) {
                    saveNamedBeans(beanSpec.getType(), beanSpec.getPropertyName(), (CommonDDBean []) data);
                } else if(data != null) {
//                    System.out.println("saveAllNamedBeans: unexpected datatype - " + data.getClass().getSimpleName());
                }
            } catch(Exception ex) {
                // if property does not exist, we'll get a runtime exception from schema2beans.
//                System.out.println("saveAllNamedBeans: " + ex.getLocalizedMessage());
            }
        }
    }
    
    protected void restoreAllNamedBeans(CommonDDBean parentBean, String version) {
        Iterator iter = getNamedBeanSpecs().iterator();
        while(iter.hasNext()) {
            NamedBean beanSpec = (NamedBean) iter.next();
            Map beanMap = getNamedBeanMap(beanSpec.getType());
            restoreNamedBeans(beanMap, beanSpec.getType(), parentBean, version);
        }
    }
    
    protected void restoreNamedBeans(Map beanMap, String parentPropertyName, CommonDDBean parentBean, String version) {
        if(beanMap != null && beanMap.size() > 0) {
            for (Iterator beanIter = beanMap.entrySet().iterator(); beanIter.hasNext();) {
                try {
                    Map.Entry entry = (Map.Entry) beanIter.next();
                    CommonDDBean bean = (CommonDDBean) entry.getValue();
                    parentBean.addValue(parentPropertyName, bean.cloneVersion(version));
                } catch(Exception ex) {
                    // if property does not exist, ignore the runtime exception from schema2beans.
//                    System.out.println("restoreNamedBeans: " + ex.getLocalizedMessage());
                }
            }
        }
    }
    
    protected void updateNamedBeanCache(String type) {
        if(parent != null) {
            String name = getComponentName();
            if(Utils.notEmpty(name)) {
                parent.removeNamedBean(type, name);
            }
        }
    }
    
    protected Collection getNamedBeanSpecs() {
        return Collections.EMPTY_LIST;
    }
    
    private static Collection commonAppBeanSpecs = new ArrayList();
    
    static {
        commonAppBeanSpecs.add(new NamedBean(SunWebApp.EJB_REF, 
                org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef.EJB_REF_NAME));
        commonAppBeanSpecs.add(new NamedBean(SunWebApp.MESSAGE_DESTINATION_REF, 
                org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME));
        commonAppBeanSpecs.add(new NamedBean(SunWebApp.RESOURCE_ENV_REF, 
                org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef.RESOURCE_ENV_REF_NAME));
        commonAppBeanSpecs.add(new NamedBean(SunWebApp.RESOURCE_REF, 
                org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef.RES_REF_NAME));
        commonAppBeanSpecs.add(new NamedBean(SunWebApp.SERVICE_REF, 
                org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef.SERVICE_REF_NAME));
    }
    
    protected static Collection getCommonNamedBeanSpecs() {
        return commonAppBeanSpecs;
    }
    
    protected static class NamedBean {
        private final String type;
        private final String propertyName;
        
        public NamedBean(final String t, final String pn) {
            type = t;
            propertyName = pn;
        }
        
        public String getType() {
            return type;
        }
        
        public String getPropertyName() {
            return propertyName;
        }
    }
    

    /** -----------------------------------------------------------------------
     *  Validation implementation
     */
    protected final synchronized ErrorMessageDB getMessageDB() {
        if(errorMessageDB == null) {
            errorMessageDB = ErrorMessageDB.createMessageDB();
        }
        return errorMessageDB;
    }

    /** !PW This member is interesting.  It stores the list of fieldId's that this
     *  bean can validate.  The list is built as the various derived classes add
     *  the fields they control to the list (see ejb's which are quite multi-tiered.)
     *  In that sense, two objects of the same type (2 ejb-ref's, 2 servlets, etc.)
     *  have the same list and should be able to use the same list.  However, this
     *  field cannot be static because then it would be shared by all classes,
     *  irrespective of type.  For now, it will be unique per bean, but a better
     *  way is probably some registry where beans of the same type can share the
     *  same list.
     */
    protected List validationFieldList = new ArrayList();

    /** override this method (and call overridden version via super) to add
     *  fields to the validation field id list.
     */
    protected void updateValidationFieldList() {
    }

    public void validationStateChanged(Boolean newState) {
        isValid = newState;
        getPCS().firePropertyChange(DISPLAY_NAME, "", getDisplayName());
    }

    /** Returns previous result of validateFields() or invokes method if status is
     *  out of date.
     *
     *  @return true if valid, false otherwise. 
     */
    public boolean isValid() {
        if(isValid == null) {
            boolean tempValid = validateFields(true);
            isValid = Boolean.valueOf(tempValid);
        }

        return isValid.booleanValue();
    }

    /** Validate the fields managed by this bean.  Used by the customizers 
     *  (and possibly incremental deployment.)
     *
     * @return true or false as to whether bean is valid or not.
     */
    public boolean validateFields(boolean shortCircuit) {
        ErrorMessageDB messageDB = getMessageDB();
        boolean result = true;

        messageDB.clearErrors();
        for(Iterator iter = validationFieldList.iterator(); iter.hasNext() && (result || !shortCircuit); ) {
            boolean fieldResult = validateField((String) iter.next());
            result = result && fieldResult;
        }

        isValid = Boolean.valueOf(result);

        return result;
    }

    /** Validate a single field managed by this bean.  Used by the customizers
     *  (and possibly incremental deployment.)
     *
     * @param field Field spec (xpath to this field in DTD, should be defined
     *   constant in bean class.)
     * @return true or false as to whether field is valid or not.
     */
    public boolean validateField(String fieldId) {
        return true;
    }

    /** -----------------------------------------------------------------------
     * Implementation of XpathListener interface
     */
    public void fireXpathEvent(XpathEvent xpe) {
//		dumpNotification("fireXpathEvent", xpe);
    }

    /* ------------------------------------------------------------------------
     * Version retrieval methods
     */
    public J2EEBaseVersion getJ2EEModuleVersion() {
        Base parent = getParent();
        if(parent != null) {
            return getParent().getJ2EEModuleVersion();
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("getJ2EEModuleVersion() called on child DConfigBean with null parent: " + this));
        }
        return null;
    }

    public ASDDVersion getAppServerVersion() {
        Base parent = getParent();
        if(parent != null) {
            return getParent().getAppServerVersion();
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("getAppServerVersion() called on child DConfigBean with null parent: " + this));
        }
        return null;
    }

    /* ------------------------------------------------------------------------
     * Child bean finder methods
     */
    protected DDBean getNameDD(String nameXpath) throws ConfigurationException {
        DDBean nameDD = null;

        DDBean[] beans = getDDBean().getChildBean(nameXpath);
        if(beans.length == 1) {
            // Found the DDBean we want.
            nameDD = beans[0];
        } else {
            Object [] args = new Object[2];
            args[0] = getDDBean().getXpath();
            args[1] = nameXpath;

            if(beans.length > 1) {
                throw Utils.makeCE("ERR_DDBeanHasDuplicateRequiredXpaths", args, null);	// NOI18N
            } else {
                throw Utils.makeCE("ERR_DDBeanMissingRequiredXpath", args, null);	// NOI18N
            }
        }

        return nameDD;
    }

    protected void validateDDBean(DDBean ddBean) throws ConfigurationException {
        // DDBean cannot be null
        if(ddBean == null) {
            throw Utils.makeCE("ERR_DDBeanIsNull", null, null);	// NOI18N
        }

        // DDBean's xpath cannot be null
        if(ddBean.getXpath() == null) {
            throw Utils.makeCE("ERR_DDBeanHasNullXpath", null, null);	// NOI18N
        }

        // Note: DDBean's text field can be empty (and so presumably can be null).
    }



    /* ------------------------------------------------------------------------
     * DConfigBean interface methods
     */
    /** Returns the beans that hold configuration data for particular
     * subelements of the application.xml
     * @param dDBean DDBean representing an xpath for which we want to attach a DConfigBean.
     * @throws ConfigurationException if there is an error creating the sub-bean
     * @return The DConfigBean that holds extended configuration data.
     */    
    public DConfigBean getDConfigBean(DDBean dDBean) 
            throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        try {
            jsr88Logger.entering(Base.class.toString(),"getDConfigBean",dDBean);

            validateDDBean(dDBean);
            Base dcbResult = getDCBInstance(dDBean);

            // !PW If we get a result from the cache, we should verify that the bean
            // return is the correct type for the DDBean passed in, in case someone
            // is trying to reuse DDBean objects (ala Vince in his test case!)

            if(dcbResult == null) {
                dcbResult = getDCBFactoryMgr().createDCB(dDBean, this);

                if(dcbResult != null) {
                    putDCBInstance(dcbResult);
                    addChild(dcbResult);

                    // Lastly, if this bean is a member of a group, return the head
                    // of the group to the caller.
                    //
                    Base groupHead = dcbResult.getDCBHead();
                    if(groupHead != null) {
                        dcbResult = groupHead;
                    }
                    
                    // !PW FIXME bug workaround IZ 41214
                    beanAdded(dcbResult.getDDBean().getXpath());
                }
            }

            return dcbResult;
        } catch(java.lang.AssertionError ex) {
            ConfigurationException ce = new ConfigurationException();
            ce.initCause(ex);
            throw ce;			
        } catch(RuntimeException ex) {
            throw Utils.makeCE("ERR_UnknownConfigException", null, ex);	// NOI18N
        }
    }

    /** !PW FIXME Workaround for broken XpathEvent.BEAN_ADDED not being sent.
     *  Override this method (see WebAppRoot) to be notified if a child bean
     *  is created.  See IZ 41214
     */
    protected void beanAdded(String xpath) {
    }

    /** !PW FIXME Workaround for broken XpathEvent.BEAN_REMOVED not being sent.
     *  Override this method (see WebAppRoot) to be notified if a child bean
     *  is destroyed.  See IZ 41214
     */
    protected void beanRemoved(String xpath) {
    }

    /**
     * @return
     */    
    public DDBean getDDBean() {
        return this.dDBean;
    }

    /** Xpaths that this bean extends.  Each DConfigBean that has children will
     * provide an array of xpaths (which, by the way, are happen to be the keys
     * in the factory mapping for those children).
     * @return The array of xpaths that are interesting to this DConfigBean instance
     */    
    public String[] getXpaths() {
        return getDCBFactoryMgr().getFactoryKeys();
    }

    /** The DDBean (or one of it's children) that this DConfigBean is bound to
     *  has changed.
     *
     * @param xpathEvent
     */    
    public void notifyDDChange(XpathEvent xpathEvent) {
//		dumpNotification("notifyDDChange", xpathEvent);
    }

//	protected void dumpNotification(String fnName, XpathEvent xpathEvent) {
//		String type;
//		
//		if(xpathEvent.isAddEvent()) {
//			type = "BEAN_ADD";
//		} else if(xpathEvent.isRemoveEvent()) {
//			type = "BEAN_REMOVE";
//		} else if(xpathEvent.isChangeEvent()) {
//			type = "BEAN_CHANGE";
//		} else {
//			type = "UNKNOWN TYPE";
//		}
//		
//		System.out.println(fnName + ": XPATHEVENT: " + type + 
//			", DCB identity = " + getIdentity() +
//            ", DCB type = " + getClass().getName() + 
//			", xpath = " + xpathEvent.getBean().getXpath() + 
//			", DCB xpath = " + getDDBean().getXpath());		
//	}

        /** JSR-88: Removes a child DConfigBean from this bean.
         *  Spec interpretation note: If the dConfigBean parameter refers to a bean
         *  that has it's own children, those children are also removed, ad infinitum.
         *
     * @param dConfigBean The child DConfigBean to remove from this DConfigBean
     * @throws BeanNotFoundException
     */    
    public void removeDConfigBean(DConfigBean dConfigBean) throws BeanNotFoundException {
        if(dConfigBean != null) {
            if(dConfigBean.getDDBean() != null) {
                if(((Base) dConfigBean).getParent() == this) {
                    // Handle children first.

                    // Can't use iterator here or we will possibly get a ConcurrentModificationException
                    // as the children clean themselves up.
                    Base beanToRemove = (Base) dConfigBean;
                    Object children[] = beanToRemove.getChildren().toArray();
                    for(int i = 0; i < children.length; i++) {
                        try {
                            beanToRemove.removeDConfigBean((Base) children[i]);
                        } catch(BeanNotFoundException ex) {
                            // This would suggest a corrupt tree or bad code somewhere if it happens.
                            // Catch & log it and continue cleaning the tree.
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }

                    DDBean key = dConfigBean.getDDBean();
                    beanToRemove = removeDCBInstance(key);

                    if(beanToRemove != null) {
                        if(beanToRemove instanceof BaseRoot) {
                            // remove from root cache as well.
                            BaseRoot rootBean = (BaseRoot) getConfig().getDCBRootCache().remove(key);

                            if(rootBean != null) {
                                assert(rootBean == beanToRemove); // these should be the same, right?
                            }
                        } else if(beanToRemove instanceof BaseModuleRef) {
                            // Clean up patch list - the patch list should be empty, but you never know...
                            getConfig().getPatchList().remove(key);
                        }
                    }

                    if(beanToRemove != null) {
                        // !PW FIXME 1st half - workaround for IZ 41214 (see method comment)
                        String beanXpath = beanToRemove.getDDBean().getXpath();

                        // cleanup bean before throwing away
                        beanToRemove.cleanup();
                        beanToRemove = null;

                        // !PW FIXME 2nd half - workaround for IZ 41214 (see method comment)
                        beanRemoved(beanXpath);
                    } else {
                        Object [] args = new Object [2];
                        args[0] = dConfigBean.getDDBean();
                        args[1] = key.getXpath();
                        throw new BeanNotFoundException(MessageFormat.format(
                            bundle.getString("ERR_DConfigBeanNotFoundOnRemove"), args));
                    }
                } else {
                    // The parent of the DConfigBean parameter is not this instance - spec violation.
                    throw new BeanNotFoundException(
                        bundle.getString("ERR_DConfigBeanWrongParentOnRemove"));
                }
            } else {
                // DDBean is null.  This could be that this DConfigBean has
                // previously been removed.
                throw new BeanNotFoundException(
                    bundle.getString("ERR_DConfigBeanNotFoundOnRemoveNullDDBean"));
            }
        } else {
            // DConfigBean is null
            throw new BeanNotFoundException(
                bundle.getString("ERR_DConfigBeanNotFoundOnRemoveNullDConfigBean"));
        }
    }

    /**
     * @param pCL
     */    
    public void addPropertyChangeListener(PropertyChangeListener pCL) {
        propertyChangeSupport.addPropertyChangeListener(pCL);
    }

    /**
     * @param pCL
     */    
    public void removePropertyChangeListener(PropertyChangeListener pCL) {
        propertyChangeSupport.removePropertyChangeListener(pCL);
    }

    /**
     * @return
     */    
    protected PropertyChangeSupport getPCS() {
        return propertyChangeSupport;
    }

    /**
     * @return
     */    
    protected VetoableChangeSupport getVCS() {
        return vetoableChangeSupport;
    }

    /** Adds a VetoableChangeListener to the listener list.
     * @param l The listener to add.
     *
     */
    public void addVetoableChangeListener(VetoableChangeListener l) {
        vetoableChangeSupport.addVetoableChangeListener(l);
    }

    /** Removes a VetoableChangeListener from the listener list.
     * @param l The listener to remove.
     *
     */
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        vetoableChangeSupport.removeVetoableChangeListener(l);
    }

    /**
     * @return
     */    
    public Base getParent() {
        return parent;
    }

    /** Retrieve the parser provided by the root DCB in this tree.  Failing that,
     *  return the parser provided by the master root DCB of the entire configuration.
     */
    protected ConfigParser getParser() {
        Base parent = getParent();
        if(parent != null) {
            return parent.getParser();
        }

        SunONEDeploymentConfiguration config = getConfig();
        if(config != null) {
            BaseRoot dcbRoot = config.getMasterDCBRoot();
            if(dcbRoot != null) {
                return dcbRoot.getParser();
            }
        }

        return null;
    }

    /** A DConfigBean may represent data that would go into multiple descriptor 
     * files.  A DConfigBean may also expose properties of a super bean. These
     * snippets are used to hold the schema2beans object and the name of the file
     * that the bean will be part of. They are merged in Base.addToGraphs()
     * to produce the deployment plan file.
     *
     * @return a collection of snippet objects for this bean. Null is not allowed.
     */
    abstract Collection getSnippets();

    /** Loads the values of the DConfigBean properties from the deployment plan file
     * that this bean's DeploymentConfiguration parent read.  This method should be
     * called by init.  It is also called in the restore methods on
     * DeploymentConfiguration.
     *
     * @param config The SunONEDeploymentConfig object that read in the deployment plan file
     * @return true if a bean was found and loaded, false otherwise.
     */	
    abstract boolean loadFromPlanFile(SunONEDeploymentConfiguration config);

    /** This method operates recursively to perform a depth first search of the
     * DConfigBean hierarchy, creating the corresponding schema2beans graph as
     * it travels.  Pieces of the graph can be merged as children of the tracking
     * parent, or into other parts of a root found in the graph, or finally, into
     * whole new roots that are added to the final map.
     *
     * @param map A map of file names to schema2beans object graphs
     * @param bbCurrent The current tracking parent basebean 
     * @parem bbKey The map lookup key that matches the current tracking bean passed in
     */    
    public void addToGraphs(Map map, CommonDDBean bbCurrent, String bbKey) {
        jsr88Logger.entering(this.getClass().toString(), "addToGraphs");	// NOI18N

        String uriText = getUriText();
        Collection snippets = getSnippets();

        boolean isFirst = true;
        CommonDDBean newCurrentBean = null;
        String newSnippetKey = "";

        Iterator iter = snippets.iterator();
        while(iter.hasNext()) {
            try {
                CommonDDBean bean = null;
                Snippet s = (Snippet) iter.next();

                if(s.hasDDSnippet()) {
                    String snippetKey = Utils.getFQNKey(uriText, s.getFileName());
                    if(snippetKey.compareTo(bbKey) == 0) {
                        // merge with current basebean
                        bean = s.mergeIntoRovingDD(bbCurrent);
                    } else if(map.containsKey(snippetKey)) {
                        // merge with root -- this option is unlikely to be used it means that the
                        // current snippet IS represented in existing graph AND the current roving
                        // bean is NOT in the same tree, so we must merge at root level with the root
                        // we found in the graph.
                        //
                        // !PW This option is now used by WebAppCache, a javabean that represents the
                        // cache portion of sun-web.xml and is owned/parented by WebAppRoot.
                        //
                        try {
                            bean = s.mergeIntoRootDD((CommonDDBean) map.get(snippetKey));
                        } catch(UnsupportedOperationException ex) {
                            jsr88Logger.finest("Invalid Snippet:  Snippet Class: " + s.getClass().getName());
                            CommonDDBean parent = (CommonDDBean) map.get(snippetKey);
                            jsr88Logger.finest("Parent Bean: " + ((parent != null) ? parent.getClass().getName() : "(null -- ack!)"));
                            jsr88Logger.finest("Snippet Key: " + snippetKey);
                            jsr88Logger.finest("Snippet Property Name: " + s.getPropertyName());
                            throw ex;
                        }
                    } else {
                        // create new basebean from root and add to graph
                        bean = s.getDDSnippet();

                        // !PW FIXME Cmp SNIPPET is temporarily returning null here.
                        if(bean != null) {
                            map.put(snippetKey, bean);
                        }
                    }

                    if(isFirst) {
                        // Save bean from first snippet for passing to children.
                        newCurrentBean = bean;
                        newSnippetKey = snippetKey;
                        isFirst = false;
                    }
                }
            } catch(Exception ex) {
                jsr88Logger.log(Level.SEVERE, "Base.newAddToGraph() -- exception processing bean", ex);	// NOI18N
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        // Handle children before returning
        //
        Collection childList = getChildren();
        iter = childList.iterator();
        while(iter.hasNext()) {
            Base childDCB = (Base) iter.next();
            childDCB.addToGraphs(map, processParentBean(newCurrentBean, childDCB), newSnippetKey);
        }

        jsr88Logger.exiting(this.getClass().toString(), "addToGraphs");	// NOI18N
    }

    /** This method allows some customization of how parent basebeans are adjusted
     *  when the depthfirst search in addToGraphs is passing a parent basebean
     *  on to a child DConfigBean.  In most cases, no adjustment is necessary.
     *  See BaseEjb.processParentBean for a case where it is.
     */
    protected CommonDDBean processParentBean(CommonDDBean bean, DConfigBean child) {
        // Calculate what the parent S2B bean should be for this child and
        // return that.
        //
        // Basic implementation assumes no translation is necessary
        return bean;
    }

    /**
     * @return
     */    
    Collection getChildren() {
        return children;
    }

    public String getUriText() {
        if(parent != null) {
            return parent.getUriText();
        }

        // This should never get executed actually, since this method is
        // overridden in BaseRoot.  But just in case...
        return ""; // NOI18N
    }

    static private char XPATH_SEPCHAR = '/';

    /** Holds value of property xpath. */
    //private String xpath;

    /** hack to clean up the J2EE 1.4 RI beta 1 getText() value on a DDBean.
     * @param dDBean the bean that is having its text cleaned
     */
    static String cleanDDBeanText(DDBean dDBean) {
        String candidate = null;

        try {
            if(dDBean == null) {
                return candidate;
            }

            candidate = dDBean.getText();
            if (null == candidate || (candidate.length() == 0)) {
                return candidate;
            }

            if (!candidate.startsWith("<?xml")) {	// NOI18N
                return candidate;
            }

            String xpath = dDBean.getXpath();
            if (null == xpath || (xpath.length() == 0)) {
                return candidate;
            }

            int lindex = xpath.lastIndexOf(XPATH_SEPCHAR);
            if (lindex > -1) {
                lindex += 1;
                String finalEl = xpath.substring(lindex);
                finalEl = "<" + finalEl + ">";	// NOI18N
                int elementPos = candidate.indexOf(finalEl);
                if (elementPos < 0) {
                    return candidate;
                }
                String retVal = candidate.substring(elementPos + finalEl.length());
                if (retVal.length() < finalEl.length() + 1) {
                    return retVal;
                }
                retVal = retVal.substring(0,retVal.length() - (finalEl.length()+2));
                return retVal;
            }
        } catch(RuntimeException ex) {
            jsr88Logger.throwing("Base", "cleanDDBeanText", ex);	// NOI18N
        }

        return candidate;
    }

    private DDBean secondary;
    private Set children = new LinkedHashSet();

    /**
     * @param newKid
     */	
    protected void addChild(DConfigBean bean) {
        children.add(bean);
    }

    protected boolean removeChild(DConfigBean bean) {
        return children.remove(bean);
    }

    /**
     * @param secondary
     */	
    void setSecondary(DDBean secondary) {
        this.secondary = secondary;
    }

    /**
     * @return
     */    
    public SunONEDeploymentConfiguration getConfig() {
        if (null != parent) {
            return parent.getConfig();
        }

        return null; 
    }

    /* ------------------------------------------------------------------------
     * Implementation of DConfigBeanUIFactory interface
     *
     * This interface allows DConfigBeanProperties object retrieval which is
     * primarily toallow UI customization beyond that provided by JSR-88 1.1 and
     * the Java Beans 1.01 specification.
     */
    /** Retrieve the DConfigBeanProperties object for this DConfigBean
     *
     * @param self Implementation artifact.  Should be null or same as 'this'.
     *   It is not used.
     * @return DConfigBeanProperties Object that provides the extra properties
     *   needed for display.
     */
    public DConfigBeanProperties getUICustomization(DConfigBean self) {
        return new DConfigBeanProperties() {
            public String getDisplayName() {
                return Base.this.getDisplayName();
            }

            public String getHelpId() {
                return Base.this.getHelpId();
            }
        };
    }

    /** Getter for displayName property
     * @return String suitable for display
     */
    public String getDisplayName() {
        // This shows the name of the particular bean, if it has one.  For example,
        // servlets, ejb's, modules inside EAR's, etc.
        //
        String name = getComponentName();
        Object [] args = new Object [1];
        args[0] = Utils.notEmpty(name) ? name : getDescriptorElement();
        String pattern = bundle.getString(isValid() ? "LBL_BeanDisplayName" : "LBL_BeanDisplayNameBroken");
        return MessageFormat.format(pattern, args);		
    }

    /** Getter for helpId property.  Override this method to provide the correct
     *  help context id for any specific DConfigBean.
     *
     *  @return Help context ID for this DConfigBean
     */
        abstract public String getHelpId();
/*	public String getHelpId() {
        // the default should be no help, not a debug message only targetted at us, the developer
                // of this plugin.
        //assert false : this.getClass().getName() + " does not override getHelpId!!!"; // NOI18N		
        return "";
    }
*/	
    /* ------------------------------------------------------------------------
     * DConfigBean caching support.  Allows lookup of existing DCB's by their
     * associated DDBean as a key.
     */

    /**
     * @param base
     */	
    protected void putDCBInstance(Base base) {
        DDBean key = base.getDDBean();
        if(key != null) {
            SunONEDeploymentConfiguration config = getConfig();
            if(config != null) {
                Map cache = config.getDCBCache();

                Object existingDCB = cache.get(key);
                if(existingDCB != null) {
//					jsr88Logger.finest("DCBCache: Replacing existing DCB '" + existingDCB + "' with '" + base + "'");	// NOI18N
                } else {
//					jsr88Logger.finest("DCBCache: Adding DCB to cache, ddbean key = '" + key.getXpath() + "'");	// NOI18N
                }

                cache.put(key, base);
            } else {
//				jsr88Logger.finest("DCBCache: Error: DCB '" + this + "' has null config so '" + base + "' cannot be cached");	// NOI18N
            }
        } else {
//			jsr88Logger.finest("DCBCache: Error: DCB '" + base + "' has null DDBean");	// NOI18N
        }
    }

    /**
     * @param key
     * @return
     */	
    protected Base getDCBInstance(DDBean key) {
//		jsr88Logger.finest("DCBCache: Looking for DCB to match ddbean key '" + key.getXpath() + "'");

        Base result = null;
        SunONEDeploymentConfiguration config = getConfig();

        if(config != null) {
            Map cache = config.getDCBCache();		
            Object o = cache.get(key);

            if(o != null) {
                if(o instanceof Base) {
                    result = (Base) o;
                } else {
//					jsr88Logger.finest("DCBCache(get): Error: object matching DDBean key is wrong type: '" + o.getClass().getName() + "'");	// NOI18N
                }
            } else {
//				jsr88Logger.finest("DCBCache: No DCB match for key: '" + key.getXpath() + "'");	// NOI18N
            }
        } else {
//			jsr88Logger.finest("DCBCache(get): Error: DCB '" + this + "' has null config therefore no cache to search");	// NOI18N
        }

        return result;
    }

    /**
     * @param base
     * @return
     */	
    protected Base removeDCBInstance(Base base) {
        return removeDCBInstance(base.getDDBean());
    }

    /**
     * @param key
     * @return
     */	
    protected Base removeDCBInstance(DDBean key) {
        Base result = null;
        SunONEDeploymentConfiguration config = getConfig();

        if(config != null) {
            Map cache = config.getDCBCache();
            Object o = cache.remove(key);

            if(o != null) {
                if(o instanceof Base) {
                    result = (Base) o;
                } else {
//					jsr88Logger.finest("DCBCache: Error: object matching DDBean key is wrong type: '" + o.getClass().getName() + "'");	// NOI18N
                }
            }
        } else {
//			jsr88Logger.finest("DCBCache(get): Error: DCB '" + this + "' has null config therefore no cache to search");	// NOI18N
        }

        return result;
    }

    /* ------------------------------------------------------------------------
     * Group child bean support.  For any child beans that are stored as groups,
     * e.g. SecurityRoleMapping, ResourceEnvRef, etc., the parent owns the head
     * of the group.  This support provides that storage, as well as a mechanism
     * for locating the head for a particular group (some parents have children
     * in more than one group, e.g. WarRoot has both ejbRef's and resRef's, both
     * of which are grouped.
     */
    /** ----------------------- Support used by parent DCB --------------------
     */
    /** dcbChildGroupMap is initally null because most beans will not even use
     *  this system.  Only DCB's that have linked groups of like child DCB's,
     *  such as SecurityRoleMapping, ResRef, or EjbRef will use this capability.
     */
    private Map dcbChildGroupMap = null;

    /**
     * @param dDBean
     * @return
     */	
    protected Base getDCBGroup(DDBean dDBean) {
        Base dcbResult = null;

        if(dcbChildGroupMap != null) {
            dcbResult = (Base) dcbChildGroupMap.get(dDBean.getXpath());
        }

        return dcbResult;
    }

    /**
     * @param dcb
     */	
    protected void addDCBGroup(Base dcb) {
        if(dcbChildGroupMap == null) {
            dcbChildGroupMap = new HashMap(7);
        }

        if(getDCBGroup(dcb.getDDBean()) == null) {
            dcbChildGroupMap.put(dcb.getDDBean().getXpath(), dcb);
        }
    }

    /** ----------------------- Support used by child DCB --------------------
     */
    /** internal list of beans */
    private List groupDCBList = null;
    private Base dcbHead = null;

    /** initializes a member of a bean group, making the bean the head (or 
     *  adding this bean to an existing group in the specified parent).
     * @param dDBean used to get the xpath that allows finding an existing 
     *  group in the parent, if any.
     * @param parent the parent of this bean, where we look to see if there
     *  is an existing group
     */
    protected void initGroup(DDBean dDBean, Base parent) {
        if(parent != null) {
            Base dcb = parent.getDCBGroup(dDBean);
            if(dcb != null) {
                /* Head has already been created -- this is an additional bean
                 * of same type.
                 */
                dcbHead = dcb;
                dcbHead.addDCBToGroup(this);
            } else {
                /* Head is null, this is the first bean of it's type in for
                 * the given parent.
                 */
                dcbHead = this;
                addDCBToGroup(this);

                parent.addDCBGroup(this);
            }
        }
    }

    /** Adds beans to the list of like-grouped beans.  Only the head bean in the
     *  list will initialize and use this list.  Other beans in the list will
     *  have a reference to the head bean.  If the head bean is null, the list
     *  reference should also be null and that indicates this bean does not
     *  support being grouped (though it may still be the parent of beans that
     *  are grouped).
     * @param dcb DConfigBean to add to this group.  Should only call this
     *   method on the bean that is the head of a group (i.e. do not call it
     *   on a member of a group that is not the head.)
     */
    private void addDCBToGroup(Base dcb) {
        if(groupDCBList == null) {
            groupDCBList = new ArrayList(10);
        }

        groupDCBList.add(dcb);
    }

    /** Retrieves the head bean of a bean group.
     * @return returns the head bean of bean group.
     */
    protected Base getDCBHead() {
        return dcbHead;
    }

    /* ------------------------------------------------------------------------
     * Xpath to Factory mapping support
     */
    private static final java.util.Map defaultXPathToFactory = new java.util.HashMap();

    /** Retrieve the XPathToFactory map for this DConfigBean.  For Base, this is
     *  the default map, which is empty.
     * @return
     */  
    protected java.util.Map getXPathToFactoryMap() {
        return defaultXPathToFactory;
    }

    private DCBFactoryMgr factoryMgrInstance = null;

    /** Retrieve the factory manager for this DConfigBean.  If one has not been
     *  constructed yet, create it.
     * @return
     */
    DCBFactoryMgr getDCBFactoryMgr() {
        if(factoryMgrInstance == null) {
            factoryMgrInstance = new DCBFactoryMgr(getXPathToFactoryMap(), getDDBean().getXpath());
        }

        return factoryMgrInstance;
    }

    /* ------------------------------------------------------------------------
     * More persistence support
     */

    /** Determine which file this bean is likely to go into.  This is based on
     *  the bean's DDBean "buddy".
     */
    protected String constructFileName() {
        String ddXpath = dDBean.getXpath();
        StringBuffer fname = new StringBuffer(32);
        fname.append("sun-"); // NOI18N

        if(null != ddXpath) {
            if(ddXpath.startsWith("/ejb-jar")) { // NOI18N
                fname.append("ejb-jar"); // NOI18N
            } else if(ddXpath.startsWith("/web-app")) { // NOI18N
                fname.append("web"); // NOI18N
            } else if(ddXpath.startsWith("/application")) { // NOI18N
                if (ddXpath.indexOf("client") > -1 ) { // NOI18N
                    fname.append("application-client"); // NOI18N
                } else {
                    fname.append("application"); // NOI18N
                }
            } else if(ddXpath.startsWith("/connector")) { // NOI18N
                fname.append("connector"); // NOI18N
            } else {
                String mess = MessageFormat.format(bundle.getString("ERR_InvalidXPathValueUsage"), // NOI18N
                    new Object[] { ddXpath });
                throw new java.lang.IllegalStateException(mess); 
            }
        } else {
            // this is bad
            throw new java.lang.IllegalStateException("null Xpath value"); // FIXME
        }

        fname.append(".xml");	// NOI18N
        return fname.toString();
    }

    /** This is a basic snippet and will be the base class for most if not all
     *  snippet objects in the DCB hierarchy.  The methods most likely to need
     *  overriding are getDefaultSnippet(), hasDDSnippet() if the snippet in
     *  question could be optional in it's entirety, and getPropertyName() if 
     *  the default merge code is being used.
     *
     *  If custom merge is required, see the interface documentation in Snippet
     *  for specifics on all of these methods.
     */
    abstract class DefaultSnippet implements Snippet {

        public abstract CommonDDBean getDDSnippet();

        public org.netbeans.modules.schema2beans.BaseBean getCmpDDSnippet() {
            return null;
        }

        public boolean hasDDSnippet() {
            return true;
        }

        public String getFileName() {
            return constructFileName();
        }

        public CommonDDBean mergeIntoRootDD(CommonDDBean ddRoot) {
            throw new java.lang.UnsupportedOperationException();
        }

        public CommonDDBean mergeIntoRovingDD(CommonDDBean ddParent) {
            CommonDDBean newBean = getDDSnippet();
            if(newBean != null) {
                if(ddParent != null) {
                    String propertyName = getPropertyName();
                    if(propertyName != null) {
                        ddParent.addValue(propertyName, newBean);
                    } else {
                        jsr88Logger.severe("No property name for " + Base.this.getClass()); // NOI18N
                    }
                } else {
                    jsr88Logger.severe("mergeIntoRovingDD() called with null parent (called on root bean?)"); // NOI18N
                }
            } else {
                jsr88Logger.severe("No snippet to merge for " + Base.this.getClass()); // NOI18N
            }
            return newBean;
        }

        public String getPropertyName() {
            return null;
        }
    }

    protected static class NameBasedFinder implements ConfigFinder {
        private String propertyName;
        private String propertyValue;
        private Class beanType;

        public NameBasedFinder(String propName, String propValue, Class type) {
            this.propertyName = propName;
            this.propertyValue = propValue;
            this.beanType = type;
        }

        public Object find(Object obj) {
            Object result = null;
            CommonDDBean root = (CommonDDBean) obj;
            String[] props = root.findPropertyValue(propertyName, propertyValue);

            for(int i = 0; i < props.length; i++) {
                CommonDDBean candidate = root.getPropertyParent(props[i]);
                if(beanType.isInstance(candidate)) {
                    result = candidate;
                    break;
                }
            }

            return result;
        }
    }
}
