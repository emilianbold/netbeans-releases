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
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

import org.openide.*;
import org.openide.actions.*;
import org.openide.awt.SplittedPanel;
import org.openide.awt.ToolbarToggleButton;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.NbMarshalledObject;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

/** Default explorer which contains toolbar with cut/copy/paste,
* switchable property sheet and menu view actions in the toolbar.
*
* @author Ian Formanek, David Simonek, Jaroslav Tulach
*/
public final class NbMainExplorer extends CloneableTopComponent 
implements ItemListener, Runnable {
  static final long serialVersionUID=6021472310669753679L; 
//  static final long serialVersionUID=-9070275145808944151L;
  
  /** The message formatter for Explorer title */
  private static MessageFormat formatExplorerTitle;

  /** list of roots (Node) */
  private List roots;
  
  /** assignes to each node one explorer panel (Node, ExplorerTab) */
  private Map rootsToPanels;

  /** currently selected node */
  private Node currentRoot;
  
  /** ExplorerManagers for property sheet */
  private transient ExplorerManager sheetManager;

  /** Listener which tracks changes on the managers for each tab and provides synchronization
  * of rootContext, exploredContext and selectedNodes with property sheet and updating the title of the Explorer */
  private transient ManagerListener managersListener;

  /** Listener which tracks changes on the root nodes (which are displayed as tabs) */
  private transient RootsListener rootsListener;

  /** action handler for cut/copy/paste/delete */
  private transient ExplorerActions actions;

  /** Switchable property view panel */
  private transient PropertySheet propertySheet;
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

    managersListener = new ManagerListener ();
    rootsListener = new RootsListener ();

    // listening on changes of
    PropertyChangeListener l = new WeakListener.PropertyChange (rootsListener);
    TopManager.getDefault ().addPropertyChangeListener (l);

    IDESettings ideS = (IDESettings)IDESettings.findObject (IDESettings.class);
    ideS.addPropertyChangeListener (l);
    
    propertySheet = new PropertySheet ();
    
    split.add(tabs, SplittedPanel.ADD_LEFT);
    split.setSplitType(SplittedPanel.HORIZONTAL);
    split.setSplitAbsolute(true);

    setLayout(new BorderLayout ());
    add(split, BorderLayout.CENTER);
    add(toolbar = createToolbar(), BorderLayout.NORTH);
  }

  /** Clones the explorer 
  *
  protected CloneableTopComponent createClonedObject () {
    NbMainExplorer main = new NbMainExplorer ();
    Iterator it = rootsToPanels.entrySet ().iterator ();
    
    HashMap map = new HashMap (rootsToPanels.size ());
    
    while (it.hasNext ()) {
      Map.Entry me = (Map.Entry)it.next ();
      Node n = (Node)me.getKey ();
      ExplorerTab tab = (ExplorerTab)me.getValue ();
      ExplorerManager man = (ExplorerManager)tab.getExplorerManager ().clone ();
      
      main.createPanel (n, man);
    }
    
    main.rootsToPanels = map;
    
    return main;
  }
  */
  
  /** Rarely used, only when fresh Expl created & no components selected. */
  public org.openide.util.HelpCtx getHelpCtx () {
    return new HelpCtx (NbMainExplorer.class);
  }
  
  /** Attaches all listeners.
  */
  public void addNotify () {
    super.addNotify ();

    tabs.addChangeListener (managersListener);
    
    SwingUtilities.invokeLater (this);
  }
  
  /** Removes all listeners.
  */
  public void removeNotify () {
    super.removeNotify ();
    
    if (actions != null) {
      actions.detach ();
      actions = null;
    }
    tabs.removeChangeListener (managersListener);
  }
  
  /** Also requests focus for current tab */
  public void requestFocus () {
    super.requestFocus();
    if (currentRoot != null) {
      ((ExplorerPanel)rootsToPanels.get(currentRoot)).requestFocus();
    }
  }
  
  /** Updates roots, selected panel, nodes, etc.
  */
  public void run () {
    refreshRoots ();
    
    ExplorerManager currentManager = 
      getRootPanel (currentRoot).getExplorerManager ();
    // create actions and attach them if we are activated
    if (actions == null) {
      IDESettings ideS = (IDESettings)IDESettings.findObject (IDESettings.class);
      actions = new ExplorerActions();
      actions.setConfirmDelete (ideS.getConfirmDelete ());
    }
    if (this.equals(TopComponent.getRegistry().getActivated())) {
      actions.attach(currentManager);
    }
    
    propertySheet.setNodes (currentManager.getSelectedNodes ());
    setActivatedNodes (currentManager.getSelectedNodes ());
    updateTitle ();
  }

  /** Refreshes current state of components, so they
  * will reflect new nodes.
  */
  final void refreshRoots () {
    List l = getRoots ();
    
    if (rootsToPanels == null) {
      rootsToPanels = new HashMap (7);
    }
    
    // first of all we have to remove the roots that
    // are no longer there
    if (roots != null) {
      HashSet toRemove = new HashSet (roots);
      toRemove.removeAll (l);
      // toRemove now contains only roots that are used no more

      Iterator it = rootsToPanels.entrySet ().iterator ();
      while (it.hasNext ()) {
        Map.Entry me = (Map.Entry)it.next ();
        Node r = (Node)me.getKey ();
        
        if (toRemove.contains (r)) {
          ExplorerTab tab = (ExplorerTab)me.getValue ();
          
          tabs.removeChangeListener (managersListener);
          tabs.remove (tab);
          tabs.addChangeListener (managersListener);
          
          it.remove ();
        }
      }
    } else {
      // initialize roots and map
      roots = new LinkedList ();
    }
    // ^^^ all tabs that should be are removed
    

    ListIterator it = l.listIterator ();
    while (it.hasNext ()) {
      Node r = (Node)it.next ();
      ExplorerTab tab = getRootPanel (r);

      if (tab == null) {
        // create and insert new tab
        tab = createPanel (r);
        
        tabs.insertTab (
          r.getDisplayName (), 
          new ImageIcon (r.getIcon (BeanInfo.ICON_COLOR_16x16)), 
          tab, 
          r.getShortDescription (),
          it.previousIndex ()
        );
      }
    }

    roots = l;

    // now select the right component
    ExplorerTab tab = getRootPanel (currentRoot);
    if (tab == null) {
      // root not found
      currentRoot = (Node)roots.get (0);
      tabs.setSelectedIndex (0);
    } else {
      tabs.setSelectedComponent (tab);
    }
  }
  
  /** Creates a panel for given node.
  */
  private ExplorerTab createPanel (Node n) {
    ExplorerManager manager = new ExplorerManager ();
    manager.setRootContext (n);
    
    return createPanel (n, manager);
  }
  
  /** Creates a panel for given node.
  */
  private ExplorerTab createPanel (Node n, ExplorerManager manager) {
    ExplorerTab panel = createPanel (manager);
    
    return panel;
  }
  
  /** Creates a panel for given node.
  */
  private ExplorerTab createPanel (ExplorerManager manager) {
    ExplorerTab panel = new ExplorerTab (manager);
    
    rootsToPanels.put (manager.getRootContext (), panel);
    
    manager.addPropertyChangeListener (
      new WeakListener.PropertyChange (managersListener)
    );
    manager.getRootContext ().addPropertyChangeListener (
      new WeakListener.PropertyChange (rootsListener)
    );
    return panel;
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
        split.add(propertySheet, SplittedPanel.ADD_LEFT);
      } else {
        split.setKeepSecondSame(true);
        split.add(propertySheet, SplittedPanel.ADD_RIGHT);
      }
    }
    else {              // hiding property sheet pane
      split.remove(propertySheet);
      int splitPos = split.getSplitPosition ();
      if (splitType == SplittedPanel.HORIZONTAL) {
        sheetWidth = propertySheet.getSize().width;
        compSize.width -= sheetWidth;
      } else {
        sheetHeight = propertySheet.getSize().height;
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
    Node[] selNodes = getRootPanel (currentRoot).getExplorerManager ().getSelectedNodes ();
    String name = null;
    if (selNodes.length == 0) {
      name = NbBundle.getBundle (NbMainExplorer.class).getString ("CTL_MainExplorerTitle_No");
    } else if (selNodes.length > 1) {
      name = NbBundle.getBundle (NbMainExplorer.class).getString ("CTL_MainExplorerTitle_Multiple");
    } else { // one node selected
      if (formatExplorerTitle == null) {
        formatExplorerTitle = new MessageFormat (
          NbBundle.getBundle (NbMainExplorer.class).getString ("FMT_MainExplorerTitle")
        );
      }
      name = formatExplorerTitle.format (new Object[] { 
          selNodes[0].getDisplayName () 
        }
      );
    }
    if (name == null) {
      name = "";
    }
    setName(name);
  }

  /** Activates copy/cut/paste actions.
  */
  protected void componentActivated () {
    if (actions != null) {
      ExplorerManager currentManager = 
        getRootPanel (currentRoot).getExplorerManager ();
      actions.attach (currentManager);
    }
  }

  /** Deactivates copy/cut/paste actions.
  */
  protected void componentDeactivated () {
    if (actions != null) {
      actions.detach ();
    }
  }
  
  //
  // Find methods
  // 
  
  private static List getRoots () {
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
    list.addLast (ns.project ());
    list.addLast (ns.session ());
    
    return list;
  }

  /** Utility method, creates the explorer's toolbar */
  private JToolBar createToolbar () {
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
    ImageIcon icon = new ImageIcon (NbMainExplorer.class.getResource(
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
  
  
  
  
  
  
  /** Serialize this top component.
  * @param in the stream to serialize to
  */
  public void writeExternal (ObjectOutput out)
              throws IOException {
    super.writeExternal(out);
    // write explorer panels and current one

    int len = roots.size ();
    out.writeInt (len);
    Iterator it = roots.iterator ();
    for (int i = 0; i < len; i++) {
      Node r = (Node)it.next ();
      ExplorerManager man = getRootPanel (r).getExplorerManager ();

      out.writeObject(new NbMarshalledObject (man));
    }

    // serializes handle to the node
    Node.Handle h = currentRoot.getHandle ();
    out.writeObject (new NbMarshalledObject (h));


    // write switchable sheet state
    out.writeBoolean (sheetVisible);
    out.writeBoolean (split.getPanesSwapped());
    out.writeInt (split.getSplitPosition());
    out.writeInt (split.getSplitType());
  }
  
  
  /** Deserialize this top component, sets as default.
  * @param in the stream to deserialize from
  */
  public void readExternal (ObjectInput in)
  throws IOException, ClassNotFoundException {
    super.readExternal(in);
    // read and update explorer panels (and managers)
    // and update tabbed pane



    int cnt = in.readInt ();
    // root to manager (Node, ExplorerTab)
    rootsToPanels = new HashMap (cnt);
    roots = new LinkedList ();

    for (int i = 0; i < cnt; i++) {
      NbMarshalledObject obj = (NbMarshalledObject)in.readObject ();
      try {
        ExplorerManager man = (ExplorerManager)obj.get ();
        Node r = man.getRootContext ();
        ExplorerTab tab = createPanel (man);
        
        tabs.addTab (
          r.getDisplayName (), 
          new ImageIcon (r.getIcon (BeanInfo.ICON_COLOR_16x16)), 
          tab, 
          r.getShortDescription ()
        );
        
        roots.add (r);
        
      } catch (IOException e) {
      } catch (ClassNotFoundException e) {
      }
    }
    
    NbMarshalledObject obj = (NbMarshalledObject)in.readObject ();
    try {
      currentRoot = ((Node.Handle)obj.get ()).getNode ();
    } catch (IOException e) {
    } catch (ClassNotFoundException e) {
    }
    
    // read property shhet switcher state...
    sheetVisible = in.readBoolean ();
    boolean swapped = in.readBoolean ();
    split.setSplitPosition(in.readInt ());
    split.setSplitType(in.readInt ());
    if (sheetVisible) {
      //split.setKeepFirstSame(true);
      split.add(propertySheet, SplittedPanel.ADD_RIGHT);
      if (swapped)
        split.swapPanes();
    }
    // toggle button (do without listening)
    sheetSwitcher.removeItemListener (this);
    sheetSwitcher.setSelected(sheetVisible); 
    sheetSwitcher.addItemListener (this);
    explorer = this;
  }
  
  /** Finds the right panel for given node.
  * @return the panel or null if no such panel exists
  */
  final ExplorerTab getRootPanel (Node root) {
    return (ExplorerTab)rootsToPanels.get (root);
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
    static final long serialVersionUID =-8202452314155464024L;
    /** composited view */
    private BeanTreeView view;
    
    public ExplorerTab (ExplorerManager m) {
      super (m);
      
      view = new BeanTreeView ();
      setLayout (new BorderLayout ());
      add (view);
    }

    /** Request focus also for asociated view */
    public void requestFocus () {
      super.requestFocus();
      view.requestFocus();
    }
  }
  
  /** Manager listener. Attached to currently selected explorer
  * panel listener. Also listens to changes in tabs.
  */
  private final class ManagerListener extends Object 
  implements PropertyChangeListener, ChangeListener {
    public void propertyChange (PropertyChangeEvent evt) {
      ExplorerTab tab = getRootPanel (currentRoot);
      if (tab == null) return;
      
      ExplorerManager currentManager = tab.getExplorerManager ();
      propertySheet.setNodes (currentManager.getSelectedNodes ());
      
      setActivatedNodes (currentManager.getSelectedNodes ());
      updateTitle ();
    }
    
    public void stateChanged (ChangeEvent evt) {
      int index = tabs.getSelectedIndex ();
      if (index < 0 || index >= roots.size ()) {
        currentRoot = null;
        if (actions != null)
          actions.detach();
        return;
      }
      currentRoot = (Node)roots.get (index);

      ExplorerManager currentManager = getRootPanel (currentRoot).getExplorerManager ();
      if (actions != null)
        actions.attach(currentManager);
      propertySheet.setNodes (currentManager.getSelectedNodes ());
      
      updateTitle ();
      setActivatedNodes (currentManager.getSelectedNodes ());
    }
  }
  
  /** Listener on roots, each root is listened for changes of 
  * name, etc.
  */
  private final class RootsListener extends Object 
  implements PropertyChangeListener {
    public void propertyChange (PropertyChangeEvent evt) {
      Object source = evt.getSource ();
      if (source instanceof IDESettings) {
        // possible change in confirm delete settings
        ExplorerActions a = actions;
        IDESettings ideS = (IDESettings)source;

        if (a != null) {
          a.setConfirmDelete (ideS.getConfirmDelete ());
        }
        return;
      }
      if (TopManager.PROP_PLACES.equals (evt.getPropertyName ())) {
        // possible change in list of roots
        refreshRoots ();
        return;
      }

      if (source == TopManager.getDefault ()) {
        // no notifications from top manager are needed
        // except PROP_PLACES
        return;
      }
      
      Node n = (Node)source;
      ExplorerTab tab = getRootPanel (n);
      if (tab == null) {
        return;
      }

      int i = roots.indexOf (n);
      
      if (Node.PROP_DISPLAY_NAME.equals (evt.getPropertyName ())) {
        tabs.setTitleAt (i, n.getDisplayName ());
      } else if (Node.PROP_ICON.equals (evt.getPropertyName ())) {
        tabs.setIconAt (i, new ImageIcon (
          n.getIcon (BeanInfo.ICON_COLOR_16x16)
        ));
      }
/* [IAN] - this is just waiting for Sun to fix bug #4158286 : no way to change ToolTip text on tabs in JTabbedPane
          else if (PROP_SHORT_DESCRIPTION.equals (evt.getPropertyName ())) {
            tabs.setTooltipAt (i, roots[i].getShortDescription ());
          } *
          break;
        }
      }
*/      
    }
  }
  
  public static void main (String[] args) throws Exception {
    NbMainExplorer e = new NbMainExplorer ();
    e.open ();
  }
}

/*
* Log
*  41   Gandalf   1.40        10/25/99 Ian Formanek    Fixed title of Main 
*       Explorer - now displays selected node instead of explored context
*  40   Gandalf   1.39        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  39   Gandalf   1.38        10/7/99  David Simonek   request focus related 
*       bugs repaired
*  38   Gandalf   1.37        9/22/99  Jaroslav Tulach Solving class cast 
*       exception.  
*  37   Gandalf   1.36        9/20/99  Jaroslav Tulach #1603
*  36   Gandalf   1.35        9/15/99  David Simonek   cut/copy/delete actions 
*       bugfix
*  35   Gandalf   1.34        8/29/99  Ian Formanek    Removed obsoleted import
*  34   Gandalf   1.33        8/20/99  Ian Formanek    Reverted last 2 changes
*  33   Gandalf   1.32        8/20/99  Ian Formanek    Fixed bug with explorer 
*       when starting clean IDE
*  32   Gandalf   1.31        8/19/99  David Simonek   cut/copy/paste/delete 
*       actions enabling hopefully fixed
*  31   Gandalf   1.30        8/18/99  David Simonek   bugfix #3463, #3461  
*  30   Gandalf   1.29        8/17/99  David Simonek   commentaries removed
*  29   Gandalf   1.28        8/13/99  Jaroslav Tulach New Main Explorer
*  28   Gandalf   1.27        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  27   Gandalf   1.26        8/3/99   Jaroslav Tulach Getting better and 
*       better.
*  26   Gandalf   1.25        8/3/99   Jaroslav Tulach Serialization of 
*       NbMainExplorer improved again.
*  25   Gandalf   1.24        8/2/99   Jaroslav Tulach 
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
