/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.api.webservices;

import java.util.List;
import java.util.Collections;

import javax.swing.JPanel;

import org.openide.WizardValidationException;

/**
 *
 * @author Peter Williams
 */
public interface WsCompileEditorSupport {
    
    /** Editor fires this property event when the user edits a feature list
     */
    public static final String PROP_FEATURES_CHANGED = "featuresChanged";
    public static final String PROP_DEBUG_CHANGED = "debugChanged";
    public static final String PROP_OPTIMIZE_CHANGED = "optimizeChanged";
    public static final String PROP_VERBOSE_CHANGED = "verboseChanged";
    
    public WsCompileEditorSupport.Panel getWsCompileSupport();
    
    public interface Panel {

        /** The panel for the host
         */
        public JPanel getComponent();
        
        /** Call to initialize the properties in the editor panel
         */
        public void initValues(List/*ServiceSettings*/ settings);
        
        /** Validation entry point.
         */
        public void validatePanel() throws WizardValidationException;
    }
    
    public final class ServiceSettings {
        private String name;
        private StubDescriptor stubType;
        private List/*String*/ availableFeatures;
        private List/*String*/ importantFeatures;
        private String currentFeatures;
        private String newFeatures;
        
        public ServiceSettings(String sn, StubDescriptor st, String c, List a, List i) {
            name = sn;
            stubType = st;
            currentFeatures = newFeatures = c;
            availableFeatures = Collections.unmodifiableList(a);
            importantFeatures = Collections.unmodifiableList(i);
        }
        
        public String getServiceName() {
            return name;
        }
        
        public StubDescriptor getStubDescriptor() {
            return stubType;
        }
        
        public String getCurrentFeatures() {
            return currentFeatures;
        }
        
        public String getNewFeatures() {
            return newFeatures;
        }
        
        public List/*String*/ getAvailableFeatures() {
            return availableFeatures;
        }
        
        public List/*String*/ getImportantFeatures() {
            return importantFeatures;
        }
        
        public String toString() {
            return getServiceName();
        }
        
        public void setNewFeatures(String nf) {
            newFeatures = nf;
        }
    }
    
    public final class FeatureDescriptor {
        
        private String serviceName;
        private String features;
        
        public FeatureDescriptor(String serviceName, String features) {
            this.serviceName = serviceName;
            this.features = features;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public String getFeatures() {
            return features;
        }
    }
}
