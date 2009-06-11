/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb;

/**
 *
 * @author Egor Ushakov
 */
public class VariableInfo {

    private VariableInfo() {
    }

    public static String getStDStringValue(String value) {
        return "{static npos = 4294967295, _M_dataplus = {<std::allocator<char>> = {<__gnu_cxx::new_allocator<char>> = {<No data fields>}, <No data fields>}, _M_p = 0x806b904 \"" + value + "\"}}";
    }

    public static final String STD_STRING_PTYPE = "    class std::basic_string<char,std::char_traits<char>,std::allocator<char> > \n" +
    "    {\n" +
    "  public:\n" +
    "    static const size_t npos;\n" +
    "  private:\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> >::_Alloc_hider _M_dataplus;\n" +
    "\n" +
    "    char * _M_data() const;\n" +
    "    char * _M_data(char*);\n" +
    "    \n" +
    "    class std::basic_string<char,std::char_traits<char>,std::allocator<char> >::_Rep * _M_rep() const;\n" +
    "    \n" +
    "    struct __gnu_cxx::__normal_iterator<char*,std::basic_string<char, std::char_traits<char>, std::allocator<char> > > _M_ibegin() const;\n" +
    "    \n" +
    "    struct __gnu_cxx::__normal_iterator<char*,std::basic_string<char, std::char_traits<char>, std::allocator<char> > > _M_iend() const;\n" +
    "    void _M_leak();\n" +
    "    size_t _M_check(unsigned int, char const*) const;\n" +
    "    size_t _M_limit(unsigned int, unsigned int) const;\n" +
    "    static \n" +
    "    void _S_copy_chars(char*, __gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>);\n" +
    "    static \n" +
    "    void _S_copy_chars(char*, __gnu_cxx::__normal_iterator<char const*, std::string>, __gnu_cxx::__normal_iterator<char const*, std::string>);\n" +
    "    static void _S_copy_chars(char*, char*, char*);\n" +
    "    static void _S_copy_chars(char*, char const*, char const*);\n" +
    "    void _M_mutate(unsigned int, unsigned int, unsigned int);\n" +
    "    void _M_leak_hard();\n" +
    "    static \n" +
    "    class std::basic_string<char,std::char_traits<char>,std::allocator<char> >::_Rep & _S_empty_rep();\n" +
    "  public:\n" +
    "    void basic_string(void);\n" +
    "    void basic_string(const std::allocator<char> &);\n" +
    "    void basic_string(\n" +
    "    const std::basic_string<char,std::char_traits<char>,std::allocator<char> > &);\n" +
    "    void basic_string(\n" +
    "    const std::basic_string<char,std::char_traits<char>,std::allocator<char> > &, unsigned int, unsigned int);\n" +
    "    void basic_string(\n" +
    "    const std::basic_string<char,std::char_traits<char>,std::allocator<char> > &, unsigned int, unsigned int, const std::allocator<char> &);\n" +
    "    void basic_string(const char *, unsigned int, \n" +
    "    const std::allocator<char> &);\n" +
    "    void basic_string(const char *, const std::allocator<char> &);\n" +
    "    void basic_string(unsigned int, char, const std::allocator<char> &);\n" +
    "    ~basic_string(int);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & operator=(std::string const&);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & operator=(char const*);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & operator=(char);\n" +
    "    \n" +
    "    struct __gnu_cxx::__normal_iterator<char*,std::basic_string<char, std::char_traits<char>, std::allocator<char> > > begin();\n" +
    "    \n" +
    "    struct __gnu_cxx::__normal_iterator<const char*,std::basic_string<char, std::char_traits<char>, std::allocator<char> > > begin() const;\n" +
    "    \n" +
    "    struct __gnu_cxx::__normal_iterator<char*,std::basic_string<char, std::char_traits<char>, std::allocator<char> > > end();\n" +
    "    \n" +
    "    struct __gnu_cxx::__normal_iterator<const char*,std::basic_string<char, std::char_traits<char>, std::allocator<char> > > end() const;\n" +
    "    \n" +
    "    struct std::reverse_iterator<__gnu_cxx::__normal_iterator<char*, std::basic_string<char, std::char_traits<char>, std::allocator<char> > > > rbegin();\n" +
    "    \n" +
    "    struct std::reverse_iterator<__gnu_cxx::__normal_iterator<const char*, std::basic_string<char, std::char_traits<char>, std::allocator<char> > > >\n" +
    "     rbegin() const;\n" +
    "    \n" +
    "    struct std::reverse_iterator<__gnu_cxx::__normal_iterator<char*, std::basic_string<char, std::char_traits<char>, std::allocator<char> > > > rend();\n" +
    "    \n" +
    "    struct std::reverse_iterator<__gnu_cxx::__normal_iterator<const char*, std::basic_string<char, std::char_traits<char>, std::allocator<char> > > >\n" +
    "     rend() const;\n" +
    "    size_t size() const;\n" +
    "    size_t length() const;\n" +
    "    size_t max_size() const;\n" +
    "    void resize(unsigned int, char);\n" +
    "    void resize(unsigned int);\n" +
    "    size_t capacity() const;\n" +
    "    void reserve(unsigned int);\n" +
    "    void clear();\n" +
    "    bool empty() const;\n" +
    "    const char & operator[](unsigned int) const;\n" +
    "    char & operator[](unsigned int);\n" +
    "    const char & at(unsigned int) const;\n" +
    "    char & at(unsigned int);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & operator+=(std::string const&);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & operator+=(char const*);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & operator+=(char);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & append(std::string const&);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & append(std::string const&, unsigned int, unsigned int);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & append(char const*, unsigned int);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & append(char const*);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & append(unsigned int, char);\n" +
    "    void push_back(char);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & assign(std::string const&);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & assign(std::string const&, unsigned int, unsigned int);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & assign(char const*, unsigned int);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & assign(char const*);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & assign(unsigned int, char);\n" +
    "    \n" +
    "    void insert(__gnu_cxx::__normal_iterator<char*, std::string>, unsigned int, char);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & insert(unsigned int, std::string const&);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & insert(unsigned int, std::string const&, unsigned int, unsigned int);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & insert(unsigned int, char const*, unsigned int);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & insert(unsigned int, char const*);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & insert(unsigned int, unsigned int, char);\n" +
    "    \n" +
    "    struct __gnu_cxx::__normal_iterator<char*,std::basic_string<char, std::char_traits<char>, std::allocator<char> > >\n" +
    "     insert(__gnu_cxx::__normal_iterator<char*, std::string>, char);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & erase(unsigned int, unsigned int);\n" +
    "    \n" +
    "    struct __gnu_cxx::__normal_iterator<char*,std::basic_string<char, std::char_traits<char>, std::allocator<char> > >\n" +
    "     erase(__gnu_cxx::__normal_iterator<char*, std::string>);\n" +
    "    \n" +
    "    struct __gnu_cxx::__normal_iterator<char*,std::basic_string<char, std::char_traits<char>, std::allocator<char> > >\n" +
    "     erase(__gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(unsigned int, unsigned int, std::string const&);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(unsigned int, unsigned int, std::string const&, unsigned int, unsigned int);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(unsigned int, unsigned int, char const*, unsigned int);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(unsigned int, unsigned int, char const*);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(unsigned int, unsigned int, unsigned int, char);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(__gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>, std::string const&);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(__gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>, char const*, unsigned int);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(__gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>, char const*);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(__gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>, unsigned int, char);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(__gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>, char*, char*);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(__gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>, char const*, char const*);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(__gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & replace(__gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char*, std::string>, __gnu_cxx::__normal_iterator<char const*, std::string>, __gnu_cxx::__normal_iterator<char const*, std::string>);\n" +
    "  private:\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & _M_replace_aux(unsigned int, unsigned int, unsigned int, char);\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > & _M_replace_safe(unsigned int, unsigned int, char const*, unsigned int);\n" +
    "    static \n" +
    "    char * _S_construct(unsigned int, char, std::allocator<char> const&);\n" +
    "  public:\n" +
    "    size_t copy(char*, unsigned int, unsigned int) const;\n" +
    "    void swap(std::string&);\n" +
    "    const char * c_str() const;\n" +
    "    const char * data() const;\n" +
    "    std::allocator<char> get_allocator() const;\n" +
    "    size_t find(char const*, unsigned int, unsigned int) const;\n" +
    "    size_t find(std::string const&, unsigned int) const;\n" +
    "    size_t find(char const*, unsigned int) const;\n" +
    "    size_t find(char, unsigned int) const;\n" +
    "    size_t rfind(std::string const&, unsigned int) const;\n" +
    "    size_t rfind(char const*, unsigned int, unsigned int) const;\n" +
    "    size_t rfind(char const*, unsigned int) const;\n" +
    "    size_t rfind(char, unsigned int) const;\n" +
    "    size_t find_first_of(std::string const&, unsigned int) const;\n" +
    "    size_t find_first_of(char const*, unsigned int, unsigned int) const;\n" +
    "    size_t find_first_of(char const*, unsigned int) const;\n" +
    "    size_t find_first_of(char, unsigned int) const;\n" +
    "    size_t find_last_of(std::string const&, unsigned int) const;\n" +
    "    size_t find_last_of(char const*, unsigned int, unsigned int) const;\n" +
    "    size_t find_last_of(char const*, unsigned int) const;\n" +
    "    size_t find_last_of(char, unsigned int) const;\n" +
    "    size_t find_first_not_of(std::string const&, unsigned int) const;\n" +
    "    size_t find_first_not_of(char const*, unsigned int, unsigned int) const;\n" +
    "    size_t find_first_not_of(char const*, unsigned int) const;\n" +
    "    size_t find_first_not_of(char, unsigned int) const;\n" +
    "    size_t find_last_not_of(std::string const&, unsigned int) const;\n" +
    "    size_t find_last_not_of(char const*, unsigned int, unsigned int) const;\n" +
    "    size_t find_last_not_of(char const*, unsigned int) const;\n" +
    "    size_t find_last_not_of(char, unsigned int) const;\n" +
    "    \n" +
    "    std::basic_string<char,std::char_traits<char>,std::allocator<char> > substr(unsigned int, unsigned int) const;\n" +
    "    int compare(std::string const&) const;\n" +
    "    int compare(unsigned int, unsigned int, std::string const&) const;\n" +
    "    \n" +
    "    int compare(unsigned int, unsigned int, std::string const&, unsigned int, unsigned int) const;\n" +
    "    int compare(char const*) const;\n" +
    "    int compare(unsigned int, unsigned int, char const*) const;\n" +
    "    int compare(unsigned int, unsigned int, char const*, unsigned int) const;\n" +
    "}\n";

}
