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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.action.SendJMSGenerator;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb.MessageDestinationUiSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;


/**
 * Provide action for sending a JMS Message
 * 
 * @author Chris Webster
 * @author Martin Adamek
 */
public class SendJMSMessageAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
       try {           
            ElementHandle<TypeElement> beanClass = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
            FileObject srcFile = nodes[0].getLookup().lookup(FileObject.class);
            Project enterpriseProject = FileOwnerQuery.getOwner(srcFile);
            EnterpriseReferenceContainer erc = enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
            J2eeModuleProvider provider = enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
            
            MessageDestinationUiSupport.DestinationsHolder holder = 
                    SendJMSMessageUiSupport.getDestinations(provider);
            SendJmsMessagePanel sendJmsMessagePanel = SendJmsMessagePanel.newInstance(
                    provider,
                    holder.getModuleDestinations(),
                    holder.getServerDestinations(),
                    SendJMSMessageUiSupport.getMdbs(),
                    erc.getServiceLocatorName(),
                    ClasspathInfo.create(srcFile));
            final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                    sendJmsMessagePanel,
                    NbBundle.getMessage(SendJMSMessageAction.class,"LBL_SendJmsMessage"),
                    true,
                    DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION,
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(SendJMSMessageAction.class),
                    null);
            
            sendJmsMessagePanel.addPropertyChangeListener(SendJmsMessagePanel.IS_VALID,
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            Object newvalue = evt.getNewValue();
                            if ((newvalue != null) && (newvalue instanceof Boolean)) {
                                dialogDescriptor.setValid(((Boolean)newvalue).booleanValue());
                            }
                        }
                    });
            sendJmsMessagePanel.verifyAndFire();

            Object option = DialogDisplayer.getDefault().notify(dialogDescriptor);
            if (option != DialogDescriptor.OK_OPTION) {
                return;
            }
            
            String serviceLocator = sendJmsMessagePanel.getServiceLocator();
            ServiceLocatorStrategy serviceLocatorStrategy = null;
            if (serviceLocator != null) {
                serviceLocatorStrategy = 
                        ServiceLocatorStrategy.create(enterpriseProject, srcFile, serviceLocator);
            }
            
            MessageDestination messageDestination = sendJmsMessagePanel.getDestination();
            Project mdbHolderProject = sendJmsMessagePanel.getMdbHolderProject();
            SendJMSGenerator generator = new SendJMSGenerator(messageDestination, mdbHolderProject != null ? mdbHolderProject : enterpriseProject);
            generator.genMethods(
                    erc, 
                    beanClass.getQualifiedName().toString(), 
                    sendJmsMessagePanel.getConnectionFactory(),
                    srcFile, 
                    serviceLocatorStrategy,
                    enterpriseProject.getLookup().lookup(J2eeModuleProvider.class)
                    );
            if (serviceLocator != null) {
                erc.setServiceLocatorName(serviceLocator);
            }
        } catch (IOException ioe) {
            NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(ioe.getMessage(),
            NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
        } 
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return false;
        }
        FileObject fileObject = nodes[0].getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            return false;
        }
        Project project = FileOwnerQuery.getOwner(fileObject);
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
        if (serverInstanceId == null) {
            return true;
        }
        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceId);
        if (platform == null) {
            return true;
        }
        if (!platform.getSupportedModuleTypes().contains(J2eeModule.EJB)) {
            return false;
        }
        String j2eeVersion = j2eeModuleProvider.getJ2eeModule().getModuleVersion();
        Object moduleType = j2eeModuleProvider.getJ2eeModule().getModuleType();
        if (Util.isJavaEE5orHigher(project) ||
                (J2eeModule.WAR.equals(moduleType) && WebApp.VERSION_2_4.equals(j2eeVersion)) ||
                (J2eeModule.EJB.equals(moduleType) && EjbJar.VERSION_2_1.equals(j2eeVersion)))  {
            JavaSource javaSource = JavaSource.forFileObject(fileObject);
            final boolean[] isInterface = new boolean[1];
            try {
                final ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
                if (javaSource == null || elementHandle == null) {
                    return false;
                }
                javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TypeElement typeElement = elementHandle.resolve(controller);
                        isInterface[0] = ElementKind.INTERFACE == typeElement.getKind();
                    }
                }, true);
                return elementHandle == null ? false : !isInterface[0];
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return false;
    }
    
    public String getName() {
        return NbBundle.getMessage(SendJMSMessageAction.class, "LBL_SendJMSMessageAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
     * protected void initialize() {
     * super.initialize();
     * putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(CallEjbAction.class, "HINT_Action"));
     * }
     */

    public Action createContextAwareInstance(Lookup actionContext) {
        boolean enable = enable(actionContext.lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]));
        return enable ? this : null;
    }
    
}
