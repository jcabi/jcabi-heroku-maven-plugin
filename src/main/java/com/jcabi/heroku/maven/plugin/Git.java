/**
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
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Git engine.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.4
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "script")
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
     * Location of shell script.
     */
    private final transient String script;

    /**
     * Public ctor.
     * @param key Location of SSH key
     * @param temp Temp directory
     * @throws IOException If some error inside
     */
    public Git(@NotNull final File key,
        @NotNull final File temp) throws IOException {
        if (!new File(Git.SSH).exists()) {
            throw new IllegalStateException(
                String.format("SSH is not installed at '%s'", Git.SSH)
            );
        }
        final File kfile = new File(temp, "heroku.pem");
        FileUtils.copyFile(key, kfile);
        this.chmod(kfile, Git.PERMS);
        final File file = new File(temp, "git-ssh.sh");
        this.script = file.getAbsolutePath();
        FileUtils.writeStringToFile(
            new File(this.script),
            String.format(
                // @checkstyle LineLength (1 line)
                "set -x && %s -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i '%s' $@",
                Git.SSH,
                kfile.getAbsolutePath()
            )
        );
        file.setExecutable(true);
    }

    /**
     * Execute git with these arguments.
     * @param dir In which directory to run it
     * @param args Arguments to pass to it
     * @return Stdout
     * @checkstyle MagicNumber (2 lines)
     */
    @RetryOnFailure(delay = 3000, attempts = 2)
    public String exec(@NotNull final File dir, @NotNull final String... args) {
        final List<String> commands = new ArrayList<String>(args.length + 1);
        commands.add("git");
        for (final String arg : args) {
            commands.add(arg);
        }
        Logger.info(this, "%s:...", StringUtils.join(commands, " "));
        final ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(dir);
        builder.environment().put("GIT_SSH", this.script);
        return new VerboseProcess(builder).stdout();
    }

    /**
     * Change file permissions.
     * @param file The file to change
     * @param mode Permissions to set
     * @throws IOException If some error inside
     * @see http://stackoverflow.com/questions/664432
     * @see http://stackoverflow.com/questions/1556119
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
