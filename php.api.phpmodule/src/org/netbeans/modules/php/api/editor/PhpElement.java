package org.netbeans.modules.php.api.editor;

import org.openide.util.Parameters;

/**
 * Class representing a PHP element ({@link PhpClass PHP class}, method, field etc.).
 * @since 1.13
 */
public abstract class PhpElement {

    private final String name;
    private final String fullyQualifiedName;
    private final int offset;
    private final String description;

    protected PhpElement(String name, String fullyQualifiedName, int offset) {
        this(name, fullyQualifiedName, offset, null);
    }

    protected PhpElement(String name, String fullyQualifiedName, int offset, String description) {
        Parameters.notEmpty("name", name);
        Parameters.notEmpty("fullyQualifiedName", fullyQualifiedName);

        this.name = name;
        this.fullyQualifiedName = fullyQualifiedName;
        this.offset = offset;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public int getOffset() {
        return offset;
    }

    public String getDescription() {
        return description;
    }
}
