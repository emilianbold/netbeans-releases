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

/*
 * ProgressIndicator.java
 *
 * Created on April 25, 2001, 7:06 AM
 */

package org.netbeans.modules.uml.integration.ide.dialogs;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 * A simple dialog that allow the integration to update the user on the
 * progress of a time consuming process.
 * @author  Trey Spiva
 * @version 1.0
 */
public class ProgressIndicator extends javax.swing.JDialog
                               implements IProgressIndicator {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /** The default constructor of ProgressIndicator. */
    public ProgressIndicator(Frame owner) {
        super(owner);
        initComponents ();

        ImageIcon icon = new ImageIcon(ProgressIndicator.class.getResource
                        ("ProgressBackground.jpg"));
        this.getContentPane().add(new ImagePanel(icon.getImage()),
                        BorderLayout.CENTER);

        Border         oldBorder = mProgress.getBorder();
        Border         margin    = new EmptyBorder(0, 5, 5, 5);
        CompoundBorder newBorder = new CompoundBorder(margin, oldBorder);
        mProgress.setBorder(newBorder);

        setSize(400, 130);

        // Center the window.
        try {
            setLocationRelativeTo(null);
        }
        catch (NoSuchMethodError err) {
            Log.out("Used only with JDK 1.4");
        }
    }

    /**
     * Creates a new ProgressIndicator and set the title of the dialog.
     * @param title The title of the progress indicator.
     */
    public ProgressIndicator(String title) {
        this((Frame) null);

        setTitle(title);
    }

    public ProgressIndicator(Frame parent, String title) {
        this(parent);

        setTitle(title);
    }

    /**
     * Sets the maximum range of the progress bar.  The maximum value is used to
     * detimine the position of the progress.
     * @param value The maximum value.
     */
    public void setMaxRange(int value) {
        mProgress.setMaximum(value);
        update(getGraphics());
    }

    /**
     * Returns the maximum range of the progress bar.
     * @return The maximum value.
     */
    public int getMaxRange() {
        return mProgress.getMaximum();
    }

    /**
     * Sets the minimum range of the progress bar.  The minimum value is used to
     * detimine the position of the progress.
     * @param value The minimum value.
     */
    public void setMinRange(int value) {
        mProgress.setMinimum(value);
    }

    /**
     * Sets the current progress information.
     * @param msg A message to be displayed to the user.
     * @param rangeCompleted The value to indicate the progress.
     */
    public void setProgress(String msg, int rangeCompleted) {
        if(msg != null && msg.length() > 0)
            mMessage.setText(msg);
        mProgress.setValue(rangeCompleted);

        update(getGraphics());
    }

    public void incrementProgress(String msg) {
        if (msg != null && msg.length() > 0)
            mMessage.setText(msg);
        mProgress.setValue(mProgress.getValue() + 1);
        update(getGraphics());
    }

    /**
     * Displays the progress indicator.
     * @deprecated - use setVisible(boolean)
     */
    public void show() {
        super.show();

        update(getGraphics());
    }

    public void setVisible(boolean val) 
    {
        super.setVisible(val);
        update(getGraphics());
    }
    
    
    /**
     *  Closes the progress dialog.
     */
    public void done() {
        closeDialog(null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        mProgressControls = new javax.swing.JPanel();
        mMessage = new javax.swing.JLabel();
        mProgress = new javax.swing.JProgressBar();
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        setName("GDProgressIndicator");
        setTitle("Progress Inddicator");
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        mProgressControls.setLayout(new java.awt.BorderLayout());
        mProgressControls.setBorder(new javax.swing.border.BevelBorder(0));

        mMessage.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        mMessage.setText("Processing...");
        mProgressControls.add(mMessage, java.awt.BorderLayout.NORTH);


        mProgressControls.add(mProgress, java.awt.BorderLayout.SOUTH);


        getContentPane().add(mProgressControls, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible (false);
        dispose ();
    }//GEN-LAST:event_closeDialog


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mProgressControls;
    private javax.swing.JLabel mMessage;
    private javax.swing.JProgressBar mProgress;
    // End of variables declaration//GEN-END:variables

    class ImagePanel extends JComponent {
        Image image;

        public ImagePanel(Image image) {
            this.image = image;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g); //paint background

            //Now draw the image scaled.
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }

        public Dimension getPreferredSize() {
            return new Dimension(image.getWidth(this), image.getHeight(this));
        }
    }
}
