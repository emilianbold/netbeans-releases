/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl.ui.wizard;

import java.text.MessageFormat;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.plugins.api.OptionalDeploymentManagerFactory;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class AddServerInstanceWizard extends WizardDescriptor {
    public final static String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N
    public final static String PROP_SERVER = "ServInstWizard_server"; // NOI18N

    private final static String PROP_AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle"; // NOI18N
    private final static String PROP_CONTENT_DISPLAYED = "WizardPanel_contentDisplayed"; // NOI18N
    private final static String PROP_CONTENT_NUMBERED = "WizardPanel_contentNumbered"; // NOI18N
    private final static String PROP_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    private final static String PROP_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; // NOI18N
    private final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N

    private AddServerInstanceWizardIterator iterator;
    private ServerChooserPanel chooser;

    public AddServerInstanceWizard() {
        this(new AddServerInstanceWizardIterator());
        
        putProperty(PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
        putProperty(PROP_CONTENT_DISPLAYED, Boolean.TRUE);
        putProperty(PROP_CONTENT_NUMBERED, Boolean.TRUE);
        
        setTitle(NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_Title"));
        setTitleFormat(new MessageFormat(NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_TitleFormat")));
        
        initialize();
    }
    
    private AddServerInstanceWizard(AddServerInstanceWizardIterator iterator) {
        super(iterator);
        this.iterator = iterator;
    }
    
    public void setErrorMessage(String message) {
        putProperty(PROP_ERROR_MESSAGE, message);
    }
    
    protected void updateState() {
        super.updateState();
        
        String[] contentData = getContentData();
        if (contentData != null) {
            putProperty(PROP_CONTENT_DATA, contentData);
            putProperty(PROP_CONTENT_SELECTED_INDEX, new Integer(getContentSelectedIndex()));
        }
    }

    private ServerChooserPanel getChooser() {
        if (chooser == null)
            chooser = new ServerChooserPanel();

        return chooser;
    }
    
    private String[] getContentData() {
        JComponent first;
        String[] firstContentData;
        
        first = (JComponent)getChooser().getComponent();
        firstContentData = (String[])first.getClientProperty(PROP_CONTENT_DATA);
        
        if (iterator.current().equals(getChooser())) {
            return firstContentData;
        } else {
            JComponent component = (JComponent)iterator.current().getComponent();
            String[] componentContentData = (String[])component.getClientProperty(PROP_CONTENT_DATA);
            if (componentContentData == null)
                return firstContentData;
            
            String[] contentData = new String[componentContentData.length + 1];
            contentData[0] = firstContentData[0];
            System.arraycopy(componentContentData, 0, contentData, 1, componentContentData.length);
            return contentData;
        }
    }

    private int getContentSelectedIndex() {
        if (iterator.current().equals(getChooser())) {
            return 0;
        } else {
            JComponent component = (JComponent)iterator.current().getComponent();
            Integer componentIndex = (Integer)component.getClientProperty(PROP_CONTENT_SELECTED_INDEX);
            if (componentIndex != null)
                return componentIndex.intValue() + 1;
            else
                return 1;
        }
    }
    
    private static class AddServerInstanceWizardIterator implements WizardDescriptor.InstantiatingIterator {
        private AddServerInstanceWizard wd;
        public boolean showingChooser;
        private WizardDescriptor.InstantiatingIterator iterator;
        private HashMap iterators;
        
        public AddServerInstanceWizardIterator() {
            showingChooser = true;
            iterators = new HashMap();
        }
        
        public void addChangeListener(ChangeListener l) {
        }
        
        public WizardDescriptor.Panel current() {
            if (showingChooser)
                return wd.getChooser();
            else
                if (iterator != null)
                    return iterator.current();
                else
                    return null;
        }
        
        public boolean hasNext() {
            if (showingChooser)
                return true;
            else
                if (iterator != null)
                    return iterator.hasNext();
                else
                    return false;
        }
        
        public boolean hasPrevious() {
            if (showingChooser)
                return false;
            else
                return true;
        }
        
        public String name() {
            return null;
        }
        
        public void nextPanel() {
            if (iterator == null)
                iterator = getServerIterator();
            else {
                if (!showingChooser)
                    iterator.nextPanel();
            }
            showingChooser = false;
        }
        
        public void previousPanel() {
            if (iterator.hasPrevious())
                iterator.previousPanel();
            else {
                showingChooser = true;
                iterator = null;
            }
        }
        
        public void removeChangeListener(ChangeListener l) {
        }
        
        public void uninitialize(WizardDescriptor wizard) {
        }

        public void initialize(WizardDescriptor wizard) {
            wd = (AddServerInstanceWizard)wizard;
            
            JComponent chooser = (JComponent)wd.getChooser().getComponent();
            chooser.putClientProperty(PROP_CONTENT_DATA, new String[] { 
                NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_ChooseServer"),
                NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_Ellipsis")
            });
        }

        public java.util.Set instantiate() throws java.io.IOException {
            if (iterator != null) {
                return iterator.instantiate();
            }
            else
                return null;
        }
        
        private WizardDescriptor.InstantiatingIterator getServerIterator() {
            Server server = getSelectedServer();
            if (server == null)
                return null;
            
            WizardDescriptor.InstantiatingIterator iterator = (WizardDescriptor.InstantiatingIterator)iterators.get(server);
            if (iterator != null)
                return iterator;
            
            OptionalDeploymentManagerFactory factory = server.getOptionalFactory();
            if (factory != null) {
                iterator = factory.getAddInstanceIterator();
                iterator.initialize(wd);
                iterators.put(server, iterator);
                return iterator;
            }
            else
                return null;
        }
        
        private Server getSelectedServer() {
            return (Server)wd.getProperty(PROP_SERVER);
        }
    }  
}
