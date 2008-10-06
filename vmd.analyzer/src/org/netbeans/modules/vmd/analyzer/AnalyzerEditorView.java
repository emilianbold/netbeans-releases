/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.analyzer;

import org.netbeans.modules.vmd.api.analyzer.Analyzer;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Kaspar
 */
public class AnalyzerEditorView implements DataEditorView, DesignDocumentAwareness {

    private static final Color BACKGROUND_COLOR = new Color (0xFBF9F3);

    private static final long serialVersionUID = -1;

    static final String ANALYZER_ID = "analyzer"; // NOI18N

    private DataObjectContext context;
    private transient HashMap<Analyzer, JComponent> components;
    private transient DesignDocument document;
    private transient JScrollPane scroll;
    private transient JToolBar toolbar;

    public AnalyzerEditorView (DataObjectContext context) {
        this.context = context;
        init ();
    }

    private void init () {
        components = new HashMap<Analyzer, JComponent> ();
        Collection<? extends Analyzer> analyzers = Lookup.getDefault ().lookupResult (Analyzer.class).allInstances ();

        JPanel panel = new JPanel ();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder (BorderFactory.createEmptyBorder (1, 1, 1, 1));
        scroll = new JScrollPane (panel);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets (10, 10, 10, 10);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = GridBagConstraints.REMAINDER;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        for (Analyzer analyzer : analyzers) {
            String projectType = context.getProjectType ();
            if (projectType == null  ||  ! projectType.equals (analyzer.getProjectType ()))
                continue;
            JComponent visualRepresentation = analyzer.createVisualRepresentation ();
            components.put (analyzer, visualRepresentation);
            if (visualRepresentation != null)
                panel.add (new AnalyzerPanel (analyzer, visualRepresentation), constraints);
        }
        constraints.weighty = 1.0;
        JPanel filler = new JPanel ();
        filler.setOpaque (false);
        panel.add (filler, constraints);
        context.addDesignDocumentAwareness (this);

        toolbar = new JToolBar ();
        toolbar.setFloatable (false);
        toolbar.setRollover (true);
        toolbar.setPreferredSize (new Dimension (14, 14));
        toolbar.setSize (new Dimension (14, 14));
        JToolBar.Separator separator = new JToolBar.Separator ();
        separator.setOrientation(JSeparator.VERTICAL);
        toolbar.add (separator);

        JButton refreshButton = new JButton ();
        refreshButton.setOpaque (false);
        refreshButton.setToolTipText (NbBundle.getMessage(AnalyzerEditorView.class, "TTIP_Refresh")); // NOI18N
        refreshButton.setBorderPainted (false);
        refreshButton.setRolloverEnabled (true);
        refreshButton.setSize (14, 14);
        refreshButton.setIcon (new ImageIcon (ImageUtilities.loadImage ("org/netbeans/modules/vmd/analyzer/resources/refresh.png"))); // NOI18N
        refreshButton.addActionListener (new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                updateAnalyzers ();
            }
        });
        toolbar.add (refreshButton);
        
        // vlv: print
        panel.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
    }

    public DataObjectContext getContext () {
        return context;
    }

    public DataEditorView.Kind getKind () {
        return DataEditorView.Kind.MODEL;
    }

    public boolean canShowSideWindows () {
        return true;
    }

    public Collection<String> getTags () {
        return Collections.emptySet ();
    }

    public String preferredID () {
        return ANALYZER_ID;
    }

    public String getDisplayName () {
        return NbBundle.getMessage(AnalyzerEditorView.class, "DISP_AnalyzerView"); // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (AnalyzerEditorView.class);
    }

    public JComponent getVisualRepresentation () {
        return scroll;
    }

    public JComponent getToolbarRepresentation () {
        return toolbar;
    }

    public UndoRedo getUndoRedo () {
        return null;
    }

    public void componentOpened () {
    }

    public void componentClosed () {
    }

    public void componentShowing () {
    }

    public void componentHidden () {
    }

    public void componentActivated () {
        updateAnalyzers ();
    }

    public void componentDeactivated () {
    }

    public int getOpenPriority () {
        return 0;
    }

    public int getEditPriority () {
        return 0;
    }

    public int getOrder () {
        return 3000;
    }

    private void writeObject (java.io.ObjectOutputStream out) throws IOException {
        out.writeObject (context);
    }

    private void readObject (java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object object = in.readObject ();
        if (! (object instanceof DataObjectContext))
            throw new ClassNotFoundException ("DataObjectContext expected but not found"); // NOI18N
        context = (DataObjectContext) object;
        init ();
    }

    public void setDesignDocument (DesignDocument designDocument) {
        document = designDocument;
        componentActivated ();
    }

    private void updateAnalyzers () {
        final DesignDocument doc = document;
        for (Map.Entry<Analyzer, JComponent> entry : components.entrySet ())
            entry.getKey ().update (entry.getValue (), doc);
    }

}
