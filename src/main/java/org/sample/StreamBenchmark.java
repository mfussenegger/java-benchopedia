/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sample;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2)
@Fork(value = 2)
@State(Scope.Thread)
public class StreamBenchmark {

    @Param({"31", "3000", "50000"})
    int size;

    List<Integer> items;
    Iterable<Integer> values;

    @Setup
    public void setup() {
        items = IntStream.range(1, size).mapToObj(x -> x).toList();
        values = () -> items.stream()
            .map(x -> x * 2)
            .unordered()
            .filter(x -> x % 2 == 0)
            .iterator();
    }


    @Benchmark
    public void map_list_baseline(Blackhole blackhole) {
        for (var x : items) {
            blackhole.consume(x);
        }
    }

    @Benchmark
    public void map_list_using_stream(Blackhole blackhole) {
        Iterable<Integer> mapped = () -> items.stream()
            .map(x -> x * 2)
            .iterator();
        for (var x : mapped) {
            blackhole.consume(x);
        }
    }

    @Benchmark
    public void map_list_using_lazy_mapped_iterable(Blackhole blackhole) {
        var mapped = new MappingIterable<>(items, x -> x * 2);
        for (var x : mapped) {
            blackhole.consume(x);
        }
    }

    @Benchmark
    public void map_list_using_lazy_mapped_list(Blackhole blackhole) {
        var mapped = new MappingList<>(items, x -> x * 2);
        for (var x : mapped) {
            blackhole.consume(x);
        }
    }

    @Benchmark
    public void map_iterable_baseline(Blackhole blackhole) {
        for (var x : values) {
            blackhole.consume(x);
        }
    }

    @Benchmark
    public void map_iterable_using_spliterator_stream(Blackhole blackhole) {
        Iterable<Integer> mapped = () -> StreamSupport.stream(values.spliterator(), false)
            .map(x -> x * 2)
            .iterator();
        for (var x : mapped) {
            blackhole.consume(x);
        }
    }

    @Benchmark
    public void map_iterable_using_mapping_iterable(Blackhole blackhole) {
        Iterable<Integer> mapped = new MappingIterable<>(values, x -> x * 2);
        for (var x : mapped) {
            blackhole.consume(x);
        }
    }


    static class MappingList<I, O> extends AbstractList<O> {

        private final List<I> list;
        private final Function<? super I, ? extends O> mapper;

        public MappingList(List<I> list, Function<? super I, ? extends O> mapper) {
            this.list = list;
            this.mapper = mapper;
        }

        @Override
        public O get(int index) {
            return mapper.apply(list.get(index));
        }

        @Override
        public int size() {
            return list.size();
        }
    }

    static class MappingIterable<I, O> implements Iterable<O> {

      private final Iterable<? extends I> items;
      private final Function<? super I, ? extends O> mapper;

      public MappingIterable(Iterable<? extends I> items, Function<? super I, ? extends O> mapper) {
          this.items = items;
          this.mapper = mapper;
      }

      @Override
      public Iterator<O> iterator() {
        Iterator<? extends I> iterator = items.iterator();
        return new Iterator<O>(){

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public O next() {
                return mapper.apply(iterator.next());
            }
        };
      }
    }
}
