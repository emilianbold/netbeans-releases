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

package org.netbeans.modules.j2ee.persistence.spi;

import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.openide.filesystems.FileObject;

/**
 * Provides the entity class scope for the given file.
 *
 * @author Andrei Badea
 * @since 1.3
 */
public interface EntityClassScopeProvider {

    /**
     * Returns the entity class scope for the given file.
     *
     * @param  fo the file object to find the entity class scope for; cannot be null.
     * @return a entity class scope or null if there was no entity class
     *         scope for the given file.
     */
    public EntityClassScope findEntityClassScope(FileObject fo);
}
