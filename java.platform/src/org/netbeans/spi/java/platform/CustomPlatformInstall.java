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

import org.openide.WizardDescriptor;


/**
 * Defines an API for registering custom Java platform installer. The installer
 * is responsible for instantiation of {@link JavaPlatform} through the provided
 * TemplateWizard.Iterator. If your installer selects the platform on the local disk you
 * probably don't want to use this class, the {@link PlatformInstall} class
 * creates an platform chooser for you. You want to use this class if the
 * platform is not on the local disk, eg. you want to download it from the web.
 * @author Tomas Zezula
 * @since 1.5
 */
public abstract class CustomPlatformInstall extends GeneralPlatformInstall {
    
    /**
     * Returns the {@link WizardDescriptor#InstantiatingIterator} used to install
     * the platform.
     * @return TemplateWizard.Iterator instance responsible for instantiating
     * the platform. The instantiate method of the returned iterator should
     * return the Set containing the created JavaPlatform.
     */
    public abstract WizardDescriptor.InstantiatingIterator createIterator();            
    
}
