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
import org.openide.filesystems.*;

/**
 * Defines an API for registering custom Java platform installer. The Installer
 * is responsible for recognizing the platform, through its {@link #accept} method,
 * and for instantiation itself, through the provided TemplateWizard.Iterator.
 *
 * @author Svata Dedic, Tomas Zezula
 */
public abstract class PlatformInstall extends GeneralPlatformInstall {
    /**
     * Determines whether the Recognizer recognizes a Java Platform in 
     * the passed folder. The check done by this method should be quick
     * and should not involve launching the virtual machine. The framework will
     * call a more detailed check later.
     * @return TemplateWizard.Iterator instance responsible for instantiating
     * the platform. The instantiate method of the returned iterator should
     * return the Set containing the created JavaPlatform.
     */
    public abstract WizardDescriptor.InstantiatingIterator createIterator(FileObject baseFolder);


    /**
     * Checks whether the folder actually contains the supported SDK type.
     * @param baseFolder folder where the SDK is installed
     * @return true if the PlatformInstall recognizes the folder
     */
    public abstract boolean accept(FileObject baseFolder);    

}
