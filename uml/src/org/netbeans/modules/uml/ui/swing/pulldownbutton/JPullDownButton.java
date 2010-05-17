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
