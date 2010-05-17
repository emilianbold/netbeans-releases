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

package org.netbeans.microedition.lcdui;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;

import org.netbeans.microedition.lcdui.laf.ColorSchema;
import org.netbeans.microedition.lcdui.laf.TableColorSchema;

/**
 *
 * @author ads
 */
interface TablePaintStrategy {

    void drawHeaderBackground( Graphics g, int j, int i, int y,
            int colWidth, int headersHeight );

    int getForegroundHeaderColor( int j );

    int getColor( int x, int y, int aColorSpecifier );

    void drawCell( Graphics g, int column, int row, int x, int y, int width, 
            int height , int aColorSpecifier);

}

class BaseColorSchemaStrategy implements TablePaintStrategy {

    BaseColorSchemaStrategy( ColorSchema colorSchema ) {
        myColorSchema = colorSchema;
    }

    public void drawHeaderBackground( Graphics g, int j, int i, int y,
            int colWidth, int headersHeight )
    {
    }

    public int getForegroundHeaderColor( int j ) {
        return getColorSchema().getColor(Display.COLOR_FOREGROUND);
    }

    public int getColor( int x, int y, int colorSpecifier ) {
        return getColorSchema().getColor(colorSpecifier);
    }
    
    public void drawCell( Graphics g, int column, int row, int x, int y,
            int width, int height, int colorSpecifier )
    {
    }
    
    private ColorSchema getColorSchema(){
        return myColorSchema;
    }
    
    private ColorSchema myColorSchema;

}

class TableColorSchemaStrategy implements TablePaintStrategy {

    TableColorSchemaStrategy( TableColorSchema colorSchema ) {
        myColorSchema = colorSchema;
    }

    public void drawHeaderBackground( Graphics g, int index, int x, int y,
            int width, int height )
    {
        if ( getColorSchema().isHeaderTransparent( index )){
            return;
        }
        g.setColor( getColorSchema().getHeaderColor( index, Display.COLOR_BACKGROUND));
        g.fillRect( x , y, width, height);
    }

    public int getForegroundHeaderColor( int x ) {
        return getColorSchema().getHeaderColor( x, Display.COLOR_FOREGROUND);
    }
    
    public int getColor( int x, int y, int colorSpecifier ) {
        return getColorSchema().getColor( x, y , colorSpecifier);
    }
    
    public void drawCell( Graphics g, int column, int row, int x, int y,
            int width, int height, int colorSpecifier )
    {
        if ( getColorSchema().isBackgroundTransparent(column, row  )){
            return;
        }
        g.setColor( getColorSchema().getColor( column, row , colorSpecifier));
        g.fillRect( x , y, width, height);
    }
    
    private TableColorSchema getColorSchema(){
        return myColorSchema;
    }

    private TableColorSchema myColorSchema;

}
