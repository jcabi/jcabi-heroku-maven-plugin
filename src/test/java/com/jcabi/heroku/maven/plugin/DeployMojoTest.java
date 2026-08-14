/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.heroku.maven.plugin;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.velocity.VelocityPage;
import java.util.Collections;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.model.Build;
import org.apache.maven.model.Extension;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link DeployMojo} (more detailed test is in maven invoker).
 * @since 0.4
 */
final class DeployMojoTest {

    @Test
    void skipsExecutionWhenRequired() {
        final DeployMojo mojo = new DeployMojo();
        mojo.setSkip(true);
        Assertions.assertDoesNotThrow(
            mojo::execute,
            "skipped execution cannot fail"
        );
    }

    @Test
    void velocityTemplateCorrectlyBuildsSettingsXml() {
        final Server server = new Server();
        server.setUsername("john");
        server.setPassword("xxx");
        final Settings settings = new Settings();
        settings.addServer(server);
        final String nspace = "http://maven.apache.org/SETTINGS/1.0.0";
        MatcherAssert.assertThat(
            "the server credentials cannot be lost in settings.xml",
            new VelocityPage(
                "com/jcabi/heroku/maven/plugin/settings.xml.vm"
            ).set("settings", settings).toString(),
            Matchers.allOf(
                XhtmlMatchers.hasXPath(
                    "//ns1:server[ns1:username='john' and ns1:password='xxx']",
                    nspace
                ),
                XhtmlMatchers.hasXPath(
                    "//ns1:server[ns1:username='john' and not(ns1:privateKey)]",
                    nspace
                )
            )
        );
    }

    @Test
    void velocityTemplateCorrectlyBuildsPomXml() {
        final Build build = new Build();
        final Extension ext = new Extension();
        ext.setArtifactId("test-foo");
        build.addExtension(ext);
        final MavenProject project = new MavenProject();
        project.setBuild(build);
        final String nspace = "http://maven.apache.org/POM/4.0.0";
        MatcherAssert.assertThat(
            "the project details cannot be lost in pom.xml",
            new VelocityPage(
                "com/jcabi/heroku/maven/plugin/pom.xml.vm"
            ).set("project", project).set("timestamp", "332211").set(
                "deps",
                Collections.singletonList(
                    new DefaultArtifact("fooo", "", "", "", "", "", null)
                )
            ).toString(),
            Matchers.allOf(
                XhtmlMatchers.hasXPath(
                    "//ns1:name[.='332211']",
                    nspace
                ),
                XhtmlMatchers.hasXPath(
                    "//ns1:extension[ns1:artifactId='test-foo']",
                    nspace
                ),
                XhtmlMatchers.hasXPath(
                    "//ns1:dependency[ns1:groupId='fooo']",
                    nspace
                ),
                XhtmlMatchers.hasXPath(
                    "//ns1:configuration[ns1:outputDirectory='${basedir}']",
                    nspace
                )
            )
        );
    }
}
