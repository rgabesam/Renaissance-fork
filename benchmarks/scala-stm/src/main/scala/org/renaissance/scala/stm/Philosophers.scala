package org.renaissance.scala.stm

import org.renaissance.Benchmark
import org.renaissance.Benchmark._
import org.renaissance.BenchmarkContext
import org.renaissance.BenchmarkResult
import org.renaissance.BenchmarkResult.Validators
import org.renaissance.License

@Name("philosophers")
@Group("scala")
@Group("scala-stm")
@Summary("Solves a variant of the dining philosophers problem using ScalaSTM.")
@Licenses(Array(License.BSD3))
@Repetitions(30)
@Parameter(name = "thread_count", defaultValue = "$cpu.count")
@Parameter(
  name = "meal_count",
  defaultValue = "500000",
  summary = "Number of meals consumed by each philosopher thread"
)
@Configuration(name = "test", settings = Array("meal_count = 500"))
@Configuration(name = "jmh")
final class Philosophers extends Benchmark {

  // TODO: Consolidate benchmark parameters across the suite.
  //  See: https://github.com/renaissance-benchmarks/renaissance/issues/27

  private var threadCountParam: Int = _

  /**
   * Number of meals consumed by each Philosopher thread.
   */
  private var mealCountParam: Int = _

  override def setUpBeforeAll(c: BenchmarkContext) = {
    threadCountParam = c.parameter("thread_count").toPositiveInteger
    mealCountParam = c.parameter("meal_count").toPositiveInteger
  }

  override def run(c: BenchmarkContext): BenchmarkResult = {
    // TODO: Return something useful, not elapsed time
    RealityShowPhilosophers.run(mealCountParam, threadCountParam)

    // TODO: add proper validation
    Validators.dummy()
  }

}
