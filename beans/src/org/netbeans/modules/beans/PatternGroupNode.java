/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Collection;
import java.util.Iterator;
import java.beans.Introspector;

import org.openide.src.SourceException;
import org.openide.src.Type;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.util.datatransfer.NewType;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.actions.NewAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;

import org.netbeans.modules.beans.beaninfo.GenerateBeanInfoAction;

/** Subnodes of this node are nodes representing source code patterns i.e.
 * PropertyPatternNode or EventSetPatternNode.
 *
 * @author Petr Hrebejk
 */
public class  PatternGroupNode extends AbstractNode {

    /** Menu labels */
    private static final String MENU_CREATE_PROPERTY;
    private static final String MENU_CREATE_IDXPROPERTY;
    private static final String MENU_CREATE_UNICASTSE;
    private static final String MENU_CREATE_MULTICASTSE;

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
    private boolean wri = true;

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


    static {
        ResourceBundle bundle = PatternNode.bundle;
        MENU_CREATE_PROPERTY = bundle.getString("MENU_CREATE_PROPERTY");
        MENU_CREATE_IDXPROPERTY = bundle.getString("MENU_CREATE_IDXPROPERTY");
        MENU_CREATE_UNICASTSE = bundle.getString("MENU_CREATE_UNICASTSE");
        MENU_CREATE_MULTICASTSE = bundle.getString("MENU_CREATE_MULTICASTSE");
    }

    public static final String ICON_BASE =
        "/org/netbeans/modules/beans/resources/patternGroup"; // NOI18N

    public PatternGroupNode( PatternChildren children ) {
        super( (Children)children );
        setName( PatternNode.bundle.getString( "Patterns" ) );
        setShortDescription ( PatternNode.bundle.getString( "Patterns_HINT" ) );
        setIconBase( ICON_BASE );
        setActions(DEFAULT_ACTIONS);

        CookieSet cs = getCookieSet();
        cs.add( children.getPatternAnalyser() );
    }

    public PatternGroupNode( PatternChildren children, boolean isWriteable ) {
        this( children );
        wri = isWriteable;
        if ( !wri ) {
            setActions(DEFAULT_ACTIONS_NON_WRITEABLE);
        }
    }

    /*
    public Node cloneNode() {
      return new PatternGroupNode( ((PatternChildren) getChildren()).cloneChildren() );
}
    */

    public HelpCtx getHelpCtx () {
        return new HelpCtx (PatternGroupNode.class);
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
                   createNewType(MENU_CREATE_PROPERTY, PATTERN_KIND_PROPERTY ),
                   createNewType(MENU_CREATE_IDXPROPERTY, PATTERN_KIND_IDX_PROPERTY ),
                   createNewType(MENU_CREATE_UNICASTSE, PATTERN_KIND_UC_EVENT_SET ),
                   createNewType(MENU_CREATE_MULTICASTSE, PATTERN_KIND_MC_EVENT_SET  ),
               };
        /*
    }
        else {
          // no new types
          return super.getNewTypes();
    }
        */
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

                   /** Help context */
                   public org.openide.util.HelpCtx getHelpCtx() {
                       return new org.openide.util.HelpCtx (PatternGroupNode.class.getName () + "." + name); // NOI18N
                   }

                   /** Creates new element */
                   public void create () throws IOException {
                       try {
                           createElement(kind);
                       }
                       catch (SourceException e) {
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

            /*
            dd = new DialogDescriptor( (propertyPanel = new PropertyPatternPanel()),
              PatternNode.bundle.getString( "CTL_TITLE_NewProperty"),            // Title
              true,                                                 // Modal
              new Object[] { NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION },  // Option list
              NotifyDescriptor.OK_OPTION,                           // Default
              DialogDescriptor.BOTTOM_ALIGN,                        // Align
              new HelpCtx (PatternGroupNode.class.getName () + ".dialogProperty"), // Help
              propertyPanel );
            */

            dd = new DialogDescriptor( (propertyPanel = new PropertyPatternPanel()),
                                       PatternNode.bundle.getString( "CTL_TITLE_NewProperty"),            // Title
                                       true,                                                 // Modal
                                       propertyPanel );
            dd.setHelpCtx (new HelpCtx (PatternGroupNode.class.getName () + ".dialogProperty")); // NOI18N
            dd.setClosingOptions( new Object[]{} );

            dialog = TopManager.getDefault().createDialog( dd );
            propertyPanel.setDialog( dialog );
            propertyPanel.setForInterface( forInterface );
            propertyPanel.setGroupNode( this );
            dialog.show ();

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
                                       PatternNode.bundle.getString( "CTL_TITLE_NewIdxProperty"),     // Title
                                       true,                                                       // Modal
                                       NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
                                       NotifyDescriptor.OK_OPTION,                           // Default
                                       DialogDescriptor.BOTTOM_ALIGN,                        // Align
                                       new HelpCtx (PatternGroupNode.class.getName () + ".dialogIdxProperty"), // Help // NOI18N
                                       idxPropertyPanel );
            dd.setClosingOptions( new Object[]{} );

            dialog = TopManager.getDefault().createDialog( dd );
            idxPropertyPanel.setDialog( dialog );
            idxPropertyPanel.setForInterface( forInterface );
            idxPropertyPanel.setGroupNode( this );
            dialog.show ();

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

            dd = new DialogDescriptor( (uEventSetPanel = new UEventSetPatternPanel()),
                                       PatternNode.bundle.getString( "CTL_TITLE_NewUniCastES"),     // Title
                                       true,                                                 // Modal
                                       NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
                                       NotifyDescriptor.OK_OPTION,                           // Default
                                       DialogDescriptor.BOTTOM_ALIGN,                        // Align
                                       new HelpCtx (PatternGroupNode.class.getName () + ".dialogUniCastES"), // Help // NOI18N
                                       uEventSetPanel );
            dd.setClosingOptions( new Object[]{} );

            dialog = TopManager.getDefault().createDialog( dd );
            uEventSetPanel.setDialog( dialog );
            uEventSetPanel.setForInterface( forInterface );
            uEventSetPanel.setGroupNode( this );
            dialog.show ();

            if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
                UEventSetPatternPanel.Result result = uEventSetPanel.getResult();
                EventSetPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(),
                                        result.type, result.implementation, result.firing,
                                        result.passEvent, true );
            }
            return;
        case PATTERN_KIND_MC_EVENT_SET:
            EventSetPatternPanel eventSetPanel;

            dd = new DialogDescriptor( (eventSetPanel = new EventSetPatternPanel()),
                                       PatternNode.bundle.getString( "CTL_TITLE_NewMultiCastES"),     // Title
                                       true,                                                 // Modal
                                       NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
                                       NotifyDescriptor.OK_OPTION,                           // Default
                                       DialogDescriptor.BOTTOM_ALIGN,                        // Align
                                       new HelpCtx (PatternGroupNode.class.getName () + ".dialogMultiCastES"), // Help // NOI18N
                                       eventSetPanel );
            dd.setClosingOptions( new Object[]{} );

            dialog = TopManager.getDefault().createDialog( dd );
            eventSetPanel.setDialog( dialog );
            eventSetPanel.setForInterface( forInterface );
            eventSetPanel.setGroupNode( this );
            dialog.show ();

            if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
                EventSetPatternPanel.Result result = eventSetPanel.getResult();
                EventSetPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(),
                                        result.type, result.implementation, result.firing,
                                        result.passEvent, false );
            }
            //EventSetPattern.create( ((PatternChildren)getChildren()).getPatternAnalyser(), "NewEventListener", "java.util.EventListener", false ); // NOI18N
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

        patterns[0] = ((PatternChildren)getChildren()).getPatternAnalyser().getPropertyPatterns();
        patterns[1] = ((PatternChildren)getChildren()).getPatternAnalyser().getIdxPropertyPatterns();

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
}

/*
 * Log
 *  13   Gandalf   1.12        1/13/00  Petr Hrebejk    i18n mk3
 *  12   Gandalf   1.11        1/12/00  Petr Hrebejk    i18n  
 *  11   Gandalf   1.10        1/11/00  Jesse Glick     Context help.
 *  10   Gandalf   1.9         11/11/99 Jesse Glick     Display miscellany.
 *  9    Gandalf   1.8         11/10/99 Petr Hrebejk    Canged to work with 
 *       DialogDescriptor.setClosingOptions()
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         9/15/99  Petr Hrebejk    Duplicity recognization 
 *       by EventSets made better
 *  6    Gandalf   1.5         9/13/99  Petr Hrebejk    Creating multiple 
 *       Properties/EventSet with the same name vorbiden. Forms made i18n
 *  5    Gandalf   1.4         8/9/99   Petr Hrebejk    Decapitalization of 
 *       property name
 *  4    Gandalf   1.3         7/26/99  Petr Hrebejk    Better implementation of
 *       patterns resolving
 *  3    Gandalf   1.2         7/21/99  Petr Hrebejk    Bug fixes interface 
 *       bodies, is for boolean etc
 *  2    Gandalf   1.1         7/8/99   Jesse Glick     Context help.
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 
