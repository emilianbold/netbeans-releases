/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.microedition.lcdui.laf;

/**
 *
 * @author ads
 */
public abstract class TableColorSchema extends ColorSchema {

    public boolean isBackgroundTransparent() {
        return true;
    }

    /**
     * Gets color based on row, column and color specifier.
     * The color specifer
     * corresponds to values listed in Display class.
     * @param x column
     * @param y row
     * @param aColorSpecifier - color specifier from Display.COLOR* constnants
     * @return color to be used for given specifier, row and column
     */
    public int getColor( int x , int y ,int aColorSpecifier){
        return getColor(aColorSpecifier);
    }

    /**
     * If true the background for specified row and column is transparent.
     * This is helpful for some devices,
     * for example when drawing custom items on Nokia Seris 40 feature pack 1,
     * the background does not have to be erased and the custom item looks much
     * better when transparent.
     * @param x column
     * @param y row
     * @return true when the background should be transparent, false otherwise
     */
    public boolean isBackgroundTransparent( int x , int y ){
        return false;
    }

    /**
     * Gets color of header ( if any ) based on column and color specifier.
     * The color specifer
     * corresponds to values listed in Display class.
     * @param x column
     * @param aColorSpecifier
     * @return color to be used for given specifier and row
     */
    public int getHeaderColor( int x , int aColorSpecifier ){
        return getColor( aColorSpecifier );
    }

    /**
     * If true the header background for specified column
     * is transparent.
     * 
     * @param x column
     * @return true when the background should be transparent, false otherwise
     */
    public boolean isHeaderTransparent( int x ){
        return false;
    }
}
