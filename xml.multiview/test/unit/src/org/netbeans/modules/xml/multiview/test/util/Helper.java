/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.multiview.test.util;

import java.io.*;
import org.openide.filesystems.FileStateInvalidException;

import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.test.BookDataObject;
import org.netbeans.modules.xml.multiview.test.bookmodel.Chapter;

public class Helper {

    public static File getBookFile(File dataDir) {
        String result = dataDir.getAbsolutePath() + "/projects/webapp/web/WEB-INF/sample.book";
        return new File(result);
    }
    
    public static javax.swing.JTextField getChapterTitleTF(BookDataObject dObj, Chapter chapter) {
        ToolBarMultiViewElement mvEl = dObj.getActiveMultiViewElement0();
        javax.swing.JPanel sectionPanel = mvEl.getSectionView().findSectionPanel(chapter).getInnerPanel();
        if (sectionPanel==null) return null;
        java.awt.Component[] children = sectionPanel.getComponents();
        for (int i=0;i<children.length;i++) {
            if (children[i] instanceof javax.swing.JTextField) {
                return (javax.swing.JTextField)children[i];
            }
        }
        return  null;
    }
    
    public static boolean isTextInFile(String text, File file) throws java.io.IOException {
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
        String line;
        while ((line=reader.readLine())!=null) {
            if (line.indexOf(text)>=0) return true;
        }
        return false;
    }
    
}
