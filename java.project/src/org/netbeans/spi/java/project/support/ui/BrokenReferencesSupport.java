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

package org.netbeans.spi.java.project.support.ui;

import java.awt.Dialog;
import org.netbeans.modules.java.project.BrokenReferencesCustomizer;
import org.netbeans.modules.java.project.BrokenReferencesModel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Support for managing broken project references. Project freshly checkout from
 * VCS can has broken references of several types: reference to other project, 
 * reference to a foreign file, reference to an external source root, reference
 * to a Java Library or reference to a Java Platform. This class has helper
 * methods for detection of these problems and for fixing them.
 * <div class="nonnormative">
 * Typical usage of this class it to check whether the project has some broken
 * references and if it has then providing an action on project's node which
 * allows to correct these configuration problems by showing broken references
 * customizer.
 * </div>
 * @author David Konecny
 */
public class BrokenReferencesSupport {
    
    private BrokenReferencesSupport() {}

    /**
     * Checks whether the project has some broken references or not.
     * @param evaluator property evaluator associated with the project
     * @param properties array of property names which values hold
     *    references which may be broken. For example for J2SE project
     *    the property names will be: "javac.classpath", "run.classpath", etc.
     * @param platform name of the platform used by the project or 
     *    "default_platform" if default platform is used
     * @return true if some problem was found and it is necessary to give
     *    user a chance to fix them
     */
    public static boolean isBroken(PropertyEvaluator evaluator, String[] properties, String platform) {
        return BrokenReferencesModel.isBroken(evaluator, properties, platform);
    }
    
    /**
     * Shows UI customizer which gives users chance to fix encountered problems.
     * @param projectHelper AntProjectHelper associated with the project.
     * @param referenceHelper ReferenceHelper associated with the project.
     * @param properties array of property names which values hold
     *    references which may be broken. For example for J2SE project
     *    the property names will be: "javac.classpath", "run.classpath", etc.
     * @param platform name of the platform used by the project or 
     *    "default_platform" if default platform is used
     */
    public static void showCustomizer(AntProjectHelper projectHelper, 
            ReferenceHelper referenceHelper, String[] properties, String platform) {
        BrokenReferencesModel model = new BrokenReferencesModel(projectHelper, referenceHelper, properties, platform);
        BrokenReferencesCustomizer customizer = new BrokenReferencesCustomizer(model);
        Object close = NbBundle.getMessage(BrokenReferencesCustomizer.class,"LBL_BrokenLinksCustomizer_Close");
        DialogDescriptor dd = new DialogDescriptor(customizer, 
            NbBundle.getMessage(BrokenReferencesCustomizer.class, 
            "LBL_BrokenLinksCustomizer_Title", projectHelper.getDisplayName()),
            true, new Object[] {close}, close, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(dd);
            dlg.setVisible(true);
        } finally {
            if (dlg != null)
                dlg.dispose();
        }
    }
    
}
