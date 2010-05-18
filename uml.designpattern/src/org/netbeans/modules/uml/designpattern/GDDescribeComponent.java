/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.designpattern;

import java.awt.BorderLayout;
import java.util.ResourceBundle;

import javax.swing.JTabbedPane;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.HelpCtx;

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
