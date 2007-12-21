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

package org.netbeans.modules.xslt.model;

import java.util.List;


/**
 * Container for "param" elements.
 * 
 * @author ads
 *
 */
public interface ParamContainer {
    
    String PARAM_PROPERTY = "param";        // NOI18N

    /**
     * @return params children.
     * Note that resulting collection is unmodifiable. 
     */
    List<Param> getParams();
    
    /**
     * Add new <code>param</code> element at <code>position</code>. 
     * @param param new param element.
     * @param position position for new element.
     */
    void addParam(Param param, int position);
    
    /**
     * Append new param element.
     * @param param new param child element for appending.
     */
    void appendParam(Param param);
    
    /**
     * Removes existing <code>param</code> child element.
     * @param param param child element.
     */
    void removeParam(Param param);
}
