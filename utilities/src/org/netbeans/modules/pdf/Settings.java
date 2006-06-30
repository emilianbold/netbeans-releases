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
package org.netbeans.modules.pdf;

import java.io.File;
import java.util.Properties;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;

import org.openide.util.Lookup;


/**
 * New PDF settings.
 *
 * @author Libor Kramolis
 * @author  Marian Petras
 */
public class Settings {
    public static final String PROP_PDF_VIEWER = "PDFViewer";           //NOI18N
    private File viewer;
    private PropertyChangeSupport supp = new PropertyChangeSupport(this);


    public static Settings getDefault() {
        return (Settings) Lookup.getDefault().lookup(Settings.class);
    }

    public File getPDFViewer() {
        return viewer;
    }

    public void setPDFViewer(File viewer) {
        ErrorManager.getDefault()
                .getInstance("org.netbeans.modules.pdf")                //NOI18N
                .log("Settings [" + this + "].setPDFViewer: " + viewer);//NOI18N

        File old = this.viewer;
        this.viewer = viewer;
        supp.firePropertyChange(PROP_PDF_VIEWER, old, viewer);
    }

    /**
     * @see http://www.netbeans.org/download/dev/javadoc/SettingsAPIs/org/netbeans/spi/settings/doc-files/api.html#xmlprops
     */
    private void readProperties(Properties p) {
        String fileName = p.getProperty(PROP_PDF_VIEWER);
        if (fileName != null && fileName.length() != 0) {
            viewer = new File(fileName);
        } else {
            viewer = null;
        }
    }

    /**
     * @see http://www.netbeans.org/download/dev/javadoc/SettingsAPIs/org/netbeans/spi/settings/doc-files/api.html#xmlprops
     */
    private void writeProperties(Properties p) {
        p.setProperty(PROP_PDF_VIEWER,
                      viewer != null ? viewer.toString() : null);
    }


    public void addPropertyChangeListener(PropertyChangeListener l) {
        ErrorManager.getDefault()
              .getInstance("org.netbeans.modules.pdf")                  //NOI18N
              .log("Settings [" + this + "].addPropertyChangeListener: "//NOI18N
                   + l);

        supp.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        ErrorManager.getDefault()
           .getInstance("org.netbeans.modules.pdf")                     //NOI18N
           .log("Settings [" + this + "].removePropertyChangeListener: "//NOI18N
                + l);

        supp.removePropertyChangeListener(l);
    }

}
