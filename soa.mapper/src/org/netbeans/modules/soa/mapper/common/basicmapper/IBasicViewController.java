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

import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IDnDCustomizer;
import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IDnDHandler;
import org.netbeans.modules.soa.mapper.common.IMapperViewController;

/**
 * <p>
 *
 * Title: </p>IBasicViewController <p>
 *
 * Description: </p>IBasicViewController provides an extension of the IMapperViewController
 * to controller the behavior of the IMapperView <p>
 *
 * @author    Un Seng Leong
 * @created   December 23, 2002
 */
public interface IBasicViewController
    extends IMapperViewController {

    /**
     * Set the main mapper controller.
     *
     * @param controller  the mapper controller
     */
    public void setMapperController(IBasicController controller);

    /**
     * Return the mapper controller.
     *
     * @return   the mapper controller.
     */
    public IBasicController getMapperController();

    /**
     * Set the dnd handler for this view to hanlder dnd operations.
     *
     * @param handler  the dnd handler for this mapper to hanlder dnd
     *      operations.
     */
    public void setDnDHandler(IDnDHandler handler);

    /**
     * Returns the dnd handler for this view to hanlder dnd operations.
     *
     * @return   the dnd handler for this mapper to hanlder dnd operations.
     */
    public IDnDHandler getDnDHandler();
    
    /**
     * Set the dnd customizer for this view to customize dnd operations.
     *
     * @param handler  the dnd handler for this mapper to hanlder dnd
     *      operations.
     */
    public void setDnDCustomizer(IDnDCustomizer customizer);

    /**
     * Returns the dnd customizer for this view to customize dnd operations.
     *
     * @return   the dnd handler for this mapper to hanlder dnd operations.
     */
    public IDnDCustomizer getDnDCustomizer();
}
