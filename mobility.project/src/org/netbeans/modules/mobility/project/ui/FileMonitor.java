package org.netbeans.modules.mobility.project.ui;

public interface FileMonitor {

    public void fileCreated();

    public void fileDeleted();

    public void fileChanged();
}
