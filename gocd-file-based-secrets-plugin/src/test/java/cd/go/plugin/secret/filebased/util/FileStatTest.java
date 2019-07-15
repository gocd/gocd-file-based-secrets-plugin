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

package cd.go.plugin.secret.filebased.util;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class FileStatTest {

    @Nested
    class Changed_method {

        @Test
        void shouldStatANonExistantFile(@TempDir File tempDir) {
            File file = new File(tempDir, UUID.randomUUID().toString());

            FileStat fileStat = new FileStat(file);
            assertThat(fileStat.changed(0)).isTrue();
            assertThat(fileStat.changed(0)).isFalse();
        }

        @Test
        void shouldReturnTrueIfFileHasBeenModifiedWithinAnInterval(@TempDir File tempDir) throws InterruptedException, IOException {
            File file = new File(tempDir, UUID.randomUUID().toString());
            assertThat(file.createNewFile()).isTrue();

            FileStat fileStat = new FileStat(file);
            fileStat.refresh();

            assertThat(fileStat.changed(0)).isFalse();
            Thread.sleep(1500);

            assertThat(file.setLastModified(System.currentTimeMillis())).isTrue();

            assertThat(fileStat.changed(2000)).isFalse();

            assertThat(fileStat.changed(1000)).isTrue();
        }

        @Test
        void shouldReturnTrueIfFileContentsChanged(@TempDir File tempDir) throws IOException, InterruptedException {
            File file = new File(tempDir, UUID.randomUUID().toString());
            assertThat(file.createNewFile()).isTrue();

            FileStat fileStat = new FileStat(file);
            fileStat.refresh();

            assertThat(fileStat.changed(0)).isFalse();
            FileUtils.writeStringToFile(file, "foo", UTF_8);
            Thread.sleep(100);
            assertThat(fileStat.changed(110)).isFalse();
        }
    }
}
