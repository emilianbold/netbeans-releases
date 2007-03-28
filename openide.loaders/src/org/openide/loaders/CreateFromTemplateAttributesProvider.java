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
 * Software is Sun Microsystems, Inc. 
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc. 
 */

package org.openide.loaders;

import java.util.Map;

/** This is an interface for <q>smart templating</q>.
 * Implementations of this class can be registered in the global {@link org.openide.util.Lookup}
 * and allows anyone provide additional parameters to each {@link CreateFromTemplateHandler}s
 * when a template is instantiating.
 * Read more in the <a href="@TOP@/architecture-summary.html#script">howto document</a>.
 * 
 * @author Jaroslav Tulach
 * @since 6.3
 */
public interface CreateFromTemplateAttributesProvider {
    /** Called when a template is about to be instantiated to provide additional
     * values to the {@link CreateFromTemplateHandler} that will handle the 
     * template instantiation.
     * 
     * @param template the template that is being processed
     * @param target the destition folder
     * @param name the name of the object to create
     * @return map of named objects, or null
     */
    Map<String,?> attributesFor(DataObject template, DataFolder target, String name);
}
