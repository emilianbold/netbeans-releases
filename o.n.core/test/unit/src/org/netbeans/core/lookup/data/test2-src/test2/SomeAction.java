package test2;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
public class SomeAction extends AbstractAction {
    public static final long TIME_CLASS_LOADED = System.currentTimeMillis();
    public SomeAction() {
        super("SomeAction");
    }
    public void actionPerformed(ActionEvent e) {
    }
}
