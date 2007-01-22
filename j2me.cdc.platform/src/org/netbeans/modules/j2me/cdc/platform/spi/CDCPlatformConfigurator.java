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

package org.netbeans.modules.j2me.cdc.platform.spi;

import javax.swing.JPanel;

/**
 *
 * @author suchys
 */
public abstract class CDCPlatformConfigurator {
    
    public static final CDCPlatformConfigurator NO_CONFIGURATOR = new NoConfigurator();
    
    /**
     * @return true if platform/tools are correctly configured
     */
    public abstract boolean isConfigured();
    
    /**
     * @return panel embeding tools
     */
    public abstract JPanel getConfigurationTools();
    
    /**
     * @return error / warning (error != null && !isConfigured) / (error != null && isConfigured)
     */
    public abstract String getInfo();
        
    private static class NoConfigurator extends CDCPlatformConfigurator {
        public boolean isConfigured() {
            return true;
        }

        public JPanel getConfigurationTools() {
            return null;
        }

        public String getInfo() {
            return null;
        }        
    }
}
