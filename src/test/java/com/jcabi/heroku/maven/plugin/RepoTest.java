/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.heroku.maven.plugin;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for {@link Repo}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class RepoTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Repo can add files to Git repo.
     * @throws Exception If something is wrong
     */
    @Test
    public void addsFilesToSimpleGitRepo() throws Exception {
        final File key = this.temp.newFile();
        FileUtils.writeStringToFile(key, "");
        final File folder = this.temp.newFolder();
        final Git git = new Git(key, folder);
        git.exec(
            this.temp.newFolder(),
            "init",
            folder.getPath()
        );
        final Repo repo = new Repo(git, folder);
        final String name = "extra.txt";
        repo.add(name, "\u0433 text content!");
        MatcherAssert.assertThat(
            new File(folder, name).exists(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            FileUtils.readFileToString(
                new File(folder, name), CharEncoding.UTF_8
            ),
            Matchers.startsWith("\u0433 text content")
        );
    }

}
