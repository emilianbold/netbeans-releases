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
 * @author ads
 *
 */
public interface SortContainer extends XslComponent {

    String SORT_PROPERTY = "sort";          // NOI18N

    /**
     * @return sorts children for this template.
     */
    List<Sort> getSorts();
    
    /**
     * Add new <code>sort</code> element at <code>position</code>. 
     * @param sort new sort element.
     * @param position position for new element.
     */
    void addSort(Sort sort, int position);
    
    /**
     * Append new sort element.
     * @param sort new sort child element for appending.
     */
    void appendSort(Sort sort);
    
    /**
     * Removes existing <code>sort</code> child element.
     * @param sort sort child element.
     */
    void removeSort(Sort sort);
}
