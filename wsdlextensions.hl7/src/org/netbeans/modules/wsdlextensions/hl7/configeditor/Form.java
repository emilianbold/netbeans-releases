/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.hl7.configeditor;

import javax.swing.JComponent;

/**
 * General control interface for the various GUI "forms" (panels, dialogs, etc.)
 * that comprise the MQ implementation of the extensibility element
 * configuration editor.
 *
 * @author Vishnuvardhan P.R
 */
interface Form {
    /**
     * Signal for the form to reread its data model into its view, in effect
     * discarding uncommitted changes made thru the view.
     */
    void refresh();

    /**
     * Signal for the form to update its data model with uncommitted changes
     * made thru its view.
     */
    void commit();

    /**
     * Populate the form's internal data model with the information provided.
     * Since the supplied model type is opaque, an implementation may choose to
     * disregard it if it cannot resolve the object's information to its own
     * data model.
     *
     * @param model Some object tagged as a {@link FormModel} that is of some
     * more meaningful type that an implementation can process.
     */
    void loadModel(FormModel model);

    /**
     * Returns the form's own data model.
     *
     * @return Form data model
     */
    FormModel getModel();

    /**
     * The Swing component that represents the form's visual representation.
     *
     * @return The form's view.
     */
    JComponent getComponent();

    /** Tag interface */
    static interface FormModel {
    }
}

