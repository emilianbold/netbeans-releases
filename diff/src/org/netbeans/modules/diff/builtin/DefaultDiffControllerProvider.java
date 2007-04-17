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
package org.netbeans.modules.diff.builtin;

import org.netbeans.spi.diff.DiffControllerProvider;
import org.netbeans.spi.diff.DiffControllerImpl;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.diff.builtin.visualizer.editable.EditableDiffView;

import java.io.IOException;

/**
 * Default implementation of DiffControllerProvider.
 * 
 * @author Maros Sandor
 */
public class DefaultDiffControllerProvider extends DiffControllerProvider {

    public DiffControllerImpl createDiffController(StreamSource base, StreamSource modified) throws IOException {
        return new EditableDiffView(base, modified);
    }
}
