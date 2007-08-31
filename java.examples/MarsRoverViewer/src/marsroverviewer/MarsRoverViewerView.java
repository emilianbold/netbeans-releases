/*
 * Copyright (c) 2007, Sun Microsystems, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems, Inc. nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */ 

package marsroverviewer;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.jdesktop.application.Task;

/**
 * This application is a simple image viewer. This class displays the main frame
 * of the application and provides much of the logic. This class is called by
 * the main application class, MarsRoverViewerApp. For an overview of the
 * application see the comments for the MarsRoverViewerApp class. 
 */
public class MarsRoverViewerView extends FrameView {

    /* The following fields define the application's internal state.
     * We track our current - imageIndex - position in the list of URLs.
     * The value of imageTask is managed by ShowImagTask, it's initialized
     * (on the EDT) when the task is constructed and cleared (on the EDT)
     * by the task, when it's done.  The boolean *enabled fields are updated
     * by calling updateNextPreviousEnabledProperties().
     */
    private List<URL> imageLocations;
    private int imageIndex = 0;
    private ShowImageTask imageTask = null; 
    private boolean nextImageEnabled = true;
    private boolean previousImageEnabled = false;

    public MarsRoverViewerView(SingleFrameApplication app, List<URL> imageLocations) {
        super(app);
        this.imageLocations = imageLocations;

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connect action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox(ActionEvent e) {
        if (aboutBox == null) {
            JFrame mainFrame = MarsRoverViewerApp.getApplication().getMainFrame();
            aboutBox = new MarsRoverViewerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        MarsRoverViewerApp.getApplication().show(aboutBox);
    }

    /* The nextImage, previousImage and refreshImage actions clear the
     * displayed image, by calling showImageLoading(), to free up heap space.
     * Most of the images we're loading are so large that there's not
     * enough heap space (by default) to accomodate both the old and
     * new ones.  We could adjust the heap size parameters to
     * eliminate the problem, however it's more neighborly to just
     * limit the heap's growth.
     */

    @Action(enabledProperty = "nextImageEnabled")
    public Task nextImage() {
	Task task = null;
	if (imageIndex < (imageLocations.size() - 1)) {
	    imageIndex += 1;
	    updateNextPreviousEnabledProperties();
	    task = new ShowImageTask(imageLocations.get(imageIndex));
	}
	return task;
    }

    @Action(enabledProperty = "previousImageEnabled")
    public Task previousImage() {
	Task task = null;
	if (imageIndex > 0) {
	    imageIndex -= 1;
	    updateNextPreviousEnabledProperties();
	    task = new ShowImageTask(imageLocations.get(imageIndex));
	}
	return task;
    }

    @Action
    public Task refreshImage() {
	return new ShowImageTask(imageLocations.get(imageIndex));
    }

    @Action
    public void stopLoading() {
	if ((imageTask != null) && !imageTask.isDone()) {
	    imageTask.cancel(true);
	}
    }

    /* The properties below define the enabled state for the 
     * corresponding @Actions.
     */

    private void updateNextPreviousEnabledProperties() {
	setNextImageEnabled(imageIndex < (imageLocations.size() - 1));
	setPreviousImageEnabled(imageIndex > 0);
    }

    public boolean isNextImageEnabled() { 
	return nextImageEnabled; 
    }

    public void setNextImageEnabled(boolean nextImageEnabled) {
	boolean oldValue = this.nextImageEnabled;
	this.nextImageEnabled = nextImageEnabled;
	firePropertyChange("nextImageEnabled", oldValue, this.nextImageEnabled);
    }

    public boolean isPreviousImageEnabled() { 
	return previousImageEnabled; 
    }

    public void setPreviousImageEnabled(boolean previousImageEnabled) {
	boolean oldValue = this.previousImageEnabled;
	this.previousImageEnabled = previousImageEnabled;
	firePropertyChange("previousImageEnabled", oldValue, this.previousImageEnabled);
    }

    /* A application specific subclass of LoadImageTask.
     * 
     * This class is constructed on the EDT.  The constructor
     * stops the current ShowImageTask, if one is still
     * running, clears the display (imageLabel) so that 
     * we'll only have one enormous image on the heap, and 
     * updates the enabled state of the next/previous @Actions.
     * When the task completes, we update the GUI.
     */
    private class ShowImageTask extends LoadImageTask {
	ShowImageTask(URL imageURL) {
	    super(MarsRoverViewerApp.getApplication(), imageURL);
	    stopLoading();  
	    imageTask = this;
	    showImageMessage(imageURL, "loadingWait");
	}

	@Override protected void cancelled() {
	    if (imageTask == this) {
		showImageMessage(getImageURL(), "loadingCancelled");
	    }
	}

	@Override protected void succeeded(BufferedImage image) {
            super.succeeded(image);
	    if (imageTask == this) {
		showImage(getImageURL(), image);
	    }
	}

	@Override protected void failed(Throwable e) {
            super.failed(e);
	    if (imageTask == this) {
		showImageMessage(getImageURL(), "loadingFailed");
	    }
	}
    
	@Override protected void finished() {
            super.finished();
	    imageTask = null;
	}
    }

    private void showImage(URL imageURL, BufferedImage image) {
	int width = image.getWidth();
	int height = image.getHeight();
	String tip = getResourceMap().getString("imageTooltip", imageURL, width, height);
	imageLabel.setToolTipText(tip);
	imageLabel.setText(null);
	imageLabel.setIcon(new ImageIcon(image));
    }

    private void showImageMessage(URL imageURL, String key) {
	String msg = getResourceMap().getString(key, imageURL);
	imageLabel.setToolTipText("");
	imageLabel.setText(msg);
	imageLabel.setIcon(null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        imageLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem previousMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem nextMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem refreshMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem stopMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator separator1 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        toolbar = new javax.swing.JToolBar();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        scrollPane.setName("scrollPane"); // NOI18N

        imageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageLabel.setName("imageLabel"); // NOI18N
        imageLabel.setOpaque(true);
        scrollPane.setViewportView(imageLabel);

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
        );
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(marsroverviewer.MarsRoverViewerApp.class).getContext().getResourceMap(MarsRoverViewerView.class);
        resourceMap.injectComponents(mainPanel);

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(marsroverviewer.MarsRoverViewerApp.class).getContext().getActionMap(MarsRoverViewerView.class, this);
        previousMenuItem.setAction(actionMap.get("previousImage")); // NOI18N
        previousMenuItem.setName("previousMenuItem"); // NOI18N
        previousMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(previousMenuItem);

        nextMenuItem.setAction(actionMap.get("nextImage")); // NOI18N
        nextMenuItem.setName("nextMenuItem"); // NOI18N
        fileMenu.add(nextMenuItem);

        refreshMenuItem.setAction(actionMap.get("refreshImage")); // NOI18N
        refreshMenuItem.setName("refreshMenuItem"); // NOI18N
        fileMenu.add(refreshMenuItem);

        stopMenuItem.setAction(actionMap.get("stopLoading")); // NOI18N
        stopMenuItem.setName("stopMenuItem"); // NOI18N
        fileMenu.add(stopMenuItem);

        separator1.setName("separator1"); // NOI18N
        fileMenu.add(separator1);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);
        resourceMap.injectComponents(menuBar);

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setName("toolbar"); // NOI18N

        previousButton.setAction(actionMap.get("previousImage")); // NOI18N
        previousButton.setFocusable(false);
        previousButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        previousButton.setName("previousButton"); // NOI18N
        previousButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(previousButton);

        nextButton.setAction(actionMap.get("nextImage")); // NOI18N
        nextButton.setFocusable(false);
        nextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextButton.setName("nextButton"); // NOI18N
        nextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(nextButton);

        refreshButton.setAction(actionMap.get("refreshImage")); // NOI18N
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setName("refreshButton"); // NOI18N
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(refreshButton);

        stopButton.setAction(actionMap.get("stopLoading")); // NOI18N
        stopButton.setFocusable(false);
        stopButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stopButton.setName("stopButton"); // NOI18N
        stopButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(stopButton);
        resourceMap.injectComponents(toolbar);

        statusPanel.setName("statusPanel"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusMessageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 306, Short.MAX_VALUE)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusMessageLabel)
                    .add(statusAnimationLabel)
                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3))
        );
        resourceMap.injectComponents(statusPanel);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(toolbar);
    }// </editor-fold>//GEN-END:initComponents

    private void previousMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousMenuItemActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_previousMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel imageLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton refreshButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JButton stopButton;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
