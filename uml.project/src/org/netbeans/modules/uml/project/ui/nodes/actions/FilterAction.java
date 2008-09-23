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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.uml.project.ui.nodes.actions;
import javax.swing.SwingUtilities;
import org.netbeans.modules.uml.ui.controls.filter.IFilterDialog;
import org.netbeans.modules.uml.ui.support.ErrorDialogIconKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.SwingPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.swing.projecttree.JFilterDialog;
import org.netbeans.modules.uml.project.UMLProjectModule;
import org.netbeans.modules.uml.project.ui.nodes.ModelRootNodeCookie;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;



/**
 *
 * @author  Craig Conover, craig.conover@sun.com
 */
public class FilterAction extends CookieAction
{
    public FilterAction ()
    {
    }
    
    
    public String getName ()
    {
        return (String)NbBundle.getBundle (FilterAction.class)
                .getString ("FilterAction_Name"); // NOI18N
    }
    
    public int mode ()
    {
        return CookieAction.MODE_ANY;
    }
    
    public boolean enable (Node[] nodes)
    {
        if (nodes != null && nodes.length == 1)
            return true;
        
        return false;
    }
    
    public Class[] cookieClasses ()
    {
        return new Class[]{null};
    }
    
    public HelpCtx getHelpCtx ()
    {
        return null;
    }
    
    
    public void performAction (final Node[] nodes)
    {
        SwingUtilities.invokeLater ( new Runnable ()
        { 
            public void run ()
            {
                // cvc - 6286956
                if (!continueToFilter ())
                    return;
                
                ModelRootNodeCookie cookie = (ModelRootNodeCookie)nodes[0]
                        .getCookie (ModelRootNodeCookie.class);
                
                if (cookie != null)
                {
                    // cvc - CR 6271328
                    // only listen while Filter dialog is "alive" otherwise, all
                    // projects will listen to each others' filter dialogs
                    cookie.filterListenerRegistered (true);
                    
                    // attempt to retreive the cached filter dialog
                    // IFilterDialog dialog = cookie.getFilterDialog();
                    DefaultTreeModel model = cookie.getTreeModelFilter ();
                    
                    IFilterDialog dialog = UIFactory.createProjectTreeFilterDialog (
                            WindowManager.getDefault ().getMainWindow (),
                            UMLProjectModule.getProjectTreeModel ());
                    
                    if (dialog != null)
                    {
                        if (model != null && dialog instanceof JFilterDialog)
                        {
                            // reuse the same filter settings as last time
                            JFilterDialog filterDialog = (JFilterDialog)dialog;
                            filterDialog.show (model);
                        }
                        
                        else
                            // filtered for the very first time
                            dialog.show ();
                        
                        // cvc - CR6271053
                        // the filter's OK action will be listened to directly by
                        // the node(s) that need to refresh themselves.
                        // final ModelRootNodeCookie cookie =
                        //	(ModelRootNodeCookie)nodes[0].getCookie(ModelRootNodeCookie.class);
                        
                        // if (cookie != null)
                        // {
                        //	cookie.recalculateChildren();
                        // }
                    } // if dialog !null
                    
                    // cvc - CR 6271328
                    // only listen while Filter dialog is "alive" otherwise, all
                    // projects will listen to each others' filter dialogs
                    cookie.filterListenerRegistered (false);
                    
                } // if cookie !null
            }
        }); 
    }
    
    
    // cvc - 6286956
    // the filter should be fixed in the Coke release to work properly so
    // that this warning dialog is not necessary
    private boolean continueToFilter ()
    {
        String key = "Default"; // NOI18N
        String path = ""; // NOI18N
        String name = "UML_ShowMe_Dont_Show_Filter_Warning_Dialog"; // NOI18N
        
        //Kris Richards - This is a "show me dialog" preference. Need to get the 
        // preference value for the propertysupport module.
        String showMe = NbPreferences.forModule(DummyCorePreference.class).get(name, "PSK_ASK");
        
        if (showMe.equals("PSK_NEVER"))
            return true ;

        int result = 0;
        boolean userClickedYes = true;
        IPreferenceQuestionDialog dialog = new SwingPreferenceQuestionDialog ();
       
        String title = NbBundle.getMessage (FilterAction.class,
                "LBL_FilterCollapseNodesWarning_Title"); // NOI18N
        
        String msg = NbBundle.getMessage (FilterAction.class,
                "MSG_FilterCollapseNodesWarning_Message"); // NOI18N
        
        result =
                (int)dialog.displayFromStrings (
                key,
                path,
                name,
                "PSK_NEVER", // NOI18N
                "PSK_NEVER",
                "PSK_ASK", // NOI18N
                msg,
                SimpleQuestionDialogResultKind.SQDRK_RESULT_YES,
                title,
                SimpleQuestionDialogKind.SQDK_YESNO,
                ErrorDialogIconKind.EDIK_ICONQUESTION,
                null);
        
        if (result == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
            userClickedYes = true;
        
        if (result == SimpleQuestionDialogResultKind.SQDRK_RESULT_NO)
            userClickedYes = false;
        
        if (result == SimpleQuestionDialogResultKind.SQDRK_RESULT_CANCEL)
            //user pressed the escape key or cancelled the save dialog
            //in which case do not do nothing, getback to the old state.
            userClickedYes = false;
        
        return userClickedYes;
    }

    @Override
    protected boolean asynchronous()
    {
        return false;
    }


    
}
