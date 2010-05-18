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
/*
 * Created on Jun 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.netbeans.modules.visualweb.faces.dt.std;

import java.awt.Component;
import javax.faces.application.Application;
//import com.sun.jsfcl.std.property.LocalizedMessageRuntimeException;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.faces.FacesDesignContext;
import org.netbeans.modules.visualweb.propertyeditors.IllegalTextArgumentException;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ValueBindingOnlyPropertyEditor extends ValueBindingPropertyEditor {

    protected static final ComponentBundle bundle = ComponentBundle.getBundle(ValueBindingPanel.class);

    public Component getCustomEditor() {

        ValueBindingOnlyPanel panel = new ValueBindingOnlyPanel(this, liveProperty);
        return panel;
    }

    /*
     * The only diff with super's version is that if we receive anything but blank and
     * value binding expression, we throw an IllegalArgumentException.
     *  (non-Javadoc)
     * @see java.beans.PropertyEditor#setAsText(java.lang.String)
     */

    public void setAsText(String text) throws IllegalArgumentException {

        verifyString(text);
        if (text.startsWith("#{")) { //NOI18N
            FacesDesignContext fctx = (FacesDesignContext)liveProperty.getDesignBean().getDesignContext();
            Application app = fctx.getFacesContext().getApplication();
            super.setValue(app.createValueBinding(text));
        } else if (text.trim().length() == 0) {
            superSetValue(null);
        }
    }

    public void verifyString(String string) {

        if (string.startsWith("#{")) { //NOI18N
            return;
        } else if (string.trim().length() == 0) {
            return;
        }
        throw new IllegalTextArgumentException(bundle.getMessage("vbExpected", string)); // NOI18N
    }
}
