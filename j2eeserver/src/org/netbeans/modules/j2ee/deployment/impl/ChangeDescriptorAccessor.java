package org.netbeans.modules.j2ee.deployment.impl;

import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor;

public abstract class ChangeDescriptorAccessor {

    private static volatile ChangeDescriptorAccessor accessor;

    public static void setDefault(ChangeDescriptorAccessor accessor) {
        if (ChangeDescriptorAccessor.accessor != null) {
            throw new IllegalStateException("Already initialized accessor");
        }
        ChangeDescriptorAccessor.accessor = accessor;
    }

    public static ChangeDescriptorAccessor getDefault() {
        if (accessor != null) {
            return accessor;
        } // that will assign value to the DEFAULT field above
        Class c = DeploymentChangeDescriptor.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
        assert accessor != null : "The accessor field must be initialized";
        return accessor;
    }

    /** Accessor to constructor */
    public abstract DeploymentChangeDescriptor newDescriptor(ServerFileDistributor.AppChanges desc);

    public abstract DeploymentChangeDescriptor withChangedServerResources(DeploymentChangeDescriptor desc);
}
