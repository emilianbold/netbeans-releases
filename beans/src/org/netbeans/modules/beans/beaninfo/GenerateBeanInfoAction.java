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

package com.netbeans.developer.modules.beans.beaninfo;

import java.util.ResourceBundle;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.src.ClassElement;

import com.netbeans.developer.modules.beans.PatternAnalyser;

/** 
* Search doc action.
*
* @author   Petr Hrebejk
*/
public class GenerateBeanInfoAction extends NodeAction implements java.awt.event.ActionListener {

  /** Resource bundle */
  private static final ResourceBundle bundle = NbBundle.getBundle( GenerateBeanInfoAction.class );


  /** generated Serialized Version UID */
  //static final long serialVersionUID = 1391479985940417455L;

  // The dialog for BeanInfo generation
  private java.awt.Dialog biDialog = null;
  
  
  /** Human presentable name of the action. This should be
  * presented as an item in a menu.
  * @return the name of the action
  */
  public String getName () {
    return NbBundle.getBundle (GenerateBeanInfoAction.class).getString ("CTL_GENBI_MenuItem");
  }

  /** The action's icon location.
  * @return the action's icon location
  */
  protected String iconResource () {
    return null;
    //return "/com/netbeans/developer/modules/javadoc/resources/searchDoc.gif";
  }
  
  /** Help context where to find more about the action.
  * @return the help context for this action
  */
  public HelpCtx getHelpCtx () {
    return new HelpCtx (GenerateBeanInfoAction.class);
  }

  protected boolean enable( Node[] activatedNodes ) {
    if (activatedNodes.length != 1 )
      return false;
    else
      return true;
  }


  /** This method is called by one of the "invokers" as a result of
  * some user's action that should lead to actual "performing" of the action.
  * This default implementation calls the assigned actionPerformer if it
  * is not null otherwise the action is ignored.
  */
  public void performAction ( Node[] nodes ) {
     
    if ( nodes.length < 1 )
      return;

    // Get pattern analyser & bean info and create BiAnalyser & BiNode

  
    PatternAnalyser pa = (PatternAnalyser)nodes[0].getCookie( PatternAnalyser.class );
    ClassElement superClass = BiSuperClass.createForClassElement( pa.getClassElement() );
    
    
    org.openide.src.MethodElement m[] = superClass.getMethods();
    
    pa = new PatternAnalyser( superClass );
    pa.analyzeAll();
    

    BiAnalyser bia = new BiAnalyser( pa );
    Node biNode = new BiNode( bia );

    // Open the diaog for bean info generation

    BiPanel biPanel;

    DialogDescriptor dd = new DialogDescriptor( (biPanel = new BiPanel( biNode )),
      bundle.getString( "CTL_TITLE_GenerateBeanInfo"),     // Title
      true,                                                 // Modal
      NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
      NotifyDescriptor.OK_OPTION,                           // Default
      DialogDescriptor.BOTTOM_ALIGN,                        // Align
      new HelpCtx (GenerateBeanInfoAction.class.getName () + ".dialog"), // Help
      null );
     
    biDialog = TopManager.getDefault().createDialog( dd );
    biDialog.show ();
    biPanel.expandAll();

    if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
     
      bia.regenerateSource();
      
      /*
      BeanInfoSource bis = new BeanInfoSource( pa.getClassElement(), pa );

      bis.open();
      bis.regenerateProperties();
      //bis.regenerateEvents();
      */
    }

    }

  public static void main( String[] args ) {
    
    ClassElement ce = ClassElement.forName( "btest.BeanTest" );
    PatternAnalyser pa  =  (PatternAnalyser)ce.getCookie( PatternAnalyser.class );
    
    
    //performAction()
  }
  
    
  /*
  public void actionPerformed(final java.awt.event.ActionEvent evt ) {

    biDialog.setVisible( false );
    biDialog.dispose();
  }
  */
}

/*
 * Log
 *  1    Gandalf   1.0         7/26/99  Petr Hrebejk    
 * $
 */
