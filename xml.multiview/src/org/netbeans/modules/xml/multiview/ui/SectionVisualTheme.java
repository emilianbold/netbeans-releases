/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

/**
 * SectionVisualTheme.java
 *
 * Created on September 24, 2003, 9:14 AM
 * @author  bashby, mkuchtiak
 */
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class SectionVisualTheme {
    
    /** Creates a new instance of SectionColorTheme */
     static Color documentBackgroundColor =  new java.awt.Color(255, 255, 255);
     static Color sectionActiveBackgroundColor =  new java.awt.Color(253, 250, 245);
     static Color documentMarginColor = new java.awt.Color(153, 153, 153);
     static Color sectionHeaderColor = new java.awt.Color(255, 255, 255);
     static Color sectionHeaderActiveColor = new java.awt.Color(250, 231, 214);
     static Color fillerColor = javax.swing.UIManager.getDefaults().getColor("Button.background"); //NOI18N
     static Color tableHeaderColor = new java.awt.Color(204, 204, 204);
     static Color tableGridColor = new java.awt.Color(255, 255, 255);
     static Color sectionHeaderLineColor = new java.awt.Color(229, 138, 45);
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
