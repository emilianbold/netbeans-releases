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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.pdf;

import java.beans.IntrospectionException;
import java.io.File;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.nodes.BeanNode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import static java.util.logging.Level.FINER;


/**
 * New PDF settings.
 *
 * @author Libor Kramolis
 * @author  Marian Petras
 */
public class Settings {

    private static final Logger LOG = Logger.getLogger(Settings.class.getName());

    public static final String PROP_PDF_VIEWER = "PDFViewer";           //NOI18N

    private static final Settings INSTANCE = new Settings();

    /**
     * Default instance of this system option,
     * for the convenience of associated classes.
     */
    public static Settings getDefault() {
        return INSTANCE;
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(Settings.class);
    }

    public File getPDFViewer() {
        String fileName = getPreferences().get(PROP_PDF_VIEWER, null);
        return ((fileName != null) && (fileName.length() != 0))
               ? new File(fileName)
               : null;
    }

    public void setPDFViewer(File viewer) {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("Settings[" + this + "].setPDFViewer: " + viewer);//NOI18N
        }

        getPreferences().put(PROP_PDF_VIEWER, (viewer != null)
                                              ? viewer.toString()
                                              : "");                    //NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(Settings.class, "PDFSettings");      //NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(Settings.class); 
    }

    private static BeanNode createViewNode() throws IntrospectionException {
        return new BeanNode<Settings>(Settings.getDefault());
    }         

}
