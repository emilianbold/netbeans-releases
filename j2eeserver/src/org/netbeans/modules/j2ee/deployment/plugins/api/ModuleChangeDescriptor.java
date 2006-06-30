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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.io.File;

/**
 * This interface allows a plugin to find out what about a module
 * or application has changed since the last deployment.
 * @author  George Finklang
 */
public interface ModuleChangeDescriptor {
    /**
     * Return true if any of the standard or server specific deployment descriptors have changed.
     */
    public boolean descriptorChanged();

    /**
     * Return true if any of the standard or server specific deployment descriptors have changed.
     */
    public boolean serverDescriptorChanged();
    
    /**
     * Return true if any file changes require the module class loader refresh.
     */
    public boolean classesChanged();
    
    /**
     * Return true if the manifest.mf of the module has changed.
     */
    public boolean manifestChanged();

    /**
     * Returns distribution relative paths of changed files.
     */
    public File[] getChangedFiles();
}
