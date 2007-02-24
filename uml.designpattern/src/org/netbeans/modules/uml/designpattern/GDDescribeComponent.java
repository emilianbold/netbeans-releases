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

package org.netbeans.modules.uml.designpattern;

import java.awt.BorderLayout;
import java.util.ResourceBundle;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.HelpCtx;

//import com.embarcadero.integration.GDProSupport;
import org.netbeans.modules.uml.ui.support.ProductHelper;
//import com.embarcadero.netbeans.options.DescribeProjectSettings;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogUtilities;

/**
 *  TopComponent for the Describe 6.0 properties editor.
 *
 * @author  Darshan
 * @version 1.0
 */
public class GDDescribeComponent extends TopComponent {
    static final long serialVersionUID = 12318371388356385L;
    private static GDDescribeComponent mTopComponent = null;
    private static boolean m_Initialized = false;
    private JTabbedPane m_TabPane = null;
    private static ResourceBundle mBundle = ResourceBundle.getBundle("org.netbeans.modules.uml.designpattern.Bundle");
    /**
     *  Creates a properties editor top component; the property editor control
     * is instantiated on addNotify() and destroyed on removeNotify().
     */
    public GDDescribeComponent() {
        setLayout(new BorderLayout());
        setName(mBundle.getString("Pane.Describe.Title"));
    }

    public void initializeTopComponent()
    {
       removeAll();
       setLayout(new BorderLayout());
       m_TabPane = new JTabbedPane();

       //whatever i do, i always get java.io.FileNotFoundException and the ide.log says,
       //component cannot be found from the components folder. I'm not sure if this is the right solution.
       //GDSystemTreeComponent comp1 = GDSystemTreeComponent.getInstance();
//       GDSystemTreeComponent comp1 = GDSystemTreeComponent.getDefault();
//       if (comp1 != null)
//       {
//          comp1.initializeTopComponent();
//          m_TabPane.addTab(DescribeModule.getString("Pane.ProjectView.Title"), comp1);
//       }

       //whatever i do, i always get java.io.FileNotFoundException and the ide.log says,
       //component cannot be found from the components folder. I'm not sure if this is the right solution.
       //DesignCenterComponent comp2 = DesignCenterComponent.getInstance();
       DesignCenterComponent comp2 = DesignCenterComponent.getDefault();
       if (comp2 != null)
       {
          comp2.initializeTopComponent();
         // m_TabPane.addTab(mBundle.getString("Pane.DesignCenter.Title"), comp2);
       }
       //add(m_TabPane, BorderLayout.CENTER);
       add(comp2, BorderLayout.CENTER);
       invalidate();
       doLayout();
    }

	/**
	 * Called when this component is activated. This happens when the parent
	 * window of this component gets focus (and this component is the preferred
	 * one in it), or when this component is selected in its window (and its
	 * window was already focussed). Override this method to perform some
	 * special action on component activation: typically, set performers for
	 * relevant actions. Remember to call the super method. The default
	 * implementation does nothing.
	 * <br>
	 * When the diagram becomes activated the toolbars for the diagram will
	 * become enabled.
	 */
    protected void componentActivated() {
        super.componentActivated();
//        ProjectController.describeTopComponent = false;
//        //fix for #5074494, 5095469
//        DescribeProjectSettings settings = DescribeProjectSettings.getInstance();
//        if (!settings.isConnected())
//            return ;
//        java.io.File file = settings.getAssociatedProject();
//        if (file == null)
//            return ;

//        if(m_HassBeanActivated == false)
//        {
//           //ensure that the project Tree is refreshed.
//           GDSystemTreeComponent comp = GDSystemTreeComponent.getInstance();
//           if (comp != null) {
//               comp.refresh();
//               m_HassBeanActivated = false;
//           }
//        }
    }

    protected void componentShowing() {
        super.componentShowing();
//        ProjectController.describeTopComponent = false;
//        GDProSupport.setForceShowViews(true);
//        ProjectController controller = new ProjectController();

        //we want to switch to this tab only if we are globally connected
//        if (!ProjectController.isConnected()) {
            //switch back to the filesystem top component
            TopComponent tc = WindowManager.getDefault().findTopComponent("filesystems");
            if (tc != null) {
                if(tc.isOpened())
                    tc.requestActive();
                else {
                    tc.open();
                    tc.requestActive();
                }
//            }
        }

//        if (ProjectController.isProjectConnected()) {
            //Fix for #5107364, 5107369. Samaresh
            //See related INF: http://inf.central/inf/integrationReport.jsp?id=38810
            //Basically the use case, is that user unmounts the primary file system,
            //while working on modeling. In this situation, when user comes back to
            //the modeling tab, we should detect if the primary file system where user
            //was working is valid or not? If not, we should prompt the user with mount
            //primary filesystem dialog, and this should be treated as a fresh start.
//            if(!validateProject()) {
//                controller.disconnectProjectFromDescribe();
//                DescribeProjectSettings settings = DescribeProjectSettings.getInstance();
//                //MUST set the current workspace to null
//                settings.setWorkspace(null);
//                GDSystemTreeComponent tree = GDSystemTreeComponent.getDefault();
//                //MUST clear this vector.
//                NewDialogUtilities.resetElements();
//                doConnect(controller);
//                return;
//            }
//
//            //we are already connected so open the views
//            controller.openUMLViews();
            initializeTopComponent();
//        } else {
//            doConnect(controller);
//        }

    }

 /*  private void doConnect(ProjectController controller) {
       boolean retVal = controller.connectProjectToDescribe();

       //since we connected to describe, just open the required views.
       if (retVal) {
           controller.openUMLViews();
           initializeTopComponent();
     }
       else {
        //switch back to the filesystem top component
           TopComponent tc = WindowManager.getDefault().findTopComponent(
                    "filesystems");
            if (tc != null)
            {
                if (tc.isOpened())
                    tc.requestActive();
                else
                {
                    tc.open();
                    tc.requestActive();
                }
            }
     }
   }

   private boolean validateProject() {
       DescribeProjectSettings settings = DescribeProjectSettings.getInstance();
       if(settings == null)
           return false;

       //check if the describe workspace file exist or not
       java.io.File file = new java.io.File(settings.getWorkspace());
       if(file == null || !file.exists())
           return false;

       //check if the describe project file exist or not
       file = settings.getAssociatedProject();
       if(file == null || !file.exists())
           return false;

       //see if the basedir exist or not
           file = new java.io.File(settings.getPrimaryFileSystemRoot());
       if(file == null || !file.exists())
           return false;

       if (!settings.isProjectMounted(file))
           return false;

       return true;
   }

   protected void componentHidden() {
       super.componentHidden();
       //hide all describe top components if the preference is set so
       if(!ProjectController.describeTopComponent) {
           String prefValue = ProductHelper.getPreferenceValue("",  //$NON-NLS-1$
           "HideDescribeViews"); //$NON-NLS-1$
           if (prefValue != null && prefValue.equals("PSK_YES")) {
               ProjectController controller = new ProjectController();
               controller.closeUMLViews();
           }
       }
   }
*/
	/**
	 * Called when this component is deactivated. This happens when the parent
	 * window of this component loses focus (and this component is the preferred
	 * one in the parent), or when this component loses preference in the parent
	 * window (and the parent window is focussed). Override this method to
	 * perform some special action on component deactivation: typically, unset
	 * performers for relevant actions. Remember to call the super method. The
	 * default implementation does nothing.
	 */
	protected void componentDeactivated()
   {
      super.componentDeactivated();
	}

   public static synchronized GDDescribeComponent getDefault()
   {
      if (mTopComponent == null)
      {
         mTopComponent = new GDDescribeComponent();
      }
      return mTopComponent;
   }

   public static synchronized GDDescribeComponent getInstance()
   {
      if(mTopComponent == null)
      {
          TopComponent tc = null;
          try {
              tc = WindowManager.getDefault().findTopComponent("UMLDescribe");
          } catch(Exception ex) {
              //ignore this
          }

         if (tc != null)
         {
            mTopComponent = (GDDescribeComponent)tc;
         }
         else
         {
            mTopComponent = new GDDescribeComponent();
         }
      }


      return mTopComponent;
   }

   public int getPersistenceType()
   {
      return TopComponent.PERSISTENCE_ALWAYS;
   }

   public String preferredID()
   {
      return "UMLDescribe";
   }

   public HelpCtx getHelpCtx() {
       java.awt.Component component = m_TabPane.getSelectedComponent();
       if(component.getName().equals(mBundle.getString("Pane.ProjectView.Title"))) {
           return new HelpCtx("DDEApplicationBasics9_htm_wp1317000");
       } else {
           return new HelpCtx("DEToolsDesignCenter2_htm_wp1737211");
       }
   }
}