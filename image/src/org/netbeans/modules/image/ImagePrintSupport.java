/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.image;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.io.*;

import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.cookies.PrintCookie;
import org.openide.util.NbBundle;

/** Printing support.
 * Manipulations of the image to suit page size and orientation
 * occur through the method prepareImage( PageFormat ).
 * Subclass' override this method to honour image handling.
 *
 * @author  michael wever [hair@netbeans.org]
 * @version $Revision$
 */
public class ImagePrintSupport implements PrintCookie, Printable, ImageObserver {
    /* associated dataObject */
    protected ImageDataObject dataObject;
    /* image to print */
    protected RenderedImage image;
    
    /** Creates new ImagePrintSupport */
    public ImagePrintSupport( ImageDataObject ido ) {
        dataObject = ido;
    }
    
    /** Prepare the image to fit on the given page, within the given margins. 
     * Returns null if it were unable to prepare the image for the given page. 
     * Throws a IllegalArgumentException if the page were too small for the image.
     **/
    protected RenderedImage prepareImage( PageFormat pf ) throws IllegalArgumentException {
        try{
            AffineTransform af = new AffineTransform();
            if( pf.getOrientation() == pf.LANDSCAPE ){
            }else{
                af.translate( (double)pf.getImageableX(), (double)pf.getImageableY() );
            }
            image = (RenderedImage)dataObject.getImage();
            
            /** notify if too big for page **/
            if( pf.getImageableWidth() - pf.getImageableX() < image.getWidth()
                || pf.getImageableHeight() - pf.getImageableY() < image.getHeight() )
                    throw new IllegalArgumentException("Page too small for image");            //NOI18N
            
            /* Translate image */
            AffineTransformOp afo = new AffineTransformOp( af, AffineTransformOp.TYPE_NEAREST_NEIGHBOR );
            BufferedImage o = (BufferedImage)image;
            BufferedImage i = new BufferedImage( o.getWidth()+(int)pf.getImageableX(), o.getHeight()+(int)pf.getImageableY(), o.getType() );
            return afo.filter( (BufferedImage)image, i );
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
    
    /** Print the content of the object.  */
    public void print() {
        PrinterJob job = PrinterJob.getPrinterJob();
        Book book = new Book();
        PageFormat pf = org.openide.text.PrintSettings.getPageFormat(job);
        book.append( this, pf );
        job.setPageable( book );

        // Print
        try {
            // Make sure not to print in the paper's margin.
            image = prepareImage( pf );
            if (job.printDialog()) {
                job.print();
            }
        } catch (PrinterAbortException e) { // user exception
            final String msg = NbBundle.getMessage(org.openide.text.PrintSettings.class, "CTL_Printer_Abort"); // NOI18N
            java.awt.EventQueue.invokeLater(new Runnable() { // display in the awt thread
                public void run() {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                }
            });
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }        
    }
    
    /* Implements Printable */
    public int print(Graphics graphics, PageFormat pageFormat, int page) throws PrinterException {
        if( page != 0 ) return Printable.NO_SUCH_PAGE;
        
        Graphics2D g2 = (Graphics2D)graphics;
        if( image == null ){
            /** prepareImage() failed,
             * most probably cause is image does not implement RenderedImage,
             * just draw the image then.
             **/
            graphics.drawImage(dataObject.getImage(), (int)pageFormat.getImageableX(), (int)pageFormat.getImageableY(), this );
        }else{
            g2.drawRenderedImage( image, new AffineTransform() );
        }
        return Printable.PAGE_EXISTS;
    }
    
    public boolean imageUpdate(java.awt.Image image, int flags, int param2, int param3, int param4, int param5) {
        return false;
    }
    
}
