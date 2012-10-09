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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.beans.api.model.ModelUnit;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModelFactory;
import org.netbeans.modules.web.beans.api.model.support.WebBeansModelSupport;
import org.netbeans.modules.web.jsf.editor.facelets.AbstractFaceletsLibrary;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrarySupport;
import org.netbeans.modules.web.jsf.editor.index.JsfIndex;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.spi.JsfSupportProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * per web-module instance
 *
 * @author marekfukala
 */
public class JsfSupportImpl implements JsfSupport {

	private static final Logger LOG = Logger.getLogger(JsfSupportImpl.class.getSimpleName());
    
    public static JsfSupportImpl findFor(Source source) {
        return getOwnImplementation(JsfSupportProvider.get(source));
    }

    public static JsfSupportImpl findFor(FileObject file) {
        return getOwnImplementation(JsfSupportProvider.get(file));
    }
    
    private static JsfSupportImpl getOwnImplementation(JsfSupport instance) {
        if(instance instanceof JsfSupportImpl) {
            return (JsfSupportImpl)instance;
        } else {
            //the jsf editor requires its own implementation of the JsfSupport for the time being
            return null;
        }
    }

     //synchronized in JsfSupportProvider.get(Project project)
    static JsfSupportImpl findForProject(Project project) {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if(webModule != null) {
            // #217213 - prevent NPE:
            if (webModule.getDocumentBase() == null) {
                LOG.log(Level.INFO, "project '"+project+ // NOI18N
                        "' does not have valid documentBase"); // NOI18N
                return null;
            }
            //web project
            ClassPath sourceCP = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.SOURCE);
            ClassPath compileCP = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
            // #217213 - prevent NPE; not sure what's causing it:
            if (compileCP == null) {
                LOG.log(Level.INFO, "project '"+project+ // NOI18N
                        "' does not have compilation classpath; documentBase="+ // NOI18N
                        webModule.getDocumentBase());
                return null;
            }
            ClassPath executeCP = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.EXECUTE);
            ClassPath bootCP = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.BOOT);
            
            return new JsfSupportImpl(project, webModule, sourceCP, compileCP, executeCP, bootCP);
        } else {
            //non-web project
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] sourceGroups = sources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA );
            if(sourceGroups.length > 0) {
                Collection<ClassPath> sourceCps = new HashSet<ClassPath>();
                Collection<ClassPath> compileCps = new HashSet<ClassPath>();
                Collection<ClassPath> executeCps = new HashSet<ClassPath>();
                Collection<ClassPath> bootCps = new HashSet<ClassPath>();
                for(SourceGroup sg : sourceGroups) {
                    sourceCps.add(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.SOURCE));
                    compileCps.add(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.COMPILE));
                    executeCps.add(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.EXECUTE));
                    bootCps.add(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.BOOT));
                }
                return new JsfSupportImpl(project, null, 
                        ClassPathSupport.createProxyClassPath(sourceCps.toArray(new ClassPath[]{})),
                        ClassPathSupport.createProxyClassPath(compileCps.toArray(new ClassPath[]{})),
                        ClassPathSupport.createProxyClassPath(executeCps.toArray(new ClassPath[]{})),
                        ClassPathSupport.createProxyClassPath(bootCps.toArray(new ClassPath[]{})));
            }
            
        }
        
        //no jsf support for this project
        return null;
    }
    
    private FaceletsLibrarySupport faceletsLibrarySupport;
    private Project project;
    private WebModule wm;
    private ClassPath sourceClassPath, compileClasspath, executeClassPath, bootClassPath;
    private JsfIndex index;
    private MetadataModel<WebBeansModel> webBeansModel;
    private Lookup lookup;

     private JsfSupportImpl(Project project, WebModule wm, ClassPath sourceClassPath, ClassPath compileClassPath, ClassPath executeClassPath, ClassPath bootClassPath) {
        this.project = project;
        this.wm = wm;
        this.sourceClassPath = sourceClassPath;
        this.compileClasspath = compileClassPath;
        this.executeClassPath = executeClassPath;
        this.bootClassPath = bootClassPath;
        this.faceletsLibrarySupport = new FaceletsLibrarySupport(this);

        //adds a classpath listener which invalidates the index instance after classpath change
        //and also invalidates the facelets library descriptors and tld caches
        this.compileClasspath.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                synchronized (JsfSupportImpl.this) {
                    index = null;
                }
            }
        });
        //register html extension
        //TODO this should be done declaratively via layer
        JsfHtmlExtension.activate();

        ModelUnit modelUnit = WebBeansModelSupport.getModelUnit(wm != null ? getFileObject(wm) : project.getProjectDirectory());
        webBeansModel = WebBeansModelFactory.getMetaModel(modelUnit);

        //init lookup
        //TODO do it lazy so it creates the web beans model lazily once looked up
        InstanceContent ic = new InstanceContent();
        ic.add(webBeansModel);
        this.lookup = new AbstractLookup(ic);

    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public ClassPath getClassPath() {
        return compileClasspath;
    }

    /**
     * @return can return null if this supports wraps a project of non-web type.
     */
    @Override
    public WebModule getWebModule() {
        return wm;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public Library getLibrary(String namespace) {
        return faceletsLibrarySupport.getLibraries().get(namespace);
    }

    /** Library's uri to library map 
     * Please note that a composite components library can be preset twice in the values. 
     * Once under the declared namespace key and once under the default cc namespace key.
     */
    @Override
    public Map<String, AbstractFaceletsLibrary> getLibraries() {
        return faceletsLibrarySupport.getLibraries();
    }

    //garbage methods below, needs cleanup!
    public synchronized JsfIndex getIndex() {
        if(index == null) {
	    this.index = JsfIndex.create(sourceClassPath, compileClasspath, executeClassPath, bootClassPath);
        }
        return this.index;
    }

    public FaceletsLibrarySupport getFaceletsLibrarySupport() {
	return faceletsLibrarySupport;
    }

    public synchronized MetadataModel<WebBeansModel> getWebBeansModel() {
	return webBeansModel;
    }

    @Override
    public String toString() {
        return String.format("JsfSupportImpl[%s]", getBaseFile().getPath()); //NOI18N
    }

    private FileObject getBaseFile() {
        return wm != null ? wm.getDocumentBase() : project.getProjectDirectory();
    }

    private static FileObject getFileObject(WebModule module) {
        FileObject fileObject = module.getDocumentBase();
        if (fileObject != null) {
            return fileObject;
        }
        fileObject = module.getDeploymentDescriptor();
        if (fileObject != null) {
            return fileObject;
        }
        fileObject = module.getWebInf();
        if (fileObject != null) {
            return fileObject;
        }
        FileObject[] fileObjects = module.getJavaSources();
        if (fileObjects != null) {
            for (FileObject source : fileObjects) {
                if (source != null) {
                    return source;
                }
            }
        }
        return null;
    }
    
}
