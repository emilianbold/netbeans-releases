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

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface TMapVisitor {
    /**
     *  Visit "transformMap" element.
     * @param transformMap visited element
     */
    void visit(TransformMap transformMap);
    
    /**
     * Visit "service" element.
     * @param service visited element
     */
    void visit(Service service);
    
    /**
     * Visit "operation" element.
     * @param operation visited element
     */
    void visit(Operation operation);
    
    /**
     * Visit "invoke" element.
     * @param invoke visited element 
     */
    void visit(Invoke invoke);

    /**
     * Visit "transform" element.
     * @param transform visited element
     */
    void visit(Transform transform);

    /**
     * Visit "param" element.
     * @param param visited element
     */
    void visit(Param param);
}
