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

package cd.go.plugin.secret.filebased.cli.args;

import cd.go.plugin.secret.filebased.db.BadSecretException;
import cd.go.plugin.secret.filebased.db.SecretsDatabase;
import com.beust.jcommander.Parameters;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.function.Consumer;

@Parameters(commandDescription = "Removes given secret.", commandNames = "remove")
public class RemoveSecretArgs extends HasNameArgs {
    public void execute(Consumer<Integer> exitter) throws IOException, BadSecretException, GeneralSecurityException {
        SecretsDatabase secretsDatabase = SecretsDatabase.readFrom(databaseFile);

        if (secretsDatabase.getSecret(key) != null) {
            secretsDatabase
                    .removeSecret(key)
                    .saveTo(databaseFile);
            System.err.println("Removed secret named " + key + ".");
        } else {
            System.err.println("Secret named " + key + " was not found.");
        }
    }
}
