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

package org.netbeans.modules.mobility.project.classpath;
import java.lang.ref.SoftReference;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Supplies the classpath information from {@link J2MEProject}s.
 * @author Jesse Glick, Adam Sotona
 */
public class J2MEClassPathProvider implements ClassPathProvider {
    
    private SoftReference<ClassPath> ctcp, rtcp, sp, bcp;    
    private FileObject srcDir;
    
    protected final AntProjectHelper helper;
    
    /** Do nothing */
    public J2MEClassPathProvider(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    public ClassPath findClassPath(final FileObject file, final String type) {
        if (type.equals(ClassPath.BOOT)) return getBootClassPath();
        if (!checkSrcParent(file)) return null;
        if (type.equals(ClassPath.COMPILE)) return getCompileTimeClasspath();
        if (type.equals(ClassPath.EXECUTE)) return getRunTimeClasspath();
        if (type.equals(ClassPath.SOURCE)) return getSourcepath();
        // Unrecognized type, ignore.
        return null;
    }
    
    public boolean checkSrcParent(final FileObject file) {
        if (srcDir == null || !srcDir.isValid()) {
            final String prop = helper.getStandardPropertyEvaluator().getProperty("src.dir"); //NOI18N
            if (prop != null) {
                srcDir = helper.resolveFileObject(prop);
            }
        }
        return (srcDir != null && file != null && (srcDir.equals(file) || FileUtil.isParentOf(srcDir, file)));
    }
    
    public ClassPath getCompileTimeClasspath() {
        ClassPath cp = null;
        if (ctcp == null || (cp = ctcp.get()) == null) {
            cp = ClassPathFactory.createClassPath(
                    new ProjectClassPathImplementation(helper) {
                protected String evaluatePath() {
                    String cp = J2MEProjectUtils.evaluateProperty(helper, "libs.classpath"); //NOI18N
                    if (cp != null) cp = helper.resolvePath(cp);
                    return cp;
                }
            }
            );
            ctcp = new SoftReference<ClassPath>(cp);
        }
        return cp;
    }
    
    public ClassPath getRunTimeClasspath() {
        ClassPath cp = null;
        if (rtcp == null || (cp = rtcp.get())== null) {
            cp = ClassPathFactory.createClassPath(
                    new ProjectClassPathImplementation(helper) {
                protected String evaluatePath() {
                    String cp = helper.getStandardPropertyEvaluator().getProperty("build.classes.dir"); //NOI18N
                    if (cp != null) cp = helper.resolvePath(cp);
                    return cp;
                }
            });
            rtcp = new SoftReference<ClassPath>(cp);
        }
        return cp;
    }
    
    public ClassPath getSourcepath() {
        ClassPath cp = null;
        if (sp == null || (cp = sp.get()) == null) {
            cp = ClassPathFactory.createClassPath(
                    new ProjectClassPathImplementation(helper) {
                protected String evaluatePath() {
                    String cp = helper.getStandardPropertyEvaluator().getProperty("src.dir"); //NOI18N
                    if (cp != null) cp = helper.resolvePath(cp);
                    return cp;
                }
            });
            sp = new SoftReference<ClassPath>(cp);
        }
        return cp;
    }
    
    public ClassPath getBootClassPath() {
        ClassPath cp = null;
        if (bcp == null || (cp = bcp.get()) == null) {
            cp = ClassPathFactory.createClassPath(new ProjectBootClassPathImplementation());
            bcp = new SoftReference<ClassPath>(cp);
        }
        return cp;
    }
    
    private class ProjectBootClassPathImplementation extends ProjectClassPathImplementation implements ChangeListener {
        
        public ProjectBootClassPathImplementation() {
            super(helper);
            PropertyProvider provider = PropertyUtils.globalPropertyProvider();
            provider.addChangeListener(WeakListeners.change(this, provider));
        }
        
        protected String evaluatePath() {
            String platform = J2MEProjectUtils.evaluateProperty(helper, "platform.active"); //NOI18N
            if (platform == null) return null;
            platform = helper.getStandardPropertyEvaluator().getProperty("platforms." + platform + ".home"); //NOI18N
            String cp = J2MEProjectUtils.evaluateProperty(helper, "platform.bootclasspath"); //NOI18N
            if (platform == null || cp == null) return null;
            int i;
            while ((i = cp.indexOf("${platform.home}")) >= 0) { //NOI18N
                cp = cp.substring(0, i) + platform + cp.substring(i+16);
            }
            return cp == null ? null : helper.resolvePath(cp);//NOI18N
        }
        
        public void stateChanged(@SuppressWarnings("unused")
		final ChangeEvent e) {
            propertiesChanged(null);
        }
    }
}
