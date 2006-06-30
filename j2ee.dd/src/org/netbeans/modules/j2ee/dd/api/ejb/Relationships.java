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

package org.netbeans.modules.j2ee.dd.api.ejb;

//
// This interface has all of the bean info accessor methods.
//
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface;

public interface Relationships extends CommonDDBean, DescriptionInterface {

        public static final String EJB_RELATION = "EjbRelation";	// NOI18N

        public void setEjbRelation(int index, EjbRelation value);

        public EjbRelation getEjbRelation(int index);

        public void setEjbRelation(EjbRelation[] value);

        public EjbRelation[] getEjbRelation();
        
	public int addEjbRelation(org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation value);

	public int removeEjbRelation(org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation value);

	public int sizeEjbRelation();

        public EjbRelation newEjbRelation();
        
}

