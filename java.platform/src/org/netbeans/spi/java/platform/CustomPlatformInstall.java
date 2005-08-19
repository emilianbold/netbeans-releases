/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
