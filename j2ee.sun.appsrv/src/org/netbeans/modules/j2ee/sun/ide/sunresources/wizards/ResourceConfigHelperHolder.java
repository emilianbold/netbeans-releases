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
 * ResourceConfigHelperHolder.java
 *
 * Created on October 17, 2002, 12:11 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.util.Vector;

import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;


/**
 *
 * @author  shirleyc
 */
public class ResourceConfigHelperHolder implements WizardConstants {
    private ResourceConfigHelper mainHelper = null;
    private Vector associated = new Vector();
    private boolean hasCP = false;
    private boolean hasDS = false;
       
    public ResourceConfigHelperHolder() {
        mainHelper = new ResourceConfigHelper(1);
    }
    
    public ResourceConfigHelperHolder (ResourceConfigHelper helper) {
        mainHelper = helper;
    }
    
    public ResourceConfigHelper addAssociatedHelper() {
        ResourceConfigHelper helper = new ResourceConfigHelper(1);
        associated.add(helper);
        return helper;
    }
     
    public ResourceConfigHelper getMainHelper() {
        return mainHelper;
    }
    
    public ResourceConfigHelper getConnPoolHelper() {
        mainHelper.getData().setResourceName(__JdbcConnectionPool);
        return mainHelper;
    }
    
    public ResourceConfigHelper getDataSourceHelper() {
        mainHelper.getData().setResourceName(__JdbcResource);
        return mainHelper;
    }
    
    public ResourceConfigHelper getJMSHelper() {
        mainHelper.getData().setResourceName(__JmsResource);
        return mainHelper;
    }
    
    public ResourceConfigHelper getMailHelper() {
        mainHelper.getData().setResourceName(__MailResource);
        return mainHelper;
    }
    
    public ResourceConfigHelper getPMFHelper() {
        mainHelper.getData().setResourceName(__PersistenceManagerFactoryResource);
        return mainHelper;
    }
    
    public Vector getAssociatedHelpers() {
        return associated;
    }
    
    public ResourceConfigHelper getCPHelper() {
        for (int i = 0; i < associated.size(); i++) {
            ResourceConfigHelper item = (ResourceConfigHelper)associated.elementAt(i);
            if (item.getData().getResourceName().equals(__JdbcConnectionPool)) {
                return item;
            }
        }
        return null;
    }
    
    public ResourceConfigHelper getDSHelper() {
        for (int i = 0; i < associated.size(); i++) {
            ResourceConfigHelper item = (ResourceConfigHelper)associated.elementAt(i);
            if (item.getData().getResourceName().equals(__JdbcResource)) {
                return item;
            }
        }
        return null;
    }
    
    public void removeAssociatedHelpers() {
        if (associated.size() > 0)
            associated = new Vector();
    }
    
    public void removeLastAssociatedHelper() {
        if (associated.size() > 0)
            associated.remove(associated.size() - 1);
    }
    
    public boolean hasAssociatedHelpers() {
        return hasCP || hasDS;
    }
    
    public void setHasCPHelper(boolean value) {
        hasCP = value;
    }
    
    public boolean hasCPHelper() {
        return hasCP;
    }
    
    public boolean hasDSHelper() {
        return hasDS;
    }
    
    public void setHasDSHelper(boolean value) {
        hasDS = value;
    }
}
