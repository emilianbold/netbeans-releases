/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.hibernate.hqleditor;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.hibernate.cfg.Configuration;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.util.CustomClassLoader;
import org.netbeans.modules.hibernate.hqleditor.ui.HQLEditorTopComponent;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * HQL Editor controller. Controls overall HQL query execution.
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HQLEditorController {

    private Logger logger = Logger.getLogger(HQLEditorController.class.getName());
    HQLEditorTopComponent editorTopComponent = null;

    public void executeHQLQuery(final String hql,
            final FileObject configFileObject,
            final int maxRowCount,
            final ProgressHandle ph) {
        final List<URL> localResourcesURLList = new ArrayList<URL>();

        try {
            ph.progress(10);
            ph.setDisplayName(NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionPrepare"));
            Project project = FileOwnerQuery.getOwner(configFileObject);
            // Parse POJOs from HQL
            // Check and if required compile POJO files mentioned in HQL
            final Configuration customConfiguration = processAndConstructCustomConfiguration(hql, configFileObject, project);
            // Construct custom classpath here.
            HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
            localResourcesURLList.addAll(env.getProjectClassPath(configFileObject));



            final ClassLoader customClassLoader = new CustomClassLoader(localResourcesURLList.toArray(new URL[]{}),
                    this.getClass().getClassLoader());

            Thread t = new Thread() {

                @Override
                public void run() {
                    Thread.currentThread().setContextClassLoader(customClassLoader);
                    HQLExecutor queryExecutor = new HQLExecutor();
                    try {
                        ph.progress(50);
                        ph.setDisplayName(NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionPassControlToHibernate"));
                        HQLResult r = queryExecutor.execute(hql, customConfiguration, maxRowCount, ph);
                        ph.progress(80);
                        ph.setDisplayName(NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionProcessResults"));
                        editorTopComponent.setResult(r);
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }

                }
            };
            t.setContextClassLoader(customClassLoader);
            t.start();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void init(Node[] activatedNodes) {
        editorTopComponent = new HQLEditorTopComponent(this);
        editorTopComponent.open();
        editorTopComponent.requestActive();

        editorTopComponent.fillHibernateConfigurations(activatedNodes);
    }
    
    public Configuration getHibernateConfigurationForThisContext(FileObject originalConfigFileObject) {
        // At present
        return new Configuration().configure(FileUtil.toFile(originalConfigFileObject));
    }

    private Configuration processAndConstructCustomConfiguration(String hql, FileObject configFileObject, Project project) {
        Configuration customConfiguration = new Configuration();
        HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
        List<String> pojoNameList = env.getAllPOJONamesFromConfiguration(configFileObject);
        StringTokenizer hqlTokenizer = new StringTokenizer(hql, " ");
        List<String> tokenList = new ArrayList<String>();
        while (hqlTokenizer.hasMoreTokens()) {
            tokenList.add(hqlTokenizer.nextToken());
        }
        for (String className : pojoNameList) {
            for (String hqlClassName : tokenList) {
                if (className.contains(hqlClassName.trim())) {
                    checkAndCompile(className, configFileObject, project);
                        //customConfiguration.
                }
            }
        }
        return customConfiguration;
    }

    private void checkAndCompile(String className, FileObject configFileObject, Project project) {
        HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
        List<URL> urls = new ArrayList<URL>();
        urls.addAll(env.getProjectClassPath(configFileObject));
        CustomClassLoader ccl = new CustomClassLoader(urls.toArray(new URL[]{}),
                this.getClass().getClassLoader());
        try {
            ccl.loadClass(className);
        } catch (ClassNotFoundException e) {
            // Compile the class here.
            // TODO construct a custom configuration and set only the requested mapings into it.
            // Use that config. to input Hiberante. Otherwise, the default one tries to load all POJOs from /
            // all mappings mentioned in the config.
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(ccl);

            try {
                JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
                StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, null);
                className = className.replace(".", File.separator);
                File f = new File(
                        FileUtil.toFile(project.getProjectDirectory()), File.separator + "src" +
                        File.separator + className + ".java");
                //   FileObject pojoFO = HibernateUtil.findJavaFileObjectInProject(className, project);
                //    if(pojoFO == null) {
                // TODO Cannot find the POJO class.. flag error
                //    }
                Iterable<? extends JavaFileObject> compilationUnits1 =
                        fileManager.getJavaFileObjectsFromFiles(Arrays.asList(new File[]{f}));
                List<File> outputPath = new ArrayList<File>();
                outputPath.add(new File(
                        FileUtil.toFile(project.getProjectDirectory()), File.separator + "build" + File.separator + "classes"));
                fileManager.setLocation(StandardLocation.CLASS_OUTPUT, outputPath);
                List<String> options = new ArrayList<String>();
                options.add("-target");
                options.add("1.5");
                //TODO diagnostic listener - plugin log
                javaCompiler.getTask(null, fileManager, null, options, null, compilationUnits1).call();
            //  FileObject buildXMLFileObject = project.getProjectDirectory().getFileObject("build", "xml");
            //   java.util.Properties p = new java.util.Properties();
            // p.setProperty("javac.includes", ActionUtils.antIncludesList(
            //        files, 
            //        configFileObject, 
            //        recursive));
            //  ExecutorTask task = ActionUtils.runTarget(buildXMLFileObject, new String[]{"compile-single"}, p);
            //  InputOutput io = task.getInputOutput();
            //io.
            //  int r = task.result();
            //  System.out.println("result = " + r);
            } catch (Exception ee) {
                Exceptions.printStackTrace(ee);
            }



            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

    }
}
