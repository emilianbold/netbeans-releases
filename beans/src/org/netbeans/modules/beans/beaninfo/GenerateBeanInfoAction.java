/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans.beaninfo;

import java.util.ResourceBundle;
import javax.swing.SwingUtilities;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.actions.NodeAction;
import org.openide.src.ClassElement;

import org.netbeans.modules.beans.PatternAnalyser;
import org.openide.DialogDisplayer;

/**
* Search doc action.
*
* @author   Petr Hrebejk
*/
public class GenerateBeanInfoAction extends NodeAction implements java.awt.event.ActionListener {

    /** generated Serialized Version UID */
    //static final long serialVersionUID = 1391479985940417455L;

    // The dialog for BeanInfo generation
    private java.awt.Dialog biDialog = null;


    static final long serialVersionUID =-4937492476805017833L;
    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return getString ("CTL_GENBI_MenuItem");
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return null;
        //return "/org/netbeans/modules/javadoc/resources/searchDoc.gif"; // NOI18N
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
        else {
            PatternAnalyser pa = (PatternAnalyser)activatedNodes[0].getCookie( PatternAnalyser.class );
            if (pa == null) {
                return false;
            }
            ClassElement theClass = pa.getClassElement();
            return !theClass.isInner();
        }
    }


    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction ( final Node[] nodes ) {

        if ( nodes.length < 1 )
            return;

        // Open the diaog for bean info generation

        final BiPanel biPanel;

        DialogDescriptor dd = new DialogDescriptor( (biPanel = new BiPanel()),
                              getString( "CTL_TITLE_GenerateBeanInfo"),     // Title
                              true,                                                 // Modal
                              NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
                              NotifyDescriptor.OK_OPTION,                           // Default
                              DialogDescriptor.BOTTOM_ALIGN,                        // Align
                              //new HelpCtx (GenerateBeanInfoAction.class.getName () + ".dialog"), // Help // NOI18N
                              new HelpCtx (BiPanel.BEANINFO_HELP), // Help // NOI18N                              
                              null );

        biDialog = DialogDisplayer.getDefault().createDialog( dd );
        
        initAccessibility();

        // Get pattern analyser & bean info and create BiAnalyser & BiNode

        final BiAnalyserReference biaReference = new BiAnalyserReference();

        final Task analyseTask = new Task( new Runnable() {
                                               public void run() {
                                                   PatternAnalyser pa = (PatternAnalyser)nodes[0].getCookie( PatternAnalyser.class );

                                                   ClassElement superClass = BiSuperClass.createForClassElement( pa.getClassElement() );
                                                   ClassElement theClass = pa.getClassElement();

                                                   pa = new PatternAnalyser( superClass, pa.getClassElement() );
                                                   pa.analyzeAll();

                                                   BiAnalyser bia = new BiAnalyser( pa, theClass );
                                                   final Node biNode = new BiNode( bia );

                                                   javax.swing.SwingUtilities.invokeLater( new Runnable() {
                                                                                               public void run() {
                                                                                                   biPanel.setContext( biNode );
                                                                                                   biPanel.expandAll();
                                                                                               }
                                                                                           } );

                                                   biaReference.setReference( bia );
                                               }
                                           } );

        RequestProcessor.getDefault().post( analyseTask );

        biDialog.show ();

        if ( biaReference.getReference() != null && dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {

            Task task = new Task( new Runnable() {
                                      public void run () {
                                          analyseTask.waitFinished();
                                          biaReference.getReference().regenerateSource();
                                      }
                                  } );
            RequestProcessor.getDefault().post( task );
        }

    }
    
    private void initAccessibility() {
        biDialog.getAccessibleContext().setAccessibleDescription(getString("ACSD_BeanInfoEditorDialog"));
    }    
    
    static String getString(String key) {
        return NbBundle.getBundle("org.netbeans.modules.beans.beaninfo.Bundle").getString(key);
    }

    private static class BiAnalyserReference {
        private BiAnalyser analyser = null;

        private void setReference( BiAnalyser analyser ) {
            this.analyser = analyser;
        }

        private BiAnalyser getReference() {
            return analyser;
        }

    }
}
