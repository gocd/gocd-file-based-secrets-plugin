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

import java.io.*;

public class Util {

    public static boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }

    public static void withCapturedSysOut(ThrowingBiConsumer<String, String> throwingBiConsumer) throws Exception {
        PrintStream originalSystemOutStream = System.out;
        PrintStream originalSystemErrorStream = System.err;

        try (
                ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
                PrintStream stdOutStream = new PrintStream(stdoutBuffer, true);

                ByteArrayOutputStream stdErrBuffer = new ByteArrayOutputStream();
                PrintStream stdErrStream = new PrintStream(stdErrBuffer, true)
        ) {
            System.setOut(stdOutStream);
            System.setErr(stdErrStream);
            throwingBiConsumer.accept(stdoutBuffer, stdErrBuffer);
        } finally {
            System.setErr(originalSystemErrorStream);
            System.setOut(originalSystemOutStream);
        }
    }
}
