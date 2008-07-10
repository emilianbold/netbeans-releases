package org.netbeans.jemmy.testing;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.netbeans.jemmy.operators.*;
public class Application_036 extends TestFrame {

    Button btt;
    PopupMenu pm;
    MenuItem mi;
    Menu m;
    Label l;
    ScrollPane sp;

    public Application_036() {
	super("Application_036");

	btt = new Button("Button");
	btt.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    l.setText("button pushed");
		}
	    });
	Checkbox chb = new Checkbox("Checkbox");
	Choice chs = new Choice();
	chs.add("One");
	chs.add("Two");
	chs.add("Three");
	TextField tf = new TextField("Very old text");
	TextArea ta = new TextArea("Three\n short\n lines\n");
	List ls = new List();
	ls.add("One");
	ls.add("Two");
	ls.add("Three");
	ls.add("Four");
	l = new Label("========================================");

	Panel pane = new Panel();
	pane.setLayout(new FlowLayout());
	pane.add(btt);
	pane.add(chb);
	pane.add(chs);
	pane.add(tf);
	pane.add(ta);
	pane.add(ls);
	pane.add(l);

	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(pane, BorderLayout.CENTER);

	setSize(600, 600);
    }

    public static void main(String[] argv) {
	new Application_036().show();
    }
}
