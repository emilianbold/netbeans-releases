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

package org.netbeans.modules.cnd.discovery.api;

import java.util.List;

/**
 *
 * @author Alexander Simon
 */
public interface DiscoveryProvider {

    /**
     * Returns provider ID
     */
    String getID();
    
    /**
     * Returns provider name
     */
    String getName();

    /**
     * Returns provider description
     */
    String getDescription();
    
    /**
     * Returns property keys of additional information for provider
     */
    List<String> getPropertyKeys();

    /**
     * Returns property of additional information for provider
     */
    ProviderProperty getProperty(String key);
    
    /**
     * Clean provader state
     */
   void clean();

    /**
     * Is analyzer applicable to project
     */
    boolean isApplicable(ProjectProxy project);

    /**
     * Can analyze project
     */
    boolean canAnalyze(ProjectProxy project);

    /**
     * Analyze project and returns list of configuration
     */
    List<Configuration> analyze(ProjectProxy project);
    
    /**
     * Stop analyzing.
     */
    void stop();
}
