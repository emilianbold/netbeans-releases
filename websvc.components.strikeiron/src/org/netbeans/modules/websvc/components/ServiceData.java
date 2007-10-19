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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.components;

import org.netbeans.modules.websvc.manager.model.WebServiceListModel;

/**
 *
 * @author nam
 */
public abstract class ServiceData {
    public abstract String getVersion();
    public abstract String getWsdlURL();
    public abstract Object getRawService();
    public abstract String getServiceName();
    public abstract String getProviderName();
    public abstract String getDescription();
    public abstract String getInfoPage();
    public abstract String getPurchaseLink();
    public abstract String getPackageName();
    public abstract void setPackageName(String value);
    
    public boolean alreadyExists() {
        return null != WebServiceListModel.getInstance().findWebServiceData(getWsdlURL(), getServiceName(), false);
    }
}
