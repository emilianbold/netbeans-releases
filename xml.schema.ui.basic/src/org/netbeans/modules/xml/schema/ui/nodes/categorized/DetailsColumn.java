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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.xml.schema.ui.basic.SchemaColumn;
import org.netbeans.modules.xml.xam.ui.column.ColumnProvider;
import org.netbeans.modules.xml.xam.ui.customizer.AbstractCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Inner class implementing details column.
 * Uses Border layout.
 * The customizer component provided by parent node is in the center.
 * If no customizer is provided dummy will be used.
 * The save and reset buttons will be in south.
 */
public class DetailsColumn extends SchemaColumn 
		implements ActionListener, LookupListener
{
	static final long serialVersionUID = 1L;
	
	/**
	 * If readlony
	 */
	private boolean readonly;
	
	/**
	 * customizer as center panel
	 */
	private Customizer customizer;
	
	/**
	 * title
	 */
// TAF: Remove title
//	private JLabel title;
	
	/**
	 * bottom panel containing buttons
	 */
	private JPanel bottomPanel;
	
	/**
	 * apply button
	 */
	private JButton applyButton;
	
	/**
	 * reset button
	 */
	private JButton resetButton;
	
	/**
	 * scroll pane for main pael
	 */
	private JScrollPane scrollPane;


	private Lookup.Result lookupResult;

	/**
	 * Creates details column using the customizer
	 */
	public DetailsColumn(Customizer cust)
	{
		super(null, null, false);
		customizer = cust==null || cust.getComponent()==null
				? new DummyDetails() : cust;
		initialize();
		// listen to customizer lookup event of type ColumnProvider
		lookupResult = getCustomizer().getLookup().lookup(
				new Lookup.Template(ColumnProvider.class));
		if(lookupResult!=null) lookupResult.addLookupListener(this);
	}
	
	
	/**
	 * Initializes the details column
	 */
	private void initialize()
	{
		setLayout(new BorderLayout());
		setBackground(Color.white);

// TAF: Remove title
//		// add Title
//		title = new JLabel(getTitle(), SwingConstants.CENTER);
//		add(title,BorderLayout.NORTH);
		
		// add customizer
		getCustomizer().getComponent().setBackground(Color.white);
		add(getCustomizer().getComponent(),BorderLayout.CENTER);
		scrollPane = new JScrollPane(getCustomizer().getComponent());
		scrollPane.setBorder(null);
		scrollPane.setViewportBorder(null);
		add(scrollPane,BorderLayout.CENTER);
		
		// add bottom panel
		bottomPanel = new JPanel();
		applyButton = new JButton();
		applyButton.setText(NbBundle.getMessage(DetailsColumn.class, "LBL_DetailsColumn_ApplyButton"));
		applyButton.setEnabled(false);
		applyButton.addActionListener(this);
		resetButton = new JButton();
		resetButton.setText(NbBundle.getMessage(DetailsColumn.class, "LBL_DetailsColumn_ResetButton"));
		resetButton.setEnabled(false);
		resetButton.addActionListener(this);
		bottomPanel.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(10, 10, 10, 2);
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.weightx = 1.0;
		bottomPanel.add(applyButton,gridBagConstraints);
		gridBagConstraints.insets = new Insets(10, 2, 10, 10);
		gridBagConstraints.weightx = 0.0;
		bottomPanel.add(resetButton,gridBagConstraints);
		bottomPanel.setBackground(Color.white);
		add(bottomPanel,BorderLayout.SOUTH);
		
		// add this as propertychangelistener of customizer
		getCustomizer().addPropertyChangeListener(this);
	}
	
	
	/**
	 *
	 *
	 */
	public String getTitle()
	{
		return NbBundle.getMessage(DetailsColumn.class, "LBL_DetailsColumn"); // NOI18N
	}
	
	// overridden as customizer component is main component
	public boolean requestFocusInWindow()
	{
		return getCustomizer().getComponent().requestFocusInWindow();
	}
	
	/**
	 *
	 *
	 */
	public JComponent getComponent()
	{
		return this;
	}
	
	public boolean isShowing()
	{
		boolean retValue = super.isShowing();
		if (retValue)
		{
			boolean enable = !isReadOnly();
			if(enable != getCustomizer().getComponent().isEnabled())
			{
				DetailsColumn.setEnabledComponent(getCustomizer().getComponent(),enable);
			}
		}
		return retValue;
	}
	// readonly 
	public boolean isReadOnly()
	{
		return readonly || !getCustomizer().isEditable();
	}

	public void setReadOnly(boolean readonly)
	{
		this.readonly = readonly;
	}

	private Customizer getCustomizer()
	{
		return customizer;
	}

	// property change events
	// consider special events first
	// then call super
	public void propertyChange(PropertyChangeEvent evt)
	{
		if(isReadOnly()) return;
		if (evt.getPropertyName().equals(Customizer.PROP_ACTION_APPLY))
		{
			applyButton.setEnabled(((Boolean) evt.getNewValue()).booleanValue());
		}
		else if (evt.getPropertyName().equals(Customizer.PROP_ACTION_RESET))
		{
			resetButton.setEnabled(((Boolean) evt.getNewValue()).booleanValue());
		}
		else
			super.propertyChange(evt);
	}
	
	// action events
	public void actionPerformed(ActionEvent e)
	{
		if(isReadOnly()) return;
		if (e.getSource() == applyButton)
		{
			try
			{
				getCustomizer().apply();
			}
			catch (IOException ioe)
			{
				String msg = NbBundle.getMessage(DetailsColumn.class, "MSG_DetailsColumn_ApplyFailed");
				IllegalArgumentException iae = new IllegalArgumentException(msg);
				ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
						msg, msg, ioe, new Date());
				getCustomizer().reset();
			}

		}
		else if (e.getSource() == resetButton)
		{
			getCustomizer().reset();
		}
	}

	// overridden to not do anything on focus gained event
	public void focusGained(FocusEvent e)
	{
	}

	// lookup event
	public void resultChanged(LookupEvent ev)
	{
		if(isReadOnly()) return;
		Lookup.Result source = (Lookup.Result) ev.getSource();
		if(source.allInstances().isEmpty())
		{
			getColumnView().removeColumnsAfter(this);
		} 
		else
		{
			for (Object obj: source.allInstances())
			{
				if (obj instanceof ColumnProvider)
				{
					ColumnProvider columnProvider = (ColumnProvider)obj;
					Node selNode = new AbstractNode(Children.LEAF,
							Lookups.singleton(columnProvider));
					getExplorerManager().setRootContext(selNode);
					try
					{
						getExplorerManager().setSelectedNodes(new Node[]{selNode});
					} catch (PropertyVetoException ex)
					{
					}
					break;
				}
			}
		}
	}

	/**
	 * Recursively enable/disable all components in the hierarchy under parent
	 */
	private static void setEnabledComponent(Component component, boolean enabled)
	{
		component.setEnabled(enabled);
		if(component instanceof Container)
		{
			Component[] children = ((Container)component).getComponents();
			for (Component child:children)
			{
				setEnabledComponent(child, enabled);
			}
		}
	}

	/**
	 * Inner class implementing customizer.
	 * Its just a dummy customizer.
	 */
	static class DummyDetails extends AbstractCustomizer
	{
		
		static final long serialVersionUID = 1L;
		
		public DummyDetails()
		{
			super();
			initialize();
		}
		
		private void initialize()
		{
			JTextArea textArea = new JTextArea();
			textArea.setText(NbBundle.getMessage(DetailsColumn.class, "LBL_DetailsColumn_NoCustomizer"));
			textArea.setColumns(25);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);
			textArea.setBorder(new EmptyBorder(3,3,3,3));
			add(textArea);
		}
		
		public void apply()
		{
			// do nothing
			setSaveEnabled(false);
		}
		
		public void reset()
		{
			// do nothing
			setResetEnabled(false);
		}

		public boolean isEditable()
		{
			return false;
		}

		public HelpCtx getHelpCtx()
		{
			return HelpCtx.DEFAULT_HELP;
		}
	}
}