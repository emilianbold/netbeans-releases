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

package org.netbeans.lib.editor.codetemplates.spi;

import java.util.Collection;

/**
 * Factory constructs code template processor for a given insert request.
 * <br/>
 * The factory instances are looked up
 * by {@link org.netbeans.api.editor.mimelookup.MimeLookup}
 * so they should be registered in an xml-layer in
 * <i>Editors/&lt;mime-type&gt;/CodeTemplateProcessorFactories</i> directory.
 *
 * @author Miloslav Metelka
 */
public interface CodeTemplateProcessorFactory {

    /**
     * Create an instance of code template processor for a given insert request.
     *
     * @param request non-null code template insert request to be processed
     *  by the given processor instance.
     * @return non-null instance of the processor. The constructed instance
     *  should be given the insert request so that it can operate on it.
     */
    CodeTemplateProcessor createProcessor(CodeTemplateInsertRequest request);
    
}
