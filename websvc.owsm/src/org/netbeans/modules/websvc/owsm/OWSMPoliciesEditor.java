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
package org.netbeans.modules.websvc.owsm;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.javaee.specs.support.api.JaxWsPoliciesSupport;
import org.netbeans.modules.websvc.api.wseditor.InvalidDataException;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;


/**
 * @author ads
 *
 */
class OWSMPoliciesEditor implements WSEditor {
    
    private static final String ORACLE = "oracle/";     // NOI18N
    
    OWSMPoliciesEditor( JaxWsPoliciesSupport support , Lookup lookup){
        mySupport = support;
        myFileObject = lookup.lookup( FileObject.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.api.wseditor.WSEditor#createWSEditorComponent(org.openide.nodes.Node)
     */
    @Override
    public JComponent createWSEditorComponent( Node node )
            throws InvalidDataException
    {
        if ( mySupport == null || myFileObject == null ){
            JComponent component = new JPanel();
            component.setLayout( new FlowLayout());
            component.add( new JLabel(NbBundle.getMessage( OWSMPoliciesEditor.class, 
                    "ERR_NoPoliciesSupport")));             // NOI18N
            return component;
        }
        List<String> securityPolicies = filterSecurityPolicies( );
        myPanel = new PoliciesVisualPanel( securityPolicies , myFileObject );
        return myPanel;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.api.wseditor.WSEditor#getTitle()
     */
    @Override
    public String getTitle() {
        return NbBundle.getMessage( OWSMPoliciesEditor.class, "TXT_OWSMEditorTitle");   // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.api.wseditor.WSEditor#save(org.openide.nodes.Node)
     */
    @Override
    public void save( Node node ) {
        if ( node ==null ){
            return;
        }
        if ( SwingUtilities.isEventDispatchThread() ){
            PoliciesVisualPanel.JAVA_PROCESSOR.post( new Runnable() {
                
                @Override
                public void run() {
                    doSave();
                }
            });
        }
        else {
            doSave();
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.api.wseditor.WSEditor#cancel(org.openide.nodes.Node)
     */
    @Override
    public void cancel( Node node ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.api.wseditor.WSEditor#getDescription()
     */
    @Override
    public String getDescription() {
        return NbBundle.getMessage( OWSMPoliciesEditor.class, "TXT_PanelDescription");  // NOI18N
    }
    
    private void doSave(){
        if ( myPanel == null ){
            return;
        }
        if ( myPanel.getWsFqn() == null ){
            return;
        }
        JavaSource javaSource = JavaSource.forFileObject(myFileObject );
        if ( javaSource == null ) {
            NotifyDescriptor descriptor = new NotifyDescriptor.Message( 
                    NbBundle.getMessage(OWSMPoliciesEditor.class, "ERR_NoJava"),    // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify( descriptor );
            return;
        }
        final List<String> policyIds = myPanel.getPolicyIds();
        List<String> fqns = new ArrayList<String>(2);
        fqns.add( PoliciesVisualPanel.OWSM_SECURITY_POLICY);
        if ( policyIds.size() >1 ){
            fqns.add(PoliciesVisualPanel.OWSM_SECURITY_POLICIES);
        }
        Project project = FileOwnerQuery.getOwner(myFileObject);
        mySupport.extendsProjectClasspath(project, fqns);
        try {
            javaSource.runModificationTask(new Task<WorkingCopy>() {

                @Override
                public void run( WorkingCopy workingCopy ) throws Exception {
                    workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    String wsFqn = myPanel.getWsFqn();
                    
                    CompilationUnitTree cu = workingCopy.getCompilationUnit();
                    ClassTree wsClassTree = null;
                    if (cu != null) {
                        List<? extends Tree> decls = cu.getTypeDecls();
                        for (Tree decl : decls) {
                            if (!TreeUtilities.CLASS_TREE_KINDS.contains(decl
                                    .getKind()))
                            {
                                continue;
                            }

                            ClassTree classTree = (ClassTree) decl;
                            Element element = workingCopy.getTrees()
                                    .getElement(
                                            workingCopy.getTrees().getPath(cu,
                                                    classTree));
                            if (element instanceof TypeElement) {
                                Name className = ((TypeElement) element)
                                        .getQualifiedName();
                                if (className.contentEquals(wsFqn)) {
                                    wsClassTree = classTree;
                                }
                            }
                        }
                    }
                    if (wsClassTree == null) {
                        return;
                    }
                    rewriteAnnotations(policyIds, workingCopy, wsClassTree);
                }

            }).commit();
        }
        catch (IOException e) {
            Logger.getLogger( OWSMPoliciesEditor.class.getName() ).log( 
                    Level.INFO, null, e );
        }
    }
    
    private void rewriteAnnotations( final List<String> policyIds,
            WorkingCopy workingCopy, ClassTree wsClassTree )
    {
        TreeMaker maker = workingCopy.getTreeMaker();

        ModifiersTree modifiers = wsClassTree.getModifiers();
        List<? extends AnnotationTree> annotations = modifiers.getAnnotations();
        List<AnnotationTree> clearedTrees = new ArrayList<AnnotationTree>( 
                annotations.size() );
        for (AnnotationTree annotationTree : annotations) {
            Tree annotationType = annotationTree.getAnnotationType();
            Element element = workingCopy.getTrees().getElement( workingCopy.getTrees().getPath(
                    workingCopy.getCompilationUnit() , annotationType ));
            if ( element.equals( workingCopy.getElements().getTypeElement( 
                    PoliciesVisualPanel.OWSM_SECURITY_POLICIES)) || element.
                        equals( workingCopy.getElements().getTypeElement( 
                            PoliciesVisualPanel.OWSM_SECURITY_POLICY)))
            {
                continue;
            }
            clearedTrees.add( annotationTree );
        }
        
        modifiers = maker.Modifiers( modifiers, clearedTrees );

        AnnotationTree newAnnotation = null;
        if (policyIds.size() > 1) {
            List<ExpressionTree> idList = new ArrayList<ExpressionTree>( policyIds.size() );
            for (String id : policyIds) {
                AnnotationTree annotationTree = createPolicyAnnotation(
                        maker, id);
                idList.add(annotationTree);
            }
            NewArrayTree newArray = maker.NewArray( workingCopy.getTrees().getTree(
                    workingCopy.getElements().getTypeElement( PoliciesVisualPanel.OWSM_SECURITY_POLICY)), 
                    Collections.<ExpressionTree>emptyList(), idList );
            newAnnotation = maker.Annotation(
                    maker.QualIdent(PoliciesVisualPanel.OWSM_SECURITY_POLICIES),
                    Collections.singletonList( maker.Assignment( 
                            maker.Identifier(PoliciesVisualPanel.VALUE), newArray )));
        }
        else if ( policyIds.size() == 1){
            newAnnotation = createPolicyAnnotation(maker, policyIds.get(0));
        }

        if (newAnnotation != null) {
            modifiers = maker.addModifiersAnnotation(modifiers, newAnnotation);
        }
        workingCopy.rewrite(wsClassTree.getModifiers(), modifiers);
    }
    
    private AnnotationTree createPolicyAnnotation( TreeMaker maker , String id) {
        ExpressionTree idTree = maker.Assignment(maker.Identifier(
                PoliciesVisualPanel.URI), maker.Literal(id));
        return maker.Annotation(
                maker.QualIdent(PoliciesVisualPanel.OWSM_SECURITY_POLICY), 
                Collections.singletonList( idTree ) );
    }
    
    private List<String> filterSecurityPolicies() {
        List<String> list = mySupport.getServicePolicyIds();
        Set<String> set = new LinkedHashSet<String>( SECURITY_POLICIES );
        set.retainAll( list );
        
        List<String> result = new ArrayList<String>( list.size() );
        for (String id : set) {
            result.add( ORACLE + id);
        }
        return result;
    }
    
    private static Set<String> SECURITY_POLICIES = new LinkedHashSet<String>();
    private JaxWsPoliciesSupport mySupport;
    private FileObject myFileObject;
    private PoliciesVisualPanel myPanel;

    static {
        SECURITY_POLICIES.add("binding_authorization_denyall_policy");      // NOI18N
        SECURITY_POLICIES.add("binding_authorization_permitall_policy");  // NOI18N
        SECURITY_POLICIES.add("binding_permission_authorization_policy");  // NOI18N
        SECURITY_POLICIES.add("no_authentication_service_policy");  // NOI18N
        SECURITY_POLICIES.add("no_authorization_service_policy");  // NOI18N
        SECURITY_POLICIES.add("no_messageprotection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("sts_trust_config_service_policy");  // NOI18N
        SECURITY_POLICIES.add("whitelist_authorization_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_http_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_http_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_or_username_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_or_username_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_token_bearer_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml20_token_bearer_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml20_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_sts_issued_saml_bearer_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_username_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_username_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_hok_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_with_message_integrity_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_with_message_protection_ski_basic256_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml20_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml20_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_username_id_propagation_with_msg_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_username_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_username_token_with_message_protection_ski_basic256_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_x509_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_kerberos_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_kerberos_token_with_message_protection_basic128_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_kerberos_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_saml_or_username_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_saml_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_saml20_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_sts_issued_saml_hok_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_username_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_x509_token_with_message_protection_service_policy");  // NOI18N
    }
}
