
// IZ#154594: completion fails on expressions with keyword template

struct A
{
    typedef A type;
};

template <class T>
struct B
{
    typedef T type;
};

struct C : B<template B<A> >::type
{
};