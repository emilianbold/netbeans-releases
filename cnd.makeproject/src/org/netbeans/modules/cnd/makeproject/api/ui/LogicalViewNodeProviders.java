/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api.ui;

import java.util.Vector;

public class LogicalViewNodeProviders {
    private static LogicalViewNodeProviders instance = null;
    private Vector providers = null;

    public static LogicalViewNodeProviders getInstance() {
	if (instance == null)
	    instance = new LogicalViewNodeProviders();
	return instance;
    }

    public Vector getProviders() {
	if (providers == null) {
	    providers = new Vector();
	}
	return providers;
    }

    public LogicalViewNodeProvider[] getProvidersAsArray() {
	Vector cn = getProviders();
	return (LogicalViewNodeProvider[]) cn.toArray(new LogicalViewNodeProvider[cn.size()]);
    }
    
    
    public void addProvider(LogicalViewNodeProvider provider) {
	synchronized(getProviders()) {
	    getProviders().add(provider);
	}
    }

    public void removeProvider(LogicalViewNodeProvider provider) {
	synchronized(getProviders()) {
	    getProviders().remove(provider);
	}
    }
}
