/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.NodeAction;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.DialogDescriptor;


/**
 * Provide action for using a data source.
 * @author Chris Webster
 * @author Martin Adamek
 */
public class UseDatabaseAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        Feature feature = (Feature) nodes[0].getLookup().lookup(Feature.class);
        JavaClass beanClass = JMIUtils.getDeclaringClass(feature);
        FileObject srcFile = JavaModel.getFileObject(beanClass.getResource());
        Project enterpriseProject = FileOwnerQuery.getOwner(srcFile);

        //make sure configuration is ready
        J2eeModuleProvider pwm = (J2eeModuleProvider) enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        
        EnterpriseReferenceContainer erc = (EnterpriseReferenceContainer)
            enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
        
        SelectDatabasePanel p = new SelectDatabasePanel("myDatabase", erc.getServiceLocatorName()); //NOI18N
        final DialogDescriptor nd = new DialogDescriptor(
                p, 
                NbBundle.getMessage(UseDatabaseAction.class, "LBL_ChooseDatabase"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, 
                new HelpCtx(SelectDatabasePanel.class), 
                null
                );
        p.getServiceLocatorPanel().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(ServiceLocatorStrategyPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        nd.setValid(((Boolean)newvalue).booleanValue());
                    }
                }
            }
        });
        Object option = DialogDisplayer.getDefault().notify(nd);
        if (option == NotifyDescriptor.OK_OPTION) {
            try {
                DatabaseConnection dbconn = p.getConnection();
                if (dbconn != null) {
                    String displayName = dbconn.getName();
                    if (dbconn.getJDBCConnection() == null) {
                        ConnectionManager.getDefault().showConnectionDialog(dbconn);
                    }
                
                    String databaseName = p.getDatabaseName();
                    if((databaseName.startsWith("jdbc/")) || (databaseName.startsWith("Jdbc/"))){       //NOI18N
                        databaseName = databaseName.substring("jdbc/".length());                        //NOI18N
                    }
                    
                    if (dbconn.getJDBCConnection() != null) {
                        String jndiName = generateJNDILookup(
                                databaseName, erc, 
                                beanClass.getName(), displayName, p.createServerResources());
                        String serviceLocator = p.getServiceLocator();
                        ServiceLocatorStrategy serviceLocatorStrategy = null;
                        if (serviceLocator != null) {
                            serviceLocatorStrategy = 
                            ServiceLocatorStrategy.create(enterpriseProject, srcFile, 
                                                          serviceLocator);
                        }
                        generateLookupMethod(beanClass, jndiName, databaseName,
                                             serviceLocatorStrategy);
                        if (serviceLocator != null) {
                            erc.setServiceLocatorName(serviceLocator);
                        }
                    }
                }
            } catch (IOException ioe) {
                NotifyDescriptor ndd = new NotifyDescriptor.Message(ioe.getMessage(),
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(ndd);
            }
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(UseDatabaseAction.class, "LBL_UseDbAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(CallEjbAction.class);
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    private String generateJNDILookup(String databaseName, 
                                    EnterpriseReferenceContainer erc,
                                    String className, 
                                    String nodeName, boolean createServerResources) throws IOException {
        ResourceRef ref = erc.createResourceRef(className);
        if (createServerResources) {
            ref.setDescription(nodeName);
        }
        ref.setResRefName("jdbc/"+databaseName); // NOI18N
        ref.setResAuth(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_AUTH_CONTAINER); 
        ref.setResSharingScope(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_SHARING_SCOPE_SHAREABLE);
        ref.setResType(javax.sql.DataSource.class.getName()); //NOI18N
        return erc.addResourceRef(ref, className);
    }
    
    private void generateLookupMethod(JavaClass ce, String jndiName, String databaseName,
                                      ServiceLocatorStrategy sl) {
        Method me = JMIUtils.createMethod(ce);
        me.setModifiers(Modifier.PRIVATE);
        String dbName = databaseName == null ? "db":databaseName;               //NOI18N
        StringBuffer dbBuff = new StringBuffer(dbName);
        dbBuff.setCharAt(0, Character.toUpperCase(dbBuff.charAt(0)));
        me.setName("get"+dbBuff);
        me.setType(JMIUtils.resolveType(javax.sql.DataSource.class.getName()));
        JMIUtils.addException(me, javax.naming.NamingException.class.getName());
        if (sl == null) {
            me.setBodyText(getLookupCode(jndiName));
        } else {
            me.setBodyText(getLookupCode(jndiName, sl, ce));
        }
        ce.getContents().add(me);
    }
    
    private String getLookupCode(String jndiName, ServiceLocatorStrategy sl, JavaClass target) {
        String jdbcLookupString = sl.genDataSource(jndiName, target);
        return "return (javax.sql.DataSource) " + jdbcLookupString + ";\n"; // NOI18N
    }
    
    private String getLookupCode(String jndiName) {
        return MessageFormat.format(
                "javax.naming.Context c = new javax.naming.InitialContext();\n" + // NOI18N
                "return (javax.sql.DataSource) c.lookup(\"java:comp/env/{0}\");\n", // NOI18N
                new Object[] {jndiName});
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }
	JavaClass jc = JMIUtils.getJavaClassFromNode(nodes[0]);
        return jc == null ? false : !jc.isInterface();
    }
    
    protected void initialize() {
        super.initialize();
        putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(UseDatabaseAction.class, "HINT_UseDbAction"));
    }
}
