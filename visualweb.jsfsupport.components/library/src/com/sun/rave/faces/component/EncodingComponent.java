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

package com.sun.rave.faces.component;


import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;


/**
 * <p>JavaServer Faces component that enables an application to dynamically
 * adjust the character encoding of a response, based on the current view's
 * <code>Locale</code> setting.
 */

public class EncodingComponent extends UIComponentBase {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Create a new {@link EncodingComponent} with default properties.</p>
     */
    public EncodingComponent() {

        super();
        setRendererType("com.sun.rave.faces.Encoding");                    //NOI18N

    }


    // ------------------------------------------------------ Instance Variables


    private String value = null;


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the component family to which this component belongs.</p>
     */
    public String getFamily() {

        return ("com.sun.rave.faces.Encoding");                            //NOI18N

    }


    /**
     * <p>Return the character encoding value to be used for this response.</p>
     */
    public String getValue() {

        ValueBinding vb = getValueBinding("value");                   //NOI18N
        if (vb != null) {
            return (String) vb.getValue(getFacesContext());
        } else {
            return this.value;
        }

    }


    /**
     * <p>Set the character encoding value to use for this response.</p>
     *
     * @param value New character encoding value
     */
    public void setValue(String value) {

        this.value = value;

    }


    // ---------------------------------------------------------- Public Methods


    // ----------------------------------------------------- UIComponent Methods


    // ---------------------------------------------------- StateManager Methods


    /**
     * <p>Restore the state of this component from the specified object.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param state State object from which to restore our state
     */
    public void restoreState(FacesContext context, Object state) {

        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        value = (String) values[1];

    }


    /**
     * <p>Return an object representing the saved state of this component.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    public Object saveState(FacesContext context) {

        Object values[] = new Object[2];
        values[0] = super.saveState(context);
        values[1] = value;
	return values;

    }


}
