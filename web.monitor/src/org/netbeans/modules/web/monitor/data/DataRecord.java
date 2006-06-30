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
