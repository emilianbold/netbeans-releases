/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.action.UseDatabaseGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/**
 * Provide action for using a data source.
 * @author Chris Webster
 * @author Martin Adamek
 */
public class UseDatabaseAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return;
        }
        FileObject fileObject = nodes[0].getLookup().lookup(FileObject.class);
        try {
            ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
            generate(fileObject, elementHandle);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    private boolean generate(FileObject fileObject, ElementHandle<TypeElement> elementHandle) throws IOException {
        Project project = FileOwnerQuery.getOwner(fileObject);
        //make sure configuration is ready
        J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        j2eeModuleProvider.getConfigSupport().ensureConfigurationReady();
        EnterpriseReferenceContainer enterpriseReferenceContainer = project.getLookup().lookup(EnterpriseReferenceContainer.class);
        SelectDatabasePanel selectDatabasePanel = new SelectDatabasePanel(j2eeModuleProvider, enterpriseReferenceContainer.getServiceLocatorName()); //NOI18N
        final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                selectDatabasePanel,
                NbBundle.getMessage(UseDatabaseAction.class, "LBL_ChooseDatabase"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(SelectDatabasePanel.class),
                null
                );
        //#73163: disable OK button when no db connections are available
        dialogDescriptor.setValid(checkConnections());
        selectDatabasePanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(SelectDatabasePanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        dialogDescriptor.setValid(((Boolean)newvalue).booleanValue() && checkConnections());
                    }
                }
            }
        });
        selectDatabasePanel.checkDatasource();
        Object option = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (option == NotifyDescriptor.OK_OPTION) {
            UseDatabaseGenerator generator = new UseDatabaseGenerator();
            generator.generate(
                    fileObject,
                    elementHandle,
                    selectDatabasePanel.getDatasource(),
                    selectDatabasePanel.createServerResources(),
                    selectDatabasePanel.getServiceLocator()
                    );
        }
        return false;
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return false;
        }
        FileObject fileObject = nodes[0].getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            return false;
        }
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] isInterface = new boolean[1];
        try {
            final ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = elementHandle.resolve(controller);
                    isInterface[0] = ElementKind.INTERFACE == typeElement.getKind();
                }
            }, true);
            return elementHandle == null ? false : !isInterface[0];
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return false;
    }
    
    private boolean checkConnections() {
        return ConnectionManager.getDefault().getConnections().length > 0;
    }
    
    public String getName() {
        return NbBundle.getMessage(UseDatabaseAction.class, "LBL_UseDbAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected void initialize() {
        super.initialize();
        putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(UseDatabaseAction.class, "HINT_UseDbAction"));
    }
}
