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

package org.netbeans.modules.web.spi.webmodule;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;

/**
 * Provider interface for webmodules.
 * <p>
 * The <code>org.netbeans.modules.web.webapi</code> module registers an
 * implementation of this interface to global lookup which looks for the
 * project which owns a file (if any) and checks its lookup for this interface,
 * and if it finds an instance, delegates to it. Therefore it is not normally
 * necessary for a project type provider to register its own instance just to
 * define the webmodule for files it owns, assuming it uses projects for 
 * implementation of webmodule.
 * </p>
 * <p> If needed a new implementation of this interface can be registered in 
 * global lookup.
 * </p>
 * @see WebModule#getWebModule
 * @author Pavel Buzek
 */
public interface WebModuleProvider {
    
    /**
     * Find a webmodule containing a given file.
     * @param file a file somewhere
     * @return a webmodule, or null for no answer
     * @see WebModuleFactory
     */
    WebModule findWebModule(FileObject file);
    
}
