package com.tvdrop.app.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UploadFileNamesTest {
    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test public void rejectsPathsAndControlCharacters() {
        String[] unsafe = {"../outside.apk", "..\\outside.apk", "/outside.apk", ".", "..", "", "a\u0000.apk"};
        for (String name : unsafe) {
            try {
                UploadFileNames.INSTANCE.requireSafe(name);
                fail("Accepted unsafe name: " + name);
            } catch (IllegalArgumentException expected) {
                // Expected.
            }
        }
    }

    @Test public void reservesDifferentNamesWithoutOverwriting() throws Exception {
        File directory = temporaryFolder.newFolder("uploads");
        File first = UploadFileNames.INSTANCE.reserveUniqueFile(directory, "app.apk");
        Files.write(first.toPath(), "original".getBytes(StandardCharsets.UTF_8));
        File second = UploadFileNames.INSTANCE.reserveUniqueFile(directory, "app.apk");

        assertEquals("app.apk", first.getName());
        assertEquals("app (1).apk", second.getName());
        assertEquals("original", new String(Files.readAllBytes(first.toPath()), StandardCharsets.UTF_8));
        assertTrue(second.exists());
        assertEquals(directory.getCanonicalFile(), second.getCanonicalFile().getParentFile());
    }
}
