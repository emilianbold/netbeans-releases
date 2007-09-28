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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.websvcmgr.codegen;

import java.io.Writer;
import java.util.Date;

public class DataProviderBeanInfoWriter extends java.io.PrintWriter {
    
    // Hardcode the iconFileName here
    public static final String DATA_PROVIDER_ICON_FILE_NAME = "methodicon.png";
    public static final String DATA_PROVIDER_ICON_FILE_NAME2 = "table_dp_badge.png";
    
    private DataProviderInfo dataProviderInfo;
    
    public DataProviderBeanInfoWriter(Writer writer, DataProviderInfo dataProviderInfo ){
        super(writer);
        this.dataProviderInfo = dataProviderInfo;
    }
    
    public void writeClass() {
        // package
        println( "package " + dataProviderInfo.getPackageName() + ";" );
        println();
        
        // comments
        println( "/**" );
        println( " * Source code created on " + new Date() );
        println( " */" );
        println();
        
        // Import
        println( "import java.awt.Image;" );
        println( "import javax.swing.ImageIcon;" );
        println( "import java.beans.BeanDescriptor;" );
        println( "import java.beans.PropertyDescriptor;" );
        println( "import java.beans.SimpleBeanInfo;" );
        println();
        
        // Start class
        String beanInfoClassName = dataProviderInfo.getClassName() + "BeanInfo";
        println( "public class " + beanInfoClassName + " extends SimpleBeanInfo {" );
        println();
        
        // Private variables
        String beanClassVariable = "beanClass";
        String iconFileNameVariable = "iconFileName";
        String iconFileNameVariable2 = "iconFileName2";
        String beanDescriptorVariable = "beanDescriptor";
        String propDescriptorsVariable = "propDescriptors";
        println( "    private Class " + beanClassVariable + " = " + dataProviderInfo.getClassName() + ".class;" );
        println( "    private PropertyDescriptor[] " + propDescriptorsVariable + " = null; " );
        println( "    private String " + iconFileNameVariable + " = \"" + DATA_PROVIDER_ICON_FILE_NAME + "\";" );
        println( "    private String " + iconFileNameVariable2 + " = \"" + DATA_PROVIDER_ICON_FILE_NAME2 + "\";" );
        println( "    private BeanDescriptor " + beanDescriptorVariable + " = null;" );
        println();
        
        // Method - getIcon()
        println( "    public Image getIcon(int iconKind) {" );
        println( "        ImageIcon imgIcon1 = new ImageIcon(getClass().getResource( " + iconFileNameVariable + " )); " );
        println( "        ImageIcon imgIcon2 = new ImageIcon(getClass().getResource( " + iconFileNameVariable2 + " )); " );
        println( "        return mergeImages( imgIcon1.getImage(), imgIcon2.getImage() );" );
        println( "    }" );
        println();
        
        // private method for merging two images into one
        println( "    private Image mergeImages (Image image1, Image image2) {" );
        println( "        int w = image1.getWidth(null);" );
        println( "        int h = image1.getHeight(null);" );
        println( "        int x = image1.getWidth(null) - image2.getWidth(null);" );
        println( "        int y = image1.getHeight(null) - image2.getHeight(null);" );
        println();
        println( "        java.awt.image.ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment ()." );
        println( "                                          getDefaultScreenDevice ().getDefaultConfiguration ()." );
        println( "                                          getColorModel (java.awt.Transparency.BITMASK);" );
        println( "        java.awt.image.BufferedImage buffImage = new java.awt.image.BufferedImage (model," );
        println( "             model.createCompatibleWritableRaster (w, h), model.isAlphaPremultiplied (), null);" );
        println();
        println( "        java.awt.Graphics g = buffImage.createGraphics ();" );
        println( "        g.drawImage (image1, 0, 0, null);" );
        println( "        g.drawImage (image2, x, y, null);" );
        println( "        g.dispose();" );
        println();
        println( "        return buffImage;" );
        println( "    }" );
        println();
        
        // Method - getBeanDescriptor()
        println( "    public BeanDescriptor getBeanDescriptor() {" );
        println( "        if( " + beanDescriptorVariable + " == null ) {" );
        println( "           " + beanDescriptorVariable + " = new BeanDescriptor( " + beanClassVariable + " );" );
        println( "           " + beanDescriptorVariable + ".setValue( \"trayComponent\", Boolean.TRUE );" );
        println( "        }" );
        println( "        return " + beanDescriptorVariable + ";" );
        println( "    }" );
        println( "}" );
    }
}
