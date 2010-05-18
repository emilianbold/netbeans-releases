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
package org.netbeans.modules.vmd.api.model;

import java.util.Collection;

/**
 * This interface is used for notifying and processing event firing.
 * For registering the implemenation see AccessControllerFactory interface.
 * <p>
 * Event firing is following these steps: For all registered AccessController classes a writeAccess method is called
 * by nested calls (passing the calls using Runnable). In the deepest Runnable: notifyEventFiring method is called
 * on all registered AccessController classes, then performed all event firing for all listeners, and finally
 * notifyEventFired method is called on all registered AccessController classes.
 * <p>
 * This interface is used to listen on document change and you need to lock additional lock or mutex
 * or some initialization/finalization code must performed before/after the event firing.
 * <p>
 * NOTE: Be aware that the creation of this class is called when a DesignDocument is creating,
 * do not perform any action which may call/use DesignDocument since the document is not initialized yet.
 *
 * @author David Kaspar
 */
public interface AccessController {

    /**
     * Called when the event-firing is going to happen. The firing is performed by a Runnable.
     * Using this method you are able to wrap the runnable code into your and perform your code before and after.
     * Usually this is used for performing event firing in write access on a mutex.
     * <p>
     * Note: Runnable.run method that is passed as an argument must be executed exactly once.
     * @param runnable the runnable
     */
    public void writeAccess (Runnable runnable);

    /**
     * This is called immediately before an event is fired. At that time writeAccess method is called on all AccessControllers.
     * @param event the event that is going to be fired
     */
    public void notifyEventFiring (DesignEvent event);

    /**
     * This is called immediately after an event is fired. At that time writeAccess method is called on all AccessControllers.
     * @param event the event that was fired
     */
    public void notifyEventFired (DesignEvent event);

    /**
     * This is called during event firing process to notify that new components are created in a document.
     * At the time of this method is called, all presenters are notified about their removing/adding and no event is fired
     * to any design listener yet.
     * @param createdComponents the newly created components
     */
    public void notifyComponentsCreated (Collection<DesignComponent> createdComponents);

}
