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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.properties.PropertiesDataObject; // PENDING
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.UserCancelException;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import java.net.URL;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.awt.Mnemonics;





/**
 * A static utility class grouping functions for dialogs for selection 
 * sources from project and other places.
 */
public class SelectorUtils {

  /**
   * The filter used to filter out folders and resource bundles.
   */
  public static final FilteredNode.NodeFilter BUNDLES_FILTER = 		    
    new FilteredNode.NodeFilter() {
      public boolean acceptNode(Node n) {
	// Has to be data object.
	DataObject dataObject = (DataObject)n.getCookie(DataObject.class);
	if(dataObject == null)
	  return false;
		   
	// Has to be a folder or a resource class.
	return 
	  ((dataObject instanceof DataFolder) && (isVisible(dataObject))) ||
	  (dataObject instanceof PropertiesDataObject); // PENDING same like above.
      }
    };

    private static boolean isVisible(DataObject dobj) {
        return (dobj.getPrimaryFile()==null) ||
            (VisibilityQuery.getDefault().isVisible(dobj.getPrimaryFile()));
            
    }
    
				       
  public static final FilteredNode.NodeFilter ALL_FILTER = 
    new FilteredNode.NodeFilter() {
      public boolean acceptNode(Node n) {
	return true;
      }
    };



  /**
   * Brings up a modal windows for selection of a resource bundle from
   * the given project.
   * @param prj the project to select from
   * @return DataObject representing the selected bundle file or null
   */
  static public DataObject selectBundle(Project prj, FileObject file) {
    try {
      Node root = bundlesNode(prj, file, true);

      Node[] selectedNodes= 
	NodeOperation.getDefault().
	select(
	       Util.getString("CTL_SelectPropDO_Dialog_Title"),
	       Util.getString("CTL_SelectPropDO_Dialog_RootTitle"),
	       root,
	       new NodeAcceptor() {
		 public boolean acceptNodes(Node[] nodes) {
		   if(nodes == null || nodes.length != 1) {
		     return false;
		   }

		   // Has to be data object.
		   DataObject dataObject = (DataObject)nodes[0].getCookie(DataObject.class);
		   if(dataObject == null)
		     return false;
                      
		   // Has to be of resource class.
		   return dataObject.getClass().equals(PropertiesDataObject.class); // PENDING same like above.
		 }
	       }
	       );
      return (DataObject)selectedNodes[0].getCookie(DataObject.class);
    } catch (UserCancelException uce) {
        //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, uce);
            // nobody is interested in the message
        return null;
    }

  }


    /**
     * Prepare node structure for showing available bundles. The
     * structure is relative to the file that should access the
     * bundle.
     * @param prj the <code>Project</code> <code>file</code> is in
     * @param file <code>FileObject</code> to show bundles for
     * @param includeFiles specifies whether to show jar files or just folders
     * @return root <code>Node</code> 
     */
    static public Node bundlesNode(Project prj, FileObject file, boolean includeFiles) {
        java.util.List nodes = new LinkedList();
        if (prj == null)
            prj = FileOwnerQuery.getOwner(file);

        ClassPath cp = ClassPath.getClassPath(file, ClassPath.EXECUTE);
        if (cp != null) nodes.addAll(getRootNodes(prj, getRoots(cp), BUNDLES_FILTER, includeFiles));
        
        return createRootFor(nodes, prj);
    }

    private static java.util.List getRoots(ClassPath cp) {
        ArrayList l = new ArrayList(cp.entries().size());
        Iterator eit = cp.entries().iterator();
        while(eit.hasNext()) {
            ClassPath.Entry e = (ClassPath.Entry)eit.next();

            // try to map it to sources
            URL url = e.getURL();
            SourceForBinaryQuery.Result r= SourceForBinaryQuery.findSourceRoots(url);
            FileObject [] fos = r.getRoots();
            if (fos.length > 0) {
                for (int i = 0 ; i < fos.length; i++) l.add(fos[i]);
            } else {
                if (e.getRoot()!=null) 
                    l.add(e.getRoot()); // add the class-path location
                                        // directly
            }
        }

        return l;
    }


    private static java.util.List getRootNodes(Project prj,
                                               java.util.List roots, 
                                               FilteredNode.NodeFilter filter,
                                               boolean includeFiles) {
        java.util.List nodes = new ArrayList(roots.size());      
        Iterator it = roots.iterator();
        while (it.hasNext()) {
            try {
                FileObject rfo = (FileObject)it.next();
                if (includeFiles || (FileUtil.toFile(rfo)!=null)) {
                    Project owner = org.netbeans.api.project.FileOwnerQuery.getOwner(rfo);
                    Node origNode = DataObject.find(rfo).getNodeDelegate();
                    FilteredNode node =  new FilteredNode(origNode,filter, getDisplayName(rfo, owner, prj!=owner));
                    nodes.add(node);
                }
            } catch (org.openide.loaders.DataObjectNotFoundException ex) {}
        }
        return nodes;
    }


    private static String getDisplayName(FileObject fo, Project owner, boolean incPrjName) {
        if (owner != null) {
            SourceGroup grp = getSourceGroup(fo, owner);
            String n = (grp!=null)?grp.getDisplayName():FileUtil.getFileDisplayName(fo);
            if (incPrjName) {
                ProjectInformation pi = ProjectUtils.getInformation(owner);
                n  += " [" + pi.getDisplayName() + "]";
            }
            return n;
        } else 
            return FileUtil.getFileDisplayName(fo);
    }

    private static SourceGroup getSourceGroup(FileObject file, Project prj) {
      Sources src = ProjectUtils.getSources(prj);
      SourceGroup[] srcgrps = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
      for (int i = 0 ; i < srcgrps.length; i++) {
          if (file == srcgrps[i].getRootFolder())
              return srcgrps[i];
      }
      return null;
    }


  /** 
   * Prepare node structure showing sources 
   * @param prj the project to select from
   * @param filter NodeFilter used to filter only relevant information
   * @return root Node of source files from <code>prj</code> filtered
   * by <code>filter</code>
   **/
  static public Node sourcesNode(Project prj, FilteredNode.NodeFilter filter) {
      Sources src = ProjectUtils.getSources(prj);
      SourceGroup[] srcgrps = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
      java.util.List nodes = new ArrayList();      
      for (int i = 0 ; i < srcgrps.length; i++) {
	try {
	  FileObject rfo = srcgrps[i].getRootFolder();
	  FilteredNode node = new FilteredNode(DataObject.find(rfo).getNodeDelegate(),
					       filter);
	  //	  node.setName(srcgrps[i].getName());
	  node.setDisplayName(srcgrps[i].getDisplayName());
	  //	node.setIcon(srcgrps[i].getIcon());
					     
	  nodes.add(node);
	} catch (org.openide.loaders.DataObjectNotFoundException ex) {}
      }

      return createRootFor(nodes, prj);
  }

    private static Node createRootFor(java.util.List nodes, Project prj) {
        // create a root for all gathered nodes
        Children ch = new Children.Array();
        Node[] nodesArray = new Node[ nodes.size() ];
        nodes.toArray( nodesArray );
        ch.add( nodesArray );

        AbstractNode ret = new AbstractNode(ch);
        ret.setDisplayName(ProjectUtils.getInformation(prj).getDisplayName());
        return ret;
    }




  /** Instantiate a template object.
   * Asks user for the target file's folder and creates the file.
   * @param project the project the template should be instantiated in
   * @param refFile the file for which bundle is created 
   * @param template the template to use
   * @return the generated DataObject
   * @exception UserCancelException if the user cancels the action
   * @exception IOException on I/O error
   * @see DataObject#createFromTemplate
   */
  public static DataObject instantiateTemplate(Project project, FileObject refFile, DataObject template) throws IOException {
    // Create component for for file name input.
    ObjectNameInputPanel panel = new ObjectNameInputPanel();
        
    Node repositoryNode =  SelectorUtils.bundlesNode(project, refFile, false);
        
    // Selects one folder from data systems.
    DataFolder dataFolder = 
      (DataFolder)NodeOperation.getDefault().select
      (
       I18nUtil.getBundle().getString ("CTL_Template_Dialog_Title"),
       I18nUtil.getBundle().getString ("CTL_Template_Dialog_RootTitle"),
       repositoryNode,
       new NodeAcceptor() {
	 public boolean acceptNodes(Node[] nodes) {
	   if(nodes == null || nodes.length != 1) {
	     return false;
	   }
                    
	   DataFolder cookie = (DataFolder)nodes[0].getCookie(DataFolder.class);
	   return (cookie != null && cookie.getPrimaryFile().canWrite());
	 }
       },
       panel
       )[0].getCookie(DataFolder.class);
        
       String name = panel.getText();
        
       DataObject newObject;
        
       if(name.equals ("")) { // NOI18N
	 newObject = template.createFromTemplate(dataFolder);
       } else {
	 newObject = template.createFromTemplate(dataFolder, name);
       }
        
       try {
	 return newObject;
       } catch(ClassCastException cce) {
	 throw new UserCancelException();
       }
  }

    public static DataObject selectOrCreateBundle(FileObject refFile, DataObject template) {
        Node rootNode = bundlesNode(null, refFile, true);
        FileSelector fs = new FileSelector(refFile, template);
        fs.getDialog(I18nUtil.getBundle().getString ("CTL_SelectPropDO_Dialog_Title"), null) // NOI18N
            .setVisible(true);
        return fs.isConfirmed() ? fs.getSelectedDataObject() : null;
    }

  /** Panel used by <code>instantiateTemplate</code> method. */
  private static class ObjectNameInputPanel extends JPanel {
        
    /** Generated Serialized Version UID. */
    static final long serialVersionUID = 1980214734060402958L;

    /** Text field. */
    JTextField text;

        
    /** Constructs panel. */
    public ObjectNameInputPanel () {
      BorderLayout layout = new BorderLayout();
      layout.setVgap(5);
      layout.setHgap(5);
      setLayout(layout);
            
      // label and text field with mnemonic
      String labelText = I18nUtil.getBundle().getString ("LBL_TemplateName");
      JLabel label = new JLabel();
      Mnemonics.setLocalizedText(label, labelText);
      text = new JTextField();
      text.getAccessibleContext().setAccessibleDescription(I18nUtil.getBundle().getString ("ACS_TEXT_ObjectNameInputPanel"));
            
      label.setLabelFor(text);
            
      add(BorderLayout.WEST, label);
      add(BorderLayout.CENTER, text);
    }

        
    /** Requets focus for text field. */
    public void requestFocus () {
      text.requestFocus ();
    }
        
    /** Getter for <code>text</code>. */
    public String getText () {
      return text.getText ();
    }

    /** Setter for <code>text</code>. */
    public void setText (String s) {
      setText(s);
    }
  } // End of nested class ObjectNameInputPanel
    



}
