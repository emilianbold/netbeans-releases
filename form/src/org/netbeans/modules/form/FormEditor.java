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

package com.netbeans.developer.modules.loaders.form;

import java.awt.*;
import java.beans.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.text.MessageFormat;
import javax.swing.*;

import org.openide.*;
import org.openide.awt.*;
import org.openide.explorer.*;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import com.netbeans.developer.util.NbVersion;
import com.netbeans.developer.modules.loaders.form.actions.*;
import com.netbeans.developer.modules.loaders.form.FormDataObject;
import com.netbeans.developer.modules.loaders.form.FormLoaderSettings;
import com.netbeans.developer.modules.loaders.form.palette.*;
import com.netbeans.developerx.loaders.form.formeditor.layouts.*;
import com.netbeans.developerx.loaders.form.formeditor.layouts.support.*;

/** A static class that manages global FormEditor issues.
*
* @author   Ian Formanek
*/
final public class FormEditor extends Object {

// -----------------------------------------------------------------------------
// Static variables

  /** The global version number of the FormEditor serialized format */
  public static final NbVersion FORM_EDITOR_VERSION = new NbVersion (1, 0);

  /** The prefix for event properties. The name of an event property
  * is a concatenation of this string and the event name.
  * E.g. for mousePressed event, the property is named "__EVENT__mousePressed"
  */
  public static final String EVENT_PREFIX = "__EVENT__";
  /** The prefix for component's layout properties. The name of such property
  * is a concatenation of this string and the component layout property's name.
  * E.g. for Direction layout property, the property is named "__LAYOUT__mousePressed"
  */
  public static final String LAYOUT_PREFIX = "__LAYOUT__";

  /** The resource bundle for the form editor */
  private static java.util.ResourceBundle formBundle = org.openide.util.NbBundle.getBundle (FormEditor.class);
  /** Settings of FormEditor */
  private static FormLoaderSettings formSettings = new FormLoaderSettings ();
  /** The DesignMode action */
  private static DesignModeAction designModeAction = new DesignModeAction ();
  /** The TestMode action */
  private static TestModeAction testModeAction = new TestModeAction ();
  /** The action that holds the curent palette state (selection/add mode) */
  private static PaletteAction paletteAction = new PaletteAction ();

  /** The default width of the ComponentInspector */
  public static final int DEFAULT_INSPECTOR_WIDTH = 250;
  /** The default height of the ComponentInspector */
  public static final int DEFAULT_INSPECTOR_HEIGHT = 400;
  /** The default percents of the splitting of the ComponentInspector */
  public static final int DEFAULT_INSPECTOR_PERCENTS = 30;

  /** The default width of the form window */
  public static final int DEFAULT_FORM_WIDTH = 300;
  /** The default height of the form window */
  public static final int DEFAULT_FORM_HEIGHT = 200;

  static ExplorerActions actions = new ExplorerActions ();  
  
// ---------------------------------------------------
// Private static variables

  private static ArrayList errorLog = new ArrayList ();
  private static ComponentInspector componentInspector;
  private static EmptyInspectorNode emptyInspectorNode;
  
  /** Default icon base for control panel. */
  private static final String EMPTY_INSPECTOR_ICON_BASE =
    "/com/netbeans/developer/modules/loaders/form/resources/emptyInspector";

// -----------------------------------------------------------------------------
// Static methods

  /** Provides the resource bundle for FormEditor */
  public static java.util.ResourceBundle getFormBundle() {
    return formBundle;
  }

  /** Provides the settings for the FormEditor */
  public static FormLoaderSettings getFormSettings () {
    return formSettings;
  }

  /** Provides the shared PaletteAction */
  public static PaletteAction getPaletteAction () {
    return paletteAction;
  }


  public static ComponentInspector getComponentInspector() {
    if (componentInspector == null) {
      componentInspector = new ComponentInspector ();
    }
    return componentInspector;
  }

  public static java.awt.Image getGridImage (Container gridCont) {
    Image gridImage = gridCont.createImage(100, 100);
    Graphics ig = gridImage.getGraphics();
    ig.setColor(gridCont.getBackground ());
    ig.fillRect(0, 0, 100, 100);
    ig.setColor(gridCont.getForeground());
    for (int j=0; j< 100; j+= 10)
      for (int i=0; i< 100; i+= 10)
        ig.drawLine(i,j,i,j);
    return gridImage;
  }

  public static String getSerializedBeanName (RADComponent comp) {
    StringBuffer name = new StringBuffer (comp.getFormManager ().getFormObject ().getName ());
    name.append ("$");
    name.append (comp.getName ());
    name.append (".ser");
    return name.toString ();
  }

  public static void defaultComponentInit (RADComponent radComp) {
    Object comp = radComp.getBeanInstance ();
    String varName = radComp.getName ();
    String propName = null;
    Object propValue = null;
    if (comp instanceof Button) {
      if ("".equals (((Button)comp).getLabel ())) {
        propName = "label";
        propValue = varName;
      }
    }
    else if (comp instanceof Checkbox) {
      if ("".equals (((Checkbox)comp).getLabel ())) {
        propName = "label";
        propValue = varName;
      }
    }
    else if (comp instanceof Label) {
      if ("".equals (((Label)comp).getText ())) {
        propName = "text";
        propValue = varName;
      }
    }
    else if (comp instanceof TextField) {
      if ("".equals (((TextField)comp).getText ())) {
        propName = "text";
        propValue = varName;
      }
    }
    else if (comp instanceof AbstractButton) { // JButton, JToggleButton, JCheckBox, JRadioButton
      if ("".equals (((AbstractButton)comp).getText ())) {
        propName = "text";
        propValue = varName;
      }
    }
    else if (comp instanceof JLabel) {
      if ("".equals (((JLabel)comp).getText ())) {
        propName = "text";
        propValue = varName;
      }
    }
/*    else if (comp instanceof JTable) {
      javax.swing.table.TableModel tm = ((JTable)comp).getModel ();
      if ((tm == null) || ((tm instanceof javax.swing.table.DefaultTableModel) &&
          (tm.getRowCount () == 0) && (tm.getColumnCount () == 0))) 
      {
        value = 
          new javax.swing.table.DefaultTableModel (
            new String[] {"Title 1", "Title 2", "Title 3", "Title 4"},
            4
          )
        propName = "model";
      }      
    } */
    else if ((comp instanceof JTextField) && (!(comp instanceof JPasswordField))) { // JTextField and not JPasswordField
      if ("".equals (((JTextField)comp).getText ())) {
        propName = "text";
        propValue = varName;
      }
    }

    if (propName != null) {
      Node.Property prop = radComp.getPropertyByName (propName);
      if (prop != null) {
        try {
          prop.setValue (propValue);
        } catch (IllegalAccessException e) {
          // never mind, ignore
        } catch (java.lang.reflect.InvocationTargetException e) {
          // never mind, ignore
        }
      }
    }
  }

  public static void defaultMenuInit (RADMenuItemComponent menuComp) {
    Object comp = menuComp.getBeanInstance ();
    String varName = menuComp.getName ();
    String propName = null;
    Object propValue = null;

    if (comp instanceof MenuItem) {
      if ("".equals (((MenuItem)comp).getLabel ())) {
        String value = "{0}";
        propName = "label";
        if (comp instanceof PopupMenu) {
          value = formBundle.getString("FMT_LAB_PopupMenu");
        }
        else if (comp instanceof Menu) {
          value = formBundle.getString("FMT_LAB_Menu");
        }
        else if (comp instanceof CheckboxMenuItem) {
          value = formBundle.getString("FMT_LAB_CheckboxMenuItem");
        }
        else {
          value = formBundle.getString("FMT_LAB_MenuItem");
        }

        propValue = MessageFormat.format(value, new Object[] { varName });
      }
    }
    else if (comp instanceof JMenuItem) {
      if ("".equals (((JMenuItem)comp).getText ())) {
        String value = "{0}";
        propName = "text";
        if (comp instanceof JCheckBoxMenuItem) {
          value = formBundle.getString("FMT_LAB_JCheckBoxMenuItem");
        }
        else if (comp instanceof JMenu) {
          value = formBundle.getString("FMT_LAB_JMenu");
        }
        else if (comp instanceof JRadioButtonMenuItem) {
          value = formBundle.getString("FMT_LAB_JRadioButtonMenuItem");
        }
        else {
          value = formBundle.getString("FMT_LAB_JMenuItem");
        }

        propValue = MessageFormat.format(value, new Object[] { varName });
      }
    }
    if (propName != null) {
      Node.Property prop = menuComp.getPropertyByName (propName);
      if (prop != null) {
        try {
          prop.setValue (propValue);
        } catch (IllegalAccessException e) {
          // never mind, ignore
        } catch (java.lang.reflect.InvocationTargetException e) {
          // never mind, ignore
        }
      }
    }
  }

  public static boolean isNonReflectedProperty (Class clazz, PropertyDescriptor desc) {
    if ("visible".equals (desc.getName ())) return true;
    else {
      if (Window.class.isAssignableFrom (clazz)) {
        if ("enabled".equals (desc.getName ())) return true;
        else if ("modal".equals (desc.getName ())) return true;
      }
    }
    return false;
  }
  
  /** A method that returns the supporting layout for some containers, which
  * have a special design-time support in the FormEditor.
  * @param itemClass The class of the component the layout is requested for
  * @return the DesignLayout that should be used in the container.
  */
  public static DesignSupportLayout getSupportLayout (Class itemClass) {
    if (javax.swing.JTabbedPane.class.isAssignableFrom (itemClass))
      return new JTabbedPaneSupportLayout ();
    else if (javax.swing.JScrollPane.class.isAssignableFrom (itemClass))
      return new JScrollPaneSupportLayout ();
    else if (java.awt.ScrollPane.class.isAssignableFrom (itemClass))
      return new ScrollPaneSupportLayout ();
    else if (javax.swing.JSplitPane.class.isAssignableFrom (itemClass))
      return new JSplitPaneSupportLayout ();
    else if (javax.swing.JLayeredPane.class.isAssignableFrom (itemClass))
      return new JLayeredPaneSupportLayout ();
    return null;
  } 

  /** @return The DesignLayout support for container represented by this PaletteNode, or
  * null, if this PaletteNode does not represent a Container or there is no design-time
  * support for the layout of the container
  */
  public static DesignLayout findDesignLayout (PaletteItem item) {
    if (!item.isContainer ()) return null;
    Class itemClass = item.getItemClass ();
    DesignSupportLayout supportLayout = getSupportLayout (itemClass);
    if (supportLayout != null) return supportLayout;

    Object sharedInstance = null;
    try {
      sharedInstance = item.getSharedInstance ();
    } catch (Exception e) {
    }
    if (sharedInstance == null) {
      return null;    // in the case when creation of new instance fails, we just return null
                      // to say, that we do not provide a design-time layout for such bean
    }

    DesignLayout newDesignLayout = null;
    
    Container container = null;
    try {
      Object value = item.getBeanInfo ().getBeanDescriptor ().getValue ("containerDelegate");
      if ((value != null) && (value instanceof String) && ((String)value).equals ("getContentPane")) {
        Method m = sharedInstance.getClass ().getMethod ("getContentPane", new Class [0]);
        container = (Container) m.invoke (sharedInstance, new Object [0]);
      }
    } catch (Exception e) { // effectively ignored - simply no containerDelegate
    }

    if (container == null)
      container = (Container)sharedInstance;
    LayoutManager lm = container.getLayout();

    if (lm != null) {
      if (lm instanceof FlowLayout) {
        newDesignLayout = new DesignFlowLayout ();
      } else if (lm instanceof BorderLayout) {
        newDesignLayout = new DesignBorderLayout ();
      } else if (lm instanceof CardLayout) {
        newDesignLayout = new DesignCardLayout ();
      } else if (lm instanceof GridLayout) {
        newDesignLayout = new DesignGridLayout ();
      } else if (lm instanceof GridBagLayout) {
        newDesignLayout = new DesignGridBagLayout ();
      } else if (lm instanceof EqualFlowLayout) {
        newDesignLayout = new DesignEqualFlowLayout ();
      } else if (lm instanceof AbsoluteLayout) {
        newDesignLayout = new DesignAbsoluteLayout ();
      } else if (lm instanceof BoxLayout) {
        newDesignLayout = new DesignBoxLayout ();
      }
      // [PENDING - dynamic layouts search]
      
/*      Class layoutClass = getDesignLayout(lm.getClass());
      
      try {
        newDesignLayout = (DesignLayout)layoutClass.newInstance();
      } catch (Exception e) { // if problem occurs ==>> null layout
        newDesignLayout = null;
      } */
    }

    return newDesignLayout;
  }

// ---------------------------------------------------
// inner classes

  /** The ComponentInspector explorer */
  final public static class ComponentInspector extends ExplorerPanel 
    implements java.io.Serializable 
  {
    /** The message formatter for Explorer title */
    private static java.text.MessageFormat formatInspectorTitle = new java.text.MessageFormat (
        formBundle.getString ("FMT_InspectorTitle")
      );

    /** A JDK 1.1. serial version UID */
//    static final long serialVersionUID = 6802346985641760699L;

    /** Currently focused form or null if no form is opened/focused */
    transient private FormManager2 formManager; 

    /** The Inspector's icon */
    private final static java.awt.Image inspectorIcon = java.awt.Toolkit.getDefaultToolkit ().getImage (
      ComponentInspector.class.getResource ("/com/netbeans/developer/modules/loaders/form/resources/inspector.gif"));

    ComponentInspector () {
      final ExplorerManager manager = getExplorerManager ();
      emptyInspectorNode = new EmptyInspectorNode ();
      manager.setRootContext (emptyInspectorNode);
      PropertySheetView sheet;
      SplittedPanel split = new SplittedPanel();
      split.add (new BeanTreeView(), SplittedPanel.ADD_FIRST);
      split.add (sheet = new PropertySheetView(), SplittedPanel.ADD_SECOND);
      split.setSplitType(org.openide.awt.SplittedPanel.VERTICAL);
      split.setSplitPosition(DEFAULT_INSPECTOR_PERCENTS);
      sheet.setDisplayWritableOnly (getFormSettings ().getDisplayWritableOnly ());

      add ("Center", split);
      
      manager.addPropertyChangeListener (new PropertyChangeListener () {
          public void propertyChange (PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName ())) {
              updateTitle ();
              if (formManager != null) {
                formManager.updateSelection (getExplorerManager ().getSelectedNodes ());
              }
            }
          }
        }
      ); 
      setIcon (inspectorIcon);
    }

    public void focusForm (FormManager2 formManager) {
      //System.out.println("Focus Form: "+formManager);
      this.formManager = formManager;
      designModeAction.setFormManager (formManager);
      testModeAction.setFormManager (formManager);
      if (formManager == null) {
        getExplorerManager ().setRootContext (emptyInspectorNode);
      } else {
        getExplorerManager ().setRootContext (formManager.getFormEditorSupport ().getFormRootNode ());
      }
    }

    FormManager2 getFocusedForm () {
      return formManager;
    }

    void setSelectedNodes (Node[] nodes, FormManager2 manager) throws java.beans.PropertyVetoException {
      if (manager == formManager) {
        getExplorerManager ().setSelectedNodes (nodes);
      }
    }

    Node[] getSelectedNodes () {
      return getExplorerManager ().getSelectedNodes ();
    }

    /** Called when the explored context changes.
    * The default implementation updates the title of the window.
    */
    protected void updateTitle () {
      Node[] nodes = getExplorerManager ().getSelectedNodes();
      String title;
      if (nodes.length == 0)
        title = formBundle.getString ("CTL_NoSelection");
      else if (nodes.length == 1) {
        RADComponentCookie cookie = (RADComponentCookie)nodes[0].getCookie (RADComponentCookie.class);
        if (cookie != null) {
          RADComponent radComponent = cookie.getRADComponent ();
          title = formatInspectorTitle.format (
            new Object[] { radComponent.getName() } );
        } else {
          title = formBundle.getString ("CTL_NoSelection");
        }
      }
      else
        title = formBundle.getString ("CTL_MultipleSelection");
      setName (title);
    }
    
    /** Fixed preferred size, so as the inherited preferred size is too big */
    public Dimension getPreferredSize () {
      return new Dimension (DEFAULT_INSPECTOR_WIDTH, DEFAULT_INSPECTOR_HEIGHT);
    }
    
    /** Provides a Help context for this object.
    * @see org.openide.Help
    * @see org.openide.HelpCtx
    */
    public org.openide.util.HelpCtx getHelp() {
      return new org.openide.util.HelpCtx (ComponentInspector.class);
    }

    /** replaces this in object stream */
    public Object writeReplace() {
      return new ResolvableHelper ();
    }

  }

  final public static class ResolvableHelper implements java.io.Serializable {
    public Object readResolve() {
      return FormEditor.getComponentInspector ();
    }
  };

  static class EmptyInspectorNode extends org.openide.nodes.AbstractNode {
    public EmptyInspectorNode () {
      super (org.openide.nodes.Children.LEAF);
      setIconBase (EMPTY_INSPECTOR_ICON_BASE);
    }

    public boolean canRename () {
      return false;
    }
  }

  final static class ErrorLogItem {
    public static final int WARNING = 0;
    public static final int ERROR = 1;

    public ErrorLogItem (String desc, Throwable t) {
      this (desc, t, ERROR);
    }

    public ErrorLogItem (String desc, Throwable t, int type) {
      thr = t;
      this.type = type;
      this.desc = desc;
    }

    String getDescription () {
      return desc;
    }

    Throwable getThrowable () {
      return thr;
    }

    int getType () {
      return type;
    }

    private String desc;
    private int type;
    private Throwable thr;
  }


  static void clearLog () {
    errorLog.clear ();
  }

  public static void fileError (String desc, Throwable t) {
    errorLog.add (new ErrorLogItem (desc, t, ErrorLogItem.ERROR));
  }

  public static void fileWarning (String desc, Throwable t) {
    errorLog.add (new ErrorLogItem (desc, t, ErrorLogItem.WARNING));
  }

  public static void displayErrorLog () {
    if (errorLog.size () == 0) return;
    for (Iterator it = errorLog.iterator (); it.hasNext ();) {
      ErrorLogItem item = (ErrorLogItem)it.next ();
      if (item.getType () == ErrorLogItem.WARNING) {
        System.out.println("WARNING: "+item.getDescription ());
      } else {
        System.out.println("ERROR: "+item.getDescription ());
      }
      if (item.getThrowable () != null) {
        System.out.println("Details:");
        item.getThrowable ().printStackTrace ();
      }
    }
    TopManager.getDefault ().notify (new NotifyDescriptor.Message (
          org.openide.util.NbBundle.getBundle (FormEditor.class).getString ("ERR_BetaErrorsNotification"),
          NotifyDescriptor.WARNING_MESSAGE
        )
     ); 
    
    //new ErrorLogDialog (errorLog).show (); // [PENDING]
    clearLog ();
  }

}

/*
 * Log
 *  26   Gandalf   1.25        7/12/99  Ian Formanek    Fixed to compile
 *  25   Gandalf   1.24        7/9/99   Ian Formanek    Menu editor improvements
 *  24   Gandalf   1.23        7/5/99   Ian Formanek    getComponentInstance->getBeanInstance,
 *        getComponentClass->getBeanClass
 *  23   Gandalf   1.22        6/25/99  Ian Formanek    Constants for default 
 *       form size
 *  22   Gandalf   1.21        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  21   Gandalf   1.20        6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  20   Gandalf   1.19        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  19   Gandalf   1.18        6/4/99   Ian Formanek    Fixed component 
 *       inspector icon
 *  18   Gandalf   1.17        6/4/99   Ian Formanek    
 *  17   Gandalf   1.16        6/2/99   Ian Formanek    ToolsAction, Reorder
 *  16   Gandalf   1.15        5/31/99  Ian Formanek    Design/Test Mode
 *  15   Gandalf   1.14        5/24/99  Ian Formanek    
 *  14   Gandalf   1.13        5/20/99  Ian Formanek    FormNodeCookie->RADComponentCookie
 *       
 *  13   Gandalf   1.12        5/16/99  Ian Formanek    Persistence 
 *       failure-proofness improved
 *  12   Gandalf   1.11        5/16/99  Ian Formanek    
 *  11   Gandalf   1.10        5/15/99  Ian Formanek    
 *  10   Gandalf   1.9         5/15/99  Ian Formanek    
 *  9    Gandalf   1.8         5/14/99  Ian Formanek    
 *  8    Gandalf   1.7         5/14/99  Ian Formanek    
 *  7    Gandalf   1.6         5/12/99  Ian Formanek    
 *  6    Gandalf   1.5         5/4/99   Ian Formanek    Package change
 *  5    Gandalf   1.4         5/2/99   Ian Formanek    
 *  4    Gandalf   1.3         4/7/99   Ian Formanek    Backward-compatible 
 *       deserialization finalized for Gandalf beta
 *  3    Gandalf   1.2         3/24/99  Ian Formanek    
 *  2    Gandalf   1.1         3/24/99  Ian Formanek    
 *  1    Gandalf   1.0         3/24/99  Ian Formanek    
 * $
 */
