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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JComponent;

import org.netbeans.modules.print.spi.PrintPage;
import org.netbeans.modules.print.impl.util.Util;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.14
 */
final class Paper extends JComponent {

  Paper (
    PrintPage page,
    int number,
    int count,
    boolean useRow,
    boolean useColumn) 
  {
    myPage = page;
    myNumber = number;
    myCount = String.valueOf(count);
    String row = String.valueOf(getRow() + 1);
    String column = String.valueOf(getColumn() + 1);

    if (useRow && useColumn) {
      myPageNumber = row + "." + column; // NOI18N
    }
    else if ( !useRow && useColumn) {
      myPageNumber = column;
    }
    else {
      myPageNumber = row;
    }
  }

  void setInfo (
    String name,
    Date modified,
    double scale,
    Option option)
  {
    myName = name;
    myLastModifiedDate = modified;

    myPaperWidth = option.getPaperWidth();
    myPaperHeight = option.getPaperHeight();
    myPageX = option.getPageX();
    myPageY = option.getPageY();
    myPageWidth = option.getPageWidth();
    myPageHeight = option.getPageHeight();

    myHeaderY = option.getHeaderY();
    myHasHeader = option.hasHeader();
    myHeaderLeft = expandTitle(option.getHeaderLeft());
    myHeaderCenter = expandTitle(option.getHeaderCenter());
    myHeaderRight = expandTitle(option.getHeaderRight());
    myHeaderColor = option.getHeaderColor();
    myHeaderFont = option.getHeaderFont();

    myFooterY = option.getFooterY();
    myHasFooter = option.hasFooter();
    myFooterLeft = expandTitle(option.getFooterLeft());
    myFooterCenter = expandTitle(option.getFooterCenter());
    myFooterRight = expandTitle(option.getFooterRight());
    myFooterColor = option.getFooterColor();
    myFooterFont = option.getFooterFont();

    myHasBorder = option.hasBorder();
    myBorderColor = option.getBorderColor();
    myIsPainting = true;

    setScale(scale);
  }

  int getRow() {
    return myPage.getRow();
  }

  int getColumn() {
    return myPage.getColumn();
  }

  void setScale(double scale) {
    myScale = scale;

    if (myIsPainting) {
      setPreferredSize(new Dimension (
        (int) Math.floor((myPaperWidth + SHADOW_WIDTH) * myScale),
        (int) Math.floor((myPaperHeight + SHADOW_WIDTH) * myScale)
      ));
    }
    else {
      setPreferredSize(new Dimension(myPaperWidth, myPaperHeight));
    }
  }

  int getPaperWidth() {
    return myPaperWidth + SHADOW_WIDTH;
  }

  int getPaperHeight() {
    return myPaperHeight + SHADOW_WIDTH;
  }

  @Override
  public void print(Graphics g)
  {
    myIsPainting = false;
    setScale(1.0);
    super.print(g);
    myIsPainting = true;
  }

  @Override
  public void paint(Graphics graphics)
  {
    Graphics2D g = Util.getGraphics(graphics);

    // scaling
    if (myIsPainting) {
      g.scale(myScale, myScale);
    }

    // background
    g.setColor(Color.white);
    g.fillRect(myPageX, myPageY, myPageWidth, myPageHeight);

    // page
    g.translate(myPageX, myPageY);
    myPage.print(g);
    g.translate(-myPageX, -myPageY);

    // horizontal margin
    g.setColor(Color.white);

    g.fillRect(
      0, 0,
      myPaperWidth, myPageY
    );
    
    g.fillRect(
      0, myPageY + myPageHeight,
      myPaperWidth, myPaperHeight
    );

    // header
    if (myHasHeader) {
      drawTitle(g,
        myHeaderLeft,
        myHeaderCenter,
        myHeaderRight,
        myHeaderY,
        myHeaderColor,
        myHeaderFont
      );
    }

    // footer
    if (myHasFooter) {
      drawTitle(g,
        myFooterLeft,
        myFooterCenter,
        myFooterRight,
        myFooterY,
        myFooterColor,
        myFooterFont
      );
    }

    // vertical margin
    g.setColor(Color.white);

    g.fillRect(
      0, 0,
      myPageX, myPaperHeight
    );
    
    g.fillRect(
      myPageX + myPageWidth, 0,
      myPaperWidth, myPaperHeight
    );
    
    // shadow
    if (myIsPainting) {
      g.setColor(Color.gray.darker());
      g.fillRect(
        myPaperWidth,
        SHADOW_WIDTH,
        SHADOW_WIDTH + 1,
        myPaperHeight
      );
      g.fillRect(
        SHADOW_WIDTH,
        myPaperHeight,
        myPaperWidth,
        SHADOW_WIDTH + 1
      );
      g.setColor(Color.lightGray);
      g.fillRect(myPaperWidth, 0, SHADOW_WIDTH + 1, SHADOW_WIDTH + 1);
      g.fillRect(0, myPaperHeight, SHADOW_WIDTH + 1, SHADOW_WIDTH + 1);
    }
    
    // box
    if (myIsPainting) {
      g.setColor(Color.black);
      g.drawRect(0, 0, myPaperWidth, myPaperHeight);
    }

    // border
    if (myHasBorder) {
      g.setColor(myBorderColor);
      g.drawRect(myPageX, myPageY, myPageWidth, myPageHeight);
    }

    // number
    if (myIsPainting) {
      g.setColor(NUMBER_FONT_COLOR);
      g.setFont(NUMBER_FONT_NAME);
      g.drawString(Integer.toString(myNumber), NUMBER_X, NUMBER_Y);
    }
  }

  private void drawTitle(
    Graphics2D g,
    String left,
    String center,
    String right,
    int y,
    Color color,
    Font f)
  {
    g.setColor(color);
    drawTitle(g, left,  myPageX, y, f);
    drawTitle(g, center,myPageX + (myPageWidth - getWidth(center, f))/2, y, f);
    drawTitle(g, right, myPageX + myPageWidth - getWidth(right, f), y, f);
  }

  private void drawTitle(
    Graphics2D g,
    String text,
    int x,
    int y,
    Font font)
  {
    g.setFont(font);
    g.drawString(text, x, y);
  }

  private String expandTitle(String title) {
    String t = title;
    Date printed = new Date(System.currentTimeMillis());

    t = Util.replace(t, Pattern.NAME.getName(), myName);
    t = Util.replace(t, Pattern.PAGE.getName(), myPageNumber);
    t = Util.replace(t, Pattern.USER.getName(), USER_NAME);
    t = Util.replace(t, Pattern.COUNT.getName(), myCount);
    t = Util.replace(t, Pattern.MODIFIED_DATE.getName(),getDate(myLastModifiedDate));
    t = Util.replace(t, Pattern.MODIFIED_TIME.getName(),getTime(myLastModifiedDate));
    t = Util.replace(t, Pattern.PRINTED_DATE.getName(), getDate(printed));
    t = Util.replace(t, Pattern.PRINTED_TIME.getName(), getTime(printed));

    return t;
  }

  private int getWidth(String text, Font font) {
    return (int) Math.ceil(font.getStringBounds(
      text, Util.FONT_RENDER_CONTEXT).getWidth());
  }

  private String getDate(Date timestamp) {
    return getTimestamp(timestamp, "yyyy.MM.dd"); // NOI18N
  }

  private String getTime(Date timestamp) {
    return getTimestamp(timestamp, "HH:mm:ss"); // NOI18N
  }

  private String getTimestamp(Date timestamp, String format) {
    return new SimpleDateFormat(format).format(timestamp);
  }

  private int myNumber;
  private double myScale;
  private PrintPage myPage;
  private boolean myIsPainting;

  private int myPaperWidth;
  private int myPaperHeight;
  private int myPageX;
  private int myPageY;
  private int myPageWidth;
  private int myPageHeight;

  private int myHeaderY;
  private boolean myHasHeader;
  private String myHeaderLeft;
  private String myHeaderCenter;
  private String myHeaderRight;
  private Color myHeaderColor;
  private Font myHeaderFont;

  private int myFooterY;
  private boolean myHasFooter;
  private String myFooterLeft;
  private String myFooterCenter;
  private String myFooterRight;
  private Color myFooterColor;
  private Font myFooterFont;

  private boolean myHasBorder;
  private Color myBorderColor;
  private String myName;
  private String myCount;
  private String myPageNumber;
  private Date myLastModifiedDate;

  private static final int NUMBER_FONT_SIZE = 35;
  private static final int SHADOW_WIDTH = 10; // .pt
  private static final int NUMBER_X = (int) Math.round(NUMBER_FONT_SIZE * 1.0);
  private static final int NUMBER_Y = (int) Math.round(NUMBER_FONT_SIZE * 1.5);
  private static final Color NUMBER_FONT_COLOR = new Color(125, 125, 255);
  private static final String USER_NAME = System.getProperty("user.name"); // NOI18N
  private static final Font NUMBER_FONT_NAME =
    new Font("Serif", Font.BOLD, NUMBER_FONT_SIZE); // NOI18N
}
