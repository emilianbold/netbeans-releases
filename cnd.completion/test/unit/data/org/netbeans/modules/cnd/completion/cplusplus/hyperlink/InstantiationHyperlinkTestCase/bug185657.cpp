
namespace bug185657 {

    typedef __SIZE_TYPE__ size_t;
    typedef __PTRDIFF_TYPE__ ptrdiff_t;

#define __glibcxx_base_allocator  new_allocator

    struct __true_type {
    };

    struct __false_type {
    };

    template<typename _Tp>
    class new_allocator {
    public:
        typedef size_t size_type;
        typedef ptrdiff_t difference_type;
        typedef _Tp* pointer;
        typedef const _Tp* const_pointer;
        typedef _Tp& reference;
        typedef const _Tp& const_reference;
        typedef _Tp value_type;

        template<typename _Tp1>
        struct rebind {
            typedef new_allocator<_Tp1> other;
        };

        new_allocator() throw () {
        }

        new_allocator(const new_allocator&) throw () {
        }

        template<typename _Tp1>
        new_allocator(const new_allocator<_Tp1>&) throw () {
        }

        ~new_allocator() throw () {
        }

        pointer
        address(reference __x) const {
            return &__x;
        }

        const_pointer
        address(const_reference __x) const {
            return &__x;
        }

        // NB: __n is permitted to be 0.  The C++ standard says nothing
        // about what the return value is when __n == 0.

        pointer
        allocate(size_type __n, const void* = 0) {
            return static_cast<_Tp*> (::operator new(__n * sizeof (_Tp)));
        }

        // __p is not permitted to be a null pointer.

        void
        deallocate(pointer __p, size_type) {
            ::operator delete(__p);
        }

        size_type
        max_size() const throw () {
            return size_t(-1) / sizeof (_Tp);
        }

        // _GLIBCXX_RESOLVE_LIB_DEFECTS
        // 402. wrong new expression in [some_] allocator::construct

        void
        construct(pointer __p, const _Tp& __val) {
            ::new((void *) __p) _Tp(__val);
        }

        void
        destroy(pointer __p) {
            __p->~_Tp();
        }
    };

    template<typename _Tp>
    class allocator : public __glibcxx_base_allocator<_Tp> {
    public:
        typedef size_t size_type;
        typedef ptrdiff_t difference_type;
        typedef _Tp* pointer;
        typedef const _Tp* const_pointer;
        typedef _Tp& reference;
        typedef const _Tp& const_reference;
        typedef _Tp value_type;

        template<typename _Tp1>
        struct rebind {
            typedef allocator<_Tp1> other;
        };

        allocator() throw () {
        }

        allocator(const allocator& __a) throw ()
        : __glibcxx_base_allocator<_Tp>(__a) {
        }

        template<typename _Tp1>
        allocator(const allocator<_Tp1>&) throw () {
        }

        ~allocator() throw () {
        }

        // Inherit everything else.
    };

    template<typename _Tp, typename _Alloc>
    struct _Vector_base {
        typedef typename _Alloc::template rebind<_Tp>::other _Tp_alloc_type;

        struct _Vector_impl
                : public _Tp_alloc_type {
            typename _Tp_alloc_type::pointer _M_start;
            typename _Tp_alloc_type::pointer _M_finish;
            typename _Tp_alloc_type::pointer _M_end_of_storage;

            _Vector_impl()
            : _Tp_alloc_type(), _M_start(0), _M_finish(0), _M_end_of_storage(0) {
            }

            _Vector_impl(_Tp_alloc_type const& __a)
            : _Tp_alloc_type(__a), _M_start(0), _M_finish(0), _M_end_of_storage(0) {
            }
        };

    public:
        typedef _Alloc allocator_type;

        _Tp_alloc_type &
                _M_get_Tp_allocator() {
            return *static_cast<_Tp_alloc_type*> (&this->_M_impl);
        }

        const _Tp_alloc_type &
                _M_get_Tp_allocator() const {
            return *static_cast<const _Tp_alloc_type*> (&this->_M_impl);
        }

        allocator_type
        get_allocator() const {
            return allocator_type(_M_get_Tp_allocator());
        }

        _Vector_base()
        : _M_impl() {
        }

        _Vector_base(const allocator_type & __a)
        : _M_impl(__a) {
        }

        _Vector_base(size_t __n, const allocator_type & __a)
        : _M_impl(__a) {
            this->_M_impl._M_start = this->_M_allocate(__n);
            this->_M_impl._M_finish = this->_M_impl._M_start;
            this->_M_impl._M_end_of_storage = this->_M_impl._M_start + __n;
        }

#ifdef __GXX_EXPERIMENTAL_CXX0X__

        _Vector_base(_Vector_base && __x)
        : _M_impl(__x._M_get_Tp_allocator()) {
            this->_M_impl._M_start = __x._M_impl._M_start;
            this->_M_impl._M_finish = __x._M_impl._M_finish;
            this->_M_impl._M_end_of_storage = __x._M_impl._M_end_of_storage;
            __x._M_impl._M_start = 0;
            __x._M_impl._M_finish = 0;
            __x._M_impl._M_end_of_storage = 0;
        }
#endif

        ~_Vector_base() {
            _M_deallocate(this->_M_impl._M_start, this->_M_impl._M_end_of_storage
                    - this->_M_impl._M_start);
        }

    public:
        _Vector_impl _M_impl;

        typename _Tp_alloc_type::pointer
        _M_allocate(size_t __n) {
            return __n != 0 ? _M_impl.allocate(__n) : 0;
        }

        void
        _M_deallocate(typename _Tp_alloc_type::pointer __p, size_t __n) {
            if (__p)
                _M_impl.deallocate(__p, __n);
        }
    };

#define __glibcxx_class_requires(_a,_b)
#define __glibcxx_class_requires2(_a,_b,_c)

    template<typename _Tp, typename _Alloc = allocator<_Tp> >
            class vector : protected _Vector_base<_Tp, _Alloc> {
        // Concept requirements.
        typedef typename _Alloc::value_type _Alloc_value_type;
        __glibcxx_class_requires(_Tp, _SGIAssignableConcept)
        __glibcxx_class_requires2(_Tp, _Alloc_value_type, _SameTypeConcept)

        typedef _Vector_base<_Tp, _Alloc> _Base;
        typedef typename _Base::_Tp_alloc_type _Tp_alloc_type;

    public:
        typedef _Tp value_type;
        typedef typename _Tp_alloc_type::pointer pointer;
        typedef typename _Tp_alloc_type::const_pointer const_pointer;
        typedef typename _Tp_alloc_type::reference reference;
        typedef typename _Tp_alloc_type::const_reference const_reference;
        typedef __gnu_cxx::__normal_iterator<pointer, vector> iterator;
        typedef __gnu_cxx::__normal_iterator<const_pointer, vector>
        const_iterator;
        typedef reverse_iterator<const_iterator> const_reverse_iterator;
        typedef reverse_iterator<iterator> reverse_iterator;
        typedef size_t size_type;
        typedef ptrdiff_t difference_type;
        typedef _Alloc allocator_type;

    protected:
        using _Base::_M_allocate;
        using _Base::_M_deallocate;
        using _Base::_M_impl;
        using _Base::_M_get_Tp_allocator;

    public:
        // [23.2.4.1] construct/copy/destroy
        // (assign() and get_allocator() are also listed in this section)

        /**
         *  @brief  Default constructor creates no elements.
         */
        vector()
        : _Base() {
        }

        /**
         *  @brief  Creates a %vector with no elements.
         *  @param  a  An allocator object.
         */
        explicit
        vector(const allocator_type& __a)
        : _Base(__a) {
        }

        /**
         *  @brief  Creates a %vector with copies of an exemplar element.
         *  @param  n  The number of elements to initially create.
         *  @param  value  An element to copy.
         *  @param  a  An allocator.
         *
         *  This constructor fills the %vector with @a n copies of @a value.
         */

        /**
         *  @brief  %Vector copy constructor.
         *  @param  x  A %vector of identical element and allocator types.
         *
         *  The newly-created %vector uses a copy of the allocation
         *  object used by @a x.  All the elements of @a x are copied,
         *  but any extra memory in
         *  @a x (for fast expansion) will not be copied.
         */

        /**
         *  @brief  Builds a %vector from a range.
         *  @param  first  An input iterator.
         *  @param  last  An input iterator.
         *  @param  a  An allocator.
         *
         *  Create a %vector consisting of copies of the elements from
         *  [first,last).
         *
         *  If the iterators are forward, bidirectional, or
         *  random-access, then this will call the elements' copy
         *  constructor N times (where N is distance(first,last)) and do
         *  no memory reallocation.  But if only input iterators are
         *  used, then this will do at most 2N calls to the copy
         *  constructor, and logN memory reallocations.
         */

        /**
         *  The dtor only erases the elements, and note that if the
         *  elements themselves are pointers, the pointed-to memory is
         *  not touched in any way.  Managing the pointer is the user's
         *  responsibility.
         */

        /**
         *  @brief  %Vector assignment operator.
         *  @param  x  A %vector of identical element and allocator types.
         *
         *  All the elements of @a x are copied, but any extra memory in
         *  @a x (for fast expansion) will not be copied.  Unlike the
         *  copy constructor, the allocator object is not copied.
         */
        vector&
                operator=(const vector& __x);

        /**
         *  @brief  Assigns a given value to a %vector.
         *  @param  n  Number of elements to be assigned.
         *  @param  val  Value to be assigned.
         *
         *  This function fills a %vector with @a n copies of the given
         *  value.  Note that the assignment completely changes the
         *  %vector and that the resulting %vector's size is the same as
         *  the number of elements assigned.  Old data may be lost.
         */
        void
        assign(size_type __n, const value_type& __val) {
            _M_fill_assign(__n, __val);
        }

        /**
         *  @brief  Assigns a range to a %vector.
         *  @param  first  An input iterator.
         *  @param  last   An input iterator.
         *
         *  This function fills a %vector with copies of the elements in the
         *  range [first,last).
         *
         *  Note that the assignment completely changes the %vector and
         *  that the resulting %vector's size is the same as the number
         *  of elements assigned.  Old data may be lost.
         */

        /// Get a copy of the memory allocation object.
        using _Base::get_allocator;

        // iterators

        /**
         *  Returns a read/write iterator that points to the first
         *  element in the %vector.  Iteration is done in ordinary
         *  element order.
         */
        iterator
        begin() {
            return iterator(this->_M_impl._M_start);
        }

        /**
         *  Returns a read-only (constant) iterator that points to the
         *  first element in the %vector.  Iteration is done in ordinary
         *  element order.
         */
        const_iterator
        begin() const {
            return const_iterator(this->_M_impl._M_start);
        }

        /**
         *  Returns a read/write iterator that points one past the last
         *  element in the %vector.  Iteration is done in ordinary
         *  element order.
         */
        iterator
        end() {
            return iterator(this->_M_impl._M_finish);
        }

        /**
         *  Returns a read-only (constant) iterator that points one past
         *  the last element in the %vector.  Iteration is done in
         *  ordinary element order.
         */
        const_iterator
        end() const {
            return const_iterator(this->_M_impl._M_finish);
        }

        /**
         *  Returns a read/write reverse iterator that points to the
         *  last element in the %vector.  Iteration is done in reverse
         *  element order.
         */
        reverse_iterator
        rbegin() {
            return reverse_iterator(end());
        }

        /**
         *  Returns a read-only (constant) reverse iterator that points
         *  to the last element in the %vector.  Iteration is done in
         *  reverse element order.
         */
        const_reverse_iterator
        rbegin() const {
            return const_reverse_iterator(end());
        }

        /**
         *  Returns a read/write reverse iterator that points to one
         *  before the first element in the %vector.  Iteration is done
         *  in reverse element order.
         */
        reverse_iterator
        rend() {
            return reverse_iterator(begin());
        }

        /**
         *  Returns a read-only (constant) reverse iterator that points
         *  to one before the first element in the %vector.  Iteration
         *  is done in reverse element order.
         */
        const_reverse_iterator
        rend() const {
            return const_reverse_iterator(begin());
        }

        // [23.2.4.2] capacity

        /**  Returns the number of elements in the %vector.  */
        size_type
        size() const {
            return size_type(this->_M_impl._M_finish - this->_M_impl._M_start);
        }

        /**  Returns the size() of the largest possible %vector.  */
        size_type
        max_size() const {
            return _M_get_Tp_allocator().max_size();
        }

        /**
         *  @brief  Resizes the %vector to the specified number of elements.
         *  @param  new_size  Number of elements the %vector should contain.
         *  @param  x  Data with which new elements should be populated.
         *
         *  This function will %resize the %vector to the specified
         *  number of elements.  If the number is smaller than the
         *  %vector's current size the %vector is truncated, otherwise
         *  the %vector is extended and new elements are populated with
         *  given data.
         */

        /**
         *  Returns the total number of elements that the %vector can
         *  hold before needing to allocate more memory.
         */
        size_type
        capacity() const {
            return size_type(this->_M_impl._M_end_of_storage
                    - this->_M_impl._M_start);
        }

        /**
         *  Returns true if the %vector is empty.  (Thus begin() would
         *  equal end().)
         */
        bool
        empty() const {
            return begin() == end();
        }

        /**
         *  @brief  Attempt to preallocate enough memory for specified number of
         *          elements.
         *  @param  n  Number of elements required.
         *  @throw  std::length_error  If @a n exceeds @c max_size().
         *
         *  This function attempts to reserve enough memory for the
         *  %vector to hold the specified number of elements.  If the
         *  number requested is more than max_size(), length_error is
         *  thrown.
         *
         *  The advantage of this function is that if optimal code is a
         *  necessity and the user can determine the number of elements
         *  that will be required, the user can reserve the memory in
         *  %advance, and thus prevent a possible reallocation of memory
         *  and copying of %vector data.
         */
        void
        reserve(size_type __n);

        // element access

        /**
         *  @brief  Subscript access to the data contained in the %vector.
         *  @param n The index of the element for which data should be
         *  accessed.
         *  @return  Read/write reference to data.
         *
         *  This operator allows for easy, array-style, data access.
         *  Note that data access with this operator is unchecked and
         *  out_of_range lookups are not defined. (For checked lookups
         *  see at().)
         */
        reference
        operator[](size_type __n) {
            return *(this->_M_impl._M_start + __n);
        }

        /**
         *  @brief  Subscript access to the data contained in the %vector.
         *  @param n The index of the element for which data should be
         *  accessed.
         *  @return  Read-only (constant) reference to data.
         *
         *  This operator allows for easy, array-style, data access.
         *  Note that data access with this operator is unchecked and
         *  out_of_range lookups are not defined. (For checked lookups
         *  see at().)
         */
        const_reference
        operator[](size_type __n) const {
            return *(this->_M_impl._M_start + __n);
        }

    protected:
        /// Safety check used only from at().

        void
        _M_range_check(size_type __n) const {
        }

    public:

        /**
         *  @brief  Provides access to the data contained in the %vector.
         *  @param n The index of the element for which data should be
         *  accessed.
         *  @return  Read/write reference to data.
         *  @throw  std::out_of_range  If @a n is an invalid index.
         *
         *  This function provides for safer data access.  The parameter
         *  is first checked that it is in the range of the vector.  The
         *  function throws out_of_range if the check fails.
         */
        reference
        at(size_type __n) {
            _M_range_check(__n);
            return (*this)[__n];
        }

        /**
         *  @brief  Provides access to the data contained in the %vector.
         *  @param n The index of the element for which data should be
         *  accessed.
         *  @return  Read-only (constant) reference to data.
         *  @throw  std::out_of_range  If @a n is an invalid index.
         *
         *  This function provides for safer data access.  The parameter
         *  is first checked that it is in the range of the vector.  The
         *  function throws out_of_range if the check fails.
         */
        const_reference
        at(size_type __n) const {
            _M_range_check(__n);
            return (*this)[__n];
        }

        /**
         *  Returns a read/write reference to the data at the first
         *  element of the %vector.
         */
        reference
        front() {
            return *begin();
        }

        /**
         *  Returns a read-only (constant) reference to the data at the first
         *  element of the %vector.
         */
        const_reference
        front() const {
            return *begin();
        }

        /**
         *  Returns a read/write reference to the data at the last
         *  element of the %vector.
         */
        reference
        back() {
            return *(end() - 1);
        }

        /**
         *  Returns a read-only (constant) reference to the data at the
         *  last element of the %vector.
         */
        const_reference
        back() const {
            return *(end() - 1);
        }

        // _GLIBCXX_RESOLVE_LIB_DEFECTS
        // DR 464. Suggestion for new member functions in standard containers.
        // data access

        /**
         *   Returns a pointer such that [data(), data() + size()) is a valid
         *   range.  For a non-empty %vector, data() == &front().
         */
        pointer
        data() {
            return pointer(this->_M_impl._M_start);
        }

        const_pointer
        data() const {
            return const_pointer(this->_M_impl._M_start);
        }

        // [23.2.4.3] modifiers

        /**
         *  @brief  Add data to the end of the %vector.
         *  @param  x  Data to be added.
         *
         *  This is a typical stack operation.  The function creates an
         *  element at the end of the %vector and assigns the given data
         *  to it.  Due to the nature of a %vector this operation can be
         *  done in constant time if the %vector has preallocated space
         *  available.
         */
        void
        push_back(const value_type& __x) {
            if (this->_M_impl._M_finish != this->_M_impl._M_end_of_storage) {
                this->_M_impl.construct(this->_M_impl._M_finish, __x);
                ++this->_M_impl._M_finish;
            } else
                _M_insert_aux(end(), __x);
        }

        /**
         *  @brief  Removes last element.
         *
         *  This is a typical stack operation. It shrinks the %vector by one.
         *
         *  Note that no data is returned, and if the last element's
         *  data is needed, it should be retrieved before pop_back() is
         *  called.
         */
        void
        pop_back() {
            --this->_M_impl._M_finish;
            this->_M_impl.destroy(this->_M_impl._M_finish);
        }


        /**
         *  @brief  Inserts given value into %vector before specified iterator.
         *  @param  position  An iterator into the %vector.
         *  @param  x  Data to be inserted.
         *  @return  An iterator that points to the inserted data.
         *
         *  This function will insert a copy of the given value before
         *  the specified location.  Note that this kind of operation
         *  could be expensive for a %vector and if it is frequently
         *  used the user should consider using std::list.
         */
        iterator
        insert(iterator __position, const value_type& __x);

        /**
         *  @brief  Inserts a number of copies of given data into the %vector.
         *  @param  position  An iterator into the %vector.
         *  @param  n  Number of elements to be inserted.
         *  @param  x  Data to be inserted.
         *
         *  This function will insert a specified number of copies of
         *  the given data before the location specified by @a position.
         *
         *  Note that this kind of operation could be expensive for a
         *  %vector and if it is frequently used the user should
         *  consider using std::list.
         */
        void
        insert(iterator __position, size_type __n, const value_type& __x) {
            _M_fill_insert(__position, __n, __x);
        }

        /**
         *  @brief  Inserts a range into the %vector.
         *  @param  position  An iterator into the %vector.
         *  @param  first  An input iterator.
         *  @param  last   An input iterator.
         *
         *  This function will insert copies of the data in the range
         *  [first,last) into the %vector before the location specified
         *  by @a pos.
         *
         *  Note that this kind of operation could be expensive for a
         *  %vector and if it is frequently used the user should
         *  consider using std::list.
         */


        /**
         *  @brief  Remove element at given position.
         *  @param  position  Iterator pointing to element to be erased.
         *  @return  An iterator pointing to the next element (or end()).
         *
         *  This function will erase the element at the given position and thus
         *  shorten the %vector by one.
         *
         *  Note This operation could be expensive and if it is
         *  frequently used the user should consider using std::list.
         *  The user is also cautioned that this function only erases
         *  the element, and that if the element is itself a pointer,
         *  the pointed-to memory is not touched in any way.  Managing
         *  the pointer is the user's responsibility.
         */
        iterator
        erase(iterator __position);

        /**
         *  @brief  Remove a range of elements.
         *  @param  first  Iterator pointing to the first element to be erased.
         *  @param  last  Iterator pointing to one past the last element to be
         *                erased.
         *  @return  An iterator pointing to the element pointed to by @a last
         *           prior to erasing (or end()).
         *
         *  This function will erase the elements in the range [first,last) and
         *  shorten the %vector accordingly.
         *
         *  Note This operation could be expensive and if it is
         *  frequently used the user should consider using std::list.
         *  The user is also cautioned that this function only erases
         *  the elements, and that if the elements themselves are
         *  pointers, the pointed-to memory is not touched in any way.
         *  Managing the pointer is the user's responsibility.
         */
        iterator
        erase(iterator __first, iterator __last);

        /**
         *  @brief  Swaps data with another %vector.
         *  @param  x  A %vector of the same element and allocator types.
         *
         *  This exchanges the elements between two vectors in constant time.
         *  (Three pointers, so it should be quite fast.)
         *  Note that the global std::swap() function is specialized such that
         *  std::swap(v1,v2) will feed to this function.
         */


        /**
         *  Erases all the elements.  Note that this function only erases the
         *  elements, and that if the elements themselves are pointers, the
         *  pointed-to memory is not touched in any way.  Managing the pointer is
         *  the user's responsibility.
         */

    protected:
        /**
         *  Memory expansion handler.  Uses the member allocation function to
         *  obtain @a n bytes of memory, and then copies [first,last) into it.
         */

        // Internal constructor functions follow.

        // Called by the range constructor to implement [23.1.1]/9

        // _GLIBCXX_RESOLVE_LIB_DEFECTS
        // 438. Ambiguity in the "do the right thing" clause

        // Called by the range constructor to implement [23.1.1]/9

        // Called by the second initialize_dispatch above

        // Called by the second initialize_dispatch above

        // Called by the first initialize_dispatch above and by the
        // vector(n,value,a) constructor.


        // Internal assign functions follow.  The *_aux functions do the actual
        // assignment work for the range versions.

        // Called by the range assign to implement [23.1.1]/9

        // _GLIBCXX_RESOLVE_LIB_DEFECTS
        // 438. Ambiguity in the "do the right thing" clause

        template<typename _Integer>
        void
        _M_assign_dispatch(_Integer __n, _Integer __val, __true_type) {
            _M_fill_assign(__n, __val);
        }

        // Called by the range assign to implement [23.1.1]/9

        // Called by the second assign_dispatch above

        // Called by assign(n,t), and the range assign when it turns out
        // to be the same thing.
        void
        _M_fill_assign(size_type __n, const value_type& __val);


        // Internal insert functions follow.

        // Called by the range insert to implement [23.1.1]/9

        // _GLIBCXX_RESOLVE_LIB_DEFECTS
        // 438. Ambiguity in the "do the right thing" clause

        template<typename _Integer>
        void
        _M_insert_dispatch(iterator __pos, _Integer __n, _Integer __val,
                __true_type) {
            _M_fill_insert(__pos, __n, __val);
        }



        // Called by insert(p,n,x), and the range insert when it turns out to be
        // the same thing.
        void
        _M_fill_insert(iterator __pos, size_type __n, const value_type& __x);

        // Called by insert(p,x)
        void
        _M_insert_aux(iterator __position, const value_type& __x);


    };

}

using namespace bug185657;

class A {
public:

    int bar() {
        return 0;
    }
};

void foo() {
    vector<vector<A> > board_labels;
    board_labels[0][0].bar();
}


