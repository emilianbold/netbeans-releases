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

package com.netbeans.developer.modules.beans;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;

import org.openide.src.SourceException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.util.datatransfer.NewType;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.actions.NewAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;

/** Subnodes of this node are nodes representing source code patterns i.e. 
 * PropertyPatternNode or EventSetPatternNode.
 *
 * @author Petr Hrebejk
 */
public class  PatternGroupNode extends AbstractNode {

  /** Menu labels */
  private static final String MENU_CREATE_PROPERTY;
  private static final String MENU_CREATE_IDXPROPERTY;
  private static final String MENU_CREATE_UNICASTSE;
  private static final String MENU_CREATE_MULTICASTSE;

  // Panel and dialog for new Property Pattern
  private PropertyPatternPanel newPropertyPanel = null;
  private static DialogDescriptor newPropertyDialog = null;

  // Panel and dialog for new Indexed Property Pattern
  private IdxPropertyPatternPanel newIdxPropertyPanel = null;
  private static DialogDescriptor newIdxPropertyDialog = null;

  // Panel and dialog for new multicast EventSet Pattern
  private EventSetPatternPanel newMcEventSetPanel = null;
  private static DialogDescriptor newMcEventSetDialog = null;


  /** Array of the actions of the java methods, constructors and fields. */
  private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
    SystemAction.get(GenerateBeanInfoAction.class),
    null,
    SystemAction.get(NewAction.class),
    null,
    SystemAction.get(ToolsAction.class),
    SystemAction.get(PropertiesAction.class),
  };

  static {
    ResourceBundle bundle = PatternNode.bundle;
    MENU_CREATE_PROPERTY = bundle.getString("MENU_CREATE_PROPERTY");
    MENU_CREATE_IDXPROPERTY = bundle.getString("MENU_CREATE_IDXPROPERTY");
    MENU_CREATE_UNICASTSE = bundle.getString("MENU_CREATE_UNICASTSE");
    MENU_CREATE_MULTICASTSE = bundle.getString("MENU_CREATE_MULTICASTSE");
  }

  public static final String ICON_BASE =              
    "/com/netbeans/developer/modules/beans/resources/patternGroup";
  
  public PatternGroupNode( PatternChildren children ) {
    super( (Children)children );
    setName( PatternNode.bundle.getString( "Patterns" ) );
    setIconBase( ICON_BASE );
    setActions(DEFAULT_ACTIONS);    

    CookieSet cs = getCookieSet();
    cs.add( children.getPatternAnalyser() );
  }
	
  /** Set all actions for this node.
  * @param actions new list of actions
  */
  public void setActions(SystemAction[] actions) {
    systemActions = actions;
  }

  // ================= NewTypes ====================================

  /* Get the new types that can be created in this node.
  * For example, a node representing a Java package will permit classes to be added.
  * @return array of new type operations that are allowed
  */
  public NewType[] getNewTypes() {
    //if (writeable) {
      return new NewType[] {
        createNewType(MENU_CREATE_PROPERTY, 0),
        createNewType(MENU_CREATE_IDXPROPERTY, 1),
        createNewType(MENU_CREATE_UNICASTSE, 2),
        createNewType(MENU_CREATE_MULTICASTSE, 3),
        };
    /*
    }
    else {
      // no new types
      return super.getNewTypes();
    }
    */
  }

  /** Create one new type with the given name and the kind.
  */

  private NewType createNewType(final String name, final int kind) {
    return new NewType() {
      /** Get the name of the new type.
      * @return localized name.
      */
      public String getName() {
        return name;
      }

      /** Help context */
      public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx (PatternGroupNode.class.getName () + "." + name);
      }

      /** Creates new element */
      public void create () throws IOException {
        try {
          createElement(kind);
        }
        catch (SourceException e) {
          e.printStackTrace();
          throw new IOException(e.getMessage());
        }
      }
    };
  }

  /** Creates elements of the given kind
  * @param kind The kind of the element to create.
  * @exception SourceException if action is not allowed.
  */
  private void createElement(int kind) throws SourceException {
    DialogDescriptor dd;
    Dialog           dialog;
  
    switch (kind) {
    case 0: 
      PropertyPatternPanel propertyPanel;
      
      dd = new DialogDescriptor( (propertyPanel = new PropertyPatternPanel()),
        PatternNode.bundle.getString( "CTL_TITLE_NewProperty"),            // Title
        true,                                                 // Modal
        NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
        NotifyDescriptor.OK_OPTION,                           // Default
        DialogDescriptor.BOTTOM_ALIGN,                        // Align
        null,                                                 // Help
        propertyPanel );
       
      dialog = TopManager.getDefault().createDialog( dd );
      propertyPanel.setDialog( dialog ); 
      dialog.show ();
      
      if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
        PropertyPatternPanel.Result result = propertyPanel.getResult();
        PropertyPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(), 
                                  result.name, result.type, result.mode,
                                  result.bound, result.constrained,
                                  result.withField, result.withReturn, result.withSet,
                                  result.withSupport);
      }
      return;      
    case 1:
      IdxPropertyPatternPanel idxPropertyPanel;
      
      dd = new DialogDescriptor( (idxPropertyPanel = new IdxPropertyPatternPanel()),
        PatternNode.bundle.getString( "CTL_TITLE_NewIdxProperty"),     // Title
        true,                                                       // Modal
        NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
        NotifyDescriptor.OK_OPTION,                           // Default
        DialogDescriptor.BOTTOM_ALIGN,                        // Align
        null,                                                 // Help
        idxPropertyPanel );
       
      dialog = TopManager.getDefault().createDialog( dd );
      idxPropertyPanel.setDialog( dialog ); 
      dialog.show ();

      if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
        IdxPropertyPatternPanel.Result result = idxPropertyPanel.getResult();
        IdxPropertyPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(), 
                                  result.name, result.type, result.mode,
                                  result.bound, result.constrained,
                                  result.withField, result.withReturn, result.withSet,
                                  result.withSupport,
                                  result.niGetter, result.niWithReturn,
                                  result.niSetter, result.niWithSet );
      }
      return;
    case 2:
      UEventSetPatternPanel uEventSetPanel;

      dd = new DialogDescriptor( (uEventSetPanel = new UEventSetPatternPanel()),
        PatternNode.bundle.getString( "CTL_TITLE_NewUniCastES"),     // Title
        true,                                                 // Modal
        NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
        NotifyDescriptor.OK_OPTION,                           // Default
        DialogDescriptor.BOTTOM_ALIGN,                        // Align
        null,                                                 // Help
        uEventSetPanel );
       
      dialog = TopManager.getDefault().createDialog( dd );
      uEventSetPanel.setDialog( dialog ); 
      dialog.show ();

      if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
        UEventSetPatternPanel.Result result = uEventSetPanel.getResult();
        EventSetPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(), 
                                 result.type, result.implementation, result.firing, 
                                 result.passEvent, true ); 
      }
      return;
    case 3:
      EventSetPatternPanel eventSetPanel;

      dd = new DialogDescriptor( (eventSetPanel = new EventSetPatternPanel()),
        PatternNode.bundle.getString( "CTL_TITLE_NewMultiCastES"),     // Title
        true,                                                 // Modal
        NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
        NotifyDescriptor.OK_OPTION,                           // Default
        DialogDescriptor.BOTTOM_ALIGN,                        // Align
        null,                                                 // Help
        eventSetPanel );
       
      dialog = TopManager.getDefault().createDialog( dd );
      eventSetPanel.setDialog( dialog ); 
      dialog.show ();

      if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
        EventSetPatternPanel.Result result = eventSetPanel.getResult();
        EventSetPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(), 
                                 result.type, result.implementation, result.firing, 
                                 result.passEvent, false );      
      }
      //EventSetPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(), "NewEventListener", "java.util.EventListener", false );
      return;
    }
  }

  public static void main( String[] args ) {
  }
  
}

/* 
 * Log
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 
