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

package org.netbeans.modules.beans.beaninfo;

import java.util.ResourceBundle;
import javax.swing.SwingUtilities;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.actions.NodeAction;
import org.openide.src.ClassElement;

import org.netbeans.modules.beans.PatternAnalyser;

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


    static final long serialVersionUID =-4937492476805017833L;
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
                              bundle.getString( "CTL_TITLE_GenerateBeanInfo"),     // Title
                              true,                                                 // Modal
                              NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
                              NotifyDescriptor.OK_OPTION,                           // Default
                              DialogDescriptor.BOTTOM_ALIGN,                        // Align
                              new HelpCtx (GenerateBeanInfoAction.class.getName () + ".dialog"), // Help // NOI18N
                              null );

        biDialog = TopManager.getDefault().createDialog( dd );

        // Get pattern analyser & bean info and create BiAnalyser & BiNode

        final BiAnalyserReference biaReference = new BiAnalyserReference();

        final Task analyseTask = new Task( new Runnable() {
                                               public void run() {
                                                   PatternAnalyser pa = (PatternAnalyser)nodes[0].getCookie( PatternAnalyser.class );

                                                   ClassElement superClass = BiSuperClass.createForClassElement( pa.getClassElement() );
                                                   ClassElement theClass = pa.getClassElement();

                                                   pa = new PatternAnalyser( superClass );
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

        RequestProcessor.postRequest( analyseTask );

        biDialog.show ();

        if ( biaReference.getReference() != null && dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {

            Task task = new Task( new Runnable() {
                                      public void run () {
                                          analyseTask.waitFinished();
                                          biaReference.getReference().regenerateSource();
                                      }
                                  } );
            RequestProcessor.postRequest( task );
        }

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

/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Petr Hrebejk    i18n mk3
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/18/99  Petr Hrebejk    BeanInfo analyse moved 
 *       to separate thread
 *  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  2    Gandalf   1.1         7/26/99  Petr Hrebejk    BeanInfo fix & Code 
 *       generation fix
 *  1    Gandalf   1.0         7/26/99  Petr Hrebejk    
 * $
 */
