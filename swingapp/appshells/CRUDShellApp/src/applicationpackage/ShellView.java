/*
 * ShellView.java
 */

package applicationpackage;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.Task;
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
                    firePropertyChange("recordSelected", !isRecordSelected(), isRecordSelected());
                }
            });/* DETAIL_ONLY */
        detailTable.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    firePropertyChange("detailRecordSelected", !isDetailRecordSelected(), isDetailRecordSelected());
                }
            }); /* DETAIL_ONLY */

        // tracking changes to save
        bindingGroup.addBindingListener(new AbstractBindingListener() {
            @Override
            public void targetEdited(Binding binding) {
                // save action observes saveNeeded property
                setSaveNeeded(true);
            }
        });

        // have a transaction started
        entityManager.getTransaction().begin();
    }


    public boolean isSaveNeeded() {
        return saveNeeded;
    }

    private void setSaveNeeded(boolean saveNeeded) {
        if (saveNeeded != this.saveNeeded) {
            this.saveNeeded = saveNeeded;
            firePropertyChange("saveNeeded", !saveNeeded, saveNeeded);
        }
    }

    public boolean isRecordSelected() {
        return masterTable.getSelectedRow() != -1;
    }
    /* DETAIL_ONLY */
    public boolean isDetailRecordSelected() {
        return detailTable.getSelectedRow() != -1;
    }/* DETAIL_ONLY */

    @Action
    public void newRecord() {
        _masterClass_ _masterEntityInitial_ = new _masterClass_();
        entityManager.persist(_masterEntityInitial_);
        list.add(_masterEntityInitial_);
        int row = list.size()-1;
        masterTable.setRowSelectionInterval(row, row);
        masterTable.scrollRectToVisible(masterTable.getCellRect(row, 0, true));
        setSaveNeeded(true);
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
        setSaveNeeded(true);
    }
    /* DETAIL_ONLY */
    @Action(enabledProperty = "recordSelected")
    public void newDetailRecord() {
        int index = masterTable.getSelectedRow();
        _masterClass_ _masterEntityInitial_ = list.get(/* JDK6ONLY */masterTable.convertRowIndexToModel(/* JDK6ONLY */index/* JDK6ONLY */)/* JDK6ONLY */);
        Collection<_detailClass_> _detailEntityInitial_s = _masterEntityInitial_.get_joinCollectionCapital_();
        if (_detailEntityInitial_s == null) {
            _detailEntityInitial_s = new LinkedList<_detailClass_>();
            _masterEntityInitial_.set_joinCollectionCapital_(_detailEntityInitial_s);
        }
        _detailClass_ _detailEntityInitial_ = new _detailClass_();
        entityManager.persist(_detailEntityInitial_);
        _detailEntityInitial_.set_joinCapital_(_masterEntityInitial_);
        _detailEntityInitial_s.add(_detailEntityInitial_);
        masterTable.clearSelection();
        masterTable.setRowSelectionInterval(index, index);
        int row = _detailEntityInitial_s.size()-1;
        detailTable.setRowSelectionInterval(row, row);
        detailTable.scrollRectToVisible(detailTable.getCellRect(row, 0, true));
        setSaveNeeded(true);
    }

    @Action(enabledProperty = "detailRecordSelected")
    public void deleteDetailRecord() {
        int index = masterTable.getSelectedRow();
        _masterClass_ _masterEntityInitial_ = list.get(/* JDK6ONLY */masterTable.convertRowIndexToModel(/* JDK6ONLY */index/* JDK6ONLY */)/* JDK6ONLY */);
        Collection<_detailClass_> _detailEntityInitial_s = _masterEntityInitial_.get_joinCollectionCapital_();
        int[] selected = detailTable.getSelectedRows();
        List<_detailClass_> toRemove = new ArrayList<_detailClass_>(selected.length);
        for (int idx=0; idx<selected.length; idx++) {/* JDK6ONLY */
            selected[idx] = detailTable.convertRowIndexToModel(selected[idx]);/* JDK6ONLY */
            int count = 0;
            Iterator<_detailClass_> iter = _detailEntityInitial_s.iterator();
            while (count++ < selected[idx]) iter.next();
            _detailClass_ _detailEntityInitial_ = iter.next();
            toRemove.add(_detailEntityInitial_);
            entityManager.remove(_detailEntityInitial_);
        }
        _detailEntityInitial_s.removeAll(toRemove);
        masterTable.clearSelection();
        masterTable.setRowSelectionInterval(index, index);
        setSaveNeeded(true);
    }/* DETAIL_ONLY */

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
                setSaveNeeded(false);
            }
        };
    }

    /**
     * An example action method showing how to create asynchronous tasks
     * (running on background) and how to show their progress. Note the
     * artificial 'Thread.sleep' calls making the task long enough to see the
     * progress visualization - remove the sleeps for real application.
     */
     @Action
     public Task refresh() {
        return new Task(getApplication()) {
            protected Void doInBackground() {
                try {
                    setProgress(0, 0, 4);
                    setMessage("Rolling back the current changes...");
                    setProgress(1, 0, 4);
                    entityManager.getTransaction().rollback();
                    Thread.sleep(1000L); // remove for real app
                    setProgress(2, 0, 4);

                    setMessage("Starting a new transaction...");
                    entityManager.getTransaction().begin();
                    Thread.sleep(500L); // remove for real app
                    setProgress(3, 0, 4);

                    setMessage("Fetching new data...");
                    java.util.Collection data = query.getResultList();
                    Thread.sleep(1300L); // remove for real app
                    setProgress(4, 0, 4);

                    Thread.sleep(150L); // remove for real app
                    list.clear();
                    list.addAll(data);
                } catch(InterruptedException ignore) { }
                return null;
            }
            protected void finished() {
                setMessage("Done.");
                setSaveNeeded(false);
            }
        };
    }

    @Action
    public void showAboutBox(ActionEvent e) {
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
