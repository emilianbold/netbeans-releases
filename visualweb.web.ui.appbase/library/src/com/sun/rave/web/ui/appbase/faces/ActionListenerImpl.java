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
