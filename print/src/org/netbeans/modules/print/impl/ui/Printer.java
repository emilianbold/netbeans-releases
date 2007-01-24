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

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import static org.netbeans.modules.print.api.PrintUtil.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.21
 */
final class Printer implements Printable {

  void print(Paper[] papers, Option option) {
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    myPapers = papers;
//out("SET PAPER: " + myPapers);

    if (printerJob == null) {
      return;
    }
    printerJob.setPrintable(this, option.getPageFormat());
    
    try {
      if (printerJob.printDialog()) {
        printerJob.print();
      }
    }
    catch (PrinterException e) {
      String msg = NbBundle.getMessage(
        Printer.class, "ERR_Printer_Problem", e.getLocalizedMessage()); // NOI18N
      ErrorManager.getDefault().annotate(e, msg);
      ErrorManager.getDefault().notify(ErrorManager.USER, e);
    }
    myPapers = null;
  }

  public int print(
    Graphics g,
    PageFormat pageFormat,
    int index) throws PrinterException 
  {
//out("PAPER IS: " + myPapers.length);
    if (index == myPapers.length) {
      return NO_SUCH_PAGE;
    }
//out("  print: " + index);
    myPapers[index].print(g);
  
    return PAGE_EXISTS;
  }

  private Paper[] myPapers;
}
