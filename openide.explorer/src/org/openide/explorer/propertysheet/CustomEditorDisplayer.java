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
/*
 * CustomEditorDisplayer.java
 *
 * Created on 19 October 2003, 18:08
 */
package org.openide.explorer.propertysheet;

import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.nodes.Node.*;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

import java.text.MessageFormat;

import javax.swing.*;
import javax.swing.event.*;


/** An implementation of PropertyDisplayer.EDITABLE which manages communication
 * with a custom editor, to replace that aspect of PropertyPanel's behavior.
 *
 * @author  Tim Boudreau
 */
final class CustomEditorDisplayer implements PropertyDisplayer_Editable {
    private int updatePolicy = UPDATE_ON_CONFIRMATION;
    private Property prop;
    private PropertyEnv env = null;
    private PropertyEditor editor = null;
    private Component customEditor = null;
    boolean ignoreChanges = false;
    private PropertyChangeListener editorListener = null;
    private EnvListener envListener = null;
    private PropertyModel model = null;
    private Object originalValue = null;

    /**
     * Utility field used by event firing mechanism.
     */
    private javax.swing.event.EventListenerList listenerList = null;
    private boolean ignoreChanges2 = false;

    //Some property panel specific, package private hacks
    private PropertyChangeListener remoteEnvListener = null;
    private VetoableChangeListener remotevEnvListener = null;

    /** Creates a new instance of CustomEditorDisplayer */
    public CustomEditorDisplayer(Property prop) {
        this.prop = prop;
    }

    public CustomEditorDisplayer(Property prop, PropertyModel mdl) {
        this(prop);
        model = mdl;
    }

    public void setUpdatePolicy(int i) {
        this.updatePolicy = i;

        if (env != null) {
            env.setChangeImmediate(i != UPDATE_ON_EXPLICIT_REQUEST);
        }
    }

    private Component getCustomEditor() {
        if (customEditor == null) {
            customEditor = getPropertyEditor().getCustomEditor();
        }

        return customEditor;
    }

    PropertyEditor getPropertyEditor() { //Package private for unit tests

        if (editor == null) {
            setPropertyEditor(PropUtils.getPropertyEditor(getProperty()));
        }

        return editor;
    }

    public synchronized void dispose() {
        //See issue 38004 - legacy uses of PropertyPanel depend on 
        //no-longer-displayed property panels continuing to listen to changes
        //in a property, when in custom editor mode.  Grotesque, but there it is.
        //First store local copies - we never want the possibility of getting
        //a property change *while* we're switching ourselves out and the
        //spud in
        PropertyEditor pred = editor;
        Property property = prop;
        PropertyEnv penv = env;
        Component custom = customEditor;
        PropertyModel mdl = model;
        PropertyChangeListener pcl = remoteEnvListener;
        VetoableChangeListener vcl = remotevEnvListener;

        //Clear all references to everything - we're done
        setPropertyEnv(null);
        setPropertyEditor(null);
        remotevEnvListener = null;
        remoteEnvListener = null;
        customEditor = null;
        editor = null;
        prop = null;
        model = null;

        //Support the ridiculous use case of a property panel being expected
        //to propagate changes after it no longer exists
        if (getUpdatePolicy() != UPDATE_ON_EXPLICIT_REQUEST) {
            new Spud(property, pred, mdl, penv, custom, pcl, vcl);
        }
    }

    private void setPropertyEditor(PropertyEditor editor) {
        if (this.editor != null) {
            detachFromPropertyEditor(this.editor);

            //set ignore changes even so - we may get the same property editor
            //again, in which case we're still listening to it
            ignoreChanges = true;
        }

        this.editor = editor;

        try {
            if (editor != null) {
                if (!editor.supportsCustomEditor()) {
                    throw new IllegalArgumentException(
                        "Property editor " + editor + " for property " + getProperty() +
                        " does not support a custom editor."
                    ); //NOI18N
                }

                try {
                    originalValue = editor.getValue();
                } catch (Exception e) {
                    //dve or other, don't worry
                }

                //Issue 39437 - PropertyPanel in custom editor mode
                //expects a PropertyEnv even if the editor is not
                //an ExPropertyEditor.
                PropertyEnv env = new PropertyEnv();

                //Use the hack to access the real underlying FD, for, e.g.,
                //core.projects.FileStateEditor
                env.setFeatureDescriptor(EditorPropertyDisplayer.findFeatureDescriptor(this));
                setPropertyEnv(env);

                if (editor instanceof ExPropertyEditor) {
                    ((ExPropertyEditor) editor).attachEnv(env);
                }

                attachToPropertyEditor(editor);
            }
        } finally {
            ignoreChanges = false;
        }
    }

    private void setPropertyEnv(PropertyEnv env) {
        if (this.env != null) {
            detachFromEnv(this.env);
        }

        this.env = env;

        if (env != null) {
            env.setChangeImmediate(getUpdatePolicy() != UPDATE_ON_EXPLICIT_REQUEST);
            attachToEnv(env);
        }
    }

    private void attachToEnv(PropertyEnv env) {
        env.addPropertyChangeListener(getEnvListener());
        env.addVetoableChangeListener(getEnvListener());
        env.setBeans(EditorPropertyDisplayer.findBeans(this));
    }

    private void detachFromEnv(PropertyEnv env) {
        env.removePropertyChangeListener(getEnvListener());
        env.removeVetoableChangeListener(getEnvListener());
    }

    private void attachToPropertyEditor(PropertyEditor editor) {
        //        editor.addPropertyChangeListener(WeakListeners.propertyChange(getEditorListener(), editor));
        editor.addPropertyChangeListener(getEditorListener());
    }

    private void detachFromPropertyEditor(PropertyEditor editor) {
        editor.removePropertyChangeListener(getEditorListener());
    }

    private PropertyChangeListener getEditorListener() {
        if (editorListener == null) {
            editorListener = new EditorListener();
        }

        return editorListener;
    }

    private EnvListener getEnvListener() {
        if (envListener == null) {
            envListener = new EnvListener();
        }

        return envListener;
    }

    public boolean commit() throws IllegalArgumentException {
        //        System.err.println("COMMIT - " + getProperty().getDisplayName());
        try {
            ignoreChanges = true;

            PropertyEditor editor = getPropertyEditor();
            Object entered = getEnteredValue();

            //            System.err.println("COMMIT - entered value: " + entered);
            try {
                if ((entered != null) && entered.equals(getProperty().getValue())) {
                    //                    System.err.println("  entered value matches property value, return false");
                    return false;
                }
            } catch (Exception e) {
                //IllegalAccessException, etc.
                //                System.err.println("  caught an exception, aborting");
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);

                try {
                    if (getProperty().canRead()) {
                        editor.setValue(model.getValue());
                    }
                } catch (ProxyNode.DifferentValuesException dve) {
                    // ok - no problem here, it was just the old value
                } catch (Exception ex) {
                    PropertyDialogManager.notify(ex);
                }

                return false;
            }

            PropertyEnv env = getPropertyEnv();

            Exception exception = PropUtils.updatePropertyEditor(editor, entered);

            if (exception == null) {
                if ((env != null) && PropertyEnv.STATE_NEEDS_VALIDATION.equals(env.getState())) {
                    String msg = env.silentlySetState(env.STATE_VALID, entered);

                    System.err.println("  result of silent set state: " + msg);

                    //something vetoed the change
                    if ((msg != null) && !PropertyEnv.STATE_VALID.equals(env.getState())) {
                        IllegalArgumentException iae = new IllegalArgumentException("Error setting value"); //NOI18N

                        ErrorManager.getDefault().annotate(iae, ErrorManager.USER, null, msg, null, null);

                        //set the state to invalid
                        if (!env.STATE_INVALID.equals(env.getState())) {
                            env.silentlySetState(env.STATE_INVALID, null);
                        }

                        throw iae;
                    }
                }
            }

            Object res = Boolean.FALSE;

            if (exception == null) {
                res = PropUtils.noDlgUpdateProp(getModel(), editor);
                originalValue = editor.getValue();

                if (res instanceof Exception && (!(res instanceof ProxyNode.DifferentValuesException))) {
                    exception = (Exception) res;
                }

                if (res instanceof InvocationTargetException || res instanceof IllegalAccessException) {
                    PropertyDialogManager.notify((Exception) res);
                }
            }

            if (exception != null) {
                if (exception instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) exception;
                } else {
                    PropertyDialogManager.notify(exception);

                    IllegalArgumentException iae = new IllegalArgumentException("Error setting value"); //NOI18N
                    ErrorManager.getDefault().annotate(
                        iae, ErrorManager.USER, null,
                        PropUtils.findLocalizedMessage(exception, entered, getProperty().getDisplayName()), exception,
                        null
                    );
                    throw iae;
                }
            }

            boolean result = Boolean.TRUE.equals(res);

            if (result) {
                fireActionPerformed();
            }

            return result;
        } finally {
            ignoreChanges = false;
        }
    }

    PropertyModel getModel() {
        if (model == null) {
            return new NodePropertyModel(getProperty(), null);
        } else {
            return model;
        }
    }

    void setModel(PropertyModel mdl) {
        model = mdl;
    }

    public PropertyEnv getPropertyEnv() {
        return env;
    }

    public Component getComponent() {
        return getCustomEditor();
    }

    public Object getEnteredValue() {
        PropertyEditor editor = getPropertyEditor();
        Object result;

        if (editor instanceof EnhancedCustomPropertyEditor) {
            result = ((EnhancedCustomPropertyEditor) editor).getPropertyValue();
        } else {
            result = editor.getValue(); //editor.getAsText(); //XXX getValue?
        }

        return result;
    }

    public Property getProperty() {
        return prop;
    }

    public int getUpdatePolicy() {
        return updatePolicy;
    }

    public String isModifiedValueLegal() {
        boolean legal = true;
        String msg = null;
        PropertyEditor editor = getPropertyEditor();

        //        System.err.println("IS MODIFIED VALUE LEGAL");
        if (env != null) {
            legal = env.getState() != env.STATE_INVALID;

            System.err.println(" Attempting to validate env");

            if (legal && env.STATE_NEEDS_VALIDATION.equals(env.getState())) {
                msg = env.silentlySetState(env.STATE_VALID, getEnteredValue());

                //                System.err.println("  silentlySetState returned: " + msg);
                legal = msg == null;
            }
        } else if (editor instanceof EnhancedCustomPropertyEditor) {
            Object entered = ((EnhancedCustomPropertyEditor) editor).getPropertyValue();

            try {
                editor.setValue(entered);
            } catch (IllegalStateException ise) {
                legal = false;
                msg = PropUtils.findLocalizedMessage(ise, entered, getProperty().getDisplayName());
            }
        }

        if (!legal && (msg == null)) {
            //            System.err.println(" not legal, constructing message");
            msg = MessageFormat.format(
                    NbBundle.getMessage(CustomEditorDisplayer.class, "FMT_CannotUpdateProperty"),
                    new Object[] { editor.getValue(), getProperty().getDisplayName() }
                ); //NOI18N
        }

        return msg;
    }

    public boolean isValueModified() {
        PropertyEditor editor = getPropertyEditor();
        boolean result = editor.getValue() != originalValue;

        if (!result && editor instanceof EnhancedCustomPropertyEditor) {
            Object entered = ((EnhancedCustomPropertyEditor) editor).getPropertyValue();

            if (entered != null) {
                result = entered.equals(originalValue);
            } else {
                result = originalValue == null;
            }
        }

        return result;
    }

    public void refresh() {
        //do nothing
    }

    public void reset() {
        try {
            originalValue = getProperty().getValue();
            getPropertyEditor().setValue(originalValue);
        } catch (Exception e) {
            //should not happen - the value came from the property
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
    }

    /** Sets whether or not this component is enabled.
     *
     * all panel components gets disabled when enabled parameter is set false
     * @param enabled flag defining the action.
     */
    public void setEnabled(boolean enabled) {
        //        System.err.println("SET ENABLED:" + enabled);
        Component custEditor = (Container) getComponent();

        if (custEditor instanceof Container) {
            setEnabled((Container) custEditor, enabled);
        }

        custEditor.setEnabled(enabled);
    }

    public void setEnabled(Container c, boolean enabled) {
        Component[] comp = c.getComponents();

        for (int i = 0; i < comp.length; i++) {
            if (!(comp[i] instanceof JScrollBar)) {
                comp[i].setEnabled(false);
            } else {
                ((JScrollBar) comp[i]).setFocusable(enabled);
            }

            if (comp[i] instanceof Container) {
                boolean ignore = false;

                if (comp[i] instanceof JComponent) {
                    //Issue 38065 - form editor doesn't want checkbox enabled,
                    //but for compatibility we need to drill through the entire
                    //subtree (otherwise JFileChoosers, etc., will have enabled
                    //components even though setEnabled(false) was called on them).
                    Boolean val = (Boolean) ((JComponent) comp[i]).getClientProperty("dontEnableMe"); //NOI18N

                    if (val != null) {
                        ignore = val.booleanValue();
                    }
                }

                if (!ignore) {
                    setEnabled((Container) comp[i], enabled);
                }
            }
        }

        c.setEnabled(enabled);
    }

    public void setEnteredValue(Object o) {
        PropUtils.updatePropertyEditor(getPropertyEditor(), o);
    }

    public void setActionCommand(String val) {
    }

    public String getActionCommand() {
        return null;
    }

    /**
     * Registers ActionListener to receive events.
     * @param listener The listener to register.
     */
    public synchronized void addActionListener(java.awt.event.ActionListener listener) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }

        listenerList.add(ActionListener.class, listener);
    }

    /**
     * Removes ActionListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeActionListener(java.awt.event.ActionListener listener) {
        listenerList.remove(ActionListener.class, listener);
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireActionPerformed() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "userChangedValue"); //NOI18N

        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener) listeners[i + 1]).actionPerformed(event);
            }
        }
    }

    /**
     * Registers ChangeListener to receive events.
     * @param listener The listener to register.
     */
    public synchronized void addChangeListener(ChangeListener listener) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }

        listenerList.add(ChangeListener.class, listener);
    }

    /**
     * Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }

    /**
     * Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     */
    private void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);

        if (listenerList == null) {
            return;
        }

        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(event);
            }
        }
    }

    void setRemoteEnvListener(PropertyChangeListener l) {
        //        System.err.println(" setRemoteEnvListener on " + System.identityHashCode(this) + " to " + l);
        remoteEnvListener = l;
    }

    void setRemoteEnvVetoListener(VetoableChangeListener vl) {
        remotevEnvListener = vl;
    }

    private class EnvListener implements PropertyChangeListener, VetoableChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            fireStateChanged();

            //            System.err.println(" Custom displayer got a property change");
            //Propagate changes to a property panel
            if (remoteEnvListener != null) {
                remoteEnvListener.propertyChange(evt);

                //            } else {
                //                System.err.println("But nobody is listening!");
            }
        }

        public void vetoableChange(PropertyChangeEvent evt)
        throws PropertyVetoException {
            if (remotevEnvListener != null) {
                remotevEnvListener.vetoableChange(evt);
            }
        }
    }

    private class EditorListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            //            System.err.println("Property change on CustomEditorDisplayer from " + evt.getSource() + " new value=" + evt.getNewValue());
            if (ignoreChanges) {
                //                System.err.println("  ignoring");
                return;
            }

            if (ExPropertyEditor.PROP_VALUE_VALID.equals(evt.getPropertyName())) {
                //                System.err.println("  value valid - ignoring");
                return;
            }

            if (ignoreChanges2) {
                return;
            }

            ignoreChanges2 = true;

            if (getUpdatePolicy() != UPDATE_ON_EXPLICIT_REQUEST) {
                commit();

                //            } else {
                //                System.err.println("  policy is UPDATE_ON_EXPLICIT_REQUEST - ignoring");
            }

            fireStateChanged();
            ignoreChanges2 = false;
        }
    }

    /** A class to support the truly absurd use case of expecting a property
     * panel to continue to propagate changes after it is offscreen, <i>but</i>
     * not hold any references to the editor, the property, et. al.  Stub is
     * too good a name for it.  Somehow the idea of a small potato left behind
     * by a CustomEditorDisplayer suits my mood, and is printable.  */
    private static final class Spud implements PropertyChangeListener, VetoableChangeListener {
        WeakReference envListener = null;
        WeakReference venvListener = null;
        WeakReference editor = null;
        WeakReference property = null;
        WeakReference env = null;
        WeakReference customEditor = null;
        WeakReference model = null;
        private boolean inGetModel = false;
        private boolean inGetProperty = false;
        private boolean ignoreChanges = false;

        public Spud(
            Property prop, PropertyEditor editor, PropertyModel model, PropertyEnv env, Component customEditor,
            PropertyChangeListener envListener, VetoableChangeListener veto
        ) {
            if (prop != null) {
                property = new WeakReference(property);
            }

            if (editor != null) {
                this.editor = new WeakReference(editor);
            }

            if (env != null) {
                this.env = new WeakReference(env);
                env.addPropertyChangeListener(WeakListeners.propertyChange(this, env));
                env.addVetoableChangeListener(WeakListeners.vetoableChange(this, env));
            }

            if (customEditor != null) {
                this.customEditor = new WeakReference(customEditor);
            }

            if (envListener != null) {
                this.envListener = new WeakReference(envListener);
            }

            if (veto != null) {
                this.venvListener = new WeakReference(veto);
            }

            if (model != null) {
                this.model = new WeakReference(model);
                model.addPropertyChangeListener(WeakListeners.propertyChange(this, editor));
            }

            if (editor != null) {
                editor.addPropertyChangeListener(this);
            }
        }

        private PropertyEnv getEnv() {
            PropertyEnv result = null;

            if (env != null) {
                result = (PropertyEnv) env.get();
            }

            return result;
        }

        private Component getCustomEditor() {
            Component result = null;

            if (customEditor != null) {
                result = (Component) customEditor.get();
            }

            return result;
        }

        private VetoableChangeListener getVetoListener() {
            VetoableChangeListener result = null;

            if (venvListener != null) {
                result = (VetoableChangeListener) venvListener.get();
            }

            return result;
        }

        private PropertyChangeListener getPropListener() {
            PropertyChangeListener result = null;

            if (venvListener != null) {
                result = (PropertyChangeListener) venvListener.get();
            }

            return result;
        }

        private Property getProperty() {
            Property result = null;
            inGetProperty = true;

            try {
                if (property != null) {
                    result = (Property) property.get();
                }

                if ((result == null) && !inGetModel) {
                    PropertyModel mdl = getModel();
                }
            } finally {
                inGetProperty = false;
            }

            return result;
        }

        private synchronized PropertyModel getModel() {
            inGetModel = true;

            PropertyModel result = null;

            try {
                if (model != null) {
                    result = (PropertyModel) model.get();
                }

                if ((result == null) && !inGetProperty) {
                    Property prop = getProperty();

                    if (prop != null) {
                        return new NodePropertyModel(prop, null);
                    }
                }
            } finally {
                inGetModel = false;
            }

            return result;
        }

        private PropertyEditor getPropertyEditor() {
            PropertyEditor result = null;

            if (editor != null) {
                result = (PropertyEditor) editor.get();
            }

            if (result == null) {
                Property prop = getProperty();

                if (prop != null) {
                    result = prop.getPropertyEditor();
                    editor = new WeakReference(result);
                }
            }

            if (result == null) {
                PropertyModel mdl = getModel();

                if (mdl != null) {
                    Property p = ModelProperty.toProperty(mdl);
                    result = p.getPropertyEditor();
                    editor = new WeakReference(result);
                }
            }

            return result;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (ignoreChanges) {
                return;
            }

            if (ExPropertyEditor.PROP_VALUE_VALID.equals(evt.getPropertyName())) {
                //                System.err.println("  value valid - ignoring");
                return;
            }

            ignoreChanges = true;

            try {
                if (evt.getSource() instanceof PropertyEnv) {
                    PropertyChangeListener pcl = getPropListener();

                    if (pcl != null) {
                        pcl.propertyChange(evt);
                    }
                }

                if (evt.getSource() instanceof PropertyModel) {
                }

                if (evt.getSource() instanceof PropertyEditor) {
                    handleChangeFromEditor((PropertyEditor) evt.getSource());
                }
            } finally {
                ignoreChanges = false;
            }
        }

        private void handleChangeFromEditor(PropertyEditor ed) {
            commit(ed);
        }

        public void vetoableChange(PropertyChangeEvent evt)
        throws PropertyVetoException {
            VetoableChangeListener vcl = getVetoListener();

            if (vcl != null) {
                vcl.vetoableChange(evt);
            }
        }

        public Object getEnteredValue(PropertyEditor editor) {
            Object result = null;
            Object custom = getCustomEditor();

            if (editor instanceof EnhancedCustomPropertyEditor) {
                result = ((EnhancedCustomPropertyEditor) editor).getPropertyValue();
            } else if (customEditor instanceof EnhancedCustomPropertyEditor) {
                result = ((EnhancedCustomPropertyEditor) custom).getPropertyValue();
            }

            if (result == null) {
                result = editor.getValue(); //editor.getAsText(); //XXX getValue?
            }

            return result;
        }

        private boolean commit(PropertyEditor editor) {
            //        System.err.println("COMMIT - " + getProperty().getDisplayName());
            try {
                PropertyModel model = getModel();
                Property prop = getProperty();
                Object entered = getEnteredValue(editor);
                Object value;

                if (model != null) {
                    value = model.getValue();
                } else if (prop != null) {
                    value = prop.getValue();
                } else {
                    //Nothing to talk to anymore, everything's been GC'd.
                    if (editor != null) {
                        editor.removePropertyChangeListener(this);
                    }

                    return false;
                }

                PropertyEnv env = getEnv();

                try {
                    if ((entered != null) && entered.equals(value)) {
                        //                    System.err.println("  entered value matches property value, return false");
                        return false;
                    }
                } catch (Exception e) {
                    //IllegalAccessException, etc.
                    //                System.err.println("  caught an exception, aborting");
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);

                    try {
                        if (prop.canRead()) {
                            editor.setValue(model.getValue());
                        }
                    } catch (ProxyNode.DifferentValuesException dve) {
                        // ok - no problem here, it was just the old value
                    } catch (Exception ex) {
                        PropertyDialogManager.notify(ex);
                    }

                    return false;
                }

                Exception exception = PropUtils.updatePropertyEditor(editor, entered);

                if (exception == null) {
                    if ((env != null) && PropertyEnv.STATE_NEEDS_VALIDATION.equals(env.getState())) {
                        String msg = env.silentlySetState(env.STATE_VALID, entered);

                        //something vetoed the change
                        if ((msg != null) && !PropertyEnv.STATE_VALID.equals(env.getState())) {
                            IllegalArgumentException iae = new IllegalArgumentException("Error setting value"); //NOI18N

                            ErrorManager.getDefault().annotate(iae, ErrorManager.USER, null, msg, null, null);

                            //set the state to invalid
                            if (!env.STATE_INVALID.equals(env.getState())) {
                                env.silentlySetState(env.STATE_INVALID, null);
                            }

                            throw iae;
                        }
                    }
                }

                Object res = Boolean.FALSE;

                if (exception == null) {
                    res = PropUtils.noDlgUpdateProp(model, editor);

                    if (res instanceof Exception && (!(res instanceof ProxyNode.DifferentValuesException))) {
                        exception = (Exception) res;
                    }

                    if (res instanceof InvocationTargetException || res instanceof IllegalAccessException) {
                        PropertyDialogManager.notify((Exception) res);
                    }
                }

                if (exception != null) {
                    if (exception instanceof IllegalArgumentException) {
                        throw (IllegalArgumentException) exception;
                    } else {
                        PropertyDialogManager.notify(exception);

                        IllegalArgumentException iae = new IllegalArgumentException("Error setting value"); //NOI18N
                        ErrorManager.getDefault().annotate(
                            iae, ErrorManager.USER, null,
                            PropUtils.findLocalizedMessage(exception, entered, getProperty().getDisplayName()),
                            exception, null
                        );
                        throw iae;
                    }
                }

                boolean result = Boolean.TRUE.equals(res);

                if (result && (env != null)) {
                    env.setState(env.STATE_VALID);
                }

                return result;
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            } finally {
                ignoreChanges = false;
            }

            return false;
        }
    }
}
