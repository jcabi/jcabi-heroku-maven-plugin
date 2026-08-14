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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Repo}.
 * @since 0.4
 */
final class RepoTest {

    @Test
    void addsFilesToSimpleGitRepo(@TempDir final Path temp) throws Exception {
        final File folder = temp.resolve("repo").toFile();
        final Git git = new Git(
            Files.writeString(temp.resolve("key.pem"), "").toFile(),
            folder
        );
        git.exec(temp.toFile(), "init", folder.getPath());
        final String name = "extra.txt";
        new Repo(git, folder).add(name, "г text content!");
        MatcherAssert.assertThat(
            "the file cannot lose its content on the way to the repo",
            Files.readString(folder.toPath().resolve(name)),
            Matchers.startsWith("г text content")
        );
    }
}
