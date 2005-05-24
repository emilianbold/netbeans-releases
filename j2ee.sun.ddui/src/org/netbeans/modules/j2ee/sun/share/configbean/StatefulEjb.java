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

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.CheckpointAtEndOfMethod;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Method;

/**
 *
 * @author  vkraemer
 */
public class StatefulEjb extends SessionEjb {

    /** Creates a new instance of SunONEStatelessEjbDConfigBean */	
    public StatefulEjb() {
    }

    private String availabilityEnabled;


    private CheckpointAtEndOfMethod checkpointAtEndOfMethod;
    
    
    /* ------------------------------------------------------------------------
     * XPath to Factory mapping support
     */
/*
	private HashMap statefulEjbFactoryMap;
	
	protected Map getXPathToFactoryMap() {
        if(statefulEjbFactoryMap == null) {
                statefulEjbFactoryMap = (HashMap) super.getXPathToFactoryMap();

                // add child DCB's specific to Stateful Session Beans
        }

        return statefulEjbFactoryMap;
    }
 */

    /* ------------------------------------------------------------------------
     * Persistence support.  Loads DConfigBeans from previously saved Deployment
     * plan file.
     */
    protected class StatefulEjbSnippet extends BaseEjb.BaseEjbSnippet {
        public CommonDDBean getDDSnippet() {
            Ejb ejb = (Ejb) super.getDDSnippet();

            if(availabilityEnabled != null){
                try{
                    if(availabilityEnabled.length() > 0){
                        ejb.setAvailabilityEnabled(availabilityEnabled);
                    }else{
                        ejb.setAvailabilityEnabled(null);
                    }
                }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException e){
                    //System.out.println("Not Supported Version");      //NOI18N
                }
            }

            if(null != checkpointAtEndOfMethod){
                try{
                    ejb.setCheckpointAtEndOfMethod((CheckpointAtEndOfMethod)checkpointAtEndOfMethod.clone());
                }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException e){
                    //System.out.println("Not Supported Version");      //NOI18N
                }
            }

            return ejb;
        }

        public boolean hasDDSnippet() {
            if(super.hasDDSnippet()){
                return true;
            }

            if (null != availabilityEnabled) {
                return true;
            }

            if (null != checkpointAtEndOfMethod) {
                return true;
            }
            return false;
        }
    }


    java.util.Collection getSnippets() {
        Collection snippets = new ArrayList();
        snippets.add(new StatefulEjbSnippet());
        return snippets;
    }


    protected void loadEjbProperties(Ejb savedEjb) {
        super.loadEjbProperties(savedEjb);
        try{
            availabilityEnabled =  savedEjb.getAvailabilityEnabled();
        }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException e){
            //System.out.println("Not Supported Version");      //NOI18N
        }

        CheckpointAtEndOfMethod checkpointAtEndOfMethod = null;
        try{
            checkpointAtEndOfMethod = savedEjb.getCheckpointAtEndOfMethod();
        }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException e){
            //System.out.println("Not Supported Version");      //NOI18N
        }

        if(null != checkpointAtEndOfMethod){
            this.checkpointAtEndOfMethod = checkpointAtEndOfMethod;
        }
    }


    public String getAvailabilityEnabled() {
        return this.availabilityEnabled;
    }


    public void setAvailabilityEnabled(String availabilityEnabled) throws java.beans.PropertyVetoException {
        String oldAvailabilityEnabled = this.availabilityEnabled;                                           //NOI18N
        getVCS().fireVetoableChange("availabilityEnabled", oldAvailabilityEnabled, availabilityEnabled);    //NOI18N
        this.availabilityEnabled = availabilityEnabled;                                                     //NOI18N
        getPCS().firePropertyChange("availabilityEnabled", oldAvailabilityEnabled, availabilityEnabled);    //NOI18N
    }


    public CheckpointAtEndOfMethod getCheckpointAtEndOfMethod() {
            return this.checkpointAtEndOfMethod;
    }


    public void setCheckpointAtEndOfMethod(CheckpointAtEndOfMethod checkpointAtEndOfMethod) throws java.beans.PropertyVetoException {
            CheckpointAtEndOfMethod oldCheckpointAtEndOfMethod = this.checkpointAtEndOfMethod;
            getVCS().fireVetoableChange("checkpointAtEndOfMethod", oldCheckpointAtEndOfMethod, checkpointAtEndOfMethod);       //NOI18N
            this.checkpointAtEndOfMethod = checkpointAtEndOfMethod;
            getPCS().firePropertyChange("checkpoint at end of method", oldCheckpointAtEndOfMethod, checkpointAtEndOfMethod);   //NOI18N
    }


    public void addMethod(Method method){
        if(null == checkpointAtEndOfMethod){
            checkpointAtEndOfMethod = StorageBeanFactory.getDefault().createCheckpointAtEndOfMethod();
        }
        checkpointAtEndOfMethod.addMethod(method);
    }


    public void removeMethod(Method method){
        if(null != checkpointAtEndOfMethod){
            checkpointAtEndOfMethod.removeMethod(method);
        }
    }


    public String getHelpId() {
        return "AS_CFG_StatefulEjb";                                    //NOI18N
    }
}
