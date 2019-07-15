/*
 * Copyright 2019 ThoughtWorks, Inc.
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

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CipherTest {

    @Nested
    class GenerateKey {

        @Test
        void shouldGenerate128BitKey() throws NoSuchAlgorithmException {
            assertThat(Cipher.generateKey())
                    .hasSize(128 / 8);
        }

        @Test
        void shouldGenerateRandomKeyEveryTime() throws NoSuchAlgorithmException {
            assertThat(Cipher.generateKey())
                    .isNotEqualTo(Cipher.generateKey());
        }
    }

    @Nested
    class Encrypt {

        @Test
        void shouldEncrypt() throws GeneralSecurityException {
            String clearText = "foo";

            String key = Base64.getEncoder().encodeToString(Cipher.generateKey());

            assertThat(Cipher.encrypt(key, clearText))
                    .doesNotContain(clearText);
        }

        @Test
        void shouldEncryptToDifferentValueEveryTime() throws GeneralSecurityException {
            String clearText = "foo";

            String key = Base64.getEncoder().encodeToString(Cipher.generateKey());

            assertThat(Cipher.encrypt(key, clearText))
                    .isNotEqualTo(Cipher.encrypt(key, clearText));
        }
    }

    @Nested
    class Decrypt {

        @Test
        void shouldDecrypt() throws GeneralSecurityException, BadSecretException {
            String clearText = "foo";

            String key = Base64.getEncoder().encodeToString(Cipher.generateKey());

            String encryptedValue = Cipher.encrypt(key, clearText);

            assertThat(Cipher.decrypt(key, encryptedValue)).isEqualTo(clearText);
        }

        @Test
        void shouldFailIfEncryptedValueIsTampered() throws GeneralSecurityException {
            String key = Base64.getEncoder().encodeToString(Cipher.generateKey());

            assertThatCode(() -> Cipher.decrypt(key, "junk")).hasMessage("Bad cipher text")
                    .isInstanceOf(BadSecretException.class);
        }
    }
}
