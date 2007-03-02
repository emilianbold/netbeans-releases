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

package org.netbeans.modules.ant.freeform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.ant.freeform.spi.support.Util;
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
    private final List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    private final Set<PropertyEvaluator> intermediateEvaluators = new HashSet<PropertyEvaluator>();
    
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
        if (org.netbeans.modules.ant.freeform.Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            org.netbeans.modules.ant.freeform.Util.err.log("properties for " + project.getProjectDirectory() + ": " + delegate.getProperties());
        }
    }
    
    private PropertyEvaluator initEval() throws IOException {
        // Stop listening to old intermediate evaluators.
        Iterator<PropertyEvaluator> ieIt = intermediateEvaluators.iterator();
        while (ieIt.hasNext()) {
            ieIt.next().removePropertyChangeListener(this);
            ieIt.remove();
        }
        PropertyProvider preprovider = project.helper().getStockPropertyPreprovider();
        List<PropertyProvider> defs = new ArrayList<PropertyProvider>();
        Element genldata = project.getPrimaryConfigurationData();
        Element properties = Util.findElement(genldata, "properties", FreeformProjectType.NS_GENERAL); // NOI18N
        if (properties != null) {
            for (Element e : Util.findSubElements(properties)) {
                if (e.getLocalName().equals("property")) { // NOI18N
                    String val = Util.findText(e);
                    if (val == null) {
                        val = "";
                    }
                    defs.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap(e.getAttribute("name"), val))); // NOI18N
                } else {
                    assert e.getLocalName().equals("property-file") : e;
                    String fname = Util.findText(e);
                    if (fname.contains("${")) { // NOI18N
                        // Tricky (#48230): need to listen to changes in the location of the file as well as its contents.
                        PropertyEvaluator intermediate = PropertyUtils.sequentialPropertyEvaluator(preprovider, defs.toArray(new PropertyProvider[defs.size()]));
                        fname = intermediate.evaluate(fname);
                        // Listen to changes in it, too.
                        intermediate.addPropertyChangeListener(this);
                        intermediateEvaluators.add(intermediate);
                    }
                    defs.add(PropertyUtils.propertiesFilePropertyProvider(project.helper().resolveFile(fname)));
                }
            }
        }
        return PropertyUtils.sequentialPropertyEvaluator(preprovider, defs.toArray(new PropertyProvider[defs.size()]));
    }
    
    public String getProperty(String prop) {
        return delegate.getProperty(prop);
    }
    
    public String evaluate(String text) {
        return delegate.evaluate(text);
    }
    
    public Map<String,String> getProperties() {
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
            _listeners = listeners.toArray(new PropertyChangeListener[listeners.size()]);
        }
        PropertyChangeEvent ev = new PropertyChangeEvent(this, prop, null, null);
        for (PropertyChangeListener l : _listeners) {
            l.propertyChange(ev);
        }
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        fireAnyChange();
    }
    
    private void fireAnyChange() {
        try {
            init();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        } catch (RuntimeException ex) {
            // Something else? E.g. IAE when parsing <properties> block.
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        }
        fireChange(null);
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // ignore
    }
    
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        Object source = propertyChangeEvent.getSource();
        assert source instanceof PropertyEvaluator : source;
        if (intermediateEvaluators.contains(source)) {
            // A <property-file> may have changed location. Generally need to rebuild the list of definers.
            fireAnyChange();
        } else {
            // If a properties file changes on disk, we refire that from the delegate.
            assert source == delegate : "Got change from " + source + " rather than current delegate " + delegate;
            fireChange(propertyChangeEvent.getPropertyName());
        }
    }
    
}
