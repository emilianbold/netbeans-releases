/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.monitor.data;


/**
 * DataRecord.java
 *
 *
 * Created: Wed Mar 20 19:15:16 2002
 *
 * @author Ana von Klopp
 * @version
 */

public interface DataRecord  {
        

    public void setAttributeValue(String attr, String value);
    public String getAttributeValue(String attr); 

    public void setClientData(ClientData value);

    public ClientData getClientData();

    public void setSessionData(SessionData value);

    public SessionData getSessionData();

    public void setCookiesData(CookiesData value);

    public CookiesData getCookiesData();

    public void setDispatches(Dispatches value);

    public Dispatches getDispatches();

    public void setRequestData(RequestData value);

    public RequestData getRequestData();

    public void setServletData(ServletData value);

    public ServletData getServletData();

    public void setEngineData(EngineData value);

    public EngineData getEngineData();

    public void setContextData(ContextData value);

    public ContextData getContextData();

} // DataRecord
