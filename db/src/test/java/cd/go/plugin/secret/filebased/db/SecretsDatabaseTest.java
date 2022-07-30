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

package cd.go.plugin.secret.filebased.db;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.assertj.core.api.Assertions.assertThat;

class SecretsDatabaseTest {

    @Test
    void shouldAddASecret() throws GeneralSecurityException {
        SecretsDatabase secretsDatabase = new SecretsDatabase();
        secretsDatabase.addSecret("foo", "bar");
        assertThat(secretsDatabase.getSecret("foo")).isEqualTo("bar");

    }

    @Test
    void decryptingASecretShouldCacheIt() throws GeneralSecurityException {
        SecretsDatabase secretsDatabase = new SecretsDatabase();

        secretsDatabase.addSecret("foo", "bar");
        assertThat(secretsDatabase.decryptedSecrets).doesNotContainKeys("foo");

        secretsDatabase.getSecret("foo");
        assertThat(secretsDatabase.decryptedSecrets).containsKeys("foo");
    }

    @Test
    void removingASecretShouldRemoveItFromCache() throws GeneralSecurityException {
        SecretsDatabase secretsDatabase = new SecretsDatabase();

        secretsDatabase.addSecret("foo", "bar");

        // populate cache
        secretsDatabase.getSecret("foo");
        assertThat(secretsDatabase.decryptedSecrets).containsKeys("foo");

        // remove secret
        secretsDatabase.removeSecret("foo");
        assertThat(secretsDatabase.decryptedSecrets).doesNotContainKeys("foo");
    }

    @Test
    void addingASecretShouldRemoveItFromCache() throws GeneralSecurityException {
        SecretsDatabase secretsDatabase = new SecretsDatabase();

        secretsDatabase.addSecret("foo", "bar");

        // populate cache
        secretsDatabase.getSecret("foo");
        assertThat(secretsDatabase.decryptedSecrets).containsKeys("foo");

        // add secret
        secretsDatabase.addSecret("foo", "bar");
        assertThat(secretsDatabase.decryptedSecrets).doesNotContainKeys("foo");
    }

    @Nested
    class Persistance {

        @Test
        void shouldPersistDBToDisk(@TempDir File tempDir) throws GeneralSecurityException, IOException {
            SecretsDatabase secretsDatabase = new SecretsDatabase();

            secretsDatabase.addSecret("foo", "bar");

            secretsDatabase.saveTo(new File(tempDir, "db.json"));

            SecretsDatabase loadedDB = SecretsDatabase.readFrom(new File(tempDir, "db.json"));

            // is able to decrypt
            assertThat(loadedDB.getSecret("foo")).isEqualTo("bar");

            // and saves the passphrase and all secrets
            assertThat(secretsDatabase.getSecretKey()).isEqualTo(loadedDB.getSecretKey());
            assertThat(secretsDatabase.getSecrets()).isEqualTo(loadedDB.getSecrets());
        }
    }
}
