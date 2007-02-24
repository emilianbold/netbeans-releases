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



package org.netbeans.modules.uml.ui.products.ad.viewfactory;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.ui.swing.drawingarea.SaveAsGraphicKind;
import com.tomsawyer.editor.TSEGraphImageEncoder;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSEObjectUI;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

import java.io.FileOutputStream;

/**
 * @author KevinM
 *
 *	This class extends the TSS image Encoder so we can save the transform.
 * You can only use the transform only after the image has been saved, 
 */
public class ETEGraphImageEncoder extends TSEGraphImageEncoder
{
   protected TSTransform encoderTransform = null;
   protected String fileName;
   protected int kind;
   private float QUALITY = 0.7f;

   public ETEGraphImageEncoder(TSEGraphWindow graphWindow, String sFilename, int saveAsGraphicKind)
   {
      super(graphWindow);

      fileName = sFilename;
      kind = saveAsGraphicKind;
   }

	/*
	 * Converts the graph window into an image output stream.
	 */
   public boolean save(double scale)
   {
      boolean rc = false;
      try
      {
         TSEObjectUI currentUI = getGraphWindow().getGraph().getUI();
			ETImageExportGraphUI ui = new ETImageExportGraphUI();
         getGraphWindow().getGraph().setUI(ui);

         switch (kind)
         {
            case SaveAsGraphicKind.SAFK_JPG :
//               writeJPEGFormat(new FileOutputStream(fileName));
				writeJPEGFormat(new FileOutputStream(fileName), false, 
						TSEGraphWindow.CUSTOM_SIZE, false, false, QUALITY, 
						(int)(getGraphWindow().getGraph().getFrameBounds().getWidth()*scale), 
						(int)(getGraphWindow().getGraph().getFrameBounds().getHeight()*scale));
               rc = true;
               break;
            case SaveAsGraphicKind.SAFK_PNG :
               writePNGFormat(new FileOutputStream(fileName));
               rc = true;
               break;
            case SaveAsGraphicKind.SAFK_SVG :
               writeSVGFormat(new FileOutputStream(fileName));
               rc = true;
               break;
         }
         encoderTransform = ui.getImageTransform();
         getGraphWindow().getGraph().setUI(currentUI);
      }
      catch (Exception e)
      {
         Log.stackTrace(e);
      }
      return rc;
   }
   
  	/*
  	 * Only valid after a call to save(), it can be used to locate objects on the graphic.
  	 */
   public TSTransform getEncoderTransform()
   {
      return encoderTransform;
   }
}
