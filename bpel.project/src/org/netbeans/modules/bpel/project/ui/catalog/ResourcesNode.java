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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.bpel.project.ui.catalog;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.awt.Image;
import javax.swing.Action;

import org.netbeans.api.project.Project;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.xml.retriever.catalog.CatalogElement;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.06.09
 */
public final class ResourcesNode extends AbstractNode {
    
  public ResourcesNode(Project project) {
    super(Children.LEAF);
    CatalogWriteModel catalog = getCatalog(project);

    if (catalog != null) {
      setChildren(new ResourcesChildren(catalog, project.getProjectDirectory()));
    }
  }
  
  private CatalogWriteModel getCatalog(Project project) {
    try {
      return CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(project.getProjectDirectory());
    }
    catch (CatalogModelException e) {
      return null;
    }
  }

  @Override
  public Action[] getActions(boolean context) {
    return new Action[] { // todo a
//      org.openide.util.actions.SystemAction.get( org.openide.actions.FileSystemAction.class ),
      null,
      org.openide.util.actions.SystemAction.get( org.openide.actions.FindAction.class ),
      null,
//      org.openide.util.actions.SystemAction.get( org.openide.actions.PasteAction.class ),
      null,
//      org.openide.util.actions.SystemAction.get( org.openide.actions.ToolsAction.class ),
    };
  }

  @Override
  public Image getIcon(int type) {
    return IMAGE;
  }
  
  @Override
  public Image getOpenedIcon(int type) {
    return getIcon(type);
  }
  
  @Override
  public boolean canRename() {
    return false;
  }

  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  // ------------------------------------------------------------------------
  private static class ResourcesChildren extends Children.Keys<CatalogEntry> {

    protected ResourcesChildren(CatalogWriteModel catalog, FileObject project) {
      myCatalog = catalog;
    }

    @Override
    protected Node [] createNodes(CatalogEntry entry) {
      return new Node [] { new ResourceNode(entry) };
    }
    
    @Override
    protected void addNotify() {
    // todo update if catalog is changed.

//out("!!! add notify");
//        FileObject [] sources = ProjectUtil.getSources (myProject);
//        for (int i=0; i< sources.length;i++) {
////Log.out("add ChangeListener to "+sources[i].getPath());
//            setFolderChangeListener(sources[i]);
//        }
      setKeys(getKeys());
    }
/*
    private void setFolderChangeListener(FileObject folder) {
        if (folder.isFolder()) {
            folder.addFileChangeListener(new DirectoryItemsFileChangeListener());
            FileObject[] childrens = folder.getChildren();
            for (int i =0 ; i < childrens.length ; i++) {
                if (childrens[i].isFolder()) {
                    setFolderChangeListener(childrens[i]);
//Log.out("add ChangeListener to "+childrens[i].getPath());
                }
            }
        
        }
    }
*/    
//    @Override
//    protected void removeNotify() {
//out("!!! remove notify");
//      setKeys(Collections.EMPTY_SET);
//    }

    private List<CatalogEntry> getKeys() {
      return getSystems(myCatalog, new LinkedList<CatalogEntry>());
    }
   
    private List<CatalogEntry> getSystems(CatalogWriteModel catalog, List<CatalogEntry> systems) {
      Collection<CatalogEntry> entries = myCatalog.getCatalogEntries();
//out();
//out("CATALOG");

      for (CatalogEntry entry : entries) {
//out("        type: " + entry.getEntryType());
//out("      source: " + entry.getSource() + " " + project.getFileObject(entry.getSource()));
//out("      target: " + entry.getTarget());
//out("       valid: " + entry.isValid());
        if (entry.getEntryType() == CatalogElement.nextCatalog) {
        // todo a
        }
        else if (entry.getEntryType() == CatalogElement.system) {
          systems.add(entry);
        }
      }
      return systems;
    }
/*
      private class DirectoryItemsFileChangeListener extends FileChangeAdapter {
          public void fileDeleted(FileEvent fe) {
              setKeys(getKeys());
          }
          
          public void fileRenamed(FileRenameEvent fre) {
              setKeys(getKeys());
          }
          
          public void fileFolderCreated(FileEvent fe) {
              setKeys(getKeys());
              setFolderChangeListener(fe.getFile());
//Log.out("event happend on file: "+fe.getFile().getPath());
          }
          
          public void fileAttributeChanged(FileAttributeEvent fae) {
              setKeys(getKeys());
          }
          
          public void fileDataCreated(FileEvent fe) {
              setKeys(getKeys());
          }
      }
*/
    private CatalogWriteModel myCatalog;
  }

  public static void out() {
    System.out.println();
  }

  public static void out(Object object) {
    System.out.println("*** " + object); // NOI18N
  }

  private static final String NAME = "Referenced.Resources";// NOI18N
  private static final String DISPLAY_NAME = NbBundle.getMessage(ResourcesNode.class,"LBL_Referenced_Resources");// NOI18N
  private static final Image IMAGE = Utilities.loadImage("org/netbeans/modules/bpel/project/ui/resources/resources.gif"); // NOI18N
}                                                         
