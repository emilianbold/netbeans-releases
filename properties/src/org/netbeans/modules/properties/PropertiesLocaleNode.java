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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;

import com.netbeans.ide.*;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.windows.*;
import com.netbeans.ide.actions.*;
import com.netbeans.ide.text.*;
import com.netbeans.ide.util.*;
import com.netbeans.ide.util.actions.*;
import com.netbeans.ide.nodes.Node;
import com.netbeans.ide.nodes.Children;
import com.netbeans.ide.nodes.AbstractNode;
import com.netbeans.ide.nodes.NodeListener;

/** Object that provides main functionality for properties data loader.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Ian Formanek
*/

public class PropertiesLocaleNode extends FileEntryNode {
      
  static final String PROPERTIES_ICON_BASE2 = PropertiesDataObject.PROPERTIES_ICON_BASE2;
        
  /** Creates a new PropertiesLocaleNode for the given locale-specific file */
  public PropertiesLocaleNode (PropertiesFileEntry fe) {
    super(fe, fe.getChildren());
    setDisplayName(Util.getPropertiesLabel(fe));
    setIconBase(PROPERTIES_ICON_BASE2);
    setDefaultAction (SystemAction.get(OpenAction.class));

    // edit as a viewcookie
    getCookieSet().add(((PropertiesDataObject)fe.getDataObject()).getOpenSupport());
    PropertiesEditorSupport pes = new PropertiesEditorSupport ((PropertiesFileEntry)getFileEntry());
    getCookieSet ().add (pes);
  }
  
  /** Lazily initialize set of node's actions (overridable).
  * The default implementation returns <code>null</code>.
  * <p><em>Warning:</em> do not call {@link #getActions} within this method.
  * If necessary, call {@link NodeOp#getDefaultActions} to merge in.
  * @return array of actions for this node, or <code>null</code> to use the default node actions
  */
  protected SystemAction[] createActions () {
    return new SystemAction[] {
      SystemAction.get(OpenAction.class),
      SystemAction.get(ViewAction.class),
      null,
      SystemAction.get(CutAction.class),
      SystemAction.get(CopyAction.class),
      SystemAction.get(PasteAction.class),
      null,
      SystemAction.get(DeleteAction.class),
      SystemAction.get(LangRenameAction.class),
      null,
      SystemAction.get(SaveAsTemplateAction.class),
      null,
      SystemAction.get(PropertiesAction.class)
    };
  }

  /** Set the system name. Fires a property change event.
  * Also may change the display name according to {@link #displayFormat}.
  *
  * @param s the new name
  */
  public void setName (String s) {
    super.setName (s);
    setDisplayName (Util.getPropertiesLabel(getFileEntry()));
  }

  /** Clones this node */
  public Node cloneNode() {
    return new PropertiesLocaleNode((PropertiesFileEntry)getFileEntry());
  }          
  
}                                                

