/**
 * Copyright (c) 2012, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.heroku.maven.plugin;

import com.jcabi.log.Logger;
import java.io.File;

/**
 * Heroku platform.
 *
 * @author Yegor Bugayenko (yegor@jcabi.com)
 * @version $Id$
 * @since 0.4
 */
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
    public Heroku(final Git engine, final String project) {
        this.git = engine;
        this.name = project;
    }

    /**
     * Clone repo into local copy.
     * @param path Where to copy
     * @return The repo
     */
    public Repo clone(final File path) {
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
