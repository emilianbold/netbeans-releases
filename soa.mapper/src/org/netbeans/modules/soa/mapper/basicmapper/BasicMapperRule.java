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

package org.netbeans.modules.soa.mapper.basicmapper;

import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperRule;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;

/**
 * <p>
 *
 * Title: </p> BasicMapperRule<p>
 *
 * Description: </p> BasicMapperRule provides the basic implemenation for Mapper rule that
 * listener on the mapper events and calls the coorsponding allowXXX methods<p>
 *
 * @author Un Seng Leong
 */
public class BasicMapperRule
         implements IBasicMapperRule {

    /**
     * the mapper that this listens on
     */
    private IBasicMapper mMapper;


    /**
     * Creates a new BasicMapperRule object.
     *
     * @param mapper the mapper that this listens on
     */
    public BasicMapperRule(IBasicMapper mapper) {
        mMapper = mapper;
    }


    /**
     * Return the mapper that this listens on
     *
     * @return the mapper that this listens on
     */
    public IBasicMapper getMapper() {
        return mMapper;
    }


    /**
     * Return true if the specified link is a valid link for this mapper
     * to be created, false otherwise.
     *
     * @param link  the specified link to be tested
     * @return      true if link is allowed to be created, false
     *      otherwise.
     */
    public boolean isAllowToCreate(IMapperLink link) {
        return ((link != null) && (link.getStartNode() != null)
                && (link.getEndNode() != null));
    }


    /**
     * Return true if the specified node is a valid group node for this
     * mapper to be created, false otherwise.
     *
     * @param node  the specified node to be tested
     * @return      true if link is allowed to be created, false
     *      otherwise.
     */
    public boolean isAllowToCreate(IMapperNode node) {
        return (node != null);
    }


    /**
     * Return true if the specified link is a valid link for this mapper
     * to be removed, false otherwise.
     *
     * @param link  the specified group node to be tested
     * @return      true if link is allowed to be removed, false
     *      otherwise.
     */
    public boolean isAllowToRemove(IMapperLink link) {
        return (link != null);
    }


    /**
     * Return true if the specified node is a valid node for this mapper
     * to be removed, false otherwise.
     *
     * @param node  the specified group node to be tested
     * @return      true if link is allowed to be removed, false
     *      otherwise.
     */
    public boolean isAllowToRemove(IMapperNode node) {
        return (node != null);
    }


    /**
     * Invoke an mapper event, this methods calls isAllowToXXX according to the event type.
     *
     * @param e the mapper event object
     */
    public void eventInvoked(IMapperEvent e) {
        Object eventObject = e.getTransferObject();

        if (e.getEventType().equals(IMapperEvent.REQ_NEW_LINK)
                && isAllowToCreate((IMapperLink) eventObject)) {
            mMapper.addLink((IMapperLink) eventObject);
        } else if (e.getEventType().equals(IMapperEvent.REQ_NEW_NODE)
                && isAllowToCreate((IMapperNode) eventObject)) {
            mMapper.addNode((IMapperNode) eventObject);
        } else if (e.getEventType().equals(IMapperEvent.REQ_DEL_LINK)
                && isAllowToRemove((IMapperLink) eventObject)) {
            mMapper.removeLink((IMapperLink) eventObject);
        } else if (e.getEventType().equals(IMapperEvent.REQ_DEL_NODE)
                && isAllowToRemove((IMapperNode) eventObject)) {
            mMapper.removeNode((IMapperNode) eventObject);
        }
    }
}
