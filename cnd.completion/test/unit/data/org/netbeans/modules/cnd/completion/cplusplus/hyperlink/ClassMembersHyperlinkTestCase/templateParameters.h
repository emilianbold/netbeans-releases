template<unsigned int L, class T, template<class> class C>
    class OrderedStatic<L, T, Loki::NullType> : public Private::OrderedStaticBase<T>
    {
    public:
        OrderedStatic() : Private::OrderedStaticBase<T>(L)
        {
            OrderedStaticManager::Instance().registerObject
                                (L,this,&Private::OrderedStaticCreatorFunc::createObject);
        }

        C createObject()
        {
            Private::OrderedStaticBase<T>::SetLongevity(new C<T>);
        }

    private:
        OrderedStatic(const OrderedStatic&);
        OrderedStatic& operator=(const OrderedStatic&);
    };
