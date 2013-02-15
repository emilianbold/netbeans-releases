/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.swing.plaf.nimbus;

import java.awt.Color;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import org.netbeans.swing.plaf.util.DarkIconFilter;

/**
 *
 * @author S. Aubrecht
 */
public class DarkNimbusTheme {

    public static void install( LookAndFeel laf ) {
        UIManager.put( "control", new Color( 128, 128, 128) );
        UIManager.put( "info", new Color(128,128,128) );
        UIManager.put( "nimbusBase", new Color( 18, 30, 49) );
        UIManager.put( "nimbusAlertYellow", new Color( 248, 187, 0) );
        UIManager.put( "nimbusDisabledText", new Color( 128, 128, 128) );
        UIManager.put( "nimbusFocus", new Color(115,164,209) );
        UIManager.put( "nimbusGreen", new Color(176,179,50) );
        UIManager.put( "nimbusInfoBlue", new Color( 66, 139, 221) );
        UIManager.put( "nimbusLightBackground", new Color( 18, 30, 49) );
        UIManager.put( "nimbusOrange", new Color(191,98,4) );
        UIManager.put( "nimbusRed", new Color(169,46,34) );
        UIManager.put( "nimbusSelectedText", new Color( 255, 255, 255) );
        UIManager.put( "nimbusSelectionBackground", new Color( 104, 93, 156) );
        UIManager.put( "text", new Color( 230, 230, 230) );
//        UIManager.put( "nb.imageicon.filter", new DarkIconFilter() );
        UIManager.put( "nb.errorForeground", new Color(220,0,0) ); //NOI18N
        UIManager.put( "nb.warningForeground", new Color(255,255,255) ); //NOI18N

        UIManager.put( "nb.heapview.border1", new Color( 128, 128, 128) ); //NOI18N
        UIManager.put( "nb.heapview.border2", new Color( 128, 128, 128).darker() ); //NOI18N
        UIManager.put( "nb.heapview.border3", new Color(115,164,209) ); //NOI18N

        UIManager.put( "nb.heapview.foreground", new Color( 230, 230, 230) ); //NOI18N

        UIManager.put( "nb.heapview.background1", new Color( 18, 30, 49) ); //NOI18N

        UIManager.put( "nb.heapview.background2", new Color( 18, 30, 49).brighter() ); //NOI18N

        UIManager.put( "nb.heapview.grid1.start", new Color( 97, 95, 87 ) ); //NOI18N
        UIManager.put( "nb.heapview.grid1.end", new Color( 98, 96, 88 ) ); //NOI18N
        UIManager.put( "nb.heapview.grid2.start", new Color( 99, 97, 90 ) ); //NOI18N
        UIManager.put( "nb.heapview.grid2.end", new Color( 101, 99, 92 ) ); //NOI18N
        UIManager.put( "nb.heapview.grid3.start", new Color( 102, 101, 93 ) ); //NOI18N
        UIManager.put( "nb.heapview.grid3.end", new Color( 105, 103, 95 ) ); //NOI18N
        UIManager.put( "nb.heapview.grid4.start", new Color( 107, 105, 97 ) ); //NOI18N
        UIManager.put( "nb.heapview.grid4.end", new Color( 109, 107, 99 ) ); //NOI18N
   }
}
