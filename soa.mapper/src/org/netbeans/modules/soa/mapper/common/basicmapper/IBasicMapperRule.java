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

package org.netbeans.modules.soa.mapper.common.basicmapper;

import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.IMapperListener;

/**
 * <p>
 *
 * Title: Mapper Rule </p> <p>
 *
 * Description: Generic Rule that listens on MapperEvent and gives
 * Permission to create Mapper common objects. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IBasicMapperRule
     extends IMapperListener {
    /**
     * Return the mapper that this rule will be applied on.
     *
     * @return   the mapper.
     */
    public IBasicMapper getMapper();


    /**
     * Return true if the specified link is a valid link for this mapper
     * to be created, false otherwise.
     *
     * @param link  the specified link to be tested
     * @return      true if link is allowed to be created, false
     *      otherwise.
     */
    public boolean isAllowToCreate(IMapperLink link);


    /**
     * Return true if the specified node is a valid group node for this
     * mapper to be created, false otherwise.
     *
     * @param node  the specified node to be tested
     * @return      true if link is allowed to be created, false
     *      otherwise.
     */
    public boolean isAllowToCreate(IMapperNode node);


    /**
     * Return true if the specified link is a valid link for this mapper
     * to be removed, false otherwise.
     *
     * @param link  the specified group node to be tested
     * @return      true if link is allowed to be removed, false
     *      otherwise.
     */
    public boolean isAllowToRemove(IMapperLink link);


    /**
     * Return true if the specified node is a valid node for this mapper
     * to be removed, false otherwise.
     *
     * @param node  the specified group node to be tested
     * @return      true if link is allowed to be removed, false
     *      otherwise.
     */
    public boolean isAllowToRemove(IMapperNode node);
}
