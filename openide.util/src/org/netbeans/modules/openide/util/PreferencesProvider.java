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

package org.netbeans.modules.openide.util;

import java.util.prefs.Preferences;

/**
 *
 * @author Radek Matous
 */
public interface PreferencesProvider {
    /**
     * Returns user preference node. {@link Preferences#absolutePath} of such
     * a node depends whether class provided as a parameter was loaded as a part of any module
     * or not. If so, then absolute path corresponds to slashified code name base of module.
     * If not, then absolute path corresponds to class's package.
     *
     * @param cls the class for which a user preference node is desired.
     * @return the user preference node
     */
    Preferences preferencesForModule(Class cls);
    
    /**
     * Returns the root preference node.
     *
     * @return the root preference node.
     */    
    Preferences preferencesRoot();    
}
