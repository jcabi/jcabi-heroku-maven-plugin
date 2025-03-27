/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.heroku.maven.plugin;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import java.io.File;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Heroku platform.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "git", "name" })
final class Heroku {

    /**
     * Git engine.
     */
    private final transient Git git;

    /**
     * Project name in Heroku.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param engine Git engine
     * @param project Project name in Heroku
     */
    public Heroku(@NotNull final Git engine, @NotNull final String project) {
        this.git = engine;
        this.name = project;
    }

    /**
     * Clone repo into local copy.
     * @param path Where to copy
     * @return The repo
     */
    public Repo clone(@NotNull final File path) {
        this.git.exec(
            path.getParentFile(),
            "clone",
            "--verbose",
            String.format("git@heroku.com:%s.git", this.name),
            path.getAbsolutePath()
        );
        Logger.info(
            this,
            "Heroku Git repository '%s' cloned into %s",
            this.name,
            path
        );
        this.git.exec(
            path,
            // @checkstyle MultipleStringLiterals (1 line)
            "config",
            "user.name",
            "jcabi-heroku-maven-plugin"
        );
        this.git.exec(
            path,
            "config",
            "user.email",
            "no-reply@jcabi.com"
        );
        return new Repo(this.git, path);
    }

}
