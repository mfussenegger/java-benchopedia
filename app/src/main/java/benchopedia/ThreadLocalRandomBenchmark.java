
package benchopedia;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2)
@Fork(value = 2)
@State(Scope.Benchmark)
public class ThreadLocalRandomBenchmark {

    @Param({"1000", "100000"})
    long max;

    @Benchmark
    public long tlrMax() {
        return ThreadLocalRandom.current().nextLong(max);
    }

    @Benchmark
    public long tlr() {
        return ThreadLocalRandom.current().nextLong();
    }
}
