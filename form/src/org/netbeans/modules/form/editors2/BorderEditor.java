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

package com.netbeans.developer.explorer.propertysheet.editors;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.openide.awt.SplittedPanel;
import org.openide.nodes.*;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.ListView;
import org.openide.explorer.*;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import com.netbeans.developerx.loaders.form.formeditor.border.*;
import com.netbeans.developer.modules.loaders.form.palette.*;

/**
* A property editor for swing border class.
*
* This editor should be in some subpackage under developerx package,
* but it is not possible now, because this package is only package where are
* property editors searched.
*
* @author Petr Hamernik
*/
public final class BorderEditor extends PropertyEditorSupport implements org.openide.explorer.propertysheet.editors.XMLPropertyEditor { // [PENDING - not for now]

  /** Icon bases for unknown border node. */
  private static final String UNKNOWN_BORDER_BASE = "com/netbeans/developer/explorer/propertysheet/editors/unknownBorder"; // NOI18N
  /** Icon bases for no border node. */
  private static final String NO_BORDER_BASE = "com/netbeans/developer/explorer/propertysheet/editors/nullBorder"; // NOI18N

  private static final ResourceBundle bundle = NbBundle.getBundle(BorderEditor.class);

  private static final String NO_BORDER = bundle.getString("LAB_NoBorder");
  private static final MessageFormat UNKNOWN_BORDER = new MessageFormat(bundle.getString("LAB_FMT_UnknownBorder"));
  
  // variables ..................................................................................

  private BorderPanel bPanel;

  private Border current;

  // init .......................................................................................

  public BorderEditor() {
    bPanel = null;
    current = null;
  }

  // main methods .......................................................................................

  public Object getValue () {
    return current;
  }

  public void setValue (Object object) {
    if (object instanceof Border) {
      current = (Border) object;

      if (bPanel != null) {
        bPanel.setValue(current);
      } 
    }
  }

  public String getAsText () {
    if (current == null) {
      return NO_BORDER; 
    } else if (current instanceof DesignBorder) {
      BorderInfo info = ((DesignBorder)current).getInfo();
      return info.getDisplayName ();
    } else {
      return org.openide.util.Utilities.getShortClassName (current.getClass ());
    }
  }

  public void setAsText (String string) {
    throw new IllegalArgumentException();
  }

  public String getJavaInitializationString () {
    if (current == null) {
      return null; // no code to generate
    }
    else {
      if (current instanceof DesignBorder) {
        StringBuffer buf = new StringBuffer();
        ((DesignBorder)current).getInfo().generateCode(buf);
        return buf.toString();
      }
      else {
        return null; // no code to generate
      }
    }
  }

  public boolean supportsCustomEditor () {
    return true;
  }

  public Component getCustomEditor () {
    if (bPanel == null) {
      bPanel = new BorderPanel();
      bPanel.setValue(current);
    }
    return bPanel;
  } 

  // innerclasses ............................................................................................

  final class BorderPanel extends ExplorerPanel implements PropertyChangeListener, VetoableChangeListener {
    ListView listView;
    PropertySheetView sheetView;

    NoBorderNode noBorder;
    UnknownBorderNode unknownBorder;
    Node root;

    static final long serialVersionUID =-2613206277499334010L;
    BorderPanel() {
      root = new AbstractNode (new Children.Array ());
      noBorder = new NoBorderNode();
      root.getChildren ().add (new Node[] { noBorder });

      PropertyChangeListener pListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          updateBorder(getExplorerManager ().getSelectedNodes()[0]);
        }
      };
      
      PaletteItem[] items = ComponentPalette.getDefault().getAllItems ();
      for (int i = 0; i < items.length; i++) {
        if (items[i].isBorder()) {
          try {
            BorderListNode listNode = new BorderListNode(items[i]);
            listNode.addPropertyChangeListener(pListener);
            root.getChildren ().add(new Node[] { listNode });
          } catch (IllegalAccessException e) { // ignore => not added to list
          } catch (InstantiationException e) { // ignore => not added to list
          }
        }
      } 
      
      getExplorerManager ().setRootContext(root);
      getExplorerManager ().addPropertyChangeListener(this);
      getExplorerManager ().addVetoableChangeListener(this);

      setLayout(new BorderLayout ());
      setBorder(new EmptyBorder(5, 5, 5, 5));

      SplittedPanel split = new SplittedPanel();
      split.setSplitType(SplittedPanel.VERTICAL);
      split.setSplitAbsolute(false);
      split.setSplitPosition(40);

      listView = new ListView();
      JPanel ppp = new JPanel();
      ppp.setBorder(new TitledBorder(bundle.getString("LAB_AvailableBorders")));
      ppp.setLayout(new BorderLayout());
      ppp.add(BorderLayout.CENTER, listView);
      split.add(ppp, SplittedPanel.ADD_TOP);

      sheetView = new PropertySheetView();
      split.add(sheetView, SplittedPanel.ADD_BOTTOM);

      add(BorderLayout.CENTER, split);

    }

    public org.openide.util.HelpCtx getHelpCtx () {
      return new HelpCtx (BorderPanel.class);
    }
    
    public Dimension getPreferredSize () {
      return new Dimension (360, 440);
    }

    void setValue(Border border) {
      if (border == null) {
        try {
          getExplorerManager ().setSelectedNodes(new Node[] { noBorder });
        } catch (PropertyVetoException e) {
        }
      }
      else if (border instanceof DesignBorder) {
        BorderInfo info = ((DesignBorder)border).getInfo();
        if (unknownBorder != null) {
          root.getChildren ().remove (new Node[] { unknownBorder });
          unknownBorder = null;
        }
        Node[] nodes = root.getChildren().getNodes ();
        for (int i = 0; i < nodes.length; i++) {
          if (nodes[i] instanceof BorderListNode) {
            BorderInfo nodeBorderInfo = ((BorderListNode)nodes[i]).getDesignBorder().getInfo();
            if (nodeBorderInfo.getClass().isAssignableFrom(info.getClass())) {
              ((BorderListNode)nodes[i]).setDesignBorder((DesignBorder)border);
              try {
                getExplorerManager ().setSelectedNodes(new Node[] { nodes[i] });
              } catch (PropertyVetoException e) {
              }
              return;
            }
          }
        } 
      } 
      else {
        if (unknownBorder != null) {
          unknownBorder.setBorder(border);
        }
        else {
          unknownBorder = new UnknownBorderNode(border);
          root.getChildren ().add(new Node[] { unknownBorder });
          try {
            getExplorerManager ().setSelectedNodes(new Node[] { unknownBorder });
          } catch (PropertyVetoException e) {
          }
        }
      }
    }

    void updateBorder(Node node) {
      if (node instanceof NoBorderNode) {
        BorderEditor.this.current = null;
      }
      else if (node instanceof UnknownBorderNode) {
        BorderEditor.this.current = ((UnknownBorderNode) node).getBorder();
      }
      else {
        BorderEditor.this.current = ((BorderListNode) node).getDesignBorder();
      }
      BorderEditor.this.firePropertyChange();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
      if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
        Node[] nodes = (Node[]) evt.getNewValue();
        switch (nodes.length) {
          case 0:
            try {
              getExplorerManager ().setSelectedNodes(new Node[] { noBorder });
            } catch (PropertyVetoException e) {
            }
            break;
          case 1:
            updateBorder(nodes[0]);
            break;
        }
      } 
    }

    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
      if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
        Node[] nodes = (Node[]) evt.getNewValue();
        if (nodes.length != 1)
          throw new PropertyVetoException("", evt); // NOI18N
      }
    }
                                     
  }
  
  static final class BorderListNode extends AbstractNode implements PropertyChangeListener {
    private PaletteItem paletteItem;
    private DesignBorder designBorder;

    BorderListNode(PaletteItem paletteItem) throws IllegalAccessException, InstantiationException {
      super(Children.LEAF);
      this.paletteItem = paletteItem;
      designBorder = paletteItem.createBorder ();
      setName (designBorder.getInfo ().getDisplayName ());
    }

    /** Find an icon for this node (in the closed state).
    * @param type constant from {@link java.beans.BeanInfo}
    * @return icon to use to represent the node
    */
    public Image getIcon (int type) {
      return designBorder.getInfo ().getIcon (type);
    }

    /** Find an icon for this node (in the open state).
    * This icon is used when the node may have children and is expanded.
    *
    * @param type constant from {@link java.beans.BeanInfo}
    * @return icon to use to represent the node when open
    */
    public Image getOpenedIcon (int type) {
      return getIcon (type);
    }

    /** Creates property set for this node */
    protected Sheet createSheet () {
      Node.Property[] props = designBorder.getInfo().getProperties();
      Sheet.Set propsSet = Sheet.createPropertiesSet ();
      propsSet.put(props);
      Sheet sheet = new Sheet ();
      sheet.put (propsSet);
      
      for (int i = 0; i < props.length; i++) {
        if (props[i] instanceof BorderInfoSupport.BorderProp) {
          ((BorderInfoSupport.BorderProp)props[i]).setPropertyChangeListener(this);
        }
      }

      return sheet;
    }

    public DesignBorder getDesignBorder() {
      return designBorder;
    }

    public void setDesignBorder(DesignBorder border) {
      designBorder = border;
    }

    public void propertyChange(PropertyChangeEvent evt) {
      designBorder = new DesignBorder(designBorder.getInfo());
      firePropertyChange("", null, null); // NOI18N
    }
  } 

  static final class NoBorderNode extends AbstractNode {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3454994916520236035L;
    
    NoBorderNode() {
      super(Children.LEAF);
      setDisplayName(NO_BORDER);
      setIconBase (NO_BORDER_BASE);
    }

  }

  static final class UnknownBorderNode extends AbstractNode {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3063018048992659100L;
    
    private Border border;
    
    UnknownBorderNode(Border border) {
      super(Children.LEAF);
      setBorder(border);
      setIconBase (UNKNOWN_BORDER_BASE);
    }

    void setBorder(Border border) {
      this.border = border;
      String longName = border.getClass().getName();
      int dot = longName.lastIndexOf('.');
      String shortName = (dot < 0) ? longName : longName.substring(dot + 1);
      setDisplayName(UNKNOWN_BORDER.format(new Object[] { longName, shortName }));
    }

    Border getBorder() {
      return border;
    }

  } 

//--------------------------------------------------------------------------
// XMLPropertyEditor implementation

  public static final String XML_BORDER = "Border"; // NOI18N

  public static final String ATTR_INFO = "info"; // NOI18N

  /** Called to load property value from specified XML subtree. If succesfully loaded, 
  * the value should be available via the getValue method.
  * An IOException should be thrown when the value cannot be restored from the specified XML element
  * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
  * @exception IOException thrown when the value cannot be restored from the specified XML element
  */
  public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
    if (!XML_BORDER.equals (element.getNodeName ())) {
      throw new java.io.IOException ();
    }
    org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
    try {
      String info = attributes.getNamedItem (ATTR_INFO).getNodeValue ();
      BorderInfo bi = (BorderInfo)org.openide.TopManager.getDefault ().currentClassLoader ().loadClass (info).newInstance ();
      org.w3c.dom.NodeList children = element.getChildNodes ();
      for (int i = 0; i < children.getLength (); i++) {
        if (children.item (i).getNodeType () == org.w3c.dom.Node.ELEMENT_NODE) {
          bi.readFromXML (children.item (i));
          break;
        }
      }
      setValue (new DesignBorder (bi));
    } catch (Exception e) {
      throw new java.io.IOException ();
    }
  }
  
  /** Called to store current property value into XML subtree. The property value should be set using the
  * setValue method prior to calling this method.
  * @param doc The XML document to store the XML in - should be used for creating nodes only
  * @return the XML DOM element representing a subtree of XML from which the value should be loaded
  */
  public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
    if (getValue () instanceof DesignBorder) {
      org.w3c.dom.Element el = doc.createElement (XML_BORDER);
      BorderInfo info = ((DesignBorder)getValue ()).getInfo ();
      el.setAttribute (ATTR_INFO, info.getClass ().getName ());
      org.w3c.dom.Node borderNode = info.storeToXML (doc);
      if (borderNode != null) el.appendChild (borderNode);
      return el;
    } else {
      return null; // cannot be saved
    }
  }

}

/*
 * Log
 *  16   Gandalf   1.15        1/13/00  Ian Formanek    NOI18N #2
 *  15   Gandalf   1.14        1/10/00  Ian Formanek    provides better 
 *       getAsText 
 *  14   Gandalf   1.13        12/3/99  Pavel Buzek     in readFromXML creating 
 *       instance with currentClassLoader
 *  13   Gandalf   1.12        11/27/99 Patrik Knakal   
 *  12   Gandalf   1.11        11/24/99 Pavel Buzek     added support for saving
 *       in XML format
 *  11   Gandalf   1.10        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         8/13/99  Jaroslav Tulach ExplorerManager change
 *  8    Gandalf   1.7         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  7    Gandalf   1.6         8/2/99   Ian Formanek    preview of XML 
 *       serialization of borders
 *  6    Gandalf   1.5         7/8/99   Jesse Glick     Context help.
 *  5    Gandalf   1.4         6/11/99  Jaroslav Tulach System.out commented
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/30/99  Ian Formanek    Finalized
 *  2    Gandalf   1.1         5/24/99  Ian Formanek    
 *  1    Gandalf   1.0         5/14/99  Ian Formanek    
 * $
 */
