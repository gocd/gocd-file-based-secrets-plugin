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

import cd.go.plugin.base.GsonTransformer;
import cd.go.plugin.base.executors.secrets.LookupExecutor;
import cd.go.plugin.secret.filebased.db.SecretsDatabase;
import cd.go.plugin.secret.filebased.model.LookupSecretRequest;
import cd.go.plugin.secret.filebased.util.LRUCache;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.BiFunction;

import static java.util.Collections.synchronizedMap;

public class LookupSecretsRequestExecutor extends LookupExecutor<LookupSecretRequest> {

    private static final int NOT_FOUND_ERROR_CODE = 404;

    private static final int MAX_ENTRIES = 512;

    // cheap cache implementation
    private static final Map<File, CacheEntry> FILE_STAT_CACHE = synchronizedMap(new LRUCache<>(MAX_ENTRIES));

    @Override
    protected GoPluginApiResponse execute(LookupSecretRequest lookupSecretsRequest) {
        List<Map<String, String>> responseList = new ArrayList<>();

        File secretsFile = new File(lookupSecretsRequest.getSecretsFilePath());

        List<String> unresolvedKeys = new ArrayList<>();

        try {
            CacheEntry cacheEntry = FILE_STAT_CACHE.compute(secretsFile, FileCacheEntryCacheEntryBiFunction.INSTANCE);
            SecretsDatabase secretsDatabase = cacheEntry.getSecretsDatabase();

            for (String key : lookupSecretsRequest.getKeys()) {
                String secret = secretsDatabase.getSecret(key);
                if (secret != null) {
                    Map<String, String> response = new HashMap<>();
                    response.put("key", key);
                    response.put("value", secret);
                    responseList.add(response);
                } else {
                    unresolvedKeys.add(key);
                }
            }

            if (unresolvedKeys.isEmpty()) {
                return DefaultGoPluginApiResponse.success(GsonTransformer.toJson(responseList));
            }

            Map<String, String> response = Collections.singletonMap("message", String.format("Secrets with keys %s not found.", unresolvedKeys));
            return new DefaultGoPluginApiResponse(NOT_FOUND_ERROR_CODE, GsonTransformer.toJson(response));
        } catch (IOException e) {
            Map<String, String> errorMessage = Collections.singletonMap("message", "Error while looking up secrets: " + e);
            return DefaultGoPluginApiResponse.error(GsonTransformer.toJson(errorMessage));
        }
    }

    @Override
    protected LookupSecretRequest parseRequest(String body) {
        return LookupSecretRequest.fromJSON(body);
    }

    public static class FileCacheEntryCacheEntryBiFunction implements BiFunction<File, CacheEntry, CacheEntry> {

        public static FileCacheEntryCacheEntryBiFunction INSTANCE = new FileCacheEntryCacheEntryBiFunction();

        private FileCacheEntryCacheEntryBiFunction() {
        }

        @Override
        public CacheEntry apply(File file, CacheEntry existingCacheEntry) {
            if (existingCacheEntry == null) {
                existingCacheEntry = new CacheEntry(file);
            }

            try {
                existingCacheEntry.refresh();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            return existingCacheEntry;
        }
    }
}
