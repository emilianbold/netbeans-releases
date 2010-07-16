package org.netbeans.modules.css.refactoring.api;

import org.netbeans.modules.csl.api.OffsetRange;

public class Entry {

    private String name;
    private OffsetRange astRange,  documentRange, bodyRange, documentBodyRange;
    private boolean isVirtual;
    //store charsequences - refering to the underlying immutable snapshot
    private CharSequence elementText, elementLineText;
    private int lineOffset;

    public Entry(String name, OffsetRange astRange, OffsetRange documentRange, 
            OffsetRange bodyRange, OffsetRange documentBodyRange,
            int lineOffset, CharSequence elementText, CharSequence elementLineText, boolean isVirtual) {
        this.name = name;
        this.astRange = astRange;
        this.documentRange = documentRange;
        this.isVirtual = isVirtual;
        this.elementText = elementText;
        this.elementLineText = elementLineText;
        this.lineOffset = lineOffset;
        this.bodyRange = bodyRange;
        this.documentBodyRange = documentBodyRange;
    }

    /**
     * quite similar to isValidInSourceDocument() but here we do not use the
     * adjusted start offset to check if can be translated to the source
     * but rather use the real node start offset.
     * In case of virtually generated class or selector the isVirtual
     * is always true since the dot or has doesn't exist in the css source code
     *
     */
    public boolean isVirtual() {
        return isVirtual;
    }

    public boolean isValidInSourceDocument() {
        return documentRange != null;
    }

    /**
     * 
     * @return a line offset of the document start offset in the underlying document.
     * The -1 value denotes that there has been a problem getting the line.
     */
    public int getLineOffset() {
        return lineOffset;
    }

    public CharSequence getText() {
        return elementText;
    }

    public CharSequence getLineText() {
        return elementLineText;
    }

    public String getName() {
        return name;
    }

    public OffsetRange getDocumentRange() {
        return documentRange;
    }

    public OffsetRange getRange() {
        return astRange;
    }

    public OffsetRange getBodyRange() {
        return bodyRange;
    }

    public OffsetRange getDocumentBodyRange() {
        return documentBodyRange;
    }

    @Override
    public String toString() {
        return "Entry[" + (!isValidInSourceDocument() ? "INVALID! " : "") + getName() + "; " + getRange().getStart() + " - " + getRange().getEnd() + "]"; //NOI18N
    }
}
