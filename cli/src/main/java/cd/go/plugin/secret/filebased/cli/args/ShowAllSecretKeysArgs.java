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

package cd.go.plugin.secret.filebased.cli.args;

import cd.go.plugin.secret.filebased.db.BadSecretException;
import cd.go.plugin.secret.filebased.db.SecretsDatabase;
import com.beust.jcommander.Parameters;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Parameters(commandDescription = "Returns all secret keys", commandNames = "keys")
public class ShowAllSecretKeysArgs extends DatabaseFileArgs {
    public void execute(Consumer<Integer> exitter) throws IOException, BadSecretException, GeneralSecurityException {
        Set<String> secretKeys = SecretsDatabase.readFrom(databaseFile).getAllSecretKeys();

        if (!secretKeys.isEmpty()) {
            System.out.println(secretKeys);
        } else {
            System.err.println("There are no secrets in the secrets database file.");
            exitter.accept(-1);
        }
    }
}
