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
import java.util.Map;

/**
 *
 * @author Alexander Simon
 */
public interface ItemProperties {

    /**
     * List of user include paths
     */
    List<String> getUserInludePaths();

    /**
     * List of system include paths
     */
    List<String> getSystemInludePaths();

    /**
     * List of user macros
     */
    Map<String,String> getUserMacros();

    /**
     * List of system predefined macros
     */
    Map<String,String> getSystemMacros();
    
    /**
     * Language kind
     */
    LanguageKind getLanguageKind();

    public enum LanguageKind {
        Unknown,
        C,
        CPP
    }
}
