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
package org.netbeans.modules.pdf;

import java.io.File;
import java.util.Properties;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;

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
    
    /**
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
