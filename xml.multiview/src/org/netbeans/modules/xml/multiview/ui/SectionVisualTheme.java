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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
 * This class defines the visual theme (i.e. colors) of the 
 * multiview editor.
 *
 * Created on September 24, 2003, 9:14 AM
 * @author  bashby, mkuchtiak
 */
public class SectionVisualTheme {

    /** Creates a new instance of SectionColorTheme */
     static Color documentBackgroundColor =  new java.awt.Color(255, 255, 255);
     static Color sectionActiveBackgroundColor =  new java.awt.Color(252, 250, 245);
     static Color documentMarginColor = new java.awt.Color(153, 153, 153);
     static Color sectionHeaderColor = new java.awt.Color(255, 255, 255);
     static Color containerHeaderColor = new java.awt.Color(230, 228, 223);
     static Color sectionHeaderActiveColor = new java.awt.Color(250, 232, 213);
     static Color fillerColor = javax.swing.UIManager.getDefaults().getColor("Button.background"); //NOI18N
     static Color tableHeaderColor = new java.awt.Color(204, 204, 204);
     static Color tableGridColor = new java.awt.Color(255, 255, 255);
     static Color sectionHeaderLineColor = new java.awt.Color(230, 139, 44);
     static Color hyperlinkColor = new java.awt.Color(0, 0, 255);
     static Color hyperlinkColorFocused = new java.awt.Color(04,84,145);
     static Color textColor = new java.awt.Color(0, 0, 0);
     static Color foldLineColor = new java.awt.Color(102, 102, 102);
     
     static Color errorLabelColor = javax.swing.UIManager.getDefaults().getColor("ToolBar.dockingForeground"); //NOI18N
   
     public SectionVisualTheme() {
    }
    
    static public Color getDocumentBackgroundColor(){
        return documentBackgroundColor;
    }
    static public Color getMarginColor(){
        return documentMarginColor;
    }
    static public Color getSectionHeaderColor(){
        return sectionHeaderColor;
    }
    static public Color getContainerHeaderColor(){
        return containerHeaderColor;
    }
    static public Color getSectionHeaderActiveColor(){
        return sectionHeaderActiveColor;
    }
    static public Color getSectionActiveBackgroundColor(){
        return sectionActiveBackgroundColor;
    }
    static public Color getTableHeaderColor(){
        return tableHeaderColor;
    }
    static public Color getTableGridColor(){
        return tableGridColor;
    }
    
    static public Color getSectionHeaderLineColor(){
        return sectionHeaderLineColor;
    }
    
    static public Color getHyperlinkColor(){
        return hyperlinkColor;
    }
    
    static public Color getHyperlinkColorFocused(){
        return hyperlinkColorFocused;
    }
    
    static public Color getTextColor(){
        return textColor;
    }
    
    static public Color getFillerColor(){
        return fillerColor;
    }
    
    static public Color getErrorLabelColor(){
        return errorLabelColor;
    }
    
    static public Color getFoldLineColor(){
        return foldLineColor;
    }
    
}
