package org.netbeans.modules.ruby.elements;

import org.jruby.ast.Node;
import org.jruby.parser.RubyParserResult;
import org.openide.filesystems.FileObject;


public class AstRootElement extends AstElement {
    private final FileObject fileObject;
    private final RubyParserResult result;

    public AstRootElement(FileObject fo, Node node, RubyParserResult result) {
        super(node);

        this.fileObject = fo;
        this.result = result;
    }

    public RubyParserResult getRubyParserResult() {
        return result;
    }

    @Override
    public String getName() {
        return fileObject.getNameExt();
    }
}
