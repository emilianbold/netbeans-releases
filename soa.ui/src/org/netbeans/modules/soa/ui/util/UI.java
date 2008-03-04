/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.soa.ui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Stack;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.14
 */
public final class UI {

  private UI() {}

  public static boolean isAlt(int modifiers) {
    return isModifier(modifiers, KeyEvent.ALT_MASK);
  }

  public static boolean isShift(int modifiers) {
    return isModifier(modifiers, KeyEvent.SHIFT_MASK);
  }

  public static boolean isCtrl(int modifiers) {
    return
      isModifier(modifiers, KeyEvent.CTRL_MASK) ||
      isModifier(modifiers, KeyEvent.META_MASK);
  }

  private static boolean isModifier(int modifiers, int mask) {
    return (modifiers & mask) != 0;
  }

  public static JLabel createLabel(String message) {
    JLabel label = new JLabel();
    Mnemonics.setLocalizedText(label, message);
    return label;
  }

  public static JRadioButton createRadioButton(String text, String toolTip) {
    JRadioButton button = new JRadioButton();
    Mnemonics.setLocalizedText(button, text);
    button.setText(cutMnemonicAndAmpersand(text));
    button.setToolTipText(toolTip);
    return button;
  }

  public static JButton createButton(Action action) {
    return (JButton) createAbstractButton(new JButton(), action);
  }

  public static JCheckBox createCheckBox(Action action) {
    return (JCheckBox) createAbstractButton(new JCheckBox(), action);
  }

  public static JToggleButton createToggleButton(Action action) {
    return (JToggleButton) createAbstractButton(new JToggleButton(), action);
  }

  public static void setItems(JComboBox comboBox, Object [] items) {
    Object selected = comboBox.getSelectedItem();
    comboBox.removeAllItems();
    
    for (int i=0; i < items.length; i++) {
      comboBox.insertItemAt(items [i], i);
    }
    if (items.length > 0) {
      comboBox.setSelectedIndex(0);
    }
    if (selected != null) {
      comboBox.setSelectedItem(selected);
    }
  }

  public static JPanel createSeparator(String message) {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;

    c.insets = new Insets(SMALL_INSET, 0, SMALL_INSET, 0);
    panel.add(createLabel(message), c);

    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(SMALL_INSET, SMALL_INSET, SMALL_INSET, 0);
    panel.add(new JSeparator(), c);

    return panel;
  }

  private static AbstractButton createAbstractButton(
    AbstractButton button,
    Action action)
  {
    button.setAction(action);
    mnemonicAndToolTip(button, (String) action.getValue(Action.SHORT_DESCRIPTION));
    return button;
  }

  private static void mnemonicAndToolTip(AbstractButton button, String toolTip) {
    String text = button.getText();

    if (text == null) {
      Mnemonics.setLocalizedText(button, toolTip);
      button.setText(null);
    }
    else {
      Mnemonics.setLocalizedText(button, text);
      button.setText(cutMnemonicAndAmpersand(text));
    }
    button.setToolTipText(cutMnemonicAndAmpersand(toolTip));
  }

  private static String cutMnemonicAndAmpersand(String value) {
    if (value == null) {
      return null;
    }
    int k = value.lastIndexOf(" // "); // NOI18N

    if (k != -1) {
      value = value.substring(0, k);
    }
    k = value.indexOf("&"); // NOI18N

    if (k == -1) {
      return value;
    }
    return value.substring(0, k) + value.substring(k + 1);
  }

  public static JTextArea createTextArea(int columns, String message) {
    JTextArea text = new JTextArea(message);
    text.setBackground(null);
    text.setEditable(false);
    text.setColumns(columns);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    return text;
  }

  public static void a11y(Component component, String a11y) {
      a11y(component, a11y, a11y);
  }

  public static void a11y(Component component, String a11yN, String a11yD) {
    if (a11yN != null) {  
        component.getAccessibleContext().setAccessibleName(a11yN);
    }
    if (a11yD != null) {
        component.getAccessibleContext().setAccessibleDescription(a11yD);
    }
  }

  public static String i18n(Class clazz, String key) {
    if (key == null) {
      return null;
    }
    return NbBundle.getMessage(clazz, key);
  }

  public static String i18n(Class clazz, String key, String param) {
    if (key == null) {
      return null;
    }
    return NbBundle.getMessage(clazz, key, param);
  }

  public static String i18n(Class clazz, String key, String param1, String param2) {
    if (key == null) {
      return null;
    }
    return NbBundle.getMessage(clazz, key, param1, param2);
  }

  public static String i18n(
    Class clazz,
    String key,
    String param1,
    String param2,
    String param3)
  {
    if (key == null) {
      return null;
    }
    return NbBundle.getMessage(clazz, key, param1, param2, param3);
  }

  public static boolean printWarning(String message) {
    NotifyDescriptor confirm = new NotifyDescriptor.Confirmation(
      message,
      NotifyDescriptor.YES_NO_OPTION,
      NotifyDescriptor.WARNING_MESSAGE
    );
    DialogDisplayer.getDefault().notify(confirm);

    return confirm.getValue() == NotifyDescriptor.YES_OPTION;
  }

  public static boolean printConfirmation(String message) {
    return NotifyDescriptor.YES_OPTION.equals(
      DialogDisplayer.getDefault().notify(
        new NotifyDescriptor.Confirmation(message, NotifyDescriptor.YES_NO_OPTION)));
  }

  public static void printInformation(String message) {
    print(message, NotifyDescriptor.INFORMATION_MESSAGE);
  }

  public static void printError(String message) {
    print(message, NotifyDescriptor.ERROR_MESSAGE);
  }

  private static void print(String message, int type) {
    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, type));
  }

  public static ImageIcon icon(Class clazz, String name) {
    if (name == null) {
      return null;
    }
    return new ImageIcon(clazz.getResource("image/"+ name +".gif")); // NOI18N
  }

  public static Node getSelectedNode() {
    Node [] nodes = getSelectedNodes();

    if (nodes == null) {
      return null;
    }
    return nodes [0];
  }

  public static Node [] getSelectedNodes() {
//out();
    TopComponent top = getActivateTopComponent();
//out("top: " + top);
    if (top == null) {
      return null;
    }
    Node [] nodes = top.getActivatedNodes();
//out("nodes: " + nodes);

    if (nodes == null || nodes.length == 0) {
      return null;
    }
    return nodes;
  }

  public static TopComponent getActivateTopComponent() {
    return TopComponent.getRegistry().getActivated();
  }
  
  public static void setWidth(JComponent component, int width) {
    setDimension(component, new Dimension(width, component.getPreferredSize().height));
  }

  public static void setHeight(JComponent component, int height) {
    setDimension(component, new Dimension(component.getPreferredSize().width, height));
  }

  private static void setDimension(JComponent component, Dimension dimension) {
    component.setMinimumSize(dimension);
    component.setPreferredSize(dimension);
  }

  public static int getInt(String value) {
    try {
      return Integer.parseInt(value);
    }
    catch (NumberFormatException e) {
      return -1;
    }
  }

  public static int round(double value) {
    return (int) Math.ceil(value);
  }

  public static String replace(String source, String searchFor, String replaceWith) {
    if (source == null) {
      return null;
    }
    if (searchFor == null || searchFor.length() == 0) {
      return null;
    }
    int k = 0;
    int found = source.indexOf(searchFor, k);
    StringBuffer buffer = new StringBuffer();

    while (true) {
      if (found == -1) {
        break;
      }
      buffer.append(source.substring(k, found));
      buffer.append(replaceWith);

      k = found + searchFor.length();
      found = source.indexOf(searchFor, k);
    }
    if (k > 0) {
        buffer.append(source.substring(k));
        return buffer.toString();
    }
    else {
      return source;
    }
  }

  public static DataObject getDataObject(Node node) {
    if (node == null) {
      return null;
    }
    return (DataObject) node.getLookup().lookup(DataObject.class);
  }

  public static void setImageSize(JButton button) {
    final Dimension IMAGE_BUTTON_SIZE = new Dimension(24, 24);
    button.setMaximumSize(IMAGE_BUTTON_SIZE);
    button.setMinimumSize(IMAGE_BUTTON_SIZE);
    button.setPreferredSize(IMAGE_BUTTON_SIZE);
  }

  public static JComponent getResizable(JPanel panel) {
    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    c.weightx = 1.0;
    c.weighty = 1.0;
    c.insets = new Insets(TINY_INSET, MEDIUM_INSET, 0, MEDIUM_INSET);
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    p.add(panel, c);

    return p;
  }

  public static void startTimeln() {
    tim();
    startTime();
  }
  public static void startTime() {
    ourTimes.push(System.currentTimeMillis());
  }

  public static void endTime(Object object) {
    long currentTime = System.currentTimeMillis();
    tim(object + ": " + ((currentTime - ourTimes.pop()) / MILLIS) + " sec."); // NOI18N
  }

  public static void tim() {
    if (ENABLE_TIM) {
      System.out.println();
    }
  }

  public static void tim(Object object) {
    if (ENABLE_TIM) {
      System.out.println("*** " + object); // NOI18N
    }
  }
  
  public static void log() {
    if (ENABLE_LOG) {
      System.out.println();
    }
  }

  public static void log(Object object) {
    if (ENABLE_LOG) {
      System.out.println("*** " + object); // NOI18N
    }
  }

  public static void stackTrace() {
    stackTrace(null);
  }

  public static void stackTrace(Object object) {
    out();
    out();

    if (object != null) {
      out(object);
    }
    new Exception("!!!").printStackTrace(); // NOI18N
  }

  public static void out() {
    if (ENABLE_OUT) {
      System.out.println();
    }
  }

  public static void out(Object object) {
    if (ENABLE_OUT) {
      System.out.println("*** " + object); // NOI18N
    }
  }

  // -------------------------------------------------------------
  public abstract static class IconAction extends AbstractAction {

    protected IconAction(String name, String toolTip, Icon icon) {
      super(name, icon);
      putValue(SHORT_DESCRIPTION, toolTip);
    }
  }

  // ---------------------------------------------------------------
  public abstract static class ButtonAction extends AbstractAction {

    public ButtonAction(String text, String toolTip) {
      this(text, null, toolTip);
    }
         
    public ButtonAction(Icon icon, String toolTip) {
      this(null, icon, toolTip);
    }

    public ButtonAction(String text) {
      this(text, null, text);
    }

    private ButtonAction(String text, Icon icon, String toolTip) {
      super(text, icon);
      putValue(SHORT_DESCRIPTION, toolTip);
    }
  }

  // --------------------------------------------------------
  public abstract static class Dialog extends WindowAdapter {

    protected void opened()  {}
    protected void resized() {}
    protected void updated() {}
   
    protected abstract DialogDescriptor createDescriptor();

    public void show() {
      show(true);
    }

    public void showAndWait() {
      show(false);
    }

    private void show(boolean inSwingThread) {
      if (myDialog == null) {
        myDialog = DialogDisplayer.getDefault().createDialog(createDescriptor());
        myDialog.addWindowListener(this);
        setCorner();
        myDialog.addComponentListener(
          new ComponentAdapter() {
            public void componentResized(ComponentEvent event) {
              resized();
            }
          }
        );
      }
      else {
        opened();
      }
      updated();

      if (inSwingThread) {
        SwingUtilities.invokeLater(new Runnable() { public void run() {
          myDialog.pack();
          myDialog.setVisible(true);
        }});
      }
      else {
        myDialog.pack();
        myDialog.setVisible(true);
      }
    }

    public Component getUIComponent() {
      return myDialog;
    }

    @Override
    public void windowOpened(WindowEvent event)
    {
      opened();
    }

    protected final String i18n(String key) {
      return UI.i18n(getClass(), key);
    }

    protected final String i18n(String key, String param) {
      return UI.i18n(getClass(), key, param);
    }

    private void setCorner() {
      if (myDialog instanceof JDialog) {
        ((JDialog) myDialog).getRootPane().setBorder(CORNER_BORDER);
      }
    }
  
    private java.awt.Dialog myDialog;
  }

  // ----------------------------------------------------------
  private static final class CornerBorder extends EmptyBorder {

    public CornerBorder() {
      super(0, SMALL_INSET, SMALL_INSET, SMALL_INSET);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
    {
      CORNER.paintIcon(c, g, w - CORNER.getIconWidth(), h - CORNER.getIconHeight());
    }

    private static final Icon CORNER = new ImageIcon(new byte [] {
      (byte)0x47,(byte)0x49,(byte)0x46,(byte)0x38,(byte)0x39,(byte)0x61,(byte)0x0c,
      (byte)0x00,(byte)0x0c,(byte)0x00,(byte)0xf7,(byte)0x00,(byte)0x00,(byte)0x83,
      (byte)0x83,(byte)0x83,(byte)0xd3,(byte)0xd3,(byte)0xc8,(byte)0xfd,(byte)0xfd,
      (byte)0xfd,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
      (byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x21,(byte)0xf9,(byte)0x04,
      (byte)0x01,(byte)0x00,(byte)0x00,(byte)0xff,(byte)0x00,(byte)0x2c,(byte)0x00,
      (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x0c,(byte)0x00,(byte)0x0c,(byte)0x00,
      (byte)0x40,(byte)0x08,(byte)0x34,(byte)0x00,(byte)0xff,(byte)0x09,(byte)0x1c,
      (byte)0x48,(byte)0xf0,(byte)0x9f,(byte)0x80,(byte)0x81,(byte)0x02,(byte)0x00,
      (byte)0x00,(byte)0x30,(byte)0xa8,(byte)0xd0,(byte)0x60,(byte)0x41,(byte)0x81,
      (byte)0x09,(byte)0x17,(byte)0x1e,(byte)0x7c,(byte)0x08,(byte)0xb1,(byte)0x21,
      (byte)0xc1,(byte)0x88,(byte)0x0c,(byte)0x25,(byte)0x36,(byte)0xc4,(byte)0x88,
      (byte)0x91,(byte)0x62,(byte)0x45,(byte)0x8f,(byte)0x1d,(byte)0x0b,(byte)0x72,
      (byte)0x5c,(byte)0x88,(byte)0x70,(byte)0xa3,(byte)0xc5,(byte)0x8c,(byte)0x28,
      (byte)0x13,(byte)0x8e,(byte)0xd4,(byte)0xb8,(byte)0x30,(byte)0x20,(byte)0x00,
      (byte)0x3b
    });
  }

  private static Stack<Long> ourTimes = new Stack<Long>();

  public static final int TINY_INSET = 2;
  public static final int SMALL_INSET = 7;
  public static final int MEDIUM_INSET = 11;

  private static final double MILLIS = 1000.0;

  public static final String UH = System.getProperty("user.home"); // NOI18N
  public static final String LS = System.getProperty("line.separator"); // NOI18N
  public static final String FS = System.getProperty("file.separator"); // NOI18N

  private static final Border CORNER_BORDER = new CornerBorder();

  private static final boolean ENABLE_LOG =
    System.getProperty("org.netbeans.modules.log") != null; // NOI18N
  
  private static final boolean ENABLE_OUT =
    System.getProperty("org.netbeans.modules.out") != null; // NOI18N

  private static final boolean ENABLE_TIM =
    System.getProperty("org.netbeans.modules.tim") != null; // NOI18N
}
