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

package org.netbeans.modules.ant.freeform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.ant.freeform.ui.ProjectCustomizerProvider;
import org.netbeans.modules.ant.freeform.ui.View;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;

/**
 * One freeform project.
 * @author Jesse Glick
 */
public final class FreeformProject implements Project {
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    
    public FreeformProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = new PropertyEvaluatorProxy();
        lookup = initLookup();
    }
    
    public AntProjectHelper helper() {
        return helper;
    }
    
    private PropertyEvaluator initEval() throws IOException {
        PropertyProvider preprovider = helper.getStockPropertyPreprovider();
        List/*<PropertyProvider>*/ defs = new ArrayList();
        Element genldata = helper.getPrimaryConfigurationData(true);
        Element properties = Util.findElement(genldata, "properties", FreeformProjectType.NS_GENERAL); // NOI18N
        if (properties != null) {
            List/*<Element>*/ props = Util.findSubElements(properties);
            Iterator it = props.iterator();
            while (it.hasNext()) {
                Element e = (Element)it.next();
                if (e.getLocalName().equals("property")) { // NOI18N
                    defs.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap(e.getAttribute("name"), Util.findText(e))));
                } else {
                    assert e.getLocalName().equals("property-file") : e;
                    String fname = Util.findText(e);
                    if (fname.indexOf("${") != -1) {
                        throw new IOException("XXX not yet implemented");
                    }
                    FileObject propfile = helper.resolveFileObject(fname);
                    if (propfile != null) {
                        // XXX need to listen to changes in this file too
                        Properties p = new Properties();
                        InputStream is = propfile.getInputStream();
                        try {
                            p.load(is);
                        } finally {
                            is.close();
                        }
                        defs.add(PropertyUtils.fixedPropertyProvider(p));
                    }
                }
            }
        }
        return PropertyUtils.sequentialPropertyEvaluator(preprovider, (PropertyProvider[]) defs.toArray(new PropertyProvider[defs.size()]));
    }
    
    private Lookup initLookup() throws IOException {
        Classpaths cp = new Classpaths(this);
        return Lookups.fixed(new Object[] {
            new Info(), // ProjectInformation
            new FreeformSources(this), // Sources
            new Actions(this), // ActionProvider
            new View(this), // LogicalViewProvider
            cp, // ClassPathProvider
            new SourceLevelQueryImpl(this), // SourceLevelQueryImplementation
            new SourceForBinaryQueryImpl(this), // SourceForBinaryQueryImplementation
            new WebModules(this), // WebModuleProvider
            new ProjectCustomizerProvider(this, helper, eval), // CustomizerProvider
            new OpenHook(cp), // ProjectOpenedHook
            helper().createAuxiliaryConfiguration(), // AuxiliaryConfiguration
            helper().createCacheDirectoryProvider(), // CacheDirectoryProvider
            new PrivilegedTemplatesImpl(),           // List of templates in New action popup
        });
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    private final class Info implements ProjectInformation {
        
        public Info() {}
        
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }
        
        public String getDisplayName() {
            Element genldata = helper.getPrimaryConfigurationData(true);
            Element nameEl = Util.findElement(genldata, "name", FreeformProjectType.NS_GENERAL); // NOI18N
            return Util.findText(nameEl);
        }
        
        public Icon getIcon() {
            return new ImageIcon(Utilities.loadImage("org/netbeans/modules/ant/freeform/resources/AntIcon.gif", true)); // NOI18N
        }
        
        public Project getProject() {
            return FreeformProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // XXX
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // XXX
        }
        
    }
    
    private final class OpenHook extends ProjectOpenedHook {
        
        private final Classpaths cp;
        
        public OpenHook(Classpaths cp) {
            this.cp = cp;
        }
        
        protected void projectOpened() {
            cp.opened();
        }
        
        protected void projectClosed() {
            cp.closed();
        }
        
    }
    
    /**
     * XXX: this is HOTFIX to refresh properties after the project creation.
     */
    private final class PropertyEvaluatorProxy implements PropertyEvaluator, AntProjectListener {
        
        private PropertyEvaluator delegate;
        private final List/*<ChangeListener>*/ listeners = new ArrayList();
        
        public PropertyEvaluatorProxy() throws IOException {
            init();
            helper().addAntProjectListener(this);
        }
        
        private void init() throws IOException {
            delegate = initEval();
            if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                Util.err.log("properties for " + getProjectDirectory() + ": " + delegate.getProperties());
            }
        }
        
        public String getProperty(String prop) {
            return delegate.getProperty(prop);
        }

        public String evaluate(String text) {
            return delegate.evaluate(text);
        }

        public Map/*<String,String>*/ getProperties() {
            return delegate.getProperties();
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            listeners.add(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            listeners.remove(listener);
        }
        
        private void fireChange() {
            PropertyChangeListener[] _listeners;
            synchronized (this) {
                if (listeners.isEmpty()) {
                    return;
                }
                _listeners = (PropertyChangeListener[])listeners.toArray(new PropertyChangeListener[listeners.size()]);
            }
            PropertyChangeEvent ev = new PropertyChangeEvent(this, null, null, null);
            for (int i = 0; i < _listeners.length; i++) {
                _listeners[i].propertyChange(ev);
            }
        }
        
        public void configurationXmlChanged(AntProjectEvent ev) {
            try {
                init();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }
            fireChange();
        }
        
        public void propertiesChanged(AntProjectEvent ev) {
            // ignore
        }
        
    }
    
    
    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
        };
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }
    
}
