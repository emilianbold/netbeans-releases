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

package org.netbeans.modules.java.project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractListModel;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

public class BrokenReferencesModel extends AbstractListModel {

    private String[] props;
    private String[] platformsProps;
    private AntProjectHelper helper;
    private ReferenceHelper resolver;
    private ArrayList references;

    public BrokenReferencesModel(AntProjectHelper helper, 
            ReferenceHelper resolver, String[] props, String[] platformsProps) {
        this.props = props;
        this.platformsProps = platformsProps;
        this.resolver = resolver;
        this.helper = helper;
        references = new ArrayList();
        refresh();
    }
    
    public void refresh() {
        Set all = new LinkedHashSet();
        Set s = getReferences(helper, helper.getStandardPropertyEvaluator(), props, false);
        all.addAll(s);
        s = getPlatforms(helper.getStandardPropertyEvaluator(), platformsProps, false);
        all.addAll(s);
        updateReferencesList(references, all);
        this.fireContentsChanged(this, 0, getSize());
    }

    public Object getElementAt(int index) {
        OneReference or = getOneReference(index);
        String bundleID;
        switch (or.type) {
            case REF_TYPE_LIBRARY:
            case REF_TYPE_LIBRARY_CONTENT:
                bundleID = "LBL_BrokenLinksCustomizer_BrokenLibrary"; // NOI18N
                break;
            case REF_TYPE_PROJECT:
                bundleID = "LBL_BrokenLinksCustomizer_BrokenProjectReference"; // NOI18N
                break;
            case REF_TYPE_FILE:
                bundleID = "LBL_BrokenLinksCustomizer_BrokenFileReference";
                break;
            case REF_TYPE_PLATFORM:
                bundleID = "LBL_BrokenLinksCustomizer_BrokenPlatform";
                break;
            default:
                assert false;
                return null;
        }
        return NbBundle.getMessage(BrokenReferencesCustomizer.class, bundleID, or.getDisplayID());
    }

    public String getDesciption(int index) {
        OneReference or = getOneReference(index);
        String bundleID;
        switch (or.type) {
            case REF_TYPE_LIBRARY:
                bundleID = "LBL_BrokenLinksCustomizer_BrokenLibraryDesc"; // NOI18N
                break;
            case REF_TYPE_LIBRARY_CONTENT:
                bundleID = "LBL_BrokenLinksCustomizer_BrokenLibraryContentDesc"; // NOI18N
                break;
            case REF_TYPE_PROJECT:
                bundleID = "LBL_BrokenLinksCustomizer_BrokenProjectReferenceDesc"; // NOI18N
                break;
            case REF_TYPE_FILE:
                bundleID = "LBL_BrokenLinksCustomizer_BrokenFileReferenceDesc";
                break;
            case REF_TYPE_PLATFORM:
                bundleID = "LBL_BrokenLinksCustomizer_BrokenPlatformDesc";
                break;
            default:
                assert false;
                return null;
        }
        return NbBundle.getMessage(BrokenReferencesCustomizer.class, bundleID, or.getDisplayID());
    }

    public OneReference getOneReference(int index) {
        assert index>=0 && index<references.size();
        return (OneReference)references.get(index);
    }
    
    public boolean isBroken(int index) {
        OneReference or = (OneReference)references.get(index);
        return or.broken;
    }
    
    public int getSize() {
        return references.size();
    }

    public static boolean isBroken(AntProjectHelper helper, PropertyEvaluator evaluator, String[] props, String[] platformsProps) {
        Set s = getReferences(helper, evaluator, props, true);
        if (s.size() > 0) {
            return true;
        }
        s = getPlatforms(evaluator, platformsProps, true);
        return s.size() > 0;
    }

    private static Set getReferences(AntProjectHelper helper, PropertyEvaluator evaluator, String[] ps, boolean abortAfterFirstProblem) {
        Set set = new LinkedHashSet();
        StringBuffer all = new StringBuffer();
        for (int i=0; i<ps.length; i++) {
            // evaluate given property and tokenize it
            
            String prop = evaluator.getProperty(ps[i]);
            if (prop == null) {
                continue;
            }
            String[] vals = PropertyUtils.tokenizePath(prop);
                        
            // no check whether after evaluating there are still some 
            // references which could not be evaluated
            for (int j=0; j<vals.length; j++) {
                // we are checking only: project reference, file reference, library reference
                if (!(vals[j].startsWith("${file.reference.") || vals[j].startsWith("${project.") || vals[j].startsWith("${libs."))) {
                    all.append(vals[j]);
                    continue;
                }
                if (vals[j].startsWith("${project.")) {
                    // something in the form: "${project.<projID>}/dist/foo.jar"
                    String val = vals[j].substring(2, vals[j].indexOf('}'));
                    set.add(new OneReference(REF_TYPE_PROJECT, val, true));
                } else {
                    int type = REF_TYPE_LIBRARY;
                    if (vals[j].startsWith("${file.reference")) {
                        type = REF_TYPE_FILE;
                    }
                    String val = vals[j].substring(2, vals[j].length()-1);
                    set.add(new OneReference(type, val, true));
                }
                if (abortAfterFirstProblem) {
                    break;
                }
            }
            if (set.size() > 0 && abortAfterFirstProblem) {
                break;
            }
        }
        
        // Check also that all referenced project really exist and are reachable.
        // If they are not report them as broken reference.
        // XXX: there will be API in PropertyUtils for listing of Ant 
        // prop names in String. Consider using it here.
        Iterator it = evaluator.getProperties().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            if (key.startsWith("project.")) { // NOI18N
                File f = getFile(helper, evaluator, value);
                if (f.exists()) {
                    continue;
                }
                // Check that the value is really used by some property.
                // If it is not then ignore such a project.
                if (all.indexOf(value) == -1) {
                    continue;
                }
                set.add(new OneReference(REF_TYPE_PROJECT, key, true));
            }
            else if (key.startsWith("file.reference")) {    //NOI18N
                File f = getFile(helper, evaluator, value);
                if (f.exists() || all.indexOf(value) == -1) {
                    continue;
                }
                set.add(new OneReference(REF_TYPE_FILE, key, true));
            }
        }
        
        //Check for libbraries with broken classpath content
        Set usedLibraries = new HashSet ();
        Pattern libPattern = Pattern.compile("\\$\\{(lib.[-._a-zA-Z0-9]+.classpath)\\}"); //NOI18N
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        for (int i=0; i<ps.length; i++) { 
            String propertyValue = ep.getProperty(ps[i]);
            if (propertyValue != null) {
                String[] vals = PropertyUtils.tokenizePath(propertyValue);
                for (int j=0; j<vals.length; j++) {
                    Matcher m = libPattern.matcher(vals[j]);
                    if (m.matches()) {
                        usedLibraries.add (m.group(1));
                    }
                }
            }
        }
        for (Iterator lit = usedLibraries.iterator(); lit.hasNext(); ) {
            String libraryRef = (String) lit.next ();
            String libraryName = libraryRef.substring(5,libraryRef.length()-10);
            Library lib = LibraryManager.getDefault().getLibrary (libraryName);
            if (lib == null) {
                set.add(new OneReference(REF_TYPE_LIBRARY, libraryRef, true));
            }
            else {
                //XXX: Should check all the volumes (sources, javadoc, ...)?
                List/*<URL>*/ cp = lib.getContent("classpath");    //NOI18N
                for (Iterator cpIt = cp.iterator(); cpIt.hasNext();) {
                    URL url = (URL) cpIt.next();
                    if ("jar".equals(url.getProtocol())) {   //NOI18N
                        url = FileUtil.getArchiveFile (url);
                    }
                    if (URLMapper.findFileObject (url) == null) {
                        set.add(new OneReference(REF_TYPE_LIBRARY_CONTENT, libraryRef, true));
                    }
                }
            }
        }
        
        return set;
    }
    
    private static File getFile (AntProjectHelper helper, PropertyEvaluator evaluator, String name) {
        if (helper != null) {
            return new File(helper.resolvePath(name));
        } else {
            File f = new File(name);
            if (!f.exists()) {
                // perhaps the file is relative?
                String basedir = evaluator.getProperty("basedir");
                assert basedir != null;
                f = new File(new File(basedir), name);
            }
            return f;
        }
    }

    private static Set getPlatforms(PropertyEvaluator evaluator, String[] platformsProps, boolean abortAfterFirstProblem) {
        Set set = new LinkedHashSet();
        for (int i=0; i<platformsProps.length; i++) {
            String prop = evaluator.getProperty(platformsProps[i]);
            if (prop == null) {
                continue;
            }
            if (!existPlatform(prop)) {
                
                // XXX: the J2ME stores in project.properties also platform 
                // display name and so show this display name instead of just
                // prop ID if available.
                if (evaluator.getProperty(platformsProps[i]+".description") != null) {
                    prop = evaluator.getProperty(platformsProps[i]+".description");
                }
                
                set.add(new OneReference(REF_TYPE_PLATFORM, prop, true));
            }
            if (set.size() > 0 && abortAfterFirstProblem) {
                break;
            }
        }
        return set;
    }
    
    private static void updateReferencesList(List oldBroken, Set newBroken) {
        Iterator it = oldBroken.iterator();
        while (it.hasNext()) {
            OneReference or = (OneReference)it.next();
            if (newBroken.contains(or)) {
                or.broken = true;
            } else {
                or.broken = false;
            }
        }
        it = newBroken.iterator();
        while (it.hasNext()) {
            OneReference or = (OneReference)it.next();
            if (!oldBroken.contains(or)) {
                oldBroken.add(or);
            }
        }
    }
    
    private static boolean existPlatform(String platform) {
        if (platform.equals("default_platform")) { // NOI18N
            return true;
        }
        JavaPlatform plats[] = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (int i=0; i<plats.length; i++) {
            // XXX: this should be defined as PROPERTY somewhere
            if (platform.equals(plats[i].getProperties().get("platform.ant.name")) &&
                plats[i].getInstallFolders().size()>0) { // NOI18N
                return true;
            }
        }
        return false;
    }

    // XXX: perhaps could be moved to ReferenceResolver. 
    // But nobody should need it so it is here for now.
    void updateReference(int index, File file) {
        updateReference0(index, file);
        // #48210 - check whether the folder does not contain other jars
        // which could auto resolve some broken links:
        OneReference or = getOneReference(index);
        if (or.getType() != REF_TYPE_FILE) {
            return;
        }
        for (int i=0; i<getSize(); i++) {
            if (!isBroken(i) || i == index) {
                continue;
            }
            or = getOneReference(i);
            if (or.getType() != REF_TYPE_FILE) {
                continue;
            }
            File f = new File(file.getParentFile(), or.getDisplayID());
            if (f.exists()) {
                updateReference0(i, f);
            }
        }
    }
    
    private void updateReference0(int index, File file) {
        final String reference = getOneReference(index).ID;
        FileObject myProjDirFO = helper.getProjectDirectory();
        File myProjDir = FileUtil.toFile(myProjDirFO);
        final String propertiesFile;
        final String path;
        if (CollocationQuery.areCollocated(myProjDir, file)) {
            propertiesFile = AntProjectHelper.PROJECT_PROPERTIES_PATH;
            path = PropertyUtils.relativizeFile(myProjDir, file);
            assert path != null : "expected relative path from " + myProjDir + " to " + file; // NOI18N
        } else {
            propertiesFile = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
            path = file.getAbsolutePath();
        }
        Project p;
        try {
            p = ProjectManager.getDefault().findProject(myProjDirFO);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            p = null;
        }
        final Project proj = p;
        ProjectManager.mutex().postWriteRequest(new Runnable() {
                public void run() {
                    EditableProperties props = helper.getProperties(propertiesFile);
                    if (!path.equals(props.getProperty(reference))) {
                        props.setProperty(reference, path);
                        helper.putProperties(propertiesFile, props);
                    }
                    
                    // #47541 - check that property is not defined in opposite
                    // property file and delete it if it is
                    String oppositePropertiesFile = (propertiesFile.equals(AntProjectHelper.PROJECT_PROPERTIES_PATH) ? 
                        AntProjectHelper.PRIVATE_PROPERTIES_PATH : AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    props = helper.getProperties(oppositePropertiesFile);
                    if (props.containsKey(reference)) {
                        props.remove(reference);
                        helper.putProperties(oppositePropertiesFile, props);
                    }
                    
                    if (proj != null) {
                        try {
                            ProjectManager.getDefault().saveProject(proj);
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
                        }
                    }
                }
            });
    }

    public static final int REF_TYPE_PROJECT = 1;
    public static final int REF_TYPE_FILE = 2;
    public static final int REF_TYPE_LIBRARY = 3;
    public static final int REF_TYPE_PLATFORM = 4;
    public static final int REF_TYPE_LIBRARY_CONTENT = 5;
    
    public static class OneReference {
        
        private int type;
        private boolean broken;
        private String ID;

        public OneReference(int type, String ID, boolean broken) {
            this.type = type;
            this.ID = ID;
            this.broken = broken;
        }
        
        public int getType() {
            return type;
        }
        
        public String getDisplayID() {
            switch (type) {
                
                case REF_TYPE_LIBRARY:
                case REF_TYPE_LIBRARY_CONTENT:
                    // libs.<name>.classpath
                    return ID.substring(5, ID.length()-10);
                    
                case REF_TYPE_PROJECT:
                    // project.<name>
                    return ID.substring(8);
                    
                case REF_TYPE_FILE:
                    // file.reference.<name>
                    return ID.substring(15);
                    
                case REF_TYPE_PLATFORM:
                    return ID;
                    
                default:
                    assert false;
                    return ID;
            }
        }

        public boolean equals(java.lang.Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof OneReference)) {
                return false;
            }
            OneReference or = (OneReference)o;
            return (this.type == or.type && this.ID.equals(or.ID));
        }
        
        public int hashCode() {
            int result = 7*type;
            result = 31*result + ID.hashCode();
            return result;
        }
        
    }
    
}
