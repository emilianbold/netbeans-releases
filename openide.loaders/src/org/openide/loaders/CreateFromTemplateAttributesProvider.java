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

/**
 * Implementations of this class can be registered in the global {@link org.openide.util.Lookup}
 * and allows anyone provide additional parameters to each {@link CreateFromTemplateHandler}s
 * when a template is instantiating.
 * 
 * @author Jaroslav Tulach
 */
public abstract class CreateFromTemplateAttributesProvider {
    protected abstract Map<String,? extends Object> attributesFor(DataObject template, DataFolder target, String name);
}
