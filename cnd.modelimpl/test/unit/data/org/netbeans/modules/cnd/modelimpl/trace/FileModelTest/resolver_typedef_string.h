//
// File:   resolver_typedef_string.h
// Author: vv159170
//
// Created on May 4, 2006, 2:33 PM
//

#ifndef _resolver_typedef_string_H
#define	_resolver_typedef_string_H

namespace std {
    template<typename _Alloc>
    class allocator;
    
    template<class _CharT>
    struct char_traits;
    
    template<typename _CharT, typename _Traits = char_traits<_CharT>,
    typename _Alloc = allocator<_CharT> >
    class basic_string {
    };
    
    typedef std::basic_string<char> string;
}

namespace std {
    typedef basic_string<wchar_t> wstring;
}

using std::string;

std :: wstring write(string str);

namespace A {
  string read();
  using namespace std;
  class ClassA {

  public:
    wstring read() const;
  };
}

#endif	/* _resolver_typedef_string_H */

