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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author  Jesse Glick, David Konecny
 */
public class FreeformProjectGenerator {
    
    private FreeformProjectGenerator() {}

    public static AntProjectHelper createProject(File dir, String name, Map mappings) throws IOException {
        FileObject dirFO = createProjectDir (dir);
        AntProjectHelper h = createProject(dirFO, PropertyUtils.getUsablePropertyName(name), mappings);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }
    
    public static Map/*<String,List>*/ getTargetMappings(AntProjectHelper helper) {
        Map map = new HashMap();
        Element genldata = helper.getPrimaryConfigurationData(true);
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProject.NS); // NOI18N
        List/*<Element>*/ actions = Util.findSubElements(actionsEl);
        Iterator it = actions.iterator();
        while (it.hasNext()) {
            Element actionEl = (Element)it.next();
            String name = actionEl.getAttribute("name"); // NOI18N
            List/*<Element>*/ targets = Util.findSubElements(actionEl);
            List/*<String>*/ targetNames = new ArrayList(targets.size());
            Iterator it2 = targets.iterator();
            while (it2.hasNext()) {
                Element targetEl = (Element)it2.next();
                if (!targetEl.getLocalName().equals("target")) { // NOI18N
                    continue;
                }
                targetNames.add(Util.findText(targetEl));
            }
            map.put(name, targetNames);
        }
        return map;
    }
    
    public static void putTargetMappings(AntProjectHelper helper, Map/*<String,List>*/ mappings) {
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element actionsEl = Util.findElement(data, "ide-actions", FreeformProject.NS); // NOI18N
        if (actionsEl != null) {
            data.removeChild(actionsEl);
        }
        
        Element actions = doc.createElementNS(FreeformProject.NS, "ide-actions"); // NOI18N
        Iterator it = mappings.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            Element action = doc.createElementNS(FreeformProject.NS, "action"); //NOI18N
            action.setAttribute("name", key);
            Iterator it2 = ((List)mappings.get(key)).iterator();
            while (it2.hasNext()) {
                String value = (String)it2.next();
                Element target = doc.createElementNS(FreeformProject.NS, "target"); //NOI18N
                target.appendChild(doc.createTextNode(value)); // NOI18N
                action.appendChild(target);
            }
            actions.appendChild(action);
        }
        data.appendChild(actions);
        helper.putPrimaryConfigurationData(data, true);
    }

    private static AntProjectHelper createProject(FileObject dirFO, String name, Map mappings) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, FreeformProjectType.TYPE, name);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        
        Element nm = doc.createElementNS(FreeformProject.NS, "name"); // NOI18N
        nm.appendChild(doc.createTextNode(name)); // NOI18N
        data.appendChild(nm);
        h.putPrimaryConfigurationData(data, true);
        
        putTargetMappings(h, mappings);
        
        return h;
    }

    private static FileObject createProjectDir (File dir) throws IOException {
        dir.mkdirs();
        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF;
        dirFO.getFileSystem().refresh(false);
        dirFO = FileUtil.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + dir;
        assert dirFO.isFolder() : "Not really a dir: " + dir;
        //assert dirFO.getChildren().length == 0 : "Dir must have been empty: " + dir;
        return dirFO;
    }
    
}
