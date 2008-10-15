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

package org.netbeans.modules.hudson.api;

import org.netbeans.modules.hudson.impl.HudsonVersionImpl;

/**
 * Describes Hudson version
 *
 * @author Michal Mocnak
 */
public interface HudsonVersion extends Comparable<HudsonVersion> {
    
    /**
     * Supported version
     */
    public static final HudsonVersion SUPPORTED_VERSION = new HudsonVersionImpl("1.104");
    
    /**
     * Returns major version
     *
     * @return major version
     */
    public int getMajorVersion();
    
    /**
     * Returns minor version
     *
     * @return minor version
     */
    public int getMinorVersion();
    
    public String toString();
    
    public boolean equals(Object o);
}