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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Parameters(commandDescription = "Returns value for given secret.", commandNames = "show")
public class ShowSecretArgs extends HasNameArgs {
    public void execute(Consumer<Integer> exitter) throws IOException, BadSecretException, GeneralSecurityException {
        String secret = SecretsDatabase.readFrom(databaseFile).getSecret(key);

        if (secret != null) {
            System.out.println(secret);
        } else {
            System.err.println("Secret named " + key + " was not found.");
            exitter.accept(-1);
        }
    }
}
