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

import com.netbeans.ide.awt.SplittedPanel;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.explorer.propertysheet.PropertySheetView;
import com.netbeans.ide.explorer.view.ListView;
import com.netbeans.ide.explorer.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.util.NbBundle;

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
public final class BorderEditor extends PropertyEditorSupport {

  /** Icon bases for unknown border node. */
  private static final String UNKNOWN_BORDER_BASE = "com/netbeans/developer/explorer/propertysheet/editors/unknownBorder";
  /** Icon bases for no border node. */
  private static final String NO_BORDER_BASE = "com/netbeans/developer/explorer/propertysheet/editors/nullBorder";

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
    return null;
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

    BorderPanel() {
      root = new AbstractNode(Children.LEAF);
//      root.add(noBorder = new NoBorderNode(root));

      PropertyChangeListener pListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          updateBorder(getExplorerManager ().getSelectedNodes()[0]);
        }
      };
      
/*      Node[] categories = PaletteContext.getPaletteContext().getPaletteCategories();
      for (int i = 0; i < categories.length; i++) {
        Node[] paletteNodes = ((PaletteCategory) categories[i]).getPaletteNodes ();
        for (int j = 0; j < paletteNodes.length; j++) {
          if ((paletteNodes[j] instanceof PaletteNode) && ((PaletteNode)paletteNodes[j]).isBorder()) {
            BorderListNode listNode = new BorderListNode((PaletteNode) paletteNodes[j], root);
            listNode.addPropertyChangeListener(pListener);
            root.add(listNode);
          }
        }
      } */
      
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

    public Dimension getPreferredSize () {
      return new Dimension (360, 440);
    }

    void setValue(Border border) {
/*      if (border == null) {
        try {
          getExplorerManager ().setSelectedNodes(new Node[] { noBorder });
        }
        catch (PropertyVetoException e) {
        }
      }
      else if (border instanceof DesignBorder) {
        BorderInfo info = ((DesignBorder)border).getInfo();
        if (unknownBorder != null) {
          try {
            unknownBorder.remove();
            unknownBorder = null;
          }
          catch (NodeAccessException e) {
          }
        }
        Node[] nodes = root.getSubNodes();
        for (int i = 0; i < nodes.length; i++) {
          if (nodes[i] instanceof BorderListNode) {
            BorderInfo nodeBorderInfo = ((BorderListNode)nodes[i]).getDesignBorder().getInfo();
            if (nodeBorderInfo.getClass().isAssignableFrom(info.getClass())) {
              ((BorderListNode)nodes[i]).setDesignBorder((DesignBorder)border);
              try {
                getExplorerManager ().setSelectedNodes(new Node[] { nodes[i] });
              }
              catch (PropertyVetoException e) {
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
          unknownBorder = new UnknownBorderNode(root, border);
          root.add(unknownBorder);
          try {
            getExplorerManager ().setSelectedNodes(new Node[] { unknownBorder });
          }
          catch (PropertyVetoException e) {
          }
        }
      } */
    }

    void updateBorder(Node node) {
/*      if (node instanceof NoBorderNode) {
        BorderEditor.this.current = null;
      }
      else if (node instanceof UnknownBorderNode) {
        BorderEditor.this.current = ((UnknownBorderNode) node).getBorder();
      }
      else {
        BorderEditor.this.current = ((BorderListNode) node).getDesignBorder();
      }
      BorderEditor.this.firePropertyChange(); */
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
/*      if (ExplorerManager.PROP_SELECTEDNODES.equals(evt.getPropertyName())) {
        Node[] nodes = (Node[]) evt.getNewValue();
        switch (nodes.length) {
          case 0:
            try {
              getExplorerManager ().setSelectedNodes(new Node[] { noBorder });
            }
            catch (PropertyVetoException e) {
            }
            break;
          case 1:
            updateBorder(nodes[0]);
            break;
        }
      } */
    }

    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
      if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
        Node[] nodes = (Node[]) evt.getNewValue();
        if (nodes.length != 1)
          throw new PropertyVetoException("", evt);
      }
    }
                                     
    public void removeNotify () {
      super.removeNotify ();
      getExplorerManager ().removeListeners ();
    }
  }
  
/*  static final class BorderListNode extends FilterNode implements PropertyChangeListener {
    private PaletteItem palNode;
    private DesignBorder designBorder;

    BorderListNode(PaletteItem ref, Node parent) {
      super(ref, parent);
      palNode = ref;
      designBorder = ref.getDesignBorder();
    }

    public Node.PropertySet[] getPropertySet() {
      final Node.Property[] props = designBorder.getInfo().getProperties();
      for (int i = 0; i < props.length; i++) {
        if (props[i] instanceof BorderInfoSupport.BorderProp) {
          ((BorderInfoSupport.BorderProp)props[i]).setPropertyChangeListener(this);
        }
      }
      return new PropertySet[] { new PropertySetSupport(props) };
    }

    public javax.swing.JPopupMenu getContextMenu() {
      return new javax.swing.JPopupMenu();
    }

    public DesignBorder getDesignBorder() {
      return designBorder;
    }

    public void setDesignBorder(DesignBorder border) {
      designBorder = border;
    }

    public void propertyChange(PropertyChangeEvent evt) {
      designBorder = new DesignBorder(designBorder.getInfo());
      firePropertyChange("", null, null);
    }
  } */

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
}

/*
 * Log
 *  2    Gandalf   1.1         5/24/99  Ian Formanek    
 *  1    Gandalf   1.0         5/14/99  Ian Formanek    
 * $
 */
