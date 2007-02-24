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

   /**
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog() throws HeadlessException
   {
      super();
      setLocationRelativeTo(null);
   }

   /**
    * @param owner
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Frame owner) throws HeadlessException
   {
      super(owner);
      //setLocationRelativeTo(owner);
      center(owner);
   }

   /**
    * @param owner
    * @param modal
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Frame owner, boolean modal) throws HeadlessException
   {
      super(owner, modal);
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @param title
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Frame owner, String title) throws HeadlessException
   {
      super(owner, title);
      //setLocationRelativeTo(owner);
		center(owner);
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
      //setLocationRelativeTo(owner);
		center(owner);
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
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner) throws HeadlessException
   {
      super(owner);
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @param modal
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner, boolean modal) throws HeadlessException
   {
      super(owner, modal);
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @param title
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner, String title) throws HeadlessException
   {
      super(owner, title);
      //setLocationRelativeTo(owner);
		center(owner);
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
      //setLocationRelativeTo(owner);
		center(owner);
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
      //setLocationRelativeTo(owner);
		center(owner);
   }
   
   public void center(Frame frame)
   {
//       if (frame != null)
//       {
//           Point p = frame.getLocation();
//           if (p != null)
//           {
//               int centerX = (p.x + frame.getWidth()) / 2;
//               int centerY = (p.y + frame.getHeight()) / 2;
//               int dialogHalfWidth = getWidth() / 2;
//               int dialogHalfHeight = getHeight() / 2;
//               setLocation(centerX - dialogHalfWidth, centerY - dialogHalfHeight);
//           }
//       }
       center((Component)frame);
   }
   
   public void center(Dialog dia)
   {
//       if (dia != null)
//       {
//           Point p = dia.getLocation();
//           if (p != null)
//           {
//               int centerX = (p.x + dia.getWidth()) / 2;
//               int centerY = (p.y + dia.getHeight()) / 2;
//               int dialogHalfWidth = getWidth() / 2;
//               int dialogHalfHeight = getHeight() / 2;
//               setLocation(centerX - dialogHalfWidth, centerY - dialogHalfHeight);
//           }
//       }
       center((Component)dia);
   }
        
	public void center(Component comp)
        {
            if(comp == null)
            {
                comp = WindowManager.getDefault().getMainWindow();
            }
            
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
	  JRootPane rootPane = new JRootPane();
	  KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	  rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	  return rootPane;
	}

}
