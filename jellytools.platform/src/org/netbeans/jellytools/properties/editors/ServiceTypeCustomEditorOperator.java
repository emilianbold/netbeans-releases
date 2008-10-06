/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.jellytools.properties.editors;

/*
 * ServiceTypeCustomEditorOperator.java
 *
 * Created on 6/12/02 4:39 PM
 */

import javax.swing.JDialog;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling Service Type Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class ServiceTypeCustomEditorOperator extends NbDialogOperator {

    /** Creates new ServiceTypeCustomEditorOperator
     * @throws TimeoutExpiredException when NbDialog not found
     * @param title String title of custom editor */
    public ServiceTypeCustomEditorOperator(String title) {
        super(title);
    }

    /** creates new ServiceTypeCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public ServiceTypeCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    private JButtonOperator _btDefault;
    private JSplitPaneOperator _splitPane;
    private JListOperator _lstServices;
    private PropertySheetOperator _propertySheet;

    /** Tries to find "Default" JButton in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JButtonOperator
     */
    public JButtonOperator btDefault() {
        if (_btDefault==null) {
            _btDefault = new JButtonOperator( this, Bundle.getString("org.netbeans.beaninfo.editors.Bundle", "LAB_DefaultServiceType"));
        }
        return _btDefault;
    }

    /** getter for JSplitPaneOperator
     * @return JSplitPaneOperator */    
    public JSplitPaneOperator splitPane() {
        if (_splitPane==null) {
            _splitPane=new JSplitPaneOperator(this);
        }
        return _splitPane;
    }
    
    /** Tries to find null ListView$NbList in this dialog.
     * @throws TimeoutExpiredException when component not found
     * @return JListOperator
     */
    public JListOperator lstServices() {
        if (_lstServices==null) {
            _lstServices = new JListOperator(splitPane());
        }
        return _lstServices;
    }

    /** getter for PropertySheetOperator
     * @return PropertySheetOperator */    
    public PropertySheetOperator propertySheet() {
        if (_propertySheet==null) {
            _propertySheet=new PropertySheetOperator(splitPane());
        }
        return _propertySheet;
    }
    
    /** clicks on "Default" JButton
     * @throws TimeoutExpiredException when JButton not found
     */
    public void setDefault() {
        btDefault().push();
    }
    
    /** getter for selected service type name
     * @return String service type name */    
    public String getServiceTypeValue() {
        Object o=lstServices().getSelectedValue();
        if (o!=null) return o.toString();
        return null;
    }
    
    /** setter for service type name
     * @param serviceName String name of service type to be set */    
    public void setServiceTypeValue(String serviceName) {
        lstServices().selectItem(serviceName);
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btDefault();
        lstServices();
        propertySheet();
    }
}

