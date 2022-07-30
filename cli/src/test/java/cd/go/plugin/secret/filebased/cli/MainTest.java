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

package cd.go.plugin.secret.filebased.cli;

import cd.go.plugin.secret.filebased.db.SecretsDatabase;
import cd.go.plugin.secret.filebased.db.Util;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


class MainTest {

    @Mock
    Consumer<Integer> dummyExitter = mock(Consumer.class);

    @Nested
    class WithoutArguments {
        @Test
        void shouldPrintUsageAndExitWithBadStatusWhenNoOptionIsProvided() throws Exception {
            Util.withCapturedSysOut((out, err) -> {
                new Main().run(dummyExitter);
                assertThat(err.toString()).startsWith("Usage: java -jar <path.to.plugin.jar.file> [options] [command] [command options]");
                assertThat(out.toString()).isEmpty();
                verify(dummyExitter).accept(1);
            });
        }

        @Test
        void shouldPrintUsageAndExitWithBadStatusWhenHelpOptionIsProvided() throws Exception {
            Util.withCapturedSysOut((out, err) -> {
                new Main("-h").run(dummyExitter);
                assertThat(err.toString()).startsWith("Usage: java -jar <path.to.plugin.jar.file> [options] [command] [command options]");
                assertThat(out.toString()).isEmpty();
                verify(dummyExitter).accept(1);
            });
        }
    }

    @Nested
    class Initialize {
        @Test
        void shouldInitializeSecretsDatabaseAndExitWithZero(@TempDir Path tempDirectory) throws Exception {
            Util.withCapturedSysOut((out, err) -> {
                File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));

                assertThat(databaseFile).doesNotExist();

                new Main("init", "-f", databaseFile.getAbsolutePath()).run(dummyExitter);

                assertThat(databaseFile).exists();
                assertThat(err.toString()).isEqualToIgnoringNewLines("Initialized secret database file in " + databaseFile.getAbsolutePath());
                assertThat(out.toString()).isEmpty();
                verifyNoMoreInteractions(dummyExitter);
            });
        }
    }

    @Nested
    class AddSecret {
        @Test
        void shouldBlowUpIfSecretsFileDoesNotExist(@TempDir Path tempDirectory) throws Exception {
            Util.withCapturedSysOut((out, err) -> {

                File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));

                assertThat(databaseFile).doesNotExist();

                new Main("add", "-f", databaseFile.getAbsolutePath(), "-n", "password", "-v", "p@ssw0rd").run(dummyExitter);
                assertThat(out.toString()).isEmpty();
                assertThat(err.toString()).contains("FileNotFoundException");
                assertThat(err.toString()).contains(databaseFile.getAbsolutePath());
                verify(dummyExitter).accept(-1);
            });
        }

        @Test
        void shouldUpdateExistingSecretIfItAlreadyExists(@TempDir Path tempDirectory) throws Exception {
            File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));
            new SecretsDatabase().addSecret("ssh-key", "some-ssh-key").saveTo(databaseFile);

            Util.withCapturedSysOut((out, err) -> {
                new Main("add", "-f", databaseFile.getAbsolutePath(), "-n", "ssh-key", "-v", "new-ssh-key").run(dummyExitter);
                assertThat(out.toString()).isEmpty();
                assertThat(err.toString()).isEqualToIgnoringNewLines("Added secret named ssh-key.");
                verifyNoMoreInteractions(dummyExitter);
            });

            assertThat(SecretsDatabase.readFrom(databaseFile).getSecret("ssh-key")).isEqualTo("new-ssh-key");
        }

        @Test
        void shouldAddANewSecretIfItDoesNotExists(@TempDir Path tempDirectory) throws Exception {
            File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));
            new SecretsDatabase().addSecret("ssh-key", "some-ssh-key").saveTo(databaseFile);

            Util.withCapturedSysOut((out, err) -> {
                new Main("add", "-f", databaseFile.getAbsolutePath(), "-n", "new-ssh-key", "-v", "foobar-key").run(dummyExitter);
                assertThat(out.toString()).isEmpty();
                assertThat(err.toString()).isEqualToIgnoringNewLines("Added secret named new-ssh-key.");
                verifyNoMoreInteractions(dummyExitter);
            });

            assertThat(SecretsDatabase.readFrom(databaseFile).getSecret("ssh-key")).isEqualTo("some-ssh-key");
            assertThat(SecretsDatabase.readFrom(databaseFile).getSecret("new-ssh-key")).isEqualTo("foobar-key");
        }
    }

    @Nested
    class LookupSecret {
        @Test
        void shouldLookupSecret(@TempDir Path tempDirectory) throws Exception {
            File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));
            new SecretsDatabase()
                    .addSecret("username", "foo")
                    .addSecret("password", "bar")
                    .saveTo(databaseFile);

            Util.withCapturedSysOut((out, err) -> {
                new Main("show", "-f", databaseFile.getAbsolutePath(), "-n", "username").run(dummyExitter);
                assertThat(out.toString()).isEqualToIgnoringNewLines("foo");
                assertThat(err.toString()).isEmpty();
                verifyNoMoreInteractions(dummyExitter);
            });
        }

        @Test
        void shouldPrintNotFoundMessageWhenSecretIsNotPresent(@TempDir Path tempDirectory) throws Exception {
            File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));
            new SecretsDatabase()
                    .saveTo(databaseFile);

            Util.withCapturedSysOut((out, err) -> {
                new Main("show", "-f", databaseFile.getAbsolutePath(), "-n", "deploy-key").run(dummyExitter);
                assertThat(out.toString()).isEmpty();
                assertThat(err.toString()).isEqualToIgnoringNewLines("Secret named deploy-key was not found.");
                verify(dummyExitter).accept(-1);
            });
        }

        @Test
        void shouldPrintErrorWhenSecretsFileDoesNotExists(@TempDir Path tempDirectory) throws Exception {
            File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));

            Util.withCapturedSysOut((out, err) -> {
                new Main("show", "-f", databaseFile.getAbsolutePath(), "-n", "deploy-key").run(dummyExitter);
                assertThat(out.toString()).isEmpty();
                assertThat(err.toString())
                        .contains("FileNotFoundException")
                        .contains(databaseFile.getAbsolutePath());
                verify(dummyExitter).accept(-1);
            });
        }
    }


    @Nested
    class LookupAllKeys {
        @Test
        void shouldLookupSecretKeys(@TempDir Path tempDirectory) throws Exception {
            File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));
            new SecretsDatabase()
                    .addSecret("username", "foo")
                    .addSecret("password", "bar")
                    .saveTo(databaseFile);

            Util.withCapturedSysOut((out, err) -> {
                new Main("keys", "-f", databaseFile.getAbsolutePath()).run(dummyExitter);
                assertThat(out.toString()).isEqualToIgnoringNewLines("[username, password]");
                verifyNoMoreInteractions(dummyExitter);
            });
        }

        @Test
        void shouldPrintNoKeysMessageWhenSecretsAreNotPresent(@TempDir Path tempDirectory) throws Exception {
            File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));
            new SecretsDatabase()
                    .saveTo(databaseFile);

            Util.withCapturedSysOut((out, err) -> {
                new Main("keys", "-f", databaseFile.getAbsolutePath()).run(dummyExitter);
                assertThat(out.toString()).isEmpty();
                assertThat(err.toString()).isEqualToIgnoringNewLines("There are no secrets in the secrets database file.");
                verify(dummyExitter).accept(-1);
            });
        }
    }

    @Nested
    class DeleteSecret {
        @Test
        void shouldDeleteSecret(@TempDir Path tempDirectory) throws Exception {
            File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));
            new SecretsDatabase()
                    .addSecret("ssh-key-1", "some-ssh-key-1")
                    .addSecret("ssh-key-2", "some-ssh-key-2")
                    .saveTo(databaseFile);

            Util.withCapturedSysOut((out, err) -> {
                new Main("remove", "-f", databaseFile.getAbsolutePath(), "-n", "ssh-key-2").run(dummyExitter);
                assertThat(out.toString()).isEmpty();
                assertThat(err.toString()).isEqualToIgnoringNewLines("Removed secret named ssh-key-2.");
                verifyNoMoreInteractions(dummyExitter);
            });

            assertThat(SecretsDatabase.readFrom(databaseFile).getSecret("ssh-key-1")).isEqualTo("some-ssh-key-1");
            assertThat(SecretsDatabase.readFrom(databaseFile).getSecret("ssh-key-2")).isNull();
        }

        @Test
        void shouldPrintErrorSecretWithGivenKeyDoesNotExist(@TempDir Path tempDirectory) throws Exception {
            File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));
            new SecretsDatabase()
                    .saveTo(databaseFile);

            Util.withCapturedSysOut((out, err) -> {
                new Main("remove", "-f", databaseFile.getAbsolutePath(), "-n", "foobar").run(dummyExitter);
                assertThat(out.toString()).isEmpty();
                assertThat(err.toString()).isEqualToIgnoringNewLines("Secret named foobar was not found.");
                verifyNoMoreInteractions(dummyExitter);
            });
        }

        @Test
        void shouldPrintErrorWhenSecretsFileDoesNotExists(@TempDir Path tempDirectory) throws Exception {
            File databaseFile = new File(tempDirectory.toFile(), UUID.randomUUID().toString().substring(0, 8));

            Util.withCapturedSysOut((out, err) -> {
                new Main("remove", "-f", databaseFile.getAbsolutePath(), "-n", "deploy-key").run(dummyExitter);
                assertThat(out.toString()).isEmpty();
                assertThat(err.toString())
                        .contains("FileNotFoundException")
                        .contains(databaseFile.getAbsolutePath());
                verify(dummyExitter).accept(-1);
            });
        }
    }
}
