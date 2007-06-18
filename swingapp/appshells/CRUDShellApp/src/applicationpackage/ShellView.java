/*
 * ShellView.java
 */

package applicationpackage;

import application.Action;
import application.ApplicationContext;
import application.ResourceMap;
import application.SingleFrameApplication;
import application.SingleFrameApplication.SingleFrameApplicationView;
import application.Task;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;/* DETAIL_ONLY */
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;/* DETAIL_ONLY */
import java.util.List;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * The application's main frame.
 */
public class ShellView extends SingleFrameApplicationView {
    
    public ShellView(SingleFrameApplication app) {
        super(app);

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

        // tracking table selection
        masterTable.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getSource() == masterTable.getSelectionModel()) {
                        firePropertyChange("recordSelected", !isRecordSelected(), isRecordSelected());
                    }
                }
            });

        // tracking changes to save
        bindingContext.addPropertyChangeListener("hasEditedTargetValues",
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    // save action observes saveNeeded property
                    firePropertyChange("saveNeeded", e.getOldValue(), e.getNewValue());
                }
            });

        // have a transaction started
        entityManager.getTransaction().begin();
    }


    public boolean isSaveNeeded() {
        return bindingContext.getHasEditedTargetValues();
    }

    public boolean isRecordSelected() {
        return masterTable.getSelectedRow() != -1;
    }

    @Action(enabledProperty = "saveNeeded")
    public Task save() {
        return new Task() {
            protected Void doInBackground() {
                entityManager.getTransaction().commit();
                entityManager.getTransaction().begin();
                bindingContext.clearHasEditedTargetValues();
                return null;
            }
        };
    }

    /**
     * An example task showing how to create tasks for asynchronous actions
     * running on background and indicating their progress.
     */
     @Action public Task refresh() {
        return new Task() {
            protected Void doInBackground() {
                try {
                    setProgress(0, 0, 4);
                    setMessage("Rolling back the current changes...");
                    setProgress(1, 0, 4);
                    entityManager.getTransaction().rollback();
                    Thread.sleep(1000L);
                    setProgress(2, 0, 4);

                    setMessage("Starting a new transaction...");
                    entityManager.getTransaction().begin();
                    Thread.sleep(500L);
                    setProgress(3, 0, 4);

                    setMessage("Fetching new data...");
                    java.util.Collection data = query.getResultList();
                    Thread.sleep(1300L);
                    setProgress(4, 0, 4);

                    Thread.sleep(150L);
                    list.clear();
                    list.addAll(data);
                } catch(InterruptedException ignore) { }
                return null;
            }
            protected void finished() {
                setMessage("Done.");
            }
        };
    }

    @Action(enabledProperty = "recordSelected")
    public void deleteRecord() {
        int[] selected = masterTable.getSelectedRows();
        List<_masterClass_> toRemove = new ArrayList<_masterClass_>(selected.length);
        for (int idx=0; idx<selected.length; idx++) {
            _masterClass_ _masterEntityInitial_ = list.get(/* JDK6ONLY */masterTable.convertRowIndexToModel(/* JDK6ONLY */selected[idx]/* JDK6ONLY */)/* JDK6ONLY */);
            toRemove.add(_masterEntityInitial_);
            entityManager.remove(_masterEntityInitial_);
        }
        list.removeAll(toRemove);
    }

    @Action public void newRecord() {
        _masterClass_ _masterEntityInitial_ = new _masterClass_();
        entityManager.persist(_masterEntityInitial_);
        list.add(_masterEntityInitial_);
        int row = list.size()-1;
        masterTable.setRowSelectionInterval(row, row);
        masterTable.scrollRectToVisible(masterTable.getCellRect(row, 0, true));
    }

    @Action public void showAboutBox(ActionEvent e) {
        if (aboutBox == null) {
            JFrame mainFrame = ShellApp.getApplication().getMainFrame();
            aboutBox = new ShellAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        ShellApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        taskMonitor = new application.TaskMonitor();

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 252, Short.MAX_VALUE)
        );

        fileMenu.setText(application.ApplicationContext.getInstance().getResourceMap(ShellView.class).getString("fileMenu.text")); // NOI18N

        exitMenuItem.setAction(ApplicationContext.getInstance().getActionMap(ShellView.class, this).get("quit"));
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(application.ApplicationContext.getInstance().getResourceMap(ShellView.class).getString("helpMenu.text")); // NOI18N

        aboutMenuItem.setAction(application.ApplicationContext.getInstance().getActionMap(ShellView.class, this).get("showAboutBox"));
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 226, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                taskMonitorPropertyChange(evt);
            }
        });

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

private void taskMonitorPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_taskMonitorPropertyChange
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
}//GEN-LAST:event_taskMonitorPropertyChange
   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private application.TaskMonitor taskMonitor;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
