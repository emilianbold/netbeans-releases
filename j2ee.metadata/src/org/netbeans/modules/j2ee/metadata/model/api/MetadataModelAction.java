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

package org.netbeans.modules.j2ee.metadata.model.api;

/**
 * Represents action executed against the model.
 *
 * @author Andrei Badea
 * @since 1.2
 */
public interface MetadataModelAction<T, R> {

    /**
     * Invoked in the context of the model.
     *
     * @param  metadata the model's metadata.
     * @return any value. It will be returned by {@link MetadataModel#runReadAction}.
     * @throws Exception any <code>Exception</code>, which will be rethrown
     *         by {@link MetadataModel#runReadAction}.
     */
    R run(T metadata) throws Exception;
}
