/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.soa.mapper.common;

/**
 * <p>
 *
 * Title: Mapper Event </p> <p>
 *
 * Description: Generic interface describe a mapper event. Also defines basic
 * event operations of this mapper. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IMapperEvent {
    /**
     * A event type of new link request.
     */
    public static final String REQ_NEW_LINK = "MapperEvt.ReqNewLink";

    /**
     * A event type of update link request.
     */
    public static final String REQ_UPDATE_LINK = "MapperEvt.ReqUpdateLink";
    
    /**
     * A event type of update node request.
     */
    public static final String REQ_UPDATE_NODE = "MapperEvt.ReqUpdateNode";
    
    /**
     * A event type of new link request.
     */
    public static final String REQ_NEW_LINK_FROM_NODE_AT_LOCATION = "MapperEvt.ReqNewLinkFromNodeAtLocation";
    
    /**
     * A event type of new link request.
     */
    public static final String REQ_NEW_LINK_FROM_LINK_AT_LOCATION = "MapperEvt.ReqNewLinkFromLinkAtLocation";
    
    /**
     * A event type of new group node request.
     */
    public static final String REQ_NEW_NODE = "MapperEvt.ReqNewNode";

    /**
     * A event type of remove link request.
     */
    public static final String REQ_DEL_LINK = "MapperEvt.ReqDelLink";

    /**
     * A event type of remove group node request.
     */
    public static final String REQ_DEL_NODE = "MapperEvt.ReqDelNode";

    /**
     * A event type of a link has been added to the mapper, regardless added to
     * which view model.
     */
    public static final String LINK_ADDED = "MapperEvt.LinkAdded";

    /**
     * A event type of a node has been to the mapper, regardless added to which
     * view model.
     */
    public static final String NODE_ADDED = "MapperEvt.NodeAdded";

    /**
     * A event type of link has been removed from the mapper, regardless removed
     * from which view model.
     */
    public static final String LINK_DEL = "MapperEvt.LinkDel";

    /**
     * A event type of a node has been removed from the mapper, regardless
     * removed from which view model.
     */
    public static final String NODE_DEL = "MapperEvt.NodeDel";

    /**
     * Return the source view that produce this event.
     *
     * @return   the source view that produce this event.
     */
    public Object getSource();


    /**
     * Return the event type of this event.
     *
     * @return   the event type of this event.
     */
    public String getEventType();


    /**
     * Return the object that related to this event and is transferable to all
     * listeners to this event.
     *
     * @return   the object that related to this event.
     */
    public Object getTransferObject();


    /**
     * Return a description of this event.
     *
     * @return   a description of this event.
     */
    public String getDesc();
}
