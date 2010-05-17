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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/*
 * AnnotationCustomizer.java
 *
 * Created on May 10, 2006, 1:01 PM
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.jdesktop.layout.GroupLayout;
import org.openide.util.HelpCtx;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;

/**
 *
 * @author  Ajit Bhate
 */
public class AnnotationCustomizer extends AbstractSchemaComponentCustomizer<Annotation>
		implements PropertyChangeListener
{
	static final long serialVersionUID = 1L;
	
	private ArrayList<DocumentationPanel> docPanels;
	
	/**
	 * Creates new form AnnotationCustomizer
	 */
	public AnnotationCustomizer(SchemaComponentReference<Annotation> reference)
	{
		super(reference);
		initComponents();
		initialize();
	}
	
	private void initialize()
	{
		Annotation a = getReference().get();
		if(docPanels!= null)
		{
			for(int i = docPanels.size()-1; i>=0;i--)
			{
				DocumentationPanel docPanel = docPanels.get(i);
				docPanel.removePropertyChangeListener
						(DocumentationPanel.STATE_PROPERTY,this);
				docPanels.remove(docPanel);
				panel.remove(docPanel);
			}
		}
		docPanels = new ArrayList<DocumentationPanel>();
		Collection<Documentation> documentations =
				a.getDocumentationElements();
		int idx = 0;
		for(Documentation doc:documentations)
		{
			DocumentationPanel docPanel = new DocumentationPanel(this,doc,false);
			docPanels.add(docPanel);
			addDocumentationPanel(docPanel);
			if(idx++<2) docPanel.setExpanded(true);
		}
	}

	private void addDocumentationPanel(DocumentationPanel p)
	{
		GroupLayout layout = (GroupLayout)panel.getLayout();
		GroupLayout.ParallelGroup hGroup = 
				(GroupLayout.ParallelGroup)layout.getHorizontalGroup();
		hGroup.add(p);
		GroupLayout.SequentialGroup vGroup = 
				(GroupLayout.SequentialGroup)layout.getVerticalGroup();
		vGroup.add(p);
	}

	protected void applyChanges() throws IOException
	{
		for(int i = docPanels.size()-1; i>=0;i--)
		{
			DocumentationPanel docPanel = docPanels.get(i);
			switch(docPanel.getState())
			{
				case REMOVED:
					getReference().get().removeDocumentation(docPanel.getDocumentation());
					docPanels.remove(docPanel);
					break;
				case ADDED:
					getReference().get().addDocumentation(docPanel.getDocumentation());
					docPanel.getDocumentation().setContentFragment(docPanel.getContent());
					docPanel.setState(DocumentationPanel.State.UNMODIFIED);
					break;
				case MODIFIED:
					docPanel.getDocumentation().setContentFragment(docPanel.getContent());
					docPanel.setState(DocumentationPanel.State.UNMODIFIED);
					break;
			}
		}
	}

	public void reset()
	{
		initialize();
		repaint();
		revalidate();
		setSaveEnabled(false);
		setResetEnabled(false);
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        panel = new javax.swing.JPanel();

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        addButton.setText(org.openide.util.NbBundle.getMessage(AnnotationCustomizer.class, "LBL_Add"));
        addButton.setActionCommand(org.openide.util.NbBundle.getMessage(AnnotationCustomizer.class, "LBL_Add"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(null);
        panel.setLayout(null);

        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        GroupLayout panelLayout = new GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setAutocreateGaps(true);
        panelLayout.setAutocreateContainerGaps(true);
        GroupLayout.ParallelGroup hGroup =
        panelLayout.createParallelGroup();
        panelLayout.setHorizontalGroup(hGroup);
        GroupLayout.SequentialGroup vGroup =
        panelLayout.createSequentialGroup();
        panelLayout.setVerticalGroup(vGroup);
        jScrollPane1.setViewportView(panel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .add(addButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

	private void addButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addButtonActionPerformed
	{//GEN-HEADEREND:event_addButtonActionPerformed
		Documentation doc = getReference().get().getModel().
				getFactory().createDocumentation();
		DocumentationPanel docPanel = new DocumentationPanel(this,doc,true);
		docPanels.add(docPanel);
		addDocumentationPanel(docPanel);
		docPanel.setExpanded(true);
		revalidate();
	}//GEN-LAST:event_addButtonActionPerformed

	public void propertyChange(PropertyChangeEvent evt)
	{
		Object source = evt.getSource();
		String property = evt.getPropertyName();
		// checking of property name not needed, but lets keep it
		if (property.equals(DocumentationPanel.STATE_PROPERTY) &&
				source instanceof DocumentationPanel)
		{
			DocumentationPanel p = (DocumentationPanel)source;
			DocumentationPanel.State newState =
					(DocumentationPanel.State) evt.getNewValue();
			boolean modified = false;
			if(newState == DocumentationPanel.State.UNMODIFIED)
			{
				for(DocumentationPanel docPanel:docPanels)
				{
					if(docPanel.getState() != DocumentationPanel.State.UNMODIFIED)
					{
						modified = true;
						break;
					}
				}
			}
			else if(newState == DocumentationPanel.State.REMOVED)
			{
				modified = getReference().get().getDocumentationElements().
						contains(p.getDocumentation());
				if(!modified)
				{
					docPanels.remove(p);
					for(DocumentationPanel docPanel:docPanels)
					{
						if(docPanel.getState() != DocumentationPanel.State.UNMODIFIED)
						{
							modified = true;
							break;
						}
					}
				}
				p.removePropertyChangeListener
						(DocumentationPanel.STATE_PROPERTY,this);
				panel.remove(p);
				repaint();
				revalidate();
			}
			else
			{
				modified = true;
			}
			setSaveEnabled(modified);
			setResetEnabled(modified);
		}
	}

	public HelpCtx getHelpCtx()
	{
		return new HelpCtx(AnnotationCustomizer.class);
	}
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panel;
    // End of variables declaration//GEN-END:variables
	
}
