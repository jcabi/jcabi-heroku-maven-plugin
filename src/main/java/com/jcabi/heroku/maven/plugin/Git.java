/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.heroku.maven.plugin;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseProcess;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Git engine.
 * @since 0.4
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "key", "temp" })
final class Git {

    /**
     * Permissions to set to SSH key file.
     */
    @SuppressWarnings("PMD.AvoidUsingOctalValues")
    private static final int PERMS = 0600;

    /**
     * Default SSH location.
     */
    private static final String SSH = "/usr/bin/ssh";

    /**
     * Location of SSH key.
     */
    private final transient File key;

    /**
     * Directory to keep the SSH script in.
     */
    private final transient File temp;

    /**
     * Public ctor.
     * @param pem Location of SSH key
     * @param dir Temp directory
     */
    Git(@NotNull final File pem, @NotNull final File dir) {
        this.key = pem;
        this.temp = dir;
    }

    /**
     * Execute git with these arguments.
     * @param dir In which directory to run it
     * @param args Arguments to pass to it
     * @return Stdout
     * @throws IOException If some error inside
     */
    @RetryOnFailure(delay = 3000, attempts = 2)
    String exec(@NotNull final File dir, @NotNull final String... args)
        throws IOException {
        final List<String> commands = new ArrayList<>(args.length + 1);
        commands.add("git");
        commands.addAll(Arrays.asList(args));
        Logger.info(this, "%s:...", StringUtils.join(commands, " "));
        final ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(dir);
        builder.environment().put("GIT_SSH", this.script());
        return new VerboseProcess(builder).stdout();
    }

    /**
     * Make a shell script that teaches Git to use our SSH key.
     * @return Absolute location of the script
     * @throws IOException If some error inside
     */
    private String script() throws IOException {
        if (!new File(Git.SSH).exists()) {
            throw new IllegalStateException(
                String.format("SSH is not installed at '%s'", Git.SSH)
            );
        }
        final File pem = new File(this.temp, "heroku.pem");
        FileUtils.copyFile(this.key, pem);
        this.chmod(pem, Git.PERMS);
        final File file = new File(this.temp, "git-ssh.sh");
        FileUtils.writeStringToFile(
            file,
            String.format(
                "set -x && %s -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i '%s' $@",
                Git.SSH,
                pem.getAbsolutePath()
            ),
            StandardCharsets.UTF_8
        );
        file.setExecutable(true);
        return file.getAbsolutePath();
    }

    /**
     * Change file permissions.
     * @param file The file to change
     * @param mode Permissions to set
     * @throws IOException If some error inside
     */
    private void chmod(final File file, final int mode) throws IOException {
        new VerboseProcess(
            new ProcessBuilder(
                "chmod",
                String.format("%04o", mode),
                file.getAbsolutePath()
            )
        ).stdout();
        Logger.debug(
            this,
            "chmod(%s, %3o): succeeded",
            file,
            mode
        );
    }
}
