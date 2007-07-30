/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.vmd.game.preview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.netbeans.modules.vmd.game.model.SequenceContainer;
import org.netbeans.modules.vmd.game.model.SequenceListener;
import org.netbeans.modules.vmd.game.model.StaticTile;
import org.netbeans.modules.vmd.game.view.ColorConstants;
import org.netbeans.modules.vmd.game.view.ImagePreviewComponent;

/**
 *
 * @author karel herink
 */
public class SequencePreviewPanel extends javax.swing.JPanel  implements 
		SequenceListener, ComponentListener, 
		PropertyChangeListener, ActionListener {
    
	public static final boolean DEBUG = false;

	public static final Color COLOR_BORDER_PLAIN = ColorConstants.COLOR_OUTLINE_PLAIN;
	public static final Color COLOR_BORDER_SELECTED = ColorConstants.COLOR_OUTLINE_SELECTED;

	public static final Color COLOR_TEXT_PLAIN = ColorConstants.COLOR_OUTLINE_SELECTED;
	public static final Color COLOR_TEXT_SELECTED = ColorConstants.COLOR_OUTLINE_SELECTED;
	

	
	private boolean isPlaying = false;
	private boolean isPlayingForward = true;
	
	private int sequenceIndex;
	private DefaultComboBoxModel actionsModel = new DefaultComboBoxModel();

	private SequenceContainer sequenceContainer;
	private Sequence sequence;
	private Timer timer;

	private ImagePreviewComponent imagePreview;

    public SequencePreviewPanel(Sequence sequence) {
        this.initComponents();
		this.sequenceContainer = sequenceContainer;
		this.imagePreview = new ImagePreviewComponent(true, true, true);
		this.panelSequenceAnimatedPreview.add(this.imagePreview, BorderLayout.CENTER);
		this.addComponentListener(this);
		this.setSequence(sequence);
		this.updateActions();
		this.buttonPlayForward.addActionListener(this);
		this.buttonPlayBackward.addActionListener(this);
		this.buttonPause.addActionListener(this);
		
        buttonPlayBackward.setBackground(buttonPlayBackward.getParent().getBackground());
        buttonPause.setBackground(buttonPause.getParent().getBackground());
        buttonPlayForward.setBackground(buttonPlayForward.getParent().getBackground());
		
		this.buttonPause.setEnabled(this.isPlaying);
    }
    
	public void setSequenceContainer(SequenceContainer sequenceContainer) {
		this.sequenceContainer = sequenceContainer;
		this.updateActions();
	}
	
	private Dimension getFrameSize() {
		if (this.sequence == null)
			return new Dimension(10, 10);
		return this.sequence.getFrameSize();
	}
	
	public void setSelected(boolean selected) {
		if (selected) {
			this.labelName.setForeground(ColorConstants.COLOR_TEXT_SELECTED);
		}
		else {
			this.labelName.setForeground(ColorConstants.COLOR_TEXT_PLAIN);
		}
	}
	
	
	//ActionListener----------------------------------------------------------------------
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.buttonPlayForward) {
			this.isPlaying = true;
			this.isPlayingForward = true;
		}
		else if (e.getSource() == this.buttonPlayBackward) {
			this.isPlaying = true;
			this.isPlayingForward = false;
		}
		else if (e.getSource() == this.buttonPause) {
			this.isPlaying = false;
		}
		this.buttonPause.setEnabled(this.isPlaying);
		this.buttonPlayForward.setEnabled(!this.isPlayingForward || !this.isPlaying);
		this.buttonPlayBackward.setEnabled(this.isPlayingForward || !this.isPlaying);
	}

	
		
	/**
	 * Method used to show that this preview is for a "special" sequence (e.g. the default sequence in a container).
	 * Currently only makes the name label use bold text.
	 */
	public void setImportant(boolean isImportant) {
		if (isImportant) {
			this.labelName.setFont(this.labelName.getFont().deriveFont(Font.BOLD));
		}
		else {
			this.labelName.setFont(this.labelName.getFont().deriveFont(Font.PLAIN));
		}
		this.updateActions();
	}
	
	private void updateActions() {
		this.actionsModel.removeAllElements();
		List<Action> actions;
		if (this.sequenceContainer != null) {
			actions = this.sequenceContainer.getActionsForSequence(this.sequence);
		}
		else {
			actions = this.sequence.getActions();
		}
		for (Action action : actions) {
			this.actionsModel.addElement(action);
		}
	}
	
	public void setSequence(Sequence sequence) {
		if (this.sequence == null) {
		}
		else {
			this.sequence.removeSequenceListener(this);
			this.sequence.removePropertyChangeListener(this);
			this.timer.cancel();
			int max = sequence.getFrameCount() -1;
		}
        this.sequence = sequence;
		this.labelName.setText(this.sequence.getName());
		this.labelName.setToolTipText(this.sequence.getName());
		
		this.panelSpinner.add(new SequenceTimeSpinner(this.sequence), BorderLayout.CENTER);
		this.panelSpinner.revalidate();
		
		this.timer = new Timer();
		StaticTile frame = this.sequence.getFrame(0);
		this.imagePreview.setPreviewable(frame);
		this.sequence.addSequenceListener(this);
		this.timer.schedule(new AnimationTimerTask(), 0, this.sequence.getFrameMs());
		this.sequence.addPropertyChangeListener(this);
	}
	
	public void requestPreviewFrame(int frameIndex) {
		if (!this.isPlaying && this.sequence.getFrame(frameIndex) != null)
			this.imagePreview.setPreviewable(this.sequence.getFrame(frameIndex));
	}
	
	private void sequenceChanged() {
		this.setSequence(this.sequence);
	}
	
	private void incrementSequenceIndex() {
		int index = this.sequenceIndex + 1;
		if (index >= this.sequence.getFrameCount()) {
			this.sequenceIndex = 0;
		}
		else {
			this.sequenceIndex = index;
		}
	}
	private void decrementSequenceIndex() {
		int index = this.sequenceIndex - 1;
		if (index < 0) {
			this.sequenceIndex = this.sequence.getFrameCount() -1;
		}
		else {
			this.sequenceIndex = index;
		}
	}
	
	private void currentFrameChanged() {
		//this.slider.setValue(this.sequenceIndex);
	}
	
	private class AnimationTimerTask extends TimerTask {
		public void run() {
			if (!SequencePreviewPanel.this.isPlaying || !SequencePreviewPanel.this.isShowing())
				return;
			SequencePreviewPanel.this.setCurrentFrameIndex(SequencePreviewPanel.this.sequenceIndex);
			SequencePreviewPanel.this.currentFrameChanged();
			if (SequencePreviewPanel.this.isPlayingForward) {
				SequencePreviewPanel.this.incrementSequenceIndex();
			}
			else {
				SequencePreviewPanel.this.decrementSequenceIndex();
			}
		}
	}
	
	private void setCurrentFrameIndex(int frameIndex) {
			StaticTile frame = null;
			do {
				try {
					frame = SequencePreviewPanel.this.sequence.getFrame(frameIndex);
				}
				catch (Exception e) {
					//e.printStackTrace();
					frameIndex--;
				}
			} while (frame == null);
			this.sequenceIndex = frameIndex;
			this.imagePreview.setPreviewable(frame);
	}
	
	//PropertyChangeListener-------------------------------------------------------------
	//Listen for changes in frame delay
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == this.sequence) {
			if (evt.getPropertyName().equals(Sequence.PROPERTY_FRAME_MS)) {
				this.timer.cancel();
				this.timer = new Timer();
				this.timer.schedule(new AnimationTimerTask(), 0, this.sequence.getFrameMs());
			}
			if (evt.getPropertyName().equals(Sequence.PROPERTY_NAME)) {
				this.labelName.setText(this.sequence.getName());
			}
		}
	}

	//ComponentListener-------------------------------------------------------------------
	public void componentShown(ComponentEvent e) {
	}
	public void componentHidden(ComponentEvent e) {
	}
	public void componentResized(ComponentEvent e) {
		if (DEBUG) System.out.println("SequenceAnimatedPreview Resized..."); // NOI18N
		//TODO : here i will recalculate cached images
	}
	public void componentMoved(ComponentEvent e) {
	}

	
	//SequenceListener
	public void frameAdded(Sequence sequence, int index) {
		this.sequenceChanged();
	}
	public void frameRemoved(Sequence sequence, int index) {
		this.sequenceChanged();
	}
	public void frameModified(Sequence sequence, int index) {
		//TODO : here i would recache the modified frame image i think
	}
	
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        labelName = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        buttonPlayBackward = new javax.swing.JButton();
        buttonPause = new javax.swing.JButton();
        buttonPlayForward = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        panelSequenceAnimatedPreview = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        panelSpinner = new javax.swing.JPanel();

        setBorder(new javax.swing.border.LineBorder(COLOR_BORDER_PLAIN, 2, true));

        jPanel5.setMinimumSize(new java.awt.Dimension(300, 126));
        jPanel5.setLayout(new java.awt.GridLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        labelName.setFont(new java.awt.Font("Dialog", 1, 14));
        labelName.setForeground(ColorConstants.COLOR_TEXT_PLAIN);
        labelName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelName.setText("<NONE>");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(labelName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(labelName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        jPanel2.add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel4.setMaximumSize(new java.awt.Dimension(174, 34));
        jPanel4.setLayout(new java.awt.GridLayout(1, 0));

        buttonPlayBackward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/sequece/resources/playrev.png"))); // NOI18N
        jPanel4.add(buttonPlayBackward);

        buttonPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/sequece/resources/pause.gif"))); // NOI18N
        jPanel4.add(buttonPause);

        buttonPlayForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/sequece/resources/play.png"))); // NOI18N
        jPanel4.add(buttonPlayForward);

        jPanel2.add(jPanel4, java.awt.BorderLayout.PAGE_END);

        jPanel5.add(jPanel2);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new java.awt.GridBagLayout());

        panelSequenceAnimatedPreview.setBackground(new java.awt.Color(255, 255, 255));
        panelSequenceAnimatedPreview.setBorder(javax.swing.BorderFactory.createLineBorder(ColorConstants.COLOR_OUTLINE_PLAIN));
        panelSequenceAnimatedPreview.setMaximumSize(new java.awt.Dimension(80, 80));
        panelSequenceAnimatedPreview.setMinimumSize(new java.awt.Dimension(80, 80));
        panelSequenceAnimatedPreview.setPreferredSize(new java.awt.Dimension(80, 80));
        panelSequenceAnimatedPreview.setLayout(new java.awt.BorderLayout());
        jPanel7.add(panelSequenceAnimatedPreview, new java.awt.GridBagConstraints());

        jPanel6.add(jPanel7, java.awt.BorderLayout.CENTER);

        jPanel8.setMaximumSize(new java.awt.Dimension(174, 44));
        jPanel8.setMinimumSize(new java.awt.Dimension(174, 44));

        panelSpinner.setBackground(new java.awt.Color(255, 255, 255));
        panelSpinner.setMaximumSize(new java.awt.Dimension(100, 20));
        panelSpinner.setMinimumSize(new java.awt.Dimension(100, 20));
        panelSpinner.setPreferredSize(new java.awt.Dimension(100, 20));
        panelSpinner.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .add(panelSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(panelSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.add(jPanel8, java.awt.BorderLayout.PAGE_END);

        jPanel5.add(jPanel6);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 264, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonPause;
    private javax.swing.JButton buttonPlayBackward;
    private javax.swing.JButton buttonPlayForward;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JLabel labelName;
    private javax.swing.JPanel panelSequenceAnimatedPreview;
    private javax.swing.JPanel panelSpinner;
    // End of variables declaration//GEN-END:variables

}
