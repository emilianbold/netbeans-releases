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
package org.netbeans.modules.websvc.axis2.actions;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.modules.websvc.axis2.AxisUtils;
import org.netbeans.modules.websvc.axis2.config.model.Service;
import org.netbeans.modules.websvc.axis2.nodes.OperationInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class TestOperationAction extends NodeAction  {
    
    public String getName() {
        return NbBundle.getMessage(TestOperationAction.class, "LBL_TestWsOperation");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
        
    @Override
    protected boolean asynchronous() {
        return true;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes==null || activatedNodes.length != 1) return false;
        Service service = activatedNodes[0].getLookup().lookup(Service.class);  
        if (service != null && service.getWsdlUrl() == null) {
            return true;
        } else {
            return false;
        }
    }
    
    protected void performAction(Node[] activatedNodes) {
        OperationInfo operationInfo = activatedNodes[0].getLookup().lookup(OperationInfo.class);
        Service service = activatedNodes[0].getLookup().lookup(Service.class);
        Preferences prefs = AxisUtils.getPreferences();
        String axisUrl = prefs.get("AXIS_URL", "").trim();
        if (axisUrl.length() > 0) {
            // open tester page in browser
            AxisUtils.openInBrowser(getOperationQueryString(axisUrl, service, operationInfo));
        } else {
            String message = NbBundle.getMessage(TestOperationAction.class, "TXT_AxisUrlMissing");
            NotifyDescriptor dialog = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notify(dialog);
        }
    }
    
    private String getOperationQueryString(String axisUrl, Service service, OperationInfo operation) {
        StringBuffer buf = new StringBuffer();
        List<String> paramNames = operation.getParamNames();
        List<String> paramTypes = operation.getParamTypes();
        for (int i = 0;i<paramNames.size();i++) {
            if (i>0) buf.append('&'); //NOI18N
            buf.append(paramNames.get(i)+"="+getSampleValue(paramTypes.get(i))); //NOI18N
        }
        String queries = buf.toString();
        return axisUrl+"/services/"+service.getNameAttr()+"/"+operation.getOperationName()+(queries.length() == 0 ? "":"?"+queries); //NOI18N
    }
    
    private String getSampleValue(String paramType) {
        if ("java.lang.String".equals(paramType)) { //NOI18N
            return "XYZ"; //NOI18N
        } else if (paramType.matches("int|long|short|byte|java.lang.Integer|java.lang.Long|java.lang.Short|java.lang.Byte|java.math.BigInteger")) { //NOI18N
            return "0"; //NOI18N
        } else if (paramType.matches("float|double|java.lang.Float|java.lang.Double|java.math.BigDecimal")) { //NOI18N
            return "0.0"; //NOI18N
        } else if (paramType.matches("boolean|java.lang.Boolean")) { //NOI18N
            return "false"; //NOI18N
        } else return "null"; //NOI18N
    }

}
