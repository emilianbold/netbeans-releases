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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.editor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Registry;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.modules.editor.completion.CompletionImpl;
import org.netbeans.modules.editor.completion.CompletionJList;
import org.openide.text.DocumentLine;


/**
 * Provides access to org.netbeans.modules.editor.completion.CompletionJlist
 * component.
 * Usage:
 * <pre>
 *      CompletionJlist comp = CompletionJlist.showCompletion();
 *      List list = comp.getCompletionItems();
 *      ...
 *      com.hideAll();
 * </pre>
 * @author Martin.Schovanek@sun.com
 */
public class CompletionJListOperator extends JListOperator {
    public static final String INSTANT_SUBSTITUTION = "InstantSubstitution";
    
    /**
     * This constructor is intended to use just for your own risk. 
     * It could happen, that document is changed during invocation and 
     * this costructor fails.
     */
    public CompletionJListOperator() {
        this(findCompletionJList());
    }
    
    private CompletionJListOperator(JList list) {
        super(list);
    }
    
    public List getCompletionItems() throws Exception {
        return getCompletionItems((JList) getSource());
    }
    
    private static List getCompletionItems(JList compJList)
    throws Exception {
        ListModel model = (ListModel) compJList.getModel();
        // dump items to List
        List<Object> data = new ArrayList<Object>(model.getSize());
        for (int i=0; i < model.getSize(); i++) {
            data.add(model.getElementAt(i));
        }
        return data;
    }
    
    private static JList findCompletionJList() {
        // Path to the completion model:
        // CompletionImpl.get().layout.completionPopup.getCompletionScrollPane()
        // .view.getModel()
        CompletionImpl comp = CompletionImpl.get();
        try {
            //CompletionLayout.class
            Field layoutField = CompletionImpl.class.getDeclaredField("layout");
            layoutField.setAccessible(true);
            Object layout = layoutField.get(comp);
            //CompletionLayout.CompletionPopup.class
            Field popupField = layout.getClass().getDeclaredField("completionPopup");
            popupField.setAccessible(true);
            final Object popup = popupField.get(layout);
            //CompletionScrollPane.class
            final Method getCSPaneMethod = popup.getClass().getDeclaredMethod(
                    "getCompletionScrollPane", (Class[])null);
            getCSPaneMethod.setAccessible(true);
            Object compSPane = waitFor(new Waitable() {                
                public Object actionProduced(Object obj) {
                    Object o = null;
                    if (DocumentWatcher.isActive() && DocumentWatcher.isModified()) {
                        return INSTANT_SUBSTITUTION;
                    }
                    try {
                        o = getCSPaneMethod.invoke(popup, (Object[])null);
                    } catch (Exception ex) {
                        throw new JemmyException("Invovation of " +
                                "getCompletionScrollPane() failed", ex);
                    }
                    return o;
                }
                
                public String getDescription() {
                    return "Wait getCompletionScrollPane() not null";
                }
            });
            //CompletionJList.class
            if (compSPane.equals(INSTANT_SUBSTITUTION)) {
                return null;
            }
            Field viewField = compSPane.getClass().getDeclaredField("view");
            viewField.setAccessible(true);
            final CompletionJList compJList =
                    (CompletionJList) viewField.get(compSPane);
            Object result = waitFor(new Waitable() {
                public Object actionProduced(Object obj) {
                    List list = null;
                    if (DocumentWatcher.isActive() && DocumentWatcher.isModified()) {
                        return INSTANT_SUBSTITUTION;
                    }
                    try {
                        list = getCompletionItems(compJList);
                    } catch (java.lang.Exception ex) {
                        throw new JemmyException(getDescription()+" failed", ex);
                    }
                    // chech if it is no a 'Please Wait' item
                    if (list.size() > 0 && !(list.get(0) instanceof String)) {
                        return list;
                    } else {
                        return null;
                    }
                }
                public String getDescription() {
                    return "Wait for completion items data";
                }
            });
            if (result.equals(INSTANT_SUBSTITUTION)) {
                return null;
            }
            return compJList;
        } catch (Exception ex) {
            throw new JemmyException("Cannot find CompletionJList component", ex);
        }
        
    }
    
    private static Object waitFor(Waitable action) {
        Waiter waiter = new Waiter(action);
        try {
            return waiter.waitAction(null);
        } catch (InterruptedException ex) {
            throw new JemmyException(action.getDescription()+" has been " +
                    "interrupted.", ex);
        }
    }
    
    /** Returns a CompletionJListOperator or null in case of
     * instant substitution */
    public static CompletionJListOperator showCompletion() {
        CompletionJListOperator operator = null;
        
        DocumentWatcher.start();
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().showCompletion();
            }
        };
        runInAWT(run);
        // wait CC
        JList list = findCompletionJList();
        if (list != null) {
            operator = new CompletionJListOperator(list);
        }
        DocumentWatcher.stop();
        return operator;
    }
    
    public static void showDocumentation() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().showDocumentation();
            }
        };
        runInAWT(run);
    }
    
    public static void showToolTipPopup() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().showToolTip();
            }
        };
        runInAWT(run);
    }
    
    public static void hideAll() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().hideAll();
            }
        };
        runInAWT(run);
    }
    
    public static void hideCompletion() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().hideCompletion();
            }
        };
        runInAWT(run);
    }
    
    public static void hideDocumentation() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().hideDocumentation();
            }
        };
        runInAWT(run);
    }
    
    public static void hideToolTipPopup() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().hideToolTip();
            }
        };
        runInAWT(run);
    }
    
    private static void runInAWT(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
    
    static class DocumentWatcher {
        private static BaseDocument doc;
        private static boolean modified = false;
        private static boolean active = false;
        
        static DocumentListener listener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                //setModified(true);
            }
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
            }
            public void removeUpdate(DocumentEvent e) {
                //setModified(true);
            }
        };
        
        public static void start() {
            doc = Registry.getMostActiveDocument();
            doc.addDocumentListener(listener);
            modified = false;
            active = true;
        }
        
        public static void stop() {
            if (doc != null) {
                doc.removeDocumentListener(listener);
                doc = null;
            }
            active = false;
        }
        
        public static boolean isModified() {
            if (!active) {
                throw new IllegalStateException("start() must be called before this.");
            }
            return modified;
        }
        
        public static boolean isActive() {
            return active;
        }

        private static void setModified(boolean b) {
            modified = b;
            if(doc!=null){
                doc.removeDocumentListener(listener);
                doc = null;
            }
        }
        
    }
}
