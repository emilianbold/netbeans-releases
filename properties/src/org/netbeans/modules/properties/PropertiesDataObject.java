/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.properties;

import java.util.Set;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.NotActiveException;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.actions.OpenAction;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeListener;

/** Object that provides main functionality for properties data loader.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Ian Formanek
*/
public final class PropertiesDataObject extends MultiDataObject {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 4795737295255253334L;

  /** Structural view of the dataobject */
  protected transient BundleStructure bundleStructure;

  /** JTable-based editor */
  protected transient PropertiesOpen opener;

  /** Icon base for the PropertiesNode node */
  static final String PROPERTIES_ICON_BASE =
    "com/netbeans/developer/modules/loaders/properties/propertiesObject";
  static final String PROPERTIES_ICON_BASE2 =
    "com/netbeans/developer/modules/loaders/properties/propertiesLocale";

  /** Constructor */                        
  public PropertiesDataObject (final FileObject obj, final MultiFileLoader loader)
                       throws DataObjectExistsException {
    super(obj, loader);
    // use editor support
    init();
    getCookieSet().add(opener);
    getCookieSet().add(((PropertiesFileEntry)getPrimaryEntry()).getPropertiesEditor());
  }
  
  /** Initializes the object after it is created or deserialized */
  private void init() {
    opener = new PropertiesOpen((PropertiesFileEntry)getPrimaryEntry());
    bundleStructure = null;
  }
                                         
  /** Returns the support object for JTable-editing. Should be used by all subentries as well */                                       
  public PropertiesOpen getOpenSupport () {
    return opener;
  }                               
                                          
  /** Setter for modification status. Mod.status is handled by events from entries, 
  * so this method does nothing. */
  public void setModified(boolean modif) {
    // do nothing, modification status is handled in other ways
  }             
  
              
  /** Updates modification status of this dataobject from its entries. */            
  void updateModificationStatus() {
    boolean modif = false;
    if (((PresentableFileEntry)getPrimaryEntry()).isModified())
      modif = true;
    else {
      for (Iterator it = secondaryEntries().iterator(); it.hasNext(); )
        if (((PresentableFileEntry)it.next()).isModified()) {
          modif = true;                                     
          break;
        } 
    }     
                  
    super.setModified(modif);              
  }
  
  /** Provides node that should represent this data object. When a node for representation
  * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
  * with only parent changed. This implementation creates instance
  * <CODE>DataNode</CODE>.
  * <P>
  * This method is called only once.
  *
  * @return the node representation for this data object
  * @see DataNode
  */                                                           
  protected Node createNodeDelegate () {
    PropertiesChildren pc = new PropertiesChildren();

    // properties node - creates new types
    DataNode dn = new PropertiesDataNode(this, pc);
    dn.setIconBase(PROPERTIES_ICON_BASE);
    dn.setDefaultAction (SystemAction.get(OpenAction.class));
    return dn;
  }            
   
  /** Returns a structural view of this data object */
  public BundleStructure getBundleStructure() {
    if (bundleStructure == null)
      bundleStructure = new BundleStructure(this);
    return bundleStructure;  
  } 
   
  /** Help context for this object.
  * @return help context
  */
  public org.openide.util.HelpCtx getHelpCtx () {
    return new org.openide.util.HelpCtx ("com.netbeans.developer.docs.Users_Guide.usergd-using-div-12", "USERGD-USING-TABLE-2");
  }
                                         
  /** Comparator used for ordering secondary files, works over file names */
  public static Comparator getSecondaryFilesComparator() {
    return String.CASE_INSENSITIVE_ORDER;
  }
  
  /** Deserialization */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    init();
  }

  /** listener for changes in the cookie set */
/*  private PropertyChangeListener propL = new PropertyChangeListener () {
    public void propertyChange (PropertyChangeEvent ev) {
      if (ev.getPropertyName().equals(PROP_VALID)) {
//        myReaction();
//        PropertiesDataObject.this.dispose();
      }  
      if (ev.getPropertyName().equals(PROP_NAME)) {                  
//        myReaction();
//        PropertiesDataObject.this.dispose();
      }  
    }
  };*/
                     
  /** Registers itself as a PropertyChangeListener for a given entry */                                                
/*  void registerEntryListener (PropertiesFileEntry pfe) {
    pfe.addPropertyChangeListener (propL);
  }*/
                                                  
  class PropertiesChildren extends Children.Keys {

    /** Listens to changes on the dataobject */
    private PropertyChangeListener pcl = null;  
                          
    PropertiesChildren() {
      super();
    }         
      
    /** Sets all keys in the correct order */      
    protected void mySetKeys() {
    
      TreeSet ts = new TreeSet(new Comparator() {
        public int compare(Object o1, Object o2) {
          if (o1 == o2)
            return 0;
          if (o1 instanceof MultiDataObject.Entry && o2 instanceof MultiDataObject.Entry)
            return getSecondaryFilesComparator().compare(((MultiDataObject.Entry)o1).getFile().getName(), 
                                                         ((MultiDataObject.Entry)o2).getFile().getName());
          else 
            return 0;
        }
      }
      );
      
      ts.add(getPrimaryEntry());
      for (Iterator it = secondaryEntries().iterator();it.hasNext();) {
        FileEntry fe = (FileEntry)it.next();
        ts.add(fe);
      }  
                
      setKeys(ts);
    }
           
    /** Called to notify that the children has been asked for children
    * after and that they should set its keys.
    */
    protected void addNotify () {
      mySetKeys();
      // listener
      pcl = new PropertyChangeListener () {
      
        public void propertyChange(PropertyChangeEvent evt) {
          if (evt.getPropertyName().equals(PROP_FILES)) {
//System.out.println("got prop_files");          
            mySetKeys();                                
          }  
        }
        
      }; // end of inner class
      
      PropertiesDataObject.this.addPropertyChangeListener (new WeakListener.PropertyChange(pcl));
    }

    /** Called to notify that the children has lost all of its references to
    * its nodes associated to keys and that the keys could be cleared without
    * affecting any nodes (because nobody listens to that nodes).
    */
    protected void removeNotify () {
      setKeys(new ArrayList());
    }

    protected Node[] createNodes (Object key) {
      return new Node[] { ((PropertiesFileEntry)key).getNodeDelegate() };
    }

  } // end of class PropertiesChildren

}

/*
 * <<Log>>
 *  12   Gandalf   1.11        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   Gandalf   1.10        6/8/99   Petr Jiricka    
 *  10   Gandalf   1.9         5/16/99  Petr Jiricka    
 *  9    Gandalf   1.8         5/14/99  Petr Jiricka    
 *  8    Gandalf   1.7         5/13/99  Petr Jiricka    
 *  7    Gandalf   1.6         5/12/99  Petr Jiricka    
 *  6    Gandalf   1.5         5/11/99  Ian Formanek    Undone last change to 
 *       compile
 *  5    Gandalf   1.4         5/11/99  Petr Jiricka    
 *  4    Gandalf   1.3         3/9/99   Ian Formanek    Moved images to this 
 *       package
 *  3    Gandalf   1.2         2/3/99   Jaroslav Tulach Inner class for node is 
 *       not needed
 *  2    Gandalf   1.1         1/22/99  Ian Formanek    
 *  1    Gandalf   1.0         1/22/99  Ian Formanek    
 * $
 */
