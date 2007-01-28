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

package com.sun.rave.web.ui.appbase.faces;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

/**
 * <p>ActionListener implementation that cooperates with the exception caching
 * strategy we implement in {@link ViewHandlerImpl}.</p>
 */
public class ActionListenerImpl implements ActionListener {


    // ------------------------------------------------------------ Constructor

    /**
     * <p>Create a new <code>ActionListener</code> instance that wraps the
     * specified one.</p>
     *
     * @param handler Original <code>ActionListener</code> provided by JSF
     */
    public ActionListenerImpl(ActionListener handler) {

        this.handler = handler;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The original <code>ActionListener</code> to whom we delegate.</p>
     */
     private ActionListener handler = null;


     // ------------------------------------------------ ActionListener Methods


     /**
      * <p>Handle the event, swallowing and caching any exception that is
      * thrown during the handling.</p>
      *
      * @param event <code>ActionEvent</code> to be handled
      */
     public void processAction(ActionEvent event) {

        try {
            handler.processAction(event);
        } catch (RuntimeException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().log(e.getMessage(), e);
            ViewHandlerImpl.cache(context, e);
        }

     }

}
