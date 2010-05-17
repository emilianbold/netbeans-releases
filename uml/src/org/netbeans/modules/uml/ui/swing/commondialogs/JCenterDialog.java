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



package org.netbeans.modules.uml.ui.swing.commondialogs;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.openide.windows.WindowManager;

/**
 *
 * @author Trey Spiva
 */
public class JCenterDialog extends JDialog
{

    private Component lastComponent;

   /**
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog() throws HeadlessException
   {
      super();
      setLocationRelativeTo(null);
      registerListener();
   }

   /**
    * @param owner
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Frame owner) throws HeadlessException
   {
      super(owner);
      center(owner);
      registerListener();
   }

   /**
    * @param owner
    * @param modal
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Frame owner, boolean modal) throws HeadlessException
   {
      super(owner, modal);
		center(owner);
        registerListener();
   }

   /**
    * @param owner
    * @param title
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Frame owner, String title) throws HeadlessException
   {
      super(owner, title);
		center(owner);
        registerListener();
   }

   /**
    * @param owner
    * @param title
    * @param modal
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Frame owner, String title, boolean modal) throws HeadlessException
   {
      super(owner, title, modal);
		center(owner);
        registerListener();
   }

   /**
    * @param owner
    * @param title
    * @param modal
    * @param gc
    */
   public JCenterDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc)
   {
      super(owner, title, modal, gc);
		center(owner);
        registerListener();
   }

   /**
    * @param owner
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner) throws HeadlessException
   {
      super(owner);
		center(owner);
        registerListener();
   }

   /**
    * @param owner
    * @param modal
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner, boolean modal) throws HeadlessException
   {
      super(owner, modal);
		center(owner);
        registerListener();
   }

   /**
    * @param owner
    * @param title
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner, String title) throws HeadlessException
   {
      this(owner, title,false);
   }

   /**
    * @param owner
    * @param title
    * @param modal
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner, String title, boolean modal) throws HeadlessException
   {
      super(owner, title, modal);
		center(owner);
        registerListener();
   }

   /**
    * @param owner
    * @param title
    * @param modal
    * @param gc
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) throws HeadlessException
   {
      super(owner, title, modal, gc);
		center(owner);
        registerListener();
   }
   
   public void center(Frame frame)
   {
       center((Component)frame);
   }
   
   public void center(Dialog dia)
   {
       center((Component)dia);
   }

   private void registerListener()
   {
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowActivated(WindowEvent e) {
                center(lastComponent);
            }
        }
        );
        addWindowStateListener(new WindowAdapter()
        {
            @Override
            public void windowStateChanged(WindowEvent e) {
                super.windowStateChanged(e);
            }
        }
        );
   }
        
	public void center(Component comp)
        {
            if(comp == null)
            {
                comp = WindowManager.getDefault().getMainWindow();
            }
            lastComponent=comp;
            if (comp != null)
            {
                Point p = comp.getLocation();
                if (p != null)
                {
                    // This does not seem to work well on two monitor systems
                    // The reason is that the location is in the neg numbers.
                    int centerX = p.x + (comp.getWidth() / 2);
                    int centerY = p.y + (comp.getHeight() / 2);
                    int dialogHalfWidth = getWidth() / 2;
                    int dialogHalfHeight = getHeight() / 2;
                    setLocation(centerX - dialogHalfWidth, centerY - dialogHalfHeight);
                }
            }
        }
   
    @Override
	protected JRootPane createRootPane() {
	  ActionListener actionListener = new ActionListener() {
		 public void actionPerformed(ActionEvent actionEvent) {
          try
          {
             setVisible(false);
          }
          catch(Exception e)
          {
             Log.stackTrace(e);
          }
		 }
	  };
	  JRootPane rtPane = new JRootPane();
	  KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	  rtPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	  return rtPane;
	}

}
