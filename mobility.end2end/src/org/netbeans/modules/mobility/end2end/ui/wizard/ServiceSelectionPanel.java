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
 * ServiceSelectionPanel.java
 *
 * Created on August 4, 2005, 3:45 PM
 *
 */
package org.netbeans.modules.mobility.end2end.ui.wizard;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.end2end.classdata.ClassData;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.multiview.ServicesPanel;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Skvor
 */
public class ServiceSelectionPanel implements TemplateWizard.Panel, ChangeListener {
    
    private ServicesPanel gui;
    
    private TemplateWizard templateWizard;
    private final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public static final HelpCtx HELP_CTX = new HelpCtx( "me.wcb_serviceselection" ); // NOI18N
    
    public java.awt.Component getComponent() {
        if( gui == null ) {
            gui = new ServicesPanel(null, null);
            //gui.setName( NbBundle.getMessage( WebApplicationPanel.class, "TITLE_serviceSelectionStep" ));
            gui.addChangeListener(this);
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
        ((ServicesPanel)getComponent()).setConfiguration(configuration);
        gui.setServerProjectFolder(
                ((Project)templateWizard.getProperty(GenericServiceIterator.PROP_SERVER_PROJECT)).getProjectDirectory());
        
        // Check for validity
        isValid();
    }
    
    public void storeSettings(final Object settings) {
        templateWizard = (TemplateWizard)settings;
        templateWizard.putProperty( GenericServiceIterator.PROP_CONFIGURATION, gui.getConfiguration() );
    }
    
    public boolean isValid() {
        final List<ClassData> data = gui.getConfiguration().getServices().get(0).getData();
        final boolean valid = !( data == null || data.size() == 0 );
        if( valid ){
            templateWizard.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, " " ); // NOI18N`
        } else {
            templateWizard.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage( ServiceSelectionPanel.class, "ERR_NoService" )); // NOI18N
        }
        return valid;
    }
    
    public void addChangeListener( final ChangeListener changeListener ) {
        listeners.add( changeListener );
    }
    
    public void removeChangeListener( final ChangeListener changeListener ) {
        listeners.remove( changeListener );
    }
    
    public void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( ChangeListener cl : listeners ) {
            cl.stateChanged(e);
        }
    }
    
    public void stateChanged(@SuppressWarnings("unused")
	final ChangeEvent e) {
        fireChange();
    }
}
