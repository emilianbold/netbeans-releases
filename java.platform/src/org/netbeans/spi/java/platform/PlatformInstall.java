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
import org.openide.filesystems.FileObject;

/**
 * Defines an API for registering custom Java platform installer. The Installer
 * is responsible for recognizing the platform, through its {@link #accept} method,
 * and for instantiation itself, through the provided wizard iterator.
 *
 * @author Svata Dedic, Tomas Zezula
 */
public abstract class PlatformInstall extends GeneralPlatformInstall {
    /**
     * XXX Javadoc for this method is completely inadequate. What does it do?
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
     * Checks whether a given folder contains a platform of the supported type.
     * @param baseFolder folder which may be an installation root of a platform
     * @return true if the folder is recognized
     */
    public abstract boolean accept(FileObject baseFolder);    

}
