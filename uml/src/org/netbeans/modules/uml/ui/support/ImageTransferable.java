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



package org.netbeans.modules.uml.ui.support;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 *
 * @author Trey Spiva
 */
public class ImageTransferable implements Transferable
{
   private Image mImage = null;

   /**
    *
    */
   public ImageTransferable(Image image)
   {
      super();
      setImage(image);
   }

   /* (non-Javadoc)
    * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
    */
   public DataFlavor[] getTransferDataFlavors()
   {
      return new DataFlavor[] { DataFlavor.imageFlavor };
   }

   // Returns true if flavor is supported
   public boolean isDataFlavorSupported(DataFlavor flavor)
   {
      return DataFlavor.imageFlavor.equals(flavor);
   }

   // Returns image
   public Object getTransferData(DataFlavor flavor) 
      throws UnsupportedFlavorException, IOException
   {
      if (!DataFlavor.imageFlavor.equals(flavor))
      {
         throw new UnsupportedFlavorException(flavor);
      }
      return getImage();
   }

   /**
    * @return
    */
   public Image getImage()
   {
      return mImage;
   }

   /**
    * @param image
    */
   public void setImage(Image image)
   {
      mImage = image;
   }

}
