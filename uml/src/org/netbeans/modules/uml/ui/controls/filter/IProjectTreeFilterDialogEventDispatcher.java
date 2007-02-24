/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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



package org.netbeans.modules.uml.ui.controls.filter;

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;

/**
 * The dispatcher used to fire project tree filter events.
 *
 * @author Trey Spiva
 */
public interface IProjectTreeFilterDialogEventDispatcher
   extends IEventDispatcher
{
   /**
    * Register for the IProjectTreeFilterDialogEventsSink events.
    *
    * @param sink The listener to be notified of events.
    */
   public void registerProjectTreeFilterDialogEvents(IProjectTreeFilterDialogEventsSink sink);
   
   /**
    * Revoke a IProjectTreeFilterDialogEventsSink from the event dispatcher.
    * The specified sink will no longer receive IProjectTreeFilterDialogEventsSink
    * events.
    * 
    * @param sink The listener to be notified of events.
    */
   public void revokeProjectTreeFilterDialogEvents(IProjectTreeFilterDialogEventsSink sink);
   
   /**
    * Fires the onProjectTreeFilterDialogInit to all registered 
    * IProjectTreeFilterDialogEventsSink sinkes.
    * 
    * @param dialog The filter dialog to be initialized.
    * @param payload The event payload.
    */
   public void fireProjectTreeFilterDialogInit(IFilterDialog dialog, IEventPayload payload);
   
   /**
    * Fires the onProjectTreeFilterDialogOKActivated to all registered 
    * IProjectTreeFilterDialogEventsSink sinkes.
    * 
    * @param dialog The filter dialog to be initialized.
    * @param payload The event payload.
    */
   public void fireProjectTreeFilterDialogOKActivated(IFilterDialog dialog, IEventPayload payload);
}
