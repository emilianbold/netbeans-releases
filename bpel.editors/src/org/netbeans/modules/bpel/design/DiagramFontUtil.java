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
package org.netbeans.modules.bpel.design;

import java.awt.Font;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.JLabel;

/**
 *
 * @author Alexey
 */
public class DiagramFontUtil {
    

    private static final double DIAGRAM_FONT_SIZE = 11;
    
    public static Font getFont(){
        return font;
    }
    
    public static double getZoomCorrection(){
        return zoomCorrection;
    }

    private static Font font = new JLabel().getFont()
            .deriveFont((float) DIAGRAM_FONT_SIZE);
    
    private static final double zoomCorrection = new JLabel().getFont().getSize2D() /
            DIAGRAM_FONT_SIZE;
    
    private static final Set<Locale> multibyteLocales = new HashSet<Locale>();

    static {
        multibyteLocales.add(Locale.JAPANESE);
        multibyteLocales.add(Locale.KOREAN);
        multibyteLocales.add(Locale.CHINESE);
       
        //BAD HACK. I dont know how to determine
        if (multibyteLocales.contains(Locale.getDefault())){
//            System.out.println("changin font");
            font = new Font("sansserif", 
                    font.getStyle(),
                    font.getSize());
        }
    }
}
