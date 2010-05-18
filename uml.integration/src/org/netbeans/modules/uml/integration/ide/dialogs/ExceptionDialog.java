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

package org.netbeans.modules.uml.integration.ide.dialogs;

import javax.swing.*;
import java.awt.event.*;

import javax.swing.text.html.*;
import java.awt.*;
import java.io.*;
import org.netbeans.modules.uml.integration.ide.UMLSupport;

/**
 * ExceptionDialog makes it easy to display an exception error to user.  To
 * display an error message to the user use the helper showExceptionError methods.
 * @see #showExceptionError(String msg, Exception e)
 * @see #showExceptionError(Exception e)
 * @author  Trey Spiva
 * @version 1.0
 */
public class ExceptionDialog extends JDialog
{
  protected final static String REPORT_MESSAGE = UMLSupport.getString("Errors.StdMessage");
  protected final static Dimension NO_DETAILS_SIZE = new Dimension(500, 160);
  protected final static Dimension WITH_DETAILS_SIZE = new Dimension(650, 250);
  protected final static String HIDE_DETAILS = UMLSupport.getString("Errors.HideDetails");
  protected final static String SHOW_DETAILS = UMLSupport.getString("Errors.ShowDetails");
  protected final static String DETAILS_CARD = UMLSupport.getString("Errors.WithDetails");
  protected final static String NO_DETAILS_CARD = UMLSupport.getString("Errors.NoDetails");

  /**
   * Constructs a ExceptionDialog object.
   * @param parnet The parent of the dialog.
   * @param modal Is the dialog modal or modeless.
   */
  public ExceptionDialog(java.awt.Frame parent,boolean modal)
  {
    super (parent, modal);
    initComponents ();

    setSize(NO_DETAILS_SIZE);
    mErrorMessage.setEditorKit(new HTMLEditorKit());

    //mDetailTxt.setPreferredSize(new Dimension(WITH_DETAILS_SIZE.width, WITH_DETAILS_SIZE.height - NO_DETAILS_SIZE.height));

    // Center the window.
    setLocationRelativeTo(null);

    setupForNoDetails();
    mButtonPane.getRootPane().setDefaultButton(mOkBtn);
  }

  /**
   * Sets the message of the dialog.
   */
  public void setMessage(String msg)
  {
    mErrorMessage.setText(msg + "<br>" + REPORT_MESSAGE);
  }

  /**
   * Retrieves the message of the dialog.
   */
  public void setDetailMessage(String msg)
  {
    mDetailTxt.setText(msg);
  }

  /**
   * Sets the detailed message of the dialog.  The detail message will include
   * the stack trace of the exception.
   * @param e The exception used to build the detailed message.
   */
  public void setDetailMessage(Exception e)
  {
    mDetailTxt.setText(getStackTrace(e));
    mDetailTxt.setCaretPosition(0);
  }


  /**
   * Retrieves the stack trace information from an exception.
   * @param e The exception used to retrieve the stack information.
   */
  protected String getStackTrace(Exception e)
  {
    StringWriter sWriter = new StringWriter();
    e.printStackTrace(new PrintWriter(sWriter));
    return sWriter.toString();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents()//GEN-BEGIN:initComponents
  {
    mCards = new javax.swing.JPanel();
    mErrorMessage = new javax.swing.JEditorPane();
    jScrollPane1 = new javax.swing.JScrollPane();
    mDetailTxt = new javax.swing.JTextArea();
    mButtonPane = new javax.swing.JPanel();
    mShowDetailsBtn = new javax.swing.JButton();
    mOkBtn = new javax.swing.JButton();
    setTitle(UMLSupport.getString("Errors.title"));
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        closeDialog(evt);
      }
    }
    );

    mCards.setLayout(new java.awt.CardLayout());
    mCards.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));

    mErrorMessage.setPreferredSize(new java.awt.Dimension(0, 0));
      mErrorMessage.setEditable(false);
      mErrorMessage.setOpaque(false);
      mCards.add(mErrorMessage, NO_DETAILS_CARD);



      mDetailTxt.setEditable(false);
        mDetailTxt.setTabSize(4);
        mDetailTxt.setOpaque(false);
        jScrollPane1.setViewportView(mDetailTxt);

        mCards.add(jScrollPane1, DETAILS_CARD);


    getContentPane().add(mCards, java.awt.BorderLayout.CENTER);


    mButtonPane.setLayout(new javax.swing.BoxLayout(mButtonPane, 0));
    mButtonPane.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 5, 5, 5)));

    mShowDetailsBtn.setActionCommand(SHOW_DETAILS);
      mShowDetailsBtn.setText(SHOW_DETAILS);
      mShowDetailsBtn.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
          mShowDetailsBtnActionPerformed(evt);
        }
      }
      );
      mButtonPane.add(mShowDetailsBtn);


    mButtonPane.add(Box.createHorizontalGlue());
      mOkBtn.setText(UMLSupport.getString("labels.ok"));
      mOkBtn.addActionListener(new java.awt.event.ActionListener()
      {
        public void actionPerformed(java.awt.event.ActionEvent evt)
        {
          mOkBtnActionPerformed(evt);
        }
      }
      );
      mButtonPane.add(mOkBtn);


    getContentPane().add(mButtonPane, java.awt.BorderLayout.SOUTH);

  }//GEN-END:initComponents

  private void mShowDetailsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mShowDetailsBtnActionPerformed

    String command = evt.getActionCommand();
    if(command.equals(SHOW_DETAILS) == true)
    {
      setupForWithDetails();
    }
    else
    {
      setupForNoDetails();
    }
    this.validate();
  }//GEN-LAST:event_mShowDetailsBtnActionPerformed

  /**
   * Display the error message and resize the diaglog to the standard size
   * of the dialog.
   */
  protected void setupForNoDetails()
  {
    setSize(NO_DETAILS_SIZE);
    mShowDetailsBtn.setText(SHOW_DETAILS);
    mShowDetailsBtn.setActionCommand(SHOW_DETAILS);

    CardLayout cl = (CardLayout)mCards.getLayout();
    cl.show(mCards, NO_DETAILS_CARD);
  }

  /**
   * When details are shown display the detailed message information.
   */
  protected void setupForWithDetails()
  {
    setSize(WITH_DETAILS_SIZE);
    mShowDetailsBtn.setText(HIDE_DETAILS);
    mShowDetailsBtn.setActionCommand(HIDE_DETAILS);

    CardLayout cl = (CardLayout)mCards.getLayout();
    cl.show(mCards, DETAILS_CARD);

  }

  private void mOkBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mOkBtnActionPerformed
    //System.exit(0);
    setVisible (false);
    dispose ();
  }//GEN-LAST:event_mOkBtnActionPerformed

  /** Closes the dialog */
  private void closeDialog(WindowEvent evt)//GEN-FIRST:event_closeDialog
  {
    setVisible (false);
    dispose ();
  }//GEN-LAST:event_closeDialog

  /**
   * Intialize and display the error dialog.  The dialog will be initailized with
   * a error message and a detailed message.  The detailed message will be filled
   * in from the specifed exception.
   * @param msg The error message to be displayed.
   * @param e The exception used to retrieve the detailed information.
   */
  public static void showExceptionError(String msg, Exception e)
  {
    ExceptionDialog dialog = new ExceptionDialog (new JFrame (), true);
    dialog.setMessage(msg);

    dialog.setDetailMessage(e);
    dialog.setVisible(true);
  }

  /**
   * Intialize and display the error dialog.  The dialog will be initailized with
   * a error message and a detailed message from the exception.
   * @param e The exception used to retrieve the detailed information.
   */
  public static void showExceptionError(Exception e)
  {
    ExceptionDialog dialog = new ExceptionDialog (new JFrame (), true);
    dialog.setMessage(e.getLocalizedMessage());

    dialog.setDetailMessage(e);
    dialog.setVisible(true);
  }

  /**
  * @param args the command line arguments
  */
  public static void main (String args[])
  {
    ExceptionDialog dialog = new ExceptionDialog (new javax.swing.JFrame (), true);
    dialog.setMessage("This is a <B>TEST</B> Error Message!");

    String detailMessage = "This is a very long detail message..........................VERY VERY VERY VERY VERY................................. Very long indeed\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    detailMessage += "I really hope that his scrollbars are working.............\n";
    dialog.setDetailMessage(detailMessage);
    dialog.setVisible(true);
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel mCards;
  private javax.swing.JEditorPane mErrorMessage;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTextArea mDetailTxt;
  private javax.swing.JPanel mButtonPane;
  private javax.swing.JButton mShowDetailsBtn;
  private javax.swing.JButton mOkBtn;
  // End of variables declaration//GEN-END:variables

}
