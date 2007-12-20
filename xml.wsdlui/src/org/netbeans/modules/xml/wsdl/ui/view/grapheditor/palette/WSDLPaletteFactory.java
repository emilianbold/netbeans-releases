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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.palette;

import java.io.IOException;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;

/**
 * @author radval
 * 
 */
public final class WSDLPaletteFactory {

  private WSDLPaletteFactory () {}

  /**
   * Creates a new soa palette.
   * @return a new soa palette
   */
  public static PaletteController getPalette() {
      if (ourPalette == null) {
          try {
              ourPalette = PaletteFactory.createPalette(
                  WSDL_PALETTE_FOLDER, 
                  new WSDLPaletteActions()
              );
          }
          catch (IOException e) {
              e.printStackTrace();
          }
      }
      return ourPalette;
  }
  
  private static final String WSDL_PALETTE_FOLDER = "WSDLPalette"; // NOI18N
  private static PaletteController ourPalette;
}
