/*
 * Copyright 2022 Thoughtworks, Inc.
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

package cd.go.plugin.secret.filebased.executors;

import cd.go.plugin.secret.filebased.db.SecretsDatabase;
import cd.go.plugin.secret.filebased.util.LRUCache;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.synchronizedMap;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms512m", "-Xmx512m"})
@Warmup(iterations = 2, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
public class SecretsDatabasePerformanceBenchmark {

    private final Map<File, CacheEntry> fileStatCache = synchronizedMap(new LRUCache<>(3));

    private SecretsDatabase secretsDatabase;

    private File tempFile;

    @Setup
    public void setup() throws GeneralSecurityException, IOException {
        tempFile = new File("tmp.secrets.db");
        tempFile.deleteOnExit();

        secretsDatabase = new SecretsDatabase();
        secretsDatabase.addSecret("foo", "bar");

        secretsDatabase.saveTo(tempFile);
    }

    @Benchmark
    public void readDatabaseEachTime(Blackhole bh) throws NoSuchAlgorithmException, IOException {
        SecretsDatabase secretsDatabase = SecretsDatabase.readFrom(tempFile);
        bh.consume(secretsDatabase.getSecret("foo"));
    }

    @Benchmark
    public void readDatabaseFromCache(Blackhole bh) {
        bh.consume(secretsDatabase.getSecret("foo"));
    }

    @Benchmark
    public void readDatabaseThroughFileStatCache(Blackhole bh) throws IOException {
        bh.consume(fileStatCache
                .compute(tempFile, LookupSecretsRequestExecutor.FileCacheEntryCacheEntryBiFunction.INSTANCE)
                .getSecretsDatabase()
                .getSecret("foo"));
    }
}
