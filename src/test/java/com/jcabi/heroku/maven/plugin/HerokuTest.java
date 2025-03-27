/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.heroku.maven.plugin;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for {@link Heroku}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class HerokuTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Heroku can execute simple git command.
     * @throws Exception If something is wrong
     */
    @Test
    public void clonesSimpleHerokuRepository() throws Exception {
        final File key = this.temp.newFile();
        FileUtils.writeStringToFile(
            key,
            IOUtils.toString(this.getClass().getResource("test-key.pem"))
        );
        try {
            new Heroku(
                new Git(key, this.temp.newFolder()),
                "jcabi"
            ).clone(this.temp.newFolder());
            Assert.fail("exception was expected");
        } catch (final IllegalArgumentException ex) {
            MatcherAssert.assertThat(
                ex.getMessage(),
                Matchers.containsString("Non-zero exit code ")
            );
        }
    }

}
