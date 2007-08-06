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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.loadgenerator.project.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.loadgenerator.api.EngineManager;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ScriptNodeFactory implements NodeFactory {

  public NodeList createNodes(Project p) {
    return new ScriptNodeList(p);
  }

  private static class ScriptNodeList implements NodeList<String>, PropertyChangeListener {

    private static final String SCRIPT_FILES = "scriptFiles"; //NOI18N
    private final Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    ScriptNodeList(Project proj) {
      project = proj;
    }

    public List<String> keys() {
      List<String> result = new ArrayList<String>();
      result.add(SCRIPT_FILES);
      return result;
    }

    public void addChangeListener(ChangeListener l) {
      changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
      changeSupport.removeChangeListener(l);
    }

    public Node node(String key) {
      if (key == SCRIPT_FILES) {
        return new ScriptNode(project);
      }
      assert false : "No node for key: " + key;
      return null;
    }

    public void addNotify() {
    }

    public void removeNotify() {
    }

    public void propertyChange(PropertyChangeEvent evt) {
      // The caller holds ProjectManager.mutex() read lock
      SwingUtilities.invokeLater(new Runnable() {

        public void run() {
          changeSupport.fireChange();
        }
      });
    }
  }

  private static Lookup createLookup(Project project) {
    DataFolder rootFolder = DataFolder.findFolder(project.getProjectDirectory());
    // XXX Remove root folder after FindAction rewrite
    return Lookups.fixed(new Object[]{project, rootFolder});
  }

  private static final class ScriptNode extends org.openide.nodes.AbstractNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {

    private static final Image LOADGEN_FILES_BADGE = Utilities.loadImage("org/netbeans/modules/loadgenerator/project/ui/resources/loadgen_badge.png", true); // NOI18N
    private Node projectNode;

    // icon badging >>>
    private Set files;
    private Map fileSystemListeners;
    private RequestProcessor.Task task;
    private final Object privateLock = new Object();
    private boolean iconChange;
    private boolean nameChange;
    private ChangeListener sourcesListener;
    private Map groupsListeners;
    private final Project project;
    // icon badging <<<
    private String iconbase = "org/openide/loaders/defaultFolder";

    public ScriptNode(Project prj) {
      super(ScriptChildren.forProject(prj), createLookup(prj));
      this.project = prj;
      setName("loadgenScripts"); // NOI18N
      setIconBase(iconbase);

      FileObject projectDir = prj.getProjectDirectory();
      try {
        DataObject projectDo = DataObject.find(projectDir);
        if (projectDo != null) {
          projectNode = projectDo.getNodeDelegate();
        }
      } catch (DataObjectNotFoundException e) {
      }
    }
    
    public Image getIcon(int type) {
      Image img = computeIcon(false, type);
      return (img != null) ? img : super.getIcon(type);
    }

    public Image getOpenedIcon(int type) {
      Image img = computeIcon(true, type);
      return (img != null) ? img : super.getIcon(type);
    }

    private Node getDataFolderNodeDelegate() {
      return getLookup().lookup(DataFolder.class).getNodeDelegate();
    }

    private Image computeIcon(boolean opened, int type) {
      Image image;

      image = opened ? getDataFolderNodeDelegate().getOpenedIcon(type) : getDataFolderNodeDelegate().getIcon(type);
      image = Utilities.mergeImages(image, LOADGEN_FILES_BADGE, 8, 8);

      return image;
    }

    public String getDisplayName() {
      return NbBundle.getMessage(ScriptNodeFactory.class, "LBL_Node_Script"); //NOI18N
    }

    public javax.swing.Action[] getActions(boolean context) {
      return new javax.swing.Action[]{CommonProjectActions.newFileAction(), new ScriptsRefreshAction(this)};
    }

    public void run() {
      boolean fireIcon;
      boolean fireName;
      synchronized (privateLock) {
        fireIcon = iconChange;
        fireName = nameChange;
        iconChange = false;
        nameChange = false;
      }
      if (fireIcon) {
        fireIconChange();
        fireOpenedIconChange();
      }
      if (fireName) {
        fireDisplayNameChange(null, null);
      }
    }

    public void annotationChanged(FileStatusEvent event) {
      if (task == null) {
        task = RequestProcessor.getDefault().create(this);
      }

      synchronized (privateLock) {
        if ((!iconChange && event.isIconChange()) || (!nameChange && event.isNameChange())) {
          Iterator it = files.iterator();
          while (it.hasNext()) {
            FileObject fo = (FileObject) it.next();
            if (event.hasChanged(fo)) {
              iconChange |= event.isIconChange();
              nameChange |= event.isNameChange();
            }
          }
        }
      }

      task.schedule(50); // batch by 50 ms
    }

    public void stateChanged(ChangeEvent e) {
      setProjectFiles(project);
    }

    public void propertyChange(PropertyChangeEvent evt) {
      setProjectFiles(project);
    }

    protected void setProjectFiles(Project project) {
      Sources sources = ProjectUtils.getSources(project); // returns singleton
      if (sourcesListener == null) {
        sourcesListener = WeakListeners.change(this, sources);
        sources.addChangeListener(sourcesListener);
      }
      setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
    }

    private void setGroups(Collection groups) {
      if (groupsListeners != null) {
        Iterator it = groupsListeners.keySet().iterator();
        while (it.hasNext()) {
          SourceGroup group = (SourceGroup) it.next();
          PropertyChangeListener pcl = (PropertyChangeListener) groupsListeners.get(group);
          group.removePropertyChangeListener(pcl);
        }
      }
      groupsListeners = new HashMap();
      Set roots = new HashSet();
      Iterator it = groups.iterator();
      while (it.hasNext()) {
        SourceGroup group = (SourceGroup) it.next();
        PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
        groupsListeners.put(group, pcl);
        group.addPropertyChangeListener(pcl);
        FileObject fo = group.getRootFolder();
        roots.add(fo);
      }
      setFiles(roots);
    }

    protected void setFiles(Set files) {
      if (fileSystemListeners != null) {
        Iterator it = fileSystemListeners.keySet().iterator();
        while (it.hasNext()) {
          FileSystem fs = (FileSystem) it.next();
          FileStatusListener fsl = (FileStatusListener) fileSystemListeners.get(fs);
          fs.removeFileStatusListener(fsl);
        }
      }

      fileSystemListeners = new HashMap();
      this.files = files;
      if (files == null) {
        return;
      }
      Iterator it = files.iterator();
      Set hookedFileSystems = new HashSet();
      while (it.hasNext()) {
        FileObject fo = (FileObject) it.next();
        try {
          FileSystem fs = fo.getFileSystem();
          if (hookedFileSystems.contains(fs)) {
            continue;
          }
          hookedFileSystems.add(fs);
          FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
          fs.addFileStatusListener(fsl);
          fileSystemListeners.put(fs, fsl);
        } catch (FileStateInvalidException e) {
          Exceptions.printStackTrace(Exceptions.attachMessage(e, "Can not get " + fo + " filesystem, ignoring...")); // NO18N
        }
      }
    }
  }

  private static final class ScriptChildren extends Children.Keys<FileObject> {

    private final EngineManager lgEngineManager = Lookup.getDefault().lookup(EngineManager.class);

    private final Set<FileObject> keys;
    private final java.util.Comparator<FileObject> comparator = new NodeComparator();

    private final FileChangeListener anyFileListener = new FileChangeAdapter() {

      public void fileDataCreated(FileEvent fe) {
        addKey(fe.getFile());
      }

      public void fileFolderCreated(FileEvent fe) {
        addKey(fe.getFile());
      }

      public void fileRenamed(FileRenameEvent fe) {
        addKey(fe.getFile());
      }

      public void fileDeleted(FileEvent fe) {
        removeKey(fe.getFile());
      }
    };

    private final Project project;

    private ScriptChildren(Project project) {
      this.project = project;
      keys = new HashSet<FileObject>();
      this.project.getProjectDirectory().addFileChangeListener(anyFileListener);
    }

    public static Children forProject(Project project) {
      return new ScriptChildren(project);
    }

    @Override
    protected void addNotify() {
      createKeys();
      doSetKeys();
    }

    @Override
    protected void removeNotify() {
      removeListeners();
      keys.clear();
    }

    public Node[] createNodes(FileObject key) {
      Node n = null;

      if (keys.contains(key)) {
        try {
          DataObject dataObject = DataObject.find(key);
          n = dataObject.getNodeDelegate().cloneNode();
        } catch (DataObjectNotFoundException dnfe) {
        }
      }

      return (n == null) ? new Node[0] : new Node[]{n};
    }

    public synchronized void refreshNodes() {
      addNotify();
    }

    private synchronized void addKey(FileObject key) {
      if (VisibilityQuery.getDefault().isVisible(key) && isSupported(key)) {
        //System.out.println("Adding " + key.getPath());
        keys.add(key);
        doSetKeys();
      }
      key.addFileChangeListener(anyFileListener);
    }

    private synchronized void removeKey(FileObject key) {
      //System.out.println("Removing " + key.getPath());
      key.removeFileChangeListener(anyFileListener);
      keys.remove(key);
      doSetKeys();
    }

    private synchronized void createKeys() {
      keys.clear();
      addAllScripts();
    }

    private void doSetKeys() {
      final FileObject[] result = keys.toArray(new FileObject[keys.size()]);
      java.util.Arrays.sort(result, comparator);

      SwingUtilities.invokeLater(new Runnable() {

        public void run() {
          setKeys(result);
        }
      });
    }

    private void removeListeners() {
      for (FileObject key : keys) {
        key.removeFileChangeListener(anyFileListener);
      }
    }

    private void addAllScripts() {
      FileObject rootDir = project.getProjectDirectory();
      addAllScripts(rootDir);
    }

    private void addAllScripts(FileObject forDir) {
      if (forDir.isData()) {
        return;
      }
      for (FileObject file : forDir.getChildren()) {
        file.addFileChangeListener(WeakListeners.create(FileChangeListener.class, anyFileListener, file));
        if (file.isFolder()) {
          addAllScripts(file);
        } else {
          if (VisibilityQuery.getDefault().isVisible(file) && isSupported(file)) {
            keys.add(file);
          }
        }
      }
    }

    private boolean isSupported(FileObject fo) {
      if (lgEngineManager == null) return false;
      return !lgEngineManager.findEngines(fo.getExt()).isEmpty();
    }
  }

  private static final class NodeComparator implements java.util.Comparator<FileObject> {

    public int compare(FileObject fo1, FileObject fo2) {
      int result = compareType(fo1, fo2);
      if (result == 0) {
        result = compareNames(fo1, fo2);
      }
      if (result == 0) {
        return fo1.getPath().compareTo(fo2.getPath());
      }
      return result;
    }

    private int compareType(FileObject fo1, FileObject fo2) {
      int folder1 = fo1.isFolder() ? 0 : 1;
      int folder2 = fo2.isFolder() ? 0 : 1;

      return folder1 - folder2;
    }

    private int compareNames(FileObject do1, FileObject do2) {
      return do1.getNameExt().compareTo(do2.getNameExt());
    }

    public boolean equals(Object o) {
      return o instanceof NodeComparator;
    }
  }

  private static class ScriptsRefreshAction extends AbstractAction {
    private ScriptNode associatedNode;
    public ScriptsRefreshAction(ScriptNode node) {
      super(NbBundle.getMessage(ScriptNodeFactory.class, "LBL_Refresh")); //NOI18N
      associatedNode = node;
    }
    
    public void actionPerformed(ActionEvent e) {
      ((ScriptChildren)associatedNode.getChildren()).refreshNodes();
    }
  }
}
