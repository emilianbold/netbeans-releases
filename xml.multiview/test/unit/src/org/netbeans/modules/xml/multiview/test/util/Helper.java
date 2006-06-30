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
package org.netbeans.modules.xml.multiview.test.util;

import java.io.*;
import java.awt.*;

import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;

import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.test.BookDataObject;
import org.netbeans.modules.xml.multiview.test.bookmodel.Chapter;

import javax.swing.*;
import javax.swing.text.Document;

public class Helper {

    public static File getBookFile(File dataDir) {
        return new File(dataDir, "sample.book");
    }

    public static JTextField getChapterTitleTF(final BookDataObject dObj, Chapter chapter) {
        final ToolBarMultiViewElement multiViewElement = new StepIterator() {
            ToolBarMultiViewElement multiViewElement;

            public boolean step() throws Exception {
                return (multiViewElement = dObj.getActiveMultiViewElement0()) != null;
            }
        }.multiViewElement;
        SectionView sectionView = new StepIterator() {
            SectionView sectionView;
            public boolean step() throws Exception {
                return (sectionView = multiViewElement.getSectionView()) != null;

            }
        }.sectionView;
        JPanel sectionPanel = sectionView.findSectionPanel(chapter).getInnerPanel();
        Component[] children = sectionPanel.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof JTextField) {
                return (JTextField) children[i];
            }
        }
        return null;
    }

    public static boolean isTextInFile(String text, File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line=reader.readLine())!=null) {
            if (line.indexOf(text) >= 0) {
                return true;
            }
        }
        return false;
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex){
            // ignore
        }
    }

    public static void waitForDispatchThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            return;
        }
        final boolean[] finished = new boolean[]{false};
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                finished[0] = true;
            }
        });
        new StepIterator() {
            public boolean step() throws Exception {
                return finished[0];
            }
        };
    }

    public static SaveCookie getSaveCookie(final DataObject dataObject) {
        return new StepIterator() {
            SaveCookie cookie;

            public boolean step() throws Exception {
                return ((cookie = (SaveCookie) dataObject.getCookie(SaveCookie.class)) != null);
            }
        }.cookie;
    }

    public static Document getDocument(final XmlMultiViewEditorSupport editor) {
        return new StepIterator() {
            Document document;

            public boolean step() throws Exception {
                document = editor.getDocument();
                return (document.getLength() > 0);
            }
        }.document;
    }
}