package org.netbeans.modules.uihandler;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Registers and unregisters loggers.
 */
public class Installer extends ModuleInstall {
    private static Queue<LogRecord> logs = new LinkedList<LogRecord>();
    private static UIHandler ui = new UIHandler(logs, false);
    private static UIHandler handler = new UIHandler(logs, true);
        
    
    
    public void restored() {
        Logger log = Logger.getLogger("org.netbeans.ui");
        log.setLevel(Level.FINEST);
        log.addHandler(ui);
        Logger all = Logger.getLogger("");
        all.addHandler(handler);
        /*
        Enumeration<String> en = LogManager.getLogManager().getLoggerNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            if (name.startsWith("org.netbeans.ui")) {
                Logger l = Logger.getLogger(name);
                l.setLevel(Level.FINEST);
            }
        }
         */
    }
    
    public void close() {
        Logger log = Logger.getLogger("org.netbeans.ui");
        log.removeHandler(ui);
        Logger all = Logger.getLogger("");
        all.removeHandler(handler);
    }
    
    public static List<LogRecord> getLogs() {
        synchronized (UIHandler.class) {
            return new ArrayList<LogRecord>(logs);
        }
    }
    
    public boolean closing() {
        List<LogRecord> recs = getLogs();

        SubmitPanel panel = new SubmitPanel();
        
        AbstractNode root = new AbstractNode(new Children.Array());
        for (LogRecord r : recs) {
            root.getChildren().add(new Node[] { new UINode(r) });
        }
        
        panel.getExplorerManager().setRootContext(root);
        
        NotifyDescriptor dd = new NotifyDescriptor.Message(panel, NotifyDescriptor.INFORMATION_MESSAGE);
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        Object res = DialogDisplayer.getDefault().notify(dd);
        
        return res == NotifyDescriptor.OK_OPTION;
    }
}
