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

package org.netbeans.modules.beans;

import java.awt.Dialog;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.beans.Introspector;
import javax.jmi.reflect.JmiException;

import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.JavaClass;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.CookieSet;
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
public final class  PatternGroupNode extends AbstractNode {

    /** Pattern types */
    static final int PATTERN_KIND_PROPERTY = 0;
    static final int PATTERN_KIND_IDX_PROPERTY = 1;
    static final int PATTERN_KIND_UC_EVENT_SET = 2;
    static final int PATTERN_KIND_MC_EVENT_SET = 3;

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


    private static final String ICON_BASE =
        "org/netbeans/modules/beans/resources/patternGroup.gif"; // NOI18N

    public PatternGroupNode(PatternChildren children) {
        this(children, true);
    }

    public PatternGroupNode(PatternChildren children, boolean isWriteable) {
        super(children);
        this.isWritable = isWriteable;
        if (isWritable) {
            setActions(DEFAULT_ACTIONS);
        } else {
            setActions(DEFAULT_ACTIONS_NON_WRITEABLE);
        }
        setName(PatternNode.getString("Patterns"));
        setShortDescription (PatternNode.getString("Patterns_HINT"));
        setIconBaseWithExtension(ICON_BASE);

        CookieSet cs = getCookieSet();
        cs.add(children.getPatternAnalyser());
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
                           JMIUtils.beginTrans(true);
                           boolean rollback = true;
                           try {
                               createElement(kind);
                               rollback = false;
                           } finally {
                               JMIUtils.endTrans(rollback);
                           }
                       } catch (Exception e) {
                           IOException ioe = new IOException();
                           ioe.initCause(e);
                           throw ioe;
                       }

                   }
               };
    }

    /** Creates elements of the given kind
    * @param kind The kind of the element to create.
    * @exception JmiException if action is not allowed.
    */
    private void createElement(int kind) throws JmiException, GenerateBeanException {
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

                PropertyPattern.create( getPatternAnalyser(),
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
                IdxPropertyPattern.create( getPatternAnalyser(),
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

            dd = new DialogDescriptor( (uEventSetPanel = new UEventSetPatternPanel( getPatternAnalyser())),
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
                EventSetPattern.create( getPatternAnalyser(),
                                        result.type, result.implementation, result.firing,
                                        result.passEvent, true );
            }
            return;
        case PATTERN_KIND_MC_EVENT_SET:
            EventSetPatternPanel eventSetPanel;

            dd = new DialogDescriptor( (eventSetPanel = new EventSetPatternPanel( getPatternAnalyser() )),
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
                EventSetPattern.create( getPatternAnalyser(),
                                        result.type, result.implementation, result.firing,
                                        result.passEvent, false );
            }
            return;
        }
    }

    /** Checks if there exists a pattern with given name in the class
     * @param name The name of the pattern
     * @return True if pattern with given name exists otherwise false.
     */
    boolean propertyExists( String name  ) {
        Collection[] patterns = new Collection[2];
        String decapName = Introspector.decapitalize( name );
        PatternAnalyser pa = getPatternAnalyser();
        if (!pa.isAnalyzed()) {
            // XXX check threading here
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

    /** Checks if there exists a pattern for given type
     * @param type type to query patern
     * @return event set pattern if pattern with given name exists otherwise null.
     */
    EventSetPattern findEventSetPattern( Type type ) throws JmiException {
        assert JMIUtils.isInsideTrans();
        if (!(type instanceof JavaClass)) {
            return null;
        }
        String name = Introspector.decapitalize( ((JavaClass) type).getSimpleName() );

        Collection/*<EventSetPattern>*/ eventSets = getPatternAnalyser().getEventSetPatterns();
        for (Iterator/*<EventSetPattern>*/ it = eventSets.iterator(); it.hasNext();) {
            EventSetPattern pattern = (EventSetPattern) it.next();
            if (name.equals(pattern.getName())) {
                return pattern;
            }
        }

        return null;
    }
    
    private PatternAnalyser getPatternAnalyser() {
        return (PatternAnalyser) getCookie(PatternAnalyser.class);
    }
    
}