/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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



package org.netbeans.modules.uml.ui.swing.drawingarea;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import com.tomsawyer.editor.export.TSEPrint;
import com.tomsawyer.editor.export.TSEPrintPreviewWindow;
import com.tomsawyer.editor.export.TSEPrintSetup;
import com.tomsawyer.editor.export.TSEPrintSetupDialog;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;

/*
 * 
 * @author KevinM
 *
 * Print driver.
 */
public class ADDrawingAreaPrinter
{
   protected ADGraphWindow m_graphWindow;
   protected TSEPrintSetup m_printSetup;
   protected ADDrawingAreaResourceBundle m_resources;

   public ADDrawingAreaPrinter(ADGraphWindow graphWindow, ADDrawingAreaResourceBundle resources)
   {
      m_graphWindow = graphWindow;
      m_printSetup = null;
      m_resources = resources;
   }

   protected Frame getOwnerFrame()
   {		
		IProxyUserInterface ui = ProductHelper.getProxyUserInterface();
		if (ui != null)
			return ui.getWindowHandle();
		else  
			return JOptionPane.getFrameForComponent(SwingUtilities.getRootPane((ADDrawingAreaControl) getGraphWindow().getDrawingArea()));
   }

   public boolean print(boolean showDialog)
   {
       //Jyothi: Fix Bug#6274109
//      JFrame frame = new JFrame(); 
//      frame.setBounds(0, 0, getGraphWindow().getWidth(), getGraphWindow().getHeight());
//
//      final TSEPrint print = new TSEPrint(frame, this.getPrintSetup());
        final TSEPrint print = new TSEPrint(this.getOwnerFrame(), this.getPrintSetup());

       
      // there is a repainting bug when swing components are mixed
      // with a heavy weight component, like the native print
      // dialog: the components behind the print dialog will not
      // be refreshed when one moves the print dialog around. To
      // work around this Swing problem, we have to place the
      // printing code on a separate thread, so the Swing thread
      // can proceed with the normal repainting.

      Runnable runnableImpl = new Runnable()
      {
         public void run()
         {            
             //Jyothi: Fix Bug#6274109 
             //IDE is stale and is not geting the control back after TS print.execute.. so doing it in a seperate thread
             Runnable runnableTSPrint = new Runnable()
             {
                 public void run()
                 {
                    print.execute();
                 }
             };       
             new Thread(runnableTSPrint).start();
            ADDrawingAreaPrinter.this.getOwnerFrame().setEnabled(true);            
            ADDrawingAreaPrinter.this.getOwnerFrame().setVisible(true);
         }
      };

      Thread printThread = new Thread(runnableImpl);
      this.getOwnerFrame().setEnabled(false);
      printThread.start();

      return true;
   }

   /*
    * Display the Tom Sawyer print setup (Marins, Just Selected...)
    */
   public boolean printSetup()
   {
      JDialog printSetupDialog = new TSEPrintSetupDialog(this.getOwnerFrame(), this.getPrintSetupTitle(), this.getPrintSetup());

      printSetupDialog.setModal(true);
      printSetupDialog.setVisible(true);
      return true;
   }

	/*
	 * Returns the graphWindow being printed or previewed.
	 */
   public ADGraphWindow getGraphWindow()
   {
      return m_graphWindow;
   }

   /*
    * Used to the TSEPrint class.
    */
   public TSEPrintSetup getPrintSetup()
   {
      if (m_printSetup == null)
      {
         m_printSetup = new TSEPrintSetup(getGraphWindow());
      }
      else if (this.m_printSetup.getGraphWindow() != this.getGraphWindow())
      {
         this.m_printSetup.setGraphWindow(this.getGraphWindow());
      }

      return m_printSetup;
   }

	/*
	 *
	 * @author KevinM
	 *
	 * Override the TSEPrintPreviewWindow so we can display the print dialog modal, without the painting problems.	 
	 */
   protected class ADPrintPreviewWindow extends TSEPrintPreviewWindow
   {
      ADPrintPreviewWindow()
      {
         super(ProductHelper.getProxyUserInterface().getWindowHandle(), getPrintPreviewTitle(), ADDrawingAreaPrinter.this.getPrintSetup());
         super.setBounds(0,0, 540,600);
         super.setLocationRelativeTo(null);
         super.printButton.setFocusPainted(true);
			super.printSetupButton.setFocusPainted(true);
			super.zoomInButton.setFocusPainted(true);
			super.zoomOutButton.setFocusPainted(true);
			super.closeButton.setFocusPainted(true);
			super.fitInButton.setFocusPainted(true);
                        //Jyothi: Fix for Bug#6323317
//			super.getRootPane().registerKeyboardAction
//			(
//				new ActionListener()
//				{
//					public void actionPerformed(ActionEvent evt)
//					{
//						hide();
//					}
//				},
//					  KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
//					  JComponent.WHEN_IN_FOCUSED_WINDOW
//			 );
                        //Jyothi:
                        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
                        Action escapeAction = new AbstractAction() {
                            public void actionPerformed(ActionEvent e) {                                
                                dispose();
                            }
                        };
                        super.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, "ESCAPE");
                        super.getRootPane().getActionMap().put("ESCAPE", escapeAction);
      }

		/*
		 *  (non-Javadoc)
		 * @see com.tomsawyer.editor.export.TSEPrintPreviewWindow#onPrint()
		 */
      protected void onPrint()
      {         
			//	 Override this so we can set the print dialog modal, without the painting problems.
         ADDrawingAreaPrinter.this.print(true);
      }
      //Jyothi: Fix for escape key to close the print setup dialog - instead of modifying TS jars - Provided by TomSawyer
      protected void onPrintSetup() {
          TSEPrintSetup printSetup = this.previewPane.getPrintSetup();
          
          ADPrintSetupDialog dialog =  new ADPrintSetupDialog(this, "Print Setup", printSetup);
          dialog.setVisible(true);
          
          // if the user pressed "cancel" to close the dialog,
          // don't do anything here.
          
          if (!dialog.getReturnValue()) {
              return;
          }
          
          this.previewPane.rebuildPreviewContainer(false);
          
          this.onFitIn();
          
          this.zoomTool.registerListeners();
          
          if (printSetup.getPageColumns() == 0 || printSetup.getPageRows() == 0) {
              this.printButton.setEnabled(false);
          } else {
              this.printButton.setEnabled(true);
          }
      }
   }

   /*
    * Displays the print preview window.
    */
   public boolean onPrintPreview()
   {
      ADPrintPreviewWindow printPreviewWindow = new ADPrintPreviewWindow();
      printPreviewWindow.setVisible(true);
      return true;
   }

   protected String getPrintPreviewTitle()
   {
      String title = m_resources.getString("dialog.printPreview.title");
      // title.concat(getGraphWindow().getName());
      return title;
   }

   protected String getPrintSetupTitle()
   {
      String printSetupTitle = m_resources.getString("dialog.printSetup.title");
      // printSetupTitle.concat(getGraphWindow().getName());
      return printSetupTitle;
   }
}
