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

package org.netbeans.modules.j2ee.earproject.ui;

import java.util.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.nodes.*;

import org.netbeans.modules.j2ee.dd.api.application.*;

import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.common.ui.customizer.VisualClassPathItem;


import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.Project;
import org.openide.util.RequestProcessor;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;


import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.common.ui.customizer.ArchiveProjectProperties;
import org.netbeans.api.project.FileOwnerQuery;
/**
 * List of children of a containing node.
 * Each child node is represented by one key from some data model.
 * Remember to document what your permitted keys are!
 * Edit this template to work with the classes and logic of your data model.
 * @author vkraemer
 */
public class LogicalViewChildren extends Children.Keys  implements AntProjectListener {
    
    private final AntProjectHelper model;
    
    public LogicalViewChildren(AntProjectHelper model) {
        if (null == model)
            throw new IllegalArgumentException("model");
        this.model = model;
    }
    
    protected void addNotify() {
        super.addNotify();
        // set the children to use:
        updateKeys();
        // and listen to changes in the model too:
        model.addAntProjectListener(this);
    }
    
    private void updateKeys() {
        List keys = Collections.EMPTY_LIST;

        Project p = FileOwnerQuery.getOwner(model.getProjectDirectory());
        EarProject ep = (EarProject) p.getLookup().lookup(EarProject.class);
        ArchiveProjectProperties epp = ep.getProjectProperties();
        Object t = epp.get(ArchiveProjectProperties.JAR_CONTENT_ADDITIONAL);
        if (!(t instanceof List)) {
            assert false : "jar content isn't a List???";
            return;
        }
        List vcpis = (List) t;
        Iterator iter = vcpis.iterator();
        keys = new ArrayList();
        while (iter.hasNext()) {
            t = iter.next();
            if (! (t instanceof VisualClassPathItem)) {
                assert false : "jar content element isn't a VCPI?????";
                continue;
            }
            VisualClassPathItem vcpi = (VisualClassPathItem) t;
            Object obj = vcpi.getObject();
            AntArtifact aa;
            if (obj instanceof AntArtifact) {
                aa = (AntArtifact) obj;
                p = aa.getProject();            
            }
            else continue;
            J2eeModuleProvider jmp = (J2eeModuleProvider) p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) keys.add(vcpi);
            
        }
        
       
            
        
        // get your keys somehow from the data model:
        //MyDataElement[] keys = model.getChildren();
        // you can also use Collection rather than an array
        setKeys(keys);
    }
    
    protected void removeNotify() {
        model.removeAntProjectListener(this);
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
    
    protected Node[] createNodes(Object key) {
        // interpret your key here...usually one node generated, but could be zero or more
        VisualClassPathItem vcpi = (VisualClassPathItem) key;
        return new Node[] { new ModuleNode(vcpi, model) };
    }
    
    public void modelChanged(Object ev) {
        // your data model changed, so update the children to match:
        updateKeys();
    }
    
    public void configurationXmlChanged(AntProjectEvent ape) {
        // unsafe to call Children.setKeys() while holding a mutext
        // here the caller holds ProjectManager.mutex() read access
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                updateKeys();
            }
        });
    }
 
    public void propertiesChanged(final AntProjectEvent ape) {
        
        // unsafe to call Children.setKeys() while holding a mutext
        // here the caller holds ProjectManager.mutex() read access
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                updateKeys();
            }
        });
        
    }

}
