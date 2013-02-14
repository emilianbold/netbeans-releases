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
package org.netbeans.swing.plaf.metal;

import java.awt.Color;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import org.netbeans.swing.plaf.util.DarkIconFilter;

/**
 *
 * @author S. Aubrecht, P. Somol
 */
public class DarkMetalTheme extends DefaultMetalTheme {

    private final ColorUIResource primary1 = new ColorUIResource( 121, 121, 125 );
    private final ColorUIResource primary2 = new ColorUIResource( 71, 71, 75 );
    private final ColorUIResource primary3 = new ColorUIResource( 99, 99, 99 );
    private final ColorUIResource secondary1 = new ColorUIResource( 113, 113, 113 );
    private final ColorUIResource secondary2 = new ColorUIResource( 91, 91, 95 );
    private final ColorUIResource secondary3 = new ColorUIResource( 51, 51, 55 );
    private final ColorUIResource black = new ColorUIResource( 222, 222, 222 );
    private final ColorUIResource white = new ColorUIResource( 18, 30, 49 );

    @Override
    public String getName() {
        return "NetBeans Dark Theme";
    }

    @Override
    public void addCustomEntriesToTable( UIDefaults table ) {
        super.addCustomEntriesToTable( table );
        table.put( "nb.imageicon.filter", new DarkIconFilter() ); //NOI18N
        table.put( "nb.errorForeground", new Color(255,0,0) ); //NOI18N
        table.put( "nb.warningForeground", new Color(255,255,255) ); //NOI18N
    }

    @Override
    protected ColorUIResource getPrimary1() {
        return primary1;
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return primary2;
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return primary3;
    }

    @Override
    protected ColorUIResource getSecondary1() {
        return secondary1;
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return secondary2;
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return secondary3;
    }

    @Override
    protected ColorUIResource getWhite() {
        return white;
    }

    @Override
    protected ColorUIResource getBlack() {
        return black;
    }
}
