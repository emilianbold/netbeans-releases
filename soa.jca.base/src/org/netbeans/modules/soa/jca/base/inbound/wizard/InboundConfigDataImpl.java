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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.jca.base.inbound.wizard;

import org.netbeans.modules.soa.jca.base.spi.InboundConfigCustomPanel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author echou
 */
public class InboundConfigDataImpl implements InboundConfigCustomPanel.InboundConfigData {

    private HashMap<String, String> activationProps = new HashMap<String, String> ();
    private int steadyPoolSize = 0;
    private int resizeQuantity = 16;
    private int maxPoolSize = 64;
    private long poolIdleTimeout = 600;  // in seconds
    private long maxWaitTime = 10000;  // in milliseconds

    public InboundConfigDataImpl() {

    }

    public String getActivationProperty(String propertyName) {
        return activationProps.get(propertyName);
    }

    public Set<Map.Entry<String, String>> getActivationProps() {
        return Collections.unmodifiableSet(activationProps.entrySet());
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    public long getPoolIdleTimeout() {
        return poolIdleTimeout;
    }

    public int getResizeQuantity() {
        return resizeQuantity;
    }

    public int getSteadyPoolSize() {
        return steadyPoolSize;
    }



    public void addActivationProperty(String propertyName, String propertyValue) {
        activationProps.put(propertyName, propertyValue);
    }

    public void setMaxPoolSize(int n) {
        maxPoolSize = n;
    }

    public void setSteadPoolSize(int n) {
        steadyPoolSize = n;
    }

    public void setResizeQuantity(int n) {
        resizeQuantity = n;
    }

    public void setMaxWaitTime(long n) {
        maxWaitTime = n;
    }

    public void setPoolIdleTimeout(long n) {
        poolIdleTimeout = n;
    }

}
