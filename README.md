# Scout

[![Build Status](https://secure.travis-ci.org/efritz/scout.png)](http://travis-ci.org/efritz/scout)

Scout is a simple spatial database written in Java. It is written to be used with dynamic (moving) objects in real-time.

## Usage

### AABB

Scout's geometric primitive is an [axis-aligned bounding box](http://en.wikipedia.org/wiki/Axis-aligned_bounding_box#Axis-aligned_minimum_bounding_box), or `AABB`. Scout provides both a two and three-dimensional `AABB` implementation. Higher-dimensional implementations may be used at the cost of slower execution speeds increased memory consumption.

Finer-grained bounding volumes are not directly supported. If more accurate bounding volumes are required, Scout should be used for broad-phase queries whose matches are re-queried with the proper geometries.

### Spatial Index

Scout provides a dictionary-like interface to associate arbitrary objects with a geometric space. This dictionary is fully dynamic, allowing insertions, updates, and removals at any point.

```java
// Create an index
SpatialIndex<Object> index = new SpatialIndex<Object>();

// Insert an object
index.insert(object, new AABB2(0, 0, 50, 25));

// Move an object
index.update(object, new AABB2(5, 5, 55, 30));

// Remove an object
index.remove(object);
```

The index is implemented as a variant of the R-Tree data structure. The structure follows [Guttman '84](http://www-db.deis.unibo.it/courses/SI-LS/papers/Gut84.pdf) as described with the exception of the `SplitNode` procedure. In place of Guttman's quadratic or linear-cost split algorithms, Scout uses `k-means` clustering. This heuristic aims to minimize the overlap of the resulting minimum bounding boxes.

### Queries

A spatial query traverses the index looking for elements that fulfill a given criteria. The following queries are provided. Additional queries may be constructed by implementing the `Query` interface.

Spatial Query     | Description
-------------     | -----------
AllQuery          | Returns all objects.
IntersectionQuery | Returns all objects that intersect with a given `AABB`.
ContainsQuery     | Returns all objects that are contained by a given `AABB`.
ContainedQuery    | Returns all objects that contain a given `AABB`.
DistanceQuery     | Returns all objects `d` units or less from a given `AABB`.

Instead of building a result list, Scout uses the [visitor pattern](http://en.wikipedia.org/wiki/Visitor_pattern) to iterate queried objects. The visitor will receive each object that matches the query constraints, one at a time. The order that the visitor receives objects is subject to the internal structure of the tree and cannot be guaranteed.

```java
SpatialIndex<E> index = new SpatialIndex<E>();

index.query(new ContainsQuery(new AABB(0, 0, 100, 100)), new QueryResultVisitor<E>() {
    public boolean visit(E o) {
        // do something with o
        return true;
    }
});
```

The visitor may return `false` from the `visit` method in the case that the tree traversal should halt.

### Join Queries

A spatial join query traverses two trees simultaneously looking for pairs of elements that fulfill a given criteria. Spatial join queries result in pairs of objects `(e1, e2)` such that `e1` is an element from the first index, and `e2` is an element from the second index. Joined indices need not have the same generic type. In the case that an index is joined with itself, duplicate pairs (`(e1, e2)` and `(e2, e1)`) will not result. The following join queries are provided. Additional join queries may be constructed by implementing the `JoinQuery` interface.

Spatial Query         | Description
-------------         | -----------
AllJoinQuery          | Returns all pairs of objects.
IntersectionJoinQuery | Returns all pairs of objects that intersect.
ContainsJoinQuery     | Returns all pairs of objects such that `e1.contains(e2)`.
ContainedJoinQuery    | Returns all pairs of objects such that `e2.contains(e1)`.
DistanceJoinQuery     | Returns all pairs of objects `d` units or less from each other.

Join queries also use visitors to iterate queried objects. The visitor will receive pairs of objects that match the query constraints, one at a time. The order that the visitor receives objects is subject to the internal structure of the tree and cannot be guaranteed. In the case that an index is joined with itself, the elements of a pair may be received in either order, `(e1, e2)` or `(e2, e1)`.

```java
SpatialIndex<E> index1 = new SpatialIndex<E>();
SpatialIndex<F> index2 = new SpatialIndex<F>();

index1.query(index2, new IntersectionJoinQuery(), new JoinQueryResultVisitor<E, F>() {
    public boolean visit(E o1, F, o2) {
        // do something with o1 and o2
        return true;
    }
});
```

The visitor may return `false` from the `visit` method in the case that the tree traversal should halt.

## License

Copyright (c) 2013 Eric Fritz

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
