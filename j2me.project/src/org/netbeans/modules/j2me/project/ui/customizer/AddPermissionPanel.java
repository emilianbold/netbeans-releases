package org.netbeans.modules.j2me.project.ui.customizer;

import java.awt.*;
import java.awt.event.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.openide.util.NbBundle;
import org.netbeans.modules.j2me.project.ui.customizer.PermissionsProvider.PermissionDefinition;
import org.netbeans.modules.j2me.project.ui.customizer.PermissionsProvider.PermissionDescriptor;
import org.netbeans.modules.j2me.project.ui.customizer.PermissionsProvider.PermissionError;
import org.netbeans.modules.j2me.project.ui.customizer.PermissionsProvider.PermissionsFactory;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;

public class AddPermissionPanel extends JPanel {

    private static final CharsetEncoder ASCII_CHARSET_ENCODER = Charset.forName("US-ASCII").newEncoder();

    private JComboBox<PermissionDescriptor> permissionsCombo;
    private JTextField nameField;
    private JCheckBox useNullName;
    private JTextField actionsField;
    private JCheckBox useNullActions;
    private JLabel errorLabel;
    private ParameterDefinition nameDefinition;
    private ParameterDefinition actionsDefinition;
    private DialogDescriptor dd;

    private final PermissionsFactory permissionsFactory;

    protected AddPermissionPanel(Window owner, PermissionsProvider permissionsProvider,
            Collection<String> existingPermissions, String permissionToEdit) {
        
        if (permissionToEdit != null) {
            existingPermissions = new HashSet<>(existingPermissions);
            existingPermissions.remove(permissionToEdit);
        }
        
        this.permissionsFactory = permissionsProvider.getPermissionsFactory(existingPermissions);
        
        createUI(permissionToEdit);
    }

    private static String getUIString(String uiKey) {
        return NbBundle.getMessage(AddPermissionPanel.class, uiKey);
    }

    /**
     * Creates and layouts UI components.
     */
    private void createUI(String permissionToEdit) {
        PermissionDefinition selectPermission = permissionToEdit == null ? null
                : permissionsFactory.getPermission(permissionToEdit);
        
        permissionsCombo = new JComboBox<>(permissionsFactory.getAvailablePermissions());

        permissionsCombo.setPreferredSize(new Dimension(Math.max(400, permissionsCombo.getPreferredSize().width),
                permissionsCombo.getPreferredSize().height));

        nameField = new JTextField();
        useNullName = new JCheckBox(getUIString("LBL_AddPermission_UseNull"));
        actionsField = new JTextField();
        useNullActions = new JCheckBox(getUIString("LBL_AddPermission_UseNull"));

        nameDefinition = new ParameterDefinition("LBL_AddPermission_ResourceName",
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateNameAndActions();
                    }
                });

        actionsDefinition = new ParameterDefinition("LBL_AddPermission_Action",
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!actionsDefinition.useParameter()) {
                            PermissionDescriptor permissionDescr = (PermissionDescriptor) permissionsCombo.getSelectedItem();

                            if (!permissionDescr.actionsCanBeOptional(true)) {
                                nameDefinition.setUseParameter(false);
                            }
                        }

                        updateNameAndActions();
                    }
                });

        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(new JLabel(getUIString("LBL_AddPermission_Permission")),
                new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(
                                5, 5, 0, 5), 0, 0));
        mainPanel.add(permissionsCombo, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(2, 5, 0, 5), 0, 0));

        mainPanel.add(nameDefinition, new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
        mainPanel.add(nameField, new GridBagConstraints(0, 3, 1, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(2, 5, 0, 0), 0, 0));

        mainPanel.add(useNullName, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(2, 5, 0, 5), 0, 0));

        mainPanel.add(actionsDefinition, new GridBagConstraints(0, 4, 2, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
        mainPanel.add(actionsField, new GridBagConstraints(0, 5, 1, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(2, 5, 0, 0), 0, 0));
        mainPanel.add(useNullActions, new GridBagConstraints(1, 5, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(2, 5, 0, 5), 0, 0));

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);

        mainPanel.add(errorLabel, new GridBagConstraints(0, 6, 1, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0));

        this.add(mainPanel);
        nameField.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (ASCII_CHARSET_ENCODER.canEncode(str)) {
                    super.insertString(offs, str, a);
                }
            }
        });

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateOKAction();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateOKAction();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateOKAction();
            }
        });

        actionsField.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (ASCII_CHARSET_ENCODER.canEncode(str)) {
                    super.insertString(offs, str, a);
                }
            }
        });

        actionsField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateOKAction();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateOKAction();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateOKAction();
            }
        });

        permissionsCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateNameAndActions();
            }
        });

        useNullName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateNameAndActions();
            }
        });

        useNullActions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateNameAndActions();
            }
        });
        
        PermissionDescriptor descriptor = null;

        if (selectPermission != null) {
            descriptor = permissionsFactory.getDescriptor(selectPermission);
            permissionsCombo.setSelectedItem(descriptor);

            if (selectPermission.getNumberOfParameters() > 0) {
                if (descriptor.nameCanBeOptional()) {
                    nameDefinition.setUseParameter(true);
                }

                String nameValue = selectPermission.getName();

                useNullName.setSelected(nameValue == null);

                if (nameValue != null) {
                    nameField.setText(nameValue);
                }

                if (selectPermission.getNumberOfParameters() > 1) {
                    if (descriptor.actionsCanBeOptional(true)) {
                        actionsDefinition.setUseParameter(true);
                    }

                    String actionsValue = selectPermission.getActions();

                    useNullActions.setSelected(actionsValue == null);

                    if (actionsValue != null) {
                        actionsField.setText(actionsValue);
                    }
                }
            }
        }

        updateNameAndActions();
    }

    private void updateNameAndActions() {
        PermissionDescriptor permissionDescr = (PermissionDescriptor) permissionsCombo.getSelectedItem();
        if (permissionDescr != null) {
            nameDefinition.setEnabled(permissionDescr.supportsName());
            nameDefinition.setOptional(permissionDescr.supportsName() && permissionDescr.nameCanBeOptional());

            actionsDefinition.setEnabled(permissionDescr.supportsAction() && nameDefinition.useParameter());
            actionsDefinition.setOptional(permissionDescr.supportsAction() && nameDefinition.useParameter()
                    && permissionDescr.actionsCanBeOptional(nameDefinition.useParameter()));

            useNullName.setEnabled(permissionDescr.supportsName()
                    && nameDefinition.useParameter());
            nameField.setEnabled(useNullName.isEnabled() && !useNullName.isSelected());

            useNullActions.setEnabled(permissionDescr.supportsAction()
                    && actionsDefinition.useParameter());
            actionsField.setEnabled(useNullActions.isEnabled() && !useNullActions.isSelected());

            updateOKAction();
        }
    }

    private String getNameValue() {
        return getParameterValue(useNullName, nameField, false);
    }

    private String getActionsValue() {
        return getParameterValue(useNullActions, actionsField, true);
    }

    private String getParameterValue(JCheckBox useNull, JTextField parameterField, boolean trim) {
        if (useNull.isSelected()) {
            return null;
        } else {
            String result = parameterField.getText();

            if (trim) {
                result = result.trim();
            }

            return result;
        }
    }

    private int getNumberOfParameters() {
        int result = 0;

        if (nameDefinition.useParameter()) {
            result = result + 1;

            if (actionsDefinition.useParameter()) {
                result = result + 1;
            }
        }

        return result;
    }

    private void updateOKAction() {
        PermissionDescriptor permissionDescr = (PermissionDescriptor) permissionsCombo.getSelectedItem();

        boolean enable;

        if (permissionDescr == null) {
            enable = false;
        } else {
            PermissionError valid = permissionsFactory.validatePermission(permissionDescr, getNameValue(),
                    getActionsValue(), getNumberOfParameters());

            enable = valid == PermissionError.OK;

            if (!enable) {
                errorLabel.setText(getUIString("ERR_AddPermission_" + valid.name()));
            }
        }

        if (enable) {
            errorLabel.setText(" ");
        }

        if (dd != null) {
            dd.setValid(enable);
        }
    }

    public PermissionDefinition getPermission() {
        return permissionsFactory.getPermission(
                (PermissionDescriptor) permissionsCombo.getSelectedItem(), getNameValue(), getActionsValue(),
                getNumberOfParameters());
    }

    protected void setDialogDescriptor(final DialogDescriptor desc) {
        this.dd = desc;
        dd.setHelpCtx(new HelpCtx("org.netbeans.modules.j2me.project.ui.customizer.AddPermissionPanel")); //NOI18N
        updateOKAction();
    }

    private static class ParameterDefinition extends JPanel {

        private final JCheckBox useParameter;
        private final JLabel parameterLabel;

        public ParameterDefinition(String uiKey, ActionListener actionListener) {
            super(new CardLayout());

            String labelText = getUIString(uiKey);

            useParameter = new JCheckBox(labelText);
            useParameter.setBorder(null);

            if (actionListener != null) {
                useParameter.addActionListener(actionListener);
            }

            parameterLabel = new JLabel(labelText);

            add(useParameter, Boolean.TRUE.toString());
            add(parameterLabel, Boolean.FALSE.toString());
        }

        public boolean useParameter() {
            return isEnabled() && (!useParameter.isVisible() || useParameter.isSelected());
        }

        public void setUseParameter(boolean value) {
            useParameter.setSelected(value);
        }

        public void setOptional(boolean optional) {
            ((CardLayout) getLayout()).show(this, Boolean.toString(optional));
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);

            parameterLabel.setEnabled(enabled);
            useParameter.setEnabled(enabled);
        }
    }
}
