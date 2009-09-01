/*
 * Dwarf2NameFinder.java -- decodes the DWARF-2 debug_line section.
 * Copyright (C) 2005, 2008  Casey Marshall.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.netbeans.modules.cnd.gizmo.addr2line.dwarf2line;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An interpreter for DWARF-2 "debug_line" byte codes, which, if given
 * the program counter of a running program, can determine the source
 * file and line number (and column number, but that information is not
 * currently emitted by GCC) of that statement.
 */
public class Dwarf2NameFinder {

    private static final Logger logger = Logger.getLogger(Dwarf2NameFinder.class.getName());
    private static final String DEBUG_LINE = ".debug_line"; // NOI18N
    private String binaryFile;
    private String sourceFile = null;
    private int lineNumber = -1;
    /**
     * This is the '.debug_line' section of 'binaryFile', mapped into
     * memory. It contains the DWARF-2 byte codes for the file and line
     * number information.
     */
    private MappedByteBuffer dw2;
    /**
     * A mapping of target ranges to sections where the debug info for
     * that range of addresses can be found.
     */
    private final RangeMap cache;
    private static final int DW_LNS_extended_op = 0;
    private static final int DW_LNS_copy = 1;
    private static final int DW_LNS_advance_pc = 2;
    private static final int DW_LNS_advance_line = 3;
    private static final int DW_LNS_set_file = 4;
    private static final int DW_LNS_set_column = 5;
    private static final int DW_LNS_negate_stmt = 6;
    private static final int DW_LNS_set_basic_block = 7;
    private static final int DW_LNS_const_add_pc = 8;
    private static final int DW_LNS_fixed_advance_pc = 9;
    private static final int DW_LNE_end_sequence = 1;
    private static final int DW_LNE_set_address = 2;
    private static final int DW_LNE_define_file = 3;
    private static final debug DEBUG = new debug();

    private static final class debug extends Level {

        private debug() {
            super("DWARF-2", Level.INFO.intValue()); // NOI18N
        }
    }

    private static class dw2_debug_line {

        long total_length;
        int version;
        long prologue_length;
        int minimum_instruction_length;
        boolean default_is_stmt;
        byte line_base;
        int line_range;
        int opcode_base;
        final byte[] standard_opcode_lengths = new byte[9];

        private void get(ByteBuffer b) {
            total_length = (long) b.getInt() & 0xFFFFFFFFL;
            version = b.getShort() & 0xFFFF;
            prologue_length = (long) b.getInt() & 0xFFFFFFFFL;
            minimum_instruction_length = b.get() & 0xFF;
            default_is_stmt = b.get() != 0;
            line_base = b.get();
            line_range = b.get() & 0xFF;
            opcode_base = b.get() & 0xFF;
            b.get(standard_opcode_lengths);
        }

        @Override
        public String toString() {
            java.lang.StringBuilder str = new java.lang.StringBuilder(super.toString());
            str.append(" [ total_length: ").append(total_length); // NOI18N
            str.append("; version: ").append(version); // NOI18N
            str.append("; prologue_length: ").append(prologue_length); // NOI18N
            str.append("; minimum_instruction_length: ").append(minimum_instruction_length); // NOI18N
            str.append("; default_is_stmt: ").append(default_is_stmt); // NOI18N
            str.append("; line_base: ").append(line_base); // NOI18N
            str.append("; line_range: ").append(line_range); // NOI18N
            str.append("; opcode_base: ").append(opcode_base); // NOI18N
            str.append("; standard_opcode_lengths: { "); // NOI18N
            str.append(standard_opcode_lengths[0]).append(", "); // NOI18N
            str.append(standard_opcode_lengths[1]).append(", "); // NOI18N
            str.append(standard_opcode_lengths[2]).append(", "); // NOI18N
            str.append(standard_opcode_lengths[3]).append(", "); // NOI18N
            str.append(standard_opcode_lengths[4]).append(", "); // NOI18N
            str.append(standard_opcode_lengths[5]).append(", "); // NOI18N
            str.append(standard_opcode_lengths[6]).append(", "); // NOI18N
            str.append(standard_opcode_lengths[7]).append(", "); // NOI18N
            str.append(standard_opcode_lengths[8]).append(" } ]"); // NOI18N
            return str.toString();
        }
    }

    public Dwarf2NameFinder(final String binaryFile) {
        cache = new RangeMap();
        try {
            dw2 = SectionFinder.mapSection(binaryFile, DEBUG_LINE);
            this.binaryFile = binaryFile;

            long now;
            if (Configuration.DEBUG) {
                Runtime r = Runtime.getRuntime();
                System.out.println("mem free = " + r.freeMemory() + " total = " + r.totalMemory()); // NOI18N
                System.out.print("Scanning " + binaryFile + "..."); // NOI18N
                now = -System.currentTimeMillis();
            }
            scan();
            if (Configuration.DEBUG) {
                now += System.currentTimeMillis();
                Runtime r = Runtime.getRuntime();
                System.out.println("done"); // NOI18N
                System.out.println("scanned " + cache.size() + " compilation units in " + now + " nss"); // NOI18N
                System.gc();
                System.out.println("mem free = " + r.freeMemory() + " total = " + r.totalMemory()); // NOI18N
                System.err.println(cache);
            }
        } catch (IOException ioe) {
            if (Configuration.DEBUG) {
                logger.log(DEBUG, "can''t map .debug_line in {0}: {1}", new Object[]{binaryFile, ioe.getMessage()});
            }
            dw2 = null;
            this.binaryFile = null;
            return;
        }
    }

    public void lookup(final long target) {
        lookup(target, false);
    }

    private void scan() {
        lookup(-1, true);
    }

    private void lookup(final long target, boolean scan_only) {
        if (Configuration.DEBUG) {
            logger.log(DEBUG, "Dwarf2NameFinder.lookup: {0} 0x{1}", // NOI18N
                    new Object[]{binaryFile, Long.toHexString(target)});
        }

        lineNumber = -1;
        sourceFile = null;

        if (dw2 == null) {
            return;
        }

        CacheEntry e = (CacheEntry) cache.get(target);
        if (e != null) {
            if (Configuration.DEBUG) {
                System.out.println("got cache entry " + e); // NOI18N
            }
            if (interpret(target, e.section, e.fileNames, e.header, false)) {
                return;
            }
        }

        dw2.position(0);
        dw2.limit(dw2.capacity());

        if (Configuration.DEBUG) {
            logger.log(DEBUG, "Mapped .debug_line section is {0} bytes", Integer.valueOf(dw2.capacity())); // NOI18N
        }

        while (dw2.hasRemaining()) {
            final int begin = dw2.position();
            dw2_debug_line header = new dw2_debug_line();
            header.get(dw2);
            if (Configuration.DEBUG) {
                logger.log(DEBUG, "read debug_line header: {0}", header); // NOI18N
            }
            final int end = (int) (begin + header.total_length + 4);
            final int prologue_end = (int) (begin + header.prologue_length + 9);

            if (Configuration.DEBUG) {
                logger.log(DEBUG, "this section starts at {0}, ends at {1}, and the prologue ends at {2}", // NOI18N
                        new Object[]{Integer.valueOf(begin), Integer.valueOf(end),
                            Integer.valueOf(prologue_end)});
            }

            if (header.version != 2 || header.opcode_base != 10) {
                if (Configuration.DEBUG) {
                    logger.log(DEBUG, "skipping this section; not DWARF-2 (version={0}, opcode_base={1})", // NOI18N
                            new Object[]{Integer.valueOf(header.version),
                                Integer.valueOf(header.opcode_base)});
                }
                dw2.position(end);
                continue;
            }

            dw2.limit(prologue_end);
            ByteBuffer prologue = dw2.slice();

            // Skip the directories; they end with a single null byte.
            String s;
            while ((s = getString(prologue)).length() > 0) {
                if (Configuration.DEBUG) {
                    logger.log(DEBUG, "Skipped directory: {0}", s); // NOI18N
                }
            }

            // Read the file names.
            LinkedList<String> fnames = new LinkedList<String>();
            while (prologue.hasRemaining()) {
                String fname = getString(prologue);
                if (Configuration.DEBUG) {
                    logger.log(DEBUG, "File name: {0}", fname); // NOI18N
                }
                fnames.add(fname);

                long u1 = getUleb128(prologue);
                long u2 = getUleb128(prologue);
                long u3 = getUleb128(prologue);
                if (Configuration.DEBUG) {
                    logger.log(DEBUG, "dir: {0}, time: {1}, len: {2}", new Object[]{Long.valueOf(u1), Long.valueOf(u2), Long.valueOf(u3)}); // NOI18N
                }
            }
            prologue = null;

            dw2.limit(end);
            dw2.position(prologue_end + 1);
            ByteBuffer section = dw2.slice();
            dw2.limit(dw2.capacity());
            dw2.position(end);

            if (interpret(target, section, fnames, header, scan_only)) {
                break;
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private boolean interpret(long target, ByteBuffer section, LinkedList fnames,
            dw2_debug_line header, boolean scan_only) {
        long address = 0;
        long base_address = 0;
        long prev_base_address = 0;
        String define_file = null;
        int fileno = 0;
        int lineno = 1;
        int prev_fileno = 0;
        int prev_lineno = 1;
        final int const_pc_add = 245 / header.line_range;

        long min_address = -1;
        long max_address = 0;

        section.position(0);
        section.limit(section.capacity());

        interpret:
        while (section.hasRemaining()) {
            int opcode = section.get() & 0xFF;

            if (opcode < header.opcode_base) {
                switch (opcode) {
                    case DW_LNS_extended_op: {
                        long insn_len = getUleb128(section);
                        opcode = section.get();
                        if (Configuration.DEBUG) {
                            logger.log(DEBUG, "special opcode {0}, insn_len={1}", // NOI18N
                                    new Object[]{Integer.valueOf(opcode), Long.valueOf(insn_len)});
                        }

                        switch (opcode) {
                            case DW_LNE_end_sequence:
                                if (Configuration.DEBUG) {
                                    logger.log(DEBUG, "End of sequence"); // NOI18N
                                }
                                if (!scan_only && (prev_base_address <= target && address > target)) {
                                    lineNumber = prev_lineno;
                                    sourceFile = (String) ((prev_fileno >= 0 && prev_fileno < fnames.size())
                                            ? fnames.get(prev_fileno) : define_file);
                                    if (Configuration.DEBUG) {
                                        logger.log(DEBUG, "found {0}:{1} for {2}", new Object[]{sourceFile, Integer.valueOf(lineNumber), Long.toHexString(target)}); // NOI18N
                                    }

                                    cache.put(base_address, address,
                                            new CacheEntry(fnames, section, header));

                                    return true;
                                }

                                if (scan_only && min_address != -1 && max_address != 0) {
                                    cache.put(min_address, max_address,
                                            new CacheEntry(fnames, section, header));
                                    min_address = -1;
                                    max_address = 0;
                                }

                                prev_lineno = lineno = 1;
                                prev_fileno = fileno = 0;
                                base_address = address = 0;
                                break;

                            case DW_LNE_set_address:
                                prev_base_address = base_address;
                                base_address = section.get() & 0xFF;
                                base_address |= (section.get() & 0xFFL) << 8;
                                base_address |= (section.get() & 0xFFL) << 16;
                                base_address |= (section.get() & 0xFFL) << 24;
                                address = base_address;
                                if (prev_base_address == 0) {
                                    prev_base_address = base_address;
                                }
                                if (Configuration.DEBUG) {
                                    logger.log(DEBUG, "Set address to 0x{0}", Long.toHexString(address)); // NOI18N
                                }

                                // XXX this might not be correct, as there can be more
                                // than one of these instructions in a single compilation
                                // unit.
                                //if (!scan_only && address > target) {
                                //    if (Configuration.DEBUG) {
                                //        logger.log(DEBUG, "not in this unit base=0x{0}, target=0x{1}", // NOI18N
                                //                new Object[]{Long.toHexString(address),
                                //                    Long.toHexString(target)});
                                //    }
                                //    return false;
                                //}
                                break;

                            case DW_LNE_define_file:
                                define_file = getString(section);
                                if (Configuration.DEBUG) {
                                    logger.log(DEBUG, "Define file: {0}", define_file); // NOI18N
                                }
                                getUleb128(section);
                                getUleb128(section);
                                getUleb128(section);
                                break;

                            default:
                                if (Configuration.DEBUG) {
                                    logger.log(DEBUG, "Unsupported extended opcode {0}", // NOI18N
                                            Integer.valueOf(opcode));
                                }
                                section.position(section.position() + (int) insn_len);
                                break;
                        }
                        // fallthrough is legitimate (program author said)
                    }
                    case DW_LNS_copy:
                        if (Configuration.DEBUG) {
                            logger.log(DEBUG, "Copy"); // NOI18N
                        }
                        if (!scan_only && (prev_base_address <= target && address > target)) {
                            lineNumber = prev_lineno;
                            sourceFile = (String) ((prev_fileno >= 0 && prev_fileno < fnames.size())
                                    ? fnames.get(prev_fileno) : define_file);
                            if (Configuration.DEBUG) {
                                logger.log(DEBUG, "found {0}:{1} for {2}", new Object[]{sourceFile, Integer.valueOf(lineNumber), Long.toHexString(target)}); // NOI18N
                            }

                            cache.put(base_address, address,
                                    new CacheEntry(fnames, section, header));

                            return true;
                        }
                        prev_lineno = lineno;
                        prev_fileno = fileno;
                        break;

                    case DW_LNS_advance_pc:
                         {
                            long amt = getUleb128(section);
                            address += amt * header.minimum_instruction_length;
                            if (scan_only) {
                                if (ucomp(min_address, address) > 0) {
                                    min_address = address;
                                }
                                if (ucomp(max_address, address) < 0) {
                                    max_address = address;
                                }
                            }
                            if (Configuration.DEBUG) {
                                logger.log(DEBUG, "Advance PC by {0} to 0x{1}", // NOI18N
                                        new Object[]{Long.valueOf(amt),
                                            Long.toHexString(address)});
                            }
                        }
                        break;

                    case DW_LNS_advance_line:
                         {
                            long amt = getSleb128(section);
                            prev_lineno = lineno;
                            lineno += (int) amt;
                            if (Configuration.DEBUG) {
                                logger.log(DEBUG, "Advance line by {0} to {1}", new Object[]{Long.valueOf(amt), Integer.valueOf(lineno)}); // NOI18N
                            }
                        }
                        break;

                    case DW_LNS_set_file:
                        prev_fileno = fileno;
                        fileno = (int) getUleb128(section) - 1;
                        if (Configuration.DEBUG) {
                            logger.log(DEBUG, "Set file to {0}", Integer.valueOf(fileno)); // NOI18N
                        }
                        break;

                    case DW_LNS_set_column:
                        getUleb128(section);
                        if (Configuration.DEBUG) {
                            logger.log(DEBUG, "Set column (ignored)"); // NOI18N
                        }
                        break;

                    case DW_LNS_negate_stmt:
                        if (Configuration.DEBUG) {
                            logger.log(DEBUG, "Negate statement (ignored)"); // NOI18N
                        }
                        break;

                    case DW_LNS_set_basic_block:
                        if (Configuration.DEBUG) {
                            logger.log(DEBUG, "Set basic block (ignored)"); // NOI18N
                        }
                        break;

                    case DW_LNS_const_add_pc:
                        address += const_pc_add;
                        if (scan_only) {
                            if (ucomp(min_address, address) > 0) {
                                min_address = address;
                            }
                            if (ucomp(max_address, address) < 0) {
                                max_address = address;
                            }
                        }
                        if (Configuration.DEBUG) {
                            logger.log(DEBUG, "Advance PC by (constant) {0} to 0x{1}", // NOI18N
                                    new Object[]{Integer.valueOf(const_pc_add),
                                        Long.toHexString(address)});
                        }
                        break;

                    case DW_LNS_fixed_advance_pc:
                         {
                            int amt = section.getShort() & 0xFFFF;
                            address += amt;
                            if (Configuration.DEBUG) {
                                logger.log(DEBUG, "Advance PC by (fixed) {0} to 0x{1}", // NOI18N
                                        new Object[]{Integer.valueOf(amt),
                                            Long.toHexString(address)});
                            }
                        }
                        break;
                }
            } else {
                int adj = (opcode & 0xFF) - header.opcode_base;
                int addr_adv = adj / header.line_range;
                int line_adv = header.line_base + (adj % header.line_range);
                long new_addr = address + addr_adv;
                int new_line = lineno + line_adv;
                if (Configuration.DEBUG) {
                    logger.log(DEBUG,
                            "Special opcode {0} advance line by {1} to {2} and address by {3} to 0x{4}", // NOI18N
                            new Object[]{Integer.valueOf(opcode & 0xFF),
                                Integer.valueOf(line_adv),
                                Integer.valueOf(new_line),
                                Integer.valueOf(addr_adv),
                                Long.toHexString(new_addr)});
                }
                if (!scan_only && prev_base_address <= target && new_addr >= target) {
                    lineNumber = new_addr == target ? new_line : lineno;
                    sourceFile = (String) ((fileno >= 0 && fileno < fnames.size())
                            ? fnames.get(fileno) : define_file);
                    if (Configuration.DEBUG) {
                        logger.log(DEBUG, "found {0}:{1} for {2}", new Object[]{sourceFile, Integer.valueOf(lineNumber), Long.toHexString(target)}); // NOI18N
                    }

                    cache.put(base_address, new_addr,
                            new CacheEntry(fnames, section, header));

                    return true;
                }

                prev_lineno = lineno;
                prev_fileno = fileno;
                lineno = new_line;
                address = new_addr;
                if (scan_only) {
                    if (ucomp(min_address, address) > 0) {
                        min_address = address;
                    }
                    if (ucomp(max_address, address) < 0) {
                        max_address = address;
                    }
                }
            }
        }

        if (scan_only) {
            if (min_address != -1 && max_address != 0) {
                cache.put(min_address, max_address,
                        new CacheEntry(fnames, section, header));
            }
        }
        return false;
    }

    private static class CacheEntry {

        final dw2_debug_line header;
        final LinkedList fileNames;
        final ByteBuffer section;

        CacheEntry(LinkedList fileNames, ByteBuffer section, dw2_debug_line header) {
            this.fileNames = fileNames;
            this.section = section;
            this.header = header;
        }
    }

    public void close() {
        dw2 = null;
        binaryFile = null;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    private static String getString(ByteBuffer buf) {
        int pos = buf.position();
        int len = 0;
        byte b;
        while (buf.get() != 0) {
            len++;
        }
        byte[] bytes = new byte[len];
        buf.position(pos);
        buf.get(bytes);
        buf.get();
        return new String(bytes);
    }

    private static long getUleb128(ByteBuffer buf) {
        long val = 0;
        byte b;
        int shift = 0;

        while (true) {
            b = buf.get();
            val |= (b & 0x7f) << shift;
            if ((b & 0x80) == 0) {
                break;
            }
            shift += 7;
        }

        return val;
    }

    private static long getSleb128(ByteBuffer buf) {
        long val = 0;
        int shift = 0;
        byte b;
        int size = 8 << 3;

        while (true) {
            b = buf.get();
            val |= (b & 0x7f) << shift;
            shift += 7;
            if ((b & 0x80) == 0) {
                break;
            }
        }

        if (shift < size && (b & 0x40) != 0) {
            val |= -(1 << shift);
        }

        return val;
    }

    static int ucomp(long l1, long l2) {
        if (l1 == l2) {
            return 0;
        }

        if (l1 < 0) {
            if (l2 < 0) {
                if (l1 < l2) {
                    return 1;
                } else {
                    return -1;
                }
            }
            return 1;
        } else {
            if (l2 >= 0) {
                if (l1 < l2) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return -1;
        }
    }
}
