/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.heroku.maven.plugin;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;

/**
 * Local Git repository.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
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
    private final transient String path;

    /**
     * Public ctor.
     * @param engine Git engine
     * @param file Location of repository
     */
    public Repo(@NotNull final Git engine, @NotNull final File file) {
        this.git = engine;
        this.path = file.getAbsolutePath();
    }

    /**
     * Add new file.
     * @param name Name of it
     * @param content Content of the file to write (overwrite)
     * @throws IOException If fails
     */
    public void add(@NotNull final String name, @NotNull final String content)
        throws IOException {
        final File dir = new File(this.path);
        final File file = new File(dir, name);
        FileUtils.writeStringToFile(file, content, CharEncoding.UTF_8);
        this.git.exec(dir, "add", name);
        Logger.info(
            this,
            "File %s updated, %[size]s",
            file,
            file.length()
        );
    }

    /**
     * Commit changes and push.
     */
    public void commit() {
        final File dir = new File(this.path);
        this.git.exec(dir, "status");
        this.git.exec(
            dir,
            "commit",
            "-am",
            new Date().toString()
        );
        this.git.exec(
            dir,
            "push",
            "origin",
            "master"
        );
        Logger.info(this, "Repository commited to Heroku");
    }

}
