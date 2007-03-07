/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.project.ui.nodes.actions;
import javax.swing.SwingUtilities;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.ui.controls.filter.IFilterDialog;
import org.netbeans.modules.uml.ui.support.ErrorDialogIconKind;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.SwingPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.swing.projecttree.JFilterDialog;
import org.netbeans.modules.uml.project.UMLProjectModule;
import org.netbeans.modules.uml.project.ui.nodes.ModelRootNodeCookie;
import javax.swing.tree.DefaultTreeModel;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
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
        String name = "FilterCollapseNodesWarning"; // NOI18N
        
        // the preference may not exist for whater reason, but show
        //  the filter dialog without the warning notice
        if (getPreferenceValue (key, path, name).equals (""))
            return true;
        
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
                "PSK_ALWAYS", // NOI18N
                "",
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
    
    private String getPreferenceValue (
            String prefKey, String prefPath, String prefName)
    {
        String sVal = "";
        IPreferenceManager2 pManager = ProductHelper.getPreferenceManager ();
        
        if (pManager != null)
            sVal = pManager.getPreferenceValue (prefKey, prefPath, prefName);
        
        return sVal;
    }
}