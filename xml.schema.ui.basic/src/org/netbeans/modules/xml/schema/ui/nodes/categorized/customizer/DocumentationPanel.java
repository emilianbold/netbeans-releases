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
 * DocumentationPanel.java
 *
 * Created on May 10, 2006, 10:04 AM
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.openide.util.NbBundle;

/**
 *
 * @author  Ajit Bhate
 */
public class DocumentationPanel extends javax.swing.JPanel
{
	static final long serialVersionUID = 1L;
	public static final String STATE_PROPERTY = "state";
	
	private transient Documentation documentation;
	private transient DocumentListener contentListener;
	private transient AnnotationCustomizer owner;
	private transient State state;
	/**
	 * Creates new form DocumentationPanel
	 */
	public DocumentationPanel(AnnotationCustomizer owner, 
			Documentation documentation, boolean isAdded)
	{
		this.owner = owner;
		this.documentation = documentation;
		initComponents();
		initUI();
		setState(State.UNMODIFIED);
		addPropertyChangeListener(STATE_PROPERTY,owner);
		if(isAdded)
			updateState(State.ADDED);
	}

	private void initUI()
	{
		innerPanel.setVisible(false);
		if(contentListener == null)
		{
			contentListener = new DocumentListener() {
				public void changedUpdate(DocumentEvent e)
				{
					updateState(State.MODIFIED);
				}
				public void insertUpdate(DocumentEvent e)
				{
					updateState(State.MODIFIED);
				}
				public void removeUpdate(DocumentEvent e)
				{
					updateState(State.MODIFIED);
				}
			};
		} else
		{
			contentEditorPane.getDocument().
					removeDocumentListener(contentListener);
		}
		contentEditorPane.setText(getDocumentation().getContentFragment());
		contentEditorPane.getDocument().
				addDocumentListener(contentListener);
	}

	public Documentation getDocumentation()
	{
		return documentation;
	}
	
	private boolean isModified()
	{
		return !getContent().equals(getDocumentation().getContentFragment());
	}

	public String getContent()
	{
		return contentEditorPane.getText();
	}
	
	private String getTitleButtonText()
	{
		String content = "";
		if(!titleButton.isSelected())
		{
			content = getDocumentation().getContentFragment().trim();
			if(content.length()==0)
			{
				content = NbBundle.getMessage(DocumentationPanel.class,
						"LBL_DocumentationEmptyContent");
			}
			else if (content.length()>20)
			{
				content = content.substring(0,20);
				content = content.replaceAll("<","&lt;");
			}
		}
		return NbBundle.getMessage(DocumentationPanel.class,
				"LBL_Documentation", content);
	}

	public State getState()
	{
		return state;
	}

	void setState(State state)
	{
		this.state = state;
	}

	private void updateState(final State state)
	{
		State newState = state;
		State currentState = getState();
		// its marked added so if its not removed do not change state
		if(currentState==State.ADDED && newState!= State.REMOVED) return;
		if(newState==State.MODIFIED && !isModified())
		{
			// actually it is not modified
			newState = State.UNMODIFIED;
		}
		setState(newState);
		firePropertyChange(STATE_PROPERTY,currentState,newState);
	}

	public enum State {ADDED,REMOVED,MODIFIED,UNMODIFIED}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        titleButton = new javax.swing.JButton();
        innerPanel = new javax.swing.JPanel();
        removeButton = new javax.swing.JButton();
        contentScrollPane = new javax.swing.JScrollPane();
        contentEditorPane = new javax.swing.JEditorPane()
        {
            static final long serialVersionUID = 1L;
            protected void processMouseEvent(java.awt.event.MouseEvent e)
            {
                if(e.getButton()==java.awt.event.MouseEvent.BUTTON3)
                {
                    e.consume();
                    return;
                }
                super.processMouseEvent(e);
            }
        };

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        titleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/schema/ui/nodes/resources/plus.gif")));
        titleButton.setText(getTitleButtonText());
        titleButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        titleButton.setBorderPainted(false);
        titleButton.setContentAreaFilled(false);
        titleButton.setFocusPainted(false);
        titleButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        titleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/schema/ui/nodes/resources/minus.gif")));
        titleButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                manageInnerPanel(evt);
            }
        });

        innerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        removeButton.setText(org.openide.util.NbBundle.getMessage(DocumentationPanel.class, "LBL_Remove"));
        removeButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                removeButtonActionPerformed(evt);
            }
        });

        contentScrollPane.setBorder(null);
        contentEditorPane.setContentType("text/xml");
        contentScrollPane.setViewportView(contentEditorPane);

        org.jdesktop.layout.GroupLayout innerPanelLayout = new org.jdesktop.layout.GroupLayout(innerPanel);
        innerPanel.setLayout(innerPanelLayout);
        innerPanelLayout.setHorizontalGroup(
            innerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(innerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(innerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, removeButton)
                    .add(contentScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                .addContainerGap())
        );
        innerPanelLayout.setVerticalGroup(
            innerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, innerPanelLayout.createSequentialGroup()
                .add(contentScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(removeButton)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(innerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(titleButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(titleButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(innerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void manageInnerPanel(java.awt.event.ActionEvent evt)//GEN-FIRST:event_manageInnerPanel
	{//GEN-HEADEREND:event_manageInnerPanel
		boolean expand = !titleButton.isSelected();
		setExpanded(expand);
	}//GEN-LAST:event_manageInnerPanel

	void setExpanded(final boolean expand)
	{
		titleButton.setSelected(expand);
		titleButton.setText(getTitleButtonText());
		innerPanel.setVisible(expand);
	}

	private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeButtonActionPerformed
	{//GEN-HEADEREND:event_removeButtonActionPerformed
		updateState(State.REMOVED);
	}//GEN-LAST:event_removeButtonActionPerformed
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane contentEditorPane;
    private javax.swing.JScrollPane contentScrollPane;
    private javax.swing.JPanel innerPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton titleButton;
    // End of variables declaration//GEN-END:variables
	
}
