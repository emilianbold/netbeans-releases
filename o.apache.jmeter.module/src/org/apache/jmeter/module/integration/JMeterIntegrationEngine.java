/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.jmeter.module.integration;

import java.awt.Component;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Semaphore;
import javax.swing.JPopupMenu;

import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.module.JMXTypeDataNode;
import org.apache.jmeter.module.exceptions.InitializationException;
import org.apache.jmeter.module.loadgenerator.spi.impl.ProcessDescriptor;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 *
 * 
 */
public class JMeterIntegrationEngine {
  private static JMeterIntegrationEngine instance;
  
  private static Semaphore instanceLock = new Semaphore(1);
  private Semaphore parserLock = new Semaphore(1);
  
  private Map<String, HashTree> testPlans;
  
  private Collection<ProcessDescriptor> processes;
  
  private static String jmeterPath;
  
  private class TestProcessListener implements TestListener {
    private ProcessDescriptor descriptor = null;
    
    public TestProcessListener(final ProcessDescriptor process) {
      descriptor = process;
    }
    
    public void testStarted(String string) {
      descriptor.setRunning(true);
    }
    
    public void testEnded(String string) {
      descriptor.setRunning(false);
    }
    
    public void testIterationStart(LoopIterationEvent loopIterationEvent) {
    }
    
    public void testStarted() {
      descriptor.setRunning(true);
    }
    
    public void testEnded() {
      descriptor.setRunning(false);
    }
    
  }
  
  private static String jmeterCP = System.getProperty("java.class.path");
  
  /** Creates a new instance of JMeterIntegrationEngine */
  private JMeterIntegrationEngine() {
    testPlans = new WeakHashMap<String, HashTree>();
    processes = Collections.synchronizedCollection(new ArrayList<ProcessDescriptor>());
  }
  
  /**
   * Singleton getter
   */
  public static JMeterIntegrationEngine getDefault() throws InitializationException {
    if (instance == null) {
      try {
        instanceLock.acquire();
        if (instance == null) {
          instance = new JMeterIntegrationEngine();
          instance.initJMeter();
        }
      } catch (InterruptedException ex) {
        ex.printStackTrace();
        throw new InitializationException();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        instanceLock.release();
      }
    }
    
    String classpath = System.getProperty("java.class.path");
    if (classpath.indexOf("ApacheJMeter_core") == -1) {
      System.setProperty("java.class.path", classpath + File.pathSeparator + jmeterCP);
    }
    return instance;
  }
  
  public List<TestElement> getChildren(final TestElement parent, final String testPlan) {
    List<TestElement> children = new ArrayList<TestElement>();
    
    for(Object elementObj : getPlanTree(testPlan, false).list(parent)) {
      children.add((TestElement)elementObj);
    }
    
    return children;
  }
  
  public List<TestElement> getChildren(final List<TestElement> elementPath, final String testPlan) {
    List<TestElement> children = new ArrayList<TestElement>();
    
    for(Object elementObj : getPlanTree(testPlan, false).list(elementPath)) {
      children.add((TestElement)elementObj);
    }
    
    return children;
  }
  
  public JMeterPlan getPlan(final String testPlan) {
    HashTree planTree = getPlanTree(testPlan, false);
    
    return new JMeterPlan(testPlan, planTree, (TestPlan)planTree.getArray()[0]);
  }
  
  public ProcessDescriptor runTestPlan(final String testPlan) {
    HashTree planTree = getPlanTree(testPlan, true);
    TestElement root = (TestElement)planTree.getArray()[0];
    
    JMeterEngine engine = new StandardJMeterEngine();
    
    planTree.traverse(new HashTreeTraverser() {
      public void addNode(Object object, HashTree hashTree) {
        if (object instanceof TestElement) {
          if (object instanceof ResultCollector) {
            ResultCollector collector = (ResultCollector)object;
            collector.setListener((Visualizer)getElementCustomizer(collector));
          }
        }
      }
      public void processPath() {
      }
      public void subtractNode() {
      }
    });
    
    TreeCloner cloner = new TreeCloner(false);
    planTree.traverse(cloner);
    
    ProcessDescriptor process = new ProcessDescriptor(engine, testPlan, root.getPropertyAsString(TestElement.NAME), true);
    HashTree runtimeTree = cloner.getClonedTree();
    
    runtimeTree.add(new TestProcessListener(process));
    
    engine.configure(runtimeTree);
    try {
      engine.runTest();
      processes.add(process);
    } catch (Exception e) {
      e.printStackTrace();
      process = null;
    }
    
    return process;
  }
  
  public ProcessDescriptor prepareTest(final String testPlan) {
    HashTree planTree = getPlanTree(testPlan, true);
    TestElement root = (TestElement)planTree.getArray()[0];
    
    JMeterEngine engine = new StandardJMeterEngine();
    
    planTree.traverse(new HashTreeTraverser() {
      public void addNode(Object object, HashTree hashTree) {
        if (object instanceof TestElement) {
          if (object instanceof ResultCollector) {
            ResultCollector collector = (ResultCollector)object;
            collector.setListener((Visualizer)getElementCustomizer(collector));
          }
        }
      }
      public void processPath() {
      }
      public void subtractNode() {
      }
    });
    
    TreeCloner cloner = new TreeCloner(false);
    planTree.traverse(cloner);
    
    ProcessDescriptor process = new ProcessDescriptor(engine, testPlan, root.getPropertyAsString(TestElement.NAME), true);
    HashTree runtimeTree = cloner.getClonedTree();
    
    TestPlan plan = (TestPlan)runtimeTree.getArray()[0];
    Map userVariables = plan.getUserDefinedVariables();
    
    if (userVariables.containsKey("nb.enabled")) {
      process.setNbReady(true);
      process.setThreadsCount(Integer.parseInt((String)userVariables.get("nb.users")));
      process.setRampup(Integer.parseInt((String)userVariables.get("nb.rampup")));
      process.setInterleave(Integer.parseInt((String)userVariables.get("nb.interleave")));
    }
    
    runtimeTree.add(runtimeTree.getArray()[0], new TestProcessListener(process));
    engine.configure(runtimeTree);
    
    return process;
  }
  
  public void runTestPlan(final ProcessDescriptor descriptor) {
    try {
      descriptor.getEngine().runTest();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void stopTestPlan(final ProcessDescriptor descriptor) {
    descriptor.getEngine().stopTest();
  }
  
  public Collection<ProcessDescriptor> getProcesses() {
    Collection<ProcessDescriptor> result = new ArrayList<ProcessDescriptor>();
    result.addAll(processes);
    
    return result;
  }
  
  public Component getElementCustomizer(final TestElement element) {
    return JMeterGUISupport.getDefault().getGui(element);
  }
  
  public Image getElementIcon(final TestElement element) {
    return JMeterGUISupport.getDefault().getIcon(element);
  }
  
  public JPopupMenu getElementMenu(final TestElement element) {
    return JMeterGUISupport.getDefault().getPopup(element);
  }
  
  public void add(final List<TestElement> parentPath, final TestElement child, final String testPlan) {
    HashTree planTree = getPlanTree(testPlan, false);
    HashTree newTree = planTree.getTree(parentPath);
    
    if (planTree != null) {
      planTree.getTree(parentPath).add(child);
    }
  }
  
  public HashTree getPlanTree(final String testPlan, final boolean forceReload) {
    HashTree planTree = forceReload ? null : testPlans.get(testPlan);
    
    if (planTree == null) {
      try {
        parserLock.acquire();
        if (planTree == null) {
          planTree = parseJMeterTree(testPlan);
          testPlans.put(testPlan, planTree);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {
        parserLock.release();
      }
    }
    
    return planTree;
  }
  
  public boolean savePlan(final JMeterPlan plan) {
    OutputStream out = null;
    try {
      out = new FileOutputStream(plan.getPath());
      SaveService.saveTree(plan.getTree(), out);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {}
      }
    }
    return true;
  }
  
  public Process externalEdit(final String scriptPath) throws IOException {
    //    final String cmdProcessor = (Utilities.isWindows() ? "call" : "sh");
    final String jmeterRoot = jmeterPath + File.separator + "bin";
    final String jmeterExecutable = jmeterRoot + File.separator + "jmeter" + (Utilities.isWindows() ? ".bat" : "");
    
    String[] params = null;
//    System.out.println("Userdir = " + jmeterRoot);
//    System.out.println("Scirptpath = " + decoratePath(scriptPath));
    
    if (Utilities.isWindows()) {
      params = new String[]{
        jmeterExecutable,
        "-t",
        decoratePath(scriptPath)
      };
    } else {
      params = new String[]{
        "sh",
        jmeterExecutable,
        "-t",
        decoratePath(scriptPath)
      };
    }
    return Runtime.getRuntime().exec(params,null,new File(jmeterRoot));
  }
  
  public void cleanup() {
    Iterator<ProcessDescriptor> iter = processes.iterator();
    while(iter.hasNext()) {
      ProcessDescriptor desc = iter.next();
      if (!desc.isRunning()) {
        iter.remove();
      }
    }
  }
  
  public static String getLogPath() {
    return JMeterUtils.getJMeterHome() + File.separator + "summariser.log"; //NOI18N
  }
  
  public static void clearLog() {
    //    try {
    //      File file = new File(getLogPath());
    //      if (file.exists()) {
    //        file.delete();
    //      }
    //      if (file.createNewFile()) {
    //        LoggingManager.setTarget(new FileWriter(file));
    //      }
    //    } catch (IOException e) {
    //      // IGNORE
    //    }
  }
  
  /**
   * Initializes JMeter subsystem - sets all properties required by JMeter and calls static methods to initialize JMeter internals
   */
  private static void initJMeter() throws InitializationException {
    // try to locate the module jar
    URL rsrc = JMXTypeDataNode.class.getResource("/org/apache/jmeter/JMeter.class"); // NOI18N
    
    File userDir = InstalledFileLocator.getDefault().locate("modules/jmeter/bin/jmeter.properties", "org.apache.jmeter.module", false); // NOI18N
    try {
      jmeterPath = userDir.getCanonicalPath();
//      System.out.println("Calculated JMeter path = " + jmeterPath);
      jmeterPath = jmeterPath.substring(0, jmeterPath.lastIndexOf("modules" + File.separator + "jmeter") + ("modules" + File.separator + "jmeter").length()); // NOI18N
//      System.out.println("Modified JMeter path = " + jmeterPath);
      // change user.dir property - it's required by JMeter
      //      System.setProperty("user.dir", jmeterPath + File.separator + "bin"); // NOI18N
      // set JMeter home
      JMeterUtils.setJMeterHome(jmeterPath);
      // add plugins to classpath
      final String extPath = jmeterPath + File.separator + "lib" + File.separator + "ext";
      File dir = new File(extPath);
      String[] jars = dir.list(new FilenameFilter() {
        public boolean accept(File f, String name) {
          if (name.endsWith(".jar")) {
            return true;
          }
          return false;
        }
      });
      String classPath = System.getProperty("java.class.path");
      String separator = System.getProperty("path.separator");
      
      StringBuffer newClassPath = new StringBuffer();
      for(String jar : jars) {
        newClassPath.append(separator).append(extPath).append(File.separator).append(jar);
      }
      jmeterCP = newClassPath.toString();
      
      // call getProperties - this call also initializes the properties (pretty nasty hack)
      JMeterUtils.getProperties(userDir.getCanonicalPath());
      //
      //      // initialize the rest of JMeter - JMeter initializes some static fields within the constructor -> it's hell
      JMeterUtils.setLocale(Locale.getDefault());
      //      LoggingManager.setTarget(new FileWriter(getLogPath()));
    } catch (IOException e) {
      throw new InitializationException(e);
    }
  }
  
  private HashTree parseJMeterTree(final String planPath) {
    FileObject planFile = FileUtil.toFileObject(new File(planPath));
    return parseJMeterTree(planFile);
  }
  
  private HashTree parseJMeterTree(final FileObject file) {
    InputStream planInputStream = null;
    HashTree rootTree = null;
    try {
      planInputStream = new BufferedInputStream(new FileInputStream(FileUtil.toFile(file)));
      rootTree = SaveService.loadTree(planInputStream);
      
      // Remove the disabled items
      convertSubTree(rootTree);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return rootTree;
  }
  
  // no clue about the purpose; JMeter requires it
  private void convertSubTree(HashTree tree) {
    Iterator iter = new LinkedList(tree.list()).iterator();
    while (iter.hasNext()) {
      TestElement item = (TestElement) iter.next();
      if (!item.isEnabled() || item instanceof ResultCollector) {
        tree.remove(item);
      } else {
        if (item instanceof TestPlan) {
          TestPlan tp = (TestPlan) item;
          tp.setFunctionalMode(tp.isFunctionalMode());
          tp.setSerialized(tp.isSerialized());
        }
        convertSubTree(tree.getTree(item));
      }
    }
  }
  
  private String decoratePath(final String path) {
    if (Utilities.isWindows() && path.indexOf(' ') > -1) {
      return "\"" + path + "\"";
    }
    return path;
  }
}
