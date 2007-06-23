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

package org.netbeans.modules.cnd.api.model.xref;

import org.netbeans.modules.cnd.api.model.*;

/**
 * reference object in file
 * i.e. 
 * A* B::foo() {
 * }
 * => there are 3 reference objects:
 * 1) "A" pointed to class A
 * 2) "B" pointed to class B
 * 3) "foo" pointed to declaration of method foo in class B
 *
 * reference object could have owner. 
 * Owner reference is the connection between model objects and references.
 * Could be used for instance for searching the scope of reference.
 * in the example above:
 * - reference "1" has as owner return type of method definition
 * - reference "2" has owner method definition
 * - reference "3" has owner method definition as well
 *
 *TODOD: think about example
 * #define MACRO(x) #x
 * #include MACRO(file.h)
 * what are the references and owners?
 *
 * @author Vladimir Voskresensky
 */
public interface CsmReference extends CsmOffsetable {
    
    /**
     * returns referenced object
     * this could be long operation of resolving, do not call in EQ
     */
    CsmObject getReferencedObject();
    
    CsmOffsetable getOwner();
}
