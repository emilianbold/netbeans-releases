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
