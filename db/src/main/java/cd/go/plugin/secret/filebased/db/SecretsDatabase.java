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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Set;

public class SecretsDatabase {

    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    @Expose
    @SerializedName("secret_key")
    private final String secretKey;

    @Expose
    @SerializedName("secrets")
    private final LinkedHashMap<String, String> secrets = new LinkedHashMap<>();

    final LinkedHashMap<String, String> decryptedSecrets = new LinkedHashMap<>();

    public SecretsDatabase(String secretKey) {
        this.secretKey = secretKey;
    }

    public SecretsDatabase() throws NoSuchAlgorithmException {
        this(Base64.getEncoder().encodeToString(Cipher.generateKey()));
    }

    public SecretsDatabase addSecret(String name, String value) throws GeneralSecurityException {
        synchronized (this) {
            secrets.put(name, Cipher.encrypt(secretKey, value));
            decryptedSecrets.remove(name);
        }
        return this;
    }

    public String getSecret(String name) {
        synchronized (this) {
            return decryptedSecrets.computeIfAbsent(name, key -> {
                if (secrets.containsKey(name)) {
                    try {
                        return Cipher.decrypt(secretKey, secrets.get(name));
                    } catch (BadSecretException | GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                }
                return null;
            });
        }
    }

    public Set<String> getAllSecretKeys() {
        return secrets.keySet();
    }

    public SecretsDatabase removeSecret(String name) {
        synchronized (this) {
            secrets.remove(name);
            decryptedSecrets.remove(name);
        }
        return this;
    }

    public static SecretsDatabase readFrom(File secretFile) throws IOException {
        return GSON.fromJson(Files.readString(secretFile.toPath(), StandardCharsets.UTF_8), SecretsDatabase.class);
    }

    public SecretsDatabase saveTo(File secretFile) throws IOException {
        Files.writeString(secretFile.toPath(), toJSON(), StandardCharsets.UTF_8);
        return this;
    }

    public String toJSON() {
        return GSON.toJson(this);
    }


    // for testing
    String getSecretKey() {
        return secretKey;
    }

    LinkedHashMap<String, String> getSecrets() {
        return secrets;
    }
}
