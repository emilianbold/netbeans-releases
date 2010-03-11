package org.netbeans.modules.php.api.editor;

import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Class representing a PHP element ({@link PhpClass PHP class}, method, field etc.).
 * @since 1.13
 */
public abstract class PhpBaseElement {

    private final String name;
    private final String fullyQualifiedName;
    private final FileObject file;
    private final int offset;
    private final String description;

    protected PhpBaseElement(String name, String fullyQualifiedName) {
        this(name, fullyQualifiedName, -1, null);
    }

    /**
     * @since 1.25
     */
    protected PhpBaseElement(String name, String fullyQualifiedName, FileObject file) {
        this(name, fullyQualifiedName, file, -1, null);
    }

    protected PhpBaseElement(String name, String fullyQualifiedName, String description) {
        this(name, fullyQualifiedName, -1, description);
    }

    protected PhpBaseElement(String name, String fullyQualifiedName, int offset) {
        this(name, fullyQualifiedName, offset, null);
    }

    protected PhpBaseElement(String name, String fullyQualifiedName, int offset, String description) {
        this(name, fullyQualifiedName, null, offset, description);
    }

    /**
     * @since 1.25
     */
    protected PhpBaseElement(String name, String fullyQualifiedName, FileObject file, int offset, String description) {
        Parameters.notEmpty("name", name);

        this.name = name;
        this.fullyQualifiedName = fullyQualifiedName;
        this.file = file;
        this.offset = offset;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    /**
     * @since 1.25
     */
    public FileObject getFile() {
        return file;
    }

    public int getOffset() {
        return offset;
    }

    public String getDescription() {
        return description;
    }
}
