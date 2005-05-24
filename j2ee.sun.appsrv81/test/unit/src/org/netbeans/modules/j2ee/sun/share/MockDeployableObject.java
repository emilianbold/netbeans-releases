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
 * MockDeployableObject.java
 *
 * Created on March 3, 2004, 9:59 PM
 */

package org.netbeans.modules.j2ee.sun.share;

import javax.enterprise.deploy.model.DDBeanRoot;
//import org.netbeans.modules.j2ee.sun.share.configbean.MockDDBeanRoot;

//import java.lang.UnsupportedOperationException

/**
 *
 * @author  vkraemer
 */
public class MockDeployableObject implements javax.enterprise.deploy.model.DeployableObject {
    
    DDBeanRoot ddroot;
    
    /** Creates a new instance of MockDeployableObject */
//    public MockDeployableObject() {
//    } 

    public java.util.Enumeration entries() {
        throw new UnsupportedOperationException();
    }
    
    public javax.enterprise.deploy.model.DDBean[] getChildBean(String str) {
        throw new UnsupportedOperationException();
    }
    
    public Class getClassFromScope(String str) {
        throw new UnsupportedOperationException();
    }
    
    public javax.enterprise.deploy.model.DDBeanRoot getDDBeanRoot() {
        return ddroot;
    }
        
    public javax.enterprise.deploy.model.DDBeanRoot getDDBeanRoot(String str) {
        if(str.equals("WEB-INF/webservices.xml") || str.equals("WEB-INF/web.xml")){ //NOI18N
            // TODO Extend to test ServletRef DConfigBean
            //later we need to return DDBeanRoot with some values and write more
            //tests to make sure sun-web.xml getting the required default values
            //from ServletRef DConfigBean in case this ServletRef represents a
            //web services.
            return null;
        }else{
            throw new UnsupportedOperationException();
        }
    }

    public java.io.InputStream getEntry(String str) {
        throw new UnsupportedOperationException();
    }
    
    public String getModuleDTDVersion() {
        throw new UnsupportedOperationException() ;       
    }
    
    public String[] getText(String str) {
        throw new UnsupportedOperationException();
    }
    
    public javax.enterprise.deploy.shared.ModuleType getType() {
        if (null != ddroot) {
            if (ddroot.getXpath().startsWith("/web-app")) {
                return javax.enterprise.deploy.shared.ModuleType.WAR;
            }
            if (ddroot.getXpath().startsWith("/ejb-jar")) {
                return javax.enterprise.deploy.shared.ModuleType.EJB;
            }
        }
        throw new UnsupportedOperationException();
    }
    
    public void setDDBeanRoot(javax.enterprise.deploy.model.DDBeanRoot ddroot) {
        this.ddroot = ddroot;
    }
}
