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
 * Created on Jun 14, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.commands;

import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.api.property.PropertyAdapter;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class ConstraintNamedPropertyAdapter extends PropertyAdapter implements NamedPropertyAdapter {

    
    public ConstraintNamedPropertyAdapter(WSDLComponent delegate) {
        super(delegate);
    }
    
    public abstract boolean isNameExists(String name);
    
    public void setName(String name) {
        WSDLComponent comp = getDelegate();
        if (comp instanceof NamedReferenceable){
            NamedReferenceable ref = NamedReferenceable.class.cast(comp);
            if (ref != null) {
                if (!ref.getName().equals(name)) { 
                    // try rename silent and locally
                    SharedUtils.locallyRenameRefactor((Nameable)ref, name);
                }
            }
        } else if (comp instanceof Nameable) {
            Nameable nameable = Nameable.class.cast(comp);
            if (!nameable.getName().equals(name)) {
                getDelegate().getModel().startTransaction();
                nameable.setName(name);
                getDelegate().getModel().endTransaction();
            }
        }
        
    }

    public String getName() {
        WSDLComponent comp = getDelegate();
        String name = null;
        if (comp instanceof NamedReferenceable){
            name = NamedReferenceable.class.cast(comp).getName();
        } else if (comp instanceof Nameable) {
            name = Nameable.class.cast(comp).getName();
        }
        if (name == null) {
            return "";
        }
        return name;
    }

    
}
