/*
 * ShellView.java
 */

package applicationpackage;

import application.Action;
import application.ApplicationContext;
import application.ResourceMap;
import application.SingleFrameApplication;
import application.FrameView;
import application.TaskMonitor;
import application.Task;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.Binding;

/**
 * The application's main frame.
 */
public class ShellView extends FrameView {
    
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

        // connecting action tasks to status bar via TaskMonitor
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
        bindingGroup.addBindingListener(new AbstractBindingListener() {
            @Override
            public void targetEdited(Binding binding) {
                // save action observes saveNeeded property
                if (!saveNeeded) {
                    saveNeeded = true;
                    firePropertyChange("saveNeeded", false, true);
                }
            }
        });

        // have a transaction started
        entityManager.getTransaction().begin();
    }


    public boolean isSaveNeeded() {
        return saveNeeded;
    }

    public boolean isRecordSelected() {
        return masterTable.getSelectedRow() != -1;
    }

    @Action(enabledProperty = "saveNeeded")
    public Task save() {
        return new Task(getApplication()) {
            protected Void doInBackground() {
                entityManager.getTransaction().commit();
                entityManager.getTransaction().begin();
                return null;
            }
            @Override
            protected void finished() {
                saveNeeded = false;
                ShellView.this.firePropertyChange("saveNeeded", true, false);
            }
        };
    }

    /**
     * An example task showing how to create tasks for asynchronous actions
     * running on background and indicating their progress.
     */
     @Action public Task refresh() {
        return new Task(getApplication()) {
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
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    private boolean saveNeeded;
}
