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
package org.netbeans.modules.websvc.saas.ui.nodes;

//import com.sun.tools.ws.processor.model.java.JavaMethod;
//import com.sun.tools.ws.processor.model.java.JavaParameter;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaParameter;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.netbeans.modules.websvc.saas.spi.MethodNodeActionsProvider;
import org.netbeans.modules.websvc.saas.util.SaasTransferable;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.netbeans.modules.websvc.saas.util.TypeUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author nam
 */
public class WsdlMethodNode extends AbstractNode {

    WsdlSaasMethod method;
    private Transferable transferable;

    public WsdlMethodNode(WsdlSaasMethod method) {
        this(method, new InstanceContent());
    }

    protected WsdlMethodNode(WsdlSaasMethod method, InstanceContent content) {
        super(Children.LEAF, new AbstractLookup(content));
        this.method = method;
        content.add(method);
        transferable = ExTransferable.create(
                new SaasTransferable<WsdlSaasMethod>(method, SaasTransferable.WSDL_METHOD_FLAVORS));
    }

    @Override
    public String getDisplayName() {
        return method.getName();
    }

    @Override
    public String getShortDescription() {
        JavaMethod javaMethod = method.getJavaMethod();
        String signature = "";
        if (javaMethod != null) {
            signature = javaMethod.getReturnType().getFormalName() + " " + javaMethod.getName() + "(";
            Iterator parameterIterator = javaMethod.getParametersList().iterator();
            JavaParameter currentParam = null;
            while (parameterIterator.hasNext()) {
                currentParam = (JavaParameter) parameterIterator.next();
                String parameterType = TypeUtil.getParameterType(currentParam);
                signature += parameterType + " " + currentParam.getName();
                if (parameterIterator.hasNext()) {
                    signature += ", ";
                }
            }

        }
        else{
            WSOperation wsOperation = method.getWsdlOperation();
            signature = wsOperation.getName() + "(";
            ListIterator<? extends WSParameter> iterator = wsOperation.getParameters().listIterator();
            while(iterator.hasNext()){
                WSParameter parameter =iterator.next();
                signature +=  parameter.getName();
                if(iterator.hasNext()){
                    signature += ", ";
                }
            }
        }
        signature += ")";
        return signature;
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = SaasNode.getActions(getLookup());
        for (MethodNodeActionsProvider ext : SaasUtil.getMethodNodeActionsProviders()) {
            for (Action a : ext.getMethodActions(getLookup())) {
                actions.add(a);
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }

    public Action getPreferredAction() {
        Action[] actions = getActions(true);
        return actions.length > 0 ? actions[0] : null;
    }

    public Image getIcon(int type) {
        return getMethodIcon();
    }

    public Image getOpenedIcon(int type) {
        return getMethodIcon();
    }

    private Image getMethodIcon() {
        JavaMethod javaMethod = method.getJavaMethod();
        if (javaMethod != null && !"void".equals(javaMethod.getReturnType().getRealName())) {
            Image image1 = ImageUtilities.loadImage("org/netbeans/modules/websvc/manager/resources/methodicon.png");
            Image image2 = ImageUtilities.loadImage("org/netbeans/modules/websvc/manager/resources/table_dp_badge.png");
            int x = image1.getWidth(null) - image2.getWidth(null);
            int y = image1.getHeight(null) - image2.getHeight(null);
            return ImageUtilities.mergeImages(image1, image2, x, y);
        } else {
            return ImageUtilities.loadImage("org/netbeans/modules/websvc/saas/ui/resources/methodicon.png");
        }
    }

    /**
     * Create a property sheet for the individual method node
     * @return property sheet for the data source nodes
     */
    protected Sheet createSheet() {
        JavaMethod javaMethod = method.getJavaMethod();
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("data"); // NOI18N

        if (ss == null) {
            ss = new Set();
            ss.setName("data");  // NOI18N
            ss.setDisplayName(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_INFO"));
            ss.setShortDescription(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_INFO"));
            sheet.put(ss);
        }

        if (javaMethod == null) {
            return sheet;
        }

        try {
            Reflection p;

            p = new Reflection(javaMethod, String.class, "getName", null); // NOI18N
            p.setName("name"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_NAME"));
            p.setShortDescription(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_NAME"));
            ss.put(p);
            String signature = javaMethod.getReturnType().getRealName() + " " +
                    javaMethod.getName() + "(";

            Iterator tempIterator = javaMethod.getParametersList().iterator();
            JavaParameter currentparam = null;
            while (tempIterator.hasNext()) {
                currentparam = (JavaParameter) tempIterator.next();
                signature += currentparam.getType().getRealName() + " " + currentparam.getName();
                if (tempIterator.hasNext()) {
                    signature += ", ";
                }
            }

            signature += ")";

            Iterator excpIterator = javaMethod.getExceptions();
            if (excpIterator.hasNext()) {
                signature += " throws";
                while (excpIterator.hasNext()) {
                    String currentExcp = (String) excpIterator.next();
                    signature += " " + currentExcp;
                    if (excpIterator.hasNext()) {
                        signature += ",";
                    }
                }


            }

            p = new Reflection(signature, String.class, "toString", null); // NOI18N
            p.setName("signature"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_SIGNATURE"));
            p.setShortDescription(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_SIGNATURE"));
            ss.put(p);

            p = new Reflection(javaMethod.getReturnType(), String.class, "getRealName", null); // NOI18N
            p.setName("returntype"); // NOI18N
            p.setDisplayName(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_RETURNTYPE"));
            p.setShortDescription(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_RETURNTYPE"));
            ss.put(p);

            Set paramSet = sheet.get("parameters"); // NOI18N
            if (paramSet == null) {
                paramSet = new Sheet.Set();
                paramSet.setName("parameters"); // NOI18N
                paramSet.setDisplayName(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_PARAMDIVIDER")); // NOI18N
                paramSet.setShortDescription(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_PARAMDIVIDER")); // NOI18N
                sheet.put(paramSet);
            }
            Iterator paramIterator = javaMethod.getParametersList().iterator();
            if (paramIterator.hasNext()) {
                p = new Reflection(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_PARAMTYPE"),
                        String.class,
                        "toString",
                        null); // NOI18N
                p.setName("paramdivider2"); // NOI18N

                JavaParameter currentParameter = null;
                for (int ii = 0; paramIterator.hasNext(); ii++) {
                    currentParameter = (JavaParameter) paramIterator.next();
                    if (currentParameter.getType().isHolder()) {
                        p = new Reflection(TypeUtil.getParameterType(currentParameter), String.class, "toString", null); // NOI18N
                    } else {
                        p = new Reflection(currentParameter.getType(), String.class, "getRealName", null); // NOI18N
                    }
                    p.setName("paramname" + ii); // NOI18N
                    p.setDisplayName(currentParameter.getName());
                    p.setShortDescription(currentParameter.getName() + "-" +
                            currentParameter.getType().getRealName());
                    paramSet.put(p);
                }
            }
            Set exceptionSet = sheet.get("exceptions"); // NOI18N
            if (exceptionSet == null) {
                exceptionSet = new Sheet.Set();
                exceptionSet.setName("exceptions"); // NOI18N
                exceptionSet.setDisplayName(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_EXCEPTIONDIVIDER")); // NOI18N
                exceptionSet.setShortDescription(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_EXCEPTIONDIVIDER")); // NOI18N
                sheet.put(exceptionSet);
            }

            Iterator exceptionIterator = javaMethod.getExceptions();
            String currentException = null;
            for (int ii = 0; exceptionIterator.hasNext(); ii++) {
                currentException = (String) exceptionIterator.next();
                p = new Reflection(currentException, String.class, "toString", null); // NOI18N
                p.setName("exception" + ii); // NOI18N
                p.setDisplayName(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_PARAMTYPE"));
                p.setShortDescription(NbBundle.getMessage(WsdlMethodNode.class, "METHOD_PARAMTYPE"));
                exceptionSet.put(p);
            }
        } catch (NoSuchMethodException nsme) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, nsme.getLocalizedMessage(), nsme);
        }

        return sheet;
    }
    // Handle copying and cutting specially:
    public boolean canCopy() {
        return true;
    }

    public boolean canCut() {
        return false;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        if (method.getSaas().getState() == Saas.State.READY) {
            return SaasTransferable.addFlavors(transferable);
        } else {
            method.getSaas().toStateReady(false);
            return super.clipboardCopy();
        }

    }
}
