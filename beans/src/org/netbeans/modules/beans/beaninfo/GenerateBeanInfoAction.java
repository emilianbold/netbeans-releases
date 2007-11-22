/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.beans.beaninfo;

import java.awt.*;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.actions.NodeAction;

import org.netbeans.modules.beans.PatternAnalyser;
import org.netbeans.modules.beans.BeanUtils;
import org.netbeans.modules.beans.GenerateBeanException;
import org.netbeans.jmi.javamodel.JavaClass;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;

import javax.jmi.reflect.JmiException;

/**
* Generate BI action.
*
* @author   Petr Hrebejk
*/
public class GenerateBeanInfoAction extends NodeAction implements java.awt.event.ActionListener {
    private Dialog biDialog;

    /** generated Serialized Version UID */
    //static final long serialVersionUID = 1391479985940417455L;

    // The dialog for BeanInfo generation

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
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable( Node[] activatedNodes ) {
        if (activatedNodes.length != 1 )
            return false;
        else {
            PatternAnalyser pa = (PatternAnalyser)activatedNodes[0].getCookie( PatternAnalyser.class );
            if (pa == null) {
                return false;
            }
            JavaClass theClass = pa.getClassElementHandle();
            BeanUtils.beginTrans(false);
            try  finally {
                BeanUtils.endTrans();
            }
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
                              new HelpCtx (BiPanel.BEANINFO_HELP), // Help // NOI18N                              
                              null );

        biDialog = DialogDisplayer.getDefault().createDialog( dd );
        
        initAccessibility();

        // Get pattern analyser & bean info and create BiAnalyser & BiNode

        final BiAnalyserReference biaReference = new BiAnalyserReference();
        
        final Task analyseTask = new Task( new Runnable() {
            public void run() {
                PatternAnalyser pa = (PatternAnalyser)nodes[0].getCookie( PatternAnalyser.class );
                
                try {
                    BeanUtils.beginTrans(true);
                    boolean rollback = true;
                    BiAnalyser bia;
                    try  finally {
                        BeanUtils.endTrans(rollback);
                    }
                    final Node biNode = new BiNode( bia );
                    
                    javax.swing.SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            biPanel.setContext( biNode );
                            biPanel.expandAll();
                        }
                    } );
                    
                    biaReference.setReference( bia );
                    
                } catch (GenerateBeanException e) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                } catch (JmiException e) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                }
            }
        } );

        RequestProcessor.getDefault().post( analyseTask );

        biDialog.setVisible(true);

        if ( biaReference.getReference() != null && dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {

            Task task = new Task( new Runnable() {
                public void run () {
                    analyseTask.waitFinished();
                    biaReference.getReference().regenerateSource();
                    BeanUtils.beginTrans(true);
                    boolean rollback = true;
                    try  finally {
                        BeanUtils.endTrans(rollback);
                    }
                }
            } );
            RequestProcessor.getDefault().post( task );
        }

    }

    protected boolean asynchronous() {
        return false;
    }

    private void initAccessibility() {
        biDialog.getAccessibleContext().setAccessibleDescription(getString("ACSD_BeanInfoEditorDialog"));
    }    
    
    static String getString(String key) {
        return NbBundle.getBundle("org.netbeans.modules.beans.beaninfo.Bundle").getString(key);
    }

    private static final class BiAnalyserReference {
        private BiAnalyser analyser = null;
        JavaClass syntheticClass = null;

        private void setReference( BiAnalyser analyser ) {
            this.analyser = analyser;
        }

        private BiAnalyser getReference() {
            return analyser;
        }

    }
}
