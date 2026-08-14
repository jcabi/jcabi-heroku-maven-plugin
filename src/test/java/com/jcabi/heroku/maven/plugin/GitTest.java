/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.heroku.maven.plugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Git}.
 * @since 0.4
 */
final class GitTest {

    @Test
    @Disabled
    void clonesSimpleGitRepository(@TempDir final Path temp) throws Exception {
        final File folder = temp.resolve("repo").toFile();
        MatcherAssert.assertThat(
            "the repository cannot stay uninitialized",
            new Git(
                Files.writeString(temp.resolve("key.pem"), "").toFile(),
                folder
            ).exec(
                folder.getParentFile(),
                "init",
                temp.resolve("fresh").toString()
            ),
            Matchers.containsString("Initialized empty Git repository")
        );
    }
}
