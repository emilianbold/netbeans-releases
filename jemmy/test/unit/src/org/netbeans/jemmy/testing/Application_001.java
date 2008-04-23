package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_001 extends TestDialog {

    JComboBox editable;
    DefaultComboBoxModel editableModel;

    public Application_001() {
	super("Application_001");

	getContentPane().setLayout(new BorderLayout());

	JPanel pane = new JPanel();
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	pane.setLayout(gridbag);

	getContentPane().add(new JScrollPane(pane), BorderLayout.CENTER);

	String[] editable_contents = {"editable_one", "editable_two", "editable_three", "editable_four"};
	editableModel = new DefaultComboBoxModel(editable_contents);
	editable = new JComboBox(editableModel);
	editable.setEditable(true);
	editable.getEditor().addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		editableModel.addElement(editable.getEditor().getItem());
	    }
	});
        editable.setName("editable");

	c.fill = GridBagConstraints.CENTER;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.gridheight = 1;
	c.weighty = 1.0;
	gridbag.setConstraints(editable, c);
	pane.add(editable);
	
	String[] list_contents = {"list_one", "list_two", "list_three", "list_four"};
	JList list = new JList(list_contents);
        list.setName("list");

	c.gridwidth = GridBagConstraints.REMAINDER;
	c.gridheight = 2;
	c.weighty = 1.0;
	gridbag.setConstraints(list, c);
	pane.add(list);

	String[] non_editable_contents = {"non_editable_one", "non_editable_two", "non_editable_three", "non_editable_four"};
	JComboBox non_editable = new JComboBox(non_editable_contents);
	non_editable.setEditable(false);
        non_editable.setName("non_editable");

	c.gridwidth = GridBagConstraints.REMAINDER;
	c.gridheight = 1;
	c.weighty = 1.0;
	gridbag.setConstraints(non_editable, c);
	pane.add(non_editable);

	setSize(200, 200);

        setModal(true);
    }

    public static void main(String[] argv) {
	(new Application_001()).setVisible(true);
    }
}
