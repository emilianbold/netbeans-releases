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


package org.netbeans.modules.iep.model.lib;

import java.util.List;
import java.util.Map;
import java.io.Serializable;


/**
 * A sub interface of Map. It provides a List view of Map's keys, and a List
 * view of Map's values. It also allows the Map's entries be managed as a
 * list.
 *
 * @author Bing Lu
 *
 * @since May 1, 2002
 */
public interface ListMap
    extends Map, Serializable {

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    List getKeyList();

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    List getValueList();

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     * @param key DOCUMENT ME!
     * @param value DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    Object put(int index, Object key, Object value);

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    Object remove(int index);
    
    Object get(int index);
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
