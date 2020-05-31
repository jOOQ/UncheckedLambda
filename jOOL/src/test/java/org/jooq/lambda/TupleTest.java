/**
 * Copyright (c), Data Geekery GmbH, contact@datageekery.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jooq.lambda;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.averagingInt;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static org.jooq.lambda.tuple.Tuple.collectors;
import static org.jooq.lambda.tuple.Tuple.range;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import org.jooq.lambda.tuple.Tuple5;
import org.junit.Assert;
import org.junit.Test

/**
 * @author Lukas Eder
 */
public class TupleTest {

    @Test
    public void testEqualsHashCode() {
        Set<Tuple2<Integer, String>> set = new HashSet<>();

        set.add(tuple(1, "abc"));
        assertEquals(1, set.size());
        set.add(tuple(1, "abc"));
        assertEquals(1, set.size());
        set.add(tuple(null, null));
        assertEquals(2, set.size());
        set.add(tuple(null, null));
        assertEquals(2, set.size());
        set.add(tuple(1, null));
        assertEquals(3, set.size());
        set.add(tuple(1, null));
        assertEquals(3, set.size());
    }

    @Test
    public void testEqualsNull() {
        assertFalse(tuple(1).equals(null));
        assertFalse(tuple(1, 2).equals(null));
        assertFalse(tuple(1, 2, 3).equals(null));
    }

    @Test
    public void testToString() {
        assertEquals("(1, abc)", tuple(1, "abc").toString());
    }

    @Test
    public void testToArrayAndToList() {
        assertEquals(asList(1, "a", null), asList(tuple(1, "a", null).toArray()));
        assertEquals(asList(1, "a", null), tuple(1, "a", null).toList());
    }
    
    @Test
    public void testToMap() {
        Map<String, Object> m1 = new LinkedHashMap<>();
        m1.put("v1", 1);
        m1.put("v2", "a");
        m1.put("v3", null);
        assertEquals(m1, tuple(1, "a", null).toMap());

        Map<Integer, Object> m2 = new LinkedHashMap<>();
        m2.put(0, 1);
        m2.put(1, "a");
        m2.put(2, null);
        assertEquals(m2, tuple(1, "a", null).toMap(i -> i));
        
        Map<String, Object> m3 = new LinkedHashMap<>();
        m3.put("A", 1);
        m3.put("B", "a");
        m3.put("C", null);
        assertEquals(m3, tuple(1, "a", null).toMap("A", "B", "C"));
        assertEquals(m3, tuple(1, "a", null).toMap(() -> "A", () -> "B", () -> "C"));
    }
    
    @Test
    public void testToSeq() {
        assertEquals(asList(1, "a", null), tuple(1, "a", null).toSeq().toList());
    }

    @Test
    public void testSwap() {
        assertEquals(tuple(1, "a"), tuple("a", 1).swap());
        assertEquals(tuple(1, "a"), tuple(1, "a").swap().swap());
    }

    @Test
    public void testConcat() {
        assertEquals(tuple(1, "a"), tuple(1).concat("a"));
        assertEquals(tuple(1, "a", 2), tuple(1).concat("a").concat(2));

        assertEquals(tuple(1, "a"), tuple(1).concat(tuple("a")));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4, "d"), tuple(1).concat(tuple("a", 2, "b").concat(tuple(3).concat(tuple("c", 4, "d")))));
    }

    @Test
    public void testCompareTo() {
        Set<Tuple2<Integer, String>> set = new TreeSet<>();

        set.add(tuple(2, "a"));
        set.add(tuple(1, "b"));
        set.add(tuple(1, "a"));
        set.add(tuple(2, "a"));

        assertEquals(3, set.size());
        assertEquals(asList(tuple(1, "a"), tuple(1, "b"), tuple(2, "a")), new ArrayList<>(set));
    }

    @Test
    public void testCompareToWithNulls() {
        Set<Tuple2<Integer, String>> set = new TreeSet<>();

        set.add(tuple(2, "a"));
        set.add(tuple(1, "b"));
        set.add(tuple(1, null));
        set.add(tuple(null, "a"));
        set.add(tuple(null, "b"));
        set.add(tuple(null, null));

        assertEquals(6, set.size());
        assertEquals(asList(tuple(1, "b"), tuple(1, null), tuple(2, "a"), tuple(null, "a"), tuple(null, "b"), tuple(null, null)), new ArrayList<>(set));
    }

    @Test
    public void testCompareToWithNonComparables() {
        Set<Tuple2<Integer, Object>> set = new TreeSet<>();
        Utils.assertThrows(ClassCastException.class, () -> set.add(tuple(1, new Object())));
        assertEquals(0, set.size());
    }

    @Test
    public void testIterable() {
        LinkedList<Object> list = new LinkedList<>(tuple(1, "b", null).toList());
        for (Object o : tuple(1, "b", null)) {
            assertEquals(list.poll(), o);
        }
    }

    @Test
    public void testFunctions() {
        assertEquals("(1, b, null)", tuple(1, "b", null).map((v1, v2, v3) -> tuple(v1, v2, v3).toString()));
        assertEquals("1-b", tuple(1, "b", null).map((v1, v2, v3) -> v1 + "-" + v2));
    }

    @Test
    public void testMapN() {
        assertEquals(tuple(1, "a", 2, "b"), tuple(1, null, 2, null).map2(v -> "a").map4(v -> "b"));
    }

    @Test
    public void testRangeOverlaps() {
        assertTrue(range(1, 3).overlaps(1, 3));
        assertTrue(range(1, 3).overlaps(2, 3));
        assertTrue(range(1, 3).overlaps(2, 4));
        assertTrue(range(1, 3).overlaps(3, 4));
        assertFalse(range(1, 3).overlaps(4, 5));
        assertFalse(range(1, 1).overlaps(2, 2));

        assertTrue(range(1, 3).overlaps(range(1, 3)));
        assertTrue(range(1, 3).overlaps(range(2, 3)));
        assertTrue(range(1, 3).overlaps(range(2, 4)));
        assertTrue(range(1, 3).overlaps(range(3, 4)));
        assertFalse(range(1, 3).overlaps(range(4, 5)));
        assertFalse(range(1, 1).overlaps(range(2, 2)));
    }

    @Test
    public void testRangeOverlapsWithInfiniteBounds() {
        assertTrue(range(null, null).overlaps(range(null, null)));
        assertTrue(range((Integer) null, null).overlaps(range(1, 3)));
        assertTrue(range(1, 3).overlaps(range(null, null)));

        assertTrue(range(3, null).overlaps(range(2, 3)));
        assertFalse(range(3, null).overlaps(range(1, 2)));
        assertTrue(range(3, null).overlaps(range(null, 3)));
        assertFalse(range(3, null).overlaps(range(null, 2)));

        assertTrue(range(2, 3).overlaps(range(3, null)));
        assertFalse(range(2, 3).overlaps(range(4, null)));
        assertTrue(range(2, 3).overlaps(range(null, 2)));
        assertFalse(range(2, 3).overlaps(range(null, 1)));

        assertTrue(range(null, 3).overlaps(3, null));
        assertFalse(range(null, 3).overlaps(4, null));
        assertTrue(range(null, 3).overlaps(3, 5));
        assertFalse(range(null, 3).overlaps(4, 6));
    }

    @Test
    public void testRangeIntersect() {
        assertEquals(Optional.of(range(2, 3)), range(1, 3).intersect(range(2, 4)));
        assertEquals(Optional.of(range(2, 3)), range(3, 1).intersect(range(4, 2)));
        assertEquals(Optional.of(range(3, 3)), range(1, 3).intersect(3, 5));
        assertEquals(Optional.empty(), range(1, 3).intersect(range(4, 5)));
    }

    @Test
    public void testRangeIntersectWithInfiniteBounds() {
        assertEquals(Optional.of(range(null, null)), range(null, null).intersect(range(null, null)));
        assertEquals(Optional.of(range(1, 3)), range((Integer) null, null).intersect(range(1, 3)));
        assertEquals(Optional.of(range(1, 3)), range(1, 3).intersect(range(null, null)));

        assertEquals(Optional.of(range(3, 3)), range(3, null).intersect(range(2, 3)));
        assertEquals(Optional.empty(), range(3, null).intersect(range(1, 2)));
        assertEquals(Optional.of(range(3, 3)), range(3, null).intersect(range(null, 3)));
        assertEquals(Optional.empty(), range(3, null).intersect(range(null, 2)));

        assertEquals(Optional.of(range(3, 4)), range(2, 4).intersect(range(3, null)));
        assertEquals(Optional.empty(), range(2, 3).intersect(range(4, null)));
        assertEquals(Optional.of(range(2, 3)), range(2, 4).intersect(range(null, 3)));
        assertEquals(Optional.empty(), range(2, 3).intersect(range(null, 1)));
    }

    @Test
    public void testRange() {
        assertEquals(range(1, 3), range(3, 1));
    }

    @Test
    public void testRangeContainsValue() {
        assertFalse(range(1, 3).contains((Integer) null));
        assertFalse(range((Integer) null, null).contains((Integer) null));

        assertTrue(range(1, 3).contains(1));
        assertTrue(range(1, 3).contains(2));
        assertTrue(range(1, 3).contains(3));
        assertFalse(range(1, 3).contains(0));
        assertFalse(range(1, 3).contains(4));
        assertFalse(range(1, 3).contains(-1));
        assertFalse(range(1, 3).contains(50));

        assertTrue(range(null, 3).contains(1));
        assertTrue(range((Integer) null, null).contains(1));
        assertFalse(range(null, 3).contains(4));
    }

    @Test
    public void testRangeContainsRange() {
        assertTrue(range(null, null).contains(range(null, null)));
        assertTrue(range((Integer) null, null).contains(range(0, null)));
        assertTrue(range((Integer) null, null).contains(range(null, 0)));
        assertTrue(range((Integer) null, null).contains(range(0, 0)));

        assertFalse(range(null, 0).contains(range(null, null)));

        assertFalse(range(null, 0).contains(range(null, null)));
        assertTrue(range(null, 0).contains(range(null, 0)));
        assertTrue(range(null, 0).contains(range(null, -1)));
        assertFalse(range(null, 0).contains(range(null, 1)));

        assertFalse(range(0, null).contains(range(null, null)));
        assertTrue(range(0, null).contains(range(0, null)));
        assertFalse(range(0, null).contains(range(-1, null)));
        assertTrue(range(0, null).contains(range(1, null)));

        assertFalse(range(0, 1).contains(range(null, null)));
        assertFalse(range(0, 1).contains(range(0, null)));
        assertFalse(range(0, 1).contains(range(-1, null)));
        assertFalse(range(0, 1).contains(range(1, null)));

        assertTrue(range(1, 3).contains(range(1, 1)));
        assertTrue(range(1, 3).contains(range(1, 2)));
        assertTrue(range(1, 3).contains(range(1, 3)));
        assertFalse(range(1, 3).contains(range(0, 1)));
        assertFalse(range(1, 3).contains(range(1, 4)));
        assertFalse(range(1, 3).contains(range(-1, 1)));
        assertFalse(range(1, 3).contains(range(1, 50)));

        assertTrue(range(null, 3).contains(range(1, 2)));
        assertTrue(range((Integer) null, null).contains(range(1, 5)));
        assertFalse(range(null, 3).contains(range(1, 4)));
    }

    @Test
    public void testCollectorsWithAgg() {
        Tuple5<Long, Optional<BigDecimal>, Optional<BigDecimal>, Optional<BigDecimal>, Optional<BigDecimal>> result =
        Stream.of(new BigDecimal(0), new BigDecimal(1), new BigDecimal(2))
            .collect(collectors(
                Agg.count(),
                Agg.sum(),
                Agg.avg(),
                Agg.<BigDecimal>min(),
                Agg.<BigDecimal>max()
            ));
        assertEquals(
            tuple(
                3L,
                Optional.of(new BigDecimal(3)),
                Optional.of(new BigDecimal(1)),
                Optional.of(new BigDecimal(0)),
                Optional.of(new BigDecimal(2))
            ),
            result
        );
    }

    @Test
    public void testInverseFunctions() {
        //  Test the overloaded methods of inverse Tuple.function[N]
        //  --------------------------------------------------------
        assertEquals(3, (int)Tuple.function2(t -> (int)t.v1 + (int)t.v2).apply(1, 2));
        assertEquals(6, (int)Tuple.function3(t -> (int)t.v1 + (int)t.v2 + (int)t.v3).apply(1, 2, 3));
        assertEquals(7, (int)Tuple.function4(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v4).apply(1, 2, 3, 1));
        assertEquals(7, (int)Tuple.function5(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v5).apply(1, 2, 3, 1, 1));
        assertEquals(7, (int)Tuple.function6(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v6).apply(1, 2, 3, 1, 1, 1));
        assertEquals(7, (int)Tuple.function7(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v7).apply(1, 2, 3, 1, 1, 1, 1));
        assertEquals(7, (int)Tuple.function8(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v8).apply(1, 2, 3, 1, 1, 1, 1, 1));
        assertEquals(7, (int)Tuple.function9(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v9).apply(1, 2, 3, 1, 1, 1, 1, 1, 1));
        assertEquals(7, (int)Tuple.function10(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v10).apply(1, 2, 3, 1, 1, 1, 1, 1, 1, 1));
        assertEquals(7, (int)Tuple.function11(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v11).apply(1, 2, 3, 1, 1, 1, 1, 1, 1, 1, 1));
        assertEquals(7, (int)Tuple.function12(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v12).apply(1, 2, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1));
        assertEquals(7, (int)Tuple.function13(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v13).apply(1, 2, 3, 1, 1, 1, 1, 1, 1, 1 ,1, 1, 1));
        assertEquals(7, (int)Tuple.function14(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v14).apply(1, 2, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1));
        assertEquals(7, (int)Tuple.function15(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v15).apply(1, 2, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1));
        assertEquals(7, (int)Tuple.function16(t -> (int)t.v1 + (int)t.v2 + (int)t.v3 + (int)t.v16).apply(1, 2, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1));
    }
    
    @Test
    public void testCollectors() {
        assertEquals(
            tuple(3L),
            Stream.of(1, 2, 3)
                  .collect(collectors(counting()))
        );

        assertEquals(
            tuple(3L, "1, 2, 3"),
            Stream.of(1, 2, 3)
                  .collect(collectors(
                      counting(),
                      mapping(Object::toString, joining(", "))
                  ))
        );

        assertEquals(
            tuple(3L, "1, 2, 3", 2.0),
            Stream.of(1, 2, 3)
                  .collect(collectors(
                          counting(),
                          mapping(Object::toString, joining(", ")),
                          averagingInt(Integer::intValue)
                  ))
        );
    }

    @Test
    public void testLimit() {
        assertEquals(
            tuple(),
            tuple(1, "A", 2, "B").limit0()
        );
        assertEquals(
            tuple(1),
            tuple(1, "A", 2, "B").limit1()
        );
        assertEquals(
            tuple(1, "A"),
            tuple(1, "A", 2, "B").limit2()
        );
        assertEquals(
            tuple(1, "A", 2),
            tuple(1, "A", 2, "B").limit3()
        );
        assertEquals(
            tuple(1, "A", 2, "B"),
            tuple(1, "A", 2, "B").limit4()
        );
    }

    @Test
    public void testSkip() {
        assertEquals(
            tuple(              ),
            tuple(1, "A", 2, "B").skip4()
        );
        assertEquals(
            tuple(           "B"),
            tuple(1, "A", 2, "B").skip3()
        );
        assertEquals(
            tuple(        2, "B"),
            tuple(1, "A", 2, "B").skip2()
        );
        assertEquals(
            tuple(   "A", 2, "B"),
            tuple(1, "A", 2, "B").skip1()
        );
        assertEquals(
            tuple(1, "A", 2, "B"),
            tuple(1, "A", 2, "B").skip0()
        );
    }

    @Test
    public void testSplit() {
        assertEquals(
            tuple(
                tuple(              ),
                tuple(1, "A", 2, "B")
            ),
            tuple(1, "A", 2, "B").split0()
        );
        assertEquals(
            tuple(
                tuple(1             ),
                tuple(   "A", 2, "B")
            ),
            tuple(1, "A", 2, "B").split1()
        );
        assertEquals(
            tuple(
                tuple(1, "A"        ),
                new Tuple2<>(        2, "B") // Strange IntelliJ Bug here
            ),
            tuple(1, "A", 2, "B").split2()
        );
        assertEquals(
            tuple(
                tuple(1, "A", 2     ),
                tuple(           "B")
            ),
            tuple(1, "A", 2, "B").split3()
        );
        assertEquals(
            tuple(
                tuple(1, "A", 2, "B"),
                tuple(              )
            ),
            tuple(1, "A", 2, "B").split4()
        );
    }
}
