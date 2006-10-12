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

package org.netbeans.modules.xml.xam.dom;

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Reference;

/**
 * Represents reference to a component that can be identified globally by QName.
 *
 * @author Chris Webster
 * @author Rico Cruz
 * @author Nam Nguyen
 */
public interface NamedComponentReference<T extends NamedReferenceable> extends Reference<T> {
    
    /**
     * Returns the effective namespace of the referenced component.
     * <p>
     * Note that in case of XML schema document, the effective namespace of a 
     * component could be different when the schema is included by another schema.
     *
     * @return referenced namespace that is effective in the current document.
     */
    String getEffectiveNamespace();
    
    /**
     * Returns full QName of the referenced component if the reference is not broken.
     * If reference has not been resolved or broken, the returned QNam could be 
     * partial (only local name) and implementation dependent.
     * @return QName of the referenced component.
     */
    QName getQName();
}
