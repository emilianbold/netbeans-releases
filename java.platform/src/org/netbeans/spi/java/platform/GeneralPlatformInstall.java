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

package org.netbeans.spi.java.platform;

/**
 * An super class of all the platform installers. You never subclass directly
 * this class but either the {@link CustomPlatformInstall} or {@link PlatformInstall}
 * @author Tomas Zezula
 * @since 1.5
 */
public abstract class GeneralPlatformInstall {

    GeneralPlatformInstall() {
    }

    /**
     * Gets the display name of the platform installer.
     * If the platform type has a single installer the display name should
     * correspond to the platform name. If there are more installers for
     * a single platform type the display name should also describe the installation process.
     * @return the display name
     */
    public abstract String getDisplayName ();
    
}
