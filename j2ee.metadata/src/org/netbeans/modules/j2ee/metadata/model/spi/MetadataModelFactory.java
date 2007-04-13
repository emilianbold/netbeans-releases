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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.metadata.model.spi;

import org.netbeans.modules.j2ee.metadata.model.MetadataModelAccessor;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.util.Parameters;

/**
 * Provides a way to create {@link MetadataModel} instances. They cannot be
 * created directly; instead, a model provider implements the
 * {@link MetadataModelImplementation} interface and uses this class.
 *
 * @author Andrei Badea
 * @since 1.2
 */
public final class MetadataModelFactory {

    private MetadataModelFactory() {
    }

    /**
     * Creates a metadata model.
     *
     * @param  impl the instance of {@link MetadataModelImplementation} which
     *         the model will delegate to; never null.
     * @return a {@link MetadataModel} delegating to <code>impl</code>; never null.
     * @throws NullPointerException if the <code>impl</code> parameter was null.
     */
    public static <T> MetadataModel<T> createMetadataModel(MetadataModelImplementation<T> impl) {
        Parameters.notNull("impl", impl); // NOI18N
        return MetadataModelAccessor.DEFAULT.createMetadataModel(impl);
    }
}
