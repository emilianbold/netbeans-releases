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

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.SourcesHelper;
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
final class FreeformProject implements Project {
    
    public static final String NS = "http://www.netbeans.org/ns/freeform-project/1"; // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    
    public FreeformProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = initEval();
        lookup = initLookup();
    }
    
    private PropertyEvaluator initEval() throws IOException {
        PropertyProvider preprovider = helper.getStockPropertyPreprovider();
        List/*<PropertyProvider>*/ defs = new ArrayList();
        Element genldata = helper.getPrimaryConfigurationData(true);
        Element properties = Util.findElement(genldata, "properties", NS); // NOI18N
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
        return Lookups.fixed(new Object[] {
            new Info(),
            initSources(),
            new Actions(),
        });
    }
    
    private Sources initSources() {
        final SourcesHelper h = new SourcesHelper(helper, evaluator());
        Element genldata = helper.getPrimaryConfigurationData(true);
        Element foldersE = Util.findElement(genldata, "folders", NS); // NOI18N
        if (foldersE != null) {
            List/*<Element>*/ folders = Util.findSubElements(foldersE);
            Iterator it = folders.iterator();
            while (it.hasNext()) {
                Element folderE = (Element)it.next();
                Element locationE = Util.findElement(folderE, "location", NS); // NOI18N
                String location = Util.findText(locationE);
                if (folderE.getLocalName().equals("build-folder")) { // NOI18N
                    h.addNonSourceRoot(location);
                } else {
                    assert folderE.getLocalName().equals("source-folder") : folderE;
                    Element nameE = Util.findElement(folderE, "label", NS); // NOI18N
                    String name = Util.findText(nameE);
                    Element typeE = Util.findElement(folderE, "type", NS); // NOI18N
                    if (typeE != null) {
                        String type = Util.findText(typeE);
                        h.addTypedSourceRoot(location, type, name, null, null);
                    } else {
                        h.addPrincipalSourceRoot(location, name, null, null);
                    }
                }
            }
        }
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                h.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        return h.createSources();
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
            Element genldata = helper.getPrimaryConfigurationData(true);
            Element nameEl = Util.findElement(genldata, "name", NS); // NOI18N
            return Util.findText(nameEl);
        }
        
        public String getDisplayName() {
            return getName();
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
    
    private final class Actions implements ActionProvider {
        
        public Actions() {}
        
        public String[] getSupportedActions() {
            Element genldata = helper.getPrimaryConfigurationData(true);
            Element actionsEl = Util.findElement(genldata, "ide-actions", NS); // NOI18N
            List/*<Element>*/ actions = Util.findSubElements(actionsEl);
            List/*<String>*/ names = new ArrayList(actions.size());
            Iterator it = actions.iterator();
            while (it.hasNext()) {
                Element actionEl = (Element)it.next();
                if (Util.findElement(actionEl, "context", NS) != null) { // NOI18N
                    throw new UnsupportedOperationException("XXX No support for <context> yet"); // NOI18N
                }
                names.add(actionEl.getAttribute("name")); // NOI18N
            }
            return (String[])names.toArray(new String[names.size()]);
        }
        
        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            // XXX check context, and also perhaps existence of script
            return true;
        }
        
        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            Element genldata = helper.getPrimaryConfigurationData(true);
            Element actionsEl = Util.findElement(genldata, "ide-actions", NS); // NOI18N
            List/*<Element>*/ actions = Util.findSubElements(actionsEl);
            Iterator it = actions.iterator();
            while (it.hasNext()) {
                Element actionEl = (Element)it.next();
                if (actionEl.getAttribute("name").equals(command)) { // NOI18N
                    String script;
                    Element scriptEl = Util.findElement(actionEl, "script", NS); // NOI18N
                    if (scriptEl != null) {
                        script = Util.findText(scriptEl);
                    } else {
                        script = "build.xml"; // NOI18N
                    }
                    String scriptLocation = evaluator().evaluate(script);
                    FileObject scriptFile = helper.resolveFileObject(scriptLocation);
                    if (scriptFile == null) {
                        return;
                    }
                    List/*<Element>*/ targets = Util.findSubElements(actionEl);
                    List/*<String>*/ targetNames = new ArrayList(targets.size());
                    Iterator it2 = targets.iterator();
                    while (it2.hasNext()) {
                        Element targetEl = (Element)it2.next();
                        targetNames.add(Util.findText(targetEl));
                    }
                    String[] targetNameArray;
                    if (!targetNames.isEmpty()) {
                        targetNameArray = (String[])targetNames.toArray(new String[targetNames.size()]);
                    } else {
                        // Run default target.
                        targetNameArray = null;
                    }
                    try {
                        ActionUtils.runTarget(scriptFile, targetNameArray, null);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return;
                }
            }
            throw new IllegalArgumentException("Unrecognized command: " + command); // NOI18N
        }
        
    }
    
}
