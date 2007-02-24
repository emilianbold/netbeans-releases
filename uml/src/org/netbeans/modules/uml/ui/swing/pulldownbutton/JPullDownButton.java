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



package org.netbeans.modules.uml.ui.swing.pulldownbutton;

import java.awt.Graphics;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.ComponentUI;

import org.netbeans.modules.uml.ui.swing.plaf.PullDownButtonUI;
import org.netbeans.modules.uml.ui.swing.plaf.basic.BasicPullDownButtonUI;

/**
 *
 * @author Trey Spiva
 */
public class JPullDownButton extends AbstractButton
{
   private static final String uiClassID = "PullDownButtonUI";

   private IPulldownButtonInvoker m_Invoker = null;
   private boolean m_ArrowButtonPressed = false;
   
   public JPullDownButton()
   {
      this(null);
   }
   
   public JPullDownButton(IPulldownButtonInvoker invoker)
   {
      super();
      setBorder(new EtchedBorder());
      setModel(new DefaultButtonModel());
      setUI((ComponentUI)new BasicPullDownButtonUI());
      
      setPulldownInvoker(invoker);
   }
   
   public void setPulldownInvoker(IPulldownButtonInvoker invoker)
   {
      m_Invoker = invoker;
      
      if(m_Invoker == null)
      {
         setEnabled(false);
      }
      else
      {
         setEnabled(true);
      }
   }
   
   public IPulldownButtonInvoker getPulldownInvoker()
   {
      return m_Invoker;
   }
   
   public void setEnabled(boolean b)
   {
      if(getPulldownInvoker() != null)
      {
         super.setEnabled(b);
      }
      else
      {
         super.setEnabled(false);
      }
   }

   public void setAction(Action a)
   {
      super.setAction(a);
      
      if (a instanceof IPulldownAction)
      {
         IPulldownAction pulldownAction = (IPulldownAction)a;
         setPulldownInvoker(pulldownAction.getInvoker());
      }
   }
   
   public void setBorder(javax.swing.border.Border b)
   {
      super.setBorder(b);
   }
   
   public void setArrowButtonPressed(boolean value)
   {
      m_ArrowButtonPressed = value;
   }
   
   public boolean isArrowButtonPressed()
   {
      return m_ArrowButtonPressed;
   }
   
   //**************************************************
   // UI Methods
   //**************************************************

   /**
     * Resets the UI property to a value from the current look and feel.
     * <code>JComponent</code> subclasses must override this method
     * like this:
     * <pre>
     *   public void updateUI() {
     *      setUI((SliderUI)UIManager.getUI(this);
     *   }
     *  </pre>
     *
     * @see #setUI
     * @see UIManager#getLookAndFeel
     * @see UIManager#getUI
     */
   public void updateUI()
   {
      ComponentUI ui = UIManager.getUI(this);
      
      if(ui == null)
      {
         ui = new BasicPullDownButtonUI();
      }
      setUI(ui);
   }

   /**
    * Sets the look and feel delegate for this component.
    * <code>JComponent</code> subclasses generally override this method
    * to narrow the argument type. For example, in <code>JSlider</code>:
    * <pre>
    * public void setUI(SliderUI newUI) {
    *     super.setUI(newUI);
    * }
    *  </pre>
    * <p>
    * Additionally <code>JComponent</code> subclasses must provide a
    * <code>getUI</code> method that returns the correct type.  For example:
    * <pre>
    * public SliderUI getUI() {
    *     return (SliderUI)ui;
    * }
    * </pre>
    *
    * @param newUI the new UI delegate
    * @see #updateUI
    * @see UIManager#getLookAndFeel
    * @see UIManager#getUI
    * @beaninfo
    *        bound: true
    *       hidden: true
    *    attribute: visualUpdate true
    *  description: The component's look and feel delegate.
    */
   protected void setUI(ComponentUI newUI)
   {
      if(newUI instanceof PullDownButtonUI)
      {
         super.setUI(newUI);
      }
   }

   /**
    * Returns the <code>UIDefaults</code> key used to
    * look up the name of the <code>swing.plaf.ComponentUI</code>
    * class that defines the look and feel
    * for this component.  Most applications will never need to
    * call this method.  Subclasses of <code>JComponent</code> that support
    * pluggable look and feel should override this method to
    * return a <code>UIDefaults</code> key that maps to the
    * <code>ComponentUI</code> subclass that defines their look and feel.
    *
    * @return the <code>UIDefaults</code> key for a
    *      <code>ComponentUI</code> subclass
    * @see UIDefaults#getUI
    * @beaninfo
    *      expert: true
    * description: UIClassID
    */
   public String getUIClassID()
   {
      return uiClassID;
   }
   
   public void paint(Graphics g)
   {
      super.paint(g);
   }
}
