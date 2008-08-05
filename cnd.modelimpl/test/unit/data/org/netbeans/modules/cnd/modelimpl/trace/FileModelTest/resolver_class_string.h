// 
// File:   resolver_class_string.h
// Author: vv159170
//
// Created on May 4, 2006, 2:33 PM
//

#ifndef _resolver_class_string_H
#define	_resolver_class_string_H

namespace std {
    template<typename _Alloc>
    class allocator;
    
    template<class _CharT>
    struct char_traits;
    
    template<typename _CharT, typename _Traits = char_traits<_CharT>,
    typename _Alloc = allocator<_CharT> >
    class basic_string {
    };
    
    class string : public std::basic_string<char> {
        
    };
}

namespace std {
    class wstring : protected basic_string<wchar_t> {
        
    };
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

using A::ClassA;

#endif	/* _resolver_class_string_H */

