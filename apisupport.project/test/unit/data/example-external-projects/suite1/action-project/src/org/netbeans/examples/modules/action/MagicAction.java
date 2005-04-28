package org.netbeans.examples.modules.action;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.examples.modules.lib.LibClass;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
public class MagicAction extends AbstractAction {
    public MagicAction() {
        super("Magic!");
    }
    public void actionPerformed(ActionEvent e) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(LibClass.getMagicToken()));
    }
}
