/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.model;

/**
 * @author  Winston Prakash
 */
public class WebServiceListModelEvent {
    String websvcGroupId;
    String websvcId;
    
    public WebServiceListModelEvent(String id) {
        websvcGroupId = id;
    }
    
    public void setWebServiceGroupId(String id){
        websvcGroupId = id;
    }
    
    public String getWebServiceGroupId(){
        return websvcGroupId;
    }
}
