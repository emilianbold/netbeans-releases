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

package org.netbeans.modules.vmd.game;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.game.dialog.NewSceneDialog;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.EditorManagerListener;
import org.netbeans.modules.vmd.game.model.adapter.GlobalRepositoryComboBoxModel;
import org.netbeans.modules.vmd.game.nbdialog.NewLayerDialog;
import org.netbeans.modules.vmd.game.view.main.MainView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JToolBar;

/**
 *
 * @author Karel Herink
 */
public class GameEditorView implements DataEditorView, EditorManagerListener {

    private static final long serialVersionUID = 3317521472849153199L;

    public static final boolean DEBUG = false;

    private DataObjectContext context;
    private transient GameController controller;

    private transient JComponent toolBarRepresentation;
    private transient JComboBox comboGlobal;

    /** Creates a new instance of GameEditorView */
    public GameEditorView(DataObjectContext context) {
        this.context = context;
        init();
    }

    private void init () {
        this.controller = new GameController (context);
        MainView.getInstance().addEditorManagerListener(this);
    }

    public DataObjectContext getContext() {
        return this.context;
    }

    public DataEditorView.Kind getKind() {
        return DataEditorView.Kind.MODEL;
    }

    public String preferredID() {
        return GameController.PROJECT_TYPE_GAME;
    }

    public String getDisplayName() {
        return "Game Builder";
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return null;
    }

    public JComponent getVisualRepresentation() {
        return this.controller.getVisualRepresentation();
    }

    public JComponent getToolbarRepresentation() {
        if (this.toolBarRepresentation == null) {
            JToolBar tool = new JToolBar();

            tool.addSeparator();
            this.comboGlobal = new JComboBox(new GlobalRepositoryComboBoxModel());
            comboGlobal.setMaximumRowCount(16);
            comboGlobal.setRenderer(new DefaultListCellRenderer() {
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    DefaultListCellRenderer retValue = (DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof  String) {
                        retValue.setFont(retValue.getFont().deriveFont(Font.BOLD));
                    }
                    else {
                        //this is a hack - instead use an empty image 16x16
                        retValue.setText("  " + retValue.getText());
                    }
                    return retValue;
                }

            });
            comboGlobal.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object item = comboGlobal.getSelectedItem();
                    if (item instanceof Editable) {
                        if (DEBUG) System.out.println("Request editing for: " + item);
                        MainView.getInstance().requestEditing((Editable) item);
                    }
                }
            });
            comboGlobal.setSelectedItem(MainView.getInstance().getCurrentEditable());

            tool.add(comboGlobal);
            tool.addSeparator();

            JButton buttonCreateScene = new JButton("New scene");
            buttonCreateScene.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    NewSceneDialog dialog = new NewSceneDialog();
                    DialogDescriptor dd = new DialogDescriptor(dialog, "Create a new Scene");
                    dd.setButtonListener(dialog);
                    dd.setValid(false);
                    dialog.setDialogDescriptor(dd);
                    Dialog d = DialogDisplayer.getDefault().createDialog(dd);
                    d.setVisible(true);
                }
            });

            JButton buttonCreateTiledLayer = new JButton("New layer");
            buttonCreateTiledLayer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    NewLayerDialog nld = new NewLayerDialog(null);
                    DialogDescriptor dd = new DialogDescriptor(nld, "Create a new layer");
                    dd.setButtonListener(nld);
                    dd.setValid(false);
                    nld.setDialogDescriptor(dd);
                    Dialog d = DialogDisplayer.getDefault().createDialog(dd);
                    d.setVisible(true);
                }
            });

            tool.add(buttonCreateScene);
            tool.add(buttonCreateTiledLayer);

            this.toolBarRepresentation = tool;
        }
        return this.toolBarRepresentation;
    }

    public org.openide.awt.UndoRedo getUndoRedo() {
        return null;
    }

    public void componentOpened() {
    }

    public void componentClosed() {
    }

    public void componentShowing() {
//        SwingUtilities.invokeLater (new Runnable() {
//            public void run () {
////                WindowManager.getDefault ().findTopComponentGroup (GameBuilderInspector.ID).open ();
//                GameBuilderInspector instance = GameBuilderInspector.findInstance ();
//                instance.open ();
//                instance.requestVisible ();
//            }
//        });
    }

    public void componentHidden() {
//        SwingUtilities.invokeLater (new Runnable() {
//            public void run () {
//                GameBuilderInspector instance = GameBuilderInspector.findInstance ();
//                instance.close ();
////                WindowManager.getDefault ().findTopComponentGroup (GameBuilderInspector.ID).close ();
//            }
//        });
    }

    public void componentActivated() {
        if (DEBUG)
            System.out.println("GameEditorView activated");
//        WindowManager.getDefault ().findTopComponentGroup (GameBuilderInspector.ID).open ();
        GameBuilderInspector instance = GameBuilderInspector.findInstance ();
        instance.open ();
        instance.requestVisible ();
    }

    public void componentDeactivated() {
    }

    public int getOpenPriority() {
        return getOrder ();
    }

    public int getEditPriority() {
        return - getOrder ();
    }

    public int getOrder() {
        return 500;
    }

    //EditorManagerListener
    public void editing(Editable e) {
        if (DEBUG) System.out.println("EDITING: " + e);
        if (comboGlobal != null) {
            comboGlobal.setSelectedItem(e);
            comboGlobal.repaint();
        }
    }

    private void writeObject (java.io.ObjectOutputStream out) throws IOException {
        out.writeObject (context);
    }

    private void readObject (java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object object = in.readObject ();
        if (! (object instanceof DataObjectContext))
            throw new ClassNotFoundException ("DataObjectContext expected but not found");
        context = (DataObjectContext) object;
        init ();
    }

}
