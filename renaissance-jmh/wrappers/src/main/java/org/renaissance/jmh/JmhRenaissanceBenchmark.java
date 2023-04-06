package org.renaissance.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.renaissance.BenchmarkContext;
import org.renaissance.BenchmarkResult;
import org.renaissance.BenchmarkResult.ValidationException;
import org.renaissance.core.BenchmarkDescriptor;
import org.renaissance.core.BenchmarkSuite;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.renaissance.core.DirUtils.createScratchDirectory;

public abstract class JmhRenaissanceBenchmark {

  /**
   * Determines whether to fake runs for incompatible benchmarks. This
   * is needed to avoid JMH failures in automated runs using different JVM
   * versions. Some benchmarks require specific range of JVM versions
   * and there is no way to signal incompatibility from JMH, apart from an
   * exception (which will fail JMH).
   */
  private static final boolean fakeIncompatibleBenchmarks = Boolean.parseBoolean(
    System.getProperty("org.renaissance.jmh.fakeIncompatible", "false")
  );

  /** Base directory in which to create scratch directories. */
  private static final String scratchBaseDir = System.getProperty(
    "org.renaissance.jmh.scratchBase", ""
  );

  /** Determines whether to avoid removing scratch directories on VM exit. */
  private static final boolean keepScratch = Boolean.parseBoolean(
    System.getProperty("org.renaissance.jmh.keepScratch", "false")
  );

  /** Determines the benchmark configuration to use. Defaults to 'jmh'. */
  private static final String configuration = System.getProperty(
    "org.renaissance.jmh.configuration", "jmh"
  );

  private final org.renaissance.Benchmark benchmark;
  private final BenchmarkContext context;

  private BenchmarkResult result;

  protected JmhRenaissanceBenchmark(final String name) {
    // Create scratch root so that we can initialize the suite.
    final Path scratchRootDir = createScratchRoot();

    // Get benchmark information and fake the run if necessary.
    final BenchmarkSuite suite = createSuite(scratchRootDir);
    BenchmarkDescriptor bd = suite.getBenchmark(name);
    if (!suite.isBenchmarkCompatible(bd)) {
      String message = String.format(
        "Benchmark '%s' is not compatible with this JVM version!", bd.name()
      );

      if (!fakeIncompatibleBenchmarks) {
        throw new RuntimeException(message);
      } else {
        bd = suite.getBenchmark("dummy-empty");
      }

      System.out.printf(
        "\n!!!!! %s Using '%s' to avoid failure. !!!!!\n",
        message, bd.name()
      );
    }

    // Load the benchmark.
    benchmark = suite.createBenchmark(bd);
    context = suite.createBenchmarkContext(bd);
  }

  private Path createScratchRoot() {
    try {
      return createScratchDirectory(
        Paths.get(scratchBaseDir), "jmh-", keepScratch
      );

    } catch (IOException e) {
      throw new RuntimeException("failed to create scratch root", e);
    }
  }

  private BenchmarkSuite createSuite(Path scratchRootDir) {
    try {
      return BenchmarkSuite.create(
        scratchRootDir, configuration,
        Optional.empty(), emptyMap(),
        true /* with module loader */
      );
    } catch (IOException e) {
      throw new RuntimeException("failed to create benchmark suite", e);
    }
  }

  //

  @Setup(Level.Trial)
  public final void setUpBenchmark() {
    benchmark.setUpBeforeAll(context);
  }

  @Setup(Level.Iteration)
  public final void setUpOperation() {
    benchmark.setUpBeforeEach(context);
  }

  @Benchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(MILLISECONDS)
  @Measurement(timeUnit = MILLISECONDS)
  public final void runOperation() {
    result = benchmark.run(context);
  }

  @TearDown(Level.Iteration)
  public final void tearDownOperation() throws ValidationException {
    benchmark.tearDownAfterEach(context);

    result.validate();
    result = null;
  }

  @TearDown(Level.Trial)
  public final void defaultTearBenchmark() {
    benchmark.tearDownAfterAll(context);
  }

}
