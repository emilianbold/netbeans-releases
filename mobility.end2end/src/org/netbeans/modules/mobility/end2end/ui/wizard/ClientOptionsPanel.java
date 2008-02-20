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

/*
 * ClientOptionsPanel.java
 *
 * Created on August 8, 2005, 4:36 PM
 *
 */
package org.netbeans.modules.mobility.end2end.ui.wizard;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.mobility.end2end.client.config.ClassDescriptor;
import org.netbeans.modules.mobility.end2end.client.config.ClientConfiguration;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.client.config.ServerConfiguration;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Michal Skvor
 */
public class ClientOptionsPanel implements TemplateWizard.Panel, ChangeListener {
    
    TemplateWizard templateWizard;
    
    private ClientOptionsPanelGUI gui;
    
    private final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public static final HelpCtx HELP_CTX = new HelpCtx( "me.wcb_clientoptions" ); // NOI18N
    
    final private static String TRUE = "true";
    final private static String FALSE = "false";           
    final private static String WPEM = "WizardPanel_errorMessage";
        
    public java.awt.Component getComponent() {
        if( gui == null ) {
            gui = new ClientOptionsPanelGUI();
            gui.setName( NbBundle.getMessage( WebApplicationPanel.class, "TITLE_clientOptionsStep" ));
            gui.addChangeListener( this );
        }
        return gui;
    }
    
    public HelpCtx getHelp() {
        return HELP_CTX;
    }
    
    public void readSettings( final Object settings ) {
        templateWizard = (TemplateWizard)settings;
        
        final Configuration configuration = (Configuration)templateWizard.
                getProperty( GenericServiceIterator.PROP_CONFIGURATION );
        String clientName = null;
        final ClientConfiguration cc = configuration.getClientConfiguration();
        if (cc == null){
            final ServerConfiguration sc = configuration.getServerConfigutation();
            final ClassDescriptor cd = sc.getClassDescriptor();
            final String servletName = cd.getLeafClassName();
            int pos = servletName.indexOf( "Servlet" ); // NOI18N
            if( pos < 0 ) {
                pos = 0;
            }
            clientName = servletName.substring( 0, pos ) + "Client"; // NOI18N
        } else {
            clientName = Templates.getTargetName(templateWizard);
        }
        
        if( getComponent() != null ) {
            gui.setValues( Templates.getProject( templateWizard ),
                    clientName,
                    Templates.getTargetFolder( templateWizard ));
        }
    }
    
    public void storeSettings( final Object settings ) {
        templateWizard = (TemplateWizard)settings;
        
        final Configuration configuration = (Configuration)templateWizard.
                getProperty( GenericServiceIterator.PROP_CONFIGURATION );
        final ClientConfiguration cc = new ClientConfiguration();
        
        cc.setProjectName( gui.getProjectName());
        String className = gui.getPackageFileName();
        if( !"".equals( className )) {
            className += "." + gui.getTargetName(); // NOI18N
        } else {
            className = gui.getTargetName();
        }
        
        final FileObject sourceRoot = gui.getRootFolder();
        className = className.replace('/','.');
        final ClassDescriptor cd = new ClassDescriptor( className,
                gui.getSourceGroup().getName());
        
        Templates.setTargetName( templateWizard, gui.getTargetName() );
        templateWizard.putProperty(GenericServiceIterator.PROP_CLIENT_ROOT, sourceRoot);
        templateWizard.putProperty(GenericServiceIterator.PROP_DATABINDING, new Boolean( gui.isDataBinded()));
        
        cc.setClassDescriptor( cd );
        //System.err.println(" = class name - " + className );
        final Properties props = new Properties();
        if( gui.isGenerateStubs()) {
            props.put( ClientConfiguration.PROP_CREATE_STUBS, TRUE ); // NOI18N
        } else {
            props.put( ClientConfiguration.PROP_CREATE_STUBS, FALSE ); // NOI18N
        }
        if( gui.isFloatingPointUsed()) {
            props.put( ClientConfiguration.PROP_FLOATING_POINT, TRUE ); // NOI18N
        } else {
            props.put( ClientConfiguration.PROP_FLOATING_POINT, FALSE ); // NOI18N
        }
        if( gui.isDataBinded()) {
            props.put( ClientConfiguration.PROP_DATABINDING, TRUE ); // NOI18N
        } else {
            props.put( ClientConfiguration.PROP_DATABINDING, FALSE ); // NOI18N
        }
        
        cc.setProperties( props );
        configuration.setClientConfiguration( cc );
    }
    
    public boolean isValid() {
        if( gui == null ) {
            templateWizard.putProperty(WPEM, NbBundle.getMessage(ClientOptionsPanel.class, "ERR_File_NoGUI")); // NOI18N
            return false;
        } else if( gui.getCreatedFile() != null && new File( gui.getCreatedFile()).exists()) {
            templateWizard.putProperty(WPEM, NbBundle.getMessage(ClientOptionsPanel.class, "ERR_File_AlreadyExists", gui.getTargetName() + ".java")); // NOI18N
            return false;
        } else if( "".equals(gui.getTargetName())) {
            templateWizard.putProperty(WPEM, NbBundle.getMessage(ClientOptionsPanel.class, "ERR_File_NoClassName")); // NOI18N
            return false;
        } else if( !Utilities.isJavaIdentifier( gui.getTargetName())) {
            templateWizard.putProperty(WPEM, NbBundle.getMessage(ClientOptionsPanel.class, "ERR_File_InvalidClassName")); // NOI18N
            return false;
        } else if( !isValidJavaFolderName( gui.getPackageFileName())) {
            templateWizard.putProperty(WPEM, NbBundle.getMessage(ClientOptionsPanel.class, "ERR_File_InvalidPackageName")); // NOI18N
            return false;
        }
        templateWizard.putProperty(WPEM, " "); //NOI18N
        return true;
    }
    
    private static boolean isValidJavaFolderName( final String packageFileName ) {
        if( packageFileName == null )
            return false;
        final StringTokenizer st = new StringTokenizer( packageFileName, "/" ); // NOI18N
        while( st.hasMoreElements()) {
            final String s = (String)st.nextElement();
            if( !Utilities.isJavaIdentifier( s ))
                return false;
        }
        return true;
    }
    
    public void addChangeListener( final ChangeListener changeListener ) {
        listeners.add( changeListener );
    }
    
    public void removeChangeListener( final ChangeListener changeListener ) {
        listeners.remove( changeListener );
    }
    
    private void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener cl : listeners ) {
            cl.stateChanged(e);
        }
    }
    
    public void stateChanged( @SuppressWarnings("unused")
	final ChangeEvent e ) {
        if( templateWizard != null ){
            templateWizard.setValid( isValid());
            fireChange();
        }
    }
}
