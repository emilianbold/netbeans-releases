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

package org.netbeans.modules.j2me.cdc.project.semc;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;

/**
 *
 * @author Adam Sotona
 */
public class SEMCPropertiesDescriptor implements ProjectPropertiesDescriptor {
        
    public static final String SEMC_APPLICATION_ICON_SPLASH_INSTALLONLY = "semc.application.icon.splash.installonly"; //NOI18N
    public static final String SEMC_APPLICATION_UID = "semc.application.uid"; //NOI18N

    private Reference<Set<PropertyDescriptor>> ref = new WeakReference(null);
    
    /** Creates a new instance of SEMCPropertiesDescriptor */
    public SEMCPropertiesDescriptor() {
    }
    
    public Set<PropertyDescriptor> getPropertyDescriptors() {
        Set<PropertyDescriptor> set = ref.get();
        if (set == null) {
            set = new HashSet();
            set.add(new PropertyDescriptor(SEMC_APPLICATION_ICON_SPLASH_INSTALLONLY, true, DefaultPropertyParsers.BOOLEAN_PARSER,  "false")); //NOI18N
            set.add(new PropertyDescriptor(SEMC_APPLICATION_UID, true, DefaultPropertyParsers.STRING_PARSER,  "")); //NOI18N
            ref = new WeakReference(set);
        }
        return set;
    }

}
