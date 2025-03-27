/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.heroku.maven.plugin;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for {@link Git}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class GitTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Git can execute simple git command.
     * @throws Exception If something is wrong
     */
    @Test
    @org.junit.Ignore
    public void clonesSimpleGitRepository() throws Exception {
        final File key = this.temp.newFile();
        FileUtils.writeStringToFile(key, "");
        final File folder = this.temp.newFolder();
        final Git git = new Git(key, folder);
        MatcherAssert.assertThat(
            git.exec(
                folder.getParentFile(),
                "init",
                this.temp.newFolder().getPath()
            ),
            Matchers.containsString("Initialized empty Git repository")
        );
    }

}
