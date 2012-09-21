/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.saas.services.strikeiron.ui;

import com.strikeiron.search.AUTHENTICATIONSTYLE;
import com.strikeiron.search.ArrayOfMarketPlaceService;
import com.strikeiron.search.MarketPlaceService;
import com.strikeiron.search.SORTBY;
import com.strikeiron.search.SearchOutPut;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.saas.services.strikeiron.StrikeIronSearch;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author nam
 */
public class ServiceTableModel extends DefaultTableModel {
    private static final long serialVersionUID = 1L;
    public static final QName SI_SEARCH_SERVICE = new QName("http://www.strikeiron.com", "SISearchService");
    public static final String STRIKE_IRON_HOME = WsdlServiceProxyDescriptor.WEBSVC_HOME + "/strikeiron";
    public static final String SEARCH_PROPERTIES = "search.properties";
    public static final String WSDL_LOCATION = "wsdlLocation";
    public static final String USERID = "userId";
    public static final String PASSWORD = "password";
    private static final String DEFAULT_USERID = "Sun_Search@strikeiron.com";
    private static final String DEFAULT_PASSWORD = "SearchSun.01";
    private static final String DEFAULT_URL = "http://ws.strikeiron.com/Searchsunsi01.StrikeIron/MarketplaceSearch?WSDL";
    
    static final int COLUMN_WS_NAME = 1;
    static final int COLUMN_SELECT = 0;
    
    private String wsdlLocation;
    private String userId;
    private String password;
    private AUTHENTICATIONSTYLE authenticationStyle = AUTHENTICATIONSTYLE.SIMPLE_PARAM;
    private Boolean useCustomWSDL = Boolean.TRUE;
    private SORTBY sortBy = SORTBY.NAME;
    private List<? extends WsdlServiceData> result;

    private String status;
    private boolean warnsOrErrors = false;
    private Set<Integer> selectedRows = new HashSet<Integer>();
    private RequestProcessor.Task searchTask;

    public ServiceTableModel() {
        init();
    }
    
    private void init() {
        Properties p = new Properties();
        File propFile = new File(STRIKE_IRON_HOME, SEARCH_PROPERTIES);
        if (propFile.isFile()) {
            try {
                FileInputStream fis = new FileInputStream(propFile);
                try {
                    p.load(fis);
                } finally {
                    fis.close();
                }
            } catch(IOException ioe) {
                // OK
            }
        }
        wsdlLocation = p.getProperty(WSDL_LOCATION);
        if (wsdlLocation == null) {
            wsdlLocation = DEFAULT_URL;
        }
        userId = p.getProperty(USERID);
        if (userId == null) {
            userId = DEFAULT_USERID;
        }
        password =  p.getProperty(PASSWORD);
        if (password == null) {
            password = DEFAULT_PASSWORD;
        }
    }
    
    public AUTHENTICATIONSTYLE getAuthenticationStyle() {
        return authenticationStyle;
    }
    
    public void setAuthenticationStyle(AUTHENTICATIONSTYLE v) {
        authenticationStyle = v;
    }
    
    public SORTBY getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(SORTBY v) {
        sortBy = v;
    }

    public WsdlServiceData getService(int row) {
        return result.get(row);
    }
    
    public interface SearchListener extends EventListener {
        void searchCompleted(ChangeEvent e);
        void serviceSelectionChanged(ChangeEvent e);    
    }
    
    List<SearchListener> listeners = new ArrayList<SearchListener>();
    public void addEventListener(SearchListener listener) {
        listeners.add(listener);
    }
    public void removeEventListener(SearchListener listener) {
        listeners.remove(listener);
    }
    private void fireSearchEnded() {
        for (SearchListener l : listeners) {
            l.searchCompleted(new ChangeEvent(this));
        }
    }
    private void fireServiceSelectionChanged() {
        for (SearchListener l : listeners) {
            l.serviceSelectionChanged(new ChangeEvent(this));
        }
    }
    
    public void doSearch(final String searchTerm) {
        cancelSearch();
        searchTask = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                callSearch(searchTerm);
            }
        });
    }
    
    public boolean cancelSearch() {
        if (searchTask != null) {
            boolean cancelled = searchTask.cancel();
            searchTask = null;
            fireSearchEnded();
            return cancelled;
        }
        return true;
    }

    /**
     * Returns number of selected rows in table model.
     *
     * @return  number of selected rows (never negative).
     */
    public int getSelectedCount() {
        return selectedRows.size();
    }

    public Set<? extends WsdlServiceData> getSelectedServices() {
        Set<WsdlServiceData> selection = new HashSet<WsdlServiceData>();
        for (int i : selectedRows) {
            selection.add(result.get(i));
        }
        return selection;
    }
    
    public String getStatusMessage() {
        return status;
    }

    public boolean hasWarnsOrErrors() {
        return warnsOrErrors;
    }

    private List<? extends WsdlServiceData> convertResult(List<MarketPlaceService> rawResult) {
        List<WsdlServiceData> converted = new ArrayList<WsdlServiceData>();
        if (rawResult != null) {
            for (MarketPlaceService service : rawResult) {
                WsdlServiceData raw = new SiServiceData(service);
                converted.add(raw);
            }
        }
        return converted;
    }
    
    private void clearStatusMessage() {
        status = "";
        warnsOrErrors = false;
    }
    
    private void setErrorMessage(String msg) {
        status = msg;
        warnsOrErrors = true;
    }
    
    private void setStatusMessage(String msg) {
        status = msg;
        warnsOrErrors = false;
    }
    
    private void callSearch(String searchTerm) {
        clearStatusMessage();
        selectedRows = new HashSet<Integer>();
        result = new ArrayList<WsdlServiceData>();
        fireTableDataChanged();
        
        try {
            SearchOutPut output = StrikeIronSearch.search(userId, password,
                    searchTerm, sortBy, useCustomWSDL, authenticationStyle);
            if (output != null) {
                ArrayOfMarketPlaceService amps = output.getStrikeIronWebServices();
                if (amps != null) {
                    result = convertResult(output.getStrikeIronWebServices().getMarketPlaceService());
                }
                if (output.getServiceStatus() != null) {
                    String msg = output.getServiceStatus().getStatusDescription();
                    if (msg == null || msg.trim().length() == 0 || msg.startsWith("Found")) { //NOI18N
                        setStatusMessage(NbBundle.getMessage(ServiceTableModel.class, "MSG_Found", result.size()));
                    } else {
                        setErrorMessage(NbBundle.getMessage(ServiceTableModel.class, "MSG_ERROR", msg));
                    }
                }
            }
            fireTableDataChanged();
        } catch (Exception ex) {
            setErrorMessage(ex.getLocalizedMessage());
            ex.printStackTrace();
        } finally {
            fireSearchEnded();
            searchTask = null;
        }
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Class getColumnClass(int column) {
        if (column == COLUMN_SELECT) {
            return Boolean.class;
        }
        return String.class;
    }
    
    @Override
    public String getColumnName(int column) {
        switch(column) {
        case COLUMN_WS_NAME:
            return NbBundle.getMessage(ServiceTableModel.class, "LBL_ServiceName");
        case COLUMN_SELECT:
            return NbBundle.getMessage(ServiceTableModel.class, "LBL_Select");
        }
        throw new IllegalArgumentException("column > 1"); //NOI18N
    }

    public boolean isSearching() {
        return searchTask != null && result ==  null;
    }
    
    @Override
    public int getRowCount() {
        if (isSearching()) {
            return 1;
        } else {
            return result != null ? result.size() : 0;
        }
    }
    
    @Override
    public Object getValueAt(int row, int column) {
        if (isSearching()) {
            return NbBundle.getMessage(ServiceTableModel.class, "MSG_Searching");
        } else if (result == null) {
            throw new IllegalStateException("Search has not started or has no results");
        }
        
        WsdlServiceData mps = result.get(row);
        switch(column) {
        case COLUMN_WS_NAME:
            return mps.getServiceName();
        case COLUMN_SELECT:
            return selectedRows.contains(row) || mps.isInRepository();
        default:
            throw new IllegalArgumentException("column = "+column); //NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (column == COLUMN_SELECT && value instanceof Boolean) {
            boolean selected = (Boolean) value;
            if (selected) {
                selectedRows.add(row);
            } else {
                selectedRows.remove(row);
            }
            fireServiceSelectionChanged();
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        switch(column) {
        case COLUMN_SELECT:
            return ! getService(row).isInRepository();
        default:
            return false;
        }
        
    }
    
}
