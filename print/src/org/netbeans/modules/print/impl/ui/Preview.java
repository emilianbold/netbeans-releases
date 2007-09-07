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
package org.netbeans.modules.print.impl.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.openide.DialogDescriptor;
import org.netbeans.modules.print.spi.PrintPage;
import org.netbeans.modules.print.spi.PrintProvider;
import org.netbeans.modules.print.ui.PrintUI;
import org.netbeans.modules.print.impl.util.Util;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.14
 */
public class Preview extends PrintUI implements Percent.Listener {

  /**{@inheritDoc}*/
  public Preview() {
    myOption = new Option();
    myPrinter = new Printer();
    myKeyListener = new KeyAdapter() {
      public void keyPressed(KeyEvent event) {
        char ch = event.getKeyChar();

        if (ch == '+' || ch == '=') {
          myScale.increaseValue();
        }
        else if (ch == '-' || ch == '_') {
          myScale.decreaseValue();
        }
        else if (ch == '/') {
          myScale.normalValue();
        }
        else if (ch == '*') {
          showCustom(true);
        }
      }
    };
  }

  /**{@inheritDoc}*/
  public void print(PrintProvider provider, boolean withPreview) {
    assert provider != null : "Print provider can't be null"; // NOI18N
//out();
//out("Do action");
    myPrintProvider = provider;

    if (withPreview) {
      show();
    }
    else {
      print(true);
    }
  }

  private JPanel createPanel() {
//out("Create Main panel");
    JPanel p = new JPanel(new GridBagLayout());
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // navigate
    c.anchor = GridBagConstraints.WEST;
    p.add(createNavigatePanel(), c);

    // scale
    c.weightx = 1.0;
    c.weighty = 0.0;
    p.add(createScalePanel(), c);

    // toggle
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(TINY_INSET, MEDIUM_INSET, TINY_INSET, MEDIUM_INSET);
    myToggle = createToggleButton(
      new ButtonAction(icon(Util.class, "toggle"), i18n("TLT_Toggle")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          toggle();
        }
      }
    );
    myToggle.addKeyListener(myKeyListener);
    p.add(myToggle, c);

    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(MEDIUM_INSET, 0, MEDIUM_INSET, 0);
    panel.add(p, c);

    // scroll
    c.gridy++;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(0, 0, 0, 0);
    panel.add(createScrollPanel(), c);

    toggle();

    return panel;
  }

  /**{@inheritDoc}*/
  @Override
  protected void updated()
  {
//out("Update content");
    createPapers();
    toggle();
  }

  private void toggle() {
    updatePaperNumber();
    addPapers();
    updateButtons();
  }

  private JComponent createNavigatePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // first
    panel = new JPanel(new GridBagLayout());
    c.insets = new Insets(TINY_INSET, TINY_INSET, TINY_INSET, TINY_INSET);
    myFirst = createButton(
      new ButtonAction(icon(Util.class, "first"), i18n("TLT_First")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          first();
        }
      }
    );
    myFirst.addKeyListener(myKeyListener);
    panel.add(myFirst, c);

    // previous
    myPrevious = createButton(
      new ButtonAction(
        icon(Util.class, "previous"), // NOI18N
        i18n("TLT_Previous")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          previous();
        }
      }
    );
    myPrevious.addKeyListener(myKeyListener);
    panel.add(myPrevious, c);

    // text field
    myGoto = new JTextField();
    int width = (int)Math.round(myPrevious.getPreferredSize().width/PREVIEW_FACTOR);
    int height = myPrevious.getPreferredSize().height;
    myGoto.setPreferredSize(new Dimension(width, height));
    myGoto.setMinimumSize(new Dimension(width, height));

    InputMap inputMap = myGoto.getInputMap();
    ActionMap actionMap = myGoto.getActionMap();

    inputMap.put(KeyStroke.getKeyStroke('+'), INCREASE);
    inputMap.put(KeyStroke.getKeyStroke('='), INCREASE);
    inputMap.put(KeyStroke.getKeyStroke('-'), DECREASE);
    inputMap.put(KeyStroke.getKeyStroke('_'), DECREASE);
    
    actionMap.put(INCREASE, new AbstractAction() {
      public void actionPerformed(ActionEvent event) {
        next();
      }
    });
    actionMap.put(DECREASE, new AbstractAction() {
      public void actionPerformed(ActionEvent event) {
        previous();
      }
    });
    myGoto.setHorizontalAlignment(JTextField.CENTER);
    myGoto.setToolTipText(i18n("TLT_Goto")); // NOI18N
    myGoto.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        goTo();
      }
    });
    panel.add(myGoto, c);
    
    // next
    myNext = createButton(
      new ButtonAction(icon(Util.class, "next"), i18n("TLT_Next")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          next();
        }
      }
    );
    myNext.addKeyListener(myKeyListener);
    panel.add(myNext, c);

    // last
    myLast = createButton(
      new ButtonAction(icon(Util.class, "last"), i18n("TLT_Last")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          last();
        }
      }
    );
    myLast.addKeyListener(myKeyListener);
    panel.add(myLast, c);

    return panel;
  }

  private JComponent createScalePanel() {
//out("Create scale panel");
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // fit to window
    c.insets = new Insets(TINY_INSET, MEDIUM_INSET, TINY_INSET, TINY_INSET);
    myFit = createButton(
      new ButtonAction(icon(Util.class, "fit"), i18n("TLT_Fit")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          showCustom(true);
        }
      }
    );
    myFit.addKeyListener(myKeyListener);
    panel.add(myFit, c);

    // scale
    c.insets = new Insets(TINY_INSET, TINY_INSET, TINY_INSET, TINY_INSET);
    myScale = new Percent(
      this,
      myOption.getScale(),
      PERCENTS,
      CUSTOMS.length - 1,
      CUSTOMS,
      i18n("TLT_Preview_Scale") // NOI18N
    );
    int width = myScale.getPreferredSize().width;
    int height = myPrevious.getPreferredSize().height;
    myScale.setPreferredSize(new Dimension(width, height));
    myScale.setMinimumSize(new Dimension(width, height));
    panel.add(myScale, c);
    
    // decrease
    myDecrease = createButton(
      new ButtonAction(icon(Util.class, "minus"), i18n("TLT_Zoom_Out")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myScale.decreaseValue();
        }
      }
    );
    myDecrease.addKeyListener(myKeyListener);
    panel.add(myDecrease, c);

    // increase
    myIncrease = createButton(
      new ButtonAction(icon(Util.class, "plus"), i18n("TLT_Zoom_In")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          myScale.increaseValue();
        }
      }
    );
    myIncrease.addKeyListener(myKeyListener);
    panel.add(myIncrease, c);

    return panel;
  }

  private JComponent createScrollPanel() {
//out("Create scroll panel");
    GridBagConstraints c = new GridBagConstraints();

    // papers
    myPaperPanel = new JPanel(new GridBagLayout());
    myPaperPanel.setBackground(Color.lightGray);
    JPanel panel = new JPanel(new GridBagLayout());

    c.gridy = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.insets = new Insets(0, 0, 0, 0);
    panel.setBackground(Color.lightGray);
    panel.add(myPaperPanel, c);
//  panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.yellow));
//  optionPanel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.green));
//  myPaperPanel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.green));

    // scroll
    c.fill = GridBagConstraints.BOTH;
    myScrollPanel = new MyScrollPane(panel);
    myScrollPanel.setFocusable(true);

    myScrollPanel.addWheelListener(new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent event) {
        if (SwingUtilities.isRightMouseButton(event) || event.isControlDown()) {
          myScrollPanel.setWheelScrollingEnabled(false);

          if (event.getWheelRotation() > 0) {
            myScale.increaseValue();
          }
          else {
            myScale.decreaseValue();
          }
        }
        else {
          myScrollPanel.setWheelScrollingEnabled(true);
        }
      }
    });
    myScrollPanel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
          if (SwingUtilities.isRightMouseButton(event)) {
            myScale.customValue(CUSTOMS.length - 1);
            myCustomIndex = 0;
          }
          else {
            showCustom(true);
          }
        }
      }
    });
    myScrollPanel.addKeyListener(myKeyListener);

    return myScrollPanel;
  }

  private void showCustom(boolean doNext) {
    if (doNext) {
      myCustomIndex++;

      if (myCustomIndex == CUSTOMS.length) {
        myCustomIndex = 0;
      }
    }
    myScale.customValue(myCustomIndex);
  }

  private void updateButtons() {
    myGoto.setText(getPaper(myPaperNumber));
    myFirst.setEnabled(myPaperNumber > 1);
    myPrevious.setEnabled(myPaperNumber > 1);
    myNext.setEnabled(myPaperNumber < getPaperCount());
    myLast.setEnabled(myPaperNumber < getPaperCount());
    boolean enabled = getPaperCount() > 0;
    myGoto.setEnabled(enabled);
    myScale.setEnabled(enabled);
    myToggle.setEnabled(enabled);
    myFit.setEnabled(enabled);
    myIncrease.setEnabled(enabled);
    myDecrease.setEnabled(enabled);
    myPrintButton.setEnabled(enabled);
    myOptionButton.setEnabled(enabled);
  }

  private void scrollTo() {
//out("Scroll to: " + myPaperNumber);
    Paper paper = myPapers [myPaperNumber - 1];
    int gap  = getGap();
    int x = paper.getX() - gap;
    int y = paper.getY() - gap;
    int w = paper.getWidth();
    int h = paper.getHeight();
    JViewport view = myScrollPanel.getViewport();

    if ( !view.getViewRect().contains(x, y, w, h)) {
      view.setViewPosition(new Point(x,y));
      updatePaperPanel();
    }
  }

  /**{@inheritDoc}*/
  public double getCustomValue(int index) {
    if (getPaperCount() == 0) {
      return 0.0;
    }
    int width = myPapers [0].getPaperWidth() + GAP_SIZE;
    int height = myPapers [0].getPaperHeight() + GAP_SIZE;

    if (index == 0) {
      return getWidthScale(width);
    }
    if (index == 1) {
      return getHeightScale(height);
    }
    if (index == 2) {
      return getAllScale(width, height);
    }
    return 1.0;
  }

  private double getWidthScale(int width) {
    final int JAVA_INSET = 5;
    
    double scrollWidth = (double) (myScrollPanel.getWidth() -
      myScrollPanel.getVerticalScrollBar().getWidth() - JAVA_INSET);

    return scrollWidth / width;
  }

  private double getHeightScale(int height) {
    final int JAVA_INSET = 5;
    
    double scrollHeight = (double) (myScrollPanel.getHeight() -
      myScrollPanel.getHorizontalScrollBar().getHeight() - JAVA_INSET);

    return scrollHeight / height;
  }

  private double getAllScale(int width, int height) {
    int w = width;
    int h = height;

    if ( !isSingleMode()) {
      int maxRow = 0;
      int maxColumn = 0;

      for (Paper paper : myPapers) {
        maxRow = Math.max(maxRow, paper.getRow());
        maxColumn = Math.max(maxColumn, paper.getColumn());
      }
      w *= maxColumn + 1;
      h *= maxRow + 1;
    }
    return Math.min(getWidthScale(w), getHeightScale(h));
  }

  /**{@inheritDoc}*/
  public void valueChanged(double value, int index) {
//out();
//out("Set scale: " + value + " " + index);
    if (index != -1) {
      myCustomIndex = index;
    }
    if (getPaperCount() == 0) {
      return;
    }
    for (Paper paper : myPapers) {
      paper.setScale(value);
    }
    addPapers();
  }

  private void addPapers() {
//out("Add papers");
    myPaperPanel.removeAll();

    if (getPaperCount() == 0) {
      updatePaperPanel();
      return;
    }
    int gap = getGap();
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(gap, gap, 0, 0);

    if (isSingleMode()) {
      myPaperPanel.add(myPapers [myPaperNumber - 1], c);
    }
    else {
      for (Paper paper : myPapers) {
        c.gridx = paper.getColumn();
        c.gridy = paper.getRow();
        myPaperPanel.add(paper, c);
      }
    }
    updatePaperPanel();
  }

  private void updatePaperPanel() {
    myPaperPanel.revalidate();
    myPaperPanel.repaint();
  }

  private void createPapers() {
    PrintPage [] pages = myPrintProvider.getPages(myOption);
//out("Create papers: " + pages.length);
    myPapers = null;

    if (pages == null) {
      return;
    }
    myPapers = new Paper [pages.length];
    boolean useRow = false;
    boolean useColumn = false;

    for (int i=0; i < pages.length; i++) {
      PrintPage page = pages [i];

      if (page.getRow() > 0) {
        useRow = true;
      }
      if (page.getColumn() > 0) {
        useColumn = true;
      }
    }
    String name = myPrintProvider.getName();
    
    if (name == null) {
      name = ""; // NOI18N
    }
    Date modified = myPrintProvider.getLastModifiedDate();

    if (modified == null) {
      modified = new Date(System.currentTimeMillis());
    }
    double scale = 1.0;

    if (myScale != null) {
      scale = myScale.getValue();
    }
    for (int i=0; i < pages.length; i++) {
      myPapers [i] = new Paper(
        pages [i],
        i+1,
        pages.length,
        useRow,
        useColumn
      );
      myPapers [i].setInfo(
        name,
        modified,
        scale,
        myOption
      );
    }
  }

  /**{@inheritDoc}*/
  public void invalidValue(String value) {}

  private int getPaperCount() {
    if (myPapers == null) {
      return 0;
    }
    return myPapers.length;
  }

  private void first() {
    updatePaperNumber();
    changePaper();
  }

  private void previous() {
    if (myPaperNumber == 1) {
      return;
    }
    myPaperNumber--;
    changePaper();
  }

  private void next() {
    if (myPaperNumber == getPaperCount()) {
      return;
    }
    myPaperNumber++;
    changePaper();
  }

  private void last() {
    myPaperNumber = getPaperCount();
    changePaper();
  }

  private void goTo() {
    String value = myGoto.getText();
    int number = getPaperNumber(value);
    int count = getPaperCount();

    if (number < 1 || number > count) {
      myGoto.setText(getPaper(myPaperNumber));
    }
    else {
      myPaperNumber = number;
      changePaper();
    }
    myGoto.selectAll();
  }

  private void changePaper() {
    if (isSingleMode()) {
      addPapers();
    }
    else {
      scrollTo();
    }
    updateButtons();
  }

  private void updatePaperNumber() {
    myPaperNumber = getPaperCount() == 0 ? 0 : 1;
  }

  private int getGap() {
    return (int) Math.round(GAP_SIZE * myScale.getValue());
  }

  private String getPaper(int value) {
    return Util.getPageOfCount(
      String.valueOf(value), String.valueOf(getPaperCount()));
  }
      
  @Override
  protected DialogDescriptor createDescriptor()
  {
    Object [] buttons = getButtons();
    DialogDescriptor descriptor = new DialogDescriptor(
      getResizable(createPanel()),
      i18n("LBL_Print_Preview"), // NOI18N
      true,
      buttons,
      myPrintButton,
      DialogDescriptor.DEFAULT_ALIGN,
      null,
      null
    );
    descriptor.setClosingOptions(
      new Object [] { myPrintButton, myCloseButton });

    return descriptor;
  }

  @Override
  protected void closed()
  {
//out("Closed");
    myPapers = null;
    myPrintProvider = null;
    myOption.setScale(myScale.getValue());
  }

  @Override
  protected void opened()
  {
//out("Opened");
    myScrollPanel.requestFocus();
  }

  @Override
  protected void resized()
  {
    if (myScale.isCustomValue()) {
      showCustom(false);
    }
  }

  private Object [] getButtons() {
    myPrintButton = createButton(
      new ButtonAction(
        i18n("LBL_Print_Button"), // NOI18N
        i18n("TLT_Print_Button")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          print(false);
        }
      }
    );
    myPrintButton.addKeyListener(myKeyListener);

    myOptionButton = createButton(
      new ButtonAction(
        i18n("LBL_Option_Button"), // NOI18N
        i18n("TLT_Option_Button")) { // NOI18N
        public void actionPerformed(ActionEvent event) {
          option();
        }
      }
    );
    myOptionButton.addKeyListener(myKeyListener);

    myCloseButton = createButton(
      new ButtonAction(
        i18n("LBL_Close_Button"), // NOI18N
        i18n("TLT_Close_Button")) { // NOI18N
        public void actionPerformed(ActionEvent event) {}
      }
    );
    myCloseButton.addKeyListener(myKeyListener);

    return new Object [] {
      myPrintButton,
      myOptionButton,
      myCloseButton,
    };
  }

  private boolean isSingleMode() {
    return myToggle.isSelected();
  }

  private void print(boolean doUpdate) {
    if (doUpdate) {
      createPapers();
    }
    myPrinter.print(myPapers);
  }

  private void option() {
    new Attribute(this, myOption).show();
  }

  private int getPaperNumber(String text) {
    String value = text.trim();
    StringBuffer buffer = new StringBuffer();

    for (int i=0; i < value.length(); i++) {
      char c = value.charAt(i);

      if ( !isAplha(c)) {
        break;
      }
      buffer.append(c);
    }
    return Util.getInt(buffer.toString());
  }

  private boolean isAplha(char c) {
    return "0123456789".indexOf(c) != -1; // NOI18N
  }

  // ----------------------------------------------------------
  private static final class MyScrollPane extends JScrollPane {
    MyScrollPane(JPanel panel) {
      super(
        panel,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
      );
      getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);

      int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
      int height = (int) Math.round(screenHeight * PREVIEW_FACTOR);
      int width = (int) Math.round(height * PREVIEW_FACTOR);

      Dimension dimension = new Dimension(width, height);
      setMinimumSize(dimension);
      setPreferredSize(dimension);
    }

    public void addMouseWheelListener(MouseWheelListener listener) {
      if (myMouseWheelListeners == null) {
        myMouseWheelListeners = new ArrayList<MouseWheelListener>();
      }
//out("Listener: " + listener.getClass().getName());
      myMouseWheelListeners.add(listener);
    }

    public void addWheelListener(MouseWheelListener wheelListener) {
      super.addMouseWheelListener(wheelListener);

      for (int i=0; i < myMouseWheelListeners.size(); i++) {
        super.addMouseWheelListener(myMouseWheelListeners.get(i));
      }
    }

    private List<MouseWheelListener> myMouseWheelListeners;
  }

  @Override
  protected final String i18n(String key)
  {
    return i18n(Preview.class, key);
  }

  private Paper [] myPapers;
  private JPanel myPaperPanel;
  
  private JButton myFirst;
  private JButton myPrevious;
  private JButton myNext;
  private JButton myLast;

  private JButton myFit;
  private JButton myIncrease;
  private JButton myDecrease;

  private JButton myPrintButton;
  private JButton myOptionButton;
  private JButton myCloseButton;

  private Option myOption;
  private Percent myScale;
  private JTextField myGoto;
  private int myPaperNumber;
  private int myCustomIndex;
  private JToggleButton myToggle;
  private MyScrollPane myScrollPanel;
  private KeyListener myKeyListener;

  private Printer myPrinter;
  private PrintProvider myPrintProvider;

  private static final int GAP_SIZE = 20;
  private static final int SCROLL_INCREMENT = 40;
  private static final double PREVIEW_FACTOR = 0.75;
  private static final int [] PERCENTS = new int [] { 25, 50, 75, 100, 200, 400 };

  private static final String INCREASE = "increase"; // NOI18N
  private static final String DECREASE = "decrease"; // NOI18N
  
  private final String [] CUSTOMS = new String [] {
    i18n("LBL_Fit_to_Width"), // NOI18N
    i18n("LBL_Fit_to_Height"), // NOI18N
    i18n("LBL_Fit_to_All"), // NOI18N
  };
}
