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
package org.netbeans.modules.xslt.tmap.model.api;

import org.netbeans.modules.xslt.tmap.model.impl.TMapComponents;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface Param extends Nameable, ReferenceCollection {
    TMapComponents EL_TYPE = TMapComponents.PARAM;

    String TYPE = "type"; // NOI18N

    String VALUE = "value"; // NOI18N

    String CONTENT = "content"; // NOI18N property to describe text value of param element

    ParamType getType();

    void setType(ParamType type);

    String getValue();

    void removeValue();

    void setValue(String value);

    VariableReference getVariableReference();

    void setVariableReference(VariableReference varRef);

    void setLiteralValue(String value);

    String getLiteralValue();

    void setContent(String content);
    
    void removeContent();

    String getContent();

}
