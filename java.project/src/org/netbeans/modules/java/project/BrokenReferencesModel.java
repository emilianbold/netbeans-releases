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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.AbstractListModel;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

public class BrokenReferencesModel extends AbstractListModel {

    private String[] props;
    private String[] brokenReferences;
    private String platform;
    private boolean isPlatformBroken;
    private AntProjectHelper helper;
    private ReferenceHelper resolver;

    public BrokenReferencesModel(AntProjectHelper helper, 
            ReferenceHelper resolver, String[] props, String platform) {
        this.props = props;
        this.platform = platform;
        this.resolver = resolver;
        this.helper = helper;
        refresh();
    }
    
    public void refresh() {
        brokenReferences = getReferences(helper.getStandardPropertyEvaluator(), props, false);
        isPlatformBroken = platform != null ? !existPlatform(platform) : false;
        this.fireContentsChanged(this, 0, getSize());
    }

    public Object getElementAt(int index) {
        if (index < brokenReferences.length) {
            if (isLibrary(index)) {
                return NbBundle.getMessage(BrokenReferencesCustomizer.class, "LBL_BrokenLinksCustomizer_BrokenLibrary", getLibraryID(index));
            } else {
                if (isProjectReference(index)) {
                    return NbBundle.getMessage(BrokenReferencesCustomizer.class, "LBL_BrokenLinksCustomizer_BrokenProjectReference", getProjectID(index));
                } else {
                    return NbBundle.getMessage(BrokenReferencesCustomizer.class, "LBL_BrokenLinksCustomizer_BrokenFileReference", getFileID(index));
                }
            }
        } else {
            assert index == getSize()-1;
            return NbBundle.getMessage(BrokenReferencesCustomizer.class, "LBL_BrokenLinksCustomizer_BrokenPlatform", platform);
        }
    }

    public String getDesciption(int index) {
        if (index < brokenReferences.length) {
            if (isLibrary(index)) {
                // XXX: if library classpath is defined then just WARN user 
                // that library is not defined
                return NbBundle.getMessage(BrokenReferencesCustomizer.class, "LBL_BrokenLinksCustomizer_BrokenLibraryDesc", getLibraryID(index));
            } else {
                if (isProjectReference(index)) {
                    return NbBundle.getMessage(BrokenReferencesCustomizer.class, "LBL_BrokenLinksCustomizer_BrokenProjectReferenceDesc", getProjectID(index));
                } else {
                    return NbBundle.getMessage(BrokenReferencesCustomizer.class, "LBL_BrokenLinksCustomizer_BrokenFileReferenceDesc", getFileID(index));
                }
            }
        } else {
            assert index == getSize()-1;
            return NbBundle.getMessage(BrokenReferencesCustomizer.class, "LBL_BrokenLinksCustomizer_BrokenPlatformDesc", platform);
        }
    }

    boolean isLibrary(int index) {
        return index < brokenReferences.length ? brokenReferences[index].startsWith("libs.") : false;
    }
    
    private String getLibraryID(int index) {
        // libs.<name>.classpath
        return brokenReferences[index].substring(5, brokenReferences[index].length()-10);
    }

    boolean isPlatform(int index) {
        return isPlatformBroken && index == getSize()-1;
    }

    boolean isProjectReference(int index) {
        return brokenReferences[index].startsWith("project.");
    }

    private String getProjectID(int index) {
        // project.<name>
        return brokenReferences[index].substring(8);
    }

    private String getFileID(int index) {
        // file.reference.<name>
        return brokenReferences[index].substring(15);
    }

    public int getSize() {
        return brokenReferences.length + (isPlatformBroken ? 1 : 0);
    }

    public static boolean isBroken(PropertyEvaluator evaluator, String[] props, String platform) {
        String[] broken = getReferences(evaluator, props, true);
        if (broken.length > 0) {
            return true;
        }
        return !existPlatform(platform);
    }

    private static String[] getReferences(PropertyEvaluator evaluator, String[] ps, boolean abortAfterFirstProblem) {
        Set set = new LinkedHashSet();
        for (int i=0; i<ps.length; i++) {
            // evaluate given property and tokenize it
            String[] vals = PropertyUtils.tokenizePath(evaluator.getProperty(ps[i]));
            
            // XXX: perhaps I could check here also that correctly resolved
            // path point to an existing file? For foreign file references it
            // make sence.
            
            // no check whether after evaluating there are still some 
            // references which could not be evaluated
            for (int j=0; j<vals.length; j++) {
                // we are checking only: project reference, file reference, library reference
                if (!(vals[j].startsWith("${file.reference.") || vals[j].startsWith("${project.") || vals[j].startsWith("${libs."))) {
                    continue;
                }
                if (vals[j].startsWith("${project.")) {
                    // something in the form: "${project.<projID>}/dist/foo.jar"
                    String val = vals[j].substring(2, vals[j].indexOf('}'));
                    set.add(val);
                } else {
                    set.add(vals[j].substring(2, vals[j].length()-1));
                }
                if (abortAfterFirstProblem) {
                    break;
                }
            }
            if (set.size() > 0 && abortAfterFirstProblem) {
                break;
            }
        }
        return (String[])set.toArray(new String[set.size()]);
    }
    
    private static boolean existPlatform(String platform) {
        if (platform.equals("default_platform")) { // NOI18N
            return true;
        }
        JavaPlatform plats[] = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (int i=0; i<plats.length; i++) {
            if (platform.equals(plats[i].getProperties().get("platform.ant.name"))) { // NOI18N
                return true;
            }
        }
        return false;
    }

    // XXX: perhaps could be moved to ReferenceResolver. 
    // But nobody should need it so it is here for now.
    void updateReference(int index, File file) {
        String reference = brokenReferences[index];
        File myProjDir = FileUtil.toFile(helper.getProjectDirectory());
        String propertiesFile;
        String path;
        if (CollocationQuery.areCollocated(myProjDir, file)) {
            propertiesFile = AntProjectHelper.PROJECT_PROPERTIES_PATH;
            path = PropertyUtils.relativizeFile(myProjDir, file);
            assert path != null : "expected relative path from " + myProjDir + " to " + file; // NOI18N
        } else {
            propertiesFile = AntProjectHelper.PRIVATE_PROPERTIES_PATH;
            path = file.getAbsolutePath();
        }
        EditableProperties props = helper.getProperties(propertiesFile);
        if (!path.equals(props.getProperty(reference))) {
            props.setProperty(reference, path);
            helper.putProperties(propertiesFile, props);
        }
    }
    
}
