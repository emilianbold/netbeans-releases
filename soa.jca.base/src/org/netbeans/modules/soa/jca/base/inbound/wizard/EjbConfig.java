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

/**
 * data structure for inbound mdb pool settings
 *
 * @author echou
 */
public class EjbConfig {

    private int steadyPoolSize = 0;
    private int resizeQuantity = 16;
    private int maxPoolSize = 64;
    private long poolIdleTimeout = 600;  // in seconds
    private long maxWaitTime = 10000;  // in milliseconds

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public long getPoolIdleTimeout() {
        return poolIdleTimeout;
    }

    public void setPoolIdleTimeout(long poolIdleTimeout) {
        this.poolIdleTimeout = poolIdleTimeout;
    }

    public int getResizeQuantity() {
        return resizeQuantity;
    }

    public void setResizeQuantity(int resizeQuantity) {
        this.resizeQuantity = resizeQuantity;
    }

    public int getSteadyPoolSize() {
        return steadyPoolSize;
    }

    public void setSteadyPoolSize(int steadyPoolSize) {
        this.steadyPoolSize = steadyPoolSize;
    }

}
