/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.wizard.components.actions;

import java.io.File;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Kirill Sorokin
 */
public class SetInstallationLocationDepAction extends WizardAction {
    public static final String SOURCE_PROPERTY = 
            "source.location.property";//NOI18N
    public static final String RELATIVE_LOCATION_PROPERTY = 
            "relative.location";//NOI18N
    
    public void execute() {
        final String prop              = getProperty(SOURCE_PROPERTY);
        final String relativeLocation  = getProperty(RELATIVE_LOCATION_PROPERTY);
        
        if (prop == null) {
            ErrorManager.notifyError("Source location property is not set");
            return;
        }
        
        // we do expect the property container of the wizard to be a product, if
        // it's not we should fail
        final Product product = (Product) getWizard().getContext().get(Product.class);
        
        
        if (product == null) {
            ErrorManager.notifyError("Running this action outside of product context is impossible");
            return;
        }
        
        final File sourceLocation = new File(product.getProperty(prop));
        
        final File location;
        if (relativeLocation != null) {
            location = new File(sourceLocation, relativeLocation);
        } else {
            location = sourceLocation;
        }
        
        product.setInstallationLocation(location.getAbsoluteFile());
    }
    
    @Override
    public WizardActionUi getWizardUi() {
        return null; // we do not have any ui for this action
    }
    
    public boolean isCancellable() {
        return false;
    }
    
}
