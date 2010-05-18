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
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.control.gui.WorkBenchGui;
import org.apache.jmeter.gui.GUIFactory;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.Load;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.plugin.JMeterPlugin;
import org.apache.jmeter.plugin.PluginManager;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.timers.gui.AbstractTimerGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.util.JOrphanUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JMeterGUISupport implements JMeterPlugin {
  public static final String DIRTY = "dirty";
  
  private static Semaphore initLock = new Semaphore(1);
  private static JMeterGUISupport instance = null;
  private static ThreadLocal currentNodeHolder = new ThreadLocal();
  
  private JMeterTreeModel treeModel = new JMeterTreeModel();
  
  private static TestElement lastSelectedElementCopy = null, lastSelectedElement = null;
  
  private static boolean modified = false;
  private static boolean editing = false;
  private static boolean loading = false;
  
  private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  
  private final JMeterTreeListener modifiedListener = new JMeterTreeListener() {
    public void valueChanged(final TreeSelectionEvent e) {
      super.valueChanged(e);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          TreePath path = e.getPath();
          JMeterTreeNode node = (JMeterTreeNode)path.getPathComponent(path.getPathCount() - 1);
          TestElement element = (TestElement)node.getUserObject();
          
          if (lastSelectedElement != null) {
            if (lastSelectedElementCopy != null) {
              if (!lastSelectedElement.equals(lastSelectedElementCopy)) {
                setDirty(true);
              }
            } else {
              setDirty(true);
            }
          }
          
          lastSelectedElement = element;
          lastSelectedElementCopy = (TestElement)element.clone();
        }
      });
    }
    
  };
  private final JMeterTreeListener normalListener = new JMeterTreeListener();
  
  private GuiPackage jmeterGui = null;
  private MainFrame frame = null;
  
  /** Creates a new instance of JMeterGUISupport */
  private JMeterGUISupport() {
    // NB proprietary EditorKit hack
    final String editorClassBackup = JTextPane.getEditorKitClassNameForContentType("text/html");
    JTextPane.registerEditorKitForContentType("text/html", "org.apache.jmeter.module.integration.HTMLEditorKit");
    // ****
    
    PluginManager.install(this, true);
    
    modifiedListener.setActionHandler(ActionRouter.getInstance());
    normalListener.setActionHandler(ActionRouter.getInstance());
    
    jmeterGui = GuiPackage.getInstance(modifiedListener, treeModel);
    frame = new MainFrame(ActionRouter.getInstance(), treeModel, modifiedListener);
    jmeterGui.setMainFrame(frame);
    ActionRouter.getInstance().addPostActionListener(Object.class, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
//        System.out.println("Event:  " + e.getActionCommand());
      }
    });
    // NB proprietary EditorKit hack
    JTextPane.registerEditorKitForContentType("text/html", editorClassBackup);
    // ***
    
    treeModel.addTreeModelListener(new TreeModelListener() {
      public void treeNodesChanged(TreeModelEvent e) {
//        System.out.println("Nodes changed");
      }
      public void treeNodesInserted(TreeModelEvent e) {
        if (!loading) {
//          System.out.println("Nodes inserted");
          setDirty(true);
        }
      }
      public void treeNodesRemoved(TreeModelEvent e) {
        setDirty(true);
      }
      public void treeStructureChanged(TreeModelEvent e) {
//        System.out.println("Tree changed");
      }
    });
  }
  
  public final static JMeterGUISupport getDefault() {
    if (instance == null) {
      try {
        initLock.acquire();
        if (instance == null) {
          instance = new JMeterGUISupport();
        }
        initLock.release();
      } catch (InterruptedException e) {}
    }
    return instance;
  }
  
  // <editor-fold defaultstate="collapsed" desc="JMeterPlugin implementation" >
  private static final String[][] DEFAULT_ICONS = {
    { TestPlanGui.class.getName(), "org/apache/jmeter/images/beaker.gif" },//$NON-NLS-1$
    { AbstractTimerGui.class.getName(), "org/apache/jmeter/images/timer.gif" },//$NON-NLS-1$
    { ThreadGroupGui.class.getName(), "org/apache/jmeter/images/thread.gif" },//$NON-NLS-1$
    { AbstractVisualizer.class.getName(), "org/apache/jmeter/images/meter.png" },//$NON-NLS-1$
    { AbstractConfigGui.class.getName(), "org/apache/jmeter/images/testtubes.png" },//$NON-NLS-1$
    // Note: these were the original settings (just moved to a static
    // array)
    // Commented out because there is no such file
    // {
    // AbstractPreProcessorGui.class.getName(),
    // "org/apache/jmeter/images/testtubes.gif" },
    // {
    // AbstractPostProcessorGui.class.getName(),
    // "org/apache/jmeter/images/testtubes.gif" },
    { AbstractControllerGui.class.getName(), "org/apache/jmeter/images/knob.gif" },//$NON-NLS-1$
    { WorkBenchGui.class.getName(), "org/apache/jmeter/images/clipboard.gif" },//$NON-NLS-1$
    { AbstractSamplerGui.class.getName(), "org/apache/jmeter/images/pipet.png" }//$NON-NLS-1$
    // AbstractAssertionGUI not defined
  };
  
  public String[][] getIconMappings() {
    String iconProp = JMeterUtils.getPropDefault("jmeter.icons",//$NON-NLS-1$
      "org/apache/jmeter/images/icon.properties");//$NON-NLS-1$
    Properties p = JMeterUtils.loadProperties(iconProp);
    if (p == null) {
      return DEFAULT_ICONS;
    }
    String[][] iconlist = new String[p.size()][3];
    Enumeration pe = p.keys();
    int i = 0;
    while (pe.hasMoreElements()) {
      String key = (String) pe.nextElement();
      String icons[] = JOrphanUtils.split(p.getProperty(key), " ");//$NON-NLS-1$
      iconlist[i][0] = key;
      iconlist[i][1] = icons[0];
      if (icons.length > 1)
        iconlist[i][2] = icons[1];
      i++;
    }
    return iconlist;
  }
  
  public String[][] getResourceBundles() {
    return new String[0][];
  }
// </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Property change support">
  public void addPropertyChangeListener(final PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(pcl);
  }
  
  public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(propertyName, pcl);
  }
  
  public void removePropertyChangeListener(final PropertyChangeListener pcl) {
    pcs.removePropertyChangeListener(pcl);
  }
  
  public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener pcl) {
    pcs.removePropertyChangeListener(propertyName, pcl);
  }
  // </editor-fold>
  
  public Component getGui(final TestElement element) {
    JMeterGUIComponent gui = jmeterGui.getGui(element);
    gui.configure(element);
    return (Component)gui;
  }
  
  public JPopupMenu getPopup(final TestElement element) {
    currentNodeHolder.set(element);
    return jmeterGui.getGui(element).createPopupMenu();
  }
  
  public Image getIcon(final TestElement element) {
    Object gui = jmeterGui.getGui(element);
    ImageIcon icon = GUIFactory.getIcon(gui.getClass(), element.isEnabled());
    return icon != null ? icon.getImage() : null;
  }
  
  public void loadScript(final FileObject script) {
    try {
      loading = true;
      if (isDirty()) {
        ActionRouter.getInstance().doActionNow(new ActionEvent(this, 1, ActionNames.CLOSE));
      }
      HashTree tree = SaveService.loadTree(script.getInputStream());
      GuiPackage.getInstance().setTestPlanFile(FileUtil.toFile(script).getAbsolutePath());
      
      
      new Load().insertLoadedTree(1, tree);
      editing = true;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      loading = false;
    }
  }
  
  public void unloadScript() {
    GuiPackage.getInstance().getTreeModel().clearTestPlan();
    GuiPackage.getInstance().getTreeListener().getJTree().setSelectionRow(1);
    
    // Clear the name of the test plan file
    GuiPackage.getInstance().setTestPlanFile(null);
    
    ActionRouter.getInstance().actionPerformed(new ActionEvent(this, 1, ActionNames.ADD_ALL));
    
    editing = false;
    setDirty(false);
  }
  
  public void saveScript(final FileObject script) throws Exception {
    FileLock aLock = script.lock();
    try {
      OutputStreamWriter osw = new OutputStreamWriter(script.getOutputStream(aLock));
      HashTree plan = treeModel.getTestPlan();
      convertSubTree(plan);
      SaveService.saveTree(plan, osw);
      ActionRouter.getInstance().doActionNow(new ActionEvent(this, 1, ActionNames.SUB_TREE_SAVED));
    } finally {
      aLock.releaseLock();
    }
  }
  
  public Container getEditor() {
    return frame != null ? frame.getContentPane() : null;
  }
  
  public void setDirty(final boolean value) {
    if (value != modified) {
      pcs.firePropertyChange(DIRTY, modified, value);
    }
    modified = value;
  }
  
  public boolean isDirty() {
    return modified;
  }
  
  private void convertSubTree(HashTree tree) {
    Iterator iter = new LinkedList(tree.list()).iterator();
    while (iter.hasNext()) {
      JMeterTreeNode item = (JMeterTreeNode) iter.next();
      convertSubTree(tree.getTree(item));
      TestElement testElement = item.getTestElement();
      tree.replace(item, testElement);
    }
  }
}
