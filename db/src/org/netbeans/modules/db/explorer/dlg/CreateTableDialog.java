/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.enterprise.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.DefaultCaret;
import java.io.InputStream;
import com.netbeans.ddl.impl.Specification;
import com.netbeans.ddl.impl.CreateTable;
import com.netbeans.ddl.util.CommandBuffer;
import com.netbeans.ddl.impl.CreateIndex;
import com.netbeans.ddl.util.PListReader;
import javax.swing.event.TableModelEvent;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.util.*;
import com.netbeans.enterprise.modules.db.explorer.infos.DatabaseNodeInfo;

/** 
* xxx
*
* @author Slavek Psenicka
*/
public class CreateTableDialog
{
	boolean result = false;
	Dialog dialog = null;
	JTextField dbnamefield, dbownerfield;
	JTable table;
	JComboBox ownercombo;
	JButton addbtn, delbtn;
	Specification spec;
	private Vector ttab;

	private static Map dlgtab = null;
	private static final String filename = "com/netbeans/enterprise/modules/db/resources/CreateTableDialog.plist";

	public static final Map getProperties()
	{
		if (dlgtab == null) try {
			ClassLoader cl = CreateTableDialog.class.getClassLoader();
			InputStream stream = cl.getResourceAsStream(filename);
			if (stream == null) throw new Exception("unable to open stream "+filename);
			PListReader reader = new PListReader(stream);
			dlgtab = reader.getData();
			stream.close();		
		} catch (Exception e) {
			e.printStackTrace();
			dlgtab = null;
		}
		
		return dlgtab;
	}

	public CreateTableDialog(final Specification spe, DatabaseNodeInfo nfo)	
	{  
		spec = spe;
		try {
			JLabel label;
			JPanel pane = new JPanel();
			pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constr = new GridBagConstraints();
			pane.setLayout(layout);
			pane.setMinimumSize(new Dimension(200,100));
			pane.setPreferredSize(new Dimension(502,200));
			ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
         
			// Table name field
		
			label = new JLabel(bundle.getString("CreateTableName"));
			constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.fill = GridBagConstraints.NONE;
			constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 0;
			layout.setConstraints(label, constr);
			pane.add(label);
        
            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.weighty = 0.0;
            constr.gridx = 1;
            constr.gridy = 0;
			constr.insets = new java.awt.Insets (2, 2, 2, 2);
            dbnamefield = new JTextField(bundle.getString("CreateTableUntitledName"), 50);
            layout.setConstraints(dbnamefield, constr);
            pane.add(dbnamefield);
         
			// Table owner combo
		
			label = new JLabel(bundle.getString("CreateTableOwner"));
			constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.fill = GridBagConstraints.NONE;
			constr.insets = new java.awt.Insets (2, 10, 2, 2);
            constr.gridx = 2;
            constr.gridy = 0;
			layout.setConstraints(label, constr);
			pane.add(label);
        
        	Vector users = new Vector();
        	users.add(nfo.getUser());
        
            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.gridx = 3;
            constr.gridy = 0;
			constr.insets = new java.awt.Insets (2, 2, 2, 2);
            ownercombo = new JComboBox(users);
            ownercombo.setSelectedIndex(0);
            layout.setConstraints(ownercombo, constr);
            pane.add(ownercombo);

			// Table columns in scrollpane

            constr.fill = GridBagConstraints.BOTH;
            constr.weightx = 1.0;
            constr.weighty = 1.0;
            constr.gridx = 0;
            constr.gridy = 1;
            constr.gridwidth = 4;
            constr.gridheight = 3;
			constr.insets = new java.awt.Insets (2, 2, 2, 2);
			table = new DataTable(new DataModel());
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	        JScrollPane scrollpane = new JScrollPane(table);
			scrollpane.setBorder(new BevelBorder(BevelBorder.LOWERED));
			scrollpane.setPreferredSize(new Dimension(300,150));
            layout.setConstraints(scrollpane, constr);
			pane.add(scrollpane);

			// Setup cell editors for table
			
			Map tmap = spec.getTypeMap();
			ttab = new Vector(tmap.size());
			Iterator iter = tmap.keySet().iterator();
			while (iter.hasNext()) {
				String iterkey = (String)iter.next();
				String iterval = (String)tmap.get(iterkey);
				ttab.add(new TypeElement(iterkey, iterval));
			}

			JComboBox combo = new JComboBox(ttab);
			combo.setSelectedIndex(0);
			table.setDefaultEditor(String.class, new DataCellEditor(new JTextField()));
			table.getColumn("type").setCellEditor(new DefaultCellEditor(combo));
			table.getColumn("size").setCellEditor(new DataCellEditor(new ValidableTextField(new TextFieldValidator.integer())));

       		// Button pane
       		
            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.anchor = GridBagConstraints.NORTH;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.gridx = 4;
            constr.gridy = 1;
			constr.insets = new java.awt.Insets (2, 8, 2, 2);
			JPanel btnpane = new JPanel();
			GridLayout btnlay = new GridLayout(2,1,0,5);
			btnpane.setLayout(btnlay);
            layout.setConstraints(btnpane, constr);
			pane.add(btnpane);
       
       		// Button add column
       
			addbtn = new JButton(bundle.getString("CreateTableAddButtonTitle"));
			btnpane.add(addbtn);
			addbtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					DataModel model = (DataModel)table.getModel();
					ColumnItem item = new ColumnItem();
					item.setProperty(ColumnItem.TYPE, ttab.elementAt(0));
					model.addRow(item);
				}
			});

       		// Button delete column
       
			delbtn = new JButton(bundle.getString("CreateTableRemoveButtonTitle"));
			btnpane.add(delbtn);
			delbtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					int idx = table.getSelectedRow();
					if (idx != -1) ((DataModel)table.getModel()).removeRow(idx);
				}
			});
              
			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					boolean disres = true;
					if (event.getSource() == DialogDescriptor.OK_OPTION) {
						result = validate();

						CommandBuffer cbuff = new CommandBuffer();

						if (result) {
							try {
								String tablename = getTableName();
								Vector data = ((DataModel)table.getModel()).getData();
								Vector icols = new Vector();
								CreateTable cmd = spec.createCommandCreateTable(tablename);
								cmd.setObjectOwner((String)ownercombo.getSelectedItem());
								CreateIndex icmd = spec.createCommandCreateIndex(tablename);
								icmd.setIndexName(tablename+"_IDX");
								com.netbeans.ddl.impl.TableColumn cmdcol = null;
								Enumeration enu = data.elements();
								while (enu.hasMoreElements()) {
									ColumnItem enuele = (ColumnItem)enu.nextElement();
									String name = enuele.getName();
									if (enuele.isPrimaryKey()) {
										cmdcol = (com.netbeans.ddl.impl.TableColumn)cmd.createPrimaryKeyColumn(name);
									} else if (enuele.isUnique()) {
										cmdcol = (com.netbeans.ddl.impl.TableColumn)cmd.createUniqueColumn(name);
									} else cmdcol = (com.netbeans.ddl.impl.TableColumn)cmd.createColumn(name);
									
									cmdcol.setColumnType(Specification.getType(enuele.getType().getType()));
									cmdcol.setColumnSize(enuele.getSize());
									cmdcol.setNullAllowed(enuele.allowsNull());
									String defval = enuele.getDefaultValue();
									if (defval != null && defval.length()>0) cmdcol.setDefaultValue(defval);
									if (enuele.isIndexed()) {
										com.netbeans.ddl.impl.TableColumn icol = icmd.specifyColumn(name);
									}
								}
									
								cbuff.add(cmd);
								if (icmd.getColumns().size()>0) cbuff.add(icmd);
								cbuff.execute();
								
							} catch (Exception e) {
								System.out.println(e);
								disres = false;
							}
						} else {
							Toolkit.getDefaultToolkit().beep();	
							disres = false;
						}
					} else result = false;
					
					if (disres) {
						dialog.setVisible(false);
						dialog.dispose();
					}
				}
			};				

			addbtn.doClick();
			DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("CreateTableDialogTitle"), true, listener);
			dialog = TopManager.getDefault().createDialog(descriptor);
			dialog.setResizable(true);
		} catch (MissingResourceException ex) {
			ex.printStackTrace();
		}
    }
    
    public boolean run()
    {
    	if (dialog != null) dialog.setVisible(true);
    	return result;
	}

	public String getTableName()
	{
		return dbnamefield.getText();
	}

	private boolean validate()
	{
		String tname = getTableName();
		if (tname == null || tname.length()<1) return false;
		
		Vector cols = ((DataModel)table.getModel()).getData();
		Enumeration colse = cols.elements();
		while(colse.hasMoreElements()) {
			if (!((ColumnItem)colse.nextElement()).validate()) return false;
		}
		
		return true;
	}

	class DataTable extends JTable
	{
		public DataTable(TableModel model)	
		{
			super(model);
			TableColumnModel cmodel = getColumnModel();
			int i, ccount = model.getColumnCount();
			for (i=0;i<ccount;i++) {
				TableColumn col = cmodel.getColumn(i);
				Map cmap = ColumnItem.getColumnProperty(i);
				col.setIdentifier((String)cmap.get("name"));
				if (cmap.containsKey("width")) col.setPreferredWidth(((Integer)cmap.get("width")).intValue());
				if (cmap.containsKey("minwidth")) col.setMinWidth(((Integer)cmap.get("minwidth")).intValue());
//				if (cmap.containsKey("alignment")) {}
//				if (cmap.containsKey("tip")) ((JComponent)col.getCellRenderer()).setToolTipText((String)cmap.get("tip"));
			}
		}
	}

	class FocusInvoker implements Runnable
	{
		private JTextField xxx;
		public FocusInvoker(JTextField fld)
		{
			xxx=fld;
		}
		
		public void run() {
			xxx.selectAll();
		}
	}

	class DataCellEditor extends DefaultCellEditor 
	{
    	public DataCellEditor(final JTextField x)  
    	{        
    		super(x);  
    		setClickCountToStart(1); 
			x.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
					SwingUtilities.invokeLater(new FocusInvoker(x));
				}
				public void focusLost(FocusEvent e) {
				}
			});
    	}
    }
}