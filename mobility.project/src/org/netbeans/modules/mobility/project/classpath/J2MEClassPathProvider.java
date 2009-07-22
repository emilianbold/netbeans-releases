/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    public J2MEClassPathProvider(AntProjectHelper helpers) {
        this.helper = helpers;
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
