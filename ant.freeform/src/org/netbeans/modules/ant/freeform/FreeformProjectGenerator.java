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
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author  Jesse Glick, David Konecny
 */
public class FreeformProjectGenerator {

    // for now let's assume that one Ant script is used by all actions
    private static final String PROP_ANT_SCRIPT = "ant.script";
    
    private FreeformProjectGenerator() {}

    /**
     * Creates new Freeform project at the given folder with the given name,
     * target mappings, source folders, etc.
     * @param dir project folder; cannot be null
     * @param name name of new project; cannot be null
     * @param antScript Ant script file; can be null what means default Ant script location
     * @param mappings map of <String, List> where String is name of the IDE action
     *    and List is list of target names (<String's>)
     * @param sources list of SourceFolder instances
     * @param compUnits list of JavaCompilationUnit instances
     */
    public static AntProjectHelper createProject(File dir, String name, File antScript, Map mappings, List sources, List compUnits) throws IOException {
        FileObject dirFO = createProjectDir (dir);
        AntProjectHelper h = createProject(dirFO, PropertyUtils.getUsablePropertyName(name), antScript, mappings, sources, compUnits);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }
    
    /**
     * Finds AuxiliaryConfiguration for the given project helper.
     */
    public static AuxiliaryConfiguration getAuxiliaryConfiguration(AntProjectHelper helper) {
        try {
            Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
            AuxiliaryConfiguration aux = (AuxiliaryConfiguration)p.getLookup().lookup(AuxiliaryConfiguration.class);
            assert aux != null;
            return aux;
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
    /**
     * Read target mappings from project.
     * @return map of <String, List> where String is name of the IDE action
     *    and List is list of target names (<String's>)
     */
    public static Map/*<String,List>*/ getTargetMappings(AntProjectHelper helper) {
        Map map = new HashMap();
        Element genldata = helper.getPrimaryConfigurationData(true);
        Element actionsEl = Util.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
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
    
    /**
     * Update target mappings of the project. Project is left modified and 
     * you must save it explicitely.
     * @return mappings map of <String, List> where String is name of the IDE
     *     action and List is list of target names (<String's>)
     */
    public static void putTargetMappings(AntProjectHelper helper, Map/*<String,List>*/ mappings) {
        boolean useAntScript = getProperties(helper).getProperty(PROP_ANT_SCRIPT) != null;
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element actionsEl = Util.findElement(data, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
        if (actionsEl != null) {
            data.removeChild(actionsEl);
        }
        
        Element actions = doc.createElementNS(FreeformProjectType.NS_GENERAL, "ide-actions"); // NOI18N
        Iterator it = mappings.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            Element action = doc.createElementNS(FreeformProjectType.NS_GENERAL, "action"); //NOI18N
            action.setAttribute("name", key);
            Iterator it2 = ((List)mappings.get(key)).iterator();
            while (it2.hasNext()) {
                String value = (String)it2.next();
                Element target = doc.createElementNS(FreeformProjectType.NS_GENERAL, "target"); //NOI18N
                target.appendChild(doc.createTextNode(value)); // NOI18N
                action.appendChild(target);
            }
            if (useAntScript) {
                Element script = doc.createElementNS(FreeformProjectType.NS_GENERAL, "script"); //NOI18N
                script.appendChild(doc.createTextNode("${"+PROP_ANT_SCRIPT+"}")); // NOI18N
                action.appendChild(script);
            }
            actions.appendChild(action);
        }
        data.appendChild(actions);
        helper.putPrimaryConfigurationData(data, true);
    }

    private static AntProjectHelper createProject(final FileObject dirFO, final String name, final File antScript, final Map mappings, final List sources, final List compUnits) throws IOException {
        final AntProjectHelper[] h = new AntProjectHelper[1];
        final IOException[] ioe = new IOException[1];
        ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    Project p;
                    try {
                        h[0] = ProjectGenerator.createProject(dirFO, FreeformProjectType.TYPE, name);
                        p = ProjectManager.getDefault().findProject(dirFO);
                    } catch (IOException e) {
                        ioe[0] = e;
                        return;
                    }
                    AuxiliaryConfiguration aux = (AuxiliaryConfiguration)p.getLookup().lookup(AuxiliaryConfiguration.class);
                    assert aux != null;

                    Element data = h[0].getPrimaryConfigurationData(true);
                    Document doc = data.getOwnerDocument();

                    Element nm = doc.createElementNS(FreeformProjectType.NS_GENERAL, "name"); // NOI18N
                    nm.appendChild(doc.createTextNode(name)); // NOI18N
                    data.appendChild(nm);
                    if (antScript != null) {
                        Element props = doc.createElementNS(FreeformProjectType.NS_GENERAL, "properties"); // NOI18N
                        Element property = doc.createElementNS(FreeformProjectType.NS_GENERAL, "property"); // NOI18N
                        property.setAttribute("name", PROP_ANT_SCRIPT);
                        property.appendChild(doc.createTextNode(antScript.getAbsolutePath()));
                        props.appendChild(property);
                        data.appendChild(props);
                    }
                    h[0].putPrimaryConfigurationData(data, true);

                    putTargetMappings(h[0], mappings);
                    putSourceFolders(h[0], sources);
                    putJavaCompilationUnits(h[0], aux, compUnits);
                }
            }
        );

        if (ioe[0] != null) {
            throw ioe[0];
        }
        return h[0];
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

    /**
     * Structure describing source folder.
     */
    public static final class SourceFolder {
        public String label;
        public String type;
        public String location;
    }

    /**
     * Read source folders from the project.
     * @return list of SourceFolder instances
     */
    public static List/*<SourceFolder>*/ getSourceFolders(AntProjectHelper helper) {
        ArrayList list = new ArrayList();
        Element data = helper.getPrimaryConfigurationData(true);
        Element foldersEl = Util.findElement(data, "folders", FreeformProjectType.NS_GENERAL); // NOI18N
        List/*<Element>*/ sourceFolders = Util.findSubElements(foldersEl);
        Iterator it = sourceFolders.iterator();
        while (it.hasNext()) {
            Element sourceFolderEl = (Element)it.next();
            if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                continue;
            }
            SourceFolder sf = new SourceFolder();
            Element el = Util.findElement(sourceFolderEl, "label", FreeformProjectType.NS_GENERAL);
            if (el != null) {
                sf.label = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "type", FreeformProjectType.NS_GENERAL);
            if (el != null) {
                sf.type = Util.findText(el);
            }
            el = Util.findElement(sourceFolderEl, "location", FreeformProjectType.NS_GENERAL);
            if (el != null) {
                sf.location = Util.findText(el);
            }
            list.add(sf);
        }
        return list;
    }

    /**
     * Update source folders of the project. Project is left modified and you 
     * must save it explicitely.
     * @param sources list of SourceFolder instances
     */
    public static void putSourceFolders(AntProjectHelper helper, List/*<SourceFolder>*/ sources) {
        ArrayList list = new ArrayList();
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element foldersEl = Util.findElement(data, "folders", FreeformProjectType.NS_GENERAL); // NOI18N
        if (foldersEl == null) {
            foldersEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "folders"); // NOI18N
            data.appendChild(foldersEl);
        } else {
            List/*<Element>*/ sourceFolders = Util.findSubElements(foldersEl);
            Iterator it = sourceFolders.iterator();
            while (it.hasNext()) {
                Element sourceFolderEl = (Element)it.next();
                if (!sourceFolderEl.getLocalName().equals("source-folder")) { // NOI18N
                    continue;
                }
                foldersEl.removeChild(sourceFolderEl);
            }
        }
        Iterator it2 = sources.iterator();
        while (it2.hasNext()) {
            SourceFolder sf = (SourceFolder)it2.next();
            Element sourceFolderEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "source-folder"); // NOI18N
            Element el;
            if (sf.label != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "label"); // NOI18N
                el.appendChild(doc.createTextNode(sf.label)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.location != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "location"); // NOI18N
                el.appendChild(doc.createTextNode(sf.location)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.type != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "type"); // NOI18N
                el.appendChild(doc.createTextNode(sf.type)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            foldersEl.appendChild(sourceFolderEl);
        }
        helper.putPrimaryConfigurationData(data, true);
        
        // XXX: do not call it here
        putSourceView(helper, sources);
    }
    
    /**
     * Update source folders of the project. Project is left modified and you 
     * must save it explicitely.
     * @param sources list of SourceFolder instances
     */
    public static void putSourceView(AntProjectHelper helper, List/*<SourceFolder>*/ sources) {
        // XXX: sorry, for now it removes all elements and replace ours. TBD.
        ArrayList list = new ArrayList();
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element viewEl = Util.findElement(data, "view", FreeformProjectType.NS_GENERAL); // NOI18N
        if (viewEl == null) {
            viewEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "view"); // NOI18N
            data.appendChild(viewEl);
        }
        Element itemsEl = Util.findElement(viewEl, "items", FreeformProjectType.NS_GENERAL); // NOI18N
        if (itemsEl != null) {
            viewEl.removeChild(itemsEl);
        }
        itemsEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "items"); // NOI18N
        viewEl.appendChild(itemsEl);
        
        Iterator it2 = sources.iterator();
        while (it2.hasNext()) {
            SourceFolder sf = (SourceFolder)it2.next();
            Element sourceFolderEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "source-folder"); // NOI18N
            sourceFolderEl.setAttribute("style", "packages");
            Element el;
            if (sf.label != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "label"); // NOI18N
                el.appendChild(doc.createTextNode(sf.label)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            if (sf.location != null) {
                el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "location"); // NOI18N
                el.appendChild(doc.createTextNode(sf.location)); // NOI18N
                sourceFolderEl.appendChild(el);
            }
            itemsEl.appendChild(sourceFolderEl);
        }
        Element fileEl = doc.createElementNS(FreeformProjectType.NS_GENERAL, "source-file"); // NOI18N
        Element el = doc.createElementNS(FreeformProjectType.NS_GENERAL, "location"); // NOI18N
        // XXX: use REAL Ant script here!!!
        el.appendChild(doc.createTextNode("build.xml")); // NOI18N
        fileEl.appendChild(el);
        itemsEl.appendChild(fileEl);
        
        helper.putPrimaryConfigurationData(data, true);
    }

    /**
     * Read Java compilation units from the project.
     * @return list of JavaCompilationUnit instances
     */
    public static List/*<JavaCompilationUnit>*/ getJavaCompilationUnits(
            AntProjectHelper helper, AuxiliaryConfiguration aux) {
        ArrayList list = new ArrayList();
        Element data = aux.getConfigurationFragment("java-data", FreeformProjectType.NS_JAVA, true);
        List/*<Element>*/ cus = Util.findSubElements(data);
        Iterator it = cus.iterator();
        while (it.hasNext()) {
            Element cuEl = (Element)it.next();
            JavaCompilationUnit cu = new JavaCompilationUnit();
            Element el = Util.findElement(cuEl, "package-root", FreeformProjectType.NS_JAVA);
            if (el != null) {
                cu.packageRoot = Util.findText(el);
            }
            el = Util.findElement(cuEl, "classpath", FreeformProjectType.NS_JAVA);
            if (el != null) {
                cu.classpath = Util.findText(el);
            }
            el = Util.findElement(cuEl, "built-to", FreeformProjectType.NS_JAVA);
            if (el != null) {
                cu.output = Util.findText(el);
            }
            el = Util.findElement(cuEl, "source-level", FreeformProjectType.NS_JAVA);
            if (el != null) {
                cu.sourceLevel = Util.findText(el);
            }
            list.add(cu);
        }
        return list;
    }

    /**
     * Update Java compilation units of the project. Project is left modified
     * and you must save it explicitely.
     * @param compUnits list of JavaCompilationUnit instances
     */
    public static void putJavaCompilationUnits(AntProjectHelper helper, 
            AuxiliaryConfiguration aux, List/*<JavaCompilationUnit>*/ compUnits) {
        ArrayList list = new ArrayList();
        Element data = aux.getConfigurationFragment("java-data", FreeformProjectType.NS_JAVA, true);
        if (data == null) {
            data = helper.getPrimaryConfigurationData(true).getOwnerDocument().
                createElementNS(FreeformProjectType.NS_JAVA, "java-data");
        }
        Document doc = data.getOwnerDocument();
        List cus = Util.findSubElements(data); // NOI18N
        Iterator it = cus.iterator();
        while (it.hasNext()) {
            Element cuEl = (Element)it.next();
            data.removeChild(cuEl);
        }
        Iterator it2 = compUnits.iterator();
        while (it2.hasNext()) {
            Element cuEl = doc.createElementNS(FreeformProjectType.NS_JAVA, "compilation-unit"); // NOI18N
            data.appendChild(cuEl);
            JavaCompilationUnit cu = (JavaCompilationUnit)it2.next();
            Element el;
            if (cu.packageRoot != null) {
                el = doc.createElementNS(FreeformProjectType.NS_JAVA, "package-root"); // NOI18N
                el.appendChild(doc.createTextNode(cu.packageRoot));
                cuEl.appendChild(el);
            }
            if (cu.classpath != null) {
                el = doc.createElementNS(FreeformProjectType.NS_JAVA, "classpath"); // NOI18N
                el.appendChild(doc.createTextNode(cu.classpath));
                el.setAttribute("mode", "compile");
                cuEl.appendChild(el);
            }
            if (cu.output != null) {
                el = doc.createElementNS(FreeformProjectType.NS_JAVA, "built-to"); // NOI18N
                el.appendChild(doc.createTextNode(cu.output));
                cuEl.appendChild(el);
            }
            if (cu.sourceLevel != null) {
                el = doc.createElementNS(FreeformProjectType.NS_JAVA, "source-level"); // NOI18N
                el.appendChild(doc.createTextNode(cu.sourceLevel));
                cuEl.appendChild(el);
            }
        }
        aux.putConfigurationFragment(data, true);
    }

    //XXX: The <property-file> elements are ignored at the moment.
    /**
     * Read all <property> elements and return them as Properties instance.
     */
    public static Properties getProperties(AntProjectHelper helper) {
        Properties props = new Properties();
        Element data = helper.getPrimaryConfigurationData(true);
        Element propertiesEl = Util.findElement(data, "properties", FreeformProjectType.NS_GENERAL); // NOI18N
        if (propertiesEl == null) {
            return props;
        }
        List/*<Element>*/ subElms = Util.findSubElements(propertiesEl);
        Iterator it = subElms.iterator();
        while (it.hasNext()) {
            Element el = (Element)it.next();
            if (!el.getLocalName().equals("property")) { // NOI18N
                continue;
            }
            String key = el.getAttribute("name");
            String value = Util.findText(el);
            props.put(key, value);
        }
        return props;
    }

    /**
     * Structure describing compilation unit.
     */
    public static final class JavaCompilationUnit {
        public String packageRoot;
        public String classpath;
        public String output;
        public String sourceLevel;
    }
    
}
