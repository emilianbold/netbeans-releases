package org.netbeans.modules.java.j2seproject.ui.customizer.vmo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author Rastislav Komara
 */
public class OptionsDialog extends JDialog {

    private OptionsDialog() {
        this((Frame) null);
    }

    private OptionsDialog(Frame owner) {
        super(owner);
        if (owner != null) {
            setTitle(owner.getTitle());
        }
    }

    private OptionsDialog(Dialog owner) {
        super(owner);
        if (owner != null) {
            setTitle(owner.getTitle());
        }
    }

    private OptionsDialog(Window owner) {
        super(owner);
        if (owner != null) {
            setTitle("VM Options");
        }
    }

    void createLayout(final VMOptionEditorPanel.Callback callback, VMOptionsTableModel model) {
        setContentPane(new VMOptionEditorPanel(callback, model));
    }


    public static String showCustomizer(Object owner, String options) throws Exception {
        final String[] result = {options};
        final OptionsDialog od;
        if (owner instanceof Frame) {
            od = new OptionsDialog((Frame)owner);
        } else if (owner instanceof Dialog) {
            od = new OptionsDialog((Dialog)owner);
        } else if (owner instanceof Window) {
            od = new OptionsDialog((Window)owner);
        } else {
            od = new OptionsDialog();
        }
        final VMOptionsTableModel tableModel = new VMOptionsTableModel();
        tableModel.fill(options);
        
        od.createLayout(new VMOptionEditorPanel.Callback() {
            @Override
            public void okButtonActionPerformed(ActionEvent evt) {
                final List<JavaVMOption<?>> vmOptionList = tableModel.getValidOptions();
                StringBuilder sb = new StringBuilder(" ");
                for (JavaVMOption<?> option : vmOptionList) {
                    option.print(sb);
                    sb.append(" ");
                }
                result[0] = sb.toString().trim();
                od.setVisible(false);
            }

            @Override
            public void cancelButtonActionPerformed(ActionEvent evt) {
                od.setVisible(false);
            }
        }, tableModel);

        od.setSize(480,360);        

        setScreenCenter(owner, od);

        od.setModal(true);
        od.setVisible(true);
        od.dispose();
        return result[0];
    }

    private static void setScreenCenter(Object owner, OptionsDialog od) {
        final Rectangle parentRect;
        if (owner instanceof Window) {
            Window window = (Window) owner;
            parentRect = new Rectangle(window.getLocationOnScreen(), window.getSize());
        } else if (owner instanceof Component) {
            Component component = (Component) owner;
            parentRect = new Rectangle(component.getLocationOnScreen(), component.getSize());
        } else {
            parentRect = new Rectangle(new Point(0,0), Toolkit.getDefaultToolkit().getScreenSize());
        }
        od.setLocation(new Point((int) parentRect.getCenterX() - (od.getWidth() >> 1), (int) parentRect.getCenterY() - (od.getHeight() >> 1) ));
    }

}
