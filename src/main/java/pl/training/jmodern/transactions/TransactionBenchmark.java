package pl.training.jmodern.transactions;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xmx4g", "--enable-preview"})
@Warmup(iterations = 1)
@Measurement(iterations = 3)
public class TransactionBenchmark {

    @Param({"transactions-1_000_000.xml"})
    private String xmlFile;

    private final String jdbcUrl = "jdbc:postgresql://localhost:5432/transactions";

    @Setup(Level.Trial)
    public void setup() throws Exception {
        try (var conn = DriverManager.getConnection(jdbcUrl, "bench", "bench")) {
            TransactionProcessing.DatabaseHelper.createSchema(conn);
            TransactionProcessing.DatabaseHelper.cleanTable(conn);
        }
    }

    @Setup(Level.Invocation)
    public void cleanBefore() throws Exception {
        try (var conn = DriverManager.getConnection(jdbcUrl, "bench", "bench")) {
            TransactionProcessing.DatabaseHelper.cleanTable(conn);
        }
    }

    @Benchmark
    public void v1_DomBaseline(Blackhole bh) {
        try {
            var summary = new V1_DomBaseline().process(Path.of(xmlFile), jdbcUrl);
            bh.consume(summary);
        } catch (OutOfMemoryError e) {
            bh.consume(e);
        }
    }

    @Benchmark
    public void v2_StaxStreaming(Blackhole bh) {
        var summary = new V2_StaxStreaming().process(Path.of(xmlFile), jdbcUrl);
        bh.consume(summary);
    }

    @Benchmark
    public void v3_BatchInserts(Blackhole bh) {
        var summary = new V3_BatchInserts().process(Path.of(xmlFile), jdbcUrl);
        bh.consume(summary);
    }

    @Benchmark
    public void v4_TransactionOptimized(Blackhole bh) {
        var summary = new V4_TransactionOptimized().process(Path.of(xmlFile), jdbcUrl);
        bh.consume(summary);
    }

    @Benchmark
    public void v5_ParallelProcessing(Blackhole bh) {
        var summary = new V5_ParallelProcessing().process(Path.of(xmlFile), jdbcUrl);
        bh.consume(summary);
    }

    @Benchmark
    public void v6_MemoryMappedParser(Blackhole bh) {
        var summary = new V6_MemoryMappedParser().process(Path.of(xmlFile), jdbcUrl);
        bh.consume(summary);
    }

    @Benchmark
    public void v7_CopyProtocol(Blackhole bh) {
        var summary = new V7_CopyProtocol().process(Path.of(xmlFile), jdbcUrl);
        bh.consume(summary);
    }

}
