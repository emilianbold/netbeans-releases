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

package org.netbeans.modules.mobility.cldcplatform.wizard;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.netbeans.spi.java.platform.CustomPlatformInstall;

/**
 * Installer factory for standard UEI Emulator
 *
 * @author Adam Sotona
 */
public class Installer extends CustomPlatformInstall {
    
    static Installer create() {
        return new Installer();
    }
    
    public WizardDescriptor.InstantiatingIterator createIterator() {
        return InstallerIterator.getDefault();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(Installer.class, "TXT_Installer"); // NOI18N
    }
    
}
