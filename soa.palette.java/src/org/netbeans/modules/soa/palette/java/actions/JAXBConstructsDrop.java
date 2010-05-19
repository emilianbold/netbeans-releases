/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.soa.palette.java.actions;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.palette.java.codegen.CodegenFactory;
import org.netbeans.modules.soa.palette.java.ui.JAXBConstructsrPnl;
import org.netbeans.modules.soa.palette.java.util.ProjectHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author gpatil
 */
public class JAXBConstructsDrop implements ActiveEditorDrop, Transferable  {
    private String getMessage(String key){
        return NbBundle.getMessage(JAXBConstructsDrop.class, key);
    }
    public boolean handleTransfer(JTextComponent doc) {
        Project prj = ProjectHelper.getProject(doc);
        JAXBConstructsrPnl pnl = new JAXBConstructsrPnl(prj, doc);
        DialogDescriptor dd = new DialogDescriptor(pnl, getMessage("LBL_JAXBConstructsTitle"));//NOI18N
        dd.setHelpCtx(new HelpCtx("org.netbeans.modules.soa.palette.java.jaxb.construct")); //NOI18N                
        pnl.setDD(dd);
        DialogDisplayer.getDefault().notify(dd);
        if (dd.getValue() == NotifyDescriptor.OK_OPTION) {
            try {
                CodegenFactory.getCogegenerator(CodegenFactory.JAXB_CONSTRUCTS).
                        generateCode(doc, pnl.getData());
            } catch (Exception ex){
                Exceptions.printStackTrace(ex);
            }
        }
        return true;
    }

    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor delegatorFlavor[] = new DataFlavor[1];
        delegatorFlavor[0] = ActiveEditorDrop.FLAVOR;
        return delegatorFlavor;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return ActiveEditorDrop.FLAVOR == flavor;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return (flavor == ActiveEditorDrop.FLAVOR) ? this : null;
    }
}
