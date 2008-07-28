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
 * specific language governing permissions and limitations under the1
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.hibernate.cfg.AnnotationConfiguration;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.util.CustomClassLoader;
import org.netbeans.modules.hibernate.hqleditor.ui.HQLEditorTopComponent;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.hibernate.util.HibernateUtil;
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
            final Project project = FileOwnerQuery.getOwner(configFileObject);
            // Construct custom classpath here.
            HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
            localResourcesURLList.addAll(env.getProjectClassPath(configFileObject));
            for (FileObject mappingFO : env.getAllHibernateMappingFileObjects()) {
                localResourcesURLList.add(mappingFO.getURL());
            }
            final ClassLoader customClassLoader = new CustomClassLoader(localResourcesURLList.toArray(new URL[]{}),
                    this.getClass().getClassLoader());

            Thread t = new Thread() {

                @Override
                public void run() {
                    Thread.currentThread().setContextClassLoader(customClassLoader);
                    HQLExecutor queryExecutor = new HQLExecutor();
                    try {
                        // Parse POJOs from HQL
                        // Check and if required compile POJO files mentioned in HQL
                        final AnnotationConfiguration customConfiguration = processAndConstructCustomConfiguration(hql, configFileObject, project);

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
        editorTopComponent.setFocusToEditor();

        editorTopComponent.fillHibernateConfigurations(activatedNodes);
    }

    public AnnotationConfiguration getHibernateConfigurationForThisContext(FileObject configFileObject, 
            List<FileObject> mappingFOList, 
            List<Class> annotatedClassList) {
        AnnotationConfiguration customConfiguration = new AnnotationConfiguration();

        // add mappings
        for (FileObject mappingFO : mappingFOList) {
            logger.info("Adding mapping to custom configuration " + mappingFO.getName());
            customConfiguration.addFile(FileUtil.toFile(mappingFO));
        }
        // add annotated pojos.
        for (Class annotatedPOJO : annotatedClassList) {
            logger.info("Adding annotated class to custom configuration " + annotatedPOJO);
            customConfiguration.addAnnotatedClass(annotatedPOJO);
        }
        
        Properties configProperties = new Properties();
        try {
            org.dom4j.io.SAXReader saxReader = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = saxReader.read(configFileObject.getInputStream());
            org.dom4j.Element sessionFactoryElement = document.getRootElement().element("session-factory");  //NOI18N
            Iterator propIterator = sessionFactoryElement.elementIterator("property");  //NOI18N
            while (propIterator.hasNext()) {
                org.dom4j.Element node = (org.dom4j.Element) propIterator.next();
                String name = node.attributeValue("name");  //NOI18N
                String value = node.getTextTrim();
                configProperties.setProperty(name, value);
                if(value != null && value.endsWith("password")) {  //NOI18N
                    value = "*****";  //NOI18N
                }
                logger.info("name = " + name + " value= " + value);
           }

            configProperties.setProperty("hibernate.mapping.precedence", "class, hbm");  //NOI18N
            customConfiguration.mergeProperties(configProperties);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return customConfiguration;
    }

    public AnnotationConfiguration processAndConstructCustomConfiguration(String hql, FileObject configFileObject, Project project) {
        HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);

        StringTokenizer hqlTokenizer = new StringTokenizer(hql, " \n\r\f\t(),");
        List<String> tokenList = new ArrayList<String>();
        while (hqlTokenizer.hasMoreTokens()) {
            tokenList.add(hqlTokenizer.nextToken());
        }

        // Process Mappings
        List<FileObject> matchedMappingFOList = new ArrayList<FileObject>();
        Map<FileObject, List<String>> mappingPOJOMap = env.getAllPOJONamesFromConfiguration(configFileObject);

        for (FileObject mappingFO : mappingPOJOMap.keySet()) {
            List<String> pojoNameList = mappingPOJOMap.get(mappingFO);
            logger.info("pojoNameList from configution : ");
            for (String name : pojoNameList) {
                logger.info("pojo-name " + name);
            }

            for (String className : pojoNameList) {
                for (String hqlClassName : tokenList) {
                    if (foundClassNameMatch(hqlClassName, className)) {
                        Class clazz = processMatchingClass(className, project);
                        logger.info("matching classname = " + className);
                        logger.info("Got clazz " + clazz);
                        if (clazz != null) {
                            matchedMappingFOList.add(mappingFO);
                        }
                    }
                }
            }
        }

        // Process Annotated POJOs.
        List<String> annotatedPOJOClassNameList = env.getAnnotatedPOJOClassNames(configFileObject);
        List<Class> matchedAnnotatedPOJOClassNameList = new ArrayList<Class>();
        if (annotatedPOJOClassNameList.size() != 0) {
            for (String annotatedClassName : annotatedPOJOClassNameList) {
                for (String hqlClassName : tokenList) {
                    if (foundClassNameMatch(hqlClassName, annotatedClassName)) {
                        Class clazz = processMatchingClass(annotatedClassName, project);
                        logger.info("matching classname = " + annotatedClassName);
                        logger.info("Got clazz " + clazz);
                        if (clazz != null) {
                            matchedAnnotatedPOJOClassNameList.add(clazz);
                        }
                    }
                }
            }
        }

        return getHibernateConfigurationForThisContext(
                configFileObject, 
                matchedMappingFOList, 
                matchedAnnotatedPOJOClassNameList
                );
    }

    private boolean foundClassNameMatch(String hqlClassName, String className) {
        boolean foundMatch = false;
        if (hqlClassName.indexOf(".") != -1) {
            if (className.endsWith(hqlClassName)) {
                foundMatch = true;
            }
        } else {
            if (className.indexOf(".") == -1) {
                if (className.equals(hqlClassName)) {
                    foundMatch = true;
                }
            } else {
                String actualClassName = className.substring(className.lastIndexOf(".") + 1);
                if (actualClassName.equals(hqlClassName)) {
                    foundMatch = true;
                }
            }
        }
        return foundMatch;
    }

    private Class processMatchingClass(String className, Project project) {
        FileObject clazzFO = HibernateUtil.findJavaFileObjectInProject(className, project);
        FileObject buildFolderFO = HibernateUtil.getBuildFO(project);
        return checkAndCompile(className, clazzFO, buildFolderFO, project);
    }

    private Class checkAndCompile(String className, FileObject sourceFO, FileObject buildFolderFO, Project project) {
        Class clazz = null;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            clazz = cl.loadClass(className);
            if (clazz != null) {
                logger.info("Found pre-existing class. Returning.." + clazz.getName());
                return clazz;
            }
        } catch (ClassNotFoundException e) {
            // Compile the class here.
            logger.info("CNF. Processing .. " + className);

            try {
                JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
                StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, null);
                className = className.replace(".", File.separator);

                File sourceFile = FileUtil.toFile(sourceFO);

                Iterable<? extends JavaFileObject> compilationUnits =
                        fileManager.getJavaFileObjectsFromFiles(Arrays.asList(new File[]{sourceFile}));
                List<File> outputPath = new ArrayList<File>();
                outputPath.add(FileUtil.toFile(buildFolderFO));
                fileManager.setLocation(StandardLocation.CLASS_OUTPUT, outputPath);
                fileManager.setLocation(StandardLocation.CLASS_PATH, getProjectClasspath(project, sourceFO));
                List<String> options = new ArrayList<String>();
                options.add("-target"); //NOI18N
                String javaVersion = System.getProperty("java.specification.version");  //NOI18N
                if(javaVersion != null || (!javaVersion.equals(""))) {
                    options.add(javaVersion);
                } else {
                    options.add("1.5"); //NOI18N
                }
                
                // for some reason the following is not working.. Bug in JavaC API?
//                options.add("-classpath");
//                options = addClasspath(project, sourceFO, options);
                //TODO diagnostic listener - plugin log
                Boolean b = javaCompiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
                logger.info("b = " + b);
                try {
                    className = className.replace(File.separator, ".");
                    clazz = cl.loadClass(className);
                    if (clazz != null) {
                        logger.info("Found class after processing. Returning.." + clazz.getName());
                        return clazz;
                    }
                } catch (ClassNotFoundException ee) {
                    logger.info("CNF after processing.. " + className);
                    Exceptions.printStackTrace(ee);
                }
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

        }
        return clazz;
    }
    
    private List<File> getProjectClasspath(Project project, FileObject sourceFO) {
        List<File> cpEntries = new ArrayList<File>();
        HibernateEnvironment env = (HibernateEnvironment)project.getLookup().lookup(HibernateEnvironment.class);
        //StringBuilder cpBuilder = new StringBuilder();
        for(URL url : env.getProjectClassPath(sourceFO)) {
            String cpEntry = url.getPath();
            cpEntry = cpEntry.replace("file:", "");
            cpEntry = cpEntry.replace("!/", "");
//            cpEntry += File.pathSeparator;
//            cpBuilder.append(cpEntry);
            File f = new File(cpEntry);
            cpEntries.add(f);
        }
        logger.info("Adding classpath " + cpEntries); //cpBuilder.toString());
        //options.add(cpBuilder.toString());
        return cpEntries; //options;
    }
}
