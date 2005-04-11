/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ProgressObject.java
 *
 * Created on May 30, 2001, 11:30 AM
 */

package org.netbeans.modules.j2ee.deployment.impl.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.openide.*;
import org.openide.util.*;

import javax.enterprise.deploy.spi.status.*;

/**
 *
 * @author Pete Eakle
 * @author Joe Warzecha 
 * @author George Finklang
 * @author Jeri Lockhart
 */
public class ProgressUI extends JPanel {

    private boolean changeFontSize;
    private boolean wasCancelled; 
    private String  dlgTitle;
    private String  desc;
    private Object  lockObj;
    private boolean modal;
    private static boolean autoClose = true;

    // Use frame instead of dialog if non-modal, to get window controls (minimize, maximize)
    private Dialog dialog;
    private JFrame frame;
    
    private static final int ERROR_LINES = 3;//maximum number of lines in error label
    private Component lastError = null;
    
    /** Creates new form ProgressObject */
    public ProgressUI() {
	this (true);
    }

    public ProgressUI(boolean modal) {
        initComponents ();
//        errorText.setVisible(false);//hide label
        errorMsg.setVisible(false);//hide scroll pane
        jScrollPane1.setVisible(false);
	changeFontSize = true;
	wasCancelled = false;        
	this.modal = modal;
	lockObj = new Object();
    }

    public ProgressUI(Component parent) {
	this (true);
    }

    public ProgressUI(Component parent, boolean modal) {
	this (modal);
    }

    /* 
     * Returns parent window of this component. It can be JFrame or Dialog
     * depending on modal state defined in constructor.
     * @return parent window of this UI
     */
    public Window getWindow() {
        if (frame != null) {
            return frame;
        }
        if (dialog != null) {
            return dialog;
        }
        return null;
    }
    

    public void addNotify () {
	super.addNotify ();
	if (changeFontSize) {
	    Font f = taskTitle.getFont ();
	    taskTitle.setFont (new Font (f.getName (), Font.BOLD,
					 f.getSize () + 2));
	    changeFontSize = false;
	    validate ();
	}
	msgText.setText (" ");						//NOI18N
	errorText.setText (" ");					//NOI18N
        autoCloseCheck.setSelected(autoClose);
        autoCloseCheck.setMnemonic(NbBundle.getMessage(ProgressUI.class, "LBL_Close_When_Finished_Mnemonic").charAt(0));
        autoCloseCheck.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ProgressUI.class, "ACSD_Close_When_Finished"));
    }

    /**
     *  Use this to set an explicit title on the progress window before calling startTask()
     *
     *  @param s A title for the window.  If you do not specify an explicit title, the default
     *           title "Progress Monitor" will be used.
     *
     */
    public void setTitle (String s) {
	dlgTitle = s;
        if (frame == null) return;
        
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                    frame.setTitle(dlgTitle);
            }
        });
    }

    /**
     * Use this method to set the accessible description for the
     * displayed Progress UI dialog or frame.
     *
     * @param s The accessible description for the dialog or frame.
     *          If no accessible description is set, the default
     *          accessible description of "Progress Monitor" will
     *          be used.
     */
    public void setAccessibleDescription (String s) {
	desc = s;
    } 
 
    
    /**
     * must call this next, after instantiation
     */
    public void startTask (final String msg, final int max) {
	if (wasCancelled) {
	    return;
	}
        if (dlgTitle == null) {
            dlgTitle = NbBundle.getMessage (ProgressUI.class, "LBL_Progress");
        }
        start(msg, max);
    }
    
    private void start(final String msg, final int max) {
        final String title = NbBundle.getMessage (ProgressUI.class, "LBL_Progress");
        if (this.modal) {
            if (dialog != null) {
                clearUI(msg, max);
                return;
            }

            ActionListener listener = new ActionListener () {
                public void actionPerformed (ActionEvent evt) {
                    Object o = evt.getSource ();
                    if (o == NotifyDescriptor.CANCEL_OPTION) {
                        wasCancelled = true;
                    }
                }
            };
            

	    if (desc == null) {
	        getAccessibleContext().setAccessibleDescription(title);
	    } else {
	        getAccessibleContext().setAccessibleDescription(desc);
	    }
            
            final DialogDescriptor dd = 
                            new DialogDescriptor (this, dlgTitle , modal, listener) {
                public int getOptionsAlign () {
                    return -1;
                }
            };
            Object [] options = new Object [] {};
            dd.setOptions (options);
            dd.setClosingOptions (options);
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    dialog = DialogDisplayer.getDefault().createDialog(dd);
                    myMonitor.setMaximum (max);
                    myMonitor.setValue (0);
                    taskTitle.setText (msg);
                    dialog.setVisible (true);
                }
            });
        }
        else {  // Non-modal -- use JFrame   
            if (frame != null) {
                clearUI(msg, max);
                return;
            }      
            final JPanel thisPanel = this;
            frame = new JFrame(dlgTitle );
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    myMonitor.setMaximum (max);
                    myMonitor.setValue (0);
                    taskTitle.setText (msg);
                    frame.getContentPane().add(thisPanel);
                    frame.getAccessibleContext().setAccessibleDescription(
                        desc != null ? desc 
                                     : NbBundle.getMessage(ProgressUI.class, "ACSD_Progress_Monitor"));
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
                    frame.pack();
                    frame.setBounds(Utilities.findCenterBounds(frame.getSize()));
                    frame.setVisible(true);
                }
            });
            
           
        }
    }
    
    private void clearUI(final String msg, final int max) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                myMonitor.setMaximum (max);
                myMonitor.setValue (0);
                errorText.setText (" ");				//NOI18N
                msgText.setText (" ");				//NOI18N
                taskTitle.setText (msg);
            }
        });
    }
 
    /**
     * @return number of lines in label errText or -1 if resizing is not needed.
     */
    private int numberOfTextLines() {
        FontMetrics fm = errorText.getFontMetrics(errorText.getFont());
        int textWidth = fm.bytesWidth(errorText.getText().getBytes(), 0, errorText.getText().getBytes().length);
        int labelWidth = errorText.getWidth();
        int textLines = 0;
        if (labelWidth == 0)
            return -1;
        
        textLines = textWidth / labelWidth;
        textLines += (textWidth % labelWidth) > 0 ? 1 : 0;
        
        return textLines;
    }
    
    private int deltaWindow(int textLines) {
        FontMetrics fm = errorText.getFontMetrics(errorText.getFont());
        int delta = textLines * fm.getHeight() - errorText.getHeight();
        if (delta < 0)
            delta++;
        
        return delta;
    }
    
    /**
     * Adjustment of height of dialog window dependent on errorText label.
     * Maximum window height is restricted to double of preferred height.
     * Error messages, that cannot fit are not completely displayed.
     * @return number of lines for errorText label
     */
    private void adjustWindowHeight(int delta) {
        Window window = null;
        if (frame != null) {
            window = frame;
        } else if (dialog != null) {
            window = dialog;
        }
        if (window != null) {
            int newHeight = window.getHeight() + delta;
            int maxHeight = (int) window.getPreferredSize().getHeight() * 2;
            window.setSize(window.getWidth(), (newHeight > maxHeight) ? maxHeight : newHeight);
            window.validate();
        }
    }

    public void addError (final String msg) {
	SwingUtilities.invokeLater (new Runnable () {
	    public void run () {
                
                if (modal && dialog == null || !modal && frame == null)
                    return;
                
                errorText.setText ("<html>" + msg.replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</html>"); // NOI18N
                int textLines = numberOfTextLines();
                if (textLines >=0 && textLines <= ERROR_LINES) {
                    int delta = deltaWindow(textLines);
                    if (lastError == errorMsg || lastError == null) {
                        errorMsg.setVisible(false);//hide scroll pane
                        jScrollPane1.setVisible(false);
                        errorText.setVisible(true);//show label
                        delta -= jScrollPane1.getHeight();//substract scroll pane height
                        if (lastError == errorMsg)
                            delta += errorText.getHeight();//add label height in case of switch
                        lastError = errorText;//remember label
                    }
                    adjustWindowHeight(delta);
                }
                else if (textLines > ERROR_LINES) {
                    errorMsg.setText(msg);
                    jScrollPane1.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
                    if (lastError == errorText || lastError == null) {
                        errorText.setVisible(false);//hide label
                        errorMsg.setVisible(true);//show scroll pane
                        jScrollPane1.setVisible(true);
                        int delta = -errorText.getHeight();//substract label height
                        int height = jScrollPane1.getHeight();//add scroll pane height in case of switch
                        if (height <= 0)
                            height = (int)jScrollPane1.getPreferredSize().getHeight();
                        delta += height;
                        adjustWindowHeight(delta);
                        lastError = errorMsg;//remember scroll pane
                    }
                }
	    }
	});
    }	

    public void addMessage (final String msg) {
	SwingUtilities.invokeLater (new Runnable () {
	    public void run () {
	  	msgText.setText (msg);
	    }
	});
    }	

    public void recordWork (final int value) {
	SwingUtilities.invokeLater (new Runnable () {
	    public void run () {
	  	myMonitor.setValue (value);
//                System.out.println("ProgressUI: recordWork() value = " + myMonitor.getValue()+ " , max = " + myMonitor.getMaximum());
                if ((myMonitor.getValue() >= myMonitor.getMaximum()) && autoClose) {
                    finished();
                }
            }
	});
    }	
  
    public boolean checkCancelled () {
	return wasCancelled;
    }
 
    public boolean isCompleted() {
        return true;
    }
    
    public void finished () {
        finished(true);
    }
    public void finished (boolean checkAutoclose) {
        if (checkAutoclose && !autoClose) {
            return;
        }
        if (this.modal) {
            if (dialog == null) {
                return;
            }
        }
        else {
            if (frame == null) {
                return;
            }
        } 
        disposeUI();
    }
    
    private void disposeUI() {            
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
		    synchronized (lockObj) {
                        if ((dialog != null) && (modal)) {
                            dialog.hide();
                            dialog.dispose ();
                            dialog = null;
                        } else if (frame != null) {
                            frame.hide();
                            frame.dispose ();
                            frame = null;
			}
                    }
                }
            });
    }

    //BEGIN_NOI18N
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        taskTitle = new javax.swing.JLabel();
        msgText = new javax.swing.JLabel();
        myMonitor = new javax.swing.JProgressBar();
        errorText = new javax.swing.JLabel();
        autoCloseCheck = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        errorMsg = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(448, 180));
        setPreferredSize(new java.awt.Dimension(448, 180));
        taskTitle.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(taskTitle, gridBagConstraints);

        msgText.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 24, 6, 24);
        add(msgText, gridBagConstraints);

        myMonitor.setMinimumSize(new java.awt.Dimension(400, 30));
        myMonitor.setPreferredSize(new java.awt.Dimension(400, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 12, 24);
        add(myMonitor, gridBagConstraints);

        errorText.setForeground(java.awt.Color.red);
        errorText.setText(" ");
        errorText.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 12, 24);
        add(errorText, gridBagConstraints);

        autoCloseCheck.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/deployment/impl/ui/Bundle").getString("LBL_Close_When_Finished"));
        autoCloseCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoCloseCheckActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 12, 24);
        add(autoCloseCheck, gridBagConstraints);

        jScrollPane1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setFocusCycleRoot(true);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 50));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(100, 50));
        errorMsg.setEditable(false);
        errorMsg.setForeground(java.awt.Color.red);
        errorMsg.setLineWrap(true);
        errorMsg.setWrapStyleWord(true);
        errorMsg.setBorder(null);
        errorMsg.setOpaque(false);
        jScrollPane1.setViewportView(errorMsg);
        errorMsg.getAccessibleContext().setAccessibleParent(jScrollPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 24, 12, 24);
        add(jScrollPane1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void autoCloseCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoCloseCheckActionPerformed
        // Add your handling code here:
        autoClose = autoCloseCheck.isSelected();
    }//GEN-LAST:event_autoCloseCheckActionPerformed
    //END_NOI18N


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoCloseCheck;
    private javax.swing.JTextArea errorMsg;
    private javax.swing.JLabel errorText;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel msgText;
    private javax.swing.JProgressBar myMonitor;
    private javax.swing.JLabel taskTitle;
    // End of variables declaration//GEN-END:variables

    
}
