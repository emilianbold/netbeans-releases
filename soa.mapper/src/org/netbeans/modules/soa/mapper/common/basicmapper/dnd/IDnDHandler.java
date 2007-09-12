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

package org.netbeans.modules.soa.mapper.common.basicmapper.dnd;

import java.awt.Cursor;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetListener;

/**
 * <p>
 *
 * Title: </p>IDnDHandler <p>
 *
 * Description: </p>IDnDHandler provides convience object to override the dnd
 * on the mapper view component. <p>
 *
 * @author    Un Seng Leong
 * @created   December 23, 2002
 */

public interface IDnDHandler {

    /**
     * Gets the dragGestureListener attribute of the IDnDHandler object
     *
     * @return   The dragGestureListener value
     */
    public DragGestureListener getDragGestureListener();

    /**
     * Gets the dragSourceListener attribute of the IDnDHandler object
     *
     * @return   The dragSourceListener value
     */
    public DragSourceListener getDragSourceListener();

    /**
     * Gets the dropTargetListener attribute of the IDnDHandler object
     *
     * @return   The dropTargetListener value
     */
    public DropTargetListener getDropTargetListener();

    /**
     * Return an int representing the type of action used in this Drag
     * operation.
     *
     * @return   an int representing the type of action used in this Drag
     *      operation. See <code>java.awt.dnd.DnDConstants</code> for a list of
     *      available drag actions.
     */
    public int getDragAction();

    /**
     * Return the cursor to use when start draging on the component of this
     * handler.
     *
     * @return   the cursor to use when start draging. See <code>java.awt.dnd.DragSource</code>
     *      for a list of avaiable drag cursor.
     */
    public Cursor getDragCursor();

    /**
     * Close this handler and release any system resource.
     */
    public void releaseHandler();
}
