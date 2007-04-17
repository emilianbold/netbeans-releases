/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.spi.diff;

import org.netbeans.api.diff.StreamSource;

import java.io.IOException;

/**
 * Provider for DiffControllerImpl implemetations. Providers should be registered in default lookup, in META-INF/services folder. 
 * 
 * @author Maros Sandor
 */
public abstract class DiffControllerProvider {

    /**
     * Creates a Diff Controller for supplied left and right sources.
     * 
     * @param base defines content of the Base Diff pane
     * @param modified defines content of the Modified (possibly editable) Diff pane
     * @return DiffControllerImpl implementation of the DiffControllerImpl class
     * @throws java.io.IOException when initialization of the controlloer fails (invalid sources, etc)
     */
    public abstract DiffControllerImpl createDiffController(StreamSource base, StreamSource modified) throws IOException;
}
