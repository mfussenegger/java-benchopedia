
package benchopedia;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2)
@Fork(value = 2)
@State(Scope.Thread)
public class UUIDBenchmark {

    @Benchmark
    public UUID secureRandomUUID() {
        return UUID.randomUUID();
    }

    @Benchmark
    public UUID randomLongs() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long mostBits = random.nextLong();
        long leastBits = random.nextLong();
        return new UUID(mostBits, leastBits);
    }

    public static void main(String[] args) throws Exception {
        Runner runner = new Runner(
            new OptionsBuilder()
                .include(UUIDBenchmark.class.getSimpleName())
                .build()
        );
        runner.run();
    }
}
