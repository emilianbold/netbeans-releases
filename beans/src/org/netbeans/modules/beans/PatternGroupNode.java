/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import java.awt.Dialog;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;
import java.beans.Introspector;
import java.util.Set;
import java.util.WeakHashMap;
import javax.jmi.reflect.InvalidObjectException;

import org.netbeans.modules.javacore.internalapi.ParsingListener;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.jmiimpl.javamodel.ResourceImpl;
import org.openide.loaders.DataObject;
import org.openide.src.ClassElement;

import org.openide.src.SourceException;
import org.openide.src.Type;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.actions.NewAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;

import org.netbeans.modules.beans.beaninfo.GenerateBeanInfoAction;
import org.openide.DialogDisplayer;

/** Subnodes of this node are nodes representing source code patterns i.e.
 * PropertyPatternNode or EventSetPatternNode.
 *
 * @author Petr Hrebejk
 */
public class  PatternGroupNode extends AbstractNode {

    /** Pattern types */
    static final int PATTERN_KIND_PROPERTY = 0;
    static final int PATTERN_KIND_IDX_PROPERTY = 1;
    static final int PATTERN_KIND_UC_EVENT_SET = 2;
    static final int PATTERN_KIND_MC_EVENT_SET = 3;

    // Panel and dialog for new Property Pattern
    private PropertyPatternPanel newPropertyPanel = null;
    private static DialogDescriptor newPropertyDialog = null;

    // Panel and dialog for new Indexed Property Pattern
    private IdxPropertyPatternPanel newIdxPropertyPanel = null;
    private static DialogDescriptor newIdxPropertyDialog = null;

    // Panel and dialog for new multicast EventSet Pattern
    private EventSetPatternPanel newMcEventSetPanel = null;
    private static DialogDescriptor newMcEventSetDialog = null;

    // Is the node writable?
    private boolean isWritable = true;

    /** Array of the actions of the java methods, constructors and fields. */
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
                SystemAction.get(GenerateBeanInfoAction.class),
                null,
                SystemAction.get(NewAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class),
            };

    /** Array of the actions of the java methods, constructors and fields. */
    private static final SystemAction[] DEFAULT_ACTIONS_NON_WRITEABLE = new SystemAction[] {
                SystemAction.get(GenerateBeanInfoAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class),
            };


    public static final String ICON_BASE =
        "org/netbeans/modules/beans/resources/patternGroup"; // NOI18N

    private static BeanParsingListener beanParsingListener;
    
    boolean isJDK15 = false;
    
    private ClassElement classElement;
    
    private PatternFilter filter;
    
    
    public PatternGroupNode(PatternChildren children, ClassElement clsElem, PatternFilter filter) {
        super(children);
        setName(PatternNode.getString("Patterns"));
        setShortDescription (PatternNode.getString("Patterns_HINT"));
        setIconBase(ICON_BASE);
        setActions(DEFAULT_ACTIONS);
        this.classElement = clsElem;
        this.filter = filter;

        if (isWritable) {
            DataObject dobj = (DataObject)clsElem.getCookie(DataObject.class);
            addParsingListener(dobj, new ParsListener(dobj, this));
        }

        CookieSet cs = getCookieSet();
        cs.add(((PatternChildren)children).getPatternAnalyser());
    }

    public PatternGroupNode(PatternChildren children, ClassElement clsElem, PatternFilter filter, boolean isWriteable, boolean isJDK15) {
        this(children, clsElem, filter);
        this.isWritable = isWriteable;
        this.isJDK15 = isJDK15;
        if (!isWritable || isJDK15) {
            setActions(DEFAULT_ACTIONS_NON_WRITEABLE);
        }
    }

    /** Set all actions for this node.
    * @param actions new list of actions
    */
    public void setActions(SystemAction[] actions) {
        systemActions = actions;
    }

    // ================= NewTypes ====================================

    /* Get the new types that can be created in this node.
    * For example, a node representing a Java package will permit classes to be added.
    * @return array of new type operations that are allowed
    */
    public NewType[] getNewTypes() {
        //if (writeable) {
        return new NewType[] {
                   createNewType(PatternNode.getString("MENU_CREATE_PROPERTY"), PATTERN_KIND_PROPERTY ),
                   createNewType(PatternNode.getString("MENU_CREATE_IDXPROPERTY"), PATTERN_KIND_IDX_PROPERTY ),
                   createNewType(PatternNode.getString("MENU_CREATE_UNICASTSE"), PATTERN_KIND_UC_EVENT_SET ),
                   createNewType(PatternNode.getString("MENU_CREATE_MULTICASTSE"), PATTERN_KIND_MC_EVENT_SET  ),
               };
    }

    /** Create one new type with the given name and the kind.
    */
    private NewType createNewType(final String name, final int kind) {
        return new NewType() {
                   /** Get the name of the new type.
                   * @return localized name.
                   */
                   public String getName() {
                       return name;
                   }

                   /** Creates new element */
                   public void create () throws IOException {
                       try {
                           createElement(kind);
                       }
                       catch (SourceException e) {
                           if (e instanceof SourceException.IO) {
                               if (((SourceException.IO) e).getReason() == null) {
                                   DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.INFORMATION_MESSAGE));
                                   return ;
                               }
                           }
                           e.printStackTrace();
                           throw new IOException(e.getMessage());
                       }
                   }
               };
    }

    /** Creates elements of the given kind
    * @param kind The kind of the element to create.
    * @exception SourceException if action is not allowed.
    */
    private void createElement(int kind) throws SourceException {
        DialogDescriptor dd;
        Dialog           dialog;
        boolean          forInterface = false;

        PatternAnalyser pa = (PatternAnalyser)this.getCookie( PatternAnalyser.class );
        if ( pa != null )
            forInterface = pa.getClassElement() == null ? false : pa.getClassElement().isInterface();

        switch (kind) {
        case PATTERN_KIND_PROPERTY:
            PropertyPatternPanel propertyPanel;

            dd = new DialogDescriptor( (propertyPanel = new PropertyPatternPanel()),
                                       PatternNode.getString( "CTL_TITLE_NewProperty"),            // Title
                                       true,                                                 // Modal
                                       propertyPanel );
            dd.setHelpCtx (new HelpCtx (HelpCtxKeys.BEAN_PROPERTIES_HELP));
            dd.setClosingOptions( new Object[]{} );

            dialog = DialogDisplayer.getDefault().createDialog( dd );
            propertyPanel.setDialog( dialog );
            propertyPanel.setForInterface( forInterface );
            propertyPanel.setGroupNode( this );
            dialog.setVisible(true);

            if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
                PropertyPatternPanel.Result result = propertyPanel.getResult();

                PropertyPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(),
                                        Introspector.decapitalize( result.name ),
                                        result.type, result.mode,
                                        result.bound, result.constrained,
                                        result.withField, result.withReturn, result.withSet,
                                        result.withSupport);

            }
            return;
        case PATTERN_KIND_IDX_PROPERTY:
            IdxPropertyPatternPanel idxPropertyPanel;

            dd = new DialogDescriptor( (idxPropertyPanel = new IdxPropertyPatternPanel()),
                                       PatternNode.getString( "CTL_TITLE_NewIdxProperty"),   // Title
                                       true,                                                 // Modal
                                       NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
                                       NotifyDescriptor.OK_OPTION,                           // Default
                                       DialogDescriptor.BOTTOM_ALIGN,                        // Align
                                       new HelpCtx (HelpCtxKeys.BEAN_PROPERTIES_HELP), // Help // NOI18N
                                       idxPropertyPanel );
            dd.setClosingOptions( new Object[]{} );

            dialog = DialogDisplayer.getDefault().createDialog( dd );
            idxPropertyPanel.setDialog( dialog );
            idxPropertyPanel.setForInterface( forInterface );
            idxPropertyPanel.setGroupNode( this );
            dialog.setVisible(true);

            if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {



                IdxPropertyPatternPanel.Result result = idxPropertyPanel.getResult();
                IdxPropertyPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(),
                                           Introspector.decapitalize( result.name ),
                                           result.type, result.mode,
                                           result.bound, result.constrained,
                                           result.withField, result.withReturn, result.withSet,
                                           result.withSupport,
                                           result.niGetter, result.niWithReturn,
                                           result.niSetter, result.niWithSet );
            }
            return;
        case PATTERN_KIND_UC_EVENT_SET:
            UEventSetPatternPanel uEventSetPanel;

            dd = new DialogDescriptor( (uEventSetPanel = new UEventSetPatternPanel( ((PatternChildren)getChildren()).getPatternAnalyser())),
                                       PatternNode.getString( "CTL_TITLE_NewUniCastES"),     // Title
                                       true,                                                 // Modal
                                       NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
                                       NotifyDescriptor.OK_OPTION,                           // Default
                                       DialogDescriptor.BOTTOM_ALIGN,                        // Align
                                       new HelpCtx (HelpCtxKeys.BEAN_EVENTSETS_HELP), // Help // NOI18N
                                       uEventSetPanel );
            dd.setClosingOptions( new Object[]{} );

            dialog = DialogDisplayer.getDefault().createDialog( dd );
            uEventSetPanel.setDialog( dialog );
            uEventSetPanel.setForInterface( forInterface );
            uEventSetPanel.setGroupNode( this );
            dialog.setVisible(true);

            if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
                UEventSetPatternPanel.Result result = uEventSetPanel.getResult();
                EventSetPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(),
                                        result.type, result.implementation, result.firing,
                                        result.passEvent, true );
            }
            return;
        case PATTERN_KIND_MC_EVENT_SET:
            EventSetPatternPanel eventSetPanel;

            dd = new DialogDescriptor( (eventSetPanel = new EventSetPatternPanel( ((PatternChildren)getChildren()).getPatternAnalyser() )),
                                       PatternNode.getString( "CTL_TITLE_NewMultiCastES"),   // Title
                                       true,                                                 // Modal
                                       NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
                                       NotifyDescriptor.OK_OPTION,                           // Default
                                       DialogDescriptor.BOTTOM_ALIGN,                        // Align
                                       new HelpCtx (HelpCtxKeys.BEAN_EVENTSETS_HELP), // Help // NOI18N
                                       eventSetPanel );
            dd.setClosingOptions( new Object[]{} );

            dialog = DialogDisplayer.getDefault().createDialog( dd );
            eventSetPanel.setDialog( dialog );
            eventSetPanel.setForInterface( forInterface );
            eventSetPanel.setGroupNode( this );
            dialog.setVisible(true);

            if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
                EventSetPatternPanel.Result result = eventSetPanel.getResult();
                EventSetPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(),
                                        result.type, result.implementation, result.firing,
                                        result.passEvent, false );
            }
            return;
        }
    }

    /** Checks if there exists a pattern with given name in the class
     * @param kind The kind of the pattern as in CreateElement method.
     * @param name The name of the pattern
     * @return True if pattern with given name exists otherwise false.
     */
    boolean propertyExists( String name  ) {
        Collection[] patterns = new Collection[2];
        String decapName = Introspector.decapitalize( name );
        PatternAnalyser pa = ((PatternChildren)getChildren()).getPatternAnalyser();
        if (!pa.isAnalyzed()) {
            pa.analyzeAll();
        }

        patterns[0] = pa.getPropertyPatterns();
        patterns[1] = pa.getIdxPropertyPatterns();

        for ( int i = 0; i < patterns.length && patterns[i] != null; i++ ) {
            Iterator it = patterns[i].iterator();
            while( it.hasNext() ) {
                if ( ((Pattern)it.next()).getName().equals( decapName ) ) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Checks if there exists a pattern with given name in the class
     * @param kind The kind of the pattern as in CreateElement method.
     * @param name The name of the pattern
     * @return True if pattern with given name exists otherwise false.
     */
    boolean eventSetExists( Type type ) {

        String name = Introspector.decapitalize( type.getClassName().getName() );

        Collection eventSets = ((PatternChildren)getChildren()).getPatternAnalyser().getEventSetPatterns();

        Iterator it = eventSets.iterator();
        while( it.hasNext() ) {
            if ( ((EventSetPattern)it.next()).getName().equals( name ) ) {
                return true;
            }
        }

        return false;
    }
    
    public void updateChildren(Resource resource) {
        boolean b = hasJDK15Features(resource);
        if (b != isJDK15) {
            isJDK15 = b;
            CookieSet cs = getCookieSet();
            PatternChildren ch = (PatternChildren)getChildren();
            cs.remove(ch.getPatternAnalyser());
            
            PatternChildren children = new PatternChildren(classElement, !isJDK15);
            children.setFilter(filter);
            setChildren(children);
            cs.add(children.getPatternAnalyser());
            setActions(isJDK15 ? DEFAULT_ACTIONS_NON_WRITEABLE : DEFAULT_ACTIONS);
        }
    }
    
    public static boolean hasJDK15Features(Resource resource) {
        try {
            int s = resource.getStatus();
            return ((s & ResourceImpl.HAS_ANNOTATION) > 0 || (s & ResourceImpl.HAS_ANNOTATIONTYPES) > 0 
                || (s & ResourceImpl.HAS_ENUMS) > 0 || (s & ResourceImpl.HAS_GENERICS) > 0);
        } catch (InvalidObjectException e) {
            return false;
        }
    }

    public static void addParsingListener(DataObject dobj, ParsingListener listener) {
        if (beanParsingListener == null) {
            synchronized(PatternGroupNode.class) {
                if (beanParsingListener == null)
                    beanParsingListener = new BeanParsingListener();
            }
        }
        beanParsingListener.addListener(dobj, listener);
    }
    
    public static void removeParsingListener(DataObject dobj, ParsingListener listener) {
        if (beanParsingListener == null)
            return;
        beanParsingListener.removeListener(dobj, listener);
    }
    
    // ..........................................................................
    
    static class ParsListener extends WeakReference implements ParsingListener, Runnable {
        
        private WeakReference dobjRef;
        
        ParsListener(DataObject dobj, PatternGroupNode node) {
            super(node, Utilities.activeReferenceQueue());
            dobjRef = new WeakReference(dobj);
        }
        
        public void resourceParsed(Resource resource) {
            PatternGroupNode node = (PatternGroupNode) get();
            if (node == null) {
                run();
                return;
            }
            node.updateChildren(resource);
        }
        
        public void run() {
            Object dobj = dobjRef.get();
            if (dobj != null) {
                removeParsingListener((DataObject)dobj, this);
            }
        }
        
    }
    
    static class BeanParsingListener implements ParsingListener {

        private WeakHashMap map = new WeakHashMap();

        public BeanParsingListener() {
            // [PENDING] remove listener on module uninstallation
            JavaMetamodel.getManager().addParsingListener(this);
        }
        
        public synchronized void addListener(DataObject dobj, ParsingListener listener) {
            Set set = (Set) map.get(dobj);
            if (set == null) {
                set = new HashSet();
                map.put(dobj, set);
            }
            set.add(listener);
        }
        
        public synchronized void removeListener(DataObject dobj, ParsingListener listener) {
            Set set = (Set) map.get(dobj);
            if (set != null) {
                set.remove(listener);
                if (set.isEmpty())
                    map.remove(dobj);
            }
        }
        
        public synchronized void resourceParsed(Resource resource) {
            DataObject dobj = JavaMetamodel.getManager().getDataObject(resource);
            Set set = (Set)map.get(dobj);
            if (set == null)
                return;
            for (Iterator iter = set.iterator(); iter.hasNext(); ) {
                ParsingListener listener = (ParsingListener) iter.next();
                listener.resourceParsed(resource);
            }
        }
        
    }
    
}