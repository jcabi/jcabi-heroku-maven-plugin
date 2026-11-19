/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.heroku.maven.plugin;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.velocity.VelocityPage;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.model.Build;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link DeployMojo} (more detailed test is in maven invoker).
 *
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
            ).set("settings", settings)
                .set("xml", StringEscapeUtils.ESCAPE_XML10).toString(),
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
                    new DefaultArtifact(
                        "fooo", "sample", "1.0", "runtime", "jar", "", null
                    )
                )
            ).set("xml", StringEscapeUtils.ESCAPE_XML10).toString(),
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

    @Test
    void escapesServerCredentials() throws Exception {
        final String value = "pa&ss<1>\"'&amp;";
        final Server server = new Server();
        server.setId(value);
        server.setUsername(value);
        server.setPassword(value);
        server.setPassphrase(value);
        final Settings settings = new Settings();
        settings.addServer(server);
        final Server parsed = new SettingsXpp3Reader().read(
            new StringReader(
                new VelocityPage(
                    "com/jcabi/heroku/maven/plugin/settings.xml.vm"
                ).set("settings", settings)
                    .set("xml", StringEscapeUtils.ESCAPE_XML10).toString()
            )
        ).getServers().get(0);
        MatcherAssert.assertThat(
            "all server values must survive XML rendering unchanged",
            Arrays.asList(
                parsed.getId(), parsed.getUsername(),
                parsed.getPassword(), parsed.getPassphrase()
            ),
            Matchers.everyItem(Matchers.equalTo(value))
        );
    }

    @Test
    void escapesProjectValues() throws Exception {
        final String value = "repo&<one>\"'&amp;";
        final String url = "https://example.com/maven?one=1&two=2";
        final Repository repository = new Repository();
        repository.setId(value);
        repository.setUrl(url);
        repository.setLayout(value);
        final MavenProject project = new MavenProject();
        project.setGroupId(value);
        project.setArtifactId(value);
        project.getModel().addRepository(repository);
        final Extension extension = new Extension();
        extension.setGroupId(value);
        extension.setArtifactId(value);
        extension.setVersion(value);
        final Build build = new Build();
        build.addExtension(extension);
        project.setBuild(build);
        final Model parsed = new MavenXpp3Reader().read(
            new StringReader(
                new VelocityPage(
                    "com/jcabi/heroku/maven/plugin/pom.xml.vm"
                ).set("project", project).set("timestamp", "123").set(
                    "deps", Collections.singletonList(
                        new DefaultArtifact(
                            value, value, value, "runtime", value, value, null
                        )
                    )
                ).set("xml", StringEscapeUtils.ESCAPE_XML10).toString()
            )
        );
        MatcherAssert.assertThat(
            "project values must survive XML rendering unchanged",
            Arrays.asList(
                parsed.getGroupId(), parsed.getArtifactId(),
                parsed.getRepositories().get(0).getUrl(),
                parsed.getRepositories().get(0).getId(),
                parsed.getRepositories().get(0).getLayout(),
                parsed.getBuild().getExtensions().get(0).getGroupId(),
                parsed.getBuild().getExtensions().get(0).getArtifactId(),
                parsed.getBuild().getExtensions().get(0).getVersion(),
                parsed.getProfiles().get(1).getDependencies().get(0).getGroupId(),
                parsed.getProfiles().get(1).getDependencies().get(0).getArtifactId(),
                parsed.getProfiles().get(1).getDependencies().get(0).getVersion(),
                parsed.getProfiles().get(1).getDependencies().get(0).getClassifier(),
                parsed.getProfiles().get(1).getDependencies().get(0).getType()
            ),
            Matchers.contains(
                value, String.format("%s-pom", value), url, value, value,
                value, value, value, value, value, value, value, value
            )
        );
    }
}
