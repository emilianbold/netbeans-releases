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

package org.netbeans.modules.bpel.debugger.spi.plugin;

import java.util.Properties;
import org.netbeans.modules.bpel.debugger.spi.plugin.Connector;

/**
 * The main entry point for the BPEL Debugger Plug-in functionality.
 * Implementations of this interface should be registered by putting
 * a file with the name
 * org.netbeans.modules.bpel.debugger.spi.plugin.BpelDebuggerPlugin
 * to the META-INF/debugger folder. This file should contain a single line
 * with the full name of the implementation class .
 *
 * @author Alexander Zgursky
 */
public interface BpelDebuggerPlugin {
    /**
     * Returns an identifier for the Bpel Debugger Plugin implementation.
     */
    String getName();
    String getLabel();
    Connector getConnector();
}
