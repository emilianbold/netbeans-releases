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
        templateWizard.putProperty(GenericServiceIterator.PROP_CREATE_MIDLET, new Boolean(gui.isCreateSampleMIDlet()));
        
        cc.setClassDescriptor( cd );
        //System.err.println(" = class name - " + className );
        final Properties props = new Properties();
        if( gui.isGenerateStubs()) {
            props.put( ClientConfiguration.PROP_CREATE_STUBS, TRUE ); // NOI18N
        } else {
            props.put( ClientConfiguration.PROP_CREATE_STUBS, FALSE ); // NOI18N
        }
        if( gui.isGroupedCalls()) {
            props.put( ClientConfiguration.PROP_MULTIPLE_CALL, TRUE ); // NOI18N
        } else {
            props.put( ClientConfiguration.PROP_MULTIPLE_CALL, FALSE ); // NOI18N
        }
        if( gui.isFloatingPointUsed()) {
            props.put( ClientConfiguration.PROP_FLOATING_POINT, TRUE ); // NOI18N
        } else {
            props.put( ClientConfiguration.PROP_FLOATING_POINT, FALSE ); // NOI18N
        }
        if( gui.isTracing()) {
            props.put( ClientConfiguration.PROP_TRACE, TRUE ); // NOI18N
        } else {
            props.put( ClientConfiguration.PROP_TRACE, FALSE ); // NOI18N
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
        } else if( !Utilities.isJavaIdentifier( gui.getTargetName())) {
            templateWizard.putProperty(WPEM, NbBundle.getMessage(ClientOptionsPanel.class, "ERR_File_InvalidClassName")); // NOI18N
            return false;
        } else if( !isValidJavaFolderName( gui.getPackageFileName())) {
            templateWizard.putProperty(WPEM, NbBundle.getMessage(ClientOptionsPanel.class, "ERR_File_InvalidPackageName")); // NOI18N
            return false;
        } else if( gui.isCreateSampleMIDlet() && gui.isGroupedCalls() && gui.isGenerateStubs()) {
            templateWizard.putProperty(WPEM, NbBundle.getMessage(ClientOptionsPanel.class, "MSG_NoGroupedCallMidlet")); // NOI18N
            return true;
        } else if( gui.isCreateSampleMIDlet() && gui.isGroupedCalls() && !gui.isGenerateStubs()) {
            templateWizard.putProperty(WPEM, NbBundle.getMessage(ClientOptionsPanel.class, "MSG_NoGroupedSampleMidlet")); // NOI18N
            return true;
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
