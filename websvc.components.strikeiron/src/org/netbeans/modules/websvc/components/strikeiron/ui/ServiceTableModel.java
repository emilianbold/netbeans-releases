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
package org.netbeans.modules.websvc.components.strikeiron.ui;

import com.strikeiron.search.AUTHENTICATIONSTYLE;
import com.strikeiron.search.LicenseInfo;
import com.strikeiron.search.ObjectFactory;
import com.strikeiron.search.RegisteredUser;
import com.strikeiron.search.SISearchService;
import com.strikeiron.search.SISearchServiceSoap;
import com.strikeiron.search.SORTBY;
import com.strikeiron.search.SearchOutPut;
import com.sun.xml.ws.developer.WSBindingProvider;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author nam
 */
public class ServiceTableModel extends DefaultTableModel {
    private AUTHENTICATIONSTYLE authenticationStyle = AUTHENTICATIONSTYLE.SOAP_HEADER;
    private Boolean useCustomWSDL = Boolean.TRUE;
    private SORTBY sortBy = SORTBY.NAME;
    private String searchTerm;

    public ServiceTableModel() {
        
    }
    
    
    public SearchOutPut doSearch(String searchTerm, SORTBY sortBy) {
        if (searchTerm == null) {
            searchTerm = this.searchTerm;
        }
        if (sortBy == null) {
            sortBy = this.sortBy;
        }
        try {
            SISearchService service = new SISearchService();
            SISearchServiceSoap port = service.getSISearchServiceSoap();
            setHeaderParameters(port);
            return port.search(searchTerm, sortBy, useCustomWSDL, authenticationStyle);
        } catch (Exception ex) {
            Logger.global.log(Level.INFO, ex.getLocalizedMessage(), ex);
            return null;
        }
    }

    private void setHeaderParameters(SISearchServiceSoap port) {
        RegisteredUser ru = new RegisteredUser();
        ru.setUserID("Sun_Search@strikeiron.com");
        ru.setPassword("SearchSun.01");
        LicenseInfo li = new LicenseInfo();
        li.setRegisteredUser(ru);
        WSBindingProvider bp = (WSBindingProvider) port;
        bp.setOutboundHeaders(new ObjectFactory().createLicenseInfo(li));
    }
}
