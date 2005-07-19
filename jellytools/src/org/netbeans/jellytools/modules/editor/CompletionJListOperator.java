/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.editor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.modules.editor.completion.CompletionImpl;
import org.netbeans.modules.editor.completion.CompletionJList;


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
    
    public CompletionJListOperator() {
        this(findCompletionJList());
    }
    
    public CompletionJListOperator(CompletionJList list) {
        super(list);
    }
    
    public List getCompletionItems() throws Exception {
        return getCompletionItems((CompletionJList) getSource());
    }
    
    private static List getCompletionItems(CompletionJList compJList)
    throws Exception {
        ListModel model = (ListModel) compJList.getModel();
        // dump items to List
        List data = new ArrayList(model.getSize());
        for (int i=0; i < model.getSize(); i++) {
            data.add(model.getElementAt(i));
        }
        return data;
    }
    
    private static CompletionJList findCompletionJList() {
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
                    "getCompletionScrollPane", null);
            getCSPaneMethod.setAccessible(true);
            Object compSPane = waitFor(new Waitable() {
                public Object actionProduced(Object obj) {
                    Object o = null;
                    try {
                        o = getCSPaneMethod.invoke(popup, null);
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
            Field viewField = compSPane.getClass().getDeclaredField("view");
            viewField.setAccessible(true);
            final CompletionJList compJList =
                    (CompletionJList) viewField.get(compSPane);
            waitFor(new Waitable() {
                public Object actionProduced(Object obj) {
                    List list = null;
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
    
    public static CompletionJListOperator showCompletion() {
        Runnable run = new Runnable() {
            public void run() {
                Completion.get().showCompletion();
            }
        };
        runInAWT(run);
        // wait CC
        return new CompletionJListOperator();
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
}
