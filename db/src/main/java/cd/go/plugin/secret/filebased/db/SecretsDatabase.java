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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.apache.commons.io.FileUtils.readFileToString;

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

    public SecretsDatabase(String secretKey) {
        this.secretKey = secretKey;
    }

    public SecretsDatabase() throws NoSuchAlgorithmException {
        this(Base64.getEncoder().encodeToString(Cipher.generateKey()));
    }

    public SecretsDatabase addSecret(String name, String value) throws GeneralSecurityException {
        secrets.put(name, Cipher.encrypt(secretKey, value));
        return this;
    }

    public String getSecret(String name) throws BadSecretException, GeneralSecurityException {
        if (secrets.containsKey(name)) {
            return Cipher.decrypt(secretKey, secrets.get(name));
        }
        return null;
    }

    public Set<String> getAllSecretKeys() {
        return secrets.keySet();
    }

    public SecretsDatabase removeSecret(String name) {
        secrets.remove(name);
        return this;
    }

    public static SecretsDatabase readFrom(File secretFile) throws IOException {
        return GSON.fromJson(readFileToString(secretFile, StandardCharsets.UTF_8), SecretsDatabase.class);
    }

    public SecretsDatabase saveTo(File secretFile) throws IOException {
        FileUtils.write(secretFile, toJSON(), StandardCharsets.UTF_8);
        return this;
    }

    public String toJSON() {
        return GSON.toJson(this);
    }
}
