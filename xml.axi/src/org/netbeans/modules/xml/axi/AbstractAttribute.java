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
package org.netbeans.modules.xml.axi;

import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AbstractAttribute extends AXIComponent {
    
    /**
     * Creates a new instance of Attribute
     */
    public AbstractAttribute(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of Attribute
     */
    public AbstractAttribute(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    /**
     * Creates a proxy for this Attribute.
     */
    public AbstractAttribute(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    /**
     * Allows a visitor to visit this Attribute.
     */
    public abstract void accept(AXIVisitor visitor);
    
    /**
     * Returns the name.
     */
    public abstract String getName();
    
    public static final String PROP_ATTRIBUTE       = "attribute"; //NOI18N
}
