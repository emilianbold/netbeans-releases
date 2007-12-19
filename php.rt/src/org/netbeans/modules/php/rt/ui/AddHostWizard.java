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

package org.netbeans.modules.php.rt.ui;

import java.text.MessageFormat;
import java.util.Set;

import java.util.logging.Logger;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.UiConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author ads
 */
public class AddHostWizard extends WizardDescriptor {
    
    private static final String LBL_TITLE_FORMAT            = 
            "LBL_TitleFormat";                                      // NOI18N
    private static final String LBL_TITLE                   = 
            "LBL_Title";                                            // NOI18N
    private final static String PROP_AUTO_WIZARD_STYLE      = 
            "WizardPanel_autoWizardStyle";                          // NOI18N
    private final static String PROP_CONTENT_DISPLAYED      = 
            "WizardPanel_contentDisplayed";                         // NOI18N
    private final static String PROP_CONTENT_NUMBERED       = 
            "WizardPanel_contentNumbered";                          // NOI18N
    public final static String PROP_CONTENT_DATA            = 
            "WizardPanel_contentData";                              // NOI18N
    public static final String SELECTED_INDEX               =
        "WizardPanel_contentSelectedIndex";                         // NOI18N
    
    public static final String LBL_CHOOSE_SERVER            = 
        "LBL_ChooseServer";                                         // NOI18N
    
    public static final String PROVIDER                     = 
        "Web-Provider";                                             // NOI18N
    
    public static final String NAME                         = 
        "Connection-Name";                                          // NOI18N
    
    public static final String USE_AUTOSEARCH               = 
        "Use-Autosearch";                                           // NOI18N
    
    public static final String HOST                         = 
        "host-impl";                                                // NOI18N

    public static final String WIZARD_MODE                  = 
        "wizard-mode";                                              // NOI18N

    public static final String ELIPSIS                     = "..."; // NOI18N
    
    private static Logger LOGGER = Logger.getLogger(
            AddHostWizard.class.getName());

    public AddHostWizard(Host pattern, Mode mode) {
        this(new AddHostWizardIterator());
        
        initDefaults(pattern);
        initWizard(mode);
    }
    
    public AddHostWizard() {
        this(null, Mode.CREATE_NEW);
    }
    
    public String[] getInitialSteps(){
        return INITIAL_STEPS;
    }
    
    public WebServerProvider getCurrentProvider() {
        Object obj = getProperty( PROVIDER );
        if ( obj == null ){
            return null;
        }
        return (WebServerProvider) obj;
    }
    
    /**
     * Returns Wizard mode. 
     * It can be invoked to create new host (default)
     * or to update existing.
     */
    public Mode getWizardMode(){
        Object obj = getProperty(WIZARD_MODE);
        
        if (obj == null){
            return Mode.CREATE_NEW;
        }
        if (obj instanceof Mode){
            return (Mode)obj;
        }
        return Mode.CREATE_NEW;
    }
    
    private void initWizard(Mode mode){
        putProperty(PROP_AUTO_WIZARD_STYLE, true );
        putProperty(PROP_CONTENT_DISPLAYED, true );
        putProperty(PROP_CONTENT_NUMBERED,  true );
        
        Mode setMode = (mode == null) ? Mode.CREATE_NEW : mode;
        putProperty(WIZARD_MODE, setMode);
        
        setTitle(NbBundle.getMessage(AddHostWizard.class, LBL_TITLE));
        setTitleFormat(new MessageFormat(NbBundle.getMessage(AddHostWizard.class, 
                LBL_TITLE_FORMAT)));
        
        initialize();
    }
    
    private void initDefaults(Host pattern) {
        if (pattern != null){
            putProperty(NAME, pattern.getId());
            putProperty(PROVIDER, pattern.getProvider());
            putProperty(HOST, pattern);
        }
    }

    private AddHostWizard(AddHostWizardIterator iterator) {
        super(iterator);
    }
    
    private ServerTypeChooserPanel getTypeChooser() {
        if (myChooser == null) {
            myChooser = new ServerTypeChooserPanel();
        }
        return myChooser;
    }
    
    private UiConfigProvider getConfigProvider(){
        WebServerProvider provider = getCurrentProvider();
        if ( provider == null ) {
            return null;
        }
        return provider.getConfigProvider();
    }
    
    private static class AddHostWizardIterator implements InstantiatingIterator {

        AddHostWizardIterator() {
        }
        
        public void addChangeListener(ChangeListener l) {
        }
        
        public WizardDescriptor.Panel current() {
            if ( myIndex == 0 ){
                return getWizard().getTypeChooser();
            }
            else {
                UiConfigProvider provider = getConfigProvider();
                assert provider!=null;
                return provider.getPanels()[ myIndex -1 ];
            }
        }
        
        public boolean hasNext() {
            boolean ret = false;
            if ( myIndex == 0 ){
                ret = WebServerProvider.ServerFactory.getProviders().length >0;
            }
            else {
                UiConfigProvider provider = getConfigProvider();
                if ( provider != null ) {
                    ret = provider.getPanels().length > myIndex;
                }
            }
            return ret;
        }
        
        public boolean hasPrevious() {
            return myIndex >0;
        }
        
        public String name() {
            return null;
        }
        
        public void nextPanel() {
            myIndex++;
        }
        
        public void previousPanel() {
            myIndex--;
        }
        
        public void removeChangeListener(ChangeListener l) {
        }
        
        public void uninitialize(WizardDescriptor wizard) {
            WebServerProvider[] providers = WebServerProvider.ServerFactory.
                getProviders();
            for (WebServerProvider provider : providers) {
                UiConfigProvider configProvider = provider.getConfigProvider();
                if ( configProvider != null ) {
                    configProvider.uninitilize();
                }
            }
            getWizard().putProperty( PROVIDER, null );
            clearProperties();
            myWizard = null;
        }

        public void initialize(WizardDescriptor wizard) {
            myWizard = (AddHostWizard)wizard;
        }

        public Set instantiate()  {
            UiConfigProvider provider = getConfigProvider();
            assert provider!=null;
            Set set = provider.instantiate( getWizard() );
            return set;
        }
        
        private void clearProperties() {
            Set<String>  keys = getWizard().getProperties().keySet();
            for (String key : keys) {
                getWizard().putProperty( key , null );
            }
        }
        
        private AddHostWizard getWizard() {
            return myWizard;
        }
        
        private UiConfigProvider getConfigProvider(){
            return getWizard().getConfigProvider();
        }
        
        private AddHostWizard myWizard;
        
        private int myIndex;
    }
    
    /**
     * Wizard modes. It can be invoked to create new host (default)
     * or to update existing.
     */
    public enum Mode {
        CREATE_NEW,
        UPDATE_EXISTING
    }
    
    private ServerTypeChooserPanel myChooser;
    
    private static String[] INITIAL_STEPS = new String[]{ 
            NbBundle.getMessage(AddHostWizard.class, LBL_CHOOSE_SERVER) };
}
