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
import cd.go.plugin.secret.filebased.util.FileStat;

import java.io.File;
import java.io.IOException;

class CacheEntry {

    private final FileStat fileStat;

    private volatile SecretsDatabase secretsDatabase;

    CacheEntry(File file) {
        this.fileStat = new FileStat(file);
    }

    void refresh() throws IOException {
        if (this.fileStat.changed(5000)) {
            this.secretsDatabase = null;
        }
    }

    // double checked locks
    public SecretsDatabase getSecretsDatabase() throws IOException {
        SecretsDatabase localRef = secretsDatabase;
        if (localRef == null) {
            synchronized (this) {
                localRef = secretsDatabase;
                if (localRef == null) {
                    secretsDatabase = localRef = SecretsDatabase.readFrom(this.fileStat.getFile());
                }
            }
        }
        return localRef;
    }

}
