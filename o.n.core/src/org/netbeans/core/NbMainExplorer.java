/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.impl;

import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.beans.*;
import java.text.MessageFormat;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.openide.*;
import org.openide.actions.*;
import org.openide.awt.SplittedPanel;
import org.openide.awt.ToolbarToggleButton;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;

import com.netbeans.developer.impl.output.OutputTab;

/** Default explorer which contains toolbar with cut/copy/paste,
* switchable property sheet and menu view actions in the toolbar.
*
* @author Ian Formanek, David Simonek
*/
public final class NbMainExplorer extends TopComponent implements ItemListener {
  /** The message formatter for Explorer title */
  private static MessageFormat formatExplorerTitle;

  /** The root nodes displayed as tabs - acquired from Places.roots() */
  private transient Node[] roots;
  /** Explorer panels for roots */
  private transient ExplorerPanel[] panels;
  /** ExplorerManagers for roots */
  private transient ExplorerManager[] managers;
  /** ExplorerManagers for property sheet */
  private transient ExplorerManager sheetManager;
  /** ExplorerManagers of the currently selected root (tab) */
  private transient ExplorerManager currentManager;
  /** Flag for tracking whether the manager/root listeners are added - used when opening/closing */
  private transient boolean listenersRegistered = false;

  /** Listener which tracks changes on the managers for each tab and provides synchronization
  * of rootContext, exploredContext and selectedNodes with property sheet and updating the title of the Explorer */
  private transient PropertyChangeListener managersListener;

  /** Listener which tracks changes on the root nodes (which are displayed as tabs) */
  private transient PropertyChangeListener rootsListener;

  /** action handler for cut/copy/paste/delete */
  private static ExplorerActions actions;

  /** Boolean flag - true, if this component is currently activated (and attached to ExplorerActions), false otherwise */
  private boolean activated = false;
  /** Switchable property view panel */
  private transient ExplorerPanel sheetPanel;
  /** tabbed pane containing explorer panels */
  private transient JTabbedPane tabs;
  /** Splitted panel containing tree view and property view */
  private transient SplittedPanel split;
  /** Explorer's toolbar */
  private transient JToolBar toolbar;
  /** Explorer's toolbar */
  private transient ToolbarToggleButton sheetSwitcher;
  /** Flag specifying if property sheet is visible */
  private boolean sheetVisible = false;
  /** the default width of the property sheet pane */
  private int sheetWidth = 250;
  /** the default height of the property sheet pane */
  private int sheetHeight = 400;
  /** Minimal initial height of this top component */
  public static final int MIN_HEIGHT = 150;
  /** Default width of main explorer */
  public static final int DEFAULT_WIDTH = 350;

  /** Default constructor
  */
  public NbMainExplorer () {
    split = new SplittedPanel();

    tabs = new JTabbedPane ();
    tabs.setTabPlacement (SwingConstants.BOTTOM);

    managersListener = new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource () == currentManager) {
          if (sheetVisible && (sheetManager != null)) {
            if (ExplorerManager.PROP_ROOT_CONTEXT.equals (evt.getPropertyName ())) {
              sheetManager.setRootContext (currentManager.getRootContext ());
            } else if (ExplorerManager.PROP_EXPLORED_CONTEXT.equals (evt.getPropertyName ())) {
              sheetManager.setExploredContext (currentManager.getExploredContext ());
            } else if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName ())) {
              try {
                sheetManager.setSelectedNodes (currentManager.getSelectedNodes ());
              } catch (PropertyVetoException e) {
                throw new InternalError ("Property Sheet must not not veto selection");
              }
            }
          }
          setActivatedNodes (currentManager.getSelectedNodes ());
          updateTitle ();
        }
      }
    };

    rootsListener = new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent evt) {
        if (TopManager.PROP_PLACES.equals (evt.getPropertyName ())) {
          // possible change in list of roots
          refreshRoots ();
          return;
        }
        
        for (int i = 0; i < roots.length; i++) {
          if (roots[i] == evt.getSource ()) {
            if (Node.PROP_DISPLAY_NAME.equals (evt.getPropertyName ())) {
              tabs.setTitleAt (i, roots[i].getDisplayName ());
            } else if (Node.PROP_ICON.equals (evt.getPropertyName ())) {
              tabs.setIconAt (i, new ImageIcon (roots [i].getIcon (BeanInfo.ICON_COLOR_16x16)));
            }
/* [IAN] - this is just waiting for Sun to fix bug #4158286 : no way to change ToolTip text on tabs in JTabbedPane
            else if (PROP_SHORT_DESCRIPTION.equals (evt.getPropertyName ())) {
              tabs.setTooltipAt (i, roots[i].getShortDescription ());
            } */
            break;
          }
        }
      }
    };

    TopManager.getDefault ().addPropertyChangeListener (rootsListener);
    
    roots = new Node[0];
    refreshRoots ();
    currentManager = managers[0]; // [PENDING]

    tabs.addChangeListener (new javax.swing.event.ChangeListener () {
        public void stateChanged (javax.swing.event.ChangeEvent evt) {
          int index = tabs.getSelectedIndex ();
          if (index < 0) {
            currentManager = null;
            return;
          }
          currentManager = managers[index];
          
          if (currentManager == null) {
            return;
          }
          
          if (activated) {
            actions.attach (currentManager);
          }
          if (sheetVisible && (sheetManager != null)) {
            sheetManager.setRootContext (currentManager.getRootContext ());
            sheetManager.setExploredContext (currentManager.getExploredContext ());
            try {
              sheetManager.setSelectedNodes (currentManager.getSelectedNodes ());
              } catch (PropertyVetoException e) {
                throw new InternalError ("Property Sheet must not not veto selection");
              }
          }
          updateTitle ();
          setActivatedNodes (currentManager.getSelectedNodes ());
        }
      }
    );

    split.add(tabs, SplittedPanel.ADD_LEFT);
    split.setSplitType(SplittedPanel.HORIZONTAL);
    split.setSplitAbsolute(true);

    setLayout(new BorderLayout ());
    add(split, BorderLayout.CENTER);
    add(toolbar = createToolbar(), BorderLayout.NORTH);

    updateTitle ();
  }

  /** Rarely used, only when fresh Expl created & no components selected. */
  public HelpCtx getHelpCtx () {
    return new HelpCtx (NbMainExplorer.class);
  }


  /** Refreshes current state of components, so they
  * will reflect new nodes.
  */
  private synchronized void refreshRoots () {
    List l = getRoots ();
    // first of all we have to remove the roots that
    // are no longer there
    if (roots != null) {
      HashSet toRemove = new HashSet (Arrays.asList (roots));
      toRemove.removeAll (l);


      for (int i = 0; i < roots.length; i++) {
        if (toRemove.contains (roots[i])) {
          tabs.remove (panels[i]);
          roots[i] = null;
          panels[i] = null;
        }
      }
    }

    // remember current mapping
    HashMap map;
    if (roots != null) {
      map = new HashMap (roots.length);
      for (int i = 0; i < roots.length; i++) {
        if (roots[i] != null) {
          map.put (roots[i], panels[i]);
        }
      }
    } else {
      map = null;
    }
 
    // panel list of (ExplorerPanel)
    List pl = new LinkedList ();
    // manager list (ExplorerManager)
    List ml = new LinkedList ();
    
    ListIterator it = l.listIterator ();
    while (it.hasNext ()) {
      Node r = (Node)it.next ();
      ExplorerPanel p = map == null ? null : (ExplorerPanel)map.get (r);

      if (p == null) {
        // create and insert new tab
        p = createPanel (r);
        
        tabs.insertTab (
          r.getDisplayName (), 
          new ImageIcon (r.getIcon (BeanInfo.ICON_COLOR_16x16)), 
          p, 
          r.getShortDescription (),
          it.previousIndex ()
        );
      }
      pl.add (p);
      ml.add (p.getExplorerManager ());
    }

    managers = (ExplorerManager[])ml.toArray (new ExplorerManager[0]);
    panels = (ExplorerPanel[])pl.toArray (new ExplorerPanel[0]);
    roots = (Node[])l.toArray (new Node[0]);
  }
  
  /** Creates a panel for given node.
  */
  private ExplorerPanel createPanel (Node n) {
    ExplorerPanel panel = new ExplorerTab ();
    ExplorerManager manager = panel.getExplorerManager ();
    manager.setRootContext (n);
    return panel;
  }
  
  private List getRoots () {
    Places.Nodes ns = TopManager.getDefault ().getPlaces ().nodes ();
    
    LinkedList list = new LinkedList(
      Arrays.asList (ns.roots ())
    );

//    Node[] roots = new Node[2 + moduleRoots.length];
//    roots[0] = ns.projectDesktop ();
//    roots[1] = ns.repository ();
//    System.arraycopy (moduleRoots, 0, roots, 2, moduleRoots.length);

    list.addFirst (ns.repository ());

    if (NbProjectOperation.hasProjectDesktop ()) {
      list.addLast (NbProjectOperation.getProjectDesktop ());
    }
    
    list.addLast (ns.environment ());
    list.addLast (DesktopNode.getProjectSettingsNode ());
    list.addLast (ns.session ());
    
    return list;
  }

  /** Utility method, creates the explorer's toolbar */
  JToolBar createToolbar () {
    JToolBar result = SystemAction.createToolbarPresenter(
      new SystemAction[] {
        SystemAction.get(CutAction.class),
        SystemAction.get(CopyAction.class),
        SystemAction.get(PasteAction.class),
        null,
        SystemAction.get(DeleteAction.class),
        null
      }
    );
    // property sheet switch action
    ImageIcon icon = new ImageIcon (getClass().getResource(
      "/com/netbeans/developer/impl/resources/actions/properties.gif"));
    sheetSwitcher = new ToolbarToggleButton (icon, sheetVisible);
    sheetSwitcher.setMargin (new java.awt.Insets (2, 0, 1, 0));
    sheetSwitcher.setToolTipText (NbBundle.getBundle (NbMainExplorer.class).getString ("CTL_ToggleProperties"));
    sheetSwitcher.addItemListener (this);
    result.add (sheetSwitcher);
    result.setBorder(new EmptyBorder(2, 0, 2, 2));
    result.setFloatable (false);
    return result;
  }

  /** Implementation of the ItemListener interface */
  public void itemStateChanged (ItemEvent evt) {
    sheetVisible = sheetSwitcher.isSelected();
    java.awt.Dimension size = split.getSize ();
    java.awt.Dimension compSize = getSize ();
    // add enclosing mode insets
    Rectangle modeBounds = 
      TopManager.getDefault().getWindowManager().getCurrentWorkspace().
      findMode(this).getBounds();
    compSize.width += modeBounds.width - compSize.width;
    compSize.height += modeBounds.height - compSize.height;
    // compute further...
    int splitType = split.getSplitType ();
    boolean swapped = split.getPanesSwapped();
    if (sheetVisible) { // showing property sheet pane
      getSheetPanel();  
      int splitPos;
      if (splitType == SplittedPanel.HORIZONTAL) {
        splitPos = swapped ? sheetWidth : size.width;
        compSize.width += sheetWidth;
      } else {
        splitPos = swapped ? sheetHeight : size.height;
        compSize.height += sheetHeight;
      }
      setRequestedSize (compSize);
      split.setSplitPosition (splitPos);
      if (swapped) {
        split.setKeepFirstSame(true);
        split.add(sheetPanel, SplittedPanel.ADD_LEFT);
      } else {
        split.setKeepSecondSame(true);
        split.add(sheetPanel, SplittedPanel.ADD_RIGHT);
      }
    }
    else {              // hiding property sheet pane
      split.remove(sheetPanel);
      int splitPos = split.getSplitPosition ();
      if (splitType == SplittedPanel.HORIZONTAL) {
        sheetWidth = sheetPanel.getSize().width;
        compSize.width -= sheetWidth;
      } else {
        sheetHeight = sheetPanel.getSize().height;
        compSize.height -= sheetHeight;
      }
      setRequestedSize (compSize);
      //split.setSplitPosition (splitPos);
    }
  }

  private void setRequestedSize (Dimension dim) {
    Workspace ws = TopManager.getDefault().getWindowManager().
                   getCurrentWorkspace();
    Mode mode = ws.findMode(this);
    if (mode != null) {
      Rectangle bounds = mode.getBounds();
      Rectangle newBounds = 
        new Rectangle(bounds.x, bounds.y, dim.width, dim.height);
      mode.setBounds(newBounds);
    }
    repaint();
  }

  private void updateTitle () {
    String name = currentManager == null ? null : currentManager.getExploredContext().getDisplayName();
    if (name == null) {
      name = "";
    }
    if (formatExplorerTitle == null) {
      formatExplorerTitle = new MessageFormat (
        NbBundle.getBundle (NbMainExplorer.class).getString ("FMT_MainExplorerTitle")
      );
    }
    setName(formatExplorerTitle.format (
      new Object[] { name }
    ));
  }

  public void open () {
    open(TopManager.getDefault().getWindowManager().getCurrentWorkspace());
  }
  
  /** Adds listener to the explorer panel in addition
  * to normal behaviour.
  */
  public void open (Workspace workspace) {
    super.open (workspace);
    if (!listenersRegistered) {
      for (int i = 0; i < managers.length; i++) {
        managers[i].addPropertyChangeListener (managersListener); 
        // synchronization of property sheet, activated nodes, title
      }
      // add listeners to changes on the roots
      for (int i = 0; i < roots.length; i++) {
        roots[i].addPropertyChangeListener (rootsListener);
      }
      listenersRegistered = true;
    }
    setActivatedNodes (currentManager.getSelectedNodes ());
    updateTitle ();
  }

  /** Removes listeners.
  */
  public boolean canClose (Workspace workspace, boolean last) {
    boolean result = super.canClose(workspace, last);
    if (result && last) {
      for (int i = 0; i < managers.length; i++) {
        managers[i].removePropertyChangeListener (managersListener);
      }
      for (int i = 0; i < roots.length; i++) {
       roots[i].removePropertyChangeListener (rootsListener);
      }
      listenersRegistered = false;
    }
    return result;
  }

  /** Activates copy/cut/paste actions.
  */
  protected void componentActivated () {
    if (actions == null) {
      actions = new ExplorerActions ();
    }
    actions.attach (currentManager);
    activated = true;
  }

  /** Deactivates copy/cut/paste actions.
  */
  protected void componentDeactivated () {
    activated = false;
    actions.detach ();
  }

  /** Serialize this top component.
  * @param in the stream to serialize to
  */
  public void writeExternal (ObjectOutput out)
              throws IOException {
    super.writeExternal(out);
    // write explorer panels and current one
    out.writeObject(panels);
    out.writeObject(new Integer(tabs.getSelectedIndex()));
    // write switchable sheet state
    out.writeObject(new Boolean(sheetVisible));
    out.writeObject(new Boolean(split.getPanesSwapped()));
    out.writeObject(new Integer(split.getSplitPosition()));
    out.writeObject(new Integer(split.getSplitType()));
  }
  
  
  /** Deserialize this top component, sets as default.
  * @param in the stream to deserialize from
  */
  public void readExternal (ObjectInput in)
              throws IOException, ClassNotFoundException {
    super.readExternal(in);
    // read and update explorer panels (and managers)
    // and update tabbed pane
    tabs.removeAll ();
    
    panels = (ExplorerPanel[])in.readObject();
    int selIndex = ((Integer)in.readObject()).intValue();
    managers = new ExplorerManager[panels.length];
    roots = new Node[panels.length];
    
    for (int i = 0; i < panels.length; i++) {
      managers[i] = panels[i].getExplorerManager ();
      roots[i] = managers[i].getRootContext ();
      tabs.addTab (
        roots[i].getDisplayName (), 
        new ImageIcon (roots[i].getIcon (BeanInfo.ICON_COLOR_16x16)),
        panels[i], 
        roots[i].getShortDescription ()
      );
    }
    
    /* JST: Has to refresh because roots are changing
    for (int i = 0; i < panels.length; i++) {
      managers[i] = panels[i].getExplorerManager();
      BeanTreeView treeView = new BeanTreeView ();
      panels[i].setLayout (new BorderLayout ());
      panels[i].add (treeView);
      tabs.addTab (
        roots[i].getDisplayName (), 
        new ImageIcon (roots [i].getIcon (BeanInfo.ICON_COLOR_16x16)),
        panels[i], 
        roots[i].getShortDescription ()
      );
    }
    */
    
    if (tabs.getTabCount () > selIndex) {
      currentManager = panels[selIndex].getExplorerManager();
      tabs.setSelectedIndex(selIndex);
    } else {
      currentManager = panels[0].getExplorerManager ();
    }
    
    // force later reassigning of listeners
    listenersRegistered = false;
    // read property shhet switcher state...
    sheetVisible = ((Boolean)in.readObject()).booleanValue();
    boolean swapped = ((Boolean)in.readObject()).booleanValue();
    split.setSplitPosition(((Integer)in.readObject()).intValue());
    split.setSplitType(((Integer)in.readObject()).intValue());
    if (sheetVisible) {
      //split.setKeepFirstSame(true);
      split.add(getSheetPanel(), SplittedPanel.ADD_RIGHT);
      if (swapped)
        split.swapPanes();
    }
    // toggle button (do without listening)
    sheetSwitcher.removeItemListener (this);
    sheetSwitcher.setSelected(sheetVisible); 
    sheetSwitcher.addItemListener (this);
    explorer = this;
  }

  /** Safe getter for sheet panel */
  private ExplorerPanel getSheetPanel () {
    if (sheetPanel == null) {
      PropertySheetView propertySheet = new PropertySheetView ();
      sheetPanel = new ExplorerPanel ();
      sheetPanel.add (propertySheet, BorderLayout.CENTER);
      sheetManager = sheetPanel.getExplorerManager ();
      sheetManager.setRootContext (currentManager.getRootContext ());
      sheetManager.setExploredContext (currentManager.getExploredContext ());
      try {
        sheetManager.setSelectedNodes (currentManager.getSelectedNodes ());
      } catch (PropertyVetoException e) {
        throw new InternalError ("Property Sheet must not not veto selection");
      }
    }
    return sheetPanel;
  }
    

// -----------------------------------------------------------------------------
// Static methods

  /** Static method to obtains the shared instance of NbMainExplorer
  * @return the shared instance of NbMainExplorer
  */
  public static NbMainExplorer getExplorer () {
    if (explorer == null) {
      explorer = new NbMainExplorer ();
    }
    return explorer;
  }

  /** Shared instance of NbMainExplorer */
  private static NbMainExplorer explorer;
  
  /** Class holding the explorer.
  */
  public static final class ExplorerTab extends ExplorerPanel {
    public ExplorerTab () {
      BeanTreeView treeView = new BeanTreeView ();
      setLayout (new BorderLayout ());
      add (treeView);
    }
  }
}

/*
* Log
*  24   Gandalf   1.23        8/1/99   Jaroslav Tulach MainExplorer now listens 
*       to changes in root elements.
*  23   Gandalf   1.22        7/30/99  David Simonek   
*  22   Gandalf   1.21        7/30/99  David Simonek   serialization fixes
*  21   Gandalf   1.20        7/28/99  David Simonek   canClose updates
*  20   Gandalf   1.19        7/21/99  David Simonek   properties switcher fixed
*  19   Gandalf   1.18        7/19/99  Jesse Glick     Context help.
*  18   Gandalf   1.17        7/16/99  Ian Formanek    Fixed bug #1800 - You can
*       drag off the explorer toolbar. 
*  17   Gandalf   1.16        7/15/99  Ian Formanek    Swapped Global and 
*       Project settings tabs
*  16   Gandalf   1.15        7/13/99  Ian Formanek    New MainExplorer tabs 
*       (usability&intuitiveness discussion results)
*  15   Gandalf   1.14        7/12/99  Jesse Glick     Context help.
*  14   Gandalf   1.13        7/11/99  David Simonek   window system change...
*  13   Gandalf   1.12        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  12   Gandalf   1.11        5/30/99  Ian Formanek    Fixed bug 1647 - Open, 
*       Compile, Rename, Execute and  etc. actions in popup menu in explorer are
*       sometimes disabled.  Fixed bug 1971 - If the tab is switched from 
*       Desktop to Repository with some nodes already selected, the actions in 
*       popupmenu might not be correctly enabled.  Fixed bug 1616 - Property 
*       sheet button in explorer has no tooltip.
*  11   Gandalf   1.10        5/15/99  David Simonek   switchable sheet 
*       serialized properly.....finally
*  10   Gandalf   1.9         5/14/99  David Simonek   serialization of 
*       switchable sheet state
*  9    Gandalf   1.8         5/11/99  David Simonek   changes to made window 
*       system correctly serializable
*  8    Gandalf   1.7         3/25/99  David Simonek   another small changes in 
*       window system
*  7    Gandalf   1.6         3/25/99  David Simonek   changes in window system,
*       initial positions, bugfixes
*  6    Gandalf   1.5         3/18/99  Ian Formanek    The title now updates 
*       when tab is switched
*  5    Gandalf   1.4         3/16/99  Ian Formanek    SINGLE mode removed, as 
*       it is there by default
*  4    Gandalf   1.3         3/16/99  Ian Formanek    Title improved
*  3    Gandalf   1.2         3/16/99  Ian Formanek    Added listening to icon 
*       and displayName changes on roots, support for ExplorerActions 
*       (Cut/Copy/...)
*  2    Gandalf   1.1         3/15/99  Ian Formanek    Added formatting of 
*       title, updating activatedNodes
*  1    Gandalf   1.0         3/14/99  Ian Formanek    
* $
*/
