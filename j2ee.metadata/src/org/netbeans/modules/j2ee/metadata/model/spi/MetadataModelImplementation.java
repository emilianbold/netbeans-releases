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

import java.io.IOException;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;

/**
 * The SPI for <code>MetadataModel</code>.
 *
 * @see MetadataModelFactory
 *
 * @author Andrei Badea
 * @since 1.2
 */
public interface MetadataModelImplementation<T> {

    /**
     * Corresponds to {@link org.netbeans.modules.j2ee.metadata.model.api.MetadataModel#runReadAction}.
     *
     * @param  action the action to be executed; never null.
     * @return the value returned by the action's {@link MetadataModelAction#run} method.
     * @throws MetadataModelException if the action's <code>run()</code> method
     *         threw a checked exception.
     * @throws IOException if an error occured while reading the model from its storage.
     */
    <R> R runReadAction(MetadataModelAction<T, R> action) throws MetadataModelException, IOException;
}
