/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.core.ui.components;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;

/**
 *
 * @author ak119685
 */
public abstract class DLightTargetSelectionDialog extends JDialog implements ActionListener {

  public static final int RESULT_OK = 0;
  public static final int RESULT_CANCEL = 1;
  public static final int RESULT_ERROR = -1;
  private int result = RESULT_ERROR;
  protected JButton btnOK = new JButton(NbBundle.getMessage(DLightTargetSelectionDialog.class, "DLightTargetSelectionDialog.OK_Button")); //NOI18N
  protected JButton btnCancel = new JButton(NbBundle.getMessage(DLightTargetSelectionDialog.class, "DLightTargetSelectionDialog.Cancel_Button")); //NOI18N
  protected JPanel buttonPanel = new JPanel();
  protected ArrayList<Validatable> toValidate = new ArrayList<Validatable>();

  public abstract String getProgramName();

  public abstract String getProgramArguments();

  public abstract String getWorkingDirectory();

  public DLightTargetSelectionDialog(String title) {
    super();
    setModal(true);
    setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
    setTitle(title);
  }

  public void show(Frame frame) {
    setLocationRelativeTo(frame);
    setVisible(true);
  }

  final void init() {
    initComponents();
    initCancelOKButtons();
    getRootPane().setDefaultButton(btnOK);
    pack();
  }

  void initCancelOKButtons() {
    int btnHSize = (int) btnOK.getPreferredSize().getHeight();
    btnOK.addActionListener(this);
    btnOK.setPreferredSize(new Dimension(80, btnHSize));
    btnCancel.addActionListener(this);
    btnCancel.setPreferredSize(new Dimension(80, btnHSize));

    btnOK.setToolTipText(NbBundle.getMessage(DLightTargetSelectionDialog.class, "DLightTargetSelectionDialog.OK_Button.Tooltip")); //NOI18N
    btnCancel.setToolTipText(NbBundle.getMessage(DLightTargetSelectionDialog.class, "DLightTargetSelectionDialog.Cancel_Button.Tooltip")); //NOI18N
    buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 5, 20));
    buttonPanel.add(btnOK);
    buttonPanel.add(btnCancel);
    getContentPane().add(buttonPanel);
  }

  void setResult(int result) {
    this.result = result;
  }

  String selectFile(String startWith) {
    return selectFile(startWith, null, null);
  }

  String selectFile(String startWith, FileFilter filter) {
    return selectFile(startWith, filter, null);
  }

  String selectFile(String startWith, FileFilter filter, ContentValidator validator) {
    return select(startWith, filter, validator, JFileChooser.FILES_ONLY);
  }

  String selectDirectory(String startWith) {
    return selectDirectory(startWith, null);
  }

  String selectDirectory(String startWith, ContentValidator validator) {
    return select(startWith, null, validator, JFileChooser.DIRECTORIES_ONLY);
  }

  String select(String startWith, FileFilter filter, ContentValidator validator, int mode) {
    if (startWith == null) {
      startWith = "."; //NOI18N
    }

    File startPath = new File(startWith);
    String selection = startWith;
    JFileChooser2 jfc = new JFileChooser2(startPath, validator);
    jfc.setFileSelectionMode(mode);
    jfc.setFileFilter(filter);
    jfc.addChoosableFileFilter(filter);
    jfc.setMultiSelectionEnabled(false);
    int i = jfc.showOpenDialog(this);
    if (i == JFileChooser.APPROVE_OPTION) {
      try {
        File fResult = jfc.getSelectedFile();
        selection = fResult.getCanonicalPath();
        return selection;
      } catch (IOException ex) {
      }
    }

    return null;
  }

  void addPanel(String title, JComboBox2 comboBox, JButton button) {
    JPanel panel = new JPanel(new DLightTargetSelectionDialogLayout());
    panel.setBorder(BorderFactory.createTitledBorder(title));
    if (comboBox != null) {
      panel.add(comboBox, DLightTargetSelectionDialogLayout.COMBO_IDX);
      toValidate.add(comboBox);
    }
    if (button != null) {
      panel.add(button, DLightTargetSelectionDialogLayout.BTN_IDX);
    }
    getContentPane().add(panel);
  }

  static void updateModel(DefaultComboBoxModel model, String value) {
    if (value == null) {
      return;
    }

    model.removeElement(value);
    model.insertElementAt(value, 0);
    model.setSelectedItem(value);
  }

  public int getResult() {
    return result;
  }

  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (!(src instanceof JButton)) {
      return;
    }
    if (src == btnOK) {
      approveSelection();
    } else if (src == btnCancel) {
      setResult(RESULT_CANCEL);
      setVisible(false);
    }
  }

  abstract void initComponents();

  public void approveSelection() {
    for (Validatable v : toValidate) {
      if (!v.validateContent()) {
        return;
      }
    }

    setResult(RESULT_OK);
    setVisible(false);
  }
}

interface Validatable {
  public boolean validateContent();
}

class JFileChooser2 extends JFileChooser {
  ContentValidator validator = null;

  JFileChooser2(File startPath, ContentValidator validator) {
    super(startPath);
    this.validator = validator;
  }

  @Override
  public void approveSelection() {
    if (validator != null) {
      String msgError = validator.validate(getSelectedFile().getPath());
      if (msgError != null) {
        JOptionPane.showMessageDialog(this, msgError);
        return;
      }
    }

    super.approveSelection();
  }
}

interface ContentValidator {
  public String validate(String value);
}

class JComboBox2 extends JComboBox implements Validatable {
  ContentValidator validator = null;

  public JComboBox2(final DefaultComboBoxModel model) {
    this(model, 10, true, null);
  }

  public JComboBox2(final DefaultComboBoxModel model, int maxRows) {
    this(model, maxRows, true, null);
  }

  public JComboBox2(final DefaultComboBoxModel model, int maxRows, boolean bEditable) {
    this(model, maxRows, bEditable, null);
  }

  public JComboBox2(final DefaultComboBoxModel model, int maxRows, boolean bEditable, ContentValidator validator) {
    super(model);
    this.validator = validator;
    setMaximumRowCount(maxRows);
    setEditable(bEditable);
    addFocusListener(new FocusAdapter() {

      @Override
      public void focusLost(FocusEvent e) {
        super.focusLost(e);
        //e.g
        updateModel();
      }
    });

    addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        //check current awt event
        if (EventQueue.isDispatchThread()){
          AWTEvent event = EventQueue.getCurrentEvent();
          if (event != null  && event instanceof FocusEvent){
            FocusEvent fEvent = (FocusEvent)event;
            DLightTargetSelectionDialog instance = 
                    (DLightTargetSelectionDialog)SwingUtilities.getAncestorOfClass(DLightTargetSelectionDialog.class, JComboBox2.this);
            if (fEvent.getID() == FocusEvent.FOCUS_LOST && 
                    fEvent.getOppositeComponent() == instance.btnCancel){
              return;
            }
          }
        }
        if ("comboBoxEdited".equals(e.getActionCommand())) { //NOI18N
          //we should check if we have cancel button pushed
          updateModel();
        }
      }
    });


  }

  private boolean updateModel() {
    if (!validateContent()) {
      return false;
    }

    String str = ((String) getSelectedItem()).trim();
    
    DefaultComboBoxModel model = (DefaultComboBoxModel) getModel();
    model.removeElement(str);
    model.insertElementAt(str, 0);
    model.setSelectedItem(str);

    return true;
  }

  public boolean validateContent() {
    if (validator != null) {
      String str = ((String) getSelectedItem()).trim();
      String errorMsg = validator.validate(str);

      if (errorMsg != null) {
        JOptionPane.showMessageDialog(SwingUtilities.getAncestorOfClass(JDialog.class, this), errorMsg);
        return false;
      }
    }

    return true;
  }
}
