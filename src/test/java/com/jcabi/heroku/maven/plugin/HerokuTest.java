/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.heroku.maven.plugin;

import java.io.File;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link Heroku}.
 * @since 0.4
 */
final class HerokuTest {

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void clonesSimpleHerokuRepository(@TempDir final Path temp)
        throws Exception {
        final File key = temp.resolve("key.pem").toFile();
        FileUtils.copyURLToFile(
            HerokuTest.class.getResource("test-key.pem"),
            key
        );
        MatcherAssert.assertThat(
            "the failure of the clone cannot be reported differently",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new Heroku(
                    new Git(key, temp.resolve("git").toFile()),
                    "jcabi"
                ).clone(temp.resolve("clone").toFile()),
                "the clone of an unreachable repository cannot succeed"
            ).getMessage(),
            Matchers.containsString("Non-zero exit code ")
        );
    }
}
