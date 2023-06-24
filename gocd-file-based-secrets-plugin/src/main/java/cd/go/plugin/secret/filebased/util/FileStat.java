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

package cd.go.plugin.secret.filebased.util;

import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FileStat {

    private final File file;

    private long lastStatTime;

    private boolean exists;

    private long lastModified;

    private boolean directory;

    private long length;

    private List<Byte> digest;

    @Getter(lazy = true)
    // lazy initialize, because performance
    private final MessageDigest messageDigest = createDigester();

    public FileStat(File file) {
        this.file = file;
    }

    public boolean changed(int withinInterval) {
        if (System.currentTimeMillis() <= lastStatTime + withinInterval) {
            return false;
        }
        lastStatTime = System.currentTimeMillis();
        // cache original values
        final boolean origExists = exists;
        final long origLastModified = lastModified;
        final boolean origDirectory = directory;
        final long origLength = length;
        final List<Byte> origDigest = digest;

        // refresh the values
        refresh();

        // check if any values have changed
        return exists != origExists ||
                lastModified != origLastModified ||
                directory != origDirectory ||
                length != origLength ||
                !digest.equals(origDigest);
    }

    void refresh() {
        exists = file.exists();
        directory = exists && file.isDirectory();
        lastModified = exists ? file.lastModified() : 0;
        length = (exists && !directory) ? file.length() : 0;
        digest = (exists && !directory) ? computeDigest() : Collections.emptyList();
    }

    private List<Byte> computeDigest() {
        MessageDigest messageDigest = getMessageDigest();
        messageDigest.reset();
        try {
            Files.copy(file.toPath(), new DigestOutputStream(OutputStream.nullOutputStream(), messageDigest));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        byte[] digest = messageDigest.digest();
        return IntStream.range(0, digest == null ? 0 : digest.length)
                .mapToObj( i -> Byte.valueOf(digest[i]))
                .collect(Collectors.toList());
    }

    private static MessageDigest createDigester() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }

    public File getFile() {
        return file;
    }
}
