/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates.spi;

import java.util.Collection;

/**
 * Factory constructs code template processor for a given insert request.
 * <br/>
 * The factory instances are lookup-ed
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
