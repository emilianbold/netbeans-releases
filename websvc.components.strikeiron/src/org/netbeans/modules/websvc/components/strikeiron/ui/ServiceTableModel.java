/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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
