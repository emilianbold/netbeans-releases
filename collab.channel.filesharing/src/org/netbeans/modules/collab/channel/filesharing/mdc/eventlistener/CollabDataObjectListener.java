/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener;

import org.openide.*;
import org.openide.loaders.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.event.*;


/**
 *
 * @author  Owner
 */
public class CollabDataObjectListener extends Object implements OperationListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* notifier */
    protected EventNotifier eventNotifier = null;

    /** Creates a new instance of DataObjectChangeListener */
    public CollabDataObjectListener(EventNotifier eventNotifier) {
        super();
        this.eventNotifier = eventNotifier;
    }

    /** Object has been recognized by
    * {@link DataLoaderPool#findDataObject}.
    * This allows listeners
    * to attach additional cookies, etc.
    *
    * @param ev event describing the action
    */
    public void operationPostCreate(OperationEvent ev) {
        //Do nothing
    }

    /** Object has been successfully copied.
    * @param ev event describing the action
    */
    public void operationCopy(OperationEvent.Copy ev) {
        takeAction(ev);
    }

    /** Object has been successfully moved.
    * @param ev event describing the action
    */
    public void operationMove(OperationEvent.Move ev) {
        takeAction(ev);
    }

    /** Object has been successfully deleted.
    * @param ev event describing the action
    */
    public void operationDelete(OperationEvent ev) {
        //Do nothing
    }

    /** Object has been successfully renamed.
    * @param ev event describing the action
    */
    public void operationRename(OperationEvent.Rename ev) {
        //Do nothing
    }

    /** A shadow of a data object has been created.
    * @param ev event describing the action
    */
    public void operationCreateShadow(OperationEvent.Copy ev) {
        //Do nothing
    }

    /** New instance of an object has been created.
    * @param ev event describing the action
    */
    public void operationCreateFromTemplate(OperationEvent.Copy ev) {
        takeAction(ev);
    }

    /** Object has been successfully copied.
    * @param ev event describing the action
    */
    private void takeAction(Object ev) {
        // I don't have time to fix this before code freeze; instead, I'm
        // going to make sure no one can use it.  This shouldn't be a problem
        // given that we don't use this method in the filesharing channel.
        if (true) {
            throw new RuntimeException(
                "This implementation may not be " + "safe; this message is a stopgap measure.  Contact todd.fast@" +
                "sun.com for complete info"
            );
        }

        if (ev instanceof OperationEvent.Copy || ev instanceof OperationEvent.Move) {
            Object obj = null;

            if (ev instanceof OperationEvent.Copy) {
                OperationEvent.Copy ev1 = (OperationEvent.Copy) ev;
                obj = ev1.getObject();
            } else {
                OperationEvent.Move ev1 = (OperationEvent.Move) ev;
                obj = ev1.getObject();
            }

            String fileGroupName = null;

            if (obj instanceof DataObject) {
                DataObject dataObject = (DataObject) obj;
                fileGroupName = dataObject.getName();

                EventContext evContext = new EventContext(DataObjectCreated.getEventID(), dataObject);
                CollabEvent ce = new DataObjectCreated(evContext);

                try {
                    eventNotifier.notify(ce);
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
    }
}
