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

import javax.swing.Action;
import org.netbeans.api.db.explorer.ConnectionManager;
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
        //TODO: RETOUCHE
//        JavaClass beanClass = JMIUtils.getJavaClassFromNode(nodes[0]);
//        FileObject srcFile = JavaModel.getFileObject(beanClass.getResource());
//        Project enterpriseProject = FileOwnerQuery.getOwner(srcFile);
//        
//        //make sure configuration is ready
//        J2eeModuleProvider pwm = (J2eeModuleProvider) enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
//        pwm.getConfigSupport().ensureConfigurationReady();
//        
//        EnterpriseReferenceContainer erc = (EnterpriseReferenceContainer)
//        enterpriseProject.getLookup().lookup(EnterpriseReferenceContainer.class);
//        
//        SelectDatabasePanel p = new SelectDatabasePanel(pwm, erc.getServiceLocatorName()); //NOI18N
//        final DialogDescriptor nd = new DialogDescriptor(
//                p,
//                NbBundle.getMessage(UseDatabaseAction.class, "LBL_ChooseDatabase"),
//                true,
//                DialogDescriptor.OK_CANCEL_OPTION,
//                DialogDescriptor.OK_OPTION,
//                DialogDescriptor.DEFAULT_ALIGN,
//                new HelpCtx(SelectDatabasePanel.class),
//                null
//                );
//        //#73163: disable OK button when no db connections are available
//        nd.setValid(checkConnections());
//        p.addPropertyChangeListener(new PropertyChangeListener() {
//            public void propertyChange(PropertyChangeEvent evt) {
//                if (evt.getPropertyName().equals(SelectDatabasePanel.IS_VALID)) {
//                    Object newvalue = evt.getNewValue();
//                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
//                        nd.setValid(((Boolean)newvalue).booleanValue() && checkConnections());
//                    }
//                }
//            }
//        });
//        p.checkDatasource();
//        Object option = DialogDisplayer.getDefault().notify(nd);
//        if (option == NotifyDescriptor.OK_OPTION) {
//            try {
//                Datasource ds = p.getDatasource();
//                String serviceLocator = p.getServiceLocator();
//                ServiceLocatorStrategy serviceLocatorStrategy = null;
//                if (serviceLocator != null) {
//                    serviceLocatorStrategy = ServiceLocatorStrategy.create(enterpriseProject, srcFile, serviceLocator);
//                }
//                
//                if (Utils.isJavaEE5orHigher(enterpriseProject) &&
//                        InjectionTargetQuery.isInjectionTarget(beanClass) &&
//                        serviceLocatorStrategy == null) {
//                    generateInjectedField(beanClass, ds.getJndiName());
//                } else {
//                    String jndiName = generateJNDILookup(ds.getJndiName(), erc, beanClass.getName(), ds.getUrl(), p.createServerResources()); // NOI18N
//                    generateLookupMethod(beanClass, jndiName, serviceLocatorStrategy);
//                }
//                
//                if (serviceLocator != null) {
//                    erc.setServiceLocatorName(serviceLocator);
//                }
//            } catch (IOException ioe) {
//                NotifyDescriptor ndd = new NotifyDescriptor.Message(ioe.getMessage(),
//                        NotifyDescriptor.ERROR_MESSAGE);
//                DialogDisplayer.getDefault().notify(ndd);
//            }
//        }
    }
    
    private boolean checkConnections() {
        return ConnectionManager.getDefault().getConnections().length > 0;
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
    
//    private String generateJNDILookup(String jndiName,
//            EnterpriseReferenceContainer erc,
//            String className,
//            String nodeName, boolean createServerResources) throws IOException {
//        ResourceRef ref = erc.createResourceRef(className);
//        if (createServerResources) {
//            ref.setDescription(nodeName);
//        }
//        ref.setResRefName(jndiName); // NOI18N
//        ref.setResAuth(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_AUTH_CONTAINER);
//        ref.setResSharingScope(org.netbeans.modules.j2ee.dd.api.common.ResourceRef.RES_SHARING_SCOPE_SHAREABLE);
//        ref.setResType(javax.sql.DataSource.class.getName()); //NOI18N
//        return erc.addResourceRef(ref, className);
//    }
//    
//    private void generateLookupMethod(JavaClass ce, String jndiName, ServiceLocatorStrategy sl) {
//        String methodName = "get" + Utils.jndiNameToCamelCase(jndiName, false, null); //NO18N
//        Method me = JMIUtils.createMethod(ce);
//        me.setModifiers(Modifier.PRIVATE);
//        me.setName(methodName);
//        me.setType(JMIUtils.resolveType(javax.sql.DataSource.class.getName()));
//        JMIUtils.addException(me, javax.naming.NamingException.class.getName());
//        if (sl == null) {
//            me.setBodyText(getLookupCode(jndiName));
//        } else {
//            me.setBodyText(getLookupCode(jndiName, sl, ce));
//        }
//        if (!Utils.containsFeature(ce, me)){
//            ce.getContents().add(me);
//            fixImports(ce, me);
//        }
//    }
//    
//    
//    private String getLookupCode(String jndiName, ServiceLocatorStrategy sl, JavaClass target) {
//        String jdbcLookupString = sl.genDataSource(jndiName, target);
//        return "return (javax.sql.DataSource) " + jdbcLookupString + ";\n"; // NOI18N
//    }
//    
//    private String getLookupCode(String jndiName) {
//        return MessageFormat.format(
//                "javax.naming.Context c = new javax.naming.InitialContext();\n" + // NOI18N
//                "return (javax.sql.DataSource) c.lookup(\"java:comp/env/{0}\");\n", // NOI18N
//                new Object[] {jndiName});
//    }
//    
//    private void generateInjectedField(JavaClass javaClass, String jndiName) {
//        int modifier = InjectionTargetQuery.isStaticReferenceRequired(javaClass) ? (Modifier.STATIC | Modifier.PRIVATE) : Modifier.PRIVATE;
//        String fieldName = Utils.jndiNameToCamelCase(jndiName, true, null);
//        Field field = JMIGenerationUtil.createField(javaClass, fieldName, modifier, "javax.sql.DataSource");
//        AttributeValue av = JMIGenerationUtil.createAttributeValue(javaClass, "name", jndiName);
//        Annotation a = JMIGenerationUtil.createAnnotation(javaClass, "javax.annotation.Resource", Collections.singletonList(av));
//        field.getAnnotations().add(a);
//        if (!Utils.containsFeature(javaClass, field)){
//            javaClass.getFeatures().add(0, field);
//            fixImports(javaClass, field);
//        }
//    }
//    
//    private void fixImports(JavaClass beanClass, Feature f) {
//        boolean failed = true;
//        JavaModel.getJavaRepository().beginTrans(true);
//        try {
//            JMIUtils.fixImports(beanClass, f);
//            failed = false;
//        } finally {
//            JavaModel.getJavaRepository().endTrans(failed);
//        }
//    }
    
    protected boolean enable(Node[] nodes) {
        //TODO: RETOUCHE
        return false;
//        if (nodes == null || nodes.length != 1) {
//            return false;
//        }
//        JavaClass jc = JMIUtils.getJavaClassFromNode(nodes[0]);
//        return jc == null ? false : !jc.isInterface();
    }
    
    protected void initialize() {
        super.initialize();
        putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(UseDatabaseAction.class, "HINT_UseDbAction"));
    }
}
