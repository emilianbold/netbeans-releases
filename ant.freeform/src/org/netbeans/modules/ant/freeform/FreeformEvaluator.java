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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.w3c.dom.Element;

/**
 * Manages property evaluation for the freeform project.
 * Refreshes properties if (1) project.xml changes; (2) some *.properties changes.
 * @author Jesse Glick
 */
final class FreeformEvaluator implements PropertyEvaluator, AntProjectListener, PropertyChangeListener {

    private final FreeformProject project;
    private PropertyEvaluator delegate;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    
    public FreeformEvaluator(FreeformProject project) throws IOException {
        this.project = project;
        init();
        project.helper().addAntProjectListener(this);
    }
    
    private void init() throws IOException {
        if (delegate != null) {
            delegate.removePropertyChangeListener(this);
        }
        delegate = initEval();
        delegate.addPropertyChangeListener(this);
        if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            Util.err.log("properties for " + project.getProjectDirectory() + ": " + delegate.getProperties());
        }
    }
    
    private PropertyEvaluator initEval() throws IOException {
        PropertyProvider preprovider = project.helper().getStockPropertyPreprovider();
        List/*<PropertyProvider>*/ defs = new ArrayList();
        Element genldata = project.helper().getPrimaryConfigurationData(true);
        Element properties = Util.findElement(genldata, "properties", FreeformProjectType.NS_GENERAL); // NOI18N
        if (properties != null) {
            List/*<Element>*/ props = Util.findSubElements(properties);
            Iterator it = props.iterator();
            while (it.hasNext()) {
                Element e = (Element)it.next();
                if (e.getLocalName().equals("property")) { // NOI18N
                    defs.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap(e.getAttribute("name"), Util.findText(e)))); // NOI18N
                } else {
                    assert e.getLocalName().equals("property-file") : e;
                    String fname = Util.findText(e);
                    if (fname.indexOf("${") != -1) {
                        throw new IOException("XXX not yet implemented");
                    }
                    defs.add(PropertyUtils.propertiesFilePropertyProvider(project.helper().resolveFile(fname)));
                }
            }
        }
        return PropertyUtils.sequentialPropertyEvaluator(preprovider, (PropertyProvider[]) defs.toArray(new PropertyProvider[defs.size()]));
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
    
    private void fireChange(String prop) {
        PropertyChangeListener[] _listeners;
        synchronized (this) {
            if (listeners.isEmpty()) {
                return;
            }
            _listeners = (PropertyChangeListener[])listeners.toArray(new PropertyChangeListener[listeners.size()]);
        }
        PropertyChangeEvent ev = new PropertyChangeEvent(this, prop, null, null);
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
        fireChange(null);
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }
    
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        // If a properties file changes on disk, we refire that.
        fireChange(propertyChangeEvent.getPropertyName());
    }
    
}
