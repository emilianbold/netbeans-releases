package org.netbeans.jemmy.testing;
import java.awt.*;
import javax.swing.*;
import org.netbeans.jemmy.operators.*;
public class Application_037 extends TestFrame {
    public Application_037() {

	super("Application_037");

	Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
	size.width = size.width / 2;
	size.height = size.height / 2;

	JScrollBar swhscroll = new JScrollBar(JScrollBar.HORIZONTAL, 0, 10, 0, 100);
	JScrollBar swvscroll = new JScrollBar(JScrollBar.VERTICAL  , 0, 10, 0, 100);
	JPanel swingPane = new JPanel();
	swingPane.setLayout(new BorderLayout());
	swingPane.add(swvscroll, BorderLayout.EAST);
	swingPane.add(swhscroll, BorderLayout.SOUTH);

	Scrollbar awhscroll = new Scrollbar(JScrollBar.HORIZONTAL, 0, 10, 0, 100);
	Scrollbar awvscroll = new Scrollbar(JScrollBar.VERTICAL  , 0, 10, 0, 100);
	JPanel awtPane = new JPanel();
	awtPane.setLayout(new BorderLayout());
	awtPane.add(awvscroll, BorderLayout.EAST);
	awtPane.add(awhscroll, BorderLayout.SOUTH);

	JTabbedPane tabbed = new JTabbedPane();
	tabbed.add("AWT", awtPane);
	tabbed.add("Swing", swingPane);

	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(tabbed, BorderLayout.CENTER);

	setSize(size);

    }
    public static void main(String[] argv) {
	new Application_037().show();
    }
}
