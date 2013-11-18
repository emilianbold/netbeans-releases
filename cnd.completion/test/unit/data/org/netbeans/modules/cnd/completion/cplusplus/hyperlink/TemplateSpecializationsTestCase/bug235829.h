#ifndef NEWFILE_H
#define	NEWFILE_H

namespace bug235829 {

struct A {};

template <typename T> 
struct container {};    

typedef container<A> alias;

}

#endif	/* NEWFILE_H */
