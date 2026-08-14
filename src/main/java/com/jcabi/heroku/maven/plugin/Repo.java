/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.heroku.maven.plugin;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;

/**
 * Local Git repository.
 * @since 0.4
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "git", "path" })
final class Repo {

    /**
     * Git engine.
     */
    private final transient Git git;

    /**
     * Location of repository.
     */
    private final transient File path;

    /**
     * Public ctor.
     * @param engine Git engine
     * @param file Location of repository
     */
    Repo(@NotNull final Git engine, @NotNull final File file) {
        this.git = engine;
        this.path = file;
    }

    /**
     * Add new file.
     * @param name Name of it
     * @param content Content of the file to write (overwrite)
     * @throws IOException If fails
     */
    void add(@NotNull final String name, @NotNull final String content)
        throws IOException {
        final File file = new File(this.path, name);
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        this.git.exec(this.path, "add", name);
        Logger.info(
            this,
            "File %s updated, %[size]s",
            file,
            file.length()
        );
    }

    /**
     * Commit changes and push.
     * @throws IOException If fails
     */
    void commit() throws IOException {
        this.git.exec(this.path, "status");
        this.git.exec(
            this.path,
            "commit",
            "-am",
            Instant.now().toString()
        );
        this.git.exec(
            this.path,
            "push",
            "origin",
            "master"
        );
        Logger.info(this, "Repository commited to Heroku");
    }
}
