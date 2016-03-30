/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import static org.netbeans.spi.java.classpath.support.ClassPathSupport.Selector.PROP_ACTIVE_CLASS_PATH;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Defines class path for maven2 projects..
 *
 * @author  Milos Kleint 
 */
@ProjectServiceProvider(service={ClassPathProvider.class, ActiveJ2SEPlatformProvider.class, ProjectSourcesClassPathProvider.class}, projectType="org-netbeans-modules-maven")
public final class ClassPathProviderImpl implements ClassPathProvider, ActiveJ2SEPlatformProvider, ProjectSourcesClassPathProvider {

    private static final Logger LOGGER = Logger.getLogger(ClassPathProviderImpl.class.getName());
    
    private static final int TYPE_SRC = 0;
    private static final int TYPE_TESTSRC = 1;
    private static final int TYPE_WEB = 5;
    private static final int TYPE_UNKNOWN = -1;
    
    private final @NonNull Project proj;
    private final ClassPath[] cache = new ClassPath[14];    
    
    public ClassPathProviderImpl(@NonNull Project proj) {
        this.proj = proj;        
    }
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    @Override public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            //TODO
            return new ClassPath[]{ getBootClassPath() };
        }
        if (ClassPathSupport.ENDORSED.equals(type)) {
            return new ClassPath[]{ getEndorsedClassPath() };
        }
        if (ClassPath.COMPILE.equals(type)) {
            List<ClassPath> l = new ArrayList<ClassPath>(2);
            l.add(getCompileTimeClasspath(TYPE_SRC));//
            l.add(getCompileTimeClasspath(TYPE_TESTSRC));//
            return l.toArray(new ClassPath[l.size()]);
        }
        if (ClassPath.EXECUTE.equals(type)) {
            List<ClassPath> l = new ArrayList<ClassPath>(2);
            l.add(getRuntimeClasspath(TYPE_SRC));
            l.add(getRuntimeClasspath(TYPE_TESTSRC));
            return l.toArray(new ClassPath[l.size()]);
        }
        
        if (ClassPath.SOURCE.equals(type)) {
            List<ClassPath> l = new ArrayList<ClassPath>(2);
            l.add(getSourcepath(TYPE_SRC));
            l.add(getSourcepath(TYPE_TESTSRC));
            return l.toArray(new ClassPath[l.size()]);
        }
        if (JavaClassPathConstants.MODULE_BOOT_PATH.equals(type)) {
            return new ClassPath[] {getModuleBootPath()}; //
        }
        if (JavaClassPathConstants.MODULE_COMPILE_PATH.equals(type)) {
            ClassPath[] l = new ClassPath[2];
            l[0] = getModuleCompilePath(TYPE_SRC);
            l[1] = getModuleCompilePath(TYPE_TESTSRC);
            return l;
        }
        if (JavaClassPathConstants.MODULE_CLASS_PATH.equals(type)) {
            ClassPath[] l = new ClassPath[2];
            l[0] = getModuleLegacyClassPath(0);
            l[1] = getModuleLegacyClassPath(1);
            return l;
        }
        return new ClassPath[0];
    }
    
    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots).
     */
    @Override public ClassPath getProjectSourcesClassPath(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return getBootClassPath();
        }
        if (ClassPathSupport.ENDORSED.equals(type)) {
            return getEndorsedClassPath();
        }
        if (ClassPath.COMPILE.equals(type)) {
            return getCompileTimeClasspath(TYPE_SRC);//
        }
        if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(TYPE_SRC);
        }
        if (ClassPath.EXECUTE.equals(type)) {
            return getRuntimeClasspath(TYPE_SRC);
        }
        if (type.equals(JavaClassPathConstants.MODULE_BOOT_PATH)) {            
            return getModuleBootPath(); //
        }
        if (type.equals(JavaClassPathConstants.MODULE_COMPILE_PATH)) {            
            return getModuleCompilePath(0);
        }
        if (type.equals(JavaClassPathConstants.MODULE_CLASS_PATH)) {            
            return getModuleLegacyClassPath(0);
        }
        assert false;
        return null;
    }
    
    
    @Override public ClassPath findClassPath(FileObject file, String type) {
        assert file != null;
        if(file == null) {            
            LOGGER.log(Level.WARNING, " passed null fileobject fo ClassPathProviderImpl.findClassPath."); //NOI18N
            return null;
        }
        int fileType = getType(file);
        if (fileType != TYPE_SRC &&  fileType != TYPE_TESTSRC && fileType != TYPE_WEB) {
            LOGGER.log(Level.FINEST, " bad type={0} for {1}", new Object[] {type, file}); //NOI18N
            return null;
        }
        if (type.equals(ClassPath.COMPILE)) {
            return getCompileTimeClasspath(fileType);//
        } else if (type.equals(ClassPath.EXECUTE)) {
            return getRuntimeClasspath(fileType);
        } else if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(fileType);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else if (type.equals(ClassPathSupport.ENDORSED)) {
            return getEndorsedClassPath();
        } else if (type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
            // XXX read <processorpath> from maven-compiler-plugin config
            return getCompileTimeClasspath(fileType);
        } else if (type.equals(JavaClassPathConstants.MODULE_BOOT_PATH)) {            
            return getModuleBootPath(); //
        } else if (type.equals(JavaClassPathConstants.MODULE_COMPILE_PATH)) {            
            return getModuleCompilePath(fileType);
        } else if (type.equals(JavaClassPathConstants.MODULE_CLASS_PATH)) {            
            return getModuleLegacyClassPath(fileType);
        } else {
            return null;
        }
    }

    private boolean isChildOf(FileObject child, URI[] uris) {
        for (int i = 0; i < uris.length; i++) {
            FileObject fo = FileUtilities.convertURItoFileObject(uris[i]);
            if (fo != null  && fo.isFolder() && (fo.equals(child) || FileUtil.isParentOf(fo, child))) {
                return true;
            }
        }
        return false;
    }
    
    public static FileObject[] convertStringsToFileObjects(List<String> strings) {
        FileObject[] fos = new FileObject[strings.size()];
        int index = 0;
        Iterator<String> it = strings.iterator();
        while (it.hasNext()) {
            String str = it.next();
            fos[index] = FileUtilities.convertStringToFileObject(str);
            index++;
        }
        return fos;
    }
    
    
    private int getType(FileObject file) {
        if(file == null) {
            return TYPE_UNKNOWN;
        }
        NbMavenProjectImpl project = proj.getLookup().lookup(NbMavenProjectImpl.class);
        if (isChildOf(file, project.getSourceRoots(false)) ||
            isChildOf(file, project.getGeneratedSourceRoots(false))) {
            return TYPE_SRC;
        }
        if (isChildOf(file, project.getSourceRoots(true)) ||
            isChildOf(file, project.getGeneratedSourceRoots(true))) {
            return TYPE_TESTSRC;
        }
        
        URI web = project.getWebAppDirectory();
        FileObject fo = FileUtil.toFileObject(Utilities.toFile(web));
        if (fo != null && (fo.equals(file) || FileUtil.isParentOf(fo, file))) {
            return TYPE_WEB;
        }
        
        //MEVENIDE-613, #125603 need to check later than the actual java sources..
        // sometimes the root of resources is the basedir for example that screws up 
        // test sources.
        if (isChildOf(file, project.getResources(false))) {
            return TYPE_SRC;
        }
        if (isChildOf(file, project.getResources(true))) {
            return TYPE_TESTSRC;
        }
        return TYPE_UNKNOWN;
    }
    
    
    
    private synchronized ClassPath getSourcepath(int type) {
        if (type == TYPE_WEB) {
            type = TYPE_SRC;
        }
        ClassPath cp = cache[type];
        if (cp == null) {
            NbMavenProjectImpl project = proj.getLookup().lookup(NbMavenProjectImpl.class);
            if (type == TYPE_SRC) {
                cp = ClassPathFactory.createClassPath(new SourceClassPathImpl(project));
            } else {
                cp = ClassPathFactory.createClassPath(new TestSourceClassPathImpl(project));
            }
            cache[type] = cp;
        }
        return cp;
    }
    
    private synchronized ClassPath getCompileTimeClasspath(int type) {
        if (type == TYPE_WEB) {
            type = TYPE_SRC;
        }
        ClassPath cp = cache[2+type];
        if (cp == null) {
            NbMavenProjectImpl project = proj.getLookup().lookup(NbMavenProjectImpl.class);
            if (type == TYPE_SRC) {
                cp = createMultiplexClassPath(
                    new ModuleInfoSelector(proj.getLookup().lookup(NbMavenProjectImpl.class),
                        ClassPath.EMPTY,
                                    getCompileClasspath(),
                                    "CompileTimeClasspath" // NOI18N
                    )
                 );
            } else {
                cp = ClassPathFactory.createClassPath(new TestCompileClassPathImpl(project));
            }
            cache[2+type] = cp;
        }
        return cp;
    }
    
    private synchronized ClassPath getRuntimeClasspath(int type) {
        if (type == TYPE_WEB) {
            type = TYPE_SRC;
        }
        ClassPath cp = cache[4+type];
        if (cp == null) {
            NbMavenProjectImpl project = proj.getLookup().lookup(NbMavenProjectImpl.class);
            if (type == TYPE_SRC) {
                cp = ClassPathFactory.createClassPath(new RuntimeClassPathImpl(project));
            } else {
                cp = ClassPathFactory.createClassPath(new TestRuntimeClassPathImpl(project));
            }
            cache[4+type] = cp;
        }
        return cp;
    }
    
    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache[6];
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(getBootClassPathImpl());
            cache[6] = cp;
        }
        return cp;
    }
    
    private BootClassPathImpl bcpImpl;
    private synchronized BootClassPathImpl getBootClassPathImpl() {
        if (bcpImpl == null) {
            bcpImpl = new BootClassPathImpl(proj.getLookup().lookup(NbMavenProjectImpl.class), getEndorsedClassPathImpl());
        }
        return bcpImpl;
    }

    private synchronized ClassPath getCompileClasspath() {
        ClassPath cp = cache[13];
        if (cp == null) {
            
            /*
             XXX - the classpathElements in maven-compiler-plugin always were:
              - all artifacts from the project with the scope - COMPILE, PROFILE and SYSTEM
              - the path given by project.build.getOutputDirectory
            
             until jdk9 jigsaw: 
              CompileClassPathImpl provided only the artifacts with the respective scope,
              but NOT the project.build.getOutputDirectory.
             since jdk9 jigsaw (and therefore maven-compiler-plugin 2.6): 
              it is necessary to provide also project.build.getOutputDirectory 
              (as that is the dir where maven copies the dependand jar/modules?)
              
             The question at this point is if we now should do so for all compiler versions 
             (and also for < 2.6) and jdk-s < 9 or if we should difer between m-c-p < 2.6 and >=2.6 
             and jdk version respectively.
            */
            
            cp = ClassPathFactory.createClassPath(new CompileClassPathImpl(proj.getLookup().lookup(NbMavenProjectImpl.class), true));
            cache[13] = cp;
        }
        return cp;
    }

    @Override public @NonNull JavaPlatform getJavaPlatform() {
        return getBootClassPathImpl().findActivePlatform();
    }

    private EndorsedClassPathImpl ecpImpl;
    private synchronized EndorsedClassPathImpl getEndorsedClassPathImpl() {
        if (ecpImpl == null) {
            ecpImpl = new EndorsedClassPathImpl(proj.getLookup().lookup(NbMavenProjectImpl.class));
        }
        return ecpImpl;
    }

    private ClassPath getEndorsedClassPath() {
        ClassPath cp = cache[8];
        if (cp == null) {
            getBootClassPathImpl();
            cp = ClassPathFactory.createClassPath(getEndorsedClassPathImpl());
            cache[8] = cp;
        }
        return cp;
    }
    
    private synchronized ClassPath getModuleBootPath() {
        if (cache[9] == null) {
            cache[9] =  createMultiplexClassPath(
                            new ModuleInfoSelector(proj.getLookup().lookup(NbMavenProjectImpl.class),
                                ClassPathFactory.createClassPath(getBootClassPathImpl()), 
                                ClassPathFactory.createClassPath(new PlatformModulePathImpl(proj.getLookup().lookup(NbMavenProjectImpl.class))),
                                "ModuleBootPath" // NOI18N
                            )
                        );
        }
        return cache[9];
    }

    private synchronized ClassPath getModuleCompilePath(int type) {
        if (cache[10] == null) {
            cache[10] = createMultiplexClassPath(
                            new ModuleInfoSelector(proj.getLookup().lookup(NbMavenProjectImpl.class),
                                getCompileClasspath(),
                                ClassPath.EMPTY,
                                "ModuleCompilePath" // NOI18N
                            )
                        );
        }
        return cache[10];
    }

    @NonNull
    private synchronized ClassPath getModuleLegacyClassPath(final int type) {
        assert type >=0 && type <=1;
        ClassPath cp = cache[11+type];
        if (cp == null) {
            cp = createMultiplexClassPath (
                    new ModuleInfoSelector(proj.getLookup().lookup(NbMavenProjectImpl.class), 
                        getCompileClasspath(),
                        ClassPath.EMPTY,
                        "ModuleLegacyClassPath" // NOI18N
                    )
                 );   
            cache[11+type] = cp;
        }
        return cp;
    }

    private ClassPath createMultiplexClassPath(ModuleInfoSelector moduleInfoSelector) {
        return org.netbeans.spi.java.classpath.support.ClassPathSupport.createMultiplexClassPath(moduleInfoSelector);
    }
    
    private static class ModuleInfoSelector extends ClassPathSelector {
                
        private static final String MODULE_INFO = "module-info.java"; // NOI18N
        
        private final ClassPath noModuleInfoCP;
        private final ClassPath hasModuleInfoCP;
        private final String logDesc;
        
        public ModuleInfoSelector(NbMavenProjectImpl proj, ClassPath hasModuleInfoClassPath, ClassPath noModuleInfoClassPath, String logDesc) {
            super(proj);
            this.hasModuleInfoCP = hasModuleInfoClassPath;
            this.noModuleInfoCP = noModuleInfoClassPath;
            this.logDesc = logDesc;
        }

        @Override
        public ClassPath getActiveClassPath() {
            ClassPath ret = active;
            if (ret == null) {
                ret = noModuleInfoCP;

                // see org.apache.maven.plugin.compiler.CompilerMojo
                for (String sourceRoot : proj.getOriginalMavenProject().getCompileSourceRoots()) {
                    if(new File(sourceRoot, MODULE_INFO).exists()) {
                        ret = hasModuleInfoCP;  
                        LOGGER.log(Level.FINER, "project {0} has module-info.java", proj.getProjectDirectory().getPath()); // NOI18N
                        break;
                    }
                }
                active = ret;
            }            
            LOGGER.log(Level.FINE, "project: {0} and classpath selector: {1} is returning active class path: {2}", new Object[]{proj.getProjectDirectory().getPath(), logDesc, ret}); // NOI18N
            
            return ret;
        }

        @Override
        protected boolean isReset(PropertyChangeEvent evt) {
            boolean reset = false;            
            if( (NbMavenProject.PROP_RESOURCE.equals(evt.getPropertyName()) && evt.getNewValue() instanceof URI)) {
                File file = Utilities.toFile((URI) evt.getNewValue());
                for (String sourceRoot : proj.getOriginalMavenProject().getTestCompileSourceRoots()) {
                    if(file.equals(new File(sourceRoot, MODULE_INFO))) {
                        reset = true;
                        break;
                    }
                }
                if(reset) {
                    LOGGER.log(Level.FINER, "{0} selector resource changed: {1}", new Object[]{logDesc, evt});
                }
            }
            return reset;
        }
    }
           
    private static abstract class ClassPathSelector implements org.netbeans.spi.java.classpath.support.ClassPathSupport.Selector {
        private final PropertyChangeSupport support = new PropertyChangeSupport(this);
        
        protected final NbMavenProjectImpl proj;
        protected ClassPath active = null;
        
        public ClassPathSelector(NbMavenProjectImpl proj) {
            this.proj = proj;                     
            NbMavenProject.addPropertyChangeListener(proj, (evt) -> {
                if (isReset(evt)) {
                    active = null;
                    support.firePropertyChange(PROP_ACTIVE_CLASS_PATH, null, null);
                }
            });
        }

        protected abstract boolean isReset(PropertyChangeEvent evt);
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

    }
}

